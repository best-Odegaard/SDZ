package com.saidazi.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.saidazi.entity.Competition;
import com.saidazi.mapper.CompetitionMapper;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 广东理工学院教务处「学科竞赛」栏目爬虫定时任务。
 *
 * 抓取流程：
 * 1. 从第 1 页入口 https://jwc.gdlgxy.edu.cn/sjjx/xkjs.htm 开始，跟随列表页底部「下页」链接逐页翻页。
 *    注意该站翻页地址是倒序生成的：第 1 页是 /sjjx/xkjs.htm，第 N（N≥2）页是
 *    /sjjx/xkjs/{总页数+1-N}.htm（例如共 13 页时第 2 页是 12.htm，最后一页是 1.htm），
 *    所以不能靠页码拼地址，只能从翻页控件里取链接。
 * 2. 始终从第 1 页开始全量爬取（START_PAGE=1），逐页翻到最后一页，防止漏掉最新通知；
 *    即使每次全量抓取，也依赖第 7 步的 source_url 去重避免产生重复数据。
 * 3. 标题白名单过滤：只有标题包含「通知 / 关于开展 / 关于举办」之一才抓取，
 *    其余文章（喜报、获奖新闻等）一律跳过，不进详情页、不入库。
 * 4. 解析文章标题 + 详情页链接（该站为 Visual SiteBuilder 系统，文章位于 div.doc-item 内）
 * 5. 逐篇进入详情页，自动探测正文容器（#vsb_content / .v_news_content / .content 等，取文本最长者）
 * 6. 用正则提取发布时间；解析不到时回退当前时间，保证 publish_time 不为空，可按时间倒序取到最新数据
 * 7. 按 source_url 去重后写入 competition 表（type=0 校内、organizer=教务处、status=1 已发布）
 *
 * 依赖说明：去重依赖 Competition 实体的 sourceUrl 字段与 competition 表的 source_url 列。
 * 若你的实体/表还没有该字段，请先补充：
 *   实体字段：private String sourceUrl;
 *   表列：    ALTER TABLE competition ADD COLUMN source_url VARCHAR(512) COMMENT '来源链接（爬虫去重）';
 */
@Slf4j
@Component
public class CompetitionCrawlTask {

    /**
     * 列表入口地址 = 本栏目第 1 页（最新页）。
     * 后续页码从这里跟着翻页控件的「下页」链接走，不硬编码某一页的文件名。
     */
    private static final String LIST_PAGE_URL = "https://jwc.gdlgxy.edu.cn/sjjx/xkjs.htm";

    /**
     * 起始页码：固定从第 1 页（最新页）开始抓取，防止漏掉最新通知。
     * 全量爬取配合 source_url 去重，即使重复抓取也不会产生重复数据。
     */
    private static final int START_PAGE = 1;

    /** 结束页码：0 表示一直抓到最后页；设为具体值可限制抓取范围，例如 5 表示只抓到第 5 页 */
    private static final int END_PAGE = 0;

    /** 最多翻页数，防止站点翻页控件异常时死循环 */
    private static final int MAX_PAGES = 50;

    /**
     * 标题白名单：只有当标题包含以下任一关键词时才抓取入库，其余（喜报、获奖新闻等）一律跳过。
     * 「通知」覆盖绝大多数竞赛通知；「关于开展」「关于举办」兜底那些标题不以“通知”结尾的竞赛公告。
     */
    private static final String[] INCLUDE_TITLE_KEYWORDS = {
            "通知", "关于开展", "关于举办"
    };

    private static final String USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 "
                    + "(KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36";

    /** 单次请求超时时间（毫秒） */
    private static final int REQUEST_TIMEOUT_MS = 15_000;

    /** 每抓一篇详情页后暂停 1 秒，降低被封风险 */
    private static final long SLEEP_MS = 1_000L;

    private static final int TYPE_SCHOOL = 0;    // 0 = 校内赛事
    private static final int STATUS_PUBLISHED = 1; // 1 = 已发布
    private static final String ORGANIZER = "广东理工学院教务处";

    /**
     * 正文容器自动探测候选选择器（按常见度排列）。
     * 国内高校教务 CMS 多为 Visual SiteBuilder（#vsb_content），
     * 通用文章页常见 div.v_news_content / .content / #content 等结构。
     */
    private static final String[] CONTENT_SELECTORS = {
            "#vsb_content .v_news_content",
            "#vsb_content",
            "div.v_news_content",
            "#article_content",
            "#articleContent",
            ".article-content",
            ".article_content",
            ".news-content",
            ".news_content",
            "#zoom",
            ".TRS_Editor",
            ".Custom_UnionStyle",
            ".content",
            "#content",
            "article"
    };

    /** 发布时间正则：兼容 2025-11-04、2025/11/4、2025.11.04、2025年11月4日 等写法 */
    private static final Pattern DATE_PATTERN =
            Pattern.compile("(20\\d{2})[./年-](\\d{1,2})[./月-](\\d{1,2})日?");

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-M-d");

    @Autowired
    private CompetitionMapper competitionMapper;

    /**
     * 每天凌晨 3 点执行一次
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void crawlCompetitions() {
        log.info("学科竞赛爬虫任务启动：起始页 {}，结束页 {}（0=抓到最后），入口 {}",
                START_PAGE, END_PAGE, LIST_PAGE_URL);
        int inserted = 0;
        int skipped = 0;
        int filtered = 0;
        int failed = 0;
        try {
            String pageUrl = LIST_PAGE_URL;
            int pageNo = 1;
            Set<String> visitedPages = new HashSet<>();

            while (pageUrl != null && !pageUrl.isEmpty() && pageNo <= MAX_PAGES) {
                if (END_PAGE > 0 && pageNo > END_PAGE) {
                    log.info("已到达结束页 END_PAGE={}，停止翻页", END_PAGE);
                    break;
                }
                if (!visitedPages.add(pageUrl)) {
                    log.warn("翻页链接指向已抓取过的地址，停止翻页：{}", pageUrl);
                    break;
                }

                Document listDoc = fetchDocument(pageUrl);

                if (pageNo < START_PAGE) {
                    log.info("第 {} 页不在抓取范围内（START_PAGE={}），仅用于定位后续页码：{}",
                            pageNo, START_PAGE, pageUrl);
                } else {
                    List<ListItem> items = parseListPage(listDoc);
                    log.info("第 {} 页解析到 {} 篇文章：{}", pageNo, items.size(), pageUrl);
                    for (ListItem item : items) {
                        // 标题白名单：不含「通知/关于开展/关于举办」的文章（喜报、获奖新闻等）直接跳过
                        if (!isWantedTitle(item.title)) {
                            filtered++;
                            log.info("标题不含白名单关键词，跳过不入库：{}", item.title);
                            continue;
                        }
                        try {
                            if (processArticle(item, pageUrl)) {
                                inserted++;
                            } else {
                                skipped++;
                            }
                        } catch (Exception e) {
                            failed++;
                            log.error("文章抓取失败：{}（{}）", item.title, item.href, e);
                        }
                        sleepQuietly(SLEEP_MS);
                    }
                }

                String nextPageUrl = nextPageUrl(listDoc, pageUrl);
                if (nextPageUrl == null) {
                    log.info("第 {} 页已是最后一页，翻页结束", pageNo);
                }
                pageUrl = nextPageUrl;
                pageNo++;
                if (pageUrl != null) {
                    sleepQuietly(SLEEP_MS);
                }
            }
            log.info("学科竞赛爬虫完成：新增 {} 条，已存在跳过 {} 条，标题过滤 {} 条，失败 {} 条",
                    inserted, skipped, filtered, failed);
        } catch (Exception e) {
            log.error("学科竞赛爬虫任务执行异常", e);
        }
    }

    /**
     * 取列表页底部翻页控件的「下页」链接；已是最后一页（控件无链接）时返回 null。
     * 该站控件结构：<span class="p_next p_fun"><a href="12.htm">下页</a></span>，
     * 最后一页会变成无链接的 <span class="p_next_d p_fun_d">下页</span>。
     */
    private String nextPageUrl(Document listDoc, String currentPageUrl) {
        Element next = listDoc.selectFirst("span.p_next a[href]");
        if (next == null) {
            return null;
        }
        String href = next.attr("href").trim();
        if (href.isEmpty() || href.startsWith("#") || href.toLowerCase().startsWith("javascript:")) {
            return null;
        }
        return toAbsoluteUrl(href, currentPageUrl);
    }

    /**
     * 标题白名单判断：只有命中白名单关键词（通知/关于开展/关于举办）才返回 true 抓取
     */
    private boolean isWantedTitle(String title) {
        if (title == null || title.isEmpty()) {
            return false;
        }
        for (String keyword : INCLUDE_TITLE_KEYWORDS) {
            if (title.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 下载并解析 HTML 文档
     */
    private Document fetchDocument(String url) throws IOException {
        return Jsoup.connect(url)
                .userAgent(USER_AGENT)
                .header("Accept",
                        "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8")
                .header("Accept-Language", "zh-CN,zh;q=0.9")
                .referrer("https://jwc.gdlgxy.edu.cn/")
                .timeout(REQUEST_TIMEOUT_MS)
                .followRedirects(true)
                .get();
    }

    /**
     * 解析列表页，得到「标题 + 详情页链接 + 列表页日期」。
     * 首选本网站结构 div.doc-item > a.doc-title；未命中时兜底抓取详情页样式的链接。
     */
    private List<ListItem> parseListPage(Document doc) {
        // 用 LinkedHashMap 按 href 去重，避免列表页出现重复链接
        Map<String, ListItem> result = new LinkedHashMap<>();

        Elements items = doc.select("div.doc-item");
        for (Element item : items) {
            Element titleLink = item.selectFirst("a.doc-title");
            if (titleLink == null) {
                continue;
            }
            String title = titleLink.text().trim();
            String href = titleLink.attr("href");
            if (title.isEmpty() || href.isEmpty()) {
                continue;
            }
            Element dateSpan = item.selectFirst("div.doc-meta span");
            String date = dateSpan == null ? null : dateSpan.text().trim();
            result.putIfAbsent(href, new ListItem(title, href, date));
        }

        // 兜底：标准结构没匹配到，就抓页面里所有看起来像详情页的链接
        if (result.isEmpty()) {
            for (Element a : doc.select("a[href]")) {
                String href = a.attr("href");
                if (!looksLikeDetailLink(href)) {
                    continue;
                }
                String title = a.text().trim();
                if (title.isEmpty()) {
                    continue;
                }
                result.putIfAbsent(href, new ListItem(title, href, null));
            }
        }
        return new ArrayList<>(result.values());
    }

    /**
     * 判断链接是否像一篇详情页
     */
    private boolean looksLikeDetailLink(String href) {
        if (href == null) {
            return false;
        }
        String lower = href.toLowerCase();
        // 教务 CMS 详情页常见路径：/info/1811/15671.htm、/content/xx/123.htm 等
        return lower.matches(".*/(info|content|article|news|a)/\\d+/\\d+\\.(htm|html|shtml)$")
                || lower.matches(".*/\\d{3,}\\.(htm|html|shtml)$");
    }

    /**
     * 把相对链接按指定页面地址解析成绝对地址，兼容 ../../info/1811/15671.htm 这类相对路径。
     * 列表页第 1 页在 /sjjx/ 下、其余页在 /sjjx/xkjs/ 下，用当前页做基准才准确。
     */
    private String toAbsoluteUrl(String href, String baseUrl) {
        try {
            return URI.create(baseUrl).resolve(href).toString();
        } catch (Exception e) {
            log.warn("链接解析失败，保持原样：{}", href);
            return href;
        }
    }

    /**
     * 抓取并入库单篇文章；返回 true 表示新增，false 表示已存在跳过
     *
     * @param item        列表页解析出的一条记录
     * @param listPageUrl 该条记录所在的列表页地址（用于解析相对链接）
     */
    private boolean processArticle(ListItem item, String listPageUrl) throws IOException {
        String detailUrl = toAbsoluteUrl(item.href, listPageUrl);
        if (detailUrl == null || detailUrl.isEmpty()) {
            return false;
        }

        // 去重：source_url 已存在则跳过
        Long exists = competitionMapper.selectCount(
                new LambdaQueryWrapper<Competition>().eq(Competition::getSourceUrl, detailUrl));
        if (exists != null && exists > 0) {
            log.debug("已存在，跳过：{}", detailUrl);
            return false;
        }

        Document detailDoc = fetchDocument(detailUrl);
        Element contentEl = detectContent(detailDoc);
        String description = contentEl == null ? "" : cleanText(contentEl);
        LocalDate publishDate = extractPublishDate(detailDoc, contentEl, item.listDate);

        Competition competition = new Competition();
        competition.setTitle(truncate(item.title, 128));  // title 列为 VARCHAR(128)，超长截断
        competition.setType(TYPE_SCHOOL);
        competition.setCategory("学科竞赛");               // 本栏目固定分类，可按需去掉
        competition.setOrganizer(ORGANIZER);
        competition.setDescription(description);
        competition.setStatus(STATUS_PUBLISHED);
        competition.setSourceUrl(detailUrl);

        // 发布时间：优先写详情页/列表页解析出的真实发布日期（当天 00:00）；
        // 解析不到时回退为当前时间，保证 publish_time 永远不为空，后端可直接按该字段倒序取最新数据
        LocalDateTime publishTime = (publishDate == null)
                ? LocalDateTime.now()
                : publishDate.atStartOfDay();
        competition.setPublishTime(publishTime);

        competitionMapper.insert(competition);
        log.info("新增赛事：{}（发布时间 {}{}）{}", competition.getTitle(), publishTime,
                publishDate == null ? "，详情页未解析到日期，已回退当前时间" : "", detailUrl);
        return true;
    }

    /**
     * 自动探测正文容器：依次尝试多种常见选择器，返回文本最长的元素
     */
    private Element detectContent(Document doc) {
        Element best = null;
        int bestLength = 0;
        for (String selector : CONTENT_SELECTORS) {
            Elements elements;
            try {
                elements = doc.select(selector);
            } catch (Exception e) {
                continue;
            }
            for (Element el : elements) {
                int length = el.text().length();
                if (length > bestLength) {
                    bestLength = length;
                    best = el;
                }
            }
        }
        return best;
    }

    /**
     * 提取正文纯文本，保留段落换行并折叠多余空行
     */
    private String cleanText(Element el) {
        if (el == null) {
            return "";
        }
        String text = el.wholeText()
                .replace('\u00A0', ' ')
                .replace('\r', '\n');
        text = text.replaceAll("[ \\t]*\\n[ \\t]*", "\n");
        text = text.replaceAll("\\n{3,}", "\n\n");
        return text.trim();
    }

    /**
     * 提取发布时间：meta 标签 > 正文容器之前的日期元素 > 列表页日期 > 整页兜底
     */
    private LocalDate extractPublishDate(Document doc, Element contentEl, String listDate) {
        // 1) 优先从标准 meta 标签取
        String[][] metaSelectors = {
                {"meta[name=ArticlePublishTime]", "content"},
                {"meta[name=PubDate]", "content"},
                {"meta[name=publishdate]", "content"},
                {"meta[name=releasedate]", "content"},
                {"meta[property=article:published_time]", "content"}
        };
        for (String[] meta : metaSelectors) {
            String value = doc.select(meta[0]).attr(meta[1]);
            if (value.isEmpty()) {
                value = doc.select(meta[0]).attr("value");
            }
            LocalDate d = parseDate(value);
            if (d != null) {
                return d;
            }
        }

        // 2) 在正文容器之前、且非标题/脚本的元素里找日期（发布时间通常紧跟标题）
        if (doc.body() != null) {
            for (Element el : doc.body().getAllElements()) {
                if (contentEl != null && el == contentEl) {
                    break;
                }
                String tag = el.tagName().toLowerCase();
                if ("script".equals(tag) || "style".equals(tag) || "noscript".equals(tag)) {
                    continue;
                }
                if (tag.matches("h[1-6]")) {
                    continue;
                }
                LocalDate d = parseDate(el.ownText());
                if (d != null) {
                    return d;
                }
            }
        }

        // 3) 详情页没找到，回退用列表页给出的日期
        LocalDate fromList = parseDate(listDate);
        if (fromList != null) {
            return fromList;
        }

        // 4) 最后兜底：整页正文里正则找第一个日期
        return doc.body() == null ? null : parseDate(doc.body().text());
    }

    /**
     * 从文本中解析出第一个合法日期
     */
    private LocalDate parseDate(String text) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }
        Matcher m = DATE_PATTERN.matcher(text);
        if (!m.find()) {
            return null;
        }
        String normalized = m.group()
                .replace("年", "-")
                .replace("月", "-")
                .replace("日", "")
                .replace("/", "-")
                .replace(".", "-");
        try {
            return LocalDate.parse(normalized, DATE_FORMATTER);
        } catch (Exception e) {
            log.debug("无法解析日期：{}", normalized);
            return null;
        }
    }

    private String truncate(String s, int max) {
        if (s == null) {
            return null;
        }
        return s.length() <= max ? s : s.substring(0, max);
    }

    private void sleepQuietly(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /** 列表页解析出的一条记录 */
    private static class ListItem {
        final String title;
        final String href;
        final String listDate;

        ListItem(String title, String href, String listDate) {
            this.title = title;
            this.href = href;
            this.listDate = listDate;
        }
    }
}


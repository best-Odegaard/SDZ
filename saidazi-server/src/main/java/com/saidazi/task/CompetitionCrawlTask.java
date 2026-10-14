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
 * 2. 只入库 START_PAGE 页及之后的页码（第 1 页仍会拉取，但仅用于定位第 2 页）。
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
     * 起始页码：从第几页开始「入库」。
     * 1 = 从最新页开始；当前按要求从第 2 页开始。
     * （第 1 页仍会被请求一次，因为要用它的「下页」链接找到第 2 页地址，但该页文章不入库）
     */
    private static final int START_PAGE = 2;

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

    /* ==================== 报名 / 比赛时间解析（填写 reg_start_time / reg_end_time / match_time） ====================
     * 教务处通知里没有结构化的时间字段，报名与比赛时间都写在正文里，常见写法：
     *   「（一）报名截止时间：2026年9月25日」
     *   「1.报名及作品提交时间：即日起至2026年9月20日20:00」
     *   「四、赛程安排 比赛时间：2026年9月30日9:00—17:00」
     *   「报名时间：2026年9月10日至9月25日」（后半段省略年份）
     *   「请务必在9月27日24:00前填写报名表」（没有“报名截止时间”字样，需兜底）
     * 解析策略：按标签定位 → 截取标签后一小段 → 抽日期（可选时刻）→ 落到对应字段。
     */

    /** 日期（带年份），兼容 2026年9月25日 / 2026-09-25 / 2026.9.25 */
    private static final Pattern SCHEDULE_DATE_YEAR =
            Pattern.compile("(20\\d{2})\\s*[年./-]\\s*(\\d{1,2})\\s*[月./-]\\s*(\\d{1,2})\\s*日?");

    /** 日期（省略年份），如「9月25日」「9月10日至9月25日」的后半段 */
    private static final Pattern SCHEDULE_DATE_NO_YEAR =
            Pattern.compile("(\\d{1,2})\\s*月\\s*(\\d{1,2})\\s*日");

    /** 时刻：9:00 / 20：00 / 中午12点整 / 下午3点30分 / 9点30分 */
    private static final Pattern SCHEDULE_TIME =
            Pattern.compile("(?:(\\d{1,2})\\s*[:：]\\s*(\\d{1,2})"
                    + "|(中午|上午|下午|晚上)\\s*(\\d{1,2})\\s*[点时](?:\\s*(\\d{1,2})\\s*分?)?"
                    + "|(\\d{1,2})\\s*[点时](\\d{1,2})?\\s*分)");

    /** 时间段截断标记：句读、下一章节「（二）」、序号「3.」等 */
    private static final Pattern SCHEDULE_CUT =
            Pattern.compile("[；;。\\n\\r]|（[一二三四五六七八九十]）|\\(\\d+\\)|\\d+\\.\\s*[^\\d]");

    /** 标签与字段映射：regEnd=报名截止、regStart=报名开始、regBoth=报名时间（含区间）、match=比赛时间 */
    private static final String[][] SCHEDULE_LABELS = {
            {"regEnd", "报名(?:截止|结束)(?:时间|日期|期限)?"},
            {"regEnd", "(?:提交|上报|报送)(?:参赛作品)?截止(?:时间|日期)?"},
            {"regEnd", "截止(?:时间|日期)?[:：]?\\s*(?:为|是)?"},
            {"regStart", "报名(?:开始|起始|开放)(?:时间|日期)?"},
            {"regBoth", "报名(?:及|和|与)?(?:作品)?提交(?:时间|日期|期限)"},
            {"regBoth", "报名(?:时间|日期|期限)"},
            {"match", "(?:比赛|竞赛|初赛|复赛|决赛|校赛|校内赛|大赛|赛事)(?:时间|日期)"}
    };

    /** 兜底：没有「报名截止」字样，但写成「在9月27日24:00前填写/提交/上传报名（表）」 */
    private static final Pattern SCHEDULE_DEADLINE_FALLBACK =
            Pattern.compile("(\\d{1,2})\\s*月\\s*(\\d{1,2})\\s*日"
                    + "(?:\\s*\\d{1,2}\\s*[:：]\\s*\\d{1,2})?\\s*(?:前|之前|以前)"
                    + "[^。；\\n]{0,14}(?:报名|填写|提交|上传)");

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

        // 报名 / 比赛时间：从通知正文解析后写入，否则详情页三栏与日历页都会是空的
        Schedule schedule = parseSchedule(description, publishTime.getYear());
        competition.setRegStartTime(schedule.regStart);
        competition.setRegEndTime(schedule.regEnd);
        competition.setMatchTime(schedule.matchTime);

        competitionMapper.insert(competition);
        log.info("新增赛事：{}（发布时间 {}；报名开始 {}，报名截止 {}，比赛 {}）{}",
                competition.getTitle(), publishTime, schedule.regStart, schedule.regEnd, schedule.matchTime,
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

    /* ==================== 报名 / 比赛时间：解析与回填 ==================== */

    /**
     * 回填历史赛事的报名/比赛时间。
     *
     * 背景：爬虫早期版本只写 title/description/publishTime，reg_start_time、reg_end_time、
     * match_time 三列始终为 null（只有手工录入的示例数据才有值），导致详情页三栏信息与日历页空白。
     *
     * 本方法只读取库里已存的 description 做本地解析，不发起任何网络请求，可安全重复执行；
     * 已存在的时间不会被覆盖，只补 null 的列。
     *
     * @return 统计结果 scanned（扫描条数）/ matched（正文中识别出时间）/ updated（实际写入）
     */
    public Map<String, Object> backfillSchedule() {
        List<Competition> list = competitionMapper.selectList(
                new LambdaQueryWrapper<Competition>()
                        .isNotNull(Competition::getDescription)
                        .and(w -> w.isNull(Competition::getRegStartTime)
                                .or().isNull(Competition::getRegEndTime)
                                .or().isNull(Competition::getMatchTime)));

        int scanned = list.size();
        int matched = 0;
        int updated = 0;
        for (Competition c : list) {
            int defaultYear = c.getPublishTime() != null ? c.getPublishTime().getYear()
                    : (c.getCreateTime() != null ? c.getCreateTime().getYear() : LocalDate.now().getYear());
            Schedule s = parseSchedule(c.getDescription(), defaultYear);
            if (s.isEmpty()) {
                continue;
            }
            matched++;

            Competition patch = new Competition();
            patch.setId(c.getId());
            if (s.regStart != null && c.getRegStartTime() == null) {
                patch.setRegStartTime(s.regStart);
            }
            if (s.regEnd != null && c.getRegEndTime() == null) {
                patch.setRegEndTime(s.regEnd);
            }
            if (s.matchTime != null && c.getMatchTime() == null) {
                patch.setMatchTime(s.matchTime);
            }
            if (patch.getRegStartTime() == null && patch.getRegEndTime() == null
                    && patch.getMatchTime() == null) {
                continue;
            }
            competitionMapper.updateById(patch);
            updated++;
            log.info("回填时间：id={} {} | 报名开始={} 报名截止={} 比赛={}", c.getId(),
                    truncate(c.getTitle(), 24), patch.getRegStartTime(), patch.getRegEndTime(), patch.getMatchTime());
        }
        log.info("历史赛事时间回填完成：扫描 {} 条，识别出时间 {} 条，写入 {} 条", scanned, matched, updated);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("scanned", scanned);
        result.put("matched", matched);
        result.put("updated", updated);
        return result;
    }

    /**
     * 从通知正文里解析报名开始 / 报名截止 / 比赛时间；解析不到的位置保持 null。
     *
     * @param text        通知正文
     * @param defaultYear 正文里省略年份时使用的年份（一般取发布时间所在年）
     */
    private Schedule parseSchedule(String text, int defaultYear) {
        Schedule s = new Schedule();
        if (text == null || text.trim().isEmpty()) {
            return s;
        }
        String body = text.replace('\u00A0', ' ').replace('\r', '\n');

        for (String[] label : SCHEDULE_LABELS) {
            String field = label[0];
            if (isScheduleFilled(s, field)) {
                continue;
            }
            Matcher lm = Pattern.compile(label[1]).matcher(body);
            while (lm.find()) {
                List<Dt> dates = findScheduleDates(cutSegment(body, lm.end()), defaultYear);
                if (dates.isEmpty()) {
                    continue;
                }
                if ("regEnd".equals(field)) {
                    s.regEnd = toDateTime(withTime(dates.get(0), cutSegment(body, lm.end())), true);
                } else if ("regStart".equals(field)) {
                    s.regStart = toDateTime(withTime(dates.get(0), cutSegment(body, lm.end())), false);
                } else if ("match".equals(field)) {
                    s.matchTime = toDateTime(withTime(dates.get(0), cutSegment(body, lm.end())), false);
                } else { // regBoth：一个日期视为截止，两个日期视为起止区间
                    if (dates.size() >= 2) {
                        s.regStart = toDateTime(dates.get(0), false);
                        s.regEnd = toDateTime(dates.get(dates.size() - 1), true);
                    } else {
                        s.regEnd = toDateTime(withTime(dates.get(0), cutSegment(body, lm.end())), true);
                    }
                }
                break; // 同一类标签只取第一条有效结果
            }
            if (s.regStart != null && s.regEnd != null && s.matchTime != null) {
                break;
            }
        }

        // 兜底：正文没写「报名截止时间」，但常写成「请务必在9月27日24:00前填写报名表」
        if (s.regEnd == null) {
            Matcher fm = SCHEDULE_DEADLINE_FALLBACK.matcher(body);
            if (fm.find()) {
                int year = defaultYear;
                Matcher ym = Pattern.compile("(20\\d{2})\\s*年")
                        .matcher(body.substring(Math.max(0, fm.start() - 24), fm.start()));
                if (ym.find()) {
                    year = Integer.parseInt(ym.group(1));
                }
                Dt d = new Dt(year, Integer.parseInt(fm.group(1)), Integer.parseInt(fm.group(2)), fm.end());
                s.regEnd = toDateTime(d, true);
            }
        }

        // 兜底：只识别出报名截止、没识别出报名开始，且正文写了区间「9月10日至9月25日」
        if (s.regEnd != null && s.regStart == null) {
            s.regStart = null; // 不臆造开始时间
        }
        if (s.regStart != null && s.regEnd != null && s.regStart.isAfter(s.regEnd)) {
            LocalDateTime tmp = s.regStart;
            s.regStart = s.regEnd;
            s.regEnd = tmp;
        }
        return s;
    }

    /** 某字段是否已解析到值 */
    private boolean isScheduleFilled(Schedule s, String field) {
        if ("regEnd".equals(field)) {
            return s.regEnd != null;
        }
        if ("regStart".equals(field)) {
            return s.regStart != null;
        }
        if ("match".equals(field)) {
            return s.matchTime != null;
        }
        return s.regStart != null && s.regEnd != null;
    }

    /** 截取标签之后的一小段文字（到句读或下一章节标记为止），避免把后面的内容误算进来 */
    private String cutSegment(String text, int from) {
        if (from >= text.length()) {
            return "";
        }
        String seg = text.substring(from, Math.min(text.length(), from + 42));
        Matcher m = SCHEDULE_CUT.matcher(seg);
        if (m.find()) {
            seg = seg.substring(0, m.start());
        }
        return seg;
    }

    /** 在片段里抽日期：优先带年份的写法，没有则按「9月25日」结合默认年份 */
    private List<Dt> findScheduleDates(String seg, int defaultYear) {
        List<Dt> list = new ArrayList<>();
        Matcher m = SCHEDULE_DATE_YEAR.matcher(seg);
        while (m.find()) {
            list.add(new Dt(Integer.parseInt(m.group(1)), Integer.parseInt(m.group(2)),
                    Integer.parseInt(m.group(3)), m.end()));
        }
        if (list.isEmpty()) {
            Matcher m2 = SCHEDULE_DATE_NO_YEAR.matcher(seg);
            while (m2.find()) {
                list.add(new Dt(defaultYear, Integer.parseInt(m2.group(1)),
                        Integer.parseInt(m2.group(2)), m2.end()));
            }
        }
        return list;
    }

    /** 在日期后面继续找具体时刻（9:00 / 中午12点 / 下午3点30分） */
    private Dt withTime(Dt d, String seg) {
        if (d == null || d.endIdx >= seg.length()) {
            return d;
        }
        String after = seg.substring(d.endIdx, Math.min(seg.length(), d.endIdx + 16));
        Matcher m = SCHEDULE_TIME.matcher(after);
        if (!m.find()) {
            return d;
        }
        int hour;
        int minute;
        if (m.group(1) != null) {
            hour = Integer.parseInt(m.group(1));
            minute = m.group(2) == null ? 0 : Integer.parseInt(m.group(2));
        } else if (m.group(3) != null) {
            hour = Integer.parseInt(m.group(4));
            minute = m.group(5) == null ? 0 : Integer.parseInt(m.group(5));
            String word = m.group(3);
            if (("下午".equals(word) || "晚上".equals(word)) && hour < 12) {
                hour += 12;
            } else if ("中午".equals(word) && hour <= 2) {
                hour += 12;
            }
        } else {
            hour = Integer.parseInt(m.group(6));
            minute = m.group(7) == null ? 0 : Integer.parseInt(m.group(7));
        }
        if (hour <= 23 && minute <= 59) {
            d.hour = hour;
            d.minute = minute;
        }
        return d;
    }

    /**
     * 组装 LocalDateTime：有具体时刻用具体时刻；没有则截止时间取当天 23:59:59、其余取 00:00:00
     */
    private LocalDateTime toDateTime(Dt d, boolean endOfDayIfNoTime) {
        if (d == null) {
            return null;
        }
        try {
            LocalDate date = LocalDate.of(d.year, d.month, d.day);
            if (d.hour != null) {
                return date.atTime(d.hour, d.minute);
            }
            return endOfDayIfNoTime ? date.atTime(23, 59, 59) : date.atStartOfDay();
        } catch (Exception e) {
            log.debug("非法日期，忽略：{}-{}-{}", d.year, d.month, d.day);
            return null;
        }
    }

    /** 解析出的报名/比赛时间 */
    private static class Schedule {
        LocalDateTime regStart;
        LocalDateTime regEnd;
        LocalDateTime matchTime;

        boolean isEmpty() {
            return regStart == null && regEnd == null && matchTime == null;
        }
    }

    /** 片段中的一个日期（含可选时刻） */
    private static class Dt {
        final int year;
        final int month;
        final int day;
        final int endIdx;
        Integer hour;
        Integer minute;

        Dt(int year, int month, int day, int endIdx) {
            this.year = year;
            this.month = month;
            this.day = day;
            this.endIdx = endIdx;
        }
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

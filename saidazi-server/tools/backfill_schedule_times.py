# -*- coding: utf-8 -*-
"""回填赛事「报名开始 / 报名截止 / 比赛时间」。

用途
----
爬虫早期版本只写 title / description / publish_time，reg_start_time、reg_end_time、
match_time 三列从未赋值，导致详情页三栏信息与日历页空白。本脚本从已入库的通知正文
（description）里解析出这三个时间并回填，不需要重新部署后端。

特点
----
* 只补 NULL 的列，绝不覆盖已有值；SQL 里带 `WHERE <col> IS NULL`，并发也安全
* 默认干跑（只打印将要写入的内容），加 --apply 才真正写库
* 写入前把受影响行的原始值导出成 JSON 备份，可用于还原
* 可重复执行（第二次跑不会有任何改动）
* 数据库连接信息从 saidazi-server/src/main/resources/application.yml 读取，脚本内不写密码

依赖：pip install pymysql

用法（在 saidazi-server 目录下执行）
------------------------------------
    python tools/backfill_schedule_times.py            # 干跑，看会改哪些
    python tools/backfill_schedule_times.py --apply    # 真正写入
"""

import io
import json
import os
import re
import sys

import pymysql

# ---------------------------------------------------------------- 解析规则
# 与后端 Java 版 CompetitionCrawlTask.parseSchedule 一一对应，改一处请同步另一处。

DATE_YEAR = re.compile(r'(20\d{2})\s*[年./\-]\s*(\d{1,2})\s*[月./\-]\s*(\d{1,2})\s*日?')
DATE_NOYEAR = re.compile(r'(?<![\d年])(\d{1,2})\s*月\s*(\d{1,2})\s*日')
TIME_PART = re.compile(
    r'(?:(\d{1,2})\s*[:：]\s*(\d{1,2})'
    r'|(中午|上午|下午|晚上)\s*(\d{1,2})\s*[点时](?:\s*(\d{1,2})\s*分?)?'
    r'|(\d{1,2})\s*[点时](\d{1,2})?\s*分)'
)
CUT = re.compile(r'[；;。\n\r]|（[一二三四五六七八九十]）|\(\d+\)|\d+\.\s*[^\d]')

LABELS = [
    ('regEnd',   r'报名(?:截止|结束)(?:时间|日期|期限)?'),
    ('regEnd',   r'(?:提交|上报|报送)(?:参赛作品)?截止(?:时间|日期)?'),
    ('regEnd',   r'截止(?:时间|日期)?[:：]?\s*(?:为|是)?'),
    ('regStart', r'报名(?:开始|起始|开放)(?:时间|日期)?'),
    ('regBoth',  r'报名(?:及|和|与)?(?:作品)?提交(?:时间|日期|期限)'),
    ('regBoth',  r'报名(?:时间|日期|期限)'),
    ('match',    r'(?:比赛|竞赛|初赛|复赛|决赛|校赛|校内赛|大赛|赛事)(?:时间|日期)'),
]

# 兜底：正文没写「报名截止时间」，但常写成「请务必在9月27日24:00前填写报名表」
DEADLINE_FALLBACK = re.compile(
    r'(\d{1,2})\s*月\s*(\d{1,2})\s*日(?:\s*\d{1,2}\s*[:：]\s*\d{1,2})?\s*(?:前|之前|以前)'
    r'[^。；\n]{0,14}(?:报名|填写|提交|上传)'
)


def _cut(text, start, span=42):
    """截取标签之后的一小段（到句读或下一章节标记为止）"""
    seg = text[start:start + span]
    m = CUT.search(seg)
    return seg[:m.start()] if m else seg


def _time_after(seg, pos):
    """在日期后面继续找具体时刻（9:00 / 中午12点 / 下午3点30分）"""
    m = TIME_PART.search(seg[pos:pos + 16])
    if not m:
        return None
    if m.group(1):
        h, mi = int(m.group(1)), int(m.group(2) or 0)
    elif m.group(3):
        word, h, mi = m.group(3), int(m.group(4)), int(m.group(5) or 0)
        if word in ('下午', '晚上') and h < 12:
            h += 12
        elif word == '中午' and h <= 2:
            h += 12
    else:
        h, mi = int(m.group(6)), int(m.group(7) or 0)
    return (h, mi) if h <= 23 and mi <= 59 else None


def _dates(seg, default_year):
    out = []
    for m in DATE_YEAR.finditer(seg):
        out.append((int(m.group(1)), int(m.group(2)), int(m.group(3)), m.end()))
    if not out:
        for m in DATE_NOYEAR.finditer(seg):
            out.append((default_year, int(m.group(1)), int(m.group(2)), m.end()))
    return out


def parse_schedule(text, default_year):
    """返回 {'regStart': 'Y-m-d H:M:S', 'regEnd': ..., 'matchTime': ...}，解析不到的键不出现"""
    if not text:
        return {}
    text = re.sub(r'[ \t\u00a0]+', ' ', text)
    res = {}

    def put(key, y, mo, d, t, end_of_day):
        if t:
            res[key] = '%04d-%02d-%02d %02d:%02d:00' % (y, mo, d, t[0], t[1])
        else:
            res[key] = '%04d-%02d-%02d %s' % (y, mo, d, '23:59:59' if end_of_day else '00:00:00')

    for field, label in LABELS:
        if field == 'regEnd' and 'regEnd' in res:
            continue
        if field == 'regStart' and 'regStart' in res:
            continue
        if field == 'match' and 'matchTime' in res:
            continue
        if field == 'regBoth' and 'regStart' in res and 'regEnd' in res:
            continue
        for m in re.finditer(label, text):
            seg = _cut(text, m.end())
            ds = _dates(seg, default_year)
            if not ds:
                continue
            if field == 'match':
                y, mo, d, endpos = ds[0]
                put('matchTime', y, mo, d, _time_after(seg, endpos), False)
            elif field in ('regEnd', 'regStart'):
                y, mo, d, endpos = ds[0]
                put('regEnd' if field == 'regEnd' else 'regStart',
                    y, mo, d, _time_after(seg, endpos), field == 'regEnd')
            else:  # regBoth：一个日期视为截止，两个日期视为起止区间
                if len(ds) >= 2:
                    y1, m1, d1, _ = ds[0]
                    y2, m2, d2, _ = ds[-1]
                    put('regStart', y1, m1, d1, None, False)
                    put('regEnd', y2, m2, d2, None, True)
                else:
                    y, mo, d, endpos = ds[0]
                    put('regEnd', y, mo, d, _time_after(seg, endpos), True)
            break
        if 'regStart' in res and 'regEnd' in res and 'matchTime' in res:
            break

    if 'regEnd' not in res:
        m = DEADLINE_FALLBACK.search(text)
        if m:
            year = default_year
            ym = re.search(r'(20\d{2})\s*年', text[max(0, m.start() - 24):m.start()])
            if ym:
                year = int(ym.group(1))
            put('regEnd', year, int(m.group(1)), int(m.group(2)), None, True)

    if res.get('regStart') and res.get('regEnd') and res['regStart'] > res['regEnd']:
        res['regStart'], res['regEnd'] = res['regEnd'], res['regStart']
    return res


# ---------------------------------------------------------------- 数据库
def load_db_config():
    """从 application.yml 读取数据源配置，避免把密码写死在脚本里"""
    yml = os.path.join(os.path.dirname(os.path.abspath(__file__)),
                       '..', 'src', 'main', 'resources', 'application.yml')
    text = io.open(yml, encoding='utf-8').read()
    url = re.search(r'jdbc:mysql://([^:/]+):(\d+)/([^?\s]+)', text)
    user = re.search(r'username:\s*(\S+)', text)
    pwd = re.search(r"password:\s*['\"]?([^'\"\s]+)", text)
    if not (url and user and pwd):
        sys.exit('无法从 application.yml 解析数据库配置，请手动传参')
    return dict(host=url.group(1), port=int(url.group(2)), database=url.group(3),
                user=user.group(1), password=pwd.group(1),
                charset='utf8mb4', connect_timeout=10)


def main():
    apply = '--apply' in sys.argv
    backup_path = os.path.join(os.path.dirname(os.path.abspath(__file__)),
                               'backfill_backup.json')

    conn = pymysql.connect(**load_db_config())
    cur = conn.cursor(pymysql.cursors.DictCursor)

    cur.execute("SHOW COLUMNS FROM competition")
    cols = [r['Field'] for r in cur.fetchall()]
    need = ['id', 'title', 'description', 'publish_time', 'create_time',
            'reg_start_time', 'reg_end_time', 'match_time']
    missing = [c for c in need if c not in cols]
    if missing:
        sys.exit('competition 表缺少列：%s' % missing)

    where = "WHERE deleted = 0" if 'deleted' in cols else ""
    cur.execute('SELECT %s FROM competition %s ORDER BY id' % (', '.join(need), where))
    rows = cur.fetchall()
    print('库中共 %d 条赛事' % len(rows))

    backup, plan = [], []
    for r in rows:
        base = r.get('publish_time') or r.get('create_time')
        default_year = getattr(base, 'year', 2026)
        parsed = parse_schedule(r.get('description') or '', default_year)
        patch = {}
        if parsed.get('regStart') and r['reg_start_time'] is None:
            patch['reg_start_time'] = parsed['regStart']
        if parsed.get('regEnd') and r['reg_end_time'] is None:
            patch['reg_end_time'] = parsed['regEnd']
        if parsed.get('matchTime') and r['match_time'] is None:
            patch['match_time'] = parsed['matchTime']
        if patch:
            backup.append({k: (str(r[k]) if r[k] is not None else None) for k in need})
            plan.append((r['id'], str(r['title'])[:26], patch))

    print('识别出可回填的赛事：%d 条' % len(plan))
    print('-' * 96)
    for pid, title, patch in plan:
        print('id=%-4s %-28s 报名开始=%-19s 报名截止=%-19s 比赛=%s' % (
            pid, title, patch.get('reg_start_time', '—'),
            patch.get('reg_end_time', '—'), patch.get('match_time', '—')))
    print('-' * 96)

    if not apply:
        print('【干跑】未写入数据库。确认无误后加 --apply 执行写入。')
        return

    if os.path.exists(backup_path):  # 保留最早那次备份，避免被覆盖后无法还原
        print('已存在备份 %s，本次不覆盖' % backup_path)
    else:
        io.open(backup_path, 'w', encoding='utf-8').write(
            json.dumps(backup, ensure_ascii=False, indent=1))
        print('原始值已备份：%s（%d 条）' % (backup_path, len(backup)))

    written = 0
    for pid, title, patch in plan:
        sets = ', '.join('%s=%%s' % k for k in patch)
        conds = ' AND '.join('%s IS NULL' % k for k in patch)   # 只补空列
        cur.execute('UPDATE competition SET %s WHERE id=%%s AND %s' % (sets, conds),
                    list(patch.values()) + [pid])
        written += cur.rowcount
    conn.commit()
    print('已写入 %d 行' % written)

    cur.execute('SELECT COUNT(*) AS c FROM competition WHERE reg_start_time IS NOT NULL '
                'OR reg_end_time IS NOT NULL OR match_time IS NOT NULL')
    print('现在有时间字段的赛事数：%d' % cur.fetchone()['c'])
    conn.close()


if __name__ == '__main__':
    main()

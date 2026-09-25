"""赛搭子 · 接口联调测试脚本 — 输出到文件"""
import requests, json, struct, zlib, sys, os

BASE = "http://localhost:8080"
LOG = "D:/geek/外/saidazi-server/test_report.txt"

PASS, FAIL = 0, 0
lines = []

def log(s=""):
    lines.append(s)
    print(s)

def test(name, ok, detail=""):
    global PASS, FAIL
    if ok:
        PASS += 1
        log(f"  ✅ {name}")
    else:
        FAIL += 1
        log(f"  ❌ {name}  —— {detail}" if detail else f"  ❌ {name}")

def api(method, path, **kw):
    r = requests.request(method, f"{BASE}{path}", timeout=10, **kw)
    return r.json()

log("=" * 60)
log("  赛搭子 · 接口联调测试报告")
log(f"  测试时间: 2026-07-27  |  后端: {BASE}")
log("=" * 60)

# ========== 接口① ==========
log("\n【接口① 赛事提醒】")
log("-" * 40)

j = api("POST", "/api/reminder/add",
        params={"userId": 1, "competitionId": 1,
                "remindTime": "2026-08-01 10:00:00", "remindType": 1})
test("①-1 设置提醒", j["code"] == 200 and j["data"]["userId"] == 1,
     json.dumps({"id": j["data"]["id"], "status": j["data"]["status"]}))
rid = j["data"].get("id")

j = api("GET", "/api/reminder/list", params={"userId": 1})
test("①-2 查询提醒列表", j["code"] == 200 and len(j["data"]) > 0,
     f"共 {len(j['data'])} 条")

if rid:
    j = api("DELETE", f"/api/reminder/{rid}", params={"userId": 1})
    test("①-3 取消提醒", j["code"] == 200)

# ========== 接口② ==========
log("\n【接口② 智能匹配】")
log("-" * 40)

j = api("GET", "/api/match/teams", params={"userId": 1})
ok = j["code"] == 200 and len(j["data"]) > 0
test("②-1 推荐组队", ok, f"推荐 {len(j['data'])} 个组队")
if j["code"] == 200:
    for item in j["data"]:
        log(f"    ├─ [{item['matchScore']}分] 共同技能: {item.get('commonSkills','')}")

j = api("GET", "/api/match/mates", params={"userId": 1})
ok = j["code"] == 200
test("②-2 推荐队友", ok, f"推荐 {len(j['data'])} 个队友")
if j["code"] == 200:
    for item in j["data"]:
        log(f"    ├─ [{item['matchScore']}分] 共同技能: {item.get('commonSkills','')}")

# ========== 接口③ ==========
log("\n【接口③ 图片上传】")
log("-" * 40)

# 创建最小 PNG
sig = b'\x89PNG\r\n\x1a\n'
def chunk(t, d):
    c = t + d
    return struct.pack('>I', len(d)) + c + struct.pack('>I', zlib.crc32(c) & 0xffffffff)
ihdr = struct.pack('>IIBBBBB', 1, 1, 8, 2, 0, 0, 0)
raw = b'\x00\xff\xff\xff'
png_data = sig + chunk(b'IHDR', ihdr) + chunk(b'IDAT', zlib.compress(raw)) + chunk(b'IEND', b'')

r = requests.post(f"{BASE}/api/file/upload",
                  files={"file": ("test.png", png_data, "image/png")}, timeout=10)
j = r.json()
url = j.get("data", "")
test("③-1 单张上传", j["code"] == 200 and ("/uploads/" in url), f"URL: {url}")

r = requests.post(f"{BASE}/api/file/upload",
                  files={"file": ("bad.pdf", b"fake", "application/pdf")}, timeout=10)
j = r.json()
test("③-2 非法格式拦截", j["code"] != 200, j.get("message", ""))

if url:
    j = api("DELETE", "/api/file/delete", params={"url": url})
    test("③-3 删除图片", j["code"] == 200)

# ========== 接口④ ==========
log("\n【接口④ 组队审核】")
log("-" * 40)

j = api("POST", "/api/team/apply", params={"userId": 3, "teamId": 1})
app_id = j["data"].get("id") if j["code"] == 200 else None
test("④-1 申请组队(不要求简历)", j["code"] == 200 and j["data"]["status"] == 0,
     f"申请ID={app_id}")

j = api("POST", "/api/team/apply", params={"userId": 3, "teamId": 2})
test("④-2 要求简历但未传", j["code"] != 200, j.get("message", ""))

j = api("GET", "/api/team/applications/received", params={"userId": 1})
ok = j["code"] == 200 and len(j["data"]) > 0
test("④-3 收到的申请列表", ok, f"共 {len(j['data'])} 条")
if j["code"] == 200:
    for a in j["data"]:
        s = {0: "待审核", 1: "已通过", 2: "已拒绝"}
        log(f"    ├─ ID={a['id']}, 申请人={a['applicantId']}, 状态={s.get(a['status'],'?')}, 匹配度={a.get('matchScore','?')}")

if app_id:
    j = api("PUT", "/api/team/application/process",
            params={"userId": 1},
            json={"applicationId": app_id, "action": 1})
    test("④-4 通过申请", j["code"] == 200, j.get("message", ""))

j = api("POST", "/api/team/apply", params={"userId": 3, "teamId": 1})
test("④-5 重复申请拦截", j["code"] != 200, j.get("message", ""))

if app_id:
    j = api("PUT", "/api/team/application/process",
            params={"userId": 3},
            json={"applicationId": app_id, "action": 1})
    test("④-6 权限校验(非发布人)", j["code"] != 200, j.get("message", ""))

# ========== 汇总 ==========
log("\n" + "=" * 60)
total = PASS + FAIL
log(f"  测试总数: {total}  |  ✅ 通过: {PASS}  |  ❌ 失败: {FAIL}")
if FAIL == 0:
    log("  🎉 全部通过！联调测试完成！")
else:
    log(f"  ⚠️ 有 {FAIL} 个测试失败")
log("=" * 60)

# 写入文件
with open(LOG, "w", encoding="utf-8") as f:
    f.write("\n".join(lines))
print(f"\n测试报告已保存到: {LOG}")

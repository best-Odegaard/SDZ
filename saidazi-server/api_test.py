#!/usr/bin/env python3
"""赛搭子 · 接口联调测试脚本"""
import requests, json, struct, zlib, os, time

BASE = "http://localhost:8080"
PASS = 0
FAIL = 0

def test(name, fn):
    global PASS, FAIL
    try:
        result = fn()
        if result:
            print(f"  ✅ {name}")
            PASS += 1
        else:
            print(f"  ❌ {name}")
            FAIL += 1
    except Exception as e:
        print(f"  ❌ {name}: {e}")
        FAIL += 1

def api(method, path, **kwargs):
    url = f"{BASE}{path}"
    r = requests.request(method, url, **kwargs, timeout=10)
    return r.json()

print("=" * 60)
print("  赛搭子 · 接口联调测试报告")
print("=" * 60)

# ========== 接口① 赛事提醒 ==========
print("\n【接口① 赛事提醒】")
print("-" * 40)

remind_id = None

def test_add_reminder():
    global remind_id
    j = api("POST", "/api/reminder/add",
            params={"userId": 1, "competitionId": 1,
                    "remindTime": "2026-08-01 10:00:00", "remindType": 1})
    assert j["code"] == 200
    assert j["data"]["userId"] == 1
    remind_id = j["data"]["id"]
    return True

test("①-1 设置提醒", test_add_reminder)

def test_list_reminders():
    j = api("GET", "/api/reminder/list", params={"userId": 1})
    assert j["code"] == 200
    assert len(j["data"]) > 0
    return True

test("①-2 查询提醒列表", test_list_reminders)

def test_cancel_reminder():
    global remind_id
    if not remind_id:
        return False
    j = api("DELETE", f"/api/reminder/{remind_id}", params={"userId": 1})
    assert j["code"] == 200
    return True

test("①-3 取消提醒", test_cancel_reminder)

# ========== 接口② 智能匹配 ==========
print("\n【接口② 智能匹配】")
print("-" * 40)

def test_recommend_teams():
    j = api("GET", "/api/match/teams", params={"userId": 1})
    assert j["code"] == 200
    data = j["data"]
    assert isinstance(data, list)
    print(f"    推荐组队 {len(data)} 个:")
    for item in data:
        print(f"      ├─ [{item['matchScore']}分] {item['title']} | 共同技能: {item.get('commonSkills','')}")
    return True

test("②-1 推荐组队", test_recommend_teams)

def test_recommend_mates():
    j = api("GET", "/api/match/mates", params={"userId": 1})
    assert j["code"] == 200
    data = j["data"]
    assert isinstance(data, list)
    print(f"    推荐队友 {len(data)} 个:")
    for item in data:
        print(f"      ├─ [{item['matchScore']}分] {item['title']} | 共同技能: {item.get('commonSkills','')}")
    return True

test("②-2 推荐队友", test_recommend_mates)

# ========== 接口③ 图片上传 ==========
print("\n【接口③ 图片上传】")
print("-" * 40)

# 创建测试 PNG
def create_png():
    sig = b'\x89PNG\r\n\x1a\n'
    def chunk(ctype, data):
        c = ctype + data
        return struct.pack('>I', len(data)) + c + struct.pack('>I', zlib.crc32(c) & 0xffffffff)
    ihdr = struct.pack('>IIBBBBB', 1, 1, 8, 2, 0, 0, 0)
    raw = b'\x00\xff\xff\xff'
    idat = zlib.compress(raw)
    return sig + chunk(b'IHDR', ihdr) + chunk(b'IDAT', idat) + chunk(b'IEND', b'')

png_data = create_png()
uploaded_url = None

def test_upload_single():
    global uploaded_url
    files = {"file": ("test.png", png_data, "image/png")}
    r = requests.post(f"{BASE}/api/file/upload", files=files, timeout=10)
    j = r.json()
    assert j["code"] == 200
    uploaded_url = j["data"]
    assert uploaded_url.startswith("/uploads/")
    print(f"    上传成功: {uploaded_url}")
    return True

test("③-1 单张上传", test_upload_single)

def test_upload_invalid():
    files = {"file": ("test.pdf", b"fake pdf content", "application/pdf")}
    r = requests.post(f"{BASE}/api/file/upload", files=files, timeout=10)
    j = r.json()
    assert j["code"] != 200  # 应该被拦截
    return True

test("③-2 非法格式拦截", test_upload_invalid)

def test_delete_file():
    global uploaded_url
    if not uploaded_url:
        return False
    j = api("DELETE", "/api/file/delete", params={"url": uploaded_url})
    assert j["code"] == 200
    return True

test("③-3 删除图片", test_delete_file)

# ========== 接口④ 组队审核 ==========
print("\n【接口④ 组队审核】")
print("-" * 40)

app_id = None

def test_apply_no_resume():
    global app_id
    j = api("POST", "/api/team/apply", params={"userId": 3, "teamId": 1})
    assert j["code"] == 200
    app_id = j["data"]["id"]
    assert j["data"]["status"] == 0  # 待审核
    print(f"    申请成功, id={app_id}")
    return True

test("④-1 申请组队(不要求简历)", test_apply_no_resume)

def test_apply_require_resume():
    j = api("POST", "/api/team/apply", params={"userId": 3, "teamId": 2})
    assert j["code"] != 200  # 应拦截
    return True

test("④-2 申请组队(要求简历但未传)", test_apply_require_resume)

def test_received_apps():
    j = api("GET", "/api/team/applications/received", params={"userId": 1})
    assert j["code"] == 200
    data = j["data"]
    assert len(data) > 0
    print(f"    收到 {len(data)} 条申请:")
    for a in data:
        status_map = {0: "待审核", 1: "已通过", 2: "已拒绝"}
        print(f"      ├─ ID={a['id']}, 申请人={a['applicantId']}, 状态={status_map.get(a['status'], '?')}, 匹配度={a.get('matchScore','?')}")
    return True

test("④-3 收到的申请列表", test_received_apps)

def test_approve():
    global app_id
    if not app_id:
        return False
    j = api("PUT", "/api/team/application/process",
            params={"userId": 1},
            json={"applicationId": app_id, "action": 1})
    assert j["code"] == 200
    return True

test("④-4 通过申请", test_approve)

def test_duplicate_apply():
    j = api("POST", "/api/team/apply", params={"userId": 3, "teamId": 1})
    assert j["code"] != 200
    return True

test("④-5 重复申请拦截", test_duplicate_apply)

def test_permission_check():
    global app_id
    if not app_id:
        return False
    # 用户3不是发布人，无权审核
    j = api("PUT", "/api/team/application/process",
            params={"userId": 3},
            json={"applicationId": app_id, "action": 1})
    assert j["code"] != 200
    return True

test("④-6 权限校验(非发布人)", test_permission_check)

# ========== 汇总 ==========
print("\n" + "=" * 60)
total = PASS + FAIL
print(f"  测试总数: {total}  |  ✅ 通过: {PASS}  |  ❌ 失败: {FAIL}")
if FAIL == 0:
    print("  🎉 全部通过！联调测试完成！")
else:
    print(f"  ⚠️ 有 {FAIL} 个测试失败，请检查")
print("=" * 60)

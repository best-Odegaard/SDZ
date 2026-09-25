import requests, struct, zlib, sys

BASE = "http://localhost:8080"

# 创建测试 PNG
sig = b'\x89PNG\r\n\x1a\n'
def chunk(t, d):
    c = t + d
    return struct.pack('>I', len(d)) + c + struct.pack('>I', zlib.crc32(c) & 0xffffffff)
ihdr = struct.pack('>IIBBBBB', 1, 1, 8, 2, 0, 0, 0)
png = sig + chunk(b'IHDR', ihdr) + chunk(b'IDAT', zlib.compress(b'\x00\xff\xff\xff')) + chunk(b'IEND', b'')

print("=" * 60)
print("  图片上传 · 远程访问测试")
print("=" * 60)

# 1. 上传
r = requests.post(f"{BASE}/api/file/upload", files={"file": ("test.png", png, "image/png")}, timeout=10)
j = r.json()
print(f"\n[上传] code={j['code']}, message={j['message']}")
url = j.get("data", "")
print(f"[URL] {url}")

# 2. 验证 URL 是完整的
if url.startswith("http://"):
    print("[格式] ✅ 完整URL（含协议+主机）")
elif url.startswith("/"):
    print("[格式] ⚠️ 相对路径（需前端拼接）")
else:
    print(f"[格式] ⚠️ 未知格式: {url[:50]}")

# 3. 远程访问测试
if url:
    r2 = requests.get(url, timeout=10)
    if r2.status_code == 200:
        print(f"[访问] ✅ HTTP {r2.status_code} | 文件大小: {len(r2.content)} bytes | 类型: {r2.headers.get('Content-Type','?')}")
    else:
        print(f"[访问] ❌ HTTP {r2.status_code}")

# 4. 模拟远程访问 — 用 IP 形式访问
if url and "localhost" in url:
    remote_url = url.replace("localhost:8080", "127.0.0.1:8080")
    try:
        r3 = requests.get(remote_url, timeout=10, headers={"Host": "localhost:8080"})
        print(f"[远程] 用 127.0.0.1 访问: HTTP {r3.status_code}")
    except Exception as e:
        print(f"[远程] 127.0.0.1 访问失败: {e}")

# 5. 清理
if url:
    r4 = requests.delete(f"{BASE}/api/file/delete", params={"url": url}, timeout=10)
    j4 = r4.json()
    print(f"[删除] code={j4['code']}, message={j4['message']}")

print("\n" + "=" * 60)
print("  测试完成")
print("=" * 60)

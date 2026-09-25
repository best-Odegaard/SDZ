@echo off
setlocal enabledelayedexpansion
set BASE=http://localhost:8080

echo ============================================================
echo   赛搭子 ^· 接口联调测试报告
echo ============================================================
echo.

rem ========== 接口① 赛事提醒 ==========
echo [接口① 赛事提醒]
echo ----------------------------------------

rem 1.1 设置提醒
echo [①-1] 设置提醒 ^(userId=1, competitionId=1^)
curl -s -X POST "%BASE%/api/reminder/add?userId=1^&competitionId=1^&remindTime=2026-08-01%%2010:00:00^&remindType=1" > tmp.json
type tmp.json | python3 -c "import sys,json;d=json.load(sys.stdin);print('  状态:', '成功' if d['code']==200 else '失败', '| id:', d['data']['id'] if d.get('data') else 'N/A')"

rem 1.2 查询提醒列表
echo [①-2] 查询提醒列表
curl -s "%BASE%/api/reminder/list?userId=1" > tmp.json
type tmp.json | python3 -c "import sys,json;d=json.load(sys.stdin);print('  提醒数:', len(d.get('data',[])) if d.get('data') else 0)"

rem 1.3 取消提醒
echo [①-3] 取消提醒
for /f "usebackq tokens=*" %%a in (`python3 -c "import json;d=json.load(open('tmp.json'));print(d['data'][0]['id'] if d.get('data') and len(d['data'])>0 else '')"`) do set RID=%%a
if defined RID (
  curl -s -X DELETE "%BASE%/api/reminder/%RID%?userId=1" > tmp.json
  type tmp.json | python3 -c "import sys,json;d=json.load(sys.stdin);print('  取消:', '成功' if d['code']==200 else '失败')"
) else (
  echo   跳过：无提醒记录
)

echo.

rem ========== 接口② 智能匹配 ==========
echo [接口② 智能匹配]
echo ----------------------------------------

rem 2.1 推荐组队
echo [②-1] 推荐组队 ^(userId=1^)
curl -s "%BASE%/api/match/teams?userId=1" > tmp.json
type tmp.json | python3 -c "
import sys,json
d=json.load(sys.stdin)
if d['code']==200:
    data=d.get('data',[])
    print('  推荐组队数:', len(data))
    for item in data:
        print('    ├─', '[匹配度', item['matchScore'], '分]', item.get('title','?'), '| 共同技能:', item.get('commonSkills',''))
else:
    print('  失败:', d.get('message'))
"

rem 2.2 推荐队友
echo [②-2] 推荐队友 ^(userId=1^)
curl -s "%BASE%/api/match/mates?userId=1" > tmp.json
type tmp.json | python3 -c "
import sys,json
d=json.load(sys.stdin)
if d['code']==200:
    data=d.get('data',[])
    print('  推荐队友数:', len(data))
    for item in data:
        print('    ├─', '[匹配度', item['matchScore'], '分]', item.get('title','?'), '| 共同技能:', item.get('commonSkills',''))
else:
    print('  失败:', d.get('message'))
"

echo.

rem ========== 接口③ 图片上传 ==========
echo [接口③ 图片上传]
echo ----------------------------------------

rem 先创建一个1x1的PNG文件
python3 -c "
import struct,zlib
def create_png():
    sig = b'\\x89PNG\\r\\n\\x1a\\n'
    def chunk(ctype,data):
        c = ctype + data
        return struct.pack('>I',len(data)) + c + struct.pack('>I',zlib.crc32(c) & 0xffffffff)
    ihdr = struct.pack('>IIBBBBB',1,1,8,2,0,0,0)
    raw = b'\\x00\\xff\\xff\\xff'
    idat = zlib.compress(raw)
    return sig + chunk(b'IHDR',ihdr) + chunk(b'IDAT',idat) + chunk(b'IEND',b'')
with open('test_img.png','wb') as f:
    f.write(create_png())
print('PNG created')
"

rem 3.1 单张上传
echo [③-1] 单张上传
curl -s -X POST "%BASE%/api/file/upload" -F "file=@test_img.png" > tmp.json
type tmp.json | python3 -c "
import sys,json;d=json.load(sys.stdin)
if d['code']==200:
    print('  上传成功 ^>', d['data'])
    with open('tmp_url.txt','w') as f: f.write(d['data'])
else:
    print('  失败:', d.get('message'))
"

rem 3.2 非法格式拦截
echo [③-2] 非法格式拦截
echo not-image > bad.txt
curl -s -X POST "%BASE%/api/file/upload" -F "file=@bad.txt;filename=test.pdf" > tmp.json
type tmp.json | python3 -c "import sys,json;d=json.load(sys.stdin);print('  结果:', '已拦截(正确)' if d['code']!=200 else '未拦截(异常)')"

rem 3.3 删除图片
echo [③-3] 删除图片
if exist tmp_url.txt (
  set /p DELURL=<tmp_url.txt
  curl -s -X DELETE "%BASE%/api/file/delete?url=%DELURL%" > tmp.json
  type tmp.json | python3 -c "import sys,json;d=json.load(sys.stdin);print('  结果:', '删除成功' if d['code']==200 else '删除失败')"
) else (
  echo   跳过：无URL
)

echo.

rem ========== 接口④ 组队审核 ==========
echo [接口④ 组队审核]
echo ----------------------------------------

rem 4.1 申请加入（不要求简历）
echo [④-1] 申请组队 ^(用户3^>组队1, 不要求简历^)
curl -s -X POST "%BASE%/api/team/apply?userId=3^&teamId=1" > tmp.json
type tmp.json | python3 -c "
import sys,json;d=json.load(sys.stdin)
if d['code']==200:
    print('  申请成功, id:', d['data']['id'])
    with open('app_id.txt','w') as f: f.write(str(d['data']['id']))
else:
    print('  失败:', d.get('message'))
"

rem 4.2 要求简历但未传
echo [④-2] 申请组队 ^(要求简历但未传^)
curl -s -X POST "%BASE%/api/team/apply?userId=3^&teamId=2" > tmp.json
type tmp.json | python3 -c "import sys,json;d=json.load(sys.stdin);print('  结果:', '已拦截(正确)' if d['code']!=200 else '未拦截(异常)','|', d.get('message',''))"

rem 4.3 收到的申请
echo [④-3] 收到的申请 ^(用户1^)
curl -s "%BASE%/api/team/applications/received?userId=1" > tmp.json
type tmp.json | python3 -c "
import sys,json;d=json.load(sys.stdin)
if d['code']==200:
    data=d.get('data',[])
    print('  收到', len(data), '条申请')
    for a in data:
        print('    ├─ 申请ID:', a['id'], '| 申请人:', a['applicantId'], '| 状态:', ['待审核','已通过','已拒绝'][a['status']])
else:
    print('  失败:', d.get('message'))
"

rem 4.4 通过申请
echo [④-4] 通过申请
if exist app_id.txt (
  set /p AID=<app_id.txt
  curl -s -X PUT "%BASE%/api/team/application/process?userId=1" -H "Content-Type: application/json" -d "{"""applicationId""":%AID%, """action""":1}" > tmp.json
  type tmp.json | python3 -c "import sys,json;d=json.load(sys.stdin);print('  结果:', '通过成功' if d['code']==200 else d.get('message',''))"
) else (
  echo   跳过：无申请ID
)

rem 4.5 重复申请
echo [④-5] 重复申请拦截
curl -s -X POST "%BASE%/api/team/apply?userId=3^&teamId=1" > tmp.json
type tmp.json | python3 -c "import sys,json;d=json.load(sys.stdin);print('  结果:', '已拦截(正确)' if d['code']!=200 else '未拦截(异常)','|', d.get('message',''))"

rem 4.6 权限校验
echo [④-6] 权限校验 ^(用户3不是队长^)
if exist app_id.txt (
  set /p AID=<app_id.txt
  curl -s -X PUT "%BASE%/api/team/application/process?userId=3" -H "Content-Type: application/json" -d "{"""applicationId""":%AID%, """action""":1}" > tmp.json
  type tmp.json | python3 -c "import sys,json;d=json.load(sys.stdin);print('  结果:', '已拦截(正确)' if d['code']!=200 else '未拦截(异常)','|', d.get('message',''))"
)

echo.
echo ============================================================
echo   联调测试完成
echo ============================================================

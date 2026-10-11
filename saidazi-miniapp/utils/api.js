/**
 * 赛搭子 用户端 API 封装
 * 对应 saidazi-server (Spring Boot, /api/*) 的全部接口
 *
 * 【参数位置约定】（以 Controller 真实签名为准，2026-10-11 核实）
 *   - @RequestParam 的参数 → 放 query（request 的 params）
 *   - @RequestBody 的参数  → 放 body（request 的 data）
 *   - 混合接口（如 /team/publish）：userId 走 query，实体走 body
 *
 * 【实体适配】后端实体与页面期望的字段差异，在底部「适配层」统一补齐：
 *   - Team 缺 publisher/currentCount/members → 生成兜底对象
 *   - Post 缺 author/liked → 生成兜底对象
 *   - Comment 缺 user → 生成兜底对象
 *   - 多值字段（skills/images 等）后端是逗号字符串 → 拆成数组
 *   （这些字段最终应由后端加 VO 返回真实数据，见 docs/API接口契约.md 第 6 章）
 */
import { request, getUserId, ensureUserId, BASE_URL } from './request.js'

/* ==================== 工具 ==================== */

/**
 * userId 兜底：传了就直接用；没传（未登录）则先静默自动登录再继续。
 * 自动登录失败时整条请求失败，由页面 catch 提示。
 */
function withUserId(userId) {
  if (userId != null) return Promise.resolve(userId)
  return ensureUserId()
}

/** 逗号分隔字符串 → 数组（已是数组则原样返回，空值返回 []） */
function toList(v) {
  if (Array.isArray(v)) return v
  if (typeof v === 'string' && v.trim()) return v.split(',').map((s) => s.trim()).filter(Boolean)
  return []
}

/** 生成用户兜底对象（后端未连表返回作者信息时，页面不至于渲染报错） */
function fallbackUser(id, namePrefix = '用户') {
  return {
    id: id || null,
    nickname: id ? `${namePrefix}${id}` : '未知用户',
    avatarUrl: '',
    college: '',
    major: '',
    grade: '',
    skills: [],
  }
}

/* ==================== 用户 ==================== */
export const userApi = {
  // 微信登录：@RequestParam code(必填) nickname avatarUrl → 全部走 query
  // 后端返回 { token, userInfo }，页面 auth.js 期望 { token, user } → 这里做映射
  login: (data = {}) =>
    request({
      url: '/user/login',
      method: 'POST',
      params: { code: data.code, nickname: data.nickname, avatarUrl: data.avatarUrl },
      data: {},
    }).then((res) => ({ ...res, user: res.userInfo || res.user || res })),
  // 获取用户信息（未登录时先自动登录）
  getInfo: (userId) =>
    withUserId(userId).then((id) => request({ url: '/user/info', method: 'GET', params: { userId: id } })),
  // 更新用户信息：query 带 userId，实体走 body
  updateInfo: (data = {}) =>
    withUserId(data.userId != null ? data.userId : getUserId()).then((id) =>
      request({ url: '/user/info', method: 'PUT', params: { userId: id }, data })
    ),
}

/* ==================== 赛事 ============================== */
export const competitionApi = {
  // 赛事列表（type: 0校内 1全国，category, status, page, size）
  list: (params) => request({ url: '/competition/list', method: 'GET', params }),
  // 赛事详情（id 缺失时（空数据跳转）不发请求，返回 null）
  detail: (id) =>
    id == null || id === 'undefined' || id === 'null'
      ? Promise.resolve(null)
      : request({ url: `/competition/${id}`, method: 'GET' }),
  // 搜索
  search: (keyword) => request({ url: '/competition/search', method: 'GET', params: { keyword } }),
  // 日历事件：后端返回扁平 [{competitionId,title,eventType,eventTime}] 列表，
  // 页面期望 { month, days: { 'YYYY-MM-DD': [{date,type,title,id}] } } → 这里做转换
  // eventType: 1=报名开始 2=报名截止 3=比赛日
  calendar: (month) =>
    request({ url: '/competition/calendar', method: 'GET', params: { month } }).then((rows) => {
      if (!Array.isArray(rows)) return rows
      const target = month || ''
      const map = {}
      const TYPE_NAME = { 1: '报名开始', 2: '报名截止', 3: '比赛' }
      rows.forEach((ev) => {
        const date = String(ev.eventTime || '').slice(0, 10)
        if (!date) return
        if (target && !date.startsWith(target)) return
        if (!map[date]) map[date] = []
        map[date].push({
          date,
          type: TYPE_NAME[ev.eventType] || '比赛',
          title: ev.title,
          id: ev.competitionId,
        })
      })
      return { month: target, days: map }
    }),
}

/* ==================== 收藏 ============================== */
export const favoriteApi = {
  // @RequestParam userId competitionId → query（未登录时先自动登录）
  add: (userId, competitionId) =>
    withUserId(userId).then((id) =>
      request({ url: '/favorite/add', method: 'POST', params: { userId: id, competitionId }, data: {} })
    ),
  remove: (userId, competitionId) =>
    withUserId(userId).then((id) =>
      request({ url: '/favorite/remove', method: 'DELETE', params: { userId: id, competitionId } })
    ),
  // 后端返回 List<Competition>；页面（收藏夹）期望收藏记录形状 → 补充别名
  list: (userId) =>
    withUserId(userId).then((id) =>
      request({ url: '/favorite/list', method: 'GET', params: { userId: id } }).then((rows) =>
        Array.isArray(rows)
          ? rows.map((c) => ({ ...c, competitionId: c.competitionId || c.id, competition: c }))
          : rows
      )
    ),
  check: (userId, competitionId) =>
    competitionId == null
      ? Promise.resolve(false)
      : withUserId(userId).then((id) =>
          request({ url: '/favorite/check', method: 'GET', params: { userId: id, competitionId } })
        ),
}

/* ==================== 提醒 ============================== */
export const reminderApi = {
  // @RequestParam userId competitionId remindTime remindType → 全部 query（未登录时先自动登录）
  add: (data = {}) =>
    withUserId(data.userId != null ? data.userId : getUserId()).then((id) =>
      request({
        url: '/reminder/add',
        method: 'POST',
        params: {
          userId: id,
          competitionId: data.competitionId,
          remindTime: data.remindTime,
          remindType: data.remindType,
        },
        data: {},
      })
    ),
  remove: (id, userId) =>
    withUserId(userId).then((uid) =>
      request({ url: `/reminder/${id}`, method: 'DELETE', params: { userId: uid } })
    ),
  list: (userId) =>
    withUserId(userId).then((uid) =>
      request({ url: '/reminder/list', method: 'GET', params: { userId: uid } })
    ),
}

/* ==================== 组队 ============================== */
/** 组队实体适配：逗号字符串拆数组 + 兜底 publisher/currentCount */
function adaptTeam(t) {
  if (!t || typeof t !== 'object') return t
  return {
    ...t,
    skillsRequired: toList(t.skillsRequired),
    competitionTitle: t.competitionTitle || '',
    publisher: t.publisher || fallbackUser(t.userId, '发布者'),
    currentCount: t.currentCount != null ? t.currentCount : 1,
    members: t.members || [],
  }
}

export const teamApi = {
  // 发布组队：query 带 userId，Team 实体走 body（混合接口；未登录时先自动登录）
  publish: (data = {}) =>
    withUserId(data.userId != null ? data.userId : getUserId()).then((id) =>
      request({ url: '/team/publish', method: 'POST', params: { userId: id }, data }).then(adaptTeam)
    ),
  // 后端返回 List<Team>（非分页），页面兼容数组，原样透传并做实体适配
  list: (params) =>
    request({ url: '/team/list', method: 'GET', params }).then((rows) =>
      Array.isArray(rows) ? rows.map(adaptTeam) : rows
    ),
  detail: (id) =>
    id == null || id === 'undefined' || id === 'null'
      ? Promise.resolve(null)
      : request({ url: `/team/${id}`, method: 'GET' }).then(adaptTeam),
  // 编辑组队：query userId + path id + body Team
  update: (id, data = {}) =>
    withUserId(data.userId != null ? data.userId : getUserId()).then((uid) =>
      request({ url: `/team/${id}`, method: 'PUT', params: { userId: uid }, data })
    ),
  remove: (id, userId) =>
    withUserId(userId).then((uid) =>
      request({ url: `/team/${id}`, method: 'DELETE', params: { userId: uid } })
    ),
  myPublish: (userId) =>
    withUserId(userId).then((uid) =>
      request({ url: '/team/my-publish', method: 'GET', params: { userId: uid } }).then((rows) =>
        Array.isArray(rows) ? rows.map(adaptTeam) : rows
      )
    ),
  myApply: (userId) =>
    withUserId(userId).then((uid) =>
      request({ url: '/team/my-apply', method: 'GET', params: { userId: uid } })
    ),
  // 申请加入：@RequestParam userId teamId resumeId(选填，数字) → 全部 query
  apply: (data = {}) =>
    withUserId(data.userId != null ? data.userId : getUserId()).then((id) =>
      request({
        url: '/team/apply',
        method: 'POST',
        params: {
          userId: id,
          teamId: data.teamId,
          resumeId: data.resumeId,
        },
        data: {},
      })
    ),
  // 审核申请：query userId + body {applicationId, status}
  processApplication: (data = {}) =>
    withUserId(data.userId != null ? data.userId : getUserId()).then((id) =>
      request({
        url: '/team/application/process',
        method: 'PUT',
        params: { userId: id },
        data: { applicationId: data.applicationId, status: data.status },
      })
    ),
  receivedApplications: (userId) =>
    withUserId(userId).then((uid) =>
      request({ url: '/team/applications/received', method: 'GET', params: { userId: uid } })
    ),
}

/* ==================== 智能匹配 ============================== */
export const matchApi = {
  // 推荐组队（未登录时先自动登录）
  teams: (userId, competitionId) =>
    withUserId(userId).then((uid) =>
      request({ url: '/match/teams', method: 'GET', params: { userId: uid, competitionId } }).then((rows) =>
        Array.isArray(rows) ? rows.map((r) => adaptTeam(r.team || r)) : rows
      )
    ),
  // 推荐队友（未登录时先自动登录）
  mates: (userId, competitionId) =>
    withUserId(userId).then((uid) =>
      request({ url: '/match/mates', method: 'GET', params: { userId: uid, competitionId } }).then((rows) =>
        Array.isArray(rows)
          ? rows.map((r) => {
              const u = r.user || r
              return {
                ...u,
                userId: u.userId || u.id,
                skills: toList(u.skills),
                matchScore: r.matchScore,
                matchTags: toList(r.matchTags),
              }
            })
          : rows
      )
    ),
}

/* ==================== 简历 ============================== */
/** 简历实体适配：skills 逗号字符串 → 数组 */
function adaptResume(r) {
  if (!r || typeof r !== 'object') return r
  return { ...r, skills: toList(r.skills) }
}

export const resumeApi = {
  my: (userId) =>
    withUserId(userId).then((uid) =>
      request({ url: '/resume/my', method: 'GET', params: { userId: uid } }).then(adaptResume)
    ),
  // 保存简历：query userId + body Resume（collegeMajor 必填）
  save: (data = {}) =>
    withUserId(data.userId != null ? data.userId : getUserId()).then((id) =>
      request({ url: '/resume/save', method: 'POST', params: { userId: id }, data }).then(adaptResume)
    ),
  getByUserId: (userId) => request({ url: `/resume/${userId}`, method: 'GET' }).then(adaptResume),
  // 上传附件简历：query userId + multipart file（query 参数，不能写死 localhost）
  upload: (userId, filePath) => {
    return new Promise((resolve, reject) => {
      uni.uploadFile({
        url: `${BASE_URL}/resume/upload?userId=${userId}`,
        filePath: filePath,
        name: 'file',
        success: (res) => {
          try {
            const body = JSON.parse(res.data)
            if (body.code === 200) resolve(body.data)
            else reject(body)
          } catch (e) {
            reject(e)
          }
        },
        fail: reject,
      })
    })
  },
  sentHistory: (userId) =>
    withUserId(userId).then((uid) =>
      request({ url: '/resume/sent-history', method: 'GET', params: { userId: uid } })
    ),
}

/* ==================== 帖子/社区 ============================== */
/** 帖子实体适配：author/liked 兜底 + images 拆数组 */
function adaptPost(p) {
  if (!p || typeof p !== 'object') return p
  return {
    ...p,
    author: p.author || fallbackUser(p.userId, '用户'),
    images: toList(p.images),
    liked: !!p.liked,
  }
}

/** 评论实体适配：user 兜底 */
function adaptComment(c) {
  if (!c || typeof c !== 'object') return c
  return { ...c, user: c.user || fallbackUser(c.userId, '用户') }
}

export const postApi = {
  list: (params) =>
    request({ url: '/post/list', method: 'GET', params }).then((data) => {
      if (Array.isArray(data)) return data.map(adaptPost)
      if (data && Array.isArray(data.list)) data.list = data.list.map(adaptPost)
      if (data && Array.isArray(data.records)) data.records = data.records.map(adaptPost)
      return data
    }),
  detail: (id) =>
    id == null || id === 'undefined' || id === 'null'
      ? Promise.resolve(null)
      : request({ url: `/post/${id}`, method: 'GET' }).then(adaptPost),
  // 发布帖子：query userId + body Post（未登录时先自动登录）
  publish: (data = {}) =>
    withUserId(data.userId != null ? data.userId : getUserId()).then((id) =>
      request({ url: '/post/publish', method: 'POST', params: { userId: id }, data }).then(adaptPost)
    ),
  // 编辑帖子：query userId + path id + body Post
  update: (id, data = {}) =>
    withUserId(data.userId != null ? data.userId : getUserId()).then((uid) =>
      request({ url: `/post/${id}`, method: 'PUT', params: { userId: uid }, data })
    ),
  remove: (id, userId) =>
    withUserId(userId).then((uid) =>
      request({ url: `/post/${id}`, method: 'DELETE', params: { userId: uid } })
    ),
  toggleComment: (id, userId) =>
    withUserId(userId).then((uid) =>
      request({ url: `/post/${id}/comment-toggle`, method: 'PUT', params: { userId: uid } })
    ),
  // 点赞：@RequestParam userId → query；返回 Boolean
  like: (id, userId) =>
    withUserId(userId).then((uid) =>
      request({ url: `/post/${id}/like`, method: 'POST', params: { userId: uid }, data: {} })
    ),
  // 发评论：@RequestParam userId content → 全部 query
  comment: (id, data = {}) =>
    withUserId(data.userId != null ? data.userId : getUserId()).then((id2) =>
      request({
        url: `/post/${id}/comment`,
        method: 'POST',
        params: { userId: id2, content: data.content },
        data: {},
      }).then(adaptComment)
    ),
  comments: (id) =>
    request({ url: `/post/${id}/comments`, method: 'GET' }).then((rows) =>
      Array.isArray(rows) ? rows.map(adaptComment) : rows
    ),
  my: (userId) =>
    withUserId(userId).then((uid) =>
      request({ url: '/post/my', method: 'GET', params: { userId: uid } }).then((data) =>
        Array.isArray(data) ? data.map(adaptPost) : data
      )
    ),
}

/* ==================== 往届成果 ============================== */
export const achievementApi = {
  // competitionId 缺失时返回空列表，不发请求
  list: (competitionId) =>
    competitionId == null
      ? Promise.resolve([])
      : request({ url: '/achievement/list', method: 'GET', params: { competitionId } }),
}

/* ==================== 文件 ============================== */
export const fileApi = {
  upload: (filePath) => {
    return new Promise((resolve, reject) => {
      uni.uploadFile({
        url: `${BASE_URL}/file/upload`,
        filePath: filePath,
        name: 'file',
        success: (res) => {
          try {
            const body = JSON.parse(res.data)
            if (body.code === 200) resolve(body.data)
            else reject(body)
          } catch (e) {
            reject(e)
          }
        },
        fail: reject,
      })
    })
  },
  uploadMultiple: (filePaths) => {
    return Promise.all(filePaths.map((p) => fileApi.upload(p)))
  },
  remove: (fileName) => request({ url: '/file/delete', method: 'DELETE', params: { fileName } }),
}

/**
 * 请求封装 - 基于 uni.request
 * 自动携带 JWT、统一错误处理、统一返回 data
 *
 * 【Mock 开关】
 *   USE_MOCK = true  → 默认全部走 utils/mock.js 本地假数据（界面开发阶段，无需启动后端）
 *   USE_MOCK = false → 全部走真实 saidazi-server 接口
 *
 * 【真假数据分流】
 *   在 USE_MOCK = true 时，路径命中 REAL_API_KEYWORDS（如 competition）的请求
 *   会绕过 Mock、直连真实云服务器；其余路径仍走本地假数据。
 *   页面代码无需任何修改，两者返回结构完全一致。
 */
import { mockApi } from './mock.js'

export const USE_MOCK = true

const BASE_URL = 'http://134.175.90.165:8083/api'

/* ==================== 真假数据分流 ====================
 * 路径中包含以下关键字的请求走真实云服务器接口，
 * 其余请求继续走本地 Mock 假数据。
 * =================================================== */
const REAL_API_KEYWORDS = ['competition']

function shouldUseRealApi(url) {
  return REAL_API_KEYWORDS.some((kw) => String(url || '').toLowerCase().includes(kw))
}

/* ==================== Mock 路由表 ====================
 * key: `${METHOD} ${url 正则}`，按顺序匹配，命中即返回 mock 数据
 * 说明：URL 中可能带 path 参数（如 /team/12），用正则捕获后转成参数透传
 * =================================================== */
const MOCK_ROUTES = [
  /* ---- 用户 ---- */
  { re: /^\/user\/login$/, method: 'POST', fn: (p) => mockApi.userInfo(p) },
  { re: /^\/user\/info$/, method: 'GET', fn: (p) => mockApi.userInfo(p) },
  { re: /^\/user\/info$/, method: 'PUT', fn: (p) => mockApi.userInfo(p) },

  /* ---- 赛事 ---- */
  { re: /^\/competition\/list$/, method: 'GET', fn: (p) => mockApi.competitionList(p) },
  { re: /^\/competition\/search$/, method: 'GET', fn: (p) => mockApi.competitionSearch(p.keyword) },
  { re: /^\/competition\/calendar$/, method: 'GET', fn: (p) => mockApi.competitionCalendar(p.month) },
  { re: /^\/competition\/(\d+)$/, method: 'GET', fn: (p, m) => mockApi.competitionDetail(m[1]) },

  /* ---- 收藏 ---- */
  { re: /^\/favorite\/list$/, method: 'GET', fn: () => mockApi.favoriteList() },
  { re: /^\/favorite\/check$/, method: 'GET', fn: (p) => mockApi.favoriteCheck(p.competitionId) },
  { re: /^\/favorite\/add$/, method: 'POST', fn: (p) => mockApi.favoriteAdd(p.competitionId) },
  { re: /^\/favorite\/remove$/, method: 'DELETE', fn: (p) => mockApi.favoriteRemove(p.competitionId) },

  /* ---- 提醒 ---- */
  { re: /^\/reminder\/list$/, method: 'GET', fn: () => mockApi.reminderList() },
  { re: /^\/reminder\/add$/, method: 'POST', fn: (p) => mockApi.reminderAdd(p) },
  { re: /^\/reminder\/(\d+)$/, method: 'DELETE', fn: (p, m) => mockApi.reminderRemove(m[1]) },

  /* ---- 组队 ---- */
  { re: /^\/team\/list$/, method: 'GET', fn: (p) => mockApi.teamList(p) },
  { re: /^\/team\/publish$/, method: 'POST', fn: (p) => mockApi.teamPublish(p) },
  { re: /^\/team\/apply$/, method: 'POST', fn: (p) => mockApi.teamApply(p) },
  { re: /^\/team\/application\/process$/, method: 'PUT', fn: (p) => mockApi.processApplication(p) },
  { re: /^\/team\/my-publish$/, method: 'GET', fn: () => mockApi.myPublish() },
  { re: /^\/team\/my-apply$/, method: 'GET', fn: () => mockApi.myApply() },
  { re: /^\/team\/applications\/received$/, method: 'GET', fn: () => mockApi.receivedApplications() },
  { re: /^\/team\/(\d+)$/, method: 'GET', fn: (p, m) => mockApi.teamDetail(m[1]) },

  /* ---- 智能匹配 ---- */
  { re: /^\/match\/teams$/, method: 'GET', fn: () => mockApi.matchTeams() },
  { re: /^\/match\/mates$/, method: 'GET', fn: () => mockApi.matchMates() },

  /* ---- 简历 ---- */
  { re: /^\/resume\/my$/, method: 'GET', fn: () => mockApi.resumeMy() },
  { re: /^\/resume\/save$/, method: 'POST', fn: (p) => mockApi.resumeSave(p) },
  { re: /^\/resume\/sent-history$/, method: 'GET', fn: () => mockApi.sentHistory() },
  { re: /^\/resume\/(\d+)$/, method: 'GET', fn: (p, m) => mockApi.resumeByUserId(m[1]) },

  /* ---- 帖子 / 社区 ---- */
  { re: /^\/post\/list$/, method: 'GET', fn: (p) => mockApi.postList(p) },
  { re: /^\/post\/publish$/, method: 'POST', fn: (p) => mockApi.postPublish(p) },
  { re: /^\/post\/my$/, method: 'GET', fn: () => mockApi.postMy() },
  { re: /^\/post\/(\d+)\/comments$/, method: 'GET', fn: (p, m) => mockApi.postComments(m[1]) },
  { re: /^\/post\/(\d+)\/comment$/, method: 'POST', fn: (p, m) => mockApi.postComment(m[1], p) },
  { re: /^\/post\/(\d+)\/like$/, method: 'POST', fn: (p, m) => mockApi.postLike(m[1]) },
  { re: /^\/post\/(\d+)$/, method: 'GET', fn: (p, m) => mockApi.postDetail(m[1]) },

  /* ---- 往届成果 ---- */
  { re: /^\/achievement\/list$/, method: 'GET', fn: (p) => mockApi.achievementList(p.competitionId) },
]

/** 命中 mock 路由则返回 Promise，否则返回 null 表示需走真实请求 */
function tryMock(method, url, params, data) {
  if (!USE_MOCK) return null
  // 分流：命中真实接口关键字的请求（如 competition）不走 Mock，直连云服务器
  if (shouldUseRealApi(url)) return null
  const M = method.toUpperCase()
  for (const route of MOCK_ROUTES) {
    if (route.method !== M) continue
    const m = route.re.exec(url)
    if (!m) continue
    // 合并 params 与 data 作为 mock 入参
    const payload = { ...(params || {}), ...(data || {}) }
    try {
      return Promise.resolve(route.fn(payload, m))
    } catch (e) {
      return Promise.reject(e)
    }
  }
  return null
}

function getToken() {
  return uni.getStorageSync('token') || ''
}

export function setToken(token) {
  if (token) {
    uni.setStorageSync('token', token)
  } else {
    uni.removeStorageSync('token')
  }
}

export function isLoggedIn() {
  // Mock 模式下视为已登录，方便直接预览各页面
  if (USE_MOCK) return true
  return !!getToken()
}

export function getUserId() {
  // Mock 模式固定返回 1，与 mock.js 中 currentUser.id 对应
  if (USE_MOCK) return 1
  const info = uni.getStorageSync('userInfo')
  if (info) {
    try { return JSON.parse(info).id } catch (e) { return null }
  }
  return null
}

/**
 * 通用请求
 * @param {Object} options { url, method, data, params, hideError }
 */
export function request(options) {
  const { url, method = 'GET', data, params, hideError = false } = options

  // —— Mock 优先 ——
  const mocked = tryMock(method, url, params, data)
  if (mocked) return mocked

  let fullUrl = BASE_URL + url
  if (params && Object.keys(params).length) {
    const qs = Object.keys(params)
      .filter(k => params[k] !== null && params[k] !== undefined && params[k] !== '')
      .map(k => `${encodeURIComponent(k)}=${encodeURIComponent(params[k])}`)
      .join('&')
    if (qs) fullUrl += '?' + qs
  }
  return new Promise((resolve, reject) => {
    uni.request({
      url: fullUrl,
      method: method.toUpperCase(),
      data: data,
      header: {
        'Content-Type': 'application/json',
        'Authorization': getToken() ? `Bearer ${getToken()}` : ''
      },
      success: (res) => {
        if (res.statusCode >= 200 && res.statusCode < 300) {
          const body = res.data || {}
          if (body.code === 200 || body.code === 0 || body.success) {
            resolve(body.data !== undefined ? body.data : body)
          } else {
            if (!hideError) {
              uni.showToast({ title: body.message || '请求失败', icon: 'none' })
            }
            reject(body)
          }
        } else if (res.statusCode === 401) {
          uni.showToast({ title: '请先登录', icon: 'none' })
          reject(res.data)
        } else {
          if (!hideError) {
            uni.showToast({ title: `网络错误 ${res.statusCode}`, icon: 'none' })
          }
          reject(res.data)
        }
      },
      fail: (err) => {
        if (!hideError) {
          uni.showToast({ title: '网络异常', icon: 'none' })
        }
        reject(err)
      }
    })
  })
}

export default { request, setToken, getToken, isLoggedIn, getUserId, BASE_URL, USE_MOCK }

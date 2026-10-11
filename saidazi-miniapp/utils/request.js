/**
 * 请求封装 - 基于 uni.request
 * 自动携带 JWT、统一错误处理、统一返回 data
 *
 * 【Mock 开关】
 *   USE_MOCK = true  → 全部请求走 utils/mock.js 本地假数据（界面开发阶段，无需启动后端）
 *   USE_MOCK = false → 走真实 saidazi-server 接口
 *   页面代码无需任何修改，两者返回结构完全一致。
 */
import { mockApi } from './mock.js'

export const USE_MOCK = false

/* ==================== 后端地址配置 ====================
 * 改这一行就能切换后端，不用动其他任何代码：
 *
 *   本地后端（需要自己在电脑上启动 saidazi-server）
 *     'http://localhost:8080/api'
 *
 *   团队服务器后端（师兄部署的公网服务器）
 *     'http://134.175.90.165:8080/api'
 *
 * 注意：切换到真实后端时，务必把上面的 USE_MOCK 改成 false，
 *      否则请求仍会被本地假数据拦截。
 * ==================================================== */
export const BASE_URL = 'http://134.175.90.165:8080/api'

/* ==================== 响应数据归一化 ====================
 * 后端返回与页面期望之间存在少量系统性差异，在这里统一转换，
 * 页面代码不需要感知：
 *   1. 后端 LocalDateTime 序列化成 "2026-10-10T23:28:54.75131765"
 *      → 统一转成 "2026-10-10 23:28:55"
 *   2. 后端分页字段叫 records → 补一个 list 字段指向同一数组
 * ==================================================== */

const ISO_TIME_RE = /^(\d{4}-\d{2}-\d{2})T(\d{2}:\d{2}:\d{2})(?:\.\d+)?$/

function normalizeTimeStr(v) {
  const m = ISO_TIME_RE.exec(v)
  if (!m) return v
  return `${m[1]} ${m[2]}`
}

function deepNormalize(node, depth = 0) {
  if (depth > 6 || node === null || node === undefined) return node
  if (typeof node === 'string') return normalizeTimeStr(node)
  if (Array.isArray(node)) {
    node.forEach((item, i) => { node[i] = deepNormalize(item, depth + 1) })
    return node
  }
  if (typeof node === 'object') {
    // 后端分页 { records, total, page, size } → 补 list 别名
    if (Array.isArray(node.records)) node.list = node.records
    Object.keys(node).forEach((k) => {
      node[k] = deepNormalize(node[k], depth + 1)
    })
    return node
  }
  return node
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

/* ==================== 自动登录 ====================
 * 小程序首次使用时本地没有 userId，页面请求"必填 userId"的接口会 500。
 * ensureUserId()：本地有就直接返回；没有就静默走一遍微信登录
 *   （uni.login 拿 code → 后端 /user/login 换 token + userInfo → 存本地）。
 * 并发调用会共享同一次登录（loginInFlight 去重），不会重复请求。
 * ================================================= */
let loginInFlight = null

export function ensureUserId() {
  const cached = getUserId()
  if (cached != null) return Promise.resolve(cached)
  if (USE_MOCK) return Promise.resolve(1)
  if (!loginInFlight) {
    loginInFlight = new Promise((resolve, reject) => {
      uni.login({
        provider: 'weixin',
        success: (loginRes) => {
          if (!loginRes || !loginRes.code) {
            loginInFlight = null
            return reject(new Error('uni.login 未返回 code'))
          }
          request({
            url: '/user/login',
            method: 'POST',
            params: { code: loginRes.code, nickname: '微信用户' },
            data: {},
            hideError: true,
          })
            .then((data) => {
              if (data && data.token) setToken(data.token)
              const user = data && (data.user || data.userInfo)
              if (user) uni.setStorageSync('userInfo', JSON.stringify(user))
              loginInFlight = null
              resolve(user && user.id != null ? user.id : null)
            })
            .catch((e) => {
              loginInFlight = null
              reject(e)
            })
        },
        fail: (e) => {
          loginInFlight = null
          reject(e)
        },
      })
    })
  }
  return loginInFlight
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
            const data = deepNormalize(body.data !== undefined ? body.data : body)
            resolve(data)
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

export default { request, setToken, getToken, isLoggedIn, getUserId, ensureUserId, BASE_URL, USE_MOCK }

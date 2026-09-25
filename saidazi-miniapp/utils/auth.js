/**
 * 登录态与用户工具
 */
import { setToken, isLoggedIn, getUserId, request } from './request.js'
import { userApi } from './api.js'

// re-export：页面统一从 auth.js 引入用户相关方法，避免跨模块引用路径不一致
export { isLoggedIn, getUserId, setToken }

/**
 * 微信小程序登录流程
 * 1. uni.login 获取 code
 * 2. 调后端 /user/login 换 openId + token
 * 3. 缓存 token + userInfo
 */
export function wxLogin() {
  return new Promise((resolve, reject) => {
    uni.login({
      provider: 'weixin',
      success: (loginRes) => {
        userApi.login({ code: loginRes.code }).then(data => {
          if (data && data.token) setToken(data.token)
          if (data && data.user) {
            uni.setStorageSync('userInfo', JSON.stringify(data.user))
          }
          resolve(data)
        }).catch(reject)
      },
      fail: reject
    })
  })
}

/**
 * 确保已登录，否则触发登录
 */
export function ensureLogin() {
  return new Promise((resolve, reject) => {
    if (isLoggedIn()) {
      resolve(getUserId())
    } else {
      wxLogin().then(data => {
        const uid = data && data.user ? data.user.id : null
        resolve(uid)
      }).catch(reject)
    }
  })
}

/**
 * 退出登录
 */
export function logout() {
  setToken(null)
  uni.removeStorageSync('userInfo')
}

/**
 * 格式化时间
 */
export function formatTime(dateStr, withTime = true) {
  if (!dateStr) return ''
  const d = new Date(dateStr.replace(/-/g, '/'))
  const pad = (n) => (n < 10 ? '0' + n : n)
  const ymd = `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`
  if (!withTime) return ymd
  return `${ymd} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

/**
 * 报名状态计算（基于时间）
 */
export function calcRegStatus(comp) {
  if (!comp) return ''
  const now = Date.now()
  const start = comp.regStartTime ? new Date(comp.regStartTime.replace(/-/g, '/')).getTime() : 0
  const end = comp.regEndTime ? new Date(comp.regEndTime.replace(/-/g, '/')).getTime() : 0
  if (now < start) return '未开始'
  if (now > end) return '已截止'
  return '报名中'
}

/* ==================== 展示层通用工具 ==================== */

/** 报名状态的样式类名 */
export function regStatusClass(status) {
  return { '报名中': 'reg-ing', '未开始': 'reg-wait', '已截止': 'reg-end' }[status] || 'reg-wait'
}

/** 距报名截止还剩多少天，负数表示已截止 */
export function daysToRegEnd(comp) {
  if (!comp || !comp.regEndTime) return null
  const end = new Date(comp.regEndTime.replace(/-/g, '/')).getTime()
  return Math.ceil((end - Date.now()) / 86400000)
}

/** 相对时间：刚刚 / N分钟前 / N小时前 / N天前 / 具体日期 */
export function fromNow(dateStr) {
  if (!dateStr) return ''
  const t = new Date(dateStr.replace(/-/g, '/')).getTime()
  const diff = Date.now() - t
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
  if (diff < 7 * 86400000) return `${Math.floor(diff / 86400000)}天前`
  return formatTime(dateStr, false)
}

/** 难度中文名 */
export function difficultyName(d) {
  return { 1: '入门', 2: '进阶', 3: '高阶' }[Number(d)] || '入门'
}

/** 难度样式类名 */
export function difficultyClass(d) {
  return `tag-diff-${Number(d) || 1}`
}

/** 帖子分类中文名 */
export const POST_CATEGORIES = [
  { value: 1, label: '求助答疑' },
  { value: 2, label: '备赛经验' },
  { value: 3, label: '资料分享' },
  { value: 4, label: '赛事闲聊' },
]

/** 帖子分类中文名 */
export function postCategoryName(c) {
  const hit = POST_CATEGORIES.find((i) => i.value === Number(c))
  return hit ? hit.label : '其他'
}

/** 赛事分类选项 */
export const COMPETITION_CATEGORIES = ['全部', '学科竞赛', '创新创业', '体育竞技', '文艺活动', '其他']

/** 年级选项 */
export const GRADES = ['全部', '大一', '大二', '大三', '大四', '研一']

/** 组队状态中文名 */
export function teamStatusName(s) {
  return { 0: '招募中', 1: '已满员', 2: '已关闭' }[Number(s)] || '未知'
}

/** 申请状态中文名 */
export function applyStatusName(s) {
  return { 0: '待审核', 1: '已通过', 2: '已拒绝' }[Number(s)] || '未知'
}

/** 姓名首字节（无头像时用作占位） */
export function avatarText(name) {
  if (!name) return '?'
  return String(name).trim().charAt(0)
}

/** 空值兜底展示 */
export function orDash(v) {
  return v === null || v === undefined || v === '' ? '—' : v
}

export default {
  wxLogin, ensureLogin, logout, formatTime, calcRegStatus, isLoggedIn, getUserId,
  regStatusClass, daysToRegEnd, fromNow, difficultyName, difficultyClass,
  POST_CATEGORIES, postCategoryName, COMPETITION_CATEGORIES, GRADES,
  teamStatusName, applyStatusName, avatarText, orDash
}
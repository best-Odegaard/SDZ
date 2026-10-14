/**
 * 赛搭子 用户端 API 封装
 * 对应 saidazi-server (Spring Boot, /api/*) 的全部接口
 */
import { request } from './request.js'

/* ============== 用户 ============== */
export const userApi = {
  // 微信登录（code 换 openId）
  login: (data) => request({ url: '/user/login', method: 'POST', data }),
  // 获取用户信息
  getInfo: (userId) => request({ url: '/user/info', method: 'GET', params: { userId } }),
  // 更新用户信息
  updateInfo: (data) => request({ url: '/user/info', method: 'PUT', data }),
}

/* ============== 赛事 ============== */
export const competitionApi = {
  // 赛事列表（type: 0校内 1全国，category, status, page, size）
  list: (params) => request({ url: '/competition/list', method: 'GET', params }),
  // 赛事详情
  detail: (id) => request({ url: `/competition/${id}`, method: 'GET' }),
  // 搜索
  search: (keyword) => request({ url: '/competition/search', method: 'GET', params: { keyword } }),
  // 日历事件
  calendar: (month) => request({ url: '/competition/calendar', method: 'GET', params: { month } }),
}

/* ============== 收藏 ============== */
export const favoriteApi = {
  add: (userId, competitionId) => request({ url: '/favorite/add', method: 'POST', data: { userId, competitionId } }),
  remove: (userId, competitionId) => request({ url: '/favorite/remove', method: 'DELETE', params: { userId, competitionId } }),
  list: (userId) => request({ url: '/favorite/list', method: 'GET', params: { userId } }),
  check: (userId, competitionId) => request({ url: '/favorite/check', method: 'GET', params: { userId, competitionId } }),
}

/* ============== 提醒 ============== */
export const reminderApi = {
  add: (data) => request({ url: '/reminder/add', method: 'POST', data }),
  remove: (id, userId) => request({ url: `/reminder/${id}`, method: 'DELETE', params: { userId } }),
  list: (userId) => request({ url: '/reminder/list', method: 'GET', params: { userId } }),
}

/* ============== 组队 ============== */
export const teamApi = {
  publish: (data) => request({ url: '/team/publish', method: 'POST', data }),
  list: (params) => request({ url: '/team/list', method: 'GET', params }),
  detail: (id) => request({ url: `/team/${id}`, method: 'GET' }),
  update: (id, data) => request({ url: `/team/${id}`, method: 'PUT', data }),
  remove: (id, userId) => request({ url: `/team/${id}`, method: 'DELETE', params: { userId } }),
  myPublish: (userId) => request({ url: '/team/my-publish', method: 'GET', params: { userId } }),
  myApply: (userId) => request({ url: '/team/my-apply', method: 'GET', params: { userId } }),
  apply: (data) => request({ url: '/team/apply', method: 'POST', data }),
  processApplication: (data) => request({ url: '/team/application/process', method: 'PUT', data }),
  receivedApplications: (userId) => request({ url: '/team/applications/received', method: 'GET', params: { userId } }),
}

/* ============== 智能匹配 ============== */
export const matchApi = {
  // 推荐组队
  teams: (userId, competitionId) => request({ url: '/match/teams', method: 'GET', params: { userId, competitionId } }),
  // 推荐队友
  mates: (userId, competitionId) => request({ url: '/match/mates', method: 'GET', params: { userId, competitionId } }),
}

/* ============== 简历 ============== */
export const resumeApi = {
  my: (userId) => request({ url: '/resume/my', method: 'GET', params: { userId } }),
  save: (data) => request({ url: '/resume/save', method: 'POST', data }),
  getByUserId: (userId) => request({ url: `/resume/${userId}`, method: 'GET' }),
  upload: (userId, filePath) => {
    return new Promise((resolve, reject) => {
      uni.uploadFile({
        url: 'http://134.175.90.165:8083/api/resume/upload?userId=' + userId,
        filePath: filePath,
        name: 'file',
        success: (res) => {
          const body = JSON.parse(res.data)
          if (body.code === 200) resolve(body.data)
          else reject(body)
        },
        fail: reject
      })
    })
  },
  sentHistory: (userId) => request({ url: '/resume/sent-history', method: 'GET', params: { userId } }),
}

/* ============== 帖子/社区 ============== */
export const postApi = {
  list: (params) => request({ url: '/post/list', method: 'GET', params }),
  detail: (id) => request({ url: `/post/${id}`, method: 'GET' }),
  publish: (data) => request({ url: '/post/publish', method: 'POST', data }),
  update: (id, data) => request({ url: `/post/${id}`, method: 'PUT', data }),
  remove: (id, userId) => request({ url: `/post/${id}`, method: 'DELETE', params: { userId } }),
  toggleComment: (id, userId) => request({ url: `/post/${id}/comment-toggle`, method: 'PUT', params: { userId } }),
  like: (id, userId) => request({ url: `/post/${id}/like`, method: 'POST', params: { userId } }),
  comment: (id, data) => request({ url: `/post/${id}/comment`, method: 'POST', data }),
  comments: (id) => request({ url: `/post/${id}/comments`, method: 'GET' }),
  my: (userId) => request({ url: '/post/my', method: 'GET', params: { userId } }),
}

/* ============== 往届成果 ============== */
export const achievementApi = {
  list: (competitionId) => request({ url: '/achievement/list', method: 'GET', params: { competitionId } }),
}

/* ============== 文件 ============== */
export const fileApi = {
  upload: (filePath) => {
    return new Promise((resolve, reject) => {
      uni.uploadFile({
        url: 'http://134.175.90.165:8083/api/file/upload',
        filePath: filePath,
        name: 'file',
        success: (res) => {
          const body = JSON.parse(res.data)
          if (body.code === 200) resolve(body.data)
          else reject(body)
        },
        fail: reject
      })
    })
  },
  uploadMultiple: (filePaths) => {
    return Promise.all(filePaths.map(p => fileApi.upload(p)))
  },
  remove: (fileName) => request({ url: '/file/delete', method: 'DELETE', params: { fileName } }),
}

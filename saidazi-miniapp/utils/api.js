/**
 * 赛搭子 用户端 API 封装
 * 对应 saidazi-server (Spring Boot, /api/*) 的全部接口
 */
import { request } from './request.js'

/* ============== 用户 ============== */
export const userApi = {
  // 微信登录（code 换 openId）—— 后端 @RequestParam，需拼 URL
  login: (data) => request({ url: '/user/login', method: 'POST', params: data }),
  // 获取用户信息
  getInfo: (userId) => request({ url: '/user/info', method: 'GET', params: { userId } }),
  // 更新用户信息
  updateInfo: (data) => request({ url: '/user/info', method: 'PUT', params: { userId: data.userId }, data }),
}

/* ============== 赛事 ============== */
export const competitionApi = {
  // 赛事列表（type: 0校内 1全国，category, status, page, size）
  list: (params) => request({ url: '/competition/list', method: 'GET', params }),
  // 赛事详情
  detail: (id) => request({ url: `/competition/${id}`, method: 'GET' }),
  // 搜索
  search: (keyword) => request({ url: '/competition/search', method: 'GET', params: { keyword } }),
  // 日历事件（后端参数为 year、month 两个整数）
  calendar: (year, month) => request({ url: '/competition/calendar', method: 'GET', params: { year, month } }),
}

/* ============== 收藏 ============== */
export const favoriteApi = {
  // 后端 add 为 @RequestParam，需拼 URL
  add: (userId, competitionId) => request({ url: '/favorite/add', method: 'POST', params: { userId, competitionId } }),
  remove: (userId, competitionId) => request({ url: '/favorite/remove', method: 'DELETE', params: { userId, competitionId } }),
  list: (userId) => request({ url: '/favorite/list', method: 'GET', params: { userId } }),
  check: (userId, competitionId) => request({ url: '/favorite/check', method: 'GET', params: { userId, competitionId } }),
}

/* ============== 提醒 ============== */
export const reminderApi = {
  // 后端全部 @RequestParam，需拼 URL
  add: (data) => request({ url: '/reminder/add', method: 'POST', params: data }),
  remove: (id, userId) => request({ url: `/reminder/${id}`, method: 'DELETE', params: { userId } }),
  list: (userId) => request({ url: '/reminder/list', method: 'GET', params: { userId } }),
}

/* ============== 组队 ============== */
export const teamApi = {
  // userId 走 URL，team 对象走 body
  publish: (data) => {
    const { userId, ...body } = data
    return request({ url: '/team/publish', method: 'POST', params: { userId }, data: body })
  },
  list: (params) => request({ url: '/team/list', method: 'GET', params }),
  detail: (id) => request({ url: `/team/${id}`, method: 'GET' }),
  update: (id, data) => request({ url: `/team/${id}`, method: 'PUT', params: { userId: data.userId }, data }),
  remove: (id, userId) => request({ url: `/team/${id}`, method: 'DELETE', params: { userId } }),
  myPublish: (userId) => request({ url: '/team/my-publish', method: 'GET', params: { userId } }),
  myApply: (userId) => request({ url: '/team/my-apply', method: 'GET', params: { userId } }),
  // userId / teamId / resumeId 全部 @RequestParam
  apply: (data) => {
    const { userId, applicantId, teamId, ...rest } = data
    return request({
      url: '/team/apply', method: 'POST',
      params: { userId: userId || applicantId, teamId, resumeId: rest.resumeId }
    })
  },
  // userId 走 URL，{applicationId, action} 走 body（兼容页面传 status）
  processApplication: (data) => {
    const { userId, ...rest } = data
    const body = { applicationId: rest.applicationId, action: rest.action !== undefined ? rest.action : rest.status }
    return request({ url: '/team/application/process', method: 'PUT', params: { userId }, data: body })
  },
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
  // userId 走 URL，resume 对象走 body
  save: (data) => {
    const { userId, ...body } = data
    return request({ url: '/resume/save', method: 'POST', params: { userId }, data: body })
  },
  getByUserId: (userId) => request({ url: `/resume/${userId}`, method: 'GET' }),
  upload: (userId, filePath) => {
    return new Promise((resolve, reject) => {
      uni.uploadFile({
        url: 'http://134.175.90.165:8080/api/resume/upload?userId=' + userId,
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
  // userId 走 URL，post 对象走 body
  publish: (data) => {
    const { userId, ...body } = data
    return request({ url: '/post/publish', method: 'POST', params: { userId }, data: body })
  },
  update: (id, data) => request({ url: `/post/${id}`, method: 'PUT', params: { userId: data.userId }, data }),
  remove: (id, userId) => request({ url: `/post/${id}`, method: 'DELETE', params: { userId } }),
  toggleComment: (id, userId) => request({ url: `/post/${id}/comment-toggle`, method: 'PUT', params: { userId } }),
  like: (id, userId) => request({ url: `/post/${id}/like`, method: 'POST', params: { userId } }),
  // 后端 userId / content 均为 @RequestParam，全部拼 URL
  comment: (id, data) => request({ url: `/post/${id}/comment`, method: 'POST', params: data }),
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
        url: 'http://134.175.90.165:8080/api/file/upload',
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
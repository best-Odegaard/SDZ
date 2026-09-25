import request from '@/utils/request'

// 查询往届成果列表
export function listAchievement(query) {
  return request({
    url: '/saidazi/achievement/list',
    method: 'get',
    params: query
  })
}

// 查询往届成果详细
export function getAchievement(id) {
  return request({
    url: '/saidazi/achievement/' + id,
    method: 'get'
  })
}

// 新增往届成果
export function addAchievement(data) {
  return request({
    url: '/saidazi/achievement',
    method: 'post',
    data: data
  })
}

// 修改往届成果
export function updateAchievement(data) {
  return request({
    url: '/saidazi/achievement',
    method: 'put',
    data: data
  })
}

// 删除往届成果
export function delAchievement(id) {
  return request({
    url: '/saidazi/achievement/' + id,
    method: 'delete'
  })
}
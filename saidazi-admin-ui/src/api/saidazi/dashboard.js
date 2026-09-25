import request from '@/utils/request'

// 数据概览
export function getDashboard() {
  return request({
    url: '/saidazi/dashboard',
    method: 'get'
  })
}
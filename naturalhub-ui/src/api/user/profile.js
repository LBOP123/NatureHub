import request from '@/utils/request'

// 获取指定用户公开资料
export function getPublicProfile(userId) {
  return request({
    url: '/user/profile/' + userId,
    method: 'get'
  })
}


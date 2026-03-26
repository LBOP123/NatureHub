import request from '@/utils/request'

// 提交视频生成任务（后端异步处理立即返回，timeout 设 30s 防网络抖动）
export function createVideoTask(data) {
  return request({
    url: '/user/video',
    method: 'post',
    data,
    timeout: 30000
  })
}

// 查询任务状态（轮询用）
export function getVideoTask(id) {
  return request({
    url: '/user/video/' + id,
    method: 'get'
  })
}

// 查询当前用户视频任务列表
export function listVideoTasks(params) {
  return request({
    url: '/user/video/list',
    method: 'get',
    params
  })
}

// 删除任务
export function deleteVideoTask(ids) {
  return request({
    url: '/user/video/' + ids,
    method: 'delete'
  })
}

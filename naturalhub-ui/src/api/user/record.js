import request from '@/utils/request'

// ==================== 观察记录相关接口 ====================

// 查询观察记录列表
export function listRecord(query) {
  return request({
    url: '/user/record/list',
    method: 'get',
    params: query
  })
}

// 查询观察记录详细
export function getRecord(recordId) {
  return request({
    url: '/user/record/' + recordId,
    method: 'get'
  })
}

// 新增观察记录
export function addRecord(data) {
  return request({
    url: '/user/record',
    method: 'post',
    data: data
  })
}

// 修改观察记录
export function updateRecord(data) {
  return request({
    url: '/user/record',
    method: 'put',
    data: data
  })
}

// 删除观察记录
export function delRecord(recordId) {
  return request({
    url: '/user/record/' + recordId,
    method: 'delete'
  })
}

// 提交审核
export function submitForReview(recordId) {
  return request({
    url: '/user/record/submit/' + recordId,
    method: 'put'
  })
}

// 分享到社群
export function shareRecordToCommunity(recordId) {
  return request({
    url: '/user/record/' + recordId + '/share',
    method: 'post'
  })
}

// 获取审核状态
export function getReviewStatus(recordId) {
  return request({
    url: '/user/record/review/' + recordId,
    method: 'get'
  })
}

// 导出观察记录
export function exportRecord(query) {
  return request({
    url: '/user/record/export',
    method: 'post',
    params: query
  })
}

// 上传图片/视频到OSS
export function uploadFile(data) {
  return request({
    url: '/common/upload',
    method: 'post',
    data: data,
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

// 获取观察记录统计
export function getRecordStats() {
  return request({
    url: '/user/record/stats',
    method: 'get'
  })
}

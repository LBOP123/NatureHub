import request from '@/utils/request'

// 查询举报列表
export function listReport(query) {
  return request({
    url: '/community/report/list',
    method: 'get',
    params: query
  })
}

// 查询举报详细
export function getReport(reportId) {
  return request({
    url: '/community/report/' + reportId,
    method: 'get'
  })
}

// 新增举报
export function addReport(data) {
  return request({
    url: '/community/report',
    method: 'post',
    data: data
  })
}

// 修改举报
export function updateReport(data) {
  return request({
    url: '/community/report',
    method: 'put',
    data: data
  })
}

// 删除举报
export function delReport(reportId) {
  return request({
    url: '/community/report/' + reportId,
    method: 'delete'
  })
}

// 处理举报
export function handleReport(reportId, handleResult, handleRemark) {
  return request({
    url: '/community/report/handle/' + reportId + '/' + handleResult,
    method: 'put',
    params: { handleRemark }
  })
}

// 导出举报
export function exportReport(query) {
  return request({
    url: '/community/report/export',
    method: 'post',
    params: query
  })
}

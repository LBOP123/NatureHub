import request from '@/utils/request'

// 查询观察记录列表
export function listRecord(query) {
  return request({
    url: '/system/observation/record/list',
    method: 'get',
    params: query
  })
}

// 查询观察记录详细
export function getRecord(recordId) {
  return request({
    url: '/system/observation/record/' + recordId,
    method: 'get'
  })
}

// 删除观察记录
export function delRecord(recordId) {
  return request({
    url: '/system/observation/record/' + recordId,
    method: 'delete'
  })
}

// 导出观察记录
export function exportRecord(query) {
  return request({
    url: '/system/observation/record/export',
    method: 'post',
    params: query
  })
}

// 审核通过
export function approveRecord(data) {
  return request({
    url: '/system/observation/record/approve',
    method: 'put',
    data: data
  })
}

// 审核驳回
export function rejectRecord(data) {
  return request({
    url: '/system/observation/record/reject',
    method: 'put',
    data: data
  })
}

// 批量审核通过
export function batchApproveRecord(recordIds) {
  return request({
    url: '/system/observation/record/batchApprove',
    method: 'put',
    data: recordIds
  })
}

// 获取统计信息
export function getRecordStats() {
  return request({
    url: '/system/observation/record/stats',
    method: 'get'
  })
}

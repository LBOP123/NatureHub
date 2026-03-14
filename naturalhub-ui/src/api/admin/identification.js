import request from '@/utils/request'

// 查询鉴定求助列表（管理端）
export function listIdentification(query) {
  return request({
    url: '/admin/identification/list',
    method: 'get',
    params: query
  })
}

// 查询鉴定求助详细（管理端）
export function getIdentification(identificationId) {
  return request({
    url: '/admin/identification/' + identificationId,
    method: 'get'
  })
}

// 删除鉴定求助（管理端）
export function delIdentification(identificationIds) {
  return request({
    url: '/admin/identification/' + identificationIds,
    method: 'delete'
  })
}

// 审核鉴定求助
export function auditIdentification(data) {
  return request({
    url: '/admin/identification/' + data.identificationId + '/audit',
    method: 'post',
    data: data
  })
}

// 批量审核通过
export function batchApproveIdentification(identificationIds) {
  return request({
    url: '/admin/identification/audit/batch',
    method: 'post',
    data: { ids: identificationIds, auditStatus: 'approved', auditRemark: '' }
  })
}

// 导出鉴定求助
export function exportIdentification(query) {
  return request({
    url: '/admin/identification/export',
    method: 'post',
    params: query
  })
}

// 获取统计数据
export function getIdentificationStats() {
  return request({
    url: '/admin/identification/stats',
    method: 'get'
  })
}

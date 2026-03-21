import request from '@/utils/request'

// 查询鉴定者申请列表（管理端）
export function listApplication(query) {
  return request({
    url: '/admin/identifier/application/list',
    method: 'get',
    params: query
  })
}

// 审核通过
export function approveApplication(id) {
  return request({
    url: '/admin/identifier/application/' + id + '/approve',
    method: 'post'
  })
}

// 审核拒绝
export function rejectApplication(id, rejectReason) {
  return request({
    url: '/admin/identifier/application/' + id + '/reject',
    method: 'post',
    data: { rejectReason }
  })
}

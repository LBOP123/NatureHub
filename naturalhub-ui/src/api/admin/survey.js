import request from '@/utils/request'

// 查询野外调查列表（管理端）
export function listSurvey(query) {
  return request({
    url: '/admin/survey/list',
    method: 'get',
    params: query
  })
}

// 查询野外调查详细（管理端）
export function getSurvey(surveyId) {
  return request({
    url: '/admin/survey/' + surveyId,
    method: 'get'
  })
}

// 删除野外调查（管理端）
export function delSurvey(surveyIds) {
  return request({
    url: '/admin/survey/' + surveyIds,
    method: 'delete'
  })
}

// 审核野外调查
export function auditSurvey(data) {
  return request({
    url: '/admin/survey/' + data.surveyId + '/audit',
    method: 'post',
    data: data
  })
}

// 批量审核通过
export function batchApproveSurvey(surveyIds) {
  return request({
    url: '/admin/survey/audit/batch',
    method: 'post',
    data: { ids: surveyIds, auditStatus: 'approved', auditRemark: '' }
  })
}

// 导出野外调查
export function exportSurvey(query) {
  return request({
    url: '/admin/survey/export',
    method: 'post',
    params: query
  })
}

// 获取统计数据
export function getSurveyStats() {
  return request({
    url: '/admin/survey/stats',
    method: 'get'
  })
}

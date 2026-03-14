import request from '@/utils/request'

// 查询调查记录列表（我的）
export function listSurvey(query) {
  return request({
    url: '/user/survey/list',
    method: 'get',
    params: query
  })
}

// 查询调查记录列表（广场）
export function listSurveySquare(query) {
  return request({
    url: '/user/survey/square',
    method: 'get',
    params: query
  })
}

// 查询调查记录详细
export function getSurvey(surveyId) {
  return request({
    url: '/user/survey/' + surveyId,
    method: 'get'
  })
}

// 新增调查记录
export function addSurvey(data) {
  return request({
    url: '/user/survey',
    method: 'post',
    data: data
  })
}

// 修改调查记录
export function updateSurvey(data) {
  return request({
    url: '/user/survey',
    method: 'put',
    data: data
  })
}

// 删除调查记录
export function delSurvey(surveyId) {
  return request({
    url: '/user/survey/' + surveyId,
    method: 'delete'
  })
}

// 分享到社群
export function shareSurvey(surveyId) {
  return request({
    url: '/user/survey/' + surveyId + '/share',
    method: 'post'
  })
}

// 导出调查数据
export function exportSurvey(query) {
  return request({
    url: '/user/survey/export',
    method: 'post',
    params: query
  })
}

// ==================== 管理端接口 ====================

// 查询调查记录列表（管理端）
export function listSurveyAdmin(query) {
  return request({
    url: '/admin/survey/list',
    method: 'get',
    params: query
  })
}

// 查询调查记录详细（管理端）
export function getSurveyAdmin(surveyId) {
  return request({
    url: '/admin/survey/' + surveyId,
    method: 'get'
  })
}

// 审核调查记录
export function auditSurvey(surveyId, data) {
  return request({
    url: '/admin/survey/' + surveyId + '/audit',
    method: 'post',
    data: data
  })
}

// 批量审核
export function batchAuditSurvey(data) {
  return request({
    url: '/admin/survey/audit/batch',
    method: 'post',
    data: data
  })
}

// 删除调查记录（管理端）
export function delSurveyAdmin(surveyIds) {
  return request({
    url: '/admin/survey/' + surveyIds,
    method: 'delete'
  })
}

// 导出调查记录
export function exportSurveyAdmin(query) {
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

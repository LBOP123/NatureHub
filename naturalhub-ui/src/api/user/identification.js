import request from '@/utils/request'

// 查询鉴定求助列表（我的）
export function listIdentification(query) {
  return request({
    url: '/user/identification/list',
    method: 'get',
    params: query
  })
}

// 查询指定用户的公开鉴定求助记录（他人主页用）
export function listPublicIdentification(query) {
  return request({
    url: '/user/identification/public/list',
    method: 'get',
    params: query
  })
}

// 查询鉴定求助列表（广场）
export function listIdentificationSquare(query) {
  return request({
    url: '/user/identification/square',
    method: 'get',
    params: query
  })
}

// 查询鉴定求助详细
export function getIdentification(identificationId) {
  return request({
    url: '/user/identification/' + identificationId,
    method: 'get'
  })
}

// 新增鉴定求助
export function addIdentification(data) {
  return request({
    url: '/user/identification',
    method: 'post',
    data: data
  })
}

// 修改鉴定求助
export function updateIdentification(data) {
  return request({
    url: '/user/identification',
    method: 'put',
    data: data
  })
}

// 删除鉴定求助
export function delIdentification(identificationId) {
  return request({
    url: '/user/identification/' + identificationId,
    method: 'delete'
  })
}

// 提交回答
export function submitAnswer(identificationId, data) {
  return request({
    url: '/user/identification/' + identificationId + '/answer',
    method: 'post',
    data: data
  })
}

// 修改回答
export function updateAnswer(data) {
  return request({
    url: '/user/identification/answer',
    method: 'put',
    data: data
  })
}

// 删除回答
export function delAnswer(answerId) {
  return request({
    url: '/user/identification/answer/' + answerId,
    method: 'delete'
  })
}

// 采纳最佳答案
export function setBestAnswer(identificationId, answerId) {
  return request({
    url: '/user/identification/' + identificationId + '/best/' + answerId,
    method: 'put'
  })
}

// 分享到社群
export function shareIdentification(identificationId, data) {
  return request({
    url: '/user/identification/' + identificationId + '/share',
    method: 'post',
    data: data
  })
}

// 点赞回答
export function likeAnswer(answerId) {
  return request({
    url: '/user/identification/answer/' + answerId + '/like',
    method: 'post'
  })
}

// ==================== 投票相关接口 ====================

// 获取投票状态
export function getVoteStatus(identificationId) {
  return request({
    url: '/user/identification/' + identificationId + '/vote/status',
    method: 'get'
  })
}

// 提交投票
export function submitVote(identificationId, data) {
  return request({
    url: '/user/identification/' + identificationId + '/vote',
    method: 'post',
    data: data
  })
}

// 获取投票详情
export function getVoteDetails(identificationId) {
  return request({
    url: '/user/identification/' + identificationId + '/vote/details',
    method: 'get'
  })
}

// 获取投票数量
export function getVoteCount(identificationId) {
  return request({
    url: '/user/identification/' + identificationId + '/vote/count',
    method: 'get'
  })
}

// 结束投票（提问者）
export function endVoting(identificationId) {
  return request({
    url: '/user/identification/' + identificationId + '/vote/end',
    method: 'post'
  })
}

// ==================== 管理端接口 ====================

// 查询鉴定求助列表（管理端）
export function listIdentificationAdmin(query) {
  return request({
    url: '/admin/identification/list',
    method: 'get',
    params: query
  })
}

// 查询鉴定求助详细（管理端）
export function getIdentificationAdmin(identificationId) {
  return request({
    url: '/admin/identification/' + identificationId,
    method: 'get'
  })
}

// 审核鉴定求助
export function auditIdentification(identificationId, data) {
  return request({
    url: '/admin/identification/' + identificationId + '/audit',
    method: 'post',
    data: data
  })
}

// 批量审核
export function batchAuditIdentification(data) {
  return request({
    url: '/admin/identification/audit/batch',
    method: 'post',
    data: data
  })
}

// 删除鉴定求助（管理端）
export function delIdentificationAdmin(identificationIds) {
  return request({
    url: '/admin/identification/' + identificationIds,
    method: 'delete'
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

// ==================== 管理端投票接口 ====================

// 获取投票状态（管理端）
export function getVoteStatusAdmin(identificationId) {
  return request({
    url: '/admin/identification/' + identificationId + '/vote/status',
    method: 'get'
  })
}

// 获取投票详情（管理端）
export function getVoteDetailsAdmin(identificationId) {
  return request({
    url: '/admin/identification/' + identificationId + '/vote/details',
    method: 'get'
  })
}

// 结束投票（管理端）
export function endVotingAdmin(identificationId) {
  return request({
    url: '/admin/identification/' + identificationId + '/vote/end',
    method: 'post'
  })
}

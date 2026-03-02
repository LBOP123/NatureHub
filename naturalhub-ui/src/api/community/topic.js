import request from '@/utils/request'

// 查询社群话题列表（管理端）
export function listTopic(query) {
  return request({
    url: '/community/topic/list',
    method: 'get',
    params: query
  })
}

// 查询社群话题详细
export function getTopic(topicId) {
  return request({
    url: '/community/topic/' + topicId,
    method: 'get'
  })
}

// 新增社群话题
export function addTopic(data) {
  return request({
    url: '/community/topic',
    method: 'post',
    data: data
  })
}

// 修改社群话题
export function updateTopic(data) {
  return request({
    url: '/community/topic',
    method: 'put',
    data: data
  })
}

// 删除社群话题
export function delTopic(topicId) {
  return request({
    url: '/community/topic/' + topicId,
    method: 'delete'
  })
}

// 导出社群话题
export function exportTopic(query) {
  return request({
    url: '/community/topic/export',
    method: 'post',
    params: query
  })
}

// 置顶话题
export function setTopicTop(topicId, isTop) {
  return request({
    url: '/community/topic/top/' + topicId + '/' + isTop,
    method: 'put'
  })
}

// 设置精华话题
export function setTopicEssence(topicId, isEssence) {
  return request({
    url: '/community/topic/essence/' + topicId + '/' + isEssence,
    method: 'put'
  })
}

// 关闭/开启话题
export function setTopicStatus(topicId, status) {
  return request({
    url: '/community/topic/status/' + topicId + '/' + status,
    method: 'put'
  })
}

// 审核话题
export function auditTopic(topicId, auditStatus, auditRemark) {
  return request({
    url: '/community/topic/audit/' + topicId + '/' + auditStatus,
    method: 'put',
    params: { auditRemark }
  })
}

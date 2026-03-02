import request from '@/utils/request'

// 查询社群话题列表
export function listTopic(query) {
  return request({
    url: '/user/community/topic/list',
    method: 'get',
    params: query
  })
}

// 查询社群话题详细
export function getTopic(topicId) {
  return request({
    url: '/user/community/topic/' + topicId,
    method: 'get'
  })
}

// 新增社群话题
export function addTopic(data) {
  return request({
    url: '/user/community/topic',
    method: 'post',
    data: data
  })
}

// 修改社群话题
export function updateTopic(data) {
  return request({
    url: '/user/community/topic',
    method: 'put',
    data: data
  })
}

// 删除社群话题
export function delTopic(topicId) {
  return request({
    url: '/user/community/topic/' + topicId,
    method: 'delete'
  })
}

// 点赞话题
export function likeTopic(topicId) {
  return request({
    url: '/user/community/topic/like/' + topicId,
    method: 'post'
  })
}

// 取消点赞话题
export function unlikeTopic(topicId) {
  return request({
    url: '/user/community/topic/like/' + topicId,
    method: 'delete'
  })
}

// 收藏话题
export function collectTopic(topicId) {
  return request({
    url: '/user/community/topic/collect/' + topicId,
    method: 'post'
  })
}

// 取消收藏话题
export function uncollectTopic(topicId) {
  return request({
    url: '/user/community/topic/collect/' + topicId,
    method: 'delete'
  })
}

// 查询评论列表
export function listComment(query) {
  return request({
    url: '/user/community/comment/list',
    method: 'get',
    params: query
  })
}

// 新增评论
export function addComment(data) {
  return request({
    url: '/user/community/comment',
    method: 'post',
    data: data
  })
}

// 删除评论
export function delComment(commentId) {
  return request({
    url: '/user/community/comment/' + commentId,
    method: 'delete'
  })
}

// 点赞评论
export function likeComment(commentId) {
  return request({
    url: '/user/community/comment/like/' + commentId,
    method: 'post'
  })
}

// 取消点赞评论
export function unlikeComment(commentId) {
  return request({
    url: '/user/community/comment/like/' + commentId,
    method: 'delete'
  })
}

// 我的话题列表
export function myTopicList(query) {
  return request({
    url: '/user/community/my/topic',
    method: 'get',
    params: query
  })
}

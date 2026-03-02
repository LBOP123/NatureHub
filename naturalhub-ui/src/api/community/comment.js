import request from '@/utils/request'

// 查询评论列表
export function listComment(query) {
  return request({
    url: '/community/comment/list',
    method: 'get',
    params: query
  })
}

// 查询评论详细
export function getComment(commentId) {
  return request({
    url: '/community/comment/' + commentId,
    method: 'get'
  })
}

// 删除评论
export function delComment(commentId) {
  return request({
    url: '/community/comment/' + commentId,
    method: 'delete'
  })
}

// 屏蔽评论
export function blockComment(commentId) {
  return request({
    url: '/community/comment/block/' + commentId,
    method: 'put'
  })
}

// 恢复评论
export function restoreComment(commentId) {
  return request({
    url: '/community/comment/restore/' + commentId,
    method: 'put'
  })
}

// 导出评论
export function exportComment(query) {
  return request({
    url: '/community/comment/export',
    method: 'post',
    params: query
  })
}

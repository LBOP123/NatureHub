import request from '@/utils/request'

// ==================== 用户端接口 ====================

export function listMyMark3dTasks(query) {
  return request({ url: '/user/mark3d/list', method: 'get', params: query })
}

export function getMark3dTask(id) {
  return request({ url: '/user/mark3d/' + id, method: 'get' })
}

export function createMark3dTask(data) {
  return request({ url: '/user/mark3d', method: 'post', data })
}

export function syncMark3dTaskStatus(id) {
  return request({ url: '/user/mark3d/' + id + '/sync', method: 'post' })
}

export function deleteMark3dTask(ids) {
  return request({ url: '/user/mark3d/' + ids, method: 'delete' })
}

/**
 * 切换自己某条任务的公开展示状态
 * @param {number} id 任务ID
 * @param {number} isPublic 0-不公开 1-公开
 */
export function toggleMark3dPublic(id, isPublic) {
  return request({ url: '/user/mark3d/' + id + '/public', method: 'put', data: { isPublic } })
}

/**
 * 获取指定用户公开的3D展馆模型列表（他人查看个人中心时调用）
 * @param {number} userId 目标用户ID
 */
export function getPublicMark3dGallery(userId) {
  return request({ url: '/user/mark3d/gallery/' + userId, method: 'get' })
}


// ==================== 管理端接口 ====================

export function listMark3dTasksAdmin(query) {
  return request({ url: '/admin/mark3d/list', method: 'get', params: query })
}

export function getMark3dTaskAdmin(id) {
  return request({ url: '/admin/mark3d/' + id, method: 'get' })
}

export function syncMark3dTaskStatusAdmin(id) {
  return request({ url: '/admin/mark3d/' + id + '/sync', method: 'post' })
}

export function deleteMark3dTaskAdmin(ids) {
  return request({ url: '/admin/mark3d/' + ids, method: 'delete' })
}

export function getMark3dStatistics() {
  return request({ url: '/admin/mark3d/statistics', method: 'get' })
}

/**
 * 管理端修改任务公开状态
 */
export function updateMark3dPublicAdmin(id, isPublic) {
  return request({ url: '/admin/mark3d/' + id + '/public', method: 'put', data: { isPublic } })
}
export function renameMark3dTask(id, taskName) {
  return request({ url: '/user/mark3d/' + id + '/rename', method: 'put', data: { taskName } })
}

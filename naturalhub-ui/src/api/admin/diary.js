import request from '@/utils/request'

// 查询观察日志列表（管理端）
export function listDiary(query) {
  return request({
    url: '/admin/diary/list',
    method: 'get',
    params: query
  })
}

// 查询观察日志详细（管理端）
export function getDiary(diaryId) {
  return request({
    url: '/admin/diary/' + diaryId,
    method: 'get'
  })
}

// 删除观察日志（管理端）
export function delDiary(diaryIds) {
  return request({
    url: '/admin/diary/' + diaryIds,
    method: 'delete'
  })
}

// 归档/取消归档日志（管理端）
export function archiveDiary(diaryIds, isArchived) {
  return request({
    url: '/admin/diary/archive/' + diaryIds + '/' + isArchived,
    method: 'put'
  })
}

// 导出观察日志
export function exportDiary(query) {
  return request({
    url: '/admin/diary/export',
    method: 'post',
    params: query
  })
}

// 获取统计数据
export function getStatistics() {
  return request({
    url: '/admin/diary/statistics',
    method: 'get'
  })
}

import request from '@/utils/request'

// 查询个人观察日志列表
export function listDiary(query) {
  return request({
    url: '/user/diary/list',
    method: 'get',
    params: query
  })
}

// 查询个人观察日志详细
export function getDiary(diaryId) {
  return request({
    url: '/user/diary/' + diaryId,
    method: 'get'
  })
}

// 新增个人观察日志
export function addDiary(data) {
  return request({
    url: '/user/diary',
    method: 'post',
    data: data
  })
}

// 修改个人观察日志
export function updateDiary(data) {
  return request({
    url: '/user/diary',
    method: 'put',
    data: data
  })
}

// 删除个人观察日志
export function delDiary(diaryId) {
  return request({
    url: '/user/diary/' + diaryId,
    method: 'delete'
  })
}

// 导出个人观察日志
export function exportDiary(query) {
  return request({
    url: '/user/diary/export',
    method: 'post',
    params: query
  })
}

// 归档/取消归档日志
export function archiveDiary(diaryId, isArchived) {
  return request({
    url: '/user/diary/archive/' + diaryId + '/' + isArchived,
    method: 'put'
  })
}

// 切换可见性
export function toggleVisibility(diaryId, visibility) {
  return request({
    url: '/user/diary/visibility/' + diaryId + '/' + visibility,
    method: 'put'
  })
}

// 提交审核
export function submitForReview(diaryId) {
  return request({
    url: '/user/diary/submit/' + diaryId,
    method: 'put'
  })
}

// 分享到社群
export function shareToCommunity(data) {
  return request({
    url: '/user/diary/share',
    method: 'post',
    data: data
  })
}

// 获取审核状态
export function getReviewStatus(diaryId) {
  return request({
    url: '/user/diary/review/' + diaryId,
    method: 'get'
  })
}

// 上传图片/视频到OSS
export function uploadFile(data) {
  return request({
    url: '/common/upload',
    method: 'post',
    data: data,
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

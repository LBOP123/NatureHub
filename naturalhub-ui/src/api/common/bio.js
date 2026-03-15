import request from '@/utils/request'

/**
 * 生物识别API
 */

// 生物识别（Base64或URL）
export function bioRecognize(data) {
  return request({
    url: '/common/bio/recognize',
    method: 'post',
    data: data
  })
}

// 文件上传识别
export function bioRecognizeFile(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: '/common/bio/recognizeFile',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 快速识别（仅返回最佳结果）
export function bioQuickRecognize(data) {
  return request({
    url: '/common/bio/quickRecognize',
    method: 'post',
    data: data
  })
}

// 健康检查
export function bioHealth() {
  return request({
    url: '/common/bio/health',
    method: 'get'
  })
}

// 获取识别历史记录
export function getBioRecognitionHistory(query) {
  return request({
    url: '/common/bio/history',
    method: 'get',
    params: query
  })
}

// 获取识别历史详情
export function getBioRecognitionDetail(id) {
  return request({
    url: '/common/bio/history/' + id,
    method: 'get'
  })
}

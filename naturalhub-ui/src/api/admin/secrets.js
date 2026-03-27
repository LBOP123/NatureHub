import request from '@/utils/request'

// 获取所有密钥配置（脱敏）
export function listSecrets() {
  return request({
    url: '/system/config/list',
    method: 'get',
    params: { configType: 'Y', pageNum: 1, pageSize: 100 }
  })
}

// 根据 configKey 查询单条
export function getConfigByKey(configKey) {
  return request({
    url: '/system/config/configKey/' + configKey,
    method: 'get'
  })
}

// 更新密钥值（PUT /system/config）
export function updateSecret(data) {
  return request({
    url: '/system/config',
    method: 'put',
    data: data
  })
}

// 新增密钥配置
export function addSecret(data) {
  return request({
    url: '/system/config',
    method: 'post',
    data: data
  })
}

// 删除密钥配置
export function delSecret(configId) {
  return request({
    url: '/system/config/' + configId,
    method: 'delete'
  })
}

// 刷新参数缓存
export function refreshCache() {
  return request({
    url: '/system/config/refreshCache',
    method: 'delete'
  })
}

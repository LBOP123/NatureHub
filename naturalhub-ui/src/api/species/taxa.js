import request from '@/utils/request'

// 查询物种分类基础列表
export function listTaxa(query) {
  return request({
    url: '/species/taxa/list',
    method: 'get',
    params: query
  })
}

// 查询物种分类基础详细
export function getTaxa(taxonId) {
  return request({
    url: '/species/taxa/' + taxonId,
    method: 'get'
  })
}

// 新增物种分类基础
export function addTaxa(data) {
  return request({
    url: '/species/taxa',
    method: 'post',
    data: data
  })
}

// 修改物种分类基础
export function updateTaxa(data) {
  return request({
    url: '/species/taxa',
    method: 'put',
    data: data
  })
}

// 删除物种分类基础
export function delTaxa(taxonId) {
  return request({
    url: '/species/taxa/' + taxonId,
    method: 'delete'
  })
}

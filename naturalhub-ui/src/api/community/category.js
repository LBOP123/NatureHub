import request from '@/utils/request'

// 查询板块分类列表
export function listCategory(query) {
  return request({
    url: '/community/category/list',
    method: 'get',
    params: query
  })
}

// 查询所有板块（不分页）
export function listAllCategory() {
  return request({
    url: '/community/category/listAll',
    method: 'get'
  })
}

// 查询板块分类详细
export function getCategory(categoryId) {
  return request({
    url: '/community/category/' + categoryId,
    method: 'get'
  })
}

// 新增板块分类
export function addCategory(data) {
  return request({
    url: '/community/category',
    method: 'post',
    data: data
  })
}

// 修改板块分类
export function updateCategory(data) {
  return request({
    url: '/community/category',
    method: 'put',
    data: data
  })
}

// 删除板块分类
export function delCategory(categoryId) {
  return request({
    url: '/community/category/' + categoryId,
    method: 'delete'
  })
}

// 导出板块分类
export function exportCategory(query) {
  return request({
    url: '/community/category/export',
    method: 'post',
    params: query
  })
}

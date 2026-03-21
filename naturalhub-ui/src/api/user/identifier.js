import request from '@/utils/request'

/**
 * 查询当前用户的申请状态
 * 返回 null(data) 表示从未申请
 * status: 0=待审核, 1=已通过, 2=已拒绝
 */
export function getMyApplication() {
  return request({
    url: '/user/identifier/application/my',
    method: 'get'
  })
}

/**
 * 提交申请成为鉴定者
 * @param {Object} data - { realName, expertise, bio, qualification, experience }
 */
export function submitApplication(data) {
  return request({
    url: '/user/identifier/application/submit',
    method: 'post',
    data
  })
}

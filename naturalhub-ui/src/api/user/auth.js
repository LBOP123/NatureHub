import request from '@/utils/request'

// 用户端登录
export function login(data) {
  return request({
    url: '/user/auth/login',
    headers: {
      isToken: false
    },
    method: 'post',
    data: data
  })
}

// 用户端注册
export function register(data) {
  return request({
    url: '/user/auth/register',
    headers: {
      isToken: false
    },
    method: 'post',
    data: data
  })
}

// 获取验证码
export function getCodeImg() {
  return request({
    url: '/captchaImage',
    headers: {
      isToken: false
    },
    method: 'get',
    timeout: 20000
  })
}

// 退出登录
export function logout() {
  return request({
    url: '/logout',
    method: 'post'
  })
}

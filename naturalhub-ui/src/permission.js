import router from './router'
import store from './store'
import { Message } from 'element-ui'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'
import { getToken } from '@/utils/auth'
import { isPathMatch } from '@/utils/validate'
import { isRelogin } from '@/utils/request'

NProgress.configure({ showSpinner: false })

const whiteList = ['/login', '/register']

const isWhiteList = (path) => {
  return whiteList.some(pattern => isPathMatch(pattern, path))
}

router.beforeEach((to, from, next) => {
  NProgress.start()
  
  // 判断是否是用户端路由
  const isUserRoute = to.path.startsWith('/user/')
  const isUserAuthRoute = to.path === '/user/login' || to.path === '/user/register'
  
  if (getToken()) {
    to.meta.title && store.dispatch('settings/setTitle', to.meta.title)
    /* has token*/
    if (to.path === '/login') {
      next({ path: '/' })
      NProgress.done()
    } else if (to.path === '/user/login') {
      // 用户端登录页，已登录则跳转到用户首页
      next({ path: '/user/index' })
      NProgress.done()
    } else if (isWhiteList(to.path)) {
      next()
    } else if (isUserRoute) {
      // 用户端路由，已登录直接放行
      next()
    } else {
      // 管理端路由，需要验证权限
      if (store.getters.roles.length === 0) {
        isRelogin.show = true
        // 判断当前用户是否已拉取完user_info信息
        store.dispatch('GetInfo').then(() => {
          isRelogin.show = false
          store.dispatch('GenerateRoutes').then(accessRoutes => {
            // 根据roles权限生成可访问的路由表
            router.addRoutes(accessRoutes) // 动态添加可访问路由表
            next({ ...to, replace: true }) // hack方法 确保addRoutes已完成
          })
        }).catch(err => {
            store.dispatch('LogOut').then(() => {
              Message.error(err)
              next({ path: '/' })
            })
          })
      } else {
        next()
      }
    }
  } else {
    // 没有token
    if (isWhiteList(to.path) || isUserAuthRoute) {
      // 在免登录白名单或用户端登录注册页，直接进入
      next()
    } else if (isUserRoute) {
      // 用户端其他路由，未登录跳转到用户端登录
      next(`/user/login?redirect=${encodeURIComponent(to.fullPath)}`)
      NProgress.done()
    } else {
      // 管理端路由，未登录跳转到管理端登录
      next(`/login?redirect=${encodeURIComponent(to.fullPath)}`)
      NProgress.done()
    }
  }
})

router.afterEach(() => {
  NProgress.done()
})

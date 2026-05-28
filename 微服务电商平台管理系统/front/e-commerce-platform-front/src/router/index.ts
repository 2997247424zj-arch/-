import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/store/user'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login',
      name: 'Login',
      component: () => import('@/views/login/Login.vue'),
    },
    {
      path: '/register',
      name: 'Register',
      component: () => import('@/views/login/Register.vue'),
    },
    // ===== 用户端（Layout.vue） =====
    {
      path: '/',
      component: () => import('@/layout/Layout.vue'),
      redirect: '/home',
      children: [
        {
          path: 'home',
          name: 'Home',
          component: () => import('@/views/home/Home.vue'),
        },
        {
          path: 'dashboard',
          name: 'Dashboard',
          component: () => import('@/views/dashboard/Dashboard.vue'),
          meta: { requiresAuth: true, requiresUser: true },
        },
        {
          path: 'debug/backend',
          name: 'BackendTest',
          component: () => import('@/views/debug/BackendTest.vue'),
        },
        {
          path: 'debug/access',
          name: 'QuickAccess',
          component: () => import('@/views/debug/QuickAccess.vue'),
        },
        {
          path: 'debug/login',
          name: 'LoginTest',
          component: () => import('@/views/debug/LoginTest.vue'),
        },
        {
          path: 'debug/simple-login',
          name: 'SimpleLoginTest',
          component: () => import('@/views/debug/SimpleLoginTest.vue'),
        },
        {
          path: 'debug/easy',
          name: 'EasyTest',
          component: () => import('@/views/debug/EasyTest.vue'),
        },
        {
          path: 'debug/connection',
          name: 'BackendConnection',
          component: () => import('@/views/debug/BackendConnection.vue'),
        },
        {
          path: 'product/list',
          name: 'ProductList',
          component: () => import('@/views/product/ProductList.vue'),
        },
        {
          path: 'product/search',
          name: 'SearchResult',
          component: () => import('@/views/product/SearchResult.vue'),
        },
        {
          path: 'product/detail/:id',
          name: 'ProductDetail',
          component: () => import('@/views/product/ProductDetail.vue'),
        },
        {
          path: 'cart',
          name: 'Cart',
          component: () => import('@/views/cart/Cart.vue'),
          meta: { requiresAuth: true, requiresUser: true },
        },
        {
          path: 'order/confirm',
          name: 'OrderConfirm',
          component: () => import('@/views/order/OrderConfirm.vue'),
          meta: { requiresAuth: true, requiresUser: true },
        },
        {
          path: 'order/list',
          name: 'OrderList',
          component: () => import('@/views/order/OrderList.vue'),
          meta: { requiresAuth: true, requiresUser: true },
        },
        {
          path: 'order/detail/:orderNo',
          name: 'OrderDetail',
          component: () => import('@/views/order/OrderDetail.vue'),
          meta: { requiresAuth: true, requiresUser: true },
        },
        {
          path: 'user',
          name: 'UserProfile',
          component: () => import('@/views/user/UserProfile.vue'),
          meta: { requiresAuth: true, requiresUser: true },
        },
        {
          path: 'user/address',
          name: 'UserAddress',
          component: () => import('@/views/user/UserAddress.vue'),
          meta: { requiresAuth: true, requiresUser: true },
        },
        {
          path: '404',
          name: 'NotFound',
          component: () => import('@/views/error/NotFound.vue'),
        },
        {
          path: ':pathMatch(.*)*',
          redirect: '/404',
        },
      ],
    },
    // ===== 管理员端（AdminLayout.vue）— 完全独立布局 =====
    {
      path: '/admin',
      component: () => import('@/layout/AdminLayout.vue'),
      meta: { requiresAuth: true, requiresAdmin: true },
      children: [
        {
          path: '',
          name: 'AdminDashboard',
          component: () => import('@/views/admin/AdminDashboard.vue'),
        },
        {
          path: 'users',
          name: 'AdminUsers',
          component: () => import('@/views/admin/UserManagement.vue'),
        },
        {
          path: 'products',
          name: 'AdminProducts',
          component: () => import('@/views/admin/ProductManagement.vue'),
        },
        {
          path: 'orders',
          name: 'AdminOrders',
          component: () => import('@/views/admin/OrderManagement.vue'),
        },
        {
          path: 'settings',
          name: 'AdminSettings',
          component: () => import('@/views/admin/SystemSettings.vue'),
        },
      ],
    },
  ],
})

router.beforeEach(async (to, from, next) => {
  const userStore = useUserStore()

  // 登录/注册页直接放行
  if (to.path.includes('/login') || to.path.includes('/register')) {
    // 已登录管理员，跳转到管理台；已登录普通用户，跳转到首页
    if (userStore.isLoggedIn) {
      if (userStore.userInfo?.role === 'admin') {
        next({ path: '/admin' })
      } else {
        next({ path: '/home' })
      }
      return
    }
    next()
    return
  }

  // 除登录/注册页外，优先尝试从服务端恢复 session，避免管理员刷到前台公开页
  if (!userStore.userInfo && !to.path.includes('/login') && !to.path.includes('/register')) {
    try {
      await userStore.fetchUserInfo()
    } catch {
      if (to.meta.requiresAuth) {
        next({ name: 'Login', query: { redirect: to.fullPath } })
        return
      }
    }
  }

  // 需要登录
  if (to.meta.requiresAuth && !userStore.isLoggedIn) {
    next({ name: 'Login', query: { redirect: to.fullPath } })
    return
  }

  // 需要管理员权限
  if (to.meta.requiresAdmin && userStore.userInfo?.role !== 'admin') {
    next({ name: 'Home' })
    return
  }

  // 管理员统一隔离到后台，避免看到前台用户端按钮和页面
  if (userStore.userInfo?.role === 'admin' && !to.path.startsWith('/admin')) {
    next({ name: 'AdminDashboard' })
    return
  }

  // 管理员不能访问用户端购物路由，自动跳转到管理台
  if (userStore.userInfo?.role === 'admin' && to.meta.requiresUser) {
    next({ name: 'AdminDashboard' })
    return
  }

  next()
})

export default router

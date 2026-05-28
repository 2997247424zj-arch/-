import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw, RouteRecordNormalized } from 'vue-router'

// 路由懒加载 - 提升初始加载性能
const Home = () => import('../components/Home.vue')
const Register = () => import('../components/Register.vue')
const PhoneRegister = () => import('../components/PhoneRegister.vue')
const AdminDashboard = () => import('../components/dashboard/AdminDashboard.vue')
const OperatorDashboard = () => import('../components/dashboard/OperatorDashboard.vue')
const PassengerDashboard = () => import('../components/dashboard/PassengerDashboard.vue')
const PassengerMobilePreview = () => import('../views/PassengerMobilePreviewView.vue')
const MobileHome = () => import('../views/mobile/MobileHome.vue')
const MobileBooking = () => import('../views/mobile/MobileBooking.vue')
const MobileOrders = () => import('../views/mobile/MobileOrders.vue')
const MobileProfile = () => import('../views/mobile/MobileProfile.vue')
const AdminStatisticsView = () => import('../components/dashboard/AdminStatisticsView.vue')
const AdminQuickActionsView = () => import('../components/dashboard/AdminQuickActionsView.vue')
const UserCenterView = () => import('../views/UserCenterView.vue')
const NotFound = () => import('../components/NotFound.vue')
const OrderManagementView = () => import('../views/OrderManagementView.vue')
const TicketManagementView = () => import('../views/TicketManagementView.vue')
const FlightManagementView = () => import('../views/FlightManagementView.vue')
const UserAdminView = () => import('../views/UserAdminView.vue')
const AdminProfileView = () => import('../views/AdminProfileView.vue')
const AircraftManagementView = () => import('../views/AircraftManagementView.vue')
const RefundManagementView = () => import('../views/RefundManagementView.vue')
const SystemSettingsView = () => import('../views/SystemSettingsView.vue')
const SystemLogsView = () => import('../views/SystemLogsView.vue')
const AdminAlertsView = () => import('../views/admin/AdminAlertsView.vue')
const PassengerExperienceView = () => import('../views/PassengerExperienceView.vue')
const SupportChatView = () => import('../views/SupportChatView.vue')
const WeatherForecastView = () => import('../views/WeatherForecastView.vue')
const FlightBookingView = () => import('../views/operations/FlightBookingView.vue')
const SeatSelectionView = () => import('../views/operations/SeatSelectionView.vue')
const ChangeRefundView = () => import('../views/operations/ChangeRefundView.vue')
const FlightOperationsDashboard = () => import('../views/operations/FlightOperationsDashboard.vue')
const AnalyticsView = () => import('../views/operations/AnalyticsView.vue')
const PointsAndCoupons = () => import('../components/PointsAndCoupons.vue')
const FlightRebookingView = () => import('../views/FlightRebookingView.vue')
const PassengerSeatSelectionView = () => import('../views/PassengerSeatSelectionView.vue')
const PassengerBookingConfirmView = () => import('../views/PassengerBookingConfirmView.vue')
const SpecialPassengerRequestView = () => import('../views/SpecialPassengerRequestView.vue')
const AdminSpecialServiceRequestsView = () => import('../views/operations/AdminSpecialServiceRequestsView.vue')
const OperatorSpecialServiceRequestsView = () => import('../views/operations/OperatorSpecialServiceRequestsView.vue')
const PassengerPrintTicketsView = () => import('../views/PassengerPrintTicketsView.vue')
const TermsView = () => import('../views/TermsView.vue')
const PrivacyView = () => import('../views/PrivacyView.vue')
const MembersView = () => import('../views/MembersView.vue')

// 角色主页映射
const roleHomeMap: Record<string, string> = {
  admin: '/dashboard',
  operator: '/portal/operations',
  passenger: '/portal/passengers'
}

// 路由配置
const routes: RouteRecordRaw[] = [
  {
    path: '/',
    name: 'Home',
    component: Home
  },
  {
    path: '/register',
    name: 'Register',
    component: Register
  },
  {
    path: '/register/phone',
    name: 'PhoneRegister',
    component: PhoneRegister
  },
  {
    path: '/dashboard',
    name: 'AdminDashboard',
    component: AdminDashboard,
    meta: { requiresAuth: true, roles: ['admin'] }
  },
  {
    path: '/dashboard/statistics',
    name: 'AdminStatistics',
    component: AdminStatisticsView,
    meta: { requiresAuth: true, roles: ['admin'] }
  },
  {
    path: '/dashboard/quick-actions',
    name: 'AdminQuickActions',
    component: AdminQuickActionsView,
    meta: { requiresAuth: true, roles: ['admin'] }
  },
  {
    path: '/portal/operations',
    name: 'OperatorDashboard',
    component: OperatorDashboard,
    meta: { requiresAuth: true, roles: ['operator'] }
  },
  {
    path: '/portal/passengers',
    name: 'PassengerDashboard',
    component: PassengerDashboard,
    meta: { requiresAuth: true, roles: ['passenger'] }
  },
  {
    path: '/portal/passengers/mobile-preview',
    name: 'PassengerMobilePreview',
    component: PassengerMobilePreview,
    meta: { requiresAuth: true, roles: ['passenger'] }
  },
  // 子路由，用于移动端内导航（在 MobileShell 中切换）
  {
    path: '/portal/passengers/mobile-preview/booking',
    name: 'MobileBooking',
    component: MobileBooking,
    meta: { requiresAuth: true, roles: ['passenger'] }
  },
  {
    path: '/portal/passengers/mobile-preview/orders',
    name: 'MobileOrders',
    component: MobileOrders,
    meta: { requiresAuth: true, roles: ['passenger'] }
  },
  {
    path: '/portal/passengers/mobile-preview/profile',
    name: 'MobileProfile',
    component: MobileProfile,
    meta: { requiresAuth: true, roles: ['passenger'] }
  },
  {
    path: '/portal/orders',
    name: 'OrderManagement',
    component: OrderManagementView,
    meta: {
      requiresAuth: true,
      title: '订单客户管理',
      roles: ['admin', 'passenger'] // 乘客也可访问
    },
    props: (route) => ({
      view: route.query.view || 'list',
      action: route.query.action || ''
    })
  },
  {
    path: '/portal/tickets',
    name: 'TicketManagement',
    component: TicketManagementView,
    meta: { requiresAuth: true, title: '机票退改签审核中心', roles: ['admin'] }
  },
  {
    path: '/portal/flights',
    name: 'FlightManagement',
    component: FlightManagementView,
    meta: { requiresAuth: true, title: '航班信息管理', roles: ['admin'] }
  },
  {
    path: '/portal/users',
    name: 'UserAdmin',
    component: UserAdminView,
    meta: { requiresAuth: true, title: '人员用户管理', roles: ['admin'] }
  },
  {
    path: '/portal/profile',
    name: 'AdminProfile',
    component: AdminProfileView,
    meta: { requiresAuth: true, title: '个人信息设置', roles: ['admin'] }
  },
  {
    path: '/portal/aircraft',
    name: 'AircraftManagement',
    component: AircraftManagementView,
    meta: { requiresAuth: true, title: '机型管理', roles: ['admin'] }
  },
  {
    path: '/portal/refunds',
    name: 'RefundManagement',
    // 与机票改签管理共用统一的退改签审核中心视图，避免管理员在多个页面来回切换
    component: TicketManagementView,
    meta: { requiresAuth: true, title: '机票退改签审核中心', roles: ['admin'] }
  },
  {
    path: '/portal/settings',
    name: 'SystemSettings',
    component: SystemSettingsView,
    meta: { requiresAuth: true, title: '系统设置', roles: ['admin'] }
  },
  {
    path: '/portal/admin/alerts',
    name: 'AdminAlerts',
    component: AdminAlertsView,
    meta: { requiresAuth: true, title: '异常情况监控', roles: ['admin'] }
  },
  {
    path: '/portal/logs',
    name: 'SystemLogs',
    component: SystemLogsView,
    meta: { requiresAuth: true, title: '操作日志', roles: ['admin', 'operator'] }
  },
  {
    path: '/portal/operations/booking',
    name: 'FlightBooking',
    component: FlightBookingView,
    meta: { requiresAuth: true, title: '机票预订', roles: ['admin', 'operator'] }
  },
  {
    path: '/portal/operations/seats',
    name: 'SeatSelection',
    component: SeatSelectionView,
    meta: { requiresAuth: true, title: '座位选择', roles: ['admin', 'operator'] }
  },
  {
    path: '/portal/operations/change-refund',
    name: 'ChangeRefund',
    component: ChangeRefundView,
    meta: { requiresAuth: true, title: '退改签处理', roles: ['admin', 'operator'] }
  },
  {
    path: '/portal/operations/dashboard',
    name: 'FlightOperationsDashboard',
    component: FlightOperationsDashboard,
    meta: { requiresAuth: true, title: '航班运营看板', roles: ['admin', 'operator'] }
  },
  {
    path: '/portal/operations/analytics',
    name: 'Analytics',
    component: AnalyticsView,
    meta: { requiresAuth: true, title: '运行指标与航线分析', roles: ['admin', 'operator'] }
  },
  {
    path: '/portal/passengers/view',
    name: 'PassengerExperience',
    component: PassengerExperienceView,
    meta: {
      requiresAuth: true,
      title: '乘客体验视图',
      roles: ['admin', 'passenger']
    },
    props: (route) => ({
      tab: route.query.tab || 'overview'
    })
  },
  {
    path: '/portal/passengers/booking-confirm',
    name: 'PassengerBookingConfirm',
    component: PassengerBookingConfirmView,
    meta: {
      requiresAuth: true,
      title: '确认预订信息',
      roles: ['admin', 'passenger']
    }
  },
  {
    path: '/portal/passengers/print-tickets',
    name: 'PassengerPrintTickets',
    component: PassengerPrintTicketsView,
    meta: { requiresAuth: true, title: '机票打印', roles: ['passenger'] }
  },
  {
    path: '/portal/passengers/support',
    name: 'SupportChat',
    component: SupportChatView,
    meta: {
      requiresAuth: true,
      title: '在线客服',
      roles: ['admin', 'passenger']
    }
  },
  {
    path: '/portal/passengers/weather',
    name: 'WeatherForecast',
    component: WeatherForecastView,
    meta: {
      requiresAuth: true,
      title: '出行天气',
      roles: ['admin', 'passenger', 'operator']
    }
  },
  {
    path: '/user-center',
    name: 'UserCenter',
    component: UserCenterView,
    meta: { requiresAuth: true, roles: ['admin', 'passenger'] } // 乘客也可访问
  },
  {
    path: '/portal/passengers/points-coupons',
    name: 'PointsAndCoupons',
    component: PointsAndCoupons,
    meta: {
      requiresAuth: true,
      title: '积分与优惠券',
      roles: ['passenger']
    }
  },
  {
    path: '/about/terms',
    name: 'Terms',
    component: TermsView,
    meta: { title: '服务条款' }
  },
  {
    path: '/about/privacy',
    name: 'Privacy',
    component: PrivacyView,
    meta: { title: '隐私政策' }
  },
  {
    path: '/about/members',
    name: 'Members',
    component: MembersView,
    meta: { title: '成员简介' }
  },
  {
    path: '/portal/passengers/seat-selection',
    name: 'PassengerSeatSelection',
    component: PassengerSeatSelectionView,
    meta: {
      requiresAuth: true,
      title: '选择座位',
      roles: ['admin', 'passenger']
    }
  },
  {
    path: '/portal/orders/rebook/:id',
    name: 'FlightRebooking',
    component: FlightRebookingView,
    meta: {
      requiresAuth: true,
      title: '机票改签',
      roles: ['admin', 'passenger']
    },
    props: (route) => ({
      id: route.params.id
    })
  },
  {
    path: '/portal/passengers/special-passenger',
    name: 'SpecialPassengerRequest',
    component: SpecialPassengerRequestView,
    meta: {
      requiresAuth: true,
      title: '重点旅客预约',
      roles: ['admin', 'passenger']
    }
  },
  {
    path: '/portal/admin/special-requests',
    name: 'AdminSpecialServiceRequests',
    component: AdminSpecialServiceRequestsView,
    meta: { requiresAuth: true, title: '重点旅客预约管理', roles: ['admin'] }
  },
  {
    path: '/portal/operations/special-requests-operator',
    name: 'OperatorSpecialServiceRequests',
    component: OperatorSpecialServiceRequestsView,
    meta: { requiresAuth: true, title: '重点旅客预约处理', roles: ['operator'] }
  },
  {
    path: '/portal/passengers/baggage',
    name: 'PassengerBaggageManagement',
    component: () => import('../views/PassengerBaggageManagementView.vue'),
    meta: {
      requiresAuth: true,
      title: '行李管理',
      roles: ['passenger']
    }
  },
  {
    path: '/portal/operations/baggage',
    name: 'OperatorBaggageManagement',
    component: () => import('../views/operations/BaggageManagementView.vue'),
    meta: {
      requiresAuth: true,
      title: '行李管理',
      roles: ['admin', 'operator']
    }
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: NotFound
  }
]

// 导入状态管理工具
import store from '../services/store'

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫 - 检查登录状态和角色权限
router.beforeEach(async (to, from, next) => {
  try {
    // 确保store已初始化
    if (!store.userState.role) {
      store.initializeUserState()
    }

    const requiresAuth = to.meta.requiresAuth || false
    const isAuthenticated = store.userState.isLoggedIn
    const userRole = store.userState.role

    // 未登录用户尝试访问需要认证的页面
    if (requiresAuth && !isAuthenticated) {
      // 保存目标路由，登录后跳转
      if (to.path !== '/') {
        sessionStorage.setItem('redirectAfterLogin', to.fullPath)
      }
      next('/')
      return
    }

    // 已登录用户
    if (isAuthenticated) {
      // 访问登录/注册页，自动跳转主页
      if (to.name === 'Home' || to.name === 'Register') {
        // 检查是否有重定向目标
        const redirectPath = sessionStorage.getItem('redirectAfterLogin')
        if (redirectPath) {
          sessionStorage.removeItem('redirectAfterLogin')
          next(redirectPath)
          return
        }
        const targetRoute = roleHomeMap[userRole as keyof typeof roleHomeMap]
        next(targetRoute || '/')
        return
      }

      // 检查角色权限 - 使用状态管理工具
      const allowedRoles = to.meta.roles as string[]
      if (allowedRoles && allowedRoles.length > 0) {
        if (!store.hasPermission(allowedRoles)) {
          // 跳转到用户角色对应的主页
          if (import.meta.env.DEV) {
            console.warn(`用户 ${userRole} 无权访问 ${to.path}`)
          }
          next(roleHomeMap[userRole as keyof typeof roleHomeMap] || '/')
          return
        }
      }
    }

    // 其他情况直接通过
    next()
  } catch (error) {
    console.error('路由守卫错误:', error)
    // 发生错误时跳转到首页
    next('/')
  }
})

// 路由错误处理
router.onError((error) => {
  // 忽略一些常见的非关键错误
  const errorMessage = error?.toString() || ''

  // 忽略浏览器扩展相关的错误
  if (errorMessage.includes('message channel') ||
    errorMessage.includes('Extension context') ||
    errorMessage.includes('chrome-extension')) {
    return
  }

  // 开发环境输出错误，生产环境可以上报到错误监控服务
  if (import.meta.env.DEV) {
    console.error('路由错误:', error)
  }

  // 如果是组件加载失败，尝试跳转到首页
  if (errorMessage.includes('Failed to fetch dynamically imported module') ||
    errorMessage.includes('Loading chunk') ||
    errorMessage.includes('Loading CSS chunk')) {
    // 组件加载失败，可能是网络问题，尝试刷新
    if (router.currentRoute.value.path !== '/') {
      router.push('/').catch(() => {
        // 如果跳转失败，刷新页面
        window.location.reload()
      })
    }
  }
})

// 路由完成后的处理
router.afterEach((to, from) => {
  // 更新页面标题 - 根据路由路径和查询参数动态设置
  let pageTitle = ''

  // 根据路由路径和查询参数设置标题
  if (to.path === '/portal/passengers/view') {
    // 预定航班快捷入口
    if (to.query.tab === 'search') {
      pageTitle = '预定航班'
    } else {
      pageTitle = to.meta.title as string || '乘客体验视图'
    }
  } else if (to.path === '/portal/orders') {
    // 我的订单或改签/退订管理
    if (to.query.view === 'manage') {
      pageTitle = '改签/退订管理'
    } else {
      pageTitle = '我的订单'
    }
  } else if (to.meta.title) {
    // 使用路由配置的标题
    pageTitle = to.meta.title as string
  } else {
    // 默认标题
    pageTitle = '飞机售票系统'
  }

  // 设置文档标题（不添加后缀）
  if (pageTitle) {
    document.title = pageTitle
  } else {
    document.title = '飞机售票系统'
  }

  // 滚动到顶部（如果需要）- 使用requestAnimationFrame确保DOM已更新
  if (to.path !== from.path) {
    requestAnimationFrame(() => {
      window.scrollTo({ top: 0, behavior: 'smooth' })
    })
  }

  // 预加载相关路由（性能优化）
  preloadRelatedRoutes(to)
})

// 预加载相关路由（性能优化）
function preloadRelatedRoutes(currentRoute: any) {
  // 根据当前路由预加载可能访问的下一个路由
  const preloadMap: Record<string, string[]> = {
    '/portal/passengers': ['/portal/orders', '/user-center'],
    '/portal/orders': ['/portal/passengers', '/portal/passengers/view'],
    '/portal/passengers/view': ['/portal/passengers', '/portal/orders'],
    '/dashboard': ['/portal/orders', '/portal/flights', '/portal/users']
  }

  const routesToPreload = preloadMap[currentRoute.path]
  if (routesToPreload) {
    // 延迟预加载，避免影响当前页面
    setTimeout(() => {
      routesToPreload.forEach(path => {
        const route = router.resolve(path)
        if (route.matched.length > 0) {
          // 预加载组件
          const components = route.matched[0]?.components
          if (components) {
            const component = components.default
            // 检查是否是懒加载组件（函数）
            if (component && typeof component === 'function') {
              (component as () => Promise<any>)().catch(() => {
                // 预加载失败不影响用户体验
              })
            }
          }
        }
      })
    }, 2000) // 2秒后预加载
  }
}

let allRoutesPreloaded = false

function resolveComponentFromRoute(route: RouteRecordNormalized) {
  const targetComponents = route.components
  if (targetComponents) {
    return Object.values(targetComponents)
  }
  // 兼容直接定义component的场景
  const singleComponent = (route as unknown as { component?: RouteRecordRaw['component'] }).component
  return singleComponent ? [singleComponent] : []
}

export function preloadAllRouteComponents() {
  if (allRoutesPreloaded) {
    return
  }
  allRoutesPreloaded = true

  const componentSet = new Set<RouteRecordRaw['component']>()
  router.getRoutes().forEach((route) => {
    resolveComponentFromRoute(route).forEach((component) => {
      if (component) {
        componentSet.add(component)
      }
    })
  })

  componentSet.forEach((component) => {
    if (typeof component === 'function') {
      ; (component as () => Promise<unknown>)().catch(() => {
        if (import.meta.env.DEV) {
          console.warn('页面预加载失败，忽略：', component)
        }
      })
    }
  })
}

export default router
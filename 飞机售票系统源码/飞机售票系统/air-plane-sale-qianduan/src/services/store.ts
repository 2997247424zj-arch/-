// 简单的状态管理工具
import { ref, reactive, computed } from 'vue'
import { logConfigChange } from '../utils/configLogger'

// 用户状态
const userState = reactive({
  isLoggedIn: false,
  userInfo: null as any,
  role: ''
})

// 订单状态
const orderState = reactive({
  orders: [] as any[],
  loading: false,
  error: null
})

// 主题状态 - 扩展为全局个性化设置
const themeState = reactive({
  theme: 'light' as 'light' | 'dark' | 'auto',
  primaryColor: '#1E8AE6',
  fontSize: 'medium' as 'small' | 'medium' | 'large',
  language: 'zh-CN' as 'zh-CN' | 'en-US' | 'ja-JP',
  defaultView: null as 'dashboard' | 'list' | 'grid' | null, // 移除默认显示方式
  notificationMethod: 'none' as 'browser' | 'email' | 'sms' | 'none', // 默认不发送通知
  autoRefresh: true,
  refreshInterval: 30, // 秒
  showAnimations: true,
  compactMode: false
})

// 初始化用户状态
const initializeUserState = () => {
  try {
    const userInfoStr = sessionStorage.getItem('user_info')
    
    if (userInfoStr) {
      const userInfo = JSON.parse(userInfoStr)
      userState.isLoggedIn = true
      userState.userInfo = userInfo
      userState.role = userInfo.role || 'passenger'
    }
  } catch (error) {
    if (import.meta.env.DEV) {
      console.warn('初始化用户状态失败:', error)
    }
  }
}

// Helper: 为已登录用户构造用户范围的 localStorage 键，未登录时使用全局键
const getUserScopedKey = (baseKey: string) => {
  try {
    const userStr = sessionStorage.getItem('user_info')
    if (userStr) {
      const user = JSON.parse(userStr)
      if (user && (user.id || user.userId || user.uid)) {
        const id = user.id || user.userId || user.uid
        return `${baseKey}_user_${id}`
      }
    }
  } catch (e) {
    // ignore parsing errors, fallback to baseKey
  }
  return baseKey
}

// 初始化主题状态（存储支持用户范围隔离：已登录用户的设置不会与其他用户冲突）
const initializeThemeState = () => {
  try {
    // 加载所有个性化设置（优先读取用户范围 key）
    const savedTheme = localStorage.getItem(getUserScopedKey('theme'))
    const savedPrimaryColor = localStorage.getItem(getUserScopedKey('primaryColor'))
    const savedFontSize = localStorage.getItem(getUserScopedKey('fontSize'))
    const savedLanguage = localStorage.getItem(getUserScopedKey('language'))
    const savedDefaultView = localStorage.getItem(getUserScopedKey('defaultView'))
    const savedNotificationMethod = localStorage.getItem(getUserScopedKey('notificationMethod'))
    const savedAutoRefresh = localStorage.getItem(getUserScopedKey('autoRefresh'))
    const savedRefreshInterval = localStorage.getItem(getUserScopedKey('refreshInterval'))
    const savedShowAnimations = localStorage.getItem(getUserScopedKey('showAnimations'))
    const savedCompactMode = localStorage.getItem(getUserScopedKey('compactMode'))
    
    // 如果没有存储值，保持默认值（默认浅色：'light'），以保证多个用户之间不会互相影响
    if (savedTheme) {
      themeState.theme = savedTheme as 'light' | 'dark' | 'auto'
    }
    if (savedPrimaryColor) {
      themeState.primaryColor = savedPrimaryColor
    }
    if (savedFontSize) {
      themeState.fontSize = savedFontSize as 'small' | 'medium' | 'large'
    }
    if (savedLanguage) {
      themeState.language = savedLanguage as 'zh-CN' | 'en-US' | 'ja-JP'
    }
    if (savedDefaultView) {
      themeState.defaultView = savedDefaultView as 'dashboard' | 'list' | 'grid'
    } else {
      themeState.defaultView = null // 如果没有保存的值，设为null
    }
    if (savedNotificationMethod) {
      themeState.notificationMethod = savedNotificationMethod as 'browser' | 'email' | 'sms' | 'none'
    } else {
      themeState.notificationMethod = 'none' // 如果没有保存的值，默认不发送通知
    }
    if (savedAutoRefresh !== null) {
      themeState.autoRefresh = savedAutoRefresh === 'true'
    }
    if (savedRefreshInterval) {
      themeState.refreshInterval = parseInt(savedRefreshInterval) || 30
    }
    if (savedShowAnimations !== null) {
      themeState.showAnimations = savedShowAnimations === 'true'
    }
    if (savedCompactMode !== null) {
      themeState.compactMode = savedCompactMode === 'true'
    }
    
    // 应用主题到document
    applyTheme()
    // 应用其他个性化设置
    applyPersonalizationSettings()
  } catch (error) {
    if (import.meta.env.DEV) {
      console.warn('初始化主题状态失败:', error)
    }
  }
}

// 应用主题到页面
const applyTheme = () => {
  const root = document.documentElement
  
  // 应用主题模式
  if (themeState.theme === 'auto') {
    const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches
    root.setAttribute('data-theme', prefersDark ? 'dark' : 'light')
  } else {
    root.setAttribute('data-theme', themeState.theme)
  }
  
  // 应用主色调（统一使用 --color-primary 变量）
  root.style.setProperty('--color-primary', themeState.primaryColor)
  
  // 应用字体大小
  const fontSizeMap = {
    small: '14px',
    medium: '16px',
    large: '18px'
  }
  root.style.setProperty('--base-font-size', fontSizeMap[themeState.fontSize])
}

// 设置主题
const setTheme = (theme: 'light' | 'dark' | 'auto') => {
  const oldValue = themeState.theme
  themeState.theme = theme
  localStorage.setItem(getUserScopedKey('theme'), theme)
  applyTheme()
  
  // 记录配置变更日志
  logConfigChange('theme', 'theme', oldValue, theme, '用户修改主题模式')
  
  // 触发全局更新
  const event = new CustomEvent('theme-changed', { detail: { theme } })
  window.dispatchEvent(event)
  
  // 触发全局同步事件
  window.dispatchEvent(new CustomEvent('config-sync', { 
    detail: { 
      type: 'theme', 
      key: 'theme', 
      value: theme,
      timestamp: Date.now()
    } 
  }))
}

// 设置主色调
const setPrimaryColor = (color: string) => {
  const oldValue = themeState.primaryColor
  themeState.primaryColor = color
  localStorage.setItem(getUserScopedKey('primaryColor'), color)
  applyTheme()
  
  // 记录配置变更日志
  logConfigChange('theme', 'primaryColor', oldValue, color, '用户修改主色调')
  
  // 触发全局更新
  const event = new CustomEvent('theme-changed', { detail: { primaryColor: color } })
  window.dispatchEvent(event)
  
  // 触发全局同步事件
  window.dispatchEvent(new CustomEvent('config-sync', { 
    detail: { 
      type: 'theme', 
      key: 'primaryColor', 
      value: color,
      timestamp: Date.now()
    } 
  }))
}

// 设置字体大小
const setFontSize = (size: 'small' | 'medium' | 'large') => {
  const oldValue = themeState.fontSize
  themeState.fontSize = size
  localStorage.setItem(getUserScopedKey('fontSize'), size)
  applyTheme()
  
  // 记录配置变更日志
  logConfigChange('personalization', 'fontSize', oldValue, size, '用户修改字体大小')
  
  // 触发全局更新
  const event = new CustomEvent('theme-changed', { detail: { fontSize: size } })
  window.dispatchEvent(event)
  
  // 触发全局同步事件
  window.dispatchEvent(new CustomEvent('config-sync', { 
    detail: { 
      type: 'personalization', 
      key: 'fontSize', 
      value: size,
      timestamp: Date.now()
    } 
  }))
}

// 应用其他个性化设置
const applyPersonalizationSettings = () => {
  // 应用语言设置
  document.documentElement.setAttribute('lang', themeState.language)
  
  // 应用动画设置
  if (!themeState.showAnimations) {
    document.documentElement.style.setProperty('--animation-duration', '0s')
  } else {
    document.documentElement.style.setProperty('--animation-duration', '0.3s')
  }
  
  // 应用紧凑模式
  if (themeState.compactMode) {
    document.documentElement.setAttribute('data-compact', 'true')
  } else {
    document.documentElement.removeAttribute('data-compact')
  }
}

// 设置语言
const setLanguage = (language: 'zh-CN' | 'en-US' | 'ja-JP') => {
  const oldValue = themeState.language
  themeState.language = language
  localStorage.setItem(getUserScopedKey('language'), language)
  applyPersonalizationSettings()
  
  // 记录配置变更日志
  logConfigChange('personalization', 'language', oldValue, language, '用户修改语言偏好')
  
  // 触发全局更新
  const event = new CustomEvent('personalization-changed', { detail: { language } })
  window.dispatchEvent(event)
  
  // 触发全局同步事件
  window.dispatchEvent(new CustomEvent('config-sync', { 
    detail: { 
      type: 'personalization', 
      key: 'language', 
      value: language,
      timestamp: Date.now()
    } 
  }))
}

// 设置默认视图
const setDefaultView = (view: 'dashboard' | 'list' | 'grid') => {
  const oldValue = themeState.defaultView
  themeState.defaultView = view
  localStorage.setItem(getUserScopedKey('defaultView'), view)
  
  // 记录配置变更日志
  logConfigChange('personalization', 'defaultView', oldValue, view, '用户修改默认显示方式')
  
  // 触发全局更新
  const event = new CustomEvent('personalization-changed', { detail: { defaultView: view } })
  window.dispatchEvent(event)
  
  // 触发全局同步事件
  window.dispatchEvent(new CustomEvent('config-sync', { 
    detail: { 
      type: 'personalization', 
      key: 'defaultView', 
      value: view,
      timestamp: Date.now()
    } 
  }))
}

// 设置通知方式
const setNotificationMethod = (method: 'browser' | 'email' | 'sms' | 'none') => {
  const oldValue = themeState.notificationMethod
  themeState.notificationMethod = method
  localStorage.setItem(getUserScopedKey('notificationMethod'), method)
  
  // 记录配置变更日志
  logConfigChange('personalization', 'notificationMethod', oldValue, method, '用户修改通知方式')
  
  // 触发全局更新
  const event = new CustomEvent('personalization-changed', { detail: { notificationMethod: method } })
  window.dispatchEvent(event)
  
  // 触发全局同步事件
  window.dispatchEvent(new CustomEvent('config-sync', { 
    detail: { 
      type: 'personalization', 
      key: 'notificationMethod', 
      value: method,
      timestamp: Date.now()
    } 
  }))
}

// 设置自动刷新
const setAutoRefresh = (enabled: boolean) => {
  const oldValue = themeState.autoRefresh
  themeState.autoRefresh = enabled
  localStorage.setItem(getUserScopedKey('autoRefresh'), enabled.toString())
  
  // 记录配置变更日志
  logConfigChange('personalization', 'autoRefresh', oldValue, enabled, '用户修改自动刷新设置')
  
  // 触发全局更新
  const event = new CustomEvent('personalization-changed', { detail: { autoRefresh: enabled } })
  window.dispatchEvent(event)
  
  // 触发全局同步事件
  window.dispatchEvent(new CustomEvent('config-sync', { 
    detail: { 
      type: 'personalization', 
      key: 'autoRefresh', 
      value: enabled,
      timestamp: Date.now()
    } 
  }))
}

// 设置刷新间隔
const setRefreshInterval = (interval: number) => {
  const oldValue = themeState.refreshInterval
  themeState.refreshInterval = Math.max(10, Math.min(300, interval)) // 限制在10-300秒之间
  localStorage.setItem(getUserScopedKey('refreshInterval'), themeState.refreshInterval.toString())
  
  // 记录配置变更日志
  logConfigChange('personalization', 'refreshInterval', oldValue, themeState.refreshInterval, '用户修改刷新间隔')
  
  // 触发全局更新
  const event = new CustomEvent('personalization-changed', { detail: { refreshInterval: themeState.refreshInterval } })
  window.dispatchEvent(event)
  
  // 触发全局同步事件
  window.dispatchEvent(new CustomEvent('config-sync', { 
    detail: { 
      type: 'personalization', 
      key: 'refreshInterval', 
      value: themeState.refreshInterval,
      timestamp: Date.now()
    } 
  }))
}

// 设置动画显示
const setShowAnimations = (enabled: boolean) => {
  const oldValue = themeState.showAnimations
  themeState.showAnimations = enabled
  localStorage.setItem(getUserScopedKey('showAnimations'), enabled.toString())
  applyPersonalizationSettings()
  
  // 记录配置变更日志
  logConfigChange('personalization', 'showAnimations', oldValue, enabled, '用户修改动画显示设置')
  
  // 触发全局更新
  const event = new CustomEvent('personalization-changed', { detail: { showAnimations: enabled } })
  window.dispatchEvent(event)
  
  // 触发全局同步事件
  window.dispatchEvent(new CustomEvent('config-sync', { 
    detail: { 
      type: 'personalization', 
      key: 'showAnimations', 
      value: enabled,
      timestamp: Date.now()
    } 
  }))
}

// 设置紧凑模式
const setCompactMode = (enabled: boolean) => {
  const oldValue = themeState.compactMode
  themeState.compactMode = enabled
  localStorage.setItem(getUserScopedKey('compactMode'), enabled.toString())
  applyPersonalizationSettings()
  
  // 记录配置变更日志
  logConfigChange('personalization', 'compactMode', oldValue, enabled, '用户修改紧凑模式设置')
  
  // 触发全局更新
  const event = new CustomEvent('personalization-changed', { detail: { compactMode: enabled } })
  window.dispatchEvent(event)
  
  // 触发全局同步事件
  window.dispatchEvent(new CustomEvent('config-sync', { 
    detail: { 
      type: 'personalization', 
      key: 'compactMode', 
      value: enabled,
      timestamp: Date.now()
    } 
  }))
}

// 设置用户状态
const setUserState = (info: any) => {
  userState.isLoggedIn = true
  userState.userInfo = info
  userState.role = info.role || 'passenger'
  
  // 同步到sessionStorage
  sessionStorage.setItem('user_info', JSON.stringify(info))
  sessionStorage.setItem('userRole', info.role || 'passenger')
}

// 清除用户状态
const clearUserState = () => {
  userState.isLoggedIn = false
  userState.userInfo = null
  userState.role = ''
  
  // 清除sessionStorage
  sessionStorage.removeItem('user_info')
  sessionStorage.removeItem('userRole')
}

// 检查用户权限
const hasPermission = (requiredRoles: string[]) => {
  if (!userState.isLoggedIn) return false
  return requiredRoles.includes(userState.role)
}

// 获取用户角色
const getUserRole = () => userState.role

// 设置订单数据
const setOrders = (orders: any[]) => {
  orderState.orders = orders
}

// 添加订单
const addOrder = (order: any) => {
  orderState.orders.unshift(order)
}

// 更新订单
const updateOrder = (orderId: string, updates: any) => {
  const index = orderState.orders.findIndex(o => o.id === orderId)
  if (index !== -1) {
    orderState.orders[index] = { ...orderState.orders[index], ...updates }
  }
}

// 导出store
const store = {
  // 用户相关
  userState,
  initializeUserState,
  setUserState,
  clearUserState,
  hasPermission,
  getUserRole,
  
  // 订单相关
  orderState,
  setOrders,
  addOrder,
  updateOrder,
  
  // 主题相关
  themeState,
  initializeThemeState,
  setTheme,
  setPrimaryColor,
  setFontSize,
  applyTheme,
  applyPersonalizationSettings,
  // 个性化设置相关
  setLanguage,
  setDefaultView,
  setNotificationMethod,
  setAutoRefresh,
  setRefreshInterval,
  setShowAnimations,
  setCompactMode,
  
  // 计算属性
  isPassenger: computed(() => userState.role === 'passenger'),
  isAdmin: computed(() => userState.role === 'admin'),
  isOperator: computed(() => userState.role === 'operator')
}

export default store
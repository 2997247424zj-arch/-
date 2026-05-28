/**
 * 导航工具函数
 * 提供统一的页面跳转和数据传递机制
 */

import type { Router } from 'vue-router'
import type { LocationQueryRaw } from 'vue-router'
import store from '../services/store'

/**
 * 导航配置接口
 */
export interface NavigationConfig {
  path: string
  query?: LocationQueryRaw
  params?: Record<string, string>
  state?: Record<string, any> // 页面状态数据
  replace?: boolean // 是否替换当前历史记录
}

/**
 * 安全的页面跳转
 * @param router Vue Router实例
 * @param config 导航配置
 */
export async function navigateTo(router: Router, config: NavigationConfig): Promise<void> {
  try {
    // 保存页面状态到sessionStorage（如果需要）
    if (config.state) {
      const stateKey = `page_state_${config.path}`
      sessionStorage.setItem(stateKey, JSON.stringify(config.state))
    }

          // 执行路由跳转
          if (config.replace) {
            await router.replace({
              path: config.path,
              query: config.query
            } as any)
          } else {
            await router.push({
              path: config.path,
              query: config.query
            } as any)
          }
  } catch (error) {
    console.error('导航失败:', error)
    throw error
  }
}

/**
 * 获取页面状态
 * @param path 页面路径
 */
export function getPageState(path: string): any | null {
  const stateKey = `page_state_${path}`
  const stateStr = sessionStorage.getItem(stateKey)
  if (stateStr) {
    try {
      return JSON.parse(stateStr)
    } catch {
      return null
    }
  }
  return null
}

/**
 * 清除页面状态
 * @param path 页面路径
 */
export function clearPageState(path: string): void {
  const stateKey = `page_state_${path}`
  sessionStorage.removeItem(stateKey)
}

/**
 * 预加载路由组件
 * @param router Vue Router实例
 * @param path 要预加载的路由路径
 */
export async function preloadRoute(router: Router, path: string): Promise<void> {
  try {
    const route = router.resolve(path)
    if (route.matched.length > 0) {
      const components = route.matched[0]?.components
      if (components) {
        const component = components.default
        // 检查是否是懒加载组件（函数）
        if (component && typeof component === 'function') {
          await (component as () => Promise<any>)()
        }
      }
    }
  } catch (error) {
    console.warn('预加载路由失败:', path, error)
  }
}

/**
 * 批量预加载路由
 * @param router Vue Router实例
 * @param paths 要预加载的路由路径数组
 */
export async function preloadRoutes(router: Router, paths: string[]): Promise<void> {
  // 使用Promise.allSettled确保所有预加载都完成，即使有失败的
  await Promise.allSettled(
    paths.map(path => preloadRoute(router, path))
  )
}

/**
 * 根据用户角色获取可访问的路由
 */
export function getAccessibleRoutes(role: string): string[] {
  const roleRoutes: Record<string, string[]> = {
    admin: [
      '/dashboard',
      '/portal/orders',
      '/portal/tickets',
      '/portal/flights',
      '/portal/users',
      '/portal/aircraft',
      '/portal/refunds',
      '/portal/settings',
      '/portal/logs',
      '/user-center'
    ],
    operator: [
      '/portal/operations',
      '/portal/operations/view',
      '/portal/orders',
      '/user-center'
    ],
    passenger: [
      '/portal/passengers',
      '/portal/passengers/view',
      '/portal/orders',
      '/user-center'
    ]
  }
  
  return roleRoutes[role] || []
}

/**
 * 检查路由是否可访问
 */
export function canAccessRoute(role: string, path: string): boolean {
  const accessibleRoutes = getAccessibleRoutes(role)
  return accessibleRoutes.some(route => path.startsWith(route))
}

/**
 * 统一的API错误处理函数
 * @param error 错误对象
 * @param defaultMessage 默认错误消息
 * @param showAlert 是否显示浏览器alert（默认false，只记录到控制台）
 */
export function handleApiError(error: any, defaultMessage: string = '操作失败', showAlert: boolean = false): void {
  console.error('API错误:', error)
  
  // 提取错误消息
  let message = defaultMessage
  if (error) {
    if (typeof error === 'string') {
      message = error
    } else if (error instanceof Error) {
      message = error.message || defaultMessage
    } else if (error?.message) {
      message = error.message
    } else if (typeof error === 'object' && error !== null) {
      // 尝试从错误对象中提取消息
      const errorStr = JSON.stringify(error)
      if (errorStr !== '{}') {
        message = errorStr
      }
    }
  }
  
  // 如果需要在浏览器中显示alert（主要用于调试）
  if (showAlert) {
    alert(`错误: ${message}`)
  }
  
  // 可以在这里添加更多的错误处理逻辑，比如：
  // - 发送错误日志到服务器
  // - 显示全局错误提示
  // - 根据错误类型进行不同的处理
}


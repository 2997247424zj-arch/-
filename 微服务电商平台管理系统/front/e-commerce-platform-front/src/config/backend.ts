// 后端连接配置

// 后端服务地址配置
export const BACKEND_CONFIG = {
  // 开发环境后端地址
  DEV_BASE_URL: 'http://localhost:8080',

  // 生产环境后端地址
  PROD_BASE_URL: 'https://your-production-api.com',

  // API 前缀
  API_PREFIX: '/api',

  // 请求超时时间（毫秒）
  TIMEOUT: 10000,
} as const

// 获取当前环境的后端地址
export const getBackendUrl = (): string => {
  return import.meta.env.MODE === 'production'
    ? BACKEND_CONFIG.PROD_BASE_URL
    : BACKEND_CONFIG.DEV_BASE_URL
}

// 完整的 API 基础地址
export const getApiBaseUrl = (): string => {
  return getBackendUrl() + BACKEND_CONFIG.API_PREFIX
}

// 后端连接状态检查
export const checkBackendConnection = async (): Promise<boolean> => {
  try {
    const controller = new AbortController()
    const timeoutId = setTimeout(() => controller.abort(), 5000)

    const response = await fetch(`${getBackendUrl()}/actuator/health`, {
      method: 'GET',
      signal: controller.signal,
    })

    clearTimeout(timeoutId)
    return response.ok
  } catch (error) {
    console.warn('后端连接检查失败:', error)
    return false
  }
}

import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'
import { USE_MOCK } from './useMock'

const request = axios.create({
  baseURL: '/api',
  timeout: 10000,
  withCredentials: true, // 重要：允许携带Cookie，支持Session认证
})

// Mock Request Interceptor (优先级最高)
if (USE_MOCK) {
  // 动态导入 Mock 拦截器，避免在非 Mock 模式下解析错误
  import('./useMock').then(({ mockRequestInterceptor }) => {
    request.interceptors.request.use(
      async (config) => {
        const result = await mockRequestInterceptor(config)
        // 如果返回的是 AxiosResponse，说明被 mock 拦截了
        if ('status' in result) {
          // 直接返回 mock 数据，跳过实际请求
          return Promise.reject({
            config,
            response: result,
            isAxiosError: false,
            toJSON: () => ({}),
            name: 'MockResponse',
            message: 'Mock response',
          })
        }
        return result
      },
      (error) => {
        return Promise.reject(error)
      },
    )
  })
}

// Request Interceptor
request.interceptors.request.use(
  (config) => {
    // 暂时关闭 Token 验证，使用表单验证
    // const token = localStorage.getItem('token')
    // if (token && config.headers) {
    //   config.headers['Authorization'] = `Bearer ${token}`
    // }
    return config
  },
  (error) => {
    return Promise.reject(error)
  },
)

// Response Interceptor
request.interceptors.response.use(
  (response) => {
    const res = response.data
    // 后端返回格式: { code: 200, message: "成功", data: {} } 或 { code: 200, msg: "成功", data: {} }
    if (res.code === 200) {
      return res
    } else {
      // 兼容 message 和 msg 两种格式
      const errorMsg = res.msg || res.message || 'System Error'
      // 401错误不显示提示（未登录是正常状态）
      if (res.code !== 401) {
        ElMessage.error(errorMsg)
      }
      return Promise.reject(new Error(errorMsg))
    }
  },
  (error) => {
    console.error('API Error:', error)
    // 401错误不显示提示（未登录是正常状态）
    if (error.response?.status !== 401) {
      const errorMsg = error.response?.data?.msg || error.response?.data?.message || error.message || 'Request Failed'
      ElMessage.error(errorMsg)
    }
    return Promise.reject(error)
  },
)

export default request

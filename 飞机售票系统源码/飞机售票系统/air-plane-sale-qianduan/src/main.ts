import { createApp } from 'vue'
import App from './App.vue'
import router, { preloadAllRouteComponents } from './router/index'
import store from './services/store'
import './styles/theme-tokens.css'
import './styles/theme.css'
import './styles/bluewhite-overrides.css'
import './styles/dynamics.css'
import './styles/ui-enhancements.css'
import './styles/admin-enhancements.css'
import './styles/passenger-enhancements.css'

const app = createApp(App)

// 初始化用户状态
store.initializeUserState()

// 初始化主题状态
store.initializeThemeState()

// 应用初始主题样式
const themeMode = store.themeState.theme === 'auto' 
  ? (window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light')
  : store.themeState.theme
document.documentElement.setAttribute('data-theme', themeMode || 'dark')
document.documentElement.style.setProperty('--color-primary', store.themeState.primaryColor || '#1E8AE6')
document.documentElement.style.setProperty('--font-size-base', store.themeState.fontSize === 'small' ? '14px' : store.themeState.fontSize === 'large' ? '18px' : '16px')
document.documentElement.style.setProperty('--app-surface', (themeMode || 'dark') === 'light' ? '#F7FAFF' : '#030F1F')

// 全局注入store，方便在组件中使用
app.config.globalProperties.$store = store

// 性能优化：错误处理
app.config.errorHandler = (err, instance, info) => {
  // 忽略一些常见的非关键错误
  const errorMessage = err?.toString() || ''
  
  // 忽略浏览器扩展相关的错误
  if (errorMessage.includes('message channel') || 
      errorMessage.includes('Extension context') ||
      errorMessage.includes('chrome-extension')) {
    return
  }
  
  // 开发环境输出错误，生产环境上报到错误监控服务
  if (import.meta.env.DEV) {
    console.error('全局错误:', err, info)
  }
  // 生产环境可以在这里添加错误上报到监控服务
  // 例如：Sentry.captureException(err)
}

// 性能优化：警告处理
app.config.warnHandler = (msg, instance, trace) => {
  // 忽略Suspense实验性功能的警告
  if (msg && typeof msg === 'string' && msg.includes('Suspense')) {
    return
  }
  
  // 只在开发环境输出警告
  if (import.meta.env.DEV) {
    console.warn('Vue警告:', msg, trace)
  }
}

app.use(router)

// 挂载应用
app.mount('#app')

// 全局捕获浏览器错误，忽略已知第三方脚本错误以避免影响页面运行（例如某些统计脚本的 onload 回调错误）
window.addEventListener('error', (ev: ErrorEvent) => {
  try {
    const msg = ev.message || ''
    if (msg.includes('v[w] is not a function') || msg.includes('rumt-zh')) {
      // 阻止默认处理并在控制台记录，避免弹出 overlay / 中断渲染
      ev.preventDefault()
      if (import.meta.env.DEV) {
        console.warn('Ignored third-party script error:', msg)
      }
      return
    }
  } catch (e) {}
})
// 路由就绪后在空闲时间预加载所有懒加载页面，确保切换无白屏
router.isReady().then(() => {
  const schedule = (cb: () => void) => {
    if ('requestIdleCallback' in window) {
      ;(window as unknown as { requestIdleCallback: (fn: () => void) => void }).requestIdleCallback(cb)
    } else {
      setTimeout(cb, 1000)
    }
  }
  schedule(() => preloadAllRouteComponents())
})

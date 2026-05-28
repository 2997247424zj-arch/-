<script setup lang="ts">
import { useRouter } from 'vue-router'
import GlobalDialog from './components/GlobalDialog.vue'
import AdminFooter from './components/layout/AdminFooter.vue'
import { watch, ref, onMounted, onUnmounted } from 'vue'
import store from './services/store'

const router = useRouter()
const transitionName = ref('fade')
const isLoading = ref(false)
const isTransitioning = ref(false)

// 监听主题变化，确保全局应用
watch(() => store.themeState.theme, (newTheme) => {
  const root = document.documentElement
  if (newTheme === 'auto') {
    const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches
    root.setAttribute('data-theme', prefersDark ? 'dark' : 'light')
  } else {
    root.setAttribute('data-theme', newTheme)
  }
  const themeMode = newTheme === 'auto' 
    ? (window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light')
    : newTheme
  const backgroundColor = themeMode === 'light' ? '#F7FAFF' : '#030F1F'
  document.documentElement.style.setProperty('--app-surface', backgroundColor)
  document.body.style.background = ''
  document.body.style.color = ''
  const app = document.getElementById('app')
  if (app) {
    app.style.background = ''
    app.style.color = ''
  }
}, { immediate: true })

// 监听主色调变化
watch(() => store.themeState.primaryColor, (newColor) => {
  document.documentElement.style.setProperty('--color-primary', newColor)
  document.documentElement.style.setProperty('--primary-color', newColor)
}, { immediate: true })

// 监听字体大小变化
watch(() => store.themeState.fontSize, (newSize) => {
  const fontSizeMap = {
    small: '14px',
    medium: '16px',
    large: '18px'
  }
  const fontSize = fontSizeMap[newSize] || '16px'
  document.documentElement.style.setProperty('--font-size-base', fontSize)
  document.documentElement.style.setProperty('--base-font-size', fontSize)
  document.body.style.fontSize = fontSize
}, { immediate: true })

// 监听语言变化
watch(() => store.themeState.language, (newLanguage) => {
  document.documentElement.setAttribute('lang', newLanguage)
}, { immediate: true })

// 监听动画设置变化
watch(() => store.themeState.showAnimations, (enabled) => {
  if (!enabled) {
    document.documentElement.style.setProperty('--animation-duration', '0s')
  } else {
    document.documentElement.style.setProperty('--animation-duration', '0.3s')
  }
}, { immediate: true })

// 监听紧凑模式变化
watch(() => store.themeState.compactMode, (enabled) => {
  if (enabled) {
    document.documentElement.setAttribute('data-compact', 'true')
  } else {
    document.documentElement.removeAttribute('data-compact')
  }
}, { immediate: true })

// 监听个性化设置变化事件
window.addEventListener('personalization-changed', (event: any) => {
  const detail = event.detail
  if (detail.language) {
    document.documentElement.setAttribute('lang', detail.language)
  }
  if (detail.showAnimations !== undefined) {
    if (!detail.showAnimations) {
      document.documentElement.style.setProperty('--animation-duration', '0s')
    } else {
      document.documentElement.style.setProperty('--animation-duration', '0.3s')
    }
  }
  if (detail.compactMode !== undefined) {
    if (detail.compactMode) {
      document.documentElement.setAttribute('data-compact', 'true')
    } else {
      document.documentElement.removeAttribute('data-compact')
    }
  }
})

// 监听全局配置同步事件，确保所有页面实时同步配置
window.addEventListener('config-sync', (event: any) => {
  const { type, key, value } = event.detail
  const root = document.documentElement
  
  // 根据配置类型和键名同步更新
  if (type === 'theme') {
    if (key === 'theme') {
      let resolvedTheme: 'light' | 'dark'
      if (value === 'auto') {
        const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches
        resolvedTheme = prefersDark ? 'dark' : 'light'
        root.setAttribute('data-theme', resolvedTheme)
      } else {
        resolvedTheme = value
        root.setAttribute('data-theme', value)
      }
      const surface = resolvedTheme === 'light' ? '#F7FAFF' : '#030F1F'
      root.style.setProperty('--app-surface', surface)
    } else if (key === 'primaryColor') {
      root.style.setProperty('--primary-color', value)
      root.style.setProperty('--color-primary', value)
    }
  } else if (type === 'personalization') {
    if (key === 'fontSize') {
      const fontSizeMap = {
        small: '14px',
        medium: '16px',
        large: '18px'
      }
      const sizeKey = (value as keyof typeof fontSizeMap) || 'medium'
      root.style.setProperty('--base-font-size', fontSizeMap[sizeKey])
      root.style.setProperty('--font-size-base', fontSizeMap[sizeKey])
    } else if (key === 'language') {
      root.setAttribute('lang', value)
    } else if (key === 'showAnimations') {
      if (!value) {
        root.style.setProperty('--animation-duration', '0s')
      } else {
        root.style.setProperty('--animation-duration', '0.3s')
      }
    } else if (key === 'compactMode') {
      if (value) {
        root.setAttribute('data-compact', 'true')
      } else {
        root.removeAttribute('data-compact')
      }
    }
  }
})

// ===== 高级动态体验：滚动动效与弹窗自适应 =====
const cleanupTasks: Array<() => void> = []
const animationPresets = ['fade-up', 'fade-right', 'fade-left', 'scale-in', 'blur-up'] as const
const autoAnimateSelectors = [
  'section',
  'article',
  '.stat-card',
  '.panel-card',
  '.dashboard-card',
  '.analytics-card',
  '.info-card',
  '.module-card',
  '.flight-card',
  '.overview-card',
  '.table-card',
  '.modal-card',
  '.modal-content'
]
const animatedElements = new WeakSet<Element>()
let revealObserver: IntersectionObserver | null = null
let revealMutationObserver: MutationObserver | null = null
let overlayObserver: MutationObserver | null = null
const overlayCleanupMap = new Map<HTMLElement, () => void>()

const addCleanupTask = (task: () => void) => {
  cleanupTasks.push(task)
}

const markAutoAnimateTargets = () => {
  if (typeof document === 'undefined') return
  autoAnimateSelectors.forEach((selector, index) => {
    const preset = animationPresets[index % animationPresets.length]
    document.querySelectorAll<HTMLElement>(selector).forEach((element) => {
      if (!element.dataset.animate) {
        element.dataset.animate = preset
      }
    })
  })
}

const setupRevealAnimations = () => {
  if (typeof window === 'undefined' || typeof document === 'undefined' || revealObserver) return

  const motionQuery = window.matchMedia?.('(prefers-reduced-motion: reduce)')
  if (motionQuery?.matches) return

  const handleIntersect: IntersectionObserverCallback = (entries) => {
    entries.forEach((entry) => {
      if (entry.isIntersecting) {
        entry.target.classList.add('is-visible')
        revealObserver?.unobserve(entry.target)
      }
    })
  }

  revealObserver = new IntersectionObserver(handleIntersect, {
    threshold: 0.15,
    rootMargin: '0px 0px -12% 0px'
  })

  const scanAnimatedElements = () => {
    markAutoAnimateTargets()
    document.querySelectorAll<HTMLElement>('[data-animate]').forEach((element) => {
      if (animatedElements.has(element)) return
      animatedElements.add(element)
      element.classList.add('animate-ready')
      revealObserver?.observe(element)
    })
  }

  scanAnimatedElements()

  revealMutationObserver = new MutationObserver(() => {
    requestAnimationFrame(scanAnimatedElements)
  })

  revealMutationObserver.observe(document.body, { childList: true, subtree: true })

  addCleanupTask(() => {
    revealObserver?.disconnect()
    revealObserver = null
    revealMutationObserver?.disconnect()
    revealMutationObserver = null
  })
}

const visitPotentialOverlays = (node: Node, visitor: (overlay: HTMLElement) => void) => {
  if (node instanceof HTMLElement) {
    if (node.classList.contains('modal-overlay')) {
      visitor(node)
    }
    node.querySelectorAll<HTMLElement>('.modal-overlay').forEach((overlay) => visitor(overlay))
  } else if (node instanceof DocumentFragment) {
    node.querySelectorAll<HTMLElement>('.modal-overlay').forEach((overlay) => visitor(overlay))
  }
}

const cleanupOverlay = (overlay: HTMLElement) => {
  const disposer = overlayCleanupMap.get(overlay)
  if (disposer) {
    disposer()
    overlayCleanupMap.delete(overlay)
  }
  overlay.classList.remove('modal-overlay--enhanced', 'modal-overlay--scroll')
  overlay.style.alignItems = ''
}

const enhanceOverlay = (overlay: HTMLElement) => {
  if (overlayCleanupMap.has(overlay) || typeof window === 'undefined') return

  overlay.classList.add('modal-overlay--enhanced')

  const content =
    overlay.querySelector<HTMLElement>('[data-modal-card]') ||
    overlay.querySelector<HTMLElement>('.modal-card') ||
    overlay.querySelector<HTMLElement>('.modal-content') ||
    (overlay.firstElementChild as HTMLElement | null)

  if (!content) return

  const getStretchClass = () => (content.classList.contains('modal-card') ? 'modal-card--stretch' : 'modal-content--stretch')

  const applyPlacement = () => {
    if (typeof window === 'undefined') return
    const viewportHeight = window.innerHeight || document.documentElement.clientHeight || 0
    const prefersTop = overlay.classList.contains('passenger-modal') || overlay.dataset.position === 'top'
    const clearance = prefersTop ? 180 : 140
    const availableHeight = Math.max(280, viewportHeight - clearance)
    const contentRect = content.getBoundingClientRect()
    const stretchClass = getStretchClass()

    if (contentRect.height > availableHeight) {
      overlay.classList.add('modal-overlay--scroll')
      content.classList.add(stretchClass)
      content.style.setProperty('--modal-clearance', `${clearance}px`)
    } else {
      overlay.classList.remove('modal-overlay--scroll')
      content.classList.remove('modal-card--stretch', 'modal-content--stretch')
      content.style.removeProperty('--modal-clearance')
    }

    if (prefersTop) {
      overlay.style.alignItems = 'flex-start'
    } else if (!overlay.classList.contains('modal-overlay--scroll')) {
      overlay.style.alignItems = ''
    }
  }

  const resizeObserver = new ResizeObserver(() => requestAnimationFrame(applyPlacement))
  resizeObserver.observe(content)

  window.addEventListener('resize', applyPlacement)
  requestAnimationFrame(applyPlacement)
  setTimeout(applyPlacement, 120)

  overlayCleanupMap.set(overlay, () => {
    resizeObserver.disconnect()
    window.removeEventListener('resize', applyPlacement)
    content.classList.remove('modal-card--stretch', 'modal-content--stretch')
    content.style.removeProperty('--modal-clearance')
  })
}

const setupAdaptiveOverlays = () => {
  if (typeof window === 'undefined' || typeof document === 'undefined' || overlayObserver) return

  const scanOverlays = () => {
    document.querySelectorAll<HTMLElement>('.modal-overlay').forEach((overlay) => enhanceOverlay(overlay))
  }

  overlayObserver = new MutationObserver((mutations) => {
    mutations.forEach((mutation) => {
      mutation.addedNodes.forEach((node) => visitPotentialOverlays(node, enhanceOverlay))
      mutation.removedNodes.forEach((node) => visitPotentialOverlays(node, cleanupOverlay))
    })
  })

  overlayObserver.observe(document.body, { childList: true, subtree: true })
  scanOverlays()

  addCleanupTask(() => {
    overlayObserver?.disconnect()
    overlayObserver = null
    overlayCleanupMap.forEach((dispose, overlay) => {
      dispose()
      overlay.style.alignItems = ''
    })
    overlayCleanupMap.clear()
  })
}

const initializeDynamicExperience = () => {
  setupRevealAnimations()
  setupAdaptiveOverlays()
}

// 监听路由变化，根据路由类型决定动画
// 注意：这里只处理UI层面的逻辑，不处理路由守卫（已在router/index.ts中处理）
// 使用 watch 监听路由变化，避免与 router/index.ts 中的守卫冲突
watch(() => router.currentRoute.value.path, (newPath, oldPath) => {
  const to = router.currentRoute.value
  const from = { path: oldPath || '/', meta: {} } as any
  
  // 对于相同布局的页面切换，不显示加载指示器
  const isSameLayout = (to.meta.requiresAuth && from.meta?.requiresAuth) && 
                        (to.path.startsWith('/portal/') && from.path?.startsWith('/portal/'))
  
  // 只在必要时显示加载状态
  if (!isSameLayout) {
    isLoading.value = true
  }
  
  // 登录页到仪表板：渐入
  if (from.path === '/' && to.meta.requiresAuth) {
    transitionName.value = 'fade'
  }
  // 仪表板到登录页：渐出
  else if (from.meta?.requiresAuth && to.path === '/') {
    transitionName.value = 'fade'
  }
  // 同级页面切换：使用更快的滑动
  else {
    transitionName.value = 'slide'
  }
  
  // 延迟关闭加载状态
  setTimeout(() => {
    if (isSameLayout) {
      isLoading.value = false
    } else {
      setTimeout(() => {
        isLoading.value = false
      }, 100)
    }
  }, 50)
})

// 路由错误处理 - 已在 router/index.ts 中处理，这里只处理UI状态
// router.onError 已在 router/index.ts 中定义，避免重复注册

// 过渡动画钩子
const onBeforeEnter = (el: Element) => {
  isTransitioning.value = true
  // 确保背景色
  const wrapper = el as HTMLElement
  if (wrapper) {
    wrapper.style.background = 'var(--bg-primary, #030F1F)'
  }
}

const onEnter = (el: Element, done: () => void) => {
  // 使用requestAnimationFrame确保DOM已更新
  requestAnimationFrame(() => {
    done()
  })
}

const onAfterEnter = () => {
  isTransitioning.value = false
  isLoading.value = false
}

const onBeforeLeave = (el: Element) => {
  isTransitioning.value = true
  // 确保背景色保持
  const wrapper = el as HTMLElement
  if (wrapper) {
    wrapper.style.background = 'var(--bg-primary, #030F1F)'
  }
}

const onLeave = (el: Element, done: () => void) => {
  // 快速完成离开动画
  requestAnimationFrame(() => {
    done()
  })
}

const onAfterLeave = () => {
  isTransitioning.value = false
}

// 组件加载处理（Suspense会自动处理，这里保留用于其他用途）
const handleComponentError = (error: Error) => {
  if (import.meta.env.DEV) {
    console.error('组件加载失败:', error)
  }
  isLoading.value = false
}

// 重试加载
const handleRetry = () => {
  window.location.reload()
}

// 页面初始加载完成
onMounted(() => {
  isLoading.value = false
  document.body.style.minHeight = '100vh'
  document.body.style.background = ''
  const app = document.getElementById('app')
  if (app) {
    app.style.background = ''
  }
  initializeDynamicExperience()
})

onUnmounted(() => {
  cleanupTasks.forEach((task) => task())
})
</script>

<template>
  <!-- 全局加载指示器 -->
  <transition name="loading-fade">
    <div v-if="isLoading" class="page-loading">
      <div class="loading-spinner-container">
        <div class="loading-spinner-large"></div>
        <p class="loading-text">加载中...</p>
      </div>
    </div>
  </transition>
  <GlobalDialog />

  <!-- 使用keep-alive缓存组件，避免白屏 -->
  <router-view v-slot="{ Component, route }">
    <transition 
      :name="transitionName" 
      mode="out-in" 
      appear
      @before-enter="onBeforeEnter"
      @enter="onEnter"
      @after-enter="onAfterEnter"
      @before-leave="onBeforeLeave"
      @leave="onLeave"
      @after-leave="onAfterLeave"
    >
      <keep-alive 
        :include="['PassengerDashboard', 'OrderManagementView', 'PassengerExperienceView', 'AdminDashboard', 'OperatorDashboard']"
        :max="10"
      >
        <div v-if="Component" class="route-wrapper" :key="route.fullPath">
          <div class="page-ambient" aria-hidden="true">
            <span class="page-ambient__gradient"></span>
            <span class="page-ambient__grid"></span>
            <span class="page-ambient__orb orb-1"></span>
            <span class="page-ambient__orb orb-2"></span>
          </div>
          <div class="page-shell">
            <Suspense>
              <component 
                :is="Component" 
                class="route-component"
              />
              <template #fallback>
                <div class="route-loading">
                  <div class="loading-spinner-small"></div>
                  <p>加载中...</p>
                </div>
              </template>
            </Suspense>
          </div>
        </div>
        <div v-else class="route-error">
          <p>页面加载失败，请刷新重试</p>
          <button @click="handleRetry" class="retry-btn">刷新页面</button>
        </div>
      </keep-alive>
    </transition>
  </router-view>
  <!-- 全局页脚 -->
  <AdminFooter />
</template>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'Roboto', sans-serif;
  line-height: 1.6;
  color: var(--text-primary, #333);
  background: var(--app-surface, #030f1f);
}

/* 路由过渡动画 - 渐隐效果 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.12s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  z-index: 1;
}

.fade-enter-from {
  opacity: 0;
}

.fade-leave-to {
  opacity: 0;
}

/* 确保过渡期间背景色保持 */
.fade-enter-active .route-wrapper,
.fade-leave-active .route-wrapper,
.fade-enter-from .route-wrapper,
.fade-leave-to .route-wrapper {
  background: var(--bg-primary, #030F1F) !important;
  min-height: 100vh !important;
}

/* 路由过渡动画 - 滑动效果 */
.slide-enter-active,
.slide-leave-active {
  transition: all 0.12s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  z-index: 1;
}

.slide-enter-from {
  opacity: 0;
  transform: translateX(8px);
}

.slide-leave-to {
  opacity: 0;
  transform: translateX(-8px);
}

/* 确保过渡期间背景色保持 */
.slide-enter-active .route-wrapper,
.slide-leave-active .route-wrapper,
.slide-enter-from .route-wrapper,
.slide-leave-to .route-wrapper {
  background: var(--bg-primary, #030F1F) !important;
  min-height: 100vh !important;
}

/* 全局滚动条美化 */
::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}

::-webkit-scrollbar-track {
  background: rgba(15, 23, 42, 0.3);
  border-radius: 4px;
}

::-webkit-scrollbar-thumb {
  background: rgba(99, 102, 241, 0.5);
  border-radius: 4px;
  transition: background 0.3s;
}

::-webkit-scrollbar-thumb:hover {
  background: rgba(99, 102, 241, 0.7);
}

/* 全局加载动画 */
@keyframes shimmer {
  0% {
    background-position: -1000px 0;
  }
  100% {
    background-position: 1000px 0;
  }
}

/* 全局脉动动画 */
@keyframes pulse {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.7;
  }
}

/* 全局弹跳动画 */
@keyframes bounce {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-5px);
  }
}

/* 页面加载动画 */
.page-loading {
  position: fixed;
  inset: 0;
  background: rgba(3, 7, 18, 0.95);
  backdrop-filter: blur(10px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
}

.loading-spinner-container {
  text-align: center;
}

.loading-spinner-large {
  width: 60px;
  height: 60px;
  border: 4px solid rgba(99, 102, 241, 0.2);
  border-top-color: rgba(99, 102, 241, 0.8);
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 20px;
}

.loading-text {
  color: rgba(255, 255, 255, 0.8);
  font-size: 16px;
  margin: 0;
  animation: pulse 1.5s ease-in-out infinite;
}

.loading-fade-enter-active,
.loading-fade-leave-active {
  transition: opacity 0.3s ease;
}

.loading-fade-enter-from,
.loading-fade-leave-to {
  opacity: 0;
}

/* 优化路由过渡，添加最小高度避免闪烁 */
.fade-enter-active,
.fade-leave-active,
.slide-enter-active,
.slide-leave-active {
  position: relative;
  min-height: 100vh;
  background: var(--bg-primary, #030F1F);
  will-change: opacity, transform;
}

/* 全局根元素样式，防止白屏 */
#app {
  background: var(--app-surface, var(--bg-primary, #030f1f));
  min-height: 100vh;
}

body {
  background: var(--app-surface, var(--bg-primary, #030f1f));
  margin: 0;
  padding: 0;
}

/* 全局主题变量 */
:root {
  --primary-color: #1E8AE6;
  --base-font-size: 16px;
}

[data-theme="light"] {
  --bg-primary: #F7FAFF;
  --bg-secondary: #EFF3FF;
  --bg-tertiary: #E2EAFE;
  --text-primary: #0A1F33;
  --text-secondary: #364864;
  --text-tertiary: #5C6C85;
  --border-color: rgba(10, 31, 51, 0.15);
  --color-primary: #1E8AE6;
  --color-primary-light: #4FB5FF;
  --color-primary-dark: #0B3D91;
}

[data-theme="dark"] {
  --bg-primary: #030F1F;
  --bg-secondary: #071A33;
  --bg-tertiary: #0E2647;
  --text-primary: #E6F0FF;
  --text-secondary: rgba(230, 240, 255, 0.72);
  --text-tertiary: rgba(230, 240, 255, 0.52);
  --border-color: rgba(230, 240, 255, 0.18);
  --color-primary: #1E8AE6;
  --color-primary-light: #4FB5FF;
  --color-primary-dark: #0B3D91;
}

/* 应用主题到全局 */
body {
  background: var(--app-surface, var(--bg-primary));
  color: var(--text-primary);
  font-size: var(--base-font-size);
  transition: background-color 0.3s ease, color 0.3s ease;
}

/* 主题切换时的平滑过渡 */
* {
  transition: background-color 0.3s ease, color 0.3s ease, border-color 0.3s ease;
}

/* 路由包装器 - 确保背景色始终存在 */
.route-wrapper {
  min-height: 100vh;
  background: var(--bg-primary, #030F1F);
  position: relative;
  z-index: 1;
  /* 确保在过渡期间背景色保持 */
  will-change: opacity, transform;
  /* 防止内容闪烁 */
  backface-visibility: hidden;
  -webkit-backface-visibility: hidden;
}

/* 路由加载状态 */
.route-loading {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: var(--bg-primary, #030F1F);
  color: var(--text-primary, #f8fafc);
  gap: 1rem;
}

.loading-spinner-small {
  width: 40px;
  height: 40px;
  border: 3px solid rgba(99, 102, 241, 0.2);
  border-top-color: rgba(99, 102, 241, 0.8);
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

.route-error {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  align-items: center;
}

.retry-btn {
  padding: 0.75rem 1.5rem;
  border: 1px solid rgba(99, 102, 241, 0.5);
  background: rgba(99, 102, 241, 0.1);
  color: #93c5fd;
  border-radius: 8px;
  cursor: pointer;
  font-size: 0.9rem;
  transition: all 0.3s ease;
}

.retry-btn:hover {
  background: rgba(99, 102, 241, 0.2);
  border-color: rgba(99, 102, 241, 0.7);
}

/* 路由组件容器 */
.route-component {
  min-height: 100vh;
  background: transparent;
  position: relative;
}

/* 路由错误提示 */
.route-error {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--bg-primary, #030F1F);
  color: var(--text-primary, #f8fafc);
  font-size: 1.2rem;
}

/* 确保过渡动画时背景不闪烁 - 关键修复 */
.fade-enter-active,
.fade-leave-active,
.slide-enter-active,
.slide-leave-active {
  position: relative;
}

.fade-enter-active .route-wrapper,
.fade-leave-active .route-wrapper,
.slide-enter-active .route-wrapper,
.slide-leave-active .route-wrapper {
  background: var(--bg-primary, #030F1F);
  min-height: 100vh;
}

/* 确保过渡期间背景色保持 */
.fade-enter-from .route-wrapper,
.fade-leave-to .route-wrapper,
.slide-enter-from .route-wrapper,
.slide-leave-to .route-wrapper {
  background: var(--bg-primary, #030F1F);
}

@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}
</style>

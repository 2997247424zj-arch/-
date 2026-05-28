<template>
  <div class="passenger-layout">
    <!-- 顶部导航栏 -->
    <PassengerHeader />

    <!-- 主内容区 -->
    <main class="main-content">
      <slot></slot>
    </main>

    <!-- 页脚 -->
  
  </div>
</template>

<script setup lang="ts">
import { onMounted, onUnmounted } from 'vue'
import store from '../../services/store'
import PassengerHeader from './PassengerHeader.vue'
import PassengerFooter from './PassengerFooter.vue'

// 应用全局个性化设置
const applyGlobalPersonalization = () => {
  const root = document.documentElement
  
  // 应用主题模式
  if (store.themeState.theme === 'auto') {
    const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches
    root.setAttribute('data-theme', prefersDark ? 'dark' : 'light')
  } else {
    root.setAttribute('data-theme', store.themeState.theme)
  }
  
  // 应用主色调
  root.style.setProperty('--primary-color', store.themeState.primaryColor)
  root.style.setProperty('--color-primary', store.themeState.primaryColor)
  
  // 应用字体大小
  const fontSizeMap = {
    small: '14px',
    medium: '16px',
    large: '18px'
  }
  root.style.setProperty('--base-font-size', fontSizeMap[store.themeState.fontSize])
  root.style.setProperty('--font-size-base', fontSizeMap[store.themeState.fontSize])
  
  // 应用语言设置
  root.setAttribute('lang', store.themeState.language)
  
  // 应用动画设置
  if (!store.themeState.showAnimations) {
    root.style.setProperty('--animation-duration', '0s')
  } else {
    root.style.setProperty('--animation-duration', '0.3s')
  }
  
  // 应用紧凑模式
  if (store.themeState.compactMode) {
    root.setAttribute('data-compact', 'true')
  } else {
    root.removeAttribute('data-compact')
  }
}

// 监听主题变化事件
const handleThemeChanged = () => {
  applyGlobalPersonalization()
}

// 监听个性化设置变化事件
const handlePersonalizationChanged = () => {
  applyGlobalPersonalization()
}

// 组件挂载时应用设置并监听事件
onMounted(() => {
  // 确保主题已初始化
  if (!store.themeState.theme) {
    store.initializeThemeState()
  }
  
  // 应用全局个性化设置
  applyGlobalPersonalization()
  
  // 监听主题变化
  window.addEventListener('theme-changed', handleThemeChanged)
  window.addEventListener('personalization-changed', handlePersonalizationChanged)
})

// 组件卸载时移除事件监听
onUnmounted(() => {
  window.removeEventListener('theme-changed', handleThemeChanged)
  window.removeEventListener('personalization-changed', handlePersonalizationChanged)
})
</script>

<style scoped>
.passenger-layout {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background: linear-gradient(135deg, #e0e3e9 0%, #b2c7e8 100%); /* Premium dark gradient */
  color: var(--text-primary);
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
}


.main-content {
  flex: 1;
  padding: 20px;
  max-width: 1400px;
  width: 100%;
  margin: 0 auto;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .main-content {
    padding: 16px;
  }
}
</style>
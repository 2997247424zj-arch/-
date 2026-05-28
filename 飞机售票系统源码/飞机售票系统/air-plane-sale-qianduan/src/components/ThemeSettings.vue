<template>
  <div class="theme-settings">
    <!-- 主题模式 -->
    <div class="setting-item">
      <label>{{ t('themeSettings.sections.theme') }}</label>
      <div class="theme-options">
        <button 
          v-for="theme in themes" 
          :key="theme.value"
          :class="['theme-btn', { active: currentTheme === theme.value }]"
          @click="handleThemeChange(theme.value)"
        >
          <span class="theme-icon">{{ theme.icon }}</span>
          <span class="theme-name">{{ theme.label }}</span>
        </button>
      </div>
    </div>



    <!-- 字体大小 -->
    <div class="setting-item">
      <label>{{ t('themeSettings.sections.fontSize') }}</label>
      <div class="font-size-options">
        <button 
          v-for="size in fontSizeOptions" 
          :key="size.value"
          :class="['font-size-btn', { active: currentFontSize === size.value }]"
          @click="handleFontSizeChange(size.value)"
        >
          {{ size.label }}
        </button>
      </div>
    </div>




    <!-- 其他设置 -->
    <div class="setting-item">
      <label>{{ t('themeSettings.sections.others') }}</label>
      <div class="other-settings">
 
        <div class="toggle-setting">
          <label class="toggle-label">
            <input 
              type="checkbox" 
              v-model="showAnimationsEnabled"
              @change="handleShowAnimationsChange"
              class="toggle-input"
            />
            <span class="toggle-text">{{ t('themeSettings.toggles.showAnimations') }}</span>
          </label>
        </div>
        <div class="toggle-setting">
          <label class="toggle-label">
            <input 
              type="checkbox" 
              v-model="compactModeEnabled"
              @change="handleCompactModeChange"
              class="toggle-input"
            />
            <span class="toggle-text">{{ t('themeSettings.toggles.compactMode') }}</span>
          </label>
        </div>
      </div>
    </div>

  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import store from '../services/store'
import { useI18n } from '../services/i18n'

const { t } = useI18n()

// 主题相关
const themes = computed(() => [
  { value: 'light', label: t('themeSettings.modes.light'), icon: '☀️' },
  { value: 'dark', label: t('themeSettings.modes.dark'), icon: '🌙' },
  { value: 'auto', label: t('themeSettings.modes.auto'), icon: '🔄' }
])

const colorOptions = [
  { value: '#2563eb', label: '现代蓝' },
  { value: '#3b82f6', label: '浅蓝' },
  { value: '#1e40af', label: '深蓝' },
  { value: '#12C4C0', label: '机舱青' },
  { value: '#F5B942', label: '信号琥珀' },
  { value: '#FF6B6B', label: '告警红' }
]

const fontSizeOptions = computed(() => [
  { value: 'small', label: t('themeSettings.fontSizes.small') },
  { value: 'medium', label: t('themeSettings.fontSizes.medium') },
  { value: 'large', label: t('themeSettings.fontSizes.large') }
])

const languageOptions = computed(() => [
  { value: 'zh-CN', label: t('themeSettings.languages.zh-CN'), icon: '🇨🇳' },
  { value: 'en-US', label: t('themeSettings.languages.en-US'), icon: '🇺🇸' },
  { value: 'ja-JP', label: t('themeSettings.languages.ja-JP'), icon: '🇯🇵' }
])

const viewOptions = [
  { value: 'dashboard', label: '仪表板', icon: '📊' },
  { value: 'list', label: '列表', icon: '📋' },
  { value: 'grid', label: '网格', icon: '🔲' }
]

const notificationOptions = [
  { value: 'browser', label: '浏览器通知', icon: '🔔' },
  { value: 'email', label: '邮件通知', icon: '📧' },
  { value: 'sms', label: '短信通知', icon: '📱' },
  { value: 'none', label: '不通知', icon: '🔕' }
]

const currentTheme = computed(() => store.themeState.theme)
const currentPrimaryColor = computed(() => store.themeState.primaryColor)
const currentFontSize = computed(() => store.themeState.fontSize)
const currentLanguage = computed(() => store.themeState.language)
const currentDefaultView = computed(() => store.themeState.defaultView)
const currentNotificationMethod = computed(() => store.themeState.notificationMethod)

const autoRefreshEnabled = ref(store.themeState.autoRefresh)
const refreshIntervalValue = ref(store.themeState.refreshInterval)
const showAnimationsEnabled = ref(store.themeState.showAnimations)
const compactModeEnabled = ref(store.themeState.compactMode)

// 监听store变化，同步到本地ref
watch(() => store.themeState.autoRefresh, (val) => {
  autoRefreshEnabled.value = val
})
watch(() => store.themeState.refreshInterval, (val) => {
  refreshIntervalValue.value = val
})
watch(() => store.themeState.showAnimations, (val) => {
  showAnimationsEnabled.value = val
})
watch(() => store.themeState.compactMode, (val) => {
  compactModeEnabled.value = val
})

const handleThemeChange = (theme: string) => {
  if (theme === 'light' || theme === 'dark' || theme === 'auto') {
    store.setTheme(theme)
    // 触发全局更新，确保首页和订单管理页面同步
    window.dispatchEvent(new CustomEvent('theme-sync', { detail: { type: 'theme', value: theme } }))
  }
}

const handleColorChange = (color: string) => {
  store.setPrimaryColor(color)
  // 触发全局更新
  window.dispatchEvent(new CustomEvent('theme-sync', { detail: { type: 'color', value: color } }))
}

const handleFontSizeChange = (size: string) => {
  if (size === 'small' || size === 'medium' || size === 'large') {
    store.setFontSize(size)
    // 触发全局更新
    window.dispatchEvent(new CustomEvent('theme-sync', { detail: { type: 'fontSize', value: size } }))
  }
}

const handleLanguageChange = (language: string) => {
  if (language === 'zh-CN' || language === 'en-US' || language === 'ja-JP') {
    store.setLanguage(language)
    window.dispatchEvent(new CustomEvent('theme-sync', { detail: { type: 'language', value: language } }))
  }
}

const handleDefaultViewChange = (view: string) => {
  if (view === 'dashboard' || view === 'list' || view === 'grid') {
    store.setDefaultView(view)
    window.dispatchEvent(new CustomEvent('theme-sync', { detail: { type: 'defaultView', value: view } }))
  }
}

const handleNotificationMethodChange = (method: string) => {
  if (method === 'browser' || method === 'email' || method === 'sms' || method === 'none') {
    store.setNotificationMethod(method)
  }
}

const handleAutoRefreshChange = () => {
  store.setAutoRefresh(autoRefreshEnabled.value)
}

const handleRefreshIntervalChange = () => {
  store.setRefreshInterval(refreshIntervalValue.value)
}

const handleShowAnimationsChange = () => {
  store.setShowAnimations(showAnimationsEnabled.value)
  window.dispatchEvent(new CustomEvent('theme-sync', { detail: { type: 'showAnimations', value: showAnimationsEnabled.value } }))
}

const handleCompactModeChange = () => {
  store.setCompactMode(compactModeEnabled.value)
  window.dispatchEvent(new CustomEvent('theme-sync', { detail: { type: 'compactMode', value: compactModeEnabled.value } }))
}
</script>

<style scoped>
.theme-settings {
  display: flex;
  flex-direction: column;
  gap: 2rem;
}

.setting-item {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.setting-item label {
  font-size: 0.95rem;
  font-weight: 500;
  color: var(--text-primary, #0A1F33);
  margin-bottom: 0.5rem;
}

.theme-options,
.font-size-options {
  display: flex;
  gap: 0.75rem;
  flex-wrap: wrap;
}

.theme-btn,
.font-size-btn {
  padding: 0.75rem 1.25rem;
  border: 1px solid var(--border-color, rgba(10, 31, 51, 0.12));
  /* 使用主题背景色，亮色下是柔和浅色，暗色下是深色面板 */
  background: var(--bg-secondary, rgba(15, 23, 42, 0.8));
  color: var(--text-secondary, #364864);
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.9rem;
}

.theme-btn:hover,
.font-size-btn:hover {
  background: var(--bg-tertiary, rgba(15, 23, 42, 0.9));
  border-color: var(--border-dark, rgba(10, 31, 51, 0.25));
  transform: translateY(-2px);
}

.theme-btn.active,
.font-size-btn.active {
  /* 亮色模式下是柔和浅蓝 pill，暗色模式下仍然有明显高亮 */
  background: color-mix(in srgb, var(--color-primary, #1E8AE6) 12%, transparent);
  border-color: var(--color-primary, #1E8AE6);
  color: var(--color-primary-dark, #0A2F63);
  box-shadow: 0 4px 12px rgba(30, 138, 230, 0.25);
}

.theme-icon {
  font-size: 1.2rem;
}

.color-options {
  display: flex;
  gap: 0.75rem;
  flex-wrap: wrap;
}

.color-btn {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  border: 2px solid transparent;
  cursor: pointer;
  transition: all 0.3s ease;
  position: relative;
}

.color-btn:hover {
  transform: scale(1.1);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
}

.color-btn.active {
  border-color: rgba(255, 255, 255, 0.8);
  box-shadow: 0 0 0 2px rgba(255, 255, 255, 0.3), 0 4px 12px rgba(0, 0, 0, 0.3);
  transform: scale(1.1);
}

.color-btn.active::after {
  content: '✓';
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  color: #fff;
  font-size: 1rem;
  font-weight: bold;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.5);
}

/* 语言选项 */
.language-options {
  display: flex;
  gap: 0.75rem;
  flex-wrap: wrap;
}

.language-btn {
  padding: 0.75rem 1.25rem;
  border: 1px solid rgba(255, 255, 255, 0.15);
  background: rgba(15, 23, 42, 0.6);
  color: rgba(248, 250, 252, 0.8);
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.9rem;
}

.language-btn:hover {
  background: rgba(255, 255, 255, 0.1);
  border-color: rgba(255, 255, 255, 0.25);
  transform: translateY(-2px);
}

.language-btn.active {
  background: rgba(135, 206, 235, 0.2);
  border-color: rgba(135, 206, 235, 0.5);
  color: #0A2F63;
  box-shadow: 0 4px 12px rgba(135, 206, 235, 0.3);
}

.language-icon {
  font-size: 1.2rem;
}

/* 视图选项 */
.view-options {
  display: flex;
  gap: 0.75rem;
  flex-wrap: wrap;
}

.view-btn {
  padding: 0.75rem 1.25rem;
  border: 1px solid rgba(255, 255, 255, 0.15);
  background: rgba(15, 23, 42, 0.6);
  color: rgba(248, 250, 252, 0.8);
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.9rem;
}

.view-btn:hover {
  background: rgba(255, 255, 255, 0.1);
  border-color: rgba(255, 255, 255, 0.25);
  transform: translateY(-2px);
}

.view-btn.active {
  background: rgba(135, 206, 235, 0.2);
  border-color: rgba(135, 206, 235, 0.5);
  color: #0A2F63;
  box-shadow: 0 4px 12px rgba(135, 206, 235, 0.3);
}

.view-icon {
  font-size: 1.2rem;
}

/* 通知选项 */
.notification-options {
  display: flex;
  gap: 0.75rem;
  flex-wrap: wrap;
}

.notification-btn {
  padding: 0.75rem 1.25rem;
  border: 1px solid rgba(255, 255, 255, 0.15);
  background: rgba(15, 23, 42, 0.6);
  color: rgba(248, 250, 252, 0.8);
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.9rem;
}

.notification-btn:hover {
  background: rgba(255, 255, 255, 0.1);
  border-color: rgba(255, 255, 255, 0.25);
  transform: translateY(-2px);
}

.notification-btn.active {
  background: rgba(135, 206, 235, 0.2);
  border-color: rgba(135, 206, 235, 0.5);
  color: #0A2F63;
  box-shadow: 0 4px 12px rgba(135, 206, 235, 0.3);
}

.notification-icon {
  font-size: 1.2rem;
}

/* 其他设置 */
.other-settings {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.toggle-setting {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.toggle-label {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  cursor: pointer;
  user-select: none;
}

.toggle-input {
  width: 20px;
  height: 20px;
  cursor: pointer;
  accent-color: var(--color-primary, #2563eb);
}

.toggle-text {
  font-size: 0.9rem;
  color: var(--text-primary, rgba(248, 250, 252, 0.9));
}

.interval-input {
  width: 80px;
  padding: 0.5rem;
  border: 1px solid rgba(255, 255, 255, 0.15);
  background: rgba(15, 23, 42, 0.6);
  color: rgba(248, 250, 252, 0.9);
  border-radius: 8px;
  font-size: 0.9rem;
  text-align: center;
}

.interval-input:focus {
  outline: none;
  border-color: rgba(99, 102, 241, 0.5);
  box-shadow: 0 0 0 2px rgba(99, 102, 241, 0.2);
}

.interval-unit {
  font-size: 0.9rem;
  color: var(--text-secondary, rgba(248, 250, 252, 0.7));
}

/* 响应式设计 */
@media (max-width: 768px) {
  .theme-options,
  .font-size-options,
  .color-options,
  .language-options,
  .view-options,
  .notification-options {
    gap: 0.5rem;
  }

  .theme-btn,
  .font-size-btn,
  .language-btn,
  .view-btn,
  .notification-btn {
    padding: 0.6rem 1rem;
    font-size: 0.85rem;
  }

  .color-btn {
    width: 35px;
    height: 35px;
  }
}
</style>


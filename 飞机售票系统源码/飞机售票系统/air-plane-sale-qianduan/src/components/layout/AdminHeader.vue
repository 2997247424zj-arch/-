<template>
  <header class="admin-header">
    <div class="header-left">
      <h1 class="system-title">飞机售票系统</h1>
    </div>
    <div class="header-right">
      <span class="user-info">{{ userInfo?.realName || userInfo?.username || '系统管理员' }}</span>
      <!-- Auto-refresh controls for admins -->
      <div v-if="userInfo?.role === 'admin'" class="auto-refresh-controls" style="display:flex;align-items:center;gap:8px;">
        <label style="color:rgba(255,255,255,0.7);font-size:13px;margin-right:6px;">自动刷新</label>
        <input type="checkbox" v-model="autoRefreshEnabled" @change="toggleAutoRefresh" />
        <select v-model.number="autoRefreshInterval" @change="changeInterval" style="padding:6px;border-radius:6px;background:rgba(15,23,42,0.6);color:#fff;border:1px solid rgba(255,255,255,0.06);">
          <option :value="5">5s</option>
          <option :value="10">10s</option>
          <option :value="15">15s</option>
          <option :value="30">30s</option>
        </select>
      </div>
      <button class="header-btn weather-btn" @click="goToWeather">查询天气</button>
      <button class="header-btn personalization-btn" @click="openPersonalizationSettings">个性化设置</button>
      <button ref="logoutButtonRef" class="header-btn logout-btn" @click="handleLogout">退出登录</button>
    </div>
  </header>

  <!-- 退出确认对话框 -->
  <ModalPrompt
    v-model="logoutConfirmVisible"
    title="确认退出"
    message="确定要退出登录吗？"
    type="confirm"
    confirm-text="确定退出"
    cancel-text="取消"
    :show-cancel="true"
    :adaptive-position="true"
    placement="bottom"
    :offset="18"
    :anchor="logoutAnchorRect"
    anchor-alignment="start"
    :anchor-offset-x="-60"
    @confirm="confirmLogout"
    @cancel="cancelLogout"
  />

  <transition name="personalization-overlay">
    <div
      v-if="showPersonalizationModal"
      class="personalization-modal-overlay"
      @click="closePersonalizationSettings"
    >
      <transition name="personalization-panel">
        <div
          class="personalization-modal"
          v-show="showPersonalizationModal"
          @click.stop
        >
          <header class="personalization-modal-header">
            <div>
              <p class="modal-subtitle">个性化设置</p>
              <h2>界面主题与体验偏好</h2>
            </div>
            <button class="modal-close-btn" @click="closePersonalizationSettings">×</button>
          </header>
          <div class="personalization-modal-body">
            <ThemeSettings />
          </div>
        </div>
      </transition>
    </div>
  </transition>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { authApi } from '../../services/api'
import ModalPrompt from '../ModalPrompt.vue'
import store from '../../services/store'
import ThemeSettings from '../ThemeSettings.vue'
import { onMounted as onM } from 'vue'

interface UserInfo {
  id: string
  username: string
  realName: string
  email: string
  phone: string
  role?: string
  homePath?: string
}

const router = useRouter()

const userInfo = ref<UserInfo | null>(null)
const logoutButtonRef = ref<HTMLButtonElement | null>(null)
const logoutAnchorRect = ref<{ x: number; y: number; width: number; height: number } | null>(null)
const showPersonalizationModal = ref(false)

const openPersonalizationSettings = () => {
  showPersonalizationModal.value = true
}

const goToWeather = () => {
  router.push('/portal/passengers/weather')
}

const closePersonalizationSettings = () => {
  showPersonalizationModal.value = false
}

// 退出确认对话框状态
const logoutConfirmVisible = ref(false)

// 处理退出登录 - 显示确认对话框，并记录触发按钮位置
const handleLogout = () => {
  if (logoutButtonRef.value) {
    const rect = logoutButtonRef.value.getBoundingClientRect()
    logoutAnchorRect.value = {
      x: rect.left,
      y: rect.top,
      width: rect.width,
      height: rect.height
    }
  }
  logoutConfirmVisible.value = true
}

// 确认退出登录
const confirmLogout = async () => {
  logoutConfirmVisible.value = false
  
  try {
    // 异步调用退出API，但不等待结果（避免阻塞）
    authApi.logout().catch(() => {
      // API调用失败不影响退出流程
      console.warn('退出登录API调用失败，但继续退出流程')
    })
  } catch (error) {
    // 忽略错误，继续退出流程
  }
  
  // 立即清除用户状态并跳转，不等待API响应
  store.clearUserState()
  userInfo.value = null
  router.push('/').catch(() => {
    window.location.href = '/'
  })
}

// 取消退出
const cancelLogout = () => {
  logoutConfirmVisible.value = false
}

// 加载用户信息
const loadUserInfo = async () => {
  // 先从本地存储获取，立即显示，避免卡顿
  const storedUser = sessionStorage.getItem('user_info')
  if (storedUser) {
    try {
      userInfo.value = JSON.parse(storedUser)
    } catch (error) {
      console.error('解析本地用户信息失败:', error)
    }
  }
}

onMounted(() => {
  loadUserInfo()
  // init auto-refresh settings if admin
  if (userInfo.value?.role === 'admin') {
    const enabled = sessionStorage.getItem('admin_auto_refresh')
    const interval = sessionStorage.getItem('admin_auto_refresh_interval')
    if (enabled === null) sessionStorage.setItem('admin_auto_refresh', 'true')
    if (interval === null) sessionStorage.setItem('admin_auto_refresh_interval', '10')
  }
})

// (no mobile toggle - header remains static)

// auto-refresh UI state and handlers
const autoRefreshEnabled = ref<boolean>(String(sessionStorage.getItem('admin_auto_refresh') ?? 'true') === 'true')
const autoRefreshInterval = ref<number>(Number(sessionStorage.getItem('admin_auto_refresh_interval') ?? 10))

const toggleAutoRefresh = () => {
  sessionStorage.setItem('admin_auto_refresh', autoRefreshEnabled.value ? 'true' : 'false')
  window.dispatchEvent(new Event('admin-auto-refresh-updated'))
}

const changeInterval = () => {
  sessionStorage.setItem('admin_auto_refresh_interval', String(autoRefreshInterval.value))
  window.dispatchEvent(new Event('admin-auto-refresh-updated'))
}
</script>

<style scoped>
/* 顶部标题栏 */
.admin-header {
  height: 64px;
  background: linear-gradient(180deg, var(--bg-primary, #F7FAFF), color-mix(in srgb, var(--bg-secondary, #EFF3FF) 60%, transparent));
  border-bottom: 1px solid var(--border-light, rgba(10,31,51,0.06));
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 24px;
  backdrop-filter: blur(6px) saturate(120%);
  z-index: 100;
  position: sticky;
  top: 0;
  box-shadow: 0 6px 18px rgba(16,24,40,0.04);
}

.admin-header.scroll-opaque {
  background: linear-gradient(135deg, rgba(10, 15, 30, 0.98), rgba(20, 30, 45, 0.98));
  border-bottom-color: rgba(148,163,184,0.08);
  transition: background 240ms ease, border-color 240ms;
}

.header-left {
  display: flex;
  align-items: center;
}

.system-title {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
  color: var(--color-primary);
  letter-spacing: 0.2px;
  text-shadow: 0 2px 8px rgba(37,99,235,0.06);
}

.header-right {
  display: flex;
  align-items: center;
  gap: 15px;
}

.user-info {
  color: var(--text-secondary);
  font-size: 14px;
}

.header-btn {
  padding: 8px 14px;
  border: 1px solid var(--border-color, rgba(10,31,51,0.08));
  background: linear-gradient(180deg, rgba(255,255,255,0.92), rgba(255,255,255,0.86));
  color: var(--text-primary);
  border-radius: 10px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 600;
  transition: transform 220ms ease, box-shadow 220ms ease;
  box-shadow: 0 6px 18px rgba(16,24,40,0.04);
}

.header-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 24px rgba(16,24,40,0.06);
}

.header-btn:hover {
  background: color-mix(in srgb, var(--color-primary, #2563eb) 8%, transparent);
  border-color: color-mix(in srgb, var(--color-primary, #2563eb) 12%, transparent);
  color: var(--color-primary-dark);
}

.logout-btn {
  color: var(--color-error);
  border-color: rgba(239, 68, 68, 0.12);
}

.logout-btn:hover {
  background: rgba(239, 68, 68, 0.06);
  border-color: rgba(239, 68, 68, 0.22);
  color: var(--color-error);
}

.personalization-btn {
  border-color: rgba(10,31,51,0.06);
  color: var(--text-secondary);
}

.personalization-btn:hover {
  background: color-mix(in srgb, var(--color-primary, #2563eb) 8%, transparent);
  border-color: color-mix(in srgb, var(--color-primary, #2563eb) 18%, transparent);
  color: var(--color-primary-dark);
}

.weather-btn {
  border-color: rgba(10,31,51,0.06);
  color: var(--text-secondary);
}

.weather-btn:hover {
  background: color-mix(in srgb, var(--color-primary, #2563eb) 8%, transparent);
  border-color: color-mix(in srgb, var(--color-primary, #2563eb) 18%, transparent);
  color: var(--color-primary-dark);
}

.personalization-modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(2, 6, 23, 0.28);
  backdrop-filter: blur(3px);
  display: flex;
  justify-content: flex-end;
  align-items: stretch;
  z-index: 2000;
}

.personalization-modal {
  width: min(520px, 92vw);
  height: 100vh;
  background: var(--bg-primary);
  border-left: 1px solid var(--border-color);
  box-shadow: -18px 0 40px rgba(15, 23, 42, 0.55);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.personalization-modal-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 24px 28px 16px;
  border-bottom: 1px solid var(--border-color);
}

.personalization-modal-header h2 {
  margin: 0.2rem 0 0;
  color: var(--text-primary);
  font-size: 1.4rem;
}

.modal-subtitle {
  margin: 0;
  font-size: 0.85rem;
  color: var(--text-secondary);
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.modal-close-btn {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  border: 1px solid var(--border-light);
  background: rgba(148, 163, 184, 0.06);
  color: var(--text-secondary);
  font-size: 1.4rem;
  cursor: pointer;
  transition: all 0.3s;
}

.modal-close-btn:hover {
  background: color-mix(in srgb, var(--color-primary, #1E8AE6) 8%, transparent);
  border-color: var(--color-primary);
  color: var(--text-primary);
}

.personalization-modal-body {
  padding: 24px 28px 32px;
  overflow-y: auto;
  flex: 1;
}

@media (max-width: 768px) {
  .personalization-modal {
    width: 100vw;
  }

  .personalization-modal-header,
  .personalization-modal-body {
    padding: 20px;
  }
}

.personalization-overlay-enter-active,
.personalization-overlay-leave-active {
  transition: opacity 0.25s ease;
}

.personalization-overlay-enter-from,
.personalization-overlay-leave-to {
  opacity: 0;
}

/* header button active / touch */
.header-btn:active {
  transform: translateY(0) scale(0.995);
  box-shadow: 0 2px 8px rgba(0,0,0,0.18);
}
.header-btn:focus {
  box-shadow: 0 0 0 6px rgba(30,138,230,0.08);
  outline: none;
}

.personalization-panel-enter-active,
.personalization-panel-leave-active {
  transition: transform 0.35s ease;
}

.personalization-panel-enter-from,
.personalization-panel-leave-to {
  transform: translateX(100%);
}
</style>


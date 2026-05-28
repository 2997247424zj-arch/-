<template>
  <header class="passenger-header">
    <div class="header-container">
      <div class="logo">
        <span>{{ t('layout.brand') }}</span>
      </div>
      <nav class="main-nav">
        <router-link 
          to="/portal/passengers" 
          class="nav-link"
          active-class="router-link-active"
          @click.prevent="handleNavClick('/portal/passengers')"
        >
          {{ t('layout.nav.home') }}
        </router-link>
        <router-link 
          to="/user-center" 
          class="nav-link"
          active-class="router-link-active"
          @click.prevent="handleNavClick('/user-center')"
        >
          {{ t('layout.nav.userCenter') }}
        </router-link>
      </nav>
      <div class="user-actions">
        <span class="user-welcome" v-if="userInfo">{{ userInfo.username }}</span>
        <button ref="logoutButtonRef" class="logout-btn" @click="handleLogout">{{ t('layout.logout.button') }}</button>
      </div>
    </div>
  </header>

  <!-- 退出确认对话框 -->
  <ModalPrompt
    v-model="logoutConfirmVisible"
    :title="t('layout.logout.title')"
    :message="t('layout.logout.message')"
    type="confirm"
    :confirm-text="t('layout.logout.confirm')"
    :cancel-text="t('layout.logout.cancel')"
    :show-cancel="true"
    :adaptive-position="true"
    placement="bottom"
    :offset="16"
    :anchor="logoutAnchorRect"
    anchor-alignment="start"
    :anchor-offset-x="-48"
    @confirm="confirmLogout"
    @cancel="cancelLogout"
  />
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import store from '../../services/store'
import { navigateTo } from '../../utils/navigation'
import ModalPrompt from '../ModalPrompt.vue'
import { authApi } from '../../services/api'
import { useI18n } from '../../services/i18n'

const router = useRouter()
const { t } = useI18n()

// 使用computed从store获取用户信息
const userInfo = computed(() => store.userState.userInfo)
const logoutButtonRef = ref<HTMLButtonElement | null>(null)
const logoutAnchorRect = ref<{ x: number; y: number; width: number; height: number } | null>(null)

// 退出确认对话框状态
const logoutConfirmVisible = ref(false)

// 处理导航点击 - 使用统一的导航函数
const handleNavClick = async (path: string) => {
  try {
    await navigateTo(router, { path, replace: false })
  } catch (error) {
    console.error('导航失败:', error)
    // 如果导航失败，使用传统方式
    router.push(path).catch(() => {
      console.error('路由跳转失败:', path)
    })
  }
}

// 处理退出登录 - 显示确认对话框
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
  router.push('/').catch(() => {
    window.location.href = '/'
  })
}

// 取消退出
const cancelLogout = () => {
  logoutConfirmVisible.value = false
}
</script>

<style scoped>
.passenger-header {
  background: rgba(135, 206, 235, 0.1);
  border-bottom: 1px solid var(--border-color);
  position: sticky;
  top: 0;
  z-index: 100;
  backdrop-filter: blur(18px);
}

.header-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  height: 60px;
  max-width: 1400px;
  margin: 0 auto;
}

.logo {
  font-size: 18px;
  font-weight: 600;
  background: var(--gradient-primary);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.main-nav {
  display: flex;
  gap: 16px;
}

.nav-link {
  padding: 8px 14px;
  color: var(--text-secondary);
  text-decoration: none;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.nav-link:hover,
.nav-link.router-link-active {
  color: var(--color-primary-dark);
  background: rgba(135, 206, 235, 0.15);
}

.user-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-welcome {
  color: var(--text-secondary);
}

.logout-btn {
  padding: 6px 14px;
  border: 1px solid var(--border-color);
  background: rgba(135, 206, 235, 0.08);
  color: var(--text-primary);
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.logout-btn:hover {
  background: rgba(135, 206, 235, 0.15);
  border-color: var(--color-primary);
  color: var(--color-primary-dark);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .header-container {
    padding: 0 16px;
  }
  
  .main-nav {
    display: none;
  }
}
</style>


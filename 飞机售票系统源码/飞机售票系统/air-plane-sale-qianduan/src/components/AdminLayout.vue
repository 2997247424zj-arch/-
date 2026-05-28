<template>
  <div class="admin-layout" :class="{ 'show-sidebar': isAdmin }">
    <!-- 顶部标题栏 -->
    <AdminHeader />

    <div class="admin-body">
      <!-- 左侧边栏导航 -->
      <aside class="admin-sidebar" v-if="isAdmin">
        <nav class="sidebar-nav">
          <div 
            class="nav-item" 
            :class="{ active: isStatisticsActive }"
            @click="navigateTo('/dashboard/statistics')"
            v-if="isAdmin"
          >
            <span class="nav-text">数据统计</span>
          </div>
          <div 
            class="nav-item" 
            :class="{ active: isQuickActionsActive }"
            @click="navigateTo('/dashboard/quick-actions')"
            v-if="isAdmin"
          >
            <span class="nav-text">快捷操作</span>
          </div>

          <!-- 航班信息管理 - 可展开 -->
          <div class="nav-group" v-if="isAdmin">
            <div 
              class="nav-item nav-group-header" 
              :class="{ active: isFlightManagementActive }"
              @click="toggleFlightMenu"
            >
              <span class="nav-text">航班信息管理</span>
              <span class="nav-arrow" :class="{ expanded: flightMenuExpanded }">▼</span>
            </div>
            <div class="nav-submenu" v-show="flightMenuExpanded">
              <div 
                class="nav-item nav-subitem" 
                :class="{ active: currentRoute === '/portal/flights' }"
                @click="navigateTo('/portal/flights')"
              >
                <span class="nav-text">航班信息</span>
              </div>
              <div 
                class="nav-item nav-subitem" 
                :class="{ active: currentRoute === '/portal/aircraft' }"
                @click="navigateTo('/portal/aircraft')"
              >
                <span class="nav-text">机型管理</span>
              </div>
            </div>
          </div>

          <!-- 订单业务管理 - 可展开 -->
          <div class="nav-group" v-if="isAdmin">
            <div 
              class="nav-item nav-group-header" 
              :class="{ active: isOrderManagementActive }"
              @click="toggleOrderMenu"
            >
              <span class="nav-text">订单业务管理</span>
              <span class="nav-arrow" :class="{ expanded: orderMenuExpanded }">▼</span>
            </div>
            <div class="nav-submenu" v-show="orderMenuExpanded">
              <div 
                class="nav-item nav-subitem" 
                :class="{ active: currentRoute === '/portal/orders' }"
                @click="navigateTo('/portal/orders')"
              >
                <span class="nav-text">订票信息管理</span>
              </div>
              <div 
                class="nav-item nav-subitem" 
                :class="{ active: currentRoute === '/portal/tickets' }"
                @click="navigateTo('/portal/tickets')"
              >
                <span class="nav-text">机票改签/退订管理</span>
              </div>
              <div 
                class="nav-item nav-subitem" 
                :class="{ active: currentRoute === '/portal/admin/special-requests' }"
                @click="navigateTo('/portal/admin/special-requests')"
              >
                <span class="nav-text">重点旅客预约管理</span>
              </div>
          
            </div>
          </div>

          <!-- 系统管理 - 可展开 -->
          <div class="nav-group" v-if="isAdmin">
            <div 
              class="nav-item nav-group-header" 
              :class="{ active: isSystemManagementActive }"
              @click="toggleSystemMenu"
            >
              <span class="nav-text">系统管理</span>
              <span class="nav-arrow" :class="{ expanded: systemMenuExpanded }">▼</span>
            </div>
            <div class="nav-submenu" v-show="systemMenuExpanded">
              <div 
                class="nav-item nav-subitem" 
                :class="{ active: currentRoute === '/portal/users' }"
                @click="navigateTo('/portal/users')"
              >
                <span class="nav-text">人员用户管理</span>
              </div>
              <div 
                class="nav-item nav-subitem" 
                :class="{ active: currentRoute === '/portal/admin/alerts' }"
                @click="navigateTo('/portal/admin/alerts')"
              >
                <span class="nav-text">异常情况监控</span>
              </div>
              <div 
                class="nav-item nav-subitem" 
                :class="{ active: currentRoute === '/portal/profile' }"
                @click="navigateTo('/portal/profile')"
              >
                <span class="nav-text">个人信息设置</span>
              </div>
            </div>
          </div>

          <!-- 角色体验 - 可展开 -->
          <div class="nav-group" v-if="isOperator && !isPassenger">
            <div 
              class="nav-item nav-group-header" 
              :class="{ active: isExperienceActive }"
              @click="toggleExperienceMenu"
            >
              <span class="nav-text">角色体验</span>
              <span class="nav-arrow" :class="{ expanded: experienceMenuExpanded }">▼</span>
            </div>
            <div class="nav-submenu" v-show="experienceMenuExpanded">
              <div 
                class="nav-item nav-subitem" 
                :class="{ active: currentRoute === '/portal/operations' }"
                @click="navigateTo('/portal/operations')"
                v-if="isAdmin || isOperator"
              >
                <span class="nav-text">航空运营</span>
              </div>
              <div 
                class="nav-item nav-subitem" 
                :class="{ active: currentRoute === '/portal/operations/special-requests-operator' }"
                @click="navigateTo('/portal/operations/special-requests-operator')"
                v-if="isAdmin || isOperator"
              >
                <span class="nav-text">重点旅客预约处理</span>
              </div>
            </div>
          </div>
        </nav>
      </aside>

      <!-- 右侧主内容区 -->
      <main class="admin-main">
        <slot></slot>
      </main>
    </div>

    <!-- 页脚 -->

  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import AdminHeader from './layout/AdminHeader.vue'
import AdminFooter from './layout/AdminFooter.vue'
import { initViewportEffects } from '../utils/viewport'

const router = useRouter()
const route = useRoute()

const flightMenuExpanded = ref(true) // 默认展开航班信息管理
const orderMenuExpanded = ref(true) // 默认展开订单业务管理
const systemMenuExpanded = ref(true) // 默认展开系统管理
const experienceMenuExpanded = ref(true)
// mobile sidebar state removed - keep original behavior

const currentRoute = computed(() => route.path)

// 从sessionStorage获取用户信息以判断角色
const userInfo = computed(() => {
  try {
    const stored = sessionStorage.getItem('user_info')
    return stored ? JSON.parse(stored) : null
  } catch {
    return null
  }
})

const userRole = computed(() => userInfo.value?.role || 'admin')
const isAdmin = computed(() => userRole.value === 'admin')
const isOperator = computed(() => userRole.value === 'operator')
const isPassenger = computed(() => userRole.value === 'passenger')

const isFlightManagementActive = computed(() => {
  return currentRoute.value === '/portal/flights' || 
         currentRoute.value === '/portal/aircraft'
})

const isOrderManagementActive = computed(() => {
  return currentRoute.value === '/portal/orders' || 
         currentRoute.value === '/portal/tickets' ||
         currentRoute.value === '/portal/refunds' ||
         currentRoute.value === '/portal/admin/special-requests'
})

const isSystemManagementActive = computed(() => {
  return currentRoute.value === '/portal/users' ||
         currentRoute.value === '/portal/profile' ||
         currentRoute.value === '/portal/admin/alerts'
})

const isExperienceActive = computed(() => {
  return currentRoute.value === '/portal/operations' ||
         currentRoute.value === '/portal/passengers'
})

// 左侧“数据统计 / 快捷操作” 活动状态
const isStatisticsActive = computed(() => {
  return currentRoute.value === '/dashboard' || currentRoute.value.startsWith('/dashboard/statistics')
})

const isQuickActionsActive = computed(() => {
  return currentRoute.value.startsWith('/dashboard/quick-actions')
})

// 切换航班菜单展开/收起
const toggleFlightMenu = () => {
  flightMenuExpanded.value = !flightMenuExpanded.value
}

// 切换订单菜单展开/收起
const toggleOrderMenu = () => {
  orderMenuExpanded.value = !orderMenuExpanded.value
}

// 切换系统菜单展开/收起
const toggleSystemMenu = () => {
  systemMenuExpanded.value = !systemMenuExpanded.value
}

const toggleExperienceMenu = () => {
  experienceMenuExpanded.value = !experienceMenuExpanded.value
}

// 导航到指定路由
const navigateTo = (path: string) => {
  router.push(path)
}

// 根据当前路由自动展开对应的菜单
watch(currentRoute, (newPath) => {
  if (newPath.startsWith('/portal/flights') || newPath.startsWith('/portal/aircraft')) {
    flightMenuExpanded.value = true
  }
  if (newPath.startsWith('/portal/orders') || newPath.startsWith('/portal/tickets') || newPath.startsWith('/portal/refunds')) {
    orderMenuExpanded.value = true
  }
  if (newPath.startsWith('/portal/users') || newPath.startsWith('/portal/profile')) {
    systemMenuExpanded.value = true
  }
  if (newPath.startsWith('/portal/operations') || newPath.startsWith('/portal/passengers')) {
    experienceMenuExpanded.value = true
  }
}, { immediate: true })

// ====== Admin auto-refresh dispatcher ======
// Config: read from localStorage (seconds) and enabled flag
const autoRefreshEnabled = ref<boolean>(String(sessionStorage.getItem('admin_auto_refresh') ?? 'true') === 'true')
const autoRefreshIntervalSeconds = ref<number>(Number(sessionStorage.getItem('admin_auto_refresh_interval') ?? 10))
let autoRefreshTimer: number | null = null

const startAutoRefresh = () => {
  stopAutoRefresh()
  if (!autoRefreshEnabled.value) return
  if (!isAdmin.value) return
  const intervalMs = Math.max(3, Number(autoRefreshIntervalSeconds.value || 10)) * 1000
  autoRefreshTimer = window.setInterval(() => {
    try {
      // dispatch a global event pages can listen to
      window.dispatchEvent(new CustomEvent('auto-refresh', { detail: { ts: Date.now() } }))
      // debug
      // console.debug('[AdminLayout] dispatched auto-refresh')
    } catch (e) {
      console.warn('auto-refresh dispatch failed', e)
    }
  }, intervalMs) as unknown as number
}

const stopAutoRefresh = () => {
  if (autoRefreshTimer) {
    clearInterval(autoRefreshTimer)
    autoRefreshTimer = null
  }
}

// start when mounted and when isAdmin flips true; stop otherwise
onMounted(() => {
  if (isAdmin.value && autoRefreshEnabled.value) startAutoRefresh()
  // init viewport animations and parallax once per layout mount
  const vp = initViewportEffects()
  // store on window for debug if needed
  ;(window as any).__admin_viewport_effects = vp
})

watch(isAdmin, (val) => {
  if (val && autoRefreshEnabled.value) startAutoRefresh()
  else stopAutoRefresh()
})

// mobile toggle listener removed

// allow external toggles via sessionStorage change (rare)
window.addEventListener('storage', (ev: StorageEvent) => {
  if (ev.key === 'admin_auto_refresh' || ev.key === 'admin_auto_refresh_interval') {
    autoRefreshEnabled.value = String(sessionStorage.getItem('admin_auto_refresh') ?? 'true') === 'true'
    autoRefreshIntervalSeconds.value = Number(sessionStorage.getItem('admin_auto_refresh_interval') ?? 10)
    if (isAdmin.value && autoRefreshEnabled.value) startAutoRefresh()
    else stopAutoRefresh()
  }
})
// also listen to in-window custom event from header
window.addEventListener('admin-auto-refresh-updated', () => {
  autoRefreshEnabled.value = String(sessionStorage.getItem('admin_auto_refresh') ?? 'true') === 'true'
  autoRefreshIntervalSeconds.value = Number(sessionStorage.getItem('admin_auto_refresh_interval') ?? 10)
  if (isAdmin.value && autoRefreshEnabled.value) startAutoRefresh()
  else stopAutoRefresh()
})

onUnmounted(() => {
  stopAutoRefresh()
  try { (window as any).__admin_viewport_effects?.destroy?.() } catch {}
})
</script>

<style scoped>
/* 整体布局容器：使用最小高度 + 浏览器统一滚动条，避免内部多重滚动 */
.admin-layout {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background: var(--bg-primary);
  color: var(--color-text-inverse);
  position: relative;
}

.admin-layout::before {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, #e0e3e9 0%, #b2c7e8 100%);
  opacity: 1;
  pointer-events: none;
  z-index: 0;
}


/* 主体区域：交由页面统一滚动，不再在此处裁剪滚动条 */
.admin-body {
  display: flex;
  flex: 1;
  position: relative;
  z-index: 1;
  /* 让主体区域直接露出整体背景，而不是再叠一层强烈底色 */
  background: transparent;
}

/* 左侧边栏：固定在视口内，内部只保留一个垂直滚动条 */
.admin-sidebar {
  width: 220px;
  /* 使用设计代币以便主题同步（浅色为主） */
  background: linear-gradient(180deg, var(--bg-secondary, #F7FAFF), var(--app-surface, #FFFFFF));
  border-right: 1px solid var(--border-light, rgba(15,23,42,0.06));
  flex-shrink: 0;
  backdrop-filter: blur(8px);
  display: block;
  position: sticky;
  top: 60px; /* 与顶部标题栏高度保持一致 */
  max-height: 1500px;
  overflow-y: auto; /* 整个侧边栏区域仅使用这一个滚动条 */
  box-shadow: var(--shadow-sm, 4px 0 18px rgba(16,24,40,0.06));
  transition: transform 320ms cubic-bezier(.2,.9,.2,1), opacity 240ms;
}

/* 只在管理员和运营角色显示侧边栏 */
.admin-layout.show-sidebar .admin-sidebar {
  display: block;
}

/* 移动端打开侧边栏状态 */
/* mobile-open styles removed - sidebar static behavior restored */

/* 主内容区域：由页面滚动控制，避免与侧边栏产生多重滚动条 */
.admin-main {
  flex: 1;
  background: #f6fbff;
  position: relative;
}

.sidebar-nav {
  padding: 16px 12px;
}

.nav-item {
  padding: 10px 16px;
  cursor: pointer;
  transition: all 0.22s ease;
  display: flex;
  align-items: center;
  justify-content: space-between;
  color: var(--text-secondary, #edf0f3);
  font-size: 13px;
  border-radius: 10px;
  background: transparent;
  position: relative;
  margin-bottom: 6px;
}

.nav-item:hover {
  background: linear-gradient(90deg, rgba(37,99,235,0.06), rgba(59,130,246,0.03));
  color: var(--text-primary);
  transform: translateX(2px);
}

.nav-item.active {
  /* 激活态改为柔和的胶囊高亮，使用主色变量 */
  background: linear-gradient(120deg, color-mix(in srgb, var(--color-primary) 18%, transparent), color-mix(in srgb, var(--color-primary-dark) 6%, transparent));
  color: var(--text-primary);
  box-shadow: 0 8px 18px rgba(16,24,40,0.06);
  border: 1px solid color-mix(in srgb, var(--color-primary) 12%, transparent);
}

.nav-item.active .nav-text {
  color: var(--text-primary);
}

.nav-text {
  flex: 1;
}

.nav-arrow {
  font-size: 10px;
  transition: transform 0.3s;
  color: inherit;
}

.nav-arrow.expanded {
  transform: rotate(180deg);
}

.nav-group-header {
  font-weight: 500;
  letter-spacing: 0.02em;
}

.nav-submenu {
  /* 子菜单采用轻玻璃质感，和主题颜色对齐 */
  background: color-mix(in srgb, var(--app-surface, #FFFFFF) 92%, transparent);
  border-radius: 12px;
  border: 1px solid var(--border-light, rgba(128, 160, 234, 0.06));
  margin: 6px 0 12px 10px;
  padding: 6px 6px;
  backdrop-filter: blur(6px);
  box-shadow: var(--shadow-sm, 0 6px 14px rgba(16,24,40,0.04));
}

.nav-subitem {
  padding-left: 26px;
  font-size: 12px;
}

.nav-subitem.active {
  background: linear-gradient(90deg, color-mix(in srgb, var(--color-primary) 12%, transparent), transparent);
  color: var(--text-primary);
}

/* 右侧主内容区 */
.admin-main {
  flex: 1;
  background: transparent;
  padding: 24px 32px;
  width: 100%;
  display: block;
  min-height: calc(100vh - 64px);
  position: relative;
}

/* 美化滚动条 */
.admin-main::-webkit-scrollbar {
  width: 8px;
}

.admin-main::-webkit-scrollbar-track {
  background: rgba(71, 111, 205, 0.3);
  border-radius: 4px;
}

.admin-main::-webkit-scrollbar-thumb {
  background: rgba(98, 153, 230, 0.4);
  border-radius: 4px;
}

.admin-main::-webkit-scrollbar-thumb:hover {
  background: rgba(108, 157, 226, 0.6);
}

/* 美化侧边栏 */
.admin-sidebar {
  background: linear-gradient(180deg, rgba(237, 239, 242, 0.4), rgba(188, 200, 221, 0.3));
  border-right: 1px solid rgba(148, 163, 184, 0.2);
  box-shadow: 4px 0 24px rgba(181, 153, 153, 0.2);
}

.nav-item {
  margin-bottom: 4px;
  position: relative;
}

.nav-item::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 3px;
  height: 0;
  background: linear-gradient(180deg, #1E8AE6, #a7c3e9);
  border-radius: 0 3px 3px 0;
  transition: height 0.3s ease;
}

.nav-item.active::before {
  height: 60%;
}

.nav-item.active {
  background: linear-gradient(120deg, rgba(30, 138, 230, 0.2), rgba(99, 102, 241, 0.15));
  color: #ffffff;
  box-shadow: 0 4px 12px rgba(30, 138, 230, 0.2);
  border: 1px solid rgba(30, 138, 230, 0.3);
}

.nav-submenu {
  background: rgba(166, 181, 214, 0.3);
  border-radius: 12px;
  border: 1px solid rgba(148, 163, 184, 0.15);
  margin: 6px 0 12px 12px;
  padding: 6px 4px;
  backdrop-filter: blur(10px);
}

.nav-subitem.active {
  background: linear-gradient(90deg, rgba(30, 138, 230, 0.2), transparent);
  color: #60a5fa;
  border-left: 2px solid #1E8AE6;
  padding-left: 24px;
}
</style>


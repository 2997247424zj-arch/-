<template>
  <div class="admin-layout">
    <aside class="admin-sidebar">
      <div class="sidebar-brand" @click="router.push('/admin')">
        <div class="brand-mark">
          <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round">
            <rect x="3" y="3" width="7" height="7" rx="2" />
            <rect x="14" y="3" width="7" height="7" rx="2" />
            <rect x="14" y="14" width="7" height="7" rx="2" />
            <rect x="3" y="14" width="7" height="7" rx="2" />
          </svg>
        </div>
        <div>
          <h1>MicroMall Admin</h1>
          <p>后台管理中心</p>
        </div>
      </div>

      <nav class="sidebar-nav">
        <button class="nav-btn" :class="{ active: route.path === '/admin' }" @click="router.push('/admin')">
          <span class="nav-icon">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M3 13h8V3H3zM13 21h8v-6h-8zM13 3v6h8V3zM3 21h8v-6H3z" />
            </svg>
          </span>
          <span>控制台</span>
        </button>

        <button class="nav-btn" :class="{ active: route.path.startsWith('/admin/users') }" @click="router.push('/admin/users')">
          <span class="nav-icon">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2" />
              <circle cx="9" cy="7" r="4" />
              <path d="M23 21v-2a4 4 0 0 0-3-3.87" />
              <path d="M16 3.13a4 4 0 0 1 0 7.75" />
            </svg>
          </span>
          <span>用户管理</span>
        </button>

        <button class="nav-btn" :class="{ active: route.path.startsWith('/admin/products') }" @click="router.push('/admin/products')">
          <span class="nav-icon">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M6 2l12 4v6c0 5-3.5 9.74-8 11-4.5-1.26-8-6-8-11V6l12-4z" />
            </svg>
          </span>
          <span>商品管理</span>
        </button>

        <button class="nav-btn" :class="{ active: route.path.startsWith('/admin/orders') }" @click="router.push('/admin/orders')">
          <span class="nav-icon">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
              <polyline points="14 2 14 8 20 8" />
              <line x1="16" y1="13" x2="8" y2="13" />
              <line x1="16" y1="17" x2="8" y2="17" />
              <polyline points="10 9 9 9 8 9" />
            </svg>
          </span>
          <span>订单管理</span>
        </button>

        <button class="nav-btn" :class="{ active: route.path.startsWith('/admin/settings') }" @click="router.push('/admin/settings')">
          <span class="nav-icon">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round">
              <circle cx="12" cy="12" r="3" />
              <path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 1 1-2.83 2.83l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 1 1-4 0v-.09a1.65 1.65 0 0 0-1-1.51 1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 1 1-2.83-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 1 1 0-4h.09a1.65 1.65 0 0 0 1.51-1 1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 1 1 2.83-2.83l.06.06a1.65 1.65 0 0 0 1.82.33h.01a1.65 1.65 0 0 0 1-1.51V3a2 2 0 1 1 4 0v.09a1.65 1.65 0 0 0 1 1.51h.01a1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 1 1 2.83 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82v.01a1.65 1.65 0 0 0 1.51 1H21a2 2 0 1 1 0 4h-.09a1.65 1.65 0 0 0-1.51 1z" />
            </svg>
          </span>
          <span>系统设置</span>
        </button>
      </nav>

    </aside>

    <div class="admin-main">
      <header class="admin-header">
        <div>
          <h2>{{ pageTitle }}</h2>
          <p>欢迎进入 MicroMall 管理后台</p>
        </div>
        <div class="admin-actions">
          <span class="admin-badge">管理员</span>
          <div class="admin-user">
            <el-avatar
              :size="38"
              :src="userStore.userInfo?.avatar || 'https://api.dicebear.com/7.x/thumbs/svg?seed=admin'"
            />
            <div>
              <strong>{{ userStore.userInfo?.username || 'Admin' }}</strong>
              <span>系统管理者</span>
            </div>
          </div>
          <el-button round @click="handleLogout">退出登录</el-button>
        </div>
      </header>

      <main class="admin-content">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const pageTitle = computed(() => {
  if (route.path.startsWith('/admin/users')) return '用户管理'
  if (route.path.startsWith('/admin/products')) return '商品管理'
  if (route.path.startsWith('/admin/orders')) return '订单管理'
  if (route.path.startsWith('/admin/settings')) return '系统设置'
  return '后台控制台'
})

const handleLogout = async () => {
  await userStore.logout()
  ElMessage.success('管理员已退出登录')
  router.push('/login')
}
</script>

<style scoped lang="scss">
.admin-layout {
  min-height: 100vh;
  display: grid;
  grid-template-columns: 280px 1fr;
  background: linear-gradient(180deg, #eff6ff 0%, #f8fbff 100%);
}

.admin-sidebar {
  background: linear-gradient(180deg, #0f172a 0%, #1e293b 100%);
  color: #fff;
  padding: 24px 18px;
  display: flex;
  flex-direction: column;
  gap: 28px;
  border-right: 1px solid rgba(148, 163, 184, 0.12);
  box-shadow: 16px 0 40px rgba(15, 23, 42, 0.18);
}

.sidebar-brand {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 12px 14px;
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.06);
  cursor: pointer;
  transition: background 0.2s ease;

  &:hover {
    background: rgba(255, 255, 255, 0.1);
  }

  h1 {
    font-size: 18px;
    color: #fff;
    margin-bottom: 4px;
  }

  p {
    font-size: 12px;
    color: rgba(226, 232, 240, 0.8);
  }
}

.brand-mark {
  width: 46px;
  height: 46px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #2563eb, #22c55e);
  color: #fff;
  box-shadow: 0 10px 24px rgba(37, 99, 235, 0.28);
}

.sidebar-nav {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.nav-btn {
  width: 100%;
  border: none;
  background: transparent;
  color: rgba(226, 232, 240, 0.88);
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
  border-radius: 16px;
  font-size: 15px;
  font-weight: 700;
  cursor: pointer;
  transition:
    background 0.2s ease,
    color 0.2s ease,
    transform 0.2s ease;

  &:hover {
    background: rgba(37, 99, 235, 0.18);
    color: #fff;
    transform: translateX(3px);
  }

  &.active {
    background: linear-gradient(135deg, rgba(37, 99, 235, 0.9), rgba(34, 197, 94, 0.7));
    color: #fff;
    box-shadow: 0 14px 28px rgba(37, 99, 235, 0.2);
  }
}

.nav-icon {
  width: 34px;
  height: 34px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.08);
}

.admin-main {
  min-width: 0;
  display: flex;
  flex-direction: column;
}

.admin-header {
  height: 88px;
  padding: 0 28px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
  background: rgba(255, 255, 255, 0.88);
  backdrop-filter: blur(16px);
  border-bottom: 1px solid rgba(219, 234, 254, 0.9);

  h2 {
    font-size: 28px;
    margin-bottom: 6px;
  }

  p {
    font-size: 14px;
    color: #64748b;
  }
}

.admin-actions {
  display: flex;
  align-items: center;
  gap: 16px;
}

.admin-badge {
  display: inline-flex;
  align-items: center;
  padding: 8px 14px;
  border-radius: 999px;
  background: rgba(37, 99, 235, 0.12);
  color: #1d4ed8;
  font-size: 13px;
  font-weight: 800;
}

.admin-user {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 14px 8px 8px;
  border-radius: 999px;
  background: #fff;
  box-shadow: 0 10px 24px rgba(15, 23, 42, 0.06);

  strong {
    display: block;
    color: #0f172a;
    font-size: 14px;
    margin-bottom: 2px;
  }

  span {
    display: block;
    color: #64748b;
    font-size: 12px;
  }
}

.admin-content {
  flex: 1;
  padding: 28px;
  overflow: auto;
}

@media (max-width: 1200px) {
  .admin-layout {
    grid-template-columns: 96px 1fr;
  }

  .sidebar-brand div:last-child,
  .nav-btn span:last-child {
    display: none;
  }

  .nav-btn {
    justify-content: center;
  }
}

@media (max-width: 768px) {
  .admin-layout {
    grid-template-columns: 1fr;
  }

  .admin-sidebar {
    display: none;
  }

  .admin-header {
    height: auto;
    padding: 18px 16px;
    flex-direction: column;
    align-items: flex-start;
  }

  .admin-actions {
    width: 100%;
    flex-wrap: wrap;
  }

  .admin-content {
    padding: 16px;
  }
}
</style>

<template>
  <div class="shop-root">
    <!-- ===== HEADER ===== -->
    <header class="shop-header">
      <div class="header-inner">
        <!-- Brand -->
        <div class="brand" @click="router.push('/')">
          <div class="brand-icon">
            <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
              <path d="M6 2 3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4z"/>
              <line x1="3" y1="6" x2="21" y2="6"/>
              <path d="M16 10a4 4 0 0 1-8 0"/>
            </svg>
          </div>
          <span class="brand-name">MicroMall</span>
        </div>

        <!-- Search -->
        <div class="search-wrap">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索手机、电脑、服装..."
            clearable
            size="large"
            class="search-input"
            @keyup.enter="handleSearch"
          >
            <template #prefix>
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#64748b" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round">
                <circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/>
              </svg>
            </template>
            <template #append>
              <el-button class="search-btn" @click="handleSearch">搜索</el-button>
            </template>
          </el-input>
        </div>

        <!-- Nav -->
        <nav class="nav-links">
          <a class="nav-item" :class="{ active: route.path === '/home' }" @click="router.push('/home')">首页</a>
          <a class="nav-item" :class="{ active: route.path.startsWith('/product') }" @click="router.push('/product/list')">全部商品</a>
          <a v-if="userStore.isLoggedIn && !isAdmin" class="nav-item" :class="{ active: route.path === '/dashboard' }" @click="router.push('/dashboard')">数据看板</a>
        </nav>

        <!-- Actions -->
        <div class="header-actions">
          <!-- Cart -->
          <button v-if="!isAdmin" class="action-btn" @click="router.push('/cart')" title="购物车">
            <el-badge :value="cartStore.totalCount || ''" :hidden="!cartStore.totalCount" type="danger">
              <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <circle cx="9" cy="21" r="1"/><circle cx="20" cy="21" r="1"/>
                <path d="M1 1h4l2.68 13.39a2 2 0 0 0 2 1.61h9.72a2 2 0 0 0 2-1.61L23 6H6"/>
              </svg>
            </el-badge>
            <span>购物车</span>
          </button>

          <!-- User logged in -->
          <template v-if="userStore.isLoggedIn && userStore.userInfo && !isAdmin">
            <el-dropdown @command="handleCommand" trigger="click">
              <div class="user-trigger">
                <el-avatar
                  :size="36"
                  :src="userStore.userInfo.avatar || 'https://api.dicebear.com/7.x/thumbs/svg?seed=' + userStore.userInfo.username"
                />
                <span class="user-name">{{ userStore.userInfo.username }}</span>
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                  <polyline points="6 9 12 15 18 9"/>
                </svg>
              </div>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile">
                    <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="margin-right:8px;vertical-align:middle">
                      <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/>
                    </svg>
                    个人中心
                  </el-dropdown-item>
                  <el-dropdown-item command="orders">
                    <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="margin-right:8px;vertical-align:middle">
                      <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/>
                    </svg>
                    我的订单
                  </el-dropdown-item>
                  <el-dropdown-item command="address">
                    <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="margin-right:8px;vertical-align:middle">
                      <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/><circle cx="12" cy="10" r="3"/>
                    </svg>
                    收货地址
                  </el-dropdown-item>
                  <el-dropdown-item divided command="logout">
                    <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="#e11d48" stroke-width="2" style="margin-right:8px;vertical-align:middle">
                      <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/><polyline points="16 17 21 12 16 7"/><line x1="21" y1="12" x2="9" y2="12"/>
                    </svg>
                    <span style="color:#e11d48">退出登录</span>
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>

          <template v-else-if="userStore.isLoggedIn && userStore.userInfo && isAdmin">
            <el-button type="primary" round @click="router.push('/admin')">管理后台</el-button>
          </template>

          <!-- Not logged in -->
          <template v-else>
            <el-button type="primary" round @click="router.push('/login')">登录 / 注册</el-button>
          </template>
        </div>
      </div>
    </header>

    <!-- ===== MAIN ===== -->
    <main class="shop-main">
      <router-view v-slot="{ Component }">
        <transition name="fade" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </main>

    <!-- ===== FOOTER ===== -->
    <footer class="shop-footer">
      <div class="footer-inner">
        <div class="footer-brand">
          <div class="brand" @click="router.push('/')">
            <div class="brand-icon">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                <path d="M6 2 3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4z"/><line x1="3" y1="6" x2="21" y2="6"/><path d="M16 10a4 4 0 0 1-8 0"/>
              </svg>
            </div>
            <span class="brand-name" style="font-size:18px">MicroMall</span>
          </div>
          <p class="footer-slogan">品质生活，触手可及</p>
        </div>
        <div class="footer-links-group">
          <div class="footer-col">
            <h4>帮助中心</h4>
            <ul>
              <li><a href="javascript:;">购物指南</a></li>
              <li><a href="javascript:;">订单查询</a></li>
              <li><a href="javascript:;">退换货说明</a></li>
            </ul>
          </div>
          <div class="footer-col">
            <h4>关于我们</h4>
            <ul>
              <li><a href="javascript:;">公司简介</a></li>
              <li><a href="javascript:;">隐私政策</a></li>
              <li><a href="javascript:;">用户协议</a></li>
            </ul>
          </div>
          <div class="footer-col">
            <h4>联系我们</h4>
            <ul>
              <li><a href="javascript:;">在线客服</a></li>
              <li><a href="javascript:;">投诉建议</a></li>
              <li><a href="javascript:;">商务合作</a></li>
            </ul>
          </div>
        </div>
      </div>
      <div class="footer-bottom">
        <p>&copy; 2026 MicroMall 微服务电商平台. All rights reserved.</p>
      </div>
    </footer>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/store/user'
import { useCartStore } from '@/store/cart'
import { ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const cartStore = useCartStore()
const searchKeyword = ref('')
const isAdmin = computed(() => userStore.userInfo?.role === 'admin')

onMounted(() => {
  if (userStore.isLoggedIn) {
    userStore.fetchUserInfo()
    cartStore.fetchCartList()
  }
})

const handleSearch = () => {
  const kw = searchKeyword.value.trim()
  if (kw) {
    router.push({ name: 'SearchResult', query: { keyword: kw } })
    searchKeyword.value = ''
  }
}

const handleCommand = (command: string) => {
  if (command === 'logout') {
    userStore.logout()
    cartStore.clearCartState()
    ElMessage.success('已退出登录')
    router.push('/home')
  } else if (command === 'profile') {
    router.push('/user')
  } else if (command === 'orders') {
    router.push('/order/list')
  } else if (command === 'address') {
    router.push('/user/address')
  }
}
</script>

<style scoped lang="scss">
.shop-root {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

/* ---- HEADER ---- */
.shop-header {
  position: sticky;
  top: 0;
  z-index: 200;
  background: rgba(255, 255, 255, 0.88);
  backdrop-filter: blur(20px);
  border-bottom: 1px solid rgba(219, 234, 254, 0.8);
  box-shadow: 0 8px 32px rgba(30, 64, 175, 0.06);
}

.header-inner {
  width: min(var(--page-max-width), calc(100vw - 24px));
  margin: 0 auto;
  height: 68px;
  display: flex;
  align-items: center;
  gap: 20px;
}

/* Brand */
.brand {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  flex-shrink: 0;
  user-select: none;
}

.brand-icon {
  width: 40px;
  height: 40px;
  border-radius: 12px;
  background: linear-gradient(135deg, #1e40af, #3b82f6);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  box-shadow: 0 8px 20px rgba(30, 64, 175, 0.28);
  transition: transform 0.2s;
}

.brand:hover .brand-icon {
  transform: rotate(-6deg) scale(1.05);
}

.brand-name {
  font-family: 'Rubik', sans-serif;
  font-size: 22px;
  font-weight: 800;
  background: linear-gradient(135deg, #1e40af, #3b82f6);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  letter-spacing: -0.02em;
}

/* Search */
.search-wrap {
  flex: 1;
  max-width: 620px;

  :deep(.el-input__wrapper) {
    border-radius: 999px;
    background: #f1f5f9;
    border: 1.5px solid transparent;
    box-shadow: none;
    transition: all 0.2s;

    &:hover {
      border-color: #93c5fd;
      background: #fff;
    }

    &.is-focus {
      background: #fff;
      border-color: #3b82f6;
      box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.15) !important;
    }
  }

  :deep(.el-input-group__append) {
    border-radius: 0 999px 999px 0;
    background: #1e40af;
    border-color: #1e40af;
    color: #fff;
    padding: 0 20px;
    font-weight: 700;
    transition: background 0.2s;
    cursor: pointer;

    &:hover {
      background: #2563eb;
    }
  }

  .search-btn {
    border-radius: 0 999px 999px 0;
    background: #1e40af;
    border-color: #1e40af;
    color: #fff;
    font-weight: 700;
  }
}

/* Nav */
.nav-links {
  display: flex;
  align-items: center;
  gap: 4px;
  flex-shrink: 0;
}

.nav-item {
  padding: 8px 14px;
  border-radius: 10px;
  font-size: 15px;
  font-weight: 600;
  color: #475569;
  cursor: pointer;
  transition:
    color 0.2s,
    background 0.2s;
  white-space: nowrap;

  &:hover {
    color: #1e40af;
    background: rgba(30, 64, 175, 0.08);
  }

  &.active {
    color: #1e40af;
    background: rgba(30, 64, 175, 0.1);
  }
}

/* Actions */
.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-shrink: 0;
  margin-left: auto;
}

.action-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
  background: none;
  border: none;
  cursor: pointer;
  padding: 6px 10px;
  border-radius: 12px;
  color: #475569;
  font-size: 11px;
  font-weight: 600;
  transition:
    color 0.2s,
    background 0.2s,
    transform 0.2s;

  svg {
    color: #475569;
    transition: color 0.2s, transform 0.2s;
  }

  &:hover {
    background: rgba(30, 64, 175, 0.08);
    color: #1e40af;

    svg {
      color: #1e40af;
      transform: scale(1.1);
    }
  }
}

.user-trigger {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px 12px 4px 4px;
  border-radius: 999px;
  cursor: pointer;
  border: 1.5px solid rgba(219, 234, 254, 0.8);
  background: rgba(241, 245, 249, 0.7);
  transition:
    border-color 0.2s,
    background 0.2s,
    box-shadow 0.2s;
  user-select: none;

  &:hover {
    border-color: #93c5fd;
    background: #fff;
    box-shadow: 0 4px 16px rgba(30, 64, 175, 0.1);
  }
}

.user-name {
  font-size: 14px;
  font-weight: 700;
  color: #1e293b;
  max-width: 80px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* ---- MAIN ---- */
.shop-main {
  flex: 1;
  width: min(var(--page-max-width), calc(100vw - 24px));
  margin: 0 auto;
  padding: 28px 0 48px;
}

/* ---- FOOTER ---- */
.shop-footer {
  background: #fff;
  border-top: 1px solid #dbeafe;
  margin-top: auto;
}

.footer-inner {
  width: min(var(--page-max-width), calc(100vw - 24px));
  margin: 0 auto;
  padding: 48px 0 36px;
  display: flex;
  gap: 60px;
  align-items: flex-start;
}

.footer-brand {
  flex-shrink: 0;
  width: 200px;
}

.footer-slogan {
  margin-top: 10px;
  font-size: 14px;
  color: #64748b;
}

.footer-links-group {
  display: flex;
  gap: 48px;
  flex-wrap: wrap;
}

.footer-col {
  h4 {
    font-size: 14px;
    font-weight: 800;
    color: #1e293b;
    margin-bottom: 16px;
    letter-spacing: 0.02em;
  }

  ul {
    list-style: none;
    padding: 0;
    margin: 0;
    display: flex;
    flex-direction: column;
    gap: 10px;
  }

  li a {
    font-size: 13px;
    color: #64748b;
    transition: color 0.2s;
    cursor: pointer;

    &:hover {
      color: #1e40af;
    }
  }
}

.footer-bottom {
  border-top: 1px solid #dbeafe;
  text-align: center;
  padding: 16px;

  p {
    font-size: 13px;
    color: #94a3b8;
  }
}

/* responsive */
@media (max-width: 1024px) {
  .nav-links {
    display: none;
  }

  .search-wrap {
    max-width: none;
  }
}

@media (max-width: 768px) {
  .shop-main {
    padding: 20px 0 40px;
  }

  .footer-inner {
    flex-direction: column;
    gap: 32px;
  }

  .footer-brand {
    width: 100%;
  }
}
</style>

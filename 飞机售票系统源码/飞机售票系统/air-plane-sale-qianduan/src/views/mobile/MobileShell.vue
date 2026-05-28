<template>
  <div class="mobile-shell">
    <header class="mobile-header">
      <div class="brand">飞机售票（移动端 - iQOO12Pro 模拟）</div>
      <div class="search-row" @click="setActive('booking')">
        <input readonly placeholder="出发地 - 目的地 | 选择日期 | 搜索航班" />
      </div>
    </header>

    <main class="mobile-content">
      <!-- 在手机内复用完整的 PC 端 Dashboard，子组件中使用 useRouter() 时会被我们提供的 stub 覆盖，
           这样所有导航操作会被映射到手机内部行为而不影响浏览器地址栏或 PC 端路由状态 -->
      <div class="mobile-dashboard-wrapper">
        <PassengerDashboard />
      </div>
    </main>

    <nav class="mobile-nav">
      <button :class="{active: activeTab === 'home'}" @click="setActive('home')">首页</button>
      <button :class="{active: activeTab === 'booking'}" @click="setActive('booking')">订票</button>
      <button :class="{active: activeTab === 'orders'}" @click="setActive('orders')">订单</button>
      <button :class="{active: activeTab === 'profile'}" @click="setActive('profile')">我的</button>
    </nav>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, provide, shallowReactive } from 'vue'
import { useRouter, routerKey, routeLocationKey } from 'vue-router'
import PassengerDashboard from '../../components/dashboard/PassengerDashboard.vue'

const globalRouter = useRouter()

// 内部激活标签 - 可用于视觉高亮（不再驱动外部路由）
const activeTab = ref<'home'|'booking'|'orders'|'profile'>('home')
const setActive = (tab: 'home'|'booking'|'orders'|'profile') => {
  activeTab.value = tab
}

// 退出预览：恢复全局样式并跳回 PC 端页面
const exitPreview = () => {
  try { document.documentElement.classList.remove('mobile-preview') } catch (e) {}
  globalRouter.push('/portal/passengers')
}

// 为在手机内复用 PC 端组件提供一个“本地路由”替身，拦截常见 push 操作，
// 将它们映射为 mobile shell 内部的行为，避免影响全局路由状态。
// mobileCurrentRoute 模拟 route 对象结构，包含 path / params / query
const mobileCurrentRoute = ref<{ path: string; params?: Record<string, any>; query?: Record<string, any> }>({
  path: '/portal/passengers/mobile-preview',
  params: {},
  query: {}
})
// 为满足 useRoute() 的结构，构造一个 shallowReactive route 对象并提供给子组件
const mobileRoute = shallowReactive<any>({
  path: mobileCurrentRoute.value.path,
  params: mobileCurrentRoute.value.params || {},
  query: mobileCurrentRoute.value.query || {},
  name: undefined,
  fullPath: mobileCurrentRoute.value.path,
  hash: '',
  matched: [],
  meta: {}
})

const mobileRouterStub: any = {
  push: async (to: any) => {
    const path = typeof to === 'string' ? to : (to?.path || to?.fullPath || '')
    const params = (to && to.params) ? to.params : {}
    const query = (to && to.query) ? to.query : {}
    if (!path) return

    // 统一映射规则（常见页面 -> mobile shell 内部 tab 或内部动作）
    try {
      // 乘客相关
      if (path === '/portal/passengers' || path === '/portal/passengers/mobile-preview') {
        setActive('home')
        mobileCurrentRoute.value = { path: '/portal/passengers/mobile-preview', params: {}, query: {} }
        mobileRoute.path = mobileCurrentRoute.value.path
        mobileRoute.params = mobileCurrentRoute.value.params
        mobileRoute.query = mobileCurrentRoute.value.query
        return
      }
      if (path.startsWith('/portal/passengers/view')) {
        // 搜索/订票入口，保留查询参数（例如 ?tab=search）
        setActive('booking')
        mobileCurrentRoute.value = { path: '/portal/passengers/mobile-preview/booking', params: {}, query }
        mobileRoute.path = mobileCurrentRoute.value.path
        mobileRoute.params = mobileCurrentRoute.value.params
        mobileRoute.query = mobileCurrentRoute.value.query
        return
      }
      if (path.startsWith('/portal/passengers/seat-selection')) {
        setActive('booking')
        mobileCurrentRoute.value = { path: '/portal/passengers/mobile-preview/booking/seat-selection', params, query }
        mobileRoute.path = mobileCurrentRoute.value.path
        mobileRoute.params = mobileCurrentRoute.value.params
        mobileRoute.query = mobileCurrentRoute.value.query
        return
      }
      if (path.startsWith('/portal/passengers/points-coupons')) {
        setActive('profile')
        mobileCurrentRoute.value = { path: '/portal/passengers/mobile-preview/profile/points', params, query }
        mobileRoute.path = mobileCurrentRoute.value.path
        mobileRoute.params = mobileCurrentRoute.value.params
        mobileRoute.query = mobileCurrentRoute.value.query
        return
      }
      if (path.startsWith('/portal/passengers/special-passenger')) {
        // 特殊服务：在手机内触发特殊服务弹窗 / 页面
        setActive('profile')
        mobileCurrentRoute.value = { path: '/portal/passengers/mobile-preview/profile/special-passenger', params, query }
        mobileRoute.path = mobileCurrentRoute.value.path
        mobileRoute.params = mobileCurrentRoute.value.params
        mobileRoute.query = mobileCurrentRoute.value.query
        return
      }
      if (path.startsWith('/portal/passengers/print-tickets')) {
        setActive('orders')
        mobileCurrentRoute.value = { path: '/portal/passengers/mobile-preview/orders/print-tickets', params, query }
        mobileRoute.path = mobileCurrentRoute.value.path
        mobileRoute.params = mobileCurrentRoute.value.params
        mobileRoute.query = mobileCurrentRoute.value.query
        return
      }
      if (path.startsWith('/portal/passengers/baggage')) {
        setActive('profile')
        mobileCurrentRoute.value = { path: '/portal/passengers/mobile-preview/profile/baggage', params, query }
        mobileRoute.path = mobileCurrentRoute.value.path
        mobileRoute.params = mobileCurrentRoute.value.params
        mobileRoute.query = mobileCurrentRoute.value.query
        return
      }

      // 订单相关
      if (path === '/portal/orders' || path.startsWith('/portal/orders?') || path.startsWith('/portal/orders')) {
        setActive('orders')
        mobileCurrentRoute.value = { path: '/portal/passengers/mobile-preview/orders', params, query }
        mobileRoute.path = mobileCurrentRoute.value.path
        mobileRoute.params = mobileCurrentRoute.value.params
        mobileRoute.query = mobileCurrentRoute.value.query
        return
      }
      // 订单详情：/portal/orders/:id
      const orderDetailMatch = path.match(/^\/portal\/orders\/(\d+)(\/.*)?$/)
      if (orderDetailMatch) {
        const orderId = orderDetailMatch[1]
        setActive('orders')
        mobileCurrentRoute.value = { path: '/portal/passengers/mobile-preview/orders/detail', params: { orderId }, query }
        mobileRoute.path = mobileCurrentRoute.value.path
        mobileRoute.params = mobileCurrentRoute.value.params
        mobileRoute.query = mobileCurrentRoute.value.query
        return
      }
      // 改签/重订：/portal/orders/rebook/:id 或 /portal/orders/rebook?...
      if (path.startsWith('/portal/orders/rebook') || path.includes('/orders/rebook')) {
        // 支持带 id 的路径或 query 中的 id
        const rebookId = params.id || query.id || (path.match(/rebook\/(\d+)/) || [])[1] || null
        setActive('booking')
        mobileCurrentRoute.value = { path: '/portal/passengers/mobile-preview/booking/rebook', params: { orderId: rebookId }, query }
        mobileRoute.path = mobileCurrentRoute.value.path
        mobileRoute.params = mobileCurrentRoute.value.params
        mobileRoute.query = mobileCurrentRoute.value.query
        return
      }

      // 其他路径：回退到全局路由处理（打开新页面）
      return globalRouter.push(to)
    } catch (e) {
      // 任何异常回退到全局路由
      return globalRouter.push(to)
    }
  },
  replace: async (to: any) => mobileRouterStub.push(to),
  currentRoute: mobileCurrentRoute,
  resolve: (to: any) => globalRouter.resolve(to),
  // 兼容性占位（component 使用时可能访问）
  back: () => globalRouter.back(),
  forward: () => globalRouter.forward()
}

// 提供给子组件（PassengerDashboard）使用，覆盖其 useRouter() 返回值
provide(routerKey, mobileRouterStub)
// 提供 route 对象，覆盖 useRoute()
provide(routeLocationKey, mobileRoute as any)
</script>

<style scoped>
.mobile-shell {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: #f5f7fb;
}
.mobile-header {
  padding: 12px;
  background: linear-gradient(180deg,#0ea5e9,#0284c7);
  color: #fff;
}
.mobile-header .brand {
  font-weight: 700;
  font-size: 16px;
  margin-bottom: 8px;
}
.search-row input {
  width: 100%;
  padding: 10px;
  border-radius: 8px;
  border: none;
  background: rgba(255,255,255,0.15);
  color: #fff;
  cursor: pointer;
}
.mobile-content {
  flex: 1;
  overflow: auto;
  padding: 12px;
}
.mobile-nav {
  display: flex;
  justify-content: space-around;
  border-top: 1px solid #e6eef7;
  background: #fff;
  padding: 8px 0;
}
.mobile-nav button {
  background: transparent;
  border: none;
  padding: 6px 8px;
  font-size: 13px;
  color: #6b7280;
}
.mobile-nav button.active {
  color: #0284c7;
  font-weight: 600;
}
</style>



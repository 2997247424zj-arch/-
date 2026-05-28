<template>
  <AdminLayout>
    <div class="dashboard-page quick-actions-view">
      <div class="breadcrumb">
        <span>数据统计</span>
        <span class="breadcrumb-separator">/</span>
        <span>快捷操作</span>
      </div>

      <header class="page-header">
        <div>
          <p class="page-label">管理后台 · 快捷操作</p>
          <h1>快捷操作</h1>
        </div>
      </header>

      <!-- 待处理事项（glassmorphism + responsive） -->
      <section class="pending-section" v-if="!loadingPending" aria-live="polite">
        <div class="section-title">
          <span class="title-icon">⚠️</span>
          <span>待处理事项</span>
        </div>
        <transition-group name="list-fade" tag="div" class="pending-grid" aria-hidden="false">
          <div
            v-for="item in [
              { key: 'change', title: '改签审核', subtitle: '待审核的订票申请', count: pendingItems.bookingReview, path: '/portal/tickets', variant: 'urgent' },
              { key: 'refund', title: '退订审核', subtitle: '待处理的退订申请', count: pendingItems.refundReview, path: '/portal/refunds', variant: 'warning' },
              { key: 'abnormal', title: '订单异常', subtitle: '需要关注的异常订单', count: pendingItems.abnormalOrders, path: '/portal/orders', variant: 'info' }
            ]"
            :key="item.key"
            class="pending-card"
            :class="item.variant"
            @click="handlePendingClick(item.path)"
            role="button"
            tabindex="0"
            @keydown.enter.prevent="handlePendingClick(item.path)"
            @keydown.space.prevent="handlePendingClick(item.path)"
            :aria-label="`${item.title}，数量 ${item.count}`"
          >
            <div class="pending-header">
              <div class="pending-title">
                <h3>{{ item.title }}</h3>
                <p class="pending-sub">{{ item.subtitle }}</p>
              </div>
              <span class="pending-count" :class="{ active: item.count > 0 }">{{ item.count }}</span>
            </div>
          </div>
        </transition-group>
      </section>

      <div class="quick-actions-section">
        <div class="section-title">
          <span class="title-icon">⚡</span>
          <span>快捷操作</span>
        </div>
        <div class="quick-actions-grid">
          <div 
            class="quick-action-card" 
            v-for="action in quickActions.filter(a => a.id !== 'system-logs')" 
            :key="action.id"
            @click="handleQuickAction(action)"
          >
            <div class="action-icon" :style="{ background: action.iconBg }">
              <span>{{ action.icon }}</span>
            </div>
            <div class="action-content">
              <h3>{{ action.title }}</h3>
              <p>{{ action.description }}</p>
            </div>
            <div class="action-arrow">→</div>
          </div>
        </div>
      </div>

    </div>
  </AdminLayout>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import AdminLayout from '../AdminLayout.vue'
const router = useRouter()

const quickActions = [
  { id: 'add-user', title: '新增用户', description: '快速创建新用户账号', icon: '👤', iconBg: 'linear-gradient(135deg, rgba(30, 138, 230, 0.3), rgba(10, 47, 99, 0.2))', path: '/portal/users', action: 'add' },
  { id: 'manage-flights', title: '航班管理', description: '查看和管理航班信息', icon: '✈️', iconBg: 'linear-gradient(135deg, rgba(236, 72, 153, 0.3), rgba(244, 63, 94, 0.2))', path: '/portal/flights' },
  { id: 'manage-orders', title: '订单管理', description: '处理订单和客户信息', icon: '📋', iconBg: 'linear-gradient(135deg, rgba(245, 185, 66, 0.3), rgba(217, 119, 6, 0.2))', path: '/portal/orders' },
  { id: 'system-logs', title: '操作日志', description: '查看系统操作记录', icon: '📝', iconBg: 'linear-gradient(135deg, rgba(16, 185, 129, 0.3), rgba(5, 150, 105, 0.2))', path: '/portal/logs' },
  { id: 'system-settings', title: '系统设置', description: '配置系统参数和偏好', icon: '⚙️', iconBg: 'linear-gradient(135deg, rgba(99, 102, 241, 0.3), rgba(67, 56, 202, 0.2))', path: '/portal/settings' },
  { id: 'aircraft-management', title: '机型管理', description: '管理飞机机型信息', icon: '🛫', iconBg: 'linear-gradient(135deg, rgba(59, 130, 246, 0.3), rgba(37, 99, 235, 0.2))', path: '/portal/aircraft' },
  { id: 'manage-special-requests', title: '重点旅客预约管理', description: '查看与处理乘客的重点服务预约（管理员入口）', icon: '🤝', iconBg: 'linear-gradient(135deg, rgba(99, 102, 241, 0.3), rgba(79, 70, 229, 0.2))', path: '/portal/admin/special-requests' }
]

const handleQuickAction = (action:any) => {
  if (action.action === 'add') {
    router.push(action.path).then(()=> {
      setTimeout(()=> window.dispatchEvent(new CustomEvent('trigger-add-action')), 100)
    })
  } else {
    router.push(action.path)
  }
}

// 待处理项逻辑
import { reactive, ref, onMounted } from 'vue'
import { statisticsApi, adminTicketChangeReviewApi } from '../../services/api'

const pendingItems = reactive({
  bookingReview: 0,
  refundReview: 0,
  abnormalOrders: 0
})
const loadingPending = ref(false)

const loadPendingItems = async () => {
  loadingPending.value = true
  try {
    // Prefer authoritative counts from specific review APIs when available
    try {
      const res: any = await adminTicketChangeReviewApi.getChangeReviewList({ page: 0, size: 1 })
      const d = res?.data ?? res
      pendingItems.bookingReview = Number(d?.total ?? d?.totalElements ?? 0)
    } catch (e) {
      // Fallback to statistics API if change review API not available
      const resp = await statisticsApi.getPendingItems()
      const data = resp && resp.success && resp.data ? resp.data : resp
      pendingItems.bookingReview = Number(data.flightAdjustments ?? data.bookingReview ?? 0)
    }
    // Refunds and abnormal orders continue to use statistics API
    try {
      const resp2 = await statisticsApi.getPendingItems()
      const data2 = resp2 && resp2.success && resp2.data ? resp2.data : resp2
      pendingItems.refundReview = Number(data2.maintenanceAlerts ?? data2.refundReview ?? 0)
      pendingItems.abnormalOrders = Number(data2.operationalReports ?? data2.abnormalOrders ?? 0)
    } catch (e) {
      // ignore additional errors
    }
  } catch (e) {
    console.warn('加载待处理事项失败', e)
  } finally {
    loadingPending.value = false
  }
}

const handlePendingClick = (path: string) => {
  router.push(path)
}

onMounted(() => {
  loadPendingItems()
  // 监听全局自动刷新事件
  window.addEventListener('auto-refresh', loadPendingItems)
})

</script>

.styles-scope {}

<style scoped>
/* Variables for easy theme tweaks */
:root {
  --glass-bg: rgba(255,255,255,0.04);
  --glass-border: rgba(255,255,255,0.06);
  --accent: rgba(30,138,230,0.9);
  --muted: rgba(148,163,184,0.7);
}

.pending-section {
  margin: 1rem 0 1.5rem;
}
.section-title {
  display:flex;
  align-items:center;
  gap:0.6rem;
  margin-bottom:0.75rem;
  color: var(--color-text-primary);
}
.pending-grid {
  display:grid;
  grid-template-columns: repeat(auto-fit, minmax(320px, 1fr));
  gap: 1.5rem;
}
.pending-card {
  padding: 1.25rem;
  border-radius: 14px;
  background: linear-gradient(180deg, rgba(255,255,255,0.02), rgba(255,255,255,0.01));
  border: 1px solid var(--glass-border);
  backdrop-filter: blur(8px) saturate(120%);
  box-shadow: 0 8px 26px rgba(3,10,26,0.45);
  cursor: pointer;
  transition: transform 220ms cubic-bezier(.2,.9,.2,1), box-shadow 220ms, background 220ms;
  display:flex;
  flex-direction:column;
  justify-content:space-between;
  min-height: 110px;
}
.pending-card:hover, .pending-card:focus {
  transform: translateY(-6px);
  box-shadow: 0 14px 30px rgba(3,10,26,0.55);
  outline: none;
}
.pending-card.urgent { border-left: 4px solid #f59e0b; }
.pending-card.warning { border-left: 4px solid #fb923c; }
.pending-card.info { border-left: 4px solid #60a5fa; }
.pending-header { display:flex; align-items:center; justify-content:space-between; gap:1rem; }
.pending-title h3 { margin:0; font-size:1rem; color:var(--color-text-primary); }
.pending-sub { margin:0; font-size:0.82rem; color:var(--muted); margin-top:6px; }
.pending-count {
  background: linear-gradient(90deg, rgba(255,255,255,0.06), rgba(255,255,255,0.03));
  padding:4px 10px;
  border-radius:999px;
  font-weight:700;
  color: #e6f7ff;
  min-width:44px;
  text-align:center;
  box-shadow: inset 0 -2px 6px rgba(0,0,0,0.12);
  transition: transform 180ms, box-shadow 180ms;
}
.pending-count.active {
  animation: pulse 1400ms infinite;
  transform-origin:center;
}
@keyframes pulse {
  0% { transform: scale(1); box-shadow: 0 0 0 rgba(30,138,230,0.12); }
  50% { transform: scale(1.06); box-shadow: 0 6px 18px rgba(30,138,230,0.06); }
  100% { transform: scale(1); box-shadow: 0 0 0 rgba(30,138,230,0.00); }
}

.quick-actions-grid {
  --quick-card-min: 320px;
  display: grid;
  /* fixed 3 columns to form two rows for 6 cards on wide screens */
  grid-template-columns: repeat(3, minmax(var(--quick-card-min), 1fr));
  grid-auto-rows: 1fr;
  gap: 24px;
  margin-top: 1.5rem;
}
.quick-action-card {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 1.5rem;
  min-height: 140px;
  border-radius: 12px;
  cursor: pointer;
  background: color-mix(in srgb, var(--app-surface, #FFFFFF) 96%, transparent);
  border: 1px solid var(--border-light, rgba(15,23,42,0.06));
  box-shadow: 0 10px 26px rgba(8,18,40,0.06);
  transition: transform 220ms cubic-bezier(.2,.9,.2,1), box-shadow 220ms ease, border-color 220ms ease;
  justify-content: space-between;
}
.quick-action-card:hover, .quick-action-card:focus {
  transform: translateY(-6px);
  box-shadow: 0 22px 48px rgba(8,18,40,0.12);
  border-color: color-mix(in srgb, var(--color-primary) 12%, transparent);
}
.action-icon {
  --quick-icon-size: 72px;
  width: var(--quick-icon-size);
  height: var(--quick-icon-size);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 28px;
  flex-shrink: 0;
  box-shadow: 0 8px 22px rgba(8,18,40,0.06);
}
.action-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding: 0 8px;
  min-width: 0;
}
.action-content h3 {
  margin: 0;
  font-size: 1.05rem;
  font-weight: 800;
  color: var(--text-primary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.action-content p {
  margin: 0;
  font-size: 0.95rem;
  color: var(--text-secondary);
  line-height: 1.4;
  white-space: normal;
  max-height: 3em;
  overflow: hidden;
  text-overflow: ellipsis;
}
.action-arrow {
  margin-left: 8px;
  color: var(--text-secondary);
  font-size: 18px;
  flex-shrink: 0;
  padding-left: 6px;
}

.quick-action-card:focus-visible {
  outline: 3px solid color-mix(in srgb, var(--color-primary) 12%, transparent);
  outline-offset: 3px;
  border-radius: 12px;
}

/* make page container stretch full available width */
.dashboard-page {
  width: 100%;
  max-width: none;
  padding-left: 28px;
  padding-right: 28px;
  box-sizing: border-box;
}

/* transition-group */
.list-fade-enter-from, .list-fade-leave-to { opacity:0; transform: translateY(8px); }
.list-fade-enter-active, .list-fade-leave-active { transition: all 300ms cubic-bezier(.2,.9,.2,1); }

/* Responsive */
@media (max-width: 1000px) {
  .pending-grid { grid-template-columns: repeat(2, minmax(0,1fr)); }
  .quick-actions-grid { grid-template-columns: repeat(2, minmax(220px, 1fr)); gap:16px; }
}
@media (max-width: 640px) {
  .pending-grid { grid-template-columns: 1fr; }
  .section-title { font-size: 0.95rem; }
  .quick-action-card { padding: 0.9rem; min-height: 88px; }
  .action-icon { width:56px; height:56px; font-size:20px; }
  .action-content h3 { font-size: 0.98rem; }
}
</style>



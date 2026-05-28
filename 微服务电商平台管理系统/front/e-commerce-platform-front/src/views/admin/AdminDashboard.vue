<template>
  <div class="admin-dash">
    <!-- Welcome Banner -->
    <div class="welcome-banner">
      <div class="banner-left">
        <div class="welcome-icon">
          <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <rect x="3" y="3" width="7" height="7" rx="2" />
            <rect x="14" y="3" width="7" height="7" rx="2" />
            <rect x="14" y="14" width="7" height="7" rx="2" />
            <rect x="3" y="14" width="7" height="7" rx="2" />
          </svg>
        </div>
        <div>
          <h2>控制台总览</h2>
          <p>欢迎回来！这是今天 {{ todayDate }} 的运营数据快照。</p>
        </div>
      </div>
      <el-button type="primary" round @click="loadStats">
        刷新数据
      </el-button>
    </div>

    <!-- Stats Cards -->
    <div class="stats-grid">
      <div
        v-for="card in statCards"
        :key="card.key"
        class="stat-card surface-card"
        :class="card.theme"
      >
        <div class="card-icon">
          <component :is="card.icon" class="svg-icon" />
        </div>
        <div class="card-content">
          <span class="card-label">{{ card.label }}</span>
          <div class="card-value">{{ card.prefix }}{{ (stats as any)[card.key] }}</div>
          <div class="card-trend">
            <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="#22c55e" stroke-width="2.5">
              <polyline points="23 6 13.5 15.5 8.5 10.5 1 18"/>
              <polyline points="17 6 23 6 23 12"/>
            </svg>
            <span>较上周 +{{ card.growth }}%</span>
          </div>
        </div>
      </div>
    </div>

    <!-- Quick Actions + Recent Activity -->
    <div class="panel-grid">
      <section class="quick-actions surface-card">
        <div class="section-head">
          <h3>快捷操作</h3>
          <p>一键进入各管理模块</p>
        </div>
        <div class="action-grid">
          <button class="action-item" @click="router.push('/admin/users')">
            <div class="action-icon blue">
              <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/>
                <path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/>
              </svg>
            </div>
            <span>用户管理</span>
          </button>
          <button class="action-item" @click="router.push('/admin/products')">
            <div class="action-icon green">
              <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M6 2L3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4z"/>
                <line x1="3" y1="6" x2="21" y2="6"/>
                <path d="M16 10a4 4 0 0 1-8 0"/>
              </svg>
            </div>
            <span>商品管理</span>
          </button>
          <button class="action-item" @click="router.push('/admin/orders')">
            <div class="action-icon amber">
              <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
                <polyline points="14 2 14 8 20 8"/>
                <line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/>
              </svg>
            </div>
            <span>订单管理</span>
          </button>
          <button class="action-item" @click="router.push('/admin/settings')">
            <div class="action-icon purple">
              <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <circle cx="12" cy="12" r="3"/>
                <path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1-2.83 2.83l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-4 0v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83-2.83l.06-.06A1.65 1.65 0 0 0 4.68 15a1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1 0-4h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 2.83-2.83l.06.06A1.65 1.65 0 0 0 9 4.68a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 4 0v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 2.83l-.06.06A1.65 1.65 0 0 0 19.4 9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 0 4h-.09a1.65 1.65 0 0 0-1.51 1z"/>
              </svg>
            </div>
            <span>系统设置</span>
          </button>
        </div>
      </section>

      <section class="recent-activity surface-card">
        <div class="section-head">
          <h3>最近活动</h3>
          <p>实时系统操作日志</p>
        </div>
        <el-timeline class="activity-timeline">
          <el-timeline-item
            v-for="(item, i) in activities"
            :key="i"
            :timestamp="item.time"
            placement="top"
          >
            <div class="activity-item">
              <el-tag :type="item.type" size="small">{{ item.tag }}</el-tag>
              <span>{{ item.desc }}</span>
            </div>
          </el-timeline-item>
        </el-timeline>
      </section>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

const todayDate = computed(() => {
  const now = new Date()
  return `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-${String(now.getDate()).padStart(2, '0')}`
})

const stats = ref({
  userCount: 0,
  productCount: 0,
  orderCount: 0,
  totalRevenue: '0.00',
})

const statCards = [
  {
    key: 'userCount',
    label: '注册用户数',
    prefix: '',
    growth: 12,
    theme: 'blue',
    icon: {
      template: `<svg width="26" height="26" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>`,
    },
  },
  {
    key: 'productCount',
    label: '在售商品数',
    prefix: '',
    growth: 8,
    theme: 'green',
    icon: {
      template: `<svg width="26" height="26" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M6 2L3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4z"/><line x1="3" y1="6" x2="21" y2="6"/><path d="M16 10a4 4 0 0 1-8 0"/></svg>`,
    },
  },
  {
    key: 'orderCount',
    label: '订单总量',
    prefix: '',
    growth: 24,
    theme: 'amber',
    icon: {
      template: `<svg width="26" height="26" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/></svg>`,
    },
  },
  {
    key: 'totalRevenue',
    label: '累计销售额',
    prefix: '¥',
    growth: 18,
    theme: 'pink',
    icon: {
      template: `<svg width="26" height="26" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="12" y1="1" x2="12" y2="23"/><path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"/></svg>`,
    },
  },
]

const activities = ref([
  { time: '今天 10:38', tag: '新用户', type: 'success', desc: '用户 zhangwei 完成注册' },
  { time: '今天 10:20', tag: '新订单', type: 'warning', desc: '订单 #20260424006 提交成功' },
  { time: '今天 10:12', tag: '商品', type: 'primary', desc: '商品「华为 Mate 70 Pro」已上架' },
  { time: '今天 09:50', tag: '支付', type: 'success', desc: '订单 #20260424004 完成支付 ¥3,999' },
  { time: '今天 09:28', tag: '退款', type: 'info', desc: '订单 #20260423015 申请退款审核中' },
])

const loadStats = async () => {
  try {
    const res = await fetch('/api/admin/stats', { credentials: 'include' })
    const result = await res.json()
    if (result.code === 200 && result.data) {
      const d = result.data
      stats.value = {
        userCount: d.userCount || 0,
        productCount: d.productCount || 0,
        orderCount: d.orderCount || 0,
        totalRevenue: Number(d.totalRevenue || 0).toFixed(2),
      }
    }
  } catch {
    // fallback to mock
    stats.value = { userCount: 127, productCount: 86, orderCount: 312, totalRevenue: '198420.00' }
  }
}

const activityMeta = (type: string) => {
  if (type === 'user') return { tag: '新用户', type: 'success' }
  if (type === 'order') return { tag: '订单', type: 'warning' }
  if (type === 'product') return { tag: '商品', type: 'primary' }
  return { tag: '系统', type: 'info' }
}

const loadActivities = async () => {
  try {
    const res = await fetch('/api/admin/activities', { credentials: 'include' })
    const result = await res.json()
    if (result.code === 200 && Array.isArray(result.data)) {
      activities.value = result.data.map((item: any) => ({
        time: item.timestamp,
        desc: item.content,
        ...activityMeta(item.type),
      }))
    }
  } catch {
    // keep fallback activities
  }
}

onMounted(() => {
  loadStats()
  loadActivities()
})
</script>

<style scoped lang="scss">
.admin-dash {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.welcome-banner {
  padding: 24px 28px;
  border-radius: 28px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 18px;
  background: linear-gradient(135deg, rgba(30,64,175,0.1) 0%, rgba(34,197,94,0.08) 100%);
  border: 1px solid rgba(219, 234, 254, 0.9);

  h2 {
    font-size: 28px;
    margin-bottom: 8px;
  }

  p {
    color: #64748b;
  }
}

.banner-left {
  display: flex;
  align-items: center;
  gap: 18px;
}

.welcome-icon {
  width: 60px;
  height: 60px;
  border-radius: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #1e40af, #3b82f6);
  color: #fff;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 20px;
}

.stat-card {
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 18px;
}

.card-icon {
  width: 60px;
  height: 60px;
  border-radius: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.blue .card-icon { background: #dbeafe; color: #1d4ed8; }
.green .card-icon { background: #dcfce7; color: #15803d; }
.amber .card-icon { background: #fef3c7; color: #b45309; }
.pink .card-icon { background: #fce7f3; color: #be185d; }

.card-label {
  font-size: 13px;
  color: #64748b;
  font-weight: 700;
}

.card-value {
  font-family: 'Rubik', sans-serif;
  font-size: 36px;
  font-weight: 800;
  color: #0f172a;
  line-height: 1.1;
  margin: 4px 0 8px;
}

.card-trend {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #16a34a;
  font-weight: 800;
}

.panel-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 22px;
}

.quick-actions,
.recent-activity {
  padding: 24px;
}

.section-head {
  margin-bottom: 22px;

  h3 {
    font-size: 22px;
    margin-bottom: 6px;
  }

  p {
    font-size: 14px;
    color: #64748b;
  }
}

.action-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 14px;
}

.action-item {
  border: 1px solid #dbeafe;
  background: rgba(255, 255, 255, 0.7);
  border-radius: 22px;
  padding: 20px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  font-size: 16px;
  font-weight: 800;
  color: #0f172a;
  transition: all 0.22s ease;

  &:hover {
    transform: translateY(-3px);
    border-color: #93c5fd;
    box-shadow: 0 14px 28px rgba(30, 64, 175, 0.1);
  }
}

.action-icon {
  width: 52px;
  height: 52px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;

  &.blue { background: #dbeafe; color: #1d4ed8; }
  &.green { background: #dcfce7; color: #15803d; }
  &.amber { background: #fef3c7; color: #b45309; }
  &.purple { background: #ede9fe; color: #7c3aed; }
}

.activity-timeline :deep(.el-timeline-item__content) {
  font-size: 14px;
}

.activity-item {
  display: flex;
  align-items: center;
  gap: 10px;

  span {
    color: #475569;
    font-size: 14px;
  }
}

@media (max-width: 1200px) {
  .stats-grid { grid-template-columns: repeat(2, 1fr); }
  .panel-grid { grid-template-columns: 1fr; }
}

@media (max-width: 768px) {
  .stats-grid { grid-template-columns: 1fr; }
  .action-grid { grid-template-columns: repeat(2, 1fr); }
}
</style>

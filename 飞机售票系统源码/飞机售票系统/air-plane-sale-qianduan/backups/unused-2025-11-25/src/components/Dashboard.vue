<template>
  <AdminLayout>
    <!-- 面包屑导航 -->
    <div class="breadcrumb">
      <span>数据统计</span>
    </div>

    <!-- 数据统计卡片 -->
    <div class="stats-grid">
      <div class="stat-card flight-stat">
        <div class="stat-icon">✈️</div>
        <div class="stat-content">
          <div class="stat-label">今日航班</div>
          <div class="stat-value">{{ stats.todayFlights }}</div>
          <div class="stat-change positive">+12%</div>
        </div>
      </div>
      <div class="stat-card order-stat">
        <div class="stat-icon">📋</div>
        <div class="stat-content">
          <div class="stat-label">今日订单</div>
          <div class="stat-value">{{ stats.todayOrders }}</div>
          <div class="stat-change positive">+8%</div>
        </div>
      </div>
      <div class="stat-card revenue-stat">
        <div class="stat-icon">💰</div>
        <div class="stat-content">
          <div class="stat-label">今日营收</div>
          <div class="stat-value">¥{{ formatNumber(stats.todayRevenue) }}</div>
          <div class="stat-change positive">+15%</div>
        </div>
      </div>
      <div class="stat-card user-stat">
        <div class="stat-icon">👥</div>
        <div class="stat-content">
          <div class="stat-label">活跃用户</div>
          <div class="stat-value">{{ stats.activeUsers }}</div>
          <div class="stat-change positive">+5%</div>
        </div>
      </div>
    </div>

    <!-- 待处理事项 -->
    <div class="pending-section">
      <div class="section-title">
        <span class="title-icon">⚠️</span>
        <span>待处理事项</span>
      </div>
      <div class="pending-grid">
        <div class="pending-card urgent" @click="navigateTo('/portal/tickets')">
          <div class="pending-header">
            <h3>改签审核</h3>
            <span class="pending-count">{{ pendingItems.bookingReview }}</span>
          </div>
          <p>待审核的订票申请</p>
        </div>
        <div class="pending-card warning" @click="navigateTo('/portal/refunds')">
          <div class="pending-header">
            <h3>退订审核</h3>
            <span class="pending-count">{{ pendingItems.refundReview }}</span>
          </div>
          <p>待处理的退订申请</p>
        </div>
        <div class="pending-card info" @click="navigateTo('/portal/orders')">
          <div class="pending-header">
            <h3>订单异常</h3>
            <span class="pending-count">{{ pendingItems.abnormalOrders }}</span>
          </div>
          <p>需要关注的异常订单</p>
        </div>
      </div>
    </div>

    <!-- 数据图表区域 -->
    <div class="charts-section">
      <div class="chart-card">
        <div class="chart-header">
          <h3>近7天订单趋势</h3>
          <select v-model="chartPeriod" class="period-select">
            <option value="7">近7天</option>
            <option value="30">近30天</option>
            <option value="90">近90天</option>
          </select>
        </div>
        <div class="chart-placeholder">
          <div class="chart-bars">
            <div 
              v-for="(value, index) in orderTrend" 
              :key="index"
              class="chart-bar"
              :style="{ height: `${(value / maxOrderValue) * 100}%` }"
            >
              <span class="bar-value">{{ value }}</span>
            </div>
          </div>
        </div>
      </div>
      <div class="chart-card">
        <div class="chart-header">
          <h3>热门航线TOP5</h3>
        </div>
        <div class="top-routes">
          <div 
            v-for="(route, index) in topRoutes" 
            :key="index"
            class="route-item"
          >
            <span class="route-rank">{{ index + 1 }}</span>
            <span class="route-name">{{ route.name }}</span>
            <span class="route-count">{{ route.count }}次</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 快捷操作 -->
    <div class="quick-actions-section">
      <div class="section-title">
        <span class="title-icon">⚡</span>
        <span>快捷操作</span>
      </div>
      <div class="actions-grid">
        <button class="action-card" @click="navigateTo('/portal/flights')">
          <span class="action-icon">➕</span>
          <span class="action-text">新增航班</span>
        </button>
        <button class="action-card" @click="navigateTo('/portal/users')">
          <span class="action-icon">👤</span>
          <span class="action-text">新增用户</span>
        </button>
        <button class="action-card" @click="navigateTo('/portal/aircraft')">
          <span class="action-icon">✈️</span>
          <span class="action-text">新增机型</span>
        </button>
        <button class="action-card" @click="navigateTo('/portal/settings')">
          <span class="action-icon">⚙️</span>
          <span class="action-text">系统设置</span>
        </button>
      </div>
    </div>
  </AdminLayout>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import AdminLayout from './AdminLayout.vue'

const router = useRouter()

const stats = reactive({
  todayFlights: 156,
  todayOrders: 1248,
  todayRevenue: 892560,
  activeUsers: 3420
})

const pendingItems = reactive({
  bookingReview: 12,
  refundReview: 8,
  abnormalOrders: 5
})

const chartPeriod = ref(7)
const orderTrend = ref([120, 145, 132, 168, 189, 156, 178])
const maxOrderValue = Math.max(...orderTrend.value)

const topRoutes = ref([
  { name: '北京 ⇀ 上海', count: 245 },
  { name: '上海 ⇀ 深圳', count: 198 },
  { name: '广州 ⇀ 成都', count: 176 },
  { name: '北京 ⇀ 广州', count: 165 },
  { name: '深圳 ⇀ 杭州', count: 142 }
])

const formatNumber = (num: number) => {
  return num.toLocaleString('zh-CN')
}

const navigateTo = (path: string) => {
  router.push(path)
}

// 加载统计数据
const loadStats = async () => {
  // TODO: 调用后端API获取统计数据
  // const result = await statsApi.getDashboardStats()
  // Object.assign(stats, result)
}

onMounted(() => {
  loadStats()
})
</script>

<style scoped>
/* 基础样式内联 */
.breadcrumb {
  margin-bottom: 20px;
  font-size: 14px;
  color: rgba(255, 255, 255, 0.7);
}

.breadcrumb-separator {
  margin: 0 8px;
  color: rgba(255, 255, 255, 0.4);
}

.btn-primary,
.btn-secondary {
  padding: 8px 16px;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s;
}

.btn-primary {
  background: linear-gradient(135deg, #87CEEB, #4AA3DF);
  color: #fff;
}

.btn-primary:hover {
  transform: translateY(-2px);
}

.btn-secondary {
  background: rgba(15, 23, 42, 0.6);
  color: rgba(255, 255, 255, 0.8);
  border: 1px solid rgba(255, 255, 255, 0.15);
}

/* 数据统计卡片 */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.stat-card {
  background: rgba(2, 6, 23, 0.7);
  border-radius: 16px;
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 20px;
  border: 1px solid rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(18px);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);
  transition: transform 0.3s, box-shadow 0.3s;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.4);
}

.stat-icon {
  font-size: 48px;
  width: 80px;
  height: 80px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 16px;
  background: rgba(99, 102, 241, 0.2);
  flex-shrink: 0;
}

.stat-content {
  flex: 1;
}

.stat-label {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.6);
  margin-bottom: 8px;
}

.stat-value {
  font-size: 32px;
  font-weight: bold;
  color: #fff;
  margin-bottom: 4px;
}

.stat-change {
  font-size: 12px;
  font-weight: 500;
}

.stat-change.positive {
  color: #4ade80;
}

.stat-change.negative {
  color: #f87171;
}

/* 待处理事项 */
.pending-section {
  margin-bottom: 30px;
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  color: #fff;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.title-icon {
  font-size: 20px;
}

.pending-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 20px;
}

.pending-card {
  background: rgba(2, 6, 23, 0.7);
  border-radius: 16px;
  padding: 20px;
  border: 1px solid rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(18px);
  cursor: pointer;
  transition: all 0.3s;
  border-left: 4px solid transparent;
}

.pending-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.3);
  border-left-color: #4AA3DF;
}

.pending-card.urgent {
  border-left-color: #f87171;
}

.pending-card.warning {
  border-left-color: #fbbf24;
}

.pending-card.info {
  border-left-color: #87CEEB;
}

.pending-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.pending-header h3 {
  margin: 0;
  font-size: 16px;
  color: #fff;
}

.pending-count {
  background: linear-gradient(135deg, #87CEEB, #4AA3DF);
  color: #fff;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 14px;
  font-weight: 600;
}

.pending-card p {
  margin: 0;
  color: rgba(255, 255, 255, 0.6);
  font-size: 14px;
}

/* 图表区域 */
.charts-section {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(400px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.chart-card {
  background: rgba(2, 6, 23, 0.7);
  border-radius: 16px;
  padding: 24px;
  border: 1px solid rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(18px);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.chart-header h3 {
  margin: 0;
  font-size: 16px;
  color: #fff;
}

.period-select {
  padding: 6px 12px;
  border: 1px solid rgba(255, 255, 255, 0.15);
  border-radius: 8px;
  background: rgba(15, 23, 42, 0.6);
  color: #fff;
  font-size: 14px;
  cursor: pointer;
}

.chart-placeholder {
  height: 200px;
  display: flex;
  align-items: flex-end;
  gap: 12px;
  padding: 20px 0;
}

.chart-bars {
  display: flex;
  align-items: flex-end;
  gap: 12px;
  width: 100%;
  height: 100%;
}

.chart-bar {
  flex: 1;
  background: linear-gradient(to top, #87CEEB, #4AA3DF);
  border-radius: 4px 4px 0 0;
  min-height: 20px;
  position: relative;
  transition: all 0.3s;
}

.chart-bar:hover {
  opacity: 0.8;
}

.bar-value {
  position: absolute;
  top: -20px;
  left: 50%;
  transform: translateX(-50%);
  font-size: 12px;
  color: rgba(255, 255, 255, 0.8);
  white-space: nowrap;
}

.top-routes {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.route-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 12px;
  background: rgba(15, 23, 42, 0.4);
  border-radius: 8px;
  transition: background 0.3s;
}

.route-item:hover {
  background: rgba(15, 23, 42, 0.6);
}

.route-rank {
  width: 28px;
  height: 28px;
  background: linear-gradient(135deg, #87CEEB, #4AA3DF);
  color: #fff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 14px;
  flex-shrink: 0;
}

.route-name {
  flex: 1;
  color: rgba(255, 255, 255, 0.9);
  font-size: 14px;
}

.route-count {
  color: rgba(255, 255, 255, 0.6);
  font-size: 13px;
}

/* 快捷操作 */
.quick-actions-section {
  margin-bottom: 30px;
}

.actions-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 16px;
}

.action-card {
  background: rgba(2, 6, 23, 0.7);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  padding: 20px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  transition: all 0.3s;
  backdrop-filter: blur(18px);
}

.action-card:hover {
  transform: translateY(-4px);
  border-color: rgba(99, 102, 241, 0.5);
  box-shadow: 0 8px 24px rgba(99, 102, 241, 0.2);
}

.action-icon {
  font-size: 32px;
  width: 60px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
  background: rgba(99, 102, 241, 0.2);
}

.action-text {
  color: rgba(255, 255, 255, 0.9);
  font-size: 14px;
  font-weight: 500;
}
</style>

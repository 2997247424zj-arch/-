<template>
  <AdminLayout>
    <div class="dashboard-page statistics-view">
      <div class="breadcrumb">
        <span>数据统计</span>
        <span class="breadcrumb-separator">/</span>
        <span>统计概览</span>
      </div>

      <header class="page-header">
        <div>
          <p class="page-label">管理后台 · 数据统计</p>
          <h1>仪表板统计</h1>
        </div>
        <div class="page-actions">

        </div>
      </header>

      <section class="stats-grid">
        <div class="stat-card stat-card--kpi glass-card enter-fade-up">
          <div class="kpi-top">
            <div class="stat-icon" aria-hidden="true">✈️</div>
            <button class="btn btn--ghost" @click="loadDashboardStats" :disabled="loading" aria-label="刷新统计">
              <span class="btn-ripple"></span>
              刷新
            </button>
          </div>
          <div class="stat-body">
            <div class="stat-label">总航班数</div>
            <div class="stat-value">{{ stats.totalFlights }}</div>
            <div class="stat-meta">较昨日 <span class="meta-change positive">+12%</span></div>
          </div>
        </div>

        <div class="stat-card stat-card--kpi glass-card enter-fade-up">
          <div class="kpi-top">
            <div class="stat-icon">📋</div>
            <button
              class="btn btn--ghost"
              @click="showTodayOrders = !showTodayOrders"
              :aria-pressed="showTodayOrders"
              aria-label="切换 今日/总订单"
            >
              <span class="btn-ripple"></span>
              {{ showTodayOrders ? '今日' : '总计' }}
            </button>
          </div>
          <div class="stat-body">
            <div class="stat-label">总订单数</div>
            <div class="stat-value">{{ orderValue }}</div>
            <div class="stat-meta">{{ showTodayOrders ? '今日订单数' : '实时订单数' }}</div>
          </div>
        </div>

        <div class="stat-card stat-card--kpi glass-card enter-fade-up">
          <div class="kpi-top">
            <div class="stat-icon">💰</div>
            <button
              class="btn btn--ghost"
              @click="showTodayRevenue = !showTodayRevenue"
              :aria-pressed="showTodayRevenue"
              aria-label="切换 今日/总营收"
            >
              <span class="btn-ripple"></span>
              {{ showTodayRevenue ? '今日' : '总计' }}
            </button>
          </div>
          <div class="stat-body">
            <div class="stat-label">总营收</div>
            <div class="stat-value">¥{{ formatNumber(revenueValue) }}</div>
            <div class="stat-meta">{{ showTodayRevenue ? '今日营收' : '总营收' }}</div>
          </div>
        </div>

        <div class="stat-card stat-card--kpi glass-card enter-fade-up">
          <div class="kpi-top">
            <div class="stat-icon">👥</div>
          </div>
          <div class="stat-body">
            <div class="stat-label">总用户数</div>
            <div class="stat-value">{{ stats.activeUsers }}</div>
            <div class="stat-meta">7日活跃</div>
          </div>
        </div>
      </section>

      <!-- Charts -->
      <div class="charts-section">
        <div class="chart-card">
          <div class="chart-header">
            <h3>近{{ chartPeriod }}天订单趋势</h3>
            <select v-model="chartPeriod" @change="loadOrderTrend" class="period-select">
              <option value="7">近7天</option>
              <option value="30">近30天</option>
            
            </select>
          </div>
          <div class="chart-placeholder">
            <div class="chart-bars">
              <div 
                v-for="(value, index) in orderTrend" 
                :key="index"
                class="chart-bar"
                :style="{ height: `${(value / maxOrderValue) * 100}%` }"
                role="img"
                :aria-label="`订单 ${index + 1}：${value}`"
              >
                <span class="bar-value">{{ value }}</span>
              </div>
            </div>
          </div>
        </div>

        <div class="chart-card">
          <div class="chart-header chart-header-with-legend">
            <div>
              <h3>订单类型分布</h3>
              <p class="chart-subtitle">展示近期不同订单类型的占比情况</p>
            </div>
          </div>
          <div class="chart-placeholder chart-placeholder-row">
            <div class="pie-chart" role="img" aria-label="订单类型分布饼图">
              <svg viewBox="0 0 42 42" class="donut" aria-hidden="true">
                <circle class="donut-ring" cx="21" cy="21" r="15.9155" fill="transparent" stroke="#e2e8f0" stroke-width="6"></circle>
                <circle
                  v-for="(segment, idx) in donutSegments"
                  :key="segment.label"
                  class="donut-segment"
                  cx="21"
                  cy="21"
                  r="15.9155"
                  fill="transparent"
                  :stroke="segment.color"
                  stroke-width="6"
                  :stroke-dasharray="segment.dasharray"
                  :stroke-dashoffset="segment.offset"
                ></circle>
              </svg>
              <div class="donut-center">
                <span class="donut-total">{{ totalOrderTypeCount }}</span>
                <span class="donut-label">总订单</span>
              </div>
            </div>
            <ul class="chart-legend">
              <li v-for="type in orderTypeDistribution" :key="type.label">
                <span class="legend-dot" :style="{ background: type.color }"></span>
                <span class="legend-label">{{ type.label }}</span>
                <span class="legend-value">{{ type.count }}</span>
                <span class="legend-percent">({{ Math.round((type.count/ (totalOrderTypeCount || 1))*100) }}%)</span>
              </li>
            </ul>
          </div>
        </div>
      </div>

    </div>
  </AdminLayout>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import AdminLayout from '../AdminLayout.vue'
import { statisticsApi } from '../../services/api'

// stats
const stats = reactive({
  totalFlights: 0,
  totalOrders: 0,
  todayOrders: 0,
  totalRevenue: 0,
  todayRevenue: 0,
  activeUsers: 0
})

// revenue toggle: false => show total, true => show today
const showTodayRevenue = ref(false)

const revenueValue = computed(() => {
  return showTodayRevenue.value ? (stats.todayRevenue || 0) : (stats.totalRevenue || 0)
})

// orders toggle
const showTodayOrders = ref(false)
const orderValue = computed(() => {
  return showTodayOrders.value ? (stats.todayOrders || 0) : (stats.totalOrders || 0)
})

const loading = ref(false)
const formatNumber = (num: number) => num.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ',')

const loadDashboardStats = async () => {
  loading.value = true
  try {
    const resp = await statisticsApi.getDashboardStats()
    const data = resp && resp.success && resp.data ? resp.data : resp
    if (data) {
      stats.activeUsers = data.totalUsers ?? stats.activeUsers
      stats.totalOrders = data.totalOrders ?? stats.totalOrders
      stats.todayOrders = data.todayOrders ?? stats.todayOrders
      stats.totalRevenue = data.totalRevenue ?? stats.totalRevenue
      stats.todayRevenue = data.todayRevenue ?? stats.todayRevenue
      stats.totalFlights = data.totalFlights ?? stats.totalFlights
    }
  } catch (e) {
    console.warn('加载统计失败', e)
  } finally {
    loading.value = false
  }
}

// order trend
const orderTrend = ref<number[]>([])
const chartPeriod = ref('7')
const maxOrderValue = computed(() => orderTrend.value.length ? Math.max(...orderTrend.value) : 1)
const loadOrderTrend = async () => {
  try {
    const resp = await statisticsApi.getOrderTrend({ period: Number(chartPeriod.value) })
    const data = resp && resp.success && resp.data ? resp.data : resp
    orderTrend.value = Array.isArray(data.values) ? data.values.map((v:any)=>Number(v||0)) : []
  } catch (e) {
    console.warn('加载订单趋势失败', e)
    orderTrend.value = [1200,1400,1350,1600,1800,1750,1900]
  }
}

// order type distribution
const orderTypeDistribution = reactive([
  { label: '国内机票', count: 0, color: '#1E8AE6' },
  { label: '国际机票', count: 0, color: '#ec4899' },
  { label: '退票', count: 0, color: '#10b981' },
  { label: '改签', count: 0, color: '#f59e0b' }
])
const totalOrderTypeCount = computed(() => orderTypeDistribution.reduce((s, it) => s + (it.count||0), 0))
const loadOrderTypeDistribution = async () => {
  try {
    const resp = await (statisticsApi as any).getOrderTypeDistribution()
    const data = resp && resp.success && resp.data ? resp.data : (resp || {})
    const values = [data?.domestic ?? 0, data?.international ?? 0, data?.refunds ?? 0, data?.changes ?? 0]
    orderTypeDistribution.forEach((it, i) => {
      it.count = Number(values[i] ?? 0)
    })
  } catch (e) {
    console.warn('加载订单类型分布失败', e)
  }
}

onMounted(async ()=> {
  await Promise.all([loadDashboardStats(), loadOrderTrend(), loadOrderTypeDistribution()])
})

// 饼图分段计算
const donutSegments = computed(() => {
  const total = totalOrderTypeCount.value || 1
  let accumulated = 0
  return orderTypeDistribution.map((item) => {
    const percent = Math.round(((item.count || 0) / total) * 100)
    const dasharray = `${percent} ${100 - percent}`
    const offset = 100 - accumulated
    accumulated += percent
    return {
      label: item.label,
      color: item.color,
      percent,
      dasharray,
      offset
    }
  })
})
</script>

<style scoped>
/* Light Glassmorphism Theme */
.statistics-view { padding-bottom: 3rem; color: #1e293b; }

.breadcrumb {
  margin-bottom: 20px;
  font-size: 14px;
  color: #64748b;
}

.breadcrumb-separator { margin: 0 8px; color: #94a3b8; }

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.page-header h1 {
  margin: 0;
  font-size: 24px;
  font-weight: 700;
  color: #1e293b;
}

.page-label {
  color: #64748b;
  margin: 0;
  font-size: 14px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 20px;
  margin-bottom: 24px;
}

/* Glass Card */
.glass-card, .chart-card {
  background: rgba(255, 255, 255, 0.7);
  border: 1px solid rgba(255, 255, 255, 0.8);
  border-radius: 16px;
  backdrop-filter: blur(20px);
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05), 0 2px 4px -1px rgba(0, 0, 0, 0.03);
  padding: 20px;
  transition: transform 0.2s, box-shadow 0.2s;
  animation: fadeInUp 0.4s ease-out;
}

.glass-card:hover, .chart-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.08);
}

.stat-card--kpi {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  min-height: 140px;
}

.kpi-top {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12px;
}

.stat-icon {
  font-size: 24px;
  background: rgba(255, 255, 255, 0.5);
  width: 44px;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.05);
}

.stat-label {
  color: #64748b;
  font-size: 14px;
  font-weight: 500;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #1e293b;
  margin: 4px 0;
  letter-spacing: -0.5px;
}

.stat-meta {
  font-size: 12px;
  color: #94a3b8;
}

.meta-change.positive {
  color: #10b981;
  font-weight: 600;
  background: rgba(16, 185, 129, 0.1);
  padding: 2px 6px;
  border-radius: 4px;
}

/* Buttons */
.btn {
  padding: 6px 12px;
  border-radius: 8px;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
  border: 1px solid transparent;
  transition: all 0.2s;
  display: inline-flex;
  align-items: center;
  gap: 4px;
  position: relative;
  overflow: hidden;
}

.btn--ghost {
  background: transparent;
  color: #64748b;
  border-color: #e2e8f0;
}

.btn--ghost:hover {
  background: white;
  color: #3b82f6;
  border-color: #cbd5e1;
  box-shadow: 0 2px 4px rgba(0,0,0,0.05);
}

/* Charts Section */
.charts-section {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 24px;
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.chart-header h3 {
  margin: 0;
  color: #1e293b;
  font-size: 16px;
  font-weight: 700;
}

.chart-subtitle {
  margin: 4px 0 0 0;
  font-size: 12px;
  color: #94a3b8;
}

.period-select {
  padding: 4px 8px;
  border-radius: 6px;
  border: 1px solid #cbd5e1;
  background: white;
  color: #475569;
  font-size: 12px;
  outline: none;
  cursor: pointer;
}

/* Bar Chart */
.chart-placeholder {
  width: 100%;
}

.chart-bars {
  display: flex;
  align-items: flex-end;
  gap: 16px;
  height: 240px;
  padding-bottom: 10px;
}

.chart-bar {
  flex: 1;
  background: linear-gradient(180deg, #3b82f6 0%, #60a5fa 100%);
  border-radius: 8px 8px 2px 2px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  display: flex;
  justify-content: center;
  box-shadow: 0 4px 6px rgba(59, 130, 246, 0.2);
}

.chart-bar:hover {
  transform: translateY(-4px) scaleY(1.02);
  box-shadow: 0 8px 12px rgba(59, 130, 246, 0.3);
}

.bar-value {
  position: absolute;
  top: -24px;
  font-size: 12px;
  color: #64748b;
  font-weight: 600;
}

/* Donut Chart & Legend */
.chart-placeholder-row {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  min-height: 300px;
}

.pie-chart {
  position: relative;
  width: 180px;
  height: 180px;
  margin-bottom: 24px;
}

.donut { width: 100%; height: 100%; }
.donut-center {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  text-align: center;
}

.donut-total {
  display: block;
  font-size: 24px;
  font-weight: 700;
  color: #1e293b;
}

.donut-label {
  font-size: 12px;
  color: #64748b;
}

.chart-legend {
  list-style: none;
  padding: 0;
  margin: 0;
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.chart-legend li {
  display: flex;
  align-items: center;
  padding: 8px 12px;
  background: rgba(255,255,255,0.5);
  border-radius: 8px;
  font-size: 13px;
  color: #475569;
  transition: background 0.2s;
}

.chart-legend li:hover {
  background: white;
}

.legend-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  margin-right: 12px;
}

.legend-label { flex: 1; font-weight: 500; }
.legend-value { font-weight: 700; color: #1e293b; margin-right: 6px; }
.legend-percent { color: #94a3b8; font-size: 12px; }

@keyframes fadeInUp {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

@media (max-width: 1024px) {
  .charts-section { grid-template-columns: 1fr; }
  .chart-placeholder-row { flex-direction: row; gap: 32px; justify-content: center; min-height: auto; padding: 20px 0; }
  .chart-legend { width: auto; }
}

@media (max-width: 640px) {
  .chart-placeholder-row { flex-direction: column; }
  .chart-bars { gap: 8px; }
  .chart-legend { width: 100%; }
}
</style>



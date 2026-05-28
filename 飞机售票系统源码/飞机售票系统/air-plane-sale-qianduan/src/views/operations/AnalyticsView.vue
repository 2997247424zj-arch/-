<template>
  <AdminLayout>
    <div class="page-container analytics-page">
      <div class="breadcrumb" v-if="!showOnlyAlerts && !showOnlyTopRoutes">
        <span>航空运营</span>
        <span class="breadcrumb-separator">/</span>
        <span>{{ isNetworkView ? '航班网络分析' : '运行指标监控' }}</span>
      </div>

      <header class="page-header" v-if="!showOnlyAlerts && !showOnlyTopRoutes">
        <div>
          <div class="header-top">
            <button class="back-btn" @click="handleBack">
              <span class="back-icon">←</span>
              <span>返回运营控制台</span>
            </button>
          </div>
          <p class="page-label">
            运营分析 · {{ isNetworkView ? '航班网络分析' : '运行指标监控' }}
          </p>
          <h1>{{ isNetworkView ? '航班网络分析' : '运行指标监控' }}</h1>
          <p v-if="!isNetworkView">
            实时监控放行准点率、延误率、取消率等核心运行指标，跟踪航班状态，及时发现和处理异常情况
          </p>
          <p v-else>
            从航线网络维度分析收益、流量与连接度，评估航线价值，优化航线网络布局和运力投放策略
          </p>
        </div>
        <div class="page-actions">
          <button class="ghost-btn" @click="handleExport" :disabled="loading">
            {{ loading ? '导出中...' : '导出报告' }}
          </button>
          <button class="primary-btn" @click="handleRefresh" :disabled="loading">
            {{ loading ? '刷新中...' : '刷新数据' }}
          </button>
        </div>
      </header>

      <!-- 运行指标监控页面 -->
      <template v-if="!isNetworkView">
        <div v-if="!showOnlyAlerts">
        <!-- 实时运行指标KPI -->
        <section class="kpi-section">
          <div class="section-title">
            <span class="title-icon">⚡</span>
            <span>实时运行指标</span>
          </div>
          <div class="kpi-grid">
            <div class="kpi-card ontime-rate">
              <div class="kpi-icon">✅</div>
              <div class="kpi-content">
                <div class="kpi-label">放行准点率</div>
                <div class="kpi-value">{{ metricsStats.onTimeRate }}%</div>
                <div class="kpi-change positive">
                  <span>↑</span>
                  <span>{{ metricsStats.onTimeChange }}%</span>
                </div>
                <div class="kpi-detail">今日: {{ metricsStats.todayOnTime }}/{{ metricsStats.todayTotal }}</div>
              </div>
            </div>
            <div class="kpi-card delay-rate">
              <div class="kpi-icon">⚠️</div>
              <div class="kpi-content">
                <div class="kpi-label">延误率</div>
                <div class="kpi-value">{{ metricsStats.delayRate }}%</div>
                <div class="kpi-change negative">
                  <span>↓</span>
                  <span>{{ metricsStats.delayChange }}%</span>
                </div>
                <div class="kpi-detail">平均延误: {{ metricsStats.avgDelayTime }}分钟</div>
              </div>
            </div>
            <div class="kpi-card cancel-rate">
              <div class="kpi-icon">❌</div>
              <div class="kpi-content">
                <div class="kpi-label">取消率</div>
                <div class="kpi-value">{{ metricsStats.cancelRate }}%</div>
                <div class="kpi-change neutral">
                  <span>→</span>
                  <span>{{ metricsStats.cancelChange }}%</span>
                </div>
                <div class="kpi-detail">取消: {{ metricsStats.todayCancel }}架次</div>
              </div>
            </div>
            <div class="kpi-card efficiency">
              <div class="kpi-icon">⚙️</div>
              <div class="kpi-content">
                <div class="kpi-label">运行效率</div>
                <div class="kpi-value">{{ metricsStats.efficiency }}%</div>
                <div class="kpi-change positive">
                  <span>↑</span>
                  <span>{{ metricsStats.efficiencyChange }}%</span>
                </div>
                <div class="kpi-detail">航班利用率: {{ metricsStats.utilization }}%</div>
              </div>
            </div>
          </div>
        </section>

        <!-- 实时航班状态监控 -->
        <section class="glass-card realtime-monitor-section">
          <div class="section-head">
            <div>
              <p class="section-label">实时监控</p>
              <h2>当前航班状态</h2>
            </div>
            <div class="monitor-time">
              <span>更新时间: {{ currentTime }}</span>
            </div>
          </div>
          <div class="realtime-status-grid">
            <div class="status-monitor-card">
              <div class="monitor-header">
                <span class="monitor-icon">✈️</span>
                <span class="monitor-title">正在飞行</span>
              </div>
              <div class="monitor-value">{{ realtimeStatus.inFlight }}</div>
              <div class="monitor-trend">较昨日 +{{ realtimeStatus.inFlightChange }}</div>
            </div>
            <div class="status-monitor-card">
              <div class="monitor-header">
                <span class="monitor-icon">🛫</span>
                <span class="monitor-title">即将起飞</span>
              </div>
              <div class="monitor-value">{{ realtimeStatus.departing }}</div>
              <div class="monitor-trend">1小时内: {{ realtimeStatus.departingSoon }}</div>
            </div>
            <div class="status-monitor-card">
              <div class="monitor-header">
                <span class="monitor-icon">🛬</span>
                <span class="monitor-title">即将到达</span>
              </div>
              <div class="monitor-value">{{ realtimeStatus.arriving }}</div>
              <div class="monitor-trend">1小时内: {{ realtimeStatus.arrivingSoon }}</div>
            </div>
            <div class="status-monitor-card">
              <div class="monitor-header">
                <span class="monitor-icon">⏸️</span>
                <span class="monitor-title">延误中</span>
              </div>
              <div class="monitor-value warning">{{ realtimeStatus.delayed }}</div>
              <div class="monitor-trend">平均延误: {{ realtimeStatus.avgDelay }}分钟</div>
            </div>
          </div>
        </section>

        <!-- 准点率趋势 -->
        <section class="charts-section">
          <div class="chart-card">
            <div class="chart-header">
              <h3>准点率趋势</h3>
              <div class="chart-controls">
                <button 
                  v-for="period in timePeriods" 
                  :key="period.value"
                  :class="['period-btn', { active: selectedPeriod === period.value }]"
                  @click="selectedPeriod = period.value as '7d' | '30d' | '90d'"
                >
                  {{ period.label }}
                </button>
              </div>
            </div>
            <div class="chart-placeholder">
              <div class="chart-bars">
                <div 
                  v-for="(item, index) in onTimeRateData" 
                  :key="index"
                  class="chart-bar-item"
                >
                  <div class="bar-wrapper">
                    <div 
                      class="bar ontime-bar" 
                      :style="{ height: `${item.value}%` }"
                    ></div>
                  </div>
                  <div class="bar-label">{{ item.label }}</div>
                  <div class="bar-value">{{ item.value }}%</div>
                </div>
              </div>
            </div>
          </div>

    
        </section>

        </div>
        <!-- 异常告警 -->
        <section v-if="showOnlyAlerts" class="glass-card alerts-section">
          <div class="section-head">
            <div>
              <p class="section-label">运行告警</p>
              <h2>异常情况监控</h2>
            </div>
            <div class="alerts-meta">
              <div class="alerts-summary">
                <span class="alert-badge" :class="{ 'has-alerts': alertsTotal > 0 }">
                  {{ alertsTotal }} 条告警
                </span>
                <div class="alerts-page-info">
                  <span>第 {{ alertsPage + 1 }} / {{ totalAlertPages }} 页</span>
                </div>
              </div>

              <div class="alerts-controls-top">
                <label class="page-size-label">每页</label>
                <select class="page-size-select" v-model.number="alertsPageSize" @change="onAlertPageSizeChange">
                  <option :value="4">4</option>
                  <option :value="8">8</option>
                  <option :value="12">12</option>
                </select>
              </div>
              <!-- 图例：不同颜色的球表示异常类型 -->
              <div class="alerts-legend" title="图例：不同颜色表示异常类型，紧急/重要/一般">
                <div class="legend-item"><span class="legend-dot urgent"></span> 紧急</div>
                <div class="legend-item"><span class="legend-dot warning"></span> 重要</div>
                <div class="legend-item"><span class="legend-dot info"></span> 一般</div>
              </div>
            </div>
          </div>
          <div v-if="alerts.length === 0" class="no-alerts">
            <p>✅ 当前无异常告警，运行正常</p>
          </div>
          <div v-else>
            <div class="alerts-list">
              <div 
                v-for="(alert, index) in visibleAlerts" 
                :key="alert.id || index"
                class="alert-item"
                :class="alert.level"
              >
                <div class="alert-icon">
                  <span v-if="alert.level === 'urgent'">🔴</span>
                  <span v-else-if="alert.level === 'warning'">🟡</span>
                  <span v-else>🔵</span>
                </div>
                <div class="alert-content">
                  <div class="alert-title">
                    {{ alert.title }}
                    <span v-if="alert.statusText" class="status-badge">{{ alert.statusText }}</span>
                  </div>
                  <div class="alert-desc">{{ alert.description }}</div>
                  <div class="alert-time">{{ alert.time }}</div>
                </div>
                <div class="alert-action-wrap">
                  <button v-if="!(alert.raw?.status === 'resolved' || alert.raw?.status === '已解决')" class="alert-action" @click="handleAlertAction(alert)">处理</button>
                  <button v-else class="alert-action resolved" disabled>已解决</button>
                </div>
      </div>

      <div class="alerts-pagination" v-if="alertsTotal > alertsPageSize">
        <button class="ghost-btn" @click="prevAlertPage" :disabled="alertsPage === 0">上一页</button>
        <div class="page-list">
          <button 
            v-for="p in Math.min(totalAlertPages, 7)" 
            :key="p" 
            class="ghost-btn" 
            :class="{ 'active': (p-1) === alertsPage }"
            @click="goToAlertPage(p-1)"
          >{{ p }}</button>
          <span v-if="totalAlertPages > 7" class="ellipsis">…</span>
        </div>
        <button class="ghost-btn" @click="nextAlertPage" :disabled="alertsPage >= totalAlertPages - 1">下一页</button>
      </div>
            </div>
          </div>
        </section>
      </template>

      <!-- 航班网络分析页面 -->
      <template v-else>

        <!-- 航线网络概览 -->
        <section v-if="!showOnlyTopRoutes" class="kpi-section">
          <div class="section-title">
            <span class="title-icon">🛰️</span>
            <span>航线网络概览</span>
          </div>
          <div class="kpi-grid">
            <div class="kpi-card network-routes">
              <div class="kpi-icon">🗺️</div>
              <div class="kpi-content">
                <div class="kpi-label">运营航线数</div>
                <div class="kpi-value">{{ networkStats.totalRoutes }}</div>
                <div class="kpi-change positive">
                  <span>↑</span>
                  <span>+{{ networkStats.newRoutes }}</span>
                </div>
                <div class="kpi-detail">本月新增: {{ networkStats.newRoutes }}条</div>
              </div>
            </div>
            <div class="kpi-card network-revenue">
              <div class="kpi-icon">💰</div>
              <div class="kpi-content">
                <div class="kpi-label">航线总营收</div>
                <div class="kpi-value">¥{{ formatNumber(networkStats.totalRevenue) }}</div>
                <div class="kpi-change positive">
                  <span>↑</span>
                  <span>{{ networkStats.revenueGrowth }}%</span>
                </div>
                <div class="kpi-detail">平均航线: ¥{{ formatNumber(networkStats.avgRouteRevenue) }}</div>
              </div>
            </div>
            <div class="kpi-card network-traffic">
              <div class="kpi-icon">📊</div>
              <div class="kpi-content">
                <div class="kpi-label">航线总流量</div>
                <div class="kpi-value">{{ formatNumber(networkStats.totalTraffic) }}</div>
                <div class="kpi-change positive">
                  <span>↑</span>
                  <span>{{ networkStats.trafficGrowth }}%</span>
                </div>
                <div class="kpi-detail">日均: {{ formatNumber(networkStats.dailyTraffic) }}</div>
              </div>
            </div>
    
          </div>
        </section>

        <!-- 航线收益分析 -->
        <section v-if="!showOnlyTopRoutes" class="charts-section">
          <div class="chart-card">
            <div class="chart-header">
              <h3>航线收益排行</h3>
              <div class="chart-controls">
                <button 
                  v-for="period in timePeriods" 
                  :key="period.value"
                  :class="['period-btn', { active: selectedPeriod === period.value }]"
                  @click="selectedPeriod = period.value as '7d' | '30d' | '90d'"
                >
                  {{ period.label }}
                </button>
              </div>
            </div>
            <div class="chart-placeholder">
              <div class="chart-bars">
                <div 
                  v-for="(item, index) in routeRevenueData" 
                  :key="index"
                  class="chart-bar-item"
                >
                  <div class="bar-wrapper">
                    <div 
                      class="bar revenue-bar" 
                      :style="{ height: `${(item.value / maxRouteRevenue) * 100}%` }"
                    ></div>
                  </div>
                  <div class="bar-label">{{ item.label }}</div>
                  <div class="bar-value">¥{{ formatNumber(item.value) }}</div>
                </div>
              </div>
            </div>
          </div>

          <div class="chart-card">
            <div class="chart-header">
              <h3>航线流量对比</h3>
            </div>
            <div class="chart-placeholder">
              <div class="route-comparison">
                <div 
                  v-for="(route, index) in topRoutes" 
                  :key="route.id"
                  class="comparison-item"
                >
                  <div class="comparison-label">{{ route.name }}</div>
                  <div class="comparison-bar-wrapper">
                    <div 
                      class="comparison-bar" 
                      :style="{ width: `${(route.orders / maxRouteOrders) * 100}%` }"
                    ></div>
                    <span class="comparison-value">{{ route.orders }} 订单</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </section>

        <!-- 热门航线详细分析（仅在 focus=topRoutes 时显示） -->
        <section v-if="showOnlyTopRoutes" class="glass-card routes-section">
          <div class="section-head">
            <div>
              <p class="section-label">航线分析</p>
              <h2>热门航线详细排行</h2>
              <p class="routes-explain" style="margin-top:8px; color: rgba(255,255,255,0.75); font-size:14px; max-width:760px;">
                本视图展示近周期内按指标排序的热门航线，用于评估航线价值与运力投放决策。可切换排序方式（按营收 / 按订单 / 按上座率）并导出当前排行用于离线分析。
              </p>
              <div style="margin-top:12px; display:flex; gap:12px; align-items:center;">
                <div style="display:flex; gap:12px; align-items:center;">
                  <div style="color:rgba(255,255,255,0.75); font-size:13px;">总订单：</div>
                  <div style="font-weight:700; color:#fcd34d;">{{ totalRouteOrders }}</div>
                  <div style="color:rgba(255,255,255,0.75); font-size:13px; margin-left:12px;">总营收：</div>
                  <div style="font-weight:700; color:#fcd34d;">¥{{ formatNumber(totalRouteRevenue) }}</div>
                  <div style="color:rgba(255,255,255,0.75); font-size:13px; margin-left:12px;">平均票价：</div>
                  <div style="font-weight:700; color:#fcd34d;">¥{{ formatNumber(avgRouteFare) }}</div>
                </div>
              </div>
            </div>
            <div class="route-filter">
              <button 
                v-for="sort in sortOptions" 
                :key="sort.value"
                :class="['filter-btn', { active: currentSort === sort.value }]"
                @click="currentSort = sort.value as 'revenue' | 'orders' | 'occupancy'"
              >
                {{ sort.label }}
              </button>
            </div>
            <div style="display:flex; gap:12px; align-items:center;">
              <button class="ghost-btn" @click="exportTopRoutesCsv">导出当前排行</button>
              <button class="ghost-btn" @click="returnToConsole">返回运营控制台</button>
            </div>
          </div>
        <div class="routes-list">
            <div 
              v-for="(route, index) in sortedRoutes" 
              :key="route.id"
              class="route-item"
            >
              <div class="route-rank">{{ index + 1 }}</div>
              <div class="route-info">
                <div class="route-name">{{ route.name }}</div>
                <div class="route-details">
                  <div class="detail-item">
                    <span class="detail-label">订单数:</span>
                    <span class="detail-value">{{ route.orders }}</span>
                  </div>
                  <div class="detail-item">
                    <span class="detail-label">营收:</span>
                    <span class="detail-value revenue">¥{{ formatNumber(route.revenue) }}</span>
                  </div>
                  <div class="detail-item">
                    <span class="detail-label">上座率:</span>
                    <span class="detail-value">{{ route.occupancy }}%</span>
                  </div>
                  <div class="detail-item">
                    <span class="detail-label">平均票价:</span>
                    <span class="detail-value">¥{{ formatNumber(route.avgPrice) }}</span>
                  </div>
                  <div class="detail-item">
                    <span class="detail-label">航班频次:</span>
                    <span class="detail-value">{{ route.frequency }}/天</span>
                  </div>
                </div>
              </div>
              <div class="route-trend" :class="route.trend">
                <span>{{ route.trend === 'up' ? '↑' : route.trend === 'down' ? '↓' : '→' }}</span>
                <span class="trend-text">{{ route.trendText }}</span>
              </div>
            </div>
          </div>
          <div class="routes-pagination" v-if="routesTotal > routesSize">
            <button class="ghost-btn" @click="prevRoutePage" :disabled="routesPage === 0">上一页</button>
            <div class="page-list">
              <button 
                v-for="p in Math.min(Math.ceil(routesTotal / routesSize), 7)" 
                :key="p" 
                class="ghost-btn" 
                :class="{ 'active': (p-1) === routesPage }"
                @click="goToRoutePage(p-1)"
              >{{ p }}</button>
              <span v-if="Math.ceil(routesTotal / routesSize) > 7" class="ellipsis">…</span>
            </div>
            <button class="ghost-btn" @click="nextRoutePage" :disabled="routesPage >= Math.ceil(routesTotal / routesSize) - 1">下一页</button>
            <div style="margin-left:12px; display:flex; gap:8px; align-items:center;">
              <label class="page-size-label">每页</label>
              <select class="page-size-select" v-model.number="routesSize" @change="onRoutesPageSizeChange">
                <option :value="5">5</option>
                <option :value="10">10</option>
                <option :value="20">20</option>
              </select>
            </div>
          </div>
        </section>

       
      </template>

      <!-- 提示模态框 -->
      <ModalPrompt
        v-model="showPrompt"
        :title="promptConfig.title"
        :message="promptConfig.message"
        :type="promptConfig.type"
        :show-cancel="promptConfig.showCancel"
        @confirm="showPrompt = false"
      />
    </div>
  </AdminLayout>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import AdminLayout from '../../components/AdminLayout.vue'
import ModalPrompt from '../../components/ModalPrompt.vue'
import { statisticsApi, operationsApi } from '../../services/api'

const router = useRouter()
const route = useRoute()

// 视图模式：运行指标视角 / 航线网络视角（当前主要用于文案区分）
const viewMode = computed<'metrics' | 'network'>(() => {
  return route.query.mode === 'network' ? 'network' : 'metrics'
})
const isNetworkView = computed(() => viewMode.value === 'network')

// 加载状态
const loading = ref(false)

// 热门航线汇总计算（供说明区显示）
const totalRouteOrders = computed(() => {
  try {
    return topRoutes.reduce((s: number, r: any) => s + (Number(r.orders) || 0), 0)
  } catch (e) { return 0 }
})
const totalRouteRevenue = computed(() => {
  try {
    return topRoutes.reduce((s: number, r: any) => s + (Number(r.revenue) || 0), 0)
  } catch (e) { return 0 }
})
const avgRouteFare = computed(() => {
  try {
    const cnt = topRoutes.length || 1
    const sum = topRoutes.reduce((s: number, r: any) => s + (Number(r.avgPrice) || 0), 0)
    return Math.round((sum / cnt) * 100) / 100
  } catch (e) { return 0 }
})

// 返回运营控制台（用于热门航线和告警快捷入口）
const returnToConsole = () => {
  try {
    router.push('/portal/operations')
  } catch (e) {}
}

// 导出当前热门排行为 CSV
const exportTopRoutesCsv = () => {
  try {
    const rows = [['rank','route','orders','revenue','avgPrice','occupancy','frequency']]
    sortedRoutes.value.forEach((r: any, idx: number) => {
      rows.push([String(idx+1), r.name || '', String(r.orders || 0), String(r.revenue || 0), String(r.avgPrice || 0), String(r.occupancy || 0), String(r.frequency || '')])
    })
    const csv = rows.map(r => r.map(cell => `"${String(cell).replace(/"/g,'""')}"`).join(',')).join('\n')
    const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' })
    const url = URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `热门航线排行_${new Date().toISOString().split('T')[0]}.csv`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    URL.revokeObjectURL(url)
  } catch (e) {
    console.warn('导出失败', e)
    showPromptModal('错误', '导出失败', 'error')
  }
}

// 提示模态框
const showPrompt = ref(false)
const promptConfig = reactive({
  title: '',
  message: '',
  type: 'info' as 'success' | 'error' | 'info' | 'confirm',
  showCancel: false
})

const showPromptModal = (title: string, message: string, type: 'success' | 'error' | 'info' | 'confirm' = 'info', showCancel = false) => {
  promptConfig.title = title
  promptConfig.message = message
  promptConfig.type = type
  promptConfig.showCancel = showCancel
  showPrompt.value = true
}

// 运行指标监控统计数据（初始化为空/占位，由后端填充）
const metricsStats = reactive({
  onTimeRate: 0,
  onTimeChange: 0,
  todayOnTime: 0,
  todayTotal: 0,
  delayRate: 0,
  delayChange: 0,
  avgDelayTime: 0,
  cancelRate: 0,
  cancelChange: 0,
  todayCancel: 0,
  efficiency: 0,
  efficiencyChange: 0,
  utilization: 0
})

// 实时状态（初始化空/占位，由后端填充）
const realtimeStatus = reactive({
  inFlight: 0,
  inFlightChange: 0,
  departing: 0,
  departingSoon: 0,
  arriving: 0,
  arrivingSoon: 0,
  delayed: 0,
  avgDelay: 0
})

// 当前时间
const currentTime = ref('')
let timeInterval: number | null = null

const updateTime = () => {
  const now = new Date()
  currentTime.value = now.toLocaleString('zh-CN', { 
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

// 当路由 query 中 focus=alerts 时，只显示告警区（用于快捷入口）
const showOnlyAlerts = computed(() => {
  try {
    return String(route.query.focus || '') === 'alerts'
  } catch (e) {
    return false
  }
})
// 当路由 query 中 focus=topRoutes 时只显示热门航线（快捷入口）
const showOnlyTopRoutes = computed(() => {
  try {
    return String(route.query.focus || '') === 'topRoutes'
  } catch (e) {
    return false
  }
})

// 告警数据（从后端 flight_exceptions 表加载）
const alerts = reactive<any[]>([])

// 控制告警显示数量（缩减为 4 条更紧凑）
const maxVisibleAlerts = 4
const showAllAlerts = ref(false)
const visibleAlerts = computed(() => {
  try {
    if (showAllAlerts.value) {
      // 保持原始顺序但将重要级别放前
      return alerts.sort((a,b) => priorityOf(b.level) - priorityOf(a.level))
    }
    // 优先展示 urgent -> warning -> info，数量限制为 maxVisibleAlerts
    const urgent = alerts.filter(a => a.level === 'urgent')
    const warning = alerts.filter(a => a.level === 'warning')
    const info = alerts.filter(a => a.level !== 'urgent' && a.level !== 'warning')
    const ordered = [...urgent, ...warning, ...info]
    return ordered.slice(0, maxVisibleAlerts)
  } catch (e) {
    return alerts
  }
})
const toggleAlerts = () => {
  showAllAlerts.value = !showAllAlerts.value
}

const priorityOf = (level: string) => {
  if (level === 'urgent') return 3
  if (level === 'warning') return 2
  return 1
}

const formatAlertFromException = (ex: any) => {
  const delay = Number(ex.delayMinutes ?? ex.delay_minutes ?? 0)
  let level = 'info'
  if (delay > 120) level = 'urgent'
  else if (delay > 30) level = 'warning'
  const et = String(ex.exceptionType ?? ex.exception_type ?? '').toLowerCase()
  if (et.includes('delay') || et.includes('延误')) {
    if (delay > 30) level = 'urgent'
    else if (delay > 0) level = 'warning'
  }

  const flightNo = ex.flightNo ?? ex.flight_no ?? ''
  const title = `${flightNo ? flightNo + ' ' : ''}${ex.exceptionType ?? ex.exception_type ?? ''}`.trim()
  const reason = ex.reason ?? ''
  const reportedAt = ex.reportedAt ?? ex.reported_at
  const time = reportedAt ? (new Date(reportedAt).toLocaleString()) : ''

  return {
    id: ex.id,
    raw: ex,
    level,
    title,
    description: reason + (delay ? `（延误 ${delay} 分钟）` : ''),
    time,
    // map status to display text (中文)
    statusText: (ex.status ?? ex.status)?.toString() === 'resolved' ? '已解决' : (ex.status ?? ex.status) ? String(ex.status) : ''
  }
}

// 异常告警分页
const alertsPage = ref(0)
const alertsPageSize = ref(4)
const alertsTotal = ref(0)

const loadExceptions = async (page = alertsPage.value, size = alertsPageSize.value) => {
  try {
    const resp: any = await operationsApi.getFlightExceptions(page, size)
    const data = resp || {}
    const list = data.exceptions || []
    alerts.splice(0, alerts.length, ...(Array.isArray(list) ? list.map(formatAlertFromException) : []))
    alertsTotal.value = Number(data.total || 0)
    alertsPage.value = Number(data.page || page)
    alertsPageSize.value = Number(data.size || size)
  } catch (error) {
    console.warn('加载航班异常失败:', error)
    alerts.splice(0, alerts.length)
    alertsTotal.value = 0
  }
}

const totalAlertPages = computed(() => {
  return Math.max(1, Math.ceil((alertsTotal.value || 0) / alertsPageSize.value))
})

const goToAlertPage = (p: number) => {
  if (p < 0) p = 0
  if (p >= totalAlertPages.value) p = totalAlertPages.value - 1
  alertsPage.value = p
  loadExceptions(p, alertsPageSize.value)
}

const prevAlertPage = () => {
  if (alertsPage.value > 0) goToAlertPage(alertsPage.value - 1)
}

const nextAlertPage = () => {
  if (alertsPage.value < totalAlertPages.value - 1) goToAlertPage(alertsPage.value + 1)
}

const onAlertPageSizeChange = () => {
  // reset to first page when page size changes
  alertsPage.value = 0
  loadExceptions(0, alertsPageSize.value)
}

// 热门航线分页操作
const prevRoutePage = () => {
  if (routesPage.value > 0) goToRoutePage(routesPage.value - 1)
}

const nextRoutePage = () => {
  if (routesPage.value < Math.max(0, Math.ceil((routesTotal.value || 0) / routesSize.value) - 1)) {
    goToRoutePage(routesPage.value + 1)
  }
}

const goToRoutePage = (p: number) => {
  if (p < 0) p = 0
  const totalPages = Math.max(1, Math.ceil((routesTotal.value || 0) / routesSize.value))
  if (p >= totalPages) p = totalPages - 1
  routesPage.value = p
  loadTopRoutes(p, routesSize.value)
}

const onRoutesPageSizeChange = () => {
  routesPage.value = 0
  loadTopRoutes(0, routesSize.value)
}

// 准点率数据（默认空，由后端或刷新逻辑生成）
const onTimeRateDataByPeriod: Record<string, { label: string; value: number }[]> = {
  '7d': [],
  '30d': [],
  '90d': []
}

const onTimeRateData = computed(() => onTimeRateDataByPeriod[selectedPeriod.value] || [])

// 延误分布（占位为 0）
const delayDistribution = reactive([
  { label: '0-15分钟', count: 0 },
  { label: '15-30分钟', count: 0 },
  { label: '30-60分钟', count: 0 },
  { label: '60分钟以上', count: 0 }
])

const maxDelayCount = computed(() => Math.max(...delayDistribution.map(d => d.count), 1))

// 航班网络分析统计数据（初始化空，由后端填充）
const networkStats = reactive({
  totalRoutes: 0,
  newRoutes: 0,
  totalRevenue: 0,
  revenueGrowth: 0,
  avgRouteRevenue: 0,
  totalTraffic: 0,
  trafficGrowth: 0,
  dailyTraffic: 0,
  networkEfficiency: 0,
  efficiencyChange: 0,
  connectivity: ''
})

// 航线收益数据（默认空，由后端填充）
const routeRevenueDataByPeriod: Record<string, { label: string; value: number }[]> = {
  '7d': [],
  '30d': [],
  '90d': []
}

const routeRevenueData = computed(() => routeRevenueDataByPeriod[selectedPeriod.value] || [])
const maxRouteRevenue = computed(() => Math.max(...(routeRevenueData.value.map(d => d.value) || [1])))

// 热门航线（默认空列表，由后端填充）
const topRoutes = reactive<any[]>([])

// 分页：routes
const routesPage = ref(0)
const routesSize = ref(5)
const routesTotal = ref(0)

const loadTopRoutes = async (page = routesPage.value, size = routesSize.value) => {
  try {
    const res: any = await operationsApi.getTopRoutes(page, size, currentSort.value)
    const data = (res && res.data) ? res.data : res
    const list = data.topRoutes || []
    topRoutes.splice(0, topRoutes.length, ...(Array.isArray(list) ? list.map((r: any) => ({ id: r.route || r.name || String(Math.random()), ...r })) : []))
    routesTotal.value = Number(data.total || 0)
    routesPage.value = Number(data.page || page)
    routesSize.value = Number(data.size || size)
  } catch (err) {
    console.warn('加载热门航线失败', err)
    topRoutes.splice(0, topRoutes.length)
    routesTotal.value = 0
  }
}

const maxRouteOrders = computed(() => Math.max(...topRoutes.map(r => r.orders), 1))

// 排序选项
const sortOptions = [
  { label: '按营收', value: 'revenue' },
  { label: '按订单', value: 'orders' },
  { label: '按上座率', value: 'occupancy' }
]
const currentSort = ref<'revenue' | 'orders' | 'occupancy'>('revenue')

const sortedRoutes = computed(() => {
  const routes = [...topRoutes]
  if (currentSort.value === 'revenue') {
    return routes.sort((a, b) => b.revenue - a.revenue)
  } else if (currentSort.value === 'orders') {
    return routes.sort((a, b) => b.orders - a.orders)
  } else {
    return routes.sort((a, b) => b.occupancy - a.occupancy)
  }
})

// 网络城市节点
const networkCities = reactive([
  { id: '1', name: '北京', type: 'hub', x: 20, y: 30, routes: 12 },
  { id: '2', name: '上海', type: 'hub', x: 50, y: 40, routes: 15 },
  { id: '3', name: '广州', type: 'hub', x: 45, y: 70, routes: 10 },
  { id: '4', name: '深圳', type: 'regular', x: 50, y: 75, routes: 8 },
  { id: '5', name: '成都', type: 'regular', x: 30, y: 55, routes: 6 },
  { id: '6', name: '重庆', type: 'regular', x: 35, y: 50, routes: 5 },
  { id: '7', name: '杭州', type: 'regular', x: 55, y: 45, routes: 4 }
])

// 网络连接线
const networkConnections = reactive([
  { x1: 20, y1: 30, x2: 50, y2: 40, weight: 'high' },
  { x1: 50, y1: 40, x2: 45, y2: 70, weight: 'high' },
  { x1: 45, y1: 70, x2: 50, y2: 75, weight: 'medium' },
  { x1: 20, y1: 30, x2: 50, y2: 75, weight: 'medium' },
  { x1: 50, y1: 40, x2: 30, y2: 55, weight: 'medium' },
  { x1: 20, y1: 30, x2: 30, y2: 55, weight: 'medium' },
  { x1: 30, y1: 55, x2: 35, y2: 50, weight: 'low' },
  { x1: 50, y1: 40, x2: 55, y2: 45, weight: 'low' }
])

// 时间周期
const timePeriods = [
  { label: '7天', value: '7d' },
  { label: '30天', value: '30d' },
  { label: '90天', value: '90d' }
]
const selectedPeriod = ref<'7d' | '30d' | '90d'>('7d')

// 处理告警操作（调用后端标记为已处理）
const handleAlertAction = (alert: any) => {
  const id = alert?.id ?? alert?.raw?.id
  if (!id) {
    showPromptModal('错误', '无法识别告警 ID，无法处理', 'error')
    return
  }
  operationsApi.processException(id, 'resolved', `由运营界面处理`).then((res: any) => {
    const idx = alerts.findIndex(a => a.id === alert.id)
    if (idx > -1) alerts.splice(idx, 1)
    showPromptModal('成功', `已处理告警：${alert.title}`, 'success')
  }).catch((err: any) => {
    console.error('处理告警失败:', err)
    showPromptModal('错误', '处理告警失败，请重试', 'error')
  })
}

// 格式化数字（容错：接受 undefined/null/非数字）
const formatNumber = (num: number | null | undefined) => {
  if (num === null || num === undefined) return '-'
  const n = Number(num)
  if (Number.isNaN(n)) return String(num)
  return n.toLocaleString('zh-CN')
}

// 刷新数据（从后端获取真实运行指标）
const handleRefresh = async () => {
  loading.value = true
  try {
    if (isNetworkView.value) {
      // 如果当前只展示热门航线页面，使用分页专用接口以支持分页查询
      if (showOnlyTopRoutes.value) {
        await loadTopRoutes(routesPage.value, routesSize.value)
      } else {
        const res = await operationsApi.getNetworkOverview()
        console.debug('operationsApi.getNetworkOverview response:', res)
        const payload = (res && res.networkStats) ? res : (res && res.data) ? res.data : res
        if (payload && (payload.networkStats || payload.topRoutes)) {
          const ns: any = payload.networkStats || {}
          const tr: any[] = payload.topRoutes || []
          const rr: any[] = payload.routeRevenue || []
          const nc: any[] = payload.networkCities || []

          // map networkStats
          Object.keys(ns).forEach(k => {
            ;(networkStats as any)[k] = ns[k]
          })

        // map topRoutes and routeRevenue and networkCities
        // topRoutes used in table; routeRevenue used in chart
        topRoutes.splice(0, topRoutes.length, ... (tr.map(r => ({ id: r.route || r.label || String(Math.random()), ...r }))))
        // 支持后端返回多周期 routeRevenue（{ '7d':[], '30d':[], '90d':[] }）或单一数组（视为 7d）
        const applyRouteRevenue = (rrPayload: any) => {
          try {
            const compressIfNeeded = (arr: any[]) => {
              if (!Array.isArray(arr)) return []
              const cleaned = arr.map((itm: any) => ({ label: itm.label || itm.route || '', value: Number(itm.value || itm.revenue || 0) }))
              if (cleaned.length <= 12) return cleaned
              const target = 6
              const bucketSize = Math.ceil(cleaned.length / target)
              const buckets: any[] = []
              for (let i = 0; i < cleaned.length; i += bucketSize) {
                const slice = cleaned.slice(i, i + bucketSize)
                const avg = Math.round((slice.reduce((s, it) => s + it.value, 0) / (slice.length || 1)) * 10) / 10
                const firstLabel = slice[0]?.label ?? ''
                const lastLabel = slice[slice.length-1]?.label ?? ''
                const label = slice.length === 1 ? firstLabel : `${firstLabel}${slice.length>1 && lastLabel ? '-' : ''}${lastLabel}`
                buckets.push({ label, value: avg })
              }
              return buckets
            }

            if (rrPayload && typeof rrPayload === 'object' && !Array.isArray(rrPayload)) {
              if (Array.isArray(rrPayload['7d'])) routeRevenueDataByPeriod['7d'] = compressIfNeeded(rrPayload['7d'])
              if (Array.isArray(rrPayload['30d'])) routeRevenueDataByPeriod['30d'] = compressIfNeeded(rrPayload['30d'])
              if (Array.isArray(rrPayload['90d'])) routeRevenueDataByPeriod['90d'] = compressIfNeeded(rrPayload['90d'])
            } else {
              // 单数组视为 7d，生成 30/90 天的压缩视图作为回退
              const base = Array.isArray(rrPayload) ? rrPayload : []
              routeRevenueDataByPeriod['7d'] = base.map((r: any) => ({ label: r.label || r.route, value: Number(r.value || r.revenue || 0) }))
              routeRevenueDataByPeriod['30d'] = Array.from({length:6}).map((_,i)=>({label:`第${i+1}周`, value: Math.round((((routeRevenueDataByPeriod['7d']||[]).reduce((s,r)=>s+(r.value||0),0) || 0) / 6)*10)/10}))
              routeRevenueDataByPeriod['90d'] = Array.from({length:6}).map((_,i)=>({label:`第${(i+1)*2}旬`, value: Math.round((((routeRevenueDataByPeriod['7d']||[]).reduce((s,r)=>s+(r.value||0),0) || 0) / 6)*10)/10}))
            }
          } catch (e) {
            console.warn('applyRouteRevenue failed', e)
          }
        }
        applyRouteRevenue(rr)
        networkCities.splice(0, networkCities.length, ...nc)

          if (payload.lastUpdated) currentTime.value = payload.lastUpdated
          else updateTime()
        } else {
          console.warn('Unexpected network payload:', payload)
          showPromptModal('警告', '未获取到航班网络的有效数据，保留当前显示', 'info')
        }
      }
    } else {
      // existing metrics branch
      const res = await operationsApi.getMetrics()
      console.debug('operationsApi.getMetrics response:', res)
      const payload = (res && res.metricsStats) ? res : (res && res.data) ? res.data : res

      if (payload && (payload.metricsStats || payload.realtimeStatus)) {
        const ms: any = payload.metricsStats || {}
        const rs: any = payload.realtimeStatus || {}

        metricsStats.onTimeRate = typeof ms.onTimeRate === 'number' ? ms.onTimeRate : Number(ms.onTimeRate) || metricsStats.onTimeRate
        metricsStats.onTimeChange = typeof ms.onTimeChange === 'number' ? ms.onTimeChange : Number(ms.onTimeChange) || metricsStats.onTimeChange
        metricsStats.todayOnTime = typeof ms.todayOnTime === 'number' ? ms.todayOnTime : Number(ms.todayOnTime) || metricsStats.todayOnTime
        metricsStats.todayTotal = typeof ms.todayTotal === 'number' ? ms.todayTotal : Number(ms.todayTotal) || metricsStats.todayTotal
        metricsStats.delayRate = typeof ms.delayRate === 'number' ? ms.delayRate : Number(ms.delayRate) || metricsStats.delayRate
        metricsStats.avgDelayTime = typeof ms.avgDelayTime === 'number' ? ms.avgDelayTime : Number(ms.avgDelayTime) || metricsStats.avgDelayTime
        metricsStats.cancelRate = typeof ms.cancelRate === 'number' ? ms.cancelRate : Number(ms.cancelRate) || metricsStats.cancelRate
        metricsStats.todayCancel = typeof ms.todayCancel === 'number' ? ms.todayCancel : Number(ms.todayCancel) || metricsStats.todayCancel
        metricsStats.efficiency = typeof ms.efficiency === 'number' ? ms.efficiency : Number(ms.efficiency) || metricsStats.efficiency
        metricsStats.utilization = typeof ms.utilization === 'number' ? ms.utilization : Number(ms.utilization) || metricsStats.utilization

        realtimeStatus.inFlight = typeof rs.inFlight === 'number' ? rs.inFlight : Number(rs.inFlight) || realtimeStatus.inFlight
        realtimeStatus.inFlightChange = typeof rs.inFlightChange === 'number' ? rs.inFlightChange : Number(rs.inFlightChange) || realtimeStatus.inFlightChange
        realtimeStatus.departing = typeof rs.departing === 'number' ? rs.departing : Number(rs.departing) || realtimeStatus.departing
        realtimeStatus.departingSoon = typeof rs.departingSoon === 'number' ? rs.departingSoon : Number(rs.departingSoon) || realtimeStatus.departingSoon
        realtimeStatus.arriving = typeof rs.arriving === 'number' ? rs.arriving : Number(rs.arriving) || realtimeStatus.arriving
        realtimeStatus.arrivingSoon = typeof rs.arrivingSoon === 'number' ? rs.arrivingSoon : Number(rs.arrivingSoon) || realtimeStatus.arrivingSoon
        realtimeStatus.delayed = typeof rs.delayed === 'number' ? rs.delayed : Number(rs.delayed) || realtimeStatus.delayed
        realtimeStatus.avgDelay = typeof rs.avgDelay === 'number' ? rs.avgDelay : Number(rs.avgDelay) || realtimeStatus.avgDelay

        if (payload.lastUpdated) currentTime.value = payload.lastUpdated
        else updateTime()

        // Update chart / distribution data derived from metrics
        try {
          const baseRate = Number(metricsStats.onTimeRate) || 0
          onTimeRateDataByPeriod['7d'] = ['周一','周二','周三','周四','周五','周六','周日'].map((label, idx) => {
            const variance = (idx - 3) * 0.2
            return { label, value: Math.max(0, Math.min(100, Math.round((baseRate + variance) * 10) / 10)) }
          })

          const total = Number(metricsStats.todayTotal) || 0
          const delayRateVal = Number(metricsStats.delayRate) || 0
          const delayedCount = Math.round(total * (delayRateVal / 100))
          const d0 = Math.round(delayedCount * 0.5)
          const d1 = Math.round(delayedCount * 0.3)
          const d2 = Math.round(delayedCount * 0.15)
          const d3 = Math.max(0, delayedCount - d0 - d1 - d2)
        if (Array.isArray(delayDistribution) && delayDistribution.length >= 4) {
          const dd: any = delayDistribution as any
          dd[0].count = d0
          dd[1].count = d1
          dd[2].count = d2
          dd[3].count = d3
        }
        } catch (e) {
          console.warn('更新图表衍生数据失败', e)
        }
      } else {
        console.warn('Unexpected metrics payload:', payload)
        showPromptModal('警告', '未获取到运行指标的有效数据，保留当前显示', 'info')
      }
    }
    // 同步加载异常告警
    await loadExceptions()
  } catch (error: any) {
    console.error('handleRefresh error', error)
    showPromptModal('错误', error?.message || '刷新数据失败', 'error')
  } finally {
    loading.value = false
  }
}

// 导出报告
const handleExport = async () => {
  try {
    loading.value = true
    
    // 准备导出数据
    const exportData = isNetworkView.value ? {
      networkStats: networkStats,
      routeRevenue: routeRevenueData.value,
      topRoutes: sortedRoutes.value,
      networkCities: networkCities
    } : {
      metricsStats: metricsStats,
      realtimeStatus: realtimeStatus,
      onTimeRate: onTimeRateData.value,
      delayDistribution: delayDistribution,
      alerts: alerts
    }
    
    // 转换为JSON
    const jsonContent = JSON.stringify(exportData, null, 2)
    const blob = new Blob([jsonContent], { type: 'application/json' })
    const url = URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `运营数据分析报告_${new Date().toISOString().split('T')[0]}.json`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    URL.revokeObjectURL(url)
    
    showPromptModal('成功', '报告导出成功', 'success')
  } catch (error) {
    showPromptModal('错误', '导出失败', 'error')
  } finally {
    loading.value = false
  }
}

// 返回运营控制台
const handleBack = () => {
  router.push('/portal/operations')
}

// 将后端推送的指标 payload 应用到前端状态（复用 handleRefresh 的映射逻辑）
let metricsEventSource: EventSource | null = null

const applyMetricsPayload = (payload: any) => {
  try {
    const data = (payload && payload.metricsStats) ? payload : (payload && payload.data) ? payload.data : payload
    if (!data) return

    if (data.metricsStats || data.realtimeStatus) {
      const ms: any = data.metricsStats || {}
      const rs: any = data.realtimeStatus || {}

      metricsStats.onTimeRate = typeof ms.onTimeRate === 'number' ? ms.onTimeRate : Number(ms.onTimeRate) || metricsStats.onTimeRate
      metricsStats.onTimeChange = typeof ms.onTimeChange === 'number' ? ms.onTimeChange : Number(ms.onTimeChange) || metricsStats.onTimeChange
      metricsStats.todayOnTime = typeof ms.todayOnTime === 'number' ? ms.todayOnTime : Number(ms.todayOnTime) || metricsStats.todayOnTime
      metricsStats.todayTotal = typeof ms.todayTotal === 'number' ? ms.todayTotal : Number(ms.todayTotal) || metricsStats.todayTotal
      metricsStats.delayRate = typeof ms.delayRate === 'number' ? ms.delayRate : Number(ms.delayRate) || metricsStats.delayRate
      metricsStats.avgDelayTime = typeof ms.avgDelayTime === 'number' ? ms.avgDelayTime : Number(ms.avgDelayTime) || metricsStats.avgDelayTime
      metricsStats.cancelRate = typeof ms.cancelRate === 'number' ? ms.cancelRate : Number(ms.cancelRate) || metricsStats.cancelRate
      metricsStats.todayCancel = typeof ms.todayCancel === 'number' ? ms.todayCancel : Number(ms.todayCancel) || metricsStats.todayCancel
      metricsStats.efficiency = typeof ms.efficiency === 'number' ? ms.efficiency : Number(ms.efficiency) || metricsStats.efficiency
      metricsStats.utilization = typeof ms.utilization === 'number' ? ms.utilization : Number(ms.utilization) || metricsStats.utilization

      realtimeStatus.inFlight = typeof rs.inFlight === 'number' ? rs.inFlight : Number(rs.inFlight) || realtimeStatus.inFlight
      realtimeStatus.inFlightChange = typeof rs.inFlightChange === 'number' ? rs.inFlightChange : Number(rs.inFlightChange) || realtimeStatus.inFlightChange
      realtimeStatus.departing = typeof rs.departing === 'number' ? rs.departing : Number(rs.departing) || realtimeStatus.departing
      realtimeStatus.departingSoon = typeof rs.departingSoon === 'number' ? rs.departingSoon : Number(rs.departingSoon) || realtimeStatus.departingSoon
      realtimeStatus.arriving = typeof rs.arriving === 'number' ? rs.arriving : Number(rs.arriving) || realtimeStatus.arriving
      realtimeStatus.arrivingSoon = typeof rs.arrivingSoon === 'number' ? rs.arrivingSoon : Number(rs.arrivingSoon) || realtimeStatus.arrivingSoon
      realtimeStatus.delayed = typeof rs.delayed === 'number' ? rs.delayed : Number(rs.delayed) || realtimeStatus.delayed
      realtimeStatus.avgDelay = typeof rs.avgDelay === 'number' ? rs.avgDelay : Number(rs.avgDelay) || realtimeStatus.avgDelay

      if (data.lastUpdated) currentTime.value = data.lastUpdated
      else updateTime()

      // 更新图表衍生数据（优先使用后端历史数据 onTimeHistory）
      try {
        // 支持后端返回按周期分组的 onTimeHistory：{ '7d': [...], '30d': [...], '90d': [...] }
        if (data.onTimeHistory && typeof data.onTimeHistory === 'object') {
          const map = data.onTimeHistory
          const compressIfNeeded = (arr: any[], periodKey: string) => {
            if (!Array.isArray(arr)) return []
            const cleaned = arr.map((itm: any) => ({ label: itm.label || '', value: Number(itm.value || 0) }))
            // Keep up to 12 bars; for longer series aggregate into 6 buckets for readability
            if (cleaned.length <= 12) return cleaned
            const target = 6
            const bucketSize = Math.ceil(cleaned.length / target)
            const buckets: any[] = []
            for (let i = 0; i < cleaned.length; i += bucketSize) {
              const slice = cleaned.slice(i, i + bucketSize)
              const avg = Math.round((slice.reduce((s, it) => s + it.value, 0) / (slice.length || 1)) * 10) / 10
              // label: use first item's label or a range (safe access)
              const firstLabel = slice[0]?.label ?? ''
              const lastLabel = slice[slice.length-1]?.label ?? ''
              const label = slice.length === 1 ? firstLabel : `${firstLabel}${slice.length>1 && lastLabel ? '-' : ''}${lastLabel}`
              buckets.push({ label, value: avg })
            }
            return buckets
          }
          if (Array.isArray(map['7d'])) {
            onTimeRateDataByPeriod['7d'] = compressIfNeeded(map['7d'], '7d')
          }
          if (Array.isArray(map['30d'])) {
            onTimeRateDataByPeriod['30d'] = compressIfNeeded(map['30d'], '30d')
          }
          if (Array.isArray(map['90d'])) {
            onTimeRateDataByPeriod['90d'] = compressIfNeeded(map['90d'], '90d')
          }
        } else {
          const baseRate = Number(metricsStats.onTimeRate) || 0
          onTimeRateDataByPeriod['7d'] = ['周一','周二','周三','周四','周五','周六','周日'].map((label, idx) => {
            const variance = (idx - 3) * 0.2
            return { label, value: Math.max(0, Math.min(100, Math.round((baseRate + variance) * 10) / 10)) }
          })
          // fallback for longer periods
          onTimeRateDataByPeriod['30d'] = Array.from({length:6}).map((_,i)=>({label:`第${i+1}周`, value: Math.max(0, Math.min(100, Math.round((baseRate + (i-2)*0.5) *10)/10))}))
          onTimeRateDataByPeriod['90d'] = Array.from({length:6}).map((_,i)=>({label:`第${(i+1)*2}旬`, value: Math.max(0, Math.min(100, Math.round((baseRate + (i-2)*0.8) *10)/10))}))
        }

        // routeRevenue 支持后端直接返回多周期数据（在 onTimeHistory 的处理之后执行）
        try {
          const rrPayload = data.routeRevenue || data.route_revenue || null
          const compressIfNeededRoute = (arr: any[]) => {
            if (!Array.isArray(arr)) return []
            const cleaned = arr.map((itm: any) => ({ label: itm.label || itm.route || '', value: Number(itm.value || itm.revenue || 0) }))
            if (cleaned.length <= 12) return cleaned
            const target = 6
            const bucketSize = Math.ceil(cleaned.length / target)
            const buckets: any[] = []
            for (let i = 0; i < cleaned.length; i += bucketSize) {
              const slice = cleaned.slice(i, i + bucketSize)
              const avg = Math.round((slice.reduce((s, it) => s + it.value, 0) / (slice.length || 1)) * 10) / 10
              const firstLabel = slice[0]?.label ?? ''
              const lastLabel = slice[slice.length-1]?.label ?? ''
              const label = slice.length === 1 ? firstLabel : `${firstLabel}${slice.length>1 && lastLabel ? '-' : ''}${lastLabel}`
              buckets.push({ label, value: avg })
            }
            return buckets
          }
          if (rrPayload && typeof rrPayload === 'object' && !Array.isArray(rrPayload)) {
            if (Array.isArray(rrPayload['7d'])) routeRevenueDataByPeriod['7d'] = compressIfNeededRoute(rrPayload['7d'])
            if (Array.isArray(rrPayload['30d'])) routeRevenueDataByPeriod['30d'] = compressIfNeededRoute(rrPayload['30d'])
            if (Array.isArray(rrPayload['90d'])) routeRevenueDataByPeriod['90d'] = compressIfNeededRoute(rrPayload['90d'])
          } else if (Array.isArray(rrPayload)) {
            routeRevenueDataByPeriod['7d'] = rrPayload.map((r: any) => ({ label: r.label || r.route, value: Number(r.value || r.revenue || 0) }))
            routeRevenueDataByPeriod['30d'] = Array.from({length:6}).map((_,i)=>({label:`第${i+1}周`, value: Math.round((((routeRevenueDataByPeriod['7d']||[]).reduce((s,r)=>s+(r.value||0),0) || 0) / 6)*10)/10}))
            routeRevenueDataByPeriod['90d'] = Array.from({length:6}).map((_,i)=>({label:`第${(i+1)*2}旬`, value: Math.round((((routeRevenueDataByPeriod['7d']||[]).reduce((s,r)=>s+(r.value||0),0) || 0) / 6)*10)/10}))
          }
        } catch (e) {
          console.warn('apply routeRevenue failed', e)
        }
        const total = Number(metricsStats.todayTotal) || 0
        const delayRateVal = Number(metricsStats.delayRate) || 0
        const delayedCount = Math.round(total * (delayRateVal / 100))
        const d0 = Math.round(delayedCount * 0.5)
        const d1 = Math.round(delayedCount * 0.3)
        const d2 = Math.round(delayedCount * 0.15)
        const d3 = Math.max(0, delayedCount - d0 - d1 - d2)
        if (Array.isArray(delayDistribution) && delayDistribution.length >= 4) {
          const dd: any = delayDistribution as any
          dd[0].count = d0
          dd[1].count = d1
          dd[2].count = d2
          dd[3].count = d3
        }
      } catch (e) {
        console.warn('更新图表衍生数据失败', e)
      }
    }
  } catch (e) {
    console.warn('applyMetricsPayload error', e)
  }
}

const startMetricsStream = () => {
  try {
    // Prefer explicit env var. In dev (vite) the page runs on 5173; avoid using that port for backend.
    let base = import.meta.env.VITE_API_BASE_URL
    if (!base) {
      // 如果未配置 VITE_API_BASE_URL，优先使用后端默认端口 8080（避免误发到 vite dev server 端口）
      base = `${window.location.protocol}//${window.location.hostname}:8080/api`
    }
    // 去掉末尾斜杠
    const origin = base.endsWith('/') ? base.slice(0, -1) : base
    const streamUrl = `${origin}/operations/metrics/stream`
    // 如果已经存在连接，先关闭
    if (metricsEventSource) {
      try { if (metricsEventSource) metricsEventSource.close() } catch (e) {}
      metricsEventSource = null
    }
    console.debug('Starting metrics SSE stream at', streamUrl)
    metricsEventSource = new EventSource(streamUrl)
    metricsEventSource.onmessage = (ev) => {
      try {
        const payload = ev.data ? JSON.parse(ev.data) : null
        applyMetricsPayload(payload)
      } catch (err) {
        console.warn('解析 metrics SSE 数据失败', err)
      }
    }
    metricsEventSource.onerror = (err) => {
      console.warn('metrics SSE encountered error, will attempt reconnect in 5s', err)
      // 关闭当前连接，稍后由 onMounted 的定时重连逻辑或浏览器自动重连负责
      try { if (metricsEventSource) metricsEventSource.close() } catch (e) {}
      metricsEventSource = null
      // 尝试简单重连
      setTimeout(() => {
        if (!metricsEventSource) startMetricsStream()
      }, 5000)
    }
  } catch (e) {
    console.warn('启动 metrics SSE 失败', e)
  }
}

const stopMetricsStream = () => {
  try {
    if (metricsEventSource) {
      metricsEventSource.close()
      metricsEventSource = null
    }
  } catch (e) {}
}

// 初始化
onMounted(() => {
  updateTime()
  timeInterval = window.setInterval(updateTime, 1000)
  // 首次从后端拉取一次（兼容不支持 SSE 的环境）
  handleRefresh()
  // 再启动 SSE 订阅以保持实时同步（弱订阅，不影响其他操作）
  startMetricsStream()
  // 如果路由 query 指定 focus=alerts，则滚动到告警区并展开
  try {
    const q = (route && route.query && route.query.focus) ? String(route.query.focus) : ''
    if (q === 'alerts') {
      setTimeout(() => {
        const el = document.querySelector('.alerts-section')
        if (el) {
          // 展开所有告警以方便查看
          try { showAllAlerts.value = true } catch (e) {}
          ;(el as HTMLElement).scrollIntoView({ behavior: 'smooth', block: 'start' })
        }
      }, 300)
    }
  } catch (e) {}
})

// 清理
onUnmounted(() => {
  if (timeInterval !== null) {
    clearInterval(timeInterval)
  }
  stopMetricsStream()
})
</script>

<style scoped>
.analytics-page {
  margin-top: 100px;
  padding: 2.5rem clamp(1.5rem, 6vw, 4rem) 3rem;
  color: #f8fafc;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.breadcrumb {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.7);
  margin-bottom: 20px;
}

.breadcrumb-separator {
  margin: 0 8px;
  color: rgba(255, 255, 255, 0.4);
}

.page-header {
  display: flex;
  justify-content: space-between;
  gap: 1.5rem;
  align-items: flex-start;
  flex-wrap: wrap;
}

.header-top {
  margin-bottom: 1rem;
}

.back-btn {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.6rem 1.2rem;
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 12px;
  background: rgba(201, 205, 213, 0.6);
  color: rgba(255, 255, 255, 0.9);
  font-size: 0.9rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
  backdrop-filter: blur(10px);
}

.back-btn:hover {
  background: rgba(99, 102, 241, 0.2);
  border-color: rgba(99, 102, 241, 0.4);
  color: #a5b4fc;
  transform: translateX(-4px);
}

.back-icon {
  font-size: 1.2rem;
  font-weight: 700;
}

.page-label {
  letter-spacing: 0.08em;
  text-transform: uppercase;
  font-size: 0.85rem;
  color: rgba(248, 250, 252, 0.6);
}

.page-header h1 {
  margin: 0.4rem 0;
  font-size: clamp(1.8rem, 3vw, 2.4rem);
  color: #fff;
}

.page-header p {
  color: rgba(248, 250, 252, 0.75);
  max-width: 520px;
}

.page-actions {
  display: flex;
  gap: 0.8rem;
  flex-wrap: wrap;
}

.primary-btn,
.ghost-btn {
  border-radius: 999px;
  padding: 0.65rem 1.6rem;
  border: 1px solid transparent;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
  font-size: 0.9rem;
}

.primary-btn {
  background: linear-gradient(135deg, #1E8AE6, #0A2F63);
  color: #fff;
  box-shadow: 0 10px 25px rgba(99, 102, 241, 0.35);
}

.primary-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 15px 30px rgba(99, 102, 241, 0.45);
}

.ghost-btn {
  border-color: rgba(255, 255, 255, 0.35);
  background: transparent;
  color: #f8fafc;
}

.ghost-btn:hover:not(:disabled) {
  background: rgba(255, 255, 255, 0.1);
}

.primary-btn:disabled,
.ghost-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* KPI指标 */
.kpi-section {
  margin-bottom: 1.5rem;
}

.section-title {
  font-size: 20px;
  color: white;
  font-weight: 600;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  gap: 12px;
}

.title-icon {
  font-size: 24px;
}

.kpi-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 1.2rem;
}

.kpi-card {
  display: flex;
  gap: 1rem;
  align-items: center;
  padding: 1.5rem;
  border-radius: 16px;
  background: rgba(221, 224, 240, 0.7);
  border: 1px solid rgba(255, 255, 255, 0.06);
  backdrop-filter: blur(18px);
  transition: all 0.3s ease;
}

.kpi-card:hover {
  transform: translateY(-2px);
  border-color: rgba(99, 102, 241, 0.3);
  box-shadow: 0 10px 25px rgba(99, 102, 241, 0.2);
}

.kpi-icon {
  font-size: 2.5rem;
  flex-shrink: 0;
}

.kpi-content {
  flex: 1;
}

.kpi-label {
  margin: 0 0 0.5rem 0;
  color: rgba(248, 250, 252, 0.65);
  font-size: 0.85rem;
}

.kpi-value {
  margin: 0 0 0.5rem 0;
  font-size: 1.8rem;
  font-weight: 700;
  color: #fff;
}

.kpi-change {
  font-size: 0.8rem;
  font-weight: 600;
  padding: 0.2rem 0.6rem;
  border-radius: 8px;
  display: inline-flex;
  align-items: center;
  gap: 0.3rem;
}

.kpi-change.positive {
  color: #34d399;
  background: rgba(52, 211, 153, 0.15);
  border: 1px solid rgba(52, 211, 153, 0.3);
}

.kpi-change.negative {
  color: #f87171;
  background: rgba(248, 113, 113, 0.15);
  border: 1px solid rgba(248, 113, 113, 0.3);
}

.kpi-change.neutral {
  color: #9ca3af;
  background: rgba(156, 163, 175, 0.15);
  border: 1px solid rgba(156, 163, 175, 0.3);
}

.kpi-change.negative {
  color: #f87171;
  background: rgba(248, 113, 113, 0.15);
  border: 1px solid rgba(248, 113, 113, 0.3);
}

.kpi-detail {
  font-size: 0.75rem;
  color: rgba(255, 255, 255, 0.5);
  margin-top: 0.25rem;
}

/* 实时监控样式 */
.realtime-monitor-section {
  margin-bottom: 1.5rem;
}

.monitor-time {
  font-size: 0.85rem;
  color: rgba(255, 255, 255, 0.6);
}

.realtime-status-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 1rem;
}

.status-monitor-card {
  padding: 1.5rem;
  background: rgba(132, 161, 224, 0.6);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 16px;
  transition: all 0.3s;
}

.status-monitor-card:hover {
  border-color: rgba(59, 130, 246, 0.4);
  transform: translateY(-2px);
}

.monitor-header {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 1rem;
}

.monitor-icon {
  font-size: 1.5rem;
}

.monitor-title {
  font-size: 0.9rem;
  color: rgba(255, 255, 255, 0.7);
}

.monitor-value {
  font-size: 2rem;
  font-weight: 700;
  color: #fff;
  margin-bottom: 0.5rem;
}

.monitor-value.warning {
  color: #fbbf24;
}

.monitor-trend {
  font-size: 0.85rem;
  color: rgba(255, 255, 255, 0.6);
}

/* 延误分布样式 */
.delay-distribution {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  padding: 1rem 0;
}

.delay-item {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.delay-label {
  min-width: 100px;
  font-size: 0.9rem;
  color: rgba(255, 255, 255, 0.8);
}

.delay-bar-wrapper {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.delay-bar {
  height: 24px;
  background: linear-gradient(90deg, #fbbf24, #f59e0b);
  border-radius: 12px;
  transition: width 0.5s;
}

.delay-count {
  font-size: 0.85rem;
  color: rgba(255, 255, 255, 0.7);
  min-width: 60px;
}

/* 告警样式 */
.alerts-section {
  margin-bottom: 1.5rem;
}

.alert-badge {
  padding: 0.5rem 1rem;
  border-radius: 12px;
  background: rgba(59, 130, 246, 0.2);
  border: 1px solid rgba(59, 130, 246, 0.4);
  color: #60a5fa;
  font-size: 0.9rem;
  font-weight: 600;
}

.alert-badge.has-alerts {
  background: rgba(248, 113, 113, 0.2);
  border-color: rgba(248, 113, 113, 0.4);
  color: #f87171;
}

.no-alerts {
  text-align: center;
  padding: 3rem 1rem;
  color: rgba(255, 255, 255, 0.6);
}

.alerts-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.alerts-controls {
  display: flex;
  justify-content: center;
  margin-top: 0.75rem;
}

.alerts-pagination {
  display: flex;
  gap: 12px;
  align-items: center;
  justify-content: center;
  margin-top: 12px;
}
.alerts-pagination .page-list {
  display: flex;
  gap: 6px;
}
.alerts-pagination .ghost-btn.active {
  background: rgba(99, 102, 241, 0.3);
  border-color: rgba(99, 102, 241, 0.5);
  color: #a5b4fc;
}
.alerts-pagination .ellipsis {
  color: rgba(255,255,255,0.6);
  padding: 0 6px;
}

.alerts-meta {
  display: flex;
  gap: 12px;
  align-items: center;
  justify-content: flex-end;
}
.alerts-legend {
  display: flex;
  gap: 10px;
  align-items: center;
  color: rgba(255,255,255,0.8);
  font-size: 0.9rem;
}
.alerts-legend .legend-item {
  display: flex;
  gap: 6px;
  align-items: center;
  color: rgba(255,255,255,0.8);
}
.legend-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  display: inline-block;
  box-shadow: 0 0 6px rgba(0,0,0,0.12);
}
.legend-dot.urgent { background: #f87171; }
.legend-dot.warning { background: #f59e0b; }
.legend-dot.info { background: #60a5fa; }
.alerts-summary {
  display: flex;
  gap: 12px;
  align-items: center;
}
.alerts-page-info {
  color: rgba(255,255,255,0.7);
  font-size: 0.9rem;
}
.alerts-controls-top {
  display: flex;
  gap: 8px;
  align-items: center;
}
.page-size-label {
  color: rgba(255,255,255,0.75);
  font-size: 0.85rem;
}
.page-size-select {
  background: rgba(211, 216, 227, 0.6);
  color: #fff;
  border: 1px solid rgba(255,255,255,0.08);
  padding: 6px 8px;
  border-radius: 8px;
}

.alert-item {
  display: flex;
  align-items: flex-start;
  gap: 1rem;
  padding: 1.5rem;
  background: rgba(192, 204, 230, 0.6);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  border-left: 4px solid;
  transition: all 0.3s;
}

.alert-item.urgent {
  border-left-color: #f87171;
}

.alert-item.warning {
  border-left-color: #fbbf24;
}

.alert-item.info {
  border-left-color: #60a5fa;
}

.alert-item:hover {
  background: rgba(34, 93, 229, 0.8);
  transform: translateX(4px);
}

.alert-icon {
  font-size: 1.5rem;
  flex-shrink: 0;
}

.alert-content {
  flex: 1;
}

.alert-title {
  font-size: 1rem;
  font-weight: 600;
  color: #fff;
  margin-bottom: 0.5rem;
}

.alert-desc {
  font-size: 0.9rem;
  color: rgba(255, 255, 255, 0.7);
  margin-bottom: 0.5rem;
}

.alert-time {
  font-size: 0.85rem;
  color: rgba(255, 255, 255, 0.5);
}

.alert-action {
  padding: 0.5rem 1rem;
  border: 1px solid rgba(221, 54, 7, 0.4);   /* red border for 待处理 */
  border-radius: 8px;
  background: rgba(218, 14, 14, 0.13);       /* reddish bg for 待处理 */
  color: #e71404;                             /* red for 待处理 */
  font-size: 0.9rem;
  cursor: pointer;
  transition: all 0.2s;
  flex-shrink: 0;
}

.alert-action.resolved {
  border: 1px solid rgba(16, 185, 129, 0.5);
  background: rgba(16, 185, 129, 0.16);
  color: #10b981;           /* green for 已解决 */
}

.alert-action:hover {
  background: rgba(59, 130, 246, 0.25);
  border-color: rgba(59, 130, 246, 0.6);
}

/* 准点率柱状图 */
.ontime-bar {
  background: linear-gradient(180deg, #34d399, #10b981);
}

/* 航线网络分析样式 */
.route-filter {
  display: flex;
  gap: 0.5rem;
}

.filter-btn {
  padding: 0.4rem 0.8rem;
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 8px;
  background: rgba(15, 23, 42, 0.6);
  color: rgba(255, 255, 255, 0.7);
  font-size: 0.85rem;
  cursor: pointer;
  transition: all 0.3s;
}

.filter-btn:hover {
  background: rgba(255, 255, 255, 0.1);
}

.filter-btn.active {
  background: rgba(99, 102, 241, 0.3);
  border-color: rgba(99, 102, 241, 0.5);
  color: #a5b4fc;
}

.route-details {
  display: flex;
  flex-wrap: wrap;
  gap: 1rem;
  margin-top: 0.5rem;
}

.detail-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.detail-label {
  font-size: 0.85rem;
  color: rgba(255, 255, 255, 0.6);
}

.detail-value {
  font-size: 0.9rem;
  color: rgba(255, 255, 255, 0.9);
  font-weight: 500;
}

.detail-value.revenue {
  color: #fcd34d;
  font-weight: 600;
}

.trend-text {
  font-size: 0.75rem;
  margin-left: 0.25rem;
  opacity: 0.8;
}

/* 航线对比图 */
.route-comparison {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  padding: 1rem 0;
}

.comparison-item {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.comparison-label {
  min-width: 120px;
  font-size: 0.9rem;
  color: rgba(255, 255, 255, 0.8);
}

.comparison-bar-wrapper {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.comparison-bar {
  height: 28px;
  background: linear-gradient(90deg, #1E8AE6, #0A2F63);
  border-radius: 14px;
  transition: width 0.5s;
}

.comparison-value {
  font-size: 0.85rem;
  color: rgba(255, 255, 255, 0.7);
  min-width: 80px;
}

/* 收益柱状图 */
.revenue-bar {
  background: linear-gradient(180deg, #fcd34d, #f59e0b);
}

/* 网络地图样式 */
.network-map-section {
  margin-bottom: 1.5rem;
}

.network-map {
  position: relative;
}

.map-legend {
  display: flex;
  gap: 1.5rem;
  margin-bottom: 2rem;
  flex-wrap: wrap;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.85rem;
  color: rgba(255, 255, 255, 0.7);
}

.legend-dot {
  width: 16px;
  height: 16px;
  border-radius: 50%;
}

.legend-dot.hub {
  background: #1E8AE6;
  box-shadow: 0 0 8px rgba(30, 138, 230, 0.5);
}

.legend-dot.regular {
  background: rgba(255, 255, 255, 0.4);
}

.legend-line {
  width: 40px;
  height: 2px;
}

.legend-line.high {
  background: #1E8AE6;
}

.legend-line.medium {
  background: rgba(30, 138, 230, 0.6);
}

.map-container {
  position: relative;
  width: 100%;
  height: 500px;
  background: rgba(8, 14, 35, 0.5);
  border-radius: 16px;
  border: 1px solid rgba(255, 255, 255, 0.1);
  overflow: hidden;
}

.city-node {
  position: absolute;
  padding: 0.75rem 1rem;
  background: rgba(15, 23, 42, 0.8);
  border: 2px solid;
  border-radius: 12px;
  backdrop-filter: blur(10px);
  transform: translate(-50%, -50%);
  transition: all 0.3s;
  cursor: pointer;
  z-index: 2;
}

.city-node.hub {
  border-color: #1E8AE6;
  box-shadow: 0 0 20px rgba(30, 138, 230, 0.4);
}

.city-node.regular {
  border-color: rgba(255, 255, 255, 0.3);
}

.city-node:hover {
  transform: translate(-50%, -50%) scale(1.1);
  z-index: 3;
}

.city-name {
  font-size: 0.9rem;
  font-weight: 600;
  color: #fff;
  margin-bottom: 0.25rem;
}

.city-stats {
  font-size: 0.75rem;
  color: rgba(255, 255, 255, 0.6);
}

.route-lines {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 1;
  pointer-events: none;
}

.route-lines line {
  stroke: rgba(30, 138, 230, 0.3);
  transition: stroke 0.3s;
}

.route-lines line.high {
  stroke: #1E8AE6;
  stroke-width: 1;
}

.route-lines line.medium {
  stroke: rgba(30, 138, 230, 0.6);
  stroke-width: 0.8;
}

.route-lines line.low {
  stroke: rgba(30, 138, 230, 0.3);
  stroke-width: 0.5;
}

/* 图表区域 */
.charts-section {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(500px, 1fr));
  gap: 1.5rem;
  margin-bottom: 1.5rem;
}

.glass-card {
  margin-top: 200px;
  border-radius: 28px;
  padding: 1.8rem;
  background: rgba(227, 229, 237, 0.7);
  border: 1px solid rgba(255, 255, 255, 0.06);
  box-shadow:
    0 25px 50px rgba(182, 186, 207, 0.6),
    inset 0 1px rgba(255, 255, 255, 0.08);
  backdrop-filter: blur(18px);
}

.chart-card {
  background: rgba(175, 178, 192, 0.7);
  border-radius: 28px;
  padding: 1.8rem;
  border: 1px solid rgba(255, 255, 255, 0.06);
  backdrop-filter: blur(18px);
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
  flex-wrap: wrap;
  gap: 1rem;
}

.chart-header h3 {
  margin: 0;
  color: #fff;
  font-size: 1.3rem;
}

.chart-controls {
  display: flex;
  gap: 0.5rem;
}

.period-btn {
  padding: 0.4rem 0.8rem;
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 8px;
  background: rgba(15, 23, 42, 0.6);
  color: rgba(255, 255, 255, 0.7);
  font-size: 0.85rem;
  cursor: pointer;
  transition: all 0.3s;
}

.period-btn:hover {
  background: rgba(255, 255, 255, 0.1);
}

.period-btn.active {
  background: rgba(99, 102, 241, 0.3);
  border-color: rgba(99, 102, 241, 0.5);
  color: #a5b4fc;
}

.chart-placeholder {
  min-height: 300px;
  padding: 1rem 0;
}

.chart-bars {
  display: flex;
  align-items: flex-end;
  justify-content: space-around;
  height: 250px;
  gap: 0.5rem;
}

.chart-bar-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.5rem;
}

.bar-wrapper {
  width: 100%;
  height: 200px;
  display: flex;
  align-items: flex-end;
}

.bar {
  width: 100%;
  background: linear-gradient(180deg, #1E8AE6, #0A2F63);
  border-radius: 8px 8px 0 0;
  transition: all 0.3s;
  min-height: 20px;
  box-shadow: inset 0 -18px 30px rgba(0,0,0,0.25);
  transition: height 0.6s cubic-bezier(.2,.8,.2,1);
}

.bar-label {
  font-size: 0.85rem;
  color: rgba(255, 255, 255, 0.7);
}

.bar-value {
  font-size: 0.75rem;
  color: rgba(255, 255, 255, 0.5);
}

.chart-line {
  position: relative;
  height: 250px;
  margin-bottom: 2rem;
}

.line-point {
  position: absolute;
  width: 12px;
  height: 12px;
  background: #1E8AE6;
  border-radius: 50%;
  transform: translate(-50%, 50%);
  border: 2px solid rgba(15, 23, 42, 0.8);
}

.point-value {
  position: absolute;
  bottom: 20px;
  left: 50%;
  transform: translateX(-50%);
  font-size: 0.75rem;
  color: rgba(255, 255, 255, 0.8);
  white-space: nowrap;
}

.line-labels {
  display: flex;
  justify-content: space-around;
  font-size: 0.85rem;
  color: rgba(255, 255, 255, 0.7);
}

/* 航线分析 */
.routes-section {
  margin-bottom: 1.5rem;
}

.section-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 1rem;
  margin-bottom: 1.5rem;
  flex-wrap: wrap;
}

.section-label {
  font-size: 0.85rem;
  color: rgba(248, 250, 252, 0.6);
  margin: 0;
}

.section-head h2 {
  margin: 0.3rem 0 0;
  color: #fff;
  font-size: 1.3rem;
}

.routes-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.route-item {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1.2rem;
  border-radius: 16px;
  background: rgba(236, 238, 244, 0.6);
  border: 1px solid rgba(255, 255, 255, 0.1);
  transition: all 0.3s;
}

.route-item:hover {
  background: rgba(38, 91, 213, 0.8);
  border-color: rgba(99, 102, 241, 0.3);
  transform: translateX(4px);
}

.route-rank {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: linear-gradient(135deg, #1E8AE6, #0A2F63);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 1.1rem;
  flex-shrink: 0;
}

.route-info {
  flex: 1;
}

.route-name {
  font-size: 1.1rem;
  font-weight: 600;
  color: #fff;
  margin-bottom: 0.5rem;
}

.route-stats {
  display: flex;
  gap: 1.5rem;
  font-size: 0.9rem;
  color: rgba(255, 255, 255, 0.7);
}

.route-trend {
  font-size: 1.5rem;
  font-weight: 700;
}

.route-trend.up {
  color: #34d399;
}

.route-trend.down {
  color: #f87171;
}

.route-trend.stable {
  color: #9ca3af;
}

/* 航班状态 */
.status-section {
  margin-bottom: 1.5rem;
}

.status-distribution {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.status-item {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1rem;
  border-radius: 12px;
  background: rgba(15, 23, 42, 0.6);
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.status-info {
  min-width: 120px;
}

.status-name {
  font-size: 1rem;
  font-weight: 600;
  color: #fff;
  margin-bottom: 0.3rem;
}

.status-count {
  font-size: 0.85rem;
  color: rgba(255, 255, 255, 0.6);
}

.status-bar {
  flex: 1;
  height: 24px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  overflow: hidden;
  position: relative;
}

.status-fill {
  height: 100%;
  border-radius: 12px;
  transition: width 0.5s ease;
}

.status-item.ontime .status-fill {
  background: linear-gradient(90deg, #34d399, #10b981);
}

.status-item.delayed .status-fill {
  background: linear-gradient(90deg, #fbbf24, #f59e0b);
}

.status-item.canceled .status-fill {
  background: linear-gradient(90deg, #f87171, #ef4444);
}

.status-item.boarding .status-fill {
  background: linear-gradient(90deg, #1E8AE6, #1E8AE6);
}

.status-percentage {
  min-width: 60px;
  text-align: right;
  font-size: 0.9rem;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.8);
}

/* 响应式 */
@media (max-width: 1024px) {
  .charts-section {
    grid-template-columns: 1fr;
  }
  
  .kpi-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .kpi-grid {
    grid-template-columns: 1fr;
  }
  
  .route-stats {
    flex-direction: column;
    gap: 0.5rem;
  }
}

/* 热门航线分页样式优化 */
.routes-pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  margin-top: 18px;
  padding-top: 14px;
  border-top: 1px solid rgba(255,255,255,0.04);
}
.routes-pagination .ghost-btn {
  padding: 0.5rem 0.9rem;
  min-width: 84px;
  height: 40px;
  border-radius: 999px;
}
.routes-pagination .page-list {
  display: flex;
  gap: 8px;
  align-items: center;
}
.routes-pagination .page-list .ghost-btn {
  min-width: 40px;
  height: 40px;
  padding: 0;
  border-radius: 999px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}
.routes-pagination .page-list .ghost-btn.active {
  background: rgba(99, 102, 241, 0.28);
  border-color: rgba(99, 102, 241, 0.5);
  color: #a5b4fc;
  transform: translateY(-2px);
  box-shadow: 0 6px 18px rgba(99,102,241,0.08);
}
.routes-pagination .ellipsis {
  color: rgba(255,255,255,0.6);
  padding: 0 6px;
}
.routes-pagination .page-size-select {
  background: rgba(15,23,42,0.6);
  color: #fff;
  border: 1px solid rgba(255,255,255,0.08);
  padding: 6px 8px;
  border-radius: 8px;
}

@media (max-width: 768px) {
  .routes-pagination {
    flex-direction: column;
    gap: 8px;
    padding-top: 10px;
  }
  .routes-pagination .ghost-btn {
    min-width: 120px;
  }
  .routes-pagination .page-list .ghost-btn {
    min-width: 36px;
    height: 36px;
  }
}
</style>


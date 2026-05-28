<template>
  <AdminLayout>
    <!-- 面包屑导航 -->
    <div class="breadcrumb-header">
      <div class="breadcrumb">
        <span>航空运营控制台</span>
      </div>
      <div v-if="airportWeatherBriefings.length" class="weather-briefing-bar">
        <div v-for="w in airportWeatherBriefings" :key="w.city" class="brief-item">
          <span class="brief-city">{{ w.city }}</span>
          <span class="brief-weather">{{ w.icon }} {{ w.info }}</span>
          <span class="brief-temp">{{ w.temp }}°C</span>
        </div>
      </div>
    </div>

    <!-- 运营状态卡片 -->
    <div class="stats-grid">
      <div class="stat-card ontime-stat">
        <div class="stat-icon">✈️</div>
        <div class="stat-content">
          <div class="stat-label">准点航班</div>
          <div class="stat-value">{{ stats.onTimeFlights }}</div>
          <div class="stat-change positive">+95.2%</div>
        </div>
      </div>
      <div class="stat-card delayed-stat">
        <div class="stat-icon">⚠️</div>
        <div class="stat-content">
          <div class="stat-label">延误航班</div>
          <div class="stat-value">{{ stats.delayedFlights }}</div>
          <div class="stat-change negative">+3.1%</div>
        </div>
      </div>
      <div class="stat-card canceled-stat">
        <div class="stat-icon">❌</div>
        <div class="stat-content">
          <div class="stat-label">取消航班</div>
          <div class="stat-value">{{ stats.canceledFlights }}</div>
          <div class="stat-change negative">+0.8%</div>
        </div>
      </div>
    
    </div>

   

    <!-- 运营待办事项 -->
   

    <!-- 提示模态框 -->
    <ModalPrompt
      v-model="showPrompt"
      :title="promptConfig.title"
      :message="promptConfig.message"
      :type="promptConfig.type"
      :show-cancel="promptConfig.showCancel"
      @confirm="showPrompt = false"
    />

    <!-- 待处理事项详情模态框 -->
    <Teleport to="body">
      <div v-if="showPendingDetail" class="pending-detail-modal-overlay" @click.self="closePendingDetail">
        <div class="pending-detail-modal">
          <div class="modal-header">
            <div>
              <h3>{{ currentPendingType }}详情</h3>
              <p>{{ getPendingTypeDescription(currentPendingType) }}</p>
            </div>
            <button class="close-btn" @click="closePendingDetail">×</button>
          </div>
          
          <div class="modal-body">
            <div v-if="currentPendingList.length === 0" class="empty-state">
              <p>暂无待处理项目</p>
            </div>
            <div v-else class="pending-list">
              <div 
                v-for="(item, index) in currentPendingList" 
                :key="index"
                class="pending-item"
                :class="{ 'processing': processingItemIndex === index }"
              >
                <div class="item-header">
                  <div class="item-info">
                    <h4>{{ item.title }}</h4>
                    <p>{{ item.description }}</p>
                    <div class="item-meta">
                      <span class="meta-tag" :class="item.priority">{{ item.priorityText }}</span>
                      <span class="meta-time">{{ item.time }}</span>
                    </div>
                  </div>
                  <div class="item-actions">
                    <button 
                      class="action-btn-small process-btn"
                      @click="handleSingleItemProcess(item, index)"
                      :disabled="processingItemIndex === index || item.status === 'processed'"
                    >
                      {{ item.status === 'processed' ? '已处理' : processingItemIndex === index ? '处理中...' : '处理' }}
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <div class="modal-footer">
            <button class="ghost-btn" @click="closePendingDetail">关闭</button>
            <button 
              class="primary-btn" 
              @click="handleBatchProcess"
              :disabled="currentPendingList.length === 0 || processingItemIndex !== null"
            >
              {{ processingItemIndex !== null ? '处理中...' : `批量处理 (${currentPendingList.filter(i => i.status !== 'processed').length})` }}
            </button>
          </div>
        </div>
      </div>
    </Teleport>

    <!-- 快捷操作入口（聚焦真实航空运行事务） -->
    <div class="quick-actions-section">
      <div class="section-title">
        <span class="title-icon">⚡</span>
        <span>运行事务快捷入口</span>
      </div>
      <div class="quick-actions-grid">
        <div class="quick-action-card order-action" @click="navigateTo('/portal/operations/change-refund')">
          <div class="action-icon">🔄</div>
          <h3>改签/退票处理</h3>
          <p>集中处理延误、取消等不正常航班的退改签与旅客安置</p>
        </div>
          <div class="quick-action-card special-action" @click="navigateTo('/portal/operations/special-requests-operator')">
            <div class="action-icon">🤝</div>
            <h3>重点旅客预约处理</h3>
            <p>运营：处理分配给您的重点旅客预约与现场协调</p>
          </div>
        <div class="quick-action-card weather-action" @click="navigateTo('/portal/passengers/weather')">
          <div class="action-icon">⛅</div>
          <h3>查询天气</h3>
          <p>实时监控主要航点气象，为航路规划与地勤保障提供决策支持</p>
        </div>
        <div class="quick-action-card analysis-action" @click="navigateTo('/portal/operations/analytics?mode=metrics')">
          <div class="action-icon">📊</div>
          <h3>运行指标监控</h3>
          <p>查看放行准点率、客座率等核心运行指标趋势</p>
        </div>
        <div class="quick-action-card analysis-action" @click="navigateTo('/portal/operations/analytics?mode=network')">
          <div class="action-icon">🛰️</div>
          <h3>航班网络分析</h3>
          <p>按航线维度分析流量与收益，辅助航线调整与运力投放</p>
        </div>
        <div class="quick-action-card danger-action" @click="navigateTo('/portal/operations/analytics?mode=metrics&focus=alerts')">
          <div class="action-icon">🚨</div>
          <h3>异常事件监控</h3>
          <p>快速查看并处理当前运行中的异常事件与告警</p>
        </div>
        <div class="quick-action-card primary-action" @click="navigateTo('/portal/operations/analytics?mode=network&focus=topRoutes')">
          <div class="action-icon">📈</div>
          <h3>热门航线排行</h3>
          <p>快速查看热门航线详细排行，按营收/订单/上座率排序</p>
        </div>
        <div class="quick-action-card info-action" @click="navigateTo('/portal/operations/baggage')">
          <div class="action-icon">🧳</div>
          <h3>行李管理</h3>
          <p>管理所有乘客的行李信息，处理行李托运、到达、提取等流程</p>
        </div>
      </div>
    </div>

    <!-- 运营数据图表 -->
    <div class="charts-section">
      <div class="chart-card">
        <div class="chart-header">
          <h3>航班状态分布</h3>
        </div>
        <div class="chart-placeholder">
          <div class="status-distribution">
            <div class="status-item ontime">
              <div class="status-color"></div>
              <div class="status-info">
                <span class="status-name">准点</span>
                <span class="status-value">{{ stats.onTimeFlights }}</span>
                <span class="status-percentage">{{ statusPercentages.onTime }}</span>
              </div>
            </div>
            <div class="status-item delayed">
              <div class="status-color"></div>
              <div class="status-info">
                <span class="status-name">延误</span>
                <span class="status-value">{{ stats.delayedFlights }}</span>
                <span class="status-percentage">{{ statusPercentages.delayed }}</span>
              </div>
            </div>
            <div class="status-item canceled">
              <div class="status-color"></div>
              <div class="status-info">
                <span class="status-name">取消</span>
                <span class="status-value">{{ stats.canceledFlights }}</span>
                <span class="status-percentage">{{ statusPercentages.canceled }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
      <div class="chart-card">
        <div class="chart-header">
          <h3>今日航班计划</h3>
          <div class="beijing-time">
            <span class="time-indicator"></span>
            <div class="time-text">
              <span class="time-label">北京时间</span>
              <span class="time-value">{{ beijingTime }}</span>
            </div>
          </div>
        </div>
        <div class="chart-placeholder">
          <div class="flight-schedule">
            <div v-if="scheduleLoading" class="flight-schedule-loading">正在加载航班计划...</div>
            <div v-else-if="!flightSchedule.length" class="flight-schedule-empty">今日暂无航班计划</div>
            <template v-else>
              <div v-for="(flight, index) in pagedFlightSchedule" :key="flight.number + '-' + index" class="flight-item">
                <div class="flight-info">
                  <span class="flight-number">{{ flight.number }}</span>
                  <span class="flight-route">{{ flight.origin }} → {{ flight.destination }}</span>
                </div>
                <div class="flight-time">
                  <span class="departure">{{ flight.departure }}</span>
                  <span class="arrival">{{ flight.arrival }}</span>
                </div>
                <div class="flight-status" :class="flight.status.toLowerCase()">
                  {{ flight.status }}
                </div>
              </div>
            </template>
            <!-- 航班计划分页 -->
            <div style="display:flex; gap:8px; justify-content:flex-end; align-items:center; margin-top:12px;">
              <button class="ghost-btn" :disabled="flightPage <= 0" @click="changeFlightPage(flightPage - 1)">上一页</button>
              <div style="color: rgba(255,255,255,0.8);">第 {{ flightPage + 1 }} 页</div>
              <button class="primary-btn" :disabled="!flightHasMore" @click="changeFlightPage(flightPage + 1)">下一页</button>
              <select v-model.number="flightSize" @change="onFlightSizeChange" style="margin-left:12px;">
                <option :value="4">4</option>
                <option :value="8">8</option>
                <option :value="12">12</option>
              </select>
            </div>
          </div>
        </div>
      </div>
    </div>
  </AdminLayout>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import AdminLayout from '../AdminLayout.vue'
import ModalPrompt from '../ModalPrompt.vue'
import { operationsApi, weatherApi } from '../../services/api'
import { formatTime } from '../../utils/dateFormat'

const router = useRouter()

// 加载状态
const loading = ref(false)
const pendingLoading = reactive({
  flightAdjustments: false,
  maintenanceAlerts: false,
  alerts: false
})

// 运营模块加载状态
const statusLoading = ref(false)
const scheduleLoading = ref(false)

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

// 运营状态数据
const stats = reactive({
  onTimeFlights: 0,
  delayedFlights: 0,
  canceledFlights: 0,
  activeFlights: 0
})

const statusPercentages = computed(() => {
  const total = stats.onTimeFlights + stats.delayedFlights + stats.canceledFlights
  const format = (value: number) => {
    if (!total) return '0%'
    return `${((value / total) * 100).toFixed(1)}%`
  }
  return {
    onTime: format(stats.onTimeFlights),
    delayed: format(stats.delayedFlights),
    canceled: format(stats.canceledFlights)
  }
})

// 待处理事项（演示数据，可在此调整默认数量）
const pendingItems = reactive({
  flightAdjustments: 2,
  maintenanceAlerts: 2
})

// 用于刷新时恢复到初始演示数据
const initialPendingSnapshot = { ...pendingItems }

// 待处理事项详情数据结构
interface PendingItem {
  id: string
  title: string
  description: string
  priority: 'urgent' | 'warning' | 'info'
  priorityText: string
  time: string
  status: 'pending' | 'processing' | 'processed'
  details?: any
}

// 待处理事项详细列表
const pendingDetails = reactive<{
  flightAdjustments: PendingItem[]
  maintenanceAlerts: PendingItem[]
}>({
  flightAdjustments: [
    {
      id: 'FA001',
      title: 'CA1234 航班时刻调整',
      description: '因天气原因，需要将CA1234航班从08:00调整至09:30',
      priority: 'urgent',
      priorityText: '紧急',
      time: '2小时前',
      status: 'pending',
      details: { flightNumber: 'CA1234', reason: '天气原因', originalTime: '08:00', newTime: '09:30' }
    },
    {
      id: 'FA002',
      title: 'MU5678 航线变更申请',
      description: 'MU5678航班申请临时变更航线，需审批',
      priority: 'warning',
      priorityText: '重要',
      time: '5小时前',
      status: 'pending',
      details: { flightNumber: 'MU5678', reason: '航线优化', route: '上海→广州' }
    }
  ],
  maintenanceAlerts: [
    {
      id: 'MA001',
      title: 'B-1234 定期维护到期',
      description: 'B-1234飞机定期维护将于3天后到期，请安排维护计划',
      priority: 'warning',
      priorityText: '重要',
      time: '1天前',
      status: 'pending',
      details: { aircraftId: 'B-1234', maintenanceType: '定期维护', dueDate: '3天后' }
    },
    {
      id: 'MA002',
      title: 'B-5678 安全检查提醒',
      description: 'B-5678飞机安全检查将于7天后到期',
      priority: 'info',
      priorityText: '一般',
      time: '2天前',
      status: 'pending',
      details: { aircraftId: 'B-5678', maintenanceType: '安全检查', dueDate: '7天后' }
    }
  ]
})

// 运营侧从后端拉取的异常事件（分页）
import { adminAlertsApi } from '../../services/api'
const operatorPendingAlerts = ref<any[]>([])
const operatorProcessedAlerts = ref<any[]>([])
const alertsPage = ref<number>(0)
const alertsSize = ref<number>(5)
const alertsHasMore = ref<boolean>(false)
const alertsLoading = ref<boolean>(false)

const parseAlertsResponse = (res: any) => {
  const raw = (res && res.data) ? res.data : (Array.isArray(res) ? res : (res.list || res))
  const pending: any[] = []
  const processed: any[] = []
  if (Array.isArray(raw)) {
    for (const a of raw) {
      const st = (a && a.status) ? String(a.status).toLowerCase() : ''
      if (st === 'resolved') processed.push(a)
      else pending.push(a)
    }
  }
  return { pending, processed, raw }
}

const fetchOperatorAlerts = async (page = alertsPage.value, size = alertsSize.value) => {
  pendingLoading.alerts = true
  alertsLoading.value = true
  try {
    // 使用管理员的分页告警接口以获得更稳定的分页支持
    const res: any = await adminAlertsApi.getAlerts(page, size)
    const { pending, processed, raw } = parseAlertsResponse(res)
    operatorPendingAlerts.value = pending
    operatorProcessedAlerts.value = processed
    // 如果返回条数等于 page size，可能还有下一页
    alertsHasMore.value = Array.isArray(raw) && raw.length === size
  } catch (error) {
    console.warn('获取运营告警失败', error)
    operatorPendingAlerts.value = []
    operatorProcessedAlerts.value = []
    alertsHasMore.value = false
  } finally {
    pendingLoading.alerts = false
    alertsLoading.value = false
  }
}

const changeAlertsPage = (newPage: number) => {
  alertsPage.value = Math.max(0, newPage)
  fetchOperatorAlerts(alertsPage.value, alertsSize.value)
}

const onAlertsSizeChange = () => {
  alertsPage.value = 0
  fetchOperatorAlerts(alertsPage.value, alertsSize.value)
}

const mapAlertPriority = (alert: any) => {
  const lvl = (alert && (alert.level || alert.priority || alert.exception_type)) ? String(alert.level || alert.priority || alert.exception_type).toLowerCase() : 'info'
  if (lvl.includes('urgent') || lvl.includes('critical') || lvl.includes('danger')) return 'urgent'
  if (lvl.includes('warning') || lvl.includes('warn')) return 'warning'
  return 'info'
}

const mapAlertPriorityText = (alert: any) => {
  const p = mapAlertPriority(alert)
  return p === 'urgent' ? '紧急' : p === 'warning' ? '重要' : '一般'
}

const formatAlertTime = (v: any) => {
  if (!v) return ''
  try {
    const d = new Date(v)
    return d.toLocaleString()
  } catch {
    return v
  }
}

const handleOperatorProcess = async (alert: any) => {
  if (!alert || !alert.id) return
  alert.processing = true
  try {
    await operationsApi.processAlert(alert.id, 'resolved', 'Handled by operator UI')
    alert.processing = false
    alert.status = 'resolved'
    alert.updatedAt = new Date().toISOString()
    // move to processed list
    const idx = operatorPendingAlerts.value.findIndex(a => a.id === alert.id)
    if (idx > -1) operatorPendingAlerts.value.splice(idx, 1)
    operatorProcessedAlerts.value.unshift(alert)
  } catch (error) {
    console.error('处理运营告警失败', error)
    alert.processing = false
  }
}

// 详情模态框状态
const showPendingDetail = ref(false)
const currentPendingType = ref('')
const currentPendingList = ref<PendingItem[]>([])
const processingItemIndex = ref<number | null>(null)

// 今日航班计划（后端对接）
const flightSchedule = reactive<Array<{
  number: string
  origin: string
  destination: string
  departure: string
  arrival: string
  status: string
}>>([])

const mapStatusLabel = (status: string) => {
  const lower = (status || '').toLowerCase()
  const map: Record<string, string> = {
    scheduled: '准点',
    boarding: '登机',
    departed: '起飞',
    arrived: '到达',
    delayed: '延误',
    cancelled: '取消',
    canceled: '取消'
  }
  return map[lower] || status || '准点'
}

// 格式化时间 - 将UTC时间转换为北京时间
const formatTimeToHM = formatTime

// 北京时间
const beijingTime = ref('')
let beijingTimer: number | null = null

const updateBeijingTime = () => {
  // 显示当前北京时间（仅用于显示当前时间，不影响数据时间）
  const now = new Date()
  const year = now.getFullYear()
  const month = String(now.getMonth() + 1).padStart(2, '0')
  const day = String(now.getDate()).padStart(2, '0')
  const hour = String(now.getHours()).padStart(2, '0')
  const minute = String(now.getMinutes()).padStart(2, '0')
  const second = String(now.getSeconds()).padStart(2, '0')
  beijingTime.value = `${year}-${month}-${day} ${hour}:${minute}:${second}`
}

// 导航函数
const navigateTo = (path: string) => {
  router.push(path)
}

// 加载/刷新待处理事项数据（纯前端演示，不依赖后端）
const loadPendingItems = async () => {
  loading.value = true
  try {
    // 这里可以根据需要做一些简单的随机波动，让数据更“有生命力”
    const jitter = (base: number) => {
      const delta = Math.floor(Math.random() * 3) - 1 // -1,0,1
      return Math.max(0, base + delta)
    }

    pendingItems.flightAdjustments = jitter(initialPendingSnapshot.flightAdjustments)
    pendingItems.maintenanceAlerts = jitter(initialPendingSnapshot.maintenanceAlerts)

    // 模拟网络等待，给刷新按钮一点反馈
    await new Promise(resolve => setTimeout(resolve, 400))
  } finally {
    loading.value = false
  }
}

// 打开详情模态框
const openPendingDetail = (itemType: string) => {
  currentPendingType.value = itemType
  let listKey: keyof typeof pendingDetails | null = null
  
  if (itemType === '航班调整') {
    listKey = 'flightAdjustments'
  } else if (itemType === '飞机维护') {
    listKey = 'maintenanceAlerts'
  }
  
  if (listKey) {
    // 创建新的数组引用，确保响应式更新
    currentPendingList.value = pendingDetails[listKey].map(item => ({ ...item }))
    showPendingDetail.value = true
  }
}

// 关闭详情模态框
const closePendingDetail = () => {
  showPendingDetail.value = false
  currentPendingType.value = ''
  currentPendingList.value = []
  processingItemIndex.value = null
}

// 获取待处理类型描述
const getPendingTypeDescription = (type: string) => {
  const descriptions: Record<string, string> = {
    '航班调整': '需要调整的航班计划',
    '飞机维护': '即将到期的维护任务'
  }
  return descriptions[type] || ''
}

// 处理单个项目
const handleSingleItemProcess = async (item: PendingItem, index: number) => {
  if (item.status === 'processed') return
  
  processingItemIndex.value = index
  item.status = 'processing'
  
  try {
    // 模拟处理耗时
    await new Promise(resolve => setTimeout(resolve, 800))
    
    item.status = 'processed'
    
    // 同步状态回原始数据
    let listKey: keyof typeof pendingDetails | null = null
    if (currentPendingType.value === '航班调整') {
      listKey = 'flightAdjustments'
    } else if (currentPendingType.value === '飞机维护') {
      listKey = 'maintenanceAlerts'
    }
    
    if (listKey) {
      const originalItem = pendingDetails[listKey].find(i => i.id === item.id)
      if (originalItem) {
        originalItem.status = 'processed'
      }
    }
    
    // 更新待处理数量
    syncPendingCounts()
    
    showPromptModal('成功', `已处理：${item.title}`, 'success')
  } catch (error) {
    item.status = 'pending'
    showPromptModal('错误', '处理失败，请重试', 'error')
  } finally {
    processingItemIndex.value = null
  }
}

// 批量处理
const handleBatchProcess = async () => {
  const itemsToProcess = currentPendingList.value.filter(item => item.status !== 'processed')
  if (itemsToProcess.length === 0) return
  
  processingItemIndex.value = -1 // 使用-1表示批量处理
  
  // 确定原始数据列表
  let listKey: keyof typeof pendingDetails | null = null
  if (currentPendingType.value === '航班调整') {
    listKey = 'flightAdjustments'
  } else if (currentPendingType.value === '飞机维护') {
    listKey = 'maintenanceAlerts'
  }
  
  try {
    for (let i = 0; i < itemsToProcess.length; i++) {
      const item = itemsToProcess[i]
      if (!item) continue
      
      item.status = 'processing'
      await new Promise(resolve => setTimeout(resolve, 500))
      item.status = 'processed'
      
      // 同步状态回原始数据
      if (listKey) {
        const originalItem = pendingDetails[listKey].find(i => i.id === item.id)
        if (originalItem) {
          originalItem.status = 'processed'
        }
      }
    }
    
    // 更新待处理数量
    syncPendingCounts()
    
    showPromptModal('成功', `已批量处理 ${itemsToProcess.length} 个项目`, 'success')
  } catch (error) {
    showPromptModal('错误', '批量处理失败，请重试', 'error')
  } finally {
    processingItemIndex.value = null
  }
}

// 处理运营待办事项点击
const handlePendingItemClick = async (path: string, itemType: string) => {
  // 打开详情模态框而不是直接跳转
  openPendingDetail(itemType)
}

// 处理待处理事项操作（处理/标记为已读等）
const handlePendingItemAction = async (itemType: string, action: 'process' | 'markRead' = 'process') => {
  // 打开详情模态框，让用户查看和处理具体项目
  openPendingDetail(itemType)
}

// 刷新待处理事项（供外部调用）
const refreshPendingItems = async () => {
  await loadPendingItems()
  showPromptModal('成功', '待处理事项已根据演示数据刷新', 'success')
}

// 获取今日航班状态分布
const fetchStatusDistribution = async () => {
  statusLoading.value = true
  try {
    // 请求不带 planDate 参数，后端将返回全部日期范围的状态分布
    const counts = await operationsApi.getTodayStatusDistribution()
    const onTimeTotal =
      (counts?.scheduled || 0) +
      (counts?.boarding || 0) +
      (counts?.departed || 0) +
      (counts?.arrived || 0)
    stats.onTimeFlights = onTimeTotal
    stats.delayedFlights = counts?.delayed || 0
    stats.canceledFlights = counts?.cancelled || 0
    stats.activeFlights = (counts?.boarding || 0) + (counts?.departed || 0)
    // 异步上报到后端以便持久化（非阻塞）
    try {
      operationsApi.reportStatusSnapshot({
        scheduled: counts?.scheduled || 0,
        delayed: counts?.delayed || 0,
        cancelled: counts?.cancelled || 0,
        boarding: counts?.boarding || 0,
        departed: counts?.departed || 0,
        arrived: counts?.arrived || 0
      })
    } catch (e) {
      // 忽略上报错误
    }
  } catch (error) {
    console.warn('获取航班状态分布失败', error)
  } finally {
    statusLoading.value = false
  }
}

// 获取今日航班计划
const fetchFlightPlans = async () => {
  scheduleLoading.value = true
  try {
    const today = new Date().toISOString().split('T')[0]
    const plans = await operationsApi.getTodayFlightPlans({ planDate: today })
    flightSchedule.splice(
      0,
      flightSchedule.length,
      ...plans.map(plan => ({
        number: plan.flightNo || '未知航班',
        origin: plan.originAirport || '未知',
        destination: plan.destAirport || '未知',
        departure: formatTimeToHM(plan.schedDepTime),
        arrival: formatTimeToHM(plan.schedArrTime),
        status: mapStatusLabel(plan.status)
      }))
    )
  } catch (error) {
    console.warn('获取航班计划失败', error)
  } finally {
    scheduleLoading.value = false
  }
}

// 本地分页（航班计划）
const flightPage = ref<number>(0)
const flightSize = ref<number>(4)
const flightHasMore = ref<boolean>(false)

const pagedFlightSchedule = computed(() => {
  const start = flightPage.value * flightSize.value
  const end = start + flightSize.value
  // 更新 hasMore
  flightHasMore.value = flightSchedule.length > end
  return flightSchedule.slice(start, end)
})

const changeFlightPage = (newPage: number) => {
  flightPage.value = Math.max(0, newPage)
}

const onFlightSizeChange = () => {
  flightPage.value = 0
}

// 同步待处理数量与详细列表
const syncPendingCounts = () => {
  pendingItems.flightAdjustments = pendingDetails.flightAdjustments.filter(item => item.status !== 'processed').length
  pendingItems.maintenanceAlerts = pendingDetails.maintenanceAlerts.filter(item => item.status !== 'processed').length
}

const airportWeatherBriefings = ref<any[]>([])

const fetchOperatorWeather = async () => {
  // Extract major cities from the current flight schedule
  if (!flightSchedule.length) return
  
  const cities = Array.from(new Set(flightSchedule.flatMap(f => [f.origin, f.destination]))).slice(0, 4)
  const results = []
  
      for (const city of cities) {
    try {
      // Use clean city name (strip '机场' if present for API compatibility)
      const cleanCity = city.replace(/(机场|国际)/g, '').trim()
      const res: any = await weatherApi.queryWeather(cleanCity)
      if (res && res.realtime) {
        results.push({
          city,
          info: res.realtime.info,
          temp: res.realtime.temperature,
          icon: res.realtime.info.includes('雨') ? '🌧️' : 
                res.realtime.info.includes('云') ? '🌥️' : 
                res.realtime.info.includes('晴') ? '☀️' : '☁️'
        })
      }
    } catch (e) {
      console.warn(`Weather fetch failed for ${city}`, e)
    }
  }
  airportWeatherBriefings.value = results
}

// 初始化
onMounted(() => {
  syncPendingCounts()
  loadPendingItems()
  fetchStatusDistribution()
  fetchFlightPlans().then(() => {
    fetchOperatorWeather()
  })
  // 拉取运营告警（分页）
  fetchOperatorAlerts()
  updateBeijingTime()
  beijingTimer = window.setInterval(updateBeijingTime, 1000)
})

onUnmounted(() => {
  if (beijingTimer) {
    clearInterval(beijingTimer)
    beijingTimer = null
  }
})
 
</script>

<style scoped>

:root {
  --od-primary: #1E8AE6;
  --od-primary-600: #2563eb;
  --od-success: #10b981;
  --od-warning: #f59e0b;
  --od-danger: #ef4444;
  --od-card-bg: #FFFFFF;
  --od-card-border: rgba(14,40,70,0.06);
  --od-radius-sm: 8px;
  --od-radius-md: 12px;
  --od-shadow-sm: 0 10px 30px rgba(16,24,40,0.06);
  --od-shadow-md: 0 20px 48px rgba(16,24,40,0.08);
}

.breadcrumb {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.7);
  margin: 20px;
  margin-bottom: 20px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(180px, 1fr));
  gap: 12px;
  margin: 12px;
  margin-bottom: 12px;
  align-items: stretch;
}

.stat-card {
  background: var(--od-card-bg);
  border: 1px solid var(--od-card-border);
  border-radius: var(--od-radius-md);
  padding: 18px;
  transition: transform 220ms ease, box-shadow 220ms ease, border-color 220ms ease;
  box-shadow: var(--od-shadow-sm);
  display: flex;
  align-items: center;
  gap: 16px;
  min-height: 110px;
}

.stat-card:hover {
  transform: translateY(-6px);
  box-shadow: var(--od-shadow-md);
  border-color: color-mix(in srgb, var(--od-primary-600) 30%, var(--od-card-border));
}

.stat-icon {
  font-size: 32px;
  width: 60px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
  background: linear-gradient(135deg, rgba(30,138,230,0.12), rgba(30,138,230,0.06));
  color: var(--od-primary-600);
  flex-shrink: 0;
}

.stat-content {
  color: var(--od-primary-600);
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.stat-label {
  font-size: 13px;
  color: rgba(10,31,51,0.6);
  font-weight: 600;
}

.stat-value {
  font-size: 28px;
  font-weight: 800;
  color: rgba(10,31,51,0.9);
}

.stat-change {
  font-size: 13px;
  font-weight: 700;
  padding: 6px 10px;
  border-radius: 999px;
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.stat-change.positive {
  color: var(--od-success);
  background: rgba(16, 185, 129, 0.08);
  border: 1px solid rgba(16,185,129,0.12);
}

.stat-change.negative {
  color: var(--od-danger);
  background: rgba(239, 68, 68, 0.06);
  border: 1px solid rgba(239,68,68,0.08);
}

.stat-change.positive::before { content: "▲"; color: var(--od-success); font-size: 12px; }
.stat-change.negative::before { content: "▼"; color: var(--od-danger); font-size: 12px; }

.pending-section {
  margin: 12px;
  margin-bottom: 12px;
}

.section-title {
  font-size: 20px;
  color: white;
  font-weight: 600;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  gap: 12px;
  justify-content: space-between;
}

.title-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.refresh-pending-btn {
  padding: 0.4rem 0.8rem;
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 8px;
  background: rgba(15, 23, 42, 0.6);
  color: rgba(255, 255, 255, 0.9);
  font-size: 1rem;
  cursor: pointer;
  transition: all 0.3s;
  display: flex;
  align-items: center;
  justify-content: center;
  min-width: 36px;
  height: 36px;
  backdrop-filter: blur(10px);
}

.refresh-pending-btn:hover:not(:disabled) {
  background: rgba(99, 102, 241, 0.2);
  border-color: rgba(99, 102, 241, 0.4);
  color: #a5b4fc;
  transform: rotate(180deg);
}

.refresh-pending-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  transform: none;
}

.refresh-pending-btn {
  padding: 0.4rem 0.8rem;
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 8px;
  background: rgba(15, 23, 42, 0.6);
  color: rgba(255, 255, 255, 0.9);
  font-size: 1rem;
  cursor: pointer;
  transition: all 0.3s;
  display: flex;
  align-items: center;
  justify-content: center;
  min-width: 36px;
  height: 36px;
}

.refresh-pending-btn:hover:not(:disabled) {
  background: rgba(99, 102, 241, 0.2);
  border-color: rgba(99, 102, 241, 0.4);
  color: #a5b4fc;
  transform: rotate(180deg);
}

.refresh-pending-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.title-icon {
  font-size: 24px;
}

.pending-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
  gap: 12px;
}

.pending-card {
  background: rgba(15, 23, 42, 0.6);
  backdrop-filter: blur(14px);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 12px;
  padding: 16px;
  cursor: pointer;
  transition: all 0.22s ease;
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.18);
  position: relative;
  overflow: hidden;
}

.pending-card:hover:not(.disabled):not(.loading) {
  transform: translateY(-4px);
  box-shadow: 0 12px 40px rgba(99, 102, 241, 0.3);
}

.pending-card.disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.pending-card.loading {
  cursor: wait;
  pointer-events: none;
}

.pending-loading-overlay {
  position: absolute;
  inset: 0;
  background: rgba(15, 23, 42, 0.8);
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 16px;
  backdrop-filter: blur(4px);
  z-index: 10;
}

.pending-loading-overlay span {
  color: rgba(255, 255, 255, 0.9);
  font-size: 0.9rem;
  font-weight: 500;
}

.loading-spinner-small {
  display: inline-block;
  width: 16px;
  height: 16px;
  border: 2px solid rgba(255, 255, 255, 0.2);
  border-top-color: #1E8AE6;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
  vertical-align: middle;
  margin-right: 4px;
}

.pending-card.urgent {
  border-left: 4px solid #f87171;
}

.pending-card.urgent:hover {
  box-shadow: 0 12px 40px rgba(248, 113, 113, 0.3);
  border-color: rgba(248, 113, 113, 0.3);
}

.pending-card.warning {
  border-left: 4px solid #fbbf24;
}

.pending-card.warning:hover {
  box-shadow: 0 12px 40px rgba(251, 191, 36, 0.3);
  border-color: rgba(251, 191, 36, 0.3);
}

.pending-card.info {
  border-left: 4px solid #1E8AE6;
}

.pending-card.info:hover {
  box-shadow: 0 12px 40px rgba(96, 165, 250, 0.3);
  border-color: rgba(96, 165, 250, 0.3);
}

.pending-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.pending-header h3 {
  color: white;
  font-size: 18px;
  font-weight: 600;
  margin: 0;
}

.pending-count {
  color: white;
  font-size: 20px;
  font-weight: 700;
  background: linear-gradient(135deg, rgba(99, 102, 241, 0.3), rgba(139, 92, 246, 0.3));
  padding: 4px 12px;
  border-radius: 12px;
  border: 1px solid rgba(99, 102, 241, 0.4);
}

.pending-card p {
  color: rgba(255, 255, 255, 0.6);
  font-size: 14px;
  margin: 0 0 1rem 0;
}

.pending-actions {
  display: flex;
  gap: 0.5rem;
  margin-top: 0.5rem;
}

.action-btn {
  flex: 1;
  padding: 0.5rem 1rem;
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 8px;
  background: rgba(15, 23, 42, 0.6);
  color: rgba(255, 255, 255, 0.9);
  font-size: 0.85rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
  backdrop-filter: blur(10px);
}

.action-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
}

.action-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  transform: none;
}

.process-btn {
  background: linear-gradient(135deg, rgba(99, 102, 241, 0.3), rgba(139, 92, 246, 0.3));
  border-color: rgba(99, 102, 241, 0.4);
  color: #a5b4fc;
}

.process-btn:hover:not(:disabled) {
  background: linear-gradient(135deg, rgba(99, 102, 241, 0.4), rgba(139, 92, 246, 0.4));
  border-color: rgba(99, 102, 241, 0.6);
}

.view-btn {
  background: rgba(96, 165, 250, 0.2);
  border-color: rgba(96, 165, 250, 0.4);
  color: #1E8AE6;
}

.view-btn:hover:not(:disabled) {
  background: rgba(96, 165, 250, 0.3);
  border-color: rgba(96, 165, 250, 0.6);
}

.charts-section {
  display: grid;
  grid-template-columns: 1fr 1fr;
  grid-auto-rows: 1fr; /* 每列等高，便于内容填满 */
  gap: 12px;
  margin: 12px;
  align-items: stretch;
  min-height: 360px;
}

.chart-card {
  background: var(--od-card-bg);
  border: 1px solid var(--od-card-border);
  border-radius: 12px;
  padding: 12px;
  box-shadow: var(--od-shadow-sm);
  transition: transform 180ms ease, box-shadow 180ms ease;
  display: flex;
  flex-direction: column;
  height: 100%;
}

.chart-card:hover {
  transform: translateY(-6px);
  box-shadow: var(--od-shadow-md);
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
}

.chart-header h3 {
  color: rgba(10,31,51,0.92);
  font-size: 18px;
  font-weight: 700;
  margin: 0;
}

.beijing-time {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  padding: 8px 12px;
  background: rgba(30, 138, 230, 0.18);
  border: 1px solid rgba(30, 138, 230, 0.35);
  border-radius: 12px;
  box-shadow: 0 6px 20px rgba(30, 138, 230, 0.2);

}

.time-indicator {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: #34d399;
  box-shadow: 0 0 0 6px rgba(52, 211, 153, 0.12);
}

.time-text {
  display: flex;
  flex-direction: column;
  gap: 2px;
  color: #dbeafe;
}

.time-label {
  font-size: 12px;
  letter-spacing: 0.5px;
  color: rgba(219, 234, 254, 0.8);
}

.time-value {
  font-size: 14px;
  font-weight: 700;
  font-variant-numeric: tabular-nums;
  color: #f8fafc;
}

.chart-placeholder {
  /* 让内容区域填满卡片剩余高度并支持右侧侧栏 */
  display: grid;
  grid-template-columns: 1fr 10px;
  gap: 12px;
  align-items: start;
  flex: 1;
  min-height: 0; /* 允许内部滚动 */
}

.status-legend {
  display:flex;
  flex-direction:column;
  gap:12px;
  padding: 6px 0;
}

.status-item {
  display: flex;
  align-items: center;
  gap: 32px;
  padding: 40px 64px;
  border-radius: 12px;
  background: #fff;
  border: 1px solid rgba(14,40,70,0.04);
  min-width: 420px;
}

.status-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 26px rgba(16,24,40,0.06);
}

.status-color {
  width:18px;
  height:18px;
  border-radius:6px;
  flex-shrink:0;
}
.status-item.ontime .status-color { background: var(--od-success); box-shadow: 0 6px 18px rgba(16,185,129,0.12); }
.status-item.delayed .status-color { background: var(--od-warning); box-shadow: 0 6px 18px rgba(251,191,36,0.12); }
.status-item.canceled .status-color { background: var(--od-danger); box-shadow: 0 6px 18px rgba(239,68,68,0.12); }

.status-info {
  flex:1;
  display:flex;
  align-items:center;
  justify-content:space-between;
  color: rgba(10,31,51,0.85);
}
.status-name {
  font-size: 15px;
  font-weight: 700;
  color: rgba(10,31,51,0.85);
}
.status-value {
  font-size: 16px;
  font-weight: 700;
  color: rgba(10,31,51,0.92);
}
.status-percentage {
  font-size: 13px;
  padding: 6px 10px;
  border-radius: 8px;
  background: rgba(14,40,70,0.04);
  color: rgba(10,31,51,0.8);
}

.flight-schedule {
  display: flex;
  flex-direction: column;
  gap: 8px;
  /* 占满可用高度并启用内部滚动，让分页固定在底部 */
  flex: 1 1 auto;
  min-height: 0;
  overflow: auto;
  padding-right: 6px;
  width: 600px;
  
}

.flight-schedule-loading,
.flight-schedule-empty {
  padding: 16px;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 12px;
  border: 1px solid rgba(255, 255, 255, 0.1);
  color: rgba(255, 255, 255, 0.7);
  text-align: center;
}
/* 航班信息 */
.flight-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 12px;
  background: rgba(255, 255, 255, 0.04);
  border-radius: 8px;
  border: 1px solid rgba(255, 255, 255, 0.06);
  transition: all 0.18s ease;
}

.flight-item:hover {
  background: rgba(255, 255, 255, 0.08);
  border-color: rgba(255, 255, 255, 0.2);
  transform: translateX(4px);
}

.flight-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
  color: white;
  flex: 1;
}

.flight-number {
  font-size: 16px;
  font-weight: 700;
  color: #1E8AE6;
}

.flight-route {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.7);
}

.flight-time {
  display: flex;
  flex-direction: column;
  gap: 4px;
  color: white;
  text-align: center;
  margin: 0 20px;
}

.departure, .arrival {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.8);
}

.flight-status {
  padding: 8px 16px;
  border-radius: 12px;
  font-size: 13px;
  font-weight: 600;
  white-space: nowrap;
  border: 1px solid;

}

.flight-status.准点 {
  background: rgba(52, 211, 153, 0.15);
  color: #34d399;
  border-color: rgba(52, 211, 153, 0.3);
}

.flight-status.延误 {
  background: rgba(251, 191, 36, 0.15);
  color: #fbbf24;
  border-color: rgba(251, 191, 36, 0.3);
}

.flight-status.取消 {
  background: rgba(248, 113, 113, 0.15);
  color: #f87171;
  border-color: rgba(248, 113, 113, 0.3);
}

/* 快捷操作区域 */
.quick-actions-section {
  margin: 20px;
  margin-bottom: 30px;
}

.quick-actions-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(240px, 1fr)); /* 2行 x 3列 布局 */
  gap: 20px;
}

.quick-action-card {
  background: var(--od-card-bg);
  border: 1px solid rgba(232,232,232,0.9);
  border-radius: 12px;
  padding: 18px;
  height: 180px;
  cursor: pointer;
  transition: transform 220ms ease, box-shadow 220ms ease, border-color 220ms ease;
  box-shadow: 0 10px 30px rgba(16,24,40,0.06);
  text-align: center;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10px;
}

.quick-action-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 22px 48px rgba(16,24,40,0.12);
  border-color: color-mix(in srgb, var(--od-primary-600) 24%, rgba(0,0,0,0.06));
}

.action-icon {
  width: 64px;
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
  background: linear-gradient(180deg,#ffffff,#f4f8ff);
  box-shadow: 0 8px 20px rgba(7,34,58,0.04);
  font-size: 28px;
  color: var(--od-primary-600);
  margin-bottom: 8px;
}

.quick-action-card h3 {
  color: white;
  font-size: 18px;
  font-weight: 600;
  margin: 0 0 8px 0;
}

.quick-action-card p {
  color: rgba(255, 255, 255, 0.6);
  font-size: 14px;
  margin: 0;
  line-height: 1.5;
}
/* 待处理事项详情模态框样式 */
.pending-detail-modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.75);
  backdrop-filter: blur(8px);
  z-index: 9999;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 2rem;
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

.pending-detail-modal {
  background: rgba(15, 23, 42, 0.95);
  border: 1px solid rgba(148, 163, 184, 0.2);
  border-radius: 24px;
  width: 100%;
  max-width: 800px;
  max-height: 90vh;
  display: flex;
  flex-direction: column;
  box-shadow: 0 25px 60px rgba(0, 0, 0, 0.9);
  backdrop-filter: blur(20px);
  animation: slideUp 0.3s ease;
  overflow: hidden;
}

@keyframes slideUp {
  from {
    transform: translateY(20px);
    opacity: 0;
  }
  to {
    transform: translateY(0);
    opacity: 1;
  }
}

.pending-detail-modal .modal-header {
  padding: 1.5rem 2rem;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  flex-shrink: 0;
}

.pending-detail-modal .modal-header h3 {
  margin: 0 0 0.5rem 0;
  color: #fff;
  font-size: 1.5rem;
}

.pending-detail-modal .modal-header p {
  margin: 0;
  color: rgba(248, 250, 252, 0.7);
  font-size: 0.9rem;
}

.pending-detail-modal .close-btn {
  background: transparent;
  border: none;
  color: rgba(255, 255, 255, 0.7);
  font-size: 2rem;
  line-height: 1;
  cursor: pointer;
  padding: 0;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  transition: all 0.2s;
}

.pending-detail-modal .close-btn:hover {
  background: rgba(255, 255, 255, 0.1);
  color: #fff;
}

.pending-detail-modal .modal-body {
  padding: 2rem;
  overflow-y: auto;
  flex: 1;
}

.pending-detail-modal .empty-state {
  text-align: center;
  padding: 3rem 1rem;
  color: rgba(255, 255, 255, 0.6);
}

.pending-detail-modal .pending-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.pending-detail-modal .pending-item {
  padding: 1.5rem;
  background: rgba(8, 14, 35, 0.5);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  transition: all 0.3s;
}

.pending-detail-modal .pending-item:hover {
  border-color: rgba(59, 130, 246, 0.4);
  background: rgba(8, 14, 35, 0.7);
}

.pending-detail-modal .pending-item.processing {
  border-color: rgba(59, 130, 246, 0.6);
  background: rgba(59, 130, 246, 0.1);
}

.pending-detail-modal .item-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 1rem;
}

.pending-detail-modal .item-info {
  flex: 1;
}

.pending-detail-modal .item-info h4 {
  margin: 0 0 0.5rem 0;
  color: #fff;
  font-size: 1.1rem;
  font-weight: 600;
}

.pending-detail-modal .item-info p {
  margin: 0 0 0.75rem 0;
  color: rgba(248, 250, 252, 0.7);
  font-size: 0.9rem;
  line-height: 1.5;
}

.pending-detail-modal .item-meta {
  display: flex;
  gap: 1rem;
  align-items: center;
  flex-wrap: wrap;
}

.pending-detail-modal .meta-tag {
  padding: 0.25rem 0.75rem;
  border-radius: 6px;
  font-size: 0.85rem;
  font-weight: 500;
}

.pending-detail-modal .meta-tag.urgent {
  background: rgba(248, 113, 113, 0.2);
  color: #f87171;
  border: 1px solid rgba(248, 113, 113, 0.3);
}

.pending-detail-modal .meta-tag.warning {
  background: rgba(251, 191, 36, 0.2);
  color: #fbbf24;
  border: 1px solid rgba(251, 191, 36, 0.3);
}

.pending-detail-modal .meta-tag.info {
  background: rgba(59, 130, 246, 0.2);
  color: #60a5fa;
  border: 1px solid rgba(59, 130, 246, 0.3);
}

.pending-detail-modal .meta-time {
  color: rgba(248, 250, 252, 0.5);
  font-size: 0.85rem;
}

.pending-detail-modal .item-actions {
  flex-shrink: 0;
}

.pending-detail-modal .action-btn-small {
  padding: 0.5rem 1rem;
  border: 1px solid rgba(59, 130, 246, 0.4);
  border-radius: 8px;
  background: rgba(59, 130, 246, 0.15);
  color: #60a5fa;
  font-size: 0.9rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
  white-space: nowrap;
}

.pending-detail-modal .action-btn-small:hover:not(:disabled) {
  background: rgba(59, 130, 246, 0.25);
  border-color: rgba(59, 130, 246, 0.6);
  transform: translateY(-1px);
}

.pending-detail-modal .action-btn-small:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.pending-detail-modal .modal-footer {
  padding: 1.5rem 2rem;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  display: flex;
  justify-content: flex-end;
  gap: 1rem;
  flex-shrink: 0;
}

.pending-detail-modal .modal-footer .ghost-btn,
.pending-detail-modal .modal-footer .primary-btn {
  padding: 0.75rem 1.5rem;
  font-size: 0.95rem;
}

.pending-detail-modal .modal-footer .primary-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

@media (max-width: 768px) {
  .pending-detail-modal {
    max-width: 95%;
    max-height: 95vh;
  }

  .pending-detail-modal .item-header {
    flex-direction: column;
  }

  .pending-detail-modal .item-actions {
    width: 100%;
  }

  .pending-detail-modal .action-btn-small {
    width: 100%;
  }
}

/* ===== Additional enhanced styles injected for OperatorDashboard visual improvements ===== */

/* Enhanced borders, color variants and interaction feedback */
.quick-action-card {
  border: 1.5px solid rgba(30, 138, 230, 0.12);
  border-radius: 6px;
  transition: all 180ms ease;
}
.quick-action-card:hover {
  transform: translateY(-6px);
  background: rgba(255,255,255,0.06);
  border-color: rgba(30,138,230,0.22);
  box-shadow: 0 14px 36px rgba(16,24,40,0.08);
}
.quick-action-card:active {
  transform: translateY(-2px) scale(0.995);
}

/* action variants */
.quick-action-card.primary-action { border-color: rgba(16,185,129,0.28); background: rgba(16,185,129,0.04); }
.quick-action-card.order-action   { border-color: rgba(245,158,11,0.28); background: rgba(245,158,11,0.04); }
.quick-action-card.analysis-action{ border-color: rgba(139,92,246,0.28); background: rgba(139,92,246,0.03); }
.quick-action-card.special-action { border-color: rgba(30,138,230,0.28); background: rgba(30,138,230,0.03); }
.quick-action-card.info-action    { border-color: rgba(59,130,246,0.22); background: rgba(59,130,246,0.03); }
.quick-action-card.danger-action  { border-color: rgba(239,68,68,0.28); background: rgba(239,68,68,0.03); }

.quick-action-card .action-icon {
  border-radius: 8px;
  width: 60px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 26px;
}

/* Stat card emphasis */
.stat-card {
  border: 1px solid rgba(255, 255, 255, 0.5);
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.45);
  backdrop-filter: blur(16px);
  box-shadow: 0 8px 32px rgba(31, 38, 135, 0.07);
}
.stat-value {
  font-size: 32px;
  font-weight: 900;
  color: #1e293b;
  text-shadow: 0 1px 2px rgba(0,0,0,0.05);
}
.stat-label { color: #475569; }
.stat-change { font-weight: 700; }

.quick-action-card {
  border: 1px solid rgba(255, 255, 255, 0.5);
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.45);
  backdrop-filter: blur(16px);
  box-shadow: 0 8px 32px rgba(31, 38, 135, 0.07);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}
.quick-action-card:hover {
  transform: translateY(-8px);
  background: rgba(255, 255, 255, 0.6);
  border-color: #3b82f6;
  box-shadow: 0 12px 40px rgba(59, 130, 246, 0.15);
}

.chart-card {
  border: 1px solid rgba(255, 255, 255, 0.5);
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.4);
  backdrop-filter: blur(16px);
  box-shadow: 0 8px 32px rgba(31, 38, 135, 0.07);
}

.breadcrumb-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  margin-top: 10px;
}

.weather-briefing-bar {
  display: flex;
  gap: 15px;
  background: rgba(255, 255, 255, 0.3);
  padding: 6px 16px;
  border-radius: 20px;
  backdrop-filter: blur(8px);
  border: 1px solid rgba(255, 255, 255, 0.4);
}

.brief-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #1e293b;
  font-weight: 500;
}

.brief-city { color: #3b82f6; }
.brief-temp { font-weight: 700; }


/* Buttons */
.action-btn {
  border: 2px solid rgba(30,138,230,0.12);
  border-radius: 6px;
  background: rgba(15,23,42,0.6);
  color: rgba(255,255,255,0.95);
  transition: all 160ms ease;
}
.action-btn:hover:not(:disabled) {
  background: rgba(255,255,255,0.06);
  border-color: rgba(30,138,230,0.22);
  transform: translateY(-2px);
}
.action-btn:active {
  transform: translateY(0) scale(0.992);
}

.process-btn {
  border-color: rgba(16,185,129,0.28);
  background: linear-gradient(135deg, rgba(16,185,129,0.06), rgba(16,185,129,0.03));
  color: #059669;
}
.process-btn:hover:not(:disabled) {
  background: linear-gradient(135deg, rgba(16,185,129,0.12), rgba(16,185,129,0.06));
  border-color: rgba(16,185,129,0.38);
}

.view-btn {
  border-color: rgba(59,130,246,0.22);
  background: rgba(59,130,246,0.035);
  color: #1E8AE6;
}

/* Chart and status colors - brighter and higher contrast */
.status-item {
  background: #fff;
  border: 1px solid rgba(14,40,70,0.06);
}
.status-item .status-color { box-shadow: 0 6px 18px rgba(0,0,0,0.04); }
.status-item.ontime .status-color { background: #10B981; box-shadow: 0 6px 18px rgba(16,185,129,0.14); }
.status-item.delayed .status-color { background: #f59e0b; box-shadow: 0 6px 18px rgba(245,158,11,0.12); }
.status-item.canceled .status-color { background: #ef4444; box-shadow: 0 6px 18px rgba(239,68,68,0.12); }

/* Flight status badges clearer contrast */
.flight-status {
  color: #0b2540;
  font-weight: 700;
}
.flight-status.准点 { background: rgba(16,185,129,0.12); color: #059669; border-color: rgba(16,185,129,0.2); }
.flight-status.延误 { background: rgba(251,191,36,0.12); color: #b45309; border-color: rgba(251,191,36,0.22); }
.flight-status.取消 { background: rgba(248,113,113,0.12); color: #ef4444; border-color: rgba(248,113,113,0.2); }

/* Modal footer buttons */
.ghost-btn {
  background: rgba(255,255,255,0.06);
  border: 1px solid rgba(255,255,255,0.12);
  color: rgba(255,255,255,0.9);
  border-radius: 6px;
}
.primary-btn {
  background: linear-gradient(120deg,#3B82F6,#1E40AF);
  border: 1px solid rgba(59,130,246,0.28);
  color: #fff;
  border-radius: 6px;
  box-shadow: 0 10px 24px rgba(59,130,246,0.18);
}

</style>

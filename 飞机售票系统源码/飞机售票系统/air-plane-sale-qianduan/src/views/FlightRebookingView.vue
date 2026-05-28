<template>
  <AdminLayout>
    <div class="rebooking-page">
      <!-- 返回按钮和标题 -->
      <div class="page-header-section">
        <button class="back-btn" @click="handleGoBack">
          <span class="back-icon">←</span>
          <span>返回</span>
        </button>
        <div class="header-content">
          <div class="breadcrumb">
            <span>首页</span>
            <span class="breadcrumb-separator">/</span>
            <span>订单管理</span>
            <span class="breadcrumb-separator">/</span>
            <span>机票改签</span>
          </div>
          <h1 class="page-title">机票改签 #{{ orderId || 'N/A' }}</h1>
          <p class="page-description">查询并选择新的航班进行改签</p>
        </div>
      </div>

      <!-- 原订单信息卡片 -->
      <section class="glass-card order-info-card" v-if="orderInfo">
        <h2 class="section-title">原订单信息</h2>
        <div class="info-grid">
          <div class="info-item">
            <span class="info-label">订单号</span>
            <span class="info-value">{{ orderId || 'N/A' }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">航班号</span>
            <span class="info-value">{{ orderInfo.flightNo || orderInfo.flight_no || '未提供' }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">机票号</span>
            <span class="info-value">{{ orderInfo.ticketNo || orderInfo.ticket_no || '未提供' }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">原订单金额</span>
            <span class="info-value">¥{{ originalAmount }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">原航线</span>
            <span class="info-value">{{ orderInfo.route }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">起飞时间</span>
            <span class="info-value">{{ originalDepartureTime || '未提供' }}</span>
          </div>
        </div>
      </section>

      <!-- 改签费用设置 -->
      <section class="glass-card fee-card">
        <h2 class="section-title">改签费用设置</h2>
        <div class="fee-description" v-if="!isAdmin">
          <span class="info-icon">ℹ️</span>
          <span>改签费用由系统管理员统一规定，乘客不可修改</span>
        </div>
        <div class="fee-grid">
          <div class="fee-item">
            <label class="fee-label">
              改签手续费
              <span class="fee-calc-hint" v-if="feePercent > 0">
                ({{ feePercent }}% × ¥{{ originalAmount }} = ¥{{ changeFee }})
              </span>
            </label>
            <div class="fee-input-wrapper">
              <input 
                type="number" 
                v-model.number="changeFee" 
                min="0" 
                class="fee-input"
                :class="{ 'fee-input-disabled': !isAdmin }"
                :disabled="!isAdmin"
                :placeholder="isAdmin ? '请输入改签手续费' : '系统自动计算'"
              />
              <span class="fee-unit">¥</span>
            </div>
            <div class="fee-auto-note" v-if="!isAdmin && selectedFlight">
              <span>系统根据票价自动计算（{{ feePercent }}%）</span>
            </div>
          </div>
          <div class="fee-item">
            <label class="fee-label">机票总价</label>
            <div class="fee-input-wrapper">
              <input
                type="text"
                :value="`¥${ticketTotalPrice.toFixed(2)}`"
                class="fee-input"
                disabled
              />
            </div>
          </div>
  
        </div>
      </section>

      <!-- 航班搜索区域 -->
      <section class="glass-card search-card">
        <h2 class="section-title">查询新航班</h2>
        <div class="search-section">
          <div class="search-row">
            <div class="search-item">
              <label>出发机场</label>
              <input 
                v-model="searchParams.departure" 
                type="text" 
                placeholder="例如：北京首都国际机场" 
                class="search-input"
                :disabled="!isAdmin"
                :title="!isAdmin ? '由原航线解析，乘客不可修改' : ''"
              />
            </div>
            <div class="search-item">
              <label>到达机场</label>
              <input 
                v-model="searchParams.destination" 
                type="text" 
                placeholder="例如：上海虹桥国际机场" 
                class="search-input"
                :disabled="!isAdmin"
                :title="!isAdmin ? '由原航线解析，乘客不可修改' : ''"
              />
            </div>
            <div class="search-item">
              <label>出发日期</label>
              <input 
                v-model="searchParams.date" 
                type="date" 
                class="search-input"
              />
            </div>
          
            <button 
              type="button" 
              class="primary-btn search-btn" 
              :disabled="searchLoading"
              @click="handleSearchFlights"
            >
              {{ searchLoading ? '搜索中...' : '搜索航班' }}
            </button>
          </div>
       
        </div>
      </section>

      <!-- 搜索结果 -->
      <section class="glass-card results-card" v-if="searchResults.length > 0 || searchLoading || hasSearched">
        <h2 class="section-title">搜索结果</h2>
        <div class="results-container">
          <div v-if="searchLoading" class="loading-state">
            <div class="spinner"></div>
            <p>正在加载航班...</p>
          </div>
          <div v-else-if="!searchResults.length" class="empty-state">
            <p>暂无搜索结果，请调整条件后重试</p>
          </div>
          <div v-else class="flight-results">
            <div 
              v-for="flight in searchResults" 
              :key="flight.id || flight.flightNumber" 
              class="flight-item"
              :class="{ active: selectedFlight && (selectedFlight.id === flight.id || selectedFlight.flightNumber === flight.flightNumber) }"
              @click="selectFlight(flight)"
            >
              <div class="flight-main">
                <div class="flight-route">
                  <span class="city">{{ flight.departure }}</span>
                  <span class="arrow">→</span>
                  <span class="city">{{ flight.destination }}</span>
                </div>
                <div class="flight-time">
                  <span>{{ formatTime(flight.departureTime || '') }}</span>
                  <span class="separator">-</span>
                  <span>{{ formatTime(flight.arrivalTime || '') }}</span>
                </div>
                <div class="flight-number">航班号：{{ flight.flightNumber }}</div>
              </div>
              <div class="flight-side">
                <div class="price">¥{{ flight.price }}</div>
                <div class="select-indicator" v-if="selectedFlight && (selectedFlight.id === flight.id || selectedFlight.flightNumber === flight.flightNumber)">
                  ✓ 已选择
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- 已选航班信息 -->
      <section class="glass-card selected-card" v-if="selectedFlight">
        <h2 class="section-title">已选航班</h2>
        <div class="selected-flight-info">
          <div class="selected-route">
            <span class="pill pill-success">已选航班</span>
            <div class="route-text">
              {{ selectedFlight.departure }} → {{ selectedFlight.destination }}
            </div>
          </div>
          <div class="selected-meta">
            <span class="meta-item">航班号：{{ selectedFlight.flightNumber }}</span>
            <span class="meta-item">
              {{ formatTime(selectedFlight.departureTime || '') }} - {{ formatTime(selectedFlight.arrivalTime || '') }}
            </span>
            <span class="meta-item price">¥{{ selectedFlight.price }}</span>
          </div>
          <div class="selected-price-summary" v-if="selectedFlight">
            <div class="meta-item">座位费合计：<strong>¥{{ seatFeeTotal.toFixed(2) }}</strong></div>
            <div class="meta-item">机票总价：<strong>¥{{ ticketTotalPrice.toFixed(2) }}</strong></div>
          </div>
        </div>
      </section>
      
      <!-- 改签乘客与选座 -->
      <section class="glass-card passenger-seat-card" v-if="orderInfo">
        <h2 class="section-title">选择改签乘客 & 选座</h2>
        <div class="passenger-selection">
          <label>改签乘客</label>
          <div class="passenger-list">
            <label 
              v-for="p in passengersList" 
              :key="p.id || p.passengerId || p.name"
              class="passenger-item"
            >
              <input 
                type="checkbox" 
                :value="p" 
                v-model="selectedPassengers"
              />
              <span class="passenger-name">{{ p.name || p.passengerName || p.fullName || p.idCard || '乘客' }}</span>
              <input
                type="text"
                v-model="p.phone"
                class="passenger-phone-input"
                placeholder="联系电话（选填）"
                style="margin-left:8px; padding:4px 8px; border-radius:6px; border:1px solid rgba(148,163,184,0.3); background:rgba(15,23,42,0.9); color:#e5e7eb; font-size:13px;"
              />
            </label>
            <div v-if="!passengersList.length" class="passenger-fallback">
              <p>未找到乘客列表，默认使用当前账户作为改签申请人。</p>
            </div>
          </div>
        </div>
        <div class="seat-selection-row" v-if="selectedFlight">
          <label>座位分配</label>
          <div class="seat-actions">
            <button 
              class="ghost-btn" 
              :disabled="!selectedFlight" 
              @click="openSeatSelection"
            >
              选择座位
            </button>
            <div class="seat-summary" v-if="seatAssignments.length > 0">
              <div v-for="(s, idx) in seatAssignments" :key="s.seatId || idx" class="seat-assignment">
                <span>{{ s.passengerName || selectedPassengers[idx]?.name || '乘客' }}: {{ s.seatLabel || s.seatNumber || s.seatId }} <strong>（¥{{ normalizePrice(s.price) }})</strong></span>
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- 改签原因 -->
      <section class="glass-card reason-card">
        <h2 class="section-title">改签原因</h2>
        <textarea 
          v-model="reason" 
          rows="4" 
          class="reason-textarea"
          placeholder="请填写改签原因（必填）"
        ></textarea>
      </section>

      <!-- 错误提示 -->
      <div class="error-message" v-if="errorMessage">
        {{ errorMessage }}
      </div>

      <!-- 底部操作按钮 -->
      <div class="action-footer">
        <button class="ghost-btn" @click="handleGoBack">取消</button>
        <button 
          class="primary-btn" 
          :disabled="submitting || !reason.trim()" 
          @click="handleSubmit"
        >
          {{ submitting ? '提交中...' : '确认改签' }}
        </button>
      </div>
    </div>
  </AdminLayout>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import AdminLayout from '../components/AdminLayout.vue'
import { flightApi, orderManagementApi, orderApi, frequentPassengerApi, apiUtils } from '../services/api'
import store from '../services/store'

const route = useRoute()
const router = useRouter()

// 判断是否为管理员
const isAdmin = computed(() => {
  return store.userState.role === 'admin'
})

// 订单信息
const orderId = ref<string>('')
const orderInfo = ref<any>(null)
const originalAmount = computed(() => {
  if (!orderInfo.value) return 0
  const source = orderInfo.value.amount ?? orderInfo.value.totalAmount ?? orderInfo.value.price
  return normalizePrice(source)
})
const originalDepartureTime = computed(() => {
  if (!orderInfo.value) return ''
  const dt =
    orderInfo.value.departureTime ||
    orderInfo.value.time ||
    orderInfo.value.createdAt
  return formatDateTime(dt)
})
const routeParts = computed(() => {
  if (!orderInfo.value?.route) return { from: '', to: '' }
  return parseRouteParts(orderInfo.value.route)
})

// 改签费用
const changeFee = ref<number>(0)
const priceDiff = ref<number>(0)

// 手续费百分比设置
const feePercent = ref<number>(5) // 默认5%

// 加载手续费百分比设置
const loadFeeSettings = () => {
  try {
    const saved = localStorage.getItem('feeSettings')
    if (saved) {
      const parsed = JSON.parse(saved)
      feePercent.value = parsed.changeFeePercent ?? 5
    }
  } catch (error) {
    console.warn('加载手续费设置失败:', error)
  }
}

// 搜索参数
const searchParams = reactive({
  departure: '',
  destination: '',
  date: '',
  passengers: '1'
})

// 搜索结果
const searchResults = ref<any[]>([])
const selectedFlight = ref<any>(null)
const searchLoading = ref(false)
const hasSearched = ref(false)

// 改签原因
const reason = ref('')

// 乘客与选座信息
const passengersList = ref<any[]>([])
const selectedPassengers = ref<any[]>([])
const seatAssignments = ref<any[]>([])
// 选座费用合计（机票座位费）
const seatFeeTotal = computed(() => {
  try {
    return seatAssignments.value.reduce((sum: number, a: any) => {
      const p = normalizePrice(a?.price ?? 0)
      return sum + p
    }, 0)
  } catch (e) {
    return 0
  }
})
// 机票基础价（选中航班）
const ticketBasePrice = computed(() => {
  return normalizePrice(selectedFlight.value?.price)
})
// 机票总价 = 机票基础价 + 座位费
const ticketTotalPrice = computed(() => {
  return Number((ticketBasePrice.value + seatFeeTotal.value).toFixed(2))
})

// 状态
const submitting = ref(false)
const errorMessage = ref('')

// 解析航线，兼容多种箭头/分隔符，将箭头前视为出发，箭头后视为到达
const parseRouteParts = (route: string) => {
  if (!route) return { from: '', to: '' }
  // 支持常见的箭头与分隔符：→、⇀、↦、->、至、—、–、» 等
  const separatorRegex = /(?:→|⇀|↦|->|<-|<->|—|–|至|»|—>|⇒|→)/g
  // 如果能按箭头/分隔符拆分则优先使用
  const rawParts = String(route).split(separatorRegex).map(s => s.trim()).filter(Boolean)
  if (rawParts.length >= 2) {
    return { from: rawParts[0], to: rawParts[1] }
  }
  // 兜底：尝试按空格或斜杠等分隔
  const fallbackParts = String(route).split(/[\/\-\u2014\s]+/).map(s => s.trim()).filter(Boolean)
  return {
    from: fallbackParts[0] || '',
    to: fallbackParts[1] || fallbackParts[0] || ''
  }
}
// 格式化时间（仅保留时间部分，用于航班列表）
const formatTime = (time: string) => {
  if (!time) return '--:--'
  if (time.includes(':')) return time
  return time
}

// 格式化日期时间（用于原订单起飞时间显示，与订单列表保持一致）
const formatDateTime = (dateTime: string | Date) => {
  if (!dateTime) return ''

  if (typeof dateTime === 'string') {
    let dateStr = dateTime.trim()

    if (dateStr.endsWith('Z')) {
      dateStr = dateStr.slice(0, -1)
    }
    const timezoneMatch = dateStr.match(/([+-]\d{2}:\d{2})$/)
    if (timezoneMatch) {
      dateStr = dateStr.slice(0, -timezoneMatch[0].length)
    }

    dateStr = dateStr.replace('T', ' ')

    const match = dateStr.match(/^(\d{4}-\d{2}-\d{2})\s+(\d{2}:\d{2})(?::\d{2})?/)
    if (match) {
      const [, datePart, timePart] = match
      return `${datePart} ${timePart}`
    }

    return dateStr
  } else {
    const year = dateTime.getFullYear()
    const month = String(dateTime.getMonth() + 1).padStart(2, '0')
    const day = String(dateTime.getDate()).padStart(2, '0')
    const hours = String(dateTime.getHours()).padStart(2, '0')
    const minutes = String(dateTime.getMinutes()).padStart(2, '0')
    return `${year}-${month}-${day} ${hours}:${minutes}`
  }
}

const computeFeeFromPrice = (price: number | string | undefined | null) => {
  if (price === undefined || price === null) return 0
  const numeric =
    typeof price === 'number'
      ? price
      : parseFloat(String(price).replace(/[^0-9.]/g, ''))
  if (isNaN(numeric) || numeric <= 0) return 0
  return Number((numeric * (feePercent.value / 100)).toFixed(2))
}

const normalizePrice = (price: any): number => {
  if (price === undefined || price === null) return 0
  if (typeof price === 'number') return price
  const n = parseFloat(String(price).replace(/[^0-9.]/g, ''))
  return isNaN(n) ? 0 : n
}

// 计算动态手续费百分比
const calculateDynamicFeePercent = (departureTimeRaw: string | Date | undefined): number => {
  if (!departureTimeRaw) return 5 // 默认兜底
  
  const now = new Date()
  let departureDate: Date

  if (typeof departureTimeRaw === 'string') {
    // 尝试解析日期字符串
    let dStr = departureTimeRaw.trim()
    // 简单处理兼容性，假设是标准格式或 ISO
    // 若包含空格且没有T，替换为空格以便 Date 解析（部分环境）
    // 但通常 new Date("2025-01-01 12:00") 在现代浏览器 workable
    // 最好尝试转 ISO
    if (dStr.includes(' ') && !dStr.includes('T')) {
      dStr = dStr.replace(' ', 'T')
    }
    departureDate = new Date(dStr)
  } else {
    departureDate = departureTimeRaw
  }

  if (isNaN(departureDate.getTime())) return 5

  const diffMs = departureDate.getTime() - now.getTime()
  // 转换为天数
  const diffDays = diffMs / (1000 * 60 * 60 * 24)

  if (diffDays >= 7) {
    return 5
  } else if (diffDays >= 2) {
    return 10
  } else {
    // 两天之内（包括过期）
    return 15
  }
}

// 加载订单信息
const loadOrderInfo = async () => {
  const id = (route.params.id as string) || (route.query.orderId as string)
  if (!id) {
    errorMessage.value = '订单ID不存在'
    return
  }
  
  orderId.value = id
  
  try {
    // 根据角色使用不同的API
    const isAdmin = store.userState.role === 'admin'
    const apiClient = isAdmin ? orderManagementApi : orderApi
    
    // 获取订单列表并找到对应订单
    const result: any = await apiClient.getOrders({ page: 0, size: 1000 })
    const orders = result?.orders || result?.data?.orders || result?.data || []
    
    // 仅使用严格匹配，避免相似订单号误匹配
    const targetIdStr = String(id)
    let order = orders.find((o: any) => {
      const orderIdStr = String(o.id ?? o.orderId ?? '')
      const orderNoStr = String(o.orderNo ?? '')
      return orderIdStr === targetIdStr || orderNoStr === targetIdStr
    })

    // 若列表未找到，尝试按ID单独查询（兼容后端分页或筛选）
    if (!order) {
      if (isAdmin) {
        const detail = await apiClient.getOrderById(targetIdStr)
        order = detail?.data || detail || null
      } else if (apiClient.getOrderDetail) {
        const detail = await apiClient.getOrderDetail(targetIdStr)
        order = detail?.data || detail || null
      }
    }
    
    if (order) {
      orderInfo.value = order
      // 预填搜索条件
      const parts = parseRouteParts(order.route || '')
      searchParams.departure = parts.from || ''
      searchParams.destination = parts.to || ''
      
      // 根据原订单价格和动态计算的手续费百分比计算默认改签手续费
      const originalPrice = normalizePrice(order.amount ?? order.totalAmount)
      
      // 动态计算手续费比例
      const depTime = order.departureTime || order.time || order.schedDepTime
      if (depTime) {
        feePercent.value = calculateDynamicFeePercent(depTime)
      }
      
      changeFee.value = computeFeeFromPrice(originalPrice)
    } else {
      errorMessage.value = '未找到该订单信息，请确认订单ID是否正确'
    }
  } catch (error: any) {
    console.error('加载订单信息失败:', error)
    errorMessage.value = error?.message || '加载订单信息失败，请稍后重试'
  }
}

// 搜索航班
const handleSearchFlights = async () => {
  if (!searchParams.departure || !searchParams.destination || !searchParams.date) {
    errorMessage.value = '请先填写出发城市、到达城市和出发日期'
    return
  }
  
  errorMessage.value = ''
  searchLoading.value = true
  hasSearched.value = true
  
  try {
    const params = {
      departure: searchParams.departure,
      destination: searchParams.destination,
      date: searchParams.date,
      passengers: Number(searchParams.passengers || '1')
    }
    
    const result: any = await flightApi.searchFlights(params)
    const flights = result?.flights || result || []
    searchResults.value = Array.isArray(flights) ? flights : []
    
    if (!searchResults.value.length) {
      errorMessage.value = '未查询到符合条件的航班，请调整条件重试'
    }
  } catch (error: any) {
    console.error('搜索航班失败:', error)
    errorMessage.value = error?.message || '搜索航班失败，请稍后重试'
    searchResults.value = []
  } finally {
    searchLoading.value = false
  }
}

// 选择航班
const selectFlight = (flight: any) => {
  selectedFlight.value = flight
  // 自动计算票价差额（如果有原订单价格）
  if (flight?.price !== undefined && flight?.price !== null) {
    const originalPriceRaw =
      orderInfo.value?.amount ??
      orderInfo.value?.totalAmount ??
      orderInfo.value?.price ??
      orderInfo.value?.ticketPrice
    const newPrice = normalizePrice(flight.price)
    if (originalPriceRaw !== undefined && originalPriceRaw !== null) {
      const originalPrice = normalizePrice(originalPriceRaw)
      priceDiff.value = newPrice - originalPrice // 正数需补，负数应退
    } else {
      // 没有原价时，只能把新票价当作差价（退补由运营再核算）
      priceDiff.value = newPrice
    }
  }
}

// 返回
const handleGoBack = () => {
  router.back()
}

// 打开选座页面（在新窗口/路由），并保存返回路径与航班信息到 sessionStorage
const openSeatSelection = () => {
  if (!selectedFlight.value) {
    errorMessage.value = '请先选择新的航班再选座'
    return
  }
  const flightId = selectedFlight.value.id || selectedFlight.value.flightNumber || ''
  // 保存用于座位页面展示的航班信息
  try {
    // 保存必要的上下文以便选座返回后恢复（不改变原有逻辑，只是新增保存）
    sessionStorage.setItem(`flight_${flightId}`, JSON.stringify(selectedFlight.value))
    // 保存当前搜索条件（包含用户输入的出发/到达/日期）
    try {
      sessionStorage.setItem('rebook_searchParams', JSON.stringify({
        departure: searchParams.departure,
        destination: searchParams.destination,
        date: searchParams.date,
        passengers: searchParams.passengers
      }))
      // 保存当前搜索结果（如果有）
      sessionStorage.setItem('rebook_searchResults', JSON.stringify(searchResults.value || []))
      // 保存乘客列表（以保留用户填写的电话号码）
      sessionStorage.setItem('rebook_passengers', JSON.stringify(passengersList.value || []))
    } catch (e) {
      console.warn('保存改签上下文到 sessionStorage 失败:', e)
    }
    // 记录当前用于恢复的 flightId，确保返回时能定位到已选航班
    sessionStorage.setItem('flightId', String(flightId))
    // 返回页面，用于座位选择后返回
    sessionStorage.setItem('seatSelectionReturnPath', route.fullPath)
    // 传递需要选择座位的人数（与已选乘客数量保持一致）
    const passengersCount = selectedPassengers.value.length || 1
    router.push({
      path: '/portal/passengers/seat-selection',
      query: { flightId: String(flightId), passengers: String(passengersCount) }
    })
  } catch (e) {
    console.error('打开选座失败:', e)
    errorMessage.value = '打开选座页面失败，请重试'
  }
}

// 从 sessionStorage 恢复选座信息并映射到选中的乘客
const restoreSeatSelectionFromSession = () => {
  try {
    // 尝试恢复改签页面的搜索上下文（如果来自选座页）
    const savedSearchParamsStr = sessionStorage.getItem('rebook_searchParams')
    const savedSearchResultsStr = sessionStorage.getItem('rebook_searchResults')
    const savedPassengersStr = sessionStorage.getItem('rebook_passengers')
    try {
      if (savedSearchParamsStr) {
        const parsedParams = JSON.parse(savedSearchParamsStr)
        // 只有在当前页面还未主动搜索或没有结果时才恢复，避免覆盖用户新的操作
        if (!hasSearched.value || (Array.isArray(searchResults.value) && searchResults.value.length === 0)) {
          searchParams.departure = parsedParams.departure || searchParams.departure
          searchParams.destination = parsedParams.destination || searchParams.destination
          searchParams.date = parsedParams.date || searchParams.date
          searchParams.passengers = parsedParams.passengers || searchParams.passengers
          hasSearched.value = true
        }
      }
      if (savedSearchResultsStr) {
        const parsedResults = JSON.parse(savedSearchResultsStr || '[]')
        if ((!searchResults.value || searchResults.value.length === 0) && Array.isArray(parsedResults)) {
          searchResults.value = parsedResults
        }
      }
      if (savedPassengersStr) {
        const parsedPassengers = JSON.parse(savedPassengersStr || '[]')
        if (Array.isArray(parsedPassengers) && parsedPassengers.length) {
          // 将存储的电话号码合并回当前乘客列表（按 id 或 name 匹配）
          parsedPassengers.forEach((savedP: any) => {
            const matchIdx = passengersList.value.findIndex((pp: any) => {
              if (!pp) return false
              if (savedP.id && (pp.id === savedP.id || pp.passengerId === savedP.id)) return true
              if (savedP.name && (pp.name === savedP.name || pp.passengerName === savedP.name)) return true
              return false
            })
            if (matchIdx > -1) {
              // 覆盖电话号码字段以还原用户填写的值
              passengersList.value[matchIdx].phone = savedP.phone || passengersList.value[matchIdx].phone
            }
          })
        }
      }
    } catch (e) {
      console.warn('解析 rebook 上下文失败:', e)
    }
    const selectedSeatsStr = sessionStorage.getItem('selectedSeats')
    const seatDetailsStr = sessionStorage.getItem('seatDetails')
    const flightId = sessionStorage.getItem('flightId') || ''
    if (!selectedSeatsStr || !seatDetailsStr) return
    const selectedSeatsArr = JSON.parse(selectedSeatsStr || '[]')
    const seatDetailsArr = JSON.parse(seatDetailsStr || '[]')
    // 将座位分配映射到已选乘客（按顺序）
    seatAssignments.value = seatDetailsArr.map((d: any, i: number) => {
      const passenger = selectedPassengers.value[i] || { name: selectedPassengers.value[i]?.name || selectedPassengers.value[i]?.passengerName || '乘客' }
      return {
        passengerId: passenger.id || passenger.passengerId || null,
        passengerName: passenger.name || passenger.passengerName || '',
        seatId: d.seatId,
        seatNumber: d.seatNumber,
        seatLabel: d.seatLabel,
        seatClass: d.seatClass,
        price: d.price
      }
    })
    // 如果当前页面没有选中的航班，但 sessionStorage 中有 flight_{id}，则恢复该航班
    if ((!selectedFlight.value || Object.keys(selectedFlight.value).length === 0) && flightId) {
      const storedFlightStr = sessionStorage.getItem(`flight_${flightId}`)
      if (storedFlightStr) {
        try {
          const parsed = JSON.parse(storedFlightStr)
          selectedFlight.value = parsed
        } catch (e) {
          console.warn('解析存储的航班信息失败:', e)
        }
      }
    }
    // 无论是恢复后的 selectedFlight 还是已存在的 selectedFlight，只要 flightId 匹配则附加座位信息，便于界面显示
    if (selectedFlight.value && flightId) {
      const fid = selectedFlight.value.id || selectedFlight.value.flightNumber || ''
      if (String(fid) === String(flightId)) {
        try {
          selectedFlight.value.selectedSeats = Array.isArray(selectedSeatsArr) ? [...selectedSeatsArr] : []
          selectedFlight.value.seatDetails = Array.isArray(seatDetailsArr) ? [...seatDetailsArr] : []
        } catch (e) {
          // 忽略赋值错误，保持原有逻辑不变
          console.warn('为 selectedFlight 设置座位信息失败:', e)
        }
      }
    }
    // 清理 sessionStorage 中的临时键（保留 flight_... 以便座位页仍可显示）
    sessionStorage.removeItem('selectedSeats')
    sessionStorage.removeItem('seatDetails')
    sessionStorage.removeItem('flightId')
    // 清理改签临时保存的搜索与乘客信息（已恢复）
    sessionStorage.removeItem('rebook_searchParams')
    sessionStorage.removeItem('rebook_searchResults')
    sessionStorage.removeItem('rebook_passengers')
  } catch (e) {
    console.error('恢复选座信息失败:', e)
  }
}

// 提交改签
const handleSubmit = async () => {
  if (!reason.value.trim()) {
    errorMessage.value = '请填写改签原因'
    return
  }
  
  if (!orderInfo.value) {
    errorMessage.value = '订单信息不存在'
    return
  }
  
  errorMessage.value = ''
  submitting.value = true
  
  try {
    // 构建 payload，并确保新航班的起飞时间是「搜索日期 + 航班起飞时间」的合成值
    // 例如：selectedFlight.date = '2025-12-09', selectedFlight.departureTime = '08:00' -> '2025-12-09 08:00:00'
    const selectedDate = selectedFlight.value?.date || searchParams.date || selectedFlight.value?.departureDate || ''
    let depTimeRaw = selectedFlight.value?.departureTime || selectedFlight.value?.schedDepTime || ''
    // 规范化为 HH:mm 或 HH:mm:ss
    if (typeof depTimeRaw === 'string') {
      depTimeRaw = depTimeRaw.trim()
      // 如果包含空格或 T，尝试提取时间部分
      const tMatch = depTimeRaw.match(/(\d{2}:\d{2}(?::\d{2})?)/)
      if (tMatch) depTimeRaw = tMatch[1]
    } else {
      depTimeRaw = ''
    }
    const depTimeWithSeconds = depTimeRaw && depTimeRaw.length === 5 ? `${depTimeRaw}:00` : depTimeRaw
    // 为后端兼容设置 newFlight 的时间字段（schedDepTime / departureTime / date），并显式提供 newDepartureDate
    const normalizedNewFlight = {
      ...(selectedFlight.value || {}),
      date: selectedDate || selectedFlight.value?.date,
      departureTime: depTimeRaw || selectedFlight.value?.departureTime,
      schedDepTime: depTimeRaw || selectedFlight.value?.schedDepTime,
      // 额外字段，方便后端直接使用（如果后端接受完整字符串）
      departureDateTime: selectedDate && depTimeWithSeconds ? `${selectedDate} ${depTimeWithSeconds}` : undefined
    }

    const payload = {
      orderNo: orderInfo.value.orderNo || orderInfo.value.id,
      route: orderInfo.value.route,
      // 原航班号：从原订单信息中取（例如 CZ9012）
      oldFlightNo: orderInfo.value.flightNo || orderInfo.value.flightNumber,
      changeFee: changeFee.value,
      priceDiff: priceDiff.value,
      reason: reason.value.trim(),
      newFlight: normalizedNewFlight,
      // 传递被改签的乘客和座位分配（如果有）
      passengers: selectedPassengers.value.length ? selectedPassengers.value.map((p: any, idx: number) => {
        const assign = seatAssignments.value[idx] || {}
        return {
          passengerId: p.id || p.passengerId || null,
          passengerName: p.name || p.passengerName || '',
          seatId: assign.seatId || null,
          seatNumber: assign.seatNumber || assign.seatLabel || null,
          phone: p.phone || p.phoneNumber || p.mobile || ''
        }
      }) : undefined,
      seatAssignments: seatAssignments.value.length ? seatAssignments.value : undefined,
      // 兼容：有些地方只传订单内部金额字段 amount / totalAmount
      amount: orderInfo.value.amount ?? orderInfo.value.totalAmount,
      // 传递原订单起飞时间，避免后端只能拿到“08:00”这样的时间字符串
      oldDepartureTime: orderInfo.value.departureTime || orderInfo.value.schedDepTime,
      oldDepartureDate: orderInfo.value.departureDate,
      // 新航班日期（searchFlights 返回的 date 字段）
      newDepartureDate: selectedDate || selectedFlight.value?.date
    }
    
    // 如果选择了乘客，确保座位数量与乘客数量匹配（若已选座位）
    if (selectedPassengers.value.length > 0 && seatAssignments.value.length > 0) {
      if (seatAssignments.value.length !== selectedPassengers.value.length) {
        throw new Error('选座数量必须与所选改签乘客数量一致')
      }
    }
    
    // 如果需要支付改签费/差价，先跳转到支付宝支付；否则直接提交改签申请
    const fee = Number(payload.changeFee || 0) + Number(payload.priceDiff || 0)
    if (fee > 0) {
      // 先同步打开一个空白窗口，避免浏览器把异步打开的窗口当作弹窗拦截
      const payWindow = window.open('about:blank', '_blank')
      try {
        const html = await orderApi.payReschedule({
          orderNo: payload.orderNo,
          changeFee: payload.changeFee,
          priceDiff: payload.priceDiff,
          reason: payload.reason,
          newFlight: payload.newFlight,
          passengers: payload.passengers,
          seatAssignments: payload.seatAssignments
        })
        // 将支付宝返回的 HTML 写入刚打开的窗口（表单会自动提交）
        if (payWindow && !payWindow.closed) {
          payWindow.document.open()
          payWindow.document.write(html)
          payWindow.document.close()
        } else {
          // 窗口被阻止或关闭，回退到在当前窗口打开支付页面
          window.document.open()
          window.document.write(html)
          window.document.close()
        }
      } catch (e) {
        // 如果请求失败，关闭预打开的窗口（如果存在），并抛出错误
        if (payWindow && !payWindow.closed) try { payWindow.close() } catch {}
        throw e
      }
    } else {
      // 无需支付，直接提交改签申请（后端将创建改签申请记录）
      await orderApi.requestReschedule(payload)
    }
    
    // 改签成功，返回订单列表
    alert(`改签申请已提交，订单号：${payload.orderNo}`)
    router.push('/portal/orders')
  } catch (error: any) {
    console.error('改签失败:', error)
    errorMessage.value = error?.message || '改签失败，请重试'
  } finally {
    submitting.value = false
  }
}

onMounted(async () => {
  loadFeeSettings()
  await loadOrderInfo()

  // 在订单加载完成后准备乘客列表与恢复可能的选座
  try {
    if (orderInfo.value) {
      if (Array.isArray(orderInfo.value.passengers) && orderInfo.value.passengers.length) {
        passengersList.value = orderInfo.value.passengers.map((p: any) => ({ ...p }))
      } else if (Array.isArray(orderInfo.value.tickets) && orderInfo.value.tickets.length) {
        passengersList.value = orderInfo.value.tickets.map((t: any) => ({
          id: t.passenger_id || t.passengerId || null,
          name: t.passenger_name || t.passengerName || t.name || ''
        }))
      } else if (orderInfo.value.passenger_name || orderInfo.value.passengerName) {
        passengersList.value = [{
          id: orderInfo.value.passenger_id || orderInfo.value.passengerId || null,
          name: orderInfo.value.passenger_name || orderInfo.value.passengerName || ''
        }]
      }
    }

    // 若仍未找到乘客列表，尝试读取用户的常用乘客（默认/常用）
    if (!passengersList.value || passengersList.value.length === 0) {
      try {
        const currentUser = apiUtils.getCurrentUser()
        if (currentUser && currentUser.id) {
          const fps = await frequentPassengerApi.getFrequentPassengers(typeof currentUser.id === 'string' ? parseInt(currentUser.id, 10) : currentUser.id)
          if (Array.isArray(fps) && fps.length) {
            passengersList.value = fps.map((p: any) => ({
              id: p.id,
              name: p.name || p.passengerName || ''
            }))
          } else {
            // 若没有常用乘客，使用当前登录用户作为候选
            passengersList.value = [{ id: currentUser.id, name: currentUser.realName || currentUser.username || '' }]
          }
        }
      } catch (e) {
        console.warn('尝试获取常用乘客失败:', e)
      }
    }

    // 自动预选第一个乘客（若有）
    if (passengersList.value && passengersList.value.length === 1) {
      selectedPassengers.value = [passengersList.value[0]]
    }
  } catch (e) {
    console.error('初始化乘客列表失败:', e)
  }

  // 尝试恢复选座（如果用户刚从选座页返回）
  restoreSeatSelectionFromSession()
})

// 监听路由变化（例如从选座页返回）并尝试恢复选座信息
watch(() => route.fullPath, (newPath, oldPath) => {
  try {
    restoreSeatSelectionFromSession()
  } catch (e) {
    // 忽略恢复错误，保持原有逻辑不变
    console.warn('路由变化时恢复选座失败:', e)
  }
})
</script>

<style scoped>
.rebooking-page {
  padding: 24px;
  max-width: 1400px;
  margin: 0 auto;
}

.page-header-section {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  margin-bottom: 24px;
}

.back-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  background: rgba(148, 163, 184, 0.1);
  border: 1px solid rgba(148, 163, 184, 0.3);
  border-radius: 8px;
  color: #e5e7eb;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.2s;
}

.back-btn:hover {
  background: rgba(148, 163, 184, 0.2);
  border-color: rgba(148, 163, 184, 0.5);
}

.back-icon {
  font-size: 18px;
  font-weight: bold;
}

.header-content {
  flex: 1;
}

.breadcrumb {
  margin-bottom: 8px;
  font-size: 14px;
  color: rgba(148, 163, 184, 0.9);
}

.breadcrumb-separator {
  margin: 0 6px;
}

.page-title {
  font-size: 28px;
  margin: 0 0 8px 0;
  color: #e5e7eb;
}

.page-description {
  font-size: 14px;
  color: rgba(148, 163, 184, 0.9);
  margin: 0;
}

.glass-card {
  background: rgba(15, 23, 42, 0.9);
  border-radius: 16px;
  padding: 24px;
  border: 1px solid rgba(148, 163, 184, 0.4);
  box-shadow: 0 20px 40px rgba(15, 23, 42, 0.9);
  margin-bottom: 24px;
}

.section-title {
  font-size: 18px;
  margin: 0 0 20px 0;
  color: #e5e7eb;
  font-weight: 600;
}

/* 订单信息卡片 */
.order-info-card .info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.info-label {
  font-size: 13px;
  color: rgba(148, 163, 184, 0.9);
}

.info-value {
  font-size: 15px;
  color: #e5e7eb;
  font-weight: 500;
}

.status-tag {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.status-pending {
  background: rgba(251, 191, 36, 0.2);
  color: #fbbf24;
}

.status-confirmed {
  background: rgba(34, 197, 94, 0.2);
  color: #22c55e;
}

.status-completed {
  background: rgba(59, 130, 246, 0.2);
  color: #3b82f6;
}

.status-cancelled {
  background: rgba(239, 68, 68, 0.2);
  color: #ef4444;
}

/* 费用卡片 */
.fee-description {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  background: rgba(59, 130, 246, 0.1);
  border: 1px solid rgba(59, 130, 246, 0.3);
  border-radius: 8px;
  margin-bottom: 20px;
  font-size: 13px;
  color: rgba(148, 163, 184, 0.9);
}

.info-icon {
  font-size: 16px;
}

.fee-card .fee-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
}

.fee-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.fee-label {
  font-size: 14px;
  color: rgba(148, 163, 184, 0.9);
}

.fee-input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
}

.fee-input {
  padding: 10px 14px;
  padding-right: 32px;
  border-radius: 10px;
  border: 1px solid rgba(148, 163, 184, 0.5);
  background: rgba(15, 23, 42, 0.9);
  color: #e5e7eb;
  font-size: 14px;
  width: 100%;
}

.fee-input:focus {
  outline: none;
  border-color: #3b82f6;
}

.fee-input:disabled,
.fee-input-disabled {
  background: rgba(15, 23, 42, 0.6);
  color: rgba(148, 163, 184, 0.7);
  cursor: not-allowed;
  border-color: rgba(148, 163, 184, 0.3);
}

.fee-unit {
  position: absolute;
  right: 14px;
  font-size: 14px;
  color: rgba(148, 163, 184, 0.7);
  pointer-events: none;
  user-select: none;
}

.fee-calc-hint {
  font-size: 12px;
  font-weight: 400;
  color: rgba(148, 163, 184, 0.7);
  margin-left: 8px;
}

.fee-auto-note {
  margin-top: 6px;
  font-size: 12px;
  color: rgba(59, 130, 246, 0.8);
  font-style: italic;
}

/* 搜索卡片 */
.search-section {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.search-row {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
  align-items: flex-end;
}

.search-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
  flex: 1 1 180px;
}

.search-item.small {
  flex: 0 0 120px;
}

.search-item label {
  font-size: 13px;
  color: rgba(148, 163, 184, 0.9);
}

.search-input,
.search-select {
  padding: 10px 14px;
  border-radius: 10px;
  border: 1px solid rgba(148, 163, 184, 0.5);
  background: rgba(15, 23, 42, 0.9);
  color: #e5e7eb;
  font-size: 14px;
}

.search-input:focus,
.search-select:focus {
  outline: none;
  border-color: #3b82f6;
}

.search-btn {
  padding: 10px 24px;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  border: none;
  background: linear-gradient(135deg, #3b82f6, #1d4ed8);
  color: #e5f2ff;
  transition: all 0.2s;
}

.search-btn:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.4);
}

.search-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.hint-row {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px;
  background: rgba(59, 130, 246, 0.1);
  border-radius: 8px;
  border: 1px solid rgba(59, 130, 246, 0.3);
}

.pill {
  display: inline-block;
  padding: 4px 10px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.pill-info {
  background: rgba(59, 130, 246, 0.2);
  color: #60a5fa;
}

.pill-success {
  background: rgba(34, 197, 94, 0.2);
  color: #4ade80;
}

.hint-text {
  font-size: 13px;
  color: rgba(148, 163, 184, 0.9);
}

/* 搜索结果卡片 */
.results-container {
  min-height: 200px;
}

.loading-state,
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  color: rgba(148, 163, 184, 0.9);
}

.spinner {
  width: 40px;
  height: 40px;
  border: 3px solid rgba(148, 163, 184, 0.3);
  border-top-color: #3b82f6;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 16px;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.flight-results {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.flight-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border-radius: 12px;
  border: 2px solid rgba(148, 163, 184, 0.3);
  background: rgba(15, 23, 42, 0.6);
  cursor: pointer;
  transition: all 0.2s;
}

.flight-item:hover {
  border-color: rgba(59, 130, 246, 0.5);
  background: rgba(15, 23, 42, 0.8);
}

.flight-item.active {
  border-color: #3b82f6;
  background: rgba(59, 130, 246, 0.1);
}

.flight-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.flight-route {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 500;
  color: #e5e7eb;
}

.city {
  color: #e5e7eb;
}

.arrow {
  color: rgba(148, 163, 184, 0.7);
}

.flight-time {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: rgba(148, 163, 184, 0.9);
}

.separator {
  color: rgba(148, 163, 184, 0.5);
}

.flight-number {
  font-size: 12px;
  color: rgba(148, 163, 184, 0.7);
}

.flight-side {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 8px;
}

.price {
  font-size: 20px;
  font-weight: 600;
  color: #3b82f6;
}

.select-indicator {
  font-size: 12px;
  color: #22c55e;
  font-weight: 500;
}

/* 已选航班卡片 */
.selected-flight-info {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.selected-route {
  display: flex;
  align-items: center;
  gap: 12px;
}

.route-text {
  font-size: 16px;
  font-weight: 500;
  color: #e5e7eb;
}

.selected-meta {
  display: flex;
  gap: 20px;
  flex-wrap: wrap;
  font-size: 14px;
  color: rgba(148, 163, 184, 0.9);
}

.meta-item.price {
  color: #3b82f6;
  font-weight: 600;
}

/* 改签原因卡片 */
.reason-textarea {
  width: 100%;
  padding: 12px;
  border-radius: 10px;
  border: 1px solid rgba(148, 163, 184, 0.5);
  background: rgba(15, 23, 42, 0.9);
  color: #e5e7eb;
  font-size: 14px;
  font-family: inherit;
  resize: vertical;
  min-height: 100px;
}

.reason-textarea:focus {
  outline: none;
  border-color: #3b82f6;
}

/* 错误提示 */
.error-message {
  padding: 12px 16px;
  background: rgba(239, 68, 68, 0.1);
  border: 1px solid rgba(239, 68, 68, 0.3);
  border-radius: 8px;
  color: #f87171;
  font-size: 14px;
  margin-bottom: 24px;
}

/* 底部操作按钮 */
.action-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 24px 0;
  border-top: 1px solid rgba(148, 163, 184, 0.2);
}

.ghost-btn,
.primary-btn {
  padding: 10px 24px;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  border: none;
  transition: all 0.2s;
}

.ghost-btn {
  background: transparent;
  border: 1px solid rgba(148, 163, 184, 0.7);
  color: #e5e7eb;
}

.ghost-btn:hover {
  background: rgba(148, 163, 184, 0.1);
}

.primary-btn {
  background: linear-gradient(135deg, #3b82f6, #1d4ed8);
  color: #e5f2ff;
}

.primary-btn:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.4);
}

.primary-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

@media (max-width: 768px) {
  .rebooking-page {
    padding: 16px;
  }
  
  .page-header-section {
    flex-direction: column;
  }
  
  .search-row {
    flex-direction: column;
  }
  
  .search-item {
    flex: 1 1 100%;
  }
  
  .info-grid,
  .fee-grid {
    grid-template-columns: 1fr;
  }
}
</style>


<template>
  <PassengerLayout>
    <div class="page-container pax-page">
      <div class="breadcrumb">
          <span>首页</span>
          <span class="breadcrumb-separator">/</span>
          <span>行程中心</span>
        </div>

      <header class="page-header">
        <div>
          <p class="page-label">旅客 · 全旅程关怀</p>
          <h1>我的行程中心</h1>
          <p>面向乘客端，围绕实时航班查询、订单管理、在线值机与退改服务进行模块化布局。</p>
        </div>
        <div class="page-actions">
          <button class="ghost-btn">联系客服</button>
          <button class="primary-btn">新增行程</button>
        </div>
      </header>

      <!-- 搜索航班面板 - 根据tab参数显示 -->
      <section v-if="activeTab === 'search'" class="glass-card search-panel" ref="searchPanelRef">
        <div class="panel-header">
          <div>
            <p class="panel-label">智能匹配</p>
            <h2>航班搜索</h2>
          </div>
        </div>

        <form @submit.prevent="handleSearchSubmit" class="search-form">
          <div class="form-row">
            <div class="form-group">
              <label for="departure">出发机场</label>
              <input
                id="departure"
                v-model="searchForm.departure"
                type="text"
                placeholder="例如：北京"
                required
              />
            </div>

            <div class="form-group">
              <label for="destination">到达机场</label>
              <input
                id="destination"
                v-model="searchForm.destination"
                type="text"
                placeholder="例如：上海"
                required
              />
            </div>
          </div>

          <div class="form-row">
            <div class="form-group">
              <label for="date">出发日期</label>
              <input id="date" v-model="searchForm.date" type="date" required />
            </div>
       
            <div class="form-group">
              <label for="cabinClass">舱位等级</label>
              <select id="cabinClass" v-model="searchForm.cabinClass">
                <option value="">全部舱位</option>
                <option value="economy">经济舱</option>
                <option value="business">商务舱</option>
                <option value="first">头等舱</option>
              </select>
            </div>
          </div>

          <button type="submit" class="search-btn" :disabled="searchLoading">
            {{ searchLoading ? '搜索中...' : '搜索航班' }}
          </button>
        </form>

        <!-- 搜索结果 -->
        <div v-if="searchResults.length > 0" class="search-results">
          <h3>搜索结果</h3>
          <div class="results-list">
            <div
              v-for="flight in searchResults"
              :key="flight.id"
              class="result-item"
            >
              <div class="result-info">
                <h4>{{ flight.flightNumber }} · {{ flight.airline }}</h4>
                <p>{{ flight.departure }} → {{ flight.destination }}</p>
                <p>{{ formatTime(flight.departureTime || '') }} - {{ formatTime(flight.arrivalTime || '') }}</p>
                <p v-if="flight.duration" class="flight-duration">{{ flight.duration }}</p>
                <p v-if="flight.availableSeats !== undefined" class="flight-seats">
                  剩余座位: {{ flight.availableSeats }}
                  <span v-if="searchForm.cabinClass" class="cabin-hint">
                    ({{ getCabinClassText(searchForm.cabinClass) }})
                  </span>
                </p>
                <p v-else-if="flight.seats !== undefined" class="flight-seats">剩余座位: {{ flight.seats }}</p>
              </div>
              <div class="result-actions">
                <div class="price-info">
                  <span class="result-price">¥{{ flight.price }}</span>
                  <span v-if="flight.seatClass" class="cabin-badge">{{ getCabinClassText(flight.seatClass) }}</span>
                </div>
                <div class="action-buttons">

                  <button 
                    class="primary-btn" 
                    type="button"
                    @click.stop="handleBookFlight(flight.id)"
                    @mousedown.stop
                  >
                    预订
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- 座位选择模态框 -->
      <Teleport to="body">
        <Transition name="modal-fade">
          <div v-if="seatSelectionModal.visible" class="seat-modal-overlay" @click.self="closeSeatModal">
            <div class="seat-modal">
          <div class="seat-modal-header">
            <div>
              <h3>选择座位</h3>
              <p>{{ seatSelectionModal.flight?.flightNumber }} · {{ seatSelectionModal.flight?.departure }} → {{ seatSelectionModal.flight?.destination }}</p>
            </div>
            <button class="close-btn" @click="closeSeatModal">×</button>
          </div>
          
          <div class="seat-modal-body">
            <!-- 座位图例 -->
            <div class="seat-legend">
              <div class="legend-item">
                <span class="legend-color available"></span>
                <span>可用</span>
              </div>
              <div class="legend-item">
                <span class="legend-color selected"></span>
                <span>已选</span>
              </div>
              <div class="legend-item">
                <span class="legend-color occupied"></span>
                <span>已占用</span>
              </div>
              <div class="legend-item">
                <span class="legend-color premium"></span>
                <span>商务舱</span>
              </div>
            </div>

            <!-- 机舱座位图 -->
            <div class="cabin-layout">
              <!-- 商务舱 -->
              <div v-if="businessSeats.length > 0" class="cabin-section business-class">
                <h4>商务舱</h4>
                <div class="seat-grid business-grid">
                  <div
                    v-for="seat in businessSeats"
                    :key="seat.id"
                    :class="['seat', seat.status, { 'selected': selectedSeats.includes(seat.id) }]"
                    @click="handleSeatClick(seat)"
                    :title="`${seat.row}${seat.letter} - ${getSeatStatusText(seat.status)}`"
                  >
                    <span class="seat-label">{{ seat.row }}{{ seat.letter }}</span>
                    <span v-if="seat.status === 'premium'" class="seat-badge">商务</span>
                  </div>
                </div>
              </div>

              <!-- 经济舱 -->
              <div class="cabin-section economy-class">
                <h4>经济舱</h4>
                <div class="seat-grid economy-grid">
                  <div
                    v-for="seat in economySeats"
                    :key="seat.id"
                    :class="['seat', seat.status, { 'selected': selectedSeats.includes(seat.id) }]"
                    @click="handleSeatClick(seat)"
                    :title="`${seat.row}${seat.letter} - ${getSeatStatusText(seat.status)}`"
                  >
                    <span class="seat-label">{{ seat.row }}{{ seat.letter }}</span>
                  </div>
                </div>
              </div>
            </div>

            <!-- 已选座位信息 -->
            <div v-if="selectedSeats.length > 0" class="selected-seats-info">
              <h4>已选座位 ({{ selectedSeats.length }}/{{ parseInt(searchForm.passengers) }})</h4>
              <div class="selected-seats-list">
                <div
                  v-for="seatId in selectedSeats"
                  :key="seatId"
                  class="selected-seat-item"
                >
                  <span>{{ getSeatLabel(seatId) }}</span>
                  <button class="remove-btn" @click="removeSeat(seatId)">移除</button>
                </div>
              </div>
            </div>
          </div>

          <div class="seat-modal-footer">
            <button class="ghost-btn" @click="closeSeatModal">取消</button>
            <button 
              class="primary-btn" 
              @click="confirmSeatSelection"
              :disabled="selectedSeats.length !== seatSelectionRequiredCount"
            >
              确认选择 ({{ selectedSeats.length }}/{{ searchForm.passengers }})
            </button>
          </div>
          </div>
        </div>
        </Transition>
      </Teleport>

      <!-- 预订确认模态框 -->
      <Teleport to="body">
        <Transition name="modal-fade">
        <div v-if="bookingModal.visible" class="booking-modal-overlay" @click.self="closeBookingModal">
          <div class="booking-modal">
            <div class="booking-modal-header">
              <div>
                <h3>确认预订信息</h3>
                <p v-if="bookingModal.flight">
                  {{ bookingModal.flight.flightNumber }} · {{ bookingModal.flight.departure }} → {{ bookingModal.flight.destination }}
                </p>
              </div>
              <button class="close-btn" @click="closeBookingModal">×</button>
            </div>
            
            <div class="booking-modal-body">
              <!-- 航班信息 -->
              <div v-if="bookingModal.flight" class="booking-section">
                <h4>航班信息</h4>
                <div class="flight-info-grid">
                  <div class="info-item">
                    <span class="info-label">航班号</span>
                    <span class="info-value">{{ bookingModal.flight.flightNumber }}</span>
                  </div>
                  <div class="info-item">
                    <span class="info-label">航空公司</span>
                    <span class="info-value">{{ bookingModal.flight.airline }}</span>
                  </div>
                  <div class="info-item">
                    <span class="info-label">出发时间</span>
                    <span class="info-value">{{ bookingModal.flight.departureTime }}</span>
                  </div>
                  <div class="info-item">
                    <span class="info-label">到达时间</span>
                    <span class="info-value">{{ bookingModal.flight.arrivalTime }}</span>
                  </div>
                  <div class="info-item">
                    <span class="info-label">出发机场</span>
                    <span class="info-value">{{ bookingModal.flight.departure }}</span>
                  </div>
                  <div class="info-item">
                    <span class="info-label">到达机场</span>
                    <span class="info-value">{{ bookingModal.flight.destination }}</span>
                  </div>
                </div>
              </div>

              <!-- 座位信息 -->
              <div v-if="(bookingModal.flight?.selectedSeats && bookingModal.flight.selectedSeats.length > 0) || (bookingModal.flight?.seatDetails && bookingModal.flight.seatDetails.length > 0)" class="booking-section">
                <h4>已选座位</h4>
                <div class="seats-list">
                  <div v-for="(seatDetail, index) in (bookingModal.flight?.seatDetails || [])" :key="index" class="seat-detail-item">
                    <span class="seat-tag">{{ seatDetail.seatLabel || seatDetail.seatNumber }}</span>
                    <span class="seat-price" v-if="seatDetail.price > 0">+¥{{ seatDetail.price }}</span>
                  </div>
                  <span v-if="!bookingModal.flight?.seatDetails && bookingModal.flight?.selectedSeats" 
                        v-for="(seat, index) in bookingModal.flight.selectedSeats" 
                        :key="seat" 
                        class="seat-tag">
                    {{ getSeatLabel(seat) }}
                  </span>
                </div>
              </div>

              <!-- 常用乘客快速选择 -->
              <div class="booking-section">
                <h4>快速选择乘客</h4>
                <p class="section-desc">从常用乘客中快速选择，或为其他人购票</p>
                <FrequentPassengers
                  :hide-myself-tab="true"
                  @select="handleSelectFrequentPassenger"
                />
              </div>

              <!-- 乘客信息 - 使用 PassengerManager 组件 -->
              <div class="booking-section">
                <h4>乘客信息</h4>
                <PassengerManager
                  ref="passengerManagerRef"
                  v-model="bookingForm.passengers"
                  :max-passengers="parseInt(searchForm.passengers)"
                  :show-validation-summary="true"
                  @validate="passengerValidationStatus = $event"
                />
              </div>

              <!-- 价格信息 -->
              <div class="booking-section price-section">
                <h4>费用明细</h4>
                <div class="price-breakdown">
                  <div class="price-item">
                    <span>基础票价 × {{ bookingForm.passengers.length }}</span>
                    <span>¥{{ (bookingModal.flight?.price || 0) * bookingForm.passengers.length }}</span>
                  </div>
                  <div v-if="bookingModal.flight?.seatDetails && bookingModal.flight.seatDetails.length > 0" class="price-item">
                    <span>座位选择费</span>
                    <span>¥{{ bookingModal.flight.seatDetails.reduce((sum: number, s: any) => sum + (s.price || 0), 0) }}</span>
                  </div>
                  <div class="price-item total">
                    <span>总计</span>
                    <span class="total-price">¥{{ calculateTotalPrice }}</span>
                  </div>
                </div>
              </div>
            </div>

            <div class="booking-modal-footer">
              <button class="ghost-btn" @click="closeBookingModal" :disabled="bookingModal.loading">取消</button>
              <button 
                class="ghost-btn" 
                @click="openSeatSelectionFromBooking"
                :disabled="!passengerValidationStatus || bookingModal.loading"
                title="请先填写并通过乘客信息验证"
              >
                选择座位
              </button>
              <button 
                class="primary-btn" 
                @click="submitBooking"
                :disabled="bookingModal.loading"
              >
                {{ bookingModal.loading ? '提交中...' : `确认预订 (¥${calculateTotalPrice})` }}
              </button>
            </div>
          </div>
        </div>
      </Transition>
      </Teleport>

      <section v-if="activeTab !== 'search'" class="glass-card layout-map">
        <h2>乘客组件布局规划</h2>
        <div class="layout-grid">
          <article v-for="block in passengerLayout" :key="block.module" class="layout-item">
            <span class="layout-position">{{ block.position }}</span>
            <h3>{{ block.module }}</h3>
            <p>{{ block.desc }}</p>
            <ul>
              <li v-for="tip in block.tips" :key="tip">{{ tip }}</li>
            </ul>
          </article>
        </div>
      </section>

   

      
    </div>
  </PassengerLayout>
</template>

<script setup lang="ts">
import PassengerLayout from '../components/layout/PassengerLayout.vue';
import PassengerManager from '../components/PassengerManager.vue';
import FrequentPassengers from '../components/FrequentPassengers.vue';
import { computed, ref, onMounted, watch, nextTick, reactive, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { passengerApi, flightApi, orderApi, seatApi } from '../services/api'
import store from '../services/store'
import { formatTime, formatDateTime } from '../utils/dateFormat'

const router = useRouter()
const route = useRoute()

// 根据路由参数决定显示的标签页
const activeTab = computed(() => route.query.tab as string || 'overview')

// 搜索表单
const searchForm = reactive({
  departure: '',
  destination: '',
  date: '',
  passengers: '1',
  cabinClass: ''
})

// 搜索结果
const searchResults = ref<Array<{
  id: string
  flightNumber: string
  airline: string
  departure: string
  destination: string
  departureTime: string
  arrivalTime: string
  price: number
  duration?: string
  seats?: number
  availableSeats?: number
  seatClass?: string
  selectedSeats?: string[]
  seatDetails?: Array<{
    seatId: string
    seatLabel: string
    seatClass: string
    price: number
  }>
}>>([])

const searchLoading = ref(false)
const searchPanelRef = ref<HTMLElement | null>(null)

// 处理搜索提交
const handleSearchSubmit = async () => {
  try {
    searchLoading.value = true
    const result = await flightApi.searchFlights({
      departure: searchForm.departure,
      destination: searchForm.destination,
      date: searchForm.date,
      passengers: parseInt(searchForm.passengers),
      class: searchForm.cabinClass || undefined
    })
    
    if (result?.flights) {
      const flights = result.flights.map((flight: any) => ({
        ...flight,
        selectedSeats: []
      }))
      
      // 如果选择的是"全部舱位"，需要计算所有舱位的余票数之和
      if (!searchForm.cabinClass) {
        for (const flight of flights) {
          try {
            // 查询三个舱位的余票数
            const [economyResult, businessResult, firstResult] = await Promise.all([
              seatApi.getAvailableSeatCount(flight.id, 'economy').catch(() => null),
              seatApi.getAvailableSeatCount(flight.id, 'business').catch(() => null),
              seatApi.getAvailableSeatCount(flight.id, 'first').catch(() => null)
            ])
            
            // 提取余票数：兼容多种返回格式
            // 格式1: { count: xxx }
            // 格式2: { data: { count: xxx } }
            const getCount = (result: any): number => {
              if (!result) return 0
              if (typeof result.count === 'number') return result.count
              if (result.data && typeof result.data.count === 'number') return result.data.count
              return 0
            }
            
            const economyCount = getCount(economyResult)
            const businessCount = getCount(businessResult)
            const firstCount = getCount(firstResult)
            
            // 计算总和并赋值给 availableSeats
            const totalCount = economyCount + businessCount + firstCount
            flight.availableSeats = totalCount
            
            console.log(`航班 ${flight.flightNumber} 余票统计:`, {
              经济舱: economyCount,
              商务舱: businessCount,
              头等舱: firstCount,
              总计: totalCount
            })
          } catch (e) {
            console.warn(`查询航班 ${flight.id} 的余票数失败:`, e)
            // 如果查询失败，保持原有值或使用默认值
            if (flight.availableSeats === undefined) {
              flight.availableSeats = flight.seats || 0
            }
          }
        }
      }
      
      searchResults.value = flights
    } else {
      searchResults.value = []
    }
  } catch (error) {
    handleError(error, '搜索航班失败')
    searchResults.value = []
  } finally {
    searchLoading.value = false
  }
}

// 座位选择相关（与后端 seats 表结构对齐）
interface Seat {
  id: string | number
  seatNumber: string
  row: number
  position: string
  cabinClass?: string
  status: 'available' | 'occupied' | 'reserved' | 'maintenance' | 'selected' | 'premium'
  price?: number
}

const seatSelectionModal = reactive({
  visible: false,
  flight: null as any
})

const allSeats = ref<Seat[]>([])
const selectedSeats = ref<string[]>([])

// 按舱位拆分
const businessSeats = computed(() =>
  allSeats.value.filter(s =>
    s.cabinClass === '商务舱' ||
    s.status === 'premium'
  )
)
const economySeats = computed(() =>
  allSeats.value.filter(s =>
    s.cabinClass === '经济舱' &&
    !businessSeats.value.includes(s)
  )
)

// 将后端座位状态转换为前端状态（可用 / 已占用）
const convertSeatStatus = (status: string | null | undefined, cabinClass?: string): Seat['status'] => {
  // 后端 status 为“可用”或英文 available 或为空，按可用处理
  if (!status || status === '可用' || status === 'available') {
    return 'available'
  }
  // reserved、occupied、maintenance → 已占用
  return 'occupied'
}

// 从后端加载座位图（基于 seats 表）
const generateSeatMap = async (flightId: string | number) => {
  const seats: Seat[] = []

  try {
    // 先尝试为该航班创建座位（如果已经存在，后端会直接忽略）
    try {
      await seatApi.createSeatsForFlight(flightId)
    } catch (e) {
      console.log('创建座位失败或已存在，继续加载实际座位数据')
    }

    const layoutData = await seatApi.getSeatLayoutByFlightId(flightId)
    if (layoutData && layoutData.seats) {
      layoutData.seats.forEach((seat: any) => {
        seats.push({
          id: seat.id,
          seatNumber: seat.seatNumber,
          row: seat.row,
          position: seat.position,
          cabinClass: seat.cabinClass,
          status: convertSeatStatus(seat.status, seat.cabinClass),
          price: seat.price || 0
        })
      })
    }
  } catch (error) {
    console.error('从后端加载座位数据失败，临时使用默认布局:', error)
  }

  // 如果后端没有返回任何座位，退回到简单的默认布局，避免页面空白
  if (seats.length === 0) {
    for (let row = 1; row <= 2; row++) {
      ;['A', 'B', 'D', 'E'].forEach(position => {
        const isOccupied = Math.random() > 0.7
        seats.push({
          id: `${row}${position}`,
          seatNumber: `${row}${position}`,
          row,
          position,
          cabinClass: '商务舱',
          status: isOccupied ? 'occupied' : 'premium',
          price: 200
        })
      })
    }

    for (let row = 10; row <= 30; row++) {
      ;['A', 'B', 'C', 'D', 'E', 'F'].forEach(position => {
        const isOccupied = Math.random() > 0.6
        seats.push({
          id: `${row}${position}`,
          seatNumber: `${row}${position}`,
          row,
          position,
          cabinClass: '经济舱',
          status: isOccupied ? 'occupied' : 'available',
          price: 0
        })
      })
    }
  }

  allSeats.value = seats
  selectedSeats.value = []
}

// 处理座位点击
const handleSeatClick = (seat: Seat) => {
  if (seat.status === 'occupied' || seat.status === 'maintenance') return
  
  const seatId = String(seat.id ?? seat.seatNumber)
  const index = selectedSeats.value.indexOf(seatId)
  const maxSeats = parseInt(searchForm.passengers)
  
  if (index > -1) {
    selectedSeats.value.splice(index, 1)
  } else {
    if (selectedSeats.value.length < maxSeats) {
      selectedSeats.value.push(seatId)
    } else {
      alert(`最多只能选择 ${maxSeats} 个座位`)
    }
  }
}

// 移除座位
const removeSeat = (seatId: string) => {
  const index = selectedSeats.value.indexOf(seatId)
  if (index > -1) {
    selectedSeats.value.splice(index, 1)
  }
}

// 获取座位标签
const getSeatLabel = (seatId: string) => {
  const seat = allSeats.value.find(s => String(s.id) === seatId || s.seatNumber === seatId)
  return seat ? seat.seatNumber : seatId
}

// 获取座位状态文本（严格按照后端状态，可用 / 已占用 / 已选）
const getSeatStatusText = (status: string) => {
  const statusMap: Record<string, string> = {
    available: '可用',
    occupied: '已占用',
    selected: '已选'
  }
  return statusMap[status] || '未知'
}

// 打开座位选择模态框
const handleSelectSeats = async (flight: any) => {
  console.log('handleSelectSeats 被调用', flight)
  try {
    if (!flight) {
      console.error('航班信息为空')
      return
    }
    seatSelectionModal.flight = flight
    seatSelectionModal.visible = true
    await generateSeatMap(flight.id)
    
    // 如果之前已选择座位，恢复选择状态
    if (flight.selectedSeats && flight.selectedSeats.length > 0) {
      selectedSeats.value = [...flight.selectedSeats]
    } else {
      selectedSeats.value = []
    }
    console.log('座位选择模态框已打开', seatSelectionModal.visible)
  } catch (error) {
    console.error('打开座位选择模态框时出错:', error)
    alert('打开座位选择失败，请重试')
  }
}

// 关闭座位选择模态框
const closeSeatModal = () => {
  seatSelectionModal.visible = false
  seatSelectionModal.flight = null
  selectedSeats.value = []
}

// 从预订模态框打开座位选择（先填乘客信息再选座位）
const openSeatSelectionFromBooking = () => {
  if (!bookingModal.flight) return
  if (!passengerValidationStatus.value) {
    alert('请先填写并通过乘客信息验证')
    return
  }
  
  // 保存航班信息到 sessionStorage，供座位选择页面使用
  const flightId = bookingModal.flight.id || Date.now().toString()
  sessionStorage.setItem(`flight_${flightId}`, JSON.stringify(bookingModal.flight))
  
  // 保存返回路径（当前页面路径）
  // 这里直接约定返回到乘客视图的搜索页，并带上 fromSeat=1，方便触发恢复逻辑
  sessionStorage.setItem('seatSelectionReturnPath', '/portal/passengers/view?tab=search&fromSeat=1')
  // 标记：从预订模态框跳转，选完座位后需要自动回到预订模态框
  sessionStorage.setItem('reopenBookingAfterSeat', '1')
  
  // 跳转到座位选择页面
  router.push({
    path: '/portal/passengers/seat-selection',
    query: {
      flightId: flightId,
      cabinClass: bookingForm.passengers[0]?.seatClass || bookingModal.flight.seatClass || '',
      flightNumber: bookingModal.flight.flightNumber,
      departure: bookingModal.flight.departure,
      destination: bookingModal.flight.destination,
      departureTime: bookingModal.flight.departureTime,
      arrivalTime: bookingModal.flight.arrivalTime,
      passengers: bookingForm.passengers.length.toString()
    }
  })
}

// 确认座位选择
const confirmSeatSelection = async () => {
  if (selectedSeats.value.length !== seatSelectionRequiredCount.value) {
    alert(`请选择 ${seatSelectionRequiredCount.value} 个座位`)
    return
  }
  
  // 更新搜索结果中对应航班的座位信息
  if (!seatSelectionModal.flight) return

  // 将选中的座位同步到后端：设置为 occupied
  if (seatSelectionModal.flight.id && selectedSeats.value.length > 0) {
    try {
      await seatApi.updateSeatStatus(seatSelectionModal.flight.id, selectedSeats.value, 'occupied')
    } catch (e) {
      console.error('更新座位状态失败:', e)
      // 不阻塞前端流程，只给出提示
      alert('更新座位状态失败，请稍后在“我的订单”中确认座位信息')
    }
  }
  
  const flightIndex = searchResults.value.findIndex(f => f.id === seatSelectionModal.flight?.id)
  if (flightIndex > -1 && seatSelectionModal.flight) {
    const flight = searchResults.value[flightIndex]
    if (flight) {
      flight.selectedSeats = [...selectedSeats.value]
      // 更新座位详情
      flight.seatDetails = selectedSeats.value.map(seatId => {
        const seat = allSeats.value.find(s => s.id === seatId)
        return {
          seatId,
          seatLabel: getSeatLabel(seatId),
          seatClass: seat?.status === 'premium' ? 'business' : 'economy',
          price: seat?.price || 0
        }
      })
    }
  }
  
  // 显示成功提示
  const seatLabels = selectedSeats.value.map(id => getSeatLabel(id)).join(', ')
  console.log('已选择座位:', seatLabels)
  
  // 如果是在预订模态框中选择座位，则同步到乘客表单
  if (bookingModal.visible && bookingModal.flight && seatSelectionModal.flight && bookingModal.flight.id === seatSelectionModal.flight.id) {
    bookingForm.passengers.forEach((p, i) => {
      const sid = selectedSeats.value[i]
      if (sid) {
        p.seatId = sid
        p.seatLabel = getSeatLabel(sid)
      }
    })
  }
  
  closeSeatModal()
  
  // 使用更友好的提示方式
  setTimeout(() => {
    alert(`已成功选择座位: ${seatLabels}\n点击"预订"按钮继续完成预订。`)
  }, 300)
}

// 获取舱位等级文本
const getCabinClassText = (classType: string) => {
  const classMap: Record<string, string> = {
    economy: '经济舱',
    business: '商务舱',
    first: '头等舱'
  }
  return classMap[classType] || classType
}

// 预订模态框
const bookingModal = reactive({
  visible: false,
  flight: null as any,
  loading: false
})

// 预订表单
const bookingForm = reactive({
  passengers: [] as Array<{
    name: string
    idCard: string
    seatId?: string
    seatLabel?: string
    seatClass: string
    seatPreference: string
    passengerType: string
    phone: string
  }>,
  contactInfo: {
    name: '',
    phone: '',
    email: ''
  }
})

// 乘客验证状态
const passengerValidationStatus = ref(false)

// PassengerManager 组件引用
const passengerManagerRef = ref<InstanceType<typeof PassengerManager> | null>(null)

// 座位选择所需数量
const seatSelectionRequiredCount = computed(() => bookingModal.visible ? bookingForm.passengers.length : parseInt(searchForm.passengers))

// 初始化预订表单
const initBookingForm = (flight: any) => {
  const passengerCount = parseInt(searchForm.passengers)
  bookingForm.passengers = []
  
  for (let i = 0; i < passengerCount; i++) {
    bookingForm.passengers.push({
      name: '',
      idCard: '',
      seatId: flight.selectedSeats?.[i] || undefined,
      seatLabel: flight.seatDetails?.[i]?.seatLabel || undefined,
      seatClass: (flight.seatDetails?.[i]?.seatClass || 'economy') as string,
      seatPreference: 'any',
      passengerType: 'adult',
      phone: ''
    })
  }
  
  // 如果有已选座位，自动分配
  if (flight.selectedSeats && flight.selectedSeats.length > 0) {
    flight.selectedSeats.forEach((seatId: string, index: number) => {
      if (bookingForm.passengers[index]) {
        bookingForm.passengers[index].seatId = seatId
        bookingForm.passengers[index].seatLabel = getSeatLabel(seatId)
      }
    })
  }
  
  bookingForm.contactInfo = {
    name: '',
    phone: '',
    email: ''
  }
}

// 处理选择常用乘客
const handleSelectFrequentPassenger = (passenger: any) => {
  console.log('选择常用乘客:', passenger)
  // 将选中的乘客信息填充到第一个乘客位置
  if (bookingForm.passengers.length > 0) {
    // 创建新对象以确保响应式更新
    const updatedPassenger = {
      ...bookingForm.passengers[0],
      name: passenger.name || '',
      idCard: passenger.idCard || '',
      phone: passenger.phone || '',
      seatClass: (bookingForm.passengers[0]?.seatClass || 'economy') as string,
      seatPreference: (bookingForm.passengers[0]?.seatPreference || 'any') as string,
      passengerType: (bookingForm.passengers[0]?.passengerType || 'adult') as string
    }
    
    console.log('更新后的乘客信息:', updatedPassenger)
    
    // 替换整个数组以触发响应式更新
    bookingForm.passengers = [
      updatedPassenger,
      ...bookingForm.passengers.slice(1)
    ]
    
    console.log('更新后的乘客数组:', bookingForm.passengers)
    
    // 使用 nextTick 确保 DOM 更新后再触发验证
    nextTick(() => {
      // 触发验证以清除错误信息
      if (passengerManagerRef.value) {
        console.log('触发验证')
        passengerManagerRef.value.validateAll()
      }
    })
  } else {
    console.warn('乘客列表为空，无法填充信息')
  }
}

// 处理预订航班
const handleBookFlight = (flightId: string) => {
  console.log('handleBookFlight 被调用', flightId)
  try {
    const flight = searchResults.value.find(f => f.id === flightId)
    if (!flight) {
      console.error('未找到航班信息', flightId)
      alert('未找到航班信息，请刷新页面重试')
      return
    }
    
    console.log('找到航班:', flight)

    // 将航班信息存入 sessionStorage，供独立确认页面使用
    const id = flight.id || flight.flightNumber
    sessionStorage.setItem(`booking_flight_${id}`, JSON.stringify(flight))

    // 清空上一次订单留下的代金券/优惠券/乘客缓存，确保新订单是干净的
    // （仅在从搜索页进入确认页时清理，保持选座/优惠选择等其它流程不受影响）
    try {
      sessionStorage.removeItem('booking_selected_voucher')
      sessionStorage.removeItem('booking_selected_coupons')
      sessionStorage.removeItem(`booking_passengers_${id}`)
    } catch (e) {
      console.warn('清理旧的预订缓存失败:', e)
    }
    // 跳转到新的确认预订信息页面组件
    router.push({
      path: '/portal/passengers/booking-confirm',
      query: {
        flightId: String(id),
        passengers: searchForm.passengers,
        cabinClass: searchForm.cabinClass || 'economy'
      }
    })
  } catch (error) {
    console.error('处理预订时出错:', error)
    alert('打开预订页面失败，请重试')
  }
}

// 关闭预订模态框
const closeBookingModal = () => {
  bookingModal.visible = false
  bookingModal.flight = null
  bookingForm.passengers = []
  bookingForm.contactInfo = { name: '', phone: '', email: '' }
}

// 提交预订
const submitBooking = async () => {
  if (!bookingModal.flight) return

  // 验证乘客信息 - 使用 PassengerManager 的验证结果
  if (!passengerValidationStatus.value) {
    alert('请填写完整的乘客信息并通过验证')
    return
  }

  try {
    bookingModal.loading = true

    const seatFee = bookingModal.flight.seatDetails?.reduce(
      (sum: number, s: any) => sum + (s.price || 0),
      0
    ) || 0

    const bookingData = {
      flightId: bookingModal.flight.id,
      passengers: bookingForm.passengers.map((p, index) => {
        const passengerSeatFee = bookingModal.flight.seatDetails?.[index]?.price || 0
        return {
          name: p.name.trim(),
          idCard: p.idCard.trim(),
          phone: p.phone || '',
          seatClass: bookingModal.flight.seatClass || 'economy',
          seatNumber: p.seatLabel || undefined,
          seatFee: passengerSeatFee
        }
      }),
      totalAmount: calculateTotalPrice.value,
      usedPoints: undefined,
      appliedCoupons: appliedCoupons.value.length > 0 ? appliedCoupons.value.map(c => ({
        id: c.id,
        name: c.name,
        type: c.type,
        appliedAmount: c.appliedAmount
      })) : undefined,
      contactInfo: {
        name: '',
        phone: '',
        email: ''
      }
    }

    let result
    try {
      result = await flightApi.bookFlight(bookingData)
    } catch (apiError: any) {
      console.warn('预订API不可用，使用模拟数据:', apiError)
      result = {
        orderId: 'ORD' + Date.now(),
        message: '预订成功（模拟数据）'
      }
    }

    if (result) {
      let orderNo: string
      let orderId: string | number

      if (result.orderNo) {
        orderNo = result.orderNo
        orderId = result.orderId || result.id || orderNo
      } else if (result.data && result.data.orderNo) {
        orderNo = result.data.orderNo
        orderId = result.data.orderId || result.data.id || orderNo
      } else {
        orderId = result.orderId || result.id || result.order?.id || 'ORD' + Date.now()
        orderNo = String(orderId)
      }

      // 预订成功，提示用户（机票文档由后台或我的订单页面提供下载）
      alert(`预订成功！\n订单号：${orderNo}\n我们已向您的邮箱发送确认信息。`)
      closeBookingModal()
      await loadData()
      searchResults.value = []
    } else {
      throw new Error('预订失败，未收到服务器响应')
    }
  } catch (error: any) {
    handleError(error, '预订失败，请重试')
  } finally {
    bookingModal.loading = false
  }
}

// 计算总价
const calculateTotalPrice = computed(() => {
  if (!bookingModal.flight) return 0
  
  const basePrice = bookingModal.flight.price || 0
  const seatFee = bookingForm.passengers.reduce((sum, p) => {
    const seatDetail = bookingModal.flight.seatDetails?.find((s: any) => s.seatId === p.seatId)
    return sum + (seatDetail?.price || 0)
  }, 0)
  
  return (basePrice + seatFee) * bookingForm.passengers.length
})

// 监听路由参数变化
watch(() => route.query.tab, (newTab) => {
  if (newTab === 'search') {
    // 滚动到搜索区域
    nextTick(() => {
      if (searchPanelRef.value) {
        searchPanelRef.value.scrollIntoView({ behavior: 'smooth', block: 'start' })
      }
    })
  }
  
  // 当返回到搜索页面时，检查是否有座位选择信息需要恢复
  if (newTab === 'search') {
    nextTick(() => {
      restoreSeatSelection()
    })
  }
}, { immediate: true })

// 监听 fromSeat 标记（用于从独立选座页返回后恢复预订信息）
watch(() => route.query.fromSeat, (val) => {
  if (val === '1') {
    nextTick(() => {
      restoreSeatSelection()
    })
  }
})

// 已改为 currentBeijingTime，实时更新

// 加载状态
const loading = ref(false)

const passengerLayout = ref([
  {
    position: '顶部主视觉',
    module: '航班实时查询 / 行程总览',
    desc: '显示最近一次行程和当前时间线。',
    tips: ['突出航班号与状态', '支持语音播报']
  },
  {
    position: '中段左侧',
    module: '订单管理',
    desc: '对已购机票进行分类查看与操作。',
    tips: ['按状态筛选', '支持批量下载']
  },
  {
    position: '底部',
    module: '辅助入口（客服、常旅客权益、支付方式）',
    desc: '为移动端和桌面端提供一致的辅助入口。',
    tips: ['固定在底部', '与安全须知链接']
  }
])

const journeys = ref<Array<{
  id: string
  route: string
  date: string
  time: string
}>>([])

// 航班实时查询相关
const flightQueryFlights = ref<any[]>([])
const flightQueryRefreshTimer = ref<number | null>(null)

// 实时显示当前北京时间
const currentBeijingTime = ref('')
const timeUpdateTimer = ref<number | null>(null)

// 更新北京时间
const updateBeijingTime = () => {
  const now = new Date()
  const year = now.getFullYear()
  const month = String(now.getMonth() + 1).padStart(2, '0')
  const day = String(now.getDate()).padStart(2, '0')
  const hours = String(now.getHours()).padStart(2, '0')
  const minutes = String(now.getMinutes()).padStart(2, '0')
  const seconds = String(now.getSeconds()).padStart(2, '0')
  currentBeijingTime.value = `${year}/${month}/${day} ${hours}:${minutes}:${seconds}`
}

// 解析路线字段，提取出发机场和到达机场
const parseRoute = (route: string) => {
  if (!route) return { origin: '', destination: '' }
  const parts = route.split('→').map(s => s.trim())
  return {
    origin: parts[0] || '',
    destination: parts[1] || ''
  }
}

// 格式化起飞时间 - 直接使用数据库原始时间，不做任何转换
const formatDepartureTime = (timeStr: string) => {
  if (!timeStr) return ''
  try {
    // 直接使用原始字符串，只做最简单的格式化（去掉秒数）
    const cleaned = String(timeStr).trim()
    // 如果包含秒数，去掉秒数部分；如果包含T，替换为空格
    // 格式：2025-12-09 18:30:00 -> 2025-12-09 18:30
    // 格式：2025-12-09T18:30:00 -> 2025-12-09 18:30
    let result = cleaned.replace('T', ' ')
    // 去掉秒数部分（如果存在）
    if (result.match(/^\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}/)) {
      result = result.substring(0, 16)
    }
    // 如果已经是 YYYY-MM-DD HH:mm 格式，直接返回
    if (result.match(/^\d{4}-\d{2}-\d{2} \d{2}:\d{2}$/)) {
      return result
    }
    // 如果长度足够，截取前16个字符
    if (result.length >= 16) {
      return result.substring(0, 16)
    }
    return result
  } catch {
    return String(timeStr)
  }
}

// 检查时间差是否小于24小时 - 直接使用数据库时间，不进行时区转换
const isWithin24Hours = (departureTimeStr: string) => {
  if (!departureTimeStr) return false
  try {
    // 直接解析时间字符串为本地时间，不进行时区转换
    const cleaned = departureTimeStr.trim()
    const match = cleaned.match(/^(\d{4})-(\d{2})-(\d{2})[\sT](\d{2}):(\d{2})(?::(\d{2}))?/)
    if (match && match[1] && match[2] && match[3] && match[4] && match[5]) {
      const year = match[1]
      const month = match[2]
      const day = match[3]
      const hours = match[4]
      const minutes = match[5]
      const seconds = match[6]
      // 使用本地时间创建Date对象，不进行时区转换
      const departureTime = new Date(
        parseInt(year),
        parseInt(month) - 1,
        parseInt(day),
        parseInt(hours),
        parseInt(minutes),
        seconds ? parseInt(seconds) : 0
      )
      if (isNaN(departureTime.getTime())) return false
      const now = new Date()
      const diffMs = departureTime.getTime() - now.getTime()
      const diffHours = diffMs / (1000 * 60 * 60)
      // 起飞时间在未来，且时间差小于24小时
      return diffMs > 0 && diffHours < 24 && diffHours >= 0
    }
    // 如果格式不匹配，尝试使用Date解析（兼容其他格式）
    const departureTime = new Date(departureTimeStr)
    if (isNaN(departureTime.getTime())) return false
    const now = new Date()
    const diffMs = departureTime.getTime() - now.getTime()
    const diffHours = diffMs / (1000 * 60 * 60)
    return diffMs > 0 && diffHours < 24 && diffHours >= 0
  } catch {
    return false
  }
}

// 加载符合条件的航班（用于航班实时查询）
const loadFlightQueryFlights = async () => {
  try {
    // 获取所有订单，使用大的size值确保获取所有数据
    const firstPageResult = await orderApi.getOrders({ page: 0, size: 100 })
    
    if (!firstPageResult || !firstPageResult.orders) {
      flightQueryFlights.value = []
      return
    }
    
    if (firstPageResult.orders.length === 0 && firstPageResult.total > 0) {
      // 如果第一页为空但总数大于0，尝试获取所有页
      const total = firstPageResult.total || 0
      const pageSize = 10
      const totalPages = Math.ceil(total / pageSize)
      let allOrders: any[] = []
      
      for (let page = 0; page < totalPages; page++) {
        try {
          const pageResult = await orderApi.getOrders({ page, size: pageSize })
          if (pageResult && pageResult.orders && pageResult.orders.length > 0) {
            allOrders = [...allOrders, ...pageResult.orders]
          }
        } catch (e) {
          console.warn(`获取第${page + 1}页订单失败:`, e)
        }
      }
      
      if (allOrders.length === 0) {
        flightQueryFlights.value = []
        return
      }
      
      const filtered = allOrders
        .filter((order: any) => {
          const status = String(order.status || '').toLowerCase()
          const isTicketed = status === 'ticketed'
          const within24Hours = isWithin24Hours(order.departureTime)
          return isTicketed && within24Hours
        })
        .map((order: any) => {
          const routeInfo = parseRoute(order.route || '')
          return {
            id: order.id,
            passengerName: order.passengerName || '未知',
            originAirport: routeInfo.origin,
            destinationAirport: routeInfo.destination,
            departureTime: formatDepartureTime(order.departureTime)
          }
        })
      
      flightQueryFlights.value = filtered
      return
    }
    
    let allOrders = [...firstPageResult.orders]
    const total = firstPageResult.total || 0
    const pageSize = firstPageResult.size || 100
    
    // 如果数据超过一页，获取剩余页的数据
    if (total > pageSize) {
      const totalPages = Math.ceil(total / pageSize)
      for (let page = 1; page < totalPages; page++) {
        try {
          const pageResult = await orderApi.getOrders({ page, size: pageSize })
          if (pageResult && pageResult.orders) {
            allOrders = [...allOrders, ...pageResult.orders]
          }
        } catch (e) {
          console.warn(`获取第${page + 1}页订单失败:`, e)
        }
      }
    }
    
    const filtered = allOrders
      .filter((order: any) => {
        const status = String(order.status || '').toLowerCase()
        const isTicketed = status === 'ticketed'
        const within24Hours = isWithin24Hours(order.departureTime)
        return isTicketed && within24Hours
      })
      .map((order: any) => {
        const routeInfo = parseRoute(order.route || '')
        return {
          id: order.id,
          passengerName: order.passengerName || '未知',
          originAirport: routeInfo.origin,
          destinationAirport: routeInfo.destination,
          departureTime: formatDepartureTime(order.departureTime)
        }
      })
    
    flightQueryFlights.value = filtered
  } catch (error) {
    console.error('加载航班实时查询失败:', error)
    flightQueryFlights.value = []
  }
}

const passengerOrders = ref<Array<{
  id: string
  route: string
  status: string
  statusText: string
}>>([])

// 错误处理
const handleError = (error: any, defaultMessage: string = '操作失败') => {
  console.error('API错误:', error)
  alert(error?.message || defaultMessage)
}

// 辅助：将航班的日期+时间转换为时间戳（本地时区）
const toTs = (dateStr?: string, timeStr?: string) => {
  if (!dateStr) return NaN
  try {
    const [y, m, d] = dateStr.includes('-')
      ? dateStr.split('-').map((n: string) => parseInt(n, 10))
      : dateStr.split('/').map((n: string) => parseInt(n, 10))
    let hh = 0, mm = 0
    if (timeStr && /\d{1,2}:\d{2}/.test(timeStr)) {
      const parts = timeStr.split(':')
      const h = parts[0] ? parseInt(parts[0], 10) : 0
      const mi = parts[1] ? parseInt(parts[1], 10) : 0
      hh = isNaN(h) ? 0 : h
      mm = isNaN(mi) ? 0 : mi
    }
    return new Date(y || 0, (m || 1) - 1, d || 1, hh, mm, 0, 0).getTime()
  } catch {
    return NaN
  }
}

// 加载数据
const loadData = async () => {
  try {
    loading.value = true
    // 并行加载所有数据
    const [upcomingResult, ordersResult] = await Promise.all([
      passengerApi.getUpcomingFlights().catch(() => ({ flights: [] })),
      orderApi.getOrders({ page: 1, size: 10 }).catch(() => ({ orders: [] }))
    ])

    // 处理未来24小时内的待出行航班
    if (upcomingResult?.flights) {
      const nowTs = Date.now()
      const endTs = nowTs + 24 * 60 * 60 * 1000
      const list = (upcomingResult.flights as any[])
        .map((f: any) => ({ ...f, _ts: toTs(f.date, f.departureTime) }))
        .filter((f: any) => !isNaN(f._ts) && f._ts >= nowTs && f._ts <= endTs)
        .sort((a: any, b: any) => a._ts - b._ts)
        .map((f: any) => {
          // 直接使用数据库原始时间，不做任何转换
          // 如果departureTime是完整的时间字符串（包含日期和时间），提取时间部分
          let timeStr = f.departureTime || ''
          if (timeStr && timeStr.includes(' ')) {
            // 格式：2025-12-09 18:30:00 或 2025-12-09 18:30
            const parts = timeStr.split(' ')
            if (parts.length >= 2) {
              timeStr = parts[1].substring(0, 5) // 提取 HH:mm 部分
            }
          } else if (timeStr && timeStr.includes('T')) {
            // 格式：2025-12-09T18:30:00
            const parts = timeStr.split('T')
            if (parts.length >= 2) {
              timeStr = parts[1].substring(0, 5) // 提取 HH:mm 部分
            }
          }
          // 如果departureTime不存在，从date和departureTime组合中提取，但不做时区转换
          if (!timeStr && f.date && f.departureTime) {
            // 如果departureTime是单独的时间字段，直接使用
            timeStr = String(f.departureTime).substring(0, 5)
          }
          
          // 提取日期部分（不做时区转换）
          let dateStr = ''
          if (f.date) {
            const dateParts = String(f.date).split('-')
            if (dateParts.length >= 2) {
              dateStr = `${dateParts[1]}/${dateParts[2]}`
            }
          }
          
          return {
            id: f.id || `j${f._ts}`,
            route: `${f.origin || f.departure} ⇀ ${f.destination}`,
            date: dateStr,
            time: `${timeStr || ''} · ${f.flightNumber || ''}`
          }
        })
      journeys.value = list
    }

    // 处理订单列表
    if (ordersResult?.orders) {
      passengerOrders.value = ordersResult.orders.map((order: any) => ({
        id: order.id,
        route: order.route || `${order.departure} ⇀ ${order.destination}`,
        status: order.status || 'processing',
        statusText: getStatusText(order.status)
      }))
    }
  } catch (error) {
    handleError(error, '加载数据失败')
  } finally {
    loading.value = false
  }
}

// 获取状态文本
const getStatusText = (status: string) => {
  const statusMap: Record<string, string> = {
    processing: '待出行',
    confirmed: '已确认',
    completed: '已完成',
    refunded: '已退票',
    cancelled: '已取消'
  }
  return statusMap[status] || '未知'
}

// 处理行程单按钮点击
const handleItinerary = async (orderId: string) => {
  try {
    const blob = await passengerApi.getItinerary(orderId)
    // 创建下载链接
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `行程单_${orderId}.pdf`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
  } catch (error) {
    handleError(error, '下载行程单失败')
  }
}

// 处理联系客服按钮点击
const handleContactSupport = async (orderId: string) => {
  try {
    await passengerApi.contactSupport({
      orderId,
      subject: `关于订单 ${orderId} 的咨询`,
      message: `我想咨询订单 ${orderId} 的相关信息`,
      type: 'chat'
    })
    alert('客服消息已发送，我们会尽快回复您')
  } catch (error) {
    handleError(error, '联系客服失败')
  }
}


const showFlightDetail = (flightId: string) => {
  alert(`显示航班 ${flightId} 的详细信息`)
  // 这里应该打开航班详情页面或弹窗
  console.log(`显示航班详情: ${flightId}`)
}


// 监听主题同步事件，确保主题修改后页面同步更新
const handleThemeSync = (event: any) => {
  const { type, value } = event.detail
  // 重新应用主题设置
  const root = document.documentElement
  
  if (type === 'theme') {
    if (value === 'auto') {
      const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches
      root.setAttribute('data-theme', prefersDark ? 'dark' : 'light')
    } else {
      root.setAttribute('data-theme', value)
    }
  } else if (type === 'color') {
    root.style.setProperty('--primary-color', value)
    root.style.setProperty('--color-primary', value)
  } else if (type === 'fontSize') {
    const fontSizeMap = {
      small: '14px',
      medium: '16px',
      large: '18px'
    }
    const sizeKey = (value as keyof typeof fontSizeMap) || 'medium'
    root.style.setProperty('--base-font-size', fontSizeMap[sizeKey])
    root.style.setProperty('--font-size-base', fontSizeMap[sizeKey])
  } else if (type === 'showAnimations') {
    if (!value) {
      root.style.setProperty('--animation-duration', '0s')
    } else {
      root.style.setProperty('--animation-duration', '0.3s')
    }
  } else if (type === 'compactMode') {
    if (value) {
      root.setAttribute('data-compact', 'true')
    } else {
      root.removeAttribute('data-compact')
    }
  }
}

// 恢复从座位选择页面返回的座位信息
const restoreSeatSelection = () => {
  const flightId = sessionStorage.getItem('flightId')
  const selectedSeatsStr = sessionStorage.getItem('selectedSeats')
  const seatDetailsStr = sessionStorage.getItem('seatDetails')
  
  if (flightId && selectedSeatsStr && seatDetailsStr) {
    try {
      const selectedSeats = JSON.parse(selectedSeatsStr)
      const seatDetails = JSON.parse(seatDetailsStr)
      
      // 更新搜索结果中对应航班的座位信息（兼容数字 / 字符串的 id）
      const flightIndex = searchResults.value.findIndex(f => String(f.id) === flightId || f.flightNumber === flightId)
      let targetFlight: any = null
      if (flightIndex > -1) {
        const flight = searchResults.value[flightIndex]
        if (flight) {
          flight.selectedSeats = selectedSeats
          flight.seatDetails = seatDetails
          targetFlight = flight
        }
      }
      
      // 判断是否需要自动重新打开预订模态框
      const reopenFlag = sessionStorage.getItem('reopenBookingAfterSeat')
      if (reopenFlag === '1' && targetFlight) {
        // 使用选座前的乘客数量重新初始化预订表单
        bookingModal.flight = targetFlight
        initBookingForm(targetFlight)
        
        // 将选中的座位同步到乘客信息
        bookingForm.passengers.forEach((p, i) => {
          if (selectedSeats[i] && seatDetails[i]) {
            p.seatId = selectedSeats[i]
            p.seatLabel = seatDetails[i].seatLabel
            p.seatClass = (seatDetails[i].seatClass || 'economy') as string
          }
        })
        
        // 更新预订模态框中的航班信息（用于费用明细计算）
        bookingModal.flight.selectedSeats = selectedSeats
        bookingModal.flight.seatDetails = seatDetails
        bookingModal.visible = true
      } else if (bookingModal.visible && bookingModal.flight) {
        // 如果预订模态框本来就是打开的，且是同一个航班，则同步到乘客表单
        const currentFlightId = String(bookingModal.flight.id || bookingModal.flight.flightNumber)
        if (currentFlightId === flightId || bookingModal.flight.flightNumber === flightId) {
          bookingForm.passengers.forEach((p, i) => {
            if (selectedSeats[i] && seatDetails[i]) {
              p.seatId = selectedSeats[i]
              p.seatLabel = seatDetails[i].seatLabel
              p.seatClass = seatDetails[i].seatClass || 'economy'
            }
          })
          
          bookingModal.flight.selectedSeats = selectedSeats
          bookingModal.flight.seatDetails = seatDetails
        }
      }
      
      // 清理 sessionStorage
      sessionStorage.removeItem('flightId')
      sessionStorage.removeItem('selectedSeats')
      sessionStorage.removeItem('seatDetails')
      sessionStorage.removeItem('reopenBookingAfterSeat')
    } catch (e) {
      console.error('恢复座位选择信息失败:', e)
    }
  }
}

// 初始化
onMounted(async () => {
  // 确保主题已初始化
  if (!store.themeState.theme) {
    store.initializeThemeState()
  }
  
  // 应用当前主题设置
  const root = document.documentElement
  if (store.themeState.theme === 'auto') {
    const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches
    root.setAttribute('data-theme', prefersDark ? 'dark' : 'light')
  } else {
    root.setAttribute('data-theme', store.themeState.theme)
  }
  root.style.setProperty('--primary-color', store.themeState.primaryColor)
  root.style.setProperty('--color-primary', store.themeState.primaryColor)
  
  // 监听主题同步事件
  window.addEventListener('theme-sync', handleThemeSync)
  
  // 恢复从座位选择页面返回的座位信息
  restoreSeatSelection()
  
  // 初始化北京时间显示
  updateBeijingTime()
  timeUpdateTimer.value = setInterval(() => {
    updateBeijingTime()
  }, 1000)
  
  // 加载航班实时查询
  await loadFlightQueryFlights()
  
  // 设置定时刷新（每1秒刷新一次）
  flightQueryRefreshTimer.value = setInterval(() => {
    loadFlightQueryFlights()
  }, 1000)
  
  await loadData()
})

// 组件卸载时移除事件监听
onUnmounted(() => {
  window.removeEventListener('theme-sync', handleThemeSync)
  // 清除定时器
  if (timeUpdateTimer.value) {
    clearInterval(timeUpdateTimer.value)
    timeUpdateTimer.value = null
  }
  if (flightQueryRefreshTimer.value) {
    clearInterval(flightQueryRefreshTimer.value)
    flightQueryRefreshTimer.value = null
  }
})

</script>

<style scoped>
/* 直接内联基础样式 */
.page-container {
  padding: 2.5rem clamp(1.5rem, 6vw, 4rem) 3rem;
  color: #f8fafc;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
  position: relative;
  isolation: isolate;
  background: radial-gradient(circle at 10% 20%, rgba(59, 130, 246, 0.2), transparent 45%),
    radial-gradient(circle at 80% 0%, rgba(14, 165, 233, 0.25), transparent 50%),
    linear-gradient(135deg, #040920 0%, #03050f 80%);
}

.page-container::before {
  content: '';
  position: absolute;
  inset: 0;
  background: url('data:image/svg+xml,%3Csvg width="100" height="100" viewBox="0 0 100 100" xmlns="http://www.w3.org/2000/svg"%3E%3Ccircle cx="1" cy="1" r="1" fill="rgba(255,255,255,0.04)"/%3E%3C/svg%3E');
  opacity: 0.8;
  pointer-events: none;
  z-index: -1;
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
}

.primary-btn {
  background: linear-gradient(135deg, #1E8AE6, #0A2F63);
  color: #fff;
  box-shadow: 0 10px 25px rgba(99, 102, 241, 0.35);
}

.ghost-btn {
  border-color: rgba(255, 255, 255, 0.35);
  background: transparent;
  color: #f8fafc;
}

.ghost-btn:hover {
  background: rgba(255, 255, 255, 0.1);
}

.grid-two {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 1.5rem;
}

.glass-card {
  border-radius: 28px;
  padding: 1.8rem;
  background: rgba(5, 11, 32, 0.82);
  border: 1px solid rgba(148, 163, 184, 0.12);
  box-shadow:
    0 25px 60px rgba(3, 7, 18, 0.8),
    inset 0 1px rgba(255, 255, 255, 0.08);
  backdrop-filter: blur(20px);
  transition: border-color 0.3s, transform 0.3s;
}

.glass-card:hover {
  border-color: rgba(59, 130, 246, 0.4);
  transform: translateY(-2px);
}

.section-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 1rem;
  margin-bottom: 1.2rem;
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
}

.data-table {
  width: 100%;
  border-collapse: collapse;
}

.data-table th,
.data-table td {
  padding: 0.9rem 0.6rem;
  text-align: left;
  border-bottom: 1px solid rgba(148, 163, 184, 0.2);
}

.data-table th {
  color: rgba(248, 250, 252, 0.65);
  font-weight: 500;
}

.data-table td {
  color: rgba(248, 250, 252, 0.85);
}

.status-tag {
  padding: 0.3rem 0.8rem;
  border-radius: 999px;
  font-size: 0.85rem;
  display: inline-block;
}

.pax-page {
  gap: 1.5rem;
}

.layout-map h2 {
  margin-top: 0;
}

.layout-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 1rem;
}

.layout-item {
  border: 1px dashed rgba(248, 250, 252, 0.2);
  border-radius: 16px;
  padding: 1rem;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.layout-position {
  font-size: 0.85rem;
  color: rgba(248, 250, 252, 0.6);
}

.layout-item ul {
  margin: 0;
  padding-left: 1rem;
  color: rgba(248, 250, 252, 0.7);
}

.hero-card .journey-list {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 0.8rem;
}

.journey-list li {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid rgba(248, 250, 252, 0.05);
  padding-bottom: 0.8rem;
}

.journey-list li:last-child {
  border-bottom: none;
}

.journey-list small {
  color: rgba(248, 250, 252, 0.65);
}

/* 航班实时查询内容样式 */
.flight-query-content {
  margin-top: 1rem;
}

.flight-query-list {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.flight-query-item {
  padding: 0.75rem;
  background: rgba(30, 138, 230, 0.1);
  border: 1px solid rgba(30, 138, 230, 0.3);
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.flight-query-row {
  display: flex;
  align-items: center;
  font-size: 0.85rem;
}

.flight-query-label {
  color: rgba(255, 255, 255, 0.7);
  min-width: 70px;
  flex-shrink: 0;
}

.flight-query-value {
  color: var(--text-primary, #fff);
  font-weight: 500;
  flex: 1;
}

.flight-query-empty {
  margin-top: 1rem;
  padding: 2rem;
  text-align: center;
  color: rgba(255, 255, 255, 0.6);
  font-size: 0.9rem;
}

.order-card table {
  width: 100%;
}


.status-tag.processing {
  background: rgba(59, 130, 246, 0.15);
  color: #1E8AE6;
}

.status-tag.completed {
  background: rgba(34, 197, 94, 0.15);
  color: #4ade80;
}

.status-tag.refunded {
  background: rgba(248, 113, 113, 0.15);
  color: #f87171;
}

/* 搜索面板样式 */
.search-panel {
  margin-bottom: 1.5rem;
}

.search-panel .panel-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 1rem;
  margin-bottom: 1.5rem;
}

.search-panel .panel-label {
  font-size: 0.85rem;
  color: rgba(248, 250, 252, 0.6);
  margin-bottom: 0.3rem;
}

.search-panel .panel-header h2 {
  margin: 0;
  font-size: 1.6rem;
}

.search-panel .panel-pill {
  padding: 0.4rem 0.9rem;
  border-radius: 999px;
  background: rgba(34, 197, 94, 0.12);
  color: #a7f3d0;
  font-size: 0.85rem;
  border: 1px solid rgba(167, 243, 208, 0.25);
}

.search-panel .search-form {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  margin-bottom: 1.5rem;
}

.search-panel .form-row {
  display: flex;
  gap: 1rem;
  flex-wrap: wrap;
}

.search-panel .form-group {
  flex: 1;
  min-width: 220px;
}

.search-panel .form-group label {
  display: block;
  margin-bottom: 0.35rem;
  font-size: 0.9rem;
  color: rgba(248, 250, 252, 0.75);
}

.search-panel .form-group input,
.search-panel .form-group select {
  width: 100%;
  border: 1px solid rgba(255, 255, 255, 0.12);
  border-radius: 18px;
  padding: 0.85rem 1.1rem;
  background: rgba(8, 14, 35, 0.85);
  color: #f8fafc;
  font-size: 0.95rem;
  outline: none;
  transition: border-color 0.25s, box-shadow 0.25s, transform 0.25s;
}

.search-panel .form-group input:focus,
.search-panel .form-group select:focus {
  border-color: rgba(96, 165, 250, 0.9);
  box-shadow: 0 8px 25px rgba(14, 165, 233, 0.25);
  transform: translateY(-1px);
}

.search-panel .search-btn {
  width: 100%;
  border: none;
  border-radius: 18px;
  padding: 0.95rem;
  background: linear-gradient(135deg, #1E8AE6, #0A2F63);
  color: #fff;
  font-size: 1.05rem;
  font-weight: 600;
  cursor: pointer;
  box-shadow: 0 20px 40px rgba(14, 165, 233, 0.35);
  transition: all 0.3s ease;
}

.search-panel .search-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 25px 50px rgba(14, 165, 233, 0.45);
}

.search-panel .search-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.search-panel .search-results {
  margin-top: 1.5rem;
  padding-top: 1.5rem;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

.search-panel .search-results h3 {
  margin: 0 0 1rem 0;
  color: #fff;
  font-size: 1.2rem;
}

.search-panel .results-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.search-panel .result-item {
  padding: 1.8rem;
  border-radius: 20px;
  border: 1px solid rgba(255, 255, 255, 0.08);
  background: linear-gradient(120deg, rgba(180, 183, 193, 0.9), rgba(66, 95, 208, 0.9));
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 1rem;
  flex-wrap: wrap;
  transition: all 0.35s ease;
  position: relative;
  overflow: visible;
  z-index: 1;
}

.search-panel .result-item::before {
  content: '';
  position: absolute;
  inset: -30%;
  background: radial-gradient(circle, rgba(59, 130, 246, 0.35), transparent 50%);
  opacity: 0;
  transition: opacity 0.4s;
  pointer-events: none;
  z-index: 0;
}

.search-panel .result-item:hover::before {
  opacity: 1;
}

.search-panel .result-item:hover {
  border-color: rgba(59, 130, 246, 0.4);
  transform: translateY(-2px);
}

.search-panel .result-info {
  position: relative;
  z-index: 1;
  flex: 1;
  min-width: 200px;
}

.search-panel .result-info h4 {
  margin: 0 0 0.5rem 0;
  color: #fff;
  font-size: 1.1rem;
}

.search-panel .result-info p {
  margin: 0.3rem 0;
  color: rgba(248, 250, 252, 0.7);
  font-size: 0.9rem;
}

.search-panel .result-actions {
  display: flex;
  align-items: center;
  gap: 1rem;
  position: relative;
  z-index: 2;
  flex-shrink: 0;
}

.search-panel .result-price {
  font-size: 1.3rem;
  font-weight: 600;
  color: #fcd34d;
}

.search-panel .result-info .flight-duration,
.search-panel .result-info .flight-seats {
  font-size: 0.85rem;
  color: rgba(248, 250, 252, 0.6);
  margin: 0.2rem 0;
}

.search-panel .price-info {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 0.5rem;
}

.search-panel .cabin-badge {
  padding: 0.25rem 0.6rem;
  border-radius: 8px;
  background: rgba(59, 130, 246, 0.2);
  color: #60a5fa;
  font-size: 0.75rem;
  border: 1px solid rgba(59, 130, 246, 0.3);
}

.search-panel .action-buttons {
  display: flex;
  gap: 0.8rem;
  flex-wrap: wrap;
  position: relative;
  z-index: 1;
}

.search-panel .action-buttons button {
  position: relative;
  z-index: 2;
  cursor: pointer;
  pointer-events: auto;
}

/* 弹窗过渡动画 */
.modal-fade-enter-active,
.modal-fade-leave-active {
  transition: opacity 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.modal-fade-enter-active .seat-modal,
.modal-fade-enter-active .booking-modal,
.modal-fade-leave-active .seat-modal,
.modal-fade-leave-active .booking-modal {
  transition: all 0.35s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.modal-fade-enter-from {
  opacity: 0;
}

.modal-fade-enter-from .seat-modal,
.modal-fade-enter-from .booking-modal {
  opacity: 0;
  transform: scale(0.9) translateY(20px);
}

.modal-fade-leave-to {
  opacity: 0;
}

.modal-fade-leave-to .seat-modal,
.modal-fade-leave-to .booking-modal {
  opacity: 0;
  transform: scale(0.95) translateY(-10px);
}

/* 座位选择模态框样式 */
.seat-modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(6, 11, 40, 0.75);
  backdrop-filter: blur(8px);
  z-index: 9999;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 2rem;
  -webkit-overflow-scrolling: touch;
}

.seat-modal {
  background: linear-gradient(135deg, rgba(15, 23, 42, 0.98) 0%, rgba(5, 11, 30, 0.98) 100%);
  border: 1px solid rgba(148, 163, 184, 0.2);
  border-radius: 24px;
  width: 100%;
  max-width: 900px;
  max-height: 90vh;
  display: flex;
  flex-direction: column;
  box-shadow:
    0 30px 60px rgba(0, 0, 0, 0.8),
    0 0 0 1px rgba(148, 163, 184, 0.1),
    inset 0 1px 0 rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(24px);
  overflow: hidden;
  position: relative;
}

.seat-modal::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(90deg, 
    transparent 0%, 
    rgba(59, 130, 246, 0.3) 50%, 
    transparent 100%);
  pointer-events: none;
}

.seat-modal-header {
  padding: 1.5rem 2rem;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.seat-modal-header h3 {
  margin: 0 0 0.5rem 0;
  color: #fff;
  font-size: 1.5rem;
}

.seat-modal-header p {
  margin: 0;
  color: rgba(248, 250, 252, 0.7);
  font-size: 0.9rem;
}

.close-btn {
  background: rgba(148, 163, 184, 0.1);
  border: 1px solid rgba(148, 163, 184, 0.2);
  color: #cbd5e1;
  font-size: 24px;
  line-height: 1;
  cursor: pointer;
  padding: 0;
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 10px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.close-btn:hover {
  background: rgba(239, 68, 68, 0.15);
  border-color: rgba(239, 68, 68, 0.3);
  color: #f87171;
  transform: rotate(90deg) scale(1.05);
}

.close-btn:active {
  transform: rotate(90deg) scale(0.95);
}

.seat-modal-body {
  padding: 2rem;
  overflow-y: auto;
  flex: 1;
}

.seat-legend {
  display: flex;
  gap: 1.5rem;
  margin-bottom: 2rem;
  flex-wrap: wrap;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  color: rgba(248, 250, 252, 0.8);
  font-size: 0.9rem;
}

.legend-color {
  width: 24px;
  height: 24px;
  border-radius: 6px;
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.legend-color.available {
  background: rgba(34, 197, 94, 0.3);
  border-color: rgba(34, 197, 94, 0.5);
}

.legend-color.selected {
  background: rgba(59, 130, 246, 0.5);
  border-color: rgba(59, 130, 246, 0.8);
}

.legend-color.occupied {
  background: rgba(107, 114, 128, 0.4);
  border-color: rgba(107, 114, 128, 0.6);
}

.legend-color.premium {
  background: rgba(251, 191, 36, 0.3);
  border-color: rgba(251, 191, 36, 0.5);
}

.cabin-layout {
  display: flex;
  flex-direction: column;
  gap: 2rem;
}

.cabin-section {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.cabin-section h4 {
  margin: 0;
  color: #fff;
  font-size: 1.1rem;
  padding-bottom: 0.5rem;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.seat-grid {
  display: grid;
  gap: 0.5rem;
  padding: 1rem;
  background: rgba(8, 14, 35, 0.5);
  border-radius: 12px;
}

.business-grid {
  grid-template-columns: repeat(4, 1fr);
}

.economy-grid {
  grid-template-columns: repeat(6, 1fr);
}

.seat {
  aspect-ratio: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  border: 2px solid transparent;
  cursor: pointer;
  transition: all 0.2s;
  position: relative;
  background: rgba(34, 197, 94, 0.2);
  border-color: rgba(34, 197, 94, 0.4);
}

.seat.available {
  background: rgba(34, 197, 94, 0.2);
  border-color: rgba(34, 197, 94, 0.4);
}

.seat.available:hover {
  background: rgba(34, 197, 94, 0.35);
  border-color: rgba(34, 197, 94, 0.6);
  transform: scale(1.05);
}

.seat.selected {
  background: rgba(59, 130, 246, 0.5);
  border-color: rgba(59, 130, 246, 0.8);
  box-shadow: 0 0 12px rgba(59, 130, 246, 0.5);
}

.seat.occupied {
  background: rgba(107, 114, 128, 0.3);
  border-color: rgba(107, 114, 128, 0.5);
  cursor: not-allowed;
  opacity: 0.6;
}

.seat.premium {
  background: rgba(251, 191, 36, 0.3);
  border-color: rgba(251, 191, 36, 0.5);
}

.seat.premium:hover:not(.occupied) {
  background: rgba(251, 191, 36, 0.4);
  border-color: rgba(251, 191, 36, 0.7);
  transform: scale(1.05);
}

.seat-label {
  font-size: 0.75rem;
  font-weight: 600;
  color: #fff;
}

.seat-badge {
  font-size: 0.6rem;
  padding: 0.1rem 0.3rem;
  background: rgba(251, 191, 36, 0.3);
  color: #fbbf24;
  border-radius: 4px;
  margin-top: 0.2rem;
}

.selected-seats-info {
  margin-top: 2rem;
  padding-top: 1.5rem;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

.selected-seats-info h4 {
  margin: 0 0 1rem 0;
  color: #fff;
  font-size: 1rem;
}

.selected-seats-list {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.selected-seat-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.75rem 1rem;
  background: rgba(59, 130, 246, 0.15);
  border: 1px solid rgba(59, 130, 246, 0.3);
  border-radius: 8px;
  color: #fff;
}

.remove-btn {
  background: rgba(248, 113, 113, 0.2);
  border: 1px solid rgba(248, 113, 113, 0.4);
  color: #f87171;
  padding: 0.25rem 0.75rem;
  border-radius: 6px;
  font-size: 0.85rem;
  cursor: pointer;
  transition: all 0.2s;
}

.remove-btn:hover {
  background: rgba(248, 113, 113, 0.3);
  border-color: rgba(248, 113, 113, 0.6);
}

.seat-modal-footer {
  padding: 1.5rem 2rem;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  display: flex;
  justify-content: flex-end;
  gap: 1rem;
}

.seat-modal-footer .ghost-btn,
.seat-modal-footer .primary-btn {
  padding: 0.75rem 1.5rem;
  font-size: 0.95rem;
}

.seat-modal-footer .primary-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* 预订确认模态框样式 */
.booking-modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(6, 11, 40, 0.75);
  backdrop-filter: blur(8px);
  z-index: 9999;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 2rem;
  overflow-y: auto;
  -webkit-overflow-scrolling: touch;
}

.booking-modal {
  background: linear-gradient(135deg, rgba(15, 23, 42, 0.98) 0%, rgba(5, 11, 30, 0.98) 100%);
  border: 1px solid rgba(148, 163, 184, 0.2);
  border-radius: 24px;
  width: 100%;
  max-width: 800px;
  max-height: 90vh;
  display: flex;
  flex-direction: column;
  box-shadow:
    0 30px 60px rgba(0, 0, 0, 0.8),
    0 0 0 1px rgba(148, 163, 184, 0.1),
    inset 0 1px 0 rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(24px);
  overflow: hidden;
  position: relative;
}

.booking-modal::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(90deg, 
    transparent 0%, 
    rgba(59, 130, 246, 0.3) 50%, 
    transparent 100%);
  pointer-events: none;
}

.booking-modal-header {
  padding: 1.5rem 2rem;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  flex-shrink: 0;
}

.booking-modal-header h3 {
  margin: 0 0 0.5rem 0;
  color: #fff;
  font-size: 1.5rem;
}

.booking-modal-header p {
  margin: 0;
  color: rgba(248, 250, 252, 0.7);
  font-size: 0.9rem;
}

.booking-modal-body {
  padding: 2rem;
  overflow-y: auto;
  flex: 1;
}

.booking-section {
  margin-bottom: 2rem;
}

.booking-section:last-child {
  margin-bottom: 0;
}

.booking-section h4 {
  margin: 0 0 1rem 0;
  color: #fff;
  font-size: 1.1rem;
  padding-bottom: 0.5rem;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.flight-info-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 1rem;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.info-label {
  font-size: 0.85rem;
  color: rgba(248, 250, 252, 0.6);
}

.info-value {
  font-size: 0.95rem;
  color: #fff;
  font-weight: 500;
}

.seats-list {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
}

.seat-tag {
  padding: 0.5rem 1rem;
  background: rgba(59, 130, 246, 0.2);
  border: 1px solid rgba(59, 130, 246, 0.4);
  border-radius: 8px;
  color: #60a5fa;
  font-size: 0.9rem;
  font-weight: 500;
}

.passenger-form {
  padding: 1rem;
  background: rgba(8, 14, 35, 0.5);
  border-radius: 12px;
  margin-bottom: 1rem;
}

.passenger-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
  color: #fff;
  font-weight: 500;
}

.seat-badge-small {
  padding: 0.25rem 0.75rem;
  background: rgba(59, 130, 246, 0.2);
  border: 1px solid rgba(59, 130, 246, 0.4);
  border-radius: 6px;
  color: #60a5fa;
  font-size: 0.85rem;
}

.booking-section .form-row {
  display: flex;
  gap: 1rem;
  margin-bottom: 1rem;
}

.booking-section .form-row:last-child {
  margin-bottom: 0;
}

.booking-section .form-group {
  flex: 1;
}

.booking-section .form-group label {
  display: block;
  margin-bottom: 0.5rem;
  color: rgba(248, 250, 252, 0.8);
  font-size: 0.9rem;
}

.required {
  color: #f87171;
}

.booking-section .form-group input {
  width: 100%;
  padding: 0.75rem 1rem;
  background: rgba(8, 14, 35, 0.8);
  border: 1px solid rgba(255, 255, 255, 0.12);
  border-radius: 12px;
  color: #fff;
  font-size: 0.95rem;
  outline: none;
  transition: all 0.25s;
}

.booking-section .form-group input:focus {
  border-color: rgba(96, 165, 250, 0.9);
  box-shadow: 0 0 0 3px rgba(96, 165, 250, 0.1);
}

.price-section {
  background: rgba(8, 14, 35, 0.5);
  padding: 1.5rem;
  border-radius: 12px;
  border: 1px solid rgba(59, 130, 246, 0.2);
}

.price-breakdown {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.price-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: rgba(248, 250, 252, 0.8);
  font-size: 0.95rem;
}

.price-item.total {
  padding-top: 0.75rem;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  margin-top: 0.5rem;
  font-size: 1.1rem;
  font-weight: 600;
  color: #fff;
}

.total-price {
  color: #fcd34d;
  font-size: 1.3rem;
  font-weight: 700;
}

.booking-modal-footer {
  padding: 1.5rem 2rem;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  display: flex;
  justify-content: flex-end;
  gap: 1rem;
  flex-shrink: 0;
}

.booking-modal-footer .ghost-btn,
.booking-modal-footer .primary-btn {
  padding: 0.75rem 1.5rem;
  font-size: 0.95rem;
}

.booking-modal-footer .primary-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

@media (max-width: 768px) {
  .booking-modal {
    max-width: 95%;
    max-height: 95vh;
  }

  .flight-info-grid {
    grid-template-columns: 1fr;
  }

  .booking-section .form-row {
    flex-direction: column;
  }
}

/* 响应式适配 */
@media (max-width: 768px) {
  .page-container {
    padding: 1.5rem 1rem;
  }

  .page-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .page-actions {
    width: 100%;
    justify-content: flex-start;
  }

  .grid-two {
    grid-template-columns: 1fr;
  }

  .layout-grid {
    grid-template-columns: 1fr;
  }

  .data-table {
    font-size: 0.85rem;
  }

  .data-table th,
  .data-table td {
    padding: 0.6rem 0.4rem;
  }

  .action-buttons {
    flex-direction: column;
    gap: 0.4rem;
  }

  .action-buttons .ghost-btn {
    width: 100%;
  }
}

@media (max-width: 480px) {
  .data-table {
    display: block;
    overflow-x: auto;
    -webkit-overflow-scrolling: touch;
  }

  .journey-list li {
    flex-direction: column;
    align-items: flex-start;
    gap: 0.8rem;
  }

  .form-row {
    flex-direction: column;
  }

  .result-item {
    flex-direction: column;
    align-items: flex-start;
  }

  .result-actions {
    width: 100%;
    justify-content: space-between;
  }
}

/* 常用乘客选择部分样式 */
.section-desc {
  margin: 0.5rem 0 0 0;
  color: rgba(248, 250, 252, 0.6);
  font-size: 0.9rem;
}
</style>


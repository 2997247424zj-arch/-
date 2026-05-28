<template>
  <PassengerLayout>
    <div class="seat-selection-page">
      <!-- 返回按钮 -->
      <div class="page-header">
        <button class="back-btn" @click="handleBack">
          <span class="back-icon">←</span>
          <span>返回上一级</span>
        </button>
      </div>

      <!-- 航班信息卡片 -->
      <section class="glass-card flight-info-card">
        <div class="flight-info-header">
          <div>
            <h3>{{ flightInfo.flightNumber }}</h3>
            <p>{{ flightInfo.departure }} → {{ flightInfo.destination }}</p>
            <p>{{ flightInfo.departureTime }} - {{ flightInfo.arrivalTime }}</p>
          </div>
          <div class="flight-stats">
            <div class="stat-item">
              <span class="stat-label">已选座位</span>
              <span class="stat-value">{{ selectedSeats.length }}/{{ requiredSeatCount }}</span>
            </div>
            <div class="stat-item">
              <span class="stat-label">可用座位</span>
              <span class="stat-value">{{ availableSeatsCount }}</span>
            </div>
          </div>
        </div>
      </section>

      <!-- 座位图 -->
      <section class="glass-card seat-map-section">
        <div v-if="loading" class="loading-indicator">
          <p>正在加载座位布局...</p>
        </div>
        <div v-else>
          <div class="section-head">
            <div>
              <p class="section-label">座位选择</p>
              <h2>机舱座位图</h2>
            </div>
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
          
            </div>
          </div>

        <!-- 机舱布局 -->
        <div class="cabin-layout">
          <!-- 头等舱 -->
          <div v-if="firstClassSeats.length > 0" class="cabin-section business-class">
            <h4>头等舱</h4>
            <div class="seat-grid business-grid">
              <div
                v-for="seat in firstClassSeats"
                :key="seat.id"
                :class="['seat', seat.status, { 'selected': selectedSeats.includes(String(seat.id)) || selectedSeats.includes(seat.seatNumber) }]"
                @click="handleSeatClick(seat)"
                :title="`${seat.seatNumber} - ${getSeatStatusText(seat.status)}`"
              >
                <span class="seat-label">{{ seat.seatNumber }}</span>
                <span class="seat-badge">头等</span>
              </div>
            </div>
          </div>

          <!-- 商务舱 -->
          <div v-if="businessSeats.length > 0" class="cabin-section business-class">
            <h4>商务舱</h4>
            <div class="seat-grid business-grid">
              <div
                v-for="seat in businessSeats"
                :key="seat.id"
                :class="['seat', seat.status, { 'selected': selectedSeats.includes(String(seat.id)) || selectedSeats.includes(seat.seatNumber) }]"
                @click="handleSeatClick(seat)"
                :title="`${seat.seatNumber} - ${getSeatStatusText(seat.status)}`"
              >
                <span class="seat-label">{{ seat.seatNumber }}</span>
                <span v-if="seat.status === 'premium' || seat.cabinClass === '商务舱'" class="seat-badge">商务</span>
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
                :class="['seat', seat.status, { 'selected': selectedSeats.includes(String(seat.id)) || selectedSeats.includes(seat.seatNumber) }]"
                @click="handleSeatClick(seat)"
                :title="`${seat.seatNumber} - ${getSeatStatusText(seat.status)}`"
              >
                <span class="seat-label">{{ seat.seatNumber }}</span>
              </div>
            </div>
          </div>
        </div>

          <!-- 已选座位信息 -->
          <div v-if="selectedSeats.length > 0" class="selected-seats-info">
            <h4>已选座位 ({{ selectedSeats.length }}/{{ requiredSeatCount }})</h4>
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
      </section>

      <!-- 操作按钮 -->
      <div class="action-buttons">
        <button class="ghost-btn" @click="handleBack">取消</button>
        <button 
          class="primary-btn" 
          @click="confirmSelection"
          :disabled="selectedSeats.length !== requiredSeatCount"
        >
          确认选择 ({{ selectedSeats.length }}/{{ requiredSeatCount }})
        </button>
      </div>
    </div>
  </PassengerLayout>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import PassengerLayout from '../components/layout/PassengerLayout.vue'
import { seatApi } from '../services/api'

const router = useRouter()
const route = useRoute()

// 座位接口
interface Seat {
  id: string | number
  seatNumber: string
  row: number
  position: string
  cabinClass?: string
  status: 'available' | 'occupied' | 'reserved' | 'maintenance' | 'selected' | 'premium'
  price?: number
}

// 航班信息
const flightInfo = reactive({
  flightNumber: '',
  departure: '',
  destination: '',
  departureTime: '',
  arrivalTime: ''
})

// 座位数据
const allSeats = ref<Seat[]>([])
const selectedSeats = ref<string[]>([])
const requiredSeatCount = ref(1)
const loading = ref(false)

// 头等舱座位
const firstClassSeats = computed(() => {
  return allSeats.value.filter(s => s.cabinClass === '头等舱')
})

// 商务舱座位
const businessSeats = computed(() => {
  return allSeats.value.filter(s => s.cabinClass === '商务舱')
})

// 经济舱座位
const economySeats = computed(() => {
  return allSeats.value.filter(s => s.cabinClass === '经济舱')
})

// 可用座位数量
const availableSeatsCount = computed(() => {
  return allSeats.value.filter(s => 
    s.status === 'available' || 
    s.status === 'premium' || 
    s.status === 'reserved'
  ).length
})

// 从后端加载座位布局
const loadSeatLayout = async (flightId: string | number) => {
  loading.value = true
  try {
    // 先尝试创建座位（如果不存在）
    try {
      await seatApi.createSeatsForFlight(flightId)
    } catch (e) {
      // 如果创建失败（可能已存在），忽略错误
      console.log('座位可能已存在，继续加载')
    }

    // 获取座位布局
    const layoutData = await seatApi.getSeatLayoutByFlightId(flightId)
    
    if (layoutData && layoutData.seats) {
      // 转换后端数据格式到前端格式
      const seats: Seat[] = layoutData.seats.map((seat: any) => ({
        id: seat.id,
        seatNumber: seat.seatNumber,
        row: seat.row,
        position: seat.position,
        cabinClass: seat.cabinClass,
        status: convertSeatStatus(seat.status),
        price: seat.price || 0
      }))
      
      allSeats.value = seats
    } else {
      // 如果后端没有数据，使用默认布局
      generateDefaultSeatMap()
    }
  } catch (error: any) {
    console.error('加载座位布局失败:', error)
    // 如果加载失败，使用默认布局
    generateDefaultSeatMap()
  } finally {
    loading.value = false
  }
}

// 转换座位状态：严格跟随后端，可用/已占用
const convertSeatStatus = (status: string | null | undefined): Seat['status'] => {
  // 后端 status 为“可用”或英文 available 或为空，按可用处理
  if (!status || status === '可用' || status === 'available') {
    return 'available'
  }
  // 其他（已占用 / 维护中等）统一视为已占用
  return 'occupied'
}

// 生成默认座位图（作为后备方案）
const generateDefaultSeatMap = () => {
  const seats: Seat[] = []
  
  // 商务舱：2排，每排4个座位 (A, B, D, E)
  for (let row = 1; row <= 2; row++) {
    ['A', 'B', 'D', 'E'].forEach(letter => {
      const isOccupied = Math.random() > 0.7
      seats.push({
        id: `${row}${letter}`,
        seatNumber: `${row}${letter}`,
        row,
        position: letter,
        cabinClass: '商务舱',
        status: isOccupied ? 'occupied' : 'premium',
        price: 200
      })
    })
  }
  
  // 经济舱：10-30排，每排6个座位 (A, B, C, D, E, F)
  for (let row = 10; row <= 30; row++) {
    ['A', 'B', 'C', 'D', 'E', 'F'].forEach(letter => {
      const isOccupied = Math.random() > 0.6
      seats.push({
        id: `${row}${letter}`,
        seatNumber: `${row}${letter}`,
        row,
        position: letter,
        cabinClass: '经济舱',
        status: isOccupied ? 'occupied' : 'available',
        price: 0
      })
    })
  }
  
  allSeats.value = seats
}

// 处理座位点击
const handleSeatClick = (seat: Seat) => {
  if (seat.status === 'occupied' || seat.status === 'maintenance') {
    alert('该座位已被占用或维护中')
    return
  }
  
  const seatId = String(seat.id || seat.seatNumber)
  const index = selectedSeats.value.indexOf(seatId)
  
  if (index > -1) {
    // 取消选择
    selectedSeats.value.splice(index, 1)
    // 恢复座位状态为可用
    seat.status = 'available'
  } else {
    // 选择座位
    if (selectedSeats.value.length < requiredSeatCount.value) {
      selectedSeats.value.push(seatId)
      seat.status = 'selected'
    } else {
      alert(`最多只能选择 ${requiredSeatCount.value} 个座位`)
    }
  }
}

// 移除座位
const removeSeat = (seatId: string) => {
  const seat = allSeats.value.find(s => String(s.id) === seatId || s.seatNumber === seatId)
  if (seat) {
    seat.status = 'available'
  }
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

// 获取座位状态文本
const getSeatStatusText = (status: string) => {
  const statusMap: Record<string, string> = {
    available: '可用',
    occupied: '已占用',
    selected: '已选',
    premium: '商务舱'
  }
  return statusMap[status] || '未知'
}

// 确认选择
const confirmSelection = async () => {
  if (selectedSeats.value.length !== requiredSeatCount.value) {
    alert(`请选择 ${requiredSeatCount.value} 个座位`)
    return
  }

  const flightId = route.query.flightId as string

  // 调用后端接口，将选中的座位标记为已占用
  if (flightId && selectedSeats.value.length > 0) {
    try {
      await seatApi.updateSeatStatus(flightId, selectedSeats.value, 'occupied')
    } catch (e) {
      console.error('更新座位状态失败:', e)
      alert('更新座位状态失败，请稍后重试')
    }
  }

  // 保存选择的座位信息到 sessionStorage，供返回时使用
  const seatDetails = selectedSeats.value.map(seatId => {
    const seat = allSeats.value.find(s => String(s.id) === seatId || s.seatNumber === seatId)
    return {
      seatId,
      seatNumber: seat?.seatNumber || seatId,
      seatLabel: getSeatLabel(seatId),
      seatClass: seat?.cabinClass === '商务舱' || seat?.status === 'premium' ? 'business' : 'economy',
      price: seat?.price || 0,
      row: seat?.row,
      position: seat?.position
    }
  })
  
  sessionStorage.setItem('selectedSeats', JSON.stringify(selectedSeats.value))
  sessionStorage.setItem('seatDetails', JSON.stringify(seatDetails))
  sessionStorage.setItem('flightId', route.query.flightId as string || '')
  
  // 返回上一级
  handleBack()
}

// 返回上一级
const handleBack = () => {
  // 从 sessionStorage 获取返回路径，如果没有则返回乘客视图
  const returnPath = sessionStorage.getItem('seatSelectionReturnPath') || '/portal/passengers/view?tab=search'
  sessionStorage.removeItem('seatSelectionReturnPath')
  router.push(returnPath)
}

// 初始化
onMounted(async () => {
  // 从路由参数或 sessionStorage 获取航班信息
  const flightId = route.query.flightId as string
  const cabinClass = route.query.cabinClass as string || ''
  const flightData = sessionStorage.getItem(`flight_${flightId}`)
  
  if (flightData) {
    try {
      const flight = JSON.parse(flightData)
      flightInfo.flightNumber = flight.flightNumber || ''
      flightInfo.departure = flight.departure || ''
      flightInfo.destination = flight.destination || ''
      flightInfo.departureTime = flight.departureTime || ''
      flightInfo.arrivalTime = flight.arrivalTime || ''
      
      // 获取需要的座位数量
      requiredSeatCount.value = parseInt(route.query.passengers as string || '1')
      
      // 如果有已选座位，恢复选择状态
      const savedSeats = sessionStorage.getItem('selectedSeats')
      if (savedSeats) {
        try {
          selectedSeats.value = JSON.parse(savedSeats)
        } catch (e) {
          console.error('解析已选座位失败:', e)
        }
      }
    } catch (e) {
      console.error('解析航班信息失败:', e)
    }
  } else {
    // 如果没有航班信息，尝试从 query 参数获取
    flightInfo.flightNumber = route.query.flightNumber as string || ''
    flightInfo.departure = route.query.departure as string || ''
    flightInfo.destination = route.query.destination as string || ''
    flightInfo.departureTime = route.query.departureTime as string || ''
    flightInfo.arrivalTime = route.query.arrivalTime as string || ''
    requiredSeatCount.value = parseInt(route.query.passengers as string || '1')
  }
  
  // 从后端加载座位布局
  if (flightId) {
    await loadSeatLayout(flightId)
  } else {
    // 如果没有航班ID，使用默认布局
    generateDefaultSeatMap()
  }
  
  // 如果传入了舱位等级，只保留对应舱位的座位
  if (cabinClass) {
    const cabinMap: Record<string, string> = {
      economy: '经济舱',
      business: '商务舱',
      first: '头等舱'
    }
    const target = cabinMap[cabinClass] || cabinClass
    allSeats.value = allSeats.value.filter(s => s.cabinClass === target)
  }

  // 恢复已选座位的状态
  if (selectedSeats.value.length > 0) {
    selectedSeats.value.forEach(seatId => {
      const seat = allSeats.value.find(s => String(s.id) === seatId || s.seatNumber === seatId)
      if (seat) {
        seat.status = 'selected'
      }
    })
  }
})
</script>

<style scoped>
.seat-selection-page {
  padding: 2rem;
  max-width: 1400px;
  margin: 0 auto;
  min-height: calc(100vh - 200px);
}

.page-header {
  margin-bottom: 2rem;
}

.back-btn {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem 1.5rem;
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 12px;
  background: rgba(15, 23, 42, 0.6);
  color: rgba(255, 255, 255, 0.9);
  font-size: 0.95rem;
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
  transition: transform 0.3s;
}

.back-btn:hover .back-icon {
  transform: translateX(-2px);
}

.glass-card {
  border-radius: 28px;
  padding: 1.8rem;
  background: rgba(2, 6, 23, 0.7);
  border: 1px solid rgba(255, 255, 255, 0.06);
  box-shadow:
    0 25px 50px rgba(2, 6, 23, 0.6),
    inset 0 1px rgba(255, 255, 255, 0.08);
  backdrop-filter: blur(18px);
  margin-bottom: 1.5rem;
}

.flight-info-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 2rem;
  flex-wrap: wrap;
}

.flight-info-header h3 {
  margin: 0 0 0.5rem 0;
  color: #fff;
  font-size: 1.5rem;
}

.flight-info-header p {
  margin: 0.3rem 0;
  color: rgba(248, 250, 252, 0.7);
}

.flight-stats {
  display: flex;
  gap: 2rem;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.3rem;
}

.stat-label {
  font-size: 0.85rem;
  color: rgba(248, 250, 252, 0.6);
}

.stat-value {
  font-size: 1.5rem;
  font-weight: 700;
  color: #fff;
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
}

.seat-legend {
  display: flex;
  gap: 1rem;
  flex-wrap: wrap;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.85rem;
  color: rgba(248, 250, 252, 0.7);
}

.legend-color {
  width: 20px;
  height: 20px;
  border-radius: 4px;
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.legend-color.available {
  background: rgba(34, 197, 94, 0.3);
  border-color: rgba(34, 197, 94, 0.5);
}

.legend-color.selected {
  background: rgba(99, 102, 241, 0.5);
  border-color: rgba(99, 102, 241, 0.7);
}

.legend-color.occupied {
  background: rgba(148, 163, 184, 0.3);
  border-color: rgba(148, 163, 184, 0.5);
}

.legend-color.premium {
  background: rgba(245, 158, 11, 0.3);
  border-color: rgba(245, 158, 11, 0.5);
}

.cabin-layout {
  display: flex;
  flex-direction: column;
  gap: 2rem;
}

.cabin-section {
  padding: 1.5rem;
  border-radius: 16px;
  background: rgba(15, 23, 42, 0.6);
}

.cabin-section h4 {
  margin: 0 0 1rem 0;
  color: #fff;
  font-size: 1.1rem;
}

.seat-grid {
  display: grid;
  gap: 0.5rem;
  justify-content: center;
}

.business-grid {
  grid-template-columns: repeat(4, 60px);
}

.economy-grid {
  grid-template-columns: repeat(6, 50px);
}

.seat {
  width: 50px;
  height: 50px;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 2px solid transparent;
  position: relative;
  font-size: 0.75rem;
}

.seat.available {
  background: rgba(34, 197, 94, 0.2);
  border-color: rgba(34, 197, 94, 0.4);
}

.seat.available:hover {
  background: rgba(34, 197, 94, 0.4);
  transform: scale(1.1);
}

.seat.selected {
  background: rgba(99, 102, 241, 0.5);
  border-color: rgba(99, 102, 241, 0.8);
  box-shadow: 0 0 10px rgba(99, 102, 241, 0.5);
}

.seat.occupied {
  background: rgba(148, 163, 184, 0.2);
  border-color: rgba(148, 163, 184, 0.4);
  cursor: not-allowed;
  opacity: 0.5;
}

.seat.premium {
  background: rgba(245, 158, 11, 0.2);
  border-color: rgba(245, 158, 11, 0.4);
}

.seat.premium:hover {
  background: rgba(245, 158, 11, 0.4);
  transform: scale(1.1);
}

.seat-label {
  font-weight: 600;
  color: #fff;
}

.seat-badge {
  font-size: 0.6rem;
  color: rgba(245, 158, 11, 1);
  margin-top: 2px;
}

.selected-seats-info {
  margin-top: 2rem;
  padding-top: 1.5rem;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

.selected-seats-info h4 {
  margin: 0 0 1rem 0;
  color: #fff;
}

.selected-seats-list {
  display: flex;
  flex-wrap: wrap;
  gap: 0.8rem;
}

.selected-seat-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.5rem 1rem;
  border-radius: 8px;
  background: rgba(99, 102, 241, 0.2);
  border: 1px solid rgba(99, 102, 241, 0.4);
  color: #fff;
}

.remove-btn {
  padding: 0.2rem 0.5rem;
  border: none;
  border-radius: 4px;
  background: rgba(248, 113, 113, 0.3);
  color: #fff;
  cursor: pointer;
  font-size: 0.75rem;
  transition: all 0.3s;
}

.remove-btn:hover {
  background: rgba(248, 113, 113, 0.5);
}

.action-buttons {
  display: flex;
  justify-content: flex-end;
  gap: 1rem;
  margin-top: 2rem;
  padding: 1.5rem 0;
}

.primary-btn,
.ghost-btn {
  border-radius: 999px;
  padding: 0.75rem 2rem;
  border: 1px solid transparent;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
  font-size: 1rem;
}

.primary-btn {
  background: linear-gradient(135deg, #1E8AE6, #0A2F63);
  color: #fff;
  box-shadow: 0 10px 25px rgba(99, 102, 241, 0.35);
}

.primary-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 15px 35px rgba(99, 102, 241, 0.45);
}

.primary-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.ghost-btn {
  border-color: rgba(255, 255, 255, 0.35);
  background: transparent;
  color: #f8fafc;
}

.ghost-btn:hover {
  background: rgba(255, 255, 255, 0.1);
}

.loading-indicator {
  text-align: center;
  padding: 3rem;
  color: rgba(255, 255, 255, 0.7);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .seat-selection-page {
    padding: 1rem;
  }
  
  .economy-grid {
    grid-template-columns: repeat(6, 40px);
  }
  
  .business-grid {
    grid-template-columns: repeat(4, 50px);
  }
  
  .seat {
    width: 40px;
    height: 40px;
    font-size: 0.65rem;
  }
  
  .action-buttons {
    flex-direction: column;
  }
  
  .primary-btn,
  .ghost-btn {
    width: 100%;
  }
}
</style>


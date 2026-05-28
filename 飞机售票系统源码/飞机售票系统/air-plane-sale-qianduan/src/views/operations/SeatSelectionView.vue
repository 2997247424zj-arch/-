<template>
  <AdminLayout>
    <div class="page-container seat-selection-page">
      <div class="breadcrumb">
        <span>航空运营</span>
        <span class="breadcrumb-separator">/</span>
        <span>座位选择</span>
      </div>

      <header class="page-header">
        <div>
          <div class="header-top">
            <button class="back-btn" @click="handleBack">
              <span class="back-icon">←</span>
              <span>返回运营控制台</span>
            </button>
          </div>
          <p class="page-label">运营控制 · 座位管理</p>
          <h1>座位选择与管理</h1>
          <p>为乘客提供可视化座位选择，支持实时座位状态更新和座位偏好设置</p>
        </div>
        <div class="page-actions">
          <button class="ghost-btn" @click="refreshSeatMap">刷新座位图</button>
          <button class="primary-btn" @click="handleSaveSelection">保存选择</button>
        </div>
      </header>

      <!-- 航班信息 -->
      <section class="glass-card flight-info-card">
        <div class="flight-info-header">
          <div>
            <h3>{{ flightInfo.flightNumber }}</h3>
            <p>{{ flightInfo.departure }} → {{ flightInfo.destination }}</p>
            <p>{{ flightInfo.date }} {{ formatTime(flightInfo.departureTime || '') }} - {{ formatTime(flightInfo.arrivalTime || '') }}</p>
          </div>
          <div class="flight-stats">
            <div class="stat-item">
              <span class="stat-label">总座位</span>
              <span class="stat-value">{{ seatMap.totalSeats }}</span>
            </div>
            <div class="stat-item">
              <span class="stat-label">已选</span>
              <span class="stat-value">{{ seatMap.selectedSeats.length }}</span>
            </div>
            <div class="stat-item">
              <span class="stat-label">可用</span>
              <span class="stat-value">{{ seatMap.availableSeats }}</span>
            </div>
          </div>
        </div>
      </section>

      <!-- 座位图 -->
      <section class="glass-card seat-map-section">
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
            <div class="legend-item">
              <span class="legend-color premium"></span>
              <span>商务舱</span>
            </div>
          </div>
        </div>

        <!-- 机舱布局 -->
        <div class="cabin-layout">
          <!-- 商务舱 -->
          <div class="cabin-section business-class">
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
          <h4>已选座位 ({{ selectedSeats.length }})</h4>
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
      </section>

      <!-- 价格信息 -->
      <section class="glass-card pricing-section">
        <div class="section-head">
          <div>
            <p class="section-label">价格计算</p>
            <h2>费用明细</h2>
          </div>
        </div>
        <div class="price-breakdown">
          <div class="price-item">
            <span>基础票价</span>
            <span>¥{{ pricing.basePrice }}</span>
          </div>
          <div class="price-item">
            <span>座位选择费</span>
            <span>¥{{ pricing.seatFee }}</span>
          </div>
          <div class="price-item">
            <span>税费</span>
            <span>¥{{ pricing.tax }}</span>
          </div>
          <div class="price-item total">
            <span>总计</span>
            <span>¥{{ pricing.total }}</span>
          </div>
        </div>
      </section>
    </div>
  </AdminLayout>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import AdminLayout from '../../components/AdminLayout.vue'
import { formatTime } from '../../utils/dateFormat'

const router = useRouter()

// 将时间减去8小时（用于时区转换）
const subtractHours = (timeStr: string, hours: number = 8): string => {
  if (!timeStr) return ''
  try {
    // 提取HH:mm格式的时间
    let timePart = ''
    if (timeStr.includes(' ')) {
      const parts = timeStr.split(' ')
      if (parts.length >= 2) {
        timePart = parts[1].substring(0, 5) // 提取 HH:mm 部分
      }
    } else if (timeStr.includes('T')) {
      const parts = timeStr.split('T')
      if (parts.length >= 2) {
        timePart = parts[1].substring(0, 5) // 提取 HH:mm 部分
      }
    } else if (timeStr.match(/^\d{2}:\d{2}$/)) {
      timePart = timeStr // 已经是HH:mm格式
    } else {
      timePart = timeStr.substring(0, 5) // 尝试提取前5个字符
    }
    
    if (!timePart || !timePart.match(/^\d{2}:\d{2}$/)) {
      return timeStr // 如果无法解析，返回原值
    }
    
    // 解析小时和分钟
    const [hourStr, minuteStr] = timePart.split(':')
    let hour = parseInt(hourStr, 10)
    const minute = parseInt(minuteStr, 10)
    
    // 减去指定小时数
    hour -= hours
    
    // 处理跨日情况
    if (hour < 0) {
      hour += 24
    }
    
    // 格式化为HH:mm
    return `${String(hour).padStart(2, '0')}:${String(minute).padStart(2, '0')}`
  } catch {
    return timeStr
  }
}

// 航班信息
const flightInfo = reactive({
  flightNumber: 'CA1234',
  departure: '北京',
  destination: '上海',
  date: '2025-11-20',
  departureTime: '08:00',
  arrivalTime: '10:30'
})

// 座位数据
interface Seat {
  id: string
  row: number
  letter: string
  status: 'available' | 'occupied' | 'selected' | 'premium'
  price?: number
}

const allSeats = ref<Seat[]>([])
const selectedSeats = ref<string[]>([])

// 生成座位图
const generateSeatMap = () => {
  const seats: Seat[] = []
  
  // 商务舱：2排，每排4个座位 (A, B, D, E)
  for (let row = 1; row <= 2; row++) {
    ['A', 'B', 'D', 'E'].forEach(letter => {
      seats.push({
        id: `${row}${letter}`,
        row,
        letter,
        status: Math.random() > 0.7 ? 'occupied' : 'premium',
        price: 1280
      })
    })
  }
  
  // 经济舱：20排，每排6个座位 (A, B, C, D, E, F)
  for (let row = 3; row <= 22; row++) {
    ['A', 'B', 'C', 'D', 'E', 'F'].forEach(letter => {
      seats.push({
        id: `${row}${letter}`,
        row,
        letter,
        status: Math.random() > 0.6 ? 'available' : 'occupied',
        price: 680
      })
    })
  }
  
  allSeats.value = seats
}

// 商务舱座位
const businessSeats = computed(() => {
  return allSeats.value.filter(seat => seat.row <= 2)
})

// 经济舱座位
const economySeats = computed(() => {
  return allSeats.value.filter(seat => seat.row > 2)
})

// 座位图统计
const seatMap = computed(() => {
  const totalSeats = allSeats.value.length
  const availableSeats = allSeats.value.filter(s => s.status === 'available' || s.status === 'premium').length
  return {
    totalSeats,
    selectedSeats: selectedSeats.value,
    availableSeats
  }
})

// 价格计算
const pricing = computed(() => {
  let basePrice = 0
  let seatFee = 0
  
  selectedSeats.value.forEach(seatId => {
    const seat = allSeats.value.find(s => s.id === seatId)
    if (seat) {
      basePrice += seat.price || 680
      if (seat.status === 'premium') {
        seatFee += 200 // 商务舱座位选择费
      } else {
        seatFee += 50 // 经济舱座位选择费
      }
    }
  })
  
  const tax = (basePrice + seatFee) * 0.1
  const total = basePrice + seatFee + tax
  
  return {
    basePrice: basePrice.toFixed(2),
    seatFee: seatFee.toFixed(2),
    tax: tax.toFixed(2),
    total: total.toFixed(2)
  }
})

// 处理座位点击
const handleSeatClick = (seat: Seat) => {
  if (seat.status === 'occupied') {
    alert('该座位已被占用')
    return
  }
  
  const index = selectedSeats.value.indexOf(seat.id)
  if (index > -1) {
    selectedSeats.value.splice(index, 1)
    seat.status = seat.row <= 2 ? 'premium' : 'available'
  } else {
    selectedSeats.value.push(seat.id)
    seat.status = 'selected'
  }
}

// 移除座位
const removeSeat = (seatId: string) => {
  const seat = allSeats.value.find(s => s.id === seatId)
  if (seat) {
    seat.status = seat.row <= 2 ? 'premium' : 'available'
  }
  const index = selectedSeats.value.indexOf(seatId)
  if (index > -1) {
    selectedSeats.value.splice(index, 1)
  }
}

// 获取座位标签
const getSeatLabel = (seatId: string) => {
  const seat = allSeats.value.find(s => s.id === seatId)
  return seat ? `${seat.row}${seat.letter}` : seatId
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

// 返回运营控制台
const handleBack = () => {
  router.push('/portal/operations')
}

// 刷新座位图
const refreshSeatMap = () => {
  generateSeatMap()
  selectedSeats.value = []
  alert('座位图已刷新')
}

// 保存选择
const handleSaveSelection = () => {
  if (selectedSeats.value.length === 0) {
    alert('请至少选择一个座位')
    return
  }
  alert(`已保存 ${selectedSeats.value.length} 个座位选择`)
}

onMounted(() => {
  generateSeatMap()
})
</script>

<style scoped>
.seat-selection-page {
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
  background: rgba(15, 23, 42, 0.6);
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
  transition: transform 0.3s;
}

.back-btn:hover .back-icon {
  transform: translateX(-2px);
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

.glass-card {
  border-radius: 28px;
  padding: 1.8rem;
  background: rgba(2, 6, 23, 0.7);
  border: 1px solid rgba(255, 255, 255, 0.06);
  box-shadow:
    0 25px 50px rgba(2, 6, 23, 0.6),
    inset 0 1px rgba(255, 255, 255, 0.08);
  backdrop-filter: blur(18px);
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

.pricing-section {
  margin-top: 1.5rem;
}

.price-breakdown {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.price-item {
  display: flex;
  justify-content: space-between;
  padding: 0.8rem 0;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  color: rgba(248, 250, 252, 0.8);
}

.price-item.total {
  border-bottom: none;
  border-top: 2px solid rgba(255, 255, 255, 0.2);
  padding-top: 1rem;
  font-size: 1.3rem;
  font-weight: 700;
  color: #fcd34d;
}
</style>


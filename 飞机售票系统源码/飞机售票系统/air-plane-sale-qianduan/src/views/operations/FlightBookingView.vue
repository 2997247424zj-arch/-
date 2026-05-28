<template>
  <AdminLayout>
    <div class="page-container booking-page">
      <div class="breadcrumb">
        <span>航空运营</span>
        <span class="breadcrumb-separator">/</span>
        <span>机票预订</span>
      </div>

      <header class="page-header">
        <div>
          <div class="header-top">
            <button class="back-btn" @click="handleBack">
              <span class="back-icon">←</span>
              <span>返回运营控制台</span>
            </button>
          </div>
          <p class="page-label">运营控制 · 机票预订</p>
          <h1>机票预订管理</h1>
          <p>为乘客提供航班查询、座位选择、票价计算和预订服务</p>
        </div>
        <div class="page-actions">
          <button class="ghost-btn" @click="handleExport">导出预订记录</button>
          <button class="primary-btn" @click="showBookingModal = true">新建预订</button>
        </div>
      </header>

      <!-- 航班搜索 -->
      <section class="glass-card search-section">
        <div class="section-head">
          <div>
            <p class="section-label">航班查询</p>
            <h2>搜索可用航班</h2>
          </div>
        </div>
        <form @submit.prevent="handleSearchFlights" class="search-form">
          <div class="form-row">
            <div class="form-group">
              <label>出发城市</label>
              <input 
                v-model="searchForm.departure" 
                type="text" 
                placeholder="例如：北京"
                required
              />
            </div>
            <div class="form-group">
              <label>到达城市</label>
              <input 
                v-model="searchForm.destination" 
                type="text" 
                placeholder="例如：上海"
                required
              />
            </div>
            <div class="form-group">
              <label>出发日期</label>
              <input 
                v-model="searchForm.date" 
                type="date"
                required
              />
            </div>
            <div class="form-group">
              <label>乘客人数</label>
              <select v-model="searchForm.passengers">
                <option value="1">1 人</option>
                <option value="2">2 人</option>
                <option value="3">3 人</option>
                <option value="4">4 人</option>
              </select>
            </div>
          </div>
          <div class="form-actions">
            <button type="submit" class="primary-btn" :disabled="searchLoading">
              {{ searchLoading ? '搜索中...' : '搜索航班' }}
            </button>
            <button type="button" class="ghost-btn" @click="resetSearch">重置</button>
          </div>
        </form>
      </section>

      <!-- 搜索结果 -->
      <section v-if="searchResults.length > 0" class="glass-card results-section">
        <div class="section-head">
          <div>
            <p class="section-label">搜索结果</p>
            <h2>找到 {{ searchResults.length }} 个可用航班</h2>
          </div>
        </div>
        <div class="flight-list">
          <div 
            v-for="flight in searchResults" 
            :key="flight.id" 
            class="flight-item"
            @click="selectFlight(flight)"
          >
            <div class="flight-info">
              <div class="flight-header">
                <h3>{{ flight.flightNumber }}</h3>
                <span class="airline-name">{{ flight.airline }}</span>
              </div>
              <div class="flight-route">
                <div class="route-item">
                  <span class="time">{{ formatTime(flight.departureTime || '') }}</span>
                  <span class="city">{{ flight.departure }}</span>
                </div>
                <div class="route-arrow">→</div>
                <div class="route-item">
                  <span class="time">{{ formatTime(flight.arrivalTime || '') }}</span>
                  <span class="city">{{ flight.destination }}</span>
                </div>
              </div>
              <div class="flight-details">
                <span>飞行时长：{{ flight.duration }}</span>
                <span>可用座位：{{ flight.seats }} 个</span>
              </div>
            </div>
            <div class="flight-pricing">
              <div class="price-section">
                <span class="price-label">经济舱</span>
                <span class="price-value">¥{{ flight.price }}</span>
              </div>
              <div class="price-section">
                <span class="price-label">商务舱</span>
                <span class="price-value">¥{{ flight.businessPrice || flight.price * 1.8 }}</span>
              </div>
              <button class="book-btn" @click.stop="handleBookFlight(flight)">立即预订</button>
            </div>
          </div>
        </div>
      </section>

      <!-- 预订模态框 -->
      <div v-if="showBookingModal && selectedFlight" class="modal-overlay passenger-modal" @click="showBookingModal = false">
        <div class="modal-content passenger-modal-content" @click.stop>
          <div class="modal-header">
            <h3>预订航班 {{ selectedFlight.flightNumber }}</h3>
            <button class="close-btn" @click="showBookingModal = false">×</button>
          </div>
          <div class="modal-body">
            <div class="booking-form">
              <div class="form-section">
                <h4>航班信息</h4>
                <div class="info-row">
                  <span>航线：</span>
                  <strong>{{ selectedFlight.departure }} → {{ selectedFlight.destination }}</strong>
                </div>
                <div class="info-row">
                  <span>时间：</span>
                  <strong>{{ formatTime(selectedFlight.departureTime || '') }} - {{ formatTime(selectedFlight.arrivalTime || '') }}</strong>
                </div>
                <div class="info-row">
                  <span>日期：</span>
                  <strong>{{ searchForm.date }}</strong>
                </div>
              </div>

              <div class="form-section">
                <h4>快速选择乘客</h4>
                <p class="section-desc">从常用乘客中快速选择，或为其他人购票</p>
                <FrequentPassengers
                  @select="handleSelectFrequentPassenger"
                />
              </div>

              <div class="form-section">
                <h4>乘客信息</h4>
                <PassengerManager
                  v-model="bookingForm.passengers"
                  :max-passengers="4"
                  :show-validation-summary="true"
                  @validate="passengerValidationStatus = $event"
                />
              </div>

              <div class="form-section">
                <h4>联系信息</h4>
                <div class="form-row">
                  <div class="form-group">
                    <label>联系人姓名</label>
                    <input v-model="bookingForm.contact.name" type="text" required />
                  </div>
                  <div class="form-group">
                    <label>联系电话</label>
                    <input v-model="bookingForm.contact.phone" type="tel" required />
                  </div>
                </div>
                <div class="form-group">
                  <label>联系邮箱</label>
                  <input v-model="bookingForm.contact.email" type="email" required />
                </div>
              </div>

              <div class="form-section">
                <h4>价格计算</h4>
                <div class="price-breakdown">
                  <div class="price-item">
                    <span>机票费用</span>
                    <span>¥{{ calculatedPrice.basePrice }}</span>
                  </div>
                  <div class="price-item">
                    <span>税费</span>
                    <span>¥{{ calculatedPrice.tax }}</span>
                  </div>
                  <div class="price-item total">
                    <span>总计</span>
                    <span>¥{{ calculatedPrice.total }}</span>
                  </div>
                </div>
              </div>

              <div class="form-actions">
                <button class="ghost-btn" @click="showBookingModal = false">取消</button>
                <button class="primary-btn" @click="handleConfirmBooking" :disabled="bookingLoading">
                  {{ bookingLoading ? '预订中...' : '确认预订' }}
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </AdminLayout>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import AdminLayout from '../../components/AdminLayout.vue'
import PassengerManager from '../../components/PassengerManager.vue'
import FrequentPassengers from '../../components/FrequentPassengers.vue'
import { flightApi } from '../../services/api'
import ModalPrompt from '../../components/ModalPrompt.vue'
import { formatTime } from '../../utils/dateFormat'

const router = useRouter()

// 搜索表单
const searchForm = reactive({
  departure: '',
  destination: '',
  date: '',
  passengers: '1'
})

// 搜索结果
const searchResults = ref<any[]>([])
const searchLoading = ref(false)

// 预订表单
const bookingForm = reactive({
  passengers: [
    {
      name: '',
      idCard: '',
      seatClass: 'economy',
      seatPreference: 'any',
      passengerType: 'adult',
      phone: ''
    }
  ],
  contact: {
    name: '',
    phone: '',
    email: ''
  }
})

// 乘客验证状态
const passengerValidationStatus = ref(false)

const selectedFlight = ref<any>(null)
const showBookingModal = ref(false)
const bookingLoading = ref(false)

// 计算价格
const calculatedPrice = computed(() => {
  if (!selectedFlight.value) {
    return { basePrice: 0, tax: 0, total: 0 }
  }
  
  let basePrice = 0
  bookingForm.passengers.forEach(passenger => {
    if (passenger.seatClass === 'economy') {
      basePrice += selectedFlight.value.price
    } else {
      basePrice += (selectedFlight.value.businessPrice || selectedFlight.value.price * 1.8)
    }
  })
  
  const tax = basePrice * 0.1 // 10% 税费
  const total = basePrice + tax
  
  return {
    basePrice: basePrice.toFixed(2),
    tax: tax.toFixed(2),
    total: total.toFixed(2)
  }
})

// 搜索航班
const handleSearchFlights = async () => {
  searchLoading.value = true
  try {
    const result = await flightApi.searchFlights({
      departure: searchForm.departure,
      destination: searchForm.destination,
      date: searchForm.date,
      passengers: parseInt(searchForm.passengers)
    })
    searchResults.value = result.flights || []
  } catch (error) {
    console.error('搜索航班失败:', error)
    alert('搜索航班失败，请重试')
  } finally {
    searchLoading.value = false
  }
}

// 重置搜索
const resetSearch = () => {
  searchForm.departure = ''
  searchForm.destination = ''
  searchForm.date = ''
  searchForm.passengers = '1'
  searchResults.value = []
}

// 选择航班
const selectFlight = (flight: any) => {
  selectedFlight.value = flight
  showBookingModal.value = true
}

// 添加乘客 - 由 PassengerManager 组件处理

// 处理选择常用乘客
const handleSelectFrequentPassenger = (passenger: any) => {
  // 将选中的乘客信息填充到第一个乘客位置
  if (bookingForm.passengers.length > 0) {
    bookingForm.passengers[0].name = passenger.name
    bookingForm.passengers[0].idCard = passenger.idCard
    bookingForm.passengers[0].phone = passenger.phone || ''
    bookingForm.passengers[0].seatClass = 'economy'
    bookingForm.passengers[0].seatPreference = 'any'
    bookingForm.passengers[0].passengerType = 'adult'
  }
}

// 预订航班
const handleBookFlight = (flight: any) => {
  selectedFlight.value = flight
  showBookingModal.value = true
}

// 确认预订
const handleConfirmBooking = async () => {
  if (!selectedFlight.value) return
  
  // 验证乘客信息
  if (!passengerValidationStatus.value) {
    alert('请填写完整的乘客信息并通过验证')
    return
  }
  
  bookingLoading.value = true
  try {
    const result = await flightApi.bookFlight({
      flightId: selectedFlight.value.id,
      passengers: bookingForm.passengers,
      contactInfo: bookingForm.contact
    })
    
    alert('预订成功！订单号：' + result.orderId)
    showBookingModal.value = false
    resetBookingForm()
  } catch (error: any) {
    alert('预订失败：' + (error.message || '请重试'))
  } finally {
    bookingLoading.value = false
  }
}

// 重置预订表单
const resetBookingForm = () => {
  bookingForm.passengers = [{
    name: '',
    idCard: '',
    seatClass: 'economy',
    seatPreference: 'any',
    passengerType: 'adult',
    phone: ''
  }]
  bookingForm.contact = {
    name: '',
    phone: '',
    email: ''
  }
  passengerValidationStatus.value = false
}

// 返回运营控制台

const handleBack = () => {
  router.push('/portal/operations')
}

// 导出
const handleExport = () => {
  alert('导出功能开发中')
}
</script>

<style scoped>
.booking-page {
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

.search-form {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.form-row {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 1rem;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.form-group label {
  font-size: 0.9rem;
  color: rgba(248, 250, 252, 0.75);
}

.form-group input,
.form-group select {
  padding: 0.75rem 1rem;
  border-radius: 12px;
  border: 1px solid rgba(255, 255, 255, 0.15);
  background: rgba(15, 23, 42, 0.6);
  color: #fff;
  font-size: 0.95rem;
}

.form-actions {
  display: flex;
  gap: 0.8rem;
  justify-content: flex-end;
}

.flight-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.flight-item {
  display: flex;
  justify-content: space-between;
  gap: 1.5rem;
  padding: 1.5rem;
  border-radius: 16px;
  border: 1px solid rgba(255, 255, 255, 0.1);
  background: rgba(15, 23, 42, 0.6);
  cursor: pointer;
  transition: all 0.3s ease;
}

.flight-item:hover {
  border-color: rgba(99, 102, 241, 0.5);
  background: rgba(15, 23, 42, 0.8);
  transform: translateY(-2px);
}

.flight-info {
  flex: 1;
}

.flight-header {
  display: flex;
  align-items: center;
  gap: 1rem;
  margin-bottom: 1rem;
}

.flight-header h3 {
  margin: 0;
  color: #fff;
  font-size: 1.3rem;
}

.airline-name {
  color: rgba(248, 250, 252, 0.7);
  font-size: 0.9rem;
}

.flight-route {
  display: flex;
  align-items: center;
  gap: 1rem;
  margin-bottom: 0.8rem;
}

.route-item {
  display: flex;
  flex-direction: column;
  gap: 0.3rem;
}

.route-item .time {
  font-size: 1.2rem;
  font-weight: 700;
  color: #fff;
}

.route-item .city {
  font-size: 0.9rem;
  color: rgba(248, 250, 252, 0.7);
}

.route-arrow {
  font-size: 1.5rem;
  color: rgba(248, 250, 252, 0.5);
}

.flight-details {
  display: flex;
  gap: 1.5rem;
  font-size: 0.85rem;
  color: rgba(248, 250, 252, 0.6);
}

.flight-pricing {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 1rem;
}

.price-section {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 0.3rem;
}

.price-label {
  font-size: 0.85rem;
  color: rgba(248, 250, 252, 0.6);
}

.price-value {
  font-size: 1.3rem;
  font-weight: 700;
  color: #fcd34d;
}

.book-btn {
  padding: 0.75rem 1.5rem;
  border: none;
  border-radius: 12px;
  background: linear-gradient(135deg, #1E8AE6, #0A2F63);
  color: #fff;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.book-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 25px rgba(99, 102, 241, 0.4);
}

/* 模态框样式 */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(6, 11, 40, 0.75);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 10000;
  padding: 20px;
}

/* 乘客相关弹窗 - 页面顶部居中 */
.modal-overlay.passenger-modal {
  align-items: flex-start;
  justify-content: center;
  padding-top: 80px;
  padding-bottom: 20px;
}

.modal-content {
  background: rgba(15, 23, 42, 0.95);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 24px;
  width: 90%;
  max-width: 800px;
  max-height: 90vh;
  overflow-y: auto;
  box-shadow: 0 25px 50px rgba(15, 23, 42, 0.5);
  margin: auto;
}

.modal-content.passenger-modal-content {
  margin-top: 0;
  max-height: calc(100vh - 160px);
}

.modal-header {
  padding: 24px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.modal-header h3 {
  margin: 0;
  color: #fff;
  font-size: 1.5rem;
}

.close-btn {
  background: none;
  border: none;
  font-size: 28px;
  color: rgba(255, 255, 255, 0.7);
  cursor: pointer;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
  transition: all 0.3s;
}

.close-btn:hover {
  background: rgba(255, 255, 255, 0.1);
  color: #fff;
}

.modal-body {
  padding: 24px;
}

.booking-form {
  display: flex;
  flex-direction: column;
  gap: 2rem;
}

.form-section {
  padding-bottom: 1.5rem;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.form-section:last-child {
  border-bottom: none;
}

.form-section h4 {
  margin: 0 0 1rem 0;
  color: #fff;
  font-size: 1.2rem;
}

.form-section h5 {
  margin: 0 0 0.8rem 0;
  color: rgba(248, 250, 252, 0.9);
  font-size: 1rem;
}

.info-row {
  display: flex;
  gap: 0.5rem;
  margin-bottom: 0.5rem;
  color: rgba(248, 250, 252, 0.8);
}

.passenger-form {
  padding: 1rem;
  border-radius: 12px;
  background: rgba(15, 23, 42, 0.6);
  margin-bottom: 1rem;
}

.add-passenger-btn {
  width: 100%;
  margin-top: 0.5rem;
}

.price-breakdown {
  display: flex;
  flex-direction: column;
  gap: 0.8rem;
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
  font-size: 1.2rem;
  font-weight: 700;
  color: #fff;
}

.form-actions {
  display: flex;
  gap: 1rem;
  justify-content: flex-end;
  margin-top: 1rem;
}

.section-desc {
  margin: -0.5rem 0 1rem 0;
  color: rgba(248, 250, 252, 0.6);
  font-size: 0.9rem;
}
</style>


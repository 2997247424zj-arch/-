<template>
  <PassengerLayout>
    <div class="page-container special-passenger-page">
      <div class="breadcrumb">
        <span @click="goBack" class="breadcrumb-link">首页</span>
        <span class="breadcrumb-separator">/</span>
        <span>重点旅客预约</span>
      </div>

      <header class="page-header">
        <div>
          <p class="page-label">特殊服务</p>
          <h1>重点旅客预约</h1>
          <p>为需要特殊协助的旅客提供专业的服务预约服务</p>
        </div>
        <div class="page-actions">
          <button class="primary-btn" @click="showCreateForm = !showCreateForm">
            {{ showCreateForm ? '取消' : '新建预约' }}
          </button>
        </div>
      </header>

      <!-- 新建预约表单 -->
      <section v-if="showCreateForm" class="glass-card create-form-section">
        <div class="section-header">
          <h2>新建预约申请</h2>
        </div>
        <form @submit.prevent="submitRequest" class="request-form">
          <!-- 订单选择 -->
          <div class="form-group">
            <label for="orderId">选择订单 <span class="required">*</span></label>
            <div class="select-wrapper">
              <button 
                type="button" 
                class="select-button"
                @click="toggleOrderSelector"
                :class="{ 'has-value': selectedOrder }"
              >
                {{ selectedOrder ? selectedOrder.orderNo : '请选择订单' }}
                <span class="arrow">▼</span>
              </button>
              <div v-if="showOrderSelector" class="dropdown-menu order-dropdown">
                <div class="dropdown-item" @click="goToOrderList">
                  <span>从订单列表选择...</span>
                </div>
                <div 
                  v-for="order in availableOrders" 
                  :key="order.id"
                  class="dropdown-item"
                  @click="selectOrder(order)"
                >
                  <div class="order-item">
                    <span class="order-no">{{ order.orderNo || order.order_no }}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 联系电话 -->
          <div class="form-group">
            <label for="phone">联系电话 <span class="required">*</span></label>
            <div class="phone-input-group">
          
              <input
                id="phone"
                v-model="form.phone"
                type="tel"
                placeholder="请输入手机号"
                required
                pattern="^1[3-9]\d{9}$"
              />
            </div>
          </div>

          <!-- 旅客类型 -->
          <div class="form-group">
            <label>旅客类型 <span class="required">*</span></label>
            <div class="radio-group">
              <label
                v-for="type in passengerTypes"
                :key="type.value"
                class="radio-label"
                :class="{ active: form.passengerType === type.value }"
              >
                <input
                  type="radio"
                  :value="type.value"
                  v-model="form.passengerType"
                  class="radio-input"
                />
                <span class="radio-icon" :class="{ checked: form.passengerType === type.value }"></span>
                <span class="radio-text">{{ type.label }}</span>
              </label>
            </div>
          </div>

          <!-- 出发机场 -->
          <div class="form-group">
            <label for="departureAirport">出发机场 <span class="required">*</span></label>
            <div class="select-wrapper">
              <button 
                type="button" 
                class="select-button"
                @click="toggleDepartureAirportSelector"
                :class="{ 'has-value': form.departureAirport }"
              >
                {{ form.departureAirport || '请选择出发机场' }}
                <span class="arrow">▼</span>
              </button>
              <div v-if="showDepartureAirportSelector" class="dropdown-menu airport-dropdown">
                <div 
                  v-for="airport in airports" 
                  :key="airport"
                  class="dropdown-item"
                  @click="selectDepartureAirport(airport)"
                >
                  {{ airport }}
                </div>
              </div>
            </div>
          </div>

          <!-- 进站服务需求 -->
          <div class="form-group">
            <label>进站服务需求</label>
            <div class="checkbox-group">
              <label class="checkbox-label">
                <input type="checkbox" v-model="form.entryServices.selfEquipment" />
                <span>自备器械</span>
              </label>
              <label class="checkbox-label">
                <input type="checkbox" v-model="form.entryServices.priorityEntry" />
                <span>优先进站</span>
              </label>
              <label class="checkbox-label">
                <input type="checkbox" v-model="form.entryServices.wheelchair" />
                <span>提供轮椅</span>
              </label>
              <label class="checkbox-label">
                <input type="checkbox" v-model="form.entryServices.stretcher" />
                <span>提供担架</span>
              </label>
            </div>
          </div>

          <!-- 到达机场 -->
          <div class="form-group">
            <label for="arrivalAirport">到达机场 <span class="required">*</span></label>
            <div class="select-wrapper">
              <button 
                type="button" 
                class="select-button"
                @click="toggleArrivalAirportSelector"
                :class="{ 'has-value': form.arrivalAirport }"
              >
                {{ form.arrivalAirport || '请选择到达机场' }}
                <span class="arrow">▼</span>
              </button>
              <div v-if="showArrivalAirportSelector" class="dropdown-menu airport-dropdown">
                <div 
                  v-for="airport in airports" 
                  :key="airport"
                  class="dropdown-item"
                  @click="selectArrivalAirport(airport)"
                >
                  {{ airport }}
                </div>
              </div>
            </div>
          </div>

          <!-- 出站服务需求 -->
          <div class="form-group">
            <label>出站服务需求</label>
            <div class="checkbox-group">
              <label class="checkbox-label">
                <input type="checkbox" v-model="form.exitServices.selfEquipment" />
                <span>自备器械</span>
              </label>
              <label class="checkbox-label">
                <input type="checkbox" v-model="form.exitServices.convenientExit" />
                <span>便利出站</span>
              </label>
              <label class="checkbox-label">
                <input type="checkbox" v-model="form.exitServices.wheelchair" />
                <span>提供轮椅</span>
              </label>
              <label class="checkbox-label">
                <input type="checkbox" v-model="form.exitServices.stretcher" />
                <span>提供担架</span>
              </label>
            </div>
          </div>

          <!-- 情况描述 -->
          <div class="form-group">
            <label for="description">情况描述（选填）</label>
            <textarea
              id="description"
              v-model="form.description"
              rows="4"
              placeholder="请详细描述您的特殊需求..."
            ></textarea>
          </div>

          <!-- 错误提示 -->
          <div v-if="formError" class="form-error">{{ formError }}</div>

          <!-- 提交按钮 -->
          <div class="form-actions">
            <button type="button" class="ghost-btn" @click="resetForm">重置</button>
            <button type="submit" class="primary-btn" :disabled="submitting">
              {{ submitting ? '提交中...' : '提交申请' }}
            </button>
          </div>
        </form>
      </section>

      <!-- 预约记录列表 -->
      <section class="glass-card records-section">
        <div class="section-header">
          <h2>我的预约记录</h2>
          <div class="filter-controls">
            <select v-model="filterStatus" @change="loadRequests" class="status-filter">
              <option value="">全部状态</option>
              <option value="pending">待处理</option>
              <option value="approved">已批准</option>
              <option value="processing">处理中</option>
              <option value="completed">已完成</option>
              <option value="rejected">已拒绝</option>
              <option value="cancelled">已取消</option>
            </select>
          </div>
        </div>

        <!-- 加载状态 -->
        <div v-if="loading" class="loading-state">
          <p>加载中...</p>
        </div>

        <!-- 空状态 -->
        <div v-else-if="requests.length === 0" class="empty-state">
          <p>暂无预约记录</p>
        </div>

        <!-- 记录列表 -->
        <div v-else class="requests-list">
          <div
            v-for="request in requests"
            :key="request.id"
            class="request-card"
            :class="`status-${request.status}`"
          >
            <div class="request-header">
              <div class="request-info">
                <h3>订单号: {{ request.orderNo || request.order_no || request.orderId }}</h3>
                <span class="status-badge" :class="`status-${request.status}`">
                  {{ getStatusText(request.status) }}
                </span>
              </div>
              <div class="request-meta">
                <span class="meta-item">
                  <span class="meta-label">申请时间:</span>
                  <span class="meta-value">{{ formatDateTime(request.createdAt || request.created_at) }}</span>
                </span>
              </div>
            </div>

            <div class="request-body">
              <div class="info-row">
                <span class="info-label">旅客类型:</span>
                <span class="info-value">{{ getPassengerTypeText(request.passengerType || request.passenger_type) }}</span>
              </div>
              <div class="info-row">
                <span class="info-label">联系电话:</span>
                <span class="info-value">{{ request.phone }}</span>
              </div>
              <div class="info-row">
                <span class="info-label">出发机场:</span>
                <span class="info-value">{{ request.departureAirport || request.departure_airport }}</span>
              </div>
              <div class="info-row">
                <span class="info-label">到达机场:</span>
                <span class="info-value">{{ request.arrivalAirport || request.arrival_airport }}</span>
              </div>
              
              <!-- 服务需求 -->
              <div v-if="request.entryServices || request.entry_services || request.exitServices || request.exit_services" class="services-info">
                <div v-if="request.entryServices || request.entry_services" class="service-group">
                  <span class="service-label">进站服务:</span>
                  <div class="service-tags">
                    <span v-if="(request.entryServices || request.entry_services)?.selfEquipment" class="service-tag">自备器械</span>
                    <span v-if="(request.entryServices || request.entry_services)?.priorityEntry" class="service-tag">优先进站</span>
                    <span v-if="(request.entryServices || request.entry_services)?.wheelchair" class="service-tag">提供轮椅</span>
                    <span v-if="(request.entryServices || request.entry_services)?.stretcher" class="service-tag">提供担架</span>
                  </div>
                </div>
                <div v-if="request.exitServices || request.exit_services" class="service-group">
                  <span class="service-label">出站服务:</span>
                  <div class="service-tags">
                    <span v-if="(request.exitServices || request.exit_services)?.selfEquipment" class="service-tag">自备器械</span>
                    <span v-if="(request.exitServices || request.exit_services)?.convenientExit" class="service-tag">便利出站</span>
                    <span v-if="(request.exitServices || request.exit_services)?.wheelchair" class="service-tag">提供轮椅</span>
                    <span v-if="(request.exitServices || request.exit_services)?.stretcher" class="service-tag">提供担架</span>
                  </div>
                </div>
              </div>

              <div v-if="request.description" class="description-info">
                <span class="description-label">情况描述:</span>
                <p class="description-text">{{ request.description }}</p>
              </div>
            </div>
          </div>
        </div>

        <!-- 分页 -->
        <div v-if="totalPages > 1" class="pagination">
          <button 
            class="pagination-btn"
            :disabled="currentPage === 0"
            @click="changePage(currentPage - 1)"
          >
            上一页
          </button>
          <span class="pagination-info">
            第 {{ currentPage + 1 }} / {{ totalPages }} 页 (共 {{ total }} 条)
          </span>
          <button 
            class="pagination-btn"
            :disabled="currentPage >= totalPages - 1"
            @click="changePage(currentPage + 1)"
          >
            下一页
          </button>
        </div>
      </section>
    </div>
  </PassengerLayout>
</template>

<script setup lang="ts">
import PassengerLayout from '../components/layout/PassengerLayout.vue'
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { specialServiceRequestApi, orderApi } from '../services/api'
import { formatDateTime } from '../utils/dateFormat'

const router = useRouter()
const route = useRoute()

// 返回上一页
const goBack = () => {
  router.push('/portal/passengers')
}

// 表单显示控制
const showCreateForm = ref(false)

// 表单数据
const form = reactive({
  orderId: null as number | null,
  countryCode: '+86',
  phone: '',
  passengerType: '',
  departureAirport: '',
  arrivalAirport: '',
  entryServices: {
    selfEquipment: false,
    priorityEntry: false,
    wheelchair: false,
    stretcher: false
  },
  exitServices: {
    selfEquipment: false,
    convenientExit: false,
    wheelchair: false,
    stretcher: false
  },
  description: ''
})

const formError = ref('')
const submitting = ref(false)

// 下拉选择器控制
const showOrderSelector = ref(false)
const showDepartureAirportSelector = ref(false)
const showArrivalAirportSelector = ref(false)

const selectedOrder = ref<any>(null)
const availableOrders = ref<any[]>([])

// 旅客类型选项
const passengerTypes = [
  { value: 'elderly', label: '无陪伴年长旅客' },
  { value: 'pregnant', label: '无陪伴孕妇旅客' },
  { value: 'visual', label: '视觉障碍旅客' },
  { value: 'hearing', label: '听觉障碍旅客' },
  { value: 'wheelchair', label: '轮椅行动障碍旅客' },
  { value: 'stretcher', label: '担架 (车) 行动障碍旅客' },
  { value: 'guide_dog', label: '携带导盲犬旅客' }
]

// 机场列表
const airports = [
  '北京首都国际机场', '上海浦东国际机场', '上海虹桥国际机场', 
  '广州白云国际机场', '深圳宝安国际机场', '成都双流国际机场',
  '西安咸阳国际机场', '杭州萧山国际机场', '南京禄口国际机场',
  '武汉天河国际机场', '长沙黄花国际机场', '郑州新郑国际机场'
]

// 解析路线
const parseRoute = (route: string) => {
  if (!route) return { origin: '', destination: '' }
  const parts = route.split('→').map(s => s.trim())
  return {
    origin: parts[0] || '',
    destination: parts[1] || ''
  }
}

// 订单选择相关
const toggleOrderSelector = () => {
  showOrderSelector.value = !showOrderSelector.value
  showDepartureAirportSelector.value = false
  showArrivalAirportSelector.value = false
}

const goToOrderList = () => {
  sessionStorage.setItem('selectedOrderForSpecialPassenger', JSON.stringify({}))
  sessionStorage.setItem('returnToSpecialPassenger', 'true')
  router.push({
    path: '/portal/orders',
    query: {
      from: 'special-passenger',
      selectMode: 'true'
    }
  })
}

const selectOrder = (order: any) => {
  selectedOrder.value = {
    ...order,
    orderNo: order.orderNo || order.order_no || String(order.id)
  }
  form.orderId = order.id
  showOrderSelector.value = false
  
  // 解析航线，自动填充出发机场和到达机场
  if (order.route) {
    const routeInfo = parseRoute(order.route)
    form.departureAirport = routeInfo.origin
    form.arrivalAirport = routeInfo.destination
  }
}

// 机场选择相关
const toggleDepartureAirportSelector = () => {
  showDepartureAirportSelector.value = !showDepartureAirportSelector.value
  showOrderSelector.value = false
  showArrivalAirportSelector.value = false
}

const selectDepartureAirport = (airport: string) => {
  form.departureAirport = airport
  showDepartureAirportSelector.value = false
}

const toggleArrivalAirportSelector = () => {
  showArrivalAirportSelector.value = !showArrivalAirportSelector.value
  showOrderSelector.value = false
  showDepartureAirportSelector.value = false
}

const selectArrivalAirport = (airport: string) => {
  form.arrivalAirport = airport
  showArrivalAirportSelector.value = false
}

// 重置表单
const resetForm = () => {
  form.orderId = null
  form.countryCode = '+86'
  form.phone = ''
  form.passengerType = ''
  form.departureAirport = ''
  form.arrivalAirport = ''
  form.entryServices = {
    selfEquipment: false,
    priorityEntry: false,
    wheelchair: false,
    stretcher: false
  }
  form.exitServices = {
    selfEquipment: false,
    convenientExit: false,
    wheelchair: false,
    stretcher: false
  }
  form.description = ''
  formError.value = ''
  selectedOrder.value = null
}

// 提交申请
const submitRequest = async () => {
  // 验证
  if (!form.orderId) {
    formError.value = '请选择订单'
    return
  }
  if (!form.phone || !/^1[3-9]\d{9}$/.test(form.phone)) {
    formError.value = '请输入正确的手机号'
    return
  }
  if (!form.passengerType) {
    formError.value = '请选择旅客类型'
    return
  }
  if (!form.departureAirport) {
    formError.value = '请填写出发机场'
    return
  }
  if (!form.arrivalAirport) {
    formError.value = '请填写到达机场'
    return
  }

  formError.value = ''
  submitting.value = true

  try {
    // 获取订单号
    let orderNo = ''
    if (selectedOrder.value && selectedOrder.value.orderNo) {
      orderNo = selectedOrder.value.orderNo
    } else if (form.orderId) {
      orderNo = String(form.orderId)
    } else {
      throw new Error('无法获取订单号')
    }
    
    // 调用API
    await specialServiceRequestApi.createRequest({
      orderNo: orderNo,
      phone: form.phone,
      passengerType: form.passengerType,
      departureAirport: form.departureAirport,
      arrivalAirport: form.arrivalAirport,
      entryServices: form.entryServices,
      exitServices: form.exitServices,
      description: form.description || undefined
    })
    
    // 成功提示
    alert('您的重点旅客预约申请已提交，我们将在24小时内与您联系确认服务安排。')
    
    // 重置表单并刷新列表
    resetForm()
    showCreateForm.value = false
    await loadRequests()
  } catch (error: any) {
    formError.value = error.message || '提交申请失败，请重试'
    console.error('提交申请失败:', error)
  } finally {
    submitting.value = false
  }
}

// 预约记录列表
const requests = ref<any[]>([])
const loading = ref(false)
const currentPage = ref(0)
const totalPages = ref(0)
const total = ref(0)
const filterStatus = ref('')

// 加载预约记录
const loadRequests = async () => {
  try {
    loading.value = true
    const result = await specialServiceRequestApi.getRequests({
      page: currentPage.value,
      size: 10,
      status: filterStatus.value || undefined
    })
    
    if (result) {
      requests.value = result.list || []
      totalPages.value = result.totalPages || 0
      total.value = result.total || 0
    }
  } catch (error) {
    console.error('加载预约记录失败:', error)
    requests.value = []
  } finally {
    loading.value = false
  }
}

// 切换页码
const changePage = (page: number) => {
  currentPage.value = page
  loadRequests()
}

// 获取状态文本
const getStatusText = (status: string) => {
  const statusMap: Record<string, string> = {
    pending: '待处理',
    approved: '已批准',
    processing: '处理中',
    completed: '已完成',
    rejected: '已拒绝',
    cancelled: '已取消'
  }
  return statusMap[status] || status
}

// 获取旅客类型文本
const getPassengerTypeText = (type: string) => {
  const typeObj = passengerTypes.find(t => t.value === type)
  return typeObj ? typeObj.label : type
}

// 判断订单是否为"待出行"状态
const isTicketedStatus = (order: any) => {
  const status = String(order.status || order.statusText || '').toLowerCase()
  const statusText = String(order.statusText || order.status || '').toLowerCase()
  return (
    status === 'ticketed' ||
    statusText === '待出行' ||
    statusText === '已出票' ||
    order.status === '待出行' ||
    order.status === '已出票'
  )
}

// 加载订单列表（只加载"待出行"状态的订单，并排除已用于预约的订单号）
const loadOrders = async () => {
  try {
    // 先获取第一页，检查总数并根据需要分页拉取全部订单
    const firstPage = await orderApi.getOrders({ page: 0, size: 100 })
    if (!firstPage || !firstPage.orders) {
      availableOrders.value = []
      return
    }

    let allOrders: any[] = [...firstPage.orders]
    const total = firstPage.total || 0
    const pageSize = firstPage.size || 100

    if (total > pageSize) {
      const totalPages = Math.ceil(total / pageSize)
      for (let page = 1; page < totalPages; page++) {
        try {
          const pageResult = await orderApi.getOrders({ page, size: pageSize })
          if (pageResult && pageResult.orders && pageResult.orders.length > 0) {
            allOrders = [...allOrders, ...pageResult.orders]
          }
        } catch (e) {
          console.warn(`获取第${page + 1}页订单失败:`, e)
        }
      }
    }

    // 只保留"待出行"状态的订单
    const ticketedOrders = allOrders.filter((order: any) => isTicketedStatus(order))

    // 尝试获取当前用户已提交的重点旅客预约，收集已经使用过的订单号并从下拉中排除
    const usedOrderNos = new Set<string>()
    try {
      const reqResult: any = await specialServiceRequestApi.getRequests({ page: 0, size: 1000 })
      const list = reqResult?.list || []
      list.forEach((r: any) => {
        const no = r.orderNo || r.order_no || r.orderId || r.order_id
        if (no) usedOrderNos.add(String(no))
      })
    } catch (err) {
      // 如果获取预约列表失败，不阻塞订单加载，记录错误并继续（保持原有逻辑）
      console.warn('获取已提交的重点旅客预约失败，暂不排除已用订单:', err)
    }

    // 过滤出未被使用的订单
    const filteredOrders = ticketedOrders.filter((order: any) => {
      const orderNo = order.orderNo || order.order_no || String(order.id)
      return !usedOrderNos.has(String(orderNo))
    })

    availableOrders.value = filteredOrders.map((order: any) => ({
      id: order.id,
      orderNo: order.orderNo || order.order_no || String(order.id),
      route: `${order.origin || order.route || ''} → ${order.destination || ''}`,
      time: order.departureTime || order.time || '',
      amount: order.amount || order.price || 0
    }))
  } catch (error) {
    console.error('加载订单列表失败:', error)
    availableOrders.value = []
  }
}

// 点击外部关闭下拉
const handleClickOutside = (event: MouseEvent) => {
  const target = event.target as HTMLElement
  if (!target.closest('.select-wrapper') && 
      !target.closest('.dropdown-menu')) {
    showOrderSelector.value = false
    showDepartureAirportSelector.value = false
    showArrivalAirportSelector.value = false
  }
}

// 初始化
onMounted(async () => {
  // 检查是否有从订单列表返回的订单选择
  const selectedOrderData = sessionStorage.getItem('selectedOrderForSpecialPassenger')
  const returnToSpecialPassenger = sessionStorage.getItem('returnToSpecialPassenger')
  
  if (returnToSpecialPassenger === 'true') {
    sessionStorage.removeItem('returnToSpecialPassenger')
    showCreateForm.value = true
  }
  
  if (selectedOrderData) {
    try {
      const orderData = JSON.parse(selectedOrderData)
      if (orderData && orderData.id) {
        // 填充订单信息
        selectedOrder.value = {
          id: orderData.id || orderData.orderId,
          orderNo: orderData.orderNo || orderData.order_no || String(orderData.id || orderData.orderId),
          route: orderData.route || '',
          time: orderData.time || '',
          amount: orderData.amount || 0
        }
        form.orderId = orderData.id || orderData.orderId
        
        // 解析航线，填充出发机场和到达机场
        if (orderData.route) {
          const routeInfo = parseRoute(orderData.route)
          form.departureAirport = routeInfo.origin
          form.arrivalAirport = routeInfo.destination
        }
        
        showCreateForm.value = true
      }
      sessionStorage.removeItem('selectedOrderForSpecialPassenger')
    } catch (e) {
      console.warn('解析选中的订单数据失败:', e)
    }
  }
  
  // 加载订单列表和预约记录
  await Promise.all([loadOrders(), loadRequests()])
  
  // 添加点击外部关闭下拉的事件监听
  document.addEventListener('click', handleClickOutside)
})
</script>

<style scoped>
.special-passenger-page {
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

.breadcrumb {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.7);
  margin-bottom: 20px;
}

.breadcrumb-link {
  cursor: pointer;
  transition: color 0.3s;
}

.breadcrumb-link:hover {
  color: rgba(255, 255, 255, 0.9);
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

.primary-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 15px 30px rgba(99, 102, 241, 0.45);
}

.primary-btn:disabled {
  opacity: 0.6;
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

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
}

.section-header h2 {
  margin: 0;
  color: #fff;
  font-size: 1.5rem;
}

.filter-controls {
  display: flex;
  gap: 1rem;
}

.status-filter {
  padding: 0.5rem 1rem;
  border-radius: 12px;
  background: rgba(8, 14, 35, 0.85);
  border: 1px solid rgba(255, 255, 255, 0.12);
  color: #f8fafc;
  font-size: 0.9rem;
  cursor: pointer;
  outline: none;
  transition: all 0.25s;
}

.status-filter:hover {
  border-color: rgba(96, 165, 250, 0.9);
}

.status-filter:focus {
  border-color: rgba(96, 165, 250, 0.9);
  box-shadow: 0 0 0 3px rgba(96, 165, 250, 0.1);
}

/* 表单样式 */
.request-form {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.form-group label {
  font-size: 0.9rem;
  color: rgba(248, 250, 252, 0.8);
  font-weight: 500;
}

.required {
  color: #f87171;
}

.select-wrapper {
  position: relative;
}

.select-button {
  width: 100%;
  padding: 0.85rem 1.1rem;
  border-radius: 18px;
  background: rgba(228, 231, 243, 0.85);
  border: 1px solid rgba(211, 217, 228, 0.12);
  color: #f8fafc;
  font-size: 0.95rem;
  cursor: pointer;
  display: flex;
  justify-content: space-between;
  align-items: center;
  transition: all 0.25s;
}

.select-button:hover {
  border-color: rgba(96, 165, 250, 0.9);
}

.select-button.has-value {
  color: #fff;
}

.arrow {
  font-size: 0.8rem;
  transition: transform 0.3s;
}

.dropdown-menu {
  position: absolute;
  top: calc(100% + 0.5rem);
  left: 0;
  right: 0;
  background: rgba(219, 221, 227, 0.95);
  border: 1px solid rgba(255, 255, 255, 0.12);
  border-radius: 12px;
  padding: 0.5rem;
  z-index: 100;
  max-height: 300px;
  overflow-y: auto;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.5);
}

.dropdown-item {
  padding: 0.75rem 1rem;
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.2s;
  color: #f8fafc;
}

.dropdown-item:hover {
  background: rgba(59, 130, 246, 0.2);
}

.order-item {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.order-no {
  font-weight: 600;
  color: #fff;
}

.order-route {
  font-size: 0.85rem;
  color: rgba(248, 250, 252, 0.7);
}

.phone-input-group {
  display: flex;
  gap: 0.5rem;
}

.country-code-select {
  flex-shrink: 0;
  width: 80px;
  padding: 0.85rem 1rem;
  border-radius: 18px;
  background: rgba(8, 14, 35, 0.85);
  border: 1px solid rgba(255, 255, 255, 0.12);
  color: #f8fafc;
  font-size: 0.95rem;
  cursor: pointer;
  outline: none;
}

.phone-input-group input {
  flex: 1;
  padding: 0.85rem 1.1rem;
  border-radius: 18px;
  background: rgba(8, 14, 35, 0.85);
  border: 1px solid rgba(255, 255, 255, 0.12);
  color: #f8fafc;
  font-size: 0.95rem;
  outline: none;
  transition: all 0.25s;
}

.phone-input-group input:focus {
  border-color: rgba(96, 165, 250, 0.9);
  box-shadow: 0 0 0 3px rgba(96, 165, 250, 0.1);
}

.radio-group {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 0.75rem;
}

.radio-label {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem 1rem;
  border-radius: 12px;
  background: rgba(8, 14, 35, 0.5);
  border: 1px solid rgba(255, 255, 255, 0.1);
  cursor: pointer;
  transition: all 0.25s;
}

.radio-label:hover {
  border-color: rgba(59, 130, 246, 0.5);
  background: rgba(59, 130, 246, 0.1);
}

.radio-label.active {
  border-color: rgba(59, 130, 246, 0.8);
  background: rgba(59, 130, 246, 0.2);
}

.radio-input {
  display: none;
}

.radio-icon {
  width: 18px;
  height: 18px;
  border-radius: 50%;
  border: 2px solid rgba(255, 255, 255, 0.3);
  position: relative;
  transition: all 0.25s;
}

.radio-icon.checked {
  border-color: #3b82f6;
  background: #3b82f6;
}

.radio-icon.checked::after {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #fff;
}

.radio-text {
  flex: 1;
  color: #f8fafc;
  font-size: 0.9rem;
}

.checkbox-group {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 0.75rem;
}

.checkbox-label {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  cursor: pointer;
  color: #f8fafc;
  font-size: 0.9rem;
}

.checkbox-label input[type="checkbox"] {
  width: 18px;
  height: 18px;
  cursor: pointer;
}

textarea {
  width: 100%;
  padding: 0.85rem 1.1rem;
  border-radius: 18px;
  background: rgba(8, 14, 35, 0.85);
  border: 1px solid rgba(255, 255, 255, 0.12);
  color: #f8fafc;
  font-size: 0.95rem;
  font-family: inherit;
  outline: none;
  resize: vertical;
  transition: all 0.25s;
}

textarea:focus {
  border-color: rgba(96, 165, 250, 0.9);
  box-shadow: 0 0 0 3px rgba(96, 165, 250, 0.1);
}

.form-error {
  padding: 0.75rem 1rem;
  border-radius: 12px;
  background: rgba(248, 113, 113, 0.15);
  border: 1px solid rgba(248, 113, 113, 0.3);
  color: #f87171;
  font-size: 0.9rem;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 1rem;
  margin-top: 1rem;
}

/* 记录列表样式 */
.loading-state,
.empty-state {
  padding: 3rem;
  text-align: center;
  color: rgba(255, 255, 255, 0.6);
}
  
.requests-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.request-card {
  padding: 1.5rem;
  border-radius: 16px;
  background: rgba(234, 235, 238, 0.5);
  border: 1px solid rgba(255, 255, 255, 0.1);
  transition: all 0.3s;
}

.request-card:hover {
  border-color: rgba(59, 130, 246, 0.4);
  transform: translateY(-2px);
}

.request-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 1rem;
  flex-wrap: wrap;
  gap: 1rem;
}

.request-info {
  display: flex;
  align-items: center;
  gap: 1rem;
  flex-wrap: wrap;
}

.request-info h3 {
  margin: 0;
  color: #fff;
  font-size: 1.1rem;
}

.status-badge {
  padding: 0.35rem 0.85rem;
  border-radius: 999px;
  font-size: 0.85rem;
  font-weight: 500;
}

.status-badge.status-pending {
  background: rgba(251, 191, 36, 0.15);
  color: #fbbf24;
  border: 1px solid rgba(251, 191, 36, 0.3);
}

.status-badge.status-approved {
  background: rgba(34, 197, 94, 0.15);
  color: #22c55e;
  border: 1px solid rgba(34, 197, 94, 0.3);
}

.status-badge.status-processing {
  background: rgba(59, 130, 246, 0.15);
  color: #3b82f6;
  border: 1px solid rgba(59, 130, 246, 0.3);
}

.status-badge.status-completed {
  background: rgba(34, 197, 94, 0.15);
  color: #22c55e;
  border: 1px solid rgba(34, 197, 94, 0.3);
}

.status-badge.status-rejected {
  background: rgba(248, 113, 113, 0.15);
  color: #f87171;
  border: 1px solid rgba(248, 113, 113, 0.3);
}

.status-badge.status-cancelled {
  background: rgba(107, 114, 128, 0.15);
  color: #9ca3af;
  border: 1px solid rgba(107, 114, 128, 0.3);
}

.request-meta {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  align-items: flex-end;
}

.meta-item {
  display: flex;
  gap: 0.5rem;
  font-size: 0.85rem;
}

.meta-label {
  color: rgba(255, 255, 255, 0.6);
}

.meta-value {
  color: rgba(255, 255, 255, 0.9);
}

.request-body {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.info-row {
  display: flex;
  gap: 1rem;
  font-size: 0.9rem;
}

.info-label {
  color: rgba(255, 255, 255, 0.6);
  min-width: 100px;
  flex-shrink: 0;
}

.info-value {
  color: rgba(255, 255, 255, 0.9);
  flex: 1;
}

.services-info {
  margin-top: 0.5rem;
  padding-top: 1rem;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

.service-group {
  display: flex;
  gap: 1rem;
  margin-bottom: 0.75rem;
  align-items: flex-start;
}

.service-label {
  color: rgba(255, 255, 255, 0.6);
  font-size: 0.9rem;
  min-width: 100px;
  flex-shrink: 0;
}

.service-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
  flex: 1;
}

.service-tag {
  padding: 0.35rem 0.75rem;
  border-radius: 8px;
  background: rgba(59, 130, 246, 0.15);
  border: 1px solid rgba(59, 130, 246, 0.3);
  color: #60a5fa;
  font-size: 0.85rem;
}

.description-info {
  margin-top: 0.5rem;
  padding-top: 1rem;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

.description-label {
  color: rgba(255, 255, 255, 0.6);
  font-size: 0.9rem;
  display: block;
  margin-bottom: 0.5rem;
}

.description-text {
  color: rgba(255, 255, 255, 0.8);
  font-size: 0.9rem;
  line-height: 1.6;
  margin: 0;
  white-space: pre-wrap;
}

/* 分页样式 */
.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 1rem;
  margin-top: 2rem;
  padding-top: 1.5rem;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

.pagination-btn {
  padding: 0.5rem 1rem;
  border-radius: 12px;
  background: rgba(8, 14, 35, 0.85);
  border: 1px solid rgba(255, 255, 255, 0.12);
  color: #f8fafc;
  font-size: 0.9rem;
  cursor: pointer;
  transition: all 0.25s;
}

.pagination-btn:hover:not(:disabled) {
  border-color: rgba(96, 165, 250, 0.9);
  background: rgba(59, 130, 246, 0.2);
}

.pagination-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.pagination-info {
  color: rgba(255, 255, 255, 0.7);
  font-size: 0.9rem;
}

/* 响应式 */
@media (max-width: 768px) {
  .special-passenger-page {
    padding: 1.5rem 1rem;
  }

  .page-header {
    flex-direction: column;
  }

  .radio-group {
    grid-template-columns: 1fr;
  }

  .checkbox-group {
    grid-template-columns: 1fr;
  }

  .request-header {
    flex-direction: column;
  }

  .request-meta {
    align-items: flex-start;
  }
}
</style>


<template>
  <PassengerLayout>
    <!-- 面包屑导航 -->
      <div class="breadcrumb">
        <span>首页</span>
        <span class="breadcrumb-separator">/</span>
        <span>个人中心</span>
      </div>

    <section class="grid-two">
      <article class="glass-card">
        <h2>账号概要</h2>
        <ul class="info-list">
          <li>
            <span>用户名</span>
            <strong>{{ user.username }}</strong>
          </li>
          <li>
            <span>真实姓名</span>
            <strong>{{ user.realName }}</strong>
          </li>
          <li>
            <span>绑定手机</span>
            <strong>{{ user.phone || '未绑定' }}</strong>
          </li>
          <li>
            <span>注册时间</span>
            <strong>{{ formattedRegisteredAt }}</strong>
          </li>
        </ul>
      </article>
    </section>

    <section class="glass-card">
      <header class="section-head">
        <div>
          <p class="section-label">个性化设置</p>
          <h2>主题风格</h2>
        </div>
      </header>
      <ThemeSettings />
    </section>

    <!-- 成功提示窗 -->
    <ModalPrompt
      v-model="successDialog.visible"
      :title="successDialog.title"
      :message="successDialog.message"
      type="success"
      confirm-text="好的"
      @confirm="handleSuccessConfirm"
    />

    <!-- 重点旅客预约弹窗 -->
    <Transition name="modal-fade">
      <div v-if="specialPassengerDialog.visible" class="special-passenger-overlay" @click.self="closeSpecialPassengerDialog">
        <div class="special-passenger-modal">
        <div class="modal-header">
          <div>
            <p class="modal-label">重点旅客预约</p>
            <h3>申请特殊服务协助</h3>
          </div>
          <button class="modal-close" @click="closeSpecialPassengerDialog">×</button>
        </div>

        <div class="special-passenger-form">
          <!-- 订单选择 -->
          <div class="form-group">
            <label class="form-label">订单选择 <span class="required">*</span></label>
            <div class="select-wrapper" @click="toggleOrderSelector">
              <input 
                type="text" 
                readonly
                :value="selectedOrder ? (selectedOrder.orderNo || selectedOrder.id) : '请选择乘车订单'"
                class="form-input select-input"
                placeholder="请选择乘车订单"
              />
              <span class="select-arrow">></span>
            </div>
            <div v-if="showOrderSelector" class="order-selector-dropdown">
              <div 
                v-for="order in availableOrders" 
                :key="order.id"
                class="order-option"
                @click="selectOrder(order)"
              >
                <div class="order-option-main">
                  <strong>订单 #{{ order.id }}</strong>
                  <span>{{ order.route }}</span>
                </div>
                <div class="order-option-meta">
                  <span>{{ order.time }}</span>
                  <span>¥{{ order.amount || 0 }}</span>
                </div>
              </div>
              <div v-if="availableOrders.length === 0" class="order-option empty">
                暂无可用订单
              </div>
            </div>
          </div>

          <!-- 联系电话 -->
          <div class="form-group">
            <label class="form-label">联系电话 <span class="required">*</span></label>
            <div class="phone-input-wrapper">
              <select v-model="specialPassengerForm.countryCode" class="country-code-select">
                <option value="+86">+86</option>
                <option value="+1">+1</option>
                <option value="+852">+852</option>
                <option value="+853">+853</option>
                <option value="+886">+886</option>
              </select>
              <input 
                type="tel" 
                v-model="specialPassengerForm.phone"
                class="form-input phone-input"
                placeholder="请填写手机号"
                maxlength="11"
              />
            </div>
          </div>

          <!-- 旅客类型 -->
          <div class="form-group" style="position: relative;">
            <label class="form-label">旅客类型 <span class="required">*</span></label>
            <div class="select-wrapper" @click.stop="togglePassengerTypeSelector">
              <input 
                type="text" 
                readonly
                :value="selectedPassengerType || '请选择旅客类型'"
                class="form-input select-input"
                placeholder="请选择旅客类型"
              />
              <span class="select-arrow">></span>
              <!-- 旅客类型选择下拉 -->
              <div v-if="showPassengerTypeSelector" class="passenger-type-dropdown" @click.stop>
                <div 
                  v-for="type in passengerTypes" 
                  :key="type.value"
                  class="passenger-type-option"
                  :class="{ active: specialPassengerForm.passengerType === type.value }"
                  @click.stop="selectPassengerType(type.value)"
                >
                  <span class="radio-icon" :class="{ checked: specialPassengerForm.passengerType === type.value }"></span>
                  <span>{{ type.label }}</span>
                </div>
              </div>
            </div>
          </div>

          <!-- 提示信息 -->
          <div class="info-banner">
            如发车站或到站与实际不符,可进行修改。
          </div>

          <!-- 出发机场 -->
          <div class="form-group">
            <label class="form-label">出发机场 <span class="required">*</span></label>
            <input 
              type="text" 
              v-model="specialPassengerForm.departureAirport"
              class="form-input"
              placeholder="请填写出发机场"
            />
          </div>

          <!-- 进站服务需求 -->
          <div class="form-group">
            <label class="form-label">进站服务需求</label>
            <div class="checkbox-group">
              <label class="checkbox-item">
                <input 
                  type="checkbox" 
                  v-model="specialPassengerForm.entryServices.selfEquipment"
                  class="checkbox-input"
                />
                <span class="checkbox-label">自备器械</span>
              </label>
              <label class="checkbox-item">
                <input 
                  type="checkbox" 
                  v-model="specialPassengerForm.entryServices.priorityEntry"
                  class="checkbox-input"
                />
                <span class="checkbox-label">优先进站</span>
              </label>
              <label class="checkbox-item">
                <input 
                  type="checkbox" 
                  v-model="specialPassengerForm.entryServices.wheelchair"
                  class="checkbox-input"
                />
                <span class="checkbox-label">提供轮椅</span>
              </label>
              <label class="checkbox-item">
                <input 
                  type="checkbox" 
                  v-model="specialPassengerForm.entryServices.stretcher"
                  class="checkbox-input"
                />
                <span class="checkbox-label">提供担架</span>
              </label>
            </div>
          </div>

          <!-- 到达机场 -->
          <div class="form-group">
            <label class="form-label">到达机场 <span class="required">*</span></label>
            <input 
              type="text" 
              v-model="specialPassengerForm.arrivalAirport"
              class="form-input"
              placeholder="请填写到达机场"
            />
          </div>

          <!-- 出站服务需求 -->
          <div class="form-group">
            <label class="form-label">出站服务需求</label>
            <div class="checkbox-group">
              <label class="checkbox-item">
                <input 
                  type="checkbox" 
                  v-model="specialPassengerForm.exitServices.selfEquipment"
                  class="checkbox-input"
                />
                <span class="checkbox-label">自备器械</span>
              </label>
              <label class="checkbox-item">
                <input 
                  type="checkbox" 
                  v-model="specialPassengerForm.exitServices.convenientExit"
                  class="checkbox-input"
                />
                <span class="checkbox-label">便利出站</span>
              </label>
              <label class="checkbox-item">
                <input 
                  type="checkbox" 
                  v-model="specialPassengerForm.exitServices.wheelchair"
                  class="checkbox-input"
                />
                <span class="checkbox-label">提供轮椅</span>
              </label>
              <label class="checkbox-item">
                <input 
                  type="checkbox" 
                  v-model="specialPassengerForm.exitServices.stretcher"
                  class="checkbox-input"
                />
                <span class="checkbox-label">提供担架</span>
              </label>
            </div>
          </div>

          <!-- 情况描述 -->
          <div class="form-group">
            <label class="form-label">情况描述 <span class="optional">(非必填)</span></label>
            <textarea
              v-model="specialPassengerForm.description"
              class="form-textarea"
              placeholder="请简述您需要的服务内容"
              rows="4"
            ></textarea>
          </div>

          <p v-if="specialPassengerDialog.error" class="form-error">{{ specialPassengerDialog.error }}</p>

          <div class="modal-actions">
            <button class="modal-btn ghost" @click="closeSpecialPassengerDialog">取消</button>
            <button
              class="modal-btn primary"
              :disabled="specialPassengerDialog.submitting"
              @click="submitSpecialPassengerRequest"
            >
              <span v-if="specialPassengerDialog.submitting">提交中...</span>
              <span v-else>提交申请</span>
            </button>
          </div>
          </div>
        </div>
      </div>
    </Transition>
  </PassengerLayout>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, reactive } from 'vue'
import { useRouter } from 'vue-router'
import PassengerLayout from '../components/layout/PassengerLayout.vue'
import store from '../services/store'
import ThemeSettings from '../components/ThemeSettings.vue'
import { passengerApi, orderApi, specialServiceRequestApi } from '../services/api'
import ModalPrompt from '../components/ModalPrompt.vue'

const router = useRouter()

// 当前登录用户（来自全局 store，带默认值，避免空白闪烁）
const user = computed(() => {
  const info = store.userState.userInfo || {}
  return {
    username: info.username || '',
    realName: info.realName || '',
    email: info.email || '',
    phone: info.phone || '',
    registeredAt: info.registeredAt || info.createdAt || info.register_time || ''
  }
})

const formattedRegisteredAt = computed(() => {
  const v = user.value.registeredAt
  if (!v) return ''
  // 兼容时间戳/ISO/日期字符串
  try {
    if (/^\d{10,13}$/.test(String(v))) {
      const ts = String(v).length === 13 ? Number(v) : Number(v) * 1000
      return new Date(ts).toISOString().slice(0, 10)
    }
    const d = new Date(v)
    if (!isNaN(d.getTime())) return d.toISOString().slice(0, 10)
  } catch {}
  return String(v).slice(0, 10)
})

// 账号统计
const accountStats = ref({
  totalOrders: 12,
  totalSpent: '¥28,560',
  upcomingFlights: 2
})

// 主题相关
// 主题设置已移至ThemeSettings组件

onMounted(async () => {
  // 确保主题已初始化
  if (!store.themeState.theme) {
    store.initializeThemeState()
  }

  // 检查是否有从订单列表返回的订单选择
  const selectedOrderData = sessionStorage.getItem('selectedOrderForSpecialPassenger')
  if (selectedOrderData) {
    try {
      const orderData = JSON.parse(selectedOrderData)
      // 填充订单信息
      selectedOrder.value = {
        id: orderData.orderNo || orderData.id,
        route: orderData.route || '',
        time: orderData.time || '',
        amount: orderData.amount || 0
      }
      specialPassengerForm.orderId = orderData.orderId || orderData.id
      
      // 解析航线，填充出发机场和到达机场
      if (orderData.route) {
        const routeInfo = parseRoute(orderData.route)
        specialPassengerForm.departureAirport = routeInfo.origin
        specialPassengerForm.arrivalAirport = routeInfo.destination
      }
      
      // 清除sessionStorage中的选择数据
      sessionStorage.removeItem('selectedOrderForSpecialPassenger')
    } catch (e) {
      console.warn('解析选中的订单数据失败:', e)
    }
  }

  // 同步用户资料：如本地缺字段则从 /passenger/profile 兜底补齐
  try {
    const needProfile = !user.value.username || !user.value.realName || !user.value.email || !user.value.registeredAt
    if (needProfile) {
      const resp = await passengerApi.getProfile()
      const profile = (resp && (resp.data || resp)) || {}
      if (profile && Object.keys(profile).length) {
        store.setUserState({ ...(store.userState.userInfo || {}), ...profile })
      }
    }
  } catch (e) {
    console.warn('加载个人资料失败:', e)
  }
  
  // 从store获取统计数据（如果有）
  if (store.orderState.orders.length > 0) {
    accountStats.value.totalOrders = store.orderState.orders.length
  }

  // 加载航班实时查询
  try {
    await loadFlightQueryFlights()
  } catch (error) {
    console.error('loadFlightQueryFlights() 调用失败:', error)
  }
  
  // 设置定时刷新（每1秒刷新一次）
  flightQueryRefreshTimer.value = setInterval(() => {
    try {
      loadFlightQueryFlights()
    } catch (error) {
      console.error('定时刷新航班数据失败:', error)
    }
  }, 1000)
})

// 导航函数
const navigateTo = (path: string) => {
  router.push(path)
}

const viewOrders = () => {
  router.push('/portal/orders')
}

const viewFlights = () => {
  router.push('/portal/passengers/view')
}

// 重点旅客预约相关
const specialPassengerDialog = reactive({
  visible: false,
  error: '',
  submitting: false
})

const specialPassengerForm = reactive({
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

const showOrderSelector = ref(false)
const showPassengerTypeSelector = ref(false)
const showDepartureAirportSelector = ref(false)
const showArrivalAirportSelector = ref(false)

const selectedOrder = ref<any>(null)
const availableOrders = ref<any[]>([])

// 成功提示窗
const successDialog = reactive({
  visible: false,
  title: '',
  message: ''
})

// 航班实时查询相关
const flightQueryFlights = ref<any[]>([])
const flightQueryRefreshTimer = ref<number | null>(null)

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
    console.log('========== 开始加载航班实时查询数据 ==========')
    
    // 获取所有订单，使用大的size值确保获取所有数据
    // 先获取第一页看总数
    console.log('调用 orderApi.getOrders({ page: 0, size: 100 })')
    const firstPageResult = await orderApi.getOrders({ page: 0, size: 100 })
    
    console.log('第一页查询结果:', JSON.stringify(firstPageResult, null, 2))
    
    if (!firstPageResult) {
      console.warn('未获取到订单数据，firstPageResult为空')
      flightQueryFlights.value = []
      return
    }
    
    // 检查orders数组是否存在且不为空
    if (!firstPageResult.orders) {
      console.warn('订单数组不存在，firstPageResult:', firstPageResult)
      flightQueryFlights.value = []
      return
    }
    
    if (firstPageResult.orders.length === 0 && firstPageResult.total > 0) {
      console.warn('订单数组为空但total>0，可能是分页问题，尝试获取所有页')
      // 如果第一页为空但总数大于0，尝试获取所有页
      const total = firstPageResult.total || 0
      const pageSize = 10
      const totalPages = Math.ceil(total / pageSize)
      let allOrders: any[] = []
      
      for (let page = 0; page < totalPages; page++) {
        try {
          console.log(`尝试获取第${page + 1}页，页码: ${page}`)
          const pageResult = await orderApi.getOrders({ page, size: pageSize })
          if (pageResult && pageResult.orders && pageResult.orders.length > 0) {
            allOrders = [...allOrders, ...pageResult.orders]
            console.log(`第${page + 1}页获取到${pageResult.orders.length}条订单`)
          }
        } catch (e) {
          console.warn(`获取第${page + 1}页订单失败:`, e)
        }
      }
      
      if (allOrders.length === 0) {
        console.warn('所有页都未获取到订单数据')
        flightQueryFlights.value = []
        return
      }
      
      console.log('获取到的所有订单数量:', allOrders.length)
      console.log('所有订单数据:', allOrders)
      
      // 继续处理筛选逻辑
      const filtered = allOrders
        .filter((order: any) => {
          // 筛选条件：状态为ticketed（不区分大小写）且起飞时间在24小时内
          const status = String(order.status || '').toLowerCase()
          const isTicketed = status === 'ticketed'
          const within24Hours = isWithin24Hours(order.departureTime)
          
          // 调试日志
          if (isTicketed) {
            console.log('找到ticketed订单 - 原始数据:', {
              id: order.id,
              orderNo: order.orderNo,
              passengerName: order.passengerName,
              route: order.route,
              departureTime原始值: order.departureTime,
              departureTime类型: typeof order.departureTime,
              departureTime格式化后: formatDepartureTime(order.departureTime),
              status: order.status,
              within24Hours: within24Hours
            })
          }
          
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
      console.log('符合条件的航班数量:', filtered.length)
      console.log('符合条件的航班列表:', filtered)
      return
    }
    
    let allOrders = [...firstPageResult.orders]
    const total = firstPageResult.total || 0
    const pageSize = firstPageResult.size || 100
    
    console.log('第一页订单数量:', allOrders.length, '总数:', total)
    
    // 如果数据超过一页，获取剩余页的数据
    if (total > pageSize) {
      const totalPages = Math.ceil(total / pageSize)
      console.log(`需要获取${totalPages}页数据`)
      for (let page = 1; page < totalPages; page++) {
        try {
          const pageResult = await orderApi.getOrders({ page, size: pageSize })
          if (pageResult && pageResult.orders) {
            allOrders = [...allOrders, ...pageResult.orders]
            console.log(`第${page + 1}页获取到${pageResult.orders.length}条订单`)
          }
        } catch (e) {
          console.warn(`获取第${page + 1}页订单失败:`, e)
        }
      }
    }
    
    console.log('获取到的所有订单数量:', allOrders.length)
    console.log('所有订单数据:', allOrders)
    
    const filtered = allOrders
      .filter((order: any) => {
        // 筛选条件：状态为ticketed（不区分大小写）且起飞时间在24小时内
        const status = String(order.status || '').toLowerCase()
        const isTicketed = status === 'ticketed'
        const within24Hours = isWithin24Hours(order.departureTime)
        
        // 调试日志
        if (isTicketed) {
          console.log('找到ticketed订单 - 原始数据:', {
            id: order.id,
            orderNo: order.orderNo,
            passengerName: order.passengerName,
            route: order.route,
            departureTime原始值: order.departureTime,
            departureTime类型: typeof order.departureTime,
            departureTime格式化后: formatDepartureTime(order.departureTime),
            status: order.status,
            within24Hours: within24Hours
          })
        }
        
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
    console.log('符合条件的航班数量:', filtered.length)
    console.log('符合条件的航班列表:', filtered)
  } catch (error) {
    console.error('加载航班实时查询失败:', error)
    flightQueryFlights.value = []
  }
}

const passengerTypes = [
  { value: 'elderly', label: '无陪伴年长旅客' },
  { value: 'pregnant', label: '无陪伴孕妇旅客' },
  { value: 'visual', label: '视觉障碍旅客' },
  { value: 'hearing', label: '听觉障碍旅客' },
  { value: 'wheelchair', label: '轮椅行动障碍旅客' },
  { value: 'stretcher', label: '担架 (车) 行动障碍旅客' },
  { value: 'guide_dog', label: '携带导盲犬旅客' }
]

const airports = [
  '北京首都国际机场', '上海浦东国际机场', '上海虹桥国际机场', 
  '广州白云国际机场', '深圳宝安国际机场', '成都双流国际机场',
  '西安咸阳国际机场', '杭州萧山国际机场', '南京禄口国际机场',
  '武汉天河国际机场', '长沙黄花国际机场', '郑州新郑国际机场'
]

const selectedPassengerType = computed(() => {
  const type = passengerTypes.find(t => t.value === specialPassengerForm.passengerType)
  return type ? type.label : ''
})

const openSpecialPassengerDialog = async () => {
  try {
    // 先清空所有数据
    selectedOrder.value = null
    Object.assign(specialPassengerForm, {
      orderId: null,
      countryCode: '+86',
      phone: '',
      passengerType: '',
      departureAirport: '',
      arrivalAirport: '',
      entryServices: { selfEquipment: false, priorityEntry: false, wheelchair: false, stretcher: false },
      exitServices: { selfEquipment: false, convenientExit: false, wheelchair: false, stretcher: false },
      description: ''
    })
    specialPassengerDialog.error = ''
    
    // 检查是否有从订单列表返回的订单选择
    const selectedOrderData = sessionStorage.getItem('selectedOrderForSpecialPassenger')
    if (selectedOrderData) {
      try {
        const orderData = JSON.parse(selectedOrderData)
        // 填充订单信息
        selectedOrder.value = {
          id: orderData.id || orderData.orderId,
          orderNo: orderData.orderNo || orderData.order_no || String(orderData.id || orderData.orderId),
          route: orderData.route || '',
          time: orderData.time || '',
          amount: orderData.amount || 0
        }
        specialPassengerForm.orderId = orderData.id || orderData.orderId
        
        // 解析航线，填充出发机场和到达机场
        if (orderData.route) {
          const routeInfo = parseRoute(orderData.route)
          specialPassengerForm.departureAirport = routeInfo.origin
          specialPassengerForm.arrivalAirport = routeInfo.destination
        }
        
        // 清除sessionStorage中的选择数据
        sessionStorage.removeItem('selectedOrderForSpecialPassenger')
      } catch (e) {
        console.warn('解析选中的订单数据失败:', e)
      }
    }
    
    // 加载订单列表（用于下拉选择，如果用户想重新选择）
    const ordersResult = await orderApi.getOrders()
    if (ordersResult && ordersResult.orders) {
      availableOrders.value = ordersResult.orders.map((order: any) => ({
        id: order.id,
        orderNo: order.orderNo || order.order_no || String(order.id),
        route: `${order.origin || order.route || ''} → ${order.destination || ''}`,
        time: order.departureTime || order.time || '',
        amount: order.amount || order.price || 0
      }))
    }
    specialPassengerDialog.visible = true
  } catch (error) {
    console.error('加载订单列表失败:', error)
  }
}

const closeSpecialPassengerDialog = () => {
  specialPassengerDialog.visible = false
  showOrderSelector.value = false
  showPassengerTypeSelector.value = false
  showDepartureAirportSelector.value = false
  showArrivalAirportSelector.value = false
}

const toggleOrderSelector = () => {
  // 跳转到订单列表页面，传递参数表示从预约页面来
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
  specialPassengerForm.orderId = order.id
  showOrderSelector.value = false
  
  // 解析航线，自动填充出发机场和到达机场
  if (order.route) {
    const routeInfo = parseRoute(order.route)
    specialPassengerForm.departureAirport = routeInfo.origin
    specialPassengerForm.arrivalAirport = routeInfo.destination
  }
}

const togglePassengerTypeSelector = () => {
  showPassengerTypeSelector.value = !showPassengerTypeSelector.value
  showOrderSelector.value = false
  showDepartureAirportSelector.value = false
  showArrivalAirportSelector.value = false
}

const selectPassengerType = (type: string) => {
  specialPassengerForm.passengerType = type
  showPassengerTypeSelector.value = false
}

const toggleDepartureAirportSelector = () => {
  showDepartureAirportSelector.value = !showDepartureAirportSelector.value
  showOrderSelector.value = false
  showPassengerTypeSelector.value = false
  showArrivalAirportSelector.value = false
}

const selectDepartureAirport = (airport: string) => {
  specialPassengerForm.departureAirport = airport
  showDepartureAirportSelector.value = false
}

const toggleArrivalAirportSelector = () => {
  showArrivalAirportSelector.value = !showArrivalAirportSelector.value
  showOrderSelector.value = false
  showPassengerTypeSelector.value = false
  showDepartureAirportSelector.value = false
}

const selectArrivalAirport = (airport: string) => {
  specialPassengerForm.arrivalAirport = airport
  showArrivalAirportSelector.value = false
}

// 处理成功提示窗确认
const handleSuccessConfirm = () => {
  successDialog.visible = false
  // 跳转到普通乘客首页
  router.push('/portal/passengers')
}

const submitSpecialPassengerRequest = async () => {
  // 验证必填字段
  if (!specialPassengerForm.orderId) {
    specialPassengerDialog.error = '请选择订单'
    return
  }
  if (!specialPassengerForm.phone || !/^1[3-9]\d{9}$/.test(specialPassengerForm.phone)) {
    specialPassengerDialog.error = '请输入正确的手机号'
    return
  }
  if (!specialPassengerForm.passengerType) {
    specialPassengerDialog.error = '请选择旅客类型'
    return
  }
  if (!specialPassengerForm.departureAirport) {
    specialPassengerDialog.error = '请填写出发机场'
    return
  }
  if (!specialPassengerForm.arrivalAirport) {
    specialPassengerDialog.error = '请填写到达机场'
    return
  }

  specialPassengerDialog.error = ''
  specialPassengerDialog.submitting = true

  try {
    // 获取订单号 - 直接使用选择订单的订单号
    let orderNo = ''
    if (selectedOrder.value && selectedOrder.value.orderNo) {
      // 直接使用选择订单的订单号
      orderNo = selectedOrder.value.orderNo
    } else if (specialPassengerForm.orderId) {
      // 如果没有selectedOrder或orderNo，使用orderId作为订单号
      orderNo = String(specialPassengerForm.orderId)
    } else {
      throw new Error('无法获取订单号')
    }
    
    // 调用后端API提交重点旅客预约
    await specialServiceRequestApi.createRequest({
      orderNo: orderNo,
      phone: specialPassengerForm.phone,
      passengerType: specialPassengerForm.passengerType,
      departureAirport: specialPassengerForm.departureAirport,
      arrivalAirport: specialPassengerForm.arrivalAirport,
      entryServices: specialPassengerForm.entryServices,
      exitServices: specialPassengerForm.exitServices,
      description: specialPassengerForm.description || undefined
    })
    
    closeSpecialPassengerDialog()
    successDialog.title = '提交成功'
    successDialog.message = '您的重点旅客预约申请已提交，我们将在24小时内与您联系确认服务安排。'
    successDialog.visible = true
  } catch (error: any) {
    console.error('提交申请失败:', error)
    specialPassengerDialog.error = error.message || '提交失败，请重试'
  } finally {
    specialPassengerDialog.submitting = false
  }
}

// 点击外部关闭下拉
const handleClickOutside = (event: MouseEvent) => {
  const target = event.target as HTMLElement
  // 检查是否点击在下拉框相关元素上
  const isClickOnDropdown = target.closest('.order-selector-dropdown') ||
                            target.closest('.passenger-type-dropdown') ||
                            target.closest('.airport-selector-dropdown') ||
                            target.closest('.select-wrapper')
  
  if (!isClickOnDropdown) {
    showOrderSelector.value = false
    showPassengerTypeSelector.value = false
    showDepartureAirportSelector.value = false
    showArrivalAirportSelector.value = false
  }
}

// 注意：onMounted已经在上面定义过了，这里只添加事件监听
// 添加点击外部关闭下拉的事件监听（在第一个onMounted中已经处理，这里不需要重复）

onUnmounted(() => {
  // 移除事件监听
  document.removeEventListener('click', handleClickOutside)
  // 清除定时器
  if (flightQueryRefreshTimer.value) {
    clearInterval(flightQueryRefreshTimer.value)
    flightQueryRefreshTimer.value = null
  }
})
</script>

<style scoped>
.breadcrumb {
  margin-bottom: 20px;
  font-size: 14px;
  color: var(--text-secondary, rgba(255, 255, 255, 0.7));
}

.breadcrumb-separator {
  margin: 0 8px;
  color: #999;
}

.grid-two {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 1.5rem;
  margin-bottom: 20px;
}

.glass-card {
  border-radius: 8px;
  padding: 1.8rem;
  background: var(--bg-secondary, rgba(2, 6, 23, 0.7));
  border: 1px solid var(--border-color, rgba(255, 255, 255, 0.1));
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  backdrop-filter: blur(18px);
}

.glass-card h2 {
  color: var(--text-primary, #fff);
  margin-bottom: 1rem;
}

.primary-btn,
.ghost-btn {
  border-radius: 4px;
  padding: 0.65rem 1.6rem;
  border: 1px solid transparent;
  font-weight: 600;
  cursor: pointer;
  font-size: 14px;
}

.primary-btn {
  background: #2196f3;
  color: #fff;
}

.primary-btn:hover {
  background: #1976d2;
}

.ghost-btn {
  border-color: #ddd;
  background: #fff;
  color: #666;
}

.ghost-btn:hover {
  background: #f5f5f5;
}

.info-list {
  list-style: none;
  padding: 0;
  margin: 1rem 0 0;
  display: flex;
  flex-direction: column;
  gap: 0.9rem;
}

.info-list li {
  display: flex;
  justify-content: space-between;
  color: var(--text-secondary, rgba(255, 255, 255, 0.7));
}

.info-list strong {
  color: var(--text-primary, #fff);
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 1rem;
  margin: 1rem 0;
}

.stat-item {
  text-align: center;
  padding: 1rem;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 8px;
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.stat-value {
  font-size: 1.5rem;
  font-weight: 700;
  color: var(--text-primary, #fff);
  margin-bottom: 0.5rem;
}

.stat-label {
  font-size: 0.85rem;
  color: var(--text-secondary, rgba(255, 255, 255, 0.7));
}

.stats-actions {
  display: flex;
  gap: 0.75rem;
  flex-wrap: wrap;
  margin-top: 1rem;
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
}

.quick-actions-grid {
  display: grid;
  /* 三列布局以支持 7 个功能项：宽度自适应但不小于 200px */
  grid-template-columns: repeat(3, minmax(200px, 1fr));
  gap: 24px; /* 横向与纵向间距一致 */
  margin-top: 1.25rem;
  align-items: start;
  justify-items: stretch;
  padding: 8px 0;
}

@media (max-width: 1200px) {
  .quick-actions-grid {
    grid-template-columns: repeat(2, minmax(180px, 1fr));
    gap: 20px;
  }
}

@media (max-width: 768px) {
  .quick-actions-grid {
    grid-template-columns: 1fr;
  }
}

.quick-action-card-wrapper {
  position: relative;
}

.quick-action-card {
  padding: 16px;
  display: flex;
  align-items: center; /* 垂直居中图标与文字 */
  gap: 14px;
  background: rgba(255, 255, 255, 0.04);
  border: 2px solid rgba(255, 255, 255, 0.10);
  border-radius: 8px; /* 轻微圆角 */
  min-height: 84px; /* 统一高度，适应内容 */
  box-sizing: border-box;
  width: 100%;
  cursor: pointer;
  transition: transform 180ms ease, box-shadow 180ms ease, border-color 180ms ease;
  text-align: left;
}

.quick-action-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 14px 40px rgba(16, 24, 40, 0.18);
  border-color: rgba(37, 99, 235, 0.95);
}

/* 区分每个快捷操作卡片的边框视觉（按顺序） */
.quick-actions-grid .quick-action-card:nth-child(1) {
  box-shadow: 0 14px 40px rgba(16, 24, 40, 0.18), inset 6px 0 0 rgba(37,99,235,1);
  border-color: rgba(37,99,235,0.18);
}
.quick-actions-grid .quick-action-card:nth-child(2) {
  box-shadow: 0 14px 40px rgba(16, 24, 40, 0.18), inset 6px 0 0 rgba(245,158,11,1);
  border-color: rgba(245,158,11,0.18);
}
.quick-actions-grid .quick-action-card:nth-child(3) {
  box-shadow: 0 14px 40px rgba(16, 24, 40, 0.18), inset 6px 0 0 rgba(139,92,246,1);
  border-color: rgba(139,92,246,0.18);
}
.quick-actions-grid .quick-action-card:nth-child(4) {
  box-shadow: 0 14px 40px rgba(16, 24, 40, 0.18), inset 6px 0 0 rgba(16,185,129,1);
  border-color: rgba(16,185,129,0.18);
}
.quick-actions-grid .quick-action-card:nth-child(5) {
  box-shadow: 0 14px 40px rgba(16, 24, 40, 0.18), inset 6px 0 0 rgba(6,182,212,1);
  border-color: rgba(6,182,212,0.18);
}
.quick-actions-grid .quick-action-card:nth-child(6) {
  box-shadow: 0 14px 40px rgba(16, 24, 40, 0.18), inset 6px 0 0 rgba(59,130,246,1);
  border-color: rgba(59,130,246,0.18);
}

/* 航班实时查询面板样式 */
.quick-action-card .action-content {
  display: flex;
  align-items: center;
  gap: 12px;
  width: 100%;
}

.action-icon {
  width: 56px;
  height: 56px;
  min-width: 56px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(180deg,#ffffff,#f2f7ff);
  border: 1px solid rgba(7,34,58,0.06);
  box-shadow: 0 6px 18px rgba(7,34,58,0.04);
  flex-shrink: 0;
}

.action-title {
  font-size: 16px;
  font-weight: 700;
  color: var(--text-primary, #0A1F33);
  margin: 0;
}
.action-desc {
  font-size: 13px;
  color: var(--text-secondary, rgba(55,65,81,0.7));
  margin-top: 4px;
}

.flight-query-panel {
  margin-top: 1rem;
  padding: 1rem;
  background: rgba(30, 138, 230, 0.1);
  border: 1px solid rgba(30, 138, 230, 0.3);
  border-radius: 12px;
  backdrop-filter: blur(10px);
}

.flight-query-header {
  margin-bottom: 0.75rem;
}

.flight-query-title {
  font-size: 0.9rem;
  font-weight: 600;
  color: var(--text-primary, #fff);
}

.flight-query-list {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.flight-query-item {
  padding: 0.75rem;
  background: rgba(2, 6, 23, 0.4);
  border: 1px solid rgba(255, 255, 255, 0.1);
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

.action-icon {
  font-size: 2.5rem;
  margin-bottom: 0.75rem;
}

.action-title {
  font-size: 1rem;
  font-weight: 600;
  color: var(--text-primary, #fff);
  margin-bottom: 0.5rem;
}

.action-desc {
  font-size: 0.85rem;
  color: var(--text-secondary, rgba(255, 255, 255, 0.7));
}

/* 主题设置样式已移至ThemeSettings组件 */

/* 重点旅客预约弹窗样式 */
.special-passenger-overlay {
  position: fixed;
  inset: 0;
  background: rgba(2, 6, 23, 0.75);
  backdrop-filter: blur(8px);
  display: flex;
  align-items: flex-start;
  justify-content: center;
  z-index: 1200;
  padding: clamp(16px, 4vw, 32px);
  padding-top: calc(clamp(16px, 4vw, 32px) + 50px);
}

.special-passenger-modal {
  width: min(600px, 100%);
  background: rgba(15, 23, 42, 0.96);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 24px;
  padding: clamp(20px, 4vw, 32px);
  box-shadow: 0 30px 80px rgba(3, 7, 18, 0.6);
  color: #fff;
  max-height: 90vh;
  overflow-y: auto;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.modal-label {
  font-size: 13px;
  text-transform: uppercase;
  letter-spacing: 0.2em;
  color: rgba(255, 255, 255, 0.5);
  margin-bottom: 8px;
}

.modal-header h3 {
  margin: 0;
  color: #fff;
  font-size: 20px;
}

.modal-close {
  background: rgba(148, 163, 184, 0.1);
  border: 1px solid rgba(148, 163, 184, 0.2);
  color: #cbd5e1;
  font-size: 24px;
  line-height: 1;
  cursor: pointer;
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 10px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.modal-close:hover {
  background: rgba(239, 68, 68, 0.15);
  border-color: rgba(239, 68, 68, 0.3);
  color: #f87171;
  transform: rotate(90deg) scale(1.05);
}

.modal-close:active {
  transform: rotate(90deg) scale(0.95);
}

.modal-close-old {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  border: 1px solid rgba(255, 255, 255, 0.2);
  background: transparent;
  color: #fff;
  font-size: 20px;
  line-height: 1;
  cursor: pointer;
}

.special-passenger-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
  position: relative;
}

.form-label {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.9);
  font-weight: 500;
}

.required {
  color: #f87171;
  margin-left: 2px;
}

.optional {
  color: rgba(255, 255, 255, 0.5);
  font-size: 12px;
  margin-left: 4px;
}

.form-input,
.form-textarea {
  width: 100%;
  padding: 12px 16px;
  border-radius: 12px;
  border: 1px solid rgba(255, 255, 255, 0.16);
  background: rgba(2, 6, 23, 0.4);
  color: #fff;
  font-size: 14px;
  font-family: inherit;
  transition: all 0.2s ease;
}

.form-input:focus,
.form-textarea:focus {
  outline: none;
  border-color: #1E8AE6;
  box-shadow: 0 0 0 2px rgba(30, 138, 230, 0.2);
}

.form-textarea {
  resize: vertical;
  min-height: 80px;
}

.select-wrapper {
  position: relative;
  cursor: pointer;
}

.select-input {
  cursor: pointer;
  padding-right: 40px;
}

.select-arrow {
  position: absolute;
  right: 16px;
  top: 50%;
  transform: translateY(-50%);
  color: rgba(255, 255, 255, 0.6);
  font-size: 16px;
  pointer-events: none;
}

.phone-input-wrapper {
  display: flex;
  gap: 8px;
}

.country-code-select {
  width: 80px;
  padding: 12px;
  border-radius: 12px;
  border: 1px solid rgba(255, 255, 255, 0.16);
  background: rgba(2, 6, 23, 0.4);
  color: #fff;
  font-size: 14px;
  cursor: pointer;
}

.phone-input {
  flex: 1;
}

.order-selector-dropdown,
.passenger-type-dropdown,
.airport-selector-dropdown {
  position: absolute;
  top: calc(100% + 8px);
  left: 0;
  right: 0;
  background: rgba(15, 23, 42, 0.98);
  border: 1px solid rgba(255, 255, 255, 0.16);
  border-radius: 12px;
  max-height: 300px;
  overflow-y: auto;
  z-index: 1000;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.5);
}

.order-option {
  padding: 14px 16px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
  cursor: pointer;
  transition: background 0.2s ease;
}

.order-option:hover {
  background: rgba(30, 138, 230, 0.1);
}

.order-option:last-child {
  border-bottom: none;
}

.order-option.empty {
  text-align: center;
  color: rgba(255, 255, 255, 0.5);
  cursor: default;
}

.order-option-main {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
}

.order-option-main strong {
  color: #fff;
  font-size: 15px;
}

.order-option-main span {
  color: rgba(255, 255, 255, 0.7);
  font-size: 14px;
}

.order-option-meta {
  display: flex;
  gap: 12px;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.5);
}

.passenger-type-option {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
  cursor: pointer;
  transition: background 0.2s ease;
  color: #fff;
}

.passenger-type-option:hover {
  background: rgba(30, 138, 230, 0.1);
}

.passenger-type-option:last-child {
  border-bottom: none;
}

.radio-icon {
  width: 20px;
  height: 20px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-radius: 50%;
  position: relative;
  flex-shrink: 0;
  transition: all 0.2s ease;
}

.radio-icon.checked {
  border-color: #1E8AE6;
  background: #1E8AE6;
}

.radio-icon.checked::after {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 8px;
  height: 8px;
  background: #fff;
  border-radius: 50%;
}

.airport-option {
  padding: 14px 16px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
  cursor: pointer;
  transition: background 0.2s ease;
  color: #fff;
}

.airport-option:hover {
  background: rgba(30, 138, 230, 0.1);
}

.airport-option:last-child {
  border-bottom: none;
}

.checkbox-group {
  display: flex;
  gap: 24px;
  flex-wrap: wrap;
}

.checkbox-item {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  user-select: none;
}

.checkbox-input {
  width: 20px;
  height: 20px;
  cursor: pointer;
  accent-color: #1E8AE6;
}

.checkbox-label {
  color: rgba(255, 255, 255, 0.9);
  font-size: 14px;
}

.info-banner {
  padding: 12px 16px;
  background: rgba(251, 191, 36, 0.15);
  border: 1px solid rgba(251, 191, 36, 0.3);
  border-radius: 12px;
  color: rgba(251, 191, 36, 0.9);
  font-size: 13px;
  line-height: 1.5;
}

.form-error {
  color: #fda4af;
  font-size: 13px;
  margin: -8px 0 0;
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  flex-wrap: wrap;
  margin-top: 8px;
}

.modal-btn {
  border-radius: 999px;
  padding: 10px 24px;
  font-weight: 600;
  cursor: pointer;
  border: 1px solid transparent;
  transition: all 0.2s ease;
}

.modal-btn.ghost {
  background: transparent;
  border-color: rgba(255, 255, 255, 0.3);
  color: rgba(255, 255, 255, 0.85);
}

.modal-btn.primary {
  background: linear-gradient(120deg, #1E8AE6, #0A2F63);
  color: #fff;
  box-shadow: 0 10px 24px rgba(30, 138, 230, 0.3);
}

.modal-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

@media (max-width: 640px) {
  .special-passenger-modal {
    max-width: 95%;
    padding: 20px;
  }
  
  .phone-input-wrapper {
    flex-direction: column;
  }
  
  .country-code-select {
    width: 100%;
  }
  
  .checkbox-group {
    flex-direction: column;
    gap: 12px;
  }
}

</style>


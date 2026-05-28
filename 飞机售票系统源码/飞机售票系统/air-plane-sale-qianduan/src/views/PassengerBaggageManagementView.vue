<template>
  <PassengerLayout>
    <div class="baggage-management-page">
      <!-- 页面头部 -->
      <div class="page-header">
        <div class="header-content">
          <h1>行李管理</h1>
          <p>登记、查询和管理您的行李信息</p>
        </div>
        <button class="btn-primary" @click="showCreateForm = !showCreateForm">
          {{ showCreateForm ? '取消登记' : '登记行李' }}
        </button>
      </div>

      <!-- 统计卡片 -->
      <div class="stats-grid">
        <div class="stat-card">
          <div class="stat-icon">🧳</div>
          <div class="stat-info">
            <div class="stat-label">总行李数</div>
            <div class="stat-value">{{ stats.total }}</div>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon">📦</div>
          <div class="stat-info">
            <div class="stat-label">已登记</div>
            <div class="stat-value">{{ stats.registered }}</div>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon">✈️</div>
          <div class="stat-info">
            <div class="stat-label">运输中</div>
            <div class="stat-value">{{ stats.inTransit }}</div>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon">✅</div>
          <div class="stat-info">
            <div class="stat-label">已提取</div>
            <div class="stat-value">{{ stats.delivered }}</div>
          </div>
        </div>
      </div>

      <!-- 登记表单 -->
      <div v-if="showCreateForm" class="form-card">
        <h2>登记新行李</h2>
        <form @submit.prevent="handleSubmit">
          <!-- 订单选择 -->
          <div class="form-group">
            <label>选择订单 <span class="required">*</span></label>
            <div class="select-wrapper">
              <button
                type="button"
                class="select-btn"
                @click="showOrderList = !showOrderList"
                :class="{ 'has-value': form.orderNo }"
              >
                {{ selectedOrderText || '请选择订单' }}
                <span class="arrow">▼</span>
              </button>
              <div v-if="showOrderList" class="dropdown">
                <div class="dropdown-header">
                  <input
                    v-model="orderSearchKeyword"
                    type="text"
                    placeholder="搜索订单号..."
                    class="search-input"
                    @click.stop
                  />
                </div>
                <div class="dropdown-list">
                <div
                  v-for="order in filteredOrders"
                  :key="order.id"
                  class="dropdown-item"
                  :class="{ disabled: orderHasBaggage(order.orderNo || order.order_no) }"
                  @click="selectOrder(order)"
                >
                  <div class="order-info">
                    <span class="order-no">{{ order.orderNo || order.order_no }}</span>
                    <span v-if="orderHasBaggage(order.orderNo || order.order_no)" class="order-registered">（已登记）</span>
                  </div>
                </div>
                  <div v-if="filteredOrders.length === 0" class="dropdown-empty">
                    暂无可用订单
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 行李类型 -->
          <div class="form-group">
            <label>行李类型 <span class="required">*</span></label>
            <div class="radio-group">
              <label
                v-for="type in baggageTypes"
                :key="type.value"
                class="radio-label"
                :class="{ active: form.baggageType === type.value }"
              >
                <input
                  type="radio"
                  :value="type.value"
                  v-model="form.baggageType"
                  class="radio-input"
                />
                <span class="radio-text">{{ type.label }}</span>
              </label>
            </div>
          </div>

          <!-- 行李数量 -->
          <div class="form-group">
            <label>行李数量 <span class="required">*</span></label>
            <input
              v-model.number="form.baggageCount"
              type="number"
              min="1"
              max="10"
              class="form-input"
              placeholder="请输入行李数量"
              required
            />
          </div>

          <!-- 重量信息（并排显示） -->
          <div class="form-row">
            <div class="form-group form-group-half">
              <label>总重量（公斤）</label>
              <input
                v-model.number="form.totalWeight"
                type="number"
                step="0.1"
                min="0"
                class="form-input"
                placeholder="请输入总重量"
                @input="calculateFees"
              />
            </div>
            <div class="form-group form-group-half">
              <label>重量限制（公斤）</label>
              <input
                v-model.number="form.weightLimit"
                type="number"
                step="0.1"
                min="0"
                class="form-input"
                placeholder="20"
                readonly
                style="background-color: rgba(255, 255, 255, 0.7); cursor: not-allowed;"
              />
            </div>
          </div>

          <!-- 尺寸 -->
          <div class="form-group">
            <label>尺寸（长×宽×高，厘米）</label>
            <input
              v-model="form.dimensions"
              type="text"
              class="form-input"
              placeholder="例如：50×40×30"
            />
          </div>

          <!-- 费用信息（并排显示） -->
          <div class="form-row">
            <div class="form-group form-group-half">
              <label>行李费用（元）</label>
              <input
                v-model.number="form.baggageFee"
                type="number"
                step="0.01"
                min="0"
                class="form-input"
                placeholder="自动计算"
                readonly
                style="background-color: rgba(255, 255, 255, 0.7); cursor: not-allowed;"
              />
            </div>
            <div class="form-group form-group-half">
              <label>超重/超规费用（元）</label>
              <input
                v-model.number="form.excessFee"
                type="number"
                step="0.01"
                min="0"
                class="form-input"
                placeholder="自动计算"
                readonly
                style="background-color: rgba(255, 255, 255, 0.7); cursor: not-allowed;"
              />
            </div>
          </div>

          <!-- 描述 -->
          <div class="form-group">
            <label>行李描述</label>
            <textarea
              v-model="form.description"
              class="form-textarea"
              rows="3"
              placeholder="请输入行李描述（可选）"
            ></textarea>
          </div>

          <!-- 备注 -->
          <div class="form-group">
            <label>备注</label>
            <textarea
              v-model="form.remark"
              class="form-textarea"
              rows="2"
              placeholder="请输入备注信息（可选）"
            ></textarea>
          </div>

          <!-- 表单错误提示 -->
          <div v-if="formError" class="form-error">{{ formError }}</div>

          <!-- 提交按钮 -->
          <div class="form-actions">
            <button type="button" class="btn-cancel" @click="showCreateForm = false">取消</button>
            <button type="button" class="btn-secondary" @click="resetForm">重置</button>
            <button type="submit" class="btn-primary" :disabled="submitting">
              {{ submitting ? '提交中...' : '提交登记' }}
            </button>
          </div>
        </form>
      </div>

      <!-- 筛选和搜索 -->
      <div class="filter-bar">
        <div class="filter-group">
          <label>状态筛选：</label>
          <select v-model="statusFilter" class="filter-select" @change="loadBaggageList">
            <option value="">全部</option>
            <option value="registered">已登记</option>
            <option value="checked_in">已托运</option>
            <option value="in_transit">运输中</option>
            <option value="arrived">已到达</option>
            <option value="delivered">已提取</option>
          </select>
        </div>
        <div class="search-group">
          <input
            v-model="searchKeyword"
            type="text"
            class="search-input"
            placeholder="搜索行李编号或订单号..."
            @input="handleSearch"
          />
        </div>
      </div>

      <!-- 行李列表 -->
      <div class="baggage-list">
        <div v-if="loading" class="loading">加载中...</div>
        <div v-else-if="baggageList.length === 0" class="empty-state">
          <div class="empty-icon">🧳</div>
          <div class="empty-text">暂无行李记录</div>
        </div>
        <div v-else class="baggage-items">
          <div
            v-for="baggage in filteredBaggageList"
            :key="baggage.id"
            class="baggage-item"
          >
            <div class="baggage-header">
              <div class="baggage-no">
                <span class="label">行李编号：</span>
                <span class="value">{{ baggage.baggageNo || '待生成' }}</span>
              </div>
              <div class="baggage-status" :class="`status-${baggage.status}`">
                {{ getStatusText(baggage.status) }}
              </div>
            </div>
            <div class="baggage-body">
              <div class="baggage-info">
                <div class="info-item">
                  <span class="label">订单号：</span>
                  <span class="value">{{ baggage.orderno }}</span>
                </div>
                <div class="info-item">
                  <span class="label">类型：</span>
                  <span class="value">{{ getBaggageTypeText(baggage.baggageType) }}</span>
                </div>
                <div class="info-item">
                  <span class="label">数量：</span>
                  <span class="value">{{ baggage.baggageCount }}件</span>
                </div>
                <div v-if="baggage.totalWeight" class="info-item">
                  <span class="label">重量：</span>
                  <span class="value">{{ baggage.totalWeight }}kg</span>
                </div>
              </div>
            </div>
            <div class="baggage-footer">
              <button class="view-detail-btn" @click.stop="viewDetail(baggage)">
                <span class="btn-icon">👁️</span>
                <span class="btn-text">查看详情</span>
              </button>
              <!-- 当状态为 arrived（已到达）时显示提取按钮 -->
              <button
                v-if="baggage.status === 'arrived'"
                class="view-detail-btn"
                @click.stop="handlePickup(baggage)"
                :disabled="pickupLoading"
              >
                <span class="btn-icon">🎟️</span>
                <span class="btn-text">{{ pickupLoading ? '提取中...' : '提取行李' }}</span>
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- 分页 -->
      <div v-if="baggageList.length > 0" class="pagination">
        <button
          class="page-btn"
          :disabled="currentPage === 0"
          @click="changePage(currentPage - 1)"
        >
          上一页
        </button>
        <span class="page-info">
          第 {{ currentPage + 1 }} 页 / 共 {{ totalPages }} 页
        </span>
        <button
          class="page-btn"
          :disabled="currentPage >= totalPages - 1"
          @click="changePage(currentPage + 1)"
        >
          下一页
        </button>
      </div>

      <!-- 详情模态框 -->
      <div v-if="showDetailModal" class="modal-overlay" @click="showDetailModal = false">
        <div class="modal-content" @click.stop>
          <div class="modal-header">
            <h2>行李详情</h2>
            <button class="modal-close" @click="showDetailModal = false">×</button>
          </div>
          <div v-if="selectedBaggage" class="modal-body">
            <div class="detail-section">
              <h3>基本信息</h3>
              <div class="detail-grid">
                <div class="detail-item">
                  <span class="detail-label">行李编号：</span>
                  <span class="detail-value">{{ selectedBaggage.baggageNo || '待生成' }}</span>
                </div>
                <div class="detail-item">
                  <span class="detail-label">订单号：</span>
                  <span class="detail-value">{{ selectedBaggage.orderno }}</span>
                </div>
                <div class="detail-item">
                  <span class="detail-label">状态：</span>
                  <span class="detail-value" :class="`status-${selectedBaggage.status}`">
                    {{ getStatusText(selectedBaggage.status) }}
                  </span>
                </div>
                <div class="detail-item">
                  <span class="detail-label">类型：</span>
                  <span class="detail-value">{{ getBaggageTypeText(selectedBaggage.baggageType) }}</span>
                </div>
                <div class="detail-item">
                  <span class="detail-label">数量：</span>
                  <span class="detail-value">{{ selectedBaggage.baggageCount }}件</span>
                </div>
                <div v-if="selectedBaggage.totalWeight" class="detail-item">
                  <span class="detail-label">总重量：</span>
                  <span class="detail-value">{{ selectedBaggage.totalWeight }}kg</span>
                </div>
                <div v-if="selectedBaggage.weightLimit" class="detail-item">
                  <span class="detail-label">重量限制：</span>
                  <span class="detail-value">{{ selectedBaggage.weightLimit }}kg</span>
                </div>
                <div v-if="selectedBaggage.dimensions" class="detail-item">
                  <span class="detail-label">尺寸：</span>
                  <span class="detail-value">{{ selectedBaggage.dimensions }}</span>
                </div>
                <div v-if="selectedBaggage.baggageFee != null && selectedBaggage.baggageFee !== 0" class="detail-item">
                  <span class="detail-label">行李费用：</span>
                  <span class="detail-value">¥{{ selectedBaggage.baggageFee }}</span>
                </div>
                <div v-if="selectedBaggage.excessFee != null && selectedBaggage.excessFee !== 0" class="detail-item">
                  <span class="detail-label">超重/超规费用：</span>
                  <span class="detail-value">¥{{ selectedBaggage.excessFee }}</span>
                </div>
              </div>
            </div>
            <div v-if="selectedBaggage.description" class="detail-section">
              <h3>描述</h3>
              <p class="detail-text">{{ selectedBaggage.description }}</p>
            </div>
            <div v-if="selectedBaggage.remark" class="detail-section">
              <h3>备注</h3>
              <p class="detail-text">{{ selectedBaggage.remark }}</p>
            </div>
            <div v-if="selectedBaggage.operatorRemark" class="detail-section">
              <h3>运营备注</h3>
              <p class="detail-text">{{ selectedBaggage.operatorRemark }}</p>
            </div>
            <div class="detail-section">
              <h3>时间信息</h3>
              <div class="detail-grid">
                <div v-if="selectedBaggage.createTime || selectedBaggage.create_time" class="detail-item">
                  <span class="detail-label">登记时间：</span>
                  <span class="detail-value">
                    {{ formatDate(selectedBaggage.createTime || selectedBaggage.create_time) }}
                  </span>
                </div>
                <div v-if="selectedBaggage.updateTime || selectedBaggage.update_time" class="detail-item">
                  <span class="detail-label">更新时间：</span>
                  <span class="detail-value">
                    {{ formatDate(selectedBaggage.updateTime || selectedBaggage.update_time) }}
                  </span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 成功提示模态框 -->
      <ModalPrompt
        v-model="showSuccessModal"
        type="success"
        title="登记成功"
        :message="successMessage"
        confirm-text="确定"
        @confirm="showSuccessModal = false"
      />
    </div>
  </PassengerLayout>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import PassengerLayout from '../components/layout/PassengerLayout.vue'
import ModalPrompt from '../components/ModalPrompt.vue'
import { baggageApi, orderApi, apiUtils } from '../services/api'

const router = useRouter()

// 页面状态
const loading = ref(false)
const submitting = ref(false)
const showCreateForm = ref(false)
const showOrderList = ref(false)
const showDetailModal = ref(false)
const showSuccessModal = ref(false)
const formError = ref('')
const successMessage = ref('')
const orderSearchKeyword = ref('')
const searchKeyword = ref('')
const statusFilter = ref('')

// 分页
const currentPage = ref(0)
const pageSize = ref(5)
const totalPages = ref(0)

// 数据
const baggageList = ref<any[]>([])
const orders = ref<any[]>([])
const selectedBaggage = ref<any>(null)

// 统计数据
const stats = reactive({
  total: 0,
  registered: 0,
  inTransit: 0,
  delivered: 0
})

// 表单数据
const form = reactive({
  orderNo: '',
  baggageType: '',
  baggageCount: 1,
  totalWeight: null as number | null,
  weightLimit: 20, // 统一设为20公斤
  dimensions: '',
  baggageFee: null as number | null,
  excessFee: null as number | null,
  description: '',
  remark: '',
  arrivalTimeFlight: null as string | null // 订单到达时间
})

// 行李类型选项（使用中文字段值）
const baggageTypes = [
  { label: '托运行李', value: '托运行李' },
  { label: '手提行李', value: '手提行李' },
  { label: '特殊行李', value: '特殊行李' }
]

// 计算属性
const selectedOrderText = computed(() => {
  if (!form.orderNo) return ''
  const order = orders.value.find(o => (o.orderNo || o.order_no) === form.orderNo)
  if (order) {
    return order.orderNo || order.order_no || form.orderNo
  }
  return form.orderNo
})

const filteredOrders = computed(() => {
  if (!orderSearchKeyword.value) return orders.value
  const keyword = orderSearchKeyword.value.toLowerCase()
  return orders.value.filter(order => {
    const orderNo = (order.orderNo || order.order_no || '').toLowerCase()
    return orderNo.includes(keyword)
  })
})

const filteredBaggageList = computed(() => {
  if (!searchKeyword.value) return baggageList.value
  const keyword = searchKeyword.value.toLowerCase()
  return baggageList.value.filter(baggage => {
    const baggageNo = (baggage.baggageNo || '').toLowerCase()
    const orderNo = (baggage.orderno || '').toLowerCase()
    return baggageNo.includes(keyword) || orderNo.includes(keyword)
  })
})

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

// 方法
const loadOrders = async () => {
  try {
    const result = await orderApi.getOrders({ page: 0, size: 100 })
    if (result && result.orders) {
      // 只保留"待出行"状态的订单
      orders.value = result.orders.filter((order: any) => isTicketedStatus(order))
    }
  } catch (error: any) {
    console.error('加载订单列表失败:', error)
  }
}

const loadBaggageList = async () => {
  loading.value = true
  try {
    const params: any = {
      page: currentPage.value,
      size: pageSize.value
    }
    if (statusFilter.value) {
      params.status = statusFilter.value
    }
    
    const result = await baggageApi.getBaggageList(params)
    if (result && result.list) {
      baggageList.value = result.list
      totalPages.value = result.totalPages || 1
      
      // 更新统计数据
      updateStats(result.list)
    }
  } catch (error: any) {
    console.error('加载行李列表失败:', error)
    formError.value = error.message || '加载行李列表失败'
  } finally {
    loading.value = false
  }
}

// 检查订单是否已有行李登记
const orderHasBaggage = (orderNo: string | undefined | null) => {
  const no = String(orderNo || '').trim()
  if (!no) return false
  return baggageList.value.some(b => {
    const bn = String(b.orderno || b.orderNo || b.order_no || '')
    return bn === no
  })
}

const updateStats = (list: any[]) => {
  stats.total = list.length
  stats.registered = list.filter(b => b.status === 'registered').length
  stats.inTransit = list.filter(b => 
    b.status === 'in_transit' || b.status === 'checked_in'
  ).length
  stats.delivered = list.filter(b => b.status === 'delivered').length
}

const selectOrder = async (order: any) => {
  const orderNo = order.orderNo || order.order_no

  // 如果已有该订单的行李则阻止重复登记
  if (orderHasBaggage(orderNo)) {
    formError.value = '该订单已登记行李，不能重复登记'
    alert('该订单已登记行李，不能重复登记')
    // 关闭下拉但不选中
    showOrderList.value = false
    orderSearchKeyword.value = ''
    return
  }

  form.orderNo = orderNo
  showOrderList.value = false
  orderSearchKeyword.value = ''

  // 获取订单详情以获取到达时间
  try {
    const orderDetail = await orderApi.getOrderDetail(form.orderNo)
    // 尝试多种可能的字段名
    const arrivalTime = orderDetail?.arrivalTime || 
                       orderDetail?.arrival_time || 
                       orderDetail?.arrivalTimeFlight ||
                       order?.arrivalTime ||
                       order?.arrival_time
    if (arrivalTime) {
      form.arrivalTimeFlight = arrivalTime
      console.log('获取到订单到达时间:', arrivalTime)
    }
  } catch (error: any) {
    console.warn('获取订单详情失败，将使用订单列表中的到达时间:', error)
    // 如果获取详情失败，尝试使用订单列表中的到达时间
    const arrivalTime = order?.arrivalTime || order?.arrival_time
    if (arrivalTime) {
      form.arrivalTimeFlight = arrivalTime
      console.log('使用订单列表中的到达时间:', arrivalTime)
    }
  }
  
  // 重置重量限制为20公斤
  form.weightLimit = 20
  // 重新计算费用
  calculateFees()
}

// 计算行李费用和超重费用
const calculateFees = () => {
  const weightLimit = form.weightLimit || 20 // 默认20公斤
  const totalWeight = form.totalWeight
  
  if (!totalWeight || totalWeight <= 0) {
    form.baggageFee = 0
    form.excessFee = 0
    return
  }
  
  // 如果总重量没超过限制重量，费用为0
  if (totalWeight <= weightLimit) {
    form.baggageFee = 0
    form.excessFee = 0
  } else {
    // 超过部分按15元一公斤计算
    const excessWeight = totalWeight - weightLimit
    const fee = Math.ceil(excessWeight) * 15 // 向上取整，每公斤15元
    form.baggageFee = fee
    form.excessFee = fee
  }
}

const handleSubmit = async () => {
  formError.value = ''
  
  // 验证表单
  if (!form.orderNo) {
    formError.value = '请选择订单'
    return
  }
  if (!form.baggageType) {
    formError.value = '请选择行李类型'
    return
  }
  if (!form.baggageCount || form.baggageCount < 1) {
    formError.value = '请输入有效的行李数量'
    return
  }
  
  // 确保重量限制为20公斤
  form.weightLimit = 20
  
  // 重新计算费用
  calculateFees()
  
  submitting.value = true
  try {
    const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'

    // ensure passengerId is included (backend requires it)
    const currentUser = apiUtils.getCurrentUser()
    if (!currentUser || !currentUser.id) {
      alert('请先登录后再登记行李并支付')
      submitting.value = false
      return
    }

    const payload = {
      passengerId: typeof currentUser.id === 'string' ? parseInt(currentUser.id, 10) : currentUser.id,
      baggage: {
        orderno: form.orderNo,
        baggageType: form.baggageType,
        baggageCount: form.baggageCount,
        totalWeight: form.totalWeight || null,
        weightLimit: form.weightLimit || 20,
        dimensions: form.dimensions || undefined,
        baggageFee: form.baggageFee || 0,
        excessFee: form.excessFee || 0,
        description: form.description || undefined,
        remark: form.remark || undefined,
        arrivalTimeFlight: form.arrivalTimeFlight || undefined,
        status: 'registered'
      },
      totalAmount: (form.baggageFee || 0) + (form.excessFee || 0)
    }

    // 先同步打开一个空白窗口，避免浏览器把异步打开的窗口当作弹窗拦截
    const payWindow = window.open('about:blank', '_blank')
    try {
      const resp = await fetch(`${API_BASE_URL}/alipay/payBaggage`, {
        method: 'POST',
        credentials: 'include',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(payload)
      })

      if (!resp.ok) {
        const text = await resp.text().catch(() => '')
        throw new Error(`支付发起失败：${resp.status} ${resp.statusText} ${text}`)
      }

      // 返回的是支付宝页面 HTML（iframe 或新窗口展示）
      const html = await resp.text()
      if (payWindow && !payWindow.closed) {
        payWindow.document.open()
        payWindow.document.write(html)
        payWindow.document.close()
      } else {
        // 如果预打开窗口被阻止或被关闭，回退到在当前窗口打开支付页面
        alert('浏览器阻止弹窗，请允许弹窗或手动打开支付页面')
        const newWin = window.open()
        newWin?.document.open()
        newWin?.document.write(html)
        newWin?.document.close()
      }
    } catch (e) {
      // 出错时关闭预打开的窗口（如果存在），并抛出错误以在上层捕获
      if (payWindow && !payWindow.closed) try { payWindow.close() } catch {}
      throw e
    }

    // 等待支付完成后后端会在回调中创建行李记录并重定向到个人页
    // 这里先关闭表单并提示用户在支付完成后查看个人中心
    successMessage.value = '正在打开支付页面，请完成支付，支付完成后将自动生成行李登记记录。'
    showSuccessModal.value = true
    resetForm()
    showCreateForm.value = false
    // 不立即刷新列表，等待支付回调插入数据后用户回到个人页面时可刷新
  } catch (error: any) {
    console.error('发起行李支付失败:', error)
    formError.value = error.message || '发起支付失败，请重试'
  } finally {
    submitting.value = false
  }
}

const resetForm = () => {
  form.orderNo = ''
  form.baggageType = ''
  form.baggageCount = 1
  form.totalWeight = null
  form.weightLimit = 20 // 重置为20公斤
  form.dimensions = ''
  form.baggageFee = null
  form.excessFee = null
  form.description = ''
  form.remark = ''
  form.arrivalTimeFlight = null
  formError.value = ''
  orderSearchKeyword.value = ''
}

const viewDetail = async (baggage: any) => {
  try {
    const detail = await baggageApi.getBaggageDetail(baggage.id)
    selectedBaggage.value = detail
    showDetailModal.value = true
  } catch (error: any) {
    console.error('获取行李详情失败:', error)
    // 如果获取详情失败，使用列表中的数据
    selectedBaggage.value = baggage
    showDetailModal.value = true
  }
}

const pickupLoading = ref(false)
const handlePickup = async (baggage: any) => {
  if (!confirm('确认已在现场提取到行李并提交？（仅当运营已标记“已到达”时可提取）')) return
  pickupLoading.value = true
  try {
    await baggageApi.pickupBaggage(baggage.id)
    alert('提取成功，已通知运营人员更新状态。')
    await loadBaggageList()
  } catch (error: any) {
    console.error('提取行李失败:', error)
    alert('提取失败：' + (error?.message || error))
  } finally {
    pickupLoading.value = false
  }
}

const handleSearch = () => {
  // 搜索功能由计算属性处理
}

const changePage = (page: number) => {
  if (page >= 0 && page < totalPages.value) {
    currentPage.value = page
    loadBaggageList()
  }
}

const formatDate = (date: string | Date | null | undefined) => {
  if (!date) return '-'
  const d = new Date(date)
  if (isNaN(d.getTime())) return '-'
  return d.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const formatOrderRoute = (order: any) => {
  if (order.departureCity && order.arrivalCity) {
    return `${order.departureCity} → ${order.arrivalCity}`
  }
  if (order.route) {
    return order.route
  }
  return '未知路线'
}

const getStatusText = (status: string) => {
  const statusMap: Record<string, string> = {
    registered: '已登记',
    checked_in: '已托运',
    in_transit: '运输中',
    arrived: '已到达',
    delivered: '已提取',
    lost: '丢失',
    damaged: '损坏',
    delayed: '延误'
  }
  return statusMap[status] || status || '未知'
}

const getBaggageTypeText = (type: string) => {
  // 现在直接使用中文字段值，不需要转换
  if (type === '托运行李' || type === '手提行李' || type === '特殊行李') {
    return type
  }
  // 兼容旧数据格式
  const typeMap: Record<string, string> = {
    checked: '托运行李',
    carry_on: '手提行李',
    special: '特殊行李'
  }
  return typeMap[type] || type || '未知'
}

// 点击外部关闭下拉菜单
const handleClickOutside = (event: MouseEvent) => {
  const target = event.target as HTMLElement
  if (!target.closest('.select-wrapper')) {
    showOrderList.value = false
  }
}

// 生命周期
onMounted(async () => {
  // 先加载已有行李列表，以便在选择订单时能够判断订单是否已登记
  await loadBaggageList()
  await loadOrders()
  document.addEventListener('click', handleClickOutside)
})

watch(() => showCreateForm.value, async (newVal) => {
  if (newVal) {
    // 打开登记表单时先确保已加载行李列表和订单列表，避免重复登记
    await loadBaggageList()
    await loadOrders()
    // 确保重量限制为20公斤
    form.weightLimit = 20
    // 重置费用
    form.baggageFee = null
    form.excessFee = null
    // 清除之前可能的表单错误提示
    formError.value = ''
  }
})

// 监听总重量变化，自动计算费用
watch(() => form.totalWeight, () => {
  calculateFees()
})

// 清理事件监听器
onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
})
</script>

<style scoped>
.baggage-management-page {
  padding: 24px;
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 32px;
}

.header-content h1 {
  font-size: 32px;
  font-weight: 600;
  margin: 0 0 8px 0;
  color: var(--text-primary, #1a1a1a);
}

.header-content p {
  font-size: 16px;
  color: var(--text-secondary, #666);
  margin: 0;
}

.btn-primary {
  padding: 12px 24px;
  background: #ffffff;
  color: #1e3a5f;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  cursor: pointer;
  transition: all 0.3s;
  font-weight: 600;
}

.btn-primary:hover:not(:disabled) {
  background: #f0f0f0;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(255, 255, 255, 0.3);
}

.btn-primary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-secondary {
  padding: 12px 24px;
  background: rgba(255, 255, 255, 0.2);
  color: #ffffff;
  border: 1px solid rgba(255, 255, 255, 0.3);
  border-radius: 8px;
  font-size: 16px;
  cursor: pointer;
  transition: all 0.3s;
}

.btn-secondary:hover {
  background: rgba(255, 255, 255, 0.3);
  border-color: rgba(255, 255, 255, 0.5);
}

.btn-cancel {
  padding: 12px 24px;
  background: rgba(255, 255, 255, 0.15);
  color: #ffffff;
  border: 1px solid rgba(255, 255, 255, 0.3);
  border-radius: 8px;
  font-size: 16px;
  cursor: pointer;
  transition: all 0.3s;
}

.btn-cancel:hover {
  background: rgba(255, 255, 255, 0.25);
  border-color: rgba(255, 255, 255, 0.5);
}

/* 统计卡片 */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
  margin-bottom: 32px;
}

.stat-card {
  background: white;
  border-radius: 12px;
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: transform 0.3s, box-shadow 0.3s;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
}

.stat-icon {
  font-size: 40px;
  line-height: 1;
}

.stat-info {
  flex: 1;
}

.stat-label {
  font-size: 14px;
  color: var(--text-secondary, #666);
  margin-bottom: 4px;
}

.stat-value {
  font-size: 28px;
  font-weight: 600;
  color: var(--text-primary, #1a1a1a);
}

/* 表单卡片 */
.form-card {
  background: linear-gradient(135deg, #eaedf1 0%, #2578de 100%);
  border-radius: 12px;
  padding: 32px;
  margin-bottom: 32px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.2);
}

.form-card h2 {
  font-size: 24px;
  margin: 0 0 24px 0;
  color: #ffffff;
  font-weight: 600;
}

.form-group {
  margin-bottom: 24px;
}

.form-group label {
  display: block;
  font-size: 14px;
  font-weight: 500;
  color: #ffffff;
  margin-bottom: 8px;
}

.required {
  color: #ff4d4f;
}

.form-input,
.form-textarea {
  width: 100%;
  padding: 12px;
  border: 1px solid rgba(255, 255, 255, 0.3);
  border-radius: 8px;
  font-size: 16px;
  transition: border-color 0.3s;
  box-sizing: border-box;
  background: rgba(255, 255, 255, 0.95);
  color: #1a1a1a;
}

.form-input::placeholder,
.form-textarea::placeholder {
  color: #999;
}

.form-input:focus,
.form-textarea:focus {
  outline: none;
  border-color: var(--primary-color, #1890ff);
}

.form-textarea {
  resize: vertical;
  font-family: inherit;
}

/* 表单行布局（用于并排显示字段） */
.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  margin-bottom: 24px;
}

.form-group-half {
  margin-bottom: 0;
}

@media (max-width: 768px) {
  .form-row {
    grid-template-columns: 1fr;
    gap: 0;
  }
  
  .form-group-half {
    margin-bottom: 24px;
  }
}

/* 下拉选择 */
.select-wrapper {
  position: relative;
}

.select-btn {
  width: 100%;
  padding: 12px;
  border: 1px solid rgba(255, 255, 255, 0.3);
  border-radius: 8px;
  background: rgba(44, 82, 130, 0.6);
  text-align: left;
  cursor: pointer;
  display: flex;
  justify-content: space-between;
  align-items: center;
  transition: all 0.3s;
  color: #ffffff;
}

.select-btn:hover {
  border-color: rgba(255, 255, 255, 0.6);
  background: rgba(44, 82, 130, 0.8);
}

.select-btn.has-value {
  color: #ffffff;
}

.arrow {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.8);
  transition: transform 0.3s;
}

.dropdown {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  background: rgba(30, 58, 95, 0.98);
  border: 1px solid rgba(255, 255, 255, 0.3);
  border-radius: 8px;
  margin-top: 4px;
  max-height: 300px;
  overflow: hidden;
  z-index: 1000;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
}

.dropdown-header {
  padding: 12px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.2);
}

.dropdown-header .search-input {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid rgba(255, 255, 255, 0.3);
  border-radius: 6px;
  font-size: 14px;
  background: rgba(255, 255, 255, 0.1);
  color: #ffffff;
  transition: all 0.3s;
}

.dropdown-header .search-input::placeholder {
  color: rgba(255, 255, 255, 0.6);
}

.dropdown-header .search-input:focus {
  outline: none;
  border-color: rgba(255, 255, 255, 0.6);
  background: rgba(255, 255, 255, 0.15);
}

.dropdown-list {
  max-height: 250px;
  overflow-y: auto;
}

.dropdown-item {
  padding: 12px;
  cursor: pointer;
  transition: background 0.2s;
}

.dropdown-item:hover {
  background: rgba(255, 255, 255, 0.15);
}

.dropdown-item.disabled {
  color: rgba(255, 255, 255, 0.6);
  opacity: 0.7;
  cursor: not-allowed;
  background: transparent;
}

.order-registered {
  margin-left: 8px;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.7);
}

.order-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.order-no {
  font-weight: 500;
  color: #ffffff;
}

.order-route {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.8);
}

.dropdown-empty {
  padding: 24px;
  text-align: center;
  color: rgba(255, 255, 255, 0.6);
}

/* 单选按钮组 */
.radio-group {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
}

.radio-label {
  display: flex;
  align-items: center;
  padding: 12px 20px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
  background: rgba(255, 255, 255, 0.1);
}

.radio-label:hover {
  border-color: rgba(255, 255, 255, 0.6);
  background: rgba(255, 255, 255, 0.15);
}

.radio-label.active {
  border-color: #ffffff;
  background: rgba(255, 255, 255, 0.25);
}

.radio-input {
  margin-right: 8px;
}

.radio-text {
  font-size: 16px;
  color: #ffffff;
}

.form-error {
  padding: 12px;
  background: rgba(255, 77, 79, 0.2);
  border: 1px solid rgba(255, 204, 199, 0.5);
  border-radius: 8px;
  color: #ffcccc;
  margin-bottom: 24px;
}

.form-actions {
  display: flex;
  gap: 16px;
  justify-content: flex-end;
  margin-top: 32px;
}

/* 筛选栏 */
.filter-bar {
  display: flex;
  gap: 16px;
  margin-bottom: 24px;
  flex-wrap: wrap;
}

.filter-group {
  display: flex;
  align-items: center;
  gap: 8px;
}

.filter-group label {
  font-size: 14px;
  color: var(--text-secondary, #666);
}

.filter-select {
  padding: 8px 12px;
  border: 1px solid #d9d9d9;
  border-radius: 8px;
  font-size: 14px;
  cursor: pointer;
}

.search-group {
  flex: 1;
  min-width: 200px;
}

.search-input {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #d9d9d9;
  border-radius: 8px;
  font-size: 14px;
}

/* 行李列表 */
.baggage-list {
  margin-bottom: 24px;
}

.loading,
.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: var(--text-secondary, #666);
}

.empty-icon {
  font-size: 64px;
  margin-bottom: 16px;
}

.empty-text {
  font-size: 16px;
}

.baggage-items {
  display: grid;
  gap: 16px;
}

.baggage-item {
  background: linear-gradient(135deg, rgba(233, 238, 246, 0.9) 0%, rgba(241, 243, 246, 0.9) 100%);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 12px;
  padding: 24px;
  transition: all 0.3s;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
}

.baggage-item:hover {
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.3);
  border-color: rgba(255, 255, 255, 0.3);
}

.baggage-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.baggage-no {
  font-size: 18px;
}

.baggage-no .label {
  color: rgba(255, 255, 255, 0.7);
  font-size: 14px;
}

.baggage-no .value {
  font-weight: 600;
  color: #ffffff;
  margin-left: 8px;
}

.baggage-status {
  padding: 6px 16px;
  border-radius: 16px;
  font-size: 13px;
  font-weight: 500;
  white-space: nowrap;
}

.status-registered {
  background: rgba(24, 144, 255, 0.3);
  color: #87ceeb;
  border: 1px solid rgba(24, 144, 255, 0.5);
}

.status-checked_in {
  background: rgba(82, 196, 26, 0.3);
  color: #90ee90;
  border: 1px solid rgba(82, 196, 26, 0.5);
}

.status-in_transit {
  background: rgba(250, 140, 22, 0.3);
  color: #ffb84d;
  border: 1px solid rgba(250, 140, 22, 0.5);
}

.status-arrived {
  background: rgba(82, 196, 26, 0.3);
  color: #90ee90;
  border: 1px solid rgba(82, 196, 26, 0.5);
}

.status-delivered {
  background: rgba(82, 196, 26, 0.3);
  color: #90ee90;
  border: 1px solid rgba(82, 196, 26, 0.5);
}

.baggage-body {
  margin-bottom: 12px;
}

.baggage-info {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(160px, 1fr));
  gap: 16px;
}

.info-item {
  font-size: 15px;
  padding: 8px 0;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.info-item .label {
  color: rgba(255, 255, 255, 0.7);
  font-size: 13px;
  display: block;
  margin-bottom: 4px;
}

.info-item .value {
  color: #ffffff;
  font-weight: 600;
  font-size: 16px;
}

.baggage-footer {
  padding-top: 16px;
  border-top: 1px solid rgba(255, 255, 255, 0.15);
  margin-top: 8px;
  display: flex;
  justify-content: flex-end;
}

.view-detail-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  background: rgba(255, 255, 255, 0.15);
  border: 1px solid rgba(255, 255, 255, 0.3);
  border-radius: 8px;
  color: #ffffff;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s;
  backdrop-filter: blur(10px);
}

.view-detail-btn:hover {
  background: rgba(255, 255, 255, 0.25);
  border-color: rgba(255, 255, 255, 0.5);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
}

.view-detail-btn:active {
  transform: translateY(0);
}

.btn-icon {
  font-size: 16px;
  line-height: 1;
}

.btn-text {
  font-weight: 500;
}

/* 分页 */
.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 16px;
  margin-top: 24px;
}

.page-btn {
  padding: 8px 16px;
  border: 1px solid #d9d9d9;
  border-radius: 8px;
  background: white;
  cursor: pointer;
  transition: all 0.3s;
}

.page-btn:hover:not(:disabled) {
  border-color: var(--primary-color, #1890ff);
  color: var(--primary-color, #1890ff);
}

.page-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.page-info {
  font-size: 14px;
  color: var(--text-secondary, #666);
}

/* 模态框 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 20px;
}

.modal-content {
  background: white;
  border-radius: 12px;
  max-width: 600px;
  width: 100%;
  max-height: 80vh;
  overflow-y: auto;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.2);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24px;
  border-bottom: 1px solid #f0f0f0;
}

.modal-header h2 {
  margin: 0;
  font-size: 24px;
  color: var(--text-primary, #1a1a1a);
}

.modal-close {
  background: none;
  border: none;
  font-size: 32px;
  color: #999;
  cursor: pointer;
  line-height: 1;
  padding: 0;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.modal-close:hover {
  color: var(--text-primary, #1a1a1a);
}

.modal-body {
  padding: 24px;
}

.detail-section {
  margin-bottom: 24px;
}

.detail-section:last-child {
  margin-bottom: 0;
}

.detail-section h3 {
  font-size: 18px;
  margin: 0 0 16px 0;
  color: var(--text-primary, #1a1a1a);
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
}

.detail-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.detail-label {
  font-size: 12px;
  color: var(--text-secondary, #666);
}

.detail-value {
  font-size: 16px;
  color: var(--text-primary, #1a1a1a);
  font-weight: 500;
}

.detail-text {
  font-size: 16px;
  color: var(--text-primary, #1a1a1a);
  line-height: 1.6;
  margin: 0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .baggage-management-page {
    padding: 16px;
  }

  .page-header {
    flex-direction: column;
    gap: 16px;
  }

  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .form-card {
    padding: 20px;
  }

  .filter-bar {
    flex-direction: column;
  }

  .baggage-info {
    grid-template-columns: 1fr;
  }
}
</style>

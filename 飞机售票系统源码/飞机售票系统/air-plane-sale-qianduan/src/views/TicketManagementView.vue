<template>
  <AdminLayout>
    <!-- 面包屑导航 -->
    <div class="breadcrumb">
      <span>首页</span>
      <span class="breadcrumb-separator">/</span>
      <span>机票退改签审核中心</span>
    </div>

    <!-- 搜索+统计区域 -->
    <section class="search-section">
      <div class="search-row">
        <div class="search-item">
          <label>申请号</label>
          <input 
            type="text" 
            v-model="searchParams.applicationNumber" 
            placeholder="请输入申请号"
          />
        </div>
        <div class="search-item">
          <label>客户/乘客</label>
          <input 
            type="text" 
            v-model="searchParams.customer" 
            placeholder="请输入客户或乘客姓名"
          />
        </div>
        <div class="search-item">
          <label>类型</label>
          <select v-model="searchParams.type">
            <option value="">全部类型</option>
            <option value="booking">改签审核</option>
            <option value="refund">退票审核</option>
          </select>
        </div>
        <div class="search-item">
          <label>状态</label>
          <select v-model="searchParams.status">
            <option value="">全部状态</option>
            <option value="pending">待审核</option>
            <option value="approved">已批准退票申请</option>
            <option value="rejected">已驳回</option>
          </select>
        </div>
        <div class="search-item">
          <button class="search-btn" @click="handleSearch">
            <span class="search-icon">🔍</span>
            查询
          </button>
          <button class="reset-btn" @click="handleReset">
            重置
          </button>
        </div>
      </div>

      <!-- 管理员总览 -->
      <div class="stats-row">
        <div class="stat-card">
          <div class="stat-icon stat-icon-change">🔁</div>
          <div class="stat-content">
            <p class="stat-label">待审核改签</p>
            <p class="stat-value">{{ bookingTotal }}</p>
            <p class="stat-desc">来自订票改签申请</p>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon stat-icon-refund">💸</div>
          <div class="stat-content">
            <p class="stat-label">待审核退票</p>
            <p class="stat-value">{{ filteredRefundReview.length }}</p>
            <p class="stat-desc">乘客退票/异常处理</p>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon stat-icon-fee">📊</div>
          <div class="stat-content">
            <p class="stat-label">改签手续费合计</p>
            <p class="stat-value">¥{{ totalChangeFee }}</p>
            <p class="stat-desc">按当前筛选条件统计</p>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon stat-icon-fee">🧾</div>
          <div class="stat-content">
            <p class="stat-label">退票手续费合计</p>
            <p class="stat-value">¥{{ totalRefundFee }}</p>
            <p class="stat-desc">按当前筛选条件统计</p>
          </div>
        </div>
        <div class="stat-actions">
          <div v-if="currentUserRole === 'admin'" class="filter-tabs-inline">
         
          </div>
          <button class="export-btn" @click="exportCurrentReviews">
            导出当前退改签列表
          </button>
          <button class="settings-btn" @click="showFeeSettingsModal = true">
            ⚙️ 手续费设置
          </button>
        </div>
      </div>
    </section>

    <!-- 手续费设置模态框 -->
    <div class="modal-overlay" v-if="showFeeSettingsModal" @click.self="showFeeSettingsModal = false">
      <div class="modal-content">
        <div class="modal-header">
          <h3>手续费百分比设置</h3>
          <button class="close-btn" @click="showFeeSettingsModal = false">×</button>
        </div>
        <div class="modal-body">
          <div class="fee-setting-item">
            <label class="fee-setting-label">
              <span>改签手续费百分比</span>
              <span class="fee-setting-desc">根据票价自动计算改签手续费</span>
            </label>
            <div class="fee-input-group">
              <input 
                type="number" 
                v-model.number="feeSettings.changeFeePercent" 
                min="0" 
                max="100" 
                step="0.1"
                class="fee-setting-input"
                placeholder="例如：5"
              />
              <span class="fee-unit">%</span>
            </div>
            <div class="fee-preview" v-if="feeSettings.changeFeePercent > 0">
              <span class="preview-label">示例：</span>
              <span class="preview-text">票价 ¥1000 × {{ feeSettings.changeFeePercent }}% = 手续费 ¥{{ (1000 * feeSettings.changeFeePercent / 100).toFixed(2) }}</span>
            </div>
          </div>
          <div class="fee-setting-item">
            <label class="fee-setting-label">
              <span>退票手续费百分比</span>
              <span class="fee-setting-desc">根据票价自动计算退票手续费</span>
            </label>
            <div class="fee-input-group">
              <input 
                type="number" 
                v-model.number="feeSettings.refundFeePercent" 
                min="0" 
                max="100" 
                step="0.1"
                class="fee-setting-input"
                placeholder="例如：10"
              />
              <span class="fee-unit">%</span>
            </div>
            <div class="fee-preview" v-if="feeSettings.refundFeePercent > 0">
              <span class="preview-label">示例：</span>
              <span class="preview-text">票价 ¥1000 × {{ feeSettings.refundFeePercent }}% = 手续费 ¥{{ (1000 * feeSettings.refundFeePercent / 100).toFixed(2) }}</span>
            </div>
          </div>
          <div class="fee-setting-note">
            <p>💡 提示：</p>
            <ul>
              <li>手续费将根据订单票价自动计算</li>
              <li>设置后立即生效，影响所有新的改签/退票申请</li>
              <li>已提交的申请不受影响</li>
            </ul>
          </div>
        </div>
        <div class="modal-footer">
          <button class="ghost-btn" @click="showFeeSettingsModal = false">取消</button>
          <button class="primary-btn" @click="saveFeeSettings">保存设置</button>
        </div>
      </div>
    </div>

    <!-- 模块切换器 -->
    <div class="module-switcher">
      <div class="switcher-tabs">
          <button 
          class="tab-btn" 
          :class="{ active: activeModule === 'change' }"
          @click="activeModule = 'change'"
        >
          <span class="tab-icon">✈️</span>
          <span>改签审核</span>
          <span class="tab-badge" v-if="bookingTotal > 0">
            {{ bookingTotal }}
          </span>
        </button>
        <button 
          class="tab-btn" 
          :class="{ active: activeModule === 'refund' }"
          @click="activeModule = 'refund'"
        >
          <span class="tab-icon">🔄</span>
          <span>退票审核</span>
          <span class="tab-badge" v-if="filteredRefundReview.length > 0">
            {{ filteredRefundReview.length }}
          </span>
        </button>
        <button 
          class="tab-btn" 
          :class="{ active: activeModule === 'all' }"
          @click="activeModule = 'all'"
        >
          <span class="tab-icon">📋</span>
          <span>全部</span>
        </button>
      </div>
    </div>

    <!-- 审核卡片区域（垂直堆叠） -->
    <div class="review-stack">
      <!-- 改签审核 -->
      <transition name="fade-slide">
        <div 
          v-if="activeModule === 'change' || activeModule === 'all'" 
          class="review-card"
          :key="'change'"
        >
        <div class="card-header">
          <div class="card-title">
            <span class="title-icon">✈️</span>
            <span>改签审核</span>
            <span class="badge-count">{{ bookingTotal }}</span>
          </div>
          <button class="btn-icon-only" @click="refreshBookingReview" title="刷新" :disabled="loadingBooking">
            {{ loadingBooking ? '⏳' : '🔄' }}
          </button>
        </div>
        <div class="table-container-small">
          <!-- 加载状态 -->
          <div v-if="loadingBooking" class="table-loading">
            <div class="loading-spinner-small"></div>
            <span>加载中...</span>
          </div>
          
          <!-- 空状态 -->
          <div v-else-if="visibleBookingReview.length === 0" class="table-empty">
            <div class="empty-icon">📋</div>
            <p>暂无改签申请</p>
          </div>
          
          <!-- 数据表格 -->
          <table v-else class="data-table-small">
            <thead>
            <tr>
              <th>申请号</th>
              <th>乘客</th>
              <th>航段</th>
              <th>费用</th>
              <th>原因</th>
              <th>操作</th>
            </tr>
            </thead>
            <tbody>
              <tr v-for="ticket in paginatedBooking" :key="ticket.id">
                <td>
                  <span class="app-number">{{ ticket.changeNo || ticket.id }}</span>
                </td>
              <td>{{ ticket.passenger }}</td>
                <td>
                <span class="route-text">{{ ticket.origin }} ⇀ {{ ticket.destination }}</span>
                </td>
                <td>
                <div class="fee-cell">
                  <span class="cabin-badge">{{ ticket.cabin }}</span>
                  <span class="fee-text">手续费 ¥{{ ticket.changeFee.toFixed(2) }}</span>
                  <span class="fee-text" :class="{ 'price-diff-positive': ticket.priceDiff > 0, 'price-diff-negative': ticket.priceDiff < 0 }">
                    差价 {{ ticket.priceDiff >= 0 ? '+' : '' }}¥{{ ticket.priceDiff.toFixed(2) }}
                  </span>
                </div>
                </td>
              <td class="reason-cell">{{ ticket.reason || '-' }}</td>
                <td>
                  <ReviewActions
                    :status="ticket.status"
                    @approve="handleApproveBooking(ticket)"
                    @reject="handleRejectBooking(ticket)"
                    @detail="handleBookingDetail(ticket)"
                  />
                </td>
              </tr>
            </tbody>
          </table>
          <div class="pagination" v-if="bookingTotal > 0" style="margin-top:12px;">
            <div class="pagination-info">共 {{ bookingTotal }} 条 / 每页
              <select v-model.number="bookingPageSize" @change="onBookingPageSizeChange" style="margin-left:8px;">
                <option :value="5">5</option>
                <option :value="10">10</option>
                <option :value="20">20</option>
                <option :value="50">50</option>
              </select>
            </div>
            <div class="pagination-controls">
              <button class="page-btn" :disabled="bookingCurrentPage === 1" @click="() => { bookingCurrentPage = Math.max(1, bookingCurrentPage - 1); loadBookingFromApi() }">&lt;</button>
              <span class="page-number">{{ bookingCurrentPage }} / {{ bookingTotalPages }}</span>
              <button class="page-btn" :disabled="bookingCurrentPage === bookingTotalPages" @click="() => { bookingCurrentPage = Math.min(bookingTotalPages, bookingCurrentPage + 1); loadBookingFromApi() }">&gt;</button>
            </div>
          </div>
        </div>
      </div>
      </transition>

      <!-- 退票审核 -->
      <transition name="fade-slide">
        <div 
          v-if="activeModule === 'refund' || activeModule === 'all'" 
          class="review-card refund-card"
          :key="'refund'"
        >
        <div class="card-header refund-header">
          <div class="card-title">
            <span class="title-icon refund-icon">💸</span>
            <span>退票审核</span>
            <span class="badge-count refund-badge">{{ filteredRefundReview.length }}</span>
          </div>
          <button class="btn-icon-only" @click="refreshRefundReview" title="刷新" :disabled="loadingRefund">
            {{ loadingRefund ? '⏳' : '🔄' }}
          </button>
        </div>
        <div class="table-container-small">
          <!-- 加载状态 -->
          <div v-if="loadingRefund" class="table-loading">
            <div class="loading-spinner-small"></div>
            <span>加载中...</span>
          </div>
          
          <!-- 空状态 -->
          <div v-else-if="visibleRefundReview.length === 0" class="table-empty">
            <div class="empty-icon">📋</div>
            <p>暂无退票申请</p>
          </div>
          
          <!-- 数据表格 -->
          <table v-else class="data-table-small refund-table">
            <thead>
              <tr>
                <th>申请号</th>
                <th>乘客</th>
                <th>航段</th>
                <th>航班号</th>
                <th>退款金额</th>
                <th>原因</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="refund in paginatedRefund" :key="refund.id" class="refund-row">
                <td>
                  <span class="app-number refund-app-number">#{{ refund.id }}</span>
                </td>
                <td>
                  <div class="passenger-cell">
                    <span class="passenger-avatar">{{ refund.passenger.charAt(0) }}</span>
                    <span class="passenger-name">{{ refund.passenger }}</span>
                  </div>
                </td>
                <td>
                  <div class="route-cell">
                    <span class="city-name">{{ refund.origin }}</span>
                    <span class="route-arrow">✈️</span>
                    <span class="city-name">{{ refund.destination }}</span>
                  </div>
                </td>
                <td>
                  <span class="flight-badge">{{ refund.flight }}</span>
                </td>
                <td>
                  <span class="refund-amount">¥{{ refund.refundFee.toFixed(2) }}</span>
                </td>
                <td class="reason-cell">
                  <span class="reason-text" :title="refund.reason">{{ refund.reason || '-' }}</span>
                </td>
                <td>
                  <div class="action-buttons-small">
                    <ReviewActions
                      :status="refund.status"
                      :forceShow="true"
                      @approve="handleApproveRefund(refund)"
                      @reject="handleRejectRefund(refund)"
                      @detail="handleRefundDetail(refund)"
                    />
                    <button
                      class="action-btn-small material-btn"
                      @click="handleRequestMaterial(refund)"
                      title="请求补充材料"
                    >
                      📎
                    </button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
          <div class="pagination" v-if="refundTotal > 0" style="margin-top:12px;">
            <div class="pagination-info">共 {{ refundTotal }} 条 / 每页
              <select v-model.number="refundPageSize" @change="onRefundPageSizeChange" style="margin-left:8px;">
                <option :value="5">5</option>
                <option :value="10">10</option>
                <option :value="20">20</option>
                <option :value="50">50</option>
              </select>
            </div>
            <div class="pagination-controls">
              <button class="page-btn" :disabled="refundCurrentPage === 1" @click="() => { refundCurrentPage = Math.max(1, refundCurrentPage - 1); loadRefundFromApi() }">&lt;</button>
              <span class="page-number">{{ refundCurrentPage }} / {{ refundTotalPages }}</span>
              <button class="page-btn" :disabled="refundCurrentPage === refundTotalPages" @click="() => { refundCurrentPage = Math.min(refundTotalPages, refundCurrentPage + 1); loadRefundFromApi() }">&gt;</button>
            </div>
          </div>
        </div>
      </div>
      </transition>
    </div>

    <!-- 确认弹窗 -->
    <ModalPrompt
      v-model="confirmModal.visible"
      :title="confirmModal.title"
      :message="confirmModal.message"
      :type="confirmModal.type"
      :show-cancel="confirmModal.type === 'confirm'"
      confirm-text="确定"
      cancel-text="取消"
      @confirm="confirmModal.onConfirm?.()"
      @cancel="confirmModal.onCancel?.() || (confirmModal.visible = false)"
    />

    <!-- 输入框弹窗 -->
    <ModalInput
      v-model="inputModal.visible"
      :title="inputModal.title"
      :message="inputModal.message"
      :placeholder="inputModal.placeholder"
      :multiline="inputModal.multiline"
      @confirm="inputModal.onConfirm?.($event)"
      @cancel="inputModal.onCancel?.() || (inputModal.visible = false)"
    />

    <!-- 详情模态框 -->
    <transition name="modal-fade">
      <div class="modal-overlay detail-modal-overlay" v-if="detailModal.visible" @click="closeDetailModal">
        <div class="modal-content detail-modal-content" @click.stop>
          <div class="modal-header detail-modal-header">
            <div class="header-left">
              <div class="modal-icon-wrapper">
                <span class="modal-icon-detail">📋</span>
              </div>
              <h3>{{ detailModal.title }}</h3>
            </div>
            <button class="close-btn" @click="closeDetailModal">×</button>
          </div>
          <div class="modal-body detail-modal-body" v-if="detailModal.data">
            <div class="detail-grid">
              <div class="detail-item" v-for="(value, key) in detailModal.data" :key="key">
                <div class="detail-label">{{ getLabel(key.toString()) }}</div>
                <div class="detail-value">{{ value }}</div>
              </div>
            </div>
          </div>
          <div class="modal-footer detail-modal-footer">
            <button class="modal-btn-close" @click="closeDetailModal">关闭</button>
          </div>
        </div>
      </div>
    </transition>
  </AdminLayout>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, watch } from 'vue'
import AdminLayout from '../components/AdminLayout.vue'
import ReviewActions from '../components/ReviewActions.vue'
import ModalPrompt from '../components/ModalPrompt.vue'
import ModalInput from '../components/ModalInput.vue'
import { adminTicketChangeReviewApi, ticketManagementApi, cancelRequestManagementApi, apiUtils } from '../services/api'

interface BookingReview {
  id: string
  changeNo?: string  // 改签编号
  orderNo: string
  passenger: string
  origin: string
  destination: string
  cabin: string
  priceDiff: number
  changeFee: number
  reason: string
  status?: string
  oldFlightNo?: string
  newFlightNo?: string
  oldDepartureTime?: string
  newDepartureTime?: string
}

interface RefundReview {
  id: string
  orderNo: string
  passenger: string
  flight: string
  origin: string
  destination: string
  refundFee: number
  reason: string
  status?: string
}

const searchParams = reactive({
  applicationNumber: '',
  customer: '',
  type: '',
  status: ''
})

// 当前激活的模块：'change' | 'refund' | 'all'
const activeModule = ref<'change' | 'refund' | 'all'>('change')

// 改签审核列表（从API加载）
const bookingReview = ref<BookingReview[]>([])

// 退票审核列表（暂时保留，后续可以对接退票API）
const refundReview = ref<RefundReview[]>([])

const loadingBooking = ref(false)
const loadingRefund = ref(false)

// Pagination for booking/refund lists (server-side)
const bookingCurrentPage = ref(1)
const bookingPageSize = ref(5)
const bookingTotal = ref(0)
const bookingTotalPages = computed(() => Math.max(1, Math.ceil(bookingTotal.value / bookingPageSize.value)))
const paginatedBooking = ref<BookingReview[]>([])

const refundCurrentPage = ref(1)
const refundPageSize = ref(5)
const refundTotal = ref(0)
const refundTotalPages = computed(() => Math.max(1, Math.ceil(refundTotal.value / refundPageSize.value)))
const paginatedRefund = ref<RefundReview[]>([])

// 统计数据
const stats = reactive({
  pendingChanges: 0,
  pendingRefunds: 0,
  totalChangeFee: 0,
  totalRefundFee: 0
})

// 统计字段（基于当前筛选后的数据）
const pendingChanges = computed(() => stats.pendingChanges)
const pendingRefunds = computed(() => stats.pendingRefunds)
const totalChangeFee = computed(() => stats.totalChangeFee)
const totalRefundFee = computed(() => stats.totalRefundFee)

const detailModal = reactive({
  visible: false,
  title: '',
  data: null as any
})

// 弹窗状态管理
const confirmModal = reactive({
  visible: false,
  title: '',
  message: '',
  type: 'confirm' as 'confirm' | 'success' | 'error' | 'info',
  onConfirm: null as (() => void) | null,
  onCancel: null as (() => void) | null
})

const inputModal = reactive({
  visible: false,
  title: '',
  message: '',
  placeholder: '',
  multiline: false,
  onConfirm: null as ((value: string) => void) | null,
  onCancel: null as (() => void) | null
})

// 显示确认弹窗的辅助函数
const showConfirmModal = (title: string, message: string, type: 'confirm' | 'success' | 'error' | 'info' = 'info') => {
  confirmModal.title = title
  confirmModal.message = message
  confirmModal.type = type
  confirmModal.onConfirm = () => {
    confirmModal.visible = false
  }
  confirmModal.visible = true
}

// 手续费设置
const showFeeSettingsModal = ref(false)
const feeSettings = reactive({
  changeFeePercent: 5, // 默认改签手续费 5%
  refundFeePercent: 10  // 默认退票手续费 10%
})

// 当前用户角色（用于显示管理员专用控件）
const currentUserRole = ref<string>('operator')

// 管理员页头紧凑筛选标签
const filterTabs = [
  { label: '全部', value: 'all' },
  { label: '待处理', value: 'pending' },
  { label: '已批准', value: 'approved' },
  { label: '已拒绝', value: 'rejected' }
]
const currentFilter = ref('all')

// 监听 currentFilter，将其映射到 searchParams.status 并触发重新加载
watch(currentFilter, async () => {
  if (currentFilter.value === 'all') {
    searchParams.status = ''
  } else {
    searchParams.status = currentFilter.value
  }
  // 触发两端列表刷新（不等待）
  loadBookingFromApi().catch(() => {})
  loadRefundFromApi().catch(() => {})
})

// 加载手续费设置
const loadFeeSettings = () => {
  try {
    const saved = localStorage.getItem('feeSettings')
    if (saved) {
      const parsed = JSON.parse(saved)
      feeSettings.changeFeePercent = parsed.changeFeePercent ?? 5
      feeSettings.refundFeePercent = parsed.refundFeePercent ?? 10
    }
  } catch (error) {
    console.warn('加载手续费设置失败:', error)
  }
}

// 保存手续费设置
const saveFeeSettings = () => {
  try {
    localStorage.setItem('feeSettings', JSON.stringify({
      changeFeePercent: feeSettings.changeFeePercent,
      refundFeePercent: feeSettings.refundFeePercent
    }))
    showFeeSettingsModal.value = false
    alert('手续费设置已保存！')
  } catch (error) {
    console.error('保存手续费设置失败:', error)
    alert('保存失败，请重试')
  }
}

const filteredBookingReview = computed(() => {
  let result = bookingReview.value

  if (searchParams.applicationNumber) {
    const keyword = searchParams.applicationNumber.toLowerCase()
    result = result.filter(b => 
      (b.changeNo && b.changeNo.toLowerCase().includes(keyword)) ||
      b.id.toLowerCase().includes(keyword) ||
      b.orderNo.toLowerCase().includes(keyword)
    )
  }
  if (searchParams.customer) {
    const keyword = searchParams.customer.toLowerCase()
    result = result.filter(b => b.passenger.toLowerCase().includes(keyword))
  }
  if (searchParams.type && searchParams.type !== 'booking') {
    return []
  }
  if (searchParams.status) {
    result = result.filter(b => b.status === searchParams.status)
  }

  return result
})

const filteredRefundReview = computed(() => {
  let result = refundReview.value

  if (searchParams.applicationNumber) {
    const keyword = searchParams.applicationNumber.toLowerCase()
    result = result.filter(r => 
      r.id.toLowerCase().includes(keyword) ||
      r.orderNo.toLowerCase().includes(keyword)
    )
  }
  if (searchParams.customer) {
    const keyword = searchParams.customer.toLowerCase()
    result = result.filter(r => r.passenger.toLowerCase().includes(keyword))
  }
  if (searchParams.type && searchParams.type !== 'refund') {
    return []
  }
  if (searchParams.status) {
    result = result.filter(r => r.status === searchParams.status)
  }

  return result
})

// 根据激活模块过滤显示
const visibleBookingReview = computed(() => {
  if (activeModule.value === 'change' || activeModule.value === 'all') {
    return filteredBookingReview.value
  }
  return []
})

// 根据激活模块过滤显示
const visibleRefundReview = computed(() => {
  if (activeModule.value === 'refund' || activeModule.value === 'all') {
    return filteredRefundReview.value
  }
  return []
})

// 格式化日期时间
const formatDateTime = (dateTime: string | Date | null | undefined) => {
  if (!dateTime) return ''
  
  if (typeof dateTime === 'string') {
    let dateStr = dateTime.trim()
    if (dateStr.endsWith('Z')) {
      dateStr = dateStr.slice(0, -1)
    }
    const timezoneMatch = dateStr.match(/([+-]\d{2}:\d{2})$/);
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
  }
  
  if (dateTime instanceof Date) {
    const year = dateTime.getFullYear()
    const month = String(dateTime.getMonth() + 1).padStart(2, '0')
    const day = String(dateTime.getDate()).padStart(2, '0')
    const hours = String(dateTime.getHours()).padStart(2, '0')
    const minutes = String(dateTime.getMinutes()).padStart(2, '0')
    return `${year}-${month}-${day} ${hours}:${minutes}`
  }
  
  return ''
}

// 从航线字符串中提取出发地和目的地
const parseRoute = (route: string) => {
  if (!route) return { origin: '', destination: '' }
  const parts = route.split('→').map(s => s.trim())
  return {
    origin: parts[0] || '',
    destination: parts[1] || ''
  }
}

// 将后端状态统一为前端使用的 'pending' | 'approved' | 'rejected'
const normalizeStatus = (rawStatus: any): string => {
  if (rawStatus === undefined || rawStatus === null) return 'pending'
  const s = String(rawStatus).trim()
  if (!s) return 'pending'
  const lower = s.toLowerCase()

  // 常见中文/英文映射（包含可能的变体）
  if (['pending', '待处理', '待审核', '待受理', '未处理'].some(v => lower === v || lower.includes(v))) return 'pending'
  if (['approved', '通过', '已批准', '同意'].some(v => lower === v || lower.includes(v))) return 'approved'
  if (['rejected', '拒绝', '已拒绝', '驳回'].some(v => lower === v || lower.includes(v))) return 'rejected'

  // 如果本身就是前端期望值，返回之
  if (['pending', 'approved', 'rejected'].includes(lower)) return lower

  // 兜底返回小写原值，组件仍可根据需要判断
  return lower
}

const normalizeBookingList = (raw: any): BookingReview[] => {
  if (!raw) return []
  let list: any[] = []
  if (Array.isArray(raw)) list = raw
  else if (raw.data && Array.isArray(raw.data.list)) list = raw.data.list
  else if (Array.isArray(raw.data)) list = raw.data
  else if (Array.isArray(raw?.data?.content)) list = raw.data.content
  else if (Array.isArray(raw?.content)) list = raw.content

  return list.map((item) => {
    // 解析航线
    const oldRoute = parseRoute(item.oldRoute || '')
    const newRoute = parseRoute(item.newRoute || '')
    
    // 统一状态到前端枚举
    const frontendStatus = normalizeStatus(item.status)
    
    return {
      id: String(item.id || ''),
      changeNo: item.changeNo || String(item.id || ''), // 改签编号
      orderNo: item.orderno || item.orderNo || item.orderNumber || '',
      passenger: item.applicantName || item.passenger || item.passengerName || '',
      origin: oldRoute.origin || item.origin || item.fromCity || '',
      destination: oldRoute.destination || item.destination || item.toCity || '',
      cabin: '经济舱', // 暂时使用默认值，后续可以从订单获取
      priceDiff: item.fareDiff ? parseFloat(String(item.fareDiff)) : 0,
      changeFee: item.changeFee ? parseFloat(String(item.changeFee)) : 0,
      reason: item.reason || item.remark || '',
      status: frontendStatus,
      oldFlightNo: item.oldFlightNo || '',
      newFlightNo: item.newFlightNo || '',
      oldDepartureTime: formatDateTime(item.oldDepartureTime),
      newDepartureTime: formatDateTime(item.newDepartureTime)
    }
  })
}

const normalizeRefundList = (raw: any): RefundReview[] => {
  if (!raw) return []
  let list: any[] = []
  
  // 处理后端返回格式：{ success: true, data: { list: [...], total: ... } }
  if (raw && raw.success && raw.data) {
    if (Array.isArray(raw.data.list)) {
      list = raw.data.list
    } else if (Array.isArray(raw.data)) {
      list = raw.data
    }
  } else if (Array.isArray(raw)) {
    list = raw
  } else if (Array.isArray(raw?.data)) {
    list = raw.data
  } else if (Array.isArray(raw?.data?.content)) {
    list = raw.data.content
  } else if (Array.isArray(raw?.content)) {
    list = raw.content
  }

  return list.map((item) => {
    // 解析航线（格式：北京 → 上海）
    const route = item.route || ''
    const routeParts = route.split('→').map((s: string) => s.trim())
    const origin = routeParts[0] || ''
    const destination = routeParts[1] || ''
    
    // 统一状态到前端枚举
    const frontendStatus = normalizeStatus(item.status)
    
    return {
      id: String(item.id || item.applicationId || ''),
      orderNo: item.orderno || item.orderNo || item.orderNumber || '',
      passenger: item.applicantName || item.passenger || item.passengerName || item.customer || '',
      flight: item.flightNo || item.flight || item.flightNumber || '',
      origin: origin || item.origin || item.fromCity || item.departure || '',
      destination: destination || item.destination || item.toCity || item.arrival || '',
      refundFee: Number(item.cancelFee ?? item.refundFee ?? item.fee ?? 0), // 退票手续费使用cancelFee
      reason: item.reason || item.remark || '',
      status: frontendStatus
    }
  })
}

const loadBookingFromApi = async () => {
  loadingBooking.value = true
  try {
    // 构建查询参数
    const params: any = {
      page: bookingCurrentPage.value - 1,
      size: bookingPageSize.value
    }
    
    // 申请号搜索
    if (searchParams.applicationNumber) {
      params.changeNo = searchParams.applicationNumber
    }
    
    // 客户/乘客搜索
    if (searchParams.customer) {
      params.applicantName = searchParams.customer
    }
    
    // 状态筛选（默认显示待处理的）
    // 状态筛选：仅当用户显式选择状态时才传递 status 参数（'all' 会使 searchParams.status 为空，从而不限制后端）
    if (searchParams.status) {
      const statusMap: Record<string, string> = {
        'pending': '待处理',
        'approved': '通过',
        'rejected': '拒绝'
      }
      params.status = statusMap[searchParams.status] || searchParams.status
    }
    
    const res: any = await adminTicketChangeReviewApi.getChangeReviewList(params)
    const list = normalizeBookingList(res)
    bookingReview.value = list
    // try to extract pagination meta
    const data = res?.data ?? res
    bookingTotal.value = data?.total ?? data?.totalElements ?? list.length
    // update current page/size if backend returned them
    if (data?.page !== undefined) {
      bookingCurrentPage.value = Number(data.page) + 1
    }
    if (data?.size !== undefined) {
      bookingPageSize.value = Number(data.size)
    }
    // set paginatedBooking to list (assuming backend returns page content)
    paginatedBooking.value = Array.isArray(list) ? list : []
    
    // 更新统计数据
    await loadStatistics()
  } catch (error) {
    console.warn('获取改签审核列表失败:', error)
    bookingReview.value = []
  } finally {
    loadingBooking.value = false
  }
}

// 加载统计数据
const loadStatistics = async () => {
  try {
    // 请求后端统计接口（不传 status 意味着请求全部统计数据）
    const res: any = await adminTicketChangeReviewApi.getStatistics()
    if (res && res.success && res.data) {
      stats.pendingChanges = res.data.pendingCount || 0
      stats.totalChangeFee = res.data.totalChangeFee || 0
    } else {
      // 如果API失败，使用前端计算
      stats.pendingChanges = bookingReview.value.filter(b => b.status === 'pending').length
      // 前端回退时：改签手续费合计使用所有改签记录的手续费之和（而非仅待处理）
      stats.totalChangeFee = bookingReview.value
        .reduce((sum, b) => sum + (b.changeFee || 0), 0)
    }
  } catch (error) {
    console.warn('获取统计数据失败:', error)
    // 使用前端计算
    stats.pendingChanges = bookingReview.value.filter(b => b.status === 'pending').length
    stats.totalChangeFee = bookingReview.value
      .reduce((sum, b) => sum + (b.changeFee || 0), 0)
  }
  
  // 退票统计数据（从后端API获取）
  try {
    const refundStatsRes: any = await ticketManagementApi.getRefundReviewList({ 
      page: 0, 
      size: 1 
    })
    // 尝试从统计接口获取（如果后端提供了统计接口）
    // 否则使用前端计算
    if (refundStatsRes && refundStatsRes.success && refundStatsRes.data) {
      // 如果后端返回了统计数据，使用后端数据
      // 这里暂时使用前端计算，后续可以调用专门的统计接口
    }
  } catch (error) {
    // 忽略统计接口错误
  }
  
  // 使用前端计算退票统计数据
  stats.pendingRefunds = refundReview.value.filter(r => r.status === 'pending').length
  // 退票手续费合计使用所有退票记录的手续费之和（而非仅待处理）
  stats.totalRefundFee = refundReview.value
    .reduce((sum, r) => sum + (r.refundFee || 0), 0)
}

const loadRefundFromApi = async () => {
  loadingRefund.value = true
    try {
    // 构建查询参数（使用 cancelRequestManagementApi 以对齐航空运营的退票请求表）
    const params: any = {
      page: refundCurrentPage.value - 1,
      size: refundPageSize.value
    }

    // 状态筛选（仅当用户主动选择时才筛选，默认显示所有）
    if (searchParams.status) {
      const statusMap: Record<string, string> = {
        'pending': '待处理',
        'approved': '通过',
        'rejected': '不通过' // 后端退票拒绝状态在取消表中为“不通过”
      }
      params.status = statusMap[searchParams.status] || searchParams.status
    }
    // 不设置默认状态筛选，显示所有退票申请

    const res: any = await cancelRequestManagementApi.getCancelRequestList(params)
    const list = normalizeRefundList(res)
    refundReview.value = list
    const data = res?.data ?? res
    refundTotal.value = data?.total ?? data?.totalElements ?? list.length
    if (data?.page !== undefined) {
      refundCurrentPage.value = Number(data.page) + 1
    }
    if (data?.size !== undefined) {
      refundPageSize.value = Number(data.size)
    }
    paginatedRefund.value = Array.isArray(list) ? list : []
    
    // 更新统计数据
    await loadStatistics()
  } catch (error) {
    console.warn('获取退票审核列表失败:', error)
    refundReview.value = []
  } finally {
    loadingRefund.value = false
  }
}

// pagination helpers used by template
const onBookingPageSizeChange = () => {
  bookingCurrentPage.value = 1
  loadBookingFromApi()
}

const onRefundPageSizeChange = () => {
  refundCurrentPage.value = 1
  loadRefundFromApi()
}

// 导出当前筛选后的退改签列表
const exportCurrentReviews = () => {
  const headers = [
    '类型',
    '申请号',
    '订单号',
    '乘客',
    '航线/航班',
    '手续费',
    '差价/金额',
    '原因',
    '状态'
  ]

  const bookingRows = filteredBookingReview.value.map(b => [
    '改签',
    b.id,
    b.orderNo,
    b.passenger,
    `${b.origin} ⇀ ${b.destination}`,
    b.changeFee,
    b.priceDiff,
    b.reason,
    b.status || ''
  ])

  const refundRows = filteredRefundReview.value.map(r => [
    '退票',
    r.id,
    r.orderNo,
    r.passenger,
    `${r.origin} ⇀ ${r.destination} (${r.flight})`,
    r.refundFee,
    '',
    r.reason,
    r.status || ''
  ])

  const rows = [...bookingRows, ...refundRows]
  if (!rows.length) {
    alert('当前筛选条件下没有可导出的退改签记录')
    return
  }

  const csvContent = [headers.join(','), ...rows.map(row => row.join(','))].join('\n')
  const blob = new Blob(['\ufeff' + csvContent], { type: 'text/csv;charset=utf-8;' })
  const link = document.createElement('a')
  link.href = URL.createObjectURL(blob)
  link.download = `退改签审核导出_${new Date().toISOString().split('T')[0]}.csv`
  link.click()
}

const handleSearch = async () => {
  // 搜索时根据筛选条件重新从后端获取数据
  await loadBookingFromApi()
  loadRefundFromApi()
}

const handleReset = async () => {
  searchParams.applicationNumber = ''
  searchParams.customer = ''
  searchParams.type = ''
  searchParams.status = ''
  await loadBookingFromApi()
  loadRefundFromApi()
}

const handleApproveBooking = async (ticket: BookingReview) => {
  confirmModal.title = '确认批准'
  confirmModal.message = `确定要通过改签申请 ${ticket.changeNo || ticket.id} 吗？`
  confirmModal.type = 'confirm'
  confirmModal.onConfirm = async () => {
    try {
      const currentUser = apiUtils.getCurrentUser()
      const processedBy = currentUser?.id ? (typeof currentUser.id === 'string' ? parseInt(currentUser.id, 10) : currentUser.id) : undefined
      
      const appId = typeof ticket.id === 'string' ? parseInt(ticket.id, 10) : ticket.id
      if (isNaN(appId)) {
        showConfirmModal('错误', '无效的申请ID', 'error')
        return
      }
      
      await adminTicketChangeReviewApi.approveChangeRequest(appId, processedBy)
      showConfirmModal('成功', `已通过改签申请 ${ticket.changeNo || ticket.id}，新订单已创建`, 'success')
      
      // 重新加载数据
      await loadBookingFromApi()
    } catch (error: any) {
      console.error('批准改签申请失败:', error)
      showConfirmModal('错误', error?.message || '批准改签申请失败，请稍后重试', 'error')
    }
  }
  confirmModal.visible = true
}

const handleRejectBooking = async (ticket: BookingReview) => {
  inputModal.title = '驳回改签申请'
  inputModal.message = `请输入驳回原因（申请号：${ticket.changeNo || ticket.id}）`
  inputModal.placeholder = '请输入驳回原因...'
  inputModal.multiline = true
  inputModal.onConfirm = async (reason: string) => {
    if (!reason || !reason.trim()) {
      showConfirmModal('提示', '驳回原因不能为空', 'error')
      return
    }
    
    try {
      const currentUser = apiUtils.getCurrentUser()
      const processedBy = currentUser?.id ? (typeof currentUser.id === 'string' ? parseInt(currentUser.id, 10) : currentUser.id) : undefined
      
      const appId = typeof ticket.id === 'string' ? parseInt(ticket.id, 10) : ticket.id
      if (isNaN(appId)) {
        showConfirmModal('错误', '无效的申请ID', 'error')
        return
      }
      
      await adminTicketChangeReviewApi.rejectChangeRequest(appId, reason.trim(), processedBy)
      showConfirmModal('成功', `已驳回改签申请 ${ticket.changeNo || ticket.id}，原因：${reason}`, 'success')
      
      // 重新加载数据
      await loadBookingFromApi()
    } catch (error: any) {
      console.error('驳回改签申请失败:', error)
      showConfirmModal('错误', error?.message || '驳回改签申请失败，请稍后重试', 'error')
    }
  }
  inputModal.visible = true
}

const handleApproveRefund = async (refund: RefundReview) => {
  confirmModal.title = '确认批准'
  confirmModal.message = `确定要批准退票申请 ${refund.id} 吗？`
  confirmModal.type = 'confirm'
  confirmModal.onConfirm = async () => {
    try {
      // 获取当前用户ID（处理人）- 系统管理员ID
      const currentUser = apiUtils.getCurrentUser()
      const processedBy = currentUser?.id ? (typeof currentUser.id === 'string' ? parseInt(currentUser.id, 10) : currentUser.id) : undefined
      
      if (!processedBy) {
        showConfirmModal('错误', '无法获取当前用户信息，请重新登录', 'error')
        return
      }
      
      // 调用后端API批准退票申请（使用取消请求管理 API 与航空运营逻辑保持一致），传递处理人ID
      await cancelRequestManagementApi.approveCancelRequest(refund.id, processedBy)
      
      showConfirmModal('成功', `已批准退票申请 ${refund.id}`, 'success')
      
      // 重新加载数据
      await loadRefundFromApi()
    } catch (error: any) {
      console.error('批准退票申请失败:', error)
      showConfirmModal('错误', error?.message || '批准退票申请失败，请稍后重试', 'error')
    }
  }
  confirmModal.visible = true
}

const handleRejectRefund = async (refund: RefundReview) => {
  inputModal.title = '拒绝退票申请'
  inputModal.message = `请输入拒绝原因（申请号：${refund.id}）`
  inputModal.placeholder = '请输入拒绝原因...'
  inputModal.multiline = true
  inputModal.onConfirm = async (reason: string) => {
    if (!reason || !reason.trim()) {
      showConfirmModal('提示', '拒绝原因不能为空', 'error')
      return
    }
    
    try {
      // 获取当前用户ID（处理人）- 系统管理员ID
      const currentUser = apiUtils.getCurrentUser()
      const processedBy = currentUser?.id ? (typeof currentUser.id === 'string' ? parseInt(currentUser.id, 10) : currentUser.id) : undefined
      
      if (!processedBy) {
        showConfirmModal('错误', '无法获取当前用户信息，请重新登录', 'error')
        return
      }
      
      await cancelRequestManagementApi.rejectCancelRequest(refund.id, reason.trim(), processedBy)
      showConfirmModal('成功', `已拒绝退票申请 ${refund.id}`, 'success')
      
      await loadRefundFromApi()
    } catch (error: any) {
      console.error('拒绝退票申请失败:', error)
      showConfirmModal('错误', error?.message || '拒绝退票申请失败，请稍后重试', 'error')
    }
  }
  inputModal.visible = true
}

const handleRequestMaterial = (refund: RefundReview) => {
  inputModal.title = '请求补充材料'
  inputModal.message = `请输入需要补充的材料说明（申请号：${refund.id}）`
  inputModal.placeholder = '请输入需要补充的材料说明...'
  inputModal.multiline = true
  inputModal.onConfirm = (message: string) => {
    if (!message || !message.trim()) {
      showConfirmModal('提示', '补充材料说明不能为空', 'error')
      return
    }
    
    ticketManagementApi.requestMaterial(refund.id, message.trim())
      .then(() => {
        showConfirmModal('成功', `已向申请 ${refund.id} 发送补充材料通知`, 'success')
        console.log(`请求补充材料: ${refund.id}, 说明: ${message}`)
      })
      .catch((error: any) => {
        console.error('请求补充材料失败:', error)
        showConfirmModal('错误', error?.message || '请求补充材料失败，请稍后重试', 'error')
      })
  }
  inputModal.visible = true
}

const handleBookingDetail = (ticket: BookingReview) => {
  detailModal.title = `改签审核详情 - ${ticket.changeNo || ticket.id}`
  
  // 解析新航线（如果有新航班信息）
  let newRouteText = '-'
  if (ticket.newFlightNo) {
    // 尝试从新航班号推断航线，或者使用原航线
    newRouteText = `${ticket.origin} ⇀ ${ticket.destination}`
  }
  
  const detailData: any = {
    '申请号': ticket.changeNo || ticket.id,
    '订单号': ticket.orderNo,
    '乘客': ticket.passenger,
    '原航线': `${ticket.origin} ⇀ ${ticket.destination}`,
    '原航班号': ticket.oldFlightNo || '-',
    '原起飞时间': ticket.oldDepartureTime || '-',
    '新航线': newRouteText,
    '新航班号': ticket.newFlightNo || '-',
    '新起飞时间': ticket.newDepartureTime || '-',
    '改签手续费': `¥${ticket.changeFee.toFixed(2)}`,
    '票价差': `${ticket.priceDiff >= 0 ? '+' : ''}¥${ticket.priceDiff.toFixed(2)}`,
    '申请原因': ticket.reason || '-'
  }
  detailModal.data = detailData
  detailModal.visible = true
}

const handleRefundDetail = (refund: RefundReview) => {
  detailModal.title = `退票审核详情 - ${refund.id}`
  detailModal.data = {
    '申请号': refund.id,
    '订单号': refund.orderNo,
    '乘客': refund.passenger,
    '航班号': refund.flight,
    '航线': `${refund.origin} ⇀ ${refund.destination}`,
    '退款手续费': `¥${refund.refundFee}`,
    '原因': refund.reason
  }
  detailModal.visible = true
}

const closeDetailModal = () => {
  detailModal.visible = false
  detailModal.data = null
}

const refreshBookingReview = async () => {
  await loadBookingFromApi()
}

const refreshRefundReview = () => {
  loadRefundFromApi()
}

const getLabel = (key: string) => {
  const labelMap: Record<string, string> = {
    '申请号': '申请号',
    '订单号': '订单号',
    '乘客': '乘客',
    '航线': '航线',
    '航班号': '航班号',
    '票种': '票种',
    '改签手续费': '改签手续费',
    '票价差': '票价差',
    '退款手续费': '退款手续费',
    '原因': '原因'
  }
  return labelMap[key] || key
}

onMounted(async () => {
  loadFeeSettings()
  await loadBookingFromApi()
  loadRefundFromApi()
  // 设置当前用户角色，决定是否展示管理员专用控件
  try {
    const user = apiUtils.getCurrentUser()
    currentUserRole.value = user?.role || 'operator'
    // 管理员默认显示全部，其他角色默认显示待处理
    if (currentUserRole.value === 'admin') {
      currentFilter.value = 'all'
    } else {
      currentFilter.value = 'pending'
    }
  } catch (e) {
    // ignore
  }
})
</script>

<style scoped>
/* 基础样式内联 */
.breadcrumb {
  margin-bottom: 20px;
  font-size: 14px;
  color: rgba(255, 255, 255, 0.7);
}

.breadcrumb-separator {
  margin: 0 8px;
  color: rgba(255, 255, 255, 0.4);
}

.search-section {
  background: rgba(2, 6, 23, 0.7);
  padding: 20px 20px 16px;
  border-radius: 16px;
  margin-bottom: 20px;
  border: 1px solid rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(18px);
}

.search-row {
  display: flex;
  align-items: flex-end;
  gap: 15px;
  flex-wrap: wrap;
}

.stats-row {
  margin-top: 16px;
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  align-items: stretch;
}

.stat-card {
  flex: 1 1 180px;
  min-width: 160px;
  display: flex;
  padding: 10px 12px;
  border-radius: 12px;
  background: radial-gradient(circle at top left, rgba(56, 189, 248, 0.18), rgba(15, 23, 42, 0.95));
  border: 1px solid rgba(148, 163, 184, 0.5);
  box-shadow: 0 10px 25px rgba(15, 23, 42, 0.7);
}

.stat-icon {
  width: 34px;
  height: 34px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 10px;
  font-size: 18px;
}

.stat-icon-change {
  background: rgba(59, 130, 246, 0.15);
}

.stat-icon-refund {
  background: rgba(248, 113, 113, 0.15);
}

.stat-icon-fee {
  background: rgba(251, 191, 36, 0.15);
}

.stat-content {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.stat-label {
  font-size: 13px;
  color: rgba(226, 232, 240, 0.85);
}

.stat-value {
  font-size: 20px;
  font-weight: 700;
  color: #f9fafb;
}

.stat-desc {
  font-size: 12px;
  color: rgba(148, 163, 184, 0.9);
}

.stat-actions {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  flex: 0 0 220px;
}

.export-btn,
.settings-btn {
  padding: 8px 16px;
  border-radius: 999px;
  border: 1px solid rgba(96, 165, 250, 0.8);
  background: radial-gradient(circle at top left, rgba(59, 130, 246, 0.35), rgba(15, 23, 42, 0.98));
  color: #e5f2ff;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  box-shadow: 0 8px 18px rgba(37, 99, 235, 0.45);
  transition: all 0.2s ease;
  margin-left: 12px;
}

.settings-btn {
  border-color: rgba(139, 92, 246, 0.8);
  background: radial-gradient(circle at top left, rgba(139, 92, 246, 0.35), rgba(15, 23, 42, 0.98));
  box-shadow: 0 8px 18px rgba(139, 92, 246, 0.45);
}

.export-btn:hover,
.settings-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 12px 26px rgba(37, 99, 235, 0.55);
}

.settings-btn:hover {
  box-shadow: 0 12px 26px rgba(139, 92, 246, 0.55);
}

.search-item {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.search-item label {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.7);
}

.search-item input,
.search-item select {
  padding: 8px 12px;
  border: 1px solid rgba(255, 255, 255, 0.15);
  border-radius: 8px;
  font-size: 14px;
  background: rgba(15, 23, 42, 0.6);
  color: #fff;
}

.action-buttons {
  margin-bottom: 15px;
  display: flex;
  gap: 10px;
}

.btn-primary,
.btn-secondary {
  padding: 8px 16px;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s;
}

.btn-primary {
  background: linear-gradient(135deg, #1E8AE6, #0A2F63);
  color: #fff;
}

.btn-primary:hover {
  transform: translateY(-2px);
}

.btn-secondary {
  background: rgba(15, 23, 42, 0.6);
  color: rgba(255, 255, 255, 0.8);
  border: 1px solid rgba(255, 255, 255, 0.15);
}

.table-container {
  background: rgba(2, 6, 23, 0.7);
  border-radius: 16px;
  overflow: hidden;
  border: 1px solid rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(18px);
  overflow-x: auto;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
}

.data-table th,
.data-table td {
  padding: 12px 8px;
  text-align: left;
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
  color: rgba(255, 255, 255, 0.8);
}

.data-table thead {
  background: rgba(99, 102, 241, 0.2);
}

.data-table th {
  font-weight: 600;
  color: rgba(255, 255, 255, 0.9);
}

.data-table tbody tr:hover {
  background: rgba(99, 102, 241, 0.1);
}

.status-tag {
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
  display: inline-block;
}

.status-tag.pending {
  background: rgba(251, 191, 36, 0.2);
  color: #fcd34d;
  border: 1px solid rgba(251, 191, 36, 0.3);
}

.status-tag.approved {
  background: rgba(34, 197, 94, 0.2);
  color: #4ade80;
  border: 1px solid rgba(34, 197, 94, 0.3);
}

.status-tag.rejected {
  background: rgba(248, 113, 113, 0.2);
  color: #f87171;
  border: 1px solid rgba(248, 113, 113, 0.3);
}

.action-btn {
  padding: 4px 8px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 12px;
  margin-right: 5px;
  transition: all 0.3s;
}

.edit-btn {
  background: rgba(76, 175, 80, 0.2);
  color: #81c784;
  border: 1px solid rgba(76, 175, 80, 0.3);
}

.edit-btn:hover {
  background: rgba(76, 175, 80, 0.3);
}

.delete-btn {
  background: rgba(244, 67, 54, 0.2);
  color: #ef5350;
  border: 1px solid rgba(244, 67, 54, 0.3);
}

.delete-btn:hover {
  background: rgba(244, 67, 54, 0.3);
}

.approve-btn {
  background: rgba(76, 175, 80, 0.2);
  color: #81c784;
  border: 1px solid rgba(76, 175, 80, 0.3);
}

.reject-btn {
  background: rgba(244, 67, 54, 0.2);
  color: #ef5350;
  border: 1px solid rgba(244, 67, 54, 0.3);
}

.pagination {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 20px;
}

.pagination-info {
  color: rgba(255, 255, 255, 0.7);
}

.pagination-controls {
  display: flex;
  gap: 10px;
}

.page-btn {
  padding: 6px 12px;
  border: 1px solid rgba(255, 255, 255, 0.15);
  background: rgba(15, 23, 42, 0.6);
  border-radius: 6px;
  cursor: pointer;
  color: rgba(255, 255, 255, 0.8);
}

.page-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.page-number {
  padding: 6px 12px;
  color: rgba(255, 255, 255, 0.8);
}

/* 详情弹窗样式 */
.modal-fade-enter-active,
.modal-fade-leave-active {
  transition: opacity 0.3s ease;
}

.modal-fade-enter-from,
.modal-fade-leave-to {
  opacity: 0;
}

.modal-fade-enter-active .detail-modal-content,
.modal-fade-leave-active .detail-modal-content {
  transition: transform 0.3s ease, opacity 0.3s ease;
}

.modal-fade-enter-from .detail-modal-content,
.modal-fade-leave-to .detail-modal-content {
  opacity: 0;
  transform: scale(0.95) translateY(-20px);
}

.detail-modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(6, 11, 40, 0.55);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 10000;
  padding: 20px;
  box-sizing: border-box;
  overflow-y: auto;
  overscroll-behavior: contain;
}

.detail-modal-content {
  background: rgba(15, 23, 42, 0.95);
  border-radius: 20px;
  width: 90%;
  max-width: 600px;
  max-height: 85vh;
  overflow: hidden;
  border: 1px solid rgba(255, 255, 255, 0.1);
  box-shadow:
    0 25px 50px rgba(15, 23, 42, 0.5),
    inset 0 1px rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(20px);
  display: flex;
  flex-direction: column;
  animation: modal-pop 0.3s ease-out;
}

@keyframes modal-pop {
  0% {
    opacity: 0;
    transform: scale(0.95) translateY(-10px);
  }
  100% {
    opacity: 1;
    transform: scale(1) translateY(0);
  }
}

.detail-modal-header {
  padding: 24px 28px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: linear-gradient(135deg, rgba(99, 102, 241, 0.1) 0%, rgba(139, 92, 246, 0.1) 100%);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.modal-icon-wrapper {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  background: linear-gradient(135deg, rgba(99, 102, 241, 0.2) 0%, rgba(139, 92, 246, 0.2) 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid rgba(99, 102, 241, 0.3);
}

.modal-icon-detail {
  font-size: 24px;
}

.detail-modal-header h3 {
  margin: 0;
  color: #fff;
  font-size: 20px;
  font-weight: 600;
  letter-spacing: 0.3px;
}

.close-btn {
  background: none;
  border: none;
  font-size: 28px;
  color: rgba(255, 255, 255, 0.5);
  cursor: pointer;
  padding: 0;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  transition: all 0.2s ease;
  line-height: 1;
}

.close-btn:hover {
  background: rgba(255, 255, 255, 0.1);
  color: rgba(255, 255, 255, 0.8);
}

.detail-modal-body {
  padding: 28px;
  overflow-y: auto;
  flex: 1;
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
}

.detail-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 16px;
  background: rgba(30, 41, 59, 0.4);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 12px;
  transition: all 0.2s ease;
}

.detail-item:hover {
  background: rgba(30, 41, 59, 0.6);
  border-color: rgba(99, 102, 241, 0.3);
  transform: translateY(-2px);
}

.detail-label {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.6);
  font-weight: 500;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.detail-value {
  font-size: 15px;
  color: #fff;
  font-weight: 500;
  word-break: break-word;
}

.detail-modal-footer {
  padding: 20px 28px;
  border-top: 1px solid rgba(255, 255, 255, 0.08);
  display: flex;
  justify-content: flex-end;
  background: rgba(15, 23, 42, 0.3);
}

.modal-btn-close {
  padding: 10px 24px;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
  border: none;
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%);
  color: #fff;
  box-shadow: 0 4px 12px rgba(99, 102, 241, 0.3);
}

.modal-btn-close:hover {
  background: linear-gradient(135deg, #7c3aed 0%, #a78bfa 100%);
  box-shadow: 0 6px 16px rgba(99, 102, 241, 0.4);
  transform: translateY(-1px);
}

.modal-btn-close:active {
  transform: translateY(0);
}

.modal-body {
  padding: 20px;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  color: rgba(255, 255, 255, 0.8);
  margin-bottom: 8px;
}

.form-group input,
.form-group select,
.form-group textarea {
  width: 100%;
  padding: 10px;
  border: 1px solid rgba(255, 255, 255, 0.15);
  border-radius: 8px;
  background: rgba(15, 23, 42, 0.6);
  color: #fff;
}

.modal-actions {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
}

.breadcrumb-separator {
  margin: 0 8px;
  color: rgba(255, 255, 255, 0.4);
}

/* 搜索筛选区域 */
.search-section {
  background: rgba(15, 23, 42, 0.6);
  padding: 24px;
  border-radius: 16px;
  margin: 20px;
  margin-bottom: 20px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.search-row {
  display: flex;
  align-items: flex-end;
  gap: 15px;
  flex-wrap: wrap;
}

.search-item {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.search-item label {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.7);
  font-weight: 500;
}

.search-item input,
.search-item select {
  padding: 8px 12px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 8px;
  font-size: 14px;
  width: 150px;
  transition: all 0.3s;
  background: rgba(255, 255, 255, 0.1);
  color: #fff;
}

.search-item input:focus,
.search-item select:focus {
  outline: none;
  border-color: rgba(99, 102, 241, 0.6);
  background: rgba(255, 255, 255, 0.15);
}

.search-item select option {
  background: rgba(15, 23, 42, 0.95);
  color: #fff;
}

.search-btn,
.reset-btn {
  padding: 8px 20px;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  display: inline-flex;
  align-items: center;
  gap: 5px;
  transition: all 0.3s;
  margin-right: 10px;
}

.search-btn {
  background: linear-gradient(135deg, rgba(99, 102, 241, 0.8), rgba(139, 92, 246, 0.8));
  color: #fff;
  box-shadow: 0 4px 15px rgba(99, 102, 241, 0.3);
}

.search-btn:hover {
  background: linear-gradient(135deg, rgba(99, 102, 241, 1), rgba(139, 92, 246, 1));
  box-shadow: 0 6px 20px rgba(99, 102, 241, 0.4);
  transform: translateY(-1px);
}

.reset-btn {
  background: rgba(255, 255, 255, 0.1);
  color: rgba(255, 255, 255, 0.7);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.reset-btn:hover {
  background: rgba(255, 255, 255, 0.15);
  color: rgba(255, 255, 255, 0.9);
}

/* 审核卡片区域 */
.review-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(500px, 1fr));
  gap: 20px;
  margin: 20px;
  margin-bottom: 20px;
}

.review-card {
  background: rgba(232, 233, 236, 0.6);
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 8px 32px rgba(255, 255, 255, 0.3);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  width: 100%;
  animation: cardSlideIn 0.4s ease-out;
}

@keyframes cardSlideIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.review-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 40px rgba(99, 102, 241, 0.3);
  border-color: rgba(99, 102, 241, 0.3);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  padding-bottom: 15px;
  border-bottom: 2px solid rgba(255, 255, 255, 0.1);
}

.card-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: #fff;
}

.title-icon {
  font-size: 20px;
}

.badge-count {
  background: linear-gradient(135deg, rgba(99, 102, 241, 0.8), rgba(139, 92, 246, 0.8));
  color: #fff;
  padding: 2px 10px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
  box-shadow: 0 2px 8px rgba(99, 102, 241, 0.3);
}

.btn-icon-only {
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 8px;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  font-size: 16px;
  transition: all 0.3s;
  color: rgba(255, 255, 255, 0.8);
}

.btn-icon-only:hover {
  background: rgba(255, 255, 255, 0.2);
  border-color: rgba(255, 255, 255, 0.3);
  color: #fff;
}

.table-container-small {
  overflow-x: auto;
}

.data-table-small {
  width: 100%;
  border-collapse: collapse;
  font-size: 13px;
}

.data-table-small thead {
  background: rgba(99, 102, 241, 0.2);
}

.data-table-small th {
  padding: 12px;
  text-align: left;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.9);
  font-size: 12px;
  border-bottom: 2px solid rgba(99, 102, 241, 0.3);
}

.data-table-small td {
  padding: 12px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  color: rgba(255, 255, 255, 0.8);
}

.data-table-small tbody tr:hover {
  background: rgba(99, 102, 241, 0.1);
}

.app-number {
  font-weight: 600;
  color: #1E8AE6;
  font-size: 12px;
}

.route-text {
  color: rgba(255, 255, 255, 0.8);
}

.cabin-badge {
  padding: 4px 10px;
  background: rgba(99, 102, 241, 0.2);
  color: #a5b4fc;
  border-radius: 6px;
  font-size: 11px;
  font-weight: 500;
  border: 1px solid rgba(99, 102, 241, 0.3);
}

.fee-cell {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.fee-text {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.75);
}

.price-diff-positive {
  color: #4ade80;
  font-weight: 600;
}

.price-diff-negative {
  color: #f87171;
  font-weight: 600;
}

/* 表格加载和空状态 */
.table-loading,
.table-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  color: rgba(255, 255, 255, 0.6);
}

.loading-spinner-small {
  width: 32px;
  height: 32px;
  border: 3px solid rgba(255, 255, 255, 0.1);
  border-top-color: #1E8AE6;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 12px;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 12px;
  opacity: 0.5;
}

.table-empty p {
  margin: 0;
  font-size: 14px;
}

.reason-cell {
  max-width: 200px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 过渡动画 */
.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.fade-slide-enter-from {
  opacity: 0;
  transform: translateY(20px) scale(0.95);
}

.fade-slide-leave-to {
  opacity: 0;
  transform: translateY(-20px) scale(0.95);
}

.fade-slide-enter-to,
.fade-slide-leave-from {
  opacity: 1;
  transform: translateY(0) scale(1);
}

.flight-number {
  font-weight: 500;
  color: rgba(255, 255, 255, 0.9);
}

.reason-text {
  color: rgba(255, 255, 255, 0.7);
  font-size: 12px;
}

.action-buttons-small {
  display: flex;
  gap: 5px;
}

.action-btn-small {
  width: 30px;
  height: 30px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
}

.approve-btn {
  background: linear-gradient(135deg, rgba(52, 211, 153, 0.8), rgba(16, 185, 129, 0.8));
  color: #fff;
}

.approve-btn:hover {
  background: linear-gradient(135deg, rgba(52, 211, 153, 1), rgba(16, 185, 129, 1));
  box-shadow: 0 4px 12px rgba(52, 211, 153, 0.4);
  transform: translateY(-2px);
}

.reject-btn {
  background: linear-gradient(135deg, rgba(248, 113, 113, 0.8), rgba(239, 68, 68, 0.8));
  color: #fff;
}

.reject-btn:hover {
  background: linear-gradient(135deg, rgba(248, 113, 113, 1), rgba(239, 68, 68, 1));
  box-shadow: 0 4px 12px rgba(248, 113, 113, 0.4);
  transform: translateY(-2px);
}

.material-btn {
  background: linear-gradient(135deg, rgba(251, 146, 60, 0.8), rgba(249, 115, 22, 0.8));
  color: #fff;
}

.material-btn:hover {
  background: linear-gradient(135deg, rgba(251, 146, 60, 1), rgba(249, 115, 22, 1));
  box-shadow: 0 4px 12px rgba(251, 146, 60, 0.4);
  transform: translateY(-2px);
}

.detail-btn {
  background: linear-gradient(135deg, rgba(96, 165, 250, 0.8), rgba(59, 130, 246, 0.8));
  color: #fff;
}

.detail-btn:hover {
  background: linear-gradient(135deg, rgba(96, 165, 250, 1), rgba(59, 130, 246, 1));
  box-shadow: 0 4px 12px rgba(96, 165, 250, 0.4);
  transform: translateY(-2px);
}

/* 风险策略规则 */
.rules-section {
  background: rgba(15, 23, 42, 0.6);
  border-radius: 16px;
  padding: 24px;
  margin: 20px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #fff;
  display: flex;
  align-items: center;
  gap: 8px;
}

.btn-secondary {
  padding: 8px 16px;
  background: rgba(255, 255, 255, 0.1);
  color: rgba(255, 255, 255, 0.8);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 5px;
  transition: all 0.3s;
}

.btn-secondary:hover {
  background: rgba(255, 255, 255, 0.15);
  border-color: rgba(255, 255, 255, 0.3);
  color: #fff;
}

.rules-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 15px;
}

.rule-card {
  background: rgba(255, 255, 255, 0.05);
  border-radius: 12px;
  padding: 18px;
  border: 1px solid rgba(255, 255, 255, 0.1);
  transition: all 0.3s;
  backdrop-filter: blur(10px);
}

.rule-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(99, 102, 241, 0.2);
  border-color: rgba(99, 102, 241, 0.3);
  background: rgba(255, 255, 255, 0.08);
}

.rule-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}

.rule-number {
  width: 28px;
  height: 28px;
  background: linear-gradient(135deg, rgba(99, 102, 241, 0.8), rgba(139, 92, 246, 0.8));
  color: #fff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 14px;
  flex-shrink: 0;
  box-shadow: 0 2px 8px rgba(99, 102, 241, 0.3);
}

.rule-header h3 {
  margin: 0;
  flex: 1;
  font-size: 15px;
  color: rgba(255, 255, 255, 0.9);
}

.rule-actions {
  display: flex;
  gap: 5px;
}

.rule-btn {
  width: 24px;
  height: 24px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  background: rgba(255, 255, 255, 0.1);
  border-radius: 6px;
  cursor: pointer;
  font-size: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s;
  color: rgba(255, 255, 255, 0.7);
}

.rule-btn:hover:not(:disabled) {
  background: rgba(255, 255, 255, 0.2);
  border-color: rgba(255, 255, 255, 0.3);
  color: #fff;
}

.rule-btn:disabled {
  opacity: 0.3;
  cursor: not-allowed;
}

.rule-desc {
  margin: 0 0 12px;
  color: rgba(255, 255, 255, 0.6);
  font-size: 13px;
  line-height: 1.5;
}

.rule-meta {
  display: flex;
  justify-content: space-between;
  padding-top: 12px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

.meta-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.meta-label {
  font-size: 11px;
  color: rgba(255, 255, 255, 0.5);
}

.meta-value {
  font-size: 13px;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.9);
}

.meta-value.effect {
  color: #fbbf24;
}

/* 模态框 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 10000;
  padding: 20px;
  box-sizing: border-box;
  overflow-y: auto;
  overscroll-behavior: contain;
  animation: fadeIn 0.3s ease-out;
  margin-top: -250px;
}

.modal-content {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 20px;
  width: 90%;
  max-width: 550px;
  max-height: 85vh;
  overflow-y: auto;
  box-shadow:
    0 25px 50px rgba(0, 0, 0, 0.3),
    inset 0 1px rgba(255, 255, 255, 0.3);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  animation: modal-pop 0.3s ease-out;
}

.modal-header {
  padding: 24px 24px 16px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.modal-header h3 {
  margin: 0;
  color: #1e293b;
  font-size: 22px;
  font-weight: 700;
}

.close-btn {
  background: none;
  border: none;
  font-size: 28px;
  color: #94a3b8;
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
  background: rgba(0, 0, 0, 0.05);
  color: #1e293b;
  transform: rotate(90deg);
}

.modal-body {
  padding: 24px;
}

.detail-row {
  display: flex;
  padding: 16px 0;
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
}

.detail-row:last-child {
  border-bottom: none;
}

.detail-label {
  width: 120px;
  color: #64748b;
  font-weight: 600;
  font-size: 15px;
}

.detail-value {
  flex: 1;
  color: #1e293b;
  font-size: 15px;
  font-weight: 500;
}

.modal-footer {
  padding: 16px 24px;
  border-top: 1px solid rgba(0, 0, 0, 0.05);
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.ghost-btn,
.primary-btn {
  padding: 10px 20px;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  border: none;
  transition: all 0.2s;
}

.ghost-btn {
  background: transparent;
  border: 1px solid rgba(0, 0, 0, 0.1);
  color: #64748b;
}

.ghost-btn:hover {
  background: rgba(0, 0, 0, 0.05);
}

.primary-btn {
  background: linear-gradient(135deg, #3b82f6, #1d4ed8);
  color: white;
}

.primary-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.4);
}

/* 手续费设置样式 */
.fee-setting-item {
  margin-bottom: 24px;
}

.fee-setting-label {
  display: flex;
  flex-direction: column;
  gap: 4px;
  margin-bottom: 8px;
  font-size: 14px;
  font-weight: 600;
  color: #1e293b;
}

.fee-setting-desc {
  font-size: 12px;
  font-weight: 400;
  color: #64748b;
}

.fee-input-group {
  position: relative;
  display: flex;
  align-items: center;
}

.fee-setting-input {
  width: 100%;
  padding: 12px 40px 12px 14px;
  border-radius: 10px;
  border: 1px solid rgba(0, 0, 0, 0.1);
  background: white;
  color: #1e293b;
  font-size: 14px;
}

.fee-setting-input:focus {
  outline: none;
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.fee-unit {
  position: absolute;
  right: 14px;
  font-size: 14px;
  color: #64748b;
  pointer-events: none;
}

.fee-preview {
  margin-top: 8px;
  padding: 8px 12px;
  background: rgba(59, 130, 246, 0.1);
  border-radius: 6px;
  font-size: 12px;
  color: #1e40af;
}

.preview-label {
  font-weight: 600;
  margin-right: 4px;
}

.fee-setting-note {
  margin-top: 24px;
  padding: 16px;
  background: rgba(59, 130, 246, 0.05);
  border-radius: 10px;
  border-left: 3px solid #3b82f6;
}

.fee-setting-note p {
  margin: 0 0 8px 0;
  font-size: 14px;
  font-weight: 600;
  color: #1e293b;
}

.fee-setting-note ul {
  margin: 0;
  padding-left: 20px;
  color: #64748b;
  font-size: 13px;
  line-height: 1.6;
}

.fee-setting-note li {
  margin-bottom: 4px;
}

@keyframes fadeIn {
  0% {
    opacity: 0;
  }
  100% {
    opacity: 1;
  }
}

@keyframes modal-pop {
  0% {
    opacity: 0;
    transform: scale(0.9);
  }
  100% {
    opacity: 1;
    transform: scale(1);
  }
}

/* ========== 退票审核美化样式 ========== */
.refund-card {
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.7), rgba(219, 223, 229, 0.6));
  border: 1px solid rgba(248, 113, 113, 0.2);
}

.refund-card:hover {
  border-color: rgba(248, 113, 113, 0.4);
  box-shadow: 0 12px 40px rgba(248, 113, 113, 0.15);
}

.refund-header {
  border-bottom: 2px solid rgba(248, 113, 113, 0.2);
}

.refund-icon {
  background: linear-gradient(135deg, #f87171, #ef4444);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.refund-badge {
  background: linear-gradient(135deg, rgba(248, 113, 113, 0.8), rgba(239, 68, 68, 0.8));
  box-shadow: 0 2px 8px rgba(248, 113, 113, 0.3);
}

.refund-table thead tr {
  background: linear-gradient(135deg, rgba(248, 113, 113, 0.15), rgba(239, 68, 68, 0.1));
}

.refund-table th {
  color: rgba(248, 113, 113, 0.9);
  font-weight: 600;
  text-transform: uppercase;
  font-size: 11px;
  letter-spacing: 0.5px;
}

.refund-row {
  transition: all 0.3s ease;
}

.refund-row:hover {
  background: rgba(248, 113, 113, 0.08);
}

.refund-app-number {
  background: linear-gradient(135deg, rgba(248, 113, 113, 0.2), rgba(239, 68, 68, 0.15));
  color: #f87171;
  padding: 4px 10px;
  border-radius: 6px;
  font-weight: 600;
  font-size: 12px;
}

/* 乘客单元格 */
.passenger-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}

.passenger-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-weight: 600;
  font-size: 14px;
  box-shadow: 0 2px 8px rgba(99, 102, 241, 0.3);
}

.passenger-name {
  font-weight: 500;
  color: rgba(255, 255, 255, 0.9);
}

/* 航线单元格 */
.route-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.city-name {
  color: rgba(255, 255, 255, 0.85);
  font-weight: 500;
}

.route-arrow {
  font-size: 14px;
  animation: planeMove 2s ease-in-out infinite;
}

@keyframes planeMove {
  0%, 100% { transform: translateX(0); }
  50% { transform: translateX(4px); }
}

/* 航班号徽章 */
.flight-badge {
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.2), rgba(37, 99, 235, 0.15));
  color: #60a5fa;
  padding: 4px 12px;
  border-radius: 6px;
  font-weight: 600;
  font-size: 12px;
  border: 1px solid rgba(59, 130, 246, 0.3);
}

/* 退款金额 */
.refund-amount {
  background: linear-gradient(135deg, rgba(34, 197, 94, 0.2), rgba(22, 163, 74, 0.15));
  color: #4ade80;
  padding: 6px 14px;
  border-radius: 8px;
  font-weight: 700;
  font-size: 14px;
  display: inline-block;
  border: 1px solid rgba(34, 197, 94, 0.3);
  box-shadow: 0 2px 8px rgba(34, 197, 94, 0.2);
}

/* 原因文本 */
.reason-text {
  display: inline-block;
  max-width: 120px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: rgba(255, 255, 255, 0.6);
  font-size: 13px;
}

.reason-text:hover {
  color: rgba(255, 255, 255, 0.9);
}
</style>

<!-- compact filter styles -->
<style scoped>
.filter-tabs-inline {
  display: flex;
  gap: 0.5rem;
  align-items: center;
  margin-right: 8px;
}
.tab-btn-inline {
  padding: 6px 12px;
  border-radius: 18px;
  border: 1px solid rgba(255,255,255,0.12);
  background: rgba(15,23,42,0.5);
  color: rgba(248,250,252,0.85);
  font-size: 13px;
  cursor: pointer;
  transition: all 0.18s;
}
.tab-btn-inline.active {
  background: linear-gradient(135deg,#1E8AE6,#0A2F63);
  color: #fff;
  box-shadow: 0 6px 20px rgba(30,64,175,0.28);
  border-color: rgba(30,64,175,0.6);
}
</style>

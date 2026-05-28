<template>
  <AdminLayout>
    <div class="page-container change-refund-page">
      <div class="breadcrumb">
        <span>航空运营</span>
        <span class="breadcrumb-separator">/</span>
        <span>改签/退票处理</span>
      </div>

      <header class="page-header">
        <div>
          <div class="header-top">
            <button class="back-btn" @click="handleBack">
              <span class="back-icon">←</span>
              <span>返回运营控制台</span>
            </button>
          </div>
          <p class="page-label">
            {{ currentUserRole === 'admin' ? '系统管理员 · 退改签审核' : '运行控制 · 改签/退票处理' }}
          </p>
          <h1>
            {{ currentType === 'change' ? '改签申请处理中心' : '退票申请处理中心' }}
          </h1>
          <p>
            集中处理延误、取消等不正常航班产生的
            {{ currentType === 'change' ? '改签申请' : '退票申请' }}，支持批量操作和自动费用计算
          </p>
        </div>
        <div class="page-actions">
          <!-- 管理员旁边的快速筛选标签（紧邻导出/刷新位置） -->
          <div v-if="currentUserRole === 'admin'" class="filter-tabs-inline">
            <button
              v-for="tab in filterTabs"
              :key="tab.value"
              :class="['tab-btn-inline', { active: currentFilter === tab.value }]"
              @click="currentFilter = tab.value"
            >
              {{ tab.label }}
            </button>
          </div>
          <button class="ghost-btn" @click="handleExport" :disabled="loading">
            {{ loading ? '导出中...' : '导出记录' }}
          </button>
        </div>
      </header>

      <!-- 搜索和批量操作栏 -->
      <section class="search-section glass-card">
        <div class="search-row">
          <div class="search-input-wrapper">
            <input
              type="text"
              v-model="searchKeyword"
              placeholder="搜索订单号、申请人姓名..."
              class="search-input"
              @input="handleSearch"
            />
            <span class="search-icon">🔍</span>
          </div>
          <div class="batch-actions" v-if="selectedApplications.length > 0">
            <span class="selected-count">已选择 {{ selectedApplications.length }} 项</span>
            <button class="batch-btn approve-btn" @click="handleBatchApprove">
              批量批准
            </button>
            <button class="batch-btn reject-btn" @click="handleBatchReject">
              批量拒绝
            </button>
          </div>
        </div>
      </section>

      <!-- 统计卡片 -->
      <section class="stats-grid">
        <div class="stat-card">
          <div class="stat-icon">📋</div>
          <div class="stat-content">
            <p class="stat-label">待处理申请</p>
            <p class="stat-value">{{total }}</p>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon">✅</div>
          <div class="stat-content">
            <p class="stat-label">已处理</p>
            <p class="stat-value">{{ stats.processed }}</p>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon">💰</div>
          <div class="stat-content">
            <p class="stat-label">
              {{ currentType === 'change' ? '已处理笔数' : '退款总额' }}
            </p>
            <p class="stat-value">
              {{ currentType === 'change' ? stats.processed : `¥${totalRefundFormatted}` }}
            </p>
          </div>
        </div>
      </section>

      <!-- 申请列表 -->
      <section class="glass-card applications-section">
        <div class="section-head">
          <div>
            <p class="section-label">申请列表</p>
            <h2>
              {{ currentType === 'change' ? '改签申请' : '退票申请' }}
              ({{ total }})
            </h2>
          </div>
          <div class="section-head-right">
            <div class="type-toggle">
              <button
                class="toggle-btn"
                :class="{ active: currentType === 'change' }"
                @click="currentType = 'change'"
              >
                改签申请
              </button>
              <button
                class="toggle-btn"
                :class="{ active: currentType === 'refund' }"
                @click="currentType = 'refund'"
              >
                退票申请
              </button>
            </div>
            <div class="filter-tabs">
              <button
                v-for="tab in filterTabs"
                :key="tab.value"
                :class="['tab-btn', { active: currentFilter === tab.value }]"
                @click="currentFilter = tab.value"
              >
                {{ tab.label }}
              </button>
            </div>
          </div>
        </div>

        <!-- 加载状态 -->
        <div v-if="loading" class="loading-state">
          <div class="loading-spinner"></div>
          <p>加载中...</p>
        </div>

        <!-- 空状态 -->
        <div v-else-if="filteredApplications.length === 0" class="empty-state">
          <div class="empty-icon">📭</div>
          <p>暂无申请记录</p>
        </div>

        <!-- 申请列表 -->
        <div v-else class="applications-list">
          <div
            v-for="app in paginatedApplications"
            :key="app.id"
            class="application-item"
            :class="[app.type, { selected: isSelected(app.id) }]"
          >
            <div class="app-checkbox">
              <input
                type="checkbox"
                :checked="isSelected(app.id)"
                @change="toggleSelect(app.id)"
                v-if="app.status === 'pending'"
              />
            </div>
            <div class="app-header">
              <div class="app-info">
                <h4>{{ app.type === 'change' ? '改签申请' : '退票申请' }} #{{ app.changeNo || app.id }}</h4>
                <p class="app-meta">
                  <span>订单号：{{ app.orderId }}</span>
                  <span>申请人：{{ app.passengerName }}</span>
                  <span>申请时间：{{ app.applyTime }}</span>
                </p>
              </div>
              <span :class="['status-badge', app.status]">{{ getStatusText(app.status) }}</span>
            </div>

            <div class="app-details">
              <div class="detail-row">
                <span class="detail-label">航班信息：</span>
                <span class="detail-value">{{ app.flightInfo }}</span>
              </div>
              <div class="detail-row">
                <span class="detail-label">申请原因：</span>
                <span class="detail-value">{{ app.reason }}</span>
              </div>
              <div v-if="app.type === 'change'" class="detail-row">
                <span class="detail-label">改签至：</span>
                <span class="detail-value">{{ app.newFlightInfo || '待选择' }}</span>
              </div>
              <div class="detail-row">
                <span class="detail-label">费用计算：</span>
                <span class="detail-value price">{{ app.feeCalculation }}</span>
              </div>
            </div>

            <div class="app-actions">
              <button
                v-if="app.status === 'pending' || app.rawStatus === '待审核' || app.rawStatus === '待处理'"
                class="action-btn approve-btn"
                @click="handleApprove(app)"
              >
                批准
              </button>
              <button
                v-if="app.status === 'pending' || app.rawStatus === '待审核' || app.rawStatus === '待处理'"
                class="action-btn reject-btn"
                @click="handleReject(app)"
              >
                拒绝
              </button>
              <!-- 管理员可指派给航空运营 -->
              <button
                v-if="currentUserRole === 'admin'"
                class="action-btn assign-btn"
                @click="openAssignModal(app)"
              >
                指派运营
              </button>
              <button
                class="action-btn detail-btn"
                @click="showApplicationDetail(app)"
              >
                查看详情
              </button>
            </div>
          </div>
        </div>

        <!-- 分页 -->
        <div class="pagination">
          <div class="pagination-info">
            共 {{ total }} 条，每页 {{ pageSize }} 条
          </div>
          <div class="pagination-controls">
            <button
              class="page-btn"
              :disabled="currentPage === 1"
              @click="goToPage(currentPage - 1)"
            >
              &lt;
            </button>
            <span class="page-number">{{ currentPage }} / {{ totalPages }}</span>
            <button
              class="page-btn"
              :disabled="currentPage === totalPages"
              @click="goToPage(currentPage + 1)"
            >
              &gt;
            </button>
          </div>
        </div>
      </section>

      <!-- 高级筛选模态框 -->
      <div v-if="showFilterModal" class="modal-overlay" @click="showFilterModal = false">
        <div class="modal-content filter-modal" @click.stop>
          <div class="modal-header">
            <h3>高级筛选</h3>
            <button class="close-btn" @click="showFilterModal = false">×</button>
          </div>
          <div class="modal-body">
            <div class="filter-form">
              <div class="filter-row">
                <label>申请类型</label>
                <select v-model="filterParams.type">
                  <option value="">全部</option>
                  <option value="change">改签</option>
                  <option value="refund">退票</option>
                </select>
              </div>
              <div class="filter-row">
                <label>申请状态</label>
                <select v-model="filterParams.status">
                  <option value="">全部</option>
                  <option value="pending">待处理</option>
                  <option value="approved">已批准</option>
                  <option value="rejected">已拒绝</option>
                </select>
              </div>
              <div class="filter-row">
                <label>申请日期</label>
                <div class="date-range">
                  <input type="date" v-model="filterParams.startDate" />
                  <span>至</span>
                  <input type="date" v-model="filterParams.endDate" />
                </div>
              </div>
              <div class="filter-row">
                <label>金额范围</label>
                <div class="amount-range">
                  <input type="number" v-model.number="filterParams.minAmount" placeholder="最小金额" />
                  <span>至</span>
                  <input type="number" v-model.number="filterParams.maxAmount" placeholder="最大金额" />
                </div>
              </div>
            </div>
          </div>
          <div class="modal-footer">
            <button class="ghost-btn" @click="resetFilter">重置</button>
            <button class="primary-btn" @click="applyFilter">应用筛选</button>
          </div>
        </div>
      </div>

      <!-- 拒绝原因输入模态框 -->
      <div v-if="showRejectModal" class="modal-overlay" @click="showRejectModal = false">
        <div class="modal-content reject-modal" @click.stop>
          <div class="modal-header">
            <h3>拒绝申请</h3>
            <button class="close-btn" @click="showRejectModal = false">×</button>
          </div>
          <div class="modal-body">
            <p class="reject-prompt">请输入拒绝原因（必填）：</p>
            <textarea
              v-model="rejectReason"
              class="reject-textarea"
              placeholder="请输入拒绝原因..."
              rows="4"
            ></textarea>
            <p v-if="rejectError" class="error-text">{{ rejectError }}</p>
          </div>
          <div class="modal-footer">
            <button class="ghost-btn" @click="cancelReject">取消</button>
            <button class="primary-btn reject-confirm-btn" @click="pendingRejectApps.length > 0 ? confirmBatchReject() : confirmReject()">
              确认拒绝
            </button>
          </div>
        </div>
      </div>

      <!-- 详情模态框 -->
      <div v-if="showDetailModal && selectedApplication" class="modal-overlay" @click="showDetailModal = false">
        <div class="modal-content" @click.stop>
          <div class="modal-header">
            <h3>{{ selectedApplication.type === 'change' ? '改签' : '退票' }}申请详情</h3>
            <button class="close-btn" @click="showDetailModal = false">×</button>
          </div>
          <div class="modal-body">
            <div class="detail-section">
              <h4>申请信息</h4>
              <div class="detail-grid">
                <div class="detail-item">
                  <span class="label">申请编号：</span>
                  <span class="value">#{{ selectedApplication.changeNo || selectedApplication.id }}</span>
                </div>
                <div class="detail-item">
                  <span class="label">订单号：</span>
                  <span class="value">#{{ selectedApplication.orderId }}</span>
                </div>
                <div class="detail-item">
                  <span class="label">申请人：</span>
                  <span class="value">{{ selectedApplication.passengerName }}</span>
                </div>
                <div class="detail-item">
                  <span class="label">申请时间：</span>
                  <span class="value">{{ selectedApplication.applyTime }}</span>
                </div>
                <div class="detail-item">
                  <span class="label">申请类型：</span>
                  <span class="value">{{ selectedApplication.type === 'change' ? '改签' : '退票' }}</span>
                </div>
                <div class="detail-item">
                  <span class="label">申请状态：</span>
                  <span :class="['status-badge', selectedApplication.status]">
                    {{ getStatusText(selectedApplication.status) }}
                  </span>
                </div>
              </div>
            </div>

            <div class="detail-section">
              <h4>航班信息</h4>
              <div class="detail-grid">
                <div class="detail-item">
                  <span class="label">原航班：</span>
                  <span class="value">{{ selectedApplication.flightInfo }}</span>
                </div>
                <div v-if="selectedApplication.type === 'change'" class="detail-item">
                  <span class="label">改签至：</span>
                  <span class="value">{{ selectedApplication.newFlightInfo || '待选择' }}</span>
                </div>
              </div>
            </div>

            <div class="detail-section">
              <h4>费用明细</h4>
              <div class="fee-breakdown">
                <div class="fee-item">
                  <span>原票价：</span>
                  <span>¥{{ selectedApplication.originalPrice }}</span>
                </div>
                <div class="fee-item">
                  <span>{{ selectedApplication.type === 'change' ? '改签费' : '退票费' }}：</span>
                  <span>¥{{ selectedApplication.changeFee || selectedApplication.refundFee }}</span>
                </div>
                <div class="fee-item total">
                  <span>{{ selectedApplication.type === 'change' ? '需补差价' : '退款金额' }}：</span>
                  <span>¥{{ selectedApplication.finalAmount }}</span>
                </div>
              </div>
            </div>

            <div class="detail-section">
              <h4>申请原因</h4>
              <p class="reason-text">{{ selectedApplication.reason }}</p>
            </div>
          </div>
          <div class="modal-footer">
            <button class="ghost-btn" @click="showDetailModal = false">关闭</button>
            <button
              v-if="selectedApplication.status === 'pending'"
              class="primary-btn"
              @click="handleApprove(selectedApplication)"
            >
              批准申请
            </button>
          </div>
        </div>
      </div>

      <!-- 提示模态框 -->
      <ModalPrompt
        v-model="showPrompt"
        :title="promptConfig.title"
        :message="promptConfig.message"
        :type="promptConfig.type"
        :show-cancel="promptConfig.showCancel"
        @confirm="showPrompt = false"
      />
      <!-- 指派模态框（管理员指派运营） -->
      <div v-if="showAssignModal" class="modal-overlay" @click="showAssignModal = false">
        <div class="modal-content" @click.stop>
          <div class="modal-header">
            <h3>指派给航空运营</h3>
            <button class="close-btn" @click="showAssignModal = false">×</button>
          </div>
          <div class="modal-body">
            <div class="filter-row">
              <label>选择运营人员</label>
              <select v-model.number="selectedOperator">
                <option :value="null">请选择</option>
                <option v-for="op in operatorList" :key="op.id" :value="op.id">{{ op.realName }}</option>
              </select>
              <p v-if="assignError" class="error-text">{{ assignError }}</p>
            </div>
          </div>
          <div class="modal-footer">
            <button class="ghost-btn" @click="showAssignModal = false">取消</button>
            <button class="primary-btn" @click="confirmAssign">确认指派</button>
          </div>
        </div>
      </div>
    </div>
  </AdminLayout>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import AdminLayout from '../../components/AdminLayout.vue'
import ModalPrompt from '../../components/ModalPrompt.vue'
import { changeRequestManagementApi, cancelRequestManagementApi, ticketManagementApi, apiUtils, operationsTicketChangeReviewApi } from '../../services/api'

const router = useRouter()

interface Application {
  id: string
  changeNo?: string // 改签编号
  cancelNo?: string // 退票编号
  orderId: string
  type: 'change' | 'refund'
  passengerName: string
  applyTime: string
  status: 'pending' | 'approved' | 'rejected'
  rawStatus?: string
  flightInfo: string
  newFlightInfo?: string
  reason: string
  feeCalculation: string
  originalPrice: number
  changeFee?: number
  refundFee?: number
  finalAmount: number
  rejectReason?: string
  processedTime?: string
  processor?: string
}

// 加载状态
const loading = ref(false)

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

// 统计数据（根据当前选中的申请类型动态统计）
const stats = reactive({
  pending: 0,
  processed: 0,
  totalRefund: 0,
  todayChange: 0,
  todayRefund: 0
})

// 格式化退款合计（保留2位小数）
const totalRefundFormatted = computed(() => {
  try {
    const v = Number(stats.totalRefund) || 0
    return v.toFixed(2)
  } catch {
    return '0.00'
  }
})

// 搜索关键词
const searchKeyword = ref('')

// 筛选参数
const filterParams = reactive({
  type: '',
  status: '',
  startDate: '',
  endDate: '',
  minAmount: null as number | null,
  maxAmount: null as number | null
})

// 选中的申请
const selectedApplications = ref<string[]>([])

// 拒绝相关
const showRejectModal = ref(false)
const rejectReason = ref('')
const rejectError = ref('')
const pendingRejectApp = ref<Application | null>(null)
const pendingRejectApps = ref<Application[]>([])

// 高级筛选模态框
const showFilterModal = ref(false)

// 当前操作类型：改签 / 退票
const currentType = ref<'change' | 'refund'>('change')

// 筛选标签
const filterTabs = [
  { label: '全部', value: 'all' },
  { label: '待处理', value: 'pending' },
  { label: '已批准', value: 'approved' },
  { label: '已拒绝', value: 'rejected' }
]

const currentFilter = ref('all')

// 申请列表（从API加载）
const applications = ref<Application[]>([])

// 当前用户角色，用于UI差异化展示
const currentUserRole = ref<string>('operator')

// 根据当前类型和筛选条件从后端加载数据
const loadApplications = async () => {
  loading.value = true
  try {
    // 构建查询参数
    const params: any = {
      page: currentPage.value - 1, // 后端从0开始
      size: pageSize.value
    }

    // 状态筛选（优先使用高级筛选中的状态，否则使用标签筛选）
    if (filterParams.status && filterParams.status.trim()) {
      const statusMap: Record<string, string> = {
        pending: '待处理',
        approved: '通过',
        rejected: '不通过'
      }
      params.status = statusMap[filterParams.status] || filterParams.status
    } else if (currentFilter.value !== 'all') {
      const statusMap: Record<string, string> = {
        pending: '待处理',
        approved: '通过',
        rejected: '拒绝'
      }
      params.status = statusMap[currentFilter.value] || currentFilter.value
    }

    // 搜索关键词
    if (searchKeyword.value.trim()) {
      // 尝试判断是订单号还是姓名
      const keyword = searchKeyword.value.trim()
      if (keyword.startsWith('ORD') || keyword.startsWith('#')) {
        params.orderNo = keyword.replace('#', '')
      } else {
        params.applicantName = keyword
      }
    }

    // 高级筛选
    if (filterParams.startDate) {
      params.startDate = filterParams.startDate
    }
    if (filterParams.endDate) {
      params.endDate = filterParams.endDate
    }

    let result: any
    if (currentType.value === 'change') {
      result = await changeRequestManagementApi.getChangeRequestList(params)
    } else {
      result = await cancelRequestManagementApi.getCancelRequestList(params)
    }

    if (result && result.success && result.data) {
      const data = result.data
      const list = data.list || []

      // 转换后端数据格式到前端格式
      applications.value = await Promise.all(
        list.map(async (item: any) => {
          // 状态映射（公用）
          const statusMap: Record<string, string> = {
            待处理: 'pending',
            待审核: 'pending',
            通过: 'approved',
            已批准: 'approved',
            不通过: 'rejected',
            拒绝: 'rejected',
            已拒绝: 'rejected'
          }
          const frontendStatus =
            (statusMap[item.status] || item.status || 'pending') as
              | 'pending'
              | 'approved'
              | 'rejected'

          if (currentType.value === 'change') {
            // 改签申请映射
            const oldFlightInfo = `${item.oldFlightNo || ''} ${item.oldRoute || ''} ${formatDateTime(item.oldDepartureTime)}`
            const newFlightInfo =
              item.newFlightNo && item.newRoute
                ? `${item.newFlightNo} ${item.newRoute} ${formatDateTime(item.newDepartureTime)}`
                : undefined

            let originalPrice = 0
            try {
              originalPrice = 0
            } catch {
              // ignore
            }

            let feeCalculation = ''
            if (item.changeFee !== null && item.changeFee !== undefined) {
              const changeFee = parseFloat(String(item.changeFee))
              const fareDiff = item.fareDiff ? parseFloat(String(item.fareDiff)) : 0
              if (fareDiff > 0) {
                feeCalculation = `改签费 ¥${changeFee} + 差价 ¥${fareDiff} = ¥${changeFee + fareDiff}`
              } else {
                feeCalculation = `改签费 ¥${changeFee}`
              }
            }

            return {
              id: String(item.id || ''),
              changeNo: item.changeNo || String(item.id || ''),
              orderId: item.orderno || '',
              type: 'change' as const,
              passengerName: item.applicantName || '',
              applyTime: formatDateTime(item.requestTime),
              status: frontendStatus,
              rawStatus: item.status || '',
              flightInfo: oldFlightInfo,
              newFlightInfo,
              reason: item.reason || '',
              feeCalculation,
              originalPrice,
              changeFee: item.changeFee ? parseFloat(String(item.changeFee)) : 0,
              finalAmount:
                (item.changeFee ? parseFloat(String(item.changeFee)) : 0) +
                (item.fareDiff && item.fareDiff > 0
                  ? parseFloat(String(item.fareDiff))
                  : 0),
              rejectReason: item.remark || '',
              processedTime: item.processedAt
                ? formatDateTime(item.processedAt)
                : undefined,
              processor: item.processedBy ? String(item.processedBy) : undefined
            } as Application
          }

          // 退票申请映射，对应 ticket_cancel_requests
          const routeText = item.route || ''
          const flightInfo = `${item.flightNo || ''} ${routeText} ${formatDateTime(item.departureTime)}`

          const cancelFee =
            item.cancelFee !== null && item.cancelFee !== undefined
              ? parseFloat(String(item.cancelFee))
              : 0
          const refundFare =
            item.refundFare !== null && item.refundFare !== undefined
              ? parseFloat(String(item.refundFare))
              : 0

          const feeCalculation = `退票费 ¥${cancelFee}，预计退款 ¥${refundFare}`

          return {
            id: String(item.id || ''),
            cancelNo: item.cancelNo || item.cancel_no || String(item.id || ''),
            orderId: item.orderno || item.orderNo || '',
            type: 'refund' as const,
            passengerName: item.applicantName || '',
            applyTime: formatDateTime(item.requestTime),
            status: frontendStatus,
            rawStatus: item.status || '',
            flightInfo,
            reason: item.reason || '',
            feeCalculation,
            originalPrice: cancelFee + refundFare,
            refundFee: cancelFee,
            finalAmount: refundFare,
            rejectReason: item.remark || '',
            processedTime: item.processedAt
              ? formatDateTime(item.processedAt)
              : undefined,
            processor: item.processedBy ? String(item.processedBy) : undefined
          } as Application
        })
      )

      // 更新分页信息
      total.value = data.total || 0
      currentPage.value = (data.page || 0) + 1

      // 更新统计数据
      await updateStats()
    } else {
      applications.value = []
      total.value = 0
      currentPage.value = 1
    }
  } catch (error: any) {
    console.error('加载申请列表失败:', error)
    showPromptModal('错误', error?.message || '加载申请列表失败', 'error')
    applications.value = []
    total.value = 0
    currentPage.value = 1
  } finally {
    loading.value = false
  }
}

// 格式化日期时间
const formatDateTime = (dateTime: string | Date | null | undefined) => {
  if (!dateTime) return ''
  
  if (typeof dateTime === 'string') {
    // 处理字符串格式：移除时区信息
    let dateStr = dateTime.trim()
    if (dateStr.endsWith('Z')) {
      dateStr = dateStr.slice(0, -1)
    }
    const timezoneMatch = dateStr.match(/([+-]\d{2}:\d{2})$/);
    if (timezoneMatch) {
      dateStr = dateStr.slice(0, -timezoneMatch[0].length)
    }
    dateStr = dateStr.replace('T', ' ')
    
    // 提取日期和时间部分
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

// 更新统计数据（根据当前类型分别统计）
const updateStats = async () => {
  try {
    if (currentType.value === 'change') {
      const result = await changeRequestManagementApi.getStatistics()
      if (result && result.success && result.data) {
        stats.pending = result.data.pending || 0
        stats.processed =
          (result.data.approved || 0) + (result.data.rejected || 0)
        stats.totalRefund = 0
      }
    } else {
      const result = await cancelRequestManagementApi.getStatistics()
      if (result && result.success && result.data) {
        stats.pending = result.data.pending || 0
        stats.processed =
          (result.data.approved || 0) + (result.data.rejected || 0)
        stats.totalRefund = result.data.totalRefundFare || 0
      }
    }

    // 如果后端未返回或失败，使用前端计算兜底
    if (!Number.isFinite(stats.pending) || !Number.isFinite(stats.processed)) {
      stats.pending = applications.value.filter(
        app => app.status === 'pending'
      ).length
      const today = new Date().toISOString().split('T')[0]
      let processed = 0
      let refundSum = 0
      for (const app of applications.value) {
        if (app.status === 'pending') continue
        const pt = app.processedTime
        if (pt && pt.length > 0 && pt.substring(0, 10) === today) {
          processed++
        }
        if (app.type === 'refund') {
          refundSum += app.finalAmount || 0
        }
      }
      stats.processed = processed
      stats.totalRefund = refundSum
    }
  } catch (error) {
    stats.pending = applications.value.filter(
      app => app.status === 'pending'
    ).length
    const today = new Date().toISOString().split('T')[0]
    let processed = 0
    let refundSum = 0
    for (const app of applications.value) {
      if (app.status === 'pending') continue
      const pt = app.processedTime
      if (pt && pt.length > 0 && pt.substring(0, 10) === today) {
        processed++
      }
    }
    stats.processed = processed
    for (const app of applications.value) {
      if (app.type === 'refund') {
        refundSum += app.finalAmount || 0
      }
    }
    stats.totalRefund = refundSum
  }
}

// 筛选后的申请列表（后端已筛选，直接使用）
const filteredApplications = computed(() => applications.value)

// 搜索处理
const handleSearch = async () => {
  currentPage.value = 1
  await loadApplications()
}

// 分页
const currentPage = ref(1)
const pageSize = ref(5)
const total = ref(0)

const totalPages = computed(() => Math.ceil(total.value / pageSize.value))

// 分页后的申请列表（后端已分页，直接使用）
const paginatedApplications = computed(() => filteredApplications.value)

// 跳转页面
const goToPage = async (page: number) => {
  if (page >= 1 && page <= totalPages.value) {
    currentPage.value = page
    await loadApplications()
  }
}

// 获取状态文本
const getStatusText = (status: string) => {
  const statusMap: Record<string, string> = {
    pending: '待处理',
    approved: '已批准',
    rejected: '已拒绝'
  }
  return statusMap[status] || status
}

// 显示详情
const selectedApplication = ref<Application | null>(null)
const showDetailModal = ref(false)

const showApplicationDetail = (app: Application) => {
  selectedApplication.value = app
  showDetailModal.value = true
}

// 批准申请（根据类型调用不同后端）
const handleApprove = async (app: Application) => {
  try {
    // 获取当前用户ID（处理人）
    const currentUser = apiUtils.getCurrentUser()
    const processedBy = currentUser?.id ? (typeof currentUser.id === 'string' ? parseInt(currentUser.id, 10) : currentUser.id) : undefined
    
    // 确保ID是数字类型
    const appId = typeof app.id === 'string' ? parseInt(app.id, 10) : app.id
    if (isNaN(appId)) {
      showPromptModal('错误', '无效的申请ID', 'error')
      return
    }
    
    if (app.type === 'change') {
      if (currentUserRole.value === 'admin') {
        await changeRequestManagementApi.approveChangeRequest(appId, processedBy)
      } else {
        // 运营人员使用 operations API
        await operationsTicketChangeReviewApi.approveChangeRequest(appId, processedBy)
      }
    } else {
      // 退票：管理员页面使用 ticketManagementApi，运营/其他使用 cancelRequestManagementApi
      if (currentUserRole.value === 'admin') {
        await ticketManagementApi.approveRefund(appId, processedBy)
      } else {
        await cancelRequestManagementApi.approveCancelRequest(appId, processedBy)
      }
    }
    
    // 重新加载数据
    await loadApplications()
    
    // 从选中列表中移除
    const index = selectedApplications.value.indexOf(app.id)
    if (index > -1) {
      selectedApplications.value.splice(index, 1)
    }
    
    const label = app.type === 'change' ? '改签申请' : '退票申请'
    showPromptModal('成功', `${label} #${app.id} 已批准`, 'success')
    
    if (showDetailModal.value) {
      showDetailModal.value = false
    }
  } catch (error: any) {
    console.error('批准申请失败:', error)
    showPromptModal('错误', error?.message || '批准申请失败', 'error')
  }
}

// 拒绝申请
const handleReject = (app: Application) => {
  pendingRejectApp.value = app
  pendingRejectApps.value = []
  rejectReason.value = ''
  rejectError.value = ''
  showRejectModal.value = true
}

// 确认拒绝
const confirmReject = async () => {
  if (!rejectReason.value.trim()) {
    rejectError.value = '请输入拒绝原因'
    return
  }

  try {
    const app = pendingRejectApp.value
    if (!app) return

    // 获取当前用户ID（处理人）
    const currentUser = apiUtils.getCurrentUser()
    const processedBy = currentUser?.id ? (typeof currentUser.id === 'string' ? parseInt(currentUser.id, 10) : currentUser.id) : undefined

    // 确保ID是数字类型
    const appId = typeof app.id === 'string' ? parseInt(app.id, 10) : app.id
    if (isNaN(appId)) {
      rejectError.value = '无效的申请ID'
      return
    }

    if (app.type === 'change') {
      if (currentUserRole.value === 'admin') {
        await changeRequestManagementApi.rejectChangeRequest(
          appId,
          rejectReason.value,
          processedBy
        )
      } else {
        await operationsTicketChangeReviewApi.rejectChangeRequest(
          appId,
          rejectReason.value,
          processedBy
        )
      }
    } else {
      // 退票：管理员页面使用 ticketManagementApi (它期望字段 'reason'), 运营使用 cancelRequestManagementApi (使用 'remark')
      if (currentUserRole.value === 'admin') {
        await ticketManagementApi.rejectRefund(
          appId,
          rejectReason.value,
          processedBy
        )
      } else {
        await cancelRequestManagementApi.rejectCancelRequest(
          appId,
          rejectReason.value,
          processedBy
        )
      }
    }
    
    // 重新加载数据
    await loadApplications()
    
    // 从选中列表中移除
    const index = selectedApplications.value.indexOf(app.id)
    if (index > -1) {
      selectedApplications.value.splice(index, 1)
    }
    
    const label = app.type === 'change' ? '改签申请' : '退票申请'
    showPromptModal('成功', `${label} #${app.id} 已拒绝`, 'success')
    
    showRejectModal.value = false
    pendingRejectApp.value = null
    rejectReason.value = ''
    rejectError.value = ''
  } catch (error: any) {
    console.error('拒绝申请失败:', error)
    rejectError.value = error?.message || '拒绝申请失败'
  }
}

// 取消拒绝
const cancelReject = () => {
  showRejectModal.value = false
  pendingRejectApp.value = null
  rejectReason.value = ''
  rejectError.value = ''
}

// ====== 指派给航空运营（管理员使用） ======
const showAssignModal = ref(false)
const assignError = ref('')
const operatorList = ref<{ id: number; realName: string }[]>([])
const selectedOperator = ref<number | null>(null)
const pendingAssignApp = ref<Application | null>(null)

const loadOperators = async () => {
  try {
    const res: any = await (await import('../../services/api')).userManagementApi.getUserList({ role: 'operator', size: 100 })
    const data = res && res.success && res.data ? res.data : res
    const list = data && data.list ? data.list : (Array.isArray(data) ? data : [])
    operatorList.value = list.map((u: any) => ({ id: u.id, realName: u.realName || u.name || u.username }))
  } catch (e) {
    console.warn('加载运营用户失败', e)
    operatorList.value = []
  }
}

const openAssignModal = (app: Application) => {
  pendingAssignApp.value = app
  selectedOperator.value = null
  assignError.value = ''
  showAssignModal.value = true
  loadOperators()
}

const confirmAssign = async () => {
  if (!pendingAssignApp.value) return
  if (!selectedOperator.value) {
    assignError.value = '请选择要指派的运营'
    return
  }
  try {
    const api = await import('../../services/api')
    await api.cancelRequestManagementApi.assign(pendingAssignApp.value.id, selectedOperator.value)
    showAssignModal.value = false
    pendingAssignApp.value = null
    selectedOperator.value = null
    await loadApplications()
    showPromptModal('成功', '已指派给运营', 'success')
  } catch (e: any) {
    console.error('指派失败', e)
    assignError.value = e?.message || '指派失败'
  }
}

// 批量批准
const handleBatchApprove = async () => {
  if (selectedApplications.value.length === 0) {
    showPromptModal('提示', '请先选择要批准的申请', 'info')
    return
  }

  try {
    // 获取当前用户ID（处理人）
    const currentUser = apiUtils.getCurrentUser()
    const processedBy = currentUser?.id ? (typeof currentUser.id === 'string' ? parseInt(currentUser.id, 10) : currentUser.id) : undefined

    // 只针对当前类型的待处理申请做批量操作
    const ids = applications.value
      .filter(
        app =>
          selectedApplications.value.includes(app.id) &&
          app.status === 'pending' &&
          app.type === currentType.value
      )
      .map(app => app.id)

    if (ids.length === 0) {
      showPromptModal('提示', '当前筛选类型下没有可批量批准的申请', 'info')
      return
    }

    if (currentType.value === 'change') {
      await changeRequestManagementApi.batchApprove(ids, processedBy)
    } else {
      await cancelRequestManagementApi.batchApprove(ids, processedBy)
    }
    
    // 重新加载数据
    await loadApplications()
    
    const count = ids.length
    selectedApplications.value = []
    
    const label = currentType.value === 'change' ? '改签申请' : '退票申请'
    showPromptModal('成功', `已批量批准 ${count} 个${label}`, 'success')
  } catch (error: any) {
    console.error('批量批准失败:', error)
    showPromptModal('错误', error?.message || '批量批准失败', 'error')
  }
}

// 批量拒绝
const handleBatchReject = () => {
  if (selectedApplications.value.length === 0) {
    showPromptModal('提示', '请先选择要拒绝的申请', 'info')
    return
  }

  pendingRejectApp.value = null
  pendingRejectApps.value = applications.value.filter(
    app =>
      selectedApplications.value.includes(app.id) &&
      app.status === 'pending' &&
      app.type === currentType.value
  )
  rejectReason.value = ''
  rejectError.value = ''
  showRejectModal.value = true
}

// 确认批量拒绝
const confirmBatchReject = async () => {
  if (!rejectReason.value.trim()) {
    rejectError.value = '请输入拒绝原因'
    return
  }

  try {
    // 获取当前用户ID（处理人）
    const currentUser = apiUtils.getCurrentUser()
    const processedBy = currentUser?.id ? (typeof currentUser.id === 'string' ? parseInt(currentUser.id, 10) : currentUser.id) : undefined

    // 只针对当前类型的待处理申请做批量操作
    const ids = applications.value
      .filter(
        app =>
          selectedApplications.value.includes(app.id) &&
          app.status === 'pending' &&
          app.type === currentType.value
      )
      .map(app => app.id)

    if (ids.length === 0) {
      rejectError.value = '当前筛选类型下没有可批量拒绝的申请'
      return
    }

    if (currentType.value === 'change') {
      await changeRequestManagementApi.batchReject(
        ids,
        rejectReason.value,
        processedBy
      )
    } else {
      await cancelRequestManagementApi.batchReject(
        ids,
        rejectReason.value,
        processedBy
      )
    }
    
    // 重新加载数据
    await loadApplications()
    
    const count = ids.length
    selectedApplications.value = []
    
    showRejectModal.value = false
    pendingRejectApp.value = null
    pendingRejectApps.value = []
    rejectReason.value = ''
    rejectError.value = ''
    
    const label = currentType.value === 'change' ? '改签申请' : '退票申请'
    showPromptModal('成功', `已批量拒绝 ${count} 个${label}`, 'success')
  } catch (error: any) {
    console.error('批量拒绝失败:', error)
    rejectError.value = error?.message || '批量拒绝失败'
  }
}

// 选择相关
const isSelected = (id: string) => {
  return selectedApplications.value.includes(id)
}

const toggleSelect = (id: string) => {
  const index = selectedApplications.value.indexOf(id)
  if (index > -1) {
    selectedApplications.value.splice(index, 1)
  } else {
    selectedApplications.value.push(id)
  }
}

// 导出
const handleExport = async () => {
  try {
    loading.value = true
    
    // 准备导出数据
    const exportData = filteredApplications.value.map(app => ({
      申请编号: app.id,
      订单号: app.orderId,
      申请类型: app.type === 'change' ? '改签' : '退票',
      申请人: app.passengerName,
      申请时间: app.applyTime,
      状态: getStatusText(app.status),
      航班信息: app.flightInfo,
      改签至: app.newFlightInfo || '-',
      申请原因: app.reason,
      原票价: app.originalPrice,
      费用: app.type === 'change' ? app.changeFee : app.refundFee,
      最终金额: app.finalAmount,
      拒绝原因: app.rejectReason || '-',
      处理时间: app.processedTime || '-',
      处理人: app.processor || '-'
    }))
    
    // 转换为CSV
    const headers = Object.keys(exportData[0] || {})
    const csvContent = [
      headers.join(','),
      ...exportData.map(row => headers.map(header => `"${row[header as keyof typeof row]}"`).join(','))
    ].join('\n')
    
    // 添加BOM以支持中文
    const BOM = '\uFEFF'
    const blob = new Blob([BOM + csvContent], { type: 'text/csv;charset=utf-8;' })
    const url = URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `退改签申请记录_${new Date().toISOString().split('T')[0]}.csv`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    URL.revokeObjectURL(url)
    
    showPromptModal('成功', '导出成功', 'success')
  } catch (error) {
    showPromptModal('错误', '导出失败', 'error')
  } finally {
    loading.value = false
  }
}

// 应用筛选
const applyFilter = async () => {
  currentPage.value = 1
  showFilterModal.value = false
  await loadApplications()
}

// 重置筛选
const resetFilter = async () => {
  filterParams.type = ''
  filterParams.status = ''
  filterParams.startDate = ''
  filterParams.endDate = ''
  filterParams.minAmount = null
  filterParams.maxAmount = null
  currentPage.value = 1
  await loadApplications()
}

// 监听筛选变化，重新加载数据
watch([() => currentFilter.value], async () => {
  // 切换标签时同步更新 filterParams.status（便于与高级筛选统一）
  if (currentFilter.value === 'all') {
    filterParams.status = ''
  } else {
    // 映射为后端中文状态
    const statusMap: Record<string, string> = {
      pending: '待处理',
      approved: '通过',
      rejected: '不通过'
    }
    filterParams.status = statusMap[currentFilter.value] || currentFilter.value
  }
  currentPage.value = 1
  await loadApplications()
})

// 切换操作类型（改签 / 退票），重新拉取对应列表
watch(currentType, async () => {
  currentPage.value = 1
  await loadApplications()
})

// 返回运营控制台
const handleBack = () => {
  router.push('/portal/operations')
}

// 初始化
onMounted(() => {
  const user = apiUtils.getCurrentUser()
  currentUserRole.value = user?.role || 'operator'

  // 系统管理员默认查看全部，运营默认聚焦“待处理”
  if (currentUserRole.value === 'admin') {
    currentFilter.value = 'all'
  } else {
    currentFilter.value = 'pending'
  }

  loadApplications()
})
</script>

<style scoped>
.change-refund-page {
  padding: 2.5rem clamp(1.5rem, 6vw, 4rem) 3rem;
  color: var(--text-primary);
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
  background: transparent;
}

.breadcrumb {
  font-size: 14px;
  color: var(--text-secondary);
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
  align-items: center;
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
  background: rgba(209, 213, 224, 0.6);
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
  background: linear-gradient(135deg, var(--color-primary), var(--color-primary-dark));
  color: var(--color-white);
  box-shadow: 0 10px 25px rgba(37,99,235,0.12);
}

.ghost-btn {
  border-color: var(--border-color);
  background: transparent;
  color: var(--text-primary);
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 1.2rem;
  align-items: stretch;
}

.stat-card {
  display: flex;
  gap: 1rem;
  align-items: center;
  padding: 1.5rem;
  border-radius: 16px;
  background: linear-gradient(180deg, var(--bg-secondary), var(--bg-tertiary));
  border: 1px solid var(--border-color);
  backdrop-filter: blur(6px);
  box-shadow: var(--shadow-sm);
}

.stat-icon {
  font-size: 2rem;
}

.stat-content {
  flex: 1;
}

.stat-label {
  margin: 0 0 0.5rem 0;
  color: rgba(248, 250, 252, 0.65);
  font-size: 0.85rem;
}

.stat-value {
  margin: 0;
  font-size: 1.8rem;
  font-weight: 700;
  color: #fff;
}

.glass-card {
  border-radius: 20px;
  padding: 1.2rem;
  background: linear-gradient(180deg, var(--bg-primary), var(--bg-secondary));
  border: 1px solid var(--border-color);
  box-shadow: var(--shadow-md);
  backdrop-filter: blur(6px) saturate(120%);
}

.section-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 1rem;
  margin-bottom: 1.5rem;
  flex-wrap: wrap;
}

.section-head-right {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  flex-wrap: wrap;
  justify-content: flex-end;
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

.filter-tabs {
  display: flex;
  gap: 0.5rem;
}

.tab-btn {
  padding: 0.5rem 1rem;
  border: 1px solid var(--border-color);
  background: transparent;
  color: var(--text-secondary);
  border-radius: 8px;
  cursor: pointer;
  font-size: 0.85rem;
  transition: all 0.18s;
}

.tab-btn:hover {
  background: color-mix(in srgb, var(--color-primary, #2563eb) 6%, transparent);
  color: var(--color-primary-dark);
}

.tab-btn.active {
  background: linear-gradient(135deg, var(--color-primary), var(--color-primary-dark));
  border-color: color-mix(in srgb, var(--color-primary, #2563eb) 18%, transparent);
  color: var(--color-white);
}

/* 页头内的紧凑筛选标签 */
.filter-tabs-inline {
  display: flex;
  gap: 0.4rem;
  align-items: center;
  margin-right: 0.6rem;
}
.tab-btn-inline {
  padding: 0.35rem 0.7rem;
  border: 1px solid rgba(255,255,255,0.12);
  background: rgba(15,23,42,0.45);
  color: rgba(248,250,252,0.8);
  border-radius: 8px;
  cursor: pointer;
  font-size: 0.82rem;
}
.tab-btn-inline.active {
  background: rgba(99,102,241,0.35);
  border-color: rgba(99,102,241,0.6);
  color: #fff;
}

/* 改签/退票类型切换按钮 */
.type-toggle {
  display: inline-flex;
  padding: 3px;
  border-radius: 999px;
  background: rgba(15, 23, 42, 0.8);
  border: 1px solid rgba(148, 163, 184, 0.6);
}

.toggle-btn {
  border: none;
  background: transparent;
  color: rgba(248, 250, 252, 0.75);
  font-size: 0.85rem;
  padding: 0.35rem 0.9rem;
  border-radius: 999px;
  cursor: pointer;
  transition: all 0.2s;
  white-space: nowrap;
}

.toggle-btn.active {
  background: linear-gradient(135deg, #1E8AE6, #0A2F63);
  color: #fff;
  box-shadow: 0 4px 12px rgba(37, 99, 235, 0.45);
}

.toggle-btn:not(.active):hover {
  background: rgba(148, 163, 184, 0.25);
}

.applications-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.application-item {
  padding: 1.5rem;
  border-radius: 12px;
  border: 1px solid var(--border-color);
  background: linear-gradient(180deg, var(--bg-primary), var(--bg-secondary));
  box-shadow: var(--shadow-sm);
  transition: transform var(--transition-base), box-shadow var(--transition-base), border-color var(--transition-base);
}

.application-item:hover {
  border-color: color-mix(in srgb, var(--color-primary, #2563eb) 12%, transparent);
  transform: translateY(-4px);
  box-shadow: var(--shadow-md);
}

.application-item.change {
  border-left: 4px solid var(--color-primary);
}

.application-item.refund {
  border-left: 4px solid var(--color-warning);
}

.app-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 1rem;
  margin-bottom: 1rem;
}

.app-info h4 {
  margin: 0 0 0.5rem 0;
  color: #fff;
  font-size: 1.1rem;
}

.app-meta {
  display: flex;
  gap: 1.5rem;
  flex-wrap: wrap;
  font-size: 0.85rem;
  color: rgba(248, 250, 252, 0.6);
  margin: 0;
}

.status-badge {
  padding: 0.4rem 0.9rem;
  border-radius: 999px;
  font-size: 0.85rem;
  font-weight: 600;
  white-space: nowrap;
  border: 1px solid transparent;
}
.status-badge.pending {
  background: color-mix(in srgb, var(--color-warning, #f59e0b) 12%, transparent);
  color: var(--color-warning);
  border-color: color-mix(in srgb, var(--color-warning, #f59e0b) 18%, transparent);
}
.status-badge.approved {
  background: color-mix(in srgb, var(--color-success, #10b981) 12%, transparent);
  color: var(--color-success);
  border-color: color-mix(in srgb, var(--color-success, #10b981) 18%, transparent);
}
.status-badge.rejected {
  background: color-mix(in srgb, var(--color-error, #ef4444) 12%, transparent);
  color: var(--color-error);
  border-color: color-mix(in srgb, var(--color-error, #ef4444) 18%, transparent);
}

.app-details {
  display: flex;
  flex-direction: column;
  gap: 0.8rem;
  margin-bottom: 1rem;
  padding: 1rem;
  border-radius: 12px;
  background: var(--bg-tertiary);
}

.detail-row {
  display: flex;
  gap: 0.5rem;
  font-size: 0.9rem;
}

.detail-label {
  color: rgba(248, 250, 252, 0.6);
  min-width: 100px;
}

.detail-value {
  color: rgba(248, 250, 252, 0.9);
}

.detail-value.price {
  color: #fcd34d;
  font-weight: 600;
}

.app-actions {
  display: flex;
  gap: 0.8rem;
  flex-wrap: wrap;
}

.action-btn {
  padding: 0.6rem 1.2rem;
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 8px;
  background: rgba(15, 23, 42, 0.6);
  color: #fff;
  font-size: 0.9rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
}

.approve-btn {
  background: rgba(34, 197, 94, 0.2);
  border-color: rgba(34, 197, 94, 0.4);
  color: #4ade80;
}

.approve-btn:hover {
  background: rgba(34, 197, 94, 0.3);
}

.reject-btn {
  background: rgba(248, 113, 113, 0.2);
  border-color: rgba(248, 113, 113, 0.4);
  color: #f87171;
}

.reject-btn:hover {
  background: rgba(248, 113, 113, 0.3);
}

.detail-btn {
  background: rgba(96, 165, 250, 0.2);
  border-color: rgba(96, 165, 250, 0.4);
  color: #1E8AE6;
}

.detail-btn:hover {
  background: rgba(96, 165, 250, 0.3);
}

.pagination {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.2rem 0;
  margin-top: 1rem;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

.pagination-info {
  color: rgba(255, 255, 255, 0.7);
  font-size: 0.9rem;
}

.pagination-controls {
  display: flex;
  align-items: center;
  gap: 0.8rem;
}

.page-btn {
  padding: 0.5rem 1rem;
  border: 1px solid rgba(255, 255, 255, 0.15);
  background: rgba(15, 23, 42, 0.6);
  border-radius: 8px;
  cursor: pointer;
  color: rgba(255, 255, 255, 0.8);
  transition: all 0.3s;
}

.page-btn:hover:not(:disabled) {
  background: rgba(255, 255, 255, 0.1);
}

.page-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.page-number {
  padding: 0.5rem 1rem;
  color: rgba(255, 255, 255, 0.8);
}

/* 模态框样式 */
.modal-overlay {
  position: fixed;
  inset: 0;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(6, 11, 40, 0.75);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 10000;
  padding: 20px;
  box-sizing: border-box;
  overflow-y: auto;
  overscroll-behavior: contain;
  /* 确保在移动设备上也能正确显示 */
  -webkit-overflow-scrolling: touch;
  /* 确保弹窗始终在视口中心 */
  min-height: 100vh;
  min-height: 100dvh;
}

.modal-content {
  background: rgba(15, 23, 42, 0.95);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 24px;
  width: 90%;
  max-width: 700px;
  max-height: calc(100vh - 40px);
  max-height: calc(100dvh - 40px); /* 支持动态视口高度 */
  overflow-y: auto;
  box-shadow: 0 25px 50px rgba(15, 23, 42, 0.5);
  margin: auto;
  position: relative;
  box-sizing: border-box;
  scroll-behavior: smooth;
  /* 确保在小屏幕上也能正确显示 */
  display: flex;
  flex-direction: column;
  /* 确保弹窗在滚动容器中也能居中 */
  flex-shrink: 0;
}

/* 响应式优化 - 平板 */
@media (min-width: 768px) and (max-width: 1024px) {
  .modal-content {
    width: min(650px, calc(100vw - 60px));
    padding: clamp(20px, 3vw, 24px);
  }
  
  .modal-header {
    padding: clamp(18px, 3vw, 22px);
  }
  
  .modal-body {
    padding: clamp(18px, 3vw, 22px);
  }
  
  .modal-header h3 {
    font-size: clamp(20px, 3vw, 22px);
  }
}

/* 响应式优化 - 移动端 */
@media (max-width: 767px) {
  .modal-overlay {
    padding: 12px;
    align-items: flex-start;
    padding-top: max(12px, env(safe-area-inset-top, 12px));
    padding-bottom: max(12px, env(safe-area-inset-bottom, 12px));
  }
  
  .modal-content {
    width: calc(100vw - 24px);
    max-width: none;
    border-radius: clamp(16px, 4vw, 20px);
    margin-top: auto;
    margin-bottom: auto;
  }
  
  .modal-header {
    padding: clamp(16px, 4vw, 20px);
  }
  
  .modal-header h3 {
    font-size: clamp(18px, 4.5vw, 20px);
  }
  
  .modal-body {
    padding: clamp(16px, 4vw, 20px);
  }
  
  .detail-section {
    margin-bottom: clamp(1.2rem, 3vw, 1.5rem);
  }
  
  .detail-section h4 {
    font-size: clamp(1rem, 2.5vw, 1.1rem);
  }
  
  .detail-grid {
    grid-template-columns: 1fr;
    gap: clamp(0.8rem, 2vw, 1rem);
  }
  
  .modal-footer {
    padding: clamp(16px, 4vw, 20px);
    flex-direction: column;
  }
  
  .modal-footer .ghost-btn,
  .modal-footer .primary-btn {
    width: 100%;
  }
}

/* 响应式优化 - 小屏幕移动端 */
@media (max-width: 480px) {
  .modal-overlay {
    padding: 8px;
    align-items: flex-start;
    padding-top: max(8px, env(safe-area-inset-top, 8px));
    padding-bottom: max(8px, env(safe-area-inset-bottom, 8px));
  }
  
  .modal-content {
    width: calc(100vw - 16px);
    margin-top: auto;
    margin-bottom: auto;
  }
  
  .modal-header {
    padding: 14px 16px;
  }
  
  .modal-body {
    padding: 14px 16px;
  }
  
  .modal-header h3 {
    font-size: 18px;
  }
}

/* 响应式优化 - 低高度屏幕 */
@media (max-height: 600px) {
  .modal-overlay {
    align-items: flex-start;
    padding-top: 10px;
    padding-bottom: 10px;
  }
  
  .modal-content {
    max-height: calc(100vh - 20px);
    max-height: calc(100dvh - 20px);
    margin-top: 0;
    margin-bottom: 0;
  }
  
  .modal-header {
    padding: clamp(14px, 2.5vh, 18px);
  }
  
  .modal-body {
    padding: clamp(14px, 2.5vh, 18px);
  }
  
  .detail-section {
    margin-bottom: clamp(1rem, 2vh, 1.2rem);
  }
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

.detail-section {
  margin-bottom: 2rem;
}

.detail-section h4 {
  margin: 0 0 1rem 0;
  color: #fff;
  font-size: 1.1rem;
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 1rem;
}

.detail-item {
  display: flex;
  gap: 0.5rem;
  padding: 0.8rem;
  border-radius: 8px;
  background: rgba(15, 23, 42, 0.6);
}

.detail-item .label {
  color: rgba(248, 250, 252, 0.6);
  min-width: 100px;
}

.detail-item .value {
  color: rgba(248, 250, 252, 0.9);
  font-weight: 500;
}

.fee-breakdown {
  display: flex;
  flex-direction: column;
  gap: 0.8rem;
  padding: 1rem;
  border-radius: 12px;
  background: rgba(15, 23, 42, 0.6);
}

.fee-item {
  display: flex;
  justify-content: space-between;
  padding: 0.6rem 0;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  color: rgba(248, 250, 252, 0.8);
}

.fee-item.total {
  border-bottom: none;
  border-top: 2px solid rgba(255, 255, 255, 0.2);
  padding-top: 1rem;
  font-size: 1.2rem;
  font-weight: 700;
  color: #fcd34d;
}

.reason-text {
  padding: 1rem;
  border-radius: 8px;
  background: rgba(15, 23, 42, 0.6);
  color: rgba(248, 250, 252, 0.8);
  line-height: 1.6;
  margin: 0;
}

.modal-footer {
  padding: 24px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  display: flex;
  gap: 1rem;
  justify-content: flex-end;
}

/* 搜索区域 */
.search-section {
  margin-bottom: 1.5rem;
}

.search-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 1rem;
  flex-wrap: wrap;
}

.search-input-wrapper {
  position: relative;
  flex: 1;
  min-width: 300px;
}

.search-input {
  width: 100%;
  padding: 0.75rem 1rem 0.75rem 2.5rem;
  border: 1px solid rgba(255, 255, 255, 0.15);
  border-radius: 12px;
  background: rgba(15, 23, 42, 0.6);
  color: #fff;
  font-size: 0.9rem;
  transition: all 0.3s;
}

.search-input:focus {
  outline: none;
  border-color: rgba(99, 102, 241, 0.5);
  background: rgba(15, 23, 42, 0.8);
}

.search-icon {
  position: absolute;
  left: 0.75rem;
  top: 50%;
  transform: translateY(-50%);
  font-size: 1.1rem;
  color: rgba(255, 255, 255, 0.6);
}

.batch-actions {
  display: flex;
  align-items: center;
  gap: 1rem;
  flex-wrap: wrap;
}

.selected-count {
  color: rgba(255, 255, 255, 0.8);
  font-size: 0.9rem;
}

.batch-btn {
  padding: 0.6rem 1.2rem;
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 8px;
  background: rgba(15, 23, 42, 0.6);
  color: #fff;
  font-size: 0.9rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
}

.batch-btn.approve-btn {
  background: rgba(34, 197, 94, 0.2);
  border-color: rgba(34, 197, 94, 0.4);
  color: #4ade80;
}

.batch-btn.approve-btn:hover {
  background: rgba(34, 197, 94, 0.3);
}

.batch-btn.reject-btn {
  background: rgba(248, 113, 113, 0.2);
  border-color: rgba(248, 113, 113, 0.4);
  color: #f87171;
}

.batch-btn.reject-btn:hover {
  background: rgba(248, 113, 113, 0.3);
}

/* 复选框 */
.app-checkbox {
  margin-right: 1rem;
}

.app-checkbox input[type="checkbox"] {
  width: 18px;
  height: 18px;
  cursor: pointer;
  accent-color: #1E8AE6;
}

.application-item.selected {
  border-color: rgba(99, 102, 241, 0.5);
  background: rgba(99, 102, 241, 0.1);
}

.application-item {
  display: flex;
  align-items: flex-start;
}

/* 加载和空状态 */
.loading-state,
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 4rem 2rem;
  color: rgba(255, 255, 255, 0.7);
}

.loading-spinner {
  width: 48px;
  height: 48px;
  border: 4px solid rgba(255, 255, 255, 0.1);
  border-top-color: #1E8AE6;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 1rem;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.empty-icon {
  font-size: 4rem;
  margin-bottom: 1rem;
  opacity: 0.5;
}

/* 筛选模态框 */
.filter-modal {
  max-width: 600px;
}

.filter-form {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.filter-row {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.filter-row label {
  color: rgba(255, 255, 255, 0.8);
  font-size: 0.9rem;
  font-weight: 500;
}

.filter-row select,
.filter-row input {
  padding: 0.75rem;
  border: 1px solid rgba(255, 255, 255, 0.15);
  border-radius: 8px;
  background: rgba(15, 23, 42, 0.6);
  color: #fff;
  font-size: 0.9rem;
}

.filter-row select:focus,
.filter-row input:focus {
  outline: none;
  border-color: rgba(99, 102, 241, 0.5);
}

.date-range,
.amount-range {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.date-range input,
.amount-range input {
  flex: 1;
}

.date-range span,
.amount-range span {
  color: rgba(255, 255, 255, 0.6);
  font-size: 0.9rem;
}

/* 拒绝模态框 */
.reject-modal {
  max-width: 500px;
}

.reject-prompt {
  color: rgba(255, 255, 255, 0.8);
  margin-bottom: 1rem;
  font-size: 0.95rem;
}

.reject-textarea {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid rgba(255, 255, 255, 0.15);
  border-radius: 8px;
  background: rgba(15, 23, 42, 0.6);
  color: #fff;
  font-size: 0.9rem;
  font-family: inherit;
  resize: vertical;
  min-height: 100px;
}

.reject-textarea:focus {
  outline: none;
  border-color: rgba(248, 113, 113, 0.5);
}

.error-text {
  color: #f87171;
  font-size: 0.85rem;
  margin-top: 0.5rem;
}

.reject-confirm-btn {
  background: linear-gradient(135deg, #f87171, #ef4444);
  box-shadow: 0 10px 25px rgba(248, 113, 113, 0.35);
}

.reject-confirm-btn:hover {
  box-shadow: 0 15px 30px rgba(248, 113, 113, 0.5);
}

/* 响应式 */
@media (max-width: 768px) {
  .search-row {
    flex-direction: column;
    align-items: stretch;
  }

  .batch-actions {
    width: 100%;
    justify-content: flex-start;
  }

  .application-item {
    flex-direction: column;
  }

  .app-checkbox {
    margin-bottom: 0.5rem;
  }
}
</style>


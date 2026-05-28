<template>
  <AdminLayout>
    <div class="page-container baggage-management-page">
      <div class="breadcrumb">
        <span>航空运营</span>
        <span class="breadcrumb-separator">/</span>
        <span>行李管理</span>
      </div>

      <header class="page-header">
        <div>
          <div class="header-top">
            <button class="back-btn" @click="handleBack">
              <span class="back-icon">←</span>
              <span>返回运营控制台</span>
            </button>
          </div>
          <p class="page-label">运行控制 · 行李管理</p>
          <h1>行李管理</h1>
          <p>管理所有乘客的行李信息，处理行李托运、到达、提取等流程</p>
        </div>
        <div class="page-actions">
          <button class="ghost-btn" @click="loadBaggageList" :disabled="loading">
            {{ loading ? '刷新中...' : '刷新' }}
          </button>
        </div>
      </header>

      <!-- 搜索和筛选栏 -->
      <section class="search-section glass-card">
        <div class="search-row">
          <div class="search-input-wrapper">
            <input
              type="text"
              v-model="searchKeyword"
              placeholder="搜索行李编号、订单号、航班号、乘客姓名..."
              class="search-input"
              @input="handleSearch"
            />
            <span class="search-icon">🔍</span>
          </div>
          <div class="filter-group">
            <select v-model="statusFilter" @change="loadBaggageList" class="filter-select">
              <option value="">全部状态</option>
              <option value="registered">已登记</option>
              <option value="checked_in">已托运</option>
              <option value="in_transit">运输中</option>
              <option value="arrived">已到达</option>
              <option value="delivered">已提取</option>
              <option value="lost">丢失</option>
              <option value="damaged">损坏</option>
              <option value="delayed">延误</option>
            </select>
          </div>
        </div>
      </section>

      <!-- 统计卡片 -->
      <section class="stats-grid">
        <div class="stat-card">
          <div class="stat-icon">🧳</div>
          <div class="stat-content">
            <p class="stat-label">总行李数</p>
            <p class="stat-value">{{ stats.total }}</p>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon">📦</div>
          <div class="stat-content">
            <p class="stat-label">待处理</p>
            <p class="stat-value">{{ stats.pending }}</p>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon">✅</div>
          <div class="stat-content">
            <p class="stat-label">已提取</p>
            <p class="stat-value">{{ stats.delivered }}</p>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon">⚠️</div>
          <div class="stat-content">
            <p class="stat-label">异常行李</p>
            <p class="stat-value">{{ stats.abnormal }}</p>
          </div>
        </div>
      </section>

      <!-- 行李列表 -->
      <section class="glass-card baggage-list-section">
        <div class="section-head">
          <div>
            <p class="section-label">行李列表</p>
            <h2>所有行李 ({{ total }})</h2>
          </div>
          <div class="table-controls">
            <div class="pagination-controls">
              <label class="page-size-label">每页</label>
              <select v-model.number="pageSize" @change="changePage(0)" class="page-size-select">
                <option :value="10">10</option>
                <option :value="20">20</option>
                <option :value="50">50</option>
                <option :value="100">100</option>
              </select>
              <span class="page-info-inline">第 {{ currentPage + 1 }} / {{ totalPages }} 页</span>
              <input type="number" v-model.number="pageInput" min="1" :max="totalPages" class="page-input" />
              <button class="ghost-btn" @click="jumpToPage">跳转</button>
            </div>
          </div>
        </div>

        <div v-if="loading" class="loading-state">加载中...</div>
        <div v-else-if="baggageList.length === 0" class="empty-state">
          <p>暂无行李记录</p>
        </div>
        <div v-else class="baggage-table-wrapper">
          <table class="baggage-table">
            <thead>
              <tr>
                <th>行李编号</th>
                <th>订单号</th>
                <th>乘客姓名</th>
                <th>航班号</th>
                <th>航线</th>
                <th>行李类型</th>
                <th>数量</th>
                <th>重量(kg)</th>
                <th>状态</th>
                <th>登记时间</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="baggage in baggageList" :key="baggage.id">
                <td>{{ baggage.baggageNo }}</td>
                <td>{{ baggage.orderno }}</td>
                <td>{{ baggage.passengerName || '-' }}</td>
                <td>{{ baggage.flightNo }}</td>
                <td>{{ baggage.route }}</td>
                <td>{{ baggage.baggageType }}</td>
                <td>{{ baggage.baggageCount }}</td>
                <td>{{ baggage.totalWeight || '-' }}</td>
                <td>
                  <span class="status-badge" :class="getStatusClass(baggage.status)">
                    {{ getStatusLabel(baggage.status) }}
                  </span>
                </td>
                <td>{{ formatDateTime(baggage.registeredTime) }}</td>
                <td>
                  <div class="action-buttons">
                    <button class="action-btn view-btn" @click="viewBaggageDetail(baggage)">查看</button>
                    <button class="action-btn update-btn" @click="showUpdateModal(baggage)">更新状态</button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <!-- 分页 -->
        <div v-if="totalPages > 1" class="pagination">
          <button 
            class="page-btn" 
            :disabled="currentPage === 0"
            @click="changePage(currentPage - 1)"
          >
            上一页
          </button>
          <span class="page-info">
            第 {{ currentPage + 1 }} / {{ totalPages }} 页，共 {{ total }} 条
          </span>
          <button 
            class="page-btn" 
            :disabled="currentPage >= totalPages - 1"
            @click="changePage(currentPage + 1)"
          >
            下一页
          </button>
        </div>
      </section>

      <!-- 更新状态模态框 -->
      <Teleport to="body">
        <div v-if="showUpdateStatusModal" class="modal-overlay" @click.self="closeUpdateModal">
          <div class="modal-content update-status-modal">
            <div class="modal-header">
              <h3>更新行李状态</h3>
              <button class="modal-close" @click="closeUpdateModal">×</button>
            </div>
            <div v-if="selectedBaggage" class="modal-body">
              <div class="form-group">
                <label>行李编号</label>
                <input type="text" :value="selectedBaggage.baggageNo" disabled class="disabled-input" />
              </div>
              <div class="form-group">
                <label>当前状态</label>
                <input type="text" :value="getStatusLabel(selectedBaggage.status)" disabled class="disabled-input" />
              </div>
              <div class="form-group">
                <label>新状态 <span class="required">*</span></label>
                <select v-model="updateForm.status" class="form-select">
                  <option value="registered">已登记</option>
                  <option value="checked_in">已托运</option>
                  <option value="in_transit">运输中</option>
                  <option value="arrived">已到达</option>
                  <option value="delivered">已提取</option>
                  <option value="lost">丢失</option>
                  <option value="damaged">损坏</option>
                  <option value="delayed">延误</option>
                </select>
              </div>
              <div class="form-group">
                <label>运营备注</label>
                <textarea
                  v-model="updateForm.operatorRemark"
                  rows="3"
                  placeholder="请输入运营备注信息"
                  class="form-textarea"
                ></textarea>
              </div>
              <div v-if="updateError" class="form-error">{{ updateError }}</div>
            </div>
            <div class="modal-footer">
              <button class="ghost-btn" @click="closeUpdateModal">取消</button>
              <button class="primary-btn" @click="submitUpdateStatus" :disabled="updating">
                {{ updating ? '更新中...' : '确认更新' }}
              </button>
            </div>
          </div>
        </div>
      </Teleport>

      <!-- 行李详情模态框 -->
      <Teleport to="body">
        <div v-if="showDetailModal" class="modal-overlay" @click.self="closeDetailModal">
          <div class="modal-content baggage-detail-modal">
            <div class="modal-header">
              <h3>行李详情</h3>
              <button class="modal-close" @click="closeDetailModal">×</button>
            </div>
            <div v-if="selectedBaggage" class="modal-body">
              <div class="detail-section">
                <h4>基本信息</h4>
                <div class="detail-grid">
                  <div class="detail-item">
                    <span class="detail-label">行李编号：</span>
                    <span class="detail-value">{{ selectedBaggage.baggageNo }}</span>
                  </div>
                  <div class="detail-item">
                    <span class="detail-label">订单号：</span>
                    <span class="detail-value">{{ selectedBaggage.orderno }}</span>
                  </div>
                  <div class="detail-item">
                    <span class="detail-label">乘客ID：</span>
                    <span class="detail-value">{{ selectedBaggage.passengerId }}</span>
                  </div>
                  <div class="detail-item">
                    <span class="detail-label">乘客姓名：</span>
                    <span class="detail-value">{{ selectedBaggage.passengerName || '-' }}</span>
                  </div>
                  <div class="detail-item">
                    <span class="detail-label">航班号：</span>
                    <span class="detail-value">{{ selectedBaggage.flightNo }}</span>
                  </div>
                  <div class="detail-item">
                    <span class="detail-label">航线：</span>
                    <span class="detail-value">{{ selectedBaggage.route }}</span>
                  </div>
                  <div class="detail-item">
                    <span class="detail-label">起飞时间：</span>
                    <span class="detail-value">{{ formatDateTime(selectedBaggage.departureTime) }}</span>
                  </div>
                  <div class="detail-item">
                    <span class="detail-label">行李类型：</span>
                    <span class="detail-value">{{ selectedBaggage.baggageType }}</span>
                  </div>
                  <div class="detail-item">
                    <span class="detail-label">行李数量：</span>
                    <span class="detail-value">{{ selectedBaggage.baggageCount }}件</span>
                  </div>
                  <div class="detail-item">
                    <span class="detail-label">总重量：</span>
                    <span class="detail-value">{{ selectedBaggage.totalWeight || '未填写' }}公斤</span>
                  </div>
                  <div class="detail-item">
                    <span class="detail-label">尺寸：</span>
                    <span class="detail-value">{{ selectedBaggage.dimensions || '未填写' }}</span>
                  </div>
                  <div class="detail-item">
                    <span class="detail-label">状态：</span>
                    <span class="detail-value" :class="getStatusClass(selectedBaggage.status)">
                      {{ getStatusLabel(selectedBaggage.status) }}
                    </span>
                  </div>
                </div>
              </div>

              <div class="detail-section">
                <h4>时间信息</h4>
                <div class="detail-grid">
                  <div class="detail-item">
                    <span class="detail-label">登记时间：</span>
                    <span class="detail-value">{{ formatDateTime(selectedBaggage.registeredTime) }}</span>
                  </div>
                  <div v-if="selectedBaggage.checkedInTime" class="detail-item">
                    <span class="detail-label">托运时间：</span>
                    <span class="detail-value">{{ formatDateTime(selectedBaggage.checkedInTime) }}</span>
                  </div>
                  <div v-if="selectedBaggage.arrivalTimeBaggage" class="detail-item">
                    <span class="detail-label">行李到达时间：</span>
                    <span class="detail-value">{{ formatDateTime(selectedBaggage.arrivalTimeBaggage) }}</span>
                  </div>
                  <div v-if="selectedBaggage.deliveredTime" class="detail-item">
                    <span class="detail-label">提取时间：</span>
                    <span class="detail-value">{{ formatDateTime(selectedBaggage.deliveredTime) }}</span>
                  </div>
                  <div v-if="selectedBaggage.processedAt" class="detail-item">
                    <span class="detail-label">处理时间：</span>
                    <span class="detail-value">{{ formatDateTime(selectedBaggage.processedAt) }}</span>
                  </div>
                </div>
              </div>

              <div v-if="selectedBaggage.description || selectedBaggage.remark || selectedBaggage.operatorRemark" class="detail-section">
                <h4>备注信息</h4>
                <div class="detail-text">
                  <div v-if="selectedBaggage.description">
                    <strong>行李描述：</strong>{{ selectedBaggage.description }}
                  </div>
                  <div v-if="selectedBaggage.remark">
                    <strong>备注：</strong>{{ selectedBaggage.remark }}
                  </div>
                  <div v-if="selectedBaggage.operatorRemark">
                    <strong>运营备注：</strong>{{ selectedBaggage.operatorRemark }}
                  </div>
                </div>
              </div>

              <div v-if="selectedBaggage.baggageFee || selectedBaggage.excessFee" class="detail-section">
                <h4>费用信息</h4>
                <div class="detail-grid">
                  <div v-if="selectedBaggage.baggageFee" class="detail-item">
                    <span class="detail-label">行李费用：</span>
                    <span class="detail-value">¥{{ selectedBaggage.baggageFee }}</span>
                  </div>
                  <div v-if="selectedBaggage.excessFee" class="detail-item">
                    <span class="detail-label">超重/超规费用：</span>
                    <span class="detail-value">¥{{ selectedBaggage.excessFee }}</span>
                  </div>
                </div>
              </div>
            </div>
            <div class="modal-footer">
              <button class="ghost-btn" @click="closeDetailModal">关闭</button>
            </div>
          </div>
        </div>
      </Teleport>
    </div>
  </AdminLayout>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import AdminLayout from '../../components/AdminLayout.vue'
import { baggageApi } from '../../services/api'
import { handleApiError } from '../../utils/navigation'

const router = useRouter()

// 页面状态
const loading = ref(false)
const updating = ref(false)
const showUpdateStatusModal = ref(false)
const showDetailModal = ref(false)
const searchKeyword = ref('')
const statusFilter = ref('')

// 分页
const currentPage = ref(0)
const pageSize = ref(20)
const total = ref(0)
const totalPages = computed(() => Math.ceil(total.value / pageSize.value))

// 行李列表
const baggageList = ref<any[]>([])
const selectedBaggage = ref<any>(null)

// 更新表单
const updateForm = reactive({
  status: '',
  operatorRemark: ''
})
const updateError = ref('')

// 统计数据
const stats = reactive({
  total: 0,
  pending: 0,
  delivered: 0,
  abnormal: 0
})

// 返回上一页
const handleBack = () => {
  router.push('/portal/operations')
}

// 搜索处理
const handleSearch = () => {
  currentPage.value = 0
  loadBaggageList()
}

// 加载行李列表
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
    
    if (searchKeyword.value) {
      const keyword = searchKeyword.value.trim()
      // 根据搜索关键词判断搜索类型，优先匹配更具体的格式
      if (keyword.startsWith('BG') || keyword.match(/^BG\d+/i)) {
        // 行李编号格式：BG开头
        params.baggageNo = keyword.toUpperCase()
      } else if (keyword.startsWith('ORD') || keyword.match(/^ORD\d+/i)) {
        // 订单号格式：ORD开头
        params.orderno = keyword.toUpperCase()
      } else if (keyword.match(/^[A-Z]{2}\d+/i)) {
        // 航班号格式：如CA1234, MU5678
        params.flightNo = keyword.toUpperCase()
      } else {
        // 默认按乘客姓名搜索
        params.passengerName = keyword
      }
    }
    
    const result = await baggageApi.getOperatorBaggageList(params)
    baggageList.value = result.list || []
    total.value = result.total || 0
    
    // 更新统计数据
    updateStats()
    // 同步页码输入显示
    pageInput.value = currentPage.value + 1
  } catch (error) {
    console.error('加载行李列表失败:', error)
    handleApiError(error, '加载行李列表失败')
    baggageList.value = []
  } finally {
    loading.value = false
  }
}

// 更新统计数据
const updateStats = async () => {
  try {
    // 分别查询不同状态的统计数据，避免加载过多数据
    const [
      allResult, 
      registeredResult, 
      checkedInResult, 
      deliveredResult, 
      lostResult, 
      damagedResult, 
      delayedResult
    ] = await Promise.all([
      baggageApi.getOperatorBaggageList({ page: 0, size: 1 }), // 只获取总数
      baggageApi.getOperatorBaggageList({ page: 0, size: 1, status: 'registered' }),
      baggageApi.getOperatorBaggageList({ page: 0, size: 1, status: 'checked_in' }),
      baggageApi.getOperatorBaggageList({ page: 0, size: 1, status: 'delivered' }),
      baggageApi.getOperatorBaggageList({ page: 0, size: 1, status: 'lost' }),
      baggageApi.getOperatorBaggageList({ page: 0, size: 1, status: 'damaged' }),
      baggageApi.getOperatorBaggageList({ page: 0, size: 1, status: 'delayed' })
    ])
    
    stats.total = allResult.total || 0
    stats.pending = (registeredResult.total || 0) + (checkedInResult.total || 0)
    stats.delivered = deliveredResult.total || 0
    stats.abnormal = (lostResult.total || 0) + (damagedResult.total || 0) + (delayedResult.total || 0)
  } catch (error) {
    console.warn('更新统计数据失败:', error)
    // 如果统计失败，尝试从当前列表计算
    stats.total = total.value
    stats.pending = baggageList.value.filter((b: any) => 
      b.status === 'registered' || b.status === 'checked_in'
    ).length
    stats.delivered = baggageList.value.filter((b: any) => b.status === 'delivered').length
    stats.abnormal = baggageList.value.filter((b: any) => 
      b.status === 'lost' || b.status === 'damaged' || b.status === 'delayed'
    ).length
  }
}

// 查看行李详情
const viewBaggageDetail = async (baggage: any) => {
  try {
    const detail = await baggageApi.getOperatorBaggageDetail(baggage.id)
    selectedBaggage.value = detail
    showDetailModal.value = true
  } catch (error) {
    handleApiError(error, '获取行李详情失败')
  }
}

// 关闭详情模态框
const closeDetailModal = () => {
  showDetailModal.value = false
  selectedBaggage.value = null
}

// 显示更新状态模态框
const showUpdateModal = (baggage: any) => {
  selectedBaggage.value = baggage
  updateForm.status = baggage.status
  updateForm.operatorRemark = baggage.operatorRemark || ''
  updateError.value = ''
  showUpdateStatusModal.value = true
}

// 关闭更新模态框
const closeUpdateModal = () => {
  showUpdateStatusModal.value = false
  selectedBaggage.value = null
  updateForm.status = ''
  updateForm.operatorRemark = ''
  updateError.value = ''
}

// 提交更新状态
const submitUpdateStatus = async () => {
  if (!updateForm.status) {
    updateError.value = '请选择新状态'
    return
  }
  
  if (!selectedBaggage.value) {
    return
  }
  
  updating.value = true
  updateError.value = ''
  
  try {
    await baggageApi.updateBaggageStatus(
      selectedBaggage.value.id,
      updateForm.status,
      updateForm.operatorRemark || undefined
    )
    
    closeUpdateModal()
    await loadBaggageList()
    await updateStats()
  } catch (error: any) {
    updateError.value = error.message || '更新失败，请重试'
    handleApiError(error, '更新行李状态失败')
  } finally {
    updating.value = false
  }
}

// 切换页码
const changePage = (page: number) => {
  currentPage.value = page
  pageInput.value = page + 1
  loadBaggageList()
}

// 页码输入与跳转
const pageInput = ref(1)
const jumpToPage = () => {
  const desired = Number(pageInput.value) || 1
  const target = Math.min(Math.max(desired, 1), Math.max(1, totalPages.value)) - 1
  changePage(target)
}

// 保证在加载列表后同步 pageInput（在 loadBaggageList 中更新 currentPage 会触发）
// 修改 loadBaggageList 在成功后同步 pageInput 为当前页 +1

// 获取状态标签
const getStatusLabel = (status: string) => {
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
  return statusMap[status] || status
}

// 获取状态样式类
const getStatusClass = (status: string) => {
  const classMap: Record<string, string> = {
    registered: 'status-registered',
    checked_in: 'status-checked-in',
    in_transit: 'status-in-transit',
    arrived: 'status-arrived',
    delivered: 'status-delivered',
    lost: 'status-lost',
    damaged: 'status-damaged',
    delayed: 'status-delayed'
  }
  return classMap[status] || ''
}

// 格式化日期时间
const formatDateTime = (dateTime: string | null) => {
  if (!dateTime) return '-'
  try {
    const date = new Date(dateTime)
    return date.toLocaleString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    })
  } catch {
    return dateTime
  }
}

// 初始化
onMounted(() => {
  loadBaggageList()
  updateStats()
})
</script>

<style scoped>
.baggage-management-page {
  padding: 20px;
  max-width: 1600px;
  margin: 0 auto;
}

.breadcrumb {
  margin-bottom: 20px;
  font-size: 14px;
  color: rgba(255, 255, 255, 0.7);
}

.breadcrumb-separator {
  margin: 0 8px;
  color: #999;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.header-top {
  margin-bottom: 12px;
}

.back-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  background: transparent;
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 8px;
  color: rgba(255, 255, 255, 0.8);
  cursor: pointer;
  transition: all 0.3s;
}

.back-btn:hover {
  background: rgba(255, 255, 255, 0.1);
  border-color: rgba(255, 255, 255, 0.3);
}

.back-icon {
  font-size: 18px;
}

.page-label {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.6);
  margin: 0 0 8px 0;
  text-transform: uppercase;
  letter-spacing: 0.1em;
}

.page-header h1 {
  margin: 0 0 8px 0;
  font-size: 32px;
  font-weight: 700;
  color: #fff;
}

.page-header p {
  margin: 0;
  color: rgba(255, 255, 255, 0.7);
  font-size: 14px;
}

.page-actions {
  display: flex;
  gap: 12px;
}

.primary-btn {
  padding: 10px 24px;
  background: linear-gradient(120deg, #1E8AE6, #0A2F63);
  color: #fff;
  border: none;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
}

.primary-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(30, 138, 230, 0.4);
}

.primary-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}

.ghost-btn {
  padding: 10px 24px;
  background: transparent;
  border: 1px solid rgba(255, 255, 255, 0.3);
  border-radius: 8px;
  color: rgba(255, 255, 255, 0.85);
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
}

.ghost-btn:hover:not(:disabled) {
  background: rgba(255, 255, 255, 0.1);
  border-color: rgba(255, 255, 255, 0.5);
}

.glass-card {
  background: rgba(15, 23, 42, 0.6);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 16px;
  padding: 24px;
  margin-bottom: 24px;
}

.search-section {
  margin-bottom: 24px;
}

.search-row {
  display: flex;
  gap: 16px;
  align-items: center;
}

.search-input-wrapper {
  flex: 1;
  position: relative;
}

.search-input {
  width: 100%;
  padding: 12px 16px 12px 44px;
  background: rgba(2, 6, 23, 0.4);
  border: 1px solid rgba(255, 255, 255, 0.16);
  border-radius: 8px;
  color: #fff;
  font-size: 14px;
}

.search-input:focus {
  outline: none;
  border-color: #1E8AE6;
  box-shadow: 0 0 0 2px rgba(30, 138, 230, 0.2);
}

.search-icon {
  position: absolute;
  left: 16px;
  top: 50%;
  transform: translateY(-50%);
  font-size: 16px;
}

.filter-group {
  display: flex;
  gap: 12px;
}

.filter-select {
  padding: 12px 16px;
  background: rgba(2, 6, 23, 0.4);
  border: 1px solid rgba(255, 255, 255, 0.16);
  border-radius: 8px;
  color: #fff;
  font-size: 14px;
  cursor: pointer;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
  margin-bottom: 24px;
}

.stat-card {
  background: rgba(15, 23, 42, 0.6);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-icon {
  font-size: 32px;
}

.stat-content {
  flex: 1;
}

.stat-label {
  margin: 0 0 8px 0;
  font-size: 13px;
  color: rgba(255, 255, 255, 0.6);
}

.stat-value {
  margin: 0;
  font-size: 24px;
  font-weight: 700;
  color: #fff;
}

.section-head {
  margin-bottom: 20px;
}

.section-label {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.6);
  margin: 0 0 4px 0;
  text-transform: uppercase;
  letter-spacing: 0.1em;
}

.section-head h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: #fff;
}

.loading-state,
.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: rgba(255, 255, 255, 0.7);
}

.baggage-table-wrapper {
  overflow-x: auto;
}

.baggage-table {
  width: 100%;
  border-collapse: collapse;
}

.baggage-table thead {
  background: rgba(174, 180, 206, 0.4);
}

.baggage-table th {
  padding: 12px 16px;
  text-align: left;
  font-size: 13px;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.8);
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.baggage-table td {
  padding: 12px 16px;
  font-size: 14px;
  color: rgba(255, 255, 255, 0.9);
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
}

.baggage-table tbody tr:hover {
  background: rgba(30, 138, 230, 0.05);
}

.status-badge {
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
}

.status-registered {
  background: rgba(8, 95, 234, 0.2);
  color: #60a5fa;
}

.status-checked-in {
  background: rgba(34, 197, 94, 0.2);
  color: #4ade80;
}

.status-in-transit {
  background: rgba(251, 191, 36, 0.2);
  color: #fbbf24;
}

.status-arrived {
  background: rgba(34, 197, 94, 0.2);
  color: #4ade80;
}

.status-delivered {
  background: rgba(34, 197, 94, 0.2);
  color: #4ade80;
}

.status-lost,
.status-damaged {
  background: rgba(239, 68, 68, 0.2);
  color: #f87171;
}

.status-delayed {
  background: rgba(251, 191, 36, 0.2);
  color: #fbbf24;
}

.action-buttons {
  display: flex;
  gap: 8px;
}

.action-btn {
  padding: 6px 12px;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 500;
  cursor: pointer;
  border: none;
  transition: all 0.2s;
}

.view-btn {
  background: rgba(30, 138, 230, 0.2);
  color: #1E8AE6;
  border: 1px solid rgba(30, 138, 230, 0.4);
}

.view-btn:hover {
  background: rgba(30, 138, 230, 0.3);
}

.update-btn {
  background: rgba(34, 197, 94, 0.2);
  color: #4ade80;
  border: 1px solid rgba(34, 197, 94, 0.4);
}

.update-btn:hover {
  background: rgba(34, 197, 94, 0.3);
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 16px;
  margin-top: 24px;
  padding-top: 24px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

.page-btn {
  padding: 8px 16px;
  background: rgba(2, 6, 23, 0.4);
  border: 1px solid rgba(255, 255, 255, 0.16);
  border-radius: 8px;
  color: #fff;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
}

.page-btn:hover:not(:disabled) {
  background: rgba(30, 138, 230, 0.2);
  border-color: rgba(30, 138, 230, 0.4);
}

.page-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.page-info {
  color: rgba(255, 255, 255, 0.7);
  font-size: 14px;
}

.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.75);
  backdrop-filter: blur(8px);
  z-index: 9999;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

/* Table control / pagination top */
.table-controls {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 12px;
  margin-top: -8px;
}
.pagination-controls {
  display: flex;
  align-items: center;
  gap: 8px;
}
.page-size-label {
  color: rgba(255,255,255,0.75);
  font-size: 13px;
  margin-right: 4px;
}
.page-size-select {
  padding: 6px 8px;
  border-radius: 6px;
  background: rgba(2,6,23,0.5);
  border: 1px solid rgba(255,255,255,0.12);
  color: #fff;
}
.page-info-inline {
  color: rgba(255,255,255,0.8);
  font-size: 13px;
  margin-left: 8px;
}
.page-input {
  width: 70px;
  padding: 6px 8px;
  border-radius: 6px;
  border: 1px solid rgba(255,255,255,0.12);
  background: rgba(2,6,23,0.5);
  color: #fff;
}

.modal-content {
  background: rgba(15, 23, 42, 0.95);
  border: 1px solid rgba(148, 163, 184, 0.2);
  border-radius: 24px;
  width: 100%;
  max-width: 600px;
  max-height: 90vh;
  display: flex;
  flex-direction: column;
  box-shadow: 0 25px 60px rgba(0, 0, 0, 0.9);
  backdrop-filter: blur(20px);
  animation: slideUp 0.3s ease;
}

@keyframes slideUp {
  from {
    transform: translateY(20px);
    opacity: 0;
  }
  to {
    transform: translateY(0);
    opacity: 1;
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
  font-size: 24px;
  font-weight: 600;
  color: #fff;
}

.modal-close {
  background: transparent;
  border: none;
  color: rgba(255, 255, 255, 0.7);
  font-size: 32px;
  line-height: 1;
  cursor: pointer;
  padding: 0;
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  transition: all 0.2s;
}

.modal-close:hover {
  background: rgba(255, 255, 255, 0.1);
  color: #fff;
}

.modal-body {
  padding: 24px;
  overflow-y: auto;
  flex: 1;
}

.modal-footer {
  padding: 24px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  font-size: 14px;
  font-weight: 500;
  color: #fff;
}

.required {
  color: #f87171;
  margin-left: 2px;
}

.disabled-input,
.form-select,
.form-textarea {
  width: 100%;
  padding: 12px 16px;
  background: rgba(2, 6, 23, 0.4);
  border: 1px solid rgba(255, 255, 255, 0.16);
  border-radius: 8px;
  color: #fff;
  font-size: 14px;
  font-family: inherit;
}

.disabled-input {
  opacity: 0.6;
  cursor: not-allowed;
}

.form-select:focus,
.form-textarea:focus {
  outline: none;
  border-color: #1E8AE6;
  box-shadow: 0 0 0 2px rgba(30, 138, 230, 0.2);
}

.form-textarea {
  resize: vertical;
  min-height: 80px;
}

.form-error {
  color: #f87171;
  font-size: 13px;
  margin-top: -16px;
  margin-bottom: 16px;
}

.baggage-detail-modal {
  max-width: 800px;
}

.detail-section {
  margin-bottom: 24px;
}

.detail-section h4 {
  margin: 0 0 16px 0;
  font-size: 18px;
  font-weight: 600;
  color: #fff;
  padding-bottom: 8px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.detail-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.detail-label {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.6);
}

.detail-value {
  font-size: 15px;
  color: #fff;
  font-weight: 500;
}

.detail-text {
  color: rgba(255, 255, 255, 0.9);
  font-size: 14px;
  line-height: 1.6;
}

.detail-text div {
  margin-bottom: 8px;
}

.detail-text strong {
  color: #fff;
  margin-right: 8px;
}

@media (max-width: 768px) {
  .baggage-table-wrapper {
    overflow-x: scroll;
  }

  .detail-grid {
    grid-template-columns: 1fr;
  }

  .page-header {
    flex-direction: column;
    gap: 16px;
  }
}
</style>


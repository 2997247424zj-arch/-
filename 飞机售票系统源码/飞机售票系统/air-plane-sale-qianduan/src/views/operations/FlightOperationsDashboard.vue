<template>
  <AdminLayout>
    <div class="page-container operations-dashboard">
      <div class="breadcrumb">
        <span>航空运营</span>
        <span class="breadcrumb-separator">/</span>
        <span>航班运营看板</span>
      </div>

      <header class="page-header">
        <div>
          <div class="header-top">
            <button class="back-btn" @click="handleBack">
              <span class="back-icon">←</span>
              <span>返回运营控制台</span>
            </button>
          </div>
          <p class="page-label">运营控制 · 航班管理</p>
          <h1>航班运营看板</h1>
          <p>管理航班排期、舱位库存、票价和航班状态</p>
        </div>
        <div class="page-actions">
          <button class="ghost-btn" @click="handleExport" :disabled="loading">
            {{ loading ? '导出中...' : '导出数据' }}
          </button>
          <button class="primary-btn" @click="handleRefresh" :disabled="loading">
            {{ loading ? '刷新中...' : '刷新数据' }}
          </button>
        </div>
      </header>

      <!-- 个性化控制台 -->
      <section class="glass-card personalization-panel" ref="personalizationSectionRef">
        <div class="section-head">
          <div>
            <p class="section-label">角色个性化</p>
            <h2>运营视图自定义</h2>
          </div>
          <div class="panel-actions">
            <button class="ghost-btn" @click="saveCurrentStatusAsDefault">
              将当前状态设为默认
            </button>
            <button class="ghost-btn" @click="applyDefaultFilters">
              应用默认筛选
            </button>
            <button class="ghost-btn danger" @click="resetOpsPreferences">
              重置偏好
            </button>
          </div>
        </div>
        <div class="personalization-grid">
          <div class="preference-column">
            <div class="preference-item">
              <label>默认航班状态</label>
              <select v-model="opsPreferences.defaultStatus">
                <option value="">全部状态</option>
                <option value="scheduled">计划中</option>
                <option value="boarding">登机中</option>
                <option value="departed">已起飞</option>
                <option value="arrived">已到达</option>
                <option value="delayed">延误</option>
                <option value="canceled">已取消</option>
              </select>
              <p class="item-hint">选择后将自动应用到搜索条件</p>
            </div>
            <div class="preference-item toggle">
              <label>自动刷新</label>
              <label class="switch">
                <input type="checkbox" v-model="opsPreferences.autoRefresh" />
                <span class="slider"></span>
              </label>
            </div>
            <div class="preference-item" v-if="opsPreferences.autoRefresh">
              <label>刷新间隔（秒）</label>
              <input
                type="number"
                min="10"
                max="300"
                step="10"
                v-model.number="opsPreferences.refreshInterval"
              />
            </div>
            <div class="preference-item toggles">
              <label>信息密度</label>
              <label class="checkbox">
                <input type="checkbox" v-model="opsPreferences.compactTable" />
                <span>紧凑表格模式</span>
              </label>
            </div>
            <div class="preference-item toggles">
              <label>显示内容</label>
              <label class="checkbox">
                <input type="checkbox" v-model="opsPreferences.showSeatColumns" />
                <span>显示座位余量</span>
              </label>
              <label class="checkbox">
                <input type="checkbox" v-model="opsPreferences.showPriceColumns" />
                <span>显示票价信息</span>
              </label>
            </div>
          </div>
          <div class="theme-column">
            <details open>
              <summary>界面主题与全局个性化</summary>
              <div class="theme-settings-wrapper">
                <ThemeSettings />
              </div>
            </details>
          </div>
        </div>
      </section>

      <!-- 搜索栏 -->
      <section class="glass-card search-section">
        <div class="section-head">
          <div>
            <p class="section-label">航班搜索</p>
            <h2>快速查找航班</h2>
          </div>
        </div>
        <div class="search-form">
          <div class="search-row">
            <div class="search-group">
              <label>航班号</label>
              <input
                v-model="searchParams.flightNumber"
                type="text"
                placeholder="例如：CA1234"
                @input="handleSearch"
              />
            </div>
            <div class="search-group">
              <label>出发日期</label>
              <input
                v-model="searchParams.departureDate"
                type="date"
                @change="handleSearch"
              />
            </div>
            <div class="search-group">
              <label>出发城市</label>
              <input
                v-model="searchParams.departureCity"
                type="text"
                placeholder="例如：北京"
                @input="handleSearch"
              />
            </div>
            <div class="search-group">
              <label>到达城市</label>
              <input
                v-model="searchParams.arrivalCity"
                type="text"
                placeholder="例如：上海"
                @input="handleSearch"
              />
            </div>
            <div class="search-group">
              <label>航班状态</label>
              <select v-model="searchParams.status" @change="handleSearch">
                <option value="">全部状态</option>
                <option value="scheduled">计划中</option>
                <option value="boarding">登机中</option>
                <option value="departed">已起飞</option>
                <option value="arrived">已到达</option>
                <option value="delayed">延误</option>
                <option value="canceled">已取消</option>
              </select>
            </div>
          </div>
          <div class="search-actions">
            <button class="ghost-btn" @click="resetSearch">重置</button>
            <button class="primary-btn" @click="handleSearch">搜索</button>
          </div>
        </div>
      </section>

      <!-- 数据表格 -->
      <section class="glass-card table-section">
        <div class="section-head">
          <div>
            <p class="section-label">航班列表</p>
            <h2>共 {{ filteredFlights.length }} 个航班</h2>
          </div>
        </div>

        <!-- 加载状态 -->
        <div v-if="loading" class="loading-state">
          <div class="loading-spinner"></div>
          <p>加载中...</p>
        </div>

        <!-- 空状态 -->
        <div v-else-if="filteredFlights.length === 0" class="empty-state">
          <div class="empty-icon">✈️</div>
          <p>暂无航班数据</p>
        </div>

        <!-- 表格 -->
        <div v-else class="table-wrapper">
          <table class="flights-table" :class="{ compact: opsPreferences.compactTable }">
            <thead>
              <tr>
                <th>航班号</th>
                <th>起降地</th>
                <th>起降时间</th>
                <th>状态</th>
                <template v-if="opsPreferences.showSeatColumns">
                  <th>经济舱</th>
                  <th>商务舱</th>
                  <th>头等舱</th>
                </template>
                <template v-if="opsPreferences.showPriceColumns">
                  <th>经济舱价格</th>
                  <th>商务舱价格</th>
                  <th>头等舱价格</th>
                </template>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="flight in paginatedFlights" :key="flight.id" :class="getStatusClass(flight.status)">
                <td>
                  <div class="flight-number-cell">
                    <strong>{{ flight.flightNumber }}</strong>
                    <span class="airline-name">{{ flight.airline }}</span>
                  </div>
                </td>
                <td>
                  <div class="route-cell">
                    <span class="city">{{ flight.departureCity }}</span>
                    <span class="arrow">→</span>
                    <span class="city">{{ flight.arrivalCity }}</span>
                  </div>
                </td>
                <td>
                  <div class="time-cell">
                    <div class="time-item">
                      <span class="time-label">出发</span>
                      <span class="time-value">{{ flight.departureTime }}</span>
                    </div>
                    <div class="time-item">
                      <span class="time-label">到达</span>
                      <span class="time-value">{{ flight.arrivalTime }}</span>
                    </div>
                  </div>
                </td>
                <td>
                  <span :class="['status-badge', flight.status]">
                    {{ getStatusText(flight.status) }}
                  </span>
                </td>
                <template v-if="opsPreferences.showSeatColumns">
                  <td>
                    <span :class="getSeatClass(flight.seats.economy)">
                      {{ flight.seats.economy }}
                    </span>
                  </td>
                  <td>
                    <span :class="getSeatClass(flight.seats.business)">
                      {{ flight.seats.business }}
                    </span>
                  </td>
                  <td>
                    <span :class="getSeatClass(flight.seats.first)">
                      {{ flight.seats.first }}
                    </span>
                  </td>
                </template>
                <template v-if="opsPreferences.showPriceColumns">
                  <td>
                    <span class="price">¥{{ flight.prices.economy }}</span>
                  </td>
                  <td>
                    <span class="price">¥{{ flight.prices.business }}</span>
                  </td>
                  <td>
                    <span class="price">¥{{ flight.prices.first }}</span>
                  </td>
                </template>
                <td>
                  <div class="action-buttons">
                    <button
                      class="action-btn price-btn"
                      @click="handleEditPrice(flight)"
                      :disabled="flight.status === 'canceled'"
                    >
                      修改价格
                    </button>
                    <button
                      class="action-btn status-btn"
                      @click="handleUpdateStatus(flight)"
                      :disabled="flight.status === 'canceled'"
                    >
                      更新状态
                    </button>
                    <button
                      class="action-btn cancel-btn"
                      @click="handleCancelFlight(flight)"
                      :disabled="flight.status === 'canceled' || flight.status === 'arrived'"
                    >
                      取消航班
                    </button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <!-- 分页 -->
        <div v-if="filteredFlights.length > 0" class="pagination">
          <div class="pagination-info">
            共 {{ filteredFlights.length }} 条，每页 {{ pageSize }} 条
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

      <!-- 修改价格模态框 -->
      <div v-if="showPriceModal && selectedFlight" class="modal-overlay" @click="showPriceModal = false">
        <div class="modal-content price-modal" @click.stop>
          <div class="modal-header">
            <h3>修改价格 - {{ selectedFlight.flightNumber }}</h3>
            <button class="close-btn" @click="showPriceModal = false">×</button>
          </div>
          <div class="modal-body">
            <div class="price-form">
              <div class="form-group">
                <label>经济舱价格 (¥)</label>
                <input
                  v-model.number="priceForm.economy"
                  type="number"
                  min="0"
                  step="10"
                  placeholder="请输入价格"
                />
              </div>
              <div class="form-group">
                <label>商务舱价格 (¥)</label>
                <input
                  v-model.number="priceForm.business"
                  type="number"
                  min="0"
                  step="10"
                  placeholder="请输入价格"
                />
              </div>
              <div class="form-group">
                <label>头等舱价格 (¥)</label>
                <input
                  v-model.number="priceForm.first"
                  type="number"
                  min="0"
                  step="10"
                  placeholder="请输入价格"
                />
              </div>
            </div>
          </div>
          <div class="modal-footer">
            <button class="ghost-btn" @click="showPriceModal = false">取消</button>
            <button class="primary-btn" @click="confirmPriceUpdate">确认修改</button>
          </div>
        </div>
      </div>

      <!-- 更新状态模态框 -->
      <div v-if="showStatusModal && selectedFlight" class="modal-overlay" @click="showStatusModal = false">
        <div class="modal-content status-modal" @click.stop>
          <div class="modal-header">
            <h3>更新状态 - {{ selectedFlight.flightNumber }}</h3>
            <button class="close-btn" @click="showStatusModal = false">×</button>
          </div>
          <div class="modal-body">
            <div class="status-form">
              <div class="form-group">
                <label>新状态</label>
                <select v-model="statusForm.status">
                  <option value="scheduled">计划中</option>
                  <option value="boarding">登机中</option>
                  <option value="departed">已起飞</option>
                  <option value="arrived">已到达</option>
                  <option value="delayed">延误</option>
                  <option value="canceled">已取消</option>
                </select>
              </div>
              <div class="form-group">
                <label>备注</label>
                <textarea
                  v-model="statusForm.remark"
                  placeholder="请输入状态更新备注（可选）"
                  rows="4"
                ></textarea>
              </div>
            </div>
          </div>
          <div class="modal-footer">
            <button class="ghost-btn" @click="showStatusModal = false">取消</button>
            <button class="primary-btn" @click="confirmStatusUpdate">确认更新</button>
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
    </div>
  </AdminLayout>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, onBeforeUnmount, watch } from 'vue'
import { useRouter } from 'vue-router'
import AdminLayout from '../../components/AdminLayout.vue'
import ModalPrompt from '../../components/ModalPrompt.vue'
import ThemeSettings from '../../components/ThemeSettings.vue'
import { useRolePersonalization } from '../../composables/useRolePersonalization'
import { flightManagementApi, operationsApi } from '../../services/api'

interface Flight {
  id: string
  flightNumber: string
  airline: string
  departureCity: string
  arrivalCity: string
  departureTime: string
  arrivalTime: string
  departureDate: string
  status: 'scheduled' | 'boarding' | 'departed' | 'arrived' | 'delayed' | 'canceled'
  seats: {
    economy: number
    business: number
    first: number
  }
  prices: {
    economy: number
    business: number
    first: number
  }
}

// 路由
const router = useRouter()

const { preferences: opsPreferences, resetPreferences: resetOpsPreferences } = useRolePersonalization('operations_dashboard', {
  defaultStatus: '',
  autoRefresh: false,
  refreshInterval: 60,
  showSeatColumns: true,
  showPriceColumns: true,
  compactTable: false
})

const personalizationSectionRef = ref<HTMLElement | null>(null)

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

// 搜索参数
const searchParams = reactive({
  flightNumber: '',
  departureDate: '',
  departureCity: '',
  arrivalCity: '',
  status: ''
})

// 航班列表（从后端加载）
const flights = ref<Flight[]>([])
const metrics = ref<any>({})

const normalizeServerFlight = (f: any): Flight => {
  return {
    id: String(f.id || f.flightId || f.flight_no || f.flightNumber || ''),
    flightNumber: f.flightNumber || f.flightNo || f.flight_no || '',
    airline: f.airline || f.company || '',
    departureCity: f.departure || f.originAirport || f.origin || f.departureCity || '',
    arrivalCity: f.destination || f.dest || f.destAirport || f.arrivalCity || '',
    departureTime: f.departureTime || f.sched_dep_time || '',
    arrivalTime: f.arrivalTime || f.sched_arr_time || '',
    departureDate: f.departureDate || (f.departureTime ? String(f.departureTime).split('T')[0] : ''),
    status: (f.status || '').toString() as Flight['status'],
    seats: {
      economy: (f.seats && f.seats.economy) || 0,
      business: (f.seats && f.seats.business) || 0,
      first: (f.seats && f.seats.first) || 0
    },
    prices: {
      economy: (f.prices && f.prices.economy) || f.price || 0,
      business: (f.prices && f.prices.business) || f.price || 0,
      first: (f.prices && f.prices.first) || f.price || 0
    }
  }
}

const loadFlights = async () => {
  loading.value = true
  try {
    const params: any = {
      page: 0,
      size: 200,
      departure: searchParams.departureCity || undefined,
      destination: searchParams.arrivalCity || undefined,
      airline: undefined,
      departureDateStart: searchParams.departureDate || undefined
    }
    if (searchParams.flightNumber) {
      // 后端可能不支持精确的 flightNumber 字段过滤，使用客户端过滤作为后备
      params.flightNumber = searchParams.flightNumber
    }
    const res: any = await flightManagementApi.getFlightList(params)
    const data = res?.data ?? res
    const list = data?.flights ?? data?.content ?? data
    if (Array.isArray(list)) {
      flights.value = list.map(normalizeServerFlight)
    } else {
      flights.value = []
    }
  } catch (error) {
    console.warn('加载航班失败', error)
    showPromptModal('错误', '加载航班数据失败', 'error')
  } finally {
    loading.value = false
  }
}

const loadMetrics = async () => {
  try {
    const resp: any = await operationsApi.getMetrics()
    const data = resp?.data ?? resp
    metrics.value = data || {}
  } catch (e) {
    console.warn('加载运营指标失败', e)
  }
}

// 筛选后的航班列表
const filteredFlights = computed(() => {
  let result = flights.value

  if (searchParams.flightNumber.trim()) {
    const keyword = searchParams.flightNumber.toLowerCase()
    result = result.filter(flight =>
      flight.flightNumber.toLowerCase().includes(keyword)
    )
  }

  if (searchParams.departureDate) {
    result = result.filter(flight => flight.departureDate === searchParams.departureDate)
  }

  if (searchParams.departureCity.trim()) {
    const keyword = searchParams.departureCity.toLowerCase()
    result = result.filter(flight =>
      flight.departureCity.toLowerCase().includes(keyword)
    )
  }

  if (searchParams.arrivalCity.trim()) {
    const keyword = searchParams.arrivalCity.toLowerCase()
    result = result.filter(flight =>
      flight.arrivalCity.toLowerCase().includes(keyword)
    )
  }

  if (searchParams.status) {
    result = result.filter(flight => flight.status === searchParams.status)
  }

  return result
})

// 分页
const currentPage = ref(1)
const pageSize = ref(10)

const totalPages = computed(() => Math.ceil(filteredFlights.value.length / pageSize.value))

const paginatedFlights = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredFlights.value.slice(start, end)
})

// 搜索处理
const handleSearch = () => {
  currentPage.value = 1
}

// 重置搜索
const resetSearch = () => {
  searchParams.flightNumber = ''
  searchParams.departureDate = ''
  searchParams.departureCity = ''
  searchParams.arrivalCity = ''
  searchParams.status = ''
  currentPage.value = 1
}

const applyDefaultFilters = () => {
  searchParams.status = opsPreferences.defaultStatus || ''
}

const saveCurrentStatusAsDefault = () => {
  opsPreferences.defaultStatus = searchParams.status
}

// 获取状态文本
const getStatusText = (status: string) => {
  const statusMap: Record<string, string> = {
    scheduled: '计划中',
    boarding: '登机中',
    departed: '已起飞',
    arrived: '已到达',
    delayed: '延误',
    canceled: '已取消'
  }
  return statusMap[status] || status
}

// 获取状态样式类
const getStatusClass = (status: string) => {
  return `status-${status}`
}

// 获取座位数量样式类
const getSeatClass = (count: number) => {
  if (count === 0) return 'seat-empty'
  if (count < 10) return 'seat-low'
  if (count < 30) return 'seat-medium'
  return 'seat-high'
}

// 分页
const goToPage = (page: number) => {
  if (page >= 1 && page <= totalPages.value) {
    currentPage.value = page
  }
}

// 修改价格
const selectedFlight = ref<Flight | null>(null)
const showPriceModal = ref(false)
const priceForm = reactive({
  economy: 0,
  business: 0,
  first: 0
})

const handleEditPrice = (flight: Flight) => {
  selectedFlight.value = flight
  priceForm.economy = flight.prices.economy
  priceForm.business = flight.prices.business
  priceForm.first = flight.prices.first
  showPriceModal.value = true
}

const confirmPriceUpdate = async () => {
  if (!selectedFlight.value) return

  try {
    // 调用后端更新价格（尽量兼容后端 updateFlight 接口）
    try {
      await flightManagementApi.updateFlight(selectedFlight.value.id, {
        prices: {
          economy: priceForm.economy,
          business: priceForm.business,
          first: priceForm.first
        }
      })
    } catch (e) {
      // 如果后端没有 prices 字段支持，尝试把基础 price 字段更新为 economy 作为降级兼容
      try {
        await flightManagementApi.updateFlight(selectedFlight.value.id, { price: priceForm.economy })
      } catch (_) { /* ignore */ }
    }

    // 更新本地数据
    selectedFlight.value.prices.economy = priceForm.economy
    selectedFlight.value.prices.business = priceForm.business
    selectedFlight.value.prices.first = priceForm.first

    showPromptModal('成功', `航班 ${selectedFlight.value.flightNumber} 的价格已更新`, 'success')
    showPriceModal.value = false
    selectedFlight.value = null
  } catch (error) {
    showPromptModal('错误', '更新价格失败', 'error')
  }
}

// 更新状态
const showStatusModal = ref(false)
const statusForm = reactive({
  status: 'scheduled' as Flight['status'],
  remark: ''
})

const handleUpdateStatus = (flight: Flight) => {
  selectedFlight.value = flight
  statusForm.status = flight.status
  statusForm.remark = ''
  showStatusModal.value = true
}

const confirmStatusUpdate = async () => {
  if (!selectedFlight.value) return

  try {
    // 调用后端更新状态
    try {
      await flightManagementApi.updateFlight(selectedFlight.value.id, {
        status: statusForm.status,
        remark: statusForm.remark || undefined
      })
    } catch (e) {
      console.warn('更新状态到后端失败，仍在本地更新以保持界面响应:', e)
    }

    // 更新本地数据
    selectedFlight.value.status = statusForm.status

    showPromptModal('成功', `航班 ${selectedFlight.value.flightNumber} 的状态已更新为：${getStatusText(statusForm.status)}`, 'success')
    showStatusModal.value = false
    selectedFlight.value = null
  } catch (error) {
    showPromptModal('错误', '更新状态失败', 'error')
  }
}

// 取消航班
const handleCancelFlight = async (flight: Flight) => {
  if (!confirm(`确定要取消航班 ${flight.flightNumber} 吗？`)) {
    return
  }

  try {
    // 尝试通知后端取消航班
    try {
      await flightManagementApi.updateFlight(flight.id, { status: 'canceled' })
    } catch (e) {
      console.warn('调用后端取消航班失败，已在前端标记:', e)
    }

    // 更新本地数据
    flight.status = 'canceled'
    flight.seats = { economy: 0, business: 0, first: 0 }

    showPromptModal('成功', `航班 ${flight.flightNumber} 已取消`, 'success')
  } catch (error) {
    showPromptModal('错误', '取消航班失败', 'error')
  }
}

// 刷新数据
const handleRefresh = async () => {
  loading.value = true
  try {
    await Promise.all([loadFlights(), loadMetrics()])
    showPromptModal('成功', '数据已刷新', 'success')
  } catch (error) {
    showPromptModal('错误', '刷新数据失败', 'error')
  } finally {
    loading.value = false
  }
}

// 导出数据
const handleExport = async () => {
  try {
    loading.value = true

    // 准备导出数据
    const exportData = filteredFlights.value.map(flight => ({
      航班号: flight.flightNumber,
      航空公司: flight.airline,
      出发城市: flight.departureCity,
      到达城市: flight.arrivalCity,
      出发时间: flight.departureTime,
      到达时间: flight.arrivalTime,
      出发日期: flight.departureDate,
      状态: getStatusText(flight.status),
      经济舱剩余: flight.seats.economy,
      商务舱剩余: flight.seats.business,
      头等舱剩余: flight.seats.first,
      经济舱价格: flight.prices.economy,
      商务舱价格: flight.prices.business,
      头等舱价格: flight.prices.first
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
    link.download = `航班运营数据_${new Date().toISOString().split('T')[0]}.csv`
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

const clampRefreshInterval = (value: number) => {
  if (!value) return 60
  return Math.min(300, Math.max(10, value))
}

let autoRefreshTimer: number | null = null

const stopAutoRefresh = () => {
  if (autoRefreshTimer) {
    clearInterval(autoRefreshTimer)
    autoRefreshTimer = null
  }
}

const restartAutoRefresh = () => {
  stopAutoRefresh()
  if (!opsPreferences.autoRefresh) return
  const interval = clampRefreshInterval(opsPreferences.refreshInterval) * 1000
  autoRefreshTimer = window.setInterval(() => {
    if (!loading.value) {
      handleRefresh()
    }
  }, interval)
}

const focusPersonalizationPanel = () => {
  personalizationSectionRef.value?.scrollIntoView({ behavior: 'smooth', block: 'start' })
  personalizationSectionRef.value?.classList.add('highlight-personalization')
  setTimeout(() => {
    personalizationSectionRef.value?.classList.remove('highlight-personalization')
  }, 1600)
}

const handlePersonalizationShortcut = () => {
  focusPersonalizationPanel()
}

// 监听筛选变化，重置分页
watch([() => searchParams.flightNumber, () => searchParams.departureDate, () => searchParams.departureCity, () => searchParams.arrivalCity, () => searchParams.status], () => {
  currentPage.value = 1
})

watch(() => opsPreferences.defaultStatus, () => {
  applyDefaultFilters()
}, { immediate: true })

watch(() => opsPreferences.refreshInterval, (val) => {
  const clamped = clampRefreshInterval(val)
  if (clamped !== val) {
    opsPreferences.refreshInterval = clamped
  }
})

watch(
  () => [opsPreferences.autoRefresh, opsPreferences.refreshInterval],
  () => {
    restartAutoRefresh()
  },
  { immediate: true }
)

// 返回运营控制台
const handleBack = () => {
  router.push('/portal/operations')
}

// 初始化
onMounted(() => {
  // 可以在这里加载初始数据
  loadFlights()
  loadMetrics()
  window.addEventListener('personalization-shortcut', handlePersonalizationShortcut)
})

onBeforeUnmount(() => {
  stopAutoRefresh()
  window.removeEventListener('personalization-shortcut', handlePersonalizationShortcut)
})
</script>

<style scoped>
.operations-dashboard {
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

.primary-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 15px 30px rgba(99, 102, 241, 0.5);
}

.ghost-btn {
  border-color: rgba(255, 255, 255, 0.35);
  background: transparent;
  color: #f8fafc;
}

.ghost-btn:hover:not(:disabled) {
  background: rgba(255, 255, 255, 0.1);
}

.primary-btn:disabled,
.ghost-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
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
  font-size: 1.3rem;
}

/* 搜索区域 */
.personalization-panel {
  margin-bottom: 1.5rem;
}

.panel-actions {
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
}

.panel-actions .ghost-btn.danger {
  border-color: rgba(248, 113, 113, 0.4);
  color: #f87171;
}

.personalization-grid {
  display: flex;
  gap: 1.5rem;
  flex-wrap: wrap;
}

.preference-column {
  flex: 1 1 280px;
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.preference-item {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.preference-item select,
.preference-item input[type="number"] {
  padding: 0.65rem 0.8rem;
  border: 1px solid rgba(255, 255, 255, 0.15);
  border-radius: 10px;
  background: rgba(15, 23, 42, 0.6);
  color: #fff;
}

.preference-item .item-hint {
  font-size: 0.8rem;
  color: rgba(248, 250, 252, 0.6);
  margin: 0;
}

.preference-item.toggle {
  flex-direction: row;
  align-items: center;
  justify-content: space-between;
}

.preference-item.toggles .checkbox {
  display: flex;
  align-items: center;
  gap: 0.4rem;
  font-size: 0.85rem;
  color: rgba(248, 250, 252, 0.8);
}

.preference-item.toggles .checkbox input {
  width: 16px;
  height: 16px;
  accent-color: #1E8AE6;
}

.theme-column {
  flex: 1 1 360px;
}

.theme-column details {
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 18px;
  padding: 1rem 1.2rem;
  background: rgba(1, 5, 10, 0.45);
}

.theme-column summary {
  cursor: pointer;
  font-weight: 600;
  color: rgba(248, 250, 252, 0.9);
  margin-bottom: 0.8rem;
}

.theme-settings-wrapper {
  max-height: 420px;
  overflow-y: auto;
  padding-right: 0.5rem;
}

.highlight-personalization {
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.5);
  transition: box-shadow 0.3s ease;
}

.switch {
  position: relative;
  display: inline-block;
  width: 46px;
  height: 26px;
}

.switch input {
  opacity: 0;
  width: 0;
  height: 0;
}

.switch .slider {
  position: absolute;
  cursor: pointer;
  inset: 0;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 26px;
  transition: 0.3s;
}

.switch .slider:before {
  position: absolute;
  content: "";
  height: 20px;
  width: 20px;
  left: 3px;
  bottom: 3px;
  background-color: #fff;
  transition: 0.3s;
  border-radius: 50%;
}

.switch input:checked + .slider {
  background: #1E8AE6;
}

.switch input:checked + .slider:before {
  transform: translateX(20px);
}

.search-section {
  margin-bottom: 1.5rem;
}

.search-form {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.search-row {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 1rem;
}

.search-group {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.search-group label {
  color: rgba(255, 255, 255, 0.8);
  font-size: 0.9rem;
  font-weight: 500;
}

.search-group input,
.search-group select {
  padding: 0.75rem;
  border: 1px solid rgba(255, 255, 255, 0.15);
  border-radius: 8px;
  background: rgba(15, 23, 42, 0.6);
  color: #fff;
  font-size: 0.9rem;
  transition: all 0.3s;
}

.search-group input:focus,
.search-group select:focus {
  outline: none;
  border-color: rgba(99, 102, 241, 0.5);
  background: rgba(15, 23, 42, 0.8);
}

.search-actions {
  display: flex;
  gap: 0.8rem;
  justify-content: flex-end;
}

/* 表格区域 */
.table-section {
  margin-bottom: 1.5rem;
}

.table-wrapper {
  overflow-x: auto;
  margin-top: 1rem;
}

.flights-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 0.9rem;
}

.flights-table thead {
  background: rgba(15, 23, 42, 0.8);
}

.flights-table th {
  padding: 1rem;
  text-align: left;
  color: rgba(255, 255, 255, 0.9);
  font-weight: 600;
  border-bottom: 2px solid rgba(255, 255, 255, 0.1);
  white-space: nowrap;
}

.flights-table td {
  padding: 1rem;
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
  color: rgba(255, 255, 255, 0.8);
}

.flights-table.compact th,
.flights-table.compact td {
  padding: 0.6rem 0.5rem;
  font-size: 0.8rem;
}

.flights-table tbody tr {
  transition: all 0.3s;
}

.flights-table tbody tr:hover {
  background: rgba(15, 23, 42, 0.6);
}

.flight-number-cell {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.flight-number-cell strong {
  color: #fff;
  font-size: 1rem;
}

.airline-name {
  font-size: 0.8rem;
  color: rgba(255, 255, 255, 0.6);
}

.route-cell {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.route-cell .city {
  color: #fff;
  font-weight: 500;
}

.route-cell .arrow {
  color: rgba(255, 255, 255, 0.4);
}

.time-cell {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.time-item {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.time-label {
  font-size: 0.75rem;
  color: rgba(255, 255, 255, 0.6);
}

.time-value {
  color: #fff;
  font-weight: 500;
}

.status-badge {
  padding: 0.4rem 0.9rem;
  border-radius: 999px;
  font-size: 0.85rem;
  font-weight: 600;
  white-space: nowrap;
  display: inline-block;
}

.status-badge.scheduled {
  background: rgba(96, 165, 250, 0.2);
  color: #1E8AE6;
  border: 1px solid rgba(96, 165, 250, 0.4);
}

.status-badge.boarding {
  background: rgba(251, 191, 36, 0.2);
  color: #fbbf24;
  border: 1px solid rgba(251, 191, 36, 0.4);
}

.status-badge.departed {
  background: rgba(139, 92, 246, 0.2);
  color: #a78bfa;
  border: 1px solid rgba(139, 92, 246, 0.4);
}

.status-badge.arrived {
  background: rgba(34, 197, 94, 0.2);
  color: #4ade80;
  border: 1px solid rgba(34, 197, 94, 0.4);
}

.status-badge.delayed {
  background: rgba(248, 113, 113, 0.2);
  color: #f87171;
  border: 1px solid rgba(248, 113, 113, 0.4);
}

.status-badge.canceled {
  background: rgba(107, 114, 128, 0.2);
  color: #9ca3af;
  border: 1px solid rgba(107, 114, 128, 0.4);
}

.seat-empty {
  color: #f87171;
  font-weight: 600;
}

.seat-low {
  color: #fbbf24;
  font-weight: 600;
}

.seat-medium {
  color: #1E8AE6;
  font-weight: 600;
}

.seat-high {
  color: #4ade80;
  font-weight: 600;
}

.price {
  color: #fcd34d;
  font-weight: 600;
}

.action-buttons {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.action-btn {
  padding: 0.5rem 1rem;
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 8px;
  background: rgba(15, 23, 42, 0.6);
  color: #fff;
  font-size: 0.85rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
  white-space: nowrap;
}

.action-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
}

.action-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.price-btn {
  background: rgba(96, 165, 250, 0.2);
  border-color: rgba(96, 165, 250, 0.4);
  color: #1E8AE6;
}

.price-btn:hover:not(:disabled) {
  background: rgba(96, 165, 250, 0.3);
}

.status-btn {
  background: rgba(251, 191, 36, 0.2);
  border-color: rgba(251, 191, 36, 0.4);
  color: #fbbf24;
}

.status-btn:hover:not(:disabled) {
  background: rgba(251, 191, 36, 0.3);
}

.cancel-btn {
  background: rgba(248, 113, 113, 0.2);
  border-color: rgba(248, 113, 113, 0.4);
  color: #f87171;
}

.cancel-btn:hover:not(:disabled) {
  background: rgba(248, 113, 113, 0.3);
}

/* 分页 */
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
  -webkit-overflow-scrolling: touch;
  min-height: 100vh;
  min-height: 100dvh;
}

.modal-content {
  background: rgba(15, 23, 42, 0.95);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 24px;
  width: 90%;
  max-width: 600px;
  max-height: calc(100vh - 40px);
  max-height: calc(100dvh - 40px);
  overflow-y: auto;
  box-shadow: 0 25px 50px rgba(15, 23, 42, 0.5);
  margin: auto;
  position: relative;
  box-sizing: border-box;
  scroll-behavior: smooth;
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
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

.form-group {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  margin-bottom: 1.5rem;
}

.form-group:last-child {
  margin-bottom: 0;
}

.form-group label {
  color: rgba(255, 255, 255, 0.8);
  font-size: 0.9rem;
  font-weight: 500;
}

.form-group input,
.form-group select,
.form-group textarea {
  padding: 0.75rem;
  border: 1px solid rgba(255, 255, 255, 0.15);
  border-radius: 8px;
  background: rgba(15, 23, 42, 0.6);
  color: #fff;
  font-size: 0.9rem;
  font-family: inherit;
  transition: all 0.3s;
}

.form-group input:focus,
.form-group select:focus,
.form-group textarea:focus {
  outline: none;
  border-color: rgba(99, 102, 241, 0.5);
  background: rgba(15, 23, 42, 0.8);
}

.form-group textarea {
  resize: vertical;
  min-height: 100px;
}

.modal-footer {
  padding: 24px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  display: flex;
  gap: 1rem;
  justify-content: flex-end;
}

/* 响应式 */
@media (max-width: 768px) {
  .search-row {
    grid-template-columns: 1fr;
  }

  .flights-table {
    font-size: 0.8rem;
  }

  .flights-table th,
  .flights-table td {
    padding: 0.75rem 0.5rem;
  }

  .action-buttons {
    flex-direction: row;
    flex-wrap: wrap;
  }

  .action-btn {
    font-size: 0.75rem;
    padding: 0.4rem 0.8rem;
  }
}

@media (max-width: 480px) {
  .modal-content {
    width: calc(100vw - 24px);
    max-width: none;
  }

  .modal-header,
  .modal-body,
  .modal-footer {
    padding: 16px;
  }
}
</style>


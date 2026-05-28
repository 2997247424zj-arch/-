<template>
  <AdminLayout>
    <div class="page-container aircraft-page">
      <div class="breadcrumb">
        <span>首页</span>
        <span class="breadcrumb-separator">/</span>
        <span>机型管理</span>
      </div>

      <header class="page-header">
        <div>
          <p class="page-label">机队健康 · 生命周期</p>
          <h1>机型维护与准入</h1>
          <p>覆盖机队档案、检修排程、机组适配与新增机型申报，帮助航空运营保持高可用度。</p>
        </div>
        <div class="page-actions">
          <button class="ghost-btn" @click="goBack">返回仪表盘</button>
          <button class="ghost-btn" @click="exportData">导出机型库</button>
          <button class="ghost-btn" @click="openBatchAddModal">批量新增</button>
          <button class="primary-btn" @click="openAddModal">新增机型</button>
        </div>
      </header>

      <section class="stats-grid">
        <article v-for="item in fleetKpis" :key="item.label" class="glass-card kpi-card">
          <div class="kpi-icon">{{ item.icon }}</div>
          <div>
            <p class="kpi-label">{{ item.label }}</p>
            <p class="kpi-value">{{ item.value }}</p>
            <small :class="item.trendType">{{ item.helper }}</small>
          </div>
        </article>
      </section>

      <!-- 机型列表 -->
      <section class="glass-card">
        <div class="section-head">
          <div>
            <p class="section-label">机型管理</p>
            <h2>机型列表</h2>
          </div>
          <div class="filter-actions">
            <select v-model="filterStatus" @change="handleSearch" class="filter-select">
              <option value="all">全部</option>
              <option value="active">使用中</option>
              <option value="retired">已退役</option>
            </select>
          </div>
        </div>

        <!-- 搜索栏 -->
        <div class="search-bar">
          <div class="search-group">
            <input
              v-model="searchParams.typeCode"
              type="text"
              placeholder="搜索机型代码..."
              class="search-input"
              @input="handleSearch"
            />
          </div>
          <div class="search-group">
            <input
              v-model="searchParams.manufacturer"
              type="text"
              placeholder="搜索制造商..."
              class="search-input"
              @input="handleSearch"
            />
          </div>
          <div class="search-group">
            <input
              v-model="searchParams.model"
              type="text"
              placeholder="搜索型号..."
              class="search-input"
              @input="handleSearch"
            />
          </div>
          <button class="ghost-btn" @click="clearSearch">清除</button>
        </div>

        <!-- 批量操作栏 -->
        <div v-if="selectedIds.length > 0" class="batch-actions">
          <span class="batch-info">已选择 {{ selectedIds.length }} 项</span>
          <button class="delete-btn" @click="handleBatchDelete">批量删除</button>
        </div>

        <div v-if="loading" class="loading-state">
          <p>加载中...</p>
        </div>

        <div v-else-if="aircraftTypes.length === 0" class="empty-state">
          <p>暂无机型数据</p>
        </div>

        <div v-else>
          <table class="data-table">
          <thead>
            <tr>
              <th>
                <input
                  type="checkbox"
                  :checked="isAllSelected"
                  @change="toggleSelectAll"
                  class="checkbox"
                />
              </th>
              <th>ID</th>
              <th>机型代码</th>
              <th>制造商</th>
              <th>型号</th>
              <th>座位布局</th>
              <th>状态</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in aircraftTypes" :key="item.id">
              <td>
                <input
                  type="checkbox"
                  :checked="selectedIds.includes(item.id)"
                  @change="toggleSelect(item.id)"
                  class="checkbox"
                />
              </td>
              <td>{{ item.id }}</td>
              <td>{{ item.typeCode }}</td>
              <td>{{ item.manufacturer || '-' }}</td>
              <td>{{ item.model || '-' }}</td>
              <td>
                <span v-if="item.seatLayout" class="seat-layout">
                  {{ formatSeatLayout(item.seatLayout) }}
                </span>
                <span v-else>-</span>
              </td>
              <td>
                <span :class="['status-tag', item.status]">
                  {{ item.status === 'active' ? '使用中' : '已退役' }}
                </span>
              </td>
              <td>
                <div class="action-buttons">
                  <button class="action-btn edit-btn" @click="openEditModal(item)">编辑</button>
                  <button class="action-btn delete-btn" @click="handleDelete(item.id)">删除</button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>

        <!-- 分页控件 -->
        <div class="pagination-container">
          <div class="pagination-info">
            <span>共 {{ pagination.total }} 条记录</span>
            <span>每页显示</span>
            <select v-model="pagination.size" @change="handlePageSizeChange" class="page-size-select">
              <option :value="5">5</option>
              <option :value="10">10</option>
              <option :value="20">20</option>
              <option :value="50">50</option>
              <option :value="100">100</option>
            </select>
            <span>条</span>
          </div>
          <div class="pagination-controls">
            <button
              class="pagination-btn"
              @click="goToPage(0)"
              :disabled="pagination.page === 0"
            >
              首页
            </button>
            <button
              class="pagination-btn"
              @click="goToPage(pagination.page - 1)"
              :disabled="pagination.page === 0"
            >
              上一页
            </button>
            <span class="pagination-page-info">
              第 {{ pagination.page + 1 }} / {{ pagination.totalPages }} 页
            </span>
            <button
              class="pagination-btn"
              @click="goToPage(pagination.page + 1)"
              :disabled="pagination.page >= pagination.totalPages - 1"
            >
              下一页
            </button>
            <button
              class="pagination-btn"
              @click="goToPage(pagination.totalPages - 1)"
              :disabled="pagination.page >= pagination.totalPages - 1"
            >
              末页
            </button>
            <div class="pagination-jump">
              <span>跳转到</span>
              <input
                v-model.number="jumpPage"
                type="number"
                min="1"
                :max="pagination.totalPages"
                class="jump-input"
                @keyup.enter="handleJumpPage"
              />
              <span>页</span>
              <button class="pagination-btn small" @click="handleJumpPage">跳转</button>
            </div>
          </div>
        </div>
        </div>
      </section>

      <!-- 新增/编辑模态框 -->
      <div v-if="showModal" class="modal-overlay" @click="closeModal">
        <div class="modal-content" @click.stop>
          <div class="modal-header">
            <h3>{{ editingItem ? '编辑机型' : '新增机型' }}</h3>
            <button class="modal-close" @click="closeModal">×</button>
          </div>
          <form @submit.prevent="handleSubmit" class="modal-form">
            <div class="form-group">
              <label>
                机型代码 <span class="required">*</span>
                <input
                  v-model="formData.typeCode"
                  type="text"
                  placeholder="如：A321XLR"
                  required
                  :disabled="!!editingItem"
                  list="typeCodeSuggestions"
                />
                <datalist id="typeCodeSuggestions">
                  <option v-for="type in existingTypeCodes" :key="type" :value="type" />
                </datalist>
              </label>
            </div>
            <div class="form-group">
              <label>
                制造商
                <input
                  v-model="formData.manufacturer"
                  type="text"
                  placeholder="如：Airbus"
                  list="manufacturerSuggestions"
                  @focus="loadOptions"
                />
                <datalist id="manufacturerSuggestions">
                  <option v-for="mfg in manufacturers" :key="mfg" :value="mfg" />
                </datalist>
                <small v-if="manufacturers.length > 0" class="hint-text">
                  已有选项: {{ manufacturers.join(', ') }}
                </small>
              </label>
            </div>
            <div class="form-group">
              <label>
                型号
                <input
                  v-model="formData.model"
                  type="text"
                  placeholder="如：A321XLR"
                  list="modelSuggestions"
                  @focus="loadOptions"
                />
                <datalist id="modelSuggestions">
                  <option v-for="model in models" :key="model" :value="model" />
                </datalist>
                <small v-if="models.length > 0" class="hint-text">
                  已有选项: {{ models.slice(0, 5).join(', ') }}{{ models.length > 5 ? '...' : '' }}
                </small>
              </label>
            </div>
            <div class="form-group">
              <label>
                座位布局 (JSON格式)
                <textarea
                  v-model="formData.seatLayout"
                  placeholder='例如: {"business": 16, "economy": 180}'
                  rows="3"
                ></textarea>
              </label>
            </div>
            <div class="form-group">
              <label>
                状态
                <select v-model="formData.status">
                  <option value="active">使用中</option>
                  <option value="retired">已退役</option>
                </select>
              </label>
            </div>
            <div class="modal-actions">
              <button type="button" class="ghost-btn" @click="closeModal">取消</button>
              <button type="submit" class="primary-btn">保存</button>
            </div>
          </form>
        </div>
      </div>

      <!-- 批量新增模态框 -->
      <div v-if="showBatchModal" class="modal-overlay" @click="closeBatchModal">
        <div class="modal-content batch-modal" @click.stop>
          <div class="modal-header">
            <h3>批量新增机型</h3>
            <button class="modal-close" @click="closeBatchModal">×</button>
          </div>
          <div class="batch-form">
            <!-- 快速批量添加区域 -->
            <div class="quick-batch-section">
              <div class="section-title">
                <span class="title-icon">⚡</span>
                <span>快速批量添加</span>
              </div>
              <div class="quick-batch-form">
                <div class="quick-batch-group">
                  <label>
                    <span>选择已有机型</span>
                    <select v-model="quickBatchTemplate" class="quick-select">
                      <option value="">-- 请选择机型模板 --</option>
                      <option
                        v-for="type in aircraftTypes"
                        :key="type.id"
                        :value="type.id"
                      >
                        {{ type.typeCode }} - {{ type.manufacturer || '' }} {{ type.model || '' }}
                      </option>
                    </select>
                  </label>
                </div>
                <div class="quick-batch-group">
                  <label>
                    <span>数量</span>
                    <input
                      v-model.number="quickBatchCount"
                      type="number"
                      min="1"
                      max="100"
                      placeholder="输入数量"
                      class="quick-input"
                    />
                  </label>
                </div>
                <button
                  type="button"
                  class="primary-btn quick-add-btn"
                  @click="handleQuickBatchAdd"
                  :disabled="!quickBatchTemplate || !quickBatchCount || quickBatchCount < 1"
                >
                  快速添加
                </button>
              </div>
            </div>

            <!-- 手动添加区域 -->
            <div class="manual-batch-section">
              <div class="section-title">
                <span class="title-icon">✏️</span>
                <span>手动添加</span>
              </div>
              <div class="batch-form-header">
                <button type="button" class="ghost-btn" @click="addBatchRow">
                  <span>➕</span> 添加一行
                </button>
                <button type="button" class="ghost-btn" @click="removeBatchRow">
                  <span>➖</span> 删除一行
                </button>
                <span class="row-count">当前 {{ batchFormData.length }} 行</span>
              </div>
              <div class="batch-table-container">
                <table class="batch-table">
                  <thead>
                    <tr>
                      <th style="width: 18%">机型代码*</th>
                      <th style="width: 15%">制造商</th>
                      <th style="width: 15%">型号</th>
                      <th style="width: 25%">座位布局</th>
                      <th style="width: 12%">状态</th>
                      <th style="width: 15%">操作</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="(row, index) in batchFormData" :key="index">
                      <td>
                        <input
                          v-model="row.typeCode"
                          type="text"
                          placeholder="必填"
                          required
                          class="batch-input"
                        />
                      </td>
                      <td>
                        <input
                          v-model="row.manufacturer"
                          type="text"
                          placeholder="如：Airbus"
                          class="batch-input"
                        />
                      </td>
                      <td>
                        <input
                          v-model="row.model"
                          type="text"
                          placeholder="如：A321XLR"
                          class="batch-input"
                        />
                      </td>
                      <td>
                        <input
                          v-model="row.seatLayout"
                          type="text"
                          placeholder='{"business": 16, "economy": 180}'
                          class="batch-input"
                        />
                      </td>
                      <td>
                        <select v-model="row.status" class="batch-select">
                          <option value="active">使用中</option>
                          <option value="retired">已退役</option>
                        </select>
                      </td>
                      <td>
                        <button
                          type="button"
                          class="action-btn delete-btn small-btn"
                          @click="removeBatchRowAt(index)"
                          :disabled="batchFormData.length === 1"
                        >
                          删除
                        </button>
                      </td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </div>

            <datalist id="batchTypeCodeSuggestions">
              <option v-for="type in existingTypeCodes" :key="type" :value="type" />
            </datalist>
            <datalist id="batchManufacturerSuggestions">
              <option v-for="mfg in manufacturers" :key="mfg" :value="mfg" />
            </datalist>
            <datalist id="batchModelSuggestions">
              <option v-for="model in models" :key="model" :value="model" />
            </datalist>
            <div class="modal-actions">
              <button type="button" class="ghost-btn" @click="closeBatchModal">取消</button>
              <button type="button" class="primary-btn" @click="handleBatchSubmit">
                保存 ({{ batchFormData.length }} 项)
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </AdminLayout>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import AdminLayout from '../components/AdminLayout.vue'
import { aircraftTypeApi } from '../services/api'

interface AircraftType {
  id: number
  typeCode: string
  manufacturer?: string
  model?: string
  seatLayout?: string
  status: 'active' | 'retired'
}

const router = useRouter()

// 状态管理
const loading = ref(false)
const aircraftTypes = ref<AircraftType[]>([])
const filterStatus = ref<'all' | 'active' | 'retired'>('all')
const showModal = ref(false)
const showBatchModal = ref(false)
const editingItem = ref<AircraftType | null>(null)
const selectedIds = ref<number[]>([])
const manufacturers = ref<string[]>([])
const models = ref<string[]>([])

// 分页信息
const pagination = ref({
  page: 0,
  size: 5,
  total: 0,
  totalPages: 0
})
const jumpPage = ref<number>(1)

// 搜索参数
const searchParams = ref({
  typeCode: '',
  manufacturer: '',
  model: ''
})

// 表单数据
const formData = ref({
  typeCode: '',
  manufacturer: '',
  model: '',
  seatLayout: '',
  status: 'active' as 'active' | 'retired'
})

// 批量表单数据
const batchFormData = ref<Array<{
  typeCode: string
  manufacturer: string
  model: string
  seatLayout: string
  status: 'active' | 'retired'
}>>([
  {
    typeCode: '',
    manufacturer: '',
    model: '',
    seatLayout: '',
    status: 'active'
  }
])

// 快速批量添加
const quickBatchTemplate = ref<number | string>('')
const quickBatchCount = ref<number>(1)

// 统计数据（使用分页总数）
const fleetKpis = computed(() => {
  const total = pagination.value.total
  // 需要重新加载所有数据来统计，或者使用后端统计接口
  // 这里暂时使用分页总数
  return [
    { 
      label: '机队总量', 
      value: total.toString(), 
      helper: `共 ${total} 架`, 
      trendType: 'up', 
      icon: '✈️' 
    },
    { 
      label: '当前页', 
      value: aircraftTypes.value.length.toString(), 
      helper: `第 ${pagination.value.page + 1} 页`, 
      trendType: 'up', 
      icon: '✅' 
    }
  ]
})

// 全选状态
const isAllSelected = computed(() => {
  return aircraftTypes.value.length > 0 && selectedIds.value.length === aircraftTypes.value.length
})

// 已有的机型代码列表（用于提示）
const existingTypeCodes = computed(() => {
  return aircraftTypes.value.map(a => a.typeCode)
})

// 加载机型列表
const loadAircraftTypes = async () => {
  loading.value = true
  try {
    let result
    // 如果有搜索参数，使用搜索接口
    if (searchParams.value.typeCode || searchParams.value.manufacturer || searchParams.value.model) {
      result = await aircraftTypeApi.searchAircraftTypes({
        typeCode: searchParams.value.typeCode || undefined,
        manufacturer: searchParams.value.manufacturer || undefined,
        model: searchParams.value.model || undefined,
        status: filterStatus.value === 'all' ? undefined : filterStatus.value,
        page: pagination.value.page,
        size: pagination.value.size
      })
    } else {
      // 否则使用普通列表接口
      result = await aircraftTypeApi.getAircraftTypes(
        filterStatus.value,
        pagination.value.page,
        pagination.value.size
      )
    }
    
    if (result && result.success && result.data) {
      // 处理分页数据
      if (result.data.data && Array.isArray(result.data.data)) {
        aircraftTypes.value = result.data.data
        pagination.value.total = result.data.total || 0
        pagination.value.page = result.data.page || 0
        pagination.value.size = result.data.size || 5
        pagination.value.totalPages = result.data.totalPages || 0
      } else if (Array.isArray(result.data)) {
        // 兼容旧格式（无分页）
        aircraftTypes.value = result.data
        pagination.value.total = result.data.length
        pagination.value.totalPages = 1
      } else {
        aircraftTypes.value = []
      }
    } else {
      aircraftTypes.value = []
      pagination.value.total = 0
      pagination.value.totalPages = 0
    }
    // 清除选择
    selectedIds.value = []
    // 更新跳转页码
    jumpPage.value = pagination.value.page + 1
  } catch (error: any) {
    console.error('加载机型列表失败:', error)
    alert('加载机型列表失败: ' + (error.message || '未知错误'))
    aircraftTypes.value = []
    pagination.value.total = 0
    pagination.value.totalPages = 0
  } finally {
    loading.value = false
  }
}

// 搜索处理
const handleSearch = () => {
  pagination.value.page = 0 // 搜索时重置到第一页
  loadAircraftTypes()
}

// 分页处理
const goToPage = (page: number) => {
  if (page >= 0 && page < pagination.value.totalPages) {
    pagination.value.page = page
    loadAircraftTypes()
  }
}

// 改变每页显示数量
const handlePageSizeChange = () => {
  pagination.value.page = 0 // 改变每页数量时重置到第一页
  loadAircraftTypes()
}

// 跳转到指定页
const handleJumpPage = () => {
  const page = jumpPage.value - 1
  if (page >= 0 && page < pagination.value.totalPages) {
    goToPage(page)
  } else {
    alert(`请输入有效的页码（1-${pagination.value.totalPages}）`)
    jumpPage.value = pagination.value.page + 1
  }
}

// 清除搜索
const clearSearch = () => {
  searchParams.value = {
    typeCode: '',
    manufacturer: '',
    model: ''
  }
  loadAircraftTypes()
}

// 加载可选项
const loadOptions = async () => {
  try {
    if (manufacturers.value.length === 0) {
      const mfgResult = await aircraftTypeApi.getManufacturers()
      if (mfgResult && mfgResult.success && mfgResult.data) {
        manufacturers.value = Array.isArray(mfgResult.data) ? mfgResult.data : []
      }
    }
    if (models.value.length === 0) {
      const modelResult = await aircraftTypeApi.getModels()
      if (modelResult && modelResult.success && modelResult.data) {
        models.value = Array.isArray(modelResult.data) ? modelResult.data : []
      }
    }
  } catch (error) {
    console.error('加载可选项失败:', error)
  }
}

// 格式化座位布局
const formatSeatLayout = (seatLayout: string) => {
  if (!seatLayout) return '-'
  try {
    const layout = JSON.parse(seatLayout)
    if (typeof layout === 'object') {
      const parts: string[] = []
      if (layout.business || layout.商务舱) parts.push(`商务 ${layout.business || layout.商务舱}`)
      if (layout.economy || layout.经济舱) parts.push(`经济 ${layout.economy || layout.经济舱}`)
      if (layout.first || layout.头等舱) parts.push(`头等 ${layout.first || layout.头等舱}`)
      return parts.join(' / ') || seatLayout
    }
    return seatLayout
  } catch {
    return seatLayout
  }
}

// 打开新增模态框
const openAddModal = () => {
  editingItem.value = null
  formData.value = {
    typeCode: '',
    manufacturer: '',
    model: '',
    seatLayout: '',
    status: 'active'
  }
  loadOptions()
  showModal.value = true
}

// 打开批量新增模态框
const openBatchAddModal = () => {
  batchFormData.value = [{
    typeCode: '',
    manufacturer: '',
    model: '',
    seatLayout: '',
    status: 'active'
  }]
  loadOptions()
  showBatchModal.value = true
}

// 添加批量行
const addBatchRow = () => {
  batchFormData.value.push({
    typeCode: '',
    manufacturer: '',
    model: '',
    seatLayout: '',
    status: 'active'
  })
}

// 删除批量行
const removeBatchRow = () => {
  if (batchFormData.value.length > 1) {
    batchFormData.value.pop()
  }
}

// 删除指定批量行
const removeBatchRowAt = (index: number) => {
  if (batchFormData.value.length > 1) {
    batchFormData.value.splice(index, 1)
  }
}

// 打开编辑模态框
const openEditModal = (item: AircraftType) => {
  editingItem.value = item
  formData.value = {
    typeCode: item.typeCode,
    manufacturer: item.manufacturer || '',
    model: item.model || '',
    seatLayout: item.seatLayout || '',
    status: item.status
  }
  loadOptions()
  showModal.value = true
}

// 关闭模态框
const closeModal = () => {
  showModal.value = false
  editingItem.value = null
  formData.value = {
    typeCode: '',
    manufacturer: '',
    model: '',
    seatLayout: '',
    status: 'active'
  }
}

// 关闭批量模态框
const closeBatchModal = () => {
  showBatchModal.value = false
  batchFormData.value = [{
    typeCode: '',
    manufacturer: '',
    model: '',
    seatLayout: '',
    status: 'active'
  }]
  quickBatchTemplate.value = ''
  quickBatchCount.value = 1
}

// 快速批量添加
const handleQuickBatchAdd = () => {
  if (!quickBatchTemplate.value || !quickBatchCount.value || quickBatchCount.value < 1) {
    alert('请选择机型模板并输入数量')
    return
  }

  if (quickBatchCount.value > 100) {
    alert('一次最多只能添加100个机型')
    return
  }

  const template = aircraftTypes.value.find(t => t.id === Number(quickBatchTemplate.value))
  if (!template) {
    alert('选择的机型模板不存在')
    return
  }

  // 获取所有已使用的机型代码
  const existingCodes = new Set([
    ...batchFormData.value.map(r => r.typeCode).filter(c => c),
    ...aircraftTypes.value.map(t => t.typeCode)
  ])

  // 生成指定数量的行
  const newRows = []
  const baseCode = template.typeCode
  
  for (let i = 0; i < quickBatchCount.value; i++) {
    let typeCode: string
    
    if (quickBatchCount.value === 1) {
      // 如果只添加1个，检查原代码是否可用
      if (!existingCodes.has(baseCode)) {
        typeCode = baseCode
      } else {
        // 如果原代码已存在，添加序号
        let suffix = 1
        typeCode = `${baseCode}-${String(suffix).padStart(3, '0')}`
        while (existingCodes.has(typeCode)) {
          suffix++
          typeCode = `${baseCode}-${String(suffix).padStart(3, '0')}`
        }
      }
    } else {
      // 如果添加多个，每个都添加序号
      let suffix = i + 1
      typeCode = `${baseCode}-${String(suffix).padStart(3, '0')}`
      while (existingCodes.has(typeCode)) {
        suffix++
        typeCode = `${baseCode}-${String(suffix).padStart(3, '0')}`
      }
    }
    
    // 添加到已使用集合
    existingCodes.add(typeCode)
    
    newRows.push({
      typeCode: typeCode,
      manufacturer: template.manufacturer || '',
      model: template.model || '',
      seatLayout: template.seatLayout || '',
      status: template.status
    })
  }

  // 如果第一行是空的，替换第一行；否则追加
  if (batchFormData.value.length === 1 && batchFormData.value[0] && !batchFormData.value[0].typeCode.trim()) {
    // 第一行是空的，替换第一行
    if (newRows.length > 0 && newRows[0]) {
      batchFormData.value[0] = {
        typeCode: newRows[0].typeCode,
        manufacturer: newRows[0].manufacturer || '',
        model: newRows[0].model || '',
        seatLayout: newRows[0].seatLayout || '',
        status: newRows[0].status
      }
      // 如果有更多行，追加剩余的
      if (newRows.length > 1) {
        batchFormData.value.push(...newRows.slice(1))
      }
    }
  } else {
    // 第一行有数据，直接追加
    batchFormData.value.push(...newRows)
  }
  
  // 重置快速添加表单
  quickBatchTemplate.value = ''
  quickBatchCount.value = 1

  alert(`成功添加 ${newRows.length} 行数据`)
}

// 提交表单
const handleSubmit = async () => {
  try {
    // 验证座位布局JSON格式
    if (formData.value.seatLayout) {
      try {
        JSON.parse(formData.value.seatLayout)
      } catch {
        alert('座位布局必须是有效的JSON格式')
        return
      }
    }

    if (editingItem.value) {
      // 更新
      const result = await aircraftTypeApi.updateAircraftType(editingItem.value.id, formData.value)
      if (result && result.success) {
        alert('更新机型成功')
        closeModal()
        loadAircraftTypes()
      } else {
        alert('更新机型失败: ' + (result?.message || '未知错误'))
      }
    } else {
      // 新增
      const result = await aircraftTypeApi.createAircraftType(formData.value)
      if (result && result.success) {
        alert('创建机型成功')
        closeModal()
        loadAircraftTypes()
      } else {
        alert('创建机型失败: ' + (result?.message || '未知错误'))
      }
    }
  } catch (error: any) {
    console.error('提交失败:', error)
    alert('操作失败: ' + (error.message || '未知错误'))
  }
}

// 批量提交
const handleBatchSubmit = async () => {
  try {
    // 验证必填字段
    const invalidRows = batchFormData.value.filter(row => !row.typeCode.trim())
    if (invalidRows.length > 0) {
      alert('请填写所有行的机型代码')
      return
    }

    // 验证JSON格式
    for (const row of batchFormData.value) {
      if (row.seatLayout) {
        try {
          JSON.parse(row.seatLayout)
        } catch {
          alert(`第 ${batchFormData.value.indexOf(row) + 1} 行的座位布局格式错误`)
          return
        }
      }
    }

    const result = await aircraftTypeApi.createAircraftTypes(batchFormData.value)
    if (result && result.success) {
      alert(`成功创建 ${batchFormData.value.length} 个机型`)
      closeBatchModal()
      loadAircraftTypes()
    } else {
      alert('批量创建失败: ' + (result?.message || '未知错误'))
    }
  } catch (error: any) {
    console.error('批量提交失败:', error)
    alert('批量创建失败: ' + (error.message || '未知错误'))
  }
}

// 删除机型
const handleDelete = async (id: number) => {
  if (!confirm('确定要删除这个机型吗？')) {
    return
  }

  try {
    const result = await aircraftTypeApi.deleteAircraftType(id)
    if (result && result.success) {
      alert('删除机型成功')
      loadAircraftTypes()
    } else {
      alert('删除机型失败: ' + (result?.message || '未知错误'))
    }
  } catch (error: any) {
    console.error('删除失败:', error)
    alert('删除失败: ' + (error.message || '未知错误'))
  }
}

// 批量删除
const handleBatchDelete = async () => {
  if (selectedIds.value.length === 0) {
    alert('请选择要删除的机型')
    return
  }

  const deleteCount = selectedIds.value.length
  if (!confirm(`确定要删除选中的 ${deleteCount} 个机型吗？`)) {
    return
  }

  try {
    const result = await aircraftTypeApi.deleteAircraftTypes(selectedIds.value)
    if (result && result.success) {
      alert('批量删除成功')
      const deletedIds = [...selectedIds.value]
      selectedIds.value = []
      // 删除后，如果当前页没有数据了，跳转到上一页
      const currentPageDataCount = aircraftTypes.value.length
      if (currentPageDataCount - deletedIds.length <= 0 && pagination.value.page > 0) {
        pagination.value.page = pagination.value.page - 1
      }
      loadAircraftTypes()
    } else {
      alert('批量删除失败: ' + (result?.message || '未知错误'))
    }
  } catch (error: any) {
    console.error('批量删除失败:', error)
    alert('批量删除失败: ' + (error.message || '未知错误'))
  }
}

// 切换选择
const toggleSelect = (id: number) => {
  const index = selectedIds.value.indexOf(id)
  if (index > -1) {
    selectedIds.value.splice(index, 1)
  } else {
    selectedIds.value.push(id)
  }
}

// 全选/取消全选
const toggleSelectAll = (event: Event) => {
  const target = event.target as HTMLInputElement
  if (target.checked) {
    selectedIds.value = aircraftTypes.value.map(a => a.id)
  } else {
    selectedIds.value = []
  }
}

// 导出数据
const exportData = () => {
  try {
    const dataStr = JSON.stringify(aircraftTypes.value, null, 2)
    const dataBlob = new Blob([dataStr], { type: 'application/json' })
    const url = URL.createObjectURL(dataBlob)
    const link = document.createElement('a')
    link.href = url
    link.download = `aircraft-types-${new Date().toISOString().split('T')[0]}.json`
    link.click()
    URL.revokeObjectURL(url)
  } catch (error) {
    alert('导出失败')
  }
}

// 返回仪表盘
const goBack = () => {
  router.push('/dashboard')
}

// 初始化
onMounted(() => {
  loadAircraftTypes()
  loadOptions()
})
</script>

<style scoped>
.page-container {
  padding: 2.5rem clamp(1.5rem, 6vw, 4rem) 3rem;
  color: var(--air-text-strong);
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.breadcrumb {
  font-size: 14px;
  color: var(--air-text-muted);
  margin-bottom: 20px;
}

.breadcrumb-separator {
  margin: 0 8px;
  color: rgba(230, 240, 255, 0.4);
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
  color: var(--air-text-muted);
}

.page-header h1 {
  margin: 0.4rem 0;
  font-size: clamp(1.8rem, 3vw, 2.4rem);
  color: var(--air-text-strong);
}

.page-header p {
  color: var(--air-text-muted);
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
  font-size: 0.9rem;
}

.primary-btn {
  background: var(--gradient-primary);
  color: var(--color-text-inverse);
  box-shadow: 0 10px 25px rgba(30, 138, 230, 0.35);
}

.primary-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 30px rgba(99, 102, 241, 0.45);
}

.ghost-btn {
  border-color: var(--air-border-strong);
  background: transparent;
  color: var(--air-text-strong);
}

.ghost-btn:hover {
  background: rgba(230, 240, 255, 0.08);
  border-color: rgba(230, 240, 255, 0.4);
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 1.5rem;
}

.glass-card {
  border-radius: 28px;
  padding: 1.8rem;
  background: var(--air-surface);
  border: 1px solid var(--air-border);
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
  color: var(--air-text-strong);
}

.filter-actions {
  display: flex;
  gap: 0.8rem;
}

.filter-select {
  padding: 0.5rem 1rem;
  border-radius: 12px;
  border: 1px solid var(--air-border-strong);
  background: var(--air-surface-muted);
  color: var(--air-text-strong);
  cursor: pointer;
}

/* 搜索栏 */
.search-bar {
  display: flex;
  gap: 0.8rem;
  margin-bottom: 1rem;
  flex-wrap: wrap;
  align-items: center;
}

.search-group {
  flex: 1;
  min-width: 150px;
}

.search-input {
  width: 100%;
  padding: 0.65rem 0.85rem;
  border-radius: 12px;
  border: 1px solid var(--air-border-strong);
  background: var(--air-surface-muted);
  color: var(--air-text-strong);
  font-size: 0.9rem;
}

.search-input:focus {
  outline: none;
  border-color: var(--color-primary-light);
  box-shadow: 0 0 0 3px rgba(30, 138, 230, 0.1);
}

/* 批量操作栏 */
.batch-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.8rem 1.2rem;
  margin-bottom: 1rem;
  background: rgba(30, 138, 230, 0.15);
  border-radius: 12px;
  border: 1px solid rgba(30, 138, 230, 0.4);
  box-shadow: 0 4px 12px rgba(30, 138, 230, 0.2);
  z-index: 10;
  position: relative;
}

.batch-info {
  color: var(--air-text-strong);
  font-weight: 600;
  font-size: 0.95rem;
}

.batch-actions .delete-btn {
  padding: 0.6rem 1.2rem;
  font-size: 0.9rem;
  font-weight: 600;
  background: rgba(255, 107, 107, 0.2);
  border-color: rgba(255, 107, 107, 0.5);
  color: var(--air-danger);
}

.batch-actions .delete-btn:hover {
  background: rgba(255, 107, 107, 0.3);
  border-color: rgba(255, 107, 107, 0.7);
  transform: translateY(-1px);
  box-shadow: 0 4px 8px rgba(255, 107, 107, 0.3);
}

.kpi-card {
  display: flex;
  gap: 1rem;
  align-items: center;
}

.kpi-icon {
  width: 56px;
  height: 56px;
  border-radius: 16px;
  background: rgba(30, 138, 230, 0.18);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.5rem;
}

.kpi-label {
  margin: 0;
  color: var(--air-text-muted);
}

.kpi-value {
  margin: 0.3rem 0 0;
  font-size: 1.7rem;
  color: var(--air-text-strong);
}

.kpi-card small {
  color: rgba(230, 240, 255, 0.5);
}

.kpi-card small.up {
  color: var(--air-success);
}

.data-table {
  width: 100%;
  border-collapse: collapse;
  margin-top: 1rem;
}

.data-table th,
.data-table td {
  padding: 0.9rem 0.6rem;
  text-align: left;
  border-bottom: 1px solid rgba(148, 163, 184, 0.2);
}

.data-table th {
  color: var(--air-text-muted);
  font-weight: 500;
  font-size: 0.9rem;
}

.data-table td {
  color: var(--air-text-strong);
}

.checkbox {
  width: 18px;
  height: 18px;
  cursor: pointer;
}

.status-tag {
  padding: 0.3rem 0.8rem;
  border-radius: 999px;
  font-size: 0.85rem;
  display: inline-block;
}

.status-tag.active {
  background: rgba(31, 209, 161, 0.15);
  color: var(--air-success);
}

.status-tag.retired {
  background: rgba(148, 163, 184, 0.15);
  color: var(--air-text-muted);
}

.action-buttons {
  display: flex;
  gap: 0.5rem;
}

.action-btn {
  padding: 0.4rem 0.8rem;
  border-radius: 8px;
  border: 1px solid transparent;
  font-size: 0.85rem;
  cursor: pointer;
  transition: all 0.3s;
}

.edit-btn {
  background: rgba(30, 138, 230, 0.15);
  color: var(--color-primary-light);
  border-color: rgba(30, 138, 230, 0.35);
}

.edit-btn:hover {
  background: rgba(30, 138, 230, 0.25);
}

.delete-btn {
  background: rgba(255, 107, 107, 0.15);
  color: var(--air-danger);
  border-color: rgba(255, 107, 107, 0.35);
  border: 1px solid rgba(255, 107, 107, 0.35);
  padding: 0.5rem 1rem;
  border-radius: 8px;
  font-size: 0.85rem;
  cursor: pointer;
  transition: all 0.3s;
}

.delete-btn:hover {
  background: rgba(255, 107, 107, 0.25);
  border-color: rgba(255, 107, 107, 0.5);
}

.loading-state,
.empty-state {
  text-align: center;
  padding: 3rem;
  color: var(--air-text-muted);
}

.seat-layout {
  font-size: 0.85rem;
  color: var(--air-text-muted);
}

/* 模态框样式 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.7);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  backdrop-filter: blur(4px);
}

.modal-content {
  background: var(--air-surface);
  border: 1px solid var(--air-border);
  border-radius: 24px;
  padding: 2rem;
  max-width: 600px;
  width: 90%;
  max-height: 85vh;
  overflow-y: auto;
  box-shadow: 0 25px 50px rgba(0, 0, 0, 0.5);

}

.batch-modal {
  max-width: 1200px;
  max-height: 90vh;
  padding: 2.5rem;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
}

.modal-header h3 {
  margin: 0;
  color: var(--air-text-strong);
}

.modal-close {
  background: none;
  border: none;
  font-size: 2rem;
  color: var(--air-text-muted);
  cursor: pointer;
  padding: 0;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: all 0.3s;
}

.modal-close:hover {
  background: rgba(255, 255, 255, 0.1);
  color: var(--air-text-strong);
}

.modal-form {
  display: flex;
  flex-direction: column;
  gap: 1.2rem;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.form-group label {
  font-size: 0.9rem;
  color: var(--air-text-muted);
}

.form-group input,
.form-group textarea,
.form-group select {
  padding: 0.65rem 0.85rem;
  border-radius: 12px;
  border: 1px solid var(--air-border-strong);
  background: var(--air-surface-muted);
  color: var(--air-text-strong);
  font-size: 0.9rem;
  font-family: inherit;
}

.form-group input:focus,
.form-group textarea:focus,
.form-group select:focus {
  outline: none;
  border-color: var(--color-primary-light);
  box-shadow: 0 0 0 3px rgba(30, 138, 230, 0.1);
}

.form-group textarea {
  resize: vertical;
  min-height: 80px;
}

.required {
  color: var(--air-danger);
}

.hint-text {
  font-size: 0.75rem;
  color: var(--air-text-muted);
  margin-top: 0.25rem;
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 0.8rem;
  margin-top: 1rem;
}

/* 批量表单样式 */
.batch-form {
  display: flex;
  flex-direction: column;
  gap: 2rem;
}

/* 快速批量添加区域 */
.quick-batch-section {
  background: rgba(30, 138, 230, 0.08);
  border: 1px solid rgba(30, 138, 230, 0.2);
  border-radius: 16px;
  padding: 1.5rem;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 1rem;
  font-size: 1rem;
  font-weight: 600;
  color: var(--air-text-strong);
}

.title-icon {
  font-size: 1.2rem;
}

.quick-batch-form {
  display: flex;
  gap: 1rem;
  align-items: flex-end;
  flex-wrap: wrap;
}

.quick-batch-group {
  flex: 1;
  min-width: 200px;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.quick-batch-group label {
  font-size: 0.85rem;
  color: var(--air-text-muted);
}

.quick-batch-group label span {
  display: block;
  margin-bottom: 0.4rem;
}

.quick-select,
.quick-input {
  padding: 0.65rem 0.85rem;
  border-radius: 12px;
  border: 1px solid var(--air-border-strong);
  background: var(--air-surface);
  color: var(--air-text-strong);
  font-size: 0.9rem;
  width: 100%;
}

.quick-select:focus,
.quick-input:focus {
  outline: none;
  border-color: var(--color-primary-light);
  box-shadow: 0 0 0 3px rgba(30, 138, 230, 0.1);
}

.quick-add-btn {
  padding: 0.65rem 1.5rem;
  white-space: nowrap;
}

.quick-add-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* 手动添加区域 */
.manual-batch-section {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.batch-form-header {
  display: flex;
  gap: 0.8rem;
  align-items: center;
  flex-wrap: wrap;
}

.batch-form-header .ghost-btn {
  display: flex;
  align-items: center;
  gap: 0.4rem;
  padding: 0.5rem 1rem;
  font-size: 0.85rem;
}

.row-count {
  margin-left: auto;
  color: var(--air-text-muted);
  font-size: 0.85rem;
}

.batch-table-container {
  max-height: 450px;
  overflow-y: auto;
  border: 1px solid var(--air-border);
  border-radius: 12px;
  background: var(--air-surface-muted);
}

.batch-table {
  width: 100%;
  border-collapse: collapse;
  background: var(--air-surface);
}

.batch-table th,
.batch-table td {
  padding: 0.75rem 0.6rem;
  text-align: left;
  border-bottom: 1px solid var(--air-border);
}

.batch-table th {
  background: var(--air-surface-muted);
  color: var(--air-text-muted);
  font-weight: 600;
  font-size: 0.85rem;
  position: sticky;
  top: 0;
  z-index: 1;
  border-bottom: 2px solid var(--air-border-strong);
}

.batch-table tbody tr:hover {
  background: rgba(30, 138, 230, 0.05);
}

.batch-table tbody tr:last-child td {
  border-bottom: none;
}

.batch-input,
.batch-select {
  width: 100%;
  padding: 0.5rem 0.7rem;
  border-radius: 8px;
  border: 1px solid var(--air-border-strong);
  background: var(--air-surface-muted);
  color: var(--air-text-strong);
  font-size: 0.85rem;
  transition: all 0.2s;
}

.batch-input:focus,
.batch-select:focus {
  outline: none;
  border-color: var(--color-primary-light);
  background: var(--air-surface);
  box-shadow: 0 0 0 2px rgba(30, 138, 230, 0.1);
}

.batch-input::placeholder {
  color: var(--air-text-muted);
  opacity: 0.6;
}

.small-btn {
  padding: 0.35rem 0.7rem;
  font-size: 0.8rem;
}

.small-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.aircraft-page {
  --air-surface: rgba(167, 192, 226, 0.82);
  --air-surface-muted: rgba(224, 230, 239, 0.7);
  --air-border: var(--color-border-light);
  --air-border-strong: var(--color-border-medium);
  --air-text-strong: var(--color-text-primary);
  --air-text-muted: var(--color-text-secondary);
  --air-pill-bg: rgba(30, 138, 230, 0.12);
  --air-pill-border: rgba(30, 138, 230, 0.35);
  --air-success: var(--color-success);
  --air-warning: var(--color-warning);
  --air-danger: var(--color-error);
  --air-info: var(--color-info);
  gap: 1.5rem;
}

/* 分页样式 */
.pagination-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 1.5rem;
  padding: 1rem;
  background: var(--air-surface-muted);
  border-radius: 12px;
  flex-wrap: wrap;
  gap: 1rem;
}

.pagination-info {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  color: var(--air-text-muted);
  font-size: 0.9rem;
}

.page-size-select {
  padding: 0.4rem 0.6rem;
  border-radius: 8px;
  border: 1px solid var(--air-border-strong);
  background: var(--air-surface);
  color: var(--air-text-strong);
  font-size: 0.85rem;
  cursor: pointer;
}

.pagination-controls {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  flex-wrap: wrap;
}

.pagination-btn {
  padding: 0.5rem 1rem;
  border-radius: 8px;
  border: 1px solid var(--air-border-strong);
  background: var(--air-surface);
  color: var(--air-text-strong);
  font-size: 0.85rem;
  cursor: pointer;
  transition: all 0.2s;
}

.pagination-btn:hover:not(:disabled) {
  background: rgba(30, 138, 230, 0.1);
  border-color: var(--color-primary-light);
}

.pagination-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.pagination-btn.small {
  padding: 0.4rem 0.8rem;
  font-size: 0.8rem;
}

.pagination-page-info {
  padding: 0 0.8rem;
  color: var(--air-text-strong);
  font-size: 0.9rem;
}

.pagination-jump {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-left: 1rem;
  color: var(--air-text-muted);
  font-size: 0.85rem;
}

.jump-input {
  width: 60px;
  padding: 0.4rem 0.6rem;
  border-radius: 8px;
  border: 1px solid var(--air-border-strong);
  background: var(--air-surface);
  color: var(--air-text-strong);
  font-size: 0.85rem;
  text-align: center;
}

.jump-input:focus {
  outline: none;
  border-color: var(--color-primary-light);
  box-shadow: 0 0 0 2px rgba(30, 138, 230, 0.1);
}
</style>

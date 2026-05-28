<template>
  <AdminLayout>
    <!-- 面包屑导航 + 返回运营控制台 -->
    <div class="breadcrumb">
      <span>航空运营</span>
      <span class="breadcrumb-separator">/</span>
      <span>运行事件记录</span>
    </div>
    <div class="logs-header">
      <button class="back-btn" @click="handleBack">
        <span class="back-icon">←</span>
        <span>返回运营控制台</span>
      </button>
      <h1 class="page-title">运行事件记录 / 操作日志</h1>
    </div>

    <!-- 搜索筛选区域 -->
    <div class="search-section">
      <div class="search-row">
        <div class="search-item">
          <label>操作类型</label>
          <select v-model="searchParams.actionType">
            <option value="">全部类型</option>
            <option value="create">新增</option>
            <option value="update">修改</option>
            <option value="delete">删除</option>
            <option value="login">登录</option>
            <option value="logout">登出</option>
            <option value="approve">审核</option>
          </select>
        </div>
        <div class="search-item">
          <label>操作人</label>
          <input 
            type="text" 
            v-model="searchParams.operator" 
            placeholder="请输入操作人姓名"
          />
        </div>
        <div class="search-item">
          <label>操作模块</label>
          <select v-model="searchParams.module">
            <option value="">全部模块</option>
            <option value="flight">航班管理</option>
            <option value="order">订单管理</option>
            <option value="user">用户管理</option>
            <option value="system">系统设置</option>
          </select>
        </div>
        <div class="search-item">
          <label>时间范围</label>
          <div class="date-range">
            <input type="date" v-model="searchParams.startDate" />
            <span class="date-separator">至</span>
            <input type="date" v-model="searchParams.endDate" />
          </div>
        </div>
        <div class="search-item">
          <button class="search-btn" @click="handleSearch">
            <span class="search-icon">🔍</span>
            查询
          </button>
          <button class="reset-btn" @click="handleReset">重置</button>
        </div>
      </div>
    </div>

    <!-- 操作按钮 -->
    <div class="action-buttons">
      <button class="btn-secondary" @click="handleExport">
        <span class="btn-icon">📥</span>
        导出日志
      </button>
      <button class="btn-danger" @click="handleClear">
        <span class="btn-icon">🗑</span>
        清空日志
      </button>
    </div>

    <!-- 日志列表 -->
    <div class="table-container">
      <table class="data-table">
        <thead>
          <tr>
            <th width="80">序号</th>
            <th width="120">操作时间</th>
            <th width="100">操作人</th>
            <th width="100">操作类型</th>
            <th width="120">操作模块</th>
            <th width="200">操作内容</th>
            <th width="150">IP地址</th>
            <th width="100">状态</th>
            <th width="120">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="(log, index) in paginatedLogs" :key="log.id">
            <td>{{ (currentPage - 1) * pageSize + index + 1 }}</td>
            <td>{{ log.operationTime }}</td>
            <td>{{ log.operator }}</td>
            <td>
              <span :class="['action-type-badge', log.actionType]">
                {{ getActionTypeText(log.actionType) }}
              </span>
            </td>
            <td>{{ getModuleText(log.module) }}</td>
            <td>
              <span class="log-content" :title="log.content">{{ log.content }}</span>
            </td>
            <td>{{ log.ipAddress }}</td>
            <td>
              <span :class="['status-badge', log.status]">
                {{ log.status === 'success' ? '成功' : '失败' }}
              </span>
            </td>
            <td>
              <button class="action-btn detail-btn" @click="handleDetail(log)">
                详情
              </button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 分页 -->
    <div class="pagination">
      <div class="pagination-info">
        共 {{ filteredLogs.length }} 条，每页 {{ pageSize }} 条
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
        <div class="page-jump">
          <span>前往</span>
          <input 
            type="number" 
            v-model.number="jumpPage" 
            :min="1" 
            :max="totalPages"
            @keyup.enter="goToPage(jumpPage)"
          />
          <span>页</span>
        </div>
      </div>
    </div>

    <!-- 详情模态框 -->
    <div class="modal-overlay" v-if="detailModal.visible" @click="closeDetailModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>操作日志详情</h3>
          <button class="close-btn" @click="closeDetailModal">×</button>
        </div>
        <div class="modal-body" v-if="detailModal.log">
          <div class="detail-row">
            <span class="detail-label">操作时间：</span>
            <span class="detail-value">{{ detailModal.log.operationTime }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">操作人：</span>
            <span class="detail-value">{{ detailModal.log.operator }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">操作类型：</span>
            <span :class="['action-type-badge', detailModal.log.actionType]">
              {{ getActionTypeText(detailModal.log.actionType) }}
            </span>
          </div>
          <div class="detail-row">
            <span class="detail-label">操作模块：</span>
            <span class="detail-value">{{ getModuleText(detailModal.log.module) }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">操作内容：</span>
            <span class="detail-value">{{ detailModal.log.content }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">IP地址：</span>
            <span class="detail-value">{{ detailModal.log.ipAddress }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">状态：</span>
            <span :class="['status-badge', detailModal.log.status]">
              {{ detailModal.log.status === 'success' ? '成功' : '失败' }}
            </span>
          </div>
          <div class="detail-row" v-if="detailModal.log.errorMessage">
            <span class="detail-label">错误信息：</span>
            <span class="detail-value error">{{ detailModal.log.errorMessage }}</span>
          </div>
        </div>
      </div>
    </div>
  </AdminLayout>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import AdminLayout from '../components/AdminLayout.vue'

interface Log {
  id: string
  operationTime: string
  operator: string
  actionType: 'create' | 'update' | 'delete' | 'login' | 'logout' | 'approve'
  module: 'flight' | 'order' | 'user' | 'system'
  content: string
  ipAddress: string
  status: 'success' | 'failed'
  errorMessage?: string
}

const router = useRouter()

const searchParams = reactive({
  actionType: '',
  operator: '',
  module: '',
  startDate: '',
  endDate: ''
})

const logs = ref<Log[]>([
  {
    id: '1',
    operationTime: '2024-01-15 10:30:25',
    operator: '系统管理员',
    actionType: 'create',
    module: 'flight',
    content: '新增航班：CA1234 北京-上海',
    ipAddress: '192.168.1.100',
    status: 'success'
  },
  {
    id: '2',
    operationTime: '2024-01-15 09:15:10',
    operator: '运营经理',
    actionType: 'approve',
    module: 'order',
    content: '批准订单：ORD001',
    ipAddress: '192.168.1.101',
    status: 'success'
  },
  {
    id: '3',
    operationTime: '2024-01-15 08:45:33',
    operator: '系统管理员',
    actionType: 'update',
    module: 'user',
    content: '修改用户权限：用户ID 123',
    ipAddress: '192.168.1.100',
    status: 'success'
  },
  {
    id: '4',
    operationTime: '2024-01-14 16:20:15',
    operator: '客服专员',
    actionType: 'delete',
    module: 'order',
    content: '删除订单：ORD002',
    ipAddress: '192.168.1.102',
    status: 'failed',
    errorMessage: '权限不足，无法删除订单'
  }
])

const currentPage = ref(1)
const pageSize = ref(5)
const jumpPage = ref(1)

const detailModal = reactive({
  visible: false,
  log: null as Log | null
})

const getActionTypeText = (type: string) => {
  const map: Record<string, string> = {
    create: '新增',
    update: '修改',
    delete: '删除',
    login: '登录',
    logout: '登出',
    approve: '审核'
  }
  return map[type] || type
}

const handleBack = () => {
  router.push('/portal/operations')
}

const getModuleText = (module: string) => {
  const map: Record<string, string> = {
    flight: '航班管理',
    order: '订单管理',
    user: '用户管理',
    system: '系统设置'
  }
  return map[module] || module
}

const filteredLogs = computed(() => {
  let result = logs.value

  if (searchParams.actionType) {
    result = result.filter(log => log.actionType === searchParams.actionType)
  }
  if (searchParams.operator) {
    result = result.filter(log => log.operator.includes(searchParams.operator))
  }
  if (searchParams.module) {
    result = result.filter(log => log.module === searchParams.module)
  }
  if (searchParams.startDate) {
    result = result.filter(log => log.operationTime >= searchParams.startDate)
  }
  if (searchParams.endDate) {
    result = result.filter(log => log.operationTime <= searchParams.endDate + ' 23:59:59')
  }

  return result
})

const totalPages = computed(() => Math.ceil(filteredLogs.value.length / pageSize.value))

const paginatedLogs = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredLogs.value.slice(start, end)
})

const handleSearch = () => {
  currentPage.value = 1
  // TODO: 调用后端API搜索日志
}

const handleReset = () => {
  searchParams.actionType = ''
  searchParams.operator = ''
  searchParams.module = ''
  searchParams.startDate = ''
  searchParams.endDate = ''
  currentPage.value = 1
}

const handleExport = () => {
  alert('导出日志功能')
  // TODO: 调用后端API导出日志
}

const handleClear = () => {
  if (confirm('确定要清空所有日志吗？此操作不可恢复！')) {
    alert('清空日志功能')
    // TODO: 调用后端API清空日志
  }
}

const handleDetail = (log: Log) => {
  detailModal.log = log
  detailModal.visible = true
}

const closeDetailModal = () => {
  detailModal.visible = false
  detailModal.log = null
}

const goToPage = (page: number) => {
  if (page >= 1 && page <= totalPages.value) {
    currentPage.value = page
    jumpPage.value = page
  }
}
</script>

<style scoped>
/* 基础样式内联 */
.breadcrumb {
  margin-bottom: 12px;
  font-size: 14px;
  color: rgba(255, 255, 255, 0.7);
}

.breadcrumb-separator {
  margin: 0 8px;
  color: rgba(255, 255, 255, 0.4);
}

.logs-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
  margin-bottom: 16px;
  flex-wrap: wrap;
}

.page-title {
  margin: 0;
  font-size: 1.2rem;
  color: #f9fafb;
}

.back-btn {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.45rem 1.1rem;
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 999px;
  background: rgba(15, 23, 42, 0.7);
  color: rgba(255, 255, 255, 0.9);
  font-size: 0.85rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.25s ease;
  backdrop-filter: blur(10px);
}

.back-btn:hover {
  background: rgba(99, 102, 241, 0.25);
  border-color: rgba(99, 102, 241, 0.5);
  color: #c7d2fe;
  transform: translateX(-3px);
}

.back-icon {
  font-size: 1rem;
  font-weight: 700;
}

.search-section {
  background: rgba(2, 6, 23, 0.7);
  padding: 20px;
  border-radius: 16px;
  margin-bottom: 20px;
  border: 1px solid rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(18px);
  animation: sys-search-fade-in 0.45s ease-out;
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
  animation: sys-actions-fade-in 0.45s ease-out 0.05s both;
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
  transform-origin: center;
}

.btn-primary {
  background: linear-gradient(135deg, #1E8AE6, #0A2F63);
  color: #fff;
}

.btn-primary:hover {
  transform: translateY(-2px) scale(1.01);
  box-shadow: 0 10px 24px rgba(37, 99, 235, 0.45);
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
  animation: sys-table-rise-in 0.5s ease-out 0.08s both;
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
  background: radial-gradient(circle at left, rgba(129, 140, 248, 0.18), rgba(15, 23, 42, 0.6));
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(15, 23, 42, 0.7);
}

.status-tag {
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
  display: inline-block;
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
  z-index: 10000;
  padding: 20px;
  box-sizing: border-box;
  overflow-y: auto;
  overscroll-behavior: contain;
  backdrop-filter: blur(4px);
}

.modal-content {
  background: rgba(2, 6, 23, 0.95);
  border-radius: 16px;
  width: 90%;
  max-width: 500px;
  max-height: 80vh;
  overflow-y: auto;
  border: 1px solid rgba(255, 255, 255, 0.1);
  animation: sys-modal-pop 0.28s ease-out;
}

.modal-header {
  padding: 20px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.modal-header h3 {
  margin: 0;
  color: #fff;
}

.close-btn {
  background: none;
  border: none;
  font-size: 24px;
  color: rgba(255, 255, 255, 0.6);
  cursor: pointer;
  transition: transform 0.2s ease, color 0.2s ease;
}

.close-btn:hover {
  transform: rotate(90deg);
  color: rgba(248, 250, 252, 0.95);
}

.modal-body {
  padding: 20px;
}

.log-content {
  display: block;
  max-width: 200px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.action-type-badge {
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}

.action-type-badge.create {
  background: rgba(76, 175, 80, 0.2);
  color: #81c784;
  border: 1px solid rgba(76, 175, 80, 0.3);
}

.action-type-badge.update {
  background: rgba(255, 152, 0, 0.2);
  color: #ffb74d;
  border: 1px solid rgba(255, 152, 0, 0.3);
}

.action-type-badge.delete {
  background: rgba(244, 67, 54, 0.2);
  color: #ef5350;
  border: 1px solid rgba(244, 67, 54, 0.3);
}

.action-type-badge.login,
.action-type-badge.logout {
  background: rgba(99, 102, 241, 0.2);
  color: #0A2F63;
  border: 1px solid rgba(99, 102, 241, 0.3);
}

.action-type-badge.approve {
  background: rgba(34, 197, 94, 0.2);
  color: #4ade80;
  border: 1px solid rgba(34, 197, 94, 0.3);
}

/* 系统管理页面微动画（与用户管理保持一致的动效体系） */
@keyframes sys-search-fade-in {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes sys-actions-fade-in {
  from {
    opacity: 0;
    transform: translateY(6px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes sys-table-rise-in {
  from {
    opacity: 0;
    transform: translateY(12px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes sys-modal-pop {
  from {
    opacity: 0;
    transform: translateY(14px) scale(0.98);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.status-badge.success {
  background: rgba(34, 197, 94, 0.2);
  color: #4ade80;
  border: 1px solid rgba(34, 197, 94, 0.3);
}

.status-badge.failed {
  background: rgba(244, 67, 54, 0.2);
  color: #ef5350;
  border: 1px solid rgba(244, 67, 54, 0.3);
}

.detail-value.error {
  color: #ef5350;
}
</style>


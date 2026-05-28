<template>
  <AdminLayout>
    <!-- 面包屑导航 -->
    <div class="breadcrumb">
      <span>首页</span>
      <span class="breadcrumb-separator">/</span>
      <span>机票退订管理</span>
    </div>

    <!-- 搜索筛选区域 -->
    <div class="search-section">
      <div class="search-row">
        <div class="search-item">
          <label>订单号</label>
          <input type="text" v-model="searchParams.orderNumber" placeholder="订单号" />
        </div>
        <div class="search-item">
          <label>乘客姓名</label>
          <input type="text" v-model="searchParams.passengerName" placeholder="乘客姓名" />
        </div>
        <div class="search-item">
          <label>申请日期</label>
          <div class="date-range">
            <input type="date" v-model="searchParams.startDate" />
            <span class="date-separator">至</span>
            <input type="date" v-model="searchParams.endDate" />
          </div>
        </div>
        <div class="search-item">
          <label>状态</label>
          <select v-model="searchParams.status">
            <option value="">全部</option>
            <option value="pending">待审核</option>
            <option value="approved">已批准</option>
            <option value="rejected">已拒绝</option>
          </select>
        </div>
        <div class="search-item">
          <button class="search-btn" @click="handleSearch">
            <span class="search-icon">Q</span>
            查询
          </button>
        </div>
      </div>
    </div>

    <!-- 操作按钮 -->
    <div class="action-buttons">
      <button class="btn-primary" @click="handleBatchApprove">
        <span class="btn-icon">✓</span>
        批量批准
      </button>
      <button class="btn-danger" @click="handleBatchReject">
        <span class="btn-icon">✗</span>
        批量拒绝
      </button>
    </div>

    <!-- 数据表格 -->
    <div class="table-container">
      <table class="data-table">
        <thead>
          <tr>
            <th width="50">
              <input type="checkbox" v-model="selectAll" @change="toggleSelectAll" />
            </th>
            <th width="60">索引</th>
            <th width="120">订单号</th>
            <th width="100">航班号</th>
            <th width="120">乘客姓名</th>
            <th width="150">申请时间</th>
            <th width="200">退订原因</th>
            <th width="100">退订金额</th>
            <th width="100">状态</th>
            <th width="180">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="(refund, index) in refunds" :key="refund.id">
            <td>
              <input type="checkbox" v-model="selectedRefunds" :value="refund.id" />
            </td>
            <td>{{ index + 1 }}</td>
            <td>{{ refund.orderNumber }}</td>
            <td>{{ refund.flightNumber }}</td>
            <td>{{ refund.passengerName }}</td>
            <td>{{ refund.applyTime }}</td>
            <td>{{ refund.reason }}</td>
            <td>¥{{ refund.amount }}</td>
            <td>
              <span :class="['status-badge', refund.status]">
                {{ getStatusText(refund.status) }}
              </span>
            </td>
            <td>
              <button class="action-btn approve-btn" @click="handleApprove(refund)">
                批准
              </button>
              <button class="action-btn reject-btn" @click="handleReject(refund)">
                拒绝
              </button>
              <button class="action-btn detail-btn" @click="handleDetail(refund)">
                详情
              </button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 分页控件 -->
    <div class="pagination">
      <div class="pagination-info">
        共{{ total }}条
      </div>
      <div class="pagination-controls">
        <button 
          class="page-btn" 
          :disabled="currentPage === 1"
          @click="goToPage(currentPage - 1)"
        >
          &lt;
        </button>
        <span class="page-number">{{ currentPage }}</span>
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
  </AdminLayout>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import AdminLayout from '../components/AdminLayout.vue'

interface Refund {
  id: string
  orderNumber: string
  flightNumber: string
  passengerName: string
  applyTime: string
  reason: string
  amount: number
  status: 'pending' | 'approved' | 'rejected'
}

const searchParams = reactive({
  orderNumber: '',
  passengerName: '',
  startDate: '',
  endDate: '',
  status: ''
})

const refunds = ref<Refund[]>([
  {
    id: '1',
    orderNumber: 'ORD001',
    flightNumber: 'CA1234',
    passengerName: '张三',
    applyTime: '2024-01-15 10:30',
    reason: '行程变更',
    amount: 680,
    status: 'pending'
  },
  {
    id: '2',
    orderNumber: 'ORD002',
    flightNumber: 'MU5678',
    passengerName: '李四',
    applyTime: '2024-01-16 14:20',
    reason: '个人原因',
    amount: 720,
    status: 'approved'
  }
])

const selectedRefunds = ref<string[]>([])
const selectAll = ref(false)
const currentPage = ref(1)
const pageSize = ref(5)
const total = ref(2)
const jumpPage = ref(1)

const totalPages = computed(() => Math.ceil(total.value / pageSize.value))

const getStatusText = (status: string) => {
  const statusMap: Record<string, string> = {
    pending: '待审核',
    approved: '已批准',
    rejected: '已拒绝'
  }
  return statusMap[status] || status
}

const toggleSelectAll = () => {
  if (selectAll.value) {
    selectedRefunds.value = refunds.value.map(r => r.id)
  } else {
    selectedRefunds.value = []
  }
}

const handleSearch = () => {
  console.log('搜索参数:', searchParams)
  currentPage.value = 1
}

const handleBatchApprove = () => {
  if (selectedRefunds.value.length === 0) {
    alert('请选择要批准的项目')
    return
  }
  if (confirm(`确定要批准选中的 ${selectedRefunds.value.length} 条退订申请吗？`)) {
    console.log('批量批准:', selectedRefunds.value)
    selectedRefunds.value = []
    selectAll.value = false
  }
}

const handleBatchReject = () => {
  if (selectedRefunds.value.length === 0) {
    alert('请选择要拒绝的项目')
    return
  }
  if (confirm(`确定要拒绝选中的 ${selectedRefunds.value.length} 条退订申请吗？`)) {
    console.log('批量拒绝:', selectedRefunds.value)
    selectedRefunds.value = []
    selectAll.value = false
  }
}

const handleApprove = (refund: Refund) => {
  if (confirm(`确定要批准订单 ${refund.orderNumber} 的退订申请吗？`)) {
    console.log('批准退订:', refund.id)
  }
}

const handleReject = (refund: Refund) => {
  if (confirm(`确定要拒绝订单 ${refund.orderNumber} 的退订申请吗？`)) {
    console.log('拒绝退订:', refund.id)
  }
}

const handleDetail = (refund: Refund) => {
  console.log('查看详情:', refund)
}

const goToPage = (page: number) => {
  if (page >= 1 && page <= totalPages.value) {
    currentPage.value = page
    jumpPage.value = page
  }
}
</script>

<style scoped>
.breadcrumb {
  margin: 20px;
  margin-bottom: 20px;
  font-size: 14px;
  color: rgba(255, 255, 255, 0.7);
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
  margin-bottom: 15px;
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
}

.search-item input,
.search-item select {
  padding: 6px 12px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 8px;
  font-size: 14px;
  width: 150px;
  background: rgba(255, 255, 255, 0.1);
  color: #fff;
  transition: all 0.3s;
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

.date-range {
  display: flex;
  align-items: center;
  gap: 8px;
}

.date-range input {
  width: 140px;
}

.date-separator {
  color: rgba(255, 255, 255, 0.5);
  font-size: 13px;
}

.search-btn {
  padding: 8px 20px;
  background: linear-gradient(135deg, rgba(99, 102, 241, 0.8), rgba(139, 92, 246, 0.8));
  color: #fff;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 5px;
  transition: all 0.3s;
  box-shadow: 0 4px 15px rgba(99, 102, 241, 0.3);
}

.search-btn:hover {
  background: linear-gradient(135deg, rgba(99, 102, 241, 1), rgba(139, 92, 246, 1));
  box-shadow: 0 6px 20px rgba(99, 102, 241, 0.4);
  transform: translateY(-1px);
}

.search-icon {
  font-weight: bold;
}

/* 操作按钮 */
.action-buttons {
  margin: 20px;
  margin-bottom: 15px;
  display: flex;
  gap: 10px;
}

.btn-primary,
.btn-danger {
  padding: 8px 16px;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 5px;
  transition: all 0.3s;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.2);
}

.btn-primary {
  background: linear-gradient(135deg, rgba(52, 211, 153, 0.8), rgba(16, 185, 129, 0.8));
  color: #fff;
}

.btn-primary:hover {
  background: linear-gradient(135deg, rgba(52, 211, 153, 1), rgba(16, 185, 129, 1));
  box-shadow: 0 6px 20px rgba(52, 211, 153, 0.3);
  transform: translateY(-1px);
}

.btn-danger {
  background: linear-gradient(135deg, rgba(248, 113, 113, 0.8), rgba(239, 68, 68, 0.8));
  color: #fff;
}

.btn-danger:hover {
  background: linear-gradient(135deg, rgba(248, 113, 113, 1), rgba(239, 68, 68, 1));
  box-shadow: 0 6px 20px rgba(248, 113, 113, 0.3);
  transform: translateY(-1px);
}

.btn-icon {
  font-size: 16px;
}

/* 表格容器 */
.table-container {
  background: rgba(15, 23, 42, 0.6);
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);
  margin: 20px;
  margin-bottom: 15px;
  overflow-x: auto;
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.data-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 13px;
}

.data-table thead {
  background: rgba(99, 102, 241, 0.2);
  color: #fff;
}

.data-table th {
  padding: 12px 8px;
  text-align: left;
  font-weight: 500;
  white-space: nowrap;
  color: rgba(255, 255, 255, 0.9);
  border-bottom: 2px solid rgba(99, 102, 241, 0.3);
}

.data-table td {
  padding: 12px 8px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  color: rgba(255, 255, 255, 0.8);
}

.data-table tbody tr:hover {
  background: rgba(99, 102, 241, 0.1);
}

.data-table tbody tr:last-child td {
  border-bottom: none;
}

.status-badge {
  padding: 4px 10px;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 500;
}

.status-badge.pending {
  background: rgba(251, 191, 36, 0.2);
  color: #fbbf24;
  border: 1px solid rgba(251, 191, 36, 0.3);
}

.status-badge.approved {
  background: rgba(52, 211, 153, 0.2);
  color: #34d399;
  border: 1px solid rgba(52, 211, 153, 0.3);
}

.status-badge.rejected {
  background: rgba(248, 113, 113, 0.2);
  color: #f87171;
  border: 1px solid rgba(248, 113, 113, 0.3);
}

.action-btn {
  padding: 4px 10px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 12px;
  margin-right: 5px;
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

.detail-btn {
  background: linear-gradient(135deg, rgba(96, 165, 250, 0.8), rgba(59, 130, 246, 0.8));
  color: #fff;
}

.detail-btn:hover {
  background: linear-gradient(135deg, rgba(96, 165, 250, 1), rgba(59, 130, 246, 1));
  box-shadow: 0 4px 12px rgba(96, 165, 250, 0.4);
  transform: translateY(-2px);
}

/* 分页 */
.pagination {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 20px;
  margin: 0 20px;
}

.pagination-info {
  color: rgba(255, 255, 255, 0.7);
  font-size: 14px;
}

.pagination-controls {
  display: flex;
  align-items: center;
  gap: 10px;
}

.page-btn {
  padding: 6px 12px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  background: rgba(255, 255, 255, 0.1);
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s;
  color: rgba(255, 255, 255, 0.8);
}

.page-btn:hover:not(:disabled) {
  background: rgba(255, 255, 255, 0.15);
  border-color: rgba(255, 255, 255, 0.3);
  color: #fff;
}

.page-btn:disabled {
  opacity: 0.3;
  cursor: not-allowed;
}

.page-number {
  padding: 6px 12px;
  font-size: 14px;
  color: rgba(255, 255, 255, 0.9);
  font-weight: 500;
}

.page-jump {
  display: flex;
  align-items: center;
  gap: 5px;
  margin-left: 10px;
}

.page-jump input {
  width: 50px;
  padding: 6px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 8px;
  text-align: center;
  font-size: 14px;
  background: rgba(255, 255, 255, 0.1);
  color: #fff;
}

.page-jump input:focus {
  outline: none;
  border-color: rgba(99, 102, 241, 0.6);
  background: rgba(255, 255, 255, 0.15);
}

.page-jump span {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.7);
}
</style>


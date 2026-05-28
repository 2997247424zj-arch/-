<template>
  <AdminLayout>
    <div class="admin-special-requests">
      <header class="page-header">
        <div>
          <p class="page-label">运营管理</p>
          <h1>重点旅客预约（管理员）</h1>
          <p>按状态筛选、审批、指派运营处理人员</p>
        </div>
        <div class="page-actions">
          <select v-model="filterStatus" @change="onFilterChange">
            <option value="">全部</option>
            <option value="pending">待处理</option>
            <option value="approved">已批准</option>
            <option value="processing">处理中</option>
            <option value="completed">已完成</option>
            <option value="rejected">已拒绝</option>
          </select>
          <button class="btn btn-ghost" @click="handleExport" :disabled="loading" style="display:flex;align-items:center;gap:4px;">
            <span>📤</span> 导出报表
          </button>
        </div>
      </header>

  <section class="glass-card list-section">
        <div v-if="loading" class="loading-spot">
          <div class="spinner"></div>
          <div class="loading-text">加载中...</div>
        </div>
        <div v-else>
          <div v-if="requests.length === 0" class="empty-spot">暂无预约记录</div>
          <transition-group name="list" tag="div" class="request-list" v-else>
            <div v-for="r in requests" :key="r.id" class="request-item" :data-id="r.id">
              <div class="meta">
                <div class="card-row">
                  <div class="card-title">订单：<span class="muted">{{ r.orderNo }}</span></div>
                  <div class="status-badge" :class="`status-${r.status || 'unknown'}`">{{ (r.status || 'unknown').toUpperCase() }}</div>
                </div>
                <div class="card-body">
                  <div><strong>乘客ID：</strong><span class="muted">{{ r.passengerId }}</span></div>
                  <div><strong>电话：</strong><span class="muted">{{ r.phone }}</span></div>
                  <div><strong>类型：</strong><span class="muted">{{ r.passengerType }}</span></div>
                </div>
              </div>
              <div class="card-actions">
                <button class="btn btn-primary" @click="approve(r.id)" @mouseover="onHover" @mouseout="onLeave">批准</button>
                <button class="btn btn-ghost" @click="rejectPrompt(r.id)">拒绝</button>
                <button class="btn btn-ghost" @click="openAssignModal(r.id)">指派</button>
              </div>
            </div>
          </transition-group>
          <div v-if="!requests.length && rawResponse" class="debug-response">
            <h4>调试：后端原始响应（JSON）</h4>
            <pre style="white-space:pre-wrap; max-height:220px; overflow:auto; background:rgba(0,0,0,0.5); padding:10px; border-radius:6px;">{{ JSON.stringify(rawResponse, null, 2) }}</pre>
          </div>
        </div>
      </section>
      <!-- 分页控制 -->
      <div class="pagination-controls" v-if="totalPages > 0" style="display:flex;align-items:center;justify-content:flex-end;gap:12px;margin-top:14px;">
        <div class="page-info" style="color:var(--color-text-secondary)">共 {{ total }} 条 / 第 {{ currentPage + 1 }} / {{ totalPages }} 页</div>
        <select v-model.number="pageSize" @change="onPageSizeChange">
          <option :value="5">5 / 页</option>
          <option :value="10">10 / 页</option>
          <option :value="20">20 / 页</option>
          <option :value="50">50 / 页</option>
        </select>
        <button class="btn btn-ghost" :disabled="currentPage <= 0" @click="prevPage">上一页</button>
        <button class="btn btn-ghost" :disabled="currentPage >= totalPages - 1" @click="nextPage">下一页</button>
      </div>
    <!-- 指派 Modal -->
    <transition name="fade-scale">
      <div v-if="showAssignModal" class="modal-backdrop" style="position:fixed;inset:0;display:flex;align-items:center;justify-content:center;z-index:1200;">
        <div class="modal-card modal-glass" style="width:640px;padding:20px;border-radius:12px;">
          <div class="modal-header">
            <h3>指派运营人员</h3>
            <button class="close-btn" @click="closeAssignModal">×</button>
          </div>
          <div v-if="loadingOperators" class="modal-loading" style="padding:12px">加载运营人员中...</div>
          <div v-else class="modal-body">
            <div class="operator-search" style="display:flex;gap:8px;align-items:center;margin-bottom:8px;">
              <input v-model="operatorSearch" placeholder="搜索姓名/用户名/手机号（可选）" style="flex:1;padding:8px;border-radius:6px;border:1px solid rgba(255,255,255,0.04);background:rgba(255,255,255,0.02);color:var(--color-text-primary)" />
              <button class="btn btn-ghost" @click="loadOperators">搜索</button>
              <button class="btn btn-ghost" @click="loadOperators">刷新</button>
            </div>
            <div v-if="operators.length === 0" class="empty-operators" style="padding:12px">
              未找到可用的运营人员。你也可以手动输入运营人员ID进行指派：
              <div style="margin-top:8px;display:flex;gap:8px;align-items:center;">
                <input v-model="manualOperatorId" placeholder="运营人员 ID" style="padding:8px;border-radius:6px;border:1px solid rgba(255,255,255,0.04);background:rgba(255,255,255,0.02);color:var(--color-text-primary)" />
                <button class="btn btn-primary" @click="confirmAssignManual">确认指派</button>
              </div>
            </div>
            <div v-else class="operators-list" style="max-height:320px;overflow:auto;margin-top:8px;">
              <div
                v-for="op in operators"
                :key="op.id"
                class="operator-item"
                :class="{ selected: selectedOperator === (op.id || op.userId) }"
                @click="selectedOperator = (op.id || op.userId)"
              >
                <div class="avatar">{{ (op.realName || op.name || op.username || '').slice(0,1).toUpperCase() }}</div>
                <div class="operator-info">
                  <div class="operator-name">{{ op.realName || op.name || op.username }}</div>
                  <div class="operator-meta">{{ op.phone || op.email || '' }} <span class="muted"> {{ op.department || '' }}</span></div>
                </div>
                <div class="operator-action">
                  <input type="radio" :value="op.id || op.userId" v-model="selectedOperator" />
                </div>
              </div>
            </div>
            <div class="modal-footer" style="display:flex;justify-content:flex-end;gap:12px;margin-top:14px;">
              <button class="btn btn-ghost" @click="closeAssignModal">取消</button>
              <button class="btn btn-primary" @click="confirmAssign">确认指派</button>
            </div>
          </div>
        </div>
      </div>
    </transition>
    </div>
  </AdminLayout>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import AdminLayout from '../../components/AdminLayout.vue'
import { adminSpecialServiceRequestApi, userManagementApi } from '../../services/api'

const requests = ref<any[]>([])
const loading = ref(false)
const filterStatus = ref('')
const rawResponse = ref<any>(null)
// 分页相关
const currentPage = ref(0)
const pageSize = ref(5)
const totalPages = ref(0)
const total = ref(0)

const loadRequests = async (page: number = 0) => {
  loading.value = true
  try {
    const res: any = await adminSpecialServiceRequestApi.getList({
      page,
      size: pageSize.value,
      status: filterStatus.value || undefined
    })
    console.log('adminSpecialServiceRequestApi.getList ->', res)
    const data = res?.data || res || {}
    requests.value = data.list || []
    total.value = data.total || 0
    pageSize.value = data.size ?? pageSize.value
    currentPage.value = data.page ?? page
    totalPages.value = data.totalPages ?? Math.ceil((total.value || 0) / (pageSize.value || 5))
    rawResponse.value = res
  } catch (e) {
    console.error('加载失败', e)
    requests.value = []
  } finally {
    loading.value = false
  }
}

const approve = async (id: number) => {
  try {
    await adminSpecialServiceRequestApi.approve(id)
    await loadRequests()
    alert('已批准')
  } catch (e) {
    alert('批准失败')
  }
}

const rejectPrompt = async (id: number) => {
  const reason = prompt('填写拒绝原因（可选）') || ''
  try {
    await adminSpecialServiceRequestApi.reject(id, reason)
    await loadRequests()
    alert('已拒绝')
  } catch (e) {
    alert('拒绝失败')
  }
}

const assignPrompt = async (id: number) => {
  const operatorIdStr = prompt('输入要指派的运营人员ID') || ''
  const operatorId = operatorIdStr ? parseInt(operatorIdStr, 10) : null
  if (!operatorId) return
  try {
    await adminSpecialServiceRequestApi.assign(id, operatorId)
    await loadRequests()
    alert('已指派')
  } catch (e) {
    alert('指派失败')
  }
}

// Modal 指派流程
const showAssignModal = ref(false)
const operators = ref<any[]>([])
const loadingOperators = ref(false)
const assignRequestId = ref<number | null>(null)
const selectedOperator = ref<number | null>(null)

const openAssignModal = async (id: number) => {
  assignRequestId.value = id
  showAssignModal.value = true
  loadingOperators.value = true
  try {
    await loadOperators()
  } finally {
    loadingOperators.value = false
  }
}

const confirmAssign = async () => {
  if (!assignRequestId.value || !selectedOperator.value) {
    alert('请选择要指派的运营人员')
    return
  }
  try {
    await adminSpecialServiceRequestApi.assign(assignRequestId.value, selectedOperator.value)
    showAssignModal.value = false
    selectedOperator.value = null
    assignRequestId.value = null
    await loadRequests(currentPage.value)
    alert('已指派')
  } catch (e) {
    console.error('指派失败', e)
    alert('指派失败')
  }
}

const closeAssignModal = () => {
  showAssignModal.value = false
  selectedOperator.value = null
  assignRequestId.value = null
}

const onPageSizeChange = () => {
  loadRequests(0)
}

const prevPage = () => {
  if (currentPage.value > 0) {
    loadRequests(currentPage.value - 1)
  }
}

const nextPage = () => {
  if (currentPage.value < totalPages.value - 1) {
    loadRequests(currentPage.value + 1)
  }
}

const onFilterChange = () => {
  loadRequests(0)
}

const onAutoRefresh = async () => {
  try {
    if (document.hidden) return
    await loadRequests(currentPage.value)
  } catch (e) {
    console.warn('auto-refresh loadRequests failed', e)
  }
}

onMounted(() => {
  loadRequests(0)
  window.addEventListener('auto-refresh', onAutoRefresh)
})

onUnmounted(() => {
  window.removeEventListener('auto-refresh', onAutoRefresh)
})
// 微交互占位函数（hover 效果可扩展）
const onHover = (_e: Event) => {}
const onLeave = (_e: Event) => {}

const handleExport = async () => {
  try {
    loading.value = true
    // 获取当当前筛选状态下的全部数据（不分页或取足够大的size）
    const res: any = await adminSpecialServiceRequestApi.getList({
      page: 0,
      size: 2000,
      status: filterStatus.value || undefined
    })
    const data = (res?.data?.list || res?.list || []) as any[]
    
    if (data.length === 0) {
      alert('没有可导出的数据')
      return
    }

    // CSV 表头
    const headers = ['ID', '订单号', '乘客ID', '电话', '旅客类型', '流程备注', '状态', '指派运营ID', '创建日期']
    const csvContent = [
      '\ufeff' + headers.join(','), // 添加 BOM 以支持 Excel 中文
      ...data.map(r => [
        r.id,
        r.orderNo || '',
        r.passengerId || '',
        r.phone || '',
        r.passengerType || '',
        (r.flowRemark || '').replace(/,/g, ' '),
        r.status || '',
        r.operatorId || '',
        r.createdAt || ''
      ].map(field => `"${field}"`).join(','))
    ].join('\n')

    const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' })
    const link = document.createElement('a')
    const url = URL.createObjectURL(blob)
    const fileName = `重点旅客预约报表_${new Date().toLocaleDateString()}.csv`
    
    link.setAttribute('href', url)
    link.setAttribute('download', fileName)
    link.style.visibility = 'hidden'
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    
    alert('导出成功')
  } catch (e) {
    console.error('导出失败', e)
    alert('导出报表失败')
  } finally {
    loading.value = false
  }
}

// operator search / manual assign helpers
const operatorSearch = ref('')
const manualOperatorId = ref('')

const loadOperators = async () => {
  loadingOperators.value = true
  try {
    const params: any = { page: 0, size: 200, role: 'operator' }
    if (operatorSearch.value && operatorSearch.value.trim()) params.keyword = operatorSearch.value.trim()
    const res: any = await userManagementApi.getUserList(params)
    const list = res?.data?.list || res?.data || res?.list || []
    operators.value = Array.isArray(list) ? list : []
  } catch (e) {
    console.error('加载运营人员失败', e)
    operators.value = []
  } finally {
    loadingOperators.value = false
  }
}

const confirmAssignManual = async () => {
  const idStr = manualOperatorId.value?.trim()
  if (!assignRequestId.value || !idStr) {
    alert('请输入运营人员ID')
    return
  }
  const operatorId = parseInt(idStr, 10)
  if (isNaN(operatorId)) {
    alert('运营人员ID格式不正确')
    return
  }
  try {
    await adminSpecialServiceRequestApi.assign(assignRequestId.value, operatorId)
    showAssignModal.value = false
    manualOperatorId.value = ''
    await loadRequests(currentPage.value)
    alert('已指派（手动 ID）')
  } catch (e) {
    console.error('手动指派失败', e)
    alert('手动指派失败')
  }
}
</script>

<style scoped>
.request-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.request-item {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 18px;
  border-radius: 12px;
  background: linear-gradient(180deg, var(--bg-primary), var(--bg-secondary));
  border: 1px solid var(--border-color);
  box-shadow: var(--shadow-sm);
}
.meta { display:flex; flex-direction:column; gap:8px; color:var(--text-secondary); }
.meta strong { color:var(--text-primary); margin-right:6px; }
.muted { color: var(--text-secondary); }
.card-row { display:flex; align-items:center; gap:12px; justify-content:space-between; width:100%; }
.card-title { font-size: 16px; font-weight:700; color:var(--text-primary); }
.status-badge {
  padding: 6px 10px;
  border-radius: 999px;
  font-size: 12px;
  font-weight:600;
  color:var(--color-white);
  border: 1px solid transparent;
}
.status-pending { background: color-mix(in srgb, var(--color-warning, #f59e0b) 12%, transparent); color:var(--color-warning); border-color: color-mix(in srgb, var(--color-warning, #f59e0b) 18%, transparent); }
.status-approved { background: color-mix(in srgb, var(--color-success, #10b981) 12%, transparent); color:var(--color-success); border-color: color-mix(in srgb, var(--color-success, #10b981) 18%, transparent); }
.status-processing { background: color-mix(in srgb, var(--color-primary, #2563eb) 12%, transparent); color:var(--color-primary); border-color: color-mix(in srgb, var(--color-primary, #2563eb) 18%, transparent); }
.status-completed { background: color-mix(in srgb, var(--color-primary-dark, #1e40af) 12%, transparent); color:var(--color-primary-dark); border-color: color-mix(in srgb, var(--color-primary-dark, #1e40af) 18%, transparent); }
.status-rejected { background: color-mix(in srgb, var(--color-error, #ef4444) 12%, transparent); color:var(--color-error); border-color: color-mix(in srgb, var(--color-error, #ef4444) 18%, transparent); }

.card-actions { display:flex; gap:10px; align-items:center; }
.btn { padding:6px 12px; border-radius:999px; font-size:13px; border:none; cursor:pointer; transition: all 180ms ease; }
.btn-primary { background: linear-gradient(135deg,var(--color-primary),var(--color-primary-dark)); color:var(--color-white); border:1px solid color-mix(in srgb, var(--color-primary, #2563eb) 12%, transparent); box-shadow: 0 8px 20px rgba(37,99,235,0.12); }
.btn-ghost { background: transparent; color:var(--text-primary); border:1px solid var(--border-color); }

/* smaller screens */
@media (max-width: 900px) {
  .request-item { flex-direction:column; gap:12px; }
  .card-actions { justify-content:flex-end; width:100%; }
}
.pagination-controls .page-info { margin-right:8px; }
.pagination-controls select { background: rgba(255,255,255,0.03); color:var(--color-text-primary); border-radius:6px; padding:6px; border:1px solid rgba(255,255,255,0.04); }
.pagination-controls .btn { padding:6px 10px; }
</style>

<!-- 动画效果 -->
<style scoped>
.list-enter-active, .list-leave-active {
  transition: all 350ms cubic-bezier(.2,.8,.2,1);
}
.list-enter-from {
  transform: translateY(8px);
  opacity: 0;
}
.list-enter-to {
  transform: translateY(0);
  opacity: 1;
}
.list-leave-from {
  transform: translateY(0);
  opacity: 1;
}
.list-leave-to {
  transform: translateY(-8px);
  opacity: 0;
}

.fade-scale-enter-active, .fade-scale-leave-active {
  transition: all 240ms cubic-bezier(.2,.9,.2,1);
}
.fade-scale-enter-from {
  transform: scale(.96);
  opacity: 0;
}
.fade-scale-enter-to {
  transform: scale(1);
  opacity: 1;
}
.fade-scale-leave-from {
  transform: scale(1);
  opacity: 1;
}
.fade-scale-leave-to {
  transform: scale(.96);
  opacity: 0;
}
</style>

<style scoped>
.modal-backdrop {
  backdrop-filter: blur(4px);
  background: rgba(2,6,23,0.45);
}
.modal-card.modal-glass {
  background: linear-gradient(180deg, rgba(159, 169, 190, 0.85), rgba(211, 219, 233, 0.9));
  border: 1px solid rgba(255,255,255,0.04);
  box-shadow: 0 30px 80px rgba(164, 177, 232, 0.7);
  margin-top: -400px;
}
.modal-header { display:flex;justify-content:space-between;align-items:center;margin-bottom:6px; }
.modal-header h3 { margin:0;color:var(--color-text-primary); }
.close-btn { background:transparent;border:none;color:rgba(255,255,255,0.6);font-size:20px;cursor:pointer;padding:4px 8px;border-radius:6px; }
.close-btn:hover { background:rgba(255,255,255,0.02) }
.operators-list { display:flex;flex-direction:column;gap:6px; }
.operator-item { display:flex;align-items:center;gap:12px;padding:10px;border-radius:8px;cursor:pointer;transition:all 180ms ease;background:transparent;border:1px solid transparent; }
.operator-item:hover { transform:translateY(-2px); background:rgba(255,255,255,0.02); border-color:rgba(255,255,255,0.03); }
.operator-item.selected { background:linear-gradient(90deg, rgba(59,130,246,0.12), rgba(99,102,241,0.06)); border-color: rgba(59,130,246,0.25); box-shadow: 0 6px 20px rgba(8,14,28,0.5); }
.avatar { width:40px;height:40px;border-radius:50%;background:linear-gradient(135deg,#3b82f6,#1d4ed8);display:flex;align-items:center;justify-content:center;color:#fff;font-weight:700;font-size:16px;flex-shrink:0; }
.operator-name { font-weight:700;color:var(--color-text-primary); }
.operator-meta { font-size:12px;color:rgba(148,163,184,0.9); }
.operator-action input[type="radio"] { width:18px;height:18px; }
</style>



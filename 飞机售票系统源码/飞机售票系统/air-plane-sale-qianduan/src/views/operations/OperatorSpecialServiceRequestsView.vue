<template>
  <PassengerLayout>
    <div class="operator-requests">
      <header class="page-header">
        <div>
          <p class="page-label">运营事务</p>
          <h1>重点旅客预约（运营）</h1>
          <p>显示分配给当前运营的预约，标记处理中/完成</p>
        </div>
      </header>

      <section class="glass-card list-section">
        <div v-if="loading" class="loading-placeholder">加载中...</div>
        <div v-else>
          <div v-if="requests.length === 0" class="empty-placeholder">暂无分配给您的预约</div>
          <div v-else class="request-list">
            <transition-group name="list" tag="div">
              <div v-for="r in requests" :key="r.id" class="request-item">
                <div class="card-row">
                  <div style="display:flex;align-items:center;gap:12px;">
                    <div class="avatar">{{ getInitials(r.passengerName || r.passengerId) }}</div>
                    <div>
                      <div class="card-title">{{ r.passengerName || ('乘客 ' + (r.passengerId || '—')) }}</div>
                      <div class="meta muted">
                        <div><strong>订单：</strong>{{ r.orderNo }}</div>
                        <div><strong>电话：</strong>{{ r.phone || '—' }}</div>
                      </div>
                    </div>
                  </div>

                  <div style="display:flex;align-items:center;gap:12px;">
                    <div class="status-badge" :class="statusClass(r.status)" :title="statusTooltip(r)">{{ statusText(r.status) }}</div>
                    <button class="btn btn-ghost" @click="toggleExpand(r.id)">{{ expandedMap[r.id] ? '收起' : '展开详细' }}</button>
                    <button class="btn btn-primary" @click="confirmMarkProcessing(r.id)">标记为处理中</button>
                    <button class="btn btn-ghost" @click="confirmMarkCompleted(r.id)">标记为完成</button>
                  </div>
                </div>

                <transition name="fade-scale">
                  <div v-if="expandedMap[r.id]" class="details" style="margin-top:12px;padding:12px;border-radius:8px;background:rgba(255,255,255,0.02);border:1px solid rgba(255,255,255,0.02);">
                    <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:8px;">
                      <div style="font-weight:600;color:var(--color-text-primary);">进入/退出服务详情</div>
                      <div style="font-size:12px;color:var(--color-text-secondary);">操作记录</div>
                    </div>

                    <div v-if="(r.operations || []).length > 0" style="margin-bottom:8px;">
                      <ul style="margin:0;padding-left:16px;color:var(--color-text-secondary);">
                        <li v-for="(op, idx) in r.operations" :key="idx" style="margin-bottom:6px;">
                          <strong style="color:var(--color-text-primary);">{{ op.action }}</strong>
                          <span style="margin-left:8px;color:var(--color-text-secondary);">— {{ op.operator }} · {{ op.time }}</span>
                          <div v-if="op.note" style="color:var(--color-text-secondary);margin-left:6px;font-size:12px;">备注：{{ op.note }}</div>
                        </li>
                      </ul>
                    </div>
                    <div v-else style="color:var(--color-text-secondary);margin-bottom:8px;">暂无操作记录</div>

                    <pre style="white-space:pre-wrap;font-size:13px;color:var(--color-text-secondary);margin:0;">
Entry Services:
{{ prettyJson(r.entryServices) }}

Exit Services:
{{ prettyJson(r.exitServices) }}
                    </pre>
                  </div>
                </transition>
              </div>
            </transition-group>
            <div class="pagination-controls" style="display:flex;align-items:center;justify-content:flex-end;gap:12px;margin-top:12px;" v-if="totalPages > 0">
              <div class="page-info" style="color:var(--color-text-secondary)">共 {{ total }} 条 / 第 {{ currentPage + 1 }} / {{ totalPages }} 页</div>
              <select v-model.number="pageSize" @change="onPageSizeChange" style="background: rgba(255,255,255,0.03); color:var(--color-text-primary); border-radius:6px; padding:6px; border:1px solid rgba(255,255,255,0.04);">
                <option :value="5">5 / 页</option>
                <option :value="10">10 / 页</option>
                <option :value="20">20 / 页</option>
                <option :value="50">50 / 页</option>
              </select>
              <button class="btn btn-ghost" :disabled="currentPage <= 0" @click="prevPage">上一页</button>
              <button class="btn btn-ghost" :disabled="currentPage >= totalPages - 1" @click="nextPage">下一页</button>
            </div>
          </div>
        </div>
      </section>
    </div>
  </PassengerLayout>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive } from 'vue'
import PassengerLayout from '../../components/layout/PassengerLayout.vue'
import { adminSpecialServiceRequestApi, apiUtils } from '../../services/api'

const requests = ref<any[]>([])
const loading = ref(false)
// pagination
const currentPage = ref(0)
const pageSize = ref(10)
const total = ref(0)
const totalPages = ref(0)

const loadAssigned = async () => {
  loading.value = true
  try {
    const currentUser = apiUtils.getCurrentUser()
    const operatorId = currentUser && currentUser.id ? (typeof currentUser.id === 'string' ? parseInt(currentUser.id, 10) : currentUser.id) : null
    if (!operatorId) {
      requests.value = []
      return
    }
    const pageParam = typeof arguments[0] === 'number' ? arguments[0] : currentPage.value
    const sizeParam = typeof arguments[1] === 'number' ? arguments[1] : pageSize.value
    const res: any = await adminSpecialServiceRequestApi.getAssigned({ operatorId, page: pageParam, size: sizeParam })
    const list = res?.data?.list || res?.list || []
    // pagination meta (try several common backend formats)
    total.value = res?.data?.totalElements ?? res?.data?.total ?? res?.total ?? list.length
    totalPages.value = res?.data?.totalPages ?? Math.ceil((total.value || list.length) / (sizeParam || pageSize.value))
    currentPage.value = pageParam
    pageSize.value = sizeParam
    // normalize each item to ensure operations/history exists so UI can display operation records
    requests.value = Array.isArray(list) ? list.map((it: any) => {
      return {
        ...it,
        operations: it.operations || it.operationLogs || it.history || []
      }
    }) : []
  } catch (e) {
    console.error(e)
    requests.value = []
  } finally {
    loading.value = false
  }
}

// UI state
const expandedMap = reactive<Record<string | number, boolean>>({})

const getInitials = (name: string | number | undefined) => {
  if (!name) return '—'
  const s = String(name).trim()
  if (!s) return '—'
  const parts = s.split(/\s+/)
  if (parts.length === 1) return parts[0].slice(0,1).toUpperCase()
  return (parts[0].slice(0,1) + parts[parts.length-1].slice(0,1)).toUpperCase()
}

const prettyJson = (obj: any) => {
  try { return JSON.stringify(obj, null, 2) } catch { return String(obj || '') }
}

const statusText = (s: string) => {
  switch((s||'').toLowerCase()) {
    case 'pending': return '待处理'
    case 'processing': return '处理中'
    case 'completed': return '已完成'
    case 'approved': return '已批准'
    case 'rejected': return '已拒绝'
    default: return s || '未知'
  }
}

const statusClass = (s: string) => {
  switch((s||'').toLowerCase()) {
    case 'pending': return 'status-pending'
    case 'processing': return 'status-processing'
    case 'completed': return 'status-completed'
    case 'approved': return 'status-approved'
    case 'rejected': return 'status-rejected'
    default: return ''
  }
}

const statusTooltip = (r: any) => {
  const time = r.updatedAt || r.updated_at || r.createdAt || r.created_at || ''
  const by = r.operatorName || r.operator || r.assignedOperator || ''
  return `处理人: ${by || '—'}  时间: ${time || '—'}`
}

const toggleExpand = (id: string | number) => {
  expandedMap[id] = !expandedMap[id]
}

const formatNow = () => {
  const d = new Date()
  return d.toLocaleString()
}

const pushOperationRecord = (item: any, action: string, note?: string) => {
  if (!item.operations) item.operations = []
  item.operations.unshift({
    action,
    operator: apiUtils.getCurrentUser()?.username || apiUtils.getCurrentUser()?.name || '当前用户',
    time: formatNow(),
    note: note || ''
  })
}

const confirmMarkProcessing = async (id:number) => {
  if (!confirm('确认将此预约标记为“处理中”吗？')) return
  try {
    await adminSpecialServiceRequestApi.updateStatus(id, 'processing')
    // optimistic update: update local item so it remains visible and add operation record
    const idx = requests.value.findIndex(r => r.id === id)
    if (idx !== -1) {
      requests.value[idx].status = 'processing'
      pushOperationRecord(requests.value[idx], '标记为处理中')
      // keep the card expanded so user can see operation history
      expandedMap[id] = true
    }
  } catch (e) { alert('操作失败') }
}

const confirmMarkCompleted = async (id:number) => {
  if (!confirm('确认将此预约标记为“完成”吗？')) return
  try {
    await adminSpecialServiceRequestApi.updateStatus(id, 'completed')
    const idx = requests.value.findIndex(r => r.id === id)
    if (idx !== -1) {
      requests.value[idx].status = 'completed'
      pushOperationRecord(requests.value[idx], '标记为完成')
      expandedMap[id] = true
    }
  } catch (e) { alert('操作失败') }
}

// pagination helpers
const prevPage = () => {
  if (currentPage.value > 0) {
    loadAssigned(currentPage.value - 1, pageSize.value)
  }
}

const nextPage = () => {
  if (currentPage.value < totalPages.value - 1) {
    loadAssigned(currentPage.value + 1, pageSize.value)
  }
}

const onPageSizeChange = () => {
  loadAssigned(0, pageSize.value)
}

onMounted(() => loadAssigned(0, pageSize.value))
</script>

<style scoped>
.request-list {
  display:flex;
  flex-direction:column;
  gap:12px;
}
.request-item {
  display:block;
  padding:16px;
  border-radius:12px;
  background: linear-gradient(180deg, rgba(201, 208, 220, 0.6), rgba(166, 175, 195, 0.5));
  border:1px solid rgba(255,255,255,0.03);
  box-shadow: 0 8px 28px rgba(160, 167, 198, 0.45);
}
.meta { display:flex; flex-direction:column; gap:6px; color:var(--color-text-secondary); }
.muted { color: rgba(148,163,184,0.9); }
.card-actions { display:flex; gap:10px; align-items:center; }
.btn { padding:8px 12px; border-radius:10px; font-size:13px; border:none; cursor:pointer; transition:transform .12s ease, box-shadow .12s ease; }
.btn:active { transform: translateY(1px) scale(.995); }
.btn-primary { background: linear-gradient(135deg,#3b82f6,#1d4ed8); color:#fff; box-shadow: 0 6px 18px rgba(29,78,216,0.18); border: none; }
.btn-primary:hover { filter:brightness(1.03); }
.btn-ghost { background: rgba(255,255,255,0.02); color:var(--color-text-primary); border:1px solid rgba(255,255,255,0.04); }

.avatar { width:48px;height:48px;border-radius:50%;background:linear-gradient(135deg,#3b82f6,#1d4ed8);display:flex;align-items:center;justify-content:center;color:#fff;font-weight:700;font-size:16px;flex-shrink:0; }
.card-row { display:flex;justify-content:space-between;align-items:center;gap:12px;width:100%; }
.card-title { font-size:15px;font-weight:700;color:var(--color-text-primary); }
.status-badge { padding:6px 10px;border-radius:999px;font-size:12px;font-weight:600;color:#fff; }
.status-pending { background: rgba(249,115,22,0.85); }
.status-approved { background: rgba(34,197,94,0.85); }
.status-processing { background: rgba(59,130,246,0.85); }
.status-completed { background: rgba(99,102,241,0.85); }
.status-rejected { background: rgba(239,68,68,0.85); }

@media (max-width:900px) {
  .request-item { padding:12px; }
  .card-row { flex-direction:column; align-items:flex-start; gap:8px; }
  .card-actions { justify-content:flex-end; width:100%; }
}

.details pre { background: transparent; border: none; margin: 0; padding: 0; color:var(--color-text-secondary); font-size:13px; }
.loading-placeholder, .empty-placeholder { padding:18px;color:var(--color-text-secondary); }
.request-item + .request-item { margin-top:8px; }
</style>



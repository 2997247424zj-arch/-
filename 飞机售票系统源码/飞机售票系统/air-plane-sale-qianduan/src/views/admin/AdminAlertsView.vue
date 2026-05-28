<template>
  <AdminLayout>
    <div class="page-container alerts-page">
      <div class="breadcrumb">
        <span>运行告警</span>
        <span class="breadcrumb-separator">/</span>
        <span>异常情况监控</span>
      </div>
      <header class="page-header">
        <div>

          <h1>异常情况监控</h1>
          <div class="page-action-group">
            <button class="btn-secondary" @click="handleExport" :disabled="loading" style="display:flex;align-items:center;gap:4px;">
              <span>📤</span> 导出报表
            </button>
          </div>
        </div>
      </header>
      <div class="alerts-filter" style="margin-bottom:12px; display:flex; gap:8px; align-items:center;">
        <button class="btn-primary" :class="{ active: viewMode === 'both' }" @click="() => setView('both')">全部</button>
        <button class="btn-primary" :class="{ active: viewMode === 'pending' }" @click="() => setView('pending')">未处理</button>
        <button class="btn-primary" :class="{ active: viewMode === 'processed' }" @click="() => setView('processed')">已处理</button>
      </div>

      <section class="alerts-list">
        <div v-if="(viewMode === 'both' || viewMode === 'pending') && pendingAlerts.length > 0">
          <h3 style="margin:0 0 8px 8px; color:red">未处理事件</h3>
          <div v-for="alert in pendingAlerts" :key="'u-'+alert.id" class="alert-card" :class="alert.level">
            <div class="alert-left">
              <div class="level-dot" :class="alert.level"></div>
            </div>
            <div class="alert-body">
              <div class="alert-title">{{ alert.title }}</div>
              <div class="alert-desc">{{ alert.description }}</div>
              <div class="alert-time">{{ formatTime(alert.updatedAt) }}</div>
            </div>
            <div class="alert-actions">
              <button class="btn-primary" :disabled="alert.processing" @click="handleProcess(alert)">{{ alert.processing ? '处理中...' : '处理' }}</button>
            </div>
          </div>
        </div>

        <div v-if="(viewMode === 'both' || viewMode === 'processed') && processedAlerts.length > 0" style="margin-top:1.25rem;">
          <h3 style="margin:0 0 8px 8px; color:blue">已处理事件</h3>
          <div v-for="alert in processedAlerts" :key="'p-'+alert.id" class="alert-card" :class="alert.level">
            <div class="alert-left">
              <div class="level-dot" :class="alert.level"></div>
            </div>
            <div class="alert-body">
              <div class="alert-title">{{ alert.title }}</div>
              <div class="alert-desc">{{ alert.description }}</div>
              <div class="alert-time">{{ formatTime(alert.updatedAt) }}</div>
            </div>
            <div class="alert-actions">
              <button class="btn-primary" disabled>已处理</button>
            </div>
          </div>
        </div>

        <div v-if="(viewMode==='both' && pendingAlerts.length === 0 && processedAlerts.length === 0) || (viewMode==='pending' && pendingAlerts.length === 0) || (viewMode==='processed' && processedAlerts.length === 0)" class="empty">暂无异常告警</div>
      </section>
      <div class="alerts-pagination" style="margin-top:12px; display:flex; align-items:center; gap:12px;">
        <button class="btn-primary" :disabled="page <= 0" @click="prevPage">上一页</button>
        <div style="color:var(--color-text-inverse);">第 {{ page + 1 }} 页</div>
        <button class="btn-primary" :disabled="!hasMore" @click="nextPage">下一页</button>
        <div style="margin-left:12px;">
          <label style="color:rgba(255,255,255,0.7); margin-right:8px;">每页</label>
          <select v-model.number="size" @change="onSizeChange">
            <option :value="5">5</option>
            <option :value="10">10</option>
            <option :value="20">20</option>
            <option :value="50">50</option>
          </select>
        </div>
      </div>
    </div>
  </AdminLayout>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import AdminLayout from '../../components/AdminLayout.vue'
import { adminAlertsApi } from '../../services/api'

const pendingAlerts = ref<any[]>([])
const processedAlerts = ref<any[]>([])
const page = ref<number>(0)
const size = ref<number>(5)
const hasMore = ref<boolean>(false)
const viewMode = ref<'both' | 'pending' | 'processed'>('both')

const loadAlerts = async () => {
  try {
    // 请求当前页数据
    const statusParam = viewMode.value === 'pending' ? 'pending' : (viewMode.value === 'processed' ? 'processed' : undefined)
    const res = await adminAlertsApi.getAlerts(page.value, size.value, statusParam)
    // 兼容后端不同返回格式，打印以便排查
    console.debug('adminAlerts.getAlerts raw response:', res)
    let data: any = []
    if (!res) {
      data = []
    } else if (Array.isArray(res)) {
      data = res
    } else if (res.data && Array.isArray(res.data)) {
      data = res.data
    } else if (res.list && Array.isArray(res.list)) {
      data = res.list
    } else if (res.success && res.data && Array.isArray(res.data)) {
      data = res.data
    } else if (res.success && res.data && res.data.list && Array.isArray(res.data.list)) {
      data = res.data.list
    } else if (res && typeof res === 'object') {
      // try to extract any array-like field
      const arr = Object.values(res).find(v => Array.isArray(v))
      data = arr || []
    } else {
      data = []
    }
    // split into pending and processed based on status === 'resolved'
    const pending: any[] = []
    const processed: any[] = []
    for (const a of data) {
      const st = (a && a.status) ? String(a.status).toLowerCase() : ''
      if (st === 'resolved') processed.push(a)
      else pending.push(a)
    }
    pendingAlerts.value = pending
    processedAlerts.value = processed
    // 如果返回条数等于 page size，则可能还有下一页
    hasMore.value = Array.isArray(data) && data.length === size.value
  } catch (e) {
    console.error('加载异常告警失败', e)
    pendingAlerts.value = []
    processedAlerts.value = []
  }
}

const formatTime = (v: string | null) => {
  if (!v) return ''
  try {
    const d = new Date(v)
    return d.toLocaleString()
  } catch {
    return v
  }
}

const handleProcess = (alert: any) => {
  // 调用后端更新状态为 processing -> resolved（当前简化为直接 resolved）
  alert.processing = true
  adminAlertsApi.processAlert(alert.id, 'resolved', 'Handled via UI').then((res:any) => {
    // update status and move to processed list
    alert.status = 'resolved'
    alert.operatorNote = 'Handled via UI'
    alert.processing = false
    // set updatedAt to now for display
    alert.updatedAt = new Date().toISOString()
    // remove from pending and add to processed
    const idx = pendingAlerts.value.findIndex(a => a.id === alert.id)
    if (idx > -1) pendingAlerts.value.splice(idx, 1)
    processedAlerts.value.unshift(alert)
  }).catch((err) => {
    console.error('处理告警失败', err)
    alert.processing = false
    alert._error = err?.message || String(err)
  })
}

onMounted(() => {
  loadAlerts()
})

const prevPage = async () => {
  if (page.value <= 0) return
  page.value = Math.max(0, page.value - 1)
  await loadAlerts()
}

const nextPage = async () => {
  if (!hasMore.value) return
  page.value = page.value + 1
  await loadAlerts()
}
const setView = async (m: 'both' | 'pending' | 'processed') => {
  viewMode.value = m
  page.value = 0
  await loadAlerts()
}
const onSizeChange = async () => {
  page.value = 0
  await loadAlerts()
}

const loading = ref(false)
const handleExport = async () => {
  try {
    loading.value = true
    // Fetch all for export
    const res = await adminAlertsApi.getAlerts(0, 2000, viewMode.value === 'both' ? undefined : viewMode.value) as any[]
    let data: any[] = []
    
    // Safety check for data structure
    if (Array.isArray(res)) data = res
    else if (res && (res as any).data && Array.isArray((res as any).data)) data = (res as any).data
    else if (res && (res as any).list && Array.isArray((res as any).list)) data = (res as any).list
    
    if (data.length === 0) {
      alert('没有可导出的数据')
      return
    }

    const headers = ['ID', '标题', '描述', '等级', '状态', '更新时间', '处理备注']
    const csvContent = [
       '\ufeff' + headers.join(','),
       ...data.map(item => [
         item.id,
         item.title,
         item.description,
         item.level,
         item.status,
         formatTime(item.updatedAt),
         item.operatorNote || ''
       ].map(f => `"${String(f || '').replace(/"/g, '""')}"`).join(','))
     ].join('\n')

     const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' })
     const link = document.createElement('a')
     const url = URL.createObjectURL(blob)
     link.href = url
     link.download = `运行告警_${new Date().toLocaleDateString()}.csv`
     document.body.appendChild(link)
     link.click()
     document.body.removeChild(link)
  } catch (e) {
    console.error('导出失败', e)
    alert('导出失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.alerts-page { 
  padding: 24px 32px; 
  color: #1e293b; /* Slate-800 for Light Theme */
}

/* Header & Breadcrumb */
.breadcrumb { 
  font-size: 14px; 
  color: #64748b; 
  margin-bottom: 20px; 
}

.breadcrumb-separator { margin: 0 8px; color: #94a3b8; }

.page-header { 
  display: flex; 
  justify-content: space-between; 
  align-items: center; 
  margin-bottom: 24px; 
}

.page-header h1 {
  margin: 0;
  font-size: 24px;
  font-weight: 700;
  color: #1e293b;
}

.page-action-group {
  display: flex;
  gap: 12px;
}

/* Filter Buttons */
.alerts-filter {
  margin-bottom: 24px; 
  display: flex; 
  gap: 12px; 
  align-items: center;
  background: rgba(255, 255, 255, 0.5);
  padding: 6px;
  border-radius: 12px;
  border: 1px solid rgba(255, 255, 255, 0.6);
  width: fit-content;
  backdrop-filter: blur(8px);
}

.alerts-filter button {
  padding: 8px 16px;
  border-radius: 8px;
  border: 1px solid transparent;
  background: transparent;
  color: #64748b;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}

.alerts-filter button.active {
  background: #fff;
  color: #3b82f6;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  border-color: #e2e8f0;
}

/* Alert Cards (Light Theme) */
.alerts-list { display: flex; flex-direction: column; gap: 16px; }

.alert-card { 
  display: flex; 
  gap: 16px; 
  align-items: center; 
  padding: 20px; 
  border-radius: 16px; 
  background: rgba(255, 255, 255, 0.7); 
  border: 1px solid rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(16px);
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05);
  transition: transform 0.2s, box-shadow 0.2s;
}

.alert-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.08);
}

.alert-left .level-dot { 
  width: 12px; 
  height: 12px; 
  border-radius: 50%; 
  position: relative;
}

.alert-left .level-dot::after {
  content: '';
  position: absolute;
  inset: -4px;
  border-radius: 50%;
  opacity: 0.3;
  background: inherit;
}

.alert-card.urgent .level-dot { background: #ef4444; }
.alert-card.warning .level-dot { background: #f59e0b; }
.alert-card.info .level-dot { background: #3b82f6; }

/* Status accent borders */
.alert-card.urgent { border-left: 4px solid #ef4444; }
.alert-card.warning { border-left: 4px solid #f59e0b; }
.alert-card.info { border-left: 4px solid #3b82f6; }

.alert-body { flex: 1; }

.alert-title { 
  font-weight: 700; 
  color: #1e293b; 
  margin-bottom: 4px; 
  font-size: 16px;
}

.alert-desc { 
  color: #475569; 
  margin-bottom: 4px; 
  font-size: 14px; 
  line-height: 1.5;
}

.alert-time { 
  color: #94a3b8; 
  font-size: 12px; 
  display: flex;
  align-items: center;
  gap: 4px;
}

.alert-actions .btn-primary { 
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%); 
  color: #fff; 
  border: none; 
  padding: 8px 16px; 
  border-radius: 8px; 
  cursor: pointer; 
  font-weight: 600;
  box-shadow: 0 2px 4px rgba(59, 130, 246, 0.2);
  transition: all 0.2s;
}

.alert-actions .btn-primary:hover:not(:disabled) {
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.3);
  transform: translateY(-1px);
}

.alert-actions .btn-primary:disabled {
  background: #e2e8f0;
  color: #94a3b8;
  cursor: not-allowed;
  box-shadow: none;
}

.empty { 
  color: #64748b; 
  padding: 40px; 
  text-align: center; 
  background: rgba(255,255,255,0.4);
  border-radius: 16px;
  border: 1px dashed #cbd5e1;
}

/* Pagination */
.alerts-pagination { 
  margin-top: 24px; 
  display: flex; 
  align-items: center; 
  justify-content: flex-end;
  gap: 12px; 
}

.alerts-pagination button {
  padding: 6px 12px;
  background: #fff;
  border: 1px solid #cbd5e1;
  border-radius: 8px;
  color: #475569;
  cursor: pointer;
  transition: all 0.2s;
}

.alerts-pagination button:hover:not(:disabled) {
  border-color: #3b82f6;
  color: #3b82f6;
}

.alerts-pagination button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.alerts-pagination div {
  color: #64748b;
  font-size: 14px;
}

.alerts-pagination select {
  padding: 4px 8px;
  border-radius: 6px;
  border: 1px solid #cbd5e1;
  color: #334155;
  background: #fff;
  cursor: pointer;
}

.btn-secondary {
  background: #fff;
  color: #475569;
  border: 1px solid #e2e8f0;
  padding: 8px 16px;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 600;
  transition: all 0.2s;
}
.btn-secondary:hover {
  background: #f8fafc;
  border-color: #cbd5e1;
  color: #1e293b;
}
</style>



<template>
  <div class="ticket-quick-entry">
    <div class="entry-header">
      <div class="header-left">
        <h3>打印机票快捷入口</h3>
        <div class="search-row">
          <input v-model="ticketNo" @keyup.enter="onSearch" placeholder="按机票号搜索（回车）" />
          <button class="search-btn" @click="onSearch">搜索</button>
          <button class="reset-btn" @click="resetSearch" v-if="ticketNo">重置</button>
        </div>
      </div>
      <div class="controls">
        <label>每页</label>
        <select v-model.number="pageSize" @change="onPageSizeChange">
          <option :value="5">5</option>
          <option :value="10">10</option>
          <option :value="20">20</option>
          <option :value="50">50</option>
          <option :value="100">100</option>
        </select>
      </div>
    </div>

    <table class="ticket-table">
      <div v-if="errorMessage" class="fetch-error">{{ errorMessage }}</div>
      <thead>
        <tr>
          <th>机票号</th>
          <th>乘客</th>
          <th>航班号</th>
          <th>航线</th>
          <th>起飞</th>
          <th>到达</th>
          <th>座位</th>
          <th>舱位</th>
          <th>票价</th>
          <th>状态</th>
          <th>操作</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="ticket in tickets" :key="ticket.ticket_no">
          <td>{{ ticket.ticket_no }}</td>
          <td>{{ ticket.passenger_name }}</td>
          <td>{{ ticket.flight_no }}</td>
          <td>{{ ticket.route }}</td>
          <td>{{ formatDateTime(ticket.departure_time) }}</td>
          <td>{{ formatDateTime(ticket.arrival_time) }}</td>
          <td>{{ ticket.seat_number || '-' }}</td>
          <td>{{ ticket.seat_class || '-' }}</td>
          <td>¥{{ ticket.total_price?.toFixed(2) ?? '0.00' }}</td>
          <td>{{ ticket.status }}</td>
          <td class="actions">
            <button @click="openDetail(ticket)">查看详情</button>
            <button @click="onDeleteTicket(ticket)" class="delete-btn" title="删除机票">删除</button>
            <button @click="onPrint(ticket)" :disabled="printing">打印</button>
          </td>
        </tr>
        <tr v-if="tickets.length === 0">
          <td colspan="11" class="empty">暂无机票</td>
        </tr>
      </tbody>
    </table>

    <div class="pagination">
      <button @click="prevPage" :disabled="page === 1">上一页</button>
      <span>第 {{ page }} 页 / {{ totalPages }} 页</span>
      <button @click="nextPage" :disabled="page >= totalPages">下一页</button>
    </div>

    <div v-if="detailVisible" class="detail-modal" @click.self="closeDetail">
      <div class="detail-card">
        <h4>机票详情 - {{ detail.ticket_no }}</h4>
        <div class="detail-grid">
          <div><strong>乘客姓名</strong><div>{{ detail.passenger_name }}</div></div>
          <div><strong>身份证号</strong><div>{{ detail.id_card }}</div></div>
          <div><strong>联系电话</strong><div>{{ detail.phone }}</div></div>
          <div><strong>航班号</strong><div>{{ detail.flight_no }}</div></div>
          <div><strong>航线</strong><div>{{ detail.route }}</div></div>
          <div><strong>起飞时间</strong><div>{{ formatDateTime(detail.departure_time) }}</div></div>
          <div><strong>到达时间</strong><div>{{ formatDateTime(detail.arrival_time) }}</div></div>
          <div><strong>座位号</strong><div>{{ detail.seat_number || '-' }}</div></div>
          <div><strong>舱位</strong><div>{{ detail.seat_class || '-' }}</div></div>
          <div><strong>基础票价</strong><div>¥{{ detail.base_price?.toFixed(2) ?? '0.00' }}</div></div>
          <div><strong>座位费</strong><div>¥{{ detail.seat_fee?.toFixed(2) ?? '0.00' }}</div></div>
          <div><strong>优惠</strong><div>¥{{ detail.discount_fee?.toFixed(2) ?? '0.00' }}</div></div>
          <div><strong>总价</strong><div>¥{{ detail.total_price?.toFixed(2) ?? '0.00' }}</div></div>
          <div><strong>机票号</strong><div>{{ detail.ticket_no }}</div></div>
          <div><strong>状态</strong><div>{{ detail.status }}</div></div>
        </div>
        <div class="modal-actions">
          <button @click="closeDetail">关闭</button>
          <button @click="onPrint(detail)" :disabled="printing">打印</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ticketApi } from '../services/api'

const tickets = ref<Array<any>>([])
const page = ref(1)
// 增大默认每页数量以避免仅展示少量已出票机票（后端应支持按状态过滤/分页）
const pageSize = ref(100)
const total = ref(0)
const totalPages = ref(1)
const printing = ref(false)
const ticketNo = ref('')

const detailVisible = ref(false)
const detail = reactive<any>({})
const errorMessage = ref('')

// 判断票是否为“已出票”。做统一归一化以兼容不同后端返回值（中/英/数字/带空格）
const isIssued = (status: any) => {
  if (status === null || status === undefined) return false
  const s = String(status).trim().toLowerCase()
  // 常见文本表示
  if (s === '已出票' || s === '已出' || s === '出票') return true
  if (s === 'issued' || s === '发票' || s === 'iss') return true
  // 数字或代码（如果后端使用状态码，例如 1 表示已出票）
  if (s === '1' || s === '01') return true
  return false
}

const fetchTickets = async () => {
  try {
    // 请求参数：分页 + 可选机票号 + 指定仅查询已出票机票（后端应支持该过滤并返回正确的 total）
    const paramsForLog: any = { page: page.value - 1, size: pageSize.value }
    if (ticketNo.value) paramsForLog.ticketNo = ticketNo.value
    // 前端请求时告知后端只返回状态为 "已出票" 的机票，以便后端在查询与计算 total 时应用该过滤
    paramsForLog.ticketStatus = '已出票'
    console.debug('[TicketQuickEntry] fetchTickets params:', paramsForLog)
    const resp = await ticketApi.getMyTickets(paramsForLog)
    console.debug('[TicketQuickEntry] fetchTickets resp:', resp)
    // 兼容后端返回格式：{ success, data: { list, total, page, size } } 或直接 { tickets: [], total: .. }
    if (resp && resp.success && resp.data) {
      const data = resp.data
      const list = data.list || data.tickets || data.ticketsList || []
      // 后端如果返回 total，则说明后端已对 ticketStatus 做过过滤，优先使用后端 total 来计算分页
      const serverTotal = (data && (data.total ?? data.totalElements ?? data.count ?? data.totalCount ?? data.totalRecords ?? data.total_rows ?? data.totalRows))
      if (typeof serverTotal === 'number' && !Number.isNaN(serverTotal)) {
        // 后端返回了 total，但不一定代表后端已按 ticketStatus 过滤。
        // 为了尽量保证每页展示 pageSize 个已出票项，先使用当前页返回的 list，然后在必要时从后端拉取后续页并合并（最多限制若干页以避免无限请求）。
        const serverPageSize = paramsForLog.size || pageSize.value
        let serverPageIndex = paramsForLog.page ?? (page.value - 1)
        const filteredInitial = (list || []).filter((t: any) => isIssued((t && t.status) ? t.status : ''))
        const accumulated: any[] = filteredInitial.slice()
        if ((list || []).length !== filteredInitial.length) {
          console.warn('[TicketQuickEntry] server returned total, but some items were filtered client-side to only include issued tickets.')
        }
        // 若当前页结果不足一页，尝试拉取后续服务器页以填充（最多拉取 maxFetchPages 个额外页）
        const maxFetchPages = 10
        let fetchedExtraPages = 0
        serverPageIndex = (paramsForLog.page ?? (page.value - 1)) + 1
        while (accumulated.length < pageSize.value && (serverPageIndex * serverPageSize) < serverTotal && fetchedExtraPages < maxFetchPages) {
          try {
            const nextResp = await ticketApi.getMyTickets({ page: serverPageIndex, size: serverPageSize, ticketNo: ticketNo.value || undefined, ticketStatus: '已出票' })
            const nextData = (nextResp && nextResp.success && nextResp.data) ? nextResp.data : (Array.isArray(nextResp) ? { list: nextResp } : (nextResp || {}))
            const nextList = nextData.list || nextData.tickets || nextData.ticketsList || []
            const nextFiltered = (nextList || []).filter((t: any) => isIssued((t && t.status) ? t.status : ''))
            if ((nextList || []).length > 0 && nextFiltered.length === 0) {
              // 若这一页完全被过滤掉，继续尝试下一页
              serverPageIndex++
              fetchedExtraPages++
              continue
            }
            accumulated.push(...nextFiltered)
            serverPageIndex++
            fetchedExtraPages++
          } catch (e) {
            console.warn('[TicketQuickEntry] failed to fetch extra server page while filling issued tickets:', e)
            break
          }
        }
        tickets.value = accumulated.slice(0, pageSize.value)
        // 分页总数仍使用服务器返回的 total（后端应返回已出票的 total 为最佳做法）
        total.value = serverTotal
      } else {
        // 后端未返回 total，则在前端对列表进行过滤并以当前页数组长度作为 total（视为全部返回）
        tickets.value = (list || []).filter((t: any) => isIssued((t && t.status) ? t.status : ''))
        total.value = tickets.value.length
      }
      totalPages.value = Math.max(1, Math.ceil((total.value || 0) / pageSize.value))
    } else if (Array.isArray(resp)) {
      // 后端直接返回数组（通常表示未分页或已返回全部）
      tickets.value = (resp || []).filter((t: any) => isIssued((t && t.status) ? t.status : ''))
      total.value = tickets.value.length
      totalPages.value = Math.max(1, Math.ceil(total.value / pageSize.value))
    } else {
      // 兼容直接返回对象，且可能包含 total 字段
      const list = resp?.tickets || resp?.list || []
      const serverTotal = (resp && (resp.total ?? resp.totalElements ?? resp.count ?? resp.totalCount ?? resp.totalRecords ?? resp.total_rows ?? resp.totalRows))
      if (typeof serverTotal === 'number' && !Number.isNaN(serverTotal)) {
        tickets.value = list
        total.value = serverTotal
      } else {
        tickets.value = (list || []).filter((t: any) => isIssued((t && t.status) ? t.status : ''))
        total.value = tickets.value.length
      }
      totalPages.value = Math.max(1, Math.ceil(total.value / pageSize.value))
    }

    if (!tickets.value || tickets.value.length === 0) {
      errorMessage.value = '未获取到机票记录（确认后端服务已启动并返回数据，或检查 passengerId 是否正确）'
    } else {
      errorMessage.value = ''
    }
  } catch (e: any) {
    console.error('获取机票列表失败', e)
    errorMessage.value = `获取机票列表失败：${e?.message || e}`
    tickets.value = []
    total.value = 0
    totalPages.value = 1
  }
}

const openDetail = async (ticket: any) => {
  try {
    // 获取最新详情（避免展示敏感字段：不会显示 id, passenger_id, created_at, updated_at）
    const resp = await ticketApi.getTicketDetail(ticket.id || ticket.ticket_no)
    if (resp && resp.success && resp.data) {
      Object.assign(detail, resp.data)
    } else {
      Object.assign(detail, resp || ticket)
    }
    detailVisible.value = true
  } catch (e) {
    console.error('获取机票详情失败', e)
    Object.assign(detail, ticket)
    detailVisible.value = true
  }
}

const closeDetail = () => {
  detailVisible.value = false
}

const onDeleteTicket = async (ticket: any) => {
  if (!confirm(`确定要删除机票 ${ticket.ticket_no} 吗？此操作将隐藏该机票，但不会永久删除。`)) {
    return
  }
  
  try {
    const currentUser = (window as any).currentUserInfo || JSON.parse(sessionStorage.getItem('user_info') || '{}')
    if (!currentUser || !currentUser.id) {
      alert('请先登录')
      return
    }
    
    const passengerId = typeof currentUser.id === 'string' ? parseInt(currentUser.id, 10) : currentUser.id
    const ticketId = ticket.id ? ticket.id : (ticket.ticket_no ? ticket.ticket_no : null)
    if (!ticketId) {
      alert('无法获取机票ID')
      return
    }
    const result = await ticketApi.softDeleteTicket(ticketId, passengerId)
    
    if (result && result.success) {
      alert('机票已删除')
      // 重新获取机票列表
      await fetchTickets()
    } else {
      alert(result?.message || '删除失败')
    }
  } catch (error: any) {
    console.error('删除机票失败', error)
    alert(error?.message || '删除失败，请重试')
  }
}

const onPrint = async (ticket: any) => {
  printing.value = true
  try {
    // 首选：从后端拉取详情数据并在前端构建打印页面（不生成 PDF）
    let detailResp = null
    try {
      detailResp = await ticketApi.getTicketDetail(ticket.id || ticket.ticket_no)
    } catch (err: any) {
      // ignore, we'll fallback
      detailResp = null
    }

    const printData = (detailResp && detailResp.success && detailResp.data) ? detailResp.data : (detailResp || ticket)

    const buildPrintHtml = (d: any) => {
      const title = d.ticket_no || '机票'
      const escapeHtml = (s: any) => (s === null || s === undefined) ? '-' : String(s)
      return `<!doctype html>
      <html>
      <head>
        <meta charset="utf-8">
        <title>${escapeHtml(title)}</title>
        <style>
          body{font-family: -apple-system,BlinkMacSystemFont,"Segoe UI",Roboto,"Helvetica Neue",Arial;padding:24px;color:#222}
          .print-card{max-width:720px;margin:0 auto;border:1px solid #ddd;padding:20px;border-radius:8px;}
          .header{display:flex;justify-content:space-between;align-items:center;margin-bottom:12px}
          .header h2{margin:0;font-size:18px}
          .grid{display:grid;grid-template-columns:1fr 1fr;gap:8px}
          .row{display:flex;justify-content:space-between;padding:6px 0;border-bottom:1px solid #f3f3f3}
          .label{font-weight:600;color:#333}
          @media print{ body{padding:0} .print-card{border:none;box-shadow:none} }
        </style>
      </head>
      <body>
        <div class="print-card">
          <div class="header">
            <h2>机票 - ${escapeHtml(d.ticket_no)}</h2>
            <div>${new Date().toLocaleString()}</div>
          </div>
          <div class="grid">
            <div>
              <div class="row"><div class="label">乘客</div><div>${escapeHtml(d.passenger_name)}</div></div>
              <div class="row"><div class="label">身份证</div><div>${escapeHtml(d.id_card)}</div></div>
              <div class="row"><div class="label">电话</div><div>${escapeHtml(d.phone)}</div></div>
              <div class="row"><div class="label">航班号</div><div>${escapeHtml(d.flight_no)}</div></div>
              <div class="row"><div class="label">航线</div><div>${escapeHtml(d.route)}</div></div>
            </div>
            <div>
              <div class="row"><div class="label">起飞</div><div>${escapeHtml(d.departure_time)}</div></div>
              <div class="row"><div class="label">到达</div><div>${escapeHtml(d.arrival_time)}</div></div>
              <div class="row"><div class="label">座位</div><div>${escapeHtml(d.seat_number)}</div></div>
              <div class="row"><div class="label">舱位</div><div>${escapeHtml(d.seat_class)}</div></div>
              <div class="row"><div class="label">票号</div><div>${escapeHtml(d.ticket_no)}</div></div>
            </div>
          </div>
          <div style="margin-top:12px;text-align:right;font-weight:700">总价：¥${(d.total_price !== undefined && d.total_price !== null) ? Number(d.total_price).toFixed(2) : '-'}</div>
        </div>
        <script>window.addEventListener('load', function(){ setTimeout(function(){ window.print(); }, 200); });<\/script>
      </body></html>`
    }

    const html = buildPrintHtml(printData)
    // 为避免打开新的空白页（浏览器会短暂显示 about:blank），优先使用隐藏 iframe 打印
    // 后续的回退逻辑（例如下载后端返回的 PDF URL）保留不变
    const iframe = document.createElement('iframe') as HTMLIFrameElement
    iframe.style.position = 'fixed'
    iframe.style.width = '0'
    iframe.style.height = '0'
    iframe.style.left = '-9999px'
    iframe.style.top = '-9999px'
    iframe.setAttribute('aria-hidden', 'true')
    document.body.appendChild(iframe)
    const idoc = (iframe.contentWindow || iframe.contentDocument) as any
    try {
      idoc.document.open()
      idoc.document.write(html)
      idoc.document.close()
      // 等待内容渲染再打印
      setTimeout(() => {
        try {
          const cw = iframe.contentWindow
          if (cw) {
            cw.focus()
            cw.print()
          } else {
            throw new Error('无法获取 iframe 窗口')
          }
        } catch (e) {
          console.warn('iframe 打印失败', e)
          // 最后回退：在当前窗口打开打印页面
          const w = window.open('', '_self')
          if (w) {
            w.document.open()
            w.document.write(html)
            w.document.close()
          }
        } finally {
          setTimeout(() => { iframe.remove() }, 500)
        }
      }, 300)
      return
    } catch (err) {
      iframe.remove()
      throw err
    }
  } catch (e: any) {
    console.warn('前端构建打印页面失败，尝试后端导出/下载回退：', e)
    try {
      // 回退：尝试原来的后端打印接口（支持 blob / url）
      const raw = await ticketApi.printTicket(ticket.id || ticket.ticket_no)
      let fileBlob = raw
      if (raw && raw.data instanceof Blob) fileBlob = raw.data
      if (fileBlob instanceof Blob) {
        const u = window.URL.createObjectURL(fileBlob)
        const a = document.createElement('a')
        a.href = u
        a.download = `ticket_${ticket.ticket_no || Date.now()}.pdf`
        document.body.appendChild(a)
        a.click()
        a.remove()
        window.URL.revokeObjectURL(u)
      } else {
        const url = (raw && raw.data && raw.data.url) ? raw.data.url : raw?.url
        if (url) window.open(url, '_blank')
        else throw new Error('未返回可下载文件')
      }
    } catch (err) {
      console.error('打印回退失败', err)
      alert('打印失败：' + ((err && (err as any).message) || String(err)))
    }
  } finally {
    printing.value = false
  }
}

const prevPage = () => {
  if (page.value > 1) {
    page.value--
    fetchTickets()
  }
}
const nextPage = () => {
  if (page.value < totalPages.value) {
    page.value++
    fetchTickets()
  }
}

const onSearch = () => {
  page.value = 1
  fetchTickets()
}

const resetSearch = () => {
  ticketNo.value = ''
  page.value = 1
  fetchTickets()
}

const onPageSizeChange = () => {
  page.value = 1
  fetchTickets()
}

const formatDateTime = (v: string | null) => {
  if (!v) return '-'
  try {
    const d = new Date(v)
    return d.toLocaleString()
  } catch {
    return v
  }
}

onMounted(() => {
  fetchTickets()
})
</script>

<style scoped>
.ticket-quick-entry {
  background: rgb(218, 222, 228);
  border-radius: 12px;
  padding: 12px;
  border: 1px solid rgba(0,0,0,0.06);
  color: #111;
}
.entry-header {
  display:flex;
  justify-content:space-between;
  align-items:center;
  margin-bottom:8px;
}
.ticket-table {
  width:100%;
  border-collapse:collapse;
  font-size:13px;
}
.ticket-table thead th {
  background: rgba(0,0,0,0.06);
  position: sticky;
  top: 0;
  z-index: 2;
}
.ticket-table tbody tr {
  transition: transform 220ms ease, background-color 220ms ease, box-shadow 220ms ease;
}
.ticket-table tbody tr:hover {
  transform: translateY(-3px);
  background: rgba(255,255,255,0.02);
  box-shadow: 0 2px 6px rgba(0,0,0,0.12);
}
.ticket-table tbody tr:nth-child(odd) {
  background: rgba(255,255,255,0.01);
}
.ticket-table td, .ticket-table th {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.fetch-error {
  color: #ffdede;
  background: rgba(255,0,0,0.06);
  padding: 8px 12px;
  border-radius: 8px;
  margin-bottom: 8px;
  font-size: 13px;
}
.ticket-table th, .ticket-table td {
  padding:8px;
  border-bottom:1px solid #f0f0f0;
  text-align:left;
}
.ticket-table .actions button {
  margin-right:6px;
}
.ticket-table .actions .delete-btn {
  background-color: #ff4d4f;
  color: white;
  border: none;
  padding: 4px 8px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
}
.ticket-table .actions .delete-btn:hover {
  background-color: #f5222d;
}
.pagination {
  display:flex;
  gap:12px;
  align-items:center;
  justify-content:flex-end;
  margin-top:8px;
}
.detail-modal {
  position:fixed;
  inset:0;
  background:rgba(0,0,0,0.4);
  display:flex;
  align-items:center;
  justify-content:center;
  z-index:1200;
}
.detail-card {
  background: linear-gradient(180deg,#eef6ff,#d9eafe);
  padding:20px;
  border-radius:10px;
  width:820px;
  max-width:96%;
  box-shadow: 0 10px 30px rgba(2,6,23,0.5);
  transform: translateY(-10px);
  animation: popIn 240ms ease;
  margin-top: -270px;
}
.detail-grid {
  display:grid;
  grid-template-columns: 1fr 1fr;
  gap:14px;
  margin-top:12px;
  align-items:start;
}
.detail-card h4 {
  margin:0 0 6px 0;
  font-size:18px;
  color:#0b3a66;
}
.detail-card .label {
  color:#234e7a;
  font-weight:700;
}
.detail-card .value {
  color:#0b2b45;
}
.modal-actions button {
  background:#0b66c3;
  color:#fff;
  border:none;
  padding:8px 12px;
  border-radius:6px;
  cursor:pointer;
}
.modal-actions button[disabled] {
  opacity:0.6;
  cursor:not-allowed;
}
.modal-actions button + button {
  margin-left:8px;
  background:#0b9;
}

@keyframes popIn {
  from { opacity:0; transform: translateY(-20px) scale(0.98) }
  to { opacity:1; transform: translateY(0) scale(1) }
}
.modal-actions {
  display:flex;
  justify-content:flex-end;
  gap:8px;
  margin-top:12px;
}
.empty {
  text-align:center;
  color:#888;
  padding:16px 0;
}
</style>



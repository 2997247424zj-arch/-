<template>
  <AdminLayout>
    <div class="page-container ops-page">
      <div class="breadcrumb">
        <span>首页</span>
        <span class="breadcrumb-separator">/</span>
        <span>航空运营视图</span>
      </div>

      <header class="page-header">
        <div>
          <p class="page-label">航班调度 · 运行控制</p>
          <h1>运营指挥席 · 三分钟掌握网络态势</h1>
          <p>面向 OCC（运行控制中心）人员，聚合航班调度、机组排班、航线监控与突发决策流。</p>
        </div>
        <div class="page-actions">
          <button class="ghost-btn" @click="handleExportReport">导出运行日报</button>
          <button class="primary-btn" @click="handleAssignDuty">指派值班长</button>
        </div>
      </header>

      <section class="glass-card layout-map">
        <h2>组件布局规划</h2>
        <div class="layout-grid">
          <article v-for="block in operatorLayout" :key="block.module" class="layout-item">
            <span class="layout-position">{{ block.position }}</span>
            <h3>{{ block.module }}</h3>
            <p>{{ block.desc }}</p>
            <ul>
              <li v-for="tip in block.tips" :key="tip">{{ tip }}</li>
            </ul>
          </article>
        </div>
      </section>

      <section class="grid-two">
        <article class="glass-card dispatch-card">
          <div class="section-head">
            <div>
              <p class="section-label">航班调度/案件</p>
              <h2>实时调度队列</h2>
            </div>
            <button class="refresh-btn" @click="handleRefreshDispatch">刷新 {{ dispatchTasks.length }} 条</button>
          </div>
          <ul class="task-list">
            <li v-for="task in dispatchTasks" :key="task.id">
              <div>
                <p>{{ task.title }}</p>
                <small>{{ task.route }} · {{ task.deadline }}</small>
              </div>
              <span class="status-tag" :class="task.level">{{ task.levelText }}</span>
            </li>
          </ul>
        </article>

        <article class="glass-card core-panel">
          <div class="section-head">
            <div>
              <p class="section-label">核心操作信息</p>
              <h2>机组与资源</h2>
            </div>
          </div>
          <div class="split-cards">
            <div class="mini-card">
              <p class="mini-label">机组排班</p>
              <h3>92%</h3>
              <small>24 小时内已匹配</small>
            </div>
            <div class="mini-card">
              <p class="mini-label">机位占用</p>
              <h3>68%</h3>
              <small>航班执行窗口</small>
            </div>
            <div class="mini-card">
              <p class="mini-label">航油库存</p>
              <h3>7.2 万吨</h3>
              <small>满足 5 日飞行</small>
            </div>
          </div>
        </article>
      </section>

      <section class="grid-two">
        <article class="glass-card stats-board">
          <div class="section-head">
            <div>
              <p class="section-label">数据统计 & 分析</p>
              <h2>航班 KPI 看板</h2>
            </div>
          </div>
          <table class="data-table">
            <thead>
              <tr>
                <th>指标</th>
                <th>当前值</th>
                <th>阈值</th>
                <th>趋势</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="metric in metrics" :key="metric.label">
                <td>{{ metric.label }}</td>
                <td>{{ metric.value }}</td>
                <td>{{ metric.target }}</td>
                <td :class="metric.trendType">{{ metric.trend }}</td>
              </tr>
            </tbody>
          </table>
        </article>

        <article class="glass-card quick-links">
          <div class="section-head">
            <div>
              <p class="section-label">详细模块快捷入口</p>
              <h2>常用操作</h2>
            </div>
          </div>
          <div class="link-grid">
            <button 
              v-for="link in quickActions" 
              :key="link.label" 
              class="ghost-btn"
              @click="handleQuickAction(link.action)"
            >
              {{ link.icon }} {{ link.label }}
            </button>
          </div>
        </article>

        <article class="glass-card alert-panel">
          <div class="section-head">
            <div>
              <p class="section-label">实时告警</p>
              <h2>异常监控</h2>
            </div>
            <span class="panel-pill alert-count">{{ alerts.length }} 条告警</span>
          </div>
          <ul class="alert-list">
            <li v-for="alert in alerts" :key="alert.id" :class="['alert-item', alert.level]">
              <div class="alert-icon">{{ alert.icon }}</div>
              <div class="alert-content">
                <p class="alert-title">{{ alert.title }}</p>
                <small class="alert-time">{{ alert.time }}</small>
              </div>
              <button class="alert-action-btn" @click="handleAlert(alert)">处理</button>
            </li>
          </ul>
        </article>
      </section>

      <section class="grid-two">
        <article class="glass-card chain-card">
          <div class="section-head">
            <div>
              <p class="section-label">航司调度链路核查</p>
              <h2>链路追踪</h2>
            </div>
            <button class="ghost-btn" @click="refreshChainAudit">刷新状态</button>
          </div>
          <ul class="chain-list">
            <li v-for="node in chainAudit" :key="node.stage">
              <span class="chain-stage">{{ node.stage }}</span>
              <div>
                <p>{{ node.title }}</p>
                <small>{{ node.desc }}</small>
              </div>
              <span class="status-tag" :class="node.state">{{ node.stateText }}</span>
            </li>
          </ul>
        </article>

        <article class="glass-card resource-panel">
          <div class="section-head">
            <div>
              <p class="section-label">资源管理</p>
              <h2>机队与机组</h2>
            </div>
            <button class="ghost-btn" @click="handleResourceManagement">资源管理</button>
            <button class="primary-btn" @click="handleRefreshResources">刷新资源</button>
          </div>
          <div class="resource-grid">
            <div class="resource-item">
              <div class="resource-icon">✈️</div>
              <div class="resource-info">
                <p class="resource-label">可用机队</p>
                <p class="resource-value">{{ resources.availableAircraft }}/{{ resources.totalAircraft }}</p>
                <div class="resource-progress">
                  <div class="progress-bar" :style="{ width: (resources.availableAircraft / resources.totalAircraft * 100) + '%' }"></div>
                </div>
              </div>
            </div>
            <div class="resource-item">
              <div class="resource-icon">👨‍✈️</div>
              <div class="resource-info">
                <p class="resource-label">在岗机组</p>
                <p class="resource-value">{{ resources.onDutyCrew }}/{{ resources.totalCrew }}</p>
                <div class="resource-progress">
                  <div class="progress-bar" :style="{ width: (resources.onDutyCrew / resources.totalCrew * 100) + '%' }"></div>
                </div>
              </div>
            </div>
            <div class="resource-item">
              <div class="resource-icon">🛬</div>
              <div class="resource-info">
                <p class="resource-label">可用机位</p>
                <p class="resource-value">{{ resources.availableGates }}/{{ resources.totalGates }}</p>
                <div class="resource-progress">
                  <div class="progress-bar" :style="{ width: (resources.availableGates / resources.totalGates * 100) + '%' }"></div>
                </div>
              </div>
            </div>
          </div>
        </article>
      </section>

        <article class="glass-card personal-card">
          <div class="section-head">
            <div>
              <p class="section-label">个人中心 / 我的订单</p>
              <h2>值班清单</h2>
            </div>
          </div>
          <ul class="personal-list">
            <li v-for="todo in personalTasks" :key="todo.id">
              <div>
                <p>{{ todo.title }}</p>
                <small>{{ todo.window }}</small>
              </div>
              <button class="ghost-btn" @click="handleTask(todo.id)">去处理</button>
            </li>
          </ul>
          <div class="tool-row">
            <p>辅助功能</p>
            <div class="tool-tags">
              <span v-for="tool in assistTools" :key="tool">{{ tool }}</span>
            </div>
          </div>
        </article>
      </section>
    </div>

    <!-- 提示模态框 -->
    <ModalPrompt
      v-model="promptModal.visible"
      :title="promptModal.title"
      :message="promptModal.message"
      :type="promptModal.type"
      :show-cancel="promptModal.showCancel"
      @confirm="handlePromptConfirm"
    />

    <!-- 输入模态框 -->
    <div v-if="inputModal.visible" class="modal-overlay" @click="inputModal.visible = false">
      <div class="modal-content input-modal" @click.stop>
        <div class="modal-header">
          <h3>{{ inputModal.title }}</h3>
          <button class="close-btn" @click="inputModal.visible = false">×</button>
        </div>
        <div class="modal-body">
          <p class="input-prompt">{{ inputModal.prompt }}</p>
          <input
            v-model="inputModal.value"
            type="text"
            class="modal-input"
            :placeholder="inputModal.placeholder"
            @keyup.enter="handleInputConfirm"
          />
        </div>
        <div class="modal-footer">
          <button class="ghost-btn" @click="inputModal.visible = false">取消</button>
          <button class="primary-btn" @click="handleInputConfirm">确认</button>
        </div>
      </div>
    </div>
  </AdminLayout>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import AdminLayout from '../components/AdminLayout.vue'
import ModalPrompt from '../components/ModalPrompt.vue'

const router = useRouter()

// 处理任务点击
const handleTask = (taskId: string) => {
  const task = personalTasks.value.find(t => t.id === taskId)
  if (task) {
    // 根据任务类型跳转到相应页面
    if (task.title.includes('机队调配') || task.title.includes('机型')) {
      router.push('/portal/aircraft')
    } else if (task.title.includes('审批')) {
      router.push('/portal/flights')
    } else {
      // 默认跳转到航班管理页面
      router.push('/portal/flights')
    }
  }
}

const operatorLayout = ref([
  {
    position: '顶部 · 左主视觉',
    module: '航班调度/案件',
    desc: '展示最新航班告警与指派情况，便于第一时间响应。',
    tips: ['12 列表并行滚动', '支持一键接管']
  },
  {
    position: '顶部 · 右侧',
    module: '核心操作信息（航班动态、机位分配、机组排班）',
    desc: '强调资源可用度、异常热点及关键 KPI。',
    tips: ['卡片化展示', '颜色区分可用/紧张']
  },
  {
    position: '中部 · 左侧',
    module: '数据统计与分析看板',
    desc: '对延误率、客座率、航线负荷等指标进行对比。',
    tips: ['支持阈值预警', '与历史曲线联动']
  },
  {
    position: '中部 · 右侧',
    module: '详细模块快捷入口（如航班管理、机位列表）',
    desc: '保留常用分系统按钮，移动端同样可用。',
    tips: ['分组按钮', '显示未处理数量']
  },
  {
    position: '下部 · 左侧',
    module: '航司调度链路核查',
    desc: '追踪值班链路，明晰责任人及状态。',
    tips: ['阶段节点可展开', '异常加粗']
  },
  {
    position: '下部 · 右侧',
    module: '个人中心 / 我的订单',
    desc: '列出值班任务、审批单据与私有关注航班。',
    tips: ['支持指派同事', '同步到移动端']
  },
  {
    position: '底部工具栏',
    module: '辅助功能（知识库、名单、签派工具等）',
    desc: '预留扩展位，容纳内部沟通、手册与统计导出。',
    tips: ['悬浮快捷入口', '支持键盘唤起']
  }
])

const dispatchTasks = ref([
  { id: 'd1', title: 'MU5123 补点检', route: '上海 ⇀ 东京', deadline: '08:40 起飞', level: 'urgent', levelText: '紧急' },
  { id: 'd2', title: 'CZ3001 机组替换', route: '广州 ⇀ 巴黎', deadline: '09:15 起飞', level: 'warning', levelText: '关注' },
  { id: 'd3', title: 'FM9208 返航评估', route: '深圳 ⇀ 首尔', deadline: '待重新排期', level: 'info', levelText: '记录' }
])

const metrics = ref([
  { label: '准点率', value: '91.4%', target: '≥ 90%', trend: '+1.2%', trendType: 'up' },
  { label: '客座率', value: '84.8%', target: '≥ 80%', trend: '+0.6%', trendType: 'up' },
  { label: '延误航班', value: '18 班', target: '≤ 20', trend: '-3', trendType: 'down' }
])

const quickActions = ref([
  { label: '航班总览', icon: '🗂️', action: 'flight-overview' },
  { label: '机位资源', icon: '🛬', action: 'gate-resources' },
  { label: '机组排班', icon: '👨‍✈️', action: 'crew-scheduling' },
  { label: '运行策略', icon: '📊', action: 'operation-strategy' },
  { label: '突发事件', icon: '⚠️', action: 'emergency' },
  { label: '数据导入', icon: '📥', action: 'data-import' }
])

// 实时告警
const alerts = ref([
  {
    id: 'a1',
    title: 'MU5123 航班延误预警',
    time: '5分钟前',
    level: 'urgent',
    icon: '🚨'
  },
  {
    id: 'a2',
    title: '机组资源紧张',
    time: '15分钟前',
    level: 'warning',
    icon: '⚠️'
  },
  {
    id: 'a3',
    title: '机位分配异常',
    time: '30分钟前',
    level: 'info',
    icon: 'ℹ️'
  }
])

// 资源管理数据
const resources = ref({
  availableAircraft: 42,
  totalAircraft: 58,
  onDutyCrew: 128,
  totalCrew: 156,
  availableGates: 18,
  totalGates: 24
})

// 处理快捷操作
const handleQuickAction = (action: string) => {
  const actionMap: Record<string, { title: string; message: string; route?: string }> = {
    'flight-overview': {
      title: '航班总览',
      message: '正在跳转到航班总览页面...',
      route: '/portal/flights'
    },
    'gate-resources': {
      title: '机位资源',
      message: '正在打开机位资源管理...',
      route: '/portal/aircraft'
    },
    'crew-scheduling': {
      title: '机组排班',
      message: '正在打开机组排班管理...'
    },
    'operation-strategy': {
      title: '运行策略',
      message: '正在打开运行策略配置...'
    },
    'emergency': {
      title: '突发事件',
      message: '正在打开突发事件处理中心...'
    },
    'data-import': {
      title: '数据导入',
      message: '正在打开数据导入工具...'
    }
  }
  
  const actionInfo = actionMap[action]
  if (actionInfo) {
    if (actionInfo.route) {
      router.push(actionInfo.route)
    } else {
      showPrompt(actionInfo.title, `${actionInfo.message}\n\n功能开发中，敬请期待`, 'info')
    }
  }
}

// 处理告警
const handleAlert = (alertItem: any) => {
  showPrompt(
    '处理告警',
    `告警：${alertItem.title}\n\n时间：${alertItem.time}\n\n是否标记为已处理？`,
    'confirm',
    true,
    () => {
      // 从告警列表中移除
      const index = alerts.value.findIndex(a => a.id === alertItem.id)
      if (index > -1) {
        alerts.value.splice(index, 1)
        showPrompt('成功', '告警已处理', 'success')
      }
    }
  )
}

// 刷新链路核查
const refreshChainAudit = () => {
  // 模拟刷新数据
  chainAudit.value.forEach(node => {
    if (node.state === 'warning') {
      node.state = 'done'
      node.stateText = '完成'
    }
  })
  showPrompt('成功', '链路状态已刷新', 'success')
}

// 资源管理
const handleResourceManagement = () => {
  router.push('/portal/aircraft')
}

// 导出运行日报
const handleExportReport = () => {
  showPrompt('导出运行日报', '正在导出运行日报...\n\n导出功能开发中，敬请期待', 'info')
}

// 指派值班长
const handleAssignDuty = () => {
  showInput(
    '指派值班长',
    '请输入值班长姓名：',
    '请输入姓名',
    (value) => {
      showPrompt('成功', `已指派 ${value} 为值班长`, 'success')
    }
  )
}

// 刷新调度队列
const handleRefreshDispatch = () => {
  // 模拟刷新数据
  dispatchTasks.value.forEach(task => {
    if (task.level === 'warning') {
      task.level = 'info'
      task.levelText = '已处理'
    }
  })
  showPrompt('成功', '调度队列已刷新', 'success')
}

// 刷新资源
const handleRefreshResources = () => {
  // 模拟刷新资源数据
  resources.value.availableAircraft = Math.floor(Math.random() * 20) + 40
  resources.value.onDutyCrew = Math.floor(Math.random() * 30) + 120
  resources.value.availableGates = Math.floor(Math.random() * 6) + 16
  showPrompt('成功', '资源数据已刷新', 'success')
}

// 生成新报告
const handleGenerateReport = () => {
  showInput(
    '生成报告',
    '请选择报告类型：\n1. 日报\n2. 周报\n3. 月报',
    '请输入选项（1/2/3）',
    (value) => {
      const types: Record<string, string> = { '1': '日报', '2': '周报', '3': '月报' }
      const reportType = types[value] || '报告'
      showPrompt('生成报告', `正在生成${reportType}...\n\n报告生成功能开发中`, 'info')
    }
  )
}

const chainAudit = ref([
  { stage: '1', title: '航班计划', desc: '完成航线批复', state: 'done', stateText: '通过' },
  { stage: '2', title: '签派放行', desc: '等待气象二次确认', state: 'warning', stateText: '待确认' },
  { stage: '3', title: '机务保障', desc: '完成机务复核', state: 'done', stateText: '完成' },
  { stage: '4', title: '旅客通知', desc: '短信推送中', state: 'info', stateText: '进行中' }
])

const personalTasks = ref([
  { id: 'p1', title: '复核机队调配方案', window: '08:00 - 09:00' },
  { id: 'p2', title: '审批跨区备份机型', window: '09:30 - 10:00' }
])

const assistTools = ref(['知识库', '名单校验', '签派助手', '移动审批'])

// 提示模态框
const promptModal = reactive({
  visible: false,
  title: '',
  message: '',
  type: 'info' as 'success' | 'error' | 'info' | 'confirm',
  showCancel: false,
  onConfirm: null as (() => void) | null
})

const showPrompt = (
  title: string,
  message: string,
  type: 'success' | 'error' | 'info' | 'confirm' = 'info',
  showCancel = false,
  onConfirm: (() => void) | null = null
) => {
  promptModal.title = title
  promptModal.message = message
  promptModal.type = type
  promptModal.showCancel = showCancel
  promptModal.onConfirm = onConfirm
  promptModal.visible = true
}

const handlePromptConfirm = () => {
  if (promptModal.onConfirm) {
    promptModal.onConfirm()
  }
  promptModal.visible = false
}

// 输入模态框
const inputModal = reactive({
  visible: false,
  title: '',
  prompt: '',
  placeholder: '',
  value: '',
  onConfirm: null as ((value: string) => void) | null
})

const showInput = (
  title: string,
  prompt: string,
  placeholder: string = '',
  onConfirm: ((value: string) => void) | null = null
) => {
  inputModal.title = title
  inputModal.prompt = prompt
  inputModal.placeholder = placeholder
  inputModal.value = ''
  inputModal.onConfirm = onConfirm
  inputModal.visible = true
}

const handleInputConfirm = () => {
  if (inputModal.onConfirm && inputModal.value.trim()) {
    inputModal.onConfirm(inputModal.value.trim())
  }
  inputModal.visible = false
}
</script>

<style scoped>
/* 直接内联基础样式 */
.page-container {
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
  background: linear-gradient(135deg, #87CEEB, #4AA3DF);
  color: #fff;
  box-shadow: 0 10px 25px rgba(99, 102, 241, 0.35);
}

.ghost-btn {
  border-color: rgba(255, 255, 255, 0.35);
  background: transparent;
  color: #f8fafc;
}

.ghost-btn:hover {
  background: rgba(255, 255, 255, 0.1);
}

.grid-two {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 1.5rem;
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
  color: #fff;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
}

.data-table th,
.data-table td {
  padding: 0.9rem 0.6rem;
  text-align: left;
  border-bottom: 1px solid rgba(148, 163, 184, 0.2);
}

.data-table th {
  color: rgba(248, 250, 252, 0.65);
  font-weight: 500;
}

.data-table td {
  color: rgba(248, 250, 252, 0.85);
}

.ops-page {
  gap: 1.5rem;
}

.layout-map h2 {
  margin-top: 0;
}

.layout-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 1rem;
}

.layout-item {
  border: 1px dashed rgba(248, 250, 252, 0.2);
  border-radius: 16px;
  padding: 1rem;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.layout-position {
  font-size: 0.85rem;
  color: rgba(248, 250, 252, 0.6);
}

.layout-item ul {
  margin: 0;
  padding-left: 1rem;
  color: rgba(248, 250, 252, 0.7);
}

.dispatch-card .task-list,
.quick-links .link-grid,
.chain-card .chain-list,
.personal-card .personal-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.task-list li,
.personal-list li,
.chain-list li {
  display: flex;
  justify-content: space-between;
  gap: 0.8rem;
  align-items: center;
  padding: 0.7rem 0;
  border-bottom: 1px solid rgba(248, 250, 252, 0.05);
}

.task-list li:last-child,
.personal-list li:last-child,
.chain-list li:last-child {
  border-bottom: none;
}

.task-list small,
.personal-list small,
.chain-list small {
  color: rgba(248, 250, 252, 0.6);
}

.status-tag {
  border-radius: 999px;
  padding: 0.25rem 0.9rem;
  border: 1px solid rgba(255, 255, 255, 0.15);
}

.status-tag.urgent {
  border-color: rgba(248, 113, 113, 0.5);
  color: #f87171;
}

.status-tag.warning {
  border-color: rgba(250, 204, 21, 0.5);
  color: #fbbf24;
}

.status-tag.info {
  border-color: rgba(96, 165, 250, 0.5);
  color: #87CEEB;
}

.split-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(140px, 1fr));
  gap: 1rem;
}

.mini-card {
  background: rgba(15, 23, 42, 0.6);
  border-radius: 16px;
  padding: 1rem;
}

.mini-label {
  margin: 0;
  color: rgba(248, 250, 252, 0.65);
}

.mini-card h3 {
  margin: 0.4rem 0;
  font-size: 1.5rem;
}

.mini-card small {
  color: rgba(248, 250, 252, 0.6);
}

.stats-board table {
  width: 100%;
}

.stats-board td.up {
  color: #4ade80;
}

.stats-board td.down {
  color: #f87171;
}

.link-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 0.8rem;
}

.chain-list .chain-stage {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: rgba(96, 165, 250, 0.15);
  display: flex;
  align-items: center;
  justify-content: center;
}

.tool-row {
  margin-top: 1rem;
  border-top: 1px solid rgba(248, 250, 252, 0.05);
  padding-top: 0.8rem;
}

.tool-tags {
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
}

.tool-tags span {
  padding: 0.2rem 0.7rem;
  border-radius: 999px;
  background: rgba(15, 23, 42, 0.6);
}

/* 告警面板样式 */
.alert-panel .alert-list {
  list-style: none;
  padding: 0;
  margin: 1rem 0 0;
  display: flex;
  flex-direction: column;
  gap: 0.8rem;
}

.alert-item {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1rem;
  border-radius: 12px;
  background: rgba(15, 23, 42, 0.6);
  border: 1px solid rgba(255, 255, 255, 0.1);
  transition: all 0.3s ease;
}

.alert-item:hover {
  background: rgba(15, 23, 42, 0.8);
  border-color: rgba(255, 255, 255, 0.2);
  transform: translateX(4px);
}

.alert-item.urgent {
  border-left: 3px solid #f87171;
}

.alert-item.warning {
  border-left: 3px solid #fbbf24;
}

.alert-item.info {
  border-left: 3px solid #87CEEB;
}

.alert-icon {
  font-size: 1.5rem;
  flex-shrink: 0;
}

.alert-content {
  flex: 1;
}

.alert-title {
  margin: 0 0 0.3rem 0;
  font-weight: 600;
  color: #fff;
}

.alert-time {
  color: rgba(248, 250, 252, 0.6);
  font-size: 0.85rem;
}

.alert-action-btn {
  padding: 0.5rem 1rem;
  border: 1px solid rgba(255, 255, 255, 0.2);
  background: rgba(99, 102, 241, 0.2);
  color: #a5b4fc;
  border-radius: 8px;
  cursor: pointer;
  font-size: 0.85rem;
  transition: all 0.3s ease;
}

.alert-action-btn:hover {
  background: rgba(99, 102, 241, 0.3);
  border-color: rgba(99, 102, 241, 0.5);
}

.alert-count {
  background: rgba(248, 113, 113, 0.15);
  color: #fda4af;
  border-color: rgba(248, 113, 113, 0.3);
}

.refresh-btn {
  padding: 0.4rem 0.9rem;
  border: 1px solid rgba(255, 255, 255, 0.2);
  background: rgba(99, 102, 241, 0.2);
  color: #a5b4fc;
  border-radius: 8px;
  font-size: 0.85rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s;
}

.refresh-btn:hover {
  background: rgba(99, 102, 241, 0.3);
  border-color: rgba(99, 102, 241, 0.4);
  transform: translateY(-1px);
}

/* 资源管理面板样式 */
.resource-panel .resource-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 1rem;
  margin-top: 1rem;
}

.resource-item {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1rem;
  border-radius: 12px;
  background: rgba(15, 23, 42, 0.6);
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.resource-icon {
  font-size: 2rem;
  flex-shrink: 0;
}

.resource-info {
  flex: 1;
}

.resource-label {
  margin: 0 0 0.5rem 0;
  color: rgba(248, 250, 252, 0.7);
  font-size: 0.85rem;
}

.resource-value {
  margin: 0 0 0.5rem 0;
  font-size: 1.3rem;
  font-weight: 700;
  color: #fff;
}

.resource-progress {
  width: 100%;
  height: 6px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 3px;
  overflow: hidden;
}

.progress-bar {
  height: 100%;
  background: linear-gradient(90deg, #87CEEB, #4AA3DF);
  border-radius: 3px;
  transition: width 0.3s ease;
}

/* 输入模态框样式 */
.input-modal {
  max-width: 500px;
}

.input-prompt {
  color: rgba(255, 255, 255, 0.8);
  margin-bottom: 1rem;
  font-size: clamp(0.9rem, 2.5vw, 0.95rem);
  white-space: pre-line;
}

.modal-input {
  width: 100%;
  padding: clamp(0.65rem, 2vw, 0.75rem);
  border: 1px solid rgba(255, 255, 255, 0.15);
  border-radius: clamp(8px, 2vw, 10px);
  background: rgba(15, 23, 42, 0.6);
  color: #fff;
  font-size: clamp(0.85rem, 2.5vw, 0.9rem);
  font-family: inherit;
  box-sizing: border-box;
}

.modal-input:focus {
  outline: none;
  border-color: rgba(99, 102, 241, 0.5);
  background: rgba(15, 23, 42, 0.8);
}

.modal-input::placeholder {
  color: rgba(255, 255, 255, 0.5);
}

/* 模态框通用样式 */
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
  padding: clamp(12px, 3vw, 20px);
  box-sizing: border-box;
  overflow: auto;
  overscroll-behavior: contain;
  -webkit-overflow-scrolling: touch;
}

.modal-content {
  background: rgba(15, 23, 42, 0.95);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: clamp(16px, 4vw, 24px);
  width: 90%;
  max-width: 500px;
  max-height: calc(100vh - 40px);
  max-height: calc(100dvh - 40px);
  overflow-y: auto;
  box-shadow: 0 25px 50px rgba(15, 23, 42, 0.5);
  margin: auto;
  position: relative;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  scroll-behavior: smooth;
}

.modal-header {
  padding: clamp(18px, 4vw, 24px);
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-shrink: 0;
}

.modal-header h3 {
  margin: 0;
  color: #fff;
  font-size: clamp(1.2rem, 3vw, 1.5rem);
}

.close-btn {
  background: none;
  border: none;
  font-size: clamp(24px, 5vw, 28px);
  color: rgba(255, 255, 255, 0.7);
  cursor: pointer;
  width: clamp(36px, 8vw, 40px);
  height: clamp(36px, 8vw, 40px);
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: clamp(10px, 2vw, 12px);
  transition: all 0.3s;
  flex-shrink: 0;
}

.close-btn:hover {
  background: rgba(255, 255, 255, 0.1);
  color: #fff;
}

.modal-body {
  padding: clamp(18px, 4vw, 24px);
  flex: 1;
  overflow-y: auto;
}

.modal-footer {
  padding: clamp(18px, 4vw, 24px);
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  display: flex;
  gap: clamp(0.8rem, 2vw, 1rem);
  justify-content: flex-end;
  flex-shrink: 0;
  flex-wrap: wrap;
}

/* 响应式优化 - 平板 */
@media (min-width: 768px) and (max-width: 1024px) {
  .modal-content {
    width: min(480px, calc(100vw - 60px));
    padding: clamp(20px, 3vw, 24px);
  }
}

/* 响应式优化 - 移动端 */
@media (max-width: 767px) {
  .modal-overlay {
    padding: 12px;
  }
  
  .modal-content {
    width: calc(100vw - 24px);
    max-width: none;
    border-radius: clamp(16px, 4vw, 20px);
  }
  
  .modal-header {
    padding: clamp(16px, 4vw, 20px);
  }
  
  .modal-header h3 {
    font-size: clamp(1.1rem, 4.5vw, 1.3rem);
  }
  
  .modal-body {
    padding: clamp(16px, 4vw, 20px);
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
  }
  
  .modal-content {
    width: calc(100vw - 16px);
  }
  
  .modal-header {
    padding: 14px 16px;
  }
  
  .modal-body {
    padding: 14px 16px;
  }
  
  .modal-header h3 {
    font-size: 1.2rem;
  }
}

/* 响应式优化 - 低高度屏幕 */
@media (max-height: 600px) {
  .modal-content {
    max-height: calc(100vh - 20px);
    max-height: calc(100dvh - 20px);
  }
  
  .modal-header {
    padding: clamp(14px, 2.5vh, 18px);
  }
  
  .modal-body {
    padding: clamp(14px, 2.5vh, 18px);
  }
  
  .modal-footer {
    padding: clamp(14px, 2.5vh, 18px);
  }
}
</style>


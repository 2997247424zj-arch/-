<template>
  <div class="support-chat-view">
    <header class="chat-header">
      <div class="header-info">
        <p class="page-label">在线客服 · 实时协助</p>
        <h1>乘客支持中心</h1>
        <p>连接值班客服，实时解答订单、航班、改签与行李相关问题。</p>
      </div>
      <div class="header-actions">
        <button class="ghost-btn" @click="goBack">返回上一页</button>
        <button class="primary-btn">提交服务单</button>
      </div>
    </header>

    <div class="chat-layout">
      <aside class="conversation-list">
        <div class="list-header">
          <h3>会话列表</h3>
          <span>{{ conversations.length }} 个会话</span>
        </div>
        <button
          v-for="session in conversations"
          :key="session.id"
          type="button"
          class="conversation-card"
          :class="{ active: session.id === activeConversationId }"
          @click="selectConversation(session.id)"
        >
          <div class="conversation-main">
            <div>
              <p class="conversation-title">{{ session.title }}</p>
              <p class="conversation-desc">{{ session.subtitle }}</p>
            </div>
            <span v-if="session.unread" class="badge">{{ session.unread }}</span>
          </div>
          <div class="conversation-meta">
            <span class="tag" v-for="tag in session.tags" :key="tag">{{ tag }}</span>
            <span class="timestamp">{{ session.updatedAt }}</span>
          </div>
        </button>
      </aside>

      <section class="chat-panel">
        <header class="panel-header">
          <div>
            <h3>{{ activeConversation?.title || '客服会话' }}</h3>
            <p>{{ activeConversation?.subtitle }}</p>
          </div>
          <div class="status-indicator">
            <span class="dot"></span>
            值班客服在线
          </div>
        </header>

        <div class="chat-history" ref="historyRef">
          <div
            v-for="message in activeConversation?.messages || []"
            :key="message.id"
            class="chat-bubble"
            :class="message.role"
          >
            <p class="bubble-meta">
              <span>{{ message.sender }}</span>
              <time>{{ message.time }}</time>
            </p>
            <p class="bubble-content">{{ message.content }}</p>
          </div>
        </div>

        <footer class="chat-input">
          <textarea
            v-model="draftMessage"
            rows="3"
            placeholder="请输入您的问题，例如：我想改签今晚20:30的航班……"
            @keydown.enter.prevent="sendMessage"
          />
          <div class="input-actions">
            <span class="hotkey">按 Enter 发送</span>
            <button class="primary-btn" type="button" :disabled="!draftMessage.trim()" @click="sendMessage">
              发送
            </button>
          </div>
        </footer>
      </section>

      <aside class="support-info">
        <div class="info-card">
          <h4>工单状态</h4>
          <ul>
            <li><strong>处理客服：</strong>{{ activeConversation?.handler }}</li>
            <li><strong>响应 SLA：</strong>10 分钟内</li>
            <li><strong>当前等级：</strong>{{ activeConversation?.priority }}</li>
          </ul>
        </div>
        <div class="info-card">
          <h4>常用入口</h4>
          <button class="link-btn" @click="router.push('/portal/orders')">查看我的订单</button>
          <button class="link-btn" @click="router.push('/portal/passengers')">返回乘客首页</button>
          <button class="link-btn" @click="router.push('/portal/passengers/view?tab=overview')">行程总览</button>
        </div>
      </aside>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, ref, watch } from 'vue'
import { useRouter } from 'vue-router'

interface ChatMessage {
  id: string
  role: 'agent' | 'passenger'
  sender: string
  time: string
  content: string
}

interface Conversation {
  id: string
  title: string
  subtitle: string
  handler: string
  priority: '普通' | '加急'
  unread: number
  updatedAt: string
  tags: string[]
  messages: ChatMessage[]
}

const router = useRouter()
const historyRef = ref<HTMLElement | null>(null)

const conversations = ref<Conversation[]>([
  {
    id: 'support-1',
    title: '订单改签咨询',
    subtitle: '关于 MU2158 航班改签',
    handler: '客服 杨子航',
    priority: '加急',
    unread: 0,
    updatedAt: '12:05',
    tags: ['改签', 'MU2158'],
    messages: [
      { id: 'm1', role: 'agent', sender: '客服 杨子航', time: '11:59', content: '您好，我是您的专属客服，很高兴为您服务。' },
      { id: 'm2', role: 'passenger', sender: '我', time: '12:01', content: '您好，我想把今晚 20:30 的航班改签到明天上午。' },
      { id: 'm3', role: 'agent', sender: '客服 杨子航', time: '12:02', content: '好的，我正在为您查询可用的改签舱位，请稍等。' }
    ]
  },
  {
    id: 'support-2',
    title: '退票进度',
    subtitle: '订单 ORD-20251102',
    handler: '客服 刘晓琴',
    priority: '普通',
    unread: 2,
    updatedAt: '09:40',
    tags: ['退款'],
    messages: [
      { id: 'm4', role: 'agent', sender: '客服 刘晓琴', time: '09:23', content: '您好，退款正在审核中，预计 3 个工作日完成。' },
      { id: 'm5', role: 'passenger', sender: '我', time: '09:28', content: '好的，烦请审核通过后短信通知我。' }
    ]
  }
])

const activeConversationId = ref(conversations.value[0]?.id || '')
const draftMessage = ref('')

const activeConversation = computed(() =>
  conversations.value.find(item => item.id === activeConversationId.value)
)

const goBack = () => {
  if (window.history.length > 1) {
    router.back()
  } else {
    router.push('/portal/passengers')
  }
}

const selectConversation = (id: string) => {
  activeConversationId.value = id
  const session = conversations.value.find(item => item.id === id)
  if (session) {
    session.unread = 0
  }
}

const sendMessage = () => {
  const text = draftMessage.value.trim()
  if (!text || !activeConversation.value) return
  const now = new Date()
  const time = now.toTimeString().slice(0, 5)
  activeConversation.value.messages.push({
    id: `msg-${Date.now()}`,
    role: 'passenger',
    sender: '我',
    time,
    content: text
  })
  draftMessage.value = ''
  nextTick(scrollToBottom)
  setTimeout(() => {
    activeConversation.value?.messages.push({
      id: `reply-${Date.now()}`,
      role: 'agent',
      sender: activeConversation.value?.handler || '客服',
      time,
      content: '收到，我正在处理该请求，请稍等片刻。'
    })
    nextTick(scrollToBottom)
  }, 800)
}

const scrollToBottom = () => {
  const el = historyRef.value
  if (el) {
    el.scrollTop = el.scrollHeight
  }
}

watch(activeConversationId, () => nextTick(scrollToBottom))
</script>

<style scoped>
.support-chat-view {
  padding: 32px clamp(16px, 4vw, 48px) 48px;
  color: #e2e8f0;
  min-height: 100vh;
  background: radial-gradient(circle at top, rgba(30, 58, 138, 0.35), rgba(15, 23, 42, 0.95) 55%);
  box-sizing: border-box;
}

.chat-header {
  display: flex;
  justify-content: space-between;
  gap: 24px;
  margin-bottom: 32px;
  flex-wrap: wrap;
}

.header-info h1 {
  margin: 8px 0 6px;
  font-size: clamp(26px, 4vw, 34px);
}

.page-label {
  margin: 0;
  font-size: 14px;
  color: rgba(255, 255, 255, 0.65);
  letter-spacing: 0.15em;
  text-transform: uppercase;
}

.header-actions {
  display: flex;
  gap: 12px;
  align-items: flex-start;
}

.primary-btn,
.ghost-btn,
.link-btn {
  border-radius: 999px;
  padding: 10px 20px;
  border: none;
  cursor: pointer;
  font-weight: 600;
  transition: all 0.3s ease;
}

.primary-btn {
  background: linear-gradient(135deg, #2563eb, #3b82f6);
  color: white;
  border: 1px solid rgba(59, 130, 246, 0.5);
}

.ghost-btn {
  background: transparent;
  color: #cbd5f5;
  border: 1px solid rgba(148, 163, 184, 0.4);
}

.chat-layout {
  display: grid;
  grid-template-columns: 280px minmax(0, 1fr) 240px;
  gap: 20px;
}

.conversation-list,
.support-info {
  background: rgba(15, 23, 42, 0.7);
  border: 1px solid rgba(148, 163, 184, 0.2);
  border-radius: 20px;
  padding: 20px;
  box-shadow: 0 20px 40px rgba(2, 6, 23, 0.45);
}

.conversation-card {
  width: 100%;
  border: none;
  background: rgba(148, 163, 184, 0.08);
  border-radius: 16px;
  padding: 16px;
  margin-bottom: 12px;
  color: inherit;
  text-align: left;
  cursor: pointer;
  transition: all 0.3s ease;
}

.conversation-card.active {
  background: rgba(37, 99, 235, 0.25);
  border: 1px solid rgba(59, 130, 246, 0.4);
}

.conversation-main {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.conversation-title {
  margin: 0;
  font-weight: 600;
}

.conversation-desc {
  margin: 4px 0 0;
  font-size: 13px;
  color: rgba(226, 232, 240, 0.75);
}

.conversation-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 10px;
  gap: 8px;
  flex-wrap: wrap;
}

.tag {
  padding: 4px 10px;
  border-radius: 999px;
  font-size: 12px;
  background: rgba(59, 130, 246, 0.15);
  border: 1px solid rgba(59, 130, 246, 0.2);
}

.badge {
  background: #ef4444;
  color: #fff;
  border-radius: 999px;
  padding: 2px 10px;
  font-size: 12px;
}

.chat-panel {
  background: rgba(15, 23, 42, 0.8);
  border-radius: 24px;
  border: 1px solid rgba(148, 163, 184, 0.2);
  display: flex;
  flex-direction: column;
  min-height: 620px;
  box-shadow: 0 24px 60px rgba(2, 6, 23, 0.55);
}

.panel-header {
  padding: 22px;
  border-bottom: 1px solid rgba(148, 163, 184, 0.15);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.status-indicator {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  color: #bbf7d0;
}

.status-indicator .dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: #22c55e;
  box-shadow: 0 0 10px rgba(34, 197, 94, 0.6);
}

.chat-history {
  flex: 1;
  padding: 24px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.chat-bubble {
  max-width: 70%;
  padding: 16px 18px;
  border-radius: 20px;
  background: rgba(59, 130, 246, 0.1);
  border: 1px solid rgba(59, 130, 246, 0.2);
  line-height: 1.6;
}

.chat-bubble.passenger {
  align-self: flex-end;
  background: linear-gradient(135deg, #2563eb, #1d4ed8);
  border-color: transparent;
  color: white;
}

.bubble-meta {
  margin: 0 0 6px;
  font-size: 12px;
  display: flex;
  justify-content: space-between;
  opacity: 0.8;
}

.chat-input {
  border-top: 1px solid rgba(148, 163, 184, 0.15);
  padding: 18px 22px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.chat-input textarea {
  width: 100%;
  border-radius: 16px;
  background: rgba(15, 23, 42, 0.9);
  border: 1px solid rgba(148, 163, 184, 0.3);
  color: inherit;
  padding: 14px 16px;
  resize: none;
  font-family: inherit;
}

.input-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.hotkey {
  font-size: 12px;
  color: rgba(148, 163, 184, 0.8);
}

.info-card {
  background: rgba(15, 23, 42, 0.9);
  border-radius: 16px;
  padding: 16px;
  margin-bottom: 16px;
  border: 1px solid rgba(148, 163, 184, 0.2);
}

.info-card ul {
  list-style: none;
  padding: 0;
  margin: 12px 0 0;
  line-height: 1.8;
  font-size: 14px;
}

.link-btn {
  display: block;
  width: 100%;
  margin-bottom: 8px;
  text-align: center;
  background: rgba(59, 130, 246, 0.12);
  border: 1px solid rgba(59, 130, 246, 0.3);
  color: #bfdbfe;
}

@media (max-width: 1200px) {
  .chat-layout {
    grid-template-columns: 260px minmax(0, 1fr);
  }

  .support-info {
    grid-column: span 2;
  }
}

@media (max-width: 900px) {
  .chat-layout {
    grid-template-columns: 1fr;
  }

  .conversation-list {
    order: 2;
  }

  .support-info {
    order: 3;
  }
}
</style>


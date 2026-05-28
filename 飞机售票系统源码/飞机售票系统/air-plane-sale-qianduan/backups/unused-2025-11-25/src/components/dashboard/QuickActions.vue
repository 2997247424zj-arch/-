<template>
  <div class="glass-card quick-panel">
    <div class="panel-header">
      <div>
        <p class="panel-label">高频任务</p>
        <h2>快捷入口</h2>
      </div>
      <button class="back-btn" @click="$emit('back')">返回用户界面</button>
    </div>

    <div class="action-cards">
      <div class="action-card" @click="$emit('search')">
        <div class="action-icon">🔍</div>
        <div>
          <h3>搜索航班</h3>
          <p>查找并预订航班</p>
        </div>
      </div>

      <div class="action-card" @click="$emit('orders')">
        <div class="action-icon">📋</div>
        <div>
          <h3>我的订单</h3>
          <p>查看订单历史</p>
        </div>
      </div>

      <div class="action-card" @click="$emit('profile')">
        <div class="action-icon">👤</div>
        <div>
          <h3>个人信息</h3>
          <p>管理账户信息</p>
        </div>
      </div>

      <div class="action-card" @click="$emit('tickets')">
        <div class="action-icon">🎫</div>
        <div>
          <h3>机票审核</h3>
          <p>订退票审核流程</p>
        </div>
      </div>

      <div class="action-card" @click="$emit('users')">
        <div class="action-icon">🧑‍✈️</div>
        <div>
          <h3>人员管理</h3>
          <p>用户列表与审批</p>
        </div>
      </div>
    </div>

    <div class="insight-strip">
      <div>
        <p class="strip-label">AI 推荐航线</p>
        <p class="strip-value">{{ insightCount }} 条候选</p>
      </div>
      <button class="strip-btn" @click="$emit('search')">立即查看</button>
    </div>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  insightCount: number
}>()
</script>

<style scoped>
.glass-card {
  padding: 1.75rem;
  border-radius: 28px;
  background: rgba(2, 6, 23, 0.7);
  border: 1px solid rgba(255, 255, 255, 0.06);
  box-shadow:
    0 25px 50px rgba(2, 6, 23, 0.6),
    inset 0 1px rgba(255, 255, 255, 0.08);
  height: 100%;
}

.panel-header {
  margin-bottom: 1.5rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 1rem;
}

.panel-label {
  font-size: 0.85rem;
  color: rgba(248, 250, 252, 0.6);
  margin-bottom: 0.3rem;
}

.panel-header h2 {
  margin: 0;
  font-size: 1.6rem;
}

.back-btn {
  border-radius: 999px;
  border: 1px solid rgba(255, 255, 255, 0.25);
  background: transparent;
  color: #f8fafc;
  padding: 0.4rem 1.1rem;
  font-size: 0.9rem;
  cursor: pointer;
}

.action-cards {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.action-card {
  padding: 1.3rem;
  border-radius: 20px;
  border: 1px solid rgba(255, 255, 255, 0.04);
  background: rgba(6, 12, 34, 0.85);
  display: flex;
  align-items: center;
  gap: 1rem;
  cursor: pointer;
  transition: transform 0.3s ease, border-color 0.3s ease;
}

.action-card:hover {
  transform: translateY(-2px);
  border-color: rgba(96, 165, 250, 0.6);
}

.action-icon {
  width: 52px;
  height: 52px;
  border-radius: 16px;
  display: grid;
  place-items: center;
  font-size: 1.6rem;
  background: rgba(96, 165, 250, 0.15);
}

.action-card h3 {
  margin: 0;
  font-size: 1.1rem;
}

.action-card p {
  margin: 0.3rem 0 0;
  color: rgba(248, 250, 252, 0.6);
}

.insight-strip {
  margin-top: 1.5rem;
  padding: 1rem 1.25rem;
  border-radius: 18px;
  background: rgba(248, 250, 252, 0.05);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.strip-label {
  font-size: 0.85rem;
  color: rgba(248, 250, 252, 0.6);
}

.strip-value {
  margin: 0.15rem 0 0;
  font-size: 1.2rem;
  font-weight: 600;
}

.strip-btn {
  border: none;
  border-radius: 12px;
  padding: 0.55rem 1.2rem;
  background: rgba(129, 140, 248, 0.2);
  color: #c7d2fe;
  cursor: pointer;
}
</style>


<template>
  <section class="hero-banner">
    <div class="hero-copy">
      <p class="banner-label">实时航线总览</p>
      <h2>一站式掌控航司运营动态</h2>
      <p>高阶数据面板助力快速决策，实时同步订单、航班、乘客核心数据。</p>
      <div class="banner-actions">
        <button class="primary" @click="$emit('new-booking')">新建预订</button>
        <button class="ghost" @click="$emit('view-orders')">查看订单</button>
      </div>
    </div>
    <div class="hero-metrics">
      <div class="metric-card">
        <span class="metric-value">{{ recentCount }}</span>
        <span class="metric-label">最近订单</span>
      </div>
      <div class="metric-card">
        <span class="metric-value">{{ defaultPassengers }}</span>
        <span class="metric-label">默认乘客数</span>
      </div>
      <div class="metric-card">
        <span class="metric-value">{{ systemAvailability }}</span>
        <span class="metric-label">系统可用性</span>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
defineProps<{
  recentCount: number
  defaultPassengers: string
  systemAvailability?: string
}>()
</script>

<style scoped>
.hero-banner {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 2rem;
  padding: 2rem;
  border-radius: 32px;
  background: linear-gradient(135deg, rgba(15, 118, 255, 0.25), rgba(139, 92, 246, 0.3));
  border: 1px solid rgba(255, 255, 255, 0.08);
  box-shadow: 0 30px 60px rgba(2, 6, 23, 0.6);
}

.hero-copy h2 {
  margin: 0.5rem 0 1rem;
  font-size: clamp(1.9rem, 3vw, 2.6rem);
}

.hero-copy p {
  color: rgba(248, 250, 252, 0.8);
  line-height: 1.6;
}

.banner-label {
  font-size: 0.85rem;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: rgba(248, 250, 252, 0.65);
}

.banner-actions {
  margin-top: 1.5rem;
  display: flex;
  gap: 0.8rem;
  flex-wrap: wrap;
}

.banner-actions .primary,
.banner-actions .ghost {
  border-radius: 999px;
  padding: 0.7rem 1.6rem;
  font-weight: 600;
  border: 1px solid transparent;
  cursor: pointer;
}

.banner-actions .primary {
  background: #e0f2fe;
  color: #0f172a;
}

.banner-actions .ghost {
  background: transparent;
  border-color: rgba(255, 255, 255, 0.35);
  color: #fff;
}

.hero-metrics {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(140px, 1fr));
  gap: 1rem;
}

.metric-card {
  padding: 1.2rem;
  border-radius: 20px;
  background: rgba(2, 6, 23, 0.4);
  border: 1px solid rgba(255, 255, 255, 0.08);
  text-align: center;
}

.metric-value {
  font-size: 2rem;
  font-weight: 600;
}

.metric-label {
  display: block;
  margin-top: 0.4rem;
  color: rgba(248, 250, 252, 0.65);
  font-size: 0.9rem;
}
</style>


<template>
  <section class="glass-card recent-orders">
    <div class="panel-header">
      <div>
        <p class="panel-label">流水追踪</p>
        <h2>最近订单</h2>
      </div>
      <button class="ghost" @click="$emit('view-all')">全部订单</button>
    </div>

    <div v-if="orders.length === 0" class="empty-state">
      <p>暂无订单记录</p>
      <button @click="$emit('create')" class="cta-btn">立即预订航班</button>
    </div>

    <div v-else class="orders-list">
      <div v-for="order in orders" :key="order.id" class="order-item">
        <div class="order-info">
          <h4>{{ order.flightNumber }} · {{ order.departure }} → {{ order.destination }}</h4>
          <p>出发时间：{{ order.departureText }}</p>
          <p>状态：<span :class="order.statusClass">{{ order.statusText }}</span></p>
        </div>
        <div class="order-actions">
          <span class="order-price">¥{{ order.totalPrice }}</span>
          <button @click="$emit('view-detail', order.id)" class="detail-btn">查看详情</button>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
export interface DecoratedOrder {
  id: string
  flightNumber: string
  departure: string
  destination: string
  departureText: string
  statusText: string
  statusClass: string
  totalPrice: number
}

defineProps<{
  orders: DecoratedOrder[]
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
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 1rem;
  margin-bottom: 1.5rem;
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

.ghost {
  border: 1px solid rgba(255, 255, 255, 0.2);
  background: transparent;
  border-radius: 999px;
  padding: 0.45rem 1.2rem;
  color: rgba(248, 250, 252, 0.85);
  cursor: pointer;
}

.empty-state {
  text-align: center;
  padding: 3rem 1rem;
  color: rgba(248, 250, 252, 0.6);
}

.cta-btn {
  margin-top: 1rem;
  border: none;
  border-radius: 14px;
  padding: 0.75rem 1.5rem;
  background: rgba(129, 140, 248, 0.4);
  color: #fff;
  cursor: pointer;
}

.orders-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.order-item {
  padding: 1.2rem;
  border-radius: 18px;
  border: 1px solid rgba(255, 255, 255, 0.05);
  background: rgba(5, 10, 28, 0.85);
  display: flex;
  justify-content: space-between;
  gap: 1rem;
  flex-wrap: wrap;
}

.order-info h4 {
  margin: 0;
  font-size: 1.1rem;
}

.order-info p {
  margin: 0.35rem 0 0;
  color: rgba(248, 250, 252, 0.65);
}

.status-confirmed {
  color: #34d399;
}

.status-pending {
  color: #fbbf24;
}

.status-cancelled {
  color: #f87171;
}

.status-completed {
  color: #87CEEB;
}

.order-actions {
  display: flex;
  align-items: center;
  gap: 0.9rem;
}

.order-price {
  font-size: 1.3rem;
  font-weight: 600;
}

.detail-btn {
  border: none;
  border-radius: 12px;
  padding: 0.6rem 1rem;
  background: rgba(14, 165, 233, 0.25);
  color: #bae6fd;
  cursor: pointer;
}
</style>


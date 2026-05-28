<template>
  <div class="mobile-home">
    <section class="quick-search">
      <div class="search-card" @click="navigate('booking')">
        <div class="search-title">出发 - 到达</div>
        <div class="search-sub">选择日期、舱位与乘客</div>
      </div>
    </section>

    <section class="quick-actions">
      <div class="actions-row">
        <div class="action" @click="navigate('booking')">预订航班</div>
        <div class="action" @click="navigate('orders')">我的订单</div>
        <div class="action" @click="navigate('profile')">个人中心</div>
      </div>
    </section>

    <section class="promo">
      <h4>行程提醒</h4>
      <div class="promo-card">暂无行程</div>
    </section>
  </div>
</template>

<script setup lang="ts">
const props = defineProps<{ navigate: (tab: string) => void }>()
const navigate = props.navigate || (() => {})
</script>

<style scoped>
.mobile-home .search-card {
  background: #fff;
  padding: 12px;
  border-radius: 8px;
  box-shadow: 0 6px 18px rgba(0,0,0,0.04);
}
.actions-row {
  display:flex;
  gap:8px;
  margin-top:12px;
}
.action {
  flex:1;
  background:#fff;
  padding:12px;
  border-radius:8px;
  text-align:center;
  box-shadow:0 6px 18px rgba(0,0,0,0.04);
  cursor:pointer;
}
.promo-card {
  background:#fff;
  padding:10px;
  border-radius:8px;
  margin-top:8px;
}
</style>



<template>
  <div class="mobile-booking">
    <div class="booking-header">搜索并选择航班</div>
    <div class="booking-form">
      <div class="field">出发地：<input placeholder="请输入出发城市" /></div>
      <div class="field">目的地：<input placeholder="请输入目的城市" /></div>
      <div class="field">出发日期：<input type="date" /></div>
      <div class="actions">
        <button @click="search">搜索航班</button>
      </div>
    </div>

    <div class="search-results">
      <div class="result-item">示例航班：A -> B 08:00 - 10:00 ￥520</div>
      <div class="result-item">示例航班：A -> B 12:00 - 14:00 ￥620</div>
    </div>
  </div>
</template>

<script setup lang="ts">
const search = () => {
  // 临时示例：实际应调用航班搜索接口
  alert('执行搜索（示例）')
}
</script>

<style scoped>
.booking-header {
  font-weight: 700;
  margin-bottom: 8px;
}
.booking-form {
  background:#fff;
  padding:10px;
  border-radius:8px;
}
.field {
  margin-bottom:8px;
}
.actions button {
  background:#0284c7;
  color:#fff;
  padding:8px 12px;
  border-radius:6px;
  border:none;
  cursor:pointer;
}
.search-results .result-item {
  background:#fff;
  margin-top:10px;
  padding:10px;
  border-radius:8px;
}
</style>



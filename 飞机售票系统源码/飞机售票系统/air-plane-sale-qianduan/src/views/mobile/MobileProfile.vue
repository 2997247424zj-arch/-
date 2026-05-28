<template>
  <div class="mobile-profile">
    <h3>个人中心</h3>
    <div class="profile-card">
      <div>用户名：{{ username }}</div>
      <div>手机号：{{ phone }}</div>
    </div>
    <div class="profile-actions">
      <button @click="exit()">返回桌面版</button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import store from '../../services/store'
const username = computed(() => store.userState.userInfo?.username || '未设置')
const phone = computed(() => store.userState.userInfo?.phone || '未设置')

const props = defineProps<{ exit?: () => void }>()
const exit = props.exit || (() => {})
</script>

<style scoped>
.profile-card {
  background:#fff;
  padding:12px;
  border-radius:8px;
}
.profile-actions button {
  margin-top:12px;
  padding:8px 10px;
  border-radius:8px;
  border:none;
  background:#0284c7;
  color:#fff;
  cursor:pointer;
}
</style>

 



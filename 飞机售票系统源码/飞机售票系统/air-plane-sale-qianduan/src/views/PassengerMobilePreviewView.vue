<template>
  <PassengerLayout>
    <div class="mobile-preview-root">
      <div class="preview-toolbar">
        <button class="close-btn" @click="closePreview">← 关闭预览</button>
      </div>

  <div class="mobile-frame">
    <div class="mobile-inner">
      <!-- 使用新的移动端外壳（内部复用完整的 PassengerDashboard） -->
      <MobileShell />
    </div>
  </div>
    </div>
  </PassengerLayout>
</template>

<script setup lang="ts">
import { onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import PassengerLayout from '../components/layout/PassengerLayout.vue'
import MobileShell from './mobile/MobileShell.vue'

const router = useRouter()

const closePreview = () => {
  try {
    document.documentElement.classList.remove('mobile-preview')
  } catch (e) {}
  router.push('/portal/passengers')
}

onMounted(() => {
  try {
    document.documentElement.classList.add('mobile-preview')
    localStorage.setItem('mobile_preview', 'true')
  } catch (e) {}
})

onUnmounted(() => {
  try {
    document.documentElement.classList.remove('mobile-preview')
    localStorage.setItem('mobile_preview', 'false')
  } catch (e) {}
})
</script>

<style scoped>
.mobile-preview-root {
  padding: 12px 24px;
}
.preview-toolbar {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 12px;
}
.close-btn {
  background: #fff;
  border: 1px solid rgba(0,0,0,0.08);
  padding: 6px 10px;
  border-radius: 8px;
  cursor: pointer;
}
.mobile-frame {
  display: flex;
  justify-content: center;
}
.mobile-inner {
  width: 428px; /* iQOO12Pro 近似宽度（像素） */
  min-height: 920px; /* 近似屏高以更贴合实际手机视窗 */
  max-width: 100%;
  background: #fff;
  border-radius: 18px;
  box-shadow: 0 10px 30px rgba(0,0,0,0.12);
  overflow: hidden;
}

.mobile-dashboard-wrapper {
  width: 100%;
  height: 100%;
  overflow: auto;
  -webkit-overflow-scrolling: touch;
}
</style>



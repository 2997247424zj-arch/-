<template>
  <button
    class="mobile-toggle"
    :class="{ active: isActive }"
    @click="handleToggle"
    title="切换为手机端预览"
  >
    📱 手机预览
  </button>
</template>

<script setup lang="ts">
interface Props {
  isActive: boolean
}

interface Emits {
  toggle: []
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const handleToggle = () => {
  emit('toggle')
}
</script>

<style scoped>
.mobile-toggle {
  background: rgba(255,255,255,0.04);
  color: rgba(255,255,255,0.9);
  border: 1px solid rgba(255,255,255,0.06);
  padding: 8px 12px;
  border-radius: 12px;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.18s ease;
}

.mobile-toggle:hover {
  background: rgba(255,255,255,0.08);
  transform: translateY(-2px);
  box-shadow: 0 6px 18px rgba(0,0,0,0.12);
}

.mobile-toggle.active {
  background: linear-gradient(90deg, #3B82F6, #1E40AF);
  color: #fff;
  border-color: rgba(59,130,246,0.4);
}
</style>

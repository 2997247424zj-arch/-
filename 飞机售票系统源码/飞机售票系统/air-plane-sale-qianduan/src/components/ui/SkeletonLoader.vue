<template>
  <div :class="['skeleton-loader', className]" :style="styleObject">
    <div v-if="type === 'card'" class="skeleton skeleton-rect"></div>
    <div v-else-if="type === 'avatar-line'">
      <div class="skeleton skeleton-avatar" :style="{ display: 'inline-block', verticalAlign: 'middle' }"></div>
      <div style="display:inline-block;width:calc(100% - 64px);padding-left:12px;">
        <div class="skeleton skeleton-line" :style="{ width: '70%' }"></div>
        <div class="skeleton skeleton-line" :style="{ width: '50%' }"></div>
      </div>
    </div>
    <div v-else>
      <div v-for="n in lines" :key="n" class="skeleton skeleton-line" :style="{ width: lineWidth(n) }"></div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
const props = defineProps({
  type: { type: String, default: 'lines' },
  lines: { type: Number, default: 3 },
  width: { type: [String, Number], default: '100%' },
  height: { type: [String, Number], default: 'auto' },
  className: { type: String, default: '' }
})

const styleObject = computed(() => {
  return {
    width: typeof props.width === 'number' ? props.width + 'px' : props.width,
    height: typeof props.height === 'number' ? props.height + 'px' : props.height
  }
})

function lineWidth(n: number) {
  const base = 100 - (n - 1) * 8
  return base + '%'
}
</script>

<style scoped>
.skeleton-loader { display: block; width: 100%; }
.skeleton { background: linear-gradient(90deg, rgba(0,0,0,0.04) 0%, rgba(0,0,0,0.06) 50%, rgba(0,0,0,0.04) 100%); background-size: 200% 100%; animation: skeletonShimmer 1.2s linear infinite; }
.skeleton-line { height: 12px; border-radius: 6px; margin: 8px 0; }
.skeleton-avatar { width: 48px; height: 48px; border-radius: 50%; }
.skeleton-rect { width: 100%; height: 160px; border-radius: 10px; }
</style>



<template>
  <div class="action-buttons-small">
    <button
      v-if="forceShow || isPending(status)"
      class="action-btn-small approve-btn"
      @click="$emit('approve')"
      :title="approveTitle"
    >
      ✓
    </button>
    <button
      v-if="forceShow || isPending(status)"
      class="action-btn-small reject-btn"
      @click="$emit('reject')"
      :title="rejectTitle"
    >
      ✗
    </button>
    <button
      class="action-btn-small detail-btn"
      @click="$emit('detail')"
      title="详情"
    >
      👁️
    </button>
  </div>
</template>

<script setup lang="ts">
const { status, approveTitle, rejectTitle, forceShow } = defineProps({
  status: { type: String, required: false },
  approveTitle: { type: String, default: '通过' },
  rejectTitle: { type: String, default: '驳回' },
  // 如果为 true 则无视 status 显示批准/驳回（用于某些场景如退票审核需要强制显示）
  forceShow: { type: Boolean, default: false }
})

// 兼容后端返回的中文/英文状态变体（空/无值默认认为 pending）
const isPending = (s: any): boolean => {
  if (s === undefined || s === null) return true
  const str = String(s).trim()
  if (!str) return true
  const lower = str.toLowerCase()
  const variants = ['pending', '待处理', '待审核', '待受理', '未处理']
  return variants.some(v => lower === v || lower.includes(v))
}
</script>

<style scoped>
.action-buttons-small {
  display:flex;
  gap:8px;
  align-items:center;
}
.action-btn-small {
  width:34px;
  height:34px;
  display:inline-flex;
  align-items:center;
  justify-content:center;
  border-radius:6px;
  border:1px solid rgba(255,255,255,0.06);
  background: rgba(15,23,42,0.6);
  color:#fff;
  cursor:pointer;
  transition: transform 140ms ease, box-shadow 140ms;
}
.action-btn-small:hover { transform: translateY(-2px); box-shadow: 0 6px 14px rgba(2,8,20,0.36); }
.approve-btn { border-color: rgba(34,197,94,0.2); color:#4ade80; }
.reject-btn { border-color: rgba(248,113,113,0.2); color:#f87171; }
.detail-btn { border-color: rgba(99,102,241,0.2); color:#93c5fd; }
</style>



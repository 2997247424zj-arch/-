<template>
  <transition name="fade-scale">
    <div v-if="state.visible" class="global-dialog-backdrop" @click.self="onCancel">
      <div class="global-dialog-card">
        <div class="global-dialog-header">
          <h3>{{ state.title }}</h3>
        </div>
        <div class="global-dialog-body">
          <div class="message" v-html="state.message"></div>
          <div v-if="state.type === 'prompt'" class="prompt-input">
            <input v-model="state.input" :placeholder="state.placeholder || '请输入'" @keyup.enter="onOk" />
          </div>
        </div>
        <div class="global-dialog-actions">
          <button v-if="state.type !== 'alert'" class="btn btn-ghost" @click="onCancel">取消</button>
          <button class="btn btn-primary" @click="onOk">{{ primaryText }}</button>
        </div>
      </div>
    </div>
  </transition>
</template>

<script setup lang="ts">
import { dialogState as state } from '../services/dialog'
import { computed } from 'vue'

const primaryText = computed(() => {
  return state.type === 'confirm' ? '确认' : state.type === 'prompt' ? '确认' : '知道了'
})

function onOk() {
  if (state.resolve) {
    if (state.type === 'prompt') state.resolve(state.input)
    else state.resolve(true)
  } else {
    state.visible = false
  }
}

function onCancel() {
  if (state.resolve && state.type === 'confirm') state.resolve(false)
  else if (state.resolve && state.type === 'prompt') state.resolve(null)
  state.visible = false
}
</script>

<style scoped>
.global-dialog-backdrop {
  position: fixed;
  inset: 0;
  background: rgba(2, 6, 23, 0.6);
  backdrop-filter: blur(6px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 20000;
  padding: 20px;
}
.global-dialog-card {
  width: 520px;
  max-width: 96%;
  background: linear-gradient(180deg, rgba(8,12,20,0.98), rgba(7,10,16,0.98));
  border-radius: 12px;
  border: 1px solid rgba(255,255,255,0.04);
  box-shadow: 0 20px 60px rgba(2,6,23,0.6);
  padding: 18px;
}
.global-dialog-header h3 { margin:0 0 8px 0; color:var(--color-text-primary); font-size:18px; }
.global-dialog-body .message { color:var(--color-text-secondary); margin-bottom:12px; white-space:pre-wrap; }
.prompt-input input { width:100%; padding:10px 12px; border-radius:8px; border:1px solid rgba(255,255,255,0.06); background:rgba(255,255,255,0.02); color:var(--color-text-primary); }
.global-dialog-actions { display:flex; justify-content:flex-end; gap:10px; margin-top:12px; }
.btn { padding:8px 12px; border-radius:8px; cursor:pointer; }
.btn-primary { background: linear-gradient(90deg,#3b82f6,#6366f1); color:#fff; border:none; }
.btn-ghost { background:transparent; border:1px solid rgba(255,255,255,0.06); color:var(--color-text-primary); }
</style>


<template>
  <transition name="modal-fade">
    <div
      v-if="modelValue"
      class="modal-overlay"
      @click.self="handleClose"
    >
      <div class="modal-card">
        <div class="modal-header">
          <h3>{{ title }}</h3>
          <button class="close-btn" @click="handleClose">×</button>
        </div>
        <div class="modal-body">
          <p class="modal-message" v-if="message">{{ message }}</p>
          <div class="input-wrapper">
            <textarea
              v-if="multiline"
              ref="inputRef"
              v-model="inputValue"
              :placeholder="placeholder"
              class="modal-input modal-textarea"
              rows="4"
              @keydown.enter.exact.prevent="handleConfirm"
              @keydown.enter.shift.exact="inputValue += '\n'"
            ></textarea>
            <input
              v-else
              ref="inputRef"
              v-model="inputValue"
              type="text"
              :placeholder="placeholder"
              class="modal-input"
              @keydown.enter.exact.prevent="handleConfirm"
            />
          </div>
          <div v-if="errorMessage" class="error-message">
            {{ errorMessage }}
          </div>
        </div>
        <div class="modal-actions">
          <button
            type="button"
            class="modal-btn ghost"
            @click="handleCancel"
          >
            {{ cancelText }}
          </button>
          <button
            type="button"
            class="modal-btn primary"
            :disabled="!canConfirm"
            @click="handleConfirm"
          >
            {{ confirmText }}
          </button>
        </div>
      </div>
    </div>
  </transition>
</template>

<script setup lang="ts">
import { ref, computed, watch, nextTick } from 'vue'

interface Props {
  modelValue: boolean
  title: string
  message?: string
  placeholder?: string
  confirmText?: string
  cancelText?: string
  multiline?: boolean
  required?: boolean
  maxLength?: number
}

const props = withDefaults(defineProps<Props>(), {
  message: '',
  placeholder: '请输入内容',
  confirmText: '确定',
  cancelText: '取消',
  multiline: false,
  required: true,
  maxLength: undefined
})

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
  (e: 'confirm', value: string): void
  (e: 'cancel'): void
}>()

const inputValue = ref('')
const inputRef = ref<HTMLInputElement | HTMLTextAreaElement | null>(null)
const errorMessage = ref('')

const canConfirm = computed(() => {
  if (!props.required) return true
  return inputValue.value.trim().length > 0
})

watch(() => props.modelValue, (newVal) => {
  if (newVal) {
    inputValue.value = ''
    errorMessage.value = ''
    nextTick(() => {
      inputRef.value?.focus()
    })
  }
})

const handleConfirm = () => {
  if (!canConfirm.value) {
    errorMessage.value = '内容不能为空'
    return
  }
  
  if (props.maxLength && inputValue.value.length > props.maxLength) {
    errorMessage.value = `内容不能超过 ${props.maxLength} 个字符`
    return
  }
  
  errorMessage.value = ''
  emit('confirm', inputValue.value.trim())
  emit('update:modelValue', false)
}

const handleCancel = () => {
  emit('cancel')
  emit('update:modelValue', false)
}

const handleClose = () => {
  emit('update:modelValue', false)
}
</script>

<style scoped>
.modal-fade-enter-active,
.modal-fade-leave-active {
  transition: opacity 0.3s ease;
}

.modal-fade-enter-from,
.modal-fade-leave-to {
  opacity: 0;
}

.modal-fade-enter-active .modal-card,
.modal-fade-leave-active .modal-card {
  transition: transform 0.3s ease, opacity 0.3s ease;
}

.modal-fade-enter-from .modal-card,
.modal-fade-leave-to .modal-card {
  opacity: 0;
  transform: scale(0.9) translateY(-20px);
}

.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(6, 11, 40, 0.55);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 100000;
  padding: 20px;
  box-sizing: border-box;
}

.modal-card {
  width: min(480px, calc(100vw - 40px));
  max-width: 480px;
  background: rgba(15, 23, 42, 0.95);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 20px;
  box-shadow:
    0 25px 50px rgba(15, 23, 42, 0.5),
    inset 0 1px rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(20px);
  overflow: hidden;
  animation: modal-pop 0.3s ease-out;
}

@keyframes modal-pop {
  0% {
    opacity: 0;
    transform: scale(0.95) translateY(-10px);
  }
  100% {
    opacity: 1;
    transform: scale(1) translateY(0);
  }
}

.modal-header {
  padding: 24px 28px 20px;
  border-bottom: 1px solid #ececec;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #fff;
}

.modal-header h3 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: #fff;
  letter-spacing: 0.3px;
}

.close-btn {
  background: none;
  border: none;
  font-size: 28px;
  color: rgba(255, 255, 255, 0.5);
  cursor: pointer;
  padding: 0;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  transition: all 0.2s ease;
  line-height: 1;
}

.close-btn:hover {
  background: rgba(255, 255, 255, 0.1);
  color: rgba(255, 255, 255, 0.8);
}

.modal-body {
  padding: 24px 28px;
  background: #fff;
}

.modal-message {
  margin: 0 0 16px;
  color: rgba(255, 255, 255, 0.7);
  font-size: 14px;
  line-height: 1.6;
}

.input-wrapper {
  margin-bottom: 12px;
}

.modal-input,
.modal-textarea {
  width: 100%;
  padding: 12px 16px;
  background: rgba(30, 41, 59, 0.6);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  color: #fff;
  font-size: 14px;
  font-family: inherit;
  transition: all 0.2s ease;
  box-sizing: border-box;
}

.modal-input:focus,
.modal-textarea:focus {
  outline: none;
  border-color: rgba(99, 102, 241, 0.5);
  background: rgba(30, 41, 59, 0.8);
  box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.1);
}

.modal-input::placeholder,
.modal-textarea::placeholder {
  color: rgba(255, 255, 255, 0.4);
}

.modal-textarea {
  resize: vertical;
  min-height: 100px;
  line-height: 1.6;
}

.error-message {
  margin-top: 8px;
  color: #ef4444;
  font-size: 13px;
  min-height: 20px;
}

    .modal-actions {
      padding: 20px 28px 24px;
      border-top: 1px solid rgba(255, 255, 255, 0.08);
      background: #fff;
      display: flex;
      justify-content: flex-end;
      gap: 12px;
    }

.modal-btn {
  padding: 10px 24px;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
  border: none;
  min-width: 80px;
}

.modal-btn.ghost {
  background: rgba(255, 255, 255, 0.05);
  color: rgba(255, 255, 255, 0.7);
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.modal-btn.ghost:hover {
  background: rgba(255, 255, 255, 0.1);
  color: rgba(255, 255, 255, 0.9);
}

.modal-btn.primary {
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%);
  color: #fff;
  box-shadow: 0 4px 12px rgba(99, 102, 241, 0.3);
}

.modal-btn.primary:hover:not(:disabled) {
  background: linear-gradient(135deg, #7c3aed 0%, #a78bfa 100%);
  box-shadow: 0 6px 16px rgba(99, 102, 241, 0.4);
  transform: translateY(-1px);
}

.modal-btn.primary:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.modal-btn:active:not(:disabled) {
  transform: translateY(0);
}
</style>


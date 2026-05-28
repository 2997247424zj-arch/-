<template>
  <transition name="modal-fade">
    <div
      v-if="modelValue"
      class="modal-overlay"
      :class="{
        'adaptive-position': adaptivePosition,
        'top-position': topPosition,
        'overlay-left': anchorAlignment === 'start'
      }"
      @click.self="handleClose"
    >
      <div ref="modalCardRef" class="modal-card" :class="[`modal-${type}`, { 'adaptive-card': adaptivePosition, 'top-card': topPosition }]">
        <div v-if="type !== 'error'" class="modal-icon" :class="`icon-${type}`">
          <span>{{ icon }}</span>
        </div>
        <div v-if="type !== 'error'" class="modal-content">
          <h3>{{ title }}</h3>
          <p>{{ message }}</p>
        </div>
        <div v-if="type !== 'error'" class="modal-actions">
          <button
            v-if="showCancel"
            type="button"
            class="modal-btn ghost"
            @click="handleCancel"
          >
            {{ cancelText }}
          </button>
          <button
            type="button"
            class="modal-btn primary"
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
import { computed, onMounted, onUnmounted, nextTick, watch, ref } from 'vue'

interface AnchorRect {
  x: number
  y: number
  width: number
  height: number
}

type AnchorAlignment = 'center' | 'start' | 'end'

interface Props {
  modelValue: boolean
  title: string
  message: string
  type?: 'success' | 'error' | 'info' | 'confirm'
  confirmText?: string
  cancelText?: string
  showCancel?: boolean
  autoClose?: boolean
  adaptivePosition?: boolean // 自适应位置
  topPosition?: boolean // 页面顶部居中（用于乘客相关弹窗）
  /** 顶部下滑动效（用于错误提示从顶部划出） */
  slideFromTop?: boolean
  /** 触发按钮在视口中的矩形，用于根据按钮位置智能定位弹窗 */
  anchor?: AnchorRect | null
  /** 首选的弹窗相对按钮位置 */
  placement?: 'auto' | 'top' | 'bottom' | 'left' | 'right'
  /** 弹窗与按钮之间的视觉间距 */
  offset?: number
  /** 锚点水平对齐方式 */
  anchorAlignment?: AnchorAlignment
  /** 锚点额外水平偏移量（+向右，-向左） */
  anchorOffsetX?: number
  /** 居中模式时垂直额外偏移量（+向下，-向上） */
  centerOffsetY?: number
}

const props = withDefaults(defineProps<Props>(), {
  type: 'info',
  confirmText: '知道了',
  cancelText: '取消',
  showCancel: false,
  autoClose: false,
  adaptivePosition: false,
  topPosition: false,
  slideFromTop: false,
  anchor: null,
  placement: 'auto',
  offset: 12,
  anchorAlignment: 'center',
  anchorOffsetX: 0,
  centerOffsetY: 0
})

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
  (e: 'confirm'): void
  (e: 'cancel'): void
}>()

const icon = computed(() => {
  switch (props.type) {
    case 'success':
      return '✓'
    case 'error':
      return '✕'
    case 'confirm':
      return '?'
    default:
      return 'ℹ'
  }
})

const close = () => {
  emit('update:modelValue', false)
}

const handleConfirm = () => {
  emit('confirm')
  if (!props.showCancel) {
    close()
  }
}

const handleCancel = () => {
  emit('cancel')
  close()
}

const handleClose = () => {
  if (!props.showCancel) {
    close()
  }
}

// 弹窗定位优化
const modalCardRef = ref<HTMLElement | null>(null)

// 计算并调整弹窗位置 - 自动定位到视口中心或靠近触发按钮
const adjustModalPosition = () => {
  if (!modalCardRef.value) return
  
  const modal = modalCardRef.value
  const viewportHeight = window.innerHeight
  const viewportWidth = window.innerWidth
  
  // 等待DOM更新后获取实际尺寸
  const modalHeight = modal.offsetHeight || modal.scrollHeight
  const modalWidth = modal.offsetWidth || modal.scrollWidth

  // 基础样式重置
  modal.style.position = 'fixed'
  modal.style.margin = '0'
  modal.style.zIndex = '100000'
  modal.style.maxHeight = `${Math.min(viewportHeight - 40, modalHeight)}px`
  modal.style.overflowY = modalHeight > viewportHeight - 40 ? 'auto' : 'visible'

  // 如果提供了锚点信息，则优先根据按钮位置进行智能定位
  if (props.anchor) {
    const { x, y, width, height } = props.anchor
    const centerX = x + width / 2
    const anchorTop = y
    const anchorBottom = y + height
    const offset = props.offset

    let preferredPlacement = props.placement

    // 自动选择合适的方位：优先显示在按钮下方，其次上方
    if (preferredPlacement === 'auto') {
      const spaceBelow = viewportHeight - anchorBottom
      const spaceAbove = anchorTop
      preferredPlacement = spaceBelow >= modalHeight || spaceBelow >= spaceAbove ? 'bottom' : 'top'
    }

    let top = 0

    if (preferredPlacement === 'bottom') {
      top = anchorBottom + offset
      // 如果下方空间不足，尝试自动回退到上方
      if (top + modalHeight > viewportHeight - 16) {
        const fallbackTop = anchorTop - offset - modalHeight
        if (fallbackTop >= 16) {
          preferredPlacement = 'top'
          top = fallbackTop
        } else {
          // 上下都放不下时，限制高度并贴近视口边缘
          top = Math.max(16, Math.min(anchorBottom + offset, viewportHeight - modalHeight - 16))
        }
      }
    }

    if (preferredPlacement === 'top') {
      top = anchorTop - offset - modalHeight
      if (top < 16) {
        const fallbackTop = anchorBottom + offset
        if (fallbackTop + modalHeight <= viewportHeight - 16) {
          preferredPlacement = 'bottom'
          top = fallbackTop
        } else {
          top = 16
        }
      }
    }

    // 左右位置：以按钮中心为基准居中显示
    let left = centerX - modalWidth / 2
    if (props.anchorAlignment === 'start') {
      left = x
    } else if (props.anchorAlignment === 'end') {
      left = x + width - modalWidth
    }
    left += props.anchorOffsetX
    // 防止超出左右视口
    if (left < 16) left = 16
    if (left + modalWidth > viewportWidth - 16) {
      left = viewportWidth - modalWidth - 16
    }

    modal.style.top = `${top}px`
    modal.style.left = `${left}px`
    modal.style.transform = 'none'
  } else {
    // 没有锚点信息时，使用原来的“顶部模式 / 居中模式”逻辑
    modal.style.left = '50%'

    if (props.topPosition) {
      // 顶部模式：距离顶部固定 120px
      modal.style.left = '50%'
      // 顶部滑入模式下，不设置内联 top/transform，交给 CSS 过渡控制
      if (!props.slideFromTop) {
        modal.style.top = '120px'
        modal.style.transform = 'translateX(-50%)'
      } else {
        modal.style.top = ''
        modal.style.transform = ''
      }
      if (modalHeight > viewportHeight - 240) {
        modal.style.maxHeight = `${viewportHeight - 240}px`
        modal.style.overflowY = 'auto'
      }
    } else {
      // 默认：居中模式
      const centerOffset = props.centerOffsetY
      modal.style.top = `calc(50% + ${centerOffset}px)`
      modal.style.transform = 'translate(-50%, -50%)'

      if (modalHeight > viewportHeight - 40) {
        modal.style.maxHeight = `${viewportHeight - 40}px`
        modal.style.overflowY = 'auto'
        modal.style.top = '20px'
        modal.style.transform = 'translate(-50%, 0)'
      }
    }

    if (modalWidth > viewportWidth - 40) {
      modal.style.width = `${viewportWidth - 40}px`
    }
  }
}

// 监听窗口大小变化 - 自动调整位置
const handleResize = () => {
  if (props.modelValue) {
    adjustModalPosition()
  }
}

onMounted(() => {
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
})

// 当弹窗显示时自动调整位置
watch(() => props.modelValue, async (newVal) => {
  if (newVal) {
    await nextTick()
    // 立即调整位置
    adjustModalPosition()
    // 延迟再次调整，确保DOM完全渲染
    setTimeout(() => {
      adjustModalPosition()
    }, 50)
  }
})
</script>

<style scoped>
/* 顶部滑入过渡 */
.slide-down-enter-active,
.slide-down-leave-active {
  transition: opacity 0.25s ease;
}
.slide-down-enter-active .modal-card,
.slide-down-leave-active .modal-card {
  transition: transform 0.28s ease, opacity 0.28s ease;
 
}
.slide-down-enter-from { opacity: 0; }
.slide-down-enter-from .modal-card { opacity: 0; transform: translate(-50%, -40px); }
.slide-down-enter-to { opacity: 1; }
.slide-down-leave-to { opacity: 0; }
.slide-down-leave-to .modal-card { opacity: 0; transform: translate(-50%, -20px); }

.modal-fade-enter-active,
.modal-fade-leave-active {
  transition: opacity 0.3s ease;
}

.modal-fade-enter-from,
.modal-fade-leave-to {
  opacity: 0;
}
/* 居中模式 */
.modal-overlay {
  position: fixed;
  inset: 0;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(6, 11, 40, 0.55);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 100000;
  padding: 20px;
  box-sizing: border-box;
  overflow-y: auto;
  /* 防止背景滚动 */
  overscroll-behavior: contain;
  /* 确保在移动设备上也能正确显示 */
  -webkit-overflow-scrolling: touch;
  /* 确保弹窗始终在视口中心 */
  min-height: 100vh;
  min-height: 100dvh;
  
}

/* 退出弹窗位置调整 */
.modal-overlay.adaptive-position {
  align-items: flex-start;
  padding-top: max(20px, env(safe-area-inset-top, 20px));
  padding-bottom: max(20px, env(safe-area-inset-bottom, 20px));
  margin-left: -740px;
  margin-top: 150px;

}

/* 顶部位置模式（用于乘客相关弹窗） */
.modal-overlay.top-position {
  align-items: flex-start;
  justify-content: center;
  padding-top: 120px;
  padding-bottom: 20px;
}

.modal-overlay.overlay-left {
  justify-content: flex-start;
  padding-left: clamp(24px, 6vw, 60px);
  padding-right: clamp(16px, 4vw, 32px);

}

.modal-overlay.overlay-left .modal-card {
  margin-left: 0;
  margin-right: auto;
}
/* 居中模式 */
.modal-card {
  width: min(480px, calc(100vw - 40px));
  max-width: 480px;
  background: linear-gradient(135deg, rgba(184, 194, 215, 0.98) 0%, rgba(203, 213, 228, 0.98) 100%);
  border: 1px solid rgba(255, 255, 255, 0.12);
  border-radius: 24px;
  padding: clamp(32px, 5vw, 48px) clamp(28px, 4vw, 40px) clamp(28px, 4vw, 36px);
  box-shadow:
    0 20px 60px rgba(0, 0, 0, 0.4),
    0 0 0 1px rgba(255, 255, 255, 0.05),
    inset 0 1px 0 rgba(255, 255, 255, 0.1);
  color: #fff;
  text-align: center;
  backdrop-filter: blur(24px) saturate(180%);
  animation: modal-pop 0.4s cubic-bezier(0.34, 1.56, 0.64, 1);
  margin: auto;
  position: relative;
  z-index: 100001;
  /* 确保弹窗在视口内，可以正常点击所有交互元素 */
  max-height: calc(100vh - 40px);
  max-height: calc(100dvh - 40px); /* 支持动态视口高度 */
  overflow-y: auto;
  /* 平滑滚动 */
  scroll-behavior: smooth;
  /* 确保在小屏幕上也能正确显示 */
  box-sizing: border-box;
  /* 确保弹窗始终可见 */
  will-change: transform;
  /* 确保弹窗在滚动容器中也能居中 */
  flex-shrink: 0;
  /* 添加微妙的背景光效 */
  overflow: hidden;
}

.modal-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(90deg, 
    transparent 0%, 
    rgba(99, 102, 241, 0.5) 50%, 
    transparent 100%);
  opacity: 0.6;
}

/* 自适应位置模式的卡片 */
.modal-card.adaptive-card {
  margin-top: clamp(20px, 10vh, 120px);
  margin-bottom: auto;
  position: relative;
}

/* 顶部位置模式的卡片 */
.modal-card.top-card {
  margin-top: 0;
  margin-bottom: auto;
  position: fixed;
  top: 120px;
  left: 50%;
  transform: translateX(-50%);
}

/* 响应式优化 - 平板 */
@media (min-width: 768px) and (max-width: 1024px) {
  .modal-card {
    width: min(500px, calc(100vw - 60px));
    padding: clamp(28px, 4vw, 36px) clamp(24px, 3vw, 32px) clamp(24px, 3vw, 28px);
  }
  
  .modal-card h3 {
    font-size: clamp(20px, 3vw, 22px);
  }
  
  .modal-card p {
    font-size: clamp(15px, 2.5vw, 16px);
  }
}

/* 响应式优化 - 移动端 */
@media (max-width: 767px) {
  .modal-overlay {
    padding: 12px;
    align-items: flex-start;
    padding-top: max(12px, env(safe-area-inset-top, 12px));
    padding-bottom: max(12px, env(safe-area-inset-bottom, 12px));
  }
  /* 居中模式 */
  .modal-overlay.overlay-left {
    padding-left: max(12px, env(safe-area-inset-left, 12px));
    padding-right: max(12px, env(safe-area-inset-right, 12px));
  }
  /* 居中模式 */
  .modal-card {
    width: calc(100vw - 24px);
    max-width: none;
    padding: clamp(20px, 5vw, 28px) clamp(16px, 4vw, 24px) clamp(16px, 4vw, 20px);
    border-radius: clamp(16px, 4vw, 20px);
    margin-top: auto;
    margin-bottom: auto;
  }
  
  .modal-card h3 {
    font-size: clamp(18px, 4.5vw, 20px);
  }
  
  .modal-card p {
    font-size: clamp(14px, 3.5vw, 15px);
  }
  /* 居中模式 */
  .modal-icon {
    width: clamp(56px, 14vw, 64px);
    height: clamp(56px, 14vw, 64px);
    font-size: clamp(24px, 6vw, 28px);
  }
  
  .modal-actions {
    flex-direction: column;
  }
  /* 居中模式 */
  .modal-btn {
    width: 100%;
    min-width: auto;

  }
}

/* 响应式优化 - 小屏幕移动端 */
@media (max-width: 480px) {
  .modal-overlay {
    padding: 8px;
    align-items: flex-start;
    padding-top: max(8px, env(safe-area-inset-top, 8px));
    padding-bottom: max(8px, env(safe-area-inset-bottom, 8px));
  }
  
  .modal-card {
    width: calc(100vw - 16px);
    padding: 20px 16px 16px;
    margin-top: auto;
    margin-bottom: auto;
  }
  
  .modal-card h3 {
    font-size: 18px;
  }
  
  .modal-card p {
    font-size: 14px;
  }
}

/* 响应式优化 - 低高度屏幕 */
@media (max-height: 600px) {
  .modal-overlay {
    align-items: flex-start;
    padding-top: 10px;
    padding-bottom: 10px;
  }
  
  .modal-card {
    max-height: calc(100vh - 20px);
    max-height: calc(100dvh - 20px);
    padding: clamp(20px, 3vh, 24px) clamp(20px, 4vw, 28px) clamp(16px, 2.5vh, 20px);
    margin-top: 0;
    margin-bottom: 0;
  }
  
  .modal-overlay.adaptive-position .modal-card.adaptive-card {
    margin-top: 10px;
  }
  
  .modal-icon {
    width: clamp(48px, 10vh, 60px);
    height: clamp(48px, 10vh, 60px);
    font-size: clamp(24px, 5vh, 28px);
    margin-bottom: clamp(12px, 2vh, 16px);
  }
  
  .modal-card h3 {
    margin-bottom: clamp(6px, 1vh, 8px);
  }
  
  .modal-card p {
    margin-bottom: clamp(12px, 2vh, 16px);
  }
  
  .modal-actions {
    margin-top: clamp(16px, 3vh, 20px);
  }
}

.modal-card h3 {
  font-size: clamp(20px, 4vw, 26px);
  margin-bottom: clamp(10px, 2vw, 14px);
  font-weight: 700;
  color: #fff;
  letter-spacing: -0.3px;
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
}

.modal-card.modal-confirm h3 {
  background: linear-gradient(135deg, #fff 0%, rgba(255, 255, 255, 0.9) 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.modal-card p {
  color: rgba(192, 209, 233, 0.75);
  font-size: clamp(15px, 3vw, 17px);
  line-height: 1.7;
  margin-bottom: clamp(20px, 4vw, 28px);
  font-weight: 400;
  letter-spacing: 0.2px;
}

.modal-icon {
  width: clamp(72px, 12vw, 88px);
  height: clamp(72px, 12vw, 88px);
  border-radius: 20px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: clamp(32px, 6vw, 40px);
  margin-bottom: clamp(20px, 4vw, 28px);
  position: relative;
  transition: transform 0.3s ease;
  box-shadow: 
    0 8px 24px rgba(0, 0, 0, 0.3),
    inset 0 1px 0 rgba(255, 255, 255, 0.1);
}

.modal-icon::before {
  content: '';
  position: absolute;
  inset: 0;
  border-radius: 20px;
  padding: 1px;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.2), rgba(255, 255, 255, 0.05));
  -webkit-mask: linear-gradient(#fff 0 0) content-box, linear-gradient(#fff 0 0);
  -webkit-mask-composite: xor;
  mask-composite: exclude;
  opacity: 0.5;
}

.icon-success {
  color: #10b981;
  background: linear-gradient(135deg, rgba(16, 185, 129, 0.2) 0%, rgba(5, 150, 105, 0.15) 100%);
  border: 1px solid rgba(16, 185, 129, 0.3);
  box-shadow: 
    0 8px 24px rgba(16, 185, 129, 0.2),
    inset 0 1px 0 rgba(255, 255, 255, 0.1);
}

.icon-error {
  color: #ef4444;
  background: linear-gradient(135deg, rgba(239, 68, 68, 0.2) 0%, rgba(220, 38, 38, 0.15) 100%);
  border: 1px solid rgba(239, 68, 68, 0.3);
  box-shadow: 
    0 8px 24px rgba(239, 68, 68, 0.2),
    inset 0 1px 0 rgba(255, 255, 255, 0.1);
}

.icon-info {
  color: #3b82f6;
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.2) 0%, rgba(37, 99, 235, 0.15) 100%);
  border: 1px solid rgba(59, 130, 246, 0.3);
  box-shadow: 
    0 8px 24px rgba(59, 130, 246, 0.2),
    inset 0 1px 0 rgba(255, 255, 255, 0.1);
}

.icon-confirm {
  color: #f59e0b;
  background: linear-gradient(135deg, rgba(245, 158, 11, 0.25) 0%, rgba(217, 119, 6, 0.2) 100%);
  border: 1px solid rgba(245, 158, 11, 0.4);
  box-shadow: 
    0 8px 24px rgba(245, 158, 11, 0.25),
    inset 0 1px 0 rgba(255, 255, 255, 0.15),
    0 0 20px rgba(245, 158, 11, 0.1);
  animation: icon-pulse 2s ease-in-out infinite;
}

@keyframes icon-pulse {
  0%, 100% {
    transform: scale(1);
    box-shadow: 
      0 8px 24px rgba(28, 140, 210, 0.25),
      inset 0 1px 0 rgba(255, 255, 255, 0.15),
      0 0 20px rgba(245, 158, 11, 0.1);
  }
  50% {
    transform: scale(1.02);
    box-shadow: 
      0 8px 24px rgba(245, 158, 11, 0.3),
      inset 0 1px 0 rgba(255, 255, 255, 0.15),
      0 0 30px rgba(245, 158, 11, 0.15);
  }
}

.modal-actions {
  margin-top: clamp(24px, 5vw, 32px);
  display: flex;
  justify-content: center;
  gap: clamp(12px, 3vw, 16px);
  flex-wrap: wrap;
}

/* 居中模式 */
.modal-btn {
  min-width: clamp(110px, 20vw, 140px);
  padding: clamp(12px, 2vw, 14px) clamp(24px, 4vw, 32px);
  border-radius: 12px;
  font-size: clamp(14px, 3vw, 16px);
  font-weight: 600;
  border: none;
  cursor: pointer;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
  flex: 1 1 auto;
  letter-spacing: 0.3px;
}

.modal-btn::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  width: 0;
  height: 0;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.2);
  transform: translate(-50%, -50%);
  transition: width 0.6s, height 0.6s;
}

.modal-btn:hover::before {
  width: 300px;
  height: 300px;
}

.modal-btn.primary {
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%);
  color: #fff;
  box-shadow: 
    0 4px 16px rgba(99, 102, 241, 0.4),
    0 0 0 1px rgba(255, 255, 255, 0.1) inset;
  position: relative;
}

.modal-btn.primary::after {
  content: '';
  position: absolute;
  inset: 0;
  border-radius: 12px;
  padding: 1px;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.3), rgba(255, 255, 255, 0.1));
  -webkit-mask: linear-gradient(#fff 0 0) content-box, linear-gradient(#fff 0 0);
  -webkit-mask-composite: xor;
  mask-composite: exclude;
  opacity: 0;
  transition: opacity 0.3s;
}

.modal-btn.primary:hover {
  transform: translateY(-2px);
  box-shadow: 
    0 8px 24px rgba(99, 102, 241, 0.5),
    0 0 0 1px rgba(255, 255, 255, 0.15) inset;
}

.modal-btn.primary:hover::after {
  opacity: 1;
}

.modal-btn.primary:active {
  transform: translateY(0);
  box-shadow: 
    0 4px 12px rgba(99, 102, 241, 0.4),
    0 0 0 1px rgba(255, 255, 255, 0.1) inset;
}

.modal-btn.ghost {
  background: rgba(255, 255, 255, 0.08);
  color: rgba(255, 255, 255, 0.9);
  border: 1px solid rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(10px);
}

.modal-btn.ghost:hover {
  background: rgba(255, 255, 255, 0.12);
  border-color: rgba(255, 255, 255, 0.25);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.modal-btn.ghost:active {
  transform: translateY(0);
}

.modal-btn span {
  position: relative;
  z-index: 1;
}

@keyframes modal-pop {
  0% {
    opacity: 0;
    transform: scale(0.92) translateY(-10px);
  }
  100% {
    opacity: 1;
    transform: scale(1) translateY(0);
  }
}
</style>
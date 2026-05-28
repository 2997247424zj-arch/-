<template>
  <div class="loading-spinner" :class="{ fullscreen }">
    <div class="spinner">
      <div class="bounce1"></div>
      <div class="bounce2"></div>
      <div class="bounce3"></div>
    </div>
    <p v-if="text" class="loading-text">{{ text }}</p>
  </div>
</template>

<script setup lang="ts">
interface Props {
  text?: string
  fullscreen?: boolean
}

withDefaults(defineProps<Props>(), {
  text: '加载中...',
  fullscreen: false,
})
</script>

<style scoped lang="scss">
.loading-spinner {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px;

  &.fullscreen {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(255, 255, 255, 0.9);
    z-index: 9999;
  }

  .spinner {
    display: flex;
    gap: 8px;

    > div {
      width: 12px;
      height: 12px;
      background-color: #4f46e5;
      border-radius: 100%;
      display: inline-block;
      animation: sk-bouncedelay 1.4s infinite ease-in-out both;
    }

    .bounce1 {
      animation-delay: -0.32s;
    }

    .bounce2 {
      animation-delay: -0.16s;
    }
  }

  .loading-text {
    margin-top: 16px;
    color: #64748b;
    font-size: 14px;
  }
}

@keyframes sk-bouncedelay {
  0%,
  80%,
  100% {
    transform: scale(0);
  }
  40% {
    transform: scale(1);
  }
}
</style>

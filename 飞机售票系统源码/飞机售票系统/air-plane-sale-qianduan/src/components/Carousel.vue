<template>
  <div class="carousel-container">
    <div class="carousel-wrapper" :style="{ transform: `translateX(-${currentIndex * 100}%)` }">
      <div
        v-for="(slide, index) in slides"
        :key="index"
        class="carousel-slide"
        :class="{ active: index === currentIndex }"
      >
        <div class="slide-image" :style="{ backgroundImage: `url(${slide.image})` }">
          <div class="slide-overlay"></div>
        </div>
        <div class="slide-content">
          <h3 class="slide-title">{{ slide.title }}</h3>
          <p class="slide-description">{{ slide.description }}</p>
        </div>
      </div>
    </div>

    <!-- 轮播指示器 -->
    <div class="carousel-indicators">
      <button
        v-for="(slide, index) in slides"
        :key="index"
        :class="['indicator', { active: index === currentIndex }]"
        @click="goToSlide(index)"
        :aria-label="`跳转到第 ${index + 1} 张`"
      ></button>
    </div>

    <!-- 切换按钮 -->
    <button
      class="carousel-btn prev-btn"
      @click="prevSlide"
      aria-label="上一张"
    >
      ‹
    </button>
    <button
      class="carousel-btn next-btn"
      @click="nextSlide"
      aria-label="下一张"
    >
      ›
    </button>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch } from 'vue'

interface Slide {
  image: string
  title: string
  description: string
}

interface Props {
  slides: Slide[]
  interval?: number // 轮播间隔时间（毫秒）
  autoplay?: boolean // 是否自动播放
}

const props = withDefaults(defineProps<Props>(), {
  interval: 5000,
  autoplay: true
})

const currentIndex = ref(0)
let autoplayTimer: number | null = null
const preloadedImages = new Set<string>()

const preloadImages = () => {
  props.slides.forEach(slide => {
    if (!slide?.image || preloadedImages.has(slide.image)) return
    const img = new Image()
    img.src = slide.image
    preloadedImages.add(slide.image)
  })
}

// 下一张
const nextSlide = () => {
  currentIndex.value = (currentIndex.value + 1) % props.slides.length
  resetAutoplay()
}

// 上一张
const prevSlide = () => {
  currentIndex.value = (currentIndex.value - 1 + props.slides.length) % props.slides.length
  resetAutoplay()
}

// 跳转到指定幻灯片
const goToSlide = (index: number) => {
  currentIndex.value = index
  resetAutoplay()
}

// 重置自动播放
const resetAutoplay = () => {
  if (autoplayTimer) {
    clearInterval(autoplayTimer)
  }
  if (props.autoplay) {
    startAutoplay()
  }
}

// 开始自动播放
const startAutoplay = () => {
  if (props.autoplay && props.slides.length > 1) {
    autoplayTimer = window.setInterval(() => {
      nextSlide()
    }, props.interval)
  }
}

// 停止自动播放
const stopAutoplay = () => {
  if (autoplayTimer) {
    clearInterval(autoplayTimer)
    autoplayTimer = null
  }
}

watch(
  () => props.slides,
  () => {
    preloadImages()
    resetAutoplay()
  },
  { immediate: true, deep: true }
)

onMounted(() => {
  preloadImages()
  if (props.autoplay) {
    startAutoplay()
  }
})

onUnmounted(() => {
  stopAutoplay()
})
</script>

<style scoped>
.carousel-container {
  position: relative;
  width: 100%;
  height: 100%;
  overflow: hidden;
  border-radius: 24px;
}

.carousel-wrapper {
  display: flex;
  transition: transform 0.6s cubic-bezier(0.4, 0, 0.2, 1);
  height: 100%;
}

.carousel-slide {
  min-width: 100%;
  height: 100%;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}

.slide-image {
  position: absolute;
  inset: 0;
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
}

.slide-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(
    135deg,
    rgba(2, 6, 23, 0.85) 0%,
    rgba(15, 23, 42, 0.75) 50%,
    rgba(2, 6, 23, 0.85) 100%
  );
}

.slide-content {
  position: relative;
  z-index: 1;
  text-align: center;
  padding: 2rem;
  max-width: 600px;
  color: #fff;
}

.slide-title {
  font-size: clamp(1.8rem, 4vw, 2.8rem);
  font-weight: 700;
  margin: 0 0 1rem 0;
  background: linear-gradient(135deg, #fff 0%, rgba(255, 255, 255, 0.9) 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  line-height: 1.2;
}

.slide-description {
  font-size: clamp(1rem, 2vw, 1.2rem);
  color: rgba(255, 255, 255, 0.9);
  line-height: 1.6;
  margin: 0;
}

.carousel-indicators {
  position: absolute;
  bottom: 1.5rem;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  gap: 0.5rem;
  z-index: 2;
}

.indicator {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  border: 2px solid rgba(255, 255, 255, 0.5);
  background: transparent;
  cursor: pointer;
  transition: all 0.3s ease;
  padding: 0;
}

.indicator:hover {
  background: rgba(255, 255, 255, 0.3);
  border-color: rgba(255, 255, 255, 0.7);
}

.indicator.active {
  background: #fff;
  border-color: #fff;
  width: 24px;
  border-radius: 5px;
}

.carousel-btn {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  width: 48px;
  height: 48px;
  border-radius: 50%;
  border: 2px solid rgba(255, 255, 255, 0.3);
  background: rgba(15, 23, 42, 0.6);
  backdrop-filter: blur(10px);
  color: #fff;
  font-size: 24px;
  font-weight: 300;
  cursor: pointer;
  transition: all 0.3s ease;
  z-index: 2;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0;
}

.carousel-btn:hover {
  background: rgba(135, 206, 235, 0.8);
  border-color: rgba(135, 206, 235, 0.8);
  transform: translateY(-50%) scale(1.1);
}

.prev-btn {
  left: 1.5rem;
}

.next-btn {
  right: 1.5rem;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .carousel-btn {
    width: 40px;
    height: 40px;
    font-size: 20px;
  }

  .prev-btn {
    left: 1rem;
  }

  .next-btn {
    right: 1rem;
  }

  .slide-content {
    padding: 1.5rem;
  }

  .carousel-indicators {
    bottom: 1rem;
  }
}
</style>


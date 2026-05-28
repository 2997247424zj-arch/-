<template>
  <div :class="['lazy-image-wrap', wrapperClass]" :style="wrapperStyle">
    <img
      ref="imgRef"
      :src="loaded ? src : placeholderSrc"
      :alt="alt"
      :class="['lazy-image', loaded ? 'loaded' : 'loading']"
      loading="lazy"
      @load="onLoad"
      @error="onError"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
const props = defineProps({
  src: { type: String, required: true },
  alt: { type: String, default: '' },
  placeholder: { type: String, default: '' },
  wrapperClass: { type: String, default: '' },
  width: { type: [String, Number], default: '100%' },
  height: { type: [String, Number], default: 'auto' }
})

const imgRef = ref<HTMLImageElement | null>(null)
const loaded = ref(false)
const errored = ref(false)

const placeholderSrc = props.placeholder || ''

function onLoad() {
  // small timeout to allow CSS transition
  setTimeout(() => (loaded.value = true), 80)
}
function onError() {
  errored.value = true
}

onMounted(() => {
  // If browser already cached image and fired load before mounted
  if (imgRef.value && imgRef.value.complete) {
    onLoad()
  }
})
</script>

<style scoped>
.lazy-image-wrap { display:block; overflow:hidden; border-radius:8px; width:100%; height:auto; }
.lazy-image { display:block; width:100%; height:auto; object-fit:cover; transition: filter 420ms ease, transform 420ms ease, opacity 280ms ease; filter: blur(12px) grayscale(8%); transform: scale(1.02); opacity: 0.98; }
.lazy-image.loaded { filter: none; transform: none; opacity: 1; }
.lazy-image.loading { opacity: 0.96; }
</style>



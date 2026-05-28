<template>
  <section class="gallery-section">
    <div class="gallery-header">
      <div>
        <p class="eyebrow">真实航旅瞬间</p>
        <h2>航线运营的现场画面</h2>
        <p class="subtext">
          精选真实拍摄的机队、航站与客舱照片，让数据面板背后的场景更加直观。
        </p>
      </div>
      <button class="gallery-cta" type="button" @click="handleCtaClick">
        查看航线图库
      </button>
    </div>

    <div class="gallery-grid">
      <article
        v-for="item in galleryItems"
        :key="item.title"
        class="gallery-card"
      >
        <div class="image-wrapper">
          <img
            :src="item.imageUrl"
            :alt="item.title"
            loading="lazy"
            decoding="async"
          />
          <div class="image-overlay">
            <span class="overlay-label">{{ item.statLabel }}</span>
            <span class="overlay-value">{{ item.statValue }}</span>
          </div>
        </div>
        <div class="gallery-content">
          <h3>{{ item.title }}</h3>
          <p>{{ item.description }}</p>
        </div>
      </article>
    </div>
  </section>
</template>

<script setup lang="ts">
const galleryItems = [
  {
    title: '浦东跑道清晨放行',
    description: '主干线航班以95%准点率完成早高峰放行，联合塔台快速放行机制生效。',
    imageUrl: '/飞机起飞图.png',
    statLabel: '准点率',
    statValue: '95%'
  },
  {
    title: 'A350 客舱夜航',
    description: '远程航线客座率达88%，全舱实时互联系统开放，乘客满意度保持 4.8 分。',
    imageUrl: '/轮播图2.jpg',
    statLabel: '客座率',
    statValue: '88%'
  },
  {
    title: '运营控制中心',
    description: '运行控制中心完成 320 架次航班动态监控，自动调配班次减少 18% 延误。',
    imageUrl: '/轮播图3.webp',
    statLabel: '受控航班',
    statValue: '320架次'
  }
]

const handleCtaClick = () => {
  console.info('查看航线图库')
}
</script>

<style scoped>
.gallery-section {
  margin-top: 3rem;
  padding: 2.5rem;
  border-radius: 28px;
  background: rgba(2, 6, 23, 0.78);
  border: 1px solid rgba(255, 255, 255, 0.08);
  box-shadow:
    0 30px 80px rgba(15, 23, 42, 0.45),
    inset 0 1px 0 rgba(255, 255, 255, 0.08);
  position: relative;
  overflow: hidden;
}

.gallery-section::before {
  content: '';
  position: absolute;
  inset: 0;
  background: radial-gradient(circle at 20% 20%, rgba(14, 165, 233, 0.25), transparent 45%),
    radial-gradient(circle at 80% 0%, rgba(99, 102, 241, 0.2), transparent 40%);
  filter: blur(50px);
  opacity: 0.6;
  pointer-events: none;
}

.gallery-header {
  display: flex;
  flex-wrap: wrap;
  justify-content: space-between;
  align-items: center;
  gap: 1.5rem;
  position: relative;
  z-index: 1;
}

.eyebrow {
  text-transform: uppercase;
  letter-spacing: 0.2em;
  font-size: 12px;
  color: rgba(148, 163, 184, 0.9);
  margin-bottom: 0.5rem;
}

.gallery-header h2 {
  margin: 0;
  font-size: clamp(1.75rem, 2.5vw, 2.25rem);
  color: #f8fafc;
}

.subtext {
  margin-top: 0.5rem;
  color: rgba(226, 232, 240, 0.8);
  max-width: 600px;
  font-size: 15px;
}

.gallery-cta {
  background: linear-gradient(135deg, #38bdf8 0%, #6366f1 100%);
  color: white;
  border: none;
  padding: 0.85rem 1.8rem;
  border-radius: 999px;
  font-weight: 600;
  cursor: pointer;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  box-shadow: 0 10px 30px rgba(14, 165, 233, 0.35);
}

.gallery-cta:hover {
  transform: translateY(-2px);
  box-shadow: 0 15px 35px rgba(99, 102, 241, 0.35);
}

.gallery-grid {
  margin-top: 2rem;
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 1.5rem;
  position: relative;
  z-index: 1;
}

.gallery-card {
  display: flex;
  flex-direction: column;
  background: rgba(15, 23, 42, 0.75);
  border-radius: 20px;
  border: 1px solid rgba(255, 255, 255, 0.08);
  overflow: hidden;
  backdrop-filter: blur(12px);
  box-shadow: 0 15px 35px rgba(15, 23, 42, 0.45);
}

.image-wrapper {
  position: relative;
  padding-top: 62%;
  overflow: hidden;
}

.image-wrapper img {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.6s ease;
}

.gallery-card:hover img {
  transform: scale(1.05);
}

.image-overlay {
  position: absolute;
  left: 1rem;
  bottom: 1rem;
  padding: 0.75rem 1.25rem;
  border-radius: 16px;
  background: rgba(2, 6, 23, 0.78);
  display: flex;
  flex-direction: column;
  gap: 0.2rem;
  border: 1px solid rgba(255, 255, 255, 0.1);
  box-shadow: 0 8px 20px rgba(2, 6, 23, 0.5);
}

.overlay-label {
  font-size: 12px;
  color: rgba(226, 232, 240, 0.8);
  letter-spacing: 0.08em;
}

.overlay-value {
  font-size: 20px;
  font-weight: 700;
  color: #f8fafc;
}

.gallery-content {
  padding: 1.5rem;
}

.gallery-content h3 {
  margin: 0 0 0.5rem;
  font-size: 1.1rem;
  color: #fff;
}

.gallery-content p {
  margin: 0;
  font-size: 0.95rem;
  color: rgba(226, 232, 240, 0.8);
  line-height: 1.6;
}

@media (max-width: 768px) {
  .gallery-section {
    padding: 1.5rem;
  }

  .gallery-cta {
    width: 100%;
    text-align: center;
  }
}
</style>


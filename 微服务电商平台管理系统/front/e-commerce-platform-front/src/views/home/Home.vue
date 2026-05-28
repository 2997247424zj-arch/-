<template>
  <div class="home-page">
    <section class="hero-section glass-panel">
      <div class="hero-content">
        <div class="hero-copy">
          <span class="hero-badge">精选尖货 · 当季热销 · 极速配送</span>
          <h1>焕新你的数字生活与品质消费体验</h1>
          <p>
            从旗舰数码、智能家电到潮流穿搭与生活好物，MicroMall
            为你带来更具设计感、更可靠、更高效的一站式购物体验。
          </p>
          <div class="hero-actions">
            <el-button type="primary" size="large" round @click="router.push('/product/list')">
              立即选购
            </el-button>
            <el-button size="large" round plain @click="scrollToRecommend">查看推荐</el-button>
          </div>
          <div class="hero-metrics">
            <div class="metric-card">
              <strong>3000+</strong>
              <span>精品SKU</span>
            </div>
            <div class="metric-card">
              <strong>24H</strong>
              <span>极速发货</span>
            </div>
            <div class="metric-card">
              <strong>98%</strong>
              <span>好评满意度</span>
            </div>
          </div>
        </div>

        <div class="hero-visual">
          <el-carousel height="460px" indicator-position="outside" autoplay>
            <el-carousel-item v-for="item in banners" :key="item.id">
              <div class="hero-slide" :style="{ backgroundImage: `linear-gradient(135deg, ${item.overlayStart}, ${item.overlayEnd}), url(${item.img})` }">
                <div class="slide-chip">{{ item.tag }}</div>
                <div class="slide-body">
                  <h2>{{ item.title }}</h2>
                  <p>{{ item.desc }}</p>
                </div>
              </div>
            </el-carousel-item>
          </el-carousel>
        </div>
      </div>
    </section>

    <section class="promo-strip surface-card">
      <div class="promo-left">
        <span class="promo-kicker">限时活动</span>
        <h3>春季焕新节 · 每满 300 减 50</h3>
        <p>热门数码、服饰、美妆及生活日用同步参与，限时包邮与会员积分翻倍。</p>
      </div>
      <div class="promo-right">
        <div class="promo-card">
          <strong>新人专享</strong>
          <span>注册即领 120 元礼包</span>
        </div>
        <div class="promo-card">
          <strong>PLUS 会员</strong>
          <span>购物返积分 + 专属折扣</span>
        </div>
      </div>
    </section>

    <section class="category-section">
      <div class="section-title-block">
        <div>
          <span class="eyebrow">SHOP BY CATEGORY</span>
          <h2>热门分类</h2>
        </div>
        <p>覆盖日常生活与热门消费场景，帮助你更快找到心仪商品。</p>
      </div>

      <div class="category-grid">
        <div
          v-for="cat in categories"
          :key="cat.id"
          class="category-card surface-card interactive-card"
          @click="goToCategory(cat.id)"
        >
          <div class="cat-icon" :style="{ background: cat.bg }">
            <el-icon :size="26"><component :is="cat.icon" /></el-icon>
          </div>
          <h3>{{ cat.name }}</h3>
          <p>{{ cat.desc }}</p>
        </div>
      </div>
    </section>

    <section ref="recommendSection" class="recommend-section">
      <div class="section-title-block">
        <div>
          <span class="eyebrow">EDITOR'S PICKS</span>
          <h2>为你推荐</h2>
        </div>
        <p>基于热销趋势与高评分商品，为你挑选更值得入手的精选好物。</p>
      </div>

      <div v-if="loading" class="product-skeleton-grid">
        <div v-for="i in 8" :key="i" class="skeleton-card surface-card">
          <el-skeleton animated>
            <template #template>
              <el-skeleton-item variant="image" style="width: 100%; height: 220px; border-radius: 20px" />
              <div style="padding: 16px 0 0">
                <el-skeleton-item variant="h3" style="width: 70%; height: 20px; margin-bottom: 12px" />
                <el-skeleton-item variant="text" style="width: 100%; margin-bottom: 8px" />
                <el-skeleton-item variant="text" style="width: 85%; margin-bottom: 18px" />
                <el-skeleton-item variant="button" style="width: 120px; height: 40px; border-radius: 999px" />
              </div>
            </template>
          </el-skeleton>
        </div>
      </div>

      <div v-else class="product-grid">
        <ProductCard
          v-for="item in hotProducts"
          :key="item.id"
          :product="item"
          @click="router.push(`/product/detail/${item.id}`)"
          @add-to-cart="handleAddToCart"
        />
      </div>
    </section>

    <section class="service-section surface-card">
      <div class="service-card">
        <div class="service-icon blue">
          <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.1" stroke-linecap="round" stroke-linejoin="round">
            <path d="M22 12h-4l-3 9L9 3l-3 9H2" />
          </svg>
        </div>
        <div>
          <h4>官方品质保障</h4>
          <p>严格甄选品牌与供应链，确保商品质量更稳定。</p>
        </div>
      </div>
      <div class="service-card">
        <div class="service-icon green">
          <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.1" stroke-linecap="round" stroke-linejoin="round">
            <path d="M3 3h18v13H3z" /><path d="M8 21h8" /><path d="M12 16v5" />
          </svg>
        </div>
        <div>
          <h4>全平台适配体验</h4>
          <p>支持多端浏览与下单，操作流程一致流畅。</p>
        </div>
      </div>
      <div class="service-card">
        <div class="service-icon amber">
          <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.1" stroke-linecap="round" stroke-linejoin="round">
            <path d="M12 8v4l3 3" /><circle cx="12" cy="12" r="10" />
          </svg>
        </div>
        <div>
          <h4>极速履约与售后</h4>
          <p>订单进度清晰可见，支持快速响应与高效售后处理。</p>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import {
  Iphone,
  Monitor,
  ShoppingBag,
  Goods,
  Reading,
  Van,
  Headset,
  House,
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getProductListAPI } from '@/api/modules/product'
import { addCartAPI } from '@/api/modules/cart'
import { useUserStore } from '@/store/user'
import ProductCard from '@/components/ProductCard.vue'
import type { Product } from '@/types'

const router = useRouter()
const userStore = useUserStore()
const recommendSection = ref<HTMLElement>()
const loading = ref(true)

const banners = ref([
  {
    id: 1,
    tag: '2026 新品速递',
    title: '旗舰数码焕新季',
    desc: '精选旗舰手机、轻薄电脑与智能穿戴，全面升级你的数字生活。',
    img: 'https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?auto=format&fit=crop&w=1200&q=80',
    overlayStart: 'rgba(30,64,175,0.78)',
    overlayEnd: 'rgba(59,130,246,0.34)',
  },
  {
    id: 2,
    tag: '家居生活精选',
    title: '提升日常幸福感的美好好物',
    desc: '从家电到居家用品，让空间更整洁、生活更舒适、购物更省心。',
    img: 'https://images.unsplash.com/photo-1505693416388-ac5ce068fe85?auto=format&fit=crop&w=1200&q=80',
    overlayStart: 'rgba(15,118,110,0.78)',
    overlayEnd: 'rgba(34,197,94,0.28)',
  },
  {
    id: 3,
    tag: '时尚穿搭专场',
    title: '高质感穿搭，轻松升级你的风格',
    desc: '潮流服饰、实用配件与风格单品，打造更有辨识度的个人穿搭。',
    img: 'https://images.unsplash.com/photo-1523381210434-271e8be1f52b?auto=format&fit=crop&w=1200&q=80',
    overlayStart: 'rgba(157,23,77,0.76)',
    overlayEnd: 'rgba(236,72,153,0.28)',
  },
])

const categories = ref([
  {
    id: 1,
    name: '手机数码',
    desc: '热门旗舰与智能硬件',
    icon: Iphone,
    bg: 'linear-gradient(135deg, #dbeafe, #bfdbfe)',
  },
  {
    id: 2,
    name: '电脑办公',
    desc: '高效设备与办公利器',
    icon: Monitor,
    bg: 'linear-gradient(135deg, #e0f2fe, #bae6fd)',
  },
  {
    id: 3,
    name: '服饰箱包',
    desc: '穿搭与通勤潮流选择',
    icon: ShoppingBag,
    bg: 'linear-gradient(135deg, #fce7f3, #fbcfe8)',
  },
  {
    id: 4,
    name: '品质百货',
    desc: '日用精选与实用好物',
    icon: Goods,
    bg: 'linear-gradient(135deg, #fef3c7, #fde68a)',
  },
  {
    id: 5,
    name: '图书文创',
    desc: '阅读与灵感补给站',
    icon: Reading,
    bg: 'linear-gradient(135deg, #ede9fe, #ddd6fe)',
  },
  {
    id: 6,
    name: '物流速达',
    desc: '更快履约服务体验',
    icon: Van,
    bg: 'linear-gradient(135deg, #dcfce7, #bbf7d0)',
  },
  {
    id: 7,
    name: '影音娱乐',
    desc: '沉浸式视听装备',
    icon: Headset,
    bg: 'linear-gradient(135deg, #e0e7ff, #c7d2fe)',
  },
  {
    id: 8,
    name: '居家电器',
    desc: '提升生活舒适度',
    icon: House,
    bg: 'linear-gradient(135deg, #fee2e2, #fecaca)',
  },
])

const hotProducts = ref<Product[]>([])

const fetchProducts = async () => {
  loading.value = true
  try {
    const res: any = await getProductListAPI({ page: 1, size: 8 })
    if (res.data?.list) {
      hotProducts.value = res.data.list
    } else if (Array.isArray(res.data)) {
      hotProducts.value = res.data.slice(0, 8)
    } else {
      hotProducts.value = []
    }
  } catch (err) {
    console.error('Failed to fetch home products', err)
    hotProducts.value = []
  } finally {
    loading.value = false
  }
}

const goToCategory = (id: number) => {
  router.push({ path: '/product/list', query: { categoryId: id } })
}

const handleAddToCart = async (product: Product) => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  try {
    await addCartAPI({ productId: product.id, quantity: 1 })
    ElMessage.success('已加入购物车')
  } catch {
    // interceptor handled
  }
}

const scrollToRecommend = () => {
  recommendSection.value?.scrollIntoView({ behavior: 'smooth', block: 'start' })
}

onMounted(() => {
  fetchProducts()
})
</script>

<style scoped lang="scss">
.home-page {
  display: flex;
  flex-direction: column;
  gap: 28px;
}

.hero-section {
  padding: 20px;
  border-radius: 32px;
}

.hero-content {
  display: grid;
  grid-template-columns: 1.05fr 1fr;
  gap: 24px;
  align-items: stretch;
}

.hero-copy {
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 24px 12px 24px 8px;
}

.hero-badge {
  display: inline-flex;
  align-items: center;
  width: fit-content;
  padding: 8px 14px;
  border-radius: 999px;
  background: rgba(30, 64, 175, 0.1);
  color: #1e40af;
  font-size: 13px;
  font-weight: 800;
  margin-bottom: 20px;
}

.hero-copy h1 {
  font-size: clamp(34px, 4.6vw, 56px);
  line-height: 1.06;
  margin-bottom: 18px;
}

.hero-copy p {
  font-size: 16px;
  line-height: 1.8;
  max-width: 560px;
  margin-bottom: 28px;
}

.hero-actions {
  display: flex;
  gap: 14px;
  margin-bottom: 28px;
}

.hero-metrics {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14px;
}

.metric-card {
  padding: 18px 16px;
  border-radius: 22px;
  background: linear-gradient(180deg, rgba(255,255,255,0.92), rgba(255,255,255,0.72));
  border: 1px solid rgba(219, 234, 254, 0.9);
  box-shadow: 0 12px 28px rgba(30, 64, 175, 0.08);

  strong {
    display: block;
    font-family: 'Rubik', sans-serif;
    font-size: 28px;
    color: #0f172a;
    margin-bottom: 6px;
  }

  span {
    font-size: 13px;
    color: #64748b;
    font-weight: 700;
  }
}

.hero-visual {
  min-width: 0;
}

.hero-slide {
  height: 460px;
  background-size: cover;
  background-position: center;
  border-radius: 28px;
  color: #fff;
  padding: 28px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  box-shadow: 0 24px 60px rgba(15, 23, 42, 0.2);
}

.slide-chip {
  display: inline-flex;
  width: fit-content;
  padding: 8px 14px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.16);
  backdrop-filter: blur(8px);
  font-size: 13px;
  font-weight: 800;
}

.slide-body {
  max-width: 75%;

  h2 {
    font-size: 34px;
    color: #fff;
    margin-bottom: 12px;
  }

  p {
    color: rgba(255, 255, 255, 0.9);
    line-height: 1.7;
    font-size: 15px;
  }
}

.promo-strip {
  padding: 24px 28px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 24px;
}

.promo-left {
  .promo-kicker {
    display: inline-block;
    margin-bottom: 10px;
    color: #22c55e;
    font-size: 13px;
    font-weight: 800;
    letter-spacing: 0.04em;
  }

  h3 {
    font-size: 28px;
    margin-bottom: 10px;
  }

  p {
    max-width: 640px;
    line-height: 1.7;
  }
}

.promo-right {
  display: flex;
  gap: 14px;
}

.promo-card {
  min-width: 180px;
  padding: 18px;
  border-radius: 20px;
  background: linear-gradient(135deg, rgba(30,64,175,0.08), rgba(34,197,94,0.08));
  border: 1px solid rgba(191, 219, 254, 0.9);

  strong {
    display: block;
    margin-bottom: 6px;
    font-size: 16px;
    color: #0f172a;
  }

  span {
    font-size: 13px;
    color: #64748b;
  }
}

.category-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 20px;
}

.category-card {
  padding: 24px;
}

.cat-icon {
  width: 58px;
  height: 58px;
  border-radius: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #0f172a;
  margin-bottom: 18px;
}

.category-card h3 {
  font-size: 20px;
  margin-bottom: 8px;
}

.category-card p {
  font-size: 14px;
  line-height: 1.7;
}

.product-grid,
.product-skeleton-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 22px;
}

.skeleton-card {
  padding: 18px;
  border-radius: 24px;
}

.service-section {
  padding: 24px 28px;
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 18px;
}

.service-card {
  display: flex;
  align-items: flex-start;
  gap: 14px;
  padding: 16px;
  border-radius: 22px;
  background: rgba(255, 255, 255, 0.7);
  border: 1px solid rgba(219, 234, 254, 0.9);

  h4 {
    font-size: 18px;
    margin-bottom: 6px;
  }

  p {
    font-size: 14px;
    line-height: 1.7;
  }
}

.service-icon {
  width: 48px;
  height: 48px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;

  &.blue {
    background: #dbeafe;
    color: #1d4ed8;
  }

  &.green {
    background: #dcfce7;
    color: #15803d;
  }

  &.amber {
    background: #fef3c7;
    color: #b45309;
  }
}

@media (max-width: 1024px) {
  .hero-content,
  .service-section,
  .category-grid,
  .product-grid,
  .product-skeleton-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .hero-content {
    grid-template-columns: 1fr;
  }

  .promo-strip {
    flex-direction: column;
    align-items: flex-start;
  }

  .promo-right {
    width: 100%;
    flex-wrap: wrap;
  }
}

@media (max-width: 768px) {
  .hero-section,
  .promo-strip,
  .service-section {
    padding: 18px;
  }

  .hero-metrics,
  .category-grid,
  .product-grid,
  .product-skeleton-grid,
  .service-section {
    grid-template-columns: 1fr;
  }

  .slide-body {
    max-width: 100%;

    h2 {
      font-size: 28px;
    }
  }

  .hero-actions {
    flex-wrap: wrap;
  }
}
</style>

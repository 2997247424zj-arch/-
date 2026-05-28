<template>
  <div class="product-card surface-card interactive-card" @click="handleClick">
    <div class="product-media">
      <img
        v-if="imageSrc"
        :src="imageSrc"
        :alt="product.name"
        loading="lazy"
      />
      <div v-else class="img-placeholder">暂无图片</div>

      <div class="product-badges">
        <el-tag v-if="isHot" type="danger" size="small">热销</el-tag>
        <el-tag v-if="isLowStock" type="warning" size="small">即将售罄</el-tag>
      </div>

      <div class="quick-view">查看详情</div>
    </div>

    <div class="product-info">
      <div class="product-top">
        <h3 class="name" :title="product.name">{{ product.name }}</h3>
        <p class="desc" v-if="product.description">{{ product.description }}</p>
      </div>

      <div class="rating-row">
        <div class="stars">
          <span v-for="n in 5" :key="n" class="star" :class="{ active: n <= rating }">★</span>
        </div>
        <span class="rating-text">{{ rating.toFixed(1) }} 分</span>
      </div>

      <div class="meta">
        <span class="sales" v-if="product.sales">已售 {{ product.sales }}</span>
        <span class="stock" v-if="showStock && product.stock !== undefined">库存 {{ product.stock }}</span>
      </div>

      <div class="bottom-row">
        <div class="price-box">
          <span class="price-symbol">¥</span>
          <span class="price">{{ Number(product.price).toFixed(2) }}</span>
        </div>
      </div>

      <el-button
        v-if="showAddCart"
        type="success"
        class="add-cart-btn"
        @click.stop="handleAddToCart"
      >
        加入购物车
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { Product } from '@/types'

interface Props {
  product: Product
  showStock?: boolean
  showAddCart?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  showStock: false,
  showAddCart: true,
})

const emit = defineEmits<{
  click: [product: Product]
  addToCart: [product: Product]
}>()

const imageSrc = computed(() => {
  return props.product.coverImg || (props.product as any).imgUrl || ''
})

const rating = computed(() => {
  const sales = Number(props.product.sales || 0)
  if (sales > 1000) return 4.9
  if (sales > 300) return 4.7
  if (sales > 80) return 4.5
  return 4.3
})

const isHot = computed(() => Number(props.product.sales || 0) > 300)
const isLowStock = computed(() => typeof props.product.stock === 'number' && props.product.stock > 0 && props.product.stock < 20)

const handleClick = () => {
  emit('click', props.product)
}

const handleAddToCart = () => {
  emit('addToCart', props.product)
}
</script>

<style scoped lang="scss">
.product-card {
  display: flex;
  flex-direction: column;
  overflow: hidden;
  height: 100%;
  min-height: 100%;
  border-radius: 26px;
}

.product-media {
  position: relative;
  height: 260px;
  overflow: hidden;
  background: linear-gradient(180deg, #f8fbff 0%, #eff6ff 100%);

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    transition: transform 0.35s ease;
  }

  .img-placeholder {
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #94a3b8;
    font-size: 14px;
    font-weight: 700;
  }
}

.product-card:hover {
  .product-media img {
    transform: scale(1.06);
  }

  .quick-view {
    opacity: 1;
    transform: translateY(0);
  }
}

.product-badges {
  position: absolute;
  top: 14px;
  left: 14px;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.quick-view {
  position: absolute;
  right: 16px;
  bottom: 16px;
  padding: 10px 14px;
  border-radius: 999px;
  background: rgba(15, 23, 42, 0.74);
  color: #fff;
  font-size: 13px;
  font-weight: 800;
  letter-spacing: 0.02em;
  opacity: 0;
  transform: translateY(6px);
  transition: all 0.22s ease;
}

.product-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: 18px 18px 20px;
}

.product-top {
  min-height: 86px;
}

.name {
  font-size: 18px;
  line-height: 1.4;
  font-weight: 800;
  color: #0f172a;
  margin: 0 0 10px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.desc {
  margin: 0;
  font-size: 13px;
  color: #64748b;
  line-height: 1.65;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.rating-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-top: 8px;
  margin-bottom: 10px;
}

.stars {
  display: flex;
  gap: 2px;
}

.star {
  color: #cbd5e1;
  font-size: 15px;
  line-height: 1;

  &.active {
    color: #f59e0b;
  }
}

.rating-text {
  font-size: 12px;
  color: #64748b;
  font-weight: 700;
}

.meta {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 16px;
  font-size: 12px;
  color: #94a3b8;

  .sales {
    color: #ea580c;
    font-weight: 700;
  }

  .stock {
    color: #475569;
    font-weight: 700;
  }
}

.bottom-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: auto;
  margin-bottom: 14px;
}

.price-box {
  display: flex;
  align-items: baseline;
  gap: 2px;
}

.price-symbol {
  color: #e11d48;
  font-size: 18px;
  font-weight: 800;
}

.price {
  color: #e11d48;
  font-size: 30px;
  line-height: 1;
  font-weight: 900;
  letter-spacing: -0.03em;
}

.add-cart-btn {
  width: 100%;
  height: 46px;
  border-radius: 999px;
  font-size: 15px;
  font-weight: 800;
}
</style>

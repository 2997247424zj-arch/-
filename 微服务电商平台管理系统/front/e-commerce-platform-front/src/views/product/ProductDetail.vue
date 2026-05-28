<template>
  <div class="product-detail-container" v-loading="loading">
    <div class="breadcrumb" v-if="product">
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item :to="{ path: '/product/list' }">全部商品</el-breadcrumb-item>
        <el-breadcrumb-item>{{ product.name }}</el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <div class="main-card glass-panel" v-if="product">
      <div class="gallery">
        <div class="main-image">
          <img :src="productImage" :alt="product.name" />
        </div>
      </div>

      <div class="info-section">
        <h1 class="title">{{ product.name }}</h1>
        <p class="desc">{{ product.description || '暂无详细描述' }}</p>

        <div class="price-box">
          <div class="price-label">特惠价</div>
          <div class="price"><span>¥</span>{{ Number(product.price).toFixed(2) }}</div>
        </div>

        <div class="stock-info">
          <div class="label">配送</div>
          <div class="value">至 全国各地 <span>免运费</span></div>
        </div>

        <div class="stock-info">
          <div class="label">库存</div>
          <div class="value" :class="{ 'low-stock': stock < 10 }">
            {{ stock > 0 ? `充足 (${stock}件可用)` : '缺货' }}
          </div>
        </div>

        <el-divider />

        <div class="action-box">
          <div class="quantity">
            <span class="label">数量</span>
            <el-input-number
              v-model="buyCount"
              :min="stock > 0 ? 1 : 0"
              :max="Math.max(stock, 0)"
              :disabled="stock === 0"
            />
          </div>
          <div class="buttons">
            <el-button
              color="#f97316"
              size="large"
              plain
              class="action-btn"
              :disabled="stock === 0"
              @click="handleAddToCart"
              >加入购物车</el-button
            >
            <el-button
              color="#4f46e5"
              size="large"
              class="action-btn"
              :disabled="stock === 0"
              @click="handleBuyNow"
              >立即购买</el-button
            >
          </div>
        </div>
      </div>
    </div>

    <!-- 商品评价 -->
    <div class="reviews-section" v-if="product">
      <ProductReviews :product-id="product.id" :can-review="userStore.isLoggedIn" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getProductDetailAPI, getProductStockAPI } from '@/api/modules/product'
import { addCartAPI } from '@/api/modules/cart'
import { useUserStore } from '@/store/user'
import ProductReviews from '@/components/ProductReviews.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const loading = ref(true)
const product = ref<any>(null)
const stock = ref(0)
const buyCount = ref(1)
const defaultProductImage = 'https://picsum.photos/seed/product-default/800/800'

const productImage = computed(() => {
  return product.value?.coverImg || product.value?.imgUrl || product.value?.image || defaultProductImage
})

const fetchDetail = async () => {
  const id = route.params.id as string
  try {
    const res: any = await getProductDetailAPI(id)
    if (res.data) product.value = res.data

    const stockRes: any = await getProductStockAPI(id)
    stock.value = Number(stockRes.data ?? 0)

    if (stock.value <= 0) {
      buyCount.value = 0
    } else if (buyCount.value < 1 || buyCount.value > stock.value) {
      buyCount.value = 1
    }
  } catch (err) {
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchDetail()
})

const handleAddToCart = async () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  try {
    await addCartAPI({ productId: product.value.id, quantity: buyCount.value })
    ElMessage.success('成功加入购物车')
  } catch (err) {
    // Handled globally
  }
}

const handleBuyNow = async () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  try {
    await addCartAPI({ productId: product.value.id, quantity: buyCount.value })
    router.push('/cart')
  } catch (err) {}
}
</script>

<style scoped lang="scss">
.breadcrumb {
  margin-bottom: 24px;
}

.main-card {
  display: flex;
  gap: 40px;
  background: #fff;
  padding: 40px;
  border-radius: 20px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.03);
}

.gallery {
  flex: 0 0 450px;

  .main-image {
    width: 100%;
    height: 450px;
    border-radius: 16px;
    overflow: hidden;
    border: 1px solid #f1f5f9;

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
      transition: transform 0.3s;

      &:hover {
        transform: scale(1.05);
      }
    }
  }
}

.info-section {
  flex: 1;
  display: flex;
  flex-direction: column;

  .title {
    font-size: 28px;
    font-weight: 700;
    color: #1e293b;
    margin: 0 0 12px;
    line-height: 1.4;
  }

  .desc {
    font-size: 15px;
    color: #64748b;
    margin: 0 0 24px;
    line-height: 1.6;
  }

  .price-box {
    background: linear-gradient(135deg, #fef2f2 0%, #fff1f2 100%);
    padding: 20px 24px;
    border-radius: 12px;
    margin-bottom: 24px;
    display: flex;
    align-items: baseline;
    gap: 12px;

    .price-label {
      color: #e11d48;
      font-size: 14px;
      font-weight: 500;
    }

    .price {
      color: #e11d48;
      font-size: 36px;
      font-weight: 800;

      span {
        font-size: 20px;
        margin-right: 4px;
      }
    }
  }

  .stock-info {
    display: flex;
    margin-bottom: 16px;
    font-size: 14px;

    .label {
      width: 60px;
      color: #64748b;
    }

    .value {
      color: #334155;

      span {
        color: #4f46e5;
      }

      &.low-stock {
        color: #e11d48;
        font-weight: 500;
      }
    }
  }

  .action-box {
    margin-top: 10px;

    .quantity {
      display: flex;
      align-items: center;
      gap: 16px;
      margin-bottom: 30px;

      .label {
        font-size: 14px;
        color: #64748b;
      }
    }

    .buttons {
      display: flex;
      gap: 20px;

      .action-btn {
        width: 180px;
        border-radius: 12px;
        font-weight: 600;
        letter-spacing: 1px;
      }
    }
  }
}

.reviews-section {
  margin-top: 24px;
  padding: 24px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}
</style>

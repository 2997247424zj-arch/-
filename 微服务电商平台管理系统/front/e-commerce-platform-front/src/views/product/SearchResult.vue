<template>
  <div class="search-result">
    <el-card class="search-header">
      <div class="search-info">
        <h2>搜索结果</h2>
        <p v-if="keyword">
          关键词: <span class="keyword">"{{ keyword }}"</span>
          <span class="count">找到 {{ productList.length }} 个商品</span>
        </p>
      </div>

      <el-input
        v-model="searchKeyword"
        placeholder="搜索商品名称或描述"
        class="search-input"
        clearable
        @keyup.enter="handleSearch"
      >
        <template #append>
          <el-button :icon="Search" @click="handleSearch" />
        </template>
      </el-input>
    </el-card>

    <div v-loading="loading" class="product-grid">
      <el-empty v-if="!loading && productList.length === 0" description="没有找到相关商品" />

      <div v-else class="grid-container">
        <el-card
          v-for="product in productList"
          :key="product.id"
          class="product-card"
          shadow="hover"
          @click="goToDetail(product.id)"
        >
          <div class="product-image">
            <img :src="product.imgUrl || product.image || product.coverImg || defaultImage" :alt="product.name" />
          </div>
          <div class="product-info">
            <h3 class="product-name">{{ product.name }}</h3>
            <p class="product-desc">{{ product.description }}</p>
            <div class="product-footer">
              <span class="product-price">¥{{ product.price }}</span>
              <el-button type="primary" size="small" @click.stop="addToCart(product)">
                加入购物车
              </el-button>
            </div>
          </div>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { searchProductAPI } from '@/api/modules/product'
import { addToCartAPI } from '@/api/modules/cart'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const keyword = ref('')
const searchKeyword = ref('')
const productList = ref<any[]>([])
const defaultImage = 'https://picsum.photos/seed/product-default/300/300'

const loadSearchResults = async () => {
  if (!keyword.value) return

  loading.value = true
  try {
    const res: any = await searchProductAPI(keyword.value)
    if (res.code === 200) {
      productList.value = res.data || []
    }
  } catch (error) {
    console.error('搜索失败:', error)
    ElMessage.error('搜索失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  if (!searchKeyword.value.trim()) {
    ElMessage.warning('请输入搜索关键词')
    return
  }

  router.push({
    name: 'SearchResult',
    query: { keyword: searchKeyword.value }
  })
}

const goToDetail = (id: number) => {
  router.push({ name: 'ProductDetail', params: { id } })
}

const addToCart = async (product: any) => {
  try {
    const res: any = await addToCartAPI({
      productId: product.id,
      quantity: 1
    })

    if (res.code === 200) {
      ElMessage.success('已添加到购物车')
    } else {
      ElMessage.error(res.msg || '添加失败')
    }
  } catch (error: any) {
    console.error('添加到购物车失败:', error)
    if (error.response?.status === 401) {
      ElMessage.warning('请先登录')
      router.push({ name: 'Login', query: { redirect: route.fullPath } })
    } else {
      ElMessage.error('添加失败')
    }
  }
}

onMounted(() => {
  keyword.value = (route.query.keyword as string) || ''
  searchKeyword.value = keyword.value
  if (keyword.value) {
    loadSearchResults()
  }
})

// 监听路由变化
router.afterEach((to) => {
  if (to.name === 'SearchResult') {
    keyword.value = (to.query.keyword as string) || ''
    searchKeyword.value = keyword.value
    loadSearchResults()
  }
})
</script>

<style scoped lang="scss">
.search-result {
  padding: 20px;
  max-width: var(--page-max-width);
  margin: 0 auto;
}

.search-header {
  margin-bottom: 24px;

  .el-card__body {
    display: flex;
    justify-content: space-between;
    align-items: center;
    gap: 20px;
  }

  .search-info {
    flex: 1;

    h2 {
      margin: 0 0 8px 0;
      font-size: 24px;
      color: #1e293b;
    }

    p {
      margin: 0;
      font-size: 14px;
      color: #64748b;

      .keyword {
        color: #3b82f6;
        font-weight: 600;
      }

      .count {
        margin-left: 12px;
        color: #94a3b8;
      }
    }
  }

  .search-input {
    width: 400px;
  }
}

.product-grid {
  min-height: 400px;
}

.grid-container {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
}

.product-card {
  cursor: pointer;
  transition: all 0.3s ease;

  &:hover {
    transform: translateY(-4px);
  }

  .product-image {
    width: 100%;
    height: 280px;
    overflow: hidden;
    border-radius: 8px;
    margin-bottom: 12px;

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }
  }

  .product-info {
    .product-name {
      margin: 0 0 8px 0;
      font-size: 16px;
      font-weight: 600;
      color: #1e293b;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .product-desc {
      margin: 0 0 12px 0;
      font-size: 14px;
      color: #64748b;
      height: 40px;
      overflow: hidden;
      text-overflow: ellipsis;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
    }

    .product-footer {
      display: flex;
      justify-content: space-between;
      align-items: center;

      .product-price {
        font-size: 20px;
        font-weight: 700;
        color: #ef4444;
      }
    }
  }
}
</style>

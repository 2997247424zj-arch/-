<template>
  <div class="product-list-page">
    <section class="list-hero surface-card">
      <div>
        <span class="hero-tag">SMART DISCOVERY</span>
        <h1>发现适合你的品质好物</h1>
        <p>多维筛选、智能排序与高颜值卡片浏览，让选购体验更高效、更轻松。</p>
      </div>
      <div class="hero-search glass-panel">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索商品名称、品牌或关键词"
          size="large"
          clearable
          @keyup.enter="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
          <template #append>
            <el-button type="primary" @click="handleSearch">搜索</el-button>
          </template>
        </el-input>
      </div>
    </section>

    <section class="product-layout">
      <aside class="filter-sidebar surface-card">
        <div class="filter-block">
          <div class="block-header">
            <h3>商品分类</h3>
            <span>{{ categories.length }} 个分类</span>
          </div>
          <div class="filter-tags">
            <button
              class="tag-btn"
              :class="{ active: !searchParams.categoryId }"
              @click="selectCategory('')"
            >
              全部商品
            </button>
            <button
              v-for="cat in categories"
              :key="cat.id"
              class="tag-btn"
              :class="{ active: searchParams.categoryId === cat.id.toString() }"
              @click="selectCategory(cat.id.toString())"
            >
              {{ cat.name }}
            </button>
          </div>
        </div>

        <div class="filter-block">
          <div class="block-header">
            <h3>价格区间</h3>
            <span>支持自定义</span>
          </div>
          <div class="price-box">
            <el-input-number
              v-model="searchParams.minPrice"
              :min="0"
              :controls="false"
              placeholder="最低价"
            />
            <span class="separator">—</span>
            <el-input-number
              v-model="searchParams.maxPrice"
              :min="0"
              :controls="false"
              placeholder="最高价"
            />
            <el-button type="primary" class="apply-btn" @click="applyFilters">应用筛选</el-button>
          </div>
        </div>

        <div class="filter-block quick-panel">
          <div class="block-header">
            <h3>推荐理由</h3>
          </div>
          <ul>
            <li>优先展示高销量与高口碑商品</li>
            <li>价格排序帮助快速比较</li>
            <li>分类标签支持一键切换浏览</li>
          </ul>
        </div>
      </aside>

      <main class="list-main">
        <div class="toolbar surface-card">
          <div class="toolbar-left">
            <span class="result-text">共找到 <strong>{{ total }}</strong> 件商品</span>
            <div class="sort-tags">
              <button class="sort-btn" :class="{ active: !searchParams.sortBy }" @click="selectSort('')">默认推荐</button>
              <button class="sort-btn" :class="{ active: searchParams.sortBy === 'price_asc' }" @click="selectSort('price_asc')">价格升序</button>
              <button class="sort-btn" :class="{ active: searchParams.sortBy === 'price_desc' }" @click="selectSort('price_desc')">价格降序</button>
              <button class="sort-btn" :class="{ active: searchParams.sortBy === 'sales' }" @click="selectSort('sales')">销量优先</button>
            </div>
          </div>
          <div class="toolbar-right">
            <div class="view-switch">
              <button class="view-btn" :class="{ active: viewMode === 'grid' }" @click="viewMode = 'grid'">网格</button>
              <button class="view-btn" :class="{ active: viewMode === 'compact' }" @click="viewMode = 'compact'">紧凑</button>
            </div>
          </div>
        </div>

        <div class="list-content">
          <div :class="['product-grid', viewMode]" v-if="products.length > 0">
            <ProductCard
              v-for="item in products"
              :key="item.id"
              :product="item"
              :show-stock="false"
              @click="router.push(`/product/detail/${item.id}`)"
              @add-to-cart="handleAddToCart"
            />
          </div>

          <EmptyState
            v-else
            type="default"
            title="暂无商品"
            description="当前筛选条件下没有找到合适商品，试试切换分类或调整价格区间。"
          >
            <el-button type="primary" @click="resetFilters">重置筛选</el-button>
          </EmptyState>
        </div>

        <div class="pagination-wrap" v-if="products.length > 0">
          <el-pagination
            background
            layout="prev, pager, next"
            :total="total"
            v-model:current-page="searchParams.page"
            :page-size="searchParams.size"
            @current-change="fetchProducts"
          />
        </div>
      </main>
    </section>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Search } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getProductListAPI, getCategoryListAPI, searchProductAPI } from '@/api/modules/product'
import { addCartAPI } from '@/api/modules/cart'
import { useUserStore } from '@/store/user'
import ProductCard from '@/components/ProductCard.vue'
import EmptyState from '@/components/EmptyState.vue'
import type { Product } from '@/types'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const categories = ref<any[]>([])
const products = ref<Product[]>([])
const total = ref(0)
const searchKeyword = ref('')
const viewMode = ref<'grid' | 'compact'>('grid')
const searchParams = reactive({
  page: 1,
  size: 12,
  categoryId: (route.query.categoryId as string) || '',
  keyword: (route.query.keyword as string) || '',
  minPrice: undefined as number | undefined,
  maxPrice: undefined as number | undefined,
  sortBy: '',
})

const fetchCategories = async () => {
  try {
    const res: any = await getCategoryListAPI()
    if (res.data) categories.value = res.data
  } catch {
    // handled by interceptor
  }
}

const fetchProducts = async () => {
  try {
    const params: any = { ...searchParams }
    if (!params.categoryId) delete params.categoryId
    if (!params.keyword) delete params.keyword
    if (!params.minPrice) delete params.minPrice
    if (!params.maxPrice) delete params.maxPrice
    if (!params.sortBy) delete params.sortBy

    let res: any
    if (params.keyword) {
      res = await searchProductAPI(params.keyword, params)
    } else {
      res = await getProductListAPI(params)
    }

    if (res.data?.list) {
      products.value = res.data.list
      total.value = res.data.total
    } else if (Array.isArray(res.data)) {
      products.value = res.data
      total.value = res.data.length
    } else {
      products.value = []
      total.value = 0
    }
  } catch {
    products.value = []
    total.value = 0
  }
}

const handleSearch = () => {
  searchParams.keyword = searchKeyword.value
  searchParams.page = 1
  router.replace({
    query: {
      ...route.query,
      keyword: searchParams.keyword || undefined,
      categoryId: searchParams.categoryId || undefined,
    },
  })
  fetchProducts()
}

const selectCategory = (id: string) => {
  searchParams.categoryId = id
  searchParams.page = 1
  router.replace({
    query: {
      ...route.query,
      categoryId: id || undefined,
      keyword: searchParams.keyword || undefined,
    },
  })
  fetchProducts()
}

const selectSort = (sortBy: string) => {
  searchParams.sortBy = sortBy
  searchParams.page = 1
  fetchProducts()
}

const applyFilters = () => {
  searchParams.page = 1
  fetchProducts()
}

const resetFilters = () => {
  searchParams.page = 1
  searchParams.categoryId = ''
  searchParams.keyword = ''
  searchKeyword.value = ''
  searchParams.minPrice = undefined
  searchParams.maxPrice = undefined
  searchParams.sortBy = ''
  router.replace({ query: {} })
  fetchProducts()
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
    // handled by interceptor
  }
}

watch(
  () => route.query.categoryId,
  (newVal) => {
    if (newVal !== searchParams.categoryId && newVal !== undefined) {
      searchParams.categoryId = newVal as string
      fetchProducts()
    }
  },
)

watch(
  () => route.query.keyword,
  (newVal) => {
    if (newVal !== searchParams.keyword && newVal !== undefined) {
      searchParams.keyword = newVal as string
      searchKeyword.value = newVal as string
      fetchProducts()
    }
  },
)

onMounted(() => {
  if (route.query.keyword) {
    searchKeyword.value = route.query.keyword as string
  }
  fetchCategories()
  fetchProducts()
})
</script>

<style scoped lang="scss">
.product-list-page {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.list-hero {
  padding: 28px;
  display: flex;
  justify-content: space-between;
  gap: 24px;
  align-items: center;

  h1 {
    font-size: clamp(30px, 4vw, 44px);
    margin: 10px 0 12px;
  }

  p {
    max-width: 620px;
    line-height: 1.8;
  }
}

.hero-tag {
  display: inline-flex;
  align-items: center;
  padding: 8px 14px;
  border-radius: 999px;
  background: rgba(30, 64, 175, 0.1);
  color: #1e40af;
  font-size: 13px;
  font-weight: 800;
}

.hero-search {
  width: min(420px, 100%);
  padding: 14px;
  border-radius: 24px;
}

.product-layout {
  display: grid;
  grid-template-columns: 300px 1fr;
  gap: 22px;
}

.filter-sidebar {
  padding: 22px;
  height: fit-content;
  position: sticky;
  top: 96px;
}

.filter-block + .filter-block {
  margin-top: 26px;
}

.block-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  margin-bottom: 14px;

  h3 {
    font-size: 18px;
  }

  span {
    font-size: 12px;
    color: #64748b;
    font-weight: 700;
  }
}

.filter-tags,
.sort-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.tag-btn,
.sort-btn,
.view-btn {
  border: 1px solid #dbeafe;
  background: #fff;
  color: #475569;
  border-radius: 999px;
  padding: 10px 16px;
  cursor: pointer;
  font-size: 13px;
  font-weight: 800;
  transition: all 0.2s ease;

  &:hover {
    color: #1e40af;
    border-color: #93c5fd;
    background: #eff6ff;
  }

  &.active {
    color: #fff;
    border-color: #1e40af;
    background: linear-gradient(135deg, #1e40af, #3b82f6);
    box-shadow: 0 10px 20px rgba(30, 64, 175, 0.16);
  }
}

.price-box {
  display: grid;
  grid-template-columns: 1fr auto 1fr;
  gap: 10px;
  align-items: center;

  .separator {
    color: #94a3b8;
    font-weight: 700;
  }
}

.apply-btn {
  grid-column: 1 / -1;
  margin-top: 12px;
  width: 100%;
}

.quick-panel ul {
  margin: 0;
  padding-left: 18px;
  display: flex;
  flex-direction: column;
  gap: 10px;

  li {
    color: #64748b;
    line-height: 1.7;
    font-size: 14px;
  }
}

.list-main {
  display: flex;
  flex-direction: column;
  gap: 18px;
  min-width: 0;
}

.toolbar {
  padding: 18px 20px;
  display: flex;
  justify-content: space-between;
  gap: 18px;
  align-items: center;
}

.toolbar-left {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
}

.result-text {
  color: #64748b;
  font-size: 14px;

  strong {
    color: #0f172a;
    font-size: 18px;
  }
}

.view-switch {
  display: flex;
  gap: 8px;
}

.list-content {
  min-height: 320px;
}

.product-grid {
  display: grid;
  gap: 20px;

  &.grid {
    grid-template-columns: repeat(4, minmax(0, 1fr));
  }

  &.compact {
    grid-template-columns: repeat(5, minmax(0, 1fr));
  }
}

.pagination-wrap {
  display: flex;
  justify-content: center;
  padding: 10px 0 6px;
}

@media (max-width: 1200px) {
  .product-layout {
    grid-template-columns: 1fr;
  }

  .filter-sidebar {
    position: static;
  }

  .product-grid.grid,
  .product-grid.compact {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 768px) {
  .list-hero,
  .toolbar {
    padding: 18px;
  }

  .list-hero,
  .toolbar {
    flex-direction: column;
    align-items: flex-start;
  }

  .hero-search {
    width: 100%;
  }

  .product-grid.grid,
  .product-grid.compact {
    grid-template-columns: 1fr;
  }
}
</style>

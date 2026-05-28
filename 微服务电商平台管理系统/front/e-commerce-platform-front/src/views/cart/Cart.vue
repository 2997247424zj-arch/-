<template>
  <div class="cart-container">
    <div class="cart-header">
      <h2>我的购物车</h2>
      <span class="count">共 {{ cartStore.cartList.length }} 件商品</span>
    </div>

    <div class="cart-content glass-panel">
      <el-table :data="cartStore.cartList" style="width: 100%" v-if="cartStore.cartList.length > 0">
        <el-table-column width="50" align="center">
          <template #default="scope">
            <el-checkbox
              :model-value="scope.row.isSelected === 1"
              @change="(val: any) => handleSelect(scope.row.id, val)"
            />
          </template>
        </el-table-column>
        <el-table-column label="商品信息" min-width="400">
          <template #default="scope">
            <div class="product-info-cell">
              <img
                :src="scope.row.productCoverImg || 'https://picsum.photos/seed/product-default/80/80'"
                alt="Cover"
              />
              <div class="p-info">
                <span class="name" @click="router.push(`/product/detail/${scope.row.productId}`)">
                  {{ scope.row.productName || '商品名称' }}
                </span>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="单价" width="150" align="center">
          <template #default="scope">
            <span class="price">¥{{ Number(scope.row.price || 0).toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="数量" width="200" align="center">
          <template #default="scope">
            <el-input-number
              v-model="scope.row.quantity"
              :min="1"
              @change="(val: any) => handleChangeQuantity(scope.row.id, val)"
            />
          </template>
        </el-table-column>
        <el-table-column label="小计" width="150" align="center">
          <template #default="scope">
            <span class="subtotal"
              >¥{{ (scope.row.price * scope.row.quantity || 0).toFixed(2) }}</span
            >
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" align="center">
          <template #default="scope">
            <el-button type="danger" link @click="handleDelete(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty description="购物车空空如也，快去逛逛吧~" v-else>
        <el-button color="#4f46e5" round @click="router.push('/product/list')">去购物</el-button>
      </el-empty>
    </div>

    <div class="cart-footer glass-panel" v-if="cartStore.cartList.length > 0">
      <div class="left"></div>
      <div class="right">
        <div class="summary">
          已选商品 <span class="highlight">{{ cartStore.totalCount }}</span> 件 合计：<span
            class="total-price"
            >¥{{ cartStore.totalPrice.toFixed(2) }}</span
          >
        </div>
        <el-button
          color="#f97316"
          size="large"
          class="checkout-btn"
          :disabled="cartStore.totalCount === 0"
          @click="handleCheckout"
        >
          去结算
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useCartStore } from '@/store/cart'
import { ElMessageBox, ElMessage } from 'element-plus'

const router = useRouter()
const cartStore = useCartStore()

onMounted(() => {
  cartStore.fetchCartList()
})

const handleSelect = async (id: number, val: boolean | string | number) => {
  const isSelected = val ? 1 : 0
  await cartStore.toggleSelect(id, isSelected)
}

const handleChangeQuantity = async (id: number, val: number) => {
  if (val) {
    await cartStore.updateQuantity(id, val)
  }
}

const handleDelete = (id: number) => {
  ElMessageBox.confirm('确定要移出购物车吗？', '提示', { type: 'warning' })
    .then(async () => {
      await cartStore.removeItems([id])
      ElMessage.success('已删除')
    })
    .catch(() => {})
}

const handleCheckout = () => {
  router.push('/order/confirm')
}
</script>

<style scoped lang="scss">
.cart-header {
  margin-bottom: 24px;
  display: flex;
  align-items: baseline;
  gap: 16px;

  h2 {
    font-size: 24px;
    margin: 0;
    color: #1e293b;
  }

  .count {
    color: #64748b;
    font-size: 14px;
  }
}

.glass-panel {
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05);
  margin-bottom: 24px;
  overflow: hidden;
}

.product-info-cell {
  display: flex;
  align-items: center;
  gap: 16px;

  img {
    width: 80px;
    height: 80px;
    border-radius: 8px;
    object-fit: cover;
    border: 1px solid #f1f5f9;
  }

  .p-info {
    flex: 1;
    .name {
      color: #334155;
      cursor: pointer;
      line-height: 1.4;
      &:hover {
        color: #4f46e5;
        text-decoration: underline;
      }
    }
  }
}

.price {
  font-size: 16px;
  color: #334155;
}

.subtotal {
  font-size: 16px;
  font-weight: 600;
  color: #e11d48;
}

.cart-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;

  .right {
    display: flex;
    align-items: center;
    gap: 30px;

    .summary {
      font-size: 14px;
      color: #64748b;

      .highlight {
        color: #4f46e5;
        font-weight: 600;
        margin: 0 4px;
      }

      .total-price {
        color: #e11d48;
        font-size: 24px;
        font-weight: 800;
      }
    }

    .checkout-btn {
      width: 140px;
      border-radius: 20px;
      font-size: 16px;
      font-weight: 600;
    }
  }
}
</style>

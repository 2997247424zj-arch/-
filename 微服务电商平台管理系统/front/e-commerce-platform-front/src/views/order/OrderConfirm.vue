<template>
  <div class="order-confirm-container">
    <h2 class="page-title">确认订单</h2>

    <div class="section-card">
      <div class="section-title">选择收货地址</div>
      <div class="address-grid" v-if="addressList.length > 0">
        <div
          class="address-card"
          :class="{ active: selectedAddressId === addr.id }"
          v-for="addr in addressList"
          :key="addr.id"
          @click="selectedAddressId = addr.id"
        >
          <div class="card-inner">
            <div class="name-phone">
              <span class="name">{{ addr.receiver }}</span>
              <span class="phone">{{ addr.phone }}</span>
            </div>
            <div class="detail">
              {{ addr.province }}{{ addr.city }}{{ addr.district }} {{ addr.detailAddress }}
            </div>
          </div>
          <div class="active-badge" v-if="selectedAddressId === addr.id">
            <el-icon><Select /></el-icon>
          </div>
        </div>
      </div>
      <div v-else class="no-address">
        <el-button plain @click="router.push('/user/address')">去添加收货地址</el-button>
      </div>
    </div>

    <div class="section-card">
      <div class="section-title">商品明细</div>
      <el-table :data="selectedItems" style="width: 100%">
        <el-table-column label="商品信息">
          <template #default="scope">
            <div class="product-info">
              <img
                :src="scope.row.productCoverImg || 'https://picsum.photos/seed/product-default/60/60'"
                alt=""
                class="p-img"
              />
              <span>{{ scope.row.productName }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="单价" width="150" align="center">
          <template #default="scope">¥{{ Number(scope.row.price).toFixed(2) }}</template>
        </el-table-column>
        <el-table-column label="数量" width="150" align="center" prop="quantity"></el-table-column>
        <el-table-column label="小计" width="150" align="center">
          <template #default="scope"
            >¥{{ (scope.row.price * scope.row.quantity).toFixed(2) }}</template
          >
        </el-table-column>
      </el-table>
    </div>

    <div class="footer-bar">
      <div class="amount-wrap">
        应付总额：<span class="amount">¥{{ cartStore.totalPrice.toFixed(2) }}</span>
      </div>
      <el-button
        color="#e11d48"
        size="large"
        class="submit-btn"
        :loading="submitting"
        @click="submitOrder"
      >
        提交订单
      </el-button>
    </div>

    <!-- 模拟支付弹窗 -->
    <el-dialog
      v-model="payDialogVisible"
      title="模拟收银台"
      width="400px"
      center
      :close-on-click-modal="false"
      :show-close="false"
    >
      <div class="pay-content">
        <p>订单已生成，请支付</p>
        <div class="pay-amount">¥{{ cartStore.totalPrice.toFixed(2) }}</div>

        <div class="pay-methods">
          <el-radio-group v-model="payType">
            <el-radio :value="1">支付宝支付</el-radio>
            <el-radio :value="2">微信支付</el-radio>
          </el-radio-group>
        </div>
      </div>
      <template #footer>
        <el-button @click="router.push('/order/list')">稍后支付</el-button>
        <el-button type="primary" @click="handlePay" :loading="paying">确认支付</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { Select } from '@element-plus/icons-vue'
import { useCartStore } from '@/store/cart'
import { getAddressListAPI } from '@/api/modules/user'
import { createOrderAPI } from '@/api/modules/order'
import { createPayAPI } from '@/api/modules/pay'
import { ElMessage } from 'element-plus'

const router = useRouter()
const cartStore = useCartStore()

const addressList = ref<any[]>([])
const selectedAddressId = ref<number | null>(null)

const selectedItems = computed(() => cartStore.selectedItems)
const submitting = ref(false)

const payDialogVisible = ref(false)
const currentOrderNo = ref('')
const payType = ref(1)
const paying = ref(false)

onMounted(async () => {
  if (selectedItems.value.length === 0) {
    ElMessage.warning('没有选中的商品，请先选择')
    router.push('/cart')
    return
  }

  try {
    const res: any = await getAddressListAPI()
    if (res.data) {
      addressList.value = res.data
      const defaultAddr = addressList.value.find((a) => a.isDefault === 1)
      if (defaultAddr) {
        selectedAddressId.value = defaultAddr.id
      } else if (addressList.value.length > 0) {
        selectedAddressId.value = addressList.value[0].id
      }
    }
  } catch (err) {}
})

const submitOrder = async () => {
  if (!selectedAddressId.value) {
    return ElMessage.warning('请选择收货地址')
  }

  submitting.value = true
  try {
    const res: any = await createOrderAPI({
      addressId: selectedAddressId.value,
      totalAmount: cartStore.totalPrice,
      // Real backend might need cart item IDs, usually it deduces from selected items in DB
    })
    if (res.data) {
      currentOrderNo.value = res.data.orderNo || res.data
      ElMessage.success('订单提交成功')
      await cartStore.fetchCartList() // Refresh cart
      payDialogVisible.value = true
    }
  } catch (err) {
  } finally {
    submitting.value = false
  }
}

const handlePay = async () => {
  paying.value = true
  try {
    await createPayAPI({
      orderNo: currentOrderNo.value,
      payMethod: payType.value === 1 ? 'alipay' : 'wechat',
    })
    ElMessage.success('支付成功！')
    payDialogVisible.value = false
    router.push('/order/list')
  } catch (err) {
  } finally {
    paying.value = false
  }
}
</script>

<style scoped lang="scss">
.page-title {
  margin: 0 0 24px;
  font-size: 24px;
  color: #1e293b;
}

.section-card {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 24px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.02);

  .section-title {
    font-size: 18px;
    font-weight: 600;
    margin-bottom: 20px;
    padding-bottom: 12px;
    border-bottom: 1px solid #f1f5f9;
  }
}

.address-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;

  .address-card {
    border: 2px solid #e2e8f0;
    border-radius: 8px;
    padding: 16px;
    cursor: pointer;
    position: relative;
    transition: all 0.2s;

    &:hover {
      border-color: #cbd5e1;
    }

    &.active {
      border-color: #4f46e5;
      background: #eef2ff;
    }

    .name-phone {
      margin-bottom: 8px;
      font-weight: 600;
      color: #334155;

      .name {
        margin-right: 12px;
      }
    }

    .detail {
      font-size: 13px;
      color: #64748b;
      line-height: 1.5;
    }

    .active-badge {
      position: absolute;
      right: 0;
      bottom: 0;
      width: 24px;
      height: 24px;
      background: #4f46e5;
      color: white;
      border-radius: 8px 0 4px 0;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 14px;
    }
  }
}

.product-info {
  display: flex;
  align-items: center;
  gap: 12px;

  .p-img {
    width: 60px;
    height: 60px;
    border-radius: 6px;
    object-fit: cover;
  }
}

.footer-bar {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  background: #fff;
  padding: 16px 24px;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  margin-top: 30px;
  gap: 30px;

  .amount-wrap {
    font-size: 14px;
    color: #64748b;

    .amount {
      color: #e11d48;
      font-size: 28px;
      font-weight: 800;
    }
  }

  .submit-btn {
    width: 160px;
    border-radius: 8px;
    font-size: 16px;
  }
}

.pay-content {
  text-align: center;

  .pay-amount {
    font-size: 32px;
    color: #e11d48;
    font-weight: 700;
    margin: 10px 0 20px;
  }

  .pay-methods {
    display: flex;
    justify-content: center;
  }
}
</style>

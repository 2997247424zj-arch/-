<template>
  <div class="order-detail-container" v-loading="loading">
    <div class="breadcrumb">
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item :to="{ path: '/order/list' }">我的订单</el-breadcrumb-item>
        <el-breadcrumb-item>订单详情</el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <div class="main-content" v-if="order">
      <!-- 订单状态卡片 -->
      <div class="status-card glass-panel">
        <div class="status-info">
          <div class="status-icon" :class="getStatusClass(order.status)">
            <el-icon :size="48">
              <component :is="getStatusIcon(order.status)" />
            </el-icon>
          </div>
          <div class="status-text">
            <h2>{{ getStatusText(order.status) }}</h2>
            <p v-if="order.status === OrderStatus.PENDING">请在30分钟内完成支付</p>
            <p v-else-if="order.status === OrderStatus.PAID">商品正在准备发货中</p>
            <p v-else-if="order.status === OrderStatus.COMPLETED">订单已完成，感谢您的购买</p>
            <p v-else>订单已取消</p>
          </div>
        </div>
        <div class="status-actions" v-if="order.status === OrderStatus.PENDING">
          <el-button type="primary" size="large" @click="handlePay">立即支付</el-button>
          <el-button size="large" @click="handleCancel">取消订单</el-button>
        </div>
      </div>

      <!-- 收货信息 -->
      <div class="section-card glass-panel">
        <h3 class="section-title">收货信息</h3>
        <div class="info-grid">
          <div class="info-item">
            <span class="label">收货人：</span>
            <span class="value">{{ order.receiverName }}</span>
          </div>
          <div class="info-item">
            <span class="label">联系电话：</span>
            <span class="value">{{ order.receiverPhone }}</span>
          </div>
          <div class="info-item full">
            <span class="label">收货地址：</span>
            <span class="value">{{ order.receiverAddress }}</span>
          </div>
        </div>
      </div>

      <!-- 商品清单 -->
      <div class="section-card glass-panel">
        <h3 class="section-title">商品清单</h3>
        <el-table :data="order.orderItems" style="width: 100%">
          <el-table-column label="商品信息" min-width="300">
            <template #default="scope">
              <div class="product-info">
                <img
                  :src="scope.row.productImage || scope.row.productCoverImg || 'https://picsum.photos/seed/product-default/60/60'"
                  alt=""
                  class="p-img"
                />
                <div class="p-detail">
                  <span class="p-name">{{ scope.row.productName }}</span>
                </div>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="单价" width="120" align="center">
            <template #default="scope">¥{{ Number(scope.row.price).toFixed(2) }}</template>
          </el-table-column>
          <el-table-column label="数量" width="100" align="center" prop="quantity" />
          <el-table-column label="小计" width="120" align="center">
            <template #default="scope">
              ¥{{ (scope.row.price * scope.row.quantity).toFixed(2) }}
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 订单信息 -->
      <div class="section-card glass-panel">
        <h3 class="section-title">订单信息</h3>
        <div class="info-grid">
          <div class="info-item">
            <span class="label">订单编号：</span>
            <span class="value">{{ order.orderNo }}</span>
          </div>
          <div class="info-item">
            <span class="label">创建时间：</span>
            <span class="value">{{ order.createTime }}</span>
          </div>
          <div class="info-item" v-if="order.paymentTime">
            <span class="label">支付时间：</span>
            <span class="value">{{ order.paymentTime }}</span>
          </div>
          <div class="info-item">
            <span class="label">订单金额：</span>
            <span class="value price">¥{{ Number(order.totalAmount).toFixed(2) }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 支付弹窗 -->
    <el-dialog
      v-model="payDialogVisible"
      title="订单支付"
      width="400px"
      center
      :close-on-click-modal="false"
    >
      <div class="pay-content">
        <div class="pay-amount">
          <span class="label">支付金额</span>
          <span class="amount">¥{{ Number(order?.totalAmount || 0).toFixed(2) }}</span>
        </div>
        <div class="pay-methods">
          <el-radio-group v-model="payType">
            <el-radio :value="1">
              <div class="pay-option">
                <el-icon :size="20" color="#1677ff"><Wallet /></el-icon>
                <span>支付宝</span>
              </div>
            </el-radio>
            <el-radio :value="2">
              <div class="pay-option">
                <el-icon :size="20" color="#07c160"><ChatDotRound /></el-icon>
                <span>微信支付</span>
              </div>
            </el-radio>
          </el-radio-group>
        </div>
      </div>
      <template #footer>
        <el-button @click="payDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="paying" @click="confirmPay">确认支付</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Clock, CircleCheck, CircleClose, Check, Wallet, ChatDotRound } from '@element-plus/icons-vue'
import { getOrderDetailAPI, cancelOrderAPI } from '@/api/modules/order'
import { createPayAPI } from '@/api/modules/pay'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { Order } from '@/types'
import { OrderStatus } from '@/types'

const route = useRoute()
const router = useRouter()

const loading = ref(true)
const order = ref<Order | null>(null)
const payDialogVisible = ref(false)
const payType = ref(1)
const paying = ref(false)

const fetchOrderDetail = async () => {
  const orderNo = route.params.orderNo as string
  try {
    const res: any = await getOrderDetailAPI(orderNo)
    if (res.data) {
      // 后端返回 OrderDetailDTO { order: {...}, items: [...] }，需要手动合并
      const dto = res.data
      if (dto.order) {
        order.value = { ...dto.order, orderItems: dto.items || [] }
      } else {
        // 兼容直接返回 order 对象的情况
        order.value = res.data
      }
    }
  } catch (err) {
    ElMessage.error('获取订单详情失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchOrderDetail()
})

const getStatusIcon = (status: OrderStatus) => {
  const icons: Record<OrderStatus, any> = {
    [OrderStatus.PENDING]: Clock,
    [OrderStatus.PAID]: CircleCheck,
    [OrderStatus.CANCELLED]: CircleClose,
    [OrderStatus.COMPLETED]: Check,
  }
  return icons[status] || Clock
}

const getStatusClass = (status: OrderStatus) => {
  const classes: Record<OrderStatus, string> = {
    [OrderStatus.PENDING]: 'pending',
    [OrderStatus.PAID]: 'paid',
    [OrderStatus.CANCELLED]: 'cancelled',
    [OrderStatus.COMPLETED]: 'completed',
  }
  return classes[status] || ''
}

const getStatusText = (status: OrderStatus) => {
  const texts: Record<OrderStatus, string> = {
    [OrderStatus.PENDING]: '待支付',
    [OrderStatus.PAID]: '已支付',
    [OrderStatus.CANCELLED]: '已取消',
    [OrderStatus.COMPLETED]: '已完成',
  }
  return texts[status] || '未知状态'
}

const handlePay = () => {
  payDialogVisible.value = true
}

const confirmPay = async () => {
  if (!order.value) return
  paying.value = true
  try {
    await createPayAPI({
      orderNo: order.value.orderNo,
      payMethod: payType.value === 1 ? 'alipay' : 'wechat',
    })
    ElMessage.success('支付成功')
    payDialogVisible.value = false
    fetchOrderDetail()
  } catch (err) {
    // Error handled by interceptor
  } finally {
    paying.value = false
  }
}

const handleCancel = async () => {
  if (!order.value) return
  try {
    await ElMessageBox.confirm('确定要取消该订单吗？', '提示', { type: 'warning' })
    await cancelOrderAPI(order.value.orderNo)
    ElMessage.success('订单已取消')
    fetchOrderDetail()
  } catch (err) {
    // User cancelled or API error
  }
}
</script>

<style scoped lang="scss">
.breadcrumb {
  margin-bottom: 24px;
}

.glass-panel {
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05);
}

.status-card {
  padding: 32px;
  margin-bottom: 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;

  .status-info {
    display: flex;
    align-items: center;
    gap: 24px;

    .status-icon {
      width: 80px;
      height: 80px;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;

      &.pending {
        background: linear-gradient(135deg, #fef3c7, #fde68a);
        color: #d97706;
      }

      &.paid {
        background: linear-gradient(135deg, #dbeafe, #bfdbfe);
        color: #2563eb;
      }

      &.completed {
        background: linear-gradient(135deg, #d1fae5, #a7f3d0);
        color: #059669;
      }

      &.cancelled {
        background: linear-gradient(135deg, #fee2e2, #fecaca);
        color: #dc2626;
      }
    }

    .status-text {
      h2 {
        margin: 0 0 8px;
        font-size: 24px;
        color: #1e293b;
      }

      p {
        margin: 0;
        color: #64748b;
        font-size: 14px;
      }
    }
  }

  .status-actions {
    display: flex;
    gap: 12px;
  }
}

.section-card {
  padding: 24px;
  margin-bottom: 24px;

  .section-title {
    margin: 0 0 20px;
    font-size: 18px;
    font-weight: 600;
    color: #1e293b;
    padding-bottom: 12px;
    border-bottom: 1px solid #f1f5f9;
  }
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;

  .info-item {
    display: flex;
    font-size: 14px;

    &.full {
      grid-column: span 2;
    }

    .label {
      color: #64748b;
      width: 80px;
      flex-shrink: 0;
    }

    .value {
      color: #334155;

      &.price {
        color: #e11d48;
        font-size: 18px;
        font-weight: 600;
      }
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
    border-radius: 8px;
    object-fit: cover;
    border: 1px solid #f1f5f9;
  }

  .p-detail {
    .p-name {
      color: #334155;
      font-size: 14px;
    }
  }
}

.pay-content {
  .pay-amount {
    text-align: center;
    margin-bottom: 24px;

    .label {
      display: block;
      color: #64748b;
      font-size: 14px;
      margin-bottom: 8px;
    }

    .amount {
      font-size: 36px;
      font-weight: 700;
      color: #e11d48;
    }
  }

  .pay-methods {
    display: flex;
    justify-content: center;

    .pay-option {
      display: flex;
      align-items: center;
      gap: 8px;
    }
  }
}
</style>

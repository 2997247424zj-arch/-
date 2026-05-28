<template>
  <div class="order-list-page">
    <div class="page-header surface-card">
      <div>
        <span class="header-tag">MY ORDERS</span>
        <h1>订单中心</h1>
        <p>快速查看订单状态、商品明细与售后相关进度，管理你的每一次购物记录。</p>
      </div>
      <el-button round @click="router.push('/product/list')">继续购物</el-button>
    </div>

    <div class="page-layout">
      <aside class="sidebar surface-card">
        <div class="user-panel">
          <div class="avatar-shell">
            <el-icon :size="28"><User /></el-icon>
          </div>
          <div>
            <h3>个人中心</h3>
            <p>订单、地址与资料统一管理</p>
          </div>
        </div>

        <el-menu :default-active="activeMenu" class="user-menu" router>
          <el-menu-item index="/user">
            <el-icon><User /></el-icon>
            <span>个人信息</span>
          </el-menu-item>
          <el-menu-item index="/order/list">
            <el-icon><Tickets /></el-icon>
            <span>我的订单</span>
          </el-menu-item>
          <el-menu-item index="/user/address">
            <el-icon><Location /></el-icon>
            <span>地址管理</span>
          </el-menu-item>
        </el-menu>
      </aside>

      <main class="content-box">
        <div class="filter-panel surface-card">
          <div class="section-title-row">
            <div>
              <h2>订单列表</h2>
              <p>共 {{ orders.length }} 条记录</p>
            </div>
            <div class="status-tabs">
              <button class="tab-btn" :class="{ active: statusFilter === undefined }" @click="filterByStatus(undefined)">
                全部
              </button>
              <button class="tab-btn" :class="{ active: statusFilter === 0 }" @click="filterByStatus(0)">
                待支付
              </button>
              <button class="tab-btn" :class="{ active: statusFilter === 1 }" @click="filterByStatus(1)">
                已支付
              </button>
              <button class="tab-btn" :class="{ active: statusFilter === 3 }" @click="filterByStatus(3)">
                已完成
              </button>
              <button class="tab-btn" :class="{ active: statusFilter === 2 }" @click="filterByStatus(2)">
                已取消
              </button>
            </div>
          </div>
        </div>

        <div v-loading="loading" class="order-list-wrapper">
          <div v-if="orders.length > 0" class="order-card-list">
            <article v-for="order in orders" :key="order.orderNo" class="order-card surface-card">
              <div class="order-card-header">
                <div class="order-basic">
                  <span class="order-no">订单号：{{ order.orderNo }}</span>
                  <span class="order-time">下单时间：{{ order.createTime }}</span>
                </div>
                <el-tag :type="getStatusType(order.status)">{{ getStatusText(order.status) }}</el-tag>
              </div>

              <div class="order-items">
                <div
                  v-for="item in order.orderItems?.slice(0, 3) || []"
                  :key="`${order.orderNo}-${item.id}`"
                  class="item-row"
                >
                  <img
                    class="item-image"
                    :src="item.productImage || `https://picsum.photos/seed/${item.productId}/120/120`"
                    :alt="item.productName"
                  />
                  <div class="item-info">
                    <strong>{{ item.productName }}</strong>
                    <span>数量 × {{ item.quantity }}</span>
                  </div>
                  <div class="item-price">¥{{ Number(item.price).toFixed(2) }}</div>
                </div>

                <div v-if="!order.orderItems || order.orderItems.length === 0" class="empty-items">
                  当前订单暂无商品明细
                </div>
              </div>

              <div class="order-card-footer">
                <div class="summary-box">
                  <span>共 {{ countItems(order) }} 件商品</span>
                  <strong>实付：¥{{ Number(order.totalAmount).toFixed(2) }}</strong>
                </div>
                <div class="action-group">
                  <el-button round @click="viewDetail(order.orderNo)">查看详情</el-button>
                  <el-button
                    v-if="order.status === OrderStatus.PENDING"
                    round
                    type="danger"
                    plain
                    @click="handleCancel(order.orderNo)"
                  >
                    取消订单
                  </el-button>
                  <el-button
                    v-if="order.status === OrderStatus.PAID"
                    round
                    type="success"
                    @click="handleConfirmReceive(order.orderNo)"
                  >
                    确认收货
                  </el-button>
                </div>
              </div>
            </article>
          </div>

          <el-empty v-else description="暂无订单记录">
            <el-button type="primary" @click="router.push('/product/list')">去逛逛</el-button>
          </el-empty>
        </div>
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { User, Tickets, Location } from '@element-plus/icons-vue'
import { getOrderListAPI, cancelOrderAPI, confirmReceiveAPI } from '@/api/modules/order'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { Order } from '@/types'
import { OrderStatus } from '@/types'

const router = useRouter()
const activeMenu = '/order/list'
const orders = ref<Order[]>([])
const loading = ref(false)
const statusFilter = ref<number | undefined>(undefined)

const fetchOrders = async () => {
  loading.value = true
  try {
    const params: any = {}
    if (statusFilter.value !== undefined) {
      params.status = statusFilter.value
    }
    const res: any = await getOrderListAPI(params)
    if (res.data) {
      orders.value = res.data.list || res.data
    }
  } catch {
    orders.value = []
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchOrders()
})

const getStatusType = (status: OrderStatus) => {
  const map: Record<OrderStatus, string> = {
    [OrderStatus.PENDING]: 'warning',
    [OrderStatus.PAID]: 'primary',
    [OrderStatus.CANCELLED]: 'info',
    [OrderStatus.COMPLETED]: 'success',
  }
  return map[status] || 'info'
}

const getStatusText = (status: OrderStatus) => {
  const map: Record<OrderStatus, string> = {
    [OrderStatus.PENDING]: '待支付',
    [OrderStatus.PAID]: '已支付',
    [OrderStatus.CANCELLED]: '已取消',
    [OrderStatus.COMPLETED]: '已完成',
  }
  return map[status] || '未知'
}

const countItems = (order: Order) => {
  return order.orderItems?.reduce((sum, item) => sum + item.quantity, 0) || 0
}

const viewDetail = (orderNo: string) => {
  router.push(`/order/detail/${orderNo}`)
}

const handleCancel = async (orderNo: string) => {
  try {
    await ElMessageBox.confirm('确定要取消该订单吗？', '提示', { type: 'warning' })
    await cancelOrderAPI(orderNo)
    ElMessage.success('订单已取消')
    fetchOrders()
  } catch {
    // ignore
  }
}

const handleConfirmReceive = async (orderNo: string) => {
  try {
    await ElMessageBox.confirm('确认已收到商品吗？', '提示', { type: 'info' })
    await confirmReceiveAPI(orderNo)
    ElMessage.success('已确认收货')
    fetchOrders()
  } catch {
    // ignore
  }
}

const filterByStatus = (status: number | undefined) => {
  statusFilter.value = status
  fetchOrders()
}
</script>

<style scoped lang="scss">
.order-list-page {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.page-header {
  padding: 28px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 20px;

  h1 {
    font-size: 36px;
    margin: 10px 0 12px;
  }

  p {
    max-width: 620px;
    line-height: 1.8;
  }
}

.header-tag {
  display: inline-flex;
  align-items: center;
  padding: 8px 14px;
  border-radius: 999px;
  background: rgba(30, 64, 175, 0.1);
  color: #1e40af;
  font-size: 13px;
  font-weight: 800;
}

.page-layout {
  display: grid;
  grid-template-columns: 280px 1fr;
  gap: 22px;
}

.sidebar {
  padding: 22px;
  height: fit-content;
}

.user-panel {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 18px;
  border-radius: 22px;
  background: linear-gradient(135deg, rgba(30,64,175,0.1), rgba(59,130,246,0.08));
  margin-bottom: 18px;

  h3 {
    font-size: 18px;
    margin-bottom: 4px;
  }

  p {
    font-size: 13px;
    color: #64748b;
  }
}

.avatar-shell {
  width: 54px;
  height: 54px;
  border-radius: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #1e40af;
  color: #fff;
  flex-shrink: 0;
}

.user-menu {
  border-right: none;
  background: transparent;

  :deep(.el-menu-item) {
    height: 48px;
    border-radius: 14px;
    margin-bottom: 8px;
    font-weight: 700;
  }

  :deep(.el-menu-item.is-active) {
    background: rgba(30, 64, 175, 0.1);
    color: #1e40af;
  }
}

.content-box {
  display: flex;
  flex-direction: column;
  gap: 18px;
  min-width: 0;
}

.filter-panel {
  padding: 22px;
}

.section-title-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 18px;
  flex-wrap: wrap;

  h2 {
    font-size: 24px;
    margin-bottom: 6px;
  }

  p {
    color: #64748b;
    font-size: 14px;
  }
}

.status-tabs {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.tab-btn {
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
  }
}

.order-card-list {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.order-card {
  padding: 22px;
}

.order-card-header {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: center;
  padding-bottom: 16px;
  border-bottom: 1px solid #e2e8f0;
}

.order-basic {
  display: flex;
  flex-wrap: wrap;
  gap: 14px;
  align-items: center;
}

.order-no {
  font-size: 15px;
  font-weight: 800;
  color: #0f172a;
}

.order-time {
  font-size: 13px;
  color: #64748b;
}

.order-items {
  padding: 18px 0;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.item-row {
  display: grid;
  grid-template-columns: 72px 1fr auto;
  gap: 14px;
  align-items: center;
}

.item-image {
  width: 72px;
  height: 72px;
  border-radius: 16px;
  object-fit: cover;
  border: 1px solid #dbeafe;
}

.item-info {
  display: flex;
  flex-direction: column;
  gap: 6px;

  strong {
    color: #0f172a;
    font-size: 15px;
  }

  span {
    color: #64748b;
    font-size: 13px;
  }
}

.item-price {
  color: #e11d48;
  font-size: 18px;
  font-weight: 800;
}

.empty-items {
  color: #94a3b8;
  font-size: 14px;
}

.order-card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 18px;
  padding-top: 16px;
  border-top: 1px solid #e2e8f0;
  flex-wrap: wrap;
}

.summary-box {
  display: flex;
  align-items: center;
  gap: 14px;
  color: #64748b;
  font-size: 14px;

  strong {
    color: #0f172a;
    font-size: 18px;
  }
}

.action-group {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

@media (max-width: 1200px) {
  .page-layout {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .page-header,
  .filter-panel,
  .sidebar,
  .order-card {
    padding: 18px;
  }

  .item-row {
    grid-template-columns: 1fr;
  }

  .item-image {
    width: 100%;
    height: 180px;
  }
}
</style>

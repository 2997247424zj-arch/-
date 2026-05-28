<template>
  <div class="dashboard-container">
    <h2 class="page-title">数据概览</h2>

    <!-- 统计卡片 -->
    <div class="stats-grid">
      <div class="stat-card" v-for="stat in stats" :key="stat.title">
        <div class="stat-icon" :style="{ background: stat.color }">
          <el-icon :size="32">
            <component :is="stat.icon" />
          </el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ stat.value }}</div>
          <div class="stat-title">{{ stat.title }}</div>
          <div class="stat-trend" :class="{ up: stat.trend > 0, down: stat.trend < 0 }">
            <el-icon>
              <component :is="stat.trend > 0 ? 'CaretTop' : 'CaretBottom'" />
            </el-icon>
            <span>{{ Math.abs(stat.trend) }}%</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 图表区域 -->
    <div class="charts-grid">
      <div class="chart-card">
        <h3 class="chart-title">销售趋势</h3>
        <div ref="salesChartRef" class="chart-container"></div>
      </div>

      <div class="chart-card">
        <h3 class="chart-title">商品分类占比</h3>
        <div ref="categoryChartRef" class="chart-container"></div>
      </div>
    </div>

    <!-- 最近订单 -->
    <div class="recent-orders glass-panel">
      <h3 class="section-title">最近订单</h3>
      <el-table :data="recentOrders" style="width: 100%">
        <el-table-column prop="orderNo" label="订单编号" width="180" />
        <el-table-column label="商品" min-width="200">
          <template #default="scope">
            <div class="order-products">
              {{ scope.row.orderItems?.map((item: any) => item.productName).join(', ') }}
            </div>
          </template>
        </el-table-column>
        <el-table-column label="金额" width="120">
          <template #default="scope">¥{{ Number(scope.row.totalAmount).toFixed(2) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">{{
              getStatusText(scope.row.status)
            }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
      </el-table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick } from 'vue'
import {
  ShoppingCart,
  Goods,
  Money,
  User,
  CaretTop,
  CaretBottom,
} from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import { OrderStatus } from '@/types'
import { useUserStore } from '@/store/user'

const salesChartRef = ref<HTMLElement>()
const categoryChartRef = ref<HTMLElement>()
const userStore = useUserStore()

const stats = ref([
  {
    title: '我的订单',
    value: '0',
    trend: 0,
    icon: ShoppingCart,
    color: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
  },
  {
    title: '总消费',
    value: '¥0',
    trend: 0,
    icon: Money,
    color: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
  },
  {
    title: '购物车',
    value: '0',
    trend: 0,
    icon: Goods,
    color: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)',
  },
  {
    title: '收货地址',
    value: '0',
    trend: 0,
    icon: User,
    color: 'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)',
  },
])

const recentOrders = ref<any[]>([])

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

// 加载用户数据
const loadUserData = async () => {
  try {
    // 加载订单数据
    const orderRes = await fetch('/api/order/my-list', {
      credentials: 'include'
    })
    const orderData = await orderRes.json()

    if (orderData.code === 200 && orderData.data) {
      recentOrders.value = orderData.data.slice(0, 5)
      if (stats.value[0]) stats.value[0].value = orderData.data.length.toString()

      // 计算总消费
      const totalAmount = orderData.data.reduce((sum: number, order: any) => {
        return sum + (Number(order.totalAmount) || 0)
      }, 0)
      if (stats.value[1]) stats.value[1].value = `¥${totalAmount.toFixed(2)}`
    }

    // 加载购物车数据
    const cartRes = await fetch('/api/cart/list', {
      credentials: 'include'
    })
    const cartData = await cartRes.json()

    if (cartData.code === 200 && cartData.data) {
      if (stats.value[2]) stats.value[2].value = cartData.data.length.toString()
    }

    // 加载地址数据
    const addressRes = await fetch('/api/user/address/list', {
      credentials: 'include'
    })
    const addressData = await addressRes.json()

    if (addressData.code === 200 && addressData.data) {
      if (stats.value[3]) stats.value[3].value = addressData.data.length.toString()
    }
  } catch (error) {
    console.error('加载用户数据失败:', error)
  }
}

const initSalesChart = () => {
  if (!salesChartRef.value) return

  const chart = echarts.init(salesChartRef.value)
  const option = {
    tooltip: {
      trigger: 'axis',
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true,
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: ['1月', '2月', '3月', '4月', '5月', '6月', '7月'],
    },
    yAxis: {
      type: 'value',
    },
    series: [
      {
        name: '消费金额',
        type: 'line',
        smooth: true,
        data: [0, 0, 0, 0, 0, 0, 0], // 实际应该从后端获取
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(79, 70, 229, 0.3)' },
            { offset: 1, color: 'rgba(79, 70, 229, 0.05)' },
          ]),
        },
        lineStyle: {
          color: '#4f46e5',
          width: 3,
        },
        itemStyle: {
          color: '#4f46e5',
        },
      },
    ],
  }

  chart.setOption(option)

  window.addEventListener('resize', () => {
    chart.resize()
  })
}

const initCategoryChart = () => {
  if (!categoryChartRef.value) return

  const chart = echarts.init(categoryChartRef.value)
  const option = {
    tooltip: {
      trigger: 'item',
    },
    legend: {
      bottom: '5%',
      left: 'center',
    },
    series: [
      {
        name: '购买分类',
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#fff',
          borderWidth: 2,
        },
        label: {
          show: false,
          position: 'center',
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 20,
            fontWeight: 'bold',
          },
        },
        labelLine: {
          show: false,
        },
        data: [], // 实际应该从后端获取用户的购买分类统计
      },
    ],
  }

  chart.setOption(option)

  window.addEventListener('resize', () => {
    chart.resize()
  })
}

onMounted(() => {
  loadUserData()
  nextTick(() => {
    initSalesChart()
    initCategoryChart()
  })
})
</script>

<style scoped lang="scss">
.dashboard-container {
  .page-title {
    margin: 0 0 24px;
    font-size: 24px;
    font-weight: 700;
    color: #1e293b;
  }
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 24px;
  margin-bottom: 32px;

  .stat-card {
    background: #fff;
    border-radius: 16px;
    padding: 24px;
    display: flex;
    gap: 20px;
    box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05);
    transition: all 0.3s;

    &:hover {
      transform: translateY(-4px);
      box-shadow: 0 12px 24px -4px rgba(0, 0, 0, 0.1);
    }

    .stat-icon {
      width: 64px;
      height: 64px;
      border-radius: 12px;
      display: flex;
      align-items: center;
      justify-content: center;
      color: #fff;
      flex-shrink: 0;
    }

    .stat-content {
      flex: 1;

      .stat-value {
        font-size: 28px;
        font-weight: 700;
        color: #1e293b;
        margin-bottom: 4px;
      }

      .stat-title {
        font-size: 14px;
        color: #64748b;
        margin-bottom: 8px;
      }

      .stat-trend {
        display: flex;
        align-items: center;
        gap: 4px;
        font-size: 13px;
        font-weight: 600;

        &.up {
          color: #10b981;
        }

        &.down {
          color: #ef4444;
        }
      }
    }
  }
}

.charts-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 24px;
  margin-bottom: 32px;

  .chart-card {
    background: #fff;
    border-radius: 16px;
    padding: 24px;
    box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05);

    .chart-title {
      margin: 0 0 20px;
      font-size: 18px;
      font-weight: 600;
      color: #1e293b;
    }

    .chart-container {
      height: 300px;
    }
  }
}

.recent-orders {
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05);

  .section-title {
    margin: 0 0 20px;
    font-size: 18px;
    font-weight: 600;
    color: #1e293b;
  }

  .order-products {
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }
}

@media (max-width: 1200px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .charts-grid {
    grid-template-columns: 1fr;
  }
}
</style>

<template>
  <div class="points-coupons-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1>积分与优惠券</h1>
      <p class="subtitle">管理您的积分和优惠券，享受更多优惠</p>
    </div>

    <!-- 积分卡片 -->
    <div class="points-card-section">
      <div class="points-card">
        <div class="points-header">
          <div class="points-icon">⭐</div>
          <div class="points-info">
            <div class="points-label">当前积分</div>
            <div class="points-value">{{ userPoints.currentPoints }}</div>
          </div>
        </div>
        <div class="points-details">
          <div class="detail-item">
            <span class="detail-label">本月获得</span>
            <span class="detail-value">+{{ userPoints.monthlyPoints }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">待确认</span>
            <span class="detail-value">{{ userPoints.pendingPoints }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">会员等级</span>
            <span class="detail-value member-level">{{ userPoints.memberLevel }}</span>
          </div>
        </div>
        <div class="points-progress">
          <div class="progress-label">
            <span>升级进度</span>
            <span class="progress-percent">{{ userPoints.upgradeProgress }}%</span>
          </div>
          <div class="progress-bar">
            <div class="progress-fill" :style="{ width: userPoints.upgradeProgress + '%' }"></div>
          </div>
          <div class="progress-text">还需 {{ userPoints.pointsToNextLevel }} 积分升级到 {{ userPoints.nextLevel }}</div>
        </div>
      </div>

      <!-- 积分兑换说明 -->
      <div class="points-info-card">
        <div class="info-title">💡 积分说明</div>
        <ul class="info-list">
          <li>购票每消费 ¥1 获得 10 积分</li>
          <li>积分永不过期，可随时兑换</li>
          <li>积分兑换现金规则：20 积分可抵扣 ¥1</li>
        </ul>
      </div>
    </div>

    <!-- 标签页切换 -->
    <div class="tabs-container">
      <div class="tabs">
        <button 
          v-for="tab in tabs" 
          :key="tab.id"
          :class="['tab-btn', { active: activeTab === tab.id }]"
          @click="activeTab = tab.id"
        >
          <span class="tab-icon">{{ tab.icon }}</span>
          <span class="tab-label">{{ tab.label }}</span>
          <span v-if="tab.badge" class="tab-badge">{{ tab.badge }}</span>
        </button>
      </div>
    </div>

    <!-- 优惠券列表 -->
    <div v-if="activeTab === 'coupons'" class="coupons-section">
      <div class="filter-bar">
        <button 
          v-for="filter in couponFilters" 
          :key="filter"
          :class="['filter-btn', { active: couponFilter === filter }]"
          @click="couponFilter = filter"
        >
          {{ filter }}
        </button>
      </div>

      <div v-if="loading" class="loading-state">
        <div class="loading-text">加载中...</div>
      </div>
      <div v-else-if="filteredCoupons.length > 0" class="coupons-list">
        <div 
          v-for="coupon in filteredCoupons" 
          :key="coupon.id"
          :class="['coupon-card', coupon.status]"
        >
          <div class="coupon-left">
            <div class="coupon-amount">
              <span v-if="!coupon.discountMeta?.isPercentage" class="currency">¥</span>
              <span class="amount">{{ coupon.discountMeta?.displayValue ?? coupon.amount }}</span>
              <span v-if="coupon.discountMeta?.isPercentage" class="percentage-symbol">折</span>
            </div>
            <div class="coupon-divider"></div>
          </div>
          <div class="coupon-middle">
            <div class="coupon-name">{{ coupon.name }}</div>
            <div class="coupon-desc">{{ coupon.description }}</div>
            <div class="coupon-condition">满 ¥{{ coupon.minAmount }} 可用</div>
          </div>
          <div class="coupon-right">
            <div class="coupon-status">{{ coupon.statusText }}</div>
            <button 
              v-if="coupon.status === 'available'"
              class="use-btn"
              @click="handleCouponAction(coupon)"
            >
              {{ isSelectVoucherMode ? '选择使用' : '立即使用' }}
            </button>
            <button 
              v-else-if="coupon.status === 'unused'"
              class="receive-btn"
              @click="receiveCoupon(coupon)"
            >
              领取
            </button>
            <span v-else-if="coupon.status === 'used'" class="used-text">已使用</span>
            <span v-else-if="coupon.status === 'expired'" class="expired-text">已过期</span>
            <span v-else-if="coupon.status === 'unavailable'" class="unavailable-text">不可用</span>
          </div>
        </div>
      </div>
      <div v-else class="empty-state">
        <div class="empty-icon">🎫</div>
        <div class="empty-text">暂无{{ couponFilter }}优惠券</div>
      </div>
    </div>

    <!-- 积分兑换 -->
    <div v-if="activeTab === 'exchange'" class="exchange-section">
      <div class="exchange-grid">
        <div 
          v-for="item in exchangeItems" 
          :key="item.id"
          :class="['exchange-card', { disabled: userPoints.currentPoints < item.requiredPoints }]"
        >
          <div class="exchange-icon">{{ item.icon }}</div>
          <div class="exchange-name">{{ item.name }}</div>
          <div class="exchange-desc">{{ item.description }}</div>
          <div class="exchange-points">
            <span class="points-label">需要积分</span>
            <span class="points-amount">{{ item.requiredPoints }}</span>
          </div>
          <button 
            class="exchange-btn"
            :disabled="userPoints.currentPoints < item.requiredPoints"
            @click="exchangePoints(item)"
          >
            {{ userPoints.currentPoints >= item.requiredPoints ? '兑换' : '积分不足' }}
          </button>
        </div>
      </div>
    </div>

    <!-- 积分记录 -->
    <div v-if="activeTab === 'history'" class="history-section">
      <div v-if="loading" class="loading-state">
        <div class="loading-text">加载中...</div>
      </div>
      <div v-else-if="pointsHistory.length > 0" class="history-list">
        <div 
          v-for="record in pointsHistory" 
          :key="record.id"
          class="history-item"
        >
          <div class="history-icon" :class="record.type">{{ record.icon }}</div>
          <div class="history-content">
            <div class="history-title">{{ record.title }}</div>
            <div class="history-desc">{{ record.description || '' }}</div>
            <div class="history-time">{{ record.date }}</div>
          </div>
          <div class="history-points" :class="record.type">
            {{ record.type === 'earn' ? '+' : '-' }}{{ record.points }}
          </div>
        </div>
      </div>
      <div v-else class="empty-state">
        <div class="empty-icon">📊</div>
        <div class="empty-text">暂无积分记录</div>
      </div>
    </div>

    <!-- 成功提示 -->
    <div v-if="showSuccessMessage" class="success-message">
      <div class="message-icon">✓</div>
      <div class="message-text">{{ successMessage }}</div>
      <button class="close-btn" @click="showSuccessMessage = false">×</button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { pointsApi, couponApi } from '../services/api'
import { formatDateTime } from '../utils/dateFormat'

const route = useRoute()
const router = useRouter()

// 是否从确认订单页进入的“选择代金券”模式
const isSelectVoucherMode = computed(() => route.query.mode === 'selectVoucher')

// 预订页传入的未打折基础总价（用于校验优惠券使用条件 & 预估折扣金额）
const bookingBasePrice = computed(() => {
  const priceStr = sessionStorage.getItem('booking_price_before_voucher')
  const price = priceStr ? parseFloat(priceStr) : 0
  return Number.isFinite(price) ? price : 0
})

// 用户积分信息（默认展示 10000 积分，若后台有数据则以后台为准）
const userPoints = ref({
  currentPoints: 10000,
  monthlyPoints: 0,
  pendingPoints: 0,
  memberLevel: '普通会员',
  upgradeProgress: 0,
  pointsToNextLevel: 1000,
  nextLevel: '白银会员'
})

// 加载状态
const loading = ref(false)

// 标签页配置
const tabs = ref([
  { id: 'coupons', label: '优惠券', icon: '🎫', badge: null },
  { id: 'exchange', label: '积分兑换', icon: '🎁', badge: null },
  { id: 'history', label: '积分记录', icon: '📊', badge: null }
])

// 当前活跃标签页
const activeTab = ref('coupons')

// 优惠券过滤器
const couponFilters = ['全部', '可用', '未领取', '已过期']
const couponFilter = ref('全部')

// 优惠券数据
const coupons = ref<any[]>([])

// 过滤后的优惠券
const filteredCoupons = computed(() => {
  if (couponFilter.value === '全部') {
    return coupons.value
  } else if (couponFilter.value === '可用') {
    return coupons.value.filter(c => c.status === 'available')
  } else if (couponFilter.value === '未领取') {
    return coupons.value.filter(c => c.status === 'unused')
  } else if (couponFilter.value === '已过期') {
    return coupons.value.filter(c => c.status === 'expired' || c.status === 'used')
  }
  return coupons.value
})

// 积分兑换项目（仅保留代金券，按 20 积分 = 1 元计算所需积分）
const exchangeItems = ref([
  {
    id: 1,
    name: '¥50 代金券',
    description: '可用于任何航班购票',
    icon: '💳',
    requiredPoints: 50 * 20, // 20 积分 = 1 元 → 1000 积分
    amount: 50,
    type: 'voucher'
  },
  {
    id: 2,
    name: '¥100 代金券',
    description: '可用于任何航班购票',
    icon: '💳',
    requiredPoints: 100 * 20, // 2000 积分
    amount: 100,
    type: 'voucher'
  },
  {
    id: 3,
    name: '¥200 代金券',
    description: '可用于任何航班购票',
    icon: '💳',
    requiredPoints: 200 * 20, // 4000 积分
    amount: 200,
    type: 'voucher'
  },
  {
    id: 4,
    name: '¥300 代金券',
    description: '可用于任何航班购票',
    icon: '💳',
    requiredPoints: 300 * 20, // 6000 积分
    amount: 300,
    type: 'voucher'
  }
])

// 积分记录
const pointsHistory = ref<any[]>([])

// 成功提示
const showSuccessMessage = ref(false)
const successMessage = ref('')

// 加载积分信息
const loadPointsInfo = async () => {
  try {
    loading.value = true
    const data = await pointsApi.getPointsInfo()
    if (data) {
      userPoints.value = {
        // 若后端未返回当前积分，则默认展示 10000 积分
        currentPoints: data.currentPoints ?? 10000,
        monthlyPoints: data.monthlyPoints || 0,
        pendingPoints: data.pendingPoints || 0,
        memberLevel: data.memberLevel || '普通会员',
        upgradeProgress: data.upgradeProgress || 0,
        pointsToNextLevel: data.pointsToNextLevel || 1000,
        nextLevel: data.nextLevel || '白银会员'
      }
    }
  } catch (error: any) {
    console.error('加载积分信息失败:', error)
  } finally {
    loading.value = false
  }
}

// 计算优惠券状态
const calculateCouponStatus = (coupon: any) => {
  // 如果优惠券已经使用，优先返回已使用状态（不管日期如何）
  const originalStatus = coupon.status || ''
  if (originalStatus === 'used' || originalStatus === 'USED' || originalStatus === '已使用') {
    return {
      status: 'used',
      statusText: '已使用'
    }
  }
  
  // 根据日期判断状态 - 尝试多种可能的字段名
  const now = new Date()
  // 尝试多种可能的字段名格式
  const validFrom = coupon.valid_from || coupon.validFrom || coupon.validFromDate || coupon.valid_from_date || 
                    coupon.startDate || coupon.start_date || coupon.start || coupon.issueDate || coupon.issue_date
  const validTo = coupon.valid_to || coupon.validTo || coupon.validToDate || coupon.valid_to_date || 
                  coupon.expiryDate || coupon.expiry_date || coupon.endDate || coupon.end_date || coupon.expireDate || coupon.expire_date
  
  // 调试：打印原始数据（只在开发环境）
  if (process.env.NODE_ENV === 'development') {
    console.log('优惠券原始数据:', {
      id: coupon.id,
      name: coupon.name,
      valid_from: coupon.valid_from,
      validFrom: coupon.validFrom,
      valid_to: coupon.valid_to,
      validTo: coupon.validTo,
      expiryDate: coupon.expiryDate,
      status: coupon.status,
      allKeys: Object.keys(coupon)
    })
  }
  
  // 如果没有结束日期，返回不可用
  if (!validTo) {
    console.warn('优惠券缺少结束日期信息:', coupon.id, coupon.name, '所有字段:', Object.keys(coupon))
    return {
      status: 'unavailable',
      statusText: '不可用'
    }
  }
  
  // 如果没有开始日期，返回不可用（后端应该总是返回开始日期）
  if (!validFrom) {
    console.warn('优惠券缺少开始日期信息:', coupon.id, coupon.name, '所有字段:', Object.keys(coupon))
    return {
      status: 'unavailable',
      statusText: '不可用'
    }
  }
  
  const actualValidFrom = validFrom
  
  // 解析日期，只比较日期部分（年-月-日），忽略时间
  const parseDate = (dateStr: any): Date => {
    if (!dateStr) {
      return new Date(0) // 无效日期
    }
    
    let date: Date
    
    if (typeof dateStr === 'string') {
      // 处理 'YYYY-MM-DD HH:mm:ss' 格式
      if (dateStr.includes(' ')) {
        const datePart = dateStr.split(' ')[0]
        date = new Date(datePart + 'T00:00:00')
      } else if (dateStr.includes('T')) {
        // ISO 格式
        date = new Date(dateStr)
      } else {
        // 纯日期格式 YYYY-MM-DD
        date = new Date(dateStr + 'T00:00:00')
      }
      
      // 如果解析失败，尝试其他方式
      if (isNaN(date.getTime())) {
        console.warn('日期解析失败:', dateStr)
        return new Date(0)
      }
    } else if (dateStr instanceof Date) {
      date = dateStr
    } else {
      date = new Date(dateStr)
      if (isNaN(date.getTime())) {
        console.warn('日期解析失败:', dateStr)
        return new Date(0)
      }
    }
    
    // 设置为当天的开始时间（00:00:00）进行比较
    return new Date(date.getFullYear(), date.getMonth(), date.getDate())
  }
  
  const fromDate = parseDate(actualValidFrom)
  const toDate = parseDate(validTo)
  const today = new Date(now.getFullYear(), now.getMonth(), now.getDate())
  
  // 检查日期是否有效
  if (fromDate.getTime() === 0 || toDate.getTime() === 0) {
    console.warn('优惠券日期无效:', coupon.id, { validFrom, validTo })
    return {
      status: 'unavailable',
      statusText: '不可用'
    }
  }
  
  // 调试信息（只在开发环境）
  if (process.env.NODE_ENV === 'development') {
    console.log('优惠券日期判断:', {
      couponId: coupon.id,
      couponName: coupon.name,
      validFrom: actualValidFrom,
      validTo: validTo,
      fromDate: fromDate.toISOString().split('T')[0],
      toDate: toDate.toISOString().split('T')[0],
      today: today.toISOString().split('T')[0],
      comparison: {
        todayVsFrom: today >= fromDate,
        todayVsTo: today <= toDate,
        todayTime: today.getTime(),
        fromTime: fromDate.getTime(),
        toTime: toDate.getTime()
      }
    })
  }
  
  // 判断日期范围
  if (today < fromDate) {
    // 未来才能用的优惠券
    return {
      status: 'unavailable',
      statusText: '不可用'
    }
  } else if (today >= fromDate && today <= toDate) {
    // 在有效期内
    return {
      status: 'available',
      statusText: '可用'
    }
  } else {
    // 已过期
    return {
      status: 'expired',
      statusText: '已过期'
    }
  }
}

// 判断是否是百分比折扣
const isPercentageDiscount = (coupon: any) => {
  const discountType = coupon.discount_type || coupon.discountType
  // 支持多种可能的折扣类型值
  return discountType === 'PERCENTAGE' || 
         discountType === 'percentage' || 
         discountType === '百分比' ||
         discountType === '百分比折扣' ||
         (typeof discountType === 'string' && discountType.toLowerCase().includes('percentage')) ||
         (typeof discountType === 'string' && discountType.includes('百分比'))
}

// 格式化优惠券金额显示
const formatCouponAmount = (coupon: any) => {
  const discountType = coupon.discount_type || coupon.discountType
  let discountValue = coupon.discount_value || coupon.discountValue || coupon.amount
  
  // 如果是百分比折扣，直接显示折扣系数加"折"字
  // 例如：0.9 → 0.9折, 0.85 → 0.85折
  if (isPercentageDiscount(coupon)) {
    // 确保是数字类型
    if (typeof discountValue === 'string') {
      discountValue = parseFloat(discountValue)
    } else if (typeof discountValue === 'object' && discountValue !== null) {
      // 处理BigDecimal等对象类型
      discountValue = parseFloat(discountValue.toString())
    }
    
    // 如果 discountValue 是小数（小于1），直接显示折扣系数加"折"
    if (discountValue < 1 && discountValue > 0) {
      // 直接显示折扣系数：0.9 → "0.9折", 0.85 → "0.85折"
      // 保留小数点后最多2位
      const formatted = discountValue.toFixed(2).replace(/\.?0+$/, '')
      return formatted + '折'
    } else if (discountValue >= 1) {
      // 如果已经是百分比值（如90），转换为折扣系数（90 → 0.9折）
      const zhe = discountValue / 100
      const formatted = zhe.toFixed(2).replace(/\.?0+$/, '')
      return formatted + '折'
    }
    // 如果值异常，返回原值加"折"
    return discountValue.toString() + '折'
  }
  
  // 其他情况显示金额
  return discountValue
}

// 归一化优惠券优惠信息，便于展示和联动费用
const normalizeDiscount = (coupon: any) => {
  const isPercentage = isPercentageDiscount(coupon)
  let value = coupon.discount_value || coupon.discountValue || coupon.amount || 0

  if (typeof value === 'string') {
    value = parseFloat(value)
  } else if (typeof value === 'object' && value !== null) {
    value = parseFloat(value.toString())
  }

  if (!Number.isFinite(value) || value < 0) {
    value = 0
  }

  if (isPercentage) {
    let factor = value
    // 兼容 9 / 90 / 0.9 等写法
    if (factor > 1.5 && factor <= 10) {
      factor = factor / 10
    } else if (factor > 10) {
      factor = factor / 100
    }
    // 限制折扣系数在 0-1 之间
    factor = Math.min(Math.max(factor, 0), 1)
    const displayValue = factor.toFixed(2).replace(/\.?0+$/, '')
    return {
      isPercentage: true,
      value: factor,
      displayValue
    }
  }

  // 固定金额优惠
  const displayValue =
    value % 1 === 0 ? value.toFixed(0) : value.toFixed(2).replace(/\.?0+$/, '')
  return {
    isPercentage: false,
    value,
    displayValue
  }
}

// 加载优惠券列表
const loadCoupons = async () => {
  try {
    loading.value = true
    const filter = couponFilter.value === '全部' ? '全部' : couponFilter.value
    const data = await couponApi.getCoupons({ filter })
    
    if (Array.isArray(data)) {
      // 打印第一个优惠券的完整数据结构用于调试
      if (data.length > 0 && process.env.NODE_ENV === 'development') {
        console.log('API返回的优惠券数据结构示例:', JSON.stringify(data[0], null, 2))
      }
      
      // 处理每个优惠券，计算状态和格式化显示
      coupons.value = data.map((coupon: any) => {
        const statusInfo = calculateCouponStatus(coupon)
        const discountMeta = normalizeDiscount(coupon)
        const formattedAmount = formatCouponAmount(coupon)

        // 调试信息
        if (process.env.NODE_ENV === 'development') {
          console.log('处理优惠券:', {
            id: coupon.id,
            name: coupon.name,
            discount_type: coupon.discount_type || coupon.discountType,
            discount_value: coupon.discount_value || coupon.discountValue,
            isPercentage: discountMeta.isPercentage,
            displayValue: discountMeta.displayValue
          })
        }
        
        return {
          ...coupon,
          status: statusInfo.status,
          statusText: statusInfo.statusText,
          amount: formattedAmount,
          discountMeta,
          minAmount: coupon.min_spend || coupon.minSpend || coupon.min_amount || coupon.minAmount || 0
        }
      })
      
      // 更新标签页徽章
      const availableCount = coupons.value.filter((c: any) => c.status === 'available').length
      tabs.value[0].badge = availableCount > 0 ? availableCount : null
    } else {
      console.warn('API返回的数据不是数组:', data)
    }
  } catch (error: any) {
    console.error('加载优惠券列表失败:', error)
    coupons.value = []
  } finally {
    loading.value = false
  }
}

// 加载积分记录
const loadPointsHistory = async () => {
  try {
    loading.value = true
    const data = await pointsApi.getPointsHistory({ page: 1, size: 50 })
    if (data && data.records) {
      // 格式化时间，将UTC时间转换为北京时间
      pointsHistory.value = data.records.map((record: any) => ({
        ...record,
        date: formatDateTime(record.created_at || record.date || ''),
        description: record.remark || record.description || ''
      }))
    }
  } catch (error: any) {
    console.error('加载积分记录失败:', error)
    pointsHistory.value = []
  } finally {
    loading.value = false
  }
}

// 监听标签页切换
watch(activeTab, (newTab) => {
  if (newTab === 'coupons') {
    loadCoupons()
  } else if (newTab === 'history') {
    loadPointsHistory()
  }
})

// 监听优惠券筛选器变化
watch(couponFilter, () => {
  if (activeTab.value === 'coupons') {
    loadCoupons()
  }
})

// 根据入口模式决定执行“立即使用”还是“选择返回预订页”
const handleCouponAction = (coupon: any) => {
  if (isSelectVoucherMode.value) {
    applyVoucherToBooking(coupon)
  } else {
    useCoupon(coupon)
  }
}

// 使用优惠券
const useCoupon = async (coupon: any) => {
  try {
    loading.value = true
    await couponApi.useCoupon(coupon.id)
    successMessage.value = `成功使用优惠券：${coupon.name}，立即前往购票享受优惠！`
    showSuccessMessage.value = true
    // 重新加载优惠券列表
    await loadCoupons()
    setTimeout(() => {
      showSuccessMessage.value = false
    }, 3000)
  } catch (error: any) {
    successMessage.value = error.message || '使用优惠券失败'
    showSuccessMessage.value = true
    setTimeout(() => {
      showSuccessMessage.value = false
    }, 3000)
  } finally {
    loading.value = false
  }
}

// 领取优惠券
const receiveCoupon = async (coupon: any) => {
  try {
    loading.value = true
    await couponApi.receiveCoupon(coupon.id)
    successMessage.value = `成功领取优惠券：${coupon.name}`
    showSuccessMessage.value = true
    // 重新加载优惠券列表
    await loadCoupons()
    setTimeout(() => {
      showSuccessMessage.value = false
    }, 3000)
  } catch (error: any) {
    successMessage.value = error.message || '领取优惠券失败'
    showSuccessMessage.value = true
    setTimeout(() => {
      showSuccessMessage.value = false
    }, 3000)
  } finally {
    loading.value = false
  }
}

// 计算当前已应用优惠后的价格（用于校验新优惠券门槛）
const calculateCurrentPriceAfterCoupons = (existingCoupons: any[], basePrice: number): number => {
  if (existingCoupons.length === 0) return basePrice

  // 按类型排序：先现金券，再折扣券
  const sortedCoupons = [...existingCoupons].sort((a, b) => {
    if (a.type === 'cash' && b.type === 'percentage') return -1
    if (a.type === 'percentage' && b.type === 'cash') return 1
    return 0
  })

  let currentPrice = basePrice
  sortedCoupons.forEach((coupon) => {
    // 检查满减门槛
    if (coupon.minAmount && currentPrice < coupon.minAmount) {
      return // 跳过不满足门槛的优惠券
    }

    if (coupon.type === 'percentage') {
      // 折扣券
      const factor = coupon.discountValue && Number.isFinite(coupon.discountValue)
        ? Math.min(Math.max(coupon.discountValue, 0), 1)
        : 1
      currentPrice = currentPrice * factor
    } else {
      // 现金券
      const cashOff = coupon.discountValue || 0
      currentPrice = Math.max(currentPrice - cashOff, 0)
    }
  })

  return Math.max(currentPrice, 0)
}

// 将优惠券信息添加到已选列表（支持叠加），并更新状态为USED
const applyVoucherToBooking = async (item: any) => {
  const flightId =
    (route.query.flightId as string | undefined) ||
    sessionStorage.getItem('booking_flight_id_for_points') ||
    ''

  const discountMeta = normalizeDiscount(item)
  const minAmount = item.minAmount || item.min_spend || item.minSpend || 0
  const basePrice = bookingBasePrice.value

  // 获取已保存的优惠券列表
  const existingCouponsStr = sessionStorage.getItem('booking_selected_coupons')
  let existingCoupons: any[] = []
  
  if (existingCouponsStr) {
    try {
      existingCoupons = JSON.parse(existingCouponsStr)
      // 过滤出属于当前航班的优惠券
      existingCoupons = existingCoupons.filter((c: any) => !c.flightId || c.flightId === flightId)
    } catch (e) {
      console.warn('解析已选优惠券失败:', e)
      existingCoupons = []
    }
  }

  // 计算当前已应用优惠后的价格
  const currentPrice = calculateCurrentPriceAfterCoupons(existingCoupons, basePrice)

  // 使用当前价格校验新优惠券的门槛
  if (minAmount && currentPrice < minAmount) {
    successMessage.value = `需满 ¥${minAmount} 才可使用该优惠券（当前价格：¥${currentPrice.toFixed(2)}）`
    showSuccessMessage.value = true
    setTimeout(() => {
      showSuccessMessage.value = false
    }, 2200)
    return
  }

  try {
    loading.value = true

    // 检查是否已选择过该优惠券
    if (existingCoupons.some((c: any) => c.id === item.id)) {
      successMessage.value = '该优惠券已选择'
      showSuccessMessage.value = true
      setTimeout(() => {
        showSuccessMessage.value = false
      }, 2000)
      loading.value = false
      return
    }

    // 调用API更新优惠券状态为USED
    try {
      await couponApi.useCoupon(item.id)
    } catch (apiError: any) {
      // 如果API调用失败，仍然允许选择（可能是模拟环境）
      console.warn('更新优惠券状态失败，继续选择:', apiError)
    }

    // 添加新优惠券到列表
    const newCoupon = {
      id: item.id,
      name: item.name,
      type: discountMeta.isPercentage ? 'percentage' : 'cash',
      discountValue: discountMeta.value,
      displayValue: discountMeta.displayValue,
      minAmount,
      requiredPoints: item.requiredPoints,
      flightId,
      appliedAmount: 0,
      appliedPrice: 0
    }

    existingCoupons.push(newCoupon)
    sessionStorage.setItem('booking_selected_coupons', JSON.stringify(existingCoupons))

    // 重新加载优惠券列表以更新状态
    await loadCoupons()

    successMessage.value = `已选择优惠券：${item.name}`
    showSuccessMessage.value = true
    setTimeout(() => {
      showSuccessMessage.value = false
    }, 2000)

    // 找到返回路径，默认为确认预订页面
    const returnPath =
      sessionStorage.getItem('booking_return_path_after_points') ||
      '/portal/passengers/booking-confirm'

    router.push(returnPath)
  } catch (error: any) {
    successMessage.value = error.message || '选择优惠券失败'
    showSuccessMessage.value = true
    setTimeout(() => {
      showSuccessMessage.value = false
    }, 3000)
  } finally {
    loading.value = false
  }
}

// 兑换积分
const exchangePoints = async (item: any) => {
  if (userPoints.value.currentPoints < item.requiredPoints) {
    successMessage.value = '积分不足，无法兑换'
    showSuccessMessage.value = true
    setTimeout(() => {
      showSuccessMessage.value = false
    }, 3000)
    return
  }
  
  try {
    loading.value = true
    // 积分扣减仍然通过后端接口完成
    await pointsApi.exchangePoints({
      exchangeType: item.type || 'voucher',
      requiredPoints: item.requiredPoints,
      itemName: item.name
    })

    if (isSelectVoucherMode.value) {
      // 选择模式：扣减积分后，直接把代金券应用到当前预订并返回
      applyVoucherToBooking(item)
      return
    }

    // 普通模式：仅展示提示并刷新列表
    successMessage.value = `成功兑换：${item.name}，已添加到您的账户`
    showSuccessMessage.value = true
    // 重新加载积分信息和积分记录
    await loadPointsInfo()
    if (activeTab.value === 'history') {
      await loadPointsHistory()
    }
    setTimeout(() => {
      showSuccessMessage.value = false
    }, 3000)
  } catch (error: any) {
    successMessage.value = error.message || '积分兑换失败'
    showSuccessMessage.value = true
    setTimeout(() => {
      showSuccessMessage.value = false
    }, 3000)
  } finally {
    loading.value = false
  }
}

// 组件挂载时加载数据
onMounted(() => {
  loadPointsInfo()
  loadCoupons()
})
</script>

<style scoped>
.points-coupons-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 2rem;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  min-height: 100vh;
}

/* 页面标题 */
.page-header {
  margin-bottom: 2rem;
  text-align: center;
}

.page-header h1 {
  font-size: 2.5rem;
  color: #1a202c;
  margin: 0 0 0.5rem 0;
  font-weight: 700;
}

.subtitle {
  color: #718096;
  font-size: 1.1rem;
  margin: 0;
}

/* 积分卡片区域 */
.points-card-section {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 1.5rem;
  margin-bottom: 2rem;
}

.points-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 20px;
  padding: 2rem;
  color: white;
  box-shadow: 0 10px 30px rgba(102, 126, 234, 0.3);
  transition: all 0.3s ease;
}

.points-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 15px 40px rgba(102, 126, 234, 0.4);
}

.points-header {
  display: flex;
  align-items: center;
  gap: 1.5rem;
  margin-bottom: 2rem;
  padding-bottom: 2rem;
  border-bottom: 1px solid rgba(255, 255, 255, 0.2);
}

.points-icon {
  font-size: 3rem;
}

.points-info {
  flex: 1;
}

.points-label {
  font-size: 0.9rem;
  opacity: 0.9;
  margin-bottom: 0.5rem;
  display: block;
}

.points-value {
  font-size: 2.5rem;
  font-weight: 700;
}

.points-details {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 1rem;
  margin-bottom: 2rem;
}

.detail-item {
  display: flex;
  flex-direction: column;
  gap: 0.3rem;
}

.detail-label {
  font-size: 0.85rem;
  opacity: 0.8;
}

.detail-value {
  font-size: 1.3rem;
  font-weight: 600;
}

.member-level {
  background: rgba(255, 255, 255, 0.2);
  padding: 0.3rem 0.8rem;
  border-radius: 20px;
  display: inline-block;
  width: fit-content;
}

.points-progress {
  background: rgba(255, 255, 255, 0.1);
  padding: 1rem;
  border-radius: 12px;
}

.progress-label {
  display: flex;
  justify-content: space-between;
  margin-bottom: 0.5rem;
  font-size: 0.9rem;
}

.progress-percent {
  font-weight: 600;
}

.progress-bar {
  width: 100%;
  height: 8px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 10px;
  overflow: hidden;
  margin-bottom: 0.5rem;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #ffd89b 0%, #19547b 100%);
  transition: width 0.3s ease;
}

.progress-text {
  font-size: 0.8rem;
  opacity: 0.8;
}

/* 积分说明卡片 */
.points-info-card {
  background: white;
  border-radius: 20px;
  padding: 1.5rem;
  box-shadow: 0 5px 20px rgba(0, 0, 0, 0.1);
}

.info-title {
  font-size: 1.1rem;
  font-weight: 600;
  color: #1a202c;
  margin-bottom: 1rem;
}

.info-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.info-list li {
  padding: 0.6rem 0;
  color: #4a5568;
  font-size: 0.95rem;
  border-bottom: 1px solid #e2e8f0;
}

.info-list li:last-child {
  border-bottom: none;
}

.info-list li::before {
  content: '✓ ';
  color: #48bb78;
  font-weight: 600;
  margin-right: 0.5rem;
}

/* 标签页 */
.tabs-container {
  margin-bottom: 2rem;
  background: white;
  border-radius: 15px;
  padding: 0;
  box-shadow: 0 5px 20px rgba(0, 0, 0, 0.1);
}

.tabs {
  display: flex;
  gap: 0;
}

.tab-btn {
  flex: 1;
  padding: 1.2rem;
  border: none;
  background: transparent;
  cursor: pointer;
  font-size: 1rem;
  font-weight: 500;
  color: #718096;
  border-bottom: 3px solid transparent;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  position: relative;
}

.tab-btn:hover {
  color: #667eea;
  background: rgba(102, 126, 234, 0.05);
}

.tab-btn.active {
  color: #667eea;
  border-bottom-color: #667eea;
}

.tab-icon {
  font-size: 1.3rem;
}

.tab-badge {
  background: #667eea;
  color: white;
  border-radius: 20px;
  padding: 0.2rem 0.6rem;
  font-size: 0.8rem;
  font-weight: 600;
  margin-left: 0.3rem;
}

/* 优惠券区域 */
.coupons-section {
  background: white;
  border-radius: 15px;
  padding: 2rem;
  box-shadow: 0 5px 20px rgba(0, 0, 0, 0.1);
}

.filter-bar {
  display: flex;
  gap: 1rem;
  margin-bottom: 2rem;
  flex-wrap: wrap;
}

.filter-btn {
  padding: 0.6rem 1.2rem;
  border: 2px solid #e2e8f0;
  background: white;
  border-radius: 20px;
  cursor: pointer;
  font-size: 0.95rem;
  color: #718096;
  transition: all 0.3s ease;
}

.filter-btn:hover {
  border-color: #667eea;
  color: #667eea;
}

.filter-btn.active {
  background: #667eea;
  color: white;
  border-color: #667eea;
}

.coupons-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.coupon-card {
  display: flex;
  align-items: center;
  gap: 1.5rem;
  padding: 1.5rem;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  border-radius: 15px;
  border: 2px solid #e2e8f0;
  transition: all 0.3s ease;
}

.coupon-card:hover {
  transform: translateX(5px);
  box-shadow: 0 5px 20px rgba(0, 0, 0, 0.1);
}

.coupon-card.expired,
.coupon-card.used {
  opacity: 0.6;
}

.coupon-card.unavailable {
  opacity: 0.7;
  border-color: #fbd38d;
}

.coupon-left {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1rem;
  min-width: 100px;
}

.coupon-amount {
  display: flex;
  align-items: baseline;
  gap: 0.3rem;
}

.currency {
  font-size: 1rem;
  color: #667eea;
  font-weight: 600;
}

.amount {
  font-size: 2.5rem;
  color: #667eea;
  font-weight: 700;
}

.percentage-symbol {
  font-size: 1.5rem;
  color: #667eea;
  font-weight: 700;
}

.coupon-divider {
  width: 80%;
  height: 2px;
  background: linear-gradient(90deg, transparent, #e2e8f0, transparent);
}

.coupon-middle {
  flex: 1;
}

.coupon-name {
  font-size: 1.1rem;
  font-weight: 600;
  color: #1a202c;
  margin-bottom: 0.3rem;
}

.coupon-desc {
  font-size: 0.9rem;
  color: #718096;
  margin-bottom: 0.3rem;
}

.coupon-condition {
  font-size: 0.85rem;
  color: #a0aec0;
}

.coupon-right {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.8rem;
  min-width: 100px;
}

.coupon-status {
  font-size: 0.9rem;
  color: #718096;
}

.use-btn,
.receive-btn {
  padding: 0.6rem 1.5rem;
  border: none;
  border-radius: 8px;
  font-size: 0.95rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.use-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.use-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(102, 126, 234, 0.3);
}

.receive-btn {
  background: #48bb78;
  color: white;
}

.receive-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(72, 187, 120, 0.3);
}

.expired-text {
  color: #a0aec0;
  font-size: 0.9rem;
}

.used-text {
  color: #718096;
  font-size: 0.9rem;
}

.unavailable-text {
  color: #f56565;
  font-size: 0.9rem;
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 3rem 2rem;
}

.empty-icon {
  font-size: 3rem;
  margin-bottom: 1rem;
}

.empty-text {
  color: #718096;
  font-size: 1.1rem;
}

/* 加载状态 */
.loading-state {
  text-align: center;
  padding: 3rem 2rem;
}

.loading-text {
  color: #718096;
  font-size: 1.1rem;
}

/* 积分兑换区域 */
.exchange-section {
  background: white;
  border-radius: 15px;
  padding: 2rem;
  box-shadow: 0 5px 20px rgba(0, 0, 0, 0.1);
}

.exchange-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 1.5rem;
}

.exchange-card {
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  border-radius: 15px;
  padding: 1.5rem;
  text-align: center;
  border: 2px solid #e2e8f0;
  transition: all 0.3s ease;
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.exchange-card:hover:not(.disabled) {
  transform: translateY(-8px);
  box-shadow: 0 10px 30px rgba(102, 126, 234, 0.2);
  border-color: #667eea;
}

.exchange-card.disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.exchange-icon {
  font-size: 2.5rem;
}

.exchange-name {
  font-size: 1.1rem;
  font-weight: 600;
  color: #1a202c;
}

.exchange-desc {
  font-size: 0.85rem;
  color: #718096;
}

.exchange-points {
  display: flex;
  flex-direction: column;
  gap: 0.3rem;
  padding: 1rem;
  background: rgba(102, 126, 234, 0.1);
  border-radius: 10px;
}

.points-label {
  font-size: 0.8rem;
  color: #718096;
}

.points-amount {
  font-size: 1.5rem;
  font-weight: 700;
  color: #667eea;
}

.exchange-btn {
  padding: 0.7rem 1.5rem;
  border: none;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-radius: 8px;
  font-size: 0.95rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.exchange-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(102, 126, 234, 0.3);
}

.exchange-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* 积分记录区域 */
.history-section {
  background: white;
  border-radius: 15px;
  padding: 2rem;
  box-shadow: 0 5px 20px rgba(0, 0, 0, 0.1);
}

.history-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.history-item {
  display: flex;
  align-items: center;
  gap: 1.5rem;
  padding: 1.2rem;
  background: #f7fafc;
  border-radius: 12px;
  border-left: 4px solid #e2e8f0;
  transition: all 0.3s ease;
}

.history-item:hover {
  background: #edf2f7;
  border-left-color: #667eea;
}

.history-icon {
  font-size: 1.5rem;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #e2e8f0;
  border-radius: 50%;
}

.history-icon.earn {
  background: #c6f6d5;
  color: #22543d;
}

.history-icon.spend {
  background: #fed7d7;
  color: #742a2a;
}

.history-content {
  flex: 1;
}

.history-title {
  font-size: 1rem;
  font-weight: 600;
  color: #1a202c;
  margin-bottom: 0.3rem;
}

.history-desc {
  font-size: 0.85rem;
  color: #718096;
  margin-bottom: 0.3rem;
}

.history-time {
  font-size: 0.85rem;
  color: #718096;
}

.history-points {
  font-size: 1.2rem;
  font-weight: 700;
}

.history-points.earn {
  color: #22863a;
}

.history-points.spend {
  color: #cb2431;
}

/* 成功提示 */
.success-message {
  position: fixed;
  bottom: 2rem;
  right: 2rem;
  background: linear-gradient(135deg, #48bb78 0%, #38a169 100%);
  color: white;
  padding: 1.2rem 1.5rem;
  border-radius: 12px;
  display: flex;
  align-items: center;
  gap: 1rem;
  box-shadow: 0 10px 30px rgba(72, 187, 120, 0.3);
  animation: slideIn 0.3s ease;
  z-index: 1000;
}

.message-icon {
  font-size: 1.5rem;
  font-weight: 700;
}

.message-text {
  flex: 1;
}

.close-btn {
  background: none;
  border: none;
  color: white;
  font-size: 1.5rem;
  cursor: pointer;
  padding: 0;
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
}

@keyframes slideIn {
  from {
    transform: translateX(400px);
    opacity: 0;
  }
  to {
    transform: translateX(0);
    opacity: 1;
  }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .points-coupons-container {
    padding: 1rem;
  }

  .page-header h1 {
    font-size: 1.8rem;
  }

  .points-card-section {
    grid-template-columns: 1fr;
  }

  .points-details {
    grid-template-columns: 1fr;
  }

  .tabs {
    flex-direction: column;
  }

  .tab-btn {
    border-bottom: none;
    border-right: 3px solid transparent;
  }

  .tab-btn.active {
    border-right-color: #667eea;
    border-bottom: none;
  }

  .coupon-card {
    flex-direction: column;
    text-align: center;
  }

  .coupon-right {
    width: 100%;
  }

  .exchange-grid {
    grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
  }

  .success-message {
    left: 1rem;
    right: 1rem;
    bottom: 1rem;
  }
}
</style>


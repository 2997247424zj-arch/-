<template>
  <PassengerLayout>
    <div class="page-container pax-page">
      <section class="glass-card booking-card">
        <div class="booking-modal">
          <div class="booking-modal-header">
            <div>
              <h3>确认预订信息</h3>
              <p v-if="bookingModal.flight">
                {{ bookingModal.flight.flightNumber }} ·
                {{ bookingModal.flight.departure }} → {{ bookingModal.flight.destination }}
              </p>
            </div>
            <button class="close-btn" @click="handleBack">×</button>
          </div>

          <div class="booking-modal-body">
            <!-- 航班信息 -->
            <div v-if="bookingModal.flight" class="booking-section">
              <h4>航班信息</h4>
              <div class="flight-info-grid">
                <div class="info-item">
                  <span class="info-label">航班号</span>
                  <span class="info-value">{{ bookingModal.flight.flightNumber }}</span>
                </div>
                <div class="info-item">
                  <span class="info-label">航空公司</span>
                  <span class="info-value">{{ bookingModal.flight.airline }}</span>
                </div>
                <div class="info-item">
                  <span class="info-label">出发时间</span>
                  <span class="info-value">{{ bookingModal.flight.departureTime }}</span>
                </div>
                <div class="info-item">
                  <span class="info-label">到达时间</span>
                  <span class="info-value">{{ bookingModal.flight.arrivalTime }}</span>
                </div>
                <div class="info-item">
                  <span class="info-label">出发机场</span>
                  <span class="info-value">{{ bookingModal.flight.departure }}</span>
                </div>
                <div class="info-item">
                  <span class="info-label">到达机场</span>
                  <span class="info-value">{{ bookingModal.flight.destination }}</span>
                </div>
              </div>
            </div>

            <!-- 座位信息 -->
            <div
              v-if="(bookingModal.flight?.selectedSeats && bookingModal.flight.selectedSeats.length > 0) ||
                     (bookingModal.flight?.seatDetails && bookingModal.flight.seatDetails.length > 0)"
              class="booking-section seats-section"
            >
              <div class="seats-section-header">
                <span class="seats-section-title">已选座位</span>
                <span class="seats-section-subtitle" v-if="bookingModal.flight?.seatDetails?.length">
                  已为本次行程锁定的座位
                </span>
              </div>
              <div class="seats-list">
                <div
                  v-for="(seatDetail, index) in (bookingModal.flight?.seatDetails || [])"
                  :key="index"
                  class="seat-chip"
                >
                  <span class="seat-tag">{{ seatDetail.seatLabel || seatDetail.seatNumber }}</span>
                  <span class="seat-price" v-if="seatDetail.price > 0">+¥{{ seatDetail.price }}</span>
                </div>
                <span
                  v-if="!bookingModal.flight?.seatDetails && bookingModal.flight?.selectedSeats"
                  v-for="seat in bookingModal.flight.selectedSeats"
                  :key="seat"
                  class="seat-tag"
                >
                  {{ seat }}
                </span>
              </div>
            </div>

            <!-- 常用乘客快速选择 -->
            <div class="booking-section">
              <h4>快速选择乘客</h4>
              <p class="section-desc">从常用乘客中快速选择，或为其他人购票</p>
              <FrequentPassengers
                :hide-myself-tab="true"
                @select="handleSelectFrequentPassenger"
              />
            </div>

            <!-- 乘客信息 -->
            <div class="booking-section">
              <h4>乘客信息</h4>
              <PassengerManager
                ref="passengerManagerRef"
                v-model="bookingForm.passengers"
                :max-passengers="maxPassengers"
                :show-validation-summary="true"
                @validate="passengerValidationStatus = $event"
              />
            </div>

            <!-- 价格信息 -->
            <div class="booking-section price-section">
              <h4>费用明细</h4>
              <div class="price-breakdown">
                <div class="price-item">
                  <span>基础票价 × {{ bookingForm.passengers.length }}</span>
                  <span>¥{{ (bookingModal.flight?.price || 0) * bookingForm.passengers.length }}</span>
                </div>
                <div
                  v-if="bookingModal.flight?.seatDetails && bookingModal.flight.seatDetails.length > 0"
                  class="price-item"
                >
                  <span>座位选择费</span>
                  <span>
                    ¥{{
                      bookingModal.flight.seatDetails.reduce(
                        (sum: number, s: any) => sum + (s.price || 0),
                        0
                      )
                    }}
                  </span>
                </div>
                <!-- 积分与优惠券入口 -->
                <div class="price-item points-row">
                  <div class="points-text">
                    <span class="points-label">积分与优惠券</span>
                    <span class="points-desc">会员折扣、现金红包一键管理</span>
                  </div>
                  <div class="points-action">
                    <span v-if="appliedCoupons.length > 0" class="selected-voucher-chip">
                      已选 {{ appliedCoupons.length }} 张
                    </span>
                    <button class="points-btn" type="button" @click="openPointsAndCoupons">
                      选择积分 / 优惠券
                    </button>
                  </div>
                </div>
                <!-- 已选择的优惠券列表（按应用顺序显示） -->
                <div
                  v-for="(coupon, index) in appliedCoupons"
                  :key="coupon.id || index"
                  v-show="coupon.appliedAmount > 0"
                  class="price-item discount-row"
                >
                  <div class="discount-text">
                    <span class="discount-title">{{ coupon.type === 'percentage' ? '折扣券' : '代金券' }}</span>
                    <span class="discount-name">{{ coupon.name }}</span>
                  </div>
                  <span class="discount-amount">-¥{{ coupon.appliedAmount.toFixed(2).replace(/\.?0+$/, '') }}</span>
                </div>
                <div class="price-item total">
                  <span>总计</span>
                  <span class="total-price">¥{{ calculateTotalPrice }}</span>
                </div>
              </div>
            </div>
          </div>

          <div class="booking-modal-footer">
            <button
              class="booking-btn booking-btn-ghost"
              @click="handleBack"
              :disabled="bookingModal.loading"
            >
              取消
            </button>
            <button
              class="booking-btn booking-btn-secondary"
              @click="openSeatSelectionFromBooking"
              :disabled="!passengerValidationStatus || bookingModal.loading"
            >
              选择座位
            </button>
            <button
              class="booking-btn booking-btn-primary"
              @click="submitBooking"
              :disabled="bookingModal.loading || !allPassengersHaveSeats"
            >
              {{ bookingModal.loading ? '提交中...' : `确认预订 (¥${calculateTotalPrice})` }}
            </button>
          </div>
        </div>
      </section>
    </div>
  </PassengerLayout>
</template>

<script setup lang="ts">
import PassengerLayout from '../components/layout/PassengerLayout.vue'
import PassengerManager from '../components/PassengerManager.vue'
import FrequentPassengers from '../components/FrequentPassengers.vue'
import { ref, reactive, computed, onMounted, nextTick } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { flightApi, apiUtils } from '../services/api'

// 下载订单的所有机票文档
const downloadOrderDocuments = async (orderNo: string) => {
  try {
    // 使用与api.ts相同的API_BASE_URL配置
    const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'
    const url = `${API_BASE_URL}/flights/order/${encodeURIComponent(orderNo)}/documents`
    console.log('下载文档URL:', url)
    
    const response = await fetch(url, {
      method: 'GET',
      credentials: 'include',
      headers: {
        'Accept': 'application/octet-stream, application/zip, */*'
      }
    })

    console.log('下载响应状态:', response.status, response.statusText)

    if (!response.ok) {
      // 尝试读取错误信息
      let errorMessage = `下载失败: ${response.status} ${response.statusText}`
      try {
        // 先尝试读取为文本，看是否是JSON
        const text = await response.text()
        if (text) {
          try {
            const errorData = JSON.parse(text)
            if (errorData && errorData.message) {
              errorMessage = errorData.message
            } else if (errorData && errorData.data && errorData.data.message) {
              errorMessage = errorData.data.message
            }
          } catch (e) {
            // 不是JSON，使用原始文本
            if (text.length < 200) {
              errorMessage = text
            }
          }
        }
      } catch (e) {
        // 忽略解析错误
        console.warn('解析错误响应失败:', e)
      }
      throw new Error(errorMessage)
    }

    // 检查响应类型
    const contentType = response.headers.get('content-type')
    console.log('响应Content-Type:', contentType)

    // 获取文件blob
    const blob = await response.blob()
    console.log('Blob大小:', blob.size, 'bytes')
    
    if (blob.size === 0) {
      throw new Error('下载的文件为空，可能是订单下没有机票或文档生成失败')
    }
    
    // 创建下载链接
    const downloadUrl = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = downloadUrl
    link.download = `订单_${orderNo}_机票文档.zip`
    document.body.appendChild(link)
    link.click()
    
    // 清理
    setTimeout(() => {
      document.body.removeChild(link)
      window.URL.revokeObjectURL(downloadUrl)
    }, 100)
  } catch (error: any) {
    console.error('下载机票文档失败:', error)
    throw error
  }
}

const router = useRouter()
const route = useRoute()

// 预订模态框状态（作为独立页面使用，visible 始终为 true）
const bookingModal = reactive({
  visible: true,
  flight: null as any,
  loading: false
})

// 预订表单
const bookingForm = reactive({
  passengers: [] as Array<{
    name: string
    idCard: string
    seatId?: string
    seatLabel?: string
    seatClass: string
    seatPreference: string
    passengerType: string
    phone: string
  }>,
  contactInfo: {
    name: '',
    phone: '',
    email: ''
  }
})

const passengerValidationStatus = ref(false)
const passengerManagerRef = ref<InstanceType<typeof PassengerManager> | null>(null)

// 乘客人数：优先使用路由参数，其次 1
const maxPassengers = computed(() => {
  const val = route.query.passengers as string | undefined
  const n = val ? parseInt(val) : 1
  return isNaN(n) ? 1 : n
})

// 已应用的优惠券列表（支持叠加使用）
const appliedCoupons = ref<Array<{
  id: string | number
  name: string
  type: 'cash' | 'percentage'
  discountValue: number
  displayValue?: string
  minAmount?: number
  requiredPoints?: number
  flightId?: string
  appliedAmount: number  // 实际应用的优惠金额
  appliedPrice: number   // 应用此优惠后的价格
}>>([])

// 计算未使用代金券前的基础总价
const baseTotalPrice = computed(() => {
  if (!bookingModal.flight) return 0
  const basePrice = bookingModal.flight.price || 0
  const seatFee =
    bookingModal.flight.seatDetails?.reduce(
      (sum: number, s: any) => sum + (s.price || 0),
      0
    ) || 0
  return (basePrice + seatFee) * bookingForm.passengers.length
})

// 验证：所有乘客是否已选座（用于禁用提交按钮和提交前校验）
const allPassengersHaveSeats = computed(() => {
  if (!bookingModal.flight) return false
  if (!bookingForm.passengers || bookingForm.passengers.length === 0) return false
  return bookingForm.passengers.every((p: any) => Boolean(p.seatLabel))
})

// 计算叠加优惠后的总价（按顺序应用：先现金券，再折扣券）
const calculateTotalPrice = computed(() => {
  if (appliedCoupons.value.length === 0) {
    return baseTotalPrice.value
  }

  // 按类型排序：先现金券（代金券/红包），再折扣券
  const sortedCoupons = [...appliedCoupons.value].sort((a, b) => {
    if (a.type === 'cash' && b.type === 'percentage') return -1
    if (a.type === 'percentage' && b.type === 'cash') return 1
    return 0
  })

  let currentPrice = baseTotalPrice.value

  // 按顺序应用每个优惠券，并记录实际优惠金额
  sortedCoupons.forEach((coupon, index) => {
    // 检查满减门槛
    if (coupon.minAmount && currentPrice < coupon.minAmount) {
      coupon.appliedAmount = 0
      coupon.appliedPrice = currentPrice
      return
    }

    if (coupon.type === 'percentage') {
      // 折扣券：在当前价格基础上打折
      const factor = coupon.discountValue && Number.isFinite(coupon.discountValue)
        ? Math.min(Math.max(coupon.discountValue, 0), 1)
        : 1
      const newPrice = currentPrice * factor
      coupon.appliedAmount = currentPrice - newPrice
      coupon.appliedPrice = newPrice
      currentPrice = newPrice
    } else {
      // 现金券：直接抵扣
      const cashOff = coupon.discountValue || 0
      const discount = Math.min(cashOff, currentPrice)
      coupon.appliedAmount = discount
      coupon.appliedPrice = currentPrice - discount
      currentPrice = coupon.appliedPrice
    }
  })

  return Math.max(currentPrice, 0)
})

// 总优惠金额（所有优惠券的优惠总和）
const totalDiscount = computed(() => {
  return appliedCoupons.value.reduce((sum, coupon) => sum + (coupon.appliedAmount || 0), 0)
})

// 初始化预订表单（从原 PassengerExperienceView 复制，略作适配）
const initBookingForm = (flight: any) => {
  const passengerCount = maxPassengers.value
  bookingForm.passengers = []

  for (let i = 0; i < passengerCount; i++) {
    bookingForm.passengers.push({
      name: '',
      idCard: '',
      seatId: flight.selectedSeats?.[i] || undefined,
      seatLabel: flight.seatDetails?.[i]?.seatLabel || undefined,
      seatClass: (flight.seatDetails?.[i]?.seatClass || 'economy') as string,
      seatPreference: 'any',
      passengerType: 'adult',
      phone: ''
    })
  }

  // 如果有已选座位，自动分配
  if (flight.selectedSeats && flight.selectedSeats.length > 0) {
    flight.selectedSeats.forEach((seatId: string, index: number) => {
      if (bookingForm.passengers[index]) {
        bookingForm.passengers[index].seatId = seatId
        bookingForm.passengers[index].seatLabel = flight.seatDetails?.[index]?.seatLabel || seatId
      }
    })
  }

  bookingForm.contactInfo = {
    name: '',
    phone: '',
    email: ''
  }
}

// 选择常用乘客（逻辑与 PassengerExperienceView 相同）
const handleSelectFrequentPassenger = (passenger: any) => {
  if (bookingForm.passengers.length > 0) {
    const updatedPassenger = {
      ...bookingForm.passengers[0],
      name: passenger.name || '',
      idCard: passenger.idCard || '',
      phone: passenger.phone || '',
      seatClass: (bookingForm.passengers[0]?.seatClass || 'economy') as string,
      seatPreference: (bookingForm.passengers[0]?.seatPreference || 'any') as string,
      passengerType: (bookingForm.passengers[0]?.passengerType || 'adult') as string
    }

    bookingForm.passengers = [
      updatedPassenger,
      ...bookingForm.passengers.slice(1)
    ]

    nextTick(() => {
      if (passengerManagerRef.value) {
        passengerManagerRef.value.validateAll()
      }
    })
  }
}

// 选择座位：沿用原逻辑，跳到 PassengerSeatSelectionView
const openSeatSelectionFromBooking = () => {
  if (!bookingModal.flight) return
  if (!passengerValidationStatus.value) {
    alert('请先填写并通过验证')
    return
  }

  const flightId = bookingModal.flight.id || Date.now().toString()
  // 保存当前航班信息与乘客信息，供选座页和返回后恢复使用
  sessionStorage.setItem(`flight_${flightId}`, JSON.stringify(bookingModal.flight))
  sessionStorage.setItem(`booking_passengers_${flightId}`, JSON.stringify(bookingForm.passengers))
  sessionStorage.setItem('seatSelectionReturnPath', router.currentRoute.value.fullPath)
  sessionStorage.setItem('reopenBookingAfterSeat', '1')

  router.push({
    path: '/portal/passengers/seat-selection',
    query: {
      flightId,
      cabinClass: bookingForm.passengers[0]?.seatClass || bookingModal.flight.seatClass || '',
      flightNumber: bookingModal.flight.flightNumber,
      departure: bookingModal.flight.departure,
      destination: bookingModal.flight.destination,
      departureTime: bookingModal.flight.departureTime,
      arrivalTime: bookingModal.flight.arrivalTime,
      passengers: bookingForm.passengers.length.toString()
    }
  })
}

// 打开“积分与优惠券”页面，选择本次订单要使用的代金券
const openPointsAndCoupons = () => {
  if (!bookingModal.flight) return

  // 当前航班标识，优先使用已有 flightId
  const flightId =
    (bookingModal.flight.id as string | undefined) ||
    (route.query.flightId as string | undefined) ||
    Date.now().toString()

  // 缓存当前航班和乘客信息，避免返回后丢失
  sessionStorage.setItem(`booking_flight_${flightId}`, JSON.stringify(bookingModal.flight))
  sessionStorage.setItem(`booking_passengers_${flightId}`, JSON.stringify(bookingForm.passengers))

  // 记录返回路径和当前基础总价，供“积分与优惠券”页使用
  sessionStorage.setItem('booking_return_path_after_points', router.currentRoute.value.fullPath)
  sessionStorage.setItem('booking_price_before_voucher', baseTotalPrice.value.toString())
  sessionStorage.setItem('booking_flight_id_for_points', flightId)

  router.push({
    path: '/portal/passengers/points-coupons',
    query: {
      mode: 'selectVoucher',
      flightId
    }
  })
}

// 提交预订
const submitBooking = async () => {
  if (!bookingModal.flight) return

  if (!passengerValidationStatus.value) {
    alert('请填写完整的乘客信息并通过验证')
    return
  }

  try {
    // 校验：确保所有乘客都有已选座位
    if (!allPassengersHaveSeats.value) {
      alert(`请为所有乘客选择座位（共 ${bookingForm.passengers.length} 人）`)
      return
    }

    bookingModal.loading = true

    const seatFee = bookingModal.flight.seatDetails?.reduce(
      (sum: number, s: any) => sum + (s.price || 0),
      0
    ) || 0

    const bookingData = {
      flightId: bookingModal.flight.id,
      passengers: bookingForm.passengers.map((p, index) => {
        const passengerSeatFee = bookingModal.flight.seatDetails?.[index]?.price || 0
        return {
          name: p.name.trim(),
          idCard: p.idCard.trim(),
          phone: p.phone || '',
          seatClass: bookingModal.flight.seatClass || 'economy',
          seatNumber: p.seatLabel || undefined,
          seatFee: passengerSeatFee
        }
      }),
      totalAmount: calculateTotalPrice.value,
      usedPoints: undefined,
      appliedCoupons: appliedCoupons.value.length > 0 ? appliedCoupons.value.map(c => ({
        id: c.id,
        name: c.name,
        type: c.type,
        appliedAmount: c.appliedAmount
      })) : undefined,
      contactInfo: {
        name: '',
        phone: '',
        email: ''
      }
    }

    // 发起支付流程：调用后端支付宝发起接口（后端会根据业务决定是否先创建订单或在回调时创建）
    try {
      const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'
      // ensure passengerId is included (backend requires it)
      const currentUser = apiUtils.getCurrentUser()
      if (!currentUser || !currentUser.id) {
        alert('请先登录后再预订')
        bookingModal.loading = false
        return
      }
      const payload = {
        passengerId: typeof currentUser.id === 'string' ? parseInt(currentUser.id, 10) : currentUser.id,
        ...bookingData
      }

      const resp = await fetch(`${API_BASE_URL}/alipay/pay`, {
        method: 'POST',
        credentials: 'include',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(payload)
      })

      if (!resp.ok) {
        const text = await resp.text().catch(() => '')
        throw new Error(`支付发起失败：${resp.status} ${resp.statusText} ${text}`)
      }

      // 返回的是支付宝页面 HTML（iframe 或新窗口展示）
      const html = await resp.text()
      const w = window.open('', '_blank')
      if (!w) {
        // 如果弹窗被拦截，提示用户并在当前页展示（可根据需要调整）
        alert('浏览器阻止弹窗，请允许弹窗或手动打开支付页面')
        // 作为降级：在当前页面打开一个临时窗口
        const newWin = window.open()
        newWin?.document.open()
        newWin?.document.write(html)
        newWin?.document.close()
      } else {
        w.document.open()
        w.document.write(html)
        w.document.close()
      }

      // 不在此处直接创建/下载机票，等待支付回调完成后后端会生成机票并提供下载接口
    } catch (err: any) {
      console.error('发起支付失败:', err)
      alert(err.message || '发起支付失败，请重试')
    }
  } catch (error: any) {
    alert(error.message || '预订失败，请重试')
  } finally {
    bookingModal.loading = false
  }
}

const handleBack = () => {
  router.back()
}

// 页面初始化：从 sessionStorage 中恢复航班信息
onMounted(() => {
  const flightId = route.query.flightId as string
  const key = flightId ? `booking_flight_${flightId}` : ''
  const flightData = key ? sessionStorage.getItem(key) : null

  if (flightData) {
    const flight = JSON.parse(flightData)
    bookingModal.flight = flight
    initBookingForm(flight)

    

    // 尝试恢复乘客信息（姓名 / 证件号等）
    const passengersKey = flightId ? `booking_passengers_${flightId}` : ''
    const passengersStr = passengersKey ? sessionStorage.getItem(passengersKey) : null
    if (passengersStr) {
      try {
        const savedPassengers = JSON.parse(passengersStr)
        if (Array.isArray(savedPassengers) && savedPassengers.length > 0) {
          // 只覆盖表单中的基础字段，避免丢失我们在 initBookingForm 中设置的 seatClass 等
          savedPassengers.forEach((p: any, i: number) => {
            if (bookingForm.passengers[i]) {
              bookingForm.passengers[i].name = p.name || ''
              bookingForm.passengers[i].idCard = p.idCard || ''
              bookingForm.passengers[i].phone = p.phone || ''
            }
          })
        }
      } catch (e) {
        console.warn('恢复乘客信息失败:', e)
      }
    }

    // 尝试恢复从选座页返回的座位信息
    const selectedSeatsStr = sessionStorage.getItem('selectedSeats')
    const seatDetailsStr = sessionStorage.getItem('seatDetails')
    const storedFlightId = sessionStorage.getItem('flightId')
    if (storedFlightId && storedFlightId === flightId && seatDetailsStr) {
      try {
        const selectedSeats = selectedSeatsStr ? JSON.parse(selectedSeatsStr) : []
        const seatDetails = JSON.parse(seatDetailsStr)

        // 更新航班对象上的座位信息（用于费用明细）
        bookingModal.flight.selectedSeats = selectedSeats
        bookingModal.flight.seatDetails = seatDetails

        // 将座位信息同步回乘客表单
        bookingForm.passengers.forEach((p, i) => {
          if (selectedSeats[i] && seatDetails[i]) {
            p.seatId = selectedSeats[i]
            p.seatLabel = seatDetails[i].seatLabel || seatDetails[i].seatNumber
            p.seatClass = (seatDetails[i].seatClass || p.seatClass || 'economy') as string
          }
        })
      } catch (e) {
        console.warn('恢复座位选择信息失败:', e)
      } finally {
        // 只在确认页面消费一次这些缓存
        sessionStorage.removeItem('flightId')
        sessionStorage.removeItem('selectedSeats')
        sessionStorage.removeItem('seatDetails')
      }
    }

    // 恢复从"积分与优惠券"页面选择的优惠券列表（支持多个）
    const couponsStr = sessionStorage.getItem('booking_selected_coupons')
    if (couponsStr) {
      try {
        const savedCoupons = JSON.parse(couponsStr)
        // 检查优惠券是否属于当前航班
        if (!flightId || !Array.isArray(savedCoupons) || savedCoupons.length === 0) {
          sessionStorage.removeItem('booking_selected_coupons')
          appliedCoupons.value = []
        } else {
          // 过滤出属于当前航班的优惠券
          const validCoupons = savedCoupons
            .filter((c: any) => !c.flightId || c.flightId === flightId)
            .map((c: any) => ({
              ...c,
              appliedAmount: 0,
              appliedPrice: 0
            }))
          
          if (validCoupons.length > 0) {
            appliedCoupons.value = validCoupons
          } else {
            sessionStorage.removeItem('booking_selected_coupons')
            appliedCoupons.value = []
          }
        }
      } catch (e) {
        console.warn('恢复优惠券信息失败:', e)
        sessionStorage.removeItem('booking_selected_coupons')
        appliedCoupons.value = []
      }
    } else {
      // 兼容旧数据：尝试恢复单个优惠券
      const voucherStr = sessionStorage.getItem('booking_selected_voucher')
      if (voucherStr) {
        try {
          const voucher = JSON.parse(voucherStr)
          if (flightId && voucher.flightId === flightId) {
            const normalizedVoucher = (() => {
              if (voucher.type === 'percentage') {
                const factor =
                  typeof voucher.discountValue === 'number' && voucher.discountValue > 0
                    ? Math.min(Math.max(voucher.discountValue, 0), 1)
                    : 1
                return { ...voucher, discountValue: factor }
              }
              const amount =
                typeof voucher.discountValue === 'number'
                  ? voucher.discountValue
                  : typeof voucher.amount === 'number'
                    ? voucher.amount
                    : 0
              return {
                ...voucher,
                type: voucher.type || 'cash',
                discountValue: amount
              }
            })()
            appliedCoupons.value = [{
              ...normalizedVoucher,
              appliedAmount: 0,
              appliedPrice: 0
            }]
            // 迁移到新格式
            sessionStorage.setItem('booking_selected_coupons', JSON.stringify(appliedCoupons.value))
            sessionStorage.removeItem('booking_selected_voucher')
          }
        } catch (e) {
          console.warn('迁移旧优惠券数据失败:', e)
        }
      }
      if (appliedCoupons.value.length === 0) {
        appliedCoupons.value = []
      }
    }
  } else {
    // 没有找到航班信息，清空优惠券数据并返回上一页
    sessionStorage.removeItem('booking_selected_coupons')
    sessionStorage.removeItem('booking_selected_voucher')
    appliedCoupons.value = []
    router.back()
  }
})
</script>

<style scoped>
.booking-card {
  max-width: 1120px;
  margin: 1.5rem auto 2.5rem;
  padding: 1.75rem 2rem 2rem;
  border: 1px solid var(--border-color);
  background: color-mix(in srgb, var(--app-surface, #FFFFFF) 96%, transparent);
  box-shadow: 0 20px 48px rgba(8,18,40,0.06);
}

.booking-modal {
  display: flex;
  flex-direction: column;
  gap: 1.75rem;
}

.booking-modal-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 1.5rem;
  border-bottom: 1px solid var(--border-light, rgba(148, 163, 184, 0.14));
  padding-bottom: 1rem;
}

.booking-modal-header h3 {
  margin: 0;
  font-size: 1.4rem;
  font-weight: 600;
  color: var(--text-primary);
}

.booking-modal-header p {
  margin: 0.35rem 0 0;
  font-size: 0.95rem;
  color: var(--text-secondary);
}

.close-btn {
  border: none;
  background: transparent;
  color: rgba(148, 163, 184, 0.9);
  font-size: 1.25rem;
  cursor: pointer;
  padding: 0.1rem 0.35rem;
  border-radius: 999px;
  transition: background 0.18s ease, color 0.18s ease, transform 0.12s ease;
}

.close-btn:hover {
  background: rgba(148, 163, 184, 0.16);
  color: #e5e7eb;
  transform: scale(1.03);
}

.booking-modal-body {
  display: flex;
  flex-direction: column;
  gap: 1.75rem;
}

.booking-section {
  padding-top: 0.75rem;
  border-top: 1px solid var(--border-light, rgba(15,23,42,0.06));
}

.booking-section:first-of-type {
  border-top: none;
  padding-top: 0;
}

.booking-section h4 {
  margin: 0 0 0.8rem;
  font-size: 1.05rem;
  font-weight: 600;
  color: var(--text-primary);
}

.section-desc {
  margin: 0 0 0.9rem;
  font-size: 0.9rem;
  color: rgba(148, 163, 184, 0.9);
}

.flight-info-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 0.9rem 1.75rem;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 0.18rem;
}

.info-label {
  font-size: 0.78rem;
  color: var(--text-secondary);
}

.info-value {
  font-size: 0.98rem;
  font-weight: 600;
  color: var(--text-primary);
}

.seats-section {
  padding-top: 0.9rem;
}

.seats-section-header {
  display: flex;
  flex-direction: column;
  gap: 0.18rem;
  margin-bottom: 0.5rem;
}

.seats-section-title {
  font-size: 0.95rem;
  font-weight: 600;
  letter-spacing: 0.16em;
  text-transform: uppercase;
  color: rgba(148, 163, 184, 0.95);
  font-family: system-ui, -apple-system, BlinkMacSystemFont, "SF Pro Text", "Segoe UI",
    "Helvetica Neue", Arial, "PingFang SC", "Microsoft YaHei", sans-serif;
}

.seats-section-subtitle {
  font-size: 0.8rem;
  color: rgba(148, 163, 184, 0.85);
}

.seats-list {
  display: flex;
  flex-wrap: wrap;
  gap: 0.6rem;
  margin-top: 0.4rem;
}

.seat-chip {
  display: inline-flex;
  align-items: center;
  gap: 0.35rem;
}

.seat-tag {
  padding: 0.28rem 0.75rem;
  border-radius: 999px;
  background: color-mix(in srgb, var(--color-primary) 8%, transparent);
  border: 1px solid color-mix(in srgb, var(--color-primary) 22%, transparent);
  color: color-mix(in srgb, var(--color-white) 92%, transparent);
  font-size: 0.82rem;
  white-space: nowrap;
}

.seat-price {
  font-size: 0.8rem;
  color: rgba(248, 250, 252, 0.9);
}

.price-breakdown {
  margin-top: 0.4rem;
  padding: 1rem 1.25rem;
  border-radius: 0.9rem;
  background: color-mix(in srgb, var(--app-surface, #FFFFFF) 96%, transparent);
  border: 1px solid color-mix(in srgb, var(--color-primary) 10%, var(--border-light, rgba(15,23,42,0.06)));
  display: flex;
  flex-direction: column;
  gap: 0.7rem;
  box-shadow: 0 8px 22px rgba(8,18,40,0.04);
}

.price-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 0.95rem;
  color: rgba(226, 232, 240, 0.94);
}

.points-row {
  background: color-mix(in srgb, var(--app-surface, #FFFFFF) 96%, transparent);
  border: 1px solid color-mix(in srgb, var(--color-primary) 10%, transparent);
  border-radius: 12px;
  padding: 0.75rem 0.9rem;
  gap: 0.75rem;
}

.points-text {
  display: flex;
  flex-direction: column;
  gap: 0.12rem;
}

.points-label {
  font-weight: 600;
  color: #e5f2ff;
}

.points-desc {
  font-size: 0.82rem;
  color: rgba(148, 163, 184, 0.95);
}

.points-action {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.points-btn {
  padding: 0.45rem 0.95rem;
  border-radius: 999px;
  border: 1px solid color-mix(in srgb, var(--color-primary) 14%, transparent);
  background: linear-gradient(135deg, color-mix(in srgb, var(--color-primary) 90%, transparent), color-mix(in srgb, var(--color-primary-dark) 80%, transparent));
  color: var(--color-white);
  font-weight: 600;
  cursor: pointer;
  transition:
    transform 0.12s ease,
    box-shadow 0.18s ease,
    filter 0.18s ease;
}

.points-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 10px 26px rgba(59, 130, 246, 0.45);
  filter: brightness(1.08);
}

.selected-voucher-chip {
  display: inline-flex;
  align-items: center;
  gap: 0.35rem;
  padding: 0.4rem 0.75rem;
  border-radius: 999px;
  background: rgba(34, 197, 94, 0.12);
  border: 1px solid rgba(34, 197, 94, 0.45);
  color: #bbf7d0;
  font-size: 0.88rem;
}

.chip-sub {
  color: rgba(148, 163, 184, 0.9);
  font-size: 0.78rem;
}

.price-item.total {
  margin-top: 0.3rem;
  padding-top: 0.45rem;
  border-top: 1px dashed rgba(148, 163, 184, 0.6);
  font-weight: 600;
}

.total-price {
  font-size: 1.15rem;
  color: #facc15;
}

.discount-row .discount-text {
  display: flex;
  flex-direction: column;
  gap: 0.12rem;
}

.discount-title {
  font-size: 0.82rem;
  color: rgba(148, 163, 184, 0.9);
}

.discount-name {
  font-weight: 600;
  color: #f8fafc;
}

.discount-amount {
  color: #34d399;
  font-weight: 700;
}

.booking-modal-footer {
  margin-top: 1.75rem;
  padding-top: 1.25rem;
  border-top: 1px solid var(--border-light, rgba(15,23,42,0.06));
  display: flex;
  justify-content: flex-end;
  gap: 0.8rem;
}

.booking-btn {
  min-width: 120px;
  padding: 0.6rem 1.4rem;
  border-radius: 999px;
  border: 1px solid transparent;
  font-size: 0.9rem;
  font-weight: 500;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  cursor: pointer;
  transition:
    background-color 0.16s ease-out,
    border-color 0.16s ease-out,
    box-shadow 0.16s ease-out,
    transform 0.12s ease-out,
    color 0.16s ease-out;
  font-family: system-ui, -apple-system, BlinkMacSystemFont, "SF Pro Text", "Segoe UI",
    "Helvetica Neue", Arial, "PingFang SC", "Microsoft YaHei", sans-serif;
}

.booking-btn-primary {
  background: linear-gradient(135deg, var(--color-primary), var(--color-primary-dark));
  color: var(--color-white);
  box-shadow:
    0 0 0 1px color-mix(in srgb, var(--color-primary) 16%, transparent),
    0 14px 30px rgba(15, 118, 210, 0.35);
}

.booking-btn-primary:hover:not(:disabled) {
  background: linear-gradient(135deg, #60a5fa, #38bdf8);
  transform: translateY(-1px);
  box-shadow:
    0 0 0 1px rgba(56, 189, 248, 0.7),
    0 18px 40px rgba(15, 118, 210, 0.85);
}

.booking-btn-secondary {
  background: color-mix(in srgb, var(--app-surface, #FFFFFF) 96%, transparent);
  color: var(--text-primary);
  border-color: color-mix(in srgb, var(--color-primary) 10%, transparent);
}

.booking-btn-secondary:hover:not(:disabled) {
  background: rgba(30, 64, 175, 0.95);
  box-shadow:
    0 0 0 1px rgba(129, 140, 248, 0.75),
    0 10px 22px rgba(30, 64, 175, 0.85);
  transform: translateY(-1px);
}

.booking-btn-ghost {
  background: transparent;
  color: rgba(226, 232, 240, 0.8);
  border-color: rgba(148, 163, 184, 0.45);
}

.booking-btn-ghost:hover:not(:disabled) {
  background: rgba(15, 23, 42, 0.9);
  border-color: rgba(148, 163, 184, 0.8);
  box-shadow: 0 8px 20px rgba(15, 23, 42, 0.85);
  transform: translateY(-1px);
}

.booking-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

@media (max-width: 960px) {
  .booking-card {
    padding: 1.25rem 1.25rem 1.5rem;
    margin: 1rem auto 2rem;
  }

  .flight-info-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .booking-modal-footer {
    flex-direction: column-reverse;
    align-items: stretch;
  }

  .booking-btn {
    width: 100%;
  }
}

@media (max-width: 640px) {
  .flight-info-grid {
    grid-template-columns: minmax(0, 1fr);
  }

  .booking-modal-header {
    flex-direction: column-reverse;
    align-items: flex-start;
  }
}
</style>



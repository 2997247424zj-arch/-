<template>
  <PassengerLayout>
    <!-- 面包屑导航 -->
    <div class="topbar">
      <div class="breadcrumb">
        <span>个人信息</span>
      </div>
      <div style="display: flex; align-items: center;">
     
       
      </div>
    </div>

    <!-- 个人信息卡片 -->
    <div class="profile-section">
      <div class="profile-card">
        <div class="profile-info">
          <div class="profile-name">{{ userInfo.realName }}</div>
          <div class="profile-details">
            <div class="profile-item">
              <span class="profile-label">📱 手机号：</span>
              <span class="profile-value">{{ userInfo.phone }}</span>
            </div>
            <div v-if="userInfo.username" class="profile-item">
              <span class="profile-label">👤 用户名：</span>
              <span class="profile-value">{{ userInfo.username }}</span>
            </div>
          </div>
        </div>
      
      </div>
    </div>

    <!-- 快捷操作 -->
    <div class="actions-section">
      <div class="section-title">
        <span class="title-icon">⚡</span>
        <span>快捷操作</span>
      </div>
      <div class="actions-grid">
        <div class="action-card-wrapper">
          <div
            class="action-card primary-action"
            @click="handleQuickAction('/portal/passengers/view', '搜索航班')"
            :class="{ 'loading': actionLoading === 'search' }"
          >
            <div class="action-icon">🔍</div>
            <div class="action-content">
              <div class="action-title">预定航班</div>
              <div class="action-desc">查找并预订机票</div>
            </div>
            <div v-if="actionLoading === 'search'" class="action-loading">加载中...</div>
          </div>
        </div>
        <div
          class="action-card order-action"
          @click="handleQuickAction('/portal/orders', '我的订单')"
          :class="{ 'loading': actionLoading === 'orders' }"
        >
          <div class="action-icon">📋</div>
          <div class="action-content">
            <div class="action-title">我的订单</div>
            <div class="action-desc">查看所有订单记录</div>
          </div>
          <div v-if="actionLoading === 'orders'" class="action-loading">加载中...</div>
        </div>
        <div
          class="action-card order-action"
          @click="handleQuickAction('/portal/orders', '一站式机票管理')"
          :class="{ 'loading': actionLoading === 'unifiedTicket' }"
        >
          <div class="action-icon">🎫</div>
          <div class="action-content">
            <div class="action-title">改签/退订管理</div>
            <div class="action-desc">改签、取消一键入口</div>
          </div>
          <div v-if="actionLoading === 'unifiedTicket'" class="action-loading">加载中...</div>
        </div>
        <div
          class="action-card points-action"
          @click="handleQuickAction('/portal/passengers/points-coupons', '积分优惠')"
          :class="{ 'loading': actionLoading === 'points' }"
        >
          <div class="action-icon">⭐</div>
          <div class="action-content">
            <div class="action-title">积分与优惠券</div>
            <div class="action-desc">查看积分和优惠券</div>
          </div>
          <div v-if="actionLoading === 'points'" class="action-loading">加载中...</div>
        </div>
        <div
          class="action-card special-action"
          @click="goToSpecialPassengerPage"
          :class="{ 'loading': actionLoading === 'special' }"
        >
          <div class="action-icon">♿</div>
          <div class="action-content">
            <div class="action-title">重点旅客预约</div>
            <div class="action-desc">申请特殊服务协助</div>
          </div>
          <div v-if="actionLoading === 'special'" class="action-loading">加载中...</div>
        </div>
        <div
          class="action-card special-action"
          @click="handleQuickAction('/portal/passengers/baggage', '行李管理')"
          :class="{ 'loading': actionLoading === 'baggage' }"
        >
          <div class="action-icon">🧳</div>
          <div class="action-content">
            <div class="action-title">行李管理</div>
            <div class="action-desc">登记、查询和管理行李</div>
          </div>
          <div v-if="actionLoading === 'baggage'" class="action-loading">加载中...</div>
        </div>
        <div
          class="action-card special-action"
          @click="handleQuickAction('/portal/passengers/print-tickets', '打印机票')"
          :class="{ 'loading': actionLoading === 'print' }"
        >
          <div class="action-icon">🖨️</div>
          <div class="action-content">
            <div class="action-title">打印机票</div>
            <div class="action-desc">进入机票打印页面</div>
          </div>
          <div v-if="actionLoading === 'print'" class="action-loading">加载中...</div>
        </div>
      </div>
    </div>
    
    

    <!-- 待出行航班 -->
    <div class="flights-section" ref="flightsSectionRef">
      <div class="section-title">
       
      </div>
      <div class="flights-grid">
       
      </div>
    </div>

    <!-- 订单统计 -->
    <div class="stats-section">
      <div class="section-title">
        <span class="title-icon">📊</span>
        <span>订单统计</span>
      </div>
      <div class="stats-grid">
        <div class="stat-card revenue-card">
          <div class="stat-icon">💰</div>
          <div class="stat-content">
            <div class="stat-value">¥{{ formatNumber(stats.totalSpent) }}</div>
            <div class="stat-label">总消费</div>
          </div>
        </div>
        <div class="stat-card average-card">
          <div class="stat-icon">📈</div>
          <div class="stat-content">
            <div class="stat-value">¥{{ formatNumber(stats.averageSpent) }}</div>
            <div class="stat-label">平均订单</div>
          </div>
        </div>
        <div class="stat-card growth-card">
          <div class="stat-icon">📉</div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.orderGrowth }}%</div>
            <div class="stat-label">订单增长</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 数据可视化 -->
    <div class="visualization-section">
      <div class="section-title">
        <span class="title-icon">📈</span>
        <span>数据可视化</span>
      </div>
      <div class="charts-grid">
        <!-- 消费趋势 / 月度对比切换 -->
        <div class="chart-card">
          <div class="chart-header combined-chart-header">
            <div class="chart-header-text">
              <h3>{{ chartTitle }}</h3>
              <span class="chart-period">{{ chartPeriod }}</span>
            </div>
            <div class="chart-toggle">
              <button 
                class="toggle-btn" 
                :class="{ active: chartView === 'trend' }"
                @click="chartView = 'trend'"
              >
                消费趋势
              </button>
              <button 
                class="toggle-btn" 
                :class="{ active: chartView === 'comparison' }"
                @click="chartView = 'comparison'"
              >
                月度对比
              </button>
            </div>
          </div>
          <div class="chart-container">
            <transition name="chart-fade" mode="out-in">
              <div :key="chartView">
                <div v-if="chartView === 'trend'" class="bar-chart">
                  <div 
                    v-for="(item, index) in spendingTrend" 
                    :key="`trend-${index}`"
                    class="bar-item"
                  >
                    <div class="bar-wrapper">
                      <div 
                        class="bar" 
                        :style="{ height: `${(item.value / maxSpending) * 100}%` }"
                        :title="`${item.month}: ¥${item.value}`"
                      ></div>
                    </div>
                    <div class="bar-label">{{ item.month }}</div>
                    <div class="bar-value">¥{{ item.value }}</div>
                  </div>
                </div>
                <div v-else class="comparison-chart">
                  <div class="comparison-bars">
                    <div 
                      v-for="(item, index) in monthlyComparison" 
                      :key="`comparison-${index}`"
                      class="comparison-item"
                    >
                      <div class="comparison-month">{{ item.month }}</div>
                      <div class="comparison-bars-wrapper">
                        <div 
                          class="comparison-bar orders" 
                          :style="{ height: `${(item.orders / maxOrders) * 100}%` }"
                          :title="`订单: ${item.orders}`"
                        ></div>
                        <div 
                          class="comparison-bar spending" 
                          :style="{ height: `${(item.spending / maxSpending) * 100}%` }"
                          :title="`消费: ¥${item.spending}`"
                        ></div>
                      </div>
                    </div>
                  </div>
                  <div class="comparison-legend">
                    <div class="legend-item">
                      <span class="legend-color orders"></span>
                      <span>订单数</span>
                    </div>
                    <div class="legend-item">
                      <span class="legend-color spending"></span>
                      <span>消费额</span>
                    </div>
                  </div>
                </div>
              </div>
            </transition>
          </div>
        </div>

        <!-- 订单状态分布 -->
        <div class="chart-card">
          <div class="chart-header">
            <h3>订单状态分布</h3>
            <span class="chart-period">全部订单</span>
          </div>
          <div class="chart-container">
            <div class="pie-chart">
              <svg viewBox="0 0 200 200" class="pie-svg">
                <circle
                  cx="100"
                  cy="100"
                  r="80"
                  fill="none"
                  stroke="rgba(255, 255, 255, 0.1)"
                  stroke-width="40"
                />
                <circle
                  v-for="(segment, index) in orderStatusSegments"
                  :key="index"
                  cx="100"
                  cy="100"
                  r="80"
                  fill="none"
                  :stroke="segment.color"
                  stroke-width="40"
                  :stroke-dasharray="`${segment.percentage * 502.4} 502.4`"
                  :stroke-dashoffset="getPieOffset(index)"
                  transform="rotate(-90 100 100)"
                  class="pie-segment"
                />
              </svg>
              <div class="pie-legend">
                <div 
                  v-for="(segment, index) in orderStatusSegments" 
                  :key="index"
                  class="legend-item"
                >
                  <span class="legend-color" :style="{ backgroundColor: segment.color }"></span>
                  <span class="legend-label">{{ segment.label }}</span>
                  <span class="legend-value">{{ segment.count }} ({{ segment.percentage }}%)</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 航线偏好 -->
        <div class="chart-card">
          <div class="chart-header">
            <h3>热门航线</h3>
            <span class="chart-period">最常飞</span>
          </div>
          <div class="chart-container">
            <div class="route-list">
              <div 
                v-for="(route, index) in popularRoutes" 
                :key="index"
                class="route-item"
              >
                <div class="route-info">
                  <span class="route-name">{{ route.route }}</span>
                  <span class="route-count">{{ route.count }} 次</span>
                </div>
                <div class="route-bar">
                  <div 
                    class="route-progress" 
                    :style="{ width: `${(route.count / maxRouteCount) * 100}%`, backgroundColor: route.color }"
                  ></div>
                </div>
              </div>
            </div>
          </div>
        </div>

      </div>
    </div>

    <!-- 更多服务 -->
    <div class="services-section">
      <div class="section-title">
        <span class="title-icon">🎯</span>
        <span>更多服务</span>
      </div>
      <div class="services-grid">
        <a
          class="service-card actionable"
          href="https://www.coze.cn/store/agent/7589114565770641442?bot_id=true"
          target="_blank"
          rel="noopener noreferrer"
          style="text-decoration: none; color: inherit;"
        >
          <div class="service-icon">💬</div>
          <div class="service-title">客服支持</div>
          <div class="service-desc">联系我们获取帮助</div>
          <span class="service-link">进入客服对话 &gt;</span>
        </a>
        <div class="service-card actionable" @click="handleService('travel-info')">
          <div class="service-icon">📱</div>
          <div class="service-title">出行信息</div>
          <div class="service-desc">查看航班动态和天气</div>
          <span class="service-link">查看天气预报 ></span>
        </div>
        <div
          class="service-card"
          :class="{ expanded: inlineService === 'luggage-rules' }"
          @click="handleService('luggage-rules')"
        >
          <div class="service-icon">🧳</div>
          <div class="service-title">行李规定</div>
          <div class="service-desc">了解行李托运和携带规定</div>
          <transition name="service-inline">
            <div class="service-inline" v-if="inlineService === 'luggage-rules'">
              <p>随身行李限重 5kg，尺寸不超过 20×40×55cm</p>
              <p>超重行李按 15 元/kg 收费，建议提前在线预购</p>
              <p>禁止携带易燃易爆物品及 100ml 以上液体（需托运）</p>
            </div>
          </transition>
        </div>
        <div
          class="service-card"
          :class="{ expanded: inlineService === 'airport-guide' }"
          @click="handleService('airport-guide')"
        >
          <div class="service-icon">✈️</div>
          <div class="service-title">机场指南</div>
          <div class="service-desc">查看机场位置和交通信息</div>
          <transition name="service-inline">
            <div class="service-inline" v-if="inlineService === 'airport-guide'">
              <p v-for="line in inlineServiceTexts['airport-guide']" :key="line">{{ line }}</p>
            </div>
          </transition>
        </div>
        <div
          class="service-card"
          :class="{ expanded: inlineService === 'refund-policy' }"
          @click="handleService('refund-policy')"
        >
          <div class="service-icon">📋</div>
          <div class="service-title">退改签政策</div>
          <div class="service-desc">了解退改签规则和费用</div>
          <transition name="service-inline">
            <div class="service-inline" v-if="inlineService === 'refund-policy'">
              <p v-for="line in inlineServiceTexts['refund-policy']" :key="line">{{ line }}</p>
            </div>
          </transition>
        </div>
      </div>
    </div>

    <!-- 常见问题 -->
    <div class="faq-section">
      <div class="section-title">
        <span class="title-icon">❓</span>
        <span>常见问题</span>
      </div>
      <div class="faq-list">
        <div class="faq-item" @click="toggleFaq(0)">
          <div class="faq-question">
            <span class="faq-icon">+</span>
            <span>如何改签或取消机票？</span>
          </div>
          <div v-if="expandedFaq === 0" class="faq-answer">
            进入"机票管理"页面，选择要改签或取消的机票，按照提示完成操作。改签可在出发前72小时内进行，取消退款需根据票型规则决定。
          </div>
        </div>
        <div class="faq-item" @click="toggleFaq(1)">
          <div class="faq-question">
            <span class="faq-icon">+</span>
            <span>电子机票何时发放？</span>
          </div>
          <div v-if="expandedFaq === 1" class="faq-answer">
            订票成功后，电子机票会立即发送到您的注册邮箱。请在出发前24小时内下载并打印。
          </div>
        </div>
        <div class="faq-item" @click="toggleFaq(2)">
          <div class="faq-question">
            <span class="faq-icon">+</span>
            <span>如何查询航班状态？</span>
          </div>
          <div v-if="expandedFaq === 2" class="faq-answer">
            您可以在"出行信息"服务中查看航班动态，或通过航班号在首页搜索航班，实时了解航班延误、取消等状态信息。
          </div>
        </div>
        <div class="faq-item" @click="toggleFaq(3)">
          <div class="faq-question">
            <span class="faq-icon">+</span>
            <span>行李托运有什么规定？</span>
          </div>
          <div v-if="expandedFaq === 3" class="faq-answer">
            超重部分按每公斤收取费用。随身携带行李限重5公斤，尺寸不超过20×40×55厘米。具体规定请查看"行李规定"服务。
          </div>
        </div>
        <div class="faq-item" @click="toggleFaq(4)">
          <div class="faq-question">
            <span class="faq-icon">+</span>
            <span>如何联系客服？</span>
          </div>
          <div v-if="expandedFaq === 4" class="faq-answer">
            您可以通过"客服支持"服务查看联系方式，或直接拨打客服热线400-888-8888，工作时间：周一至周日 9:00-18:00。也可发送邮件至3351402913@qq.com。
          </div>
        </div>
      </div>
    </div>
  </PassengerLayout>

  <!-- 确认对话框 -->
  <ModalPrompt
    v-model="confirmDialog.visible"
    :title="confirmDialog.title"
    :message="confirmDialog.message"
    type="confirm"
    :show-cancel="true"
    confirm-text="确认"
    cancel-text="取消"
    :anchor="confirmAnchor"
    placement="top"
    :offset="20"
    :adaptive-position="Boolean(confirmAnchor)"
    anchor-alignment="center"
    @confirm="confirmDialog.onConfirm"
    :anchor-offset-x="1000"

  />

  <!-- 信息对话框 -->
  <ModalPrompt
    v-model="infoDialog.visible"
    :title="infoDialog.title"
    :message="infoDialog.message"
    type="info"
    confirm-text="知道了"
    :anchor="infoAnchor"
    placement="top"
    :offset="20"
    :adaptive-position="Boolean(infoAnchor)"
    anchor-alignment="start"
    :anchor-offset-x="1000"
  />

  <!-- 成功提示框 -->
  <ModalPrompt
    v-model="successDialog.visible"
    :title="successDialog.title"
    :message="successDialog.message"
    type="success"
    confirm-text="好的"
    :anchor="successAnchor"
    placement="top"
    :offset="20"
    :adaptive-position="Boolean(successAnchor)"
    anchor-alignment="center"
    @confirm="handleSuccessConfirm"
  />

  <!-- 重点旅客预约弹窗 -->
  <div v-if="specialPassengerDialog.visible" class="reschedule-overlay" @click.self="closeSpecialPassengerDialog">
    <div class="reschedule-modal special-passenger-modal">
      <div class="reschedule-header">
        <div>
          <p class="reschedule-label">重点旅客预约</p>
          <h3>申请特殊服务协助</h3>
        </div>
        <button class="reschedule-close" @click="closeSpecialPassengerDialog">×</button>
      </div>

      <div class="special-passenger-form">
        <!-- 订单选择 -->
        <div class="form-group">
          <label class="form-label">订单选择 <span class="required">*</span></label>
          <div class="select-wrapper" @click="toggleOrderSelector">
            <input 
              type="text" 
              readonly
              :value="selectedOrder ? (selectedOrder.orderNo || selectedOrder.id) : '请选择乘车订单'"
              class="form-input select-input"
              placeholder="请选择乘车订单"
            />
            <span class="select-arrow">></span>
          </div>
          <!-- 订单选择下拉 -->
          <div v-if="showOrderSelector" class="order-selector-dropdown">
            <div 
              v-for="order in availableOrders" 
              :key="order.id"
              class="order-option"
              @click="selectOrder(order)"
            >
              <div class="order-option-main">
                <strong>订单 #{{ order.id }}</strong>
                <span>{{ order.route }}</span>
              </div>
              <div class="order-option-meta">
                <span>{{ order.time }}</span>
                <span>¥{{ order.amount || 0 }}</span>
              </div>
            </div>
            <div v-if="availableOrders.length === 0" class="order-option empty">
              暂无可用订单
            </div>
          </div>
        </div>

        <!-- 联系电话 -->
        <div class="form-group">
          <label class="form-label">联系电话 <span class="required">*</span></label>
          <div class="phone-input-wrapper">
            <input 
              type="tel" 
              v-model="specialPassengerForm.phone"
              class="form-input phone-input"
              placeholder="请填写手机号"
              maxlength="11"
            />
          </div>
        </div>

        <!-- 旅客类型 -->
        <div class="form-group" style="position: relative;">
          <label class="form-label">旅客类型 <span class="required">*</span></label>
          <div class="select-wrapper" @click.stop="togglePassengerTypeSelector">
            <input 
              type="text" 
              readonly
              :value="selectedPassengerType || '请选择旅客类型'"
              class="form-input select-input"
              placeholder="请选择旅客类型"
            />
            <span class="select-arrow">></span>
            <!-- 旅客类型选择下拉 -->
            <div v-if="showPassengerTypeSelector" class="passenger-type-dropdown" @click.stop>
              <div 
                v-for="type in passengerTypes" 
                :key="type.value"
                class="passenger-type-option"
                :class="{ active: specialPassengerForm.passengerType === type.value }"
                @click.stop="selectPassengerType(type.value)"
              >
                <span class="radio-icon" :class="{ checked: specialPassengerForm.passengerType === type.value }"></span>
                <span>{{ type.label }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 提示信息 -->
        <div class="info-banner">
          如发车站或到站与实际不符,可进行修改。
        </div>

        <!-- 出发机场 -->
        <div class="form-group">
          <label class="form-label">出发机场 <span class="required">*</span></label>
          <input 
            type="text" 
            v-model="specialPassengerForm.departureAirport"
            class="form-input"
            placeholder="请填写出发机场"
          />
        </div>

        <!-- 进站服务需求 -->
        <div class="form-group">
          <label class="form-label">进站服务需求</label>
          <div class="checkbox-group">
            <label class="checkbox-item">
              <input 
                type="checkbox" 
                v-model="specialPassengerForm.entryServices.selfEquipment"
                class="checkbox-input"
              />
              <span class="checkbox-label">自备器械</span>
            </label>
            <label class="checkbox-item">
              <input 
                type="checkbox" 
                v-model="specialPassengerForm.entryServices.priorityEntry"
                class="checkbox-input"
              />
              <span class="checkbox-label">优先进站</span>
            </label>
            <label class="checkbox-item">
              <input 
                type="checkbox" 
                v-model="specialPassengerForm.entryServices.wheelchair"
                class="checkbox-input"
              />
              <span class="checkbox-label">提供轮椅</span>
            </label>
            <label class="checkbox-item">
              <input 
                type="checkbox" 
                v-model="specialPassengerForm.entryServices.stretcher"
                class="checkbox-input"
              />
              <span class="checkbox-label">提供担架</span>
            </label>
          </div>
        </div>

        <!-- 到达机场 -->
        <div class="form-group">
          <label class="form-label">到达机场 <span class="required">*</span></label>
          <input 
            type="text" 
            v-model="specialPassengerForm.arrivalAirport"
            class="form-input"
            placeholder="请填写到达机场"
          />
        </div>

        <!-- 出站服务需求 -->
        <div class="form-group">
          <label class="form-label">出站服务需求</label>
          <div class="checkbox-group">
            <label class="checkbox-item">
              <input 
                type="checkbox" 
                v-model="specialPassengerForm.exitServices.selfEquipment"
                class="checkbox-input"
              />
              <span class="checkbox-label">自备器械</span>
            </label>
            <label class="checkbox-item">
              <input 
                type="checkbox" 
                v-model="specialPassengerForm.exitServices.convenientExit"
                class="checkbox-input"
              />
              <span class="checkbox-label">便利出站</span>
            </label>
            <label class="checkbox-item">
              <input 
                type="checkbox" 
                v-model="specialPassengerForm.exitServices.wheelchair"
                class="checkbox-input"
              />
              <span class="checkbox-label">提供轮椅</span>
            </label>
            <label class="checkbox-item">
              <input 
                type="checkbox" 
                v-model="specialPassengerForm.exitServices.stretcher"
                class="checkbox-input"
              />
              <span class="checkbox-label">提供担架</span>
            </label>
          </div>
        </div>

        <!-- 情况描述 -->
        <div class="form-group">
          <label class="form-label">情况描述 <span class="optional">(非必填)</span></label>
          <textarea
            v-model="specialPassengerForm.description"
            class="form-textarea"
            placeholder="请简述您需要的服务内容"
            rows="4"
          ></textarea>
        </div>

        <p v-if="specialPassengerDialog.error" class="reschedule-error">{{ specialPassengerDialog.error }}</p>

        <div class="reschedule-actions">
          <button class="reschedule-btn ghost" @click="closeSpecialPassengerDialog">取消</button>
          <button
            class="reschedule-btn primary"
            :disabled="specialPassengerDialog.submitting"
            @click="submitSpecialPassengerRequest"
          >
            <span v-if="specialPassengerDialog.submitting">提交中...</span>
            <span v-else>提交申请</span>
          </button>
        </div>
      </div>
    </div>
  </div>

  <!-- 改签弹窗 -->
  <div v-if="rescheduleDialog.visible" class="reschedule-overlay" @click.self="closeRescheduleDialog">
    <div class="reschedule-modal">
      <div class="reschedule-header">
        <div>
          <p class="reschedule-label">正在改签</p>
          <h3>{{ rescheduleDialog.flight?.origin }} → {{ rescheduleDialog.flight?.destination }}</h3>
        </div>
        <button class="reschedule-close" @click="closeRescheduleDialog">×</button>
      </div>

      <div class="current-flight" v-if="rescheduleDialog.flight">
        <div>
          <p class="flight-date">{{ rescheduleDialog.flight.date }}</p>
          <p class="flight-time">
            {{ rescheduleDialog.flight.departureTime }} → {{ rescheduleDialog.flight.arrivalTime }}
          </p>
        </div>
        <div class="flight-meta">
          <span>航班号：{{ rescheduleDialog.flight.flightNumber }}</span>
          <span>当前状态：{{ rescheduleDialog.flight.status }}</span>
        </div>
      </div>

      <div class="reschedule-meta" v-if="rescheduleDialog.flight">
        <div class="meta-block">
          <p class="reschedule-label">原航程</p>
          <p class="meta-value">{{ rescheduleDialog.flight.origin }} → {{ rescheduleDialog.flight.destination }}</p>
          <small class="meta-tip">{{ rescheduleDialog.flight.date }} {{ rescheduleDialog.flight.departureTime }} 起飞</small>
        </div>
        <div class="meta-block">
          <p class="reschedule-label">改签至</p>
          <p class="meta-value">
            <span v-if="selectedRescheduleOption">
              {{ selectedRescheduleOption.date }} {{ selectedRescheduleOption.departureTime }} → {{ selectedRescheduleOption.arrivalTime }}
            </span>
            <span v-else>请选择新的航班</span>
          </p>
          <small class="meta-tip">
            舱位：{{ selectedRescheduleOption?.cabin || '待选择' }}
          </small>
        </div>
        <div class="meta-block">
          <p class="reschedule-label">改签手续费</p>
          <p class="meta-value highlight">¥{{ rescheduleDialog.serviceFee }}</p>
          <small class="meta-tip">起点/终点不变，按票规收取固定手续费</small>
        </div>
        <div class="meta-block">
          <p class="reschedule-label">预计补差</p>
          <p class="meta-value" :class="{ rise: (selectedRescheduleOption?.priceDiff || 0) > 0 }">
            <span v-if="selectedRescheduleOption"> 
              {{ selectedRescheduleOption.priceDiff >= 0 ? '+' : '-' }}¥{{ Math.abs(selectedRescheduleOption.priceDiff) }}
            </span>
            <span v-else>待选择</span>
          </p>
        </div>
        <div class="meta-block total">
          <p class="reschedule-label">预计总费用</p>
          <p class="meta-value highlight">¥{{ rescheduleTotal }}</p>
          <small class="meta-tip">以航空运营结算为准</small>
        </div>
      </div>

      <div class="option-list">
        <p class="reschedule-label">可改签班次（席位实时锁定 5 分钟）</p>
        <div class="option-grid">
          <button
            v-for="option in rescheduleDialog.options"
            :key="option.id"
            class="option-card"
            :class="{ active: option.id === rescheduleDialog.selectedOptionId, full: option.seats <= 0 }"
            :disabled="option.seats <= 0"
            @click="rescheduleDialog.selectedOptionId = option.id"
          >
            <div class="option-main">
              <strong>{{ option.date }}</strong>
              <span>{{ option.departureTime }} → {{ option.arrivalTime }}</span>
            </div>
            <div class="option-meta">
              <span>{{ option.cabin }}</span>
              <span v-if="option.priceDiff === 0">免补差</span>
              <span v-else :class="{ rise: option.priceDiff > 0 }">
                {{ option.priceDiff > 0 ? '+' : '' }}¥{{ Math.abs(option.priceDiff) }}
              </span>
            </div>
            <div class="option-seat">
              剩余 {{ option.seats }} 座
            </div>
          </button>
        </div>
      </div>

      <label class="reschedule-label" for="reschedule-reason">改签原因</label>
      <textarea
        id="reschedule-reason"
        v-model="rescheduleDialog.reason"
        class="reschedule-textarea"
        placeholder="简单描述原因用于审核，例如“客户行程调整，需要提前一天出发”"
      ></textarea>
      <p v-if="rescheduleDialog.error" class="reschedule-error">{{ rescheduleDialog.error }}</p>

      <div class="reschedule-actions">
        <button class="reschedule-btn ghost" @click="closeRescheduleDialog">取消</button>
        <button
          class="reschedule-btn primary"
          :disabled="rescheduleDialog.submitting"
          @click="submitReschedule"
        >
          <span v-if="rescheduleDialog.submitting">提交中...</span>
          <span v-else>确认改签</span>
        </button>
      </div>
    </div>
  </div>

  <!-- 取消机票弹窗 -->
  <div v-if="cancelDialog.visible" class="reschedule-overlay" @click.self="closeCancelDialog">
    <div class="reschedule-modal">
      <div class="reschedule-header">
        <div>
          <p class="reschedule-label">正在取消</p>
          <h3>{{ cancelDialog.flight?.origin }} → {{ cancelDialog.flight?.destination }}</h3>
        </div>
        <button class="reschedule-close" @click="closeCancelDialog">×</button>
      </div>

      <div class="current-flight" v-if="cancelDialog.flight">
        <div>
          <p class="flight-date">{{ cancelDialog.flight.date }}</p>
          <p class="flight-time">
            {{ cancelDialog.flight.departureTime }} → {{ cancelDialog.flight.arrivalTime }}
          </p>
        </div>
        <div class="flight-meta">
          <span>航班号：{{ cancelDialog.flight.flightNumber }}</span>
          <span>当前状态：{{ cancelDialog.flight.status }}</span>
        </div>
      </div>

      <div class="reschedule-meta">
        <div class="meta-block">
          <p class="reschedule-label">航班号</p>
          <p class="meta-value">{{ cancelDialog.flight?.flightNumber || '未提供' }}</p>
        </div>
        <div class="meta-block">
          <p class="reschedule-label">起飞时间</p>
          <p class="meta-value">
            <span v-if="cancelDialog.flight">
              {{ cancelDialog.flight.date }} {{ cancelDialog.flight.departureTime }}
            </span>
            <span v-else>未提供</span>
          </p>
        </div>
        <div class="meta-block">
          <p class="reschedule-label">机票价格</p>
          <p class="meta-value">
            <span v-if="cancelDialog.ticketPrice > 0">¥{{ cancelDialog.ticketPrice }}</span>
            <span v-else>未提供</span>
          </p>
        </div>
        <div class="meta-block">
          <p class="reschedule-label">取消手续费</p>
          <p class="meta-value highlight">¥{{ cancelDialog.fee }}</p>
          <small class="meta-tip">起飞前 24 小时内可能提高，最终以航司结算为准</small>
        </div>
        <div class="meta-block total">
          <p class="reschedule-label">退款提示</p>
          <p class="meta-value">退款金额以航空运营规则核算</p>
          <small class="meta-tip">参考手续费已预置到请求体</small>
        </div>
      </div>

      <label class="reschedule-label" for="cancel-reason">取消原因</label>
      <div class="reason-tags">
        <button
          v-for="tag in cancelReasons"
          :key="tag"
          type="button"
          class="reason-tag"
          :class="{ active: cancelDialog.reasonCategory === tag }"
          @click="cancelDialog.reasonCategory = tag"
        >
          {{ tag }}
        </button>
      </div>
      <textarea
        id="cancel-reason"
        v-model="cancelDialog.reasonDetail"
        class="reschedule-textarea"
        placeholder="请补充说明，例如具体出差行程调整或审批编号"
      ></textarea>
      <p v-if="cancelDialog.error" class="reschedule-error">{{ cancelDialog.error }}</p>

      <div class="reschedule-actions">
        <button class="reschedule-btn ghost" @click="closeCancelDialog">返回</button>
        <button
          class="reschedule-btn primary"
          :disabled="cancelDialog.submitting"
          @click="submitCancel"
        >
          <span v-if="cancelDialog.submitting">提交中...</span>
          <span v-else>确认取消</span>
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import PassengerLayout from '../layout/PassengerLayout.vue'
import ModalPrompt from '../ModalPrompt.vue'
import store from '../../services/store'
import { passengerApi, orderApi, specialServiceRequestApi } from '../../services/api'
import TicketQuickEntry from '../../components/TicketQuickEntry.vue'
import MobilePreviewToggle from '../MobilePreviewToggle.vue'

const router = useRouter()

// 加载状态
// 手机端预览开关（将会在 documentElement 上添加/移除 class `mobile-preview`）
const isMobilePreview = ref(false)
const toggleMobilePreview = () => {
  isMobilePreview.value = !isMobilePreview.value
  try {
    if (isMobilePreview.value) {
      // 打开预览：添加样式并跳转到预览页面
      document.documentElement.classList.add('mobile-preview')
      // 跳转到专用的手机预览路由（若路由不存在可按需修改）
      router.push('/portal/passengers/mobile-preview')
    } else {
      // 关闭预览：移除样式，并在当前位于预览页时返回主页面
      document.documentElement.classList.remove('mobile-preview')
      if (router.currentRoute.value && router.currentRoute.value.path === '/portal/passengers/mobile-preview') {
        router.push('/portal/passengers')
      }
    }
    localStorage.setItem('mobile_preview', String(isMobilePreview.value))
  } catch (e) {
    console.warn('切换手机预览失败:', e)
  }
}

// 加载状态
const loading = reactive({
  profile: false,
  statistics: false,
  flights: false
})

// 用户信息 - 从store获取
const userInfo = computed(() => {
  const info = store.userState.userInfo || {}
  return {
    realName: info.realName || '游客',
    email: info.email || '',
    phone: info.phone || '未设置',
    username: info.username || '',
    idCard: info.idCard || ''
  }
})

// 统计数据
const stats = reactive({
  totalOrders: 0,
  upcomingFlights: 0,
  completedFlights: 0,
  totalSpent: 0,
  averageSpent: 0,
  orderGrowth: 0
})

// 消费趋势数据（近6个月）
const spendingTrend = reactive([
  { month: '6月', value: 3200 },
  { month: '7月', value: 4500 },
  { month: '8月', value: 3800 },
  { month: '9月', value: 5200 },
  { month: '10月', value: 4800 },
  { month: '11月', value: 5500 }
])

// 订单状态分布
interface OrderStatusSegment {
  label: string
  count: number
  percentage: number
  color: string
}
const orderStatusSegments = reactive<OrderStatusSegment[]>([])

// 热门航线
interface PopularRoute {
  route: string
  count: number
  color: string
}
const popularRoutes = reactive<PopularRoute[]>([])

// 月度对比数据
const monthlyComparison = reactive([
  { month: '6月', orders: 2, spending: 3200 },
  { month: '7月', orders: 3, spending: 4500 },
  { month: '8月', orders: 2, spending: 3800 },
  { month: '9月', orders: 4, spending: 5200 },
  { month: '10月', orders: 3, spending: 4800 },
  { month: '11月', orders: 4, spending: 5500 }
])

// 图表切换
const chartView = ref<'trend' | 'comparison'>('trend')
const chartTitle = computed(() => chartView.value === 'trend' ? '消费趋势' : '月度对比')
const chartPeriod = computed(() => chartView.value === 'trend' ? '近6个月' : '订单数 vs 消费额')

// 计算最大值用于图表比例
const maxSpending = computed(() => Math.max(...spendingTrend.map(item => item.value), ...monthlyComparison.map(item => item.spending)))
const maxOrders = computed(() => Math.max(...monthlyComparison.map(item => item.orders)))
const maxRouteCount = computed(() => Math.max(...popularRoutes.map(route => route.count)))

// 计算饼图偏移量
const getPieOffset = (index: number) => {
  let offset = 0
  for (let i = 0; i < index; i++) {
    const segment = orderStatusSegments[i]
    offset += (segment?.percentage || 0) * 5.024
  }
  return -offset
}

// 待出行航班
type FlightInfo = {
  id: number
  date: string
  departureTime: string
  arrivalTime: string
  origin: string
  destination: string
  flightNumber: string
  status: string
}

const upcomingFlights = reactive<FlightInfo[]>([])

const ticketDetailCache = reactive<Record<number, string>>({})

// 航班实时查询相关（用于预定航班快捷入口）
const flightQueryFlights = ref<any[]>([])
const flightQueryRefreshTimer = ref<number | null>(null)

// 解析路线字段，提取出发机场和到达机场
const parseRoute = (route: string) => {
  if (!route) return { origin: '', destination: '' }
  const parts = route.split('→').map(s => s.trim())
  return {
    origin: parts[0] || '',
    destination: parts[1] || ''
  }
}

// 格式化起飞时间 - 直接使用数据库原始时间，不做任何转换
const formatDepartureTime = (timeStr: string) => {
  if (!timeStr) return ''
  try {
    // 直接使用原始字符串，只做最简单的格式化（去掉秒数）
    const cleaned = String(timeStr).trim()
    // 如果包含秒数，去掉秒数部分；如果包含T，替换为空格
    // 格式：2025-12-09 18:30:00 -> 2025-12-09 18:30
    // 格式：2025-12-09T18:30:00 -> 2025-12-09 18:30
    let result = cleaned.replace('T', ' ')
    // 去掉秒数部分（如果存在）
    if (result.match(/^\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}/)) {
      result = result.substring(0, 16)
    }
    // 如果已经是 YYYY-MM-DD HH:mm 格式，直接返回
    if (result.match(/^\d{4}-\d{2}-\d{2} \d{2}:\d{2}$/)) {
      return result
    }
    // 如果长度足够，截取前16个字符
    if (result.length >= 16) {
      return result.substring(0, 16)
    }
    return result
  } catch {
    return String(timeStr)
  }
}

// 检查时间差是否小于24小时 - 直接使用数据库时间，不进行时区转换
const isWithin24Hours = (departureTimeStr: string) => {
  if (!departureTimeStr) return false
  try {
    // 直接解析时间字符串为本地时间，不进行时区转换
    const cleaned = departureTimeStr.trim()
    const match = cleaned.match(/^(\d{4})-(\d{2})-(\d{2})[\sT](\d{2}):(\d{2})(?::(\d{2}))?/)
    if (match && match[1] && match[2] && match[3] && match[4] && match[5]) {
      const year = match[1]
      const month = match[2]
      const day = match[3]
      const hours = match[4]
      const minutes = match[5]
      const seconds = match[6]
      // 使用本地时间创建Date对象，不进行时区转换
      const departureTime = new Date(
        parseInt(year),
        parseInt(month) - 1,
        parseInt(day),
        parseInt(hours),
        parseInt(minutes),
        seconds ? parseInt(seconds) : 0
      )
      if (isNaN(departureTime.getTime())) return false
      const now = new Date()
      const diffMs = departureTime.getTime() - now.getTime()
      const diffHours = diffMs / (1000 * 60 * 60)
      // 起飞时间在未来，且时间差小于24小时
      return diffMs > 0 && diffHours < 24 && diffHours >= 0
    }
    // 如果格式不匹配，尝试使用Date解析（兼容其他格式）
    const departureTime = new Date(departureTimeStr)
    if (isNaN(departureTime.getTime())) return false
    const now = new Date()
    const diffMs = departureTime.getTime() - now.getTime()
    const diffHours = diffMs / (1000 * 60 * 60)
    return diffMs > 0 && diffHours < 24 && diffHours >= 0
  } catch {
    return false
  }
}

// 加载符合条件的航班（用于航班实时查询）
const loadFlightQueryFlights = async () => {
  try {
    // 获取所有订单，使用大的size值确保获取所有数据
    const firstPageResult = await orderApi.getOrders({ page: 0, size: 100 })
    
    if (!firstPageResult || !firstPageResult.orders) {
      flightQueryFlights.value = []
      return
    }
    
    if (firstPageResult.orders.length === 0 && firstPageResult.total > 0) {
      // 如果第一页为空但总数大于0，尝试获取所有页
      const total = firstPageResult.total || 0
      const pageSize = 10
      const totalPages = Math.ceil(total / pageSize)
      let allOrders: any[] = []
      
      for (let page = 0; page < totalPages; page++) {
        try {
          const pageResult = await orderApi.getOrders({ page, size: pageSize })
          if (pageResult && pageResult.orders && pageResult.orders.length > 0) {
            allOrders = [...allOrders, ...pageResult.orders]
          }
        } catch (e) {
          console.warn(`获取第${page + 1}页订单失败:`, e)
        }
      }
      
      if (allOrders.length === 0) {
        flightQueryFlights.value = []
        return
      }
      
      const filtered = allOrders
        .filter((order: any) => {
          const status = String(order.status || '').toLowerCase()
          const isTicketed = status === 'ticketed'
          const within24Hours = isWithin24Hours(order.departureTime)
          return isTicketed && within24Hours
        })
        .map((order: any) => {
          const routeInfo = parseRoute(order.route || '')
          return {
            id: order.id,
            passengerName: order.passengerName || '未知',
            originAirport: routeInfo.origin,
            destinationAirport: routeInfo.destination,
            departureTime: formatDepartureTime(order.departureTime)
          }
        })
      
      flightQueryFlights.value = filtered
      return
    }
    
    let allOrders = [...firstPageResult.orders]
    const total = firstPageResult.total || 0
    const pageSize = firstPageResult.size || 100
    
    // 如果数据超过一页，获取剩余页的数据
    if (total > pageSize) {
      const totalPages = Math.ceil(total / pageSize)
      for (let page = 1; page < totalPages; page++) {
        try {
          const pageResult = await orderApi.getOrders({ page, size: pageSize })
          if (pageResult && pageResult.orders) {
            allOrders = [...allOrders, ...pageResult.orders]
          }
        } catch (e) {
          console.warn(`获取第${page + 1}页订单失败:`, e)
        }
      }
    }
    
    const filtered = allOrders
      .filter((order: any) => {
        const status = String(order.status || '').toLowerCase()
        const isTicketed = status === 'ticketed'
        const within24Hours = isWithin24Hours(order.departureTime)
        return isTicketed && within24Hours
      })
      .map((order: any) => {
        const routeInfo = parseRoute(order.route || '')
        return {
          id: order.id,
          passengerName: order.passengerName || '未知',
          originAirport: routeInfo.origin,
          destinationAirport: routeInfo.destination,
          departureTime: formatDepartureTime(order.departureTime)
        }
      })
    
    flightQueryFlights.value = filtered
  } catch (error) {
    console.error('加载航班实时查询失败:', error)
    flightQueryFlights.value = []
  }
}

type RescheduleOption = {
  id: string
  date: string
  departureTime: string
  arrivalTime: string
  cabin: string
  seats: number
  priceDiff: number
}

const RESCHEDULE_SERVICE_FEE = 80

const rescheduleDialog = reactive({
  visible: false,
  flight: null as FlightInfo | null,
  options: [] as RescheduleOption[],
  selectedOptionId: '',
  reason: '',
  error: '',
  submitting: false,
  serviceFee: RESCHEDULE_SERVICE_FEE
})

const cancelDialog = reactive({
  visible: false,
  flight: null as FlightInfo | null,
  reasonCategory: '',
  reasonDetail: '',
  ticketPrice: 0,
  fee: 120,
  error: '',
  submitting: false
})

const cancelReasons = [
  '行程变化',
  '健康/疫情原因',
  '公司审批未通过',
  '航班衔接风险',
  '价格或舱位原因',
  '其他'
]

// 重点旅客预约相关
const specialPassengerDialog = reactive({
  visible: false,
  error: '',
  submitting: false
})

const specialPassengerForm = reactive({
  orderId: null as number | null,
  countryCode: '+86',
  phone: '',
  passengerType: '',
  departureAirport: '',
  arrivalAirport: '',
  entryServices: {
    selfEquipment: false,
    priorityEntry: false,
    wheelchair: false,
    stretcher: false
  },
  exitServices: {
    selfEquipment: false,
    convenientExit: false,
    wheelchair: false,
    stretcher: false
  },
  description: ''
})

const showOrderSelector = ref(false)
const showPassengerTypeSelector = ref(false)
const showDepartureAirportSelector = ref(false)
const showArrivalAirportSelector = ref(false)

const selectedOrder = ref<any>(null)
const availableOrders = ref<any[]>([])

const passengerTypes = [
  { value: 'elderly', label: '无陪伴年长旅客' },
  { value: 'pregnant', label: '无陪伴孕妇旅客' },
  { value: 'visual', label: '视觉障碍旅客' },
  { value: 'hearing', label: '听觉障碍旅客' },
  { value: 'wheelchair', label: '轮椅行动障碍旅客' },
  { value: 'stretcher', label: '担架 (车) 行动障碍旅客' },
  { value: 'guide_dog', label: '携带导盲犬旅客' }
]

const airports = [
  '北京首都国际机场', '上海浦东国际机场', '上海虹桥国际机场', 
  '广州白云国际机场', '深圳宝安国际机场', '成都双流国际机场',
  '西安咸阳国际机场', '杭州萧山国际机场', '南京禄口国际机场',
  '武汉天河国际机场', '长沙黄花国际机场', '郑州新郑国际机场'
]

const selectedPassengerType = computed(() => {
  const type = passengerTypes.find(t => t.value === specialPassengerForm.passengerType)
  return type ? type.label : ''
})

const selectedRescheduleOption = computed(() =>
  rescheduleDialog.options.find(option => option.id === rescheduleDialog.selectedOptionId) || null
)

const rescheduleTotal = computed(() => {
  const option = selectedRescheduleOption.value
  const diff = option ? Math.max(0, option.priceDiff) : 0
  return rescheduleDialog.serviceFee + diff
})

// 错误处理
const handleApiError = (error: any, defaultMessage: string = '操作失败') => {
  console.error('API错误:', error)
  const message = error?.message || defaultMessage
  infoDialog.title = '错误'
  infoDialog.message = message
  // 没有锚点信息时，弹窗使用默认居中 / 顶部位置
  infoAnchor.value = null
  infoDialog.visible = true
}

// 控制是否已经预取
let passengerDataPrefetched = false

const prefetchPassengerData = async () => {
  if (passengerDataPrefetched) return
  passengerDataPrefetched = true

  const cachedStats = localStorage.getItem('passenger_stats')
  const cachedFlights = localStorage.getItem('passenger_upcoming_flights')

  if (cachedStats) {
    try {
      Object.assign(stats, JSON.parse(cachedStats))
    } catch (error) {
      console.warn('解析缓存统计数据失败:', error)
    }
  }

  if (cachedFlights) {
    try {
      const flights: FlightInfo[] = JSON.parse(cachedFlights)
      upcomingFlights.splice(0, upcomingFlights.length, ...flights)
      flights.forEach(flight => {
        ticketDetailCache[flight.id] = formatFlightDetailsMessage(flight)
      })
    } catch (error) {
      console.warn('解析缓存航班数据失败:', error)
    }
  }

  await Promise.all([loadStatistics(), loadUpcomingFlights()])
}

// 加载用户统计数据
const loadStatistics = async () => {
  try {
    loading.statistics = true
    
    // 获取基础统计数据
    const basicStats = await passengerApi.getStatistics()
    if (basicStats) {
      Object.assign(stats, {
        totalOrders: basicStats.totalOrders || 0,
        upcomingFlights: basicStats.upcomingFlights || 0,
        completedFlights: basicStats.completedFlights || 0,
        totalSpent: basicStats.totalSpent || 0,
        averageSpent: basicStats.averageSpent || 0,
        orderGrowth: basicStats.orderGrowth || 0
      })
    }
    
    // 获取消费趋势数据
    try {
      const trendData = await passengerApi.getSpendingTrend()
      if (Array.isArray(trendData) && trendData.length > 0) {
        spendingTrend.splice(0, spendingTrend.length, ...trendData)
      } else if (trendData && trendData.trend && Array.isArray(trendData.trend)) {
        spendingTrend.splice(0, spendingTrend.length, ...trendData.trend)
      }
    } catch (error) {
      console.warn('获取消费趋势失败:', error)
    }
    
    // 获取月度对比数据
    try {
      const comparisonData = await passengerApi.getMonthlyComparison()
      if (Array.isArray(comparisonData) && comparisonData.length > 0) {
        monthlyComparison.splice(0, monthlyComparison.length, ...comparisonData)
      } else if (comparisonData && comparisonData.comparison && Array.isArray(comparisonData.comparison)) {
        monthlyComparison.splice(0, monthlyComparison.length, ...comparisonData.comparison)
      }
    } catch (error) {
      console.warn('获取月度对比失败:', error)
    }
    
    // 获取订单状态分布
    try {
      const distributionData = await passengerApi.getOrderStatusDistribution()
      if (Array.isArray(distributionData) && distributionData.length > 0) {
        orderStatusSegments.splice(0, orderStatusSegments.length)
        distributionData.forEach((item: any) => {
          orderStatusSegments.push({
            label: item.label,
            count: item.count,
            percentage: item.percentage,
            color: item.color
          })
        })
      } else if (distributionData && distributionData.distribution && Array.isArray(distributionData.distribution)) {
        orderStatusSegments.splice(0, orderStatusSegments.length)
        distributionData.distribution.forEach((item: any) => {
          orderStatusSegments.push({
            label: item.label,
            count: item.count,
            percentage: item.percentage,
            color: item.color
          })
        })
      }
    } catch (error) {
      console.warn('获取状态分布失败:', error)
    }
    
    // 获取热门航线
    try {
      const routesData = await passengerApi.getPopularRoutes(5)
      if (Array.isArray(routesData) && routesData.length > 0) {
        popularRoutes.splice(0, popularRoutes.length, ...routesData)
      } else if (routesData && routesData.routes && Array.isArray(routesData.routes)) {
        popularRoutes.splice(0, popularRoutes.length, ...routesData.routes)
      }
    } catch (error) {
      console.warn('获取热门航线失败:', error)
    }
    
    // 保存到本地存储
    localStorage.setItem('passenger_stats', JSON.stringify(stats))
  } catch (error) {
    handleApiError(error, '获取统计数据失败')
  } finally {
    loading.statistics = false
  }
}

// 加载待出行航班
const loadUpcomingFlights = async () => {
  try {
    loading.flights = true
    const result = await passengerApi.getUpcomingFlights()
    if (result?.flights) {
      // 直接使用数据库原始时间，不做任何时区转换
      const processedFlights = result.flights.map((flight: any) => {
        // 处理departureTime：如果是完整时间字符串，只提取时间部分
        let departureTime = flight.departureTime || ''
        if (departureTime && departureTime.includes(' ')) {
          // 格式：2025-12-09 18:30:00 或 2025-12-09 18:30
          const parts = departureTime.split(' ')
          if (parts.length >= 2) {
            departureTime = parts[1].substring(0, 5) // 提取 HH:mm 部分
          }
        } else if (departureTime && departureTime.includes('T')) {
          // 格式：2025-12-09T18:30:00
          const parts = departureTime.split('T')
          if (parts.length >= 2) {
            departureTime = parts[1].substring(0, 5) // 提取 HH:mm 部分
          }
        } else if (departureTime && departureTime.length > 5) {
          // 如果时间字符串长度超过5，可能是完整时间，只取前5个字符（HH:mm）
          departureTime = departureTime.substring(0, 5)
        }
        
        // 处理arrivalTime：如果是完整时间字符串，只提取时间部分
        let arrivalTime = flight.arrivalTime || ''
        if (arrivalTime && arrivalTime.includes(' ')) {
          const parts = arrivalTime.split(' ')
          if (parts.length >= 2) {
            arrivalTime = parts[1].substring(0, 5)
          }
        } else if (arrivalTime && arrivalTime.includes('T')) {
          const parts = arrivalTime.split('T')
          if (parts.length >= 2) {
            arrivalTime = parts[1].substring(0, 5)
          }
        } else if (arrivalTime && arrivalTime.length > 5) {
          arrivalTime = arrivalTime.substring(0, 5)
        }
        
        return {
          ...flight,
          departureTime,
          arrivalTime
        }
      })
      
      upcomingFlights.splice(0, upcomingFlights.length, ...processedFlights)
      stats.upcomingFlights = processedFlights.length
      processedFlights.forEach((flight: FlightInfo) => {
        ticketDetailCache[flight.id] = formatFlightDetailsMessage(flight)
      })
      localStorage.setItem('passenger_upcoming_flights', JSON.stringify(processedFlights))
    }
  } catch (error) {
    handleApiError(error, '获取待出行航班失败')
  } finally {
    loading.flights = false
  }
}

// 弹窗状态 + 锚点（用于根据按钮位置定位弹窗）
const confirmDialog = reactive({
  visible: false,
  title: '',
  message: '',
  onConfirm: () => {}
})

const infoDialog = reactive({
  visible: false,
  title: '',
  message: ''
})

const successDialog = reactive({
  visible: false,
  title: '',
  message: ''
})

const confirmAnchor = ref<{ x: number; y: number; width: number; height: number } | null>(null)
const infoAnchor = ref<{ x: number; y: number; width: number; height: number } | null>(null)
const successAnchor = ref<{ x: number; y: number; width: number; height: number } | null>(null)

const expandedFaq = ref(-1)

const inlineServiceTexts: Record<string, string[]> = {
  'luggage-rules': [
    '免费托运额度：经济舱 20kg / 商务舱 30kg / 头等舱 40kg',
    '随身行李限重 5kg，尺寸不超过 20×40×55cm',
    '超重行李按 50 元/kg 收费，建议提前在线预购',
    '禁止携带易燃易爆物品及 100ml 以上液体（需托运）'
  ],
  'airport-guide': [
    '北京首都：地铁 2 号航站楼直达，值机高峰 7:30-9:30',
    '上海浦东：T1/ T2 安检分流，磁悬浮 8 分钟到龙阳路',
    '广州白云：APM 连接航站楼，机场巴士 30 分钟一班',
    '建议提前查询目的地机场交通及值机柜台位置'
  ],
  'refund-policy': [
    '改签：起飞7天以前 5%；起飞前2天至7天 10%;起飞前2天以内 15%',
    '退票：起飞7天以前 5%；起飞前2天至7天 10%;起飞前2天以内 15%'
    
  ]
}

const inlineService = ref<string | null>(null)
const inlineServiceIds = new Set(Object.keys(inlineServiceTexts))

// 快捷操作加载状态
const actionLoading = ref<string | null>(null)

// 导航函数
const navigateTo = (path: string) => {
  router.push(path)
}

// 处理快捷操作按钮点击
const handleQuickAction = async (path: string, actionName: string) => {
  try {
    // 根据不同的操作设置不同的路由参数和加载状态
    let routeConfig: any = { path }
    let loadingKey: string | null = null
    
    switch (actionName) {
      case '搜索航班':
        routeConfig = {
          path: '/portal/passengers/view',
          query: { tab: 'search' }
        }
        loadingKey = 'search'
        break
      case '我的订单':
        routeConfig = {
          path: '/portal/orders',
          query: { view: 'list' }
        }
        loadingKey = 'orders'
        break
      case '一站式机票管理':
        routeConfig = {
          path: '/portal/orders',
          query: { view: 'manage', action: 'unified' }
        }
        loadingKey = 'unifiedTicket'
        break
      case '积分优惠':
        routeConfig = {
          path: '/portal/passengers/points-coupons'
        }
        loadingKey = 'points'
        break
      case '行李管理':
        routeConfig = {
          path: '/portal/passengers/baggage'
        }
        loadingKey = 'baggage'
        break
    }
    
    // 设置加载状态
    actionLoading.value = loadingKey
    
    // 执行路由跳转
    await router.push(routeConfig)
    
    // 等待路由和组件完全渲染
    await nextTick()
    
    // 确保路由完成后再关闭加载状态
    setTimeout(() => {
      actionLoading.value = null
    }, 300)
  } catch (error) {
    console.error('快捷操作跳转失败:', error)
    actionLoading.value = null
    handleApiError(error, '页面跳转失败，请重试')
  }
}

// 根据点击事件计算锚点位置
const getAnchorFromElement = (element: HTMLElement | null) => {
  if (!element) return null
  const rect = element.getBoundingClientRect()
  return {
    x: rect.left,
    y: rect.top,
    width: rect.width,
    height: rect.height
  }
}

// 查看机票
const flightsSectionRef = ref<HTMLElement | null>(null)

const formatFlightDetailsMessage = (flight: FlightInfo) => {
  return `航班号：${flight.flightNumber} 航线：${flight.origin} → ${flight.destination} 日期：${flight.date}
起飞时间：${flight.departureTime}
到达时间：${flight.arrivalTime} 状态：${flight.status}`
}

const viewTicket = (flightId: number) => {
  console.log('查看机票:', flightId)
  infoAnchor.value = getAnchorFromElement(flightsSectionRef.value)
  showTicketDetail(flightId)
}

// 改签机票
const modifyTicket = (flightId: number, event?: MouseEvent) => {
  const flight = upcomingFlights.find(f => String(f.id) === String(flightId))
  if (!flight) {
    infoAnchor.value = getAnchorFromElement(event?.currentTarget as HTMLElement | null)
    infoDialog.title = '未找到可改签航班'
    infoDialog.message = '请刷新列表或稍后重试。'
    infoDialog.visible = true
    return
  }
  rescheduleDialog.flight = flight
  rescheduleDialog.options = createRescheduleOptions(flight)
  rescheduleDialog.selectedOptionId = rescheduleDialog.options[0]?.id || ''
  rescheduleDialog.reason = ''
  rescheduleDialog.error = ''
  rescheduleDialog.submitting = false
  rescheduleDialog.visible = true
}

// 取消机票
const cancelTicket = (flightId: number, event?: MouseEvent) => {
  const flight = upcomingFlights.find(f => String(f.id) === String(flightId))
  if (!flight) {
    infoAnchor.value = getAnchorFromElement(event?.currentTarget as HTMLElement | null)
    infoDialog.title = '未找到可取消航班'
    infoDialog.message = '请刷新列表或稍后重试。'
    infoDialog.visible = true
    return
  }
  cancelDialog.flight = flight
  // 从航班或关联订单中提取机票价格
  const rawPrice =
    (flight as any).amount ??
    (flight as any).totalAmount ??
    (flight as any).price ??
    (flight as any).ticketPrice
  const numericPrice = typeof rawPrice === 'number' ? rawPrice : parseFloat(String(rawPrice || '').replace(/[^0-9.]/g, ''))
  cancelDialog.ticketPrice = isNaN(numericPrice) || numericPrice <= 0 ? 0 : Number(numericPrice.toFixed(2))
  // 退票手续费按票价 5% 自动计算
  cancelDialog.fee = cancelDialog.ticketPrice > 0 ? Number((cancelDialog.ticketPrice * 0.05).toFixed(2)) : 0
  cancelDialog.reasonCategory = ''
  cancelDialog.reasonDetail = ''
  cancelDialog.error = ''
  cancelDialog.submitting = false
  cancelDialog.visible = true
}

const closeCancelDialog = () => {
  cancelDialog.visible = false
  cancelDialog.reasonCategory = ''
  cancelDialog.reasonDetail = ''
  cancelDialog.error = ''
}

const submitCancel = async () => {
  if (!cancelDialog.flight) return
  if (!cancelDialog.reasonCategory) {
    cancelDialog.error = '请选择取消原因类型'
    return
  }
  if (!cancelDialog.reasonDetail.trim()) {
    cancelDialog.error = '请填写取消说明'
    return
  }
  cancelDialog.error = ''
  cancelDialog.submitting = true
  try {
    // 预置与后端对接的请求体，确保字段与航空运营一致
    const payload = {
      orderId: String(cancelDialog.flight.id),
      orderNo: (cancelDialog.flight as any).orderNo || String(cancelDialog.flight.id),
      flightNumber: cancelDialog.flight.flightNumber,
      origin: cancelDialog.flight.origin,
      destination: cancelDialog.flight.destination,
      cancelFee: cancelDialog.fee,
      reason: `${cancelDialog.reasonCategory}｜${cancelDialog.reasonDetail.trim()}`
    }
    const apiClient = passengerApi as any
    if (apiClient?.requestRefund) {
      await apiClient.requestRefund(payload)
    } else {
      // 演示场景下直接模拟网络延时
      await new Promise(resolve => setTimeout(resolve, 600))
    }

    const index = upcomingFlights.findIndex(f => f.id === cancelDialog.flight?.id)
    if (index > -1) {
      upcomingFlights.splice(index, 1)
      stats.upcomingFlights = Math.max(0, stats.upcomingFlights - 1)
    }
    await loadStatistics()

    successAnchor.value = getAnchorFromElement(flightsSectionRef.value)
    successDialog.title = '取消已提交'
    successDialog.message = `航班 ${cancelDialog.flight.flightNumber} 取消申请已提交。\n预估手续费：¥${cancelDialog.fee}，实际以航空运营结算为准。`
    successDialog.visible = true
    closeCancelDialog()
  } catch (error) {
    handleApiError(error, '取消航班失败')
  } finally {
    cancelDialog.submitting = false
  }
}

// 新增：显示机票详情
const showTicketDetail = (flightId: number) => {
  const cachedMessage = ticketDetailCache[flightId]
  const flight = upcomingFlights.find(f => f.id === flightId)
  infoDialog.title = '机票详情'
  infoDialog.message = cachedMessage || (flight ? formatFlightDetailsMessage(flight) : '暂无航班信息。')
  infoDialog.visible = true
}

const closeRescheduleDialog = () => {
  rescheduleDialog.visible = false
  rescheduleDialog.flight = null
  rescheduleDialog.options = []
  rescheduleDialog.selectedOptionId = ''
  rescheduleDialog.reason = ''
  rescheduleDialog.error = ''
}

const formatDateString = (date: Date): string => {
  const iso = date.toISOString()
  const [day] = iso.split('T')
  return day || ''
}

const createRescheduleOptions = (flight: FlightInfo): RescheduleOption[] => {
  const baseDate = new Date(flight.date)
  const options: RescheduleOption[] = []
  for (let offset = -1; offset <= 1; offset++) {
    const date = new Date(baseDate)
    date.setDate(baseDate.getDate() + offset)
    const id = `${flight.id}-${offset + 2}`
    const departureTime = offset === 1 ? '14:30' : offset === -1 ? '07:45' : '10:10'
    const arrivalTime = offset === 1 ? '18:00' : offset === -1 ? '11:25' : '13:30'
    options.push({
      id,
      date: formatDateString(date),
      departureTime,
      arrivalTime,
      cabin: offset === 0 ? '经济舱 | 免费改签' : '超值经济舱',
      seats: Math.max(0, 6 - Math.abs(offset) * 2),
      priceDiff: offset === 0 ? 0 : offset > 0 ? 200 : -150
    })
  }
  return options
}

const submitReschedule = async () => {
  if (!selectedRescheduleOption.value) {
    rescheduleDialog.error = '请选择一个可改签的班次'
    return
  }
  if (!rescheduleDialog.reason.trim()) {
    rescheduleDialog.error = '请填写改签原因，便于客服审核'
    return
  }
  if (!rescheduleDialog.flight) return
  if (!rescheduleDialog.flight.origin || !rescheduleDialog.flight.destination) {
    infoDialog.title = '缺少航程信息'
    infoDialog.message = '当前航班缺少起点或终点信息，请刷新后重试。'
    infoDialog.visible = true
    return
  }
  rescheduleDialog.error = ''
  rescheduleDialog.submitting = true
  try {
    const option = selectedRescheduleOption.value
    const payload = {
      orderId: rescheduleDialog.flight.id,
      flightNumber: rescheduleDialog.flight.flightNumber,
      origin: rescheduleDialog.flight.origin,
      destination: rescheduleDialog.flight.destination,
      newDate: option?.date,
      newDepartureTime: option?.departureTime,
      newArrivalTime: option?.arrivalTime,
      cabin: option?.cabin,
      priceDiff: option?.priceDiff || 0,
      changeFee: rescheduleDialog.serviceFee,
      reason: rescheduleDialog.reason.trim()
    }

    const fee = Number(payload.changeFee || 0) + Number(payload.priceDiff || 0)
    if ((orderApi as any)?.payReschedule && fee > 0) {
      // 先同步打开空窗口，防止被浏览器拦截
      const payWindow = window.open('about:blank', '_blank')
      try {
        const html = await (orderApi as any).payReschedule({
          orderNo: payload.orderId || (payload as any).orderNo,
          changeFee: payload.changeFee,
          priceDiff: payload.priceDiff,
          reason: payload.reason,
          newFlight: (payload as any).newFlight || null
        })
        if (payWindow && !payWindow.closed) {
          payWindow.document.open()
          payWindow.document.write(html)
          payWindow.document.close()
        } else {
          window.document.open()
          window.document.write(html)
          window.document.close()
        }
      } catch (e) {
        if (payWindow && !payWindow.closed) try { payWindow.close() } catch {}
        throw e
      }
    } else {
      const apiClient = passengerApi as any
      if (apiClient?.requestReschedule) {
        await apiClient.requestReschedule(payload)
      } else {
        await new Promise(resolve => setTimeout(resolve, 900))
      }
    }
    rescheduleDialog.flight.date = option.date
    rescheduleDialog.flight.departureTime = option.departureTime
    rescheduleDialog.flight.arrivalTime = option.arrivalTime
    rescheduleDialog.flight.status = '改签成功'
    ticketDetailCache[rescheduleDialog.flight.id] = formatFlightDetailsMessage(rescheduleDialog.flight)
    closeRescheduleDialog()
    successAnchor.value = getAnchorFromElement(flightsSectionRef.value)
    successDialog.title = '改签已提交'
    successDialog.message = `已为您锁定 ${option.date} ${option.departureTime} 的航班。\n` +
      `请在 15 分钟内完成付款确认，若需补差价请至“我的订单”查看。`
    successDialog.visible = true
    await loadStatistics()
  } catch (error) {
    handleApiError(error, '提交改签申请失败')
  } finally {
    rescheduleDialog.submitting = false
  }
}

// 格式化数字
const formatNumber = (num: number) => {
  return num.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ',')
}

// 处理更多服务卡片
const handleService = (service: string) => {
  if (service === 'customer-service') {
    router.push({
      path: '/portal/passengers/support',
      query: { source: 'dashboard' }
    })
    return
  }
  if (service === 'travel-info') {
    router.push('/portal/passengers/weather')
    return
  }
  if (inlineServiceIds.has(service)) {
    inlineService.value = inlineService.value === service ? null : service
    return
  }
}

// 切换FAQ的展开/折叠
const toggleFaq = (index: number) => {
  expandedFaq.value = expandedFaq.value === index ? -1 : index
}

// 跳转到重点旅客预约页面
const goToSpecialPassengerPage = () => {
  router.push('/portal/passengers/special-passenger')
}

// 重点旅客预约相关方法
const openSpecialPassengerDialog = async () => {
  try {
    actionLoading.value = 'special'
    
    // 先清空所有数据
    selectedOrder.value = null
    Object.assign(specialPassengerForm, {
      orderId: null,
      countryCode: '+86',
      phone: '',
      passengerType: '',
      departureAirport: '',
      arrivalAirport: '',
      entryServices: { selfEquipment: false, priorityEntry: false, wheelchair: false, stretcher: false },
      exitServices: { selfEquipment: false, convenientExit: false, wheelchair: false, stretcher: false },
      description: ''
    })
    specialPassengerDialog.error = ''
    
    // 检查是否有从订单列表返回的订单选择
    const selectedOrderData = sessionStorage.getItem('selectedOrderForSpecialPassenger')
    if (selectedOrderData) {
      try {
        const orderData = JSON.parse(selectedOrderData)
        // 填充订单信息
        selectedOrder.value = {
          id: orderData.id || orderData.orderId,
          orderNo: orderData.orderNo || orderData.order_no || String(orderData.id || orderData.orderId),
          route: orderData.route || '',
          time: orderData.time || '',
          amount: orderData.amount || 0
        }
        specialPassengerForm.orderId = orderData.id || orderData.orderId
        
        // 解析航线，填充出发机场和到达机场
        if (orderData.route) {
          const routeInfo = parseRoute(orderData.route)
          specialPassengerForm.departureAirport = routeInfo.origin
          specialPassengerForm.arrivalAirport = routeInfo.destination
        }
        
        // 清除sessionStorage中的选择数据
        sessionStorage.removeItem('selectedOrderForSpecialPassenger')
      } catch (e) {
        console.warn('解析选中的订单数据失败:', e)
      }
    }
    
    // 加载订单列表（用于下拉选择，如果用户想重新选择）
    const ordersResult = await orderApi.getOrders()
    if (ordersResult && ordersResult.orders) {
      availableOrders.value = ordersResult.orders.map((order: any) => ({
        id: order.id,
        orderNo: order.orderNo || order.order_no || String(order.id),
        route: `${order.origin || order.route || ''} → ${order.destination || ''}`,
        time: order.departureTime || order.time || '',
        amount: order.amount || order.price || 0
      }))
    }
    specialPassengerDialog.visible = true
  } catch (error) {
    handleApiError(error, '加载订单列表失败')
  } finally {
    actionLoading.value = null
  }
}

const closeSpecialPassengerDialog = () => {
  specialPassengerDialog.visible = false
  showOrderSelector.value = false
  showPassengerTypeSelector.value = false
  showDepartureAirportSelector.value = false
  showArrivalAirportSelector.value = false
}

const toggleOrderSelector = () => {
  // 跳转到订单列表页面，传递参数表示从预约页面来
  router.push({
    path: '/portal/orders',
    query: {
      from: 'special-passenger',
      selectMode: 'true'
    }
  })
}

const selectOrder = (order: any) => {
  selectedOrder.value = {
    ...order,
    orderNo: order.orderNo || order.order_no || String(order.id)
  }
  specialPassengerForm.orderId = order.id
  showOrderSelector.value = false
  
  // 解析航线，自动填充出发机场和到达机场
  if (order.route) {
    const routeInfo = parseRoute(order.route)
    specialPassengerForm.departureAirport = routeInfo.origin
    specialPassengerForm.arrivalAirport = routeInfo.destination
  }
}

const togglePassengerTypeSelector = () => {
  showPassengerTypeSelector.value = !showPassengerTypeSelector.value
  showOrderSelector.value = false
  showDepartureAirportSelector.value = false
  showArrivalAirportSelector.value = false
}

const selectPassengerType = (type: string) => {
  specialPassengerForm.passengerType = type
  showPassengerTypeSelector.value = false
}

const toggleDepartureAirportSelector = () => {
  showDepartureAirportSelector.value = !showDepartureAirportSelector.value
  showOrderSelector.value = false
  showPassengerTypeSelector.value = false
  showArrivalAirportSelector.value = false
}

const selectDepartureAirport = (airport: string) => {
  specialPassengerForm.departureAirport = airport
  showDepartureAirportSelector.value = false
}

const toggleArrivalAirportSelector = () => {
  showArrivalAirportSelector.value = !showArrivalAirportSelector.value
  showOrderSelector.value = false
  showPassengerTypeSelector.value = false
  showDepartureAirportSelector.value = false
}

const selectArrivalAirport = (airport: string) => {
  specialPassengerForm.arrivalAirport = airport
  showArrivalAirportSelector.value = false
}

const submitSpecialPassengerRequest = async () => {
  // 验证必填字段
  if (!specialPassengerForm.orderId) {
    specialPassengerDialog.error = '请选择订单'
    return
  }
  if (!specialPassengerForm.phone || !/^1[3-9]\d{9}$/.test(specialPassengerForm.phone)) {
    specialPassengerDialog.error = '请输入正确的手机号'
    return
  }
  if (!specialPassengerForm.passengerType) {
    specialPassengerDialog.error = '请选择旅客类型'
    return
  }
  if (!specialPassengerForm.departureAirport) {
    specialPassengerDialog.error = '请填写出发机场'
    return
  }
  if (!specialPassengerForm.arrivalAirport) {
    specialPassengerDialog.error = '请填写到达机场'
    return
  }

  specialPassengerDialog.error = ''
  specialPassengerDialog.submitting = true

  try {
    // 获取订单号 - 直接使用选择订单的订单号
    let orderNo = ''
    if (selectedOrder.value && selectedOrder.value.orderNo) {
      // 直接使用选择订单的订单号
      orderNo = selectedOrder.value.orderNo
    } else if (specialPassengerForm.orderId) {
      // 如果没有selectedOrder或orderNo，使用orderId作为订单号
      orderNo = String(specialPassengerForm.orderId)
    } else {
      throw new Error('无法获取订单号')
    }
    
    // 调用后端API提交重点旅客预约
    await specialServiceRequestApi.createRequest({
      orderNo: orderNo,
      phone: specialPassengerForm.phone,
      passengerType: specialPassengerForm.passengerType,
      departureAirport: specialPassengerForm.departureAirport,
      arrivalAirport: specialPassengerForm.arrivalAirport,
      entryServices: specialPassengerForm.entryServices,
      exitServices: specialPassengerForm.exitServices,
      description: specialPassengerForm.description || undefined
    })
    
    closeSpecialPassengerDialog()
    successAnchor.value = null
    successDialog.title = '提交成功'
    successDialog.message = '您的重点旅客预约申请已提交，我们将在24小时内与您联系确认服务安排。'
    successDialog.visible = true
  } catch (error: any) {
    specialPassengerDialog.error = error.message || '提交申请失败，请重试'
    handleApiError(error, '提交申请失败')
  } finally {
    specialPassengerDialog.submitting = false
  }
}

// 点击外部关闭下拉
const handleClickOutside = (event: MouseEvent) => {
  const target = event.target as HTMLElement
  // 检查是否点击在下拉框相关元素上
  const isClickOnDropdown = target.closest('.order-selector-dropdown') ||
                            target.closest('.passenger-type-dropdown') ||
                            target.closest('.airport-selector-dropdown') ||
                            target.closest('.select-wrapper')
  
  if (!isClickOnDropdown) {
    showOrderSelector.value = false
    showPassengerTypeSelector.value = false
    showDepartureAirportSelector.value = false
    showArrivalAirportSelector.value = false
  }
}

// 处理成功提示窗确认
const handleSuccessConfirm = () => {
  successDialog.visible = false
  // 跳转到普通乘客首页
  router.push('/portal/passengers')
}

// 初始化数据
onMounted(async () => {
  // 检查是否需要自动打开重点旅客预约弹窗（从订单列表选择订单后返回）
  const autoOpen = sessionStorage.getItem('autoOpenSpecialPassengerDialog')
  if (autoOpen === 'true') {
    sessionStorage.removeItem('autoOpenSpecialPassengerDialog')
    // 延迟一下，确保页面完全加载后再打开弹窗
    await nextTick()
    setTimeout(() => {
      openSpecialPassengerDialog()
    }, 300)
  }
  await prefetchPassengerData()
  
  // 恢复手机预览状态（如果之前设置过）
  try {
    const saved = localStorage.getItem('mobile_preview')
    if (saved === 'true') {
      isMobilePreview.value = true
      document.documentElement.classList.add('mobile-preview')
    }
  } catch (e) {
    console.warn('恢复手机预览状态失败:', e)
  }
  
  // 添加点击外部关闭下拉的事件监听
  document.addEventListener('click', handleClickOutside)
  
  // 若当前用户手机号缺失，尝试从资料接口补齐（静默失败）
  try {
    if (!userInfo.value.phone || userInfo.value.phone === '未设置') {
      const profile = await passengerApi.getProfile()
      const phone = (profile && (profile.phone || profile?.data?.phone)) || ''
      if (phone) {
        const merged = { ...(store.userState.userInfo || {}), phone }
        store.setUserState(merged)
      }
    }
  } catch {}
  
  // 更新可视化数据（基于实际统计数据）
  if (stats.totalOrders > 0) {
    // 更新订单状态分布
    if (orderStatusSegments[0]) {
      orderStatusSegments[0].count = stats.completedFlights
    }
    if (orderStatusSegments[1]) {
      orderStatusSegments[1].count = stats.upcomingFlights
    }
    if (orderStatusSegments[2]) {
      orderStatusSegments[2].count = Math.max(0, stats.totalOrders - stats.completedFlights - stats.upcomingFlights)
    }
    
    const total = orderStatusSegments.reduce((sum, seg) => sum + seg.count, 0)
    if (total > 0) {
      orderStatusSegments.forEach(seg => {
        seg.percentage = Math.round((seg.count / total) * 100)
      })
    }
  }

  // 加载航班实时查询
  try {
    await loadFlightQueryFlights()
  } catch (error) {
    console.error('加载航班实时查询失败:', error)
  }
  
  // 设置定时刷新（每1秒刷新一次）
  flightQueryRefreshTimer.value = setInterval(() => {
    try {
      loadFlightQueryFlights()
    } catch (error) {
      console.error('定时刷新航班数据失败:', error)
    }
  }, 1000)
})

prefetchPassengerData()

onUnmounted(() => {
  // 移除事件监听
  document.removeEventListener('click', handleClickOutside)
  // 清除定时器
  if (flightQueryRefreshTimer.value) {
    clearInterval(flightQueryRefreshTimer.value)
    flightQueryRefreshTimer.value = null
  }
})
</script>

<style scoped>
.breadcrumb {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.7);
  margin-bottom: 20px;
}

.profile-section {
  margin-bottom: 30px;
}

.profile-card {
  background: rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(16px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 16px;
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 24px;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
}

.profile-info {
  flex: 1;
  color: white;
}

.profile-name {
  font-size: 24px;
  font-weight: 700;
  margin-bottom: 12px;
}

.profile-details {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.profile-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
}

.profile-label {
  color: rgba(255, 255, 255, 0.6);
  font-weight: 500;
  min-width: 80px;
}

.profile-value {
  color: rgba(255, 255, 255, 0.9);
  font-weight: 500;
  word-break: break-all;
}

.profile-email,
.profile-phone {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.7);
  margin-bottom: 4px;
}

.profile-stats {
  display: flex;
  gap: 32px;
}

.profile-stats .stat-item {
  text-align: center;
  color: rgb(255, 255, 255);
}

.profile-stats .stat-value {
  font-size: 24px;
  font-weight: 700;
  margin-bottom: 4px;
}

.profile-stats .stat-label {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.85);
  font-weight: 600;
}

.actions-section {
  margin-bottom: 30px;
}

.section-title {
  font-size: 20px;
  color: rgb(255, 255, 255);
  font-weight: 600;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  gap: 12px;
}

.title-icon {
  font-size: 24px;
}

.actions-grid {
  display: grid;
  /* 三列布局以支持 7 个功能项：宽度自适应但不小于 200px */
  grid-template-columns: repeat(3, minmax(200px, 1fr));
  gap: 24px; /* 横向与纵向间距一致 */
  align-items: start;
  justify-items: stretch;
  padding: 6px 0;
}

@media (max-width: 1200px) {
  .actions-grid {
    grid-template-columns: repeat(2, minmax(180px, 1fr));
    gap: 20px;
  }
}

.action-card-wrapper {
  position: relative;
}

.action-card {
  background: rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(16px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 16px;
  padding: 16px;
  min-height: 84px;
  box-sizing: border-box;
  width: 100%;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  display: flex;
  align-items: center;
  gap: 16px;
  position: relative;
  overflow: visible;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
}

/* 航班实时查询面板样式 */
.flight-query-panel {
  margin-top: 1rem;
  padding: 1rem;
  background: rgba(30, 138, 230, 0.1);
  border: 1px solid rgba(30, 138, 230, 0.3);
  border-radius: 12px;
  backdrop-filter: blur(10px);
}

.flight-query-header {
  margin-bottom: 0.75rem;
}

.flight-query-title {
  font-size: 0.9rem;
  font-weight: 600;
  color: var(--text-primary, #fff);
}

.flight-query-list {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.flight-query-item {
  padding: 0.75rem;
  background: rgba(2, 6, 23, 0.4);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.flight-query-row {
  display: flex;
  align-items: center;
  font-size: 0.85rem;
}

.flight-query-label {
  color: rgba(255, 255, 255, 0.7);
  min-width: 70px;
  flex-shrink: 0;
}

.flight-query-value {
  color: var(--text-primary, #fff);
  font-weight: 500;
  flex: 1;
}

.action-card:hover:not(.loading) {
  transform: translateY(-4px);
  box-shadow: 0 16px 36px rgba(16, 24, 40, 0.16);
  background: rgba(255, 255, 255, 0.12);
  border-color: rgba(30, 138, 230, 0.45);
}

.action-card:active:not(.loading) {
  transform: translateY(-2px) scale(0.98);
  box-shadow: 0 8px 24px rgba(16, 24, 40, 0.12);
}

/* 主要操作按钮 - 绿色强调 */
.action-card.primary-action {
  border-color: rgba(16, 185, 129, 0.4);
  background: rgba(16, 185, 129, 0.08);
}

.action-card.primary-action:hover:not(.loading) {
  border-color: rgba(16, 185, 129, 0.6);
  background: rgba(16, 185, 129, 0.12);
  box-shadow: 0 16px 36px rgba(16, 185, 129, 0.2);
}

/* 订单管理按钮 - 橙色 */
.action-card.order-action {
  border-color: rgba(245, 158, 11, 0.4);
  background: rgba(245, 158, 11, 0.08);
}

.action-card.order-action:hover:not(.loading) {
  border-color: rgba(245, 158, 11, 0.6);
  background: rgba(245, 158, 11, 0.12);
  box-shadow: 0 16px 36px rgba(245, 158, 11, 0.2);
}

/* 积分服务按钮 - 紫色 */
.action-card.points-action {
  border-color: rgba(139, 92, 246, 0.4);
  background: rgba(139, 92, 246, 0.08);
}

.action-card.points-action:hover:not(.loading) {
  border-color: rgba(139, 92, 246, 0.6);
  background: rgba(139, 92, 246, 0.12);
  box-shadow: 0 16px 36px rgba(139, 92, 246, 0.2);
}

/* 特殊服务按钮 - 蓝色 */
.action-card.special-action {
  border-color: rgba(30, 138, 230, 0.4);
  background: rgba(30, 138, 230, 0.08);
}

.action-card.special-action:hover:not(.loading) {
  border-color: rgba(30, 138, 230, 0.6);
  background: rgba(30, 138, 230, 0.12);
  box-shadow: 0 16px 36px rgba(30, 138, 230, 0.2);
}

.action-card.loading {
  opacity: 0.8;
  cursor: wait;
  pointer-events: none;
  background: rgba(255, 255, 255, 0.05);
  border-color: rgba(30, 138, 230, 0.1);
}

.action-loading {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  color: rgba(99, 102, 241, 0.8);
  font-size: 0.9rem;
  font-weight: 500;
}

.action-icon {
  width: 56px;
  height: 56px;
  min-width: 56px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(180deg,#ffffff,#f2f7ff);
  border: 1px solid rgba(7,34,58,0.06);
  box-shadow: 0 6px 18px rgba(7,34,58,0.04);
  font-size: 28px;
  color: #0A1F33;
  flex-shrink: 0;
}

.action-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 4px;
}

.action-title {
  font-size: 16px;
  font-weight: 700;
  color: white;
  margin: 0;
}

.action-desc {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.78);
  margin-top: 2px;
}

.flights-section {
  margin-bottom: 30px;
}

.flights-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(500px, 1fr));
  gap: 20px;
}

/* 响应式适配 */
@media (max-width: 768px) {
  .profile-card {
    flex-direction: column;
    text-align: center;
  }

  .profile-details {
    align-items: center;
  }

  .profile-label {
    min-width: auto;
  }

  .profile-stats {
    width: 100%;
    justify-content: space-around;
  }

  .actions-grid {
    grid-template-columns: 1fr;
  }

  .flights-grid {
    grid-template-columns: 1fr;
  }

  .stats-grid {
    grid-template-columns: 1fr;
  }

  .services-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 480px) {
  .services-grid {
    grid-template-columns: 1fr;
  }

  .flight-details {
    flex-direction: column;
    gap: 1rem;
  }

  .flight-actions {
    width: 100%;
    flex-direction: row;
    justify-content: space-between;
  }

  .action-btn {
    flex: 1;
  }
}

.flight-card {
  background: rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(18px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 16px;
  padding: 24px;
}

.flight-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  color: white;
}

.flight-date {
  font-size: 16px;
  font-weight: 600;
}

.flight-status {
  padding: 6px 12px;
  border-radius: 24px;
  font-size: 12px;
  font-weight: 600;
  text-transform: uppercase;
}

.flight-status.ontime {
  background: rgba(16, 185, 129, 0.2);
  color: #10b981;
}

.flight-status.delayed {
  background: rgba(245, 158, 11, 0.2);
  color: #f59e0b;
}

.flight-status.canceled {
  background: rgba(239, 68, 68, 0.2);
  color: #ef4444;
}

.flight-details {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.flight-info {
  color: white;
}

.flight-time {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 24px;
  font-weight: 700;
  margin-bottom: 8px;
}

.flight-time .time-arrow {
  font-size: 20px;
  color: rgba(255, 255, 255, 0.7);
}

.flight-route {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 8px;
}

.flight-number {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.7);
}

.flight-actions {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.action-btn {
  padding: 10px 20px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.05);
  color: white;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.action-btn:hover {
  background: rgba(255, 255, 255, 0.1);
  border-color: rgba(255, 255, 255, 0.3);
}

.view-btn:hover {
  background: rgba(59, 130, 246, 0.2);
  border-color: #3b82f6;
}

.modify-btn:hover {
  background: rgba(245, 158, 11, 0.2);
  border-color: #f59e0b;
}

.cancel-btn:hover {
  background: rgba(239, 68, 68, 0.2);
  border-color: #ef4444;
}

.stats-section {
  margin-bottom: 30px;
}

.stats-section .stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 20px;
}

.stat-card {
  background: rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(16px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 16px;
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 20px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06);
}

.stat-card:hover {
  background: rgba(255, 255, 255, 0.12);
  border-color: rgba(30, 138, 230, 0.25);
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(16, 24, 40, 0.12);
}

.stat-icon {
  font-size: 36px;
  color: white;
}

.stat-content {
  flex: 1;
  color: white;
}

.stat-value {
  font-size: 32px;
  font-weight: 800;
  margin-bottom: 8px;
  color: #ffffff;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
}

.stat-label {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.85);
  font-weight: 600;
}

.revenue-card .stat-icon {
  background: linear-gradient(135deg, #1E8AE6 0%, #0A2F63 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.average-card .stat-icon {
  background: linear-gradient(135deg, #ec4899 0%, #f43f5e 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.growth-card .stat-icon {
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

/* 更多服务区域 */
.services-section {
  margin-bottom: 30px;
}

.services-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
}

.service-card {
  background: rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(16px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 16px;
  padding: 24px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
}

.service-card.actionable {
  cursor: pointer;
}

.service-card.expanded {
  border-color: rgba(30, 138, 230, 0.45);
  box-shadow: 0 18px 36px rgba(30, 138, 230, 0.3);
  background: rgba(30, 138, 230, 0.08);
}

.service-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 12px 36px rgba(30, 138, 230, 0.25);
  border-color: rgba(30, 138, 230, 0.35);
  background: rgba(255, 255, 255, 0.12);
}

.service-link {
  font-size: 13px;
  color: #7dd3fc;
  margin-top: auto;
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.service-inline {
  margin-top: 14px;
  padding-top: 12px;
  border-top: 1px dashed rgba(148, 163, 184, 0.25);
  text-align: left;
  font-size: 13px;
  line-height: 1.6;
  color: rgba(226, 232, 240, 0.85);
}

.service-inline p {
  margin: 0 0 6px;
}

.service-inline-enter-active,
.service-inline-leave-active {
  transition: all 0.25s ease;
}

.service-inline-enter-from,
.service-inline-leave-to {
  opacity: 0;
  transform: translateY(-4px);
}

.service-icon {
  font-size: 48px;
  transition: transform 0.3s ease;
}

.service-card:hover .service-icon {
  transform: scale(1.1) rotate(5deg);
}

.service-title {
  color: white;
  font-size: 18px;
  font-weight: 600;
}

.service-desc {
  color: rgba(255, 255, 255, 0.6);
  font-size: 14px;
}

/* 常见问题区域 */
.faq-section {
  margin-bottom: 30px;
}

.faq-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.faq-item {
  background: rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(16px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 16px;
  overflow: hidden;
  transition: all 0.3s ease;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.faq-item:hover {
  border-color: rgba(30, 138, 230, 0.3);
  box-shadow: 0 8px 24px rgba(30, 138, 230, 0.15);
  background: rgba(255, 255, 255, 0.12);
}

.faq-question {
  padding: 16px 20px;
  color: white;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 16px;
  font-weight: 500;
  user-select: none;
  transition: all 0.3s ease;
}

.faq-question:hover {
  background: rgba(99, 102, 241, 0.1);
}

.faq-icon {
  color: rgba(99, 102, 241, 0.8);
  font-weight: 700;
  font-size: 18px;
  transition: transform 0.3s ease;
}

.faq-item:hover .faq-icon {
  transform: rotate(45deg);
}

.faq-answer {
  padding: 0 20px 16px;
  color: rgba(255, 255, 255, 0.7);
  font-size: 14px;
  line-height: 1.6;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  animation: slideDown 0.3s ease-out;
}

@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 数据可视化样式 */
.visualization-section {
  margin-bottom: 30px;
}

.charts-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(400px, 1fr));
  gap: 20px;
}

.chart-card {
  background: rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(16px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 16px;
  padding: 24px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
}

.chart-card:hover {
  background: rgba(255, 255, 255, 0.12);
  border-color: rgba(30, 138, 230, 0.25);
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(16, 24, 40, 0.12);
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.combined-chart-header {
  align-items: flex-start;
  gap: 16px;
}

.chart-header-text {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.chart-toggle {
  display: inline-flex;
  gap: 4px;
  padding: 6px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.12);
  border: 1px solid rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(10px);
}

.toggle-btn {
  border: 1px solid rgba(255, 255, 255, 0.2);
  background: transparent;
  color: rgba(255, 255, 255, 0.8);
  font-size: 13px;
  padding: 8px 16px;
  border-radius: 999px;
  cursor: pointer;
  transition: all 0.2s ease;
  font-weight: 600;
}

.toggle-btn:hover {
  color: white;
  border-color: rgba(255, 255, 255, 0.4);
  background: rgba(255, 255, 255, 0.1);
}

.toggle-btn.active {
  background: linear-gradient(120deg, #3B82F6, #1E40AF);
  color: #fff;
  border-color: rgba(59, 130, 246, 0.5);
  box-shadow: 0 6px 16px rgba(59, 130, 246, 0.4);
}

.chart-header h3 {
  margin: 0;
  color: white;
  font-size: 18px;
  font-weight: 600;
}

.chart-period {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.6);
  padding: 4px 12px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 12px;
}

.chart-container {
  min-height: 200px;
}

.chart-fade-enter-active,
.chart-fade-leave-active {
  transition: opacity 0.25s ease, transform 0.25s ease;
}

.chart-fade-enter-from,
.chart-fade-leave-to {
  opacity: 0;
  transform: translateY(8px);
}

/* 改签弹窗 */
.reschedule-overlay {
  position: fixed;
  inset: 0;
  background: rgba(2, 6, 23, 0.75);
  backdrop-filter: blur(8px);
  display: flex;
  align-items: flex-start;
  justify-content: center;
  z-index: 1200;
  padding: clamp(16px, 4vw, 32px);
  padding-top: calc(clamp(16px, 4vw, 32px) + 50px);
}

.reschedule-modal {
  width: min(720px, 100%);
  background: rgba(15, 23, 42, 0.98);
  border: 2px solid rgba(30, 138, 230, 0.2);
  border-radius: 16px;
  padding: clamp(20px, 4vw, 32px);
  box-shadow: 0 30px 80px rgba(3, 7, 18, 0.6);
  color: #fff;
  backdrop-filter: blur(20px);
}

.reschedule-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.reschedule-label {
  font-size: 13px;
  text-transform: uppercase;
  letter-spacing: 0.2em;
  color: rgba(255, 255, 255, 0.5);
  margin-bottom: 8px;
}

.reschedule-close {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  border: 1px solid rgba(255, 255, 255, 0.2);
  background: transparent;
  color: #fff;
  font-size: 20px;
  line-height: 1;
  cursor: pointer;
}

.current-flight {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  padding: 16px 20px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.05);
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.flight-date {
  font-size: 16px;
  font-weight: 600;
}

.flight-time {
  font-size: 20px;
  font-weight: 700;
}

.flight-meta {
  display: flex;
  flex-direction: column;
  gap: 6px;
  color: rgba(255, 255, 255, 0.75);
}

.option-list {
  margin-bottom: 16px;
}

.option-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 12px;
}

.option-card {
  border-radius: 12px;
  border: 2px solid rgba(255, 255, 255, 0.15);
  background: rgba(255, 255, 255, 0.08);
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 8px;
  color: inherit;
  text-align: left;
  cursor: pointer;
  transition: all 0.2s ease;
}

.option-card.active {
  border-color: #3B82F6;
  background: rgba(59, 130, 246, 0.15);
  box-shadow: 0 10px 30px rgba(59, 130, 246, 0.3);
  transform: translateY(-2px);
}

.option-card.full {
  opacity: 0.5;
  cursor: not-allowed;
}

.option-card .option-main {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.option-main strong {
  font-size: 16px;
}

.option-meta {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
  color: rgba(255, 255, 255, 0.65);
}

.option-meta .rise {
  color: #f87171;
  font-weight: 600;
}

.option-seat {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.6);
}

.reschedule-textarea {
  width: 100%;
  min-height: 90px;
  border-radius: 16px;
  border: 1px solid rgba(255, 255, 255, 0.16);
  background: rgba(2, 6, 23, 0.4);
  padding: 14px;
  color: #fff;
  margin-bottom: 16px;
  resize: vertical;
  font-family: inherit;
}

.reschedule-textarea:focus {
  outline: none;
  border-color: #1E8AE6;
  box-shadow: 0 0 0 2px rgba(30, 138, 230, 0.2);
}

.reschedule-error {
  color: #fda4af;
  font-size: 13px;
  margin: -8px 0 16px;
}

.reschedule-meta {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 12px;
  margin-bottom: 16px;
}

.meta-block {
  background: rgba(255, 255, 255, 0.04);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 12px;
  padding: 12px;
}

.meta-block.total {
  background: linear-gradient(135deg, rgba(30, 138, 230, 0.15), rgba(139, 92, 246, 0.12));
}

.meta-value {
  font-size: 15px;
  font-weight: 600;
  color: #fff;
}

.meta-value.highlight {
  color: #fbbf24;
}

.meta-tip {
  display: block;
  margin-top: 4px;
  color: rgba(255, 255, 255, 0.65);
  font-size: 12px;
}

.reason-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin: 6px 0 10px;
}

.reason-tag {
  border: 1px solid rgba(255, 255, 255, 0.2);
  background: rgba(255, 255, 255, 0.05);
  color: rgba(255, 255, 255, 0.85);
  border-radius: 999px;
  padding: 8px 14px;
  cursor: pointer;
  transition: all 0.2s ease;
  font-size: 13px;
}

.reason-tag.active {
  border-color: #1E8AE6;
  background: rgba(30, 138, 230, 0.18);
  color: #fff;
  box-shadow: 0 6px 16px rgba(30, 138, 230, 0.25);
}

.reschedule-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  flex-wrap: wrap;
}

.reschedule-btn {
  border-radius: 8px;
  padding: 12px 24px;
  font-weight: 600;
  cursor: pointer;
  border: 2px solid transparent;
  transition: all 0.2s ease;
  font-size: 14px;
}

.reschedule-btn.ghost {
  background: rgba(255, 255, 255, 0.1);
  border-color: rgba(255, 255, 255, 0.3);
  color: rgba(255, 255, 255, 0.9);
}

.reschedule-btn.ghost:hover {
  background: rgba(255, 255, 255, 0.15);
  border-color: rgba(255, 255, 255, 0.4);
}

.reschedule-btn.primary {
  background: linear-gradient(120deg, #3B82F6, #1E40AF);
  color: #fff;
  border-color: rgba(59, 130, 246, 0.3);
  box-shadow: 0 10px 24px rgba(59, 130, 246, 0.3);
}

.reschedule-btn.primary:hover {
  background: linear-gradient(120deg, #60A5FA, #3B82F6);
  box-shadow: 0 12px 28px rgba(59, 130, 246, 0.4);
}

.reschedule-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

@media (max-width: 640px) {
  .reschedule-modal {
    border-radius: 18px;
  }

  .option-grid {
    grid-template-columns: 1fr;
  }
}

/* 柱状图样式 */
.bar-chart {
  display: flex;
  align-items: flex-end;
  justify-content: space-around;
  height: 200px;
  gap: 12px;
  padding: 20px 0;
}

.bar-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.bar-wrapper {
  width: 100%;
  height: 150px;
  display: flex;
  align-items: flex-end;
  justify-content: center;
}

.bar {
  width: 100%;
  max-width: 40px;
  background: linear-gradient(180deg, #3B82F6 0%, #1E40AF 100%);
  border-radius: 4px 4px 0 0;
  transition: all 0.3s ease;
  cursor: pointer;
  position: relative;
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.3);
}

.bar:hover {
  background: linear-gradient(180deg, #60A5FA 0%, #3B82F6 100%);
  transform: scaleY(1.05);
  box-shadow: 0 6px 16px rgba(59, 130, 246, 0.4);
}

.bar-label {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.7);
  text-align: center;
}

.bar-value {
  font-size: 11px;
  color: rgba(255, 255, 255, 0.5);
  text-align: center;
}

/* 饼图样式 */
.pie-chart {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px;
}

.pie-svg {
  width: 200px;
  height: 200px;
}

.pie-segment {
  transition: all 0.3s ease;
  cursor: pointer;
}

.pie-segment:hover {
  opacity: 0.9;
  transform: scale(1.08);
  transform-origin: center;
  filter: brightness(1.1);
}

.pie-legend {
  display: flex;
  flex-direction: column;
  gap: 12px;
  width: 100%;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 12px;
  color: white;
  font-size: 14px;
}

.legend-color {
  width: 16px;
  height: 16px;
  border-radius: 4px;
  flex-shrink: 0;
}

.legend-label {
  flex: 1;
}

.legend-value {
  color: rgba(255, 255, 255, 0.7);
  font-size: 12px;
}

/* 航线列表样式 */
.route-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.route-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.route-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.route-name {
  color: white;
  font-size: 14px;
  font-weight: 500;
}

.route-count {
  color: rgba(255, 255, 255, 0.7);
  font-size: 12px;
}

.route-bar {
  width: 100%;
  height: 8px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 4px;
  overflow: hidden;
}

.route-progress {
  height: 100%;
  border-radius: 4px;
  transition: width 0.3s ease;
  background: linear-gradient(90deg, #3B82F6 0%, #1E40AF 100%);
  box-shadow: 0 2px 8px rgba(59, 130, 246, 0.3);
}

/* 对比图样式 */
.comparison-chart {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.comparison-bars {
  display: flex;
  align-items: flex-end;
  justify-content: space-around;
  height: 200px;
  gap: 12px;
  padding: 20px 0;
}

.comparison-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.comparison-month {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.7);
  text-align: center;
}

.comparison-bars-wrapper {
  display: flex;
  align-items: flex-end;
  gap: 4px;
  height: 150px;
  width: 100%;
  justify-content: center;
}

.comparison-bar {
  width: 20px;
  border-radius: 4px 4px 0 0;
  transition: all 0.3s ease;
  cursor: pointer;
}

.comparison-bar.orders {
  background: linear-gradient(180deg, #3B82F6 0%, #1D4ED8 100%);
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.3);
}

.comparison-bar.spending {
  background: linear-gradient(180deg, #10B981 0%, #047857 100%);
  box-shadow: 0 4px 12px rgba(16, 185, 129, 0.3);
}

.comparison-bar:hover {
  transform: scaleY(1.05);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.3);
}

.comparison-bar.orders:hover {
  background: linear-gradient(180deg, #60A5FA 0%, #3B82F6 100%);
}

.comparison-bar.spending:hover {
  background: linear-gradient(180deg, #34D399 0%, #10B981 100%);
}

.comparison-legend {
  display: flex;
  justify-content: center;
  gap: 24px;
}

.comparison-legend .legend-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.8);
}

.comparison-legend .legend-color {
  width: 12px;
  height: 12px;
  border-radius: 2px;
}

.comparison-legend .legend-color.orders {
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
}

.comparison-legend .legend-color.spending {
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
}

/* 响应式适配 */
@media (max-width: 768px) {
  .charts-grid {
    grid-template-columns: 1fr;
  }
  
  .bar-chart,
  .comparison-bars {
    height: 150px;
  }
  
  .pie-svg {
    width: 150px;
    height: 150px;
  }
}

/* 重点旅客预约弹窗样式 */
.special-passenger-modal {
  max-width: 600px;
  max-height: 90vh;
  overflow-y: auto;
  border: 2px solid rgba(30, 138, 230, 0.2);
  backdrop-filter: blur(20px);
}

.special-passenger-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-label {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.9);
  font-weight: 500;
}

.required {
  color: #f87171;
  margin-left: 2px;
}

.optional {
  color: rgba(255, 255, 255, 0.5);
  font-size: 12px;
  margin-left: 4px;
}

.form-input,
.form-textarea {
  width: 100%;
  padding: 14px 16px;
  border-radius: 8px;
  border: 2px solid rgba(255, 255, 255, 0.2);
  background: rgba(2, 6, 23, 0.6);
  color: #fff;
  font-size: 14px;
  font-family: inherit;
  transition: all 0.2s ease;
}

.form-input:focus,
.form-textarea:focus {
  outline: none;
  border-color: #3B82F6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.25);
  background: rgba(2, 6, 23, 0.8);
}

.form-textarea {
  resize: vertical;
  min-height: 80px;
}

.select-wrapper {
  position: relative;
  cursor: pointer;
}

.select-input {
  cursor: pointer;
  padding-right: 40px;
}

.select-arrow {
  position: absolute;
  right: 16px;
  top: 50%;
  transform: translateY(-50%);
  color: rgba(255, 255, 255, 0.6);
  font-size: 16px;
  pointer-events: none;
}

.phone-input-wrapper {
  display: flex;
  gap: 8px;
}

.country-code-select {
  width: 80px;
  padding: 12px;
  border-radius: 12px;
  border: 1px solid rgba(255, 255, 255, 0.16);
  background: rgba(2, 6, 23, 0.4);
  color: #fff;
  font-size: 14px;
  cursor: pointer;
}

.phone-input {
  flex: 1;
}

.order-selector-dropdown,
.passenger-type-dropdown,
.airport-selector-dropdown {
  position: absolute;
  top: calc(100% + 8px);
  left: 0;
  right: 0;
  background: rgba(15, 23, 42, 0.98);
  border: 2px solid rgba(30, 138, 230, 0.2);
  border-radius: 8px;
  max-height: 300px;
  overflow-y: auto;
  z-index: 1000;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.5);
  margin-top: 0;
  backdrop-filter: blur(15px);
}

.order-option {
  padding: 16px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  cursor: pointer;
  transition: all 0.2s ease;
}

.order-option:hover {
  background: rgba(59, 130, 246, 0.15);
  border-left: 3px solid #3B82F6;
}

.order-option:last-child {
  border-bottom: none;
}

.order-option.empty {
  text-align: center;
  color: rgba(255, 255, 255, 0.5);
  cursor: default;
}

.order-option-main {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
}

.order-option-main strong {
  color: #fff;
  font-size: 15px;
}

.order-option-main span {
  color: rgba(255, 255, 255, 0.7);
  font-size: 14px;
}

.order-option-meta {
  display: flex;
  gap: 12px;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.5);
}

.passenger-type-option {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  cursor: pointer;
  transition: all 0.2s ease;
  color: #fff;
}

.passenger-type-option:hover {
  background: rgba(59, 130, 246, 0.15);
  border-left: 3px solid #3B82F6;
}

.passenger-type-option:last-child {
  border-bottom: none;
}

.radio-icon {
  width: 20px;
  height: 20px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-radius: 50%;
  position: relative;
  flex-shrink: 0;
  transition: all 0.2s ease;
}

.radio-icon.checked {
  border-color: #1E8AE6;
  background: #1E8AE6;
}

.radio-icon.checked::after {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 8px;
  height: 8px;
  background: #fff;
  border-radius: 50%;
}

.airport-option {
  padding: 16px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  cursor: pointer;
  transition: all 0.2s ease;
  color: #fff;
}

.airport-option:hover {
  background: rgba(59, 130, 246, 0.15);
  border-left: 3px solid #3B82F6;
}

.airport-option:last-child {
  border-bottom: none;
}

.checkbox-group {
  display: flex;
  gap: 24px;
  flex-wrap: wrap;
}

.checkbox-item {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  user-select: none;
}

.checkbox-input {
  width: 20px;
  height: 20px;
  cursor: pointer;
  accent-color: #1E8AE6;
}

.checkbox-label {
  color: rgba(255, 255, 255, 0.9);
  font-size: 14px;
}

.info-banner {
  padding: 14px 18px;
  background: rgba(251, 191, 36, 0.18);
  border: 2px solid rgba(251, 191, 36, 0.4);
  border-radius: 8px;
  color: rgba(251, 191, 36, 0.95);
  font-size: 13px;
  line-height: 1.5;
  font-weight: 600;
}

@media (max-width: 640px) {
  .special-passenger-modal {
    max-width: 95%;
    padding: 20px;
  }
  
  .phone-input-wrapper {
    flex-direction: column;
  }
  
  .country-code-select {
    width: 100%;
  }
  
  .checkbox-group {
    flex-direction: column;
    gap: 12px;
  }
}

/* 右上角手机预览切换按钮 */
.topbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}
</style>
<template>
  <component :is="layoutComponent" v-if="layoutComponent">
    <div class="page-container order-page">
      <div class="breadcrumb">
          <span>{{ orderCopy.breadcrumb.home }}</span>
          <span class="breadcrumb-separator">/</span>
          <span>{{ orderCopy.breadcrumb.current }}</span>
        </div>

      <header class="page-header">
        <div>
          <p class="page-label" style="display: none;">{{ orderCopy.hero.badge }}</p>
          <h1 style="display: none;">{{ isFromSpecialPassenger ? '选择订单' : orderCopy.hero.title }}</h1>
          <p style="display: none;">{{ isFromSpecialPassenger ? '请选择待出行的订单用于重点旅客预约' : orderCopy.hero.description }}</p>
        </div>
        <div class="page-actions">
          <button v-if="isFromSpecialPassenger" class="ghost-btn" @click="router.push('/user-center')">返回</button>
          <button v-else class="ghost-btn" @click="handleExportCSV">{{ orderCopy.hero.actions.export }}</button>
        </div>
      </header>

      <section class="grid-two">
        <article class="glass-card search-panel admin-card">
          <div class="section-head">
            <div>
              <p class="section-label">{{ orderCopy.searchPanel.label }}</p>
              <h2>{{ orderCopy.searchPanel.title }}</h2>
            </div>
          </div>
          <div class="search-row">
            <div class="search-item">
              <label>{{ orderCopy.searchPanel.fields.orderNumber.label }}</label>
              <input type="text" v-model="searchParams.orderNumber" :placeholder="orderCopy.searchPanel.fields.orderNumber.placeholder" />
            </div>
            <div class="search-item">
              <label>{{ orderCopy.searchPanel.fields.customer.label }}</label>
              <input type="text" v-model="searchParams.customer" :placeholder="orderCopy.searchPanel.fields.customer.placeholder" />
            </div>
            <div class="search-item">
              <label>{{ orderCopy.searchPanel.fields.status.label }}</label>
              <input type="text" v-model="searchParams.status" :placeholder="orderCopy.searchPanel.fields.status.all" />
            </div>
          
          </div>
          <div class="search-actions-row">
            <button class="ghost-btn" type="button" @click="handleReset">{{ orderCopy.searchPanel.reset }}</button>
            <button class="primary-btn" type="button" @click="handleSearch">{{ orderCopy.searchPanel.actions.search }}</button>
            <button class="ghost-btn" type="button" @click="handleReset">{{ orderCopy.searchPanel.actions.clear }}</button>
          </div>
        </article>
      </section>

      <!-- 根据viewMode显示不同内容 -->
      <!-- 订单列表视图（默认） -->
      <section v-if="viewMode === 'list' || !viewMode || viewMode === ''" class="glass-card table-card admin-card">
        <div class="section-head">
          <div>
            <p class="section-label">{{ orderCopy.table.sectionLabel }}</p>
            <h2>{{ orderCopy.table.title }} ({{ total }})</h2>
          </div>
        </div>

    <!-- 改签确认弹窗 -->
    <div class="modal-overlay confirm-modal" v-if="modifyModal.visible" @click.self="closeModifyModal">
      <div class="modal-content confirm-card" @click.stop>
        <div class="modal-header">
          <div>
            <p class="confirm-label">改签申请</p>
            <h3>#{{ modifyModal.order?.id }} · {{ modifyModal.order?.route }}</h3>
            <p class="sub-tip">起点与终点、费用已预置与航空运营对齐</p>
          </div>
          <button class="close-btn" @click="closeModifyModal">×</button>
        </div>

        <div class="confirm-grid" v-if="modifyModal.order">
          <div class="confirm-block">
            <p class="block-title">航段</p>
            <p class="block-value">{{ modifyModal.order.route }}</p>
          </div>
          <div class="confirm-block">
            <p class="block-title">状态</p>
            <span :class="['status-tag', getStatusClass(modifyModal.order.status)]">{{ modifyModal.order.statusText || modifyModal.order.status }}</span>
          </div>
          <div class="confirm-block">
            <p class="block-title">改签手续费</p>
            <p class="block-value highlight">¥{{ modifyModal.changeFee }}</p>
          </div>
          <div class="confirm-block">
            <p class="block-title">预估差价</p>
            <p class="block-value">¥{{ modifyModal.priceDiff }}</p>
          </div>
          <div class="confirm-block total">
            <p class="block-title">预计总费用</p>
            <p class="block-value highlight">¥{{ modifyModal.changeFee + Math.max(0, modifyModal.priceDiff) }}</p>
            <small class="sub-tip">以航空运营结算为准</small>
          </div>
        </div>

        <label class="block-title" for="modify-reason">改签原因</label>
        <textarea
          id="modify-reason"
          v-model="modifyModal.reason"
          class="confirm-textarea"
          placeholder="例如：行程调整，需要提前一天出发"
        ></textarea>
        <p v-if="modifyModal.error" class="confirm-error">{{ modifyModal.error }}</p>

        <div class="modal-actions">
          <button class="btn-secondary" @click="closeModifyModal">取消</button>
          <button class="btn-primary" :disabled="modifyModal.loading" @click="submitModify">
            <span v-if="modifyModal.loading">提交中...</span>
            <span v-else>确认改签</span>
          </button>
        </div>
      </div>
    </div>

    <!-- 取消确认弹窗 -->
    <div class="modal-overlay confirm-modal" v-if="cancelModal.visible" @click.self="closeCancelModal">
      <div class="modal-content confirm-card" @click.stop>
        <div class="modal-header">
          <div>
            <p class="confirm-label">取消机票</p>
            <h3>#{{ cancelModal.order?.id }} · {{ cancelModal.order?.route }}</h3>
            <p class="sub-tip">航班号、起飞时间与取消手续费将同步到航空运营</p>
          </div>
          <button class="close-btn" @click="closeCancelModal">×</button>
        </div>

        <div class="confirm-grid" v-if="cancelModal.order">
          <div class="confirm-block">
            <p class="block-title">航段</p>
            <p class="block-value">{{ cancelModal.order.route }}</p>
          </div>
          <div class="confirm-block">
            <p class="block-title">航班号</p>
            <p class="block-value">{{ cancelModal.order.flightNo || '未提供' }}</p>
          </div>
          <div class="confirm-block">
            <p class="block-title">起飞时间</p>
            <p class="block-value">{{ cancelModal.order.time || '未提供' }}</p>
          </div>
          <div class="confirm-block">
            <p class="block-title">机票价格</p>
            <p class="block-value">
              <span v-if="cancelModal.ticketPrice > 0">¥{{ cancelModal.ticketPrice }}</span>
              <span v-else>未提供</span>
            </p>
          </div>
          <div class="confirm-block">
            <p class="block-title">状态</p>
            <span :class="['status-tag', getStatusClass(cancelModal.order.status)]">{{ cancelModal.order.statusText || cancelModal.order.status }}</span>
          </div>
          <div class="confirm-block">
            <p class="block-title">取消手续费</p>
            <p class="block-value highlight">¥{{ cancelModal.fee }}</p>
          </div>
          <div class="confirm-block total">
            <p class="block-title">退款说明</p>
            <p class="block-value">实际退款将以航司规则为准</p>
            <small class="sub-tip">预估手续费已写入请求体</small>
          </div>
        </div>

        <label class="block-title" for="cancel-reason">取消原因</label>
        <textarea
          id="cancel-reason"
          v-model="cancelModal.reason"
          class="confirm-textarea"
          placeholder="例如：行程取消/健康原因/审批未通过"
        ></textarea>
        <p v-if="cancelModal.error" class="confirm-error">{{ cancelModal.error }}</p>

        <div class="modal-actions">
          <button class="btn-secondary" @click="closeCancelModal">返回</button>
          <button class="btn-primary" :disabled="cancelModal.loading" @click="submitCancel">
            <span v-if="cancelModal.loading">提交中...</span>
            <span v-else>确认取消</span>
          </button>
        </div>
      </div>
    </div>

        <div class="table-container">
          <table class="admin-table data-table">
            <thead>
              <tr>
                <th width="50">
                  <input 
                    type="checkbox" 
                    :checked="isAllSelected" 
                    @change="handleSelectAll"
                    class="checkbox-input"
                  />
                </th>
                <th width="140">{{ orderCopy.table.headers.orderId }}</th>
                <th width="160">{{ orderCopy.table.headers.customer }}</th>
                <th width="200">{{ orderCopy.table.headers.route }}</th>
                <th width="120">航班号</th>
                <th width="140">机票号</th>
                <th width="160">{{ orderCopy.table.headers.departure }}</th>
                <th width="160">到达时间</th>
                <th width="120">{{ orderCopy.table.headers.amount }}</th>
                <th width="120">{{ orderCopy.table.headers.status }}</th>
                <th width="200">{{ orderCopy.table.headers.actions }}</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="order in paginatedOrders" :key="order.id">
                <td>
                  <input 
                    type="checkbox" 
                    :checked="selectedOrders.includes(order.id)"
                    @change="handleSelectOrder(order.id, $event)"
                    class="checkbox-input"
                  />
                </td>
                <td><span class="order-number">#{{ order.id }}</span></td>
                <td><span class="customer-name">{{ order.customer }}</span></td>
                <td><span class="route-text">{{ order.route }}</span></td>
                <td><span>{{ order.flightNo || '-' }}</span></td>
                <td><span>{{ order.ticketNo || '-' }}</span></td>
                <td>{{ order.time }}</td>
                <td>{{ order.arrivalTime || '-' }}</td>
                <td><span class="amount">¥{{ order.amount || '0' }}</span></td>
                <td>
                  <span :class="['status-tag', getStatusClass(order.status)]">{{ order.statusText }}</span>
                </td>
                <td>
                  <div class="action-buttons">
                    <button v-if="isFromSpecialPassenger" class="action-btn select-btn" @click="handleSelectOrderForSpecialPassenger(order)">选择</button>
                    <button class="action-btn detail-btn" @click="handleViewDetail(order)">详情</button>
                    <button class="action-btn delete-btn" @click="handleDelete(order)" title="删除订单（软删除，前端隐藏）">删除</button>
                    <button v-if="isAdmin && order.status === 'deleted'" class="action-btn restore-btn" @click="handleRestore(order)" title="恢复订单">恢复</button>
                    <button
                      v-if="order.status === '待支付' || order.statusText === '待支付'"
                      class="action-btn pay-btn"
                      @click="handlePayOrder(order)"
                      title="支付订单"
                    >
                      支付
                    </button>
                    <button
                      v-if="order.status === '待退款' || order.statusText === '待退款'"
                      class="action-btn refund-btn"
                      @click="handleQuickRefund(order)"
                      title="发起退款"
                    >
                      退款
                    </button>
                    
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <div class="pagination">
          <div class="pagination-info">
            {{ orderCopy.pagination.infoPrefix }} {{ total }}
            {{ orderCopy.pagination.infoMiddle }}
            <span>每页</span>
            <select v-model.number="pageSize" @change="() => { currentPage = 1; loadOrders() }" style="margin:0 8px;">
              <option :value="5">5</option>
              <option :value="10">10</option>
              <option :value="20">20</option>
              <option :value="50">50</option>
            </select>
            {{ orderCopy.pagination.infoSuffix }}
          </div>
          <div class="pagination-controls">
            <button class="page-btn" :disabled="currentPage === 1" @click="goToPage(currentPage - 1)">
              &lt;
            </button>
            <span class="page-number">{{ currentPage }} / {{ totalPages }}</span>
            <button class="page-btn" :disabled="currentPage === totalPages" @click="goToPage(currentPage + 1)">
              &gt;
            </button>
            <div class="page-jump">
              <span>{{ orderCopy.pagination.jump }}</span>
              <input
                type="number"
                v-model.number="jumpPage"
                :min="1"
                :max="totalPages"
                @keyup.enter="goToPage(jumpPage)"
              />
              <span>{{ orderCopy.pagination.page }}</span>
            </div>
          </div>
        </div>
      </section>

      <!-- 机票管理视图 -->
      <section v-if="viewMode === 'manage'" class="glass-card manage-card">
        <div class="section-head">
          <div>
            <p class="section-label">{{ orderCopy.manageView.label }}</p>
            <h2>{{ orderCopy.manageView.title }}</h2>
          </div>
          <button class="ghost-btn" @click="router.push({ path: '/portal/orders', query: { view: 'list' } })">
            {{ orderCopy.manageView.back }}
          </button>
        </div>
        <div class="manage-content">
          <p v-if="filteredOrders.length === 0" class="empty-message">{{ orderCopy.manageView.empty }}</p>
          <div v-else>
            <p>{{ orderCopy.manageView.select }}</p>
            <div class="manage-orders-list">
              <div 
                v-for="order in filteredOrders" 
                :key="order.id" 
                class="manage-order-item"
              >
                <div class="order-info">
                  <h4>#{{ order.id }}</h4>
                  <p>乘客：{{ order.customer || '未提供' }}</p>
                  <p>航线：{{ order.route || '未提供' }}</p>
                  <p>航班号：{{ order.flightNo || '未提供' }}</p>
                  <p>机票号：{{ order.ticketNo || '未提供' }}</p>
                  <p>起飞时间：{{ order.time || '未提供' }}</p>
                  <p>金额：¥{{ order.amount || 0 }}</p>
                  <p>
                    状态：
                    <span :class="['status-tag', getStatusClass(order.status)]">{{ order.statusText }}</span>
                  </p>
                </div>
                <div class="manage-actions">
                  <button class="primary-btn" @click="handleModifyOrder(order.id)">{{ orderCopy.manageView.actions.modify }}</button>
                  <button class="ghost-btn" @click="handleCancelOrder(order.id)">{{ orderCopy.manageView.actions.cancel }}</button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- 退订申请视图 -->
      <section v-if="viewMode === 'refund'" class="glass-card refund-card">
        <div class="section-head">
          <div>
            <p class="section-label">{{ orderCopy.refundView.label }}</p>
            <h2>{{ orderCopy.refundView.title }}</h2>
          </div>
          <button class="ghost-btn" @click="router.push({ path: '/portal/orders', query: { view: 'list' } })">
            {{ orderCopy.refundView.back }}
          </button>
        </div>
        <div class="refund-content">
          <p v-if="filteredOrders.length === 0" class="empty-message">{{ orderCopy.refundView.empty }}</p>
          <div v-else>
            <p>{{ orderCopy.refundView.select }}</p>
            <div class="refund-orders-list">
              <div 
                v-for="order in filteredOrders" 
                :key="order.id" 
                class="refund-order-item"
              >
                <div class="order-info">
                  <h4>#{{ order.id }}</h4>
                  <p>乘客：{{ order.customer || '未提供' }}</p>
                  <p>航线：{{ order.route || '未提供' }}</p>
                  <p>航班号：{{ order.flightNo || '未提供' }}</p>
                  <p>机票号：{{ order.ticketNo || '未提供' }}</p>
                  <p>起飞时间：{{ order.time || '未提供' }}</p>
                  <p>金额：¥{{ order.amount || 0 }}</p>
                  <p>
                    状态：
                    <span :class="['status-tag', getStatusClass(order.status)]">{{ order.statusText }}</span>
                  </p>
                </div>
                <div class="refund-actions">
                  <button 
                    class="primary-btn" 
                    @click="handleRefundOrder(order.id)"
                    :disabled="order.status === 'refunded' || order.status === 'cancelled'"
                  >
                    {{ order.status === 'refunded' ? orderCopy.refundView.applied : orderCopy.refundView.apply }}
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      <div 
        class="modal-overlay order-detail-modal" 
        v-if="modifyModal.visible" 
        @click="closeModifyModal"
      >
        <div class="modal-content" @click.stop>
          <div class="modal-header">
            <h3>机票改签 #{{ modifyModal.order?.id }}</h3>
            <button class="close-btn" @click="closeModifyModal">×</button>
          </div>
          <div class="modal-body" v-if="modifyModal.order">
            <div class="detail-row">
              <span class="detail-label">航班号</span>
              <span class="detail-value">{{ modifyModal.order.flightNo || '未提供' }}</span>
            </div>
            <div class="detail-row">
              <span class="detail-label">机票号</span>
              <span class="detail-value">{{ modifyModal.order.ticketNo || '未提供' }}</span>
            </div>
            <div class="detail-row">
              <span class="detail-label">原航线</span>
              <span class="detail-value">{{ modifyModal.order.route }}</span>
            </div>
            <div class="detail-row" v-if="parseRouteParts(modifyModal.order.route).from || parseRouteParts(modifyModal.order.route).to">
              <span class="detail-label">起止城市</span>
              <span class="detail-value">
                {{ parseRouteParts(modifyModal.order.route).from || '未提供' }} → {{ parseRouteParts(modifyModal.order.route).to || '未提供' }}
              </span>
            </div>
            <div class="detail-row">
              <span class="detail-label">改签手续费</span>
              <input 
                type="number" 
                v-model.number="modifyModal.changeFee" 
                min="0" 
                class="input"
              />
            </div>
            <div class="detail-row">
              <span class="detail-label">票价差额</span>
              <input 
                type="number" 
                v-model.number="modifyModal.priceDiff" 
                step="0.01" 
                class="input"
                placeholder="若无需补差价可填0"
              />
            </div>

            <div class="detail-row">
              <span class="detail-label">查询新航班</span>
              <div class="flight-search-inline">
                <div class="search-row">
                  <div class="search-item">
                    <label>出发城市</label>
                    <input v-model="modifyModal.search.departure" type="text" placeholder="例如：北京" />
                  </div>
                  <div class="search-item">
                    <label>到达城市</label>
                    <input v-model="modifyModal.search.destination" type="text" placeholder="例如：上海" />
                  </div>
                  <div class="search-item">
                    <label>出发日期</label>
                    <input v-model="modifyModal.search.date" type="date" />
                  </div>
                  <div class="search-item small">
                    <label>人数</label>
                    <select v-model="modifyModal.search.passengers">
                      <option value="1">1 人</option>
                      <option value="2">2 人</option>
                      <option value="3">3 人</option>
                      <option value="4">4 人</option>
                    </select>
                  </div>
                  <button 
                    type="button" 
                    class="primary-btn compact" 
                    :disabled="modifyModal.searchLoading"
                    @click="handleSearchFlightsForModify"
                  >
                    {{ modifyModal.searchLoading ? '搜索中...' : '搜索航班' }}
                  </button>
                </div>
                <div class="hint-row">
                  <span class="pill pill-info">实时</span>
                  <span class="hint-text">
                    直接调用后端航班搜索接口，结果均为实时可改签的航班。
                  </span>
                </div>
                <div class="flight-results-wrapper">
                  <div v-if="modifyModal.searchLoading" class="empty-result">正在加载航班...</div>
                  <div v-else-if="!modifyModal.searchResults.length" class="empty-result">暂无搜索结果，请调整条件</div>
                  <div 
                    v-else 
                    class="flight-results-mini"
                  >
                    <div 
                      v-for="flight in modifyModal.searchResults" 
                      :key="flight.id || flight.flightNumber" 
                      class="flight-item-mini"
                      :class="{ active: modifyModal.selectedFlight && (modifyModal.selectedFlight.id === flight.id || modifyModal.selectedFlight.flightNumber === flight.flightNumber) }"
                      @click="selectFlightForModify(flight)"
                    >
                      <div class="flight-main">
                        <div class="flight-route">
                          <span class="city">{{ flight.departure }}</span>
                          <span class="arrow">→</span>
                          <span class="city">{{ flight.destination }}</span>
                        </div>
                        <div class="flight-time">
                          <span>{{ formatTime(flight.departureTime || '') }}</span>
                          <span>-</span>
                          <span>{{ formatTime(flight.arrivalTime || '') }}</span>
                        </div>
                      </div>
                      <div class="flight-side">
                        <div class="price">¥{{ flight.price }}</div>
                        <div class="flight-no">{{ flight.flightNumber }}</div>
                      </div>
                    </div>
                  </div>
                </div>
                <div v-if="modifyModal.selectedFlight" class="flight-selected">
                  <div class="pill pill-success">已选航班</div>
                  <div class="selected-info">
                    <div class="route">{{ modifyModal.selectedFlight.departure }} → {{ modifyModal.selectedFlight.destination }}</div>
                    <div class="meta">
                      <span class="flight-id">航班号：{{ modifyModal.selectedFlight.flightNumber }}</span>
                      <span class="time">{{ formatTime(modifyModal.selectedFlight.departureTime || '') }} - {{ formatTime(modifyModal.selectedFlight.arrivalTime || '') }}</span>
                      <span class="price">¥{{ modifyModal.selectedFlight.price }}</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <div class="detail-row">
              <span class="detail-label">改签原因</span>
              <textarea 
                v-model="modifyModal.reason" 
                rows="3" 
                class="input textarea"
                placeholder="请填写改签原因（必填）"
              ></textarea>
            </div>
            <p class="error-text" v-if="modifyModal.error">{{ modifyModal.error }}</p>
          </div>
          <div class="modal-footer">
            <button class="ghost-btn" @click="closeModifyModal">取消</button>
            <button class="primary-btn" :disabled="modifyModal.loading" @click="submitModify">
              {{ modifyModal.loading ? '提交中...' : '确认改签' }}
            </button>
          </div>
        </div>
      </div>

      <div 
        class="modal-overlay order-detail-modal" 
        v-if="cancelModal.visible" 
        @click="closeCancelModal"
      >
        <div class="modal-content" @click.stop>
          <div class="modal-header">
            <h3>取消机票 #{{ cancelModal.order?.id }}</h3>
            <button class="close-btn" @click="closeCancelModal">×</button>
          </div>
          <div class="modal-body" v-if="cancelModal.order">
            <div class="detail-row">
              <span class="detail-label">航线</span>
              <span class="detail-value">{{ cancelModal.order.route }}</span>
            </div>
            <div class="detail-row">
              <span class="detail-label">航班号</span>
              <span class="detail-value">{{ cancelModal.order.flightNo || '未提供' }}</span>
            </div>
            <div class="detail-row">
              <span class="detail-label">起飞时间</span>
              <span class="detail-value">{{ cancelModal.order.time || '未提供' }}</span>
            </div>
            <div class="detail-row">
              <span class="detail-label">机票价格</span>
              <span class="detail-value">
                <span v-if="cancelModal.ticketPrice > 0">¥{{ cancelModal.ticketPrice }}</span>
                <span v-else>未提供</span>
              </span>
            </div>
            <div class="detail-row">
              <span class="detail-label">取消手续费</span>
              <input 
                type="number" 
                v-model.number="cancelModal.fee" 
                min="0" 
                class="input"
                :disabled="!isAdmin"
              />
            </div>
            <div class="detail-row">
              <span class="detail-label">取消原因</span>
              <textarea 
                v-model="cancelModal.reason" 
                rows="3" 
                class="input textarea"
                placeholder="请填写取消原因（必填）"
              ></textarea>
            </div>
            <p class="error-text" v-if="cancelModal.error">{{ cancelModal.error }}</p>
          </div>
          <div class="modal-footer">
            <button class="ghost-btn" @click="closeCancelModal">返回</button>
            <button class="primary-btn" :disabled="cancelModal.loading" @click="submitCancel">
              {{ cancelModal.loading ? '提交中...' : '确认取消' }}
            </button>
          </div>
        </div>
      </div>

      <div class="modal-overlay order-detail-modal" v-if="detailModal.visible" @click="closeDetailModal">
        <div class="modal-content" @click.stop>
          <div class="modal-header">
            <h3>{{ orderCopy.detailModal.title }} #{{ detailModal.order?.id }}</h3>
            <button class="close-btn" @click="closeDetailModal">×</button>
          </div>
          <div class="modal-body" v-if="detailModal.order">
            <div class="detail-row">
              <span class="detail-label">{{ orderCopy.detailModal.fields.customer }}</span>
              <span class="detail-value">{{ detailModal.order.customer }}</span>
            </div>
            <div class="detail-row">
              <span class="detail-label">{{ orderCopy.detailModal.fields.route }}</span>
              <span class="detail-value">{{ detailModal.order.route }}</span>
            </div>
            <div class="detail-row">
              <span class="detail-label">{{ orderCopy.detailModal.fields.departure }}</span>
              <span class="detail-value">{{ detailModal.order.time }}</span>
            </div>
            <div class="detail-row">
              <span class="detail-label">{{ orderCopy.detailModal.fields.status }}</span>
              <span :class="['status-tag', getStatusClass(detailModal.order.status)]">
                {{ detailModal.order.status }}
              </span>
            </div>
            <div class="detail-row">
              <span class="detail-label">{{ orderCopy.detailModal.fields.amount }}</span>
              <span class="detail-value amount-value">¥{{ detailModal.order.amount || 0 }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </component>
</template>

<script setup lang="ts">
import PassengerLayout from '../components/layout/PassengerLayout.vue';
import AdminLayout from '../components/AdminLayout.vue';
import { computed, ref, reactive, onMounted, watch, nextTick, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import store from '../services/store'
import { useI18n } from '../services/i18n'
import { orderApi, orderManagementApi, apiUtils, flightApi, ticketApi } from '../services/api'

const route = useRoute()
const router = useRouter()
const { t, messages } = useI18n()
const orderCopy = computed(() => messages.value.orderManagement)

// 根据路由参数决定显示的内容
const viewMode = computed(() => {
  const view = route.query.view as string
  // 确保viewMode有默认值
  return view || 'list'
})
const actionType = computed(() => route.query.action as string || '')

// 检测是否从预约页面跳转来
const isFromSpecialPassenger = computed(() => {
  return route.query.from === 'special-passenger' && route.query.selectMode === 'true'
})

// 调试信息
watch([viewMode, actionType], ([newView, newAction]) => {
  console.log('视图模式:', newView, '操作类型:', newAction)
}, { immediate: true })

// 根据角色动态选择布局，避免跨角色视觉交叉
const layoutComponent = computed(() => {
  const role = store.userState.role
  if (!role) {
    // 如果角色未定义，尝试从sessionStorage获取
    const storedRole = sessionStorage.getItem('userRole')
    if (storedRole) {
      return storedRole === 'admin' ? AdminLayout : PassengerLayout
    }
    // 默认使用PassengerLayout
    return PassengerLayout
  }
  return role === 'admin' ? AdminLayout : PassengerLayout
})

interface OrderItem {
  id: string  // 订单号（orderNo），用于显示
  orderId?: number | string  // 订单ID（数据库主键），用于查询详情
  customer: string
  route: string
  flightNo?: string
  ticketNo?: string
  time: string
  status: string  // 直接使用后端返回的状态字符串
  amount?: number
  arrivalTime?: string
  statusText?: string
  [key: string]: any
}

const searchParams = reactive({
  orderNumber: '',
  customer: '',
  status: '',
  startDate: '',
  endDate: ''
})

// 确保布局组件正确加载
watch(() => store.userState.role, () => {
  // 当角色变化时，确保布局组件重新渲染
}, { immediate: true })

// 监听主题同步事件，确保主题修改后页面同步更新
const handleThemeSync = (event: any) => {
  const { type, value } = event.detail
  // 重新应用主题设置
  const root = document.documentElement
  
  if (type === 'theme') {
    if (value === 'auto') {
      const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches
      root.setAttribute('data-theme', prefersDark ? 'dark' : 'light')
    } else {
      root.setAttribute('data-theme', value)
    }
  } else if (type === 'color') {
    root.style.setProperty('--primary-color', value)
    root.style.setProperty('--color-primary', value)
  } else if (type === 'fontSize') {
    const fontSizeMap = {
      small: '14px',
      medium: '16px',
      large: '18px'
    }
    const sizeKey = (value as keyof typeof fontSizeMap) || 'medium'
    root.style.setProperty('--base-font-size', fontSizeMap[sizeKey])
    root.style.setProperty('--font-size-base', fontSizeMap[sizeKey])
  } else if (type === 'showAnimations') {
    if (!value) {
      root.style.setProperty('--animation-duration', '0s')
    } else {
      root.style.setProperty('--animation-duration', '0.3s')
    }
  } else if (type === 'compactMode') {
    if (value) {
      root.setAttribute('data-compact', 'true')
    } else {
      root.removeAttribute('data-compact')
    }
  }
}

// 使用store管理订单数据 - 合并所有初始化逻辑
onMounted(async () => {
  // 确保用户状态已初始化
  if (!store.userState.role) {
    store.initializeUserState()
  }
  
  // 确保主题已初始化
  if (!store.themeState.theme) {
    store.initializeThemeState()
  }
  
  // 应用当前主题设置
  const root = document.documentElement
  if (store.themeState.theme === 'auto') {
    const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches
    root.setAttribute('data-theme', prefersDark ? 'dark' : 'light')
  } else {
    root.setAttribute('data-theme', store.themeState.theme)
  }
  root.style.setProperty('--primary-color', store.themeState.primaryColor)
  root.style.setProperty('--color-primary', store.themeState.primaryColor)
  
  // 监听主题同步事件
  window.addEventListener('theme-sync', handleThemeSync)
  
  // 从后端API加载订单数据
  try {
    await loadOrders()
    console.log('初始加载完成，订单数量:', store.orderState.orders.length)
  } catch (error) {
    console.error('初始加载订单失败:', error)
  }
  
  
  // 根据viewMode确保正确显示
  console.log('当前视图模式:', viewMode.value, '订单数量:', store.orderState.orders.length)
  
  // 确保页面内容正确渲染
  nextTick(() => {
    // 强制更新视图
    console.log('页面渲染完成，当前视图:', viewMode.value)
  })
})

// 组件卸载时移除事件监听
onUnmounted(() => {
  window.removeEventListener('theme-sync', handleThemeSync)
})

const currentPage = ref(1)
const pageSize = ref(5)
const jumpPage = ref(1)

// 选中的订单ID列表
const selectedOrders = ref<string[]>([])

// 全选状态
const isAllSelected = computed(() => {
  if (paginatedOrders.value.length === 0) return false
  return paginatedOrders.value.every(order => selectedOrders.value.includes(order.id))
})

// 处理单个订单选择
const handleSelectOrder = (orderId: string, event: Event) => {
  const target = event.target as HTMLInputElement
  if (target.checked) {
    if (!selectedOrders.value.includes(orderId)) {
      selectedOrders.value.push(orderId)
    }
  } else {
    const index = selectedOrders.value.indexOf(orderId)
    if (index > -1) {
      selectedOrders.value.splice(index, 1)
    }
  }
}

// 处理全选
const handleSelectAll = (event: Event) => {
  const target = event.target as HTMLInputElement
  if (target.checked) {
    // 选中当前页所有订单
    paginatedOrders.value.forEach(order => {
      if (!selectedOrders.value.includes(order.id)) {
        selectedOrders.value.push(order.id)
      }
    })
  } else {
    // 取消选中当前页所有订单
    const currentPageOrderIds = paginatedOrders.value.map(order => order.id)
    selectedOrders.value = selectedOrders.value.filter(id => !currentPageOrderIds.includes(id))
  }
}

const detailModal = reactive({
  visible: false,
  order: null as OrderItem | null
})

const modifyModal = reactive({
  visible: false,
  order: null as (OrderItem & { statusText?: string }) | null,
  changeFee: 80,
  priceDiff: 0,
  reason: '',
  error: '',
  loading: false,
  search: {
    departure: '',
    destination: '',
    date: '',
    passengers: '1'
  },
  searchResults: [] as any[],
  searchLoading: false,
  selectedFlight: null as any
})

const cancelModal = reactive({
  visible: false,
  order: null as (OrderItem & { statusText?: string }) | null,
  ticketPrice: 0,
  fee: 120,
  reason: '',
  error: '',
  loading: false
})

// 不再需要状态映射，直接使用后端返回的状态字符串

// 后端分页数据
const total = ref(0)
const totalPages = computed(() => Math.ceil(total.value / pageSize.value))

// 订单列表（从后端获取，已分页）
const orders = ref<OrderItem[]>([])

// 显示订单列表（直接使用后端返回的分页数据）
const isTicketedStatus = (order: any) => {
  const status = String(order.status || order.statusText || '').toLowerCase()
  const statusText = String(order.statusText || order.status || '').toLowerCase()
  return (
    status === 'ticketed' ||
    statusText === '待出行' ||
    statusText === '已出票' ||
    order.status === '待出行' ||
    order.status === '已出票'
  )
}

const filteredOrders = computed(() => {
  let result = orders.value.map(item => ({
    ...item,
    statusText: item.status  // 直接使用后端状态字符串
  }))
  
  // 如果是从预约页面来的，只显示"待出行"状态的订单
  if (isFromSpecialPassenger.value) {
    const beforeFilter = result.length
    console.log('从预约页面来，开始过滤订单，原始订单数量:', beforeFilter)
    console.log('原始订单状态列表:', result.map(o => ({ id: o.id, status: o.status, statusText: o.statusText })))
    
    result = result.filter(order => {
      const isMatch = isTicketedStatus(order)
      if (!isMatch) {
        console.log('订单被过滤掉:', { id: order.id, status: order.status, statusText: order.statusText })
      }
      return isMatch
    })
    
    console.log('过滤后订单数量:', result.length, '，过滤掉:', beforeFilter - result.length)
    // 更新total为过滤后的数量
    if (beforeFilter !== result.length) {
      total.value = result.length
    }
  }

  // 普通乘客在改签/取消入口，仅展示待出行订单
  if (!isAdmin.value && (viewMode.value === 'manage' || viewMode.value === 'refund')) {
    result = result.filter(order => isTicketedStatus(order))
  }
  
  return result
})

// 分页后的订单（后端已分页，直接使用）
const paginatedOrders = computed(() => filteredOrders.value)

// 监听路由参数变化，确保内容正确显示（需在 filteredOrders 定义之后）
watch(() => route.query.view, () => {
  currentPage.value = 1
  if (filteredOrders.value.length === 0) {
    console.log('订单列表为空，必要时可重新加载')
  }
}, { immediate: true })

// 监听是否从预约页面跳转来，如果是则重新加载订单
watch(() => [route.query.from, route.query.selectMode], () => {
  if (isFromSpecialPassenger.value) {
    currentPage.value = 1
    loadOrders()
  }
}, { immediate: true })

const parseRouteParts = (route: string) => {
  if (!route) return { from: '', to: '' }
  const parts = route.split(/[-–—>→至到]/).map(p => p.trim()).filter(Boolean)
  if (parts.length >= 2) {
    return { from: parts[0], to: parts[1] }
  }
  return { from: route, to: '' }
}

const handleSearch = async () => {
  try {
    currentPage.value = 1
    // 重新加载订单数据
    await loadOrders()
  } catch (error: any) {
    console.error('查询失败:', error)
    alert(error?.message || '查询失败，请重试')
  }
}

const handleReset = async () => {
  searchParams.orderNumber = ''
  searchParams.customer = ''
  searchParams.status = ''
  searchParams.startDate = ''
  searchParams.endDate = ''
  currentPage.value = 1
  // 重新加载订单数据
  await loadOrders()
}

const handleExportCSV = async () => {
  try {
    // 构建通用筛选参数（不带分页）用于导出
    const params: any = {}
    if (searchParams.orderNumber) params.orderNumber = searchParams.orderNumber
    if (searchParams.customer) params.customer = searchParams.customer
    if (searchParams.status) params.status = searchParams.status
    if (searchParams.startDate) params.startDate = searchParams.startDate
    if (searchParams.endDate) params.endDate = searchParams.endDate

    // 管理员侧直接调用后端导出接口（返回文件流）
    if (isAdmin.value && (orderManagementApi as any)?.exportOrdersCSV) {
      const blob = await (orderManagementApi as any).exportOrdersCSV(params)
      const url = URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = url
      link.download = `${orderCopy.value.csv.fileName}_${new Date().toISOString().split('T')[0]}.csv`
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      URL.revokeObjectURL(url)
      alert(t('orderManagement.alerts.csvExported'))
      return
    }

    // 非管理员：后端可能没有公开导出接口，改为分页拉取所有数据并在前端合并导出
    const pageSizeForExport = 200
    let pageIndex = 0
    let allOrders: any[] = []
    // 首次请求以获取总数
    const firstResult: any = await (orderApi as any).getOrders({ page: pageIndex, size: pageSizeForExport, ...params })
    const firstOrders = firstResult.orders || firstResult.data?.orders || []
    const totalCount = firstResult.total || firstResult.data?.total || firstOrders.length
    allOrders = allOrders.concat(firstOrders)
    const totalPagesForExport = Math.ceil((totalCount || 0) / pageSizeForExport)

    for (pageIndex = 1; pageIndex < totalPagesForExport; pageIndex++) {
      const res: any = await (orderApi as any).getOrders({ page: pageIndex, size: pageSizeForExport, ...params })
      const pageOrders = res.orders || res.data?.orders || []
      if (!pageOrders || !pageOrders.length) break
      allOrders = allOrders.concat(pageOrders)
    }

    if (!allOrders.length) {
      alert('当前筛选条件下没有可导出的订单')
      return
    }

    // 构建 CSV 内容（字段与原来保持一致）
    const headers = orderCopy.value.csv.headers
    const rows = allOrders.map((order: any) => {
      const orderNo = order.orderNo || String(order.id || '')
      const customerName = order.passengerName || order.customer || order.passenger_name || ''
      const routeStr = order.route || ''
      const flightNo = order.flightNo || order.flight_no || ''
      const ticketNo = order.ticketNo || order.ticket_no || ''
      const timeStr = order.departureTime || order.createdAt || ''
      const amountVal = (() => {
        const v = order.totalAmount ?? order.amount ?? 0
        return typeof v === 'number' ? v : String(v)
      })()
      const statusText = order.status || ''
      return [orderNo, customerName, routeStr, flightNo, ticketNo, timeStr, statusText, amountVal]
    })

    const csvContent = [headers.join(','), ...rows.map((row: any[]) => row.map(cell => `"${String(cell ?? '')}"`).join(','))].join('\n')
    const blob = new Blob(['\ufeff' + csvContent], { type: 'text/csv;charset=utf-8;' })
    const link = document.createElement('a')
    link.href = URL.createObjectURL(blob)
    link.download = `${orderCopy.value.csv.fileName}_${new Date().toISOString().split('T')[0]}.csv`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    URL.revokeObjectURL(link.href)

    alert(t('orderManagement.alerts.csvExported'))
  } catch (error: any) {
    console.error('导出失败:', error)
    alert(error?.message || '导出失败，请重试')
  }
}

// 查看订单详情
const handleViewDetail = async (order: OrderItem) => {
  try {
    console.log('查看订单详情，订单:', order)
    
    let orderDetail: any
    
    // 根据角色使用不同的API
    if (isAdmin.value) {
      // 管理员使用 orderManagementApi，使用订单ID（数据库主键）
      const orderId = order.orderId || order.id
      const result = await orderManagementApi.getOrderById(orderId)
      if (result && result.success && result.data) {
        orderDetail = result.data
      } else {
        orderDetail = result
      }
    } else {
      // 乘客使用 orderApi，使用订单号
      const currentUser = apiUtils.getCurrentUser()
      if (!currentUser || !currentUser.id) {
        alert('请先登录')
        return
      }
      orderDetail = await orderApi.getOrderDetail(order.id)
      if (orderDetail && orderDetail.success && orderDetail.data) {
        orderDetail = orderDetail.data
      }
    }
    
    console.log('订单详情数据:', orderDetail)
    
    // 处理金额：可能是BigDecimal对象或数字
    let amount = order.amount || 0
    if (orderDetail.totalAmount !== undefined && orderDetail.totalAmount !== null) {
      if (typeof orderDetail.totalAmount === 'object' && orderDetail.totalAmount !== null) {
        try {
          amount = parseFloat(orderDetail.totalAmount.toString()) || 0
        } catch (e) {
          amount = parseFloat(String(orderDetail.totalAmount.valueOf ? orderDetail.totalAmount.valueOf() : orderDetail.totalAmount)) || 0
        }
      } else {
        amount = parseFloat(String(orderDetail.totalAmount)) || 0
      }
    }
    
    // 处理日期时间
    let timeStr = order.time
    if (orderDetail.departureTime) {
      timeStr = formatDateTime(orderDetail.departureTime)
    } else if (orderDetail.createdAt) {
      timeStr = formatDateTime(orderDetail.createdAt)
    }
    
    // 更新详情模态框
    detailModal.order = {
      id: orderDetail.orderNo || order.id,
      customer: orderDetail.passengerName || order.customer,
      route: orderDetail.route || order.route,
      time: timeStr,
      status: orderDetail.status || 'created',  // 直接使用后端状态
      amount: amount
    }
    detailModal.visible = true
  } catch (error: any) {
    console.error('获取订单详情失败:', error)
    alert(error?.message || '获取订单详情失败，请重试')
  }
}

// 快捷入口：在列表中直接对处于“待退款”状态的订单发起退款请求
// - 管理员保持原有调用支付宝退款接口的逻辑不变
// - 普通乘客直接调用后端乘客退款接口（不接入支付宝），将订单与机票状态更新为中文 "已完成"
const handleQuickRefund = async (order: OrderItem) => {
  if (!order) return
  const confirmMsg = `确认对订单 ${order.id} 发起退款，金额 ¥${order.amount ?? 0} 吗？`
  if (!confirm(confirmMsg)) return

  try {
    // 管理员保持原有逻辑：调用支付宝退款接口
    if (isAdmin.value) {
      const payload: any = {
        orderNo: order.id,
        refundAmount: order.amount ?? 0,
        reason: '列表快捷退款'
      }
      if ((order as any).tradeNo) {
        payload.tradeNo = (order as any).tradeNo
      }
      const backendUrl = `${location.protocol}//${location.hostname}:8080/api/alipay/refund`
      const resp = await fetch(backendUrl, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
      })
      if (!resp.ok) {
        const text = await resp.text()
        console.error('退款接口返回非 2xx：', resp.status, text)
        alert('退款失败：' + (text || `状态码 ${resp.status}`))
        return
      }
      let data: any = null
      try {
        data = await resp.json()
      } catch (e) {
        const textFallback = await resp.text()
        console.warn('解析退款响应 JSON 失败，使用文本回退：', e, textFallback)
        data = { success: true, message: textFallback || '无内容返回，但响应状态为成功' }
      }
      if (data && data.success) {
        alert('退款已提交：' + (data.message || '成功'))
        await loadOrders()
      } else {
        console.error('退款失败：', data)
        alert('退款失败：' + (data?.message || '请查看控制台以获取更多信息'))
      }
    } else {
      // 普通乘客：直接调用乘客退款接口，后端将订单和机票标记为“已完成”
      const payload: any = {
        orderNo: order.id
      }
      const backendUrl = `${location.protocol}//${location.hostname}:8080/passenger/orders/refund`
      const resp = await fetch(backendUrl, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
      })
      if (!resp.ok) {
        const text = await resp.text()
        console.error('乘客退款接口返回非 2xx：', resp.status, text)
        alert('退款失败：' + (text || `状态码 ${resp.status}`))
        return
      }
      let data: any = null
      try {
        data = await resp.json()
      } catch (e) {
        const textFallback = await resp.text()
        data = { success: true, message: textFallback || '无内容返回，但响应状态为成功' }
      }
      if (data && data.success) {
        alert('退款成功：' + (data.message || '已完成'))
        await loadOrders()
      } else {
        console.error('退款失败：', data)
        alert('退款失败：' + (data?.message || '请查看控制台以获取更多信息'))
      }
    }
  } catch (err) {
    console.error('发起退款请求失败：', err)
    alert('发起退款请求失败，请重试')
  }
}

// 发起订单支付（快捷入口）
const handlePayOrder = async (order: OrderItem) => {
  if (!order) return
  const confirmMsg = `确认要支付订单 ${order.id} 金额 ¥${order.amount ?? 0} 吗？`
  if (!confirm(confirmMsg)) return

  try {
    // 打开空窗口以避免浏览器拦截
    const payWindow = window.open('about:blank', '_blank')
    const amount = Number(order.amount || 0)
    // 使用 orderApi.payOrder 发起后端支付请求，后端返回支付宝 page HTML
    if (!(orderApi && (orderApi as any).payOrder)) {
      throw new Error('支付接口未定义')
    }
    const html = await (orderApi as any).payOrder({ orderNo: order.id, amount })
    if (payWindow && !payWindow.closed) {
      payWindow.document.open()
      payWindow.document.write(html)
      payWindow.document.close()
    } else {
      window.document.open()
      window.document.write(html)
      window.document.close()
    }
  } catch (e: any) {
    console.error('发起支付失败：', e)
    alert('发起支付失败：' + (e?.message || e))
  }
}

// 格式化日期时间（严格按照数据库实际时间显示，不进行时区转换）
const formatDateTime = (dateTime: string | Date) => {
  if (!dateTime) return ''
  
  // 如果是字符串，直接从字符串中提取日期和时间，不进行时区转换
  if (typeof dateTime === 'string') {
    // 处理常见的日期时间格式：
    // "2025-12-05 08:00:00" 或 "2025-12-05T08:00:00" 或 "2025-12-05T08:00:00Z"
    let dateStr = dateTime.trim()
    
    // 移除时区信息（Z, +08:00, -05:00等），只保留日期和时间部分
    // 移除末尾的Z
    if (dateStr.endsWith('Z')) {
      dateStr = dateStr.slice(0, -1)
    }
    // 移除时区偏移（+08:00, -05:00等）
    const timezoneMatch = dateStr.match(/([+-]\d{2}:\d{2})$/);
    if (timezoneMatch) {
      dateStr = dateStr.slice(0, -timezoneMatch[0].length)
    }
    
    // 将T替换为空格，统一格式
    dateStr = dateStr.replace('T', ' ')
    
    // 提取日期和时间部分（格式：YYYY-MM-DD HH:mm:ss 或 YYYY-MM-DD HH:mm）
    const match = dateStr.match(/^(\d{4}-\d{2}-\d{2})\s+(\d{2}:\d{2})(?::\d{2})?/)
    if (match) {
      const [, datePart, timePart] = match
      // 只返回日期和时间，不包含秒
      return `${datePart} ${timePart}`
    }
    
    // 如果格式不匹配，尝试直接返回格式化后的字符串
    return dateStr
  } else {
    // 如果是Date对象，直接格式化（这种情况较少，因为后端通常返回字符串）
    const year = dateTime.getFullYear()
    const month = String(dateTime.getMonth() + 1).padStart(2, '0')
    const day = String(dateTime.getDate()).padStart(2, '0')
    const hours = String(dateTime.getHours()).padStart(2, '0')
    const minutes = String(dateTime.getMinutes()).padStart(2, '0')
    return `${year}-${month}-${day} ${hours}:${minutes}`
  }
}

// 判断是否为管理员
const isAdmin = computed(() => {
  const role = store.userState.role
  if (!role) {
    const storedRole = sessionStorage.getItem('userRole')
    return storedRole === 'admin'
  }
  return role === 'admin'
})

// 不再进行状态转换，直接使用后端返回的状态字符串

// 将中文状态转换为CSS类名（用于样式）
const getStatusClass = (status: string): string => {
  if (!status) return 'status-tag'
  // 将中文状态映射到英文类名（用于CSS）
  const statusMap: Record<string, string> = {
    '已创建': 'created',
    '已支付': 'paid',
    '已出票': 'ticketed',
    '待出行': 'ticketed',
    '已取消': 'cancelled',
    '已退款': 'refunded',
    // 兼容英文状态
    'created': 'created',
    'paid': 'paid',
    'ticketed': 'ticketed',
    'cancelled': 'cancelled',
    'refunded': 'refunded'
  }
  return statusMap[status] || 'status-tag'
}

// 处理从预约页面选择订单
const handleSelectOrderForSpecialPassenger = (order: OrderItem) => {
  // 保存选中的订单信息到sessionStorage
  // order.id 是订单号（用于显示），order.orderId 是数据库主键ID
  const orderData = {
    id: order.orderId || order.id,  // 数据库主键ID（用于查询详情）
    orderId: order.orderId || order.id,  // 数据库主键ID（兼容字段）
    orderNo: order.id,  // 订单号（显示用的，如 #ORD202511281600009）
    route: order.route,
    time: order.time,
    amount: order.amount || 0,
    status: order.status
  }
  sessionStorage.setItem('selectedOrderForSpecialPassenger', JSON.stringify(orderData))
  // 标记需要返回到预约页面
  sessionStorage.setItem('returnToSpecialPassenger', 'true')
  
  // 跳转到重点旅客预约页面
  router.push('/portal/passengers/special-passenger')
}

// 处理删除订单（支持管理员与普通乘客两种路径）
const handleDelete = async (order: OrderItem) => {
  if (!confirm(`确认要删除订单 #${order.id} 吗？\n\n此操作为软删除：订单数据将保留于数据库，但状态会被标记为 'deleted'，该订单会从前端列表中隐藏。`)) return

  // 立即在前端隐藏（乐观更新），避免等待后端响应造成的延迟
  const originalOrders = orders.value.slice()
  try {
    // 从本地列表中移除（按显示 ID 匹配）
    orders.value = orders.value.filter(o => String(o.id) !== String(order.id))
    // 取消勾选
    const selIdx = selectedOrders.value.indexOf(order.id)
    if (selIdx > -1) selectedOrders.value.splice(selIdx, 1)

    // 管理员使用管理接口删除订单（不用依赖机票号或乘客ID）
    if (isAdmin.value) {
      const orderId = order.orderId || order.id
      if (!orderId) {
        // 回滚
        orders.value = originalOrders
        alert('订单ID 不存在，无法删除')
        return
      }
      await orderManagementApi.deleteOrder(orderId)
      // 同步刷新一次后端数据以确保一致性（异步但不阻塞回滚）
      loadOrders().catch(() => {})
      alert(`订单 #${order.id} 已成功删除（管理员）`)
      return
    }

    // 普通乘客走机票软删除路径（需要机票号和乘客ID）
    const ticketNo = order.ticketNo
      if (!ticketNo) {
        alert('机票号不存在，无法删除')
        return
      }
      
    // 确保正确获取异步的当前用户信息
    const currentUser = await apiUtils.getCurrentUser()
      const passengerId = currentUser?.id
      if (!passengerId) {
        alert('无法获取用户信息，无法删除')
        return
      }
      
      const passengerIdNum = typeof passengerId === 'string' ? parseInt(passengerId, 10) : passengerId
      if (isNaN(passengerIdNum)) {
        alert('用户ID格式错误，无法删除')
        return
      }
      
      console.log('开始软删除机票，机票号:', ticketNo, '用户ID:', passengerIdNum)
      await ticketApi.softDeleteTicketByTicketNo(ticketNo, passengerIdNum)
    // 改为异步刷新，UI 已被乐观隐藏
    loadOrders().catch(() => {})
      alert(`订单 #${order.id} 已成功删除`)
    } catch (error: any) {
      console.error('删除失败:', error)
    const errorMessage = (error as any)?.message || '删除失败，请重试'
    // 回滚：恢复原始订单列表
    try { orders.value = originalOrders } catch (e) {}
      alert(`删除失败: ${errorMessage}`)
  }
}

// 恢复订单（软删除恢复）
const handleRestore = async (order: OrderItem) => {
  if (!confirm(`确认要恢复订单 #${order.id} 吗？\n\n此操作会将订单状态从 'deleted' 恢复为删除前的状态（若无记录则恢复为 created）。`)) return
  try {
    const orderId = order.orderId || order.id
    if (!orderId) {
      alert('订单ID不存在，无法恢复')
      return
    }
    await orderManagementApi.restoreOrder(orderId)
    await loadOrders()
    alert(`订单 #${order.id} 已恢复`)
  } catch (error: any) {
    console.error('恢复失败:', error)
    alert('恢复失败: ' + (error?.message || error))
  }
}

// 新增：关闭详情模态框
const closeDetailModal = () => {
  detailModal.visible = false;
  detailModal.order = null;
};

// 新增：获取标签文本
const getLabel = (key: string) => {
  const labelMap: Record<string, string> = {
    '订单号': '订单号',
    '客户': '客户',
    '航线': '航线',
    '起飞时间': '起飞时间',
    '状态': '状态',
    '金额': '金额'
  };
  return labelMap[key] || key;
}

const goToPage = async (page: number) => {
  if (page >= 1 && page <= totalPages.value) {
    currentPage.value = page
    jumpPage.value = page
    await loadOrders() // 重新加载数据
  }
}

// pagination helper for template
const onOrderPageSizeChange = () => {
  currentPage.value = 1
  loadOrders()
}

// formatTime helper used in template (returns HH:mm)
const formatTime = (dateTime: string | Date | undefined) => {
  const full = formatDateTime(dateTime || '')
  const parts = String(full).split(' ')
  return parts[1] || full
}

// 处理改签订单
const resolveOrderById = (orderId: string | number) => {
  const id = String(orderId)
  return (
    filteredOrders.value.find(o => String(o.id) === id) ||
    orders.value.find(o => String(o.id) === id) ||
    null
  )
}

const handleSearchFlightsForModify = async () => {
  if (!modifyModal.search.departure || !modifyModal.search.destination || !modifyModal.search.date) {
    modifyModal.error = '请先填写出发城市、到达城市和出发日期'
    return
  }
  modifyModal.error = ''
  modifyModal.searchLoading = true
  try {
    const params = {
      departure: modifyModal.search.departure,
      destination: modifyModal.search.destination,
      date: modifyModal.search.date,
      passengers: Number(modifyModal.search.passengers || '1')
    }
    const result: any = await flightApi.searchFlights(params)
    const flights = result?.flights || result || []
    modifyModal.searchResults = Array.isArray(flights) ? flights : []
    if (!modifyModal.searchResults.length) {
      modifyModal.error = '未查询到符合条件的航班，请调整条件重试'
    }
  } catch (error: any) {
    console.error('搜索航班失败:', error)
    modifyModal.error = error?.message || '搜索航班失败，请稍后重试'
  } finally {
    modifyModal.searchLoading = false
  }
}

const selectFlightForModify = (flight: any) => {
  modifyModal.selectedFlight = flight
}

// 处理改签订单（跳转到新页面）
const handleModifyOrder = (orderId: string) => {
  const target = resolveOrderById(orderId)
  if (!target) {
    alert('未找到可改签的订单，请刷新列表后重试。')
    return
  }
  // 跳转到改签页面，传递订单ID
  router.push(`/portal/orders/rebook/${orderId}`)
}

const submitModify = async () => {
  if (!modifyModal.order) return
  if (!modifyModal.reason.trim()) {
    modifyModal.error = '请填写改签原因'
    return
  }
  modifyModal.error = ''
  modifyModal.loading = true

  const payload = {
    orderNo: modifyModal.order.orderNo || modifyModal.order.id,
    route: modifyModal.order.route,
    oldFlightNo: modifyModal.order.flightNo || modifyModal.order.flightNumber,
    changeFee: modifyModal.changeFee,
    priceDiff: modifyModal.priceDiff,
    reason: modifyModal.reason.trim(),
    // 如果选择了新航班，则一并传递给后端
    newFlight: modifyModal.selectedFlight,
    amount: modifyModal.order.amount ?? modifyModal.order.totalAmount,
    oldDepartureTime: modifyModal.order.departureTime || modifyModal.order.schedDepTime,
    oldDepartureDate: modifyModal.order.departureDate,
    newDepartureDate: modifyModal.selectedFlight?.date
  }

  try {
    const apiClient: any = orderApi
    const fee = Number(payload.changeFee || 0) + Number(payload.priceDiff || 0)
    if (apiClient?.payReschedule && fee > 0) {
      // 先同步打开空窗口，避免异步后被浏览器阻止
      const payWindow = window.open('about:blank', '_blank')
      try {
        const html = await apiClient.payReschedule({
          orderNo: payload.orderNo,
          changeFee: payload.changeFee,
          priceDiff: payload.priceDiff,
          reason: payload.reason,
          newFlight: payload.newFlight
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
    } else if (apiClient?.requestReschedule) {
      await apiClient.requestReschedule(payload)
    } else {
      await new Promise(resolve => setTimeout(resolve, 600))
    }
    await loadOrders()
    closeModifyModal()
    alert(t('orderManagement.alerts.modifyPending', { id: payload.orderNo }))
  } catch (error) {
    console.error('改签失败:', error)
    modifyModal.error = (error as any)?.message || '改签失败，请重试'
  } finally {
    modifyModal.loading = false
  }
}

const closeModifyModal = () => {
  modifyModal.visible = false
  modifyModal.order = null
  modifyModal.reason = ''
  modifyModal.error = ''
  modifyModal.searchResults = []
  modifyModal.selectedFlight = null
}

// 动态计算退票手续费百分比
const calculateDynamicCancelFeePercent = (departureTimeRaw: string | Date | undefined): number => {
  if (!departureTimeRaw) return 5 // 默认兜底
  
  const now = new Date()
  let departureDate: Date

  if (typeof departureTimeRaw === 'string') {
    // 尝试解析日期字符串
    let dStr = departureTimeRaw.trim()
    if (dStr.includes(' ') && !dStr.includes('T')) {
      dStr = dStr.replace(' ', 'T')
    }
    departureDate = new Date(dStr)
  } else {
    departureDate = departureTimeRaw
  }

  if (isNaN(departureDate.getTime())) return 5

  const diffMs = departureDate.getTime() - now.getTime()
  // 转换为天数
  const diffDays = diffMs / (1000 * 60 * 60 * 24)

  if (diffDays >= 7) {
    return 5
  } else if (diffDays >= 2) {
    return 10
  } else {
    // 两天之内（包括过期）
    return 15
  }
}

// 处理取消订单（弹窗）
const handleCancelOrder = (orderId: string) => {
  const target = resolveOrderById(orderId)
  if (!target) {
    alert('未找到可取消的订单，请刷新列表后重试。')
    return
  }
  cancelModal.order = target
  // 机票价格：优先使用订单金额
  const rawPrice = typeof target.amount === 'number'
    ? target.amount
    : parseFloat(String(target.amount || '').replace(/[^0-9.]/g, ''))
  const ticketPrice = isNaN(rawPrice) || rawPrice <= 0 ? 0 : Number(rawPrice.toFixed(2))
  cancelModal.ticketPrice = ticketPrice
  // 退票手续费按动态比例计算
  const depTime = target.rawDepartureTime || target.time
  const feePercent = calculateDynamicCancelFeePercent(depTime)
  cancelModal.fee = ticketPrice > 0 ? Number((ticketPrice * (feePercent / 100)).toFixed(2)) : 0
  cancelModal.reason = ''
  cancelModal.error = ''
  cancelModal.loading = false
  cancelModal.visible = true
}

const submitCancel = async () => {
  if (!cancelModal.order) return
  if (!cancelModal.reason.trim()) {
    cancelModal.error = '请填写取消原因'
    return
  }
  cancelModal.error = ''
  cancelModal.loading = true

  try {
    if (!isAdmin.value) {
      // 普通乘客：如果存在取消手续费则走支付宝支付流程；否则直接提交取消申请
      const currentUser = await apiUtils.getCurrentUser()
      const applicantName = currentUser?.realName || currentUser?.username || ''

      const ticketBasePrice = (cancelModal.order && cancelModal.order.amount) ? Number(cancelModal.order.amount) : Number(cancelModal.ticketPrice || 0)

      const payload: any = {
        orderNo: cancelModal.order.id,
        route: cancelModal.order.route,
        flightNo: cancelModal.order.flightNo || '',
        departureTime: cancelModal.order.time,
        cancelFee: cancelModal.fee,
        // refundFare = 原订单总价 - 取消手续费（不得为负）
        refundFare: ticketBasePrice > 0 ? Math.max(0, ticketBasePrice - Number(cancelModal.fee || 0)) : 0,
        // 兼容后端旧实现：把 ticketPrice 传为订单总价，后端会用 ticketPrice - cancelFee 计算 refundFare（保持兼容，不改变后端逻辑）
        ticketPrice: ticketBasePrice,
        reason: cancelModal.reason.trim(),
        applicantName
      }

      // 乘客入口：不再跳转第三方支付，直接提交取消申请并尝试同步更新订单金额为（原始金额 - 手续费）
      const fee = Number(payload.cancelFee || 0)
      // 先提交取消请求（附带机票号以写入退票表）
      // 使用 payload（已标注为 any）直接传递，避免 TS 类型检查对附加字段的限制
      payload.ticketNo = (cancelModal.order && (cancelModal.order.ticketNo || (cancelModal.order as any).ticket_no)) || null
      await orderApi.requestCancel(payload)
      // 计算新的订单金额并尝试更新后端订单记录的 totalAmount 字段（兼容性：若后端不允许或接口不存在则忽略错误）
      try {
        const newTotal = payload.ticketPrice > 0 ? Math.max(0, Number(payload.ticketPrice) - fee) : 0
        if ((orderApi as any).updateOrderAmount) {
          await (orderApi as any).updateOrderAmount(cancelModal.order.id, newTotal)
        } else if ((orderApi as any).cancelOrder) {
          // 若没有更新接口，调用取消接口尝试触发后端的取消流程（以兼容现有后端实现）
          await (orderApi as any).cancelOrder(cancelModal.order.id)
        }
        // 更新本地显示，保持界面与预期一致（不会改变原有其余逻辑）
        if (cancelModal.order) {
          try {
            cancelModal.order.totalAmount = newTotal
            cancelModal.order.amount = newTotal
          } catch (e) { /* ignore */ }
        }
      } catch (e) {
        console.warn('同步更新订单金额失败，已提交取消请求，后续由后端处理：', e)
      }
    } else {
      // 管理员：保持原有逻辑，尽量兼容现有实现
      const adminClient: any = orderManagementApi
      if (adminClient?.requestRefund) {
        await adminClient.requestRefund({
          orderNo: cancelModal.order.id,
          route: cancelModal.order.route,
          cancelFee: cancelModal.fee,
          reason: cancelModal.reason.trim()
        })
      } else if ((orderApi as any).cancelOrder) {
        await (orderApi as any).cancelOrder(cancelModal.order.id)
      } else {
        await new Promise(resolve => setTimeout(resolve, 600))
      }
    }
    await loadOrders()
    closeCancelModal()
    alert(t('orderManagement.alerts.cancelSuccess', { id: cancelModal.order.id }))
  } catch (error) {
    console.error('取消失败:', error)
    cancelModal.error = (error as any)?.message || '取消失败，请重试'
  } finally {
    cancelModal.loading = false
  }
}

const closeCancelModal = () => {
  cancelModal.visible = false
  cancelModal.order = null
  cancelModal.reason = ''
  cancelModal.error = ''
}

// 处理退订申请
const handleRefundOrder = async (orderId: string) => {
  const reason = prompt(t('orderManagement.alerts.refundPrompt'))
  if (reason) {
    try {
      // TODO: 调用退订API
      alert(t('orderManagement.alerts.refundSubmitted', { id: orderId }))
      console.log('申请退订:', orderId, reason)
      // 重新加载数据
      await loadOrders()
    } catch (error) {
      console.error('退订申请失败:', error)
      alert(t('orderManagement.alerts.refundFailed'))
    }
  }
}

// 加载订单数据（从后端API）
const loadOrders = async () => {
  try {
    console.log('开始加载订单，角色:', isAdmin.value ? '管理员' : '乘客', '页码:', currentPage.value, '每页:', pageSize.value)
    console.log('查询参数:', {
      orderNumber: searchParams.orderNumber,
      customer: searchParams.customer,
      status: searchParams.status,
      startDate: searchParams.startDate,
      endDate: searchParams.endDate
    })
    
    // 如果是从预约页面来的，不传status参数，而是在前端过滤
    // 因为后端可能不支持status参数，或者状态值格式不匹配
    let statusFilter = searchParams.status || undefined
    // 注意：从预约页面来时，不在后端过滤，而是在前端filteredOrders中过滤
    
    let result: any
    
    // 根据角色使用不同的API
    if (isAdmin.value) {
      // 管理员使用 orderManagementApi
      result = await orderManagementApi.getOrderList({
        page: currentPage.value - 1, // 后端从0开始
        size: pageSize.value,
        orderNumber: searchParams.orderNumber || undefined,
        customer: searchParams.customer || undefined,
        status: statusFilter,  // 使用过滤后的状态
        startDate: searchParams.startDate || undefined,
        endDate: searchParams.endDate || undefined
      })
    } else {
      // 乘客使用 orderApi
      const currentUser = apiUtils.getCurrentUser()
      if (!currentUser || !currentUser.id) {
        console.warn('用户未登录，无法加载订单')
        orders.value = []
        total.value = 0
        return
      }
      
      const passengerId = typeof currentUser.id === 'string' ? parseInt(currentUser.id, 10) : currentUser.id
      if (isNaN(passengerId)) {
        console.error('用户ID格式错误:', currentUser.id)
        orders.value = []
        total.value = 0
        return
      }
      
      // 乘客查询也支持所有查询参数
      // 如果是从预约页面来的，不传status参数，而是在前端过滤
      const orderParams: any = {
        page: currentPage.value - 1,
        size: pageSize.value,
        orderNumber: searchParams.orderNumber || undefined,
        customer: searchParams.customer || undefined,
        startDate: searchParams.startDate || undefined,
        endDate: searchParams.endDate || undefined
      }
      // 如果当前为改签或退订管理入口，乘客侧应仅请求“待出行”订单以确保后端返回的分页数据为待出行订单
      if (!isFromSpecialPassenger.value && (viewMode.value === 'manage' || viewMode.value === 'refund')) {
        // 优先使用用户手动输入的status，其次使用固定的中文状态 '待出行'
        orderParams.status = statusFilter || '待出行'
      } else if (!isFromSpecialPassenger.value && statusFilter) {
        // 其他场景下（非预约页面），只有当用户手动输入状态时才传递 status 参数
        orderParams.status = statusFilter
      }
      const orderResult = await orderApi.getOrders(orderParams)
      
      // orderApi.getOrders 返回格式：{orders: [...], total: ...}
      result = {
        success: true,
        data: {
          orders: orderResult.orders || [],
          total: orderResult.total || 0,
          page: orderResult.page || 0,
          size: orderResult.size || 10,
          totalPages: orderResult.totalPages || 0
        }
      }
    }
    
    console.log('API返回结果:', result)
    
    // 处理响应数据格式
    if (result && result.success && result.data) {
      const data = result.data
      let ordersData: any[] = []
      
      // 确保 orders 是数组
      if (Array.isArray(data.orders)) {
        ordersData = data.orders
      } else if (Array.isArray(data)) {
        ordersData = data
      }
      
      console.log('解析到的订单数据数量:', ordersData.length)
      
      // 如果后端没有正确过滤，在前端再次过滤
      let filteredData = ordersData
      
      // 前端过滤逻辑（如果后端没有正确过滤）
      if (searchParams.orderNumber || searchParams.customer || searchParams.status || searchParams.startDate || searchParams.endDate) {
        filteredData = ordersData.filter((order: any) => {
          // 订单号过滤
          if (searchParams.orderNumber) {
            const orderNo = (order.orderNo || String(order.id || '')).toLowerCase()
            const searchOrderNo = searchParams.orderNumber.toLowerCase()
            if (!orderNo.includes(searchOrderNo)) {
              return false
            }
          }
          
          // 客户姓名过滤
          if (searchParams.customer) {
            const customerName = (order.passengerName || order.customer || order.passenger_name || '').toLowerCase()
            const searchCustomer = searchParams.customer.toLowerCase()
            if (!customerName.includes(searchCustomer)) {
              return false
            }
          }
          
          // 状态过滤
          if (searchParams.status) {
            const orderStatus = order.status || 'created'
            if (orderStatus !== searchParams.status) {
              return false
            }
          }
          
          // 日期范围过滤
          if (searchParams.startDate || searchParams.endDate) {
            const orderDate = order.departureTime || order.createdAt
            if (orderDate) {
              const formattedDate = formatDateTime(orderDate)
              const orderDateStr = formattedDate ? formattedDate.split(' ')[0] : null // 只取日期部分
              
              if (orderDateStr) {
                if (searchParams.startDate && orderDateStr < searchParams.startDate) {
                  return false
                }
                if (searchParams.endDate && orderDateStr > searchParams.endDate) {
                  return false
                }
              }
            }
          }
          
          return true
        })
        
        console.log('前端过滤后的订单数量:', filteredData.length)
      }
      
      // 过滤掉被标记为 deleted / 已删除 的订单（后端可能已过滤，但前端二次保障）
      const cleanedData = filteredData.filter((order: any) => {
        const st = String(order.status || order.statusText || '').toLowerCase()
        return st !== 'deleted' && st !== '已删除'
      })
      
      // 转换后端数据格式到前端格式
      orders.value = cleanedData.map((order: any) => {
        // 处理订单号：优先使用orderNo，其次使用id
        const orderNo = order.orderNo || String(order.id || '')
        // 保存订单的原始ID（数据库主键），用于查询详情
        const orderId = order.id || null
        
        // 处理金额：可能是BigDecimal对象或数字
        let amount = 0
        if (order.totalAmount !== undefined && order.totalAmount !== null) {
          if (typeof order.totalAmount === 'object' && order.totalAmount !== null) {
            try {
              amount = parseFloat(order.totalAmount.toString()) || 0
            } catch (e) {
              amount = parseFloat(String(order.totalAmount.valueOf ? order.totalAmount.valueOf() : order.totalAmount)) || 0
            }
          } else {
            amount = parseFloat(String(order.totalAmount)) || 0
          }
        }
        
        // 处理日期时间
        let timeStr = ''
        if (order.departureTime) {
          timeStr = formatDateTime(order.departureTime)
        } else if (order.createdAt) {
          timeStr = formatDateTime(order.createdAt)
        }
        
        // 处理到达时间
        let arrivalTimeStr = ''
        if (order.arrivalTime) {
          arrivalTimeStr = formatDateTime(order.arrivalTime)
        }
        
        // 处理乘客姓名
        const customerName = order.passengerName || order.customer || order.passenger_name || ''
        
        // 处理航线
        const routeStr = order.route || ''
        
        // 处理状态 - 直接使用后端返回的状态
        const orderStatus = order.status || 'created'
        
        // 航班号与机票号（直接透传后端字段，保持其他逻辑不变）
        const flightNo = order.flightNo || order.flight_no || ''
        const ticketNo = order.ticketNo || order.ticket_no || ''

        return {
          id: orderNo,  // 订单号，用于显示
          orderId: orderId,  // 订单ID，用于查询详情
          customer: customerName,
          route: routeStr,
          flightNo,
          ticketNo,
          time: timeStr,
          arrivalTime: arrivalTimeStr,  // 到达时间
          status: orderStatus,  // 直接使用后端状态，不进行转换
          amount: amount,
          rawDepartureTime: order.departureTime // 保留原始起飞时间用于计算
        }
      })
      
      // 更新分页信息
      // 如果是从预约页面来的，使用过滤后的数量（因为前端会再次过滤）
      // 如果进行了前端过滤，使用过滤后的数量；否则使用后端返回的total
      if (isFromSpecialPassenger.value) {
        // 从预约页面来，需要在前端过滤，所以先不设置total，等filteredOrders计算后再更新
        // 这里先设置一个临时值，实际会在filteredOrders中计算
        total.value = data.total || filteredData.length
      } else if (filteredData.length !== ordersData.length) {
        total.value = filteredData.length
        console.log('前端过滤后，更新total为:', total.value)
      } else {
        total.value = data.total || filteredData.length
      }
      currentPage.value = (data.page !== undefined ? data.page : 0) + 1 // 后端从0开始，前端从1开始
      jumpPage.value = currentPage.value
      
      console.log('订单加载成功，共', total.value, '条，当前页:', currentPage.value, '/', totalPages.value)
      console.log('转换后的订单数量:', orders.value.length)
      console.log('显示的订单:', orders.value.map(o => ({ id: o.id, customer: o.customer, status: o.status })))
    } else {
      console.log('后端返回空数据，清空订单列表')
      orders.value = []
      total.value = 0
    }
  } catch (error: any) {
    console.error('加载订单失败:', error)
    console.error('错误详情:', (error as any)?.message)
    
    orders.value = []
    total.value = 0
    
    // 显示错误提示
    if ((error as any)?.message && !(error as any).message.includes('用户未登录')) {
      console.warn('订单加载失败，已清空订单列表')
      // 显示友好的错误提示
      alert((error as any)?.message || '加载订单失败，请检查网络连接或稍后重试')
    }
  }
}
</script>

<style scoped>
/* 直接内联基础样式 */
.page-container {
  margin-top: -50px;
  padding: 2.5rem clamp(1.5rem, 6vw, 4rem) 3rem;
  color: #f8fafc;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
  position: relative;
  z-index: 1;
  isolation: isolate;
  background: radial-gradient(circle at 10% 20%, rgba(59, 130, 246, 0.18), transparent 45%),
    radial-gradient(circle at 85% 10%, rgba(14, 165, 233, 0.3), transparent 55%),
    linear-gradient(135deg, #030617 0%, #01030a 90%);
}

.page-container::before {
  content: '';
  position: absolute;
  inset: 0;
  background: url('data:image/svg+xml,%3Csvg width="140" height="140" xmlns="http://www.w3.org/2000/svg"%3E%3Ccircle cx="1" cy="1" r="1" fill="rgba(255,255,255,0.04)" /%3E%3C/svg%3E');
  opacity: 0.9;
  z-index: -1;
  pointer-events: none;
}

.breadcrumb {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.7);
  margin-bottom: 20px;
}

.breadcrumb-separator {
  margin: 0 8px;
  color: rgba(255, 255, 255, 0.4);
}

.page-header {
  display: flex;
  justify-content: space-between;
  gap: 1.5rem;
  align-items: flex-start;
  flex-wrap: wrap;
}

.page-label {
  letter-spacing: 0.08em;
  text-transform: uppercase;
  font-size: 0.85rem;
  color: rgba(248, 250, 252, 0.6);
}

.page-header h1 {
  margin: 0.4rem 0;
  font-size: clamp(1.8rem, 3vw, 2.4rem);
  color: #fff;
}

.page-header p {
  color: rgba(248, 250, 252, 0.75);
  max-width: 520px;
}

.page-actions {
  display: flex;
  gap: 0.8rem;
  flex-wrap: wrap;
}

.primary-btn,
.ghost-btn {
  border-radius: 999px;
  padding: 0.65rem 1.6rem;
  border: 1px solid transparent;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
}

.primary-btn {
  background: linear-gradient(135deg, #1E8AE6, #0A2F63);
  color: #fff;
  box-shadow: 0 10px 25px rgba(99, 102, 241, 0.35);
}

.primary-btn:hover {
  transform: translateY(-2px);
}

.ghost-btn {
  border-color: rgba(255, 255, 255, 0.35);
  background: transparent;
  color: #f8fafc;
}

.ghost-btn:hover {
  background: rgba(255, 255, 255, 0.1);
}

.grid-two {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 1.5rem;
}

.glass-card {
  border-radius: 28px;
  padding: 1.8rem;
  background: rgba(5, 11, 30, 0.85);
  border: 1px solid rgba(148, 163, 184, 0.14);
  box-shadow:
    0 30px 60px rgba(1, 6, 20, 0.8),
    inset 0 1px rgba(255, 255, 255, 0.08);
  backdrop-filter: blur(22px);
  transition: border-color 0.3s, transform 0.3s;
}

.glass-card:hover {
  border-color: rgba(59, 130, 246, 0.5);
  transform: translateY(-2px);
}

.section-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 1rem;
  margin-bottom: 1.2rem;
  flex-wrap: wrap;
}

.section-label {
  font-size: 0.85rem;
  color: rgba(248, 250, 252, 0.6);
  margin: 0;
}

.section-head h2,
.section-head h3 {
  margin: 0.3rem 0 0;
  color: #fff;
}

.panel-pill {
  padding: 0.3rem 0.8rem;
  border-radius: 999px;
  font-size: 0.85rem;
  background: rgba(99, 102, 241, 0.15);
  color: rgba(147, 197, 253, 0.9);
  border: 1px solid rgba(99, 102, 241, 0.3);
}

.search-item {
  display: flex;
  flex-direction: column;
  gap: 0.4rem;
}

.search-item label {
  font-size: 0.85rem;
  color: rgba(248, 250, 252, 0.75);
}

.search-item input,
.search-item select {
  padding: 0.75rem 1rem;
  border-radius: 14px;
  border: 1px solid rgba(148, 163, 184, 0.35);
  background: rgba(6, 10, 28, 0.85);
  color: #f8fafc;
  font-size: 0.95rem;
  transition: border-color 0.25s, box-shadow 0.25s, transform 0.25s;
}

.search-item input:focus,
.search-item select:focus {
  border-color: rgba(96, 165, 250, 0.9);
  box-shadow: 0 8px 24px rgba(14, 165, 233, 0.25);
  transform: translateY(-1px);
}

.date-range {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.date-separator {
  color: rgba(255, 255, 255, 0.5);
}

.table-container {
  overflow-x: auto;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
}

.data-table th,
.data-table td {
  padding: 1rem 0.7rem;
  text-align: left;
  border-bottom: 1px solid rgba(148, 163, 184, 0.15);
}

.data-table th:first-child,
.data-table td:first-child {
  text-align: center;
  width: 50px;
  padding: 1rem 0.5rem;
}

.data-table thead tr {
  background: rgba(216, 218, 225, 0.6);
}

.data-table tr:hover td {
  background: rgba(59, 130, 246, 0.06);
}

.data-table th {
  color: rgba(248, 250, 252, 0.65);
  font-weight: 500;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.data-table td {
  color: rgba(248, 250, 252, 0.85);
}

.status-tag {
  padding: 0.3rem 0.8rem;
  border-radius: 999px;
  font-size: 0.85rem;
  display: inline-block;
}

.status-tag.created {
  background: rgba(107, 114, 128, 0.2);
  color: #9ca3af;
  border: 1px solid rgba(107, 114, 128, 0.3);
}

.status-tag.paid {
  background: rgba(59, 130, 246, 0.2);
  color: #60a5fa;
  border: 1px solid rgba(59, 130, 246, 0.3);
}

.status-tag.ticketed {
  background: rgba(34, 197, 94, 0.2);
  color: #4ade80;
  border: 1px solid rgba(34, 197, 94, 0.3);
}

.status-tag.cancelled {
  background: rgba(239, 68, 68, 0.2);
  color: #f87171;
  border: 1px solid rgba(239, 68, 68, 0.3);
}

.status-tag.refunded {
  background: rgba(245, 158, 11, 0.2);
  color: #fbbf24;
  border: 1px solid rgba(245, 158, 11, 0.3);
}

.pagination {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.2rem 0;
}

.pagination-info {
  color: rgba(255, 255, 255, 0.7);
  font-size: 0.9rem;
}

.pagination-controls {
  display: flex;
  align-items: center;
  gap: 0.8rem;
}

.page-btn {
  padding: 0.5rem 1rem;
  border: 1px solid rgba(255, 255, 255, 0.15);
  background: rgba(15, 23, 42, 0.6);
  border-radius: 8px;
  cursor: pointer;
  color: rgba(255, 255, 255, 0.8);
  transition: all 0.3s;
}

.page-btn:hover:not(:disabled) {
  background: rgba(255, 255, 255, 0.1);
  border-color: rgba(255, 255, 255, 0.3);
}

.page-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.page-number {
  padding: 0.5rem 1rem;
  color: rgba(255, 255, 255, 0.8);
}

.page-jump {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.page-jump input {
  width: 60px;
  padding: 0.5rem;
  border: 1px solid rgba(255, 255, 255, 0.15);
  border-radius: 8px;
  text-align: center;
  background: rgba(15, 23, 42, 0.6);
  color: #fff;
}

.page-jump span {
  color: rgba(255, 255, 255, 0.7);
  font-size: 0.9rem;
}

.order-page {
  gap: 1.5rem;
}

.search-panel .search-row {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 1rem;
  margin-bottom: 1rem;
}

.search-actions-row {
  display: flex;
  gap: 0.8rem;
  align-items: center;
  justify-content: flex-end;
  flex-wrap: wrap;
  margin-top: 0.5rem;
  padding-top: 1rem;
  border-top: 1px solid rgba(148, 163, 184, 0.1);
}

.search-item.actions {
  display: flex;
  gap: 0.8rem;
  align-items: center;
  justify-content: flex-end;
  flex-wrap: wrap;
}

.table-card .section-actions {
  display: flex;
  gap: 0.6rem;
  flex-wrap: wrap;
}

.action-buttons {
  display: flex;
  gap: 0.4rem;
}

.action-btn {
  border-radius: 999px;
  border: 1px solid rgba(255, 255, 255, 0.15);
  background: transparent;
  color: #fff;
  padding: 0.35rem 0.9rem;
  cursor: pointer;
  transition: all 0.3s;
  font-size: 0.85rem;
}

.detail-btn {
  border-color: rgba(96, 165, 250, 0.5);
  color: #60a5fa;
}

.detail-btn:hover {
  background: rgba(96, 165, 250, 0.1);
  border-color: rgba(96, 165, 250, 0.7);
}

.select-btn {
  border-color: rgba(34, 197, 94, 0.5);
  color: #22c55e;
}

.select-btn:hover {
  background: rgba(34, 197, 94, 0.1);
  border-color: rgba(34, 197, 94, 0.7);
}

.delete-btn {
  border-color: rgba(239, 68, 68, 0.5);
  color: #ef4444;
}

.delete-btn:hover {
  background: rgba(239, 68, 68, 0.1);
  border-color: rgba(239, 68, 68, 0.7);
}

.chat-btn {
  border-color: rgba(34, 197, 94, 0.4);
}

.order-number {
  color: #1E8AE6;
  font-weight: 600;
}

.customer-name {
  font-weight: 600;
}

.amount {
  color: #fcd34d;
}

.checkbox-input {
  width: 18px;
  height: 18px;
  cursor: pointer;
  accent-color: #1E8AE6;
  border-radius: 4px;
}

/* 模态框 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.75);
  backdrop-filter: blur(8px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 10000;
  padding: 20px;
  box-sizing: border-box;
  overflow-y: auto;
  overscroll-behavior: contain;
  animation: fadeIn 0.3s ease-out;
  margin-top: 60px;
}

.modal-overlay.order-detail-modal {
  /* 确保弹窗在中间显示 */
  align-items: center;
  padding-top: 20px;
}

.modal-content {
  background: linear-gradient(135deg, rgba(15, 23, 42, 0.98) 0%, rgba(5, 11, 30, 0.98) 100%);
  border-radius: 24px;
  width: 90%;
  max-width: 580px;
  max-height: 85vh;
  overflow-y: auto;
  box-shadow:
    0 30px 60px rgba(0, 0, 0, 0.8),
    0 0 0 1px rgba(148, 163, 184, 0.1),
    inset 0 1px 0 rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(24px);
  border: 1px solid rgba(148, 163, 184, 0.15);
  animation: modal-pop 0.3s ease-out;
  position: relative;
}

.modal-content::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(90deg, 
    transparent 0%, 
    rgba(59, 130, 246, 0.3) 50%, 
    transparent 100%);
  pointer-events: none;
}

.modal-header {
  padding: 28px 28px 20px;
  border-bottom: 1px solid rgba(148, 163, 184, 0.12);
  display: flex;
  justify-content: space-between;
  align-items: center;
  position: relative;
}

.modal-header h3 {
  margin: 0;
  color: #f8fafc;
  font-size: 24px;
  font-weight: 700;
  letter-spacing: -0.02em;
  background: linear-gradient(135deg, #0a72da 0%, #417ec7 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

/* 关闭按钮 */
.close-btn {
  background: rgba(148, 163, 184, 0.1);
  border: 1px solid rgba(148, 163, 184, 0.2);
  font-size: 24px;
  color: #cbd5e1;
  cursor: pointer;
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 10px;
  transition: all 0.3s ease;
  line-height: 1;
}

.close-btn:hover {
  background: rgba(239, 68, 68, 0.15);
  border-color: rgba(239, 68, 68, 0.3);
  color: #f87171;
  transform: rotate(90deg) scale(1.05);
}

.modal-body {
  padding: 28px;
}

.flight-search-inline {
  width: 100%;
  margin-top: 8px;
}

.flight-search-inline .search-row {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: flex-end;
}

.flight-search-inline .search-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.flight-search-inline .search-item.small {
  max-width: 90px;
}

.hint-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 6px;
}

.pill {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 4px 10px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 0.01em;
}

.pill-info {
  background: rgba(59, 130, 246, 0.15);
  color: #93c5fd;
  border: 1px solid rgba(59, 130, 246, 0.3);
}

.pill-success {
  background: rgba(16, 185, 129, 0.15);
  color: #6ee7b7;
  border: 1px solid rgba(16, 185, 129, 0.25);
}

.flight-search-inline .search-item input,
.flight-search-inline .search-item select {
  padding: 6px 10px;
  border-radius: 8px;
  border: 1px solid rgba(148, 163, 184, 0.5);
  background: rgba(15, 23, 42, 0.7);
  color: #e5e7eb;
  font-size: 13px;
}

.flight-search-inline .primary-btn.compact {
  padding: 6px 14px;
  font-size: 13px;
}

.flight-results-wrapper {
  margin-top: 10px;
}

.empty-result {
  padding: 12px;
  border: 1px dashed rgba(148, 163, 184, 0.4);
  border-radius: 10px;
  color: #9ca3af;
  background: rgba(15, 23, 42, 0.6);
  text-align: center;
  font-size: 13px;
}

.flight-results-mini {
  margin-top: 10px;
  max-height: 220px;
  overflow: auto;
  border-radius: 10px;
  border: 1px solid rgba(148, 163, 184, 0.4);
  background: radial-gradient(circle at top left, rgba(56, 189, 248, 0.1), rgba(15, 23, 42, 0.9));
}

.flight-item-mini {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 12px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.flight-item-mini:hover {
  background: rgba(30, 64, 175, 0.5);
}

.flight-item-mini.active {
  border-left: 3px solid #38bdf8;
  background: rgba(37, 99, 235, 0.7);
}

.flight-item-mini .flight-main {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.flight-item-mini .flight-route {
  display: flex;
  align-items: center;
  gap: 4px;
  font-weight: 500;
}

.flight-item-mini .flight-route .city {
  color: #e5e7eb;
}

.flight-item-mini .flight-route .arrow {
  color: #93c5fd;
}

.flight-item-mini .flight-time {
  font-size: 12px;
  color: #9ca3af;
}

.flight-item-mini .flight-side {
  text-align: right;
}

.flight-item-mini .price {
  color: #f97316;
  font-weight: 600;
}

.flight-item-mini .flight-no {
  font-size: 12px;
  color: #9ca3af;
}

.hint-text {
  font-size: 12px;
  color: #9ca3af;
  margin-top: 4px;
}

.flight-selected {
  margin-top: 12px;
  padding: 12px;
  border-radius: 12px;
  border: 1px solid rgba(16, 185, 129, 0.25);
  background: linear-gradient(135deg, rgba(16, 185, 129, 0.08), rgba(15, 23, 42, 0.9));
  display: flex;
  align-items: center;
  gap: 10px;
}

.flight-selected .selected-info .route {
  font-weight: 600;
  color: #e5e7eb;
}

.flight-selected .selected-info .meta {
  margin-top: 4px;
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  font-size: 12px;
  color: #9ca3af;
}

.flight-selected .selected-info .price {
  color: #f97316;
  font-weight: 600;
}

.detail-row {
  display: flex;
  padding: 18px 0;
  border-bottom: 1px solid rgba(148, 163, 184, 0.08);
  align-items: flex-start;
  gap: 16px;
  transition: background-color 0.2s ease;
}

.detail-row:hover {
  background-color: rgba(59, 130, 246, 0.03);
  margin: 0 -28px;
  padding-left: 28px;
  padding-right: 28px;
  border-radius: 8px;
}

.detail-row:last-child {
  border-bottom: none;
}

.detail-label {
  min-width: 100px;
  color: rgba(203, 213, 225, 0.8);
  font-weight: 600;
  font-size: 14px;
  letter-spacing: 0.02em;
  text-transform: uppercase;
  flex-shrink: 0;
}

.detail-value {
  flex: 1;
  color: #f8fafc;
  font-size: 16px;
  font-weight: 500;
  line-height: 1.6;
  word-break: break-word;
}

.detail-value:has(.status-tag) {
  display: flex;
  align-items: center;
}

.amount-value {
  color: #fcd34d !important;
  font-weight: 600 !important;
  font-size: 18px !important;
  text-shadow: 0 0 20px rgba(252, 211, 77, 0.3);
}

.status-tag {
  padding: 8px 16px;
  border-radius: 12px;
  font-size: 13px;
  font-weight: 600;
  display: inline-flex;
  align-items: center;
  letter-spacing: 0.02em;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

.status-tag.created {
  background: linear-gradient(135deg, rgba(107, 114, 128, 0.2) 0%, rgba(75, 85, 99, 0.15) 100%);
  color: #9ca3af;
  border: 1px solid rgba(107, 114, 128, 0.4);
  box-shadow: 0 2px 8px rgba(75, 85, 99, 0.2);
}

.status-tag.paid {
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.2) 0%, rgba(37, 99, 235, 0.15) 100%);
  color: #60a5fa;
  border: 1px solid rgba(59, 130, 246, 0.4);
  box-shadow: 0 2px 8px rgba(37, 99, 235, 0.2);
}

.status-tag.ticketed {
  background: linear-gradient(135deg, rgba(16, 185, 129, 0.2) 0%, rgba(5, 150, 105, 0.15) 100%);
  color: #34d399;
  border: 1px solid rgba(16, 185, 129, 0.4);
  box-shadow: 0 2px 8px rgba(16, 185, 129, 0.2);
}

.status-tag.cancelled {
  background: linear-gradient(135deg, rgba(239, 68, 68, 0.2) 0%, rgba(220, 38, 38, 0.15) 100%);
  color: #f87171;
  border: 1px solid rgba(239, 68, 68, 0.4);
  box-shadow: 0 2px 8px rgba(220, 38, 38, 0.2);
}

.status-tag.refunded {
  background: linear-gradient(135deg, rgba(245, 158, 11, 0.2) 0%, rgba(217, 119, 6, 0.15) 100%);
  color: #fbbf24;
  border: 1px solid rgba(245, 158, 11, 0.4);
  box-shadow: 0 2px 8px rgba(217, 119, 6, 0.2);
}

/* 确认弹窗样式 */
.confirm-card {
  max-width: 720px;
  border: 1px solid rgba(255, 255, 255, 0.08);
  background: linear-gradient(135deg, rgba(10, 21, 46, 0.96), rgba(4, 11, 25, 0.96));
  box-shadow: 0 30px 80px rgba(0, 0, 0, 0.45);
}

.confirm-label {
  font-size: 12px;
  letter-spacing: 0.2em;
  color: rgba(255, 255, 255, 0.6);
  margin: 0;
}

.sub-tip {
  margin: 6px 0 0;
  color: rgba(255, 255, 255, 0.65);
  font-size: 12px;
}

.confirm-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(160px, 1fr));
  gap: 12px;
  margin: 16px 0;
}

.confirm-block {
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 12px;
  padding: 12px;
  background: rgba(255, 255, 255, 0.02);
}

.confirm-block.total {
  background: linear-gradient(135deg, rgba(30, 138, 230, 0.16), rgba(99, 102, 241, 0.12));
}

.block-title {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.7);
  margin-bottom: 6px;
}

.block-value {
  font-size: 15px;
  font-weight: 600;
  color: #fff;
}

.block-value.highlight {
  color: #fbbf24;
}

.confirm-textarea {
  width: 100%;
  min-height: 90px;
  border-radius: 12px;
  border: 1px solid rgba(255, 255, 255, 0.14);
  background: rgba(15, 23, 42, 0.6);
  padding: 12px;
  color: #fff;
  margin: 10px 0 0;
  resize: vertical;
  font-family: inherit;
}

.confirm-textarea:focus {
  outline: none;
  border-color: rgba(99, 102, 241, 0.7);
  box-shadow: 0 0 0 2px rgba(99, 102, 241, 0.2);
}

.confirm-error {
  color: #fda4af;
  font-size: 13px;
  margin: 6px 0 0;
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 16px;
}

.btn-primary,
.btn-secondary {
  padding: 10px 18px;
  border: none;
  border-radius: 10px;
  cursor: pointer;
  font-weight: 600;
  transition: all 0.2s ease;
}

.btn-primary {
  background: linear-gradient(135deg, #3b82f6, #6366f1);
  color: #fff;
  box-shadow: 0 10px 30px rgba(99, 102, 241, 0.35);
}

.btn-primary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  box-shadow: none;
}

.btn-secondary {
  background: rgba(255, 255, 255, 0.06);
  color: rgba(255, 255, 255, 0.85);
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.btn-secondary:hover {
  background: rgba(255, 255, 255, 0.12);
}

/* 金额特殊样式 */
.detail-value:has-text("¥") {
  color: #fcd34d;
  font-weight: 600;
  font-size: 18px;
}

.detail-row:has(.detail-value:contains("¥")) .detail-value {
  color: #fcd34d;
  font-weight: 600;
  font-size: 18px;
}

@keyframes fadeIn {
  0% {
    opacity: 0;
  }
  100% {
    opacity: 1;
  }
}

@keyframes modal-pop {
  0% {
    opacity: 0;
    transform: scale(0.9);
  }
  100% {
    opacity: 1;
    transform: scale(1);
  }
}

/* 机票管理视图样式 */
.manage-card,
.refund-card {
  margin-top: 1.5rem;
}

.manage-content,
.refund-content {
  padding: 1rem 0;
}

.manage-content p,
.refund-content p {
  margin-bottom: 1rem;
  color: rgba(248, 250, 252, 0.75);
}

.manage-orders-list,
.refund-orders-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.manage-order-item,
.refund-order-item {
  padding: 1.5rem;
  border-radius: 20px;
  border: 1px solid rgba(255, 255, 255, 0.08);
  background: linear-gradient(120deg, rgba(227, 229, 236, 0.95), rgba(240, 242, 249, 0.9));
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 1.2rem;
  flex-wrap: wrap;
  box-shadow: 0 18px 40px rgba(3, 8, 20, 0.65);
  position: relative;
  overflow: hidden;
}

.manage-order-item::before,
.refund-order-item::before {
  content: '';
  position: absolute;
  inset: -40% auto auto -40%;
  width: 60%;
  height: 60%;
  background: radial-gradient(circle, rgba(59, 130, 246, 0.35), transparent 65%);
  opacity: 0;
  transition: opacity 0.35s;
  pointer-events: none;
}

.manage-order-item:hover::before,
.refund-order-item:hover::before {
  opacity: 1;
}

.manage-order-item .order-info h4,
.refund-order-item .order-info h4 {
  margin: 0 0 0.5rem 0;
  color: #fff;
  font-size: 1.1rem;
}

.manage-order-item .order-info p,
.refund-order-item .order-info p {
  margin: 0.3rem 0;
  color: rgba(248, 250, 252, 0.7);
  font-size: 0.9rem;
}

.manage-actions,
.refund-actions {
  display: flex;
  gap: 0.8rem;
  flex-wrap: wrap;
}

@media (max-width: 768px) {
  .manage-order-item,
  .refund-order-item {
    flex-direction: column;
    align-items: flex-start;
  }

  .manage-actions,
  .refund-actions {
    width: 100%;
  }

  .manage-actions button,
  .refund-actions button {
    flex: 1;
  }
}
</style>

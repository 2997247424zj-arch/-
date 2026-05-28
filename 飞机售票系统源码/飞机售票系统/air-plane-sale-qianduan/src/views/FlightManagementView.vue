<template>
  <AdminLayout>
    <div class="flight-page">
    <!-- 面包屑导航 -->
    <div class="breadcrumb">
      <span>首页</span>
      <span class="breadcrumb-separator">/</span>
      <span>航班信息</span>
    </div>

    <!-- 搜索筛选区域 -->
    <div class="search-section">
      <div class="search-row">
        <div class="search-item">
          <label>出发日期</label>
          <div class="date-range">
            <input 
              type="date" 
              v-model="searchParams.departureDateStart" 
              placeholder="出发日期起始"
            />
            <span class="date-separator">至</span>
            <input 
              type="date" 
              v-model="searchParams.departureDateEnd" 
              placeholder="出发日期结束"
            />
          </div>
        </div>
        <div class="search-item">
          <label>航空公司</label>
          <input 
            type="text" 
            v-model="searchParams.airline" 
            placeholder="航空公司"
          />
        </div>
        <div class="search-item">
          <label>出发地</label>
          <input 
            type="text" 
            v-model="searchParams.departure" 
            placeholder="出发地"
          />
        </div>
        <div class="search-item">
          <label>目的地</label>
          <input 
            type="text" 
            v-model="searchParams.destination" 
            placeholder="目的地"
          />
        </div>
        <div class="search-item">
          <button class="search-btn" @click="() => handleSearch(true)">
            <span class="search-icon">Q</span>
            查询
          </button>
        </div>
      </div>
    </div>

    <!-- 操作按钮 -->
    <div class="action-buttons">
      <button class="btn-primary" @click="handleAdd">
        <span class="btn-icon">+</span>
        新增
      </button>
    

    </div>

    <!-- 数据表格 -->
    <div class="table-container">
      <table class="data-table">
        <thead>
          <tr>
            <th width="50">
              <input 
                type="checkbox" 
                v-model="selectAll" 
                @change="toggleSelectAll"
              />
            </th>
            <th width="60">索引</th>
            <th width="120">出发日期</th>
            <th width="100">航班号</th>

            <th width="120">航空公司</th>
            <th width="100">飞机型号</th>
            <th width="120">出发地</th>
            <th width="120">目的地</th>
            <th width="160">出发时间</th>
            <th width="100">飞行时间</th>
            <th width="80">数量</th>
            <th width="80">票价</th>

            <th width="180">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="(flight, index) in flights" :key="flight.id">
            <td>
              <input 
                type="checkbox" 
                v-model="selectedFlights" 
                :value="flight.id"
              />
            </td>
            <!-- 全局序号：根据当前页和每页数量计算总索引 -->
            <td>{{ (currentPage - 1) * pageSize + index + 1 }}</td>
            <td>{{ flight.departureDate }}</td>
            <td>{{ flight.flightNumber }}</td>
           
            <td>{{ flight.airline }}</td>
            <td>{{ flight.aircraftModel }}</td>
            <td>{{ flight.departure }}</td>
            <td>{{ flight.destination }}</td>
            <td>{{ flight.departureTime }}</td>
            <td>{{ flight.duration }}</td>
            <td>{{ flight.quantity }}</td>
            <td>¥{{ flight.price }}</td>

            <td>
              <button class="action-btn detail-btn" @click="handleDetail(flight)">
                <span class="action-icon">👁</span>
                详情
              </button>
              <button class="action-btn edit-btn" @click="handleEdit(flight)">
                <span class="action-icon">✏️</span>
                编辑
              </button>
              <button class="action-btn delete-btn" @click="handleDeleteItem(flight)">
                <span class="action-icon">🗑</span>
                删除
              </button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 分页控件 -->
    <div class="pagination">
      <div class="pagination-info">
        共{{ total }}条
        <span style="margin-left:12px;">每页</span>
        <select v-model.number="pageSize" @change="handlePageSizeChange" style="margin:0 8px;">
          <option :value="5">5</option>
          <option :value="10">10</option>
          <option :value="20">20</option>
          <option :value="50">50</option>
        </select>
        条
      </div>
      <div class="pagination-controls">
        <button 
          class="page-btn" 
          :disabled="currentPage === 1"
          @click="goToPage(currentPage - 1)"
        >
          &lt;
        </button>
        <span class="page-number">{{ currentPage }} / {{ totalPages }}</span>
        <button 
          class="page-btn" 
          :disabled="currentPage === totalPages"
          @click="goToPage(currentPage + 1)"
        >
          &gt;
        </button>
        <div class="page-jump">
          <span>前往</span>
          <input 
            type="number" 
            v-model.number="jumpPage" 
            :min="1" 
            :max="totalPages"
            @keyup.enter="goToPage(jumpPage)"
          />
          <button class="page-btn" @click="goToPage(jumpPage)" style="margin-left:8px;padding:6px 10px;">前往</button>
          <span style="margin-left:8px;">页</span>
        </div>
      </div>
    </div>
    </div>
  </AdminLayout>

  <!-- 确认对话框 -->
  <ModalPrompt
    v-model="confirmDialog.visible"
    :title="confirmDialog.title"
    :message="confirmDialog.message"
    type="confirm"
    :show-cancel="true"
    confirm-text="确认"
    cancel-text="取消"
    @confirm="confirmDialog.onConfirm"
  />

  <!-- 成功提示框 -->
  <ModalPrompt
    v-model="successDialog.visible"
    :title="successDialog.title"
    :message="successDialog.message"
    type="success"
    confirm-text="好的"
  />

  <!-- 错误提示框 -->
  <ModalPrompt
    v-model="errorDialog.visible"
    :title="errorDialog.title"
    :message="errorDialog.message"
    type="error"
    confirm-text="确定"
  />

  <!-- 信息对话框 -->
  <ModalPrompt
    v-model="infoDialog.visible"
    :title="infoDialog.title"
    :message="infoDialog.message"
    type="info"
    confirm-text="知道了"
  />

  <!-- 航班详情对话框 -->
  <div v-if="detailDialog.visible" class="modal-overlay" @click.self="closeDetailDialog">
    <div class="detail-modal">
      <div class="detail-header">
        <h3>航班详情</h3>
        <button class="close-btn" @click="closeDetailDialog">×</button>
      </div>
      <div class="detail-content" v-if="detailDialog.flight">
        <div class="detail-section">
          <div class="detail-item">
            <span class="detail-label">航班号</span>
            <span class="detail-value">{{ detailDialog.flight.flightNumber }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">航空公司</span>
            <span class="detail-value">{{ detailDialog.flight.airline }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">飞机型号</span>
            <span class="detail-value">{{ detailDialog.flight.aircraftModel }}</span>
          </div>
        </div>
        <div class="detail-section">
          <div class="detail-item">
            <span class="detail-label">出发地</span>
            <span class="detail-value">{{ detailDialog.flight.departure }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">目的地</span>
            <span class="detail-value">{{ detailDialog.flight.destination }}</span>
          </div>
        </div>
        <div class="detail-section">
          <div class="detail-item">
            <span class="detail-label">出发日期</span>
            <span class="detail-value">{{ detailDialog.flight.departureDate }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">出发时间</span>
            <span class="detail-value">{{ detailDialog.flight.departureTime }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">飞行时间</span>
            <span class="detail-value highlight">{{ detailDialog.flight.duration }}</span>
          </div>
        </div>
        <div class="detail-section">
          <div class="detail-item">
            <span class="detail-label">余票数量</span>
            <span class="detail-value">{{ detailDialog.flight.quantity }} 张</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">票价</span>
            <span class="detail-value price">¥{{ detailDialog.flight.price }}</span>
          </div>
        </div>
      </div>
      <div class="detail-actions">
        <button class="btn-secondary" @click="closeDetailDialog">关闭</button>
      </div>
    </div>
  </div>

  <!-- 新增/编辑航班对话框 -->
  <div v-if="flightFormDialog.visible" class="modal-overlay" @click.self="closeFlightFormDialog">
    <div class="flight-form-modal" tabindex="0" @keydown="onModalKeydown" style="width:900px; max-width:95%; max-height:90vh; overflow:auto;">
      <div class="form-header" style="display:flex;align-items:center;gap:12px;justify-content:space-between;">
        <div style="display:flex;align-items:center;gap:12px;">
          <h3 style="margin:0;">{{ flightFormDialog.mode === 'add' ? '新增航班' : '编辑航班' }}</h3>
          <button class="btn-secondary" @click="toggleQuickMode" style="font-size:13px;padding:6px 10px;">
            {{ quickMode ? '快速模式' : '高级模式' }}
          </button>
        </div>
        <button class="close-btn" @click="closeFlightFormDialog">×</button>
      </div>
      <div class="form-content">
        <div class="form-row">
          <div class="form-group">
            <label>航班号 <span class="required">*</span></label>
            <input 
              type="text" 
              v-model="flightForm.flightNo" 
              placeholder="请输入航班号"
              :class="{ 'error': formErrors.flightNo }"
            />
            <span v-if="formErrors.flightNo" class="error-message">{{ formErrors.flightNo }}</span>
          </div>
          <div class="form-group">
            <label>机型 <span class="required">*</span></label>
            <select 
              v-model="flightForm.aircraftTypeId" 
              :class="{ 'error': formErrors.aircraftTypeId }"
            >
              <option :value="null">请选择机型</option>
              <option 
                v-for="opt in availableAircraftOptions" 
                :key="opt.id" 
                :value="opt.id"
              >
                {{ opt.label }}
              </option>
            </select>
            <div style="margin-top:8px; display:flex; gap:8px; align-items:center;">
              <button type="button" class="btn-link" @click="openAircraftManagement">去机型管理</button>
              <span style="color:var(--flight-text-muted);font-size:12px;">（机型管理里新增后可返回选择）</span>
            </div>
            <span v-if="formErrors.aircraftTypeId" class="error-message">{{ formErrors.aircraftTypeId }}</span>
            <small v-if="flightFormDialog.mode === 'edit' && flightForm.aircraftTypeId" class="form-hint">
              当前选择: {{ getAircraftTypeName(flightForm.aircraftTypeId) }}
            </small>
          </div>
        </div>
        <div class="form-row">
          <div class="form-group">
            <label>出发机场 <span class="required">*</span></label>
            <input 
              type="text" 
              v-model="flightForm.originAirport" 
              placeholder="请输入出发机场"
              :class="{ 'error': formErrors.originAirport }"
            />
            <span v-if="formErrors.originAirport" class="error-message">{{ formErrors.originAirport }}</span>
          </div>
          <div class="form-group">
            <label>目的地机场 <span class="required">*</span></label>
            <input 
              type="text" 
              v-model="flightForm.destAirport" 
              placeholder="请输入目的地机场"
              :class="{ 'error': formErrors.destAirport }"
            />
            <span v-if="formErrors.destAirport" class="error-message">{{ formErrors.destAirport }}</span>
          </div>
        </div>
        <div class="form-row">
          <div class="form-group">
            <label>计划出发时间 <span class="required">*</span></label>
            <input 
              type="datetime-local" 
              v-model="flightForm.schedDepTime" 
              :class="{ 'error': formErrors.schedDepTime }"
            />
            <span v-if="formErrors.schedDepTime" class="error-message">{{ formErrors.schedDepTime }}</span>
          </div>
          <div class="form-group">
            <label>计划到达时间 <span class="required">*</span></label>
            <input 
              type="datetime-local" 
              v-model="flightForm.schedArrTime" 
              :class="{ 'error': formErrors.schedArrTime }"
            />
            <span v-if="formErrors.schedArrTime" class="error-message">{{ formErrors.schedArrTime }}</span>
          </div>
        </div>
        <div class="form-row">
          <div class="form-group">
            <label>余票数量</label>
            <input
              type="number"
              v-model.number="flightForm.quantity"
              placeholder="例如: 200"
              min="0"
            />
          </div>
          <div class="form-group">
            <label>票价 (¥)</label>
            <input
              type="number"
              v-model.number="flightForm.price"
              placeholder="例如: 500"
              min="0"
              step="0.01"
            />
          </div>
        </div>
        <!-- 快速模式下的简化路线信息输入：允许直接输入 JSON 或简短备注 -->
        <div class="form-row" v-if="quickMode">
          <div class="form-group" style="width:33.333%">
            <label>经停次数 (stops)</label>
            <input type="number" v-model.number="routeInfoForm.stops" min="0" placeholder="例如: 0" />
          </div>
          <div class="form-group" style="width:33.333%">
            <label>飞行里程 (distance_km)</label>
            <input type="number" v-model.number="routeInfoForm.distance" min="0" placeholder="例如: 1200" />
          </div>
          <div class="form-group" style="width:33.333%; display:flex; align-items:center; gap:8px;">
            <label style="margin:0;">提供餐食 (meal_service)</label>
            <input type="checkbox" v-model="routeInfoForm.mealService" />
          </div>
          <div class="form-group full-width" style="margin-top:8px;">
            <label>补充（可选，JSON 或备注）</label>
            <textarea
              v-model="routeInfoForm.customJson"
              placeholder='例如: {"notes":"直飞"} 或 简短备注: "直飞"'
              rows="2"
            ></textarea>
            <small class="form-hint">快速模式下填写的 JSON 会合并到 routeInfo 中，后端会白名单过滤字段</small>
          </div>
        </div>
        <div class="form-row">
          <div class="form-group">
            <label>航班状态</label>
            <select v-model="flightForm.status">
              <option value="scheduled">已排期</option>
              <option value="delayed">延误</option>
              <option value="cancelled">已取消</option>
              <option value="boarding">登机中</option>
              <option value="departed">已起飞</option>
              <option value="arrived">已到达</option>
            </select>
          </div>
        </div>
        
        <!-- 路线信息扩展区域 -->
        <div class="route-info-section" v-show="!quickMode">
          <div class="section-header" @click="toggleRouteInfo">
            <label>路线信息</label>
            <span class="toggle-icon" :class="{ 'expanded': routeInfoExpanded }">▼</span>
          </div>
          <div v-show="routeInfoExpanded" class="route-info-content">
            <div class="form-row">
              <div class="form-group">
                <label>基础票价 (¥)</label>
                <input 
                  type="number" 
                  v-model.number="routeInfoForm.price" 
                  placeholder="例如: 500"
                  min="0"
                  step="0.01"
                />
              </div>
              <div class="form-group">
                <label>飞行里程 (公里)</label>
                <input 
                  type="number" 
                  v-model.number="routeInfoForm.distance" 
                  placeholder="例如: 1200"
                  min="0"
                />
              </div>
            </div>
            <div class="form-row">
              <div class="form-group">
                <label>是否经停</label>
                <select v-model="routeInfoForm.hasStopover">
                  <option :value="false">直飞</option>
                  <option :value="true">经停</option>
                </select>
              </div>
              <div class="form-group" v-if="routeInfoForm.hasStopover">
                <label>经停城市</label>
                <input 
                  type="text" 
                  v-model="routeInfoForm.stopoverCity" 
                  placeholder="例如: 西安"
                />
              </div>
            </div>
            <div class="form-row">
              <div class="form-group">
                <label>经济舱价格 (¥)</label>
                <input 
                  type="number" 
                  v-model.number="routeInfoForm.economyPrice" 
                  placeholder="例如: 500"
                  min="0"
                  step="0.01"
                />
              </div>
              <div class="form-group">
                <label>商务舱价格 (¥)</label>
                <input 
                  type="number" 
                  v-model.number="routeInfoForm.businessPrice" 
                  placeholder="例如: 1500"
                  min="0"
                  step="0.01"
                />
              </div>
            </div>
            <div class="form-row">
              <div class="form-group">
                <label>头等舱价格 (¥)</label>
                <input 
                  type="number" 
                  v-model.number="routeInfoForm.firstClassPrice" 
                  placeholder="例如: 3000"
                  min="0"
                  step="0.01"
                />
              </div>
              <div class="form-group">
                <label>税费 (¥)</label>
                <input 
                  type="number" 
                  v-model.number="routeInfoForm.tax" 
                  placeholder="例如: 120"
                  min="0"
                  step="0.01"
                />
              </div>
            </div>
            <div class="form-row">
              <div class="form-group full-width">
                <label>备注信息</label>
                <textarea 
                  v-model="routeInfoForm.notes" 
                  placeholder="可输入其他路线相关信息..."
                  rows="2"
                ></textarea>
              </div>
            </div>
            <div class="form-row">
              <div class="form-group full-width">
                <label class="advanced-label">
                  <span>高级选项 (JSON格式)</span>
                  <small>如需添加其他自定义字段，可在此输入JSON格式数据</small>
                </label>
                <textarea 
                  v-model="routeInfoForm.customJson" 
                  placeholder='例如: {"baggageAllowance": "20kg", "mealService": true}'
                  rows="3"
                  class="json-input"
                ></textarea>
              </div>
            </div>
          </div>
        </div>
      </div>
      <div class="form-actions">
        <button class="btn-secondary" @click="closeFlightFormDialog" :disabled="flightFormDialog.loading">取消</button>
        <button 
          class="btn-primary" 
          @click="handleSaveFlight" 
          :disabled="flightFormDialog.loading"
        >
          {{ flightFormDialog.loading ? '保存中...' : '保存' }}
        </button>
      </div>
    </div>
  </div>

  <!-- 新增机型小弹窗 -->
  <div v-if="newAircraftDialog.visible" class="modal-overlay" @click.self="closeNewAircraftDialog">
    <div class="flight-form-modal" style="max-width:520px;">
      <div class="form-header">
        <h3>新增机型</h3>
        <button class="close-btn" @click="closeNewAircraftDialog">×</button>
      </div>
      <div class="form-content">
        <div class="form-row">
          <div class="form-group">
            <label>机型代码 <span class="required">*</span></label>
            <input type="text" v-model="newAircraft.typeCode" placeholder="例如: ARJ21-700" />
          </div>
          <div class="form-group">
            <label>型号</label>
            <input type="text" v-model="newAircraft.model" placeholder="例如: ARJ21-700" />
          </div>
        </div>
        <div class="form-row">
          <div class="form-group full-width">
            <label>制造商</label>
            <input type="text" v-model="newAircraft.manufacturer" placeholder="例如: 中国商飞" />
          </div>
        </div>
      </div>
      <div class="form-actions">
        <button class="btn-secondary" @click="closeNewAircraftDialog" :disabled="newAircraftDialog.loading">取消</button>
        <button class="btn-primary" @click="createNewAircraftType" :disabled="newAircraftDialog.loading">
          {{ newAircraftDialog.loading ? '创建中...' : '创建并选择' }}
        </button>
      </div>
    </div>
  </div>

  <!-- 批量复制新增模态 -->
  <div v-if="bulkDialog.visible" class="modal-overlay" @click.self="bulkDialog.visible = false">
    <div class="flight-form-modal" style="max-width:900px; width:90%; max-height:80vh; overflow:auto;">
      <div class="form-header">
        <h3>批量从选中复制新增</h3>
        <button class="close-btn" @click="bulkDialog.visible = false">×</button>
      </div>
      <div class="form-content">
        <table style="width:100%;border-collapse:collapse;">
          <thead>
            <tr>
              <th style="text-align:left;padding:8px;">选择</th>
              <th style="text-align:left;padding:8px;">源航班号</th>
              <th style="text-align:left;padding:8px;">新航班号</th>
              <th style="text-align:left;padding:8px;">出发时间</th>
              <th style="text-align:left;padding:8px;">余票</th>
              <th style="text-align:left;padding:8px;">票价</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(item, idx) in bulkItems" :key="idx" style="border-top:1px solid rgba(255,255,255,0.03);">
              <td style="padding:8px;">
                <input type="checkbox" v-model="item.selected" />
              </td>
              <td style="padding:8px;">{{ item.src?.flightNumber || item.sourceId }}</td>
              <td style="padding:8px;"><input v-model="item.flightNo" /></td>
              <td style="padding:8px;"><input v-model="item.schedDepTime" /></td>
              <td style="padding:8px;width:80px;"><input type="number" v-model.number="item.quantity" /></td>
              <td style="padding:8px;width:100px;"><input type="number" v-model.number="item.price" /></td>
            </tr>
          </tbody>
        </table>
      </div>
      <div class="form-actions" style="justify-content:flex-end;">
        <div style="margin-right:auto;padding-left:12px;color:var(--flight-text-muted);">进度: {{ bulkDialog.progress }}%</div>
        <button class="btn-secondary" @click="bulkDialog.visible = false" :disabled="bulkDialog.loading">取消</button>
        <button class="btn-primary" @click="createBulkFlights" :disabled="bulkDialog.loading">{{ bulkDialog.loading ? '创建中...' : '创建全部' }}</button>
      </div>
    </div>
  </div>

</template>

<script setup lang="ts">
// Disable automatic attribute inheritance to avoid:
// "Extraneous non-props attributes (class) were passed to component but could not be automatically inherited..."
// The SFC compiler macro is used to set inheritAttrs to false.
// This prevents unexpected class/attrs passed from router/parent being forwarded to child fragments.
defineOptions({ inheritAttrs: false })
import { ref, reactive, computed, onMounted, nextTick } from 'vue'
import AdminLayout from '../components/AdminLayout.vue'
import ModalPrompt from '../components/ModalPrompt.vue'
import { flightManagementApi, aircraftTypeApi } from '../services/api'
import { useRouter } from 'vue-router'

interface Flight {
  id: string
  departureDate: string
  flightNumber: string
  image?: string
  airline: string
  aircraftModel: string
  departure: string
  destination: string
  departureTime: string
  duration: string
  quantity: number
  price: number
  companyAccount: string
}

const searchParams = reactive({
  departureDateStart: '',
  departureDateEnd: '',
  airline: '',
  departure: '',
  destination: ''
})

const flights = ref<Flight[]>([
  {
    id: '1',
    departureDate: '2022-03-14',
    flightNumber: 'C12340',
    image: '/airplane-icon.png',
    airline: '幸运航空公司',
    aircraftModel: '波音123',
    departure: '北京机场',
    destination: '上海机场',
    departureTime: '2022-03-14 06:00:00',
    duration: '3小时10分',
    quantity: 300,
    price: 780,
    companyAccount: '111'
  }
])

const selectedFlights = ref<string[]>([])
const selectAll = ref(false)
const currentPage = ref(1)
const pageSize = ref(5)
const total = ref(1)
const jumpPage = ref(1)

const totalPages = computed(() => Math.max(1, Math.ceil(total.value / pageSize.value)))

// 兼容多种后端分页/列表返回格式，提取列表和总数
const normalizeListAndTotal = (rawResult: any) => {
  if (!rawResult) return { list: [] as any[], total: 0 }

  // 如果是包装结构 { success, data }
  const payload = (rawResult && rawResult.success !== undefined && rawResult.data !== undefined) ? rawResult.data : rawResult

  // 常见字段：flights, content, list, data.content
  let list: any[] = []
  if (Array.isArray(payload?.flights)) list = payload.flights
  else if (Array.isArray(payload?.content)) list = payload.content
  else if (Array.isArray(payload?.list)) list = payload.list
  else if (Array.isArray(payload?.data)) list = payload.data
  else if (Array.isArray(payload)) list = payload
  // 进一步兼容：payload.data.content
  else if (payload?.data && Array.isArray(payload.data.content)) list = payload.data.content

  const totalCount = payload?.total ?? payload?.totalElements ?? payload?.total_count ?? (payload?.page && (payload.page.totalElements ?? payload.page.total)) ?? (Array.isArray(list) ? list.length : 0)

  return { list, total: Number(totalCount || 0) }
}

// 弹窗状态
const confirmDialog = reactive({
  visible: false,
  title: '',
  message: '',
  onConfirm: () => {}
})

const successDialog = reactive({
  visible: false,
  title: '',
  message: ''
})

const errorDialog = reactive({
  visible: false,
  title: '',
  message: ''
})

const infoDialog = reactive({
  visible: false,
  title: '',
  message: ''
})

// 详情对话框
const detailDialog = reactive({
  visible: false,
  flight: null as Flight | null
})

// 航班表单对话框
const flightFormDialog = reactive({
  visible: false,
  mode: 'add' as 'add' | 'edit',
  loading: false,
  flightId: null as string | null
})

// 航班表单数据
const flightForm = reactive({
  flightNo: '',
  aircraftTypeId: null as number | string | null,
  originAirport: '',
  destAirport: '',
  schedDepTime: '',
  schedArrTime: '',
  status: 'scheduled',
  routeInfo: '',
  quantity: null as number | null,
  price: null as number | null
})

// 路线信息表单数据
const routeInfoForm = reactive({
  price: null as number | null,
  distance: null as number | null,
  hasStopover: false,
  stopoverCity: '',
  economyPrice: null as number | null,
  businessPrice: null as number | null,
  firstClassPrice: null as number | null,
  tax: null as number | null,
  notes: '',
  customJson: ''
  ,
  stops: null as number | null,
  mealService: false
})

// 路线信息展开状态
const routeInfoExpanded = ref(false)

// 表单验证错误
const formErrors = reactive({
  flightNo: '',
  aircraftTypeId: '',
  originAirport: '',
  destAirport: '',
  schedDepTime: '',
  schedArrTime: ''
})

// 机型列表
const aircraftTypes = ref<any[]>([])

// 切换全选
const toggleSelectAll = () => {
  if (selectAll.value) {
    selectedFlights.value = flights.value.map(f => f.id)
  } else {
    selectedFlights.value = []
  }
}

// 搜索（reset 表示是否将页码重置为第一页，默认 true，用于点击查询时）
const handleSearch = async (reset = true) => {
  try {
    if (reset) currentPage.value = 1
    const result = await flightManagementApi.getFlightList({
      page: currentPage.value - 1, // 后端使用0-based分页
      size: pageSize.value,
      departureDateStart: searchParams.departureDateStart,
      departureDateEnd: searchParams.departureDateEnd,
      airline: searchParams.airline,
      departure: searchParams.departure,
      destination: searchParams.destination
    })
    
    // 兼容各种后端返回格式，提取列表与总数
    const parsed = normalizeListAndTotal(result)
    flights.value = parsed.list || []
    total.value = parsed.total || 0
  } catch (error) {
    console.error('搜索航班失败:', error)
    flights.value = []
    total.value = 0
  }
}

// 加载机型列表
  const loadAircraftTypes = async () => {
  try {
    const result = await aircraftTypeApi.getAircraftTypes('active')
    if (result && result.success && result.data) {
      aircraftTypes.value = Array.isArray(result.data) ? result.data : []
    } else if (Array.isArray(result)) {
      aircraftTypes.value = result
    } else if (result && result.data && Array.isArray(result.data)) {
      aircraftTypes.value = result.data
    }
  } catch (error) {
    console.error('加载机型列表失败:', error)
    aircraftTypes.value = []
  }
}

  // 打开机型管理页面（在新标签页或当前路由）
  const router = useRouter()
  const openAircraftManagement = () => {
    try {
      router.push('/portal/aircraft')
    } catch (e) {
      window.open('/portal/aircraft', '_blank')
    }
  }

// 获取机型名称
const getAircraftTypeName = (aircraftTypeId: number | string | null): string => {
  if (!aircraftTypeId) return ''
  const aircraft = aircraftTypes.value.find(at => String(at.id) === String(aircraftTypeId))
  if (aircraft) {
    return `${aircraft.model || aircraft.typeCode} (${aircraft.manufacturer || '未知制造商'})`
  }
  return ''
}

// 机型下拉可选项：优先使用机型管理数据，否则从现有航班机型中提取候选
const availableAircraftOptions = computed(() => {
  if (aircraftTypes.value && aircraftTypes.value.length > 0) {
    return aircraftTypes.value.map(at => ({
      id: at.id,
      label: `${at.model || at.typeCode || at.type || at.id} ${at.manufacturer ? `(${at.manufacturer})` : ''}`.trim()
    }))
  }
  // 回退：使用 flights 中的 aircraftModel 字段作为可选项
  const models = Array.from(new Set(flights.value.map(f => f.aircraftModel).filter(Boolean)))
  return models.map(m => ({ id: `model__${m}`, label: String(m) }))
})

// 内嵌新增机型状态与逻辑
const newAircraftDialog = reactive({
  visible: false,
  loading: false
})
const newAircraft = reactive({
  typeCode: '',
  model: '',
  manufacturer: ''
})
const openNewAircraftDialog = () => {
  newAircraftDialog.visible = true
  newAircraft.typeCode = ''
  newAircraft.model = ''
  newAircraft.manufacturer = ''
}
const closeNewAircraftDialog = () => {
  if (newAircraftDialog.loading) return
  newAircraftDialog.visible = false
}
const createNewAircraftType = async () => {
  if (!newAircraft.typeCode && !newAircraft.model) {
    errorDialog.title = '请输入机型标识或型号'
    errorDialog.message = '请填写机型代码或型号以创建新机型'
    errorDialog.visible = true
    return
  }
  newAircraftDialog.loading = true
  try {
    const payload: any = {
      typeCode: newAircraft.typeCode || newAircraft.model,
      model: newAircraft.model || undefined,
      manufacturer: newAircraft.manufacturer || undefined,
      status: 'active'
    }
    const result = await aircraftTypeApi.createAircraftType(payload)
    let created: any = null
    if (result && result.success && result.data) created = result.data
    else if (result && result.id) created = result
    else created = result
    await loadAircraftTypes()
    if (created && (created.id || created.typeCode)) {
      flightForm.aircraftTypeId = created.id ?? created.typeCode ?? null
    }
    newAircraftDialog.visible = false
    successDialog.title = '机型新增成功'
    successDialog.message = '已新增机型并选择为当前机型'
    successDialog.visible = true
  } catch (e: any) {
    console.error('创建机型失败:', e)
    errorDialog.title = '创建失败'
    errorDialog.message = e?.message || '创建机型失败，请稍后重试'
    errorDialog.visible = true
  } finally {
    newAircraftDialog.loading = false
  }
}

// 快速/高级 模式切换
const quickMode = ref(true)
const toggleQuickMode = () => { quickMode.value = !quickMode.value }

// 批量复制新增支持
const bulkDialog = reactive({
  visible: false,
  loading: false,
  progress: 0
})
const bulkItems = ref<any[]>([])
const openBulkCreate = () => {
  if (!selectedFlights.value || selectedFlights.value.length === 0) {
    errorDialog.title = '请选择航班'
    errorDialog.message = '请先选择要复制的航班'
    errorDialog.visible = true
    return
  }
  bulkItems.value = selectedFlights.value.map(id => {
    const src = flights.value.find(f => String(f.id) === String(id)) as any
    return {
      selected: true,
      sourceId: id,
      flightNo: `${src?.flightNumber || ''}_copy`,
      aircraftTypeId: src?.aircraftModel || null,
      originAirport: src?.departure || '',
      destAirport: src?.destination || '',
      schedDepTime: src?.departureTime || '',
      schedArrTime: '',
      quantity: src?.quantity ?? 200,
      price: src?.price ?? 500,
      status: 'scheduled',
      src
    }
  })
  bulkDialog.visible = true
}
const createBulkFlights = async () => {
  const toCreate = bulkItems.value.filter(i => i.selected)
  if (toCreate.length === 0) {
    errorDialog.title = '没有要创建的项'
    errorDialog.message = '请勾选至少一条要创建的航班'
    errorDialog.visible = true
    return
  }
  bulkDialog.loading = true
  bulkDialog.progress = 0
  const total = toCreate.length
  const results: any[] = []
  for (let i = 0; i < total; i++) {
    const item = toCreate[i]
    // resolve aircraft type: prefer numeric id from aircraftTypes, fallback to aircraftModel string
    let resolvedTypeId: number | undefined = undefined
    let resolvedModel: string | undefined = undefined
    if (item.aircraftTypeId) {
      const raw = String(item.aircraftTypeId)
      if (raw.startsWith('model__')) {
        resolvedModel = raw.replace('model__', '')
      } else {
        // try to find numeric id by matching model or typeCode
        const matched = aircraftTypes.value.find(at => String(at.model) === raw || String(at.typeCode) === raw || String(at.id) === raw)
        if (matched) resolvedTypeId = matched.id
        else {
          // treat as model string
          resolvedModel = raw
        }
      }
    }

    const payload: any = {
      flightNo: String(item.flightNo || '').trim(),
      originAirport: item.originAirport,
      destAirport: item.destAirport,
      schedDepTime: item.schedDepTime,
      schedArrTime: item.schedArrTime,
      status: item.status,
      quantity: item.quantity,
      price: item.price
    }
    if (resolvedTypeId !== undefined) payload.aircraftTypeId = resolvedTypeId
    else if (resolvedModel) payload.aircraftModel = resolvedModel

    // basic validation before sending
    if (!payload.flightNo || !payload.originAirport || !payload.destAirport || !payload.schedDepTime) {
      results.push({ success: false, error: new Error('缺少必填字段（航班号/出发地/目的地/出发时间）') })
      bulkDialog.progress = Math.round(((i+1)/total)*100)
      continue
    }

    try {
      const res = await flightManagementApi.createFlight(payload)
      results.push({ success: true, res })
    } catch (e: any) {
      results.push({ success: false, error: e })
    } finally {
      bulkDialog.progress = Math.round(((i+1)/total)*100)
    }
  }
  bulkDialog.loading = false
  bulkDialog.visible = false
  await handleSearch()
  successDialog.title = '批量创建完成'
  successDialog.message = `成功 ${results.filter(r => r.success).length} 条，失败 ${results.filter(r => !r.success).length} 条`
  successDialog.visible = true
}
// 切换路线信息展开/收起
const toggleRouteInfo = () => {
  routeInfoExpanded.value = !routeInfoExpanded.value
}

// 重置路线信息表单
const resetRouteInfoForm = () => {
  routeInfoForm.price = null
  routeInfoForm.distance = null
  routeInfoForm.hasStopover = false
  routeInfoForm.stopoverCity = ''
  routeInfoForm.economyPrice = null
  routeInfoForm.businessPrice = null
  routeInfoForm.firstClassPrice = null
  routeInfoForm.tax = null
  routeInfoForm.notes = ''
  routeInfoForm.customJson = ''
}

// 构建路线信息JSON
const buildRouteInfo = (): string => {
  const routeInfo: any = {}
  
  // 基础信息（保留 price）
  if (routeInfoForm.price !== null && routeInfoForm.price !== undefined) {
    routeInfo.price = routeInfoForm.price
  }

  // 使用 distance_km 作为标准字段名
  if (routeInfoForm.distance !== null && routeInfoForm.distance !== undefined) {
    routeInfo.distance_km = routeInfoForm.distance
  }

  // 快速模式支持的简化字段：stops / meal_service
  if ((routeInfoForm as any).stops !== null && (routeInfoForm as any).stops !== undefined) {
    routeInfo.stops = Number((routeInfoForm as any).stops)
  }
  if ((routeInfoForm as any).mealService !== undefined) {
    routeInfo.meal_service = Boolean((routeInfoForm as any).mealService)
  }

  // 其他详细字段（仅在非快速/高级模式时使用）
  if (routeInfoForm.hasStopover) {
    routeInfo.hasStopover = true
    if (routeInfoForm.stopoverCity) {
      routeInfo.stopoverCity = routeInfoForm.stopoverCity
    }
  }

  // 舱位价格（保留但在快速模式下一般不使用）
  const cabinPrices: any = {}
  if (routeInfoForm.economyPrice !== null && routeInfoForm.economyPrice !== undefined) {
    cabinPrices.economy = routeInfoForm.economyPrice
  }
  if (routeInfoForm.businessPrice !== null && routeInfoForm.businessPrice !== undefined) {
    cabinPrices.business = routeInfoForm.businessPrice
  }
  if (routeInfoForm.firstClassPrice !== null && routeInfoForm.firstClassPrice !== undefined) {
    cabinPrices.firstClass = routeInfoForm.firstClassPrice
  }
  if (Object.keys(cabinPrices).length > 0) {
    routeInfo.cabinPrices = cabinPrices
  }

  // 税费与备注
  if (routeInfoForm.tax !== null && routeInfoForm.tax !== undefined) {
    routeInfo.tax = routeInfoForm.tax
  }
  if (routeInfoForm.notes) {
    routeInfo.notes = routeInfoForm.notes
  }

  // 自定义JSON（会合并，但服务器端会对最终 routeInfo 做白名单过滤）
  if (routeInfoForm.customJson && routeInfoForm.customJson.trim() !== '') {
    try {
      const customData = JSON.parse(routeInfoForm.customJson)
      Object.assign(routeInfo, customData)
    } catch (e) {
      console.warn('自定义JSON格式错误，已忽略:', e)
    }
  }

  return Object.keys(routeInfo).length > 0 ? JSON.stringify(routeInfo) : ''
}

// 解析路线信息JSON
const parseRouteInfo = (routeInfoJson: string | null | undefined) => {
  resetRouteInfoForm()
  
  if (!routeInfoJson || routeInfoJson.trim() === '') {
    return
  }
  
  try {
    const routeInfo = JSON.parse(routeInfoJson)
    
    if (routeInfo.price !== undefined) routeInfoForm.price = routeInfo.price
    if (routeInfo.distance_km !== undefined) routeInfoForm.distance = routeInfo.distance_km
    else if (routeInfo.distance !== undefined) routeInfoForm.distance = routeInfo.distance
    if (routeInfo.stops !== undefined) (routeInfoForm as any).stops = routeInfo.stops
    if (routeInfo.meal_service !== undefined) (routeInfoForm as any).mealService = routeInfo.meal_service
    if (routeInfo.hasStopover !== undefined) routeInfoForm.hasStopover = routeInfo.hasStopover
    if (routeInfo.stopoverCity) routeInfoForm.stopoverCity = routeInfo.stopoverCity
    if (routeInfo.notes) routeInfoForm.notes = routeInfo.notes
    
    // 舱位价格
    if (routeInfo.cabinPrices) {
      if (routeInfo.cabinPrices.economy !== undefined) routeInfoForm.economyPrice = routeInfo.cabinPrices.economy
      if (routeInfo.cabinPrices.business !== undefined) routeInfoForm.businessPrice = routeInfo.cabinPrices.business
      if (routeInfo.cabinPrices.firstClass !== undefined) routeInfoForm.firstClassPrice = routeInfo.cabinPrices.firstClass
    }
    
    // 兼容旧格式（直接使用price作为经济舱价格）
    if (routeInfo.price && !routeInfo.cabinPrices) {
      routeInfoForm.economyPrice = routeInfo.price
    }
    
    if (routeInfo.tax !== undefined) routeInfoForm.tax = routeInfo.tax
    
    // 提取自定义字段（不在标准字段中的）
    const standardFields = ['price', 'distance', 'hasStopover', 'stopoverCity', 'cabinPrices', 'tax', 'notes']
    const customFields: any = {}
    Object.keys(routeInfo).forEach(key => {
      if (!standardFields.includes(key)) {
        customFields[key] = routeInfo[key]
      }
    })
    if (Object.keys(customFields).length > 0) {
      routeInfoForm.customJson = JSON.stringify(customFields, null, 2)
    }
  } catch (e) {
    console.warn('解析路线信息JSON失败:', e)
  }
}

// 新增
const handleAdd = async () => {
  // 确保机型已加载，避免打开弹窗时下拉为空
  try {
    await loadAircraftTypes()
  } catch (e) {
    console.warn('加载机型失败:', e)
  }
  // 重置表单
  flightForm.flightNo = ''
  flightForm.aircraftTypeId = null
  flightForm.originAirport = ''
  flightForm.destAirport = ''
  flightForm.schedDepTime = ''
  flightForm.schedArrTime = ''
  flightForm.status = 'scheduled'
  flightForm.routeInfo = ''
  // 默认值，使新增更快捷
  flightForm.quantity = flightForm.quantity ?? 200
  flightForm.price = flightForm.price ?? 500
  // 默认填充路线信息简要字段，便于快速新增
  routeInfoForm.price = routeInfoForm.price ?? flightForm.price ?? 500
  routeInfoForm.distance = routeInfoForm.distance ?? 1200
  routeInfoForm.hasStopover = routeInfoForm.hasStopover ?? false
  routeInfoForm.economyPrice = routeInfoForm.economyPrice ?? flightForm.price ?? 500
  routeInfoForm.businessPrice = routeInfoForm.businessPrice ?? Math.round((flightForm.price ?? 500) * 1.8)
  routeInfoForm.firstClassPrice = routeInfoForm.firstClassPrice ?? 3000
  routeInfoForm.tax = routeInfoForm.tax ?? 120
  routeInfoForm.notes = routeInfoForm.notes ?? ''
  routeInfoForm.customJson = routeInfoForm.customJson ?? ''
  // 保持 routeInfoForm 的默认值（快速模式需要使用 routeInfo），不在这里重置
  routeInfoExpanded.value = false
  
  // 清空错误
  Object.keys(formErrors).forEach(key => {
    (formErrors as any)[key] = ''
  })
  
  flightFormDialog.mode = 'add'
  flightFormDialog.visible = true
  // 聚焦弹窗以便接收键盘事件（Ctrl+Enter 保存）
  await nextTick()
  const modalCard = document.querySelector('.flight-form-modal') as HTMLElement | null
  if (modalCard) modalCard.focus()
}

// 从选中航班复制并新增
const handleAddFromSelected = async () => {
  // 确保机型列表已加载
  try {
    await loadAircraftTypes()
  } catch (e) {
    console.warn('加载机型失败:', e)
  }
  if (!selectedFlights.value || selectedFlights.value.length === 0) {
    errorDialog.title = '请选择航班'
    errorDialog.message = '请先选择一个航班以便复制其信息'
    errorDialog.visible = true
    return
  }
  if (selectedFlights.value.length > 1) {
    errorDialog.title = '请选择单个航班'
    errorDialog.message = '请只选择一个航班来复制'
    errorDialog.visible = true
    return
  }

  const flightId = selectedFlights.value[0]
  const src = flights.value.find(f => String(f.id) === String(flightId))
  if (!src) {
    errorDialog.title = '航班未找到'
    errorDialog.message = '无法找到选中的航班，请刷新后重试'
    errorDialog.visible = true
    return
  }

  // 以选中航班信息填充表单（保留为新增模式）
  flightForm.flightNo = `${src.flightNumber || ''}_copy`
  // 尝试从机型名称匹配机型ID（若有机型列表）
  if (src.aircraftModel && aircraftTypes.value && aircraftTypes.value.length > 0) {
    const matched = aircraftTypes.value.find(at => at.model === src.aircraftModel || at.typeCode === src.aircraftModel)
    if (matched) {
      flightForm.aircraftTypeId = matched.id
    } else {
      flightForm.aircraftTypeId = null
    }
  } else {
    flightForm.aircraftTypeId = null
  }
  flightForm.originAirport = src.departure || ''
  flightForm.destAirport = src.destination || ''
  // 复制出发时间（若存在）
  flightForm.schedDepTime = src.departureTime || ''
  // 如果有出发时间和前端的 duration 字段，则尝试计算到达时间
  flightForm.schedArrTime = ''
  try {
    if (flightForm.schedDepTime && src.duration) {
      const dep = new Date(flightForm.schedDepTime)
      const m = String(src.duration).match(/(\d+)小时(\d+)分/)
      if (m) {
        dep.setHours(dep.getHours() + Number(m[1]))
        dep.setMinutes(dep.getMinutes() + Number(m[2]))
        flightForm.schedArrTime = dep.toISOString().slice(0,16)
      }
    }
  } catch (e) { /* ignore */ }
  // 尝试填充路线信息：优先使用后端 routeInfo 字段（若存在），否则使用 src 的常见字段作快速填充
  if ((src as any).routeInfo) {
    try {
      parseRouteInfo((src as any).routeInfo)
      routeInfoExpanded.value = false
    } catch (e) {
      // ignore parse errors
    }
  } else {
    routeInfoForm.price = routeInfoForm.price ?? (src as any).price ?? flightForm.price ?? 500
    routeInfoForm.distance = routeInfoForm.distance ?? (src as any).distance ?? 1200
    routeInfoForm.hasStopover = routeInfoForm.hasStopover ?? false
    routeInfoForm.economyPrice = routeInfoForm.economyPrice ?? (src as any).economyPrice ?? (src as any).price ?? flightForm.price ?? 500
    routeInfoForm.businessPrice = routeInfoForm.businessPrice ?? (src as any).businessPrice ?? Math.round((routeInfoForm.economyPrice || 500) * 1.8)
    routeInfoForm.firstClassPrice = routeInfoForm.firstClassPrice ?? (src as any).firstClassPrice ?? 3000
    routeInfoForm.tax = routeInfoForm.tax ?? (src as any).tax ?? 120
    routeInfoForm.notes = routeInfoForm.notes ?? (src as any).notes ?? ''
    routeInfoForm.customJson = routeInfoForm.customJson ?? ''
  }
  flightForm.status = 'scheduled'
  flightForm.quantity = typeof src.quantity === 'number' ? src.quantity : null
  flightForm.price = typeof src.price === 'number' ? src.price : null

  // 不自动展开路线信息，保持简单表单
  // 保留已填充的 routeInfoForm（复制时应保留路线信息），但不自动展开
  routeInfoExpanded.value = false

  // 清空表单错误
  Object.keys(formErrors).forEach(key => {
    (formErrors as any)[key] = ''
  })

  flightFormDialog.mode = 'add'
  flightFormDialog.visible = true
  await nextTick()
  const modalCard2 = document.querySelector('.flight-form-modal') as HTMLElement | null
  if (modalCard2) modalCard2.focus()
}

// 批量删除
const handleDelete = async () => {
  if (selectedFlights.value.length === 0) {
    errorDialog.title = '请选择航班'
    errorDialog.message = '请先选择要删除的航班'
    errorDialog.visible = true
    return
  }
  
  confirmDialog.title = '确认删除'
  confirmDialog.message = `确定要删除选中的 ${selectedFlights.value.length} 条航班吗？\n\n此操作不可撤销！`
  confirmDialog.onConfirm = async () => {
    confirmDialog.visible = false
    try {
      const result = await flightManagementApi.deleteFlights(selectedFlights.value)
      if (result && result.success !== false) {
        successDialog.title = '删除成功'
        successDialog.message = `已成功删除 ${selectedFlights.value.length} 条航班`
        successDialog.visible = true
        selectedFlights.value = []
        selectAll.value = false
        await handleSearch()
      } else {
        throw new Error(result?.message || '删除失败')
      }
    } catch (error: any) {
      console.error('删除航班失败:', error)
      errorDialog.title = '删除失败'
      const rawMsg = (error?.message || '').toString()
      if (/foreign key|外键|关联机票|Cannot delete or update a parent row/i.test(rawMsg)) {
        errorDialog.message = '存在关联机票，无法删除航班，请先删除相关机票或取消关联'
      } else {
        errorDialog.message = error.message || '删除失败，请稍后重试'
      }
      errorDialog.visible = true
    }
  }
  confirmDialog.visible = true
}

// 详情
const handleDetail = (flight: Flight) => {
  detailDialog.flight = flight
  detailDialog.visible = true
}

// 关闭详情对话框
const closeDetailDialog = () => {
  detailDialog.visible = false
  detailDialog.flight = null
}

// 编辑
const handleEdit = async (flight: Flight) => {
  try {
    // 获取航班详细信息（包含后端原始数据）
    const result = await flightManagementApi.getFlightById(flight.id)
    let flightData = null
    
    if (result && result.success && result.data) {
      flightData = result.data
    } else if (result && result.data) {
      flightData = result.data
    } else {
      flightData = result
    }
    
    if (!flightData) {
      throw new Error('无法获取航班详细信息')
    }
    
    // 填充表单 - 使用后端返回的完整数据
    flightForm.flightNo = flightData.flightNumber || flight.flightNumber || ''
    
    // 机型ID - 优先使用后端返回的aircraftTypeId
    if (flightData.aircraftTypeId) {
      flightForm.aircraftTypeId = flightData.aircraftTypeId
    } else {
      // 如果没有aircraftTypeId，尝试从机型名称匹配
      if (flightData.aircraftModel && aircraftTypes.value.length > 0) {
        const matchedAircraft = aircraftTypes.value.find(at => 
          at.model === flightData.aircraftModel || 
          at.typeCode === flightData.aircraftModel
        )
        if (matchedAircraft) {
          flightForm.aircraftTypeId = matchedAircraft.id
        }
      }
    }
    
    flightForm.originAirport = flightData.departure || flight.departure || ''
    flightForm.destAirport = flightData.destination || flight.destination || ''
    
    // 解析日期时间 - 优先使用后端返回的完整时间
    if (flightData.departureTime) {
      const depTime = new Date(flightData.departureTime)
      if (!isNaN(depTime.getTime())) {
        flightForm.schedDepTime = depTime.toISOString().slice(0, 16)
      }
    } else if (flight.departureTime) {
      const depTime = new Date(flight.departureTime)
      if (!isNaN(depTime.getTime())) {
        flightForm.schedDepTime = depTime.toISOString().slice(0, 16)
      }
    }
    
    // 到达时间 - 优先使用后端返回的arrivalTime
    if (flightData.arrivalTime) {
      const arrTime = new Date(flightData.arrivalTime)
      if (!isNaN(arrTime.getTime())) {
        flightForm.schedArrTime = arrTime.toISOString().slice(0, 16)
      }
    } else if (flightData.departureTime && flightData.duration) {
      // 如果没有到达时间，从出发时间+飞行时间计算
      const depTime = new Date(flightData.departureTime)
      const durationMatch = flightData.duration.match(/(\d+)小时(\d+)分/)
      if (durationMatch && durationMatch[1] && durationMatch[2]) {
        const hours = parseInt(durationMatch[1])
        const minutes = parseInt(durationMatch[2])
        depTime.setHours(depTime.getHours() + hours)
        depTime.setMinutes(depTime.getMinutes() + minutes)
        flightForm.schedArrTime = depTime.toISOString().slice(0, 16)
      }
    } else if (flight.departureTime && flight.duration) {
      // 使用前端数据计算
      const depTime = new Date(flight.departureTime)
      const durationMatch = flight.duration.match(/(\d+)小时(\d+)分/)
      if (durationMatch && durationMatch[1] && durationMatch[2]) {
        const hours = parseInt(durationMatch[1])
        const minutes = parseInt(durationMatch[2])
        depTime.setHours(depTime.getHours() + hours)
        depTime.setMinutes(depTime.getMinutes() + minutes)
        flightForm.schedArrTime = depTime.toISOString().slice(0, 16)
      }
    }
    
    // 航班状态
    flightForm.status = flightData.status || 'scheduled'
    
    // 解析路线信息
    if (flightData.routeInfo) {
      parseRouteInfo(flightData.routeInfo)
      routeInfoExpanded.value = true
    } else {
      resetRouteInfoForm()
      routeInfoExpanded.value = false
    }
    
    // 清空错误
    Object.keys(formErrors).forEach(key => {
      (formErrors as any)[key] = ''
    })
    
    flightFormDialog.mode = 'edit'
    flightFormDialog.flightId = flight.id
    flightFormDialog.visible = true
  } catch (error: any) {
    console.error('获取航班详情失败:', error)
    errorDialog.title = '获取航班信息失败'
    errorDialog.message = error.message || '无法加载航班详细信息，请稍后重试'
    errorDialog.visible = true
  }
}

// 关闭表单对话框
const closeFlightFormDialog = () => {
  if (flightFormDialog.loading) return
  flightFormDialog.visible = false
  flightFormDialog.flightId = null
  routeInfoExpanded.value = false
}

// 弹窗键盘处理（Ctrl+Enter 提交）
const onModalKeydown = (e: KeyboardEvent) => {
  if ((e.ctrlKey || e.metaKey) && (e.key === 'Enter' || e.key === 'Enter')) {
    // 防止重复触发
    if (!flightFormDialog.loading) {
      // 触发保存
      // @ts-ignore
      handleSaveFlight()
    }
  }
}

// 表单验证
const validateForm = (): boolean => {
  let isValid = true
  
  // 清空之前的错误
  Object.keys(formErrors).forEach(key => {
    (formErrors as any)[key] = ''
  })
  
  if (!flightForm.flightNo || flightForm.flightNo.trim() === '') {
    formErrors.flightNo = '请输入航班号'
    isValid = false
  }
  
  if (!flightForm.aircraftTypeId) {
    formErrors.aircraftTypeId = '请选择机型'
    isValid = false
  }
  
  if (!flightForm.originAirport || flightForm.originAirport.trim() === '') {
    formErrors.originAirport = '请输入出发机场'
    isValid = false
  }
  
  if (!flightForm.destAirport || flightForm.destAirport.trim() === '') {
    formErrors.destAirport = '请输入目的地机场'
    isValid = false
  }
  
  if (!flightForm.schedDepTime) {
    formErrors.schedDepTime = '请选择计划出发时间'
    isValid = false
  }
  
  if (!flightForm.schedArrTime) {
    formErrors.schedArrTime = '请选择计划到达时间'
    isValid = false
  }
  
  // 验证时间逻辑
  if (flightForm.schedDepTime && flightForm.schedArrTime) {
    const depTime = new Date(flightForm.schedDepTime)
    const arrTime = new Date(flightForm.schedArrTime)
    if (arrTime <= depTime) {
      formErrors.schedArrTime = '到达时间必须晚于出发时间'
      isValid = false
    }
  }
  
  return isValid
}

// 保存航班
const handleSaveFlight = async () => {
  if (!validateForm()) {
    return
  }
  
  flightFormDialog.loading = true
  
  try {
    // 构建请求数据（注意：不要将 null/undefined 的 aircraftTypeId 发送到后端，部分后端对 null 有非空约束）
    const flightData: any = {
      flightNo: flightForm.flightNo.trim(),
      originAirport: flightForm.originAirport.trim(),
      destAirport: flightForm.destAirport.trim(),
      schedDepTime: flightForm.schedDepTime,
      schedArrTime: flightForm.schedArrTime,
      status: flightForm.status
    }
    if (flightForm.quantity !== null && flightForm.quantity !== undefined) {
      flightData.quantity = Number(flightForm.quantity)
    }
    if (flightForm.price !== null && flightForm.price !== undefined) {
      flightData.price = Number(flightForm.price)
    }
    // 机型字段处理：仅在有实际值时才传 aircraftTypeId；
    // 若用户从回退的 model 文本中选择（model__开头），发送 aircraftModel 给后端以便后端匹配
    if (typeof flightForm.aircraftTypeId === 'string' && flightForm.aircraftTypeId.startsWith('model__')) {
      const model = flightForm.aircraftTypeId.replace('model__', '')
      flightData.aircraftModel = model
    } else if (flightForm.aircraftTypeId !== null && flightForm.aircraftTypeId !== undefined && flightForm.aircraftTypeId !== '') {
      // 只有在非空值时才包含 aircraftTypeId
      flightData.aircraftTypeId = flightForm.aircraftTypeId
    }
    
    // 构建路线信息JSON
    const routeInfoJson = buildRouteInfo()
    if (routeInfoJson) {
      flightData.routeInfo = routeInfoJson
    }
    
    let result
    if (flightFormDialog.mode === 'add') {
      result = await flightManagementApi.createFlight(flightData)
    } else {
      result = await flightManagementApi.updateFlight(flightFormDialog.flightId!, flightData)
    }
    
    if (result && result.success !== false) {
      // 先关闭表单弹窗，等待 DOM 更新并刷新列表，再显示成功提示，避免弹窗覆盖
      const successTitle = flightFormDialog.mode === 'add' ? '新增成功' : '更新成功'
      const successMessage = `航班 ${flightForm.flightNo} ${flightFormDialog.mode === 'add' ? '已成功创建' : '已成功更新'}`
      closeFlightFormDialog()
      await nextTick()
      // 小延迟确保模态已从 DOM 隐藏（避免覆盖）
      await new Promise(resolve => setTimeout(resolve, 120))
      await handleSearch()
      successDialog.title = successTitle
      successDialog.message = successMessage
      successDialog.visible = true
    } else {
      throw new Error(result?.message || '保存失败')
    }
  } catch (error: any) {
    console.error('保存航班失败:', error)
    errorDialog.title = '保存失败'
    errorDialog.message = error.message || '保存航班失败，请稍后重试'
    errorDialog.visible = true
  } finally {
    flightFormDialog.loading = false
  }
}

// 删除单个
const handleDeleteItem = async (flight: Flight) => {
  confirmDialog.title = '确认删除'
  confirmDialog.message = `确定要删除航班 ${flight.flightNumber} 吗？\n\n此操作不可撤销！`
  confirmDialog.onConfirm = async () => {
    confirmDialog.visible = false
    try {
      const result = await flightManagementApi.deleteFlight(flight.id)
      if (result && result.success !== false) {
        successDialog.title = '删除成功'
        successDialog.message = `航班 ${flight.flightNumber} 已成功删除`
        successDialog.visible = true
        await handleSearch()
      } else {
        throw new Error(result?.message || '删除失败')
      }
    } catch (error: any) {
      console.error('删除航班失败:', error)
      errorDialog.title = '删除失败'
      const rawMsg = (error?.message || '').toString()
      if (/foreign key|外键|关联机票|Cannot delete or update a parent row/i.test(rawMsg)) {
        errorDialog.message = '存在关联机票，无法删除航班，请先删除相关机票或取消关联'
      } else {
        errorDialog.message = error.message || '删除失败，请稍后重试'
      }
      errorDialog.visible = true
    }
  }
  confirmDialog.visible = true
}

// 分页
const goToPage = async (page: number) => {
  if (page >= 1 && page <= totalPages.value) {
    currentPage.value = page
    jumpPage.value = page
    // 调用 search 但不重置页码（避免 handleSearch 将 currentPage 重置为1）
    await handleSearch(false) // 重新加载数据
  }
}

// 图片加载错误处理
const handleImageError = (e: Event) => {
  const img = e.target as HTMLImageElement
  img.src = 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iNDAiIGhlaWdodD0iNDAiIHZpZXdCb3g9IjAgMCAyNCAyNCIgZmlsbD0ibm9uZSIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj4KPHBhdGggZD0iTTEyIDJMMTMuMDkgOC4yNkwyMCA5TDEzLjA5IDE1Ljc0TDEyIDIyTDEwLjkxIDE1Ljc0TDQgOUwxMC45MSA4LjI2TDEyIDJaIiBmaWxsPSIjMjE5NkYzIi8+Cjwvc3ZnPg=='
}

// 加载航班数据
const loadFlights = async () => {
  try {
    const result = await flightManagementApi.getFlightList({
      page: currentPage.value - 1, // 后端使用0-based分页
      size: pageSize.value
    })
    
    const parsed = normalizeListAndTotal(result)
    flights.value = parsed.list || []
    total.value = parsed.total || 0
  } catch (error) {
    console.error('加载航班数据失败:', error)
    flights.value = []
    total.value = 0
  }
}

// 分页尺寸改变处理（模板里用到）
const handlePageSizeChange = async () => {
  currentPage.value = 1
  await loadFlights()
}

onMounted(() => {
  loadFlights()
  loadAircraftTypes()
})
</script>

<style scoped>
/* 基础样式内联 */
.flight-page {
  --flight-surface: #ffffff;
  --flight-surface-muted: #f8fafc;
  --flight-border: rgba(15, 23, 42, 0.06);
  --flight-border-strong: rgba(15, 23, 42, 0.12);
  --flight-text-strong: rgba(137, 169, 244, 0.95);
  --flight-text-muted: rgba(116, 151, 231, 0.6);
  --flight-warning: #fbbf24;
  --flight-success: #10b981;
  --flight-danger: #ef4444;
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
  padding: 0;
  position: relative;
}

/* 页面背景装饰 */
.flight-page::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: 
    radial-gradient(circle at 20% 30%, rgba(30, 138, 230, 0.08), transparent 50%),
    radial-gradient(circle at 80% 70%, rgba(99, 102, 241, 0.06), transparent 50%);
  pointer-events: none;
  z-index: 0;
}

.flight-page > * {
  position: relative;
  z-index: 1;
}

.breadcrumb {
  margin-bottom: 24px;
  font-size: 14px;
  color: var(--flight-text-muted);
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 0;
}

.breadcrumb span {
  transition: color 0.3s;
}

.breadcrumb span:first-child {
  color: var(--flight-text-strong);
  font-weight: 500;
  cursor: pointer;
}

.breadcrumb span:first-child:hover {
  color: #1E8AE6;
}

.breadcrumb-separator {
  color: rgba(148, 163, 184, 0.5);
  font-weight: 300;
}

.search-section {
  background: linear-gradient(135deg, rgba(176, 191, 228, 0.9), rgba(30, 41, 59, 0.8));
  padding: 24px;
  border-radius: 20px;
  margin-bottom: 24px;
  border: 1px solid var(--flight-border);
  backdrop-filter: blur(20px);
  box-shadow: 
    0 8px 32px rgba(144, 174, 239, 0.3),
    inset 0 1px 0 rgba(255, 255, 255, 0.1);
  transition: all 0.3s ease;
}

.search-section:hover {
  border-color: rgba(30, 138, 230, 0.3);
  box-shadow: 
    0 12px 40px rgba(160, 203, 237, 0.35),
    inset 0 1px 0 rgba(255, 255, 255, 0.15);
}

.search-row {
  display: flex;
  align-items: flex-end;
  gap: 15px;
  flex-wrap: wrap;
}

.search-item {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.search-item label {
  font-size: 13px;
  color: var(--flight-text-muted);
}

.search-item input,
.search-item select {
  padding: 10px 14px;
  border: 1px solid var(--flight-border-strong);
  border-radius: 10px;
  font-size: 14px;
  background: rgba(220, 225, 232, 0.5);
  color: var(--flight-text-strong);
  transition: all 0.3s ease;
}

.search-item input:focus,
.search-item select:focus {
  outline: none;
  border-color: #1E8AE6;
  background: rgba(179, 204, 243, 0.7);
  box-shadow: 0 0 0 3px rgba(30, 138, 230, 0.15);
}

.search-item input:hover,
.search-item select:hover {
  border-color: rgba(148, 163, 184, 0.4);
}

.search-btn {
  padding: 10px 20px;
  border: none;
  border-radius: 10px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 600;
  transition: all 0.3s ease;
  background: linear-gradient(135deg, #1E8AE6 0%, #91b1df 100%);
  color: #ffffff;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  box-shadow: 0 4px 12px rgba(30, 138, 230, 0.3);
  min-width: 100px;
  justify-content: center;
  position: relative;
  overflow: hidden;
}

.search-btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
  transition: left 0.5s;
}

.search-btn:hover::before {
  left: 100%;
}

.search-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(30, 138, 230, 0.4);
}

.search-btn:active {
  transform: translateY(0);
}

.search-icon {
  font-size: 16px;
  font-weight: bold;
}

.action-buttons {
  margin-bottom: 20px;
  display: flex;
  gap: 12px;
  align-items: center;
}

.btn-primary,
.btn-secondary {
  padding: 10px 20px;
  border: none;
  border-radius: 10px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 600;
  transition: all 0.3s ease;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  background: #fff;
  color: var(--flight-text-strong);
}

.btn-primary {
  background: linear-gradient(135deg, #1E8AE6 0%, #a4b4ca 100%);
  color: #ffffff;
  position: relative;
  overflow: hidden;
}

.btn-primary::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
  transition: left 0.5s;
}

.btn-primary:hover::before {
  left: 100%;
}

.btn-primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(30, 138, 230, 0.4);
}

.btn-primary:active {
  transform: translateY(0);
}

.btn-secondary {
  background: var(--flight-surface-muted);
  color: var(--flight-text-strong);
  border: 1px solid var(--flight-border);
}

.table-container {
  background: linear-gradient(135deg, rgba(225, 228, 236, 0.95), rgba(231, 236, 244, 0.9));
  border-radius: 20px;
  overflow: hidden;
  border: 1px solid var(--flight-border);
  backdrop-filter: blur(20px);
  overflow-x: auto;
  box-shadow: 
    0 8px 32px rgba(116, 174, 233, 0.3),
    inset 0 1px 0 rgba(255, 255, 255, 0.1);
}

.data-table {
  width: 100%;
  border-collapse: collapse;
}

.data-table th,
.data-table td {
  padding: 14px 12px;
  text-align: left;
  border-bottom: 1px solid rgba(148, 163, 184, 0.1);
  color: var(--flight-text-strong);
  transition: all 0.2s ease;
}

.data-table thead {
  background: linear-gradient(135deg, rgba(30, 138, 230, 0.15), rgba(99, 102, 241, 0.1));
  position: sticky;
  top: 0;
  z-index: 10;
}

.data-table th {
  font-weight: 600;
  color: rgba(226, 232, 240, 0.9);
  font-size: 13px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  padding: 16px 12px;
}

.data-table tbody tr {
  transition: all 0.2s ease;
}

.data-table tbody tr:hover {
  background: linear-gradient(90deg, rgba(30, 138, 230, 0.08), rgba(99, 102, 241, 0.05));
  transform: scale(1.001);
  box-shadow: 0 2px 8px rgba(251, 248, 248, 0.1);
}

.data-table tbody tr:last-child td {
  border-bottom: none;
}

.status-tag {
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
  display: inline-block;
}

.status-tag.pending {
  background: rgba(245, 185, 66, 0.18);
  color: var(--flight-warning);
  border: 1px solid rgba(245, 185, 66, 0.35);
}

.status-tag.approved {
  background: rgba(31, 209, 161, 0.2);
  color: var(--flight-success);
  border: 1px solid rgba(31, 209, 161, 0.35);
}

.status-tag.rejected {
  background: rgba(255, 107, 107, 0.2);
  color: var(--flight-danger);
  border: 1px solid rgba(255, 107, 107, 0.35);
}

.action-btn {
  padding: 6px 12px;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 12px;
  margin-right: 6px;
  transition: all 0.3s ease;
  font-weight: 500;
  display: inline-flex;
  align-items: center;
  gap: 4px;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
}

.action-icon {
  font-size: 14px;
}

.detail-btn {
  background: rgba(59, 130, 246, 0.15);
  color: #60a5fa;
  border: 1px solid rgba(59, 130, 246, 0.3);
}

.detail-btn:hover {
  background: rgba(59, 130, 246, 0.25);
  transform: translateY(-1px);
  box-shadow: 0 4px 10px rgba(59, 130, 246, 0.3);
}

.edit-btn {
  background: rgba(16, 185, 129, 0.15);
  color: #10b981;
  border: 1px solid rgba(16, 185, 129, 0.3);
}

.edit-btn:hover {
  background: rgba(16, 185, 129, 0.25);
  transform: translateY(-1px);
  box-shadow: 0 4px 10px rgba(16, 185, 129, 0.3);
}

.delete-btn {
  background: rgba(239, 68, 68, 0.15);
  color: #ef4444;
  border: 1px solid rgba(239, 68, 68, 0.3);
}

.delete-btn:hover {
  background: rgba(239, 68, 68, 0.25);
  transform: translateY(-1px);
  box-shadow: 0 4px 10px rgba(239, 68, 68, 0.3);
}

.approve-btn {
  background: rgba(31, 209, 161, 0.15);
  color: var(--flight-success);
  border: 1px solid rgba(31, 209, 161, 0.3);
}

.reject-btn {
  background: rgba(255, 107, 107, 0.15);
  color: var(--flight-danger);
  border: 1px solid rgba(255, 107, 107, 0.3);
}

.pagination {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  background: linear-gradient(135deg, rgba(215, 218, 224, 0.6), rgba(185, 190, 199, 0.5));
  border-radius: 12px;
  border: 1px solid var(--flight-border);
  backdrop-filter: blur(10px);
}

.pagination-info {
  color: var(--flight-text-muted);
  font-size: 14px;
  font-weight: 500;
}

.pagination-controls {
  display: flex;
  gap: 8px;
  align-items: center;
}

.page-btn {
  padding: 8px 14px;
  border: 1px solid var(--flight-border);
  background: rgba(205, 213, 225, 0.6);
  border-radius: 8px;
  cursor: pointer;
  color: var(--flight-text-strong);
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s ease;
  min-width: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.page-btn:hover:not(:disabled) {
  background: rgba(30, 138, 230, 0.2);
  border-color: rgba(30, 138, 230, 0.4);
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(30, 138, 230, 0.2);
}

.page-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
  background: rgba(210, 213, 217, 0.3);
}

.page-number {
  padding: 8px 14px;
  color: var(--flight-text-strong);
  font-weight: 600;
  font-size: 14px;
  min-width: 36px;
  text-align: center;
}

.page-jump {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-left: 12px;
  padding-left: 12px;
  border-left: 1px solid var(--flight-border);
  font-size: 14px;
  color: var(--flight-text-muted);
}

.page-jump input {
  width: 50px;
  padding: 6px 8px;
  border: 1px solid var(--flight-border);
  background: rgba(219, 223, 231, 0.6);
  border-radius: 6px;
  color: var(--flight-text-strong);
  text-align: center;
  font-size: 14px;
}

.page-jump input:focus {
  outline: none;
  border-color: #1E8AE6;
  box-shadow: 0 0 0 2px rgba(30, 138, 230, 0.2);
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(255, 255, 255, 0.9);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 10000;
  padding: 20px;
  box-sizing: border-box;
  overflow-y: auto;
  overscroll-behavior: contain;
  backdrop-filter: blur(4px);
}

.modal-content {
  background: var(--flight-surface);
  border-radius: 16px;
  width: 90%;
  max-width: 500px;
  max-height: 80vh;
  overflow-y: auto;
  border: 1px solid var(--flight-border);
}

.modal-header {
  padding: 20px;
  border-bottom: 1px solid var(--flight-border);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.modal-header h3 {
  margin: 0;
  color: var(--flight-text-strong);
}

.close-btn {
  background: none;
  border: none;
  font-size: 24px;
  color: var(--flight-text-muted);
  cursor: pointer;
}

.modal-body {
  padding: 20px;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  color: var(--flight-text-muted);
  margin-bottom: 8px;
}

.form-group input,
.form-group select,
.form-group textarea {
  width: 100%;
  padding: 10px;
  border: 1px solid var(--flight-border-strong);
  border-radius: 8px;
  background: var(--flight-surface-muted);
  color: var(--flight-text-strong);
}

.modal-actions {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
}

/* 特定样式 */
.flight-image {
  width: 40px;
  height: 40px;
  object-fit: cover;
  border-radius: 4px;
}

/* 详情对话框样式 */
.detail-modal,
.flight-form-modal {
  background: var(--flight-surface);
  border-radius: 16px;
  width: 90%;
  max-width: 600px;
  max-height: 90vh;
  overflow-y: auto;
  border: 1px solid var(--flight-border);
  backdrop-filter: blur(18px);
  box-shadow: 0 25px 50px rgba(235, 237, 244, 0.5);
}

.detail-header,
.form-header {
  padding: 20px 24px;
  border-bottom: 1px solid var(--flight-border);
  display: flex;
  justify-content: space-between;
  align-items: center;
  position: sticky;
  top: 0;
  background: var(--flight-surface);
  z-index: 10;
}

.detail-header h3,
.form-header h3 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  background: linear-gradient(135deg, #98b1d4, #1E8AE6);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.close-btn {
  background: none;
  border: none;
  font-size: 28px;
  color: var(--flight-text-muted);
  cursor: pointer;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  transition: all 0.3s;
}

.close-btn:hover {
  background: rgba(255, 255, 255, 0.1);
  color: var(--flight-text-strong);
}

.detail-content {
  background: #fff;
  padding: 24px;
}

.detail-section {
  margin-bottom: 24px;
  padding-bottom: 24px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}

.detail-section:last-child {
  border-bottom: none;
  margin-bottom: 0;
  padding-bottom: 0;
}

.detail-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding: 12px;
  background: rgba(255, 255, 255, 0.03);
  border-radius: 8px;
  border: 1px solid rgba(255, 255, 255, 0.05);
}

.detail-item:last-child {
  margin-bottom: 0;
}

.detail-label {
  font-size: 14px;
  color: var(--flight-text-muted);
  font-weight: 500;
}

.detail-value {
  font-size: 15px;
  color: var(--flight-text-strong);
  font-weight: 600;
  text-align: right;
}

.detail-value.highlight {
  color: #1E8AE6;
  font-size: 16px;
}

.detail-value.price {
  color: #4ade80;
  font-size: 18px;
  font-weight: 700;
}

.detail-actions,
.form-actions {
  padding: 20px 24px;
  border-top: 1px solid var(--flight-border);
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  background: #fff;
}

/* 表单样式 */
.form-content {
  padding: 24px;
  background: #fff;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  margin-bottom: 20px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-group label {
  font-size: 14px;
  color: var(--flight-text-muted);
  font-weight: 500;
}

.form-group .required {
  color: #f87171;
}

.form-group input,
.form-group select,
.form-group textarea {
  padding: 10px 12px;
  border: 1px solid var(--flight-border-strong);
  border-radius: 8px;
  font-size: 14px;
  background: var(--flight-surface-muted);
  color: var(--flight-text-strong);
  transition: all 0.3s;
  font-family: inherit;
  resize: vertical;
}

.form-group textarea {
  min-height: 60px;
}

.form-group textarea.json-input {
  font-family: 'Courier New', monospace;
  font-size: 13px;
}

.form-group input:focus,
.form-group select:focus,
.form-group textarea:focus {
  outline: none;
  border-color: #1E8AE6;
  box-shadow: 0 0 0 3px rgba(30, 138, 230, 0.1);
}

.form-group input.error,
.form-group select.error {
  border-color: #f87171;
}

.error-message {
  font-size: 12px;
  color: #f87171;
  margin-top: -4px;
}

.form-hint {
  font-size: 12px;
  color: var(--flight-text-muted);
  margin-top: 4px;
  display: block;
}

.btn-secondary {
  padding: 10px 20px;
  border: 1px solid var(--flight-border);
  background: var(--flight-surface-muted);
  color: var(--flight-text-strong);
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s;
}

.btn-secondary:hover:not(:disabled) {
  background: rgba(255, 255, 255, 0.1);
  transform: translateY(-1px);
}

.btn-secondary:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* 路线信息区域 */
.route-info-section {
  margin-top: 20px;
  border: 1px solid var(--flight-border);
  border-radius: 12px;
  background: linear-gradient(135deg, rgba(56, 104, 217, 0.4), rgba(110, 148, 209, 0.3));
  backdrop-filter: blur(10px);
  box-shadow: 0 4px 16px rgba(248, 242, 242, 0.2);
  transition: all 0.3s ease;
}

.route-info-section:hover {
  border-color: rgba(30, 138, 230, 0.3);
}

.section-header {
  padding: 18px 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  cursor: pointer;
  user-select: none;
  transition: all 0.3s ease;
  border-radius: 12px 12px 0 0;
}

.section-header:hover {
  background: linear-gradient(90deg, rgba(30, 138, 230, 0.1), rgba(99, 102, 241, 0.05));
}

.section-header label {
  font-size: 14px;
  font-weight: 600;
  color: var(--flight-text-strong);
  margin: 0;
}

.toggle-icon {
  font-size: 12px;
  color: var(--flight-text-muted);
  transition: transform 0.3s;
}

.toggle-icon.expanded {
  transform: rotate(180deg);
}

.route-info-content {
  padding: 24px;
  border-top: 1px solid var(--flight-border);
  background: rgba(15, 23, 42, 0.2);
  animation: slideDown 0.3s ease;
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

.form-group.full-width {
  grid-column: 1 / -1;
}

.advanced-label {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.advanced-label small {
  font-size: 12px;
  color: var(--flight-text-muted);
  font-weight: normal;
}

/* 响应式 */
@media (max-width: 768px) {
  .detail-modal,
  .flight-form-modal {
    width: 95%;
    max-width: none;
  }
  
  .form-row {
    grid-template-columns: 1fr;
  }
  
  .detail-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
  
  .detail-value {
    text-align: left;
  }
  
  .route-info-content {
    padding: 16px;
  }
}
</style>

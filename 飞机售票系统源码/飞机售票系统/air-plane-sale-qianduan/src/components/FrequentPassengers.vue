<template>
  <div class="frequent-passengers">
    <!-- 标签页切换 -->
    <div v-if="tabs.length > 1" class="tabs-header">
      <button
        v-for="tab in tabs"
        :key="tab.id"
        :class="['tab-btn', { active: activeTab === tab.id }]"
        @click="activeTab = tab.id"
      >
        <span class="tab-icon">{{ tab.icon }}</span>
        <span>{{ tab.label }}</span>
      </button>
    </div>

    <!-- 我自己 Tab -->
    <div v-if="activeTab === 'myself'" class="tab-content">
      <div class="myself-section">
        <div class="section-header">
          <h4>我的信息</h4>
          <button class="edit-btn" @click="editMyself" v-if="!editingMyself">
            编辑
          </button>
          <button class="save-btn" @click="saveMyself" v-else>
            保存
          </button>
        </div>

        <div v-if="!editingMyself" class="myself-display">
          <div v-if="myselfInfo.name" class="info-item">
            <span class="label">姓名:</span>
            <span class="value">{{ myselfInfo.name }}</span>
          </div>
          <div v-if="myselfInfo.idCard" class="info-item">
            <span class="label">身份证号:</span>
            <span class="value">{{ maskIdCard(myselfInfo.idCard) }}</span>
          </div>
          <div v-if="myselfInfo.phone" class="info-item">
            <span class="label">电话:</span>
            <span class="value">{{ myselfInfo.phone }}</span>
          </div>
          <div v-if="!myselfInfo.name" class="empty-state">
            <span class="empty-icon">👤</span>
            <p>还未设置个人信息</p>
          </div>
        </div>

        <div v-else class="myself-form">
          <div class="form-group">
            <label>姓名 <span class="required">*</span></label>
            <input
              v-model="myselfForm.name"
              type="text"
              placeholder="请输入您的姓名"
            />
          </div>
          <div class="form-group">
            <label>身份证号 <span class="required">*</span></label>
            <input
              v-model="myselfForm.idCard"
              type="text"
              placeholder="请输入18位身份证号"
              maxlength="18"
            />
          </div>
          <div class="form-group">
            <label>电话</label>
            <input
              v-model="myselfForm.phone"
              type="tel"
              placeholder="请输入联系电话"
            />
          </div>
          <div class="form-actions">
            <button class="cancel-btn" @click="cancelEditMyself">取消</button>
            <button class="save-btn" @click="saveMyself">保存</button>
          </div>
        </div>
      </div>
    </div>

    <!-- 常用乘客 Tab -->
    <div v-if="activeTab === 'frequent'" class="tab-content">
      <div class="frequent-section">
        <div class="section-header">
          <h4>常用乘客</h4>
          <button class="add-btn" @click="showAddFrequentModal = true">
            + 添加常用乘客
          </button>
        </div>

        <div v-if="frequentPassengers.length > 0" class="passengers-grid">
          <div
            v-for="passenger in frequentPassengers"
            :key="passenger.id"
            class="passenger-card"
            @click="selectPassenger(passenger)"
          >
            <div class="card-header">
              <span class="passenger-name">{{ passenger.name }}</span>
              <div class="card-actions">
                <button
                  class="action-btn edit-btn"
                  @click.stop="editFrequentPassenger(passenger)"
                  title="编辑"
                >
                  ✎
                </button>
                <button
                  class="action-btn delete-btn"
                  @click.stop="deleteFrequentPassenger(passenger.id)"
                  title="删除"
                >
                  ✕
                </button>
              </div>
            </div>
            <div class="card-body">
              <div class="info-item">
                <span class="label">身份证:</span>
                <span class="value">{{ maskIdCard(passenger.idCard) }}</span>
              </div>
              <div v-if="passenger.relationship" class="info-item">
                <span class="label">关系:</span>
                <span class="value">{{ passenger.relationship }}</span>
              </div>
              <div v-if="passenger.phone" class="info-item">
                <span class="label">电话:</span>
                <span class="value">{{ passenger.phone }}</span>
              </div>
            </div>
            <div class="card-footer">
              <button class="select-btn" @click.stop="selectPassenger(passenger)">
                选择
              </button>
            </div>
          </div>
        </div>

        <div v-else class="empty-state">
          <span class="empty-icon">👥</span>
          <p>还没有添加常用乘客</p>
          <button class="add-btn" @click="showAddFrequentModal = true">
            立即添加
          </button>
        </div>
      </div>
    </div>

    <!-- 添加常用乘客模态框 -->
    <Teleport to="body">
      <div
        v-if="showAddFrequentModal"
        class="modal-overlay"
        @click.self="closeAddFrequentModal"
      >
        <div class="modal-content">
          <div class="modal-header">
            <h3>{{ editingFrequentPassenger ? '编辑常用乘客' : '添加常用乘客' }}</h3>
            <button class="close-btn" @click="closeAddFrequentModal">×</button>
          </div>

          <div class="modal-body">
            <div class="form-group">
              <label>姓名 <span class="required">*</span></label>
              <input
                v-model="frequentForm.name"
                type="text"
                placeholder="请输入乘客姓名"
              />
            </div>

            <div class="form-group">
              <label>身份证号 <span class="required">*</span></label>
              <input
                v-model="frequentForm.idCard"
                type="text"
                placeholder="请输入18位身份证号"
                maxlength="18"
              />
            </div>

            <div class="form-group">
              <label>与您的关系</label>
              <select v-model="frequentForm.relationship">
                <option value="">请选择</option>
                <option value="配偶">配偶</option>
                <option value="子女">子女</option>
                <option value="父母">父母</option>
                <option value="兄弟姐妹">兄弟姐妹</option>
                <option value="朋友">朋友</option>
                <option value="同事">同事</option>
                <option value="其他">其他</option>
              </select>
            </div>

            <div class="form-group">
              <label>电话</label>
              <input
                v-model="frequentForm.phone"
                type="tel"
                placeholder="请输入联系电话"
              />
            </div>

            <div class="form-group">
              <label>备注</label>
              <textarea
                v-model="frequentForm.remarks"
                placeholder="添加备注信息（可选）"
                rows="3"
              ></textarea>
            </div>
          </div>

          <div class="modal-footer">
            <button class="cancel-btn" @click="closeAddFrequentModal">取消</button>
            <button class="save-btn" @click="saveFrequentPassenger">
              {{ editingFrequentPassenger ? '更新' : '添加' }}
            </button>
          </div>
        </div>
      </div>
    </Teleport>

    <!-- 选择提示 -->
    <div v-if="selectedPassenger" class="selection-info">
      <div class="info-content">
        <span class="info-icon">✓</span>
        <span class="info-text">已选择: <strong>{{ selectedPassenger.name }}</strong></span>
        <button class="clear-btn" @click="clearSelection">清除</button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { frequentPassengerApi, apiUtils } from '../services/api'

interface Passenger {
  id?: string | number
  name: string
  idCard: string
  relationship?: string
  phone?: string
  remarks?: string
  isDefault?: boolean
}

const props = withDefaults(
  defineProps<{
    modelValue?: Passenger | null
    hideMyselfTab?: boolean
  }>(),
  {
    modelValue: null,
    hideMyselfTab: false
  }
)

const emit = defineEmits<{
  'update:modelValue': [value: Passenger | null]
  'select': [value: Passenger]
}>()

// 标签页
const tabs = computed(() => {
  const allTabs = [
    { id: 'myself', label: '我自己', icon: '👤' },
    { id: 'frequent', label: '常用乘客', icon: '👥' }
  ]
  // 如果隐藏"我自己"标签页，只返回常用乘客标签页
  if (props.hideMyselfTab) {
    return allTabs.filter(tab => tab.id !== 'myself')
  }
  return allTabs
})

const activeTab = ref(props.hideMyselfTab ? 'frequent' : 'myself')

// 监听 hideMyselfTab 变化，如果隐藏"我自己"标签页，切换到"常用乘客"
watch(() => props.hideMyselfTab, (hide) => {
  if (hide && activeTab.value === 'myself') {
    activeTab.value = 'frequent'
  }
}, { immediate: true })

// 我自己的信息
const myselfInfo = reactive<Passenger>({
  name: '',
  idCard: '',
  phone: ''
})

const myselfForm = reactive<Passenger>({
  name: '',
  idCard: '',
  phone: ''
})

const editingMyself = ref(false)

// 常用乘客
const frequentPassengers = ref<Passenger[]>([])
const frequentForm = reactive<Passenger>({
  name: '',
  idCard: '',
  relationship: '',
  phone: '',
  remarks: ''
})

const editingFrequentPassenger = ref<Passenger | null>(null)
const showAddFrequentModal = ref(false)
const selectedPassenger = ref<Passenger | null>(props.modelValue)
const loading = ref(false)

// 获取当前用户ID
const getCurrentUserId = (): number | null => {
  const user = apiUtils.getCurrentUser()
  return user?.id ? parseInt(user.id) : null
}

// 掩码身份证号
const maskIdCard = (idCard: string): string => {
  if (!idCard || idCard.length < 6) return idCard
  return idCard.substring(0, 3) + '****' + idCard.substring(idCard.length - 3)
}

// 编辑我自己
const editMyself = () => {
  myselfForm.name = myselfInfo.name
  myselfForm.idCard = myselfInfo.idCard
  myselfForm.phone = myselfInfo.phone
  editingMyself.value = true
}

// 保存我自己的信息
const saveMyself = () => {
  if (!myselfForm.name || !myselfForm.idCard) {
    alert('请填写姓名和身份证号')
    return
  }

  if (!/^\d{17}[\dXx]$/.test(myselfForm.idCard)) {
    alert('身份证号格式不正确')
    return
  }

  myselfInfo.name = myselfForm.name
  myselfInfo.idCard = myselfForm.idCard
  myselfInfo.phone = myselfForm.phone

  // 保存到本地存储（个人信息仍然使用localStorage，因为后端没有对应的接口）
  const MYSELF_STORAGE_KEY = 'flight_booking_myself_info'
  localStorage.setItem(MYSELF_STORAGE_KEY, JSON.stringify(myselfInfo))

  editingMyself.value = false
  alert('个人信息已保存')
}

// 取消编辑
const cancelEditMyself = () => {
  editingMyself.value = false
}

// 选择乘客
const selectPassenger = (passenger: Passenger) => {
  selectedPassenger.value = passenger
  emit('update:modelValue', passenger)
  emit('select', passenger)
}

// 清除选择
const clearSelection = () => {
  selectedPassenger.value = null
  emit('update:modelValue', null)
}

// 编辑常用乘客
const editFrequentPassenger = (passenger: Passenger) => {
  editingFrequentPassenger.value = { ...passenger } // 创建副本避免引用问题
  frequentForm.name = passenger.name
  frequentForm.idCard = passenger.idCard
  frequentForm.relationship = passenger.relationship || ''
  frequentForm.phone = passenger.phone || ''
  frequentForm.remarks = passenger.remarks || ''
  showAddFrequentModal.value = true
}

// 删除常用乘客
const deleteFrequentPassenger = async (id: string | number) => {
  if (!confirm('确定要删除这位常用乘客吗？')) {
    return
  }

  const userId = getCurrentUserId()
  if (!userId) {
    alert('请先登录')
    return
  }

  loading.value = true
  try {
    await frequentPassengerApi.deleteFrequentPassenger(userId, Number(id))
    // 从列表中移除
    frequentPassengers.value = frequentPassengers.value.filter(p => p.id !== id)
    alert('删除成功')
  } catch (error: any) {
    alert(error.message || '删除失败')
  } finally {
    loading.value = false
  }
}

// 保存常用乘客
const saveFrequentPassenger = async () => {
  if (!frequentForm.name || !frequentForm.idCard) {
    alert('请填写姓名和身份证号')
    return
  }

  if (!/^\d{17}[\dXx]$/.test(frequentForm.idCard)) {
    alert('身份证号格式不正确')
    return
  }

  const userId = getCurrentUserId()
  if (!userId) {
    alert('请先登录')
    return
  }

  loading.value = true
  try {
    if (editingFrequentPassenger.value) {
      // 更新现有乘客
      const passengerData = {
        name: frequentForm.name,
        idCard: frequentForm.idCard,
        relationship: frequentForm.relationship || undefined,
        phone: frequentForm.phone || undefined,
        remarks: frequentForm.remarks || undefined
      }
      const updated = await frequentPassengerApi.updateFrequentPassenger(
        userId,
        Number(editingFrequentPassenger.value.id),
        passengerData
      )
      // 更新列表中的乘客
      const index = frequentPassengers.value.findIndex(
        p => p.id === editingFrequentPassenger.value?.id
      )
      if (index > -1) {
        frequentPassengers.value[index] = {
          ...updated,
          id: updated.id
        }
      }
      alert('常用乘客已更新')
    } else {
      // 添加新乘客
      const passengerData = {
        name: frequentForm.name,
        idCard: frequentForm.idCard,
        relationship: frequentForm.relationship || undefined,
        phone: frequentForm.phone || undefined,
        remarks: frequentForm.remarks || undefined,
        isDefault: false
      }
      const newPassenger = await frequentPassengerApi.addFrequentPassenger(userId, passengerData)
      frequentPassengers.value.push({
        ...newPassenger,
        id: newPassenger.id
      })
      alert('常用乘客已添加')
    }
    closeAddFrequentModal()
  } catch (error: any) {
    alert(error.message || '操作失败')
  } finally {
    loading.value = false
  }
}

// 关闭添加常用乘客模态框
const closeAddFrequentModal = () => {
  showAddFrequentModal.value = false
  editingFrequentPassenger.value = null
  frequentForm.name = ''
  frequentForm.idCard = ''
  frequentForm.relationship = ''
  frequentForm.phone = ''
  frequentForm.remarks = ''
}

// 从后端加载常用乘客数据
const loadFrequentPassengers = async () => {
  const userId = getCurrentUserId()
  if (!userId) {
    return
  }

  loading.value = true
  try {
    const passengers = await frequentPassengerApi.getFrequentPassengers(userId)
    frequentPassengers.value = passengers.map((p: any) => ({
      id: p.id,
      name: p.name,
      idCard: p.idCard,
      relationship: p.relationship,
      phone: p.phone,
      remarks: p.remarks,
      isDefault: p.isDefault
    }))
  } catch (error) {
    console.error('加载常用乘客失败:', error)
  } finally {
    loading.value = false
  }
}

// 从本地存储加载个人信息（个人信息仍然使用localStorage）
const loadMyselfInfo = () => {
  const MYSELF_STORAGE_KEY = 'flight_booking_myself_info'
  const myselfData = localStorage.getItem(MYSELF_STORAGE_KEY)
  if (myselfData) {
    try {
      const data = JSON.parse(myselfData)
      myselfInfo.name = data.name
      myselfInfo.idCard = data.idCard
      myselfInfo.phone = data.phone
    } catch (e) {
      console.error('加载个人信息失败:', e)
    }
  }
}

// 初始化
onMounted(() => {
  loadMyselfInfo()
  loadFrequentPassengers()
})

// 暴露方法
defineExpose({
  myselfInfo,
  frequentPassengers,
  selectPassenger,
  clearSelection
})
</script>

<style scoped>
.frequent-passengers {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.tabs-header {
  display: flex;
  gap: 0.5rem;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  padding-bottom: 0;
}

.tab-btn {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.8rem 1.2rem;
  border: none;
  background: transparent;
  color: rgba(248, 250, 252, 0.6);
  font-size: 0.95rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s;
  border-bottom: 2px solid transparent;
  margin-bottom: -1px;
}

.tab-btn:hover {
  color: rgba(248, 250, 252, 0.9);
}

.tab-btn.active {
  color: #60a5fa;
  border-bottom-color: #60a5fa;
}

.tab-icon {
  font-size: 1.1rem;
}

.tab-content {
  padding: 1rem 0;
}

/* 我自己部分 */
.myself-section {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 0.8rem;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}

.section-header h4 {
  margin: 0;
  color: #fff;
  font-size: 1rem;
}

.edit-btn,
.save-btn,
.cancel-btn,
.add-btn,
.delete-btn {
  padding: 0.5rem 1rem;
  border-radius: 8px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  background: transparent;
  color: rgba(248, 250, 252, 0.8);
  font-size: 0.9rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.edit-btn:hover {
  background: rgba(99, 102, 241, 0.15);
  border-color: rgba(99, 102, 241, 0.4);
  color: #60a5fa;
}

.save-btn:hover {
  background: rgba(34, 197, 94, 0.15);
  border-color: rgba(34, 197, 94, 0.4);
  color: #86efac;
}

.cancel-btn:hover {
  background: rgba(248, 113, 113, 0.15);
  border-color: rgba(248, 113, 113, 0.4);
  color: #f87171;
}

.add-btn {
  background: rgba(99, 102, 241, 0.1);
  border-color: rgba(99, 102, 241, 0.3);
  color: #60a5fa;
}

.add-btn:hover {
  background: rgba(99, 102, 241, 0.2);
  border-color: rgba(99, 102, 241, 0.5);
}

.delete-btn {
  padding: 0.3rem 0.6rem;
  font-size: 0.85rem;
}

.delete-btn:hover {
  background: rgba(248, 113, 113, 0.15);
  border-color: rgba(248, 113, 113, 0.4);
  color: #f87171;
}

.myself-display {
  display: flex;
  flex-direction: column;
  gap: 0.8rem;
  padding: 1rem;
  border-radius: 12px;
  background: rgba(15, 23, 42, 0.6);
  border: 1px solid rgba(255, 255, 255, 0.08);
}

.info-item {
  display: flex;
  gap: 1rem;
  align-items: center;
}

.info-item .label {
  color: rgba(248, 250, 252, 0.6);
  min-width: 80px;
  font-weight: 500;
}

.info-item .value {
  color: #fff;
  font-weight: 500;
}

.myself-form {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.form-group label {
  color: rgba(248, 250, 252, 0.8);
  font-size: 0.9rem;
  font-weight: 500;
}

.required {
  color: #f87171;
}

.form-group input,
.form-group select,
.form-group textarea {
  padding: 0.75rem 1rem;
  border-radius: 8px;
  border: 1px solid rgba(255, 255, 255, 0.15);
  background: rgba(8, 14, 35, 0.6);
  color: #fff;
  font-size: 0.95rem;
  outline: none;
  transition: all 0.3s;
}

.form-group input:focus,
.form-group select:focus,
.form-group textarea:focus {
  border-color: rgba(99, 102, 241, 0.5);
  background: rgba(8, 14, 35, 0.8);
  box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.1);
}

.form-group input::placeholder,
.form-group textarea::placeholder {
  color: rgba(248, 250, 252, 0.4);
}

.form-actions {
  display: flex;
  gap: 0.8rem;
  justify-content: flex-end;
  margin-top: 0.5rem;
}

/* 常用乘客部分 */
.frequent-section {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.passengers-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 1rem;
}

.passenger-card {
  padding: 1.2rem;
  border-radius: 12px;
  background: rgba(211, 217, 231, 0.6);
  border: 1px solid rgba(255, 255, 255, 0.1);
  cursor: pointer;
  transition: all 0.3s;
  display: flex;
  flex-direction: column;
  gap: 0.8rem;
}

.passenger-card:hover {
  border-color: rgba(99, 102, 241, 0.3);
  background: rgba(15, 23, 42, 0.8);
  transform: translateY(-2px);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 0.8rem;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}

.passenger-name {
  font-size: 1.1rem;
  font-weight: 600;
  color: #fff;
}

.card-actions {
  display: flex;
  gap: 0.4rem;
}

.action-btn {
  width: 28px;
  height: 28px;
  border-radius: 6px;
  border: 1px solid rgba(255, 255, 255, 0.15);
  background: transparent;
  color: rgba(248, 250, 252, 0.6);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
  font-size: 0.9rem;
}

.action-btn:hover {
  background: rgba(255, 255, 255, 0.1);
  color: #fff;
}

.action-btn.delete-btn:hover {
  background: rgba(248, 113, 113, 0.2);
  border-color: rgba(248, 113, 113, 0.4);
  color: #f87171;
}

.card-body {
  display: flex;
  flex-direction: column;
  gap: 0.6rem;
}

.card-body .info-item {
  font-size: 0.9rem;
}

.card-body .label {
  color: rgba(248, 250, 252, 0.5);
  min-width: 60px;
}

.card-body .value {
  color: rgba(248, 250, 252, 0.9);
}

.card-footer {
  padding-top: 0.8rem;
  border-top: 1px solid rgba(255, 255, 255, 0.08);
}

.select-btn {
  width: 100%;
  padding: 0.6rem;
  border-radius: 8px;
  border: 1px solid rgba(99, 102, 241, 0.3);
  background: rgba(99, 102, 241, 0.1);
  color: #60a5fa;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.select-btn:hover {
  background: rgba(99, 102, 241, 0.2);
  border-color: rgba(99, 102, 241, 0.5);
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 3rem 1rem;
  text-align: center;
  color: rgba(248, 250, 252, 0.5);
}

.empty-icon {
  font-size: 3rem;
  margin-bottom: 1rem;
  opacity: 0.7;
}

.empty-state p {
  margin: 0 0 1rem 0;
  font-size: 0.95rem;
}

/* 模态框 */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.75);
  backdrop-filter: blur(8px);
  z-index: 9999;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 2rem;
}

.modal-content {
  background: rgba(5, 11, 32, 0.95);
  border: 1px solid rgba(148, 163, 184, 0.2);
  border-radius: 16px;
  width: 100%;
  max-width: 500px;
  max-height: 90vh;
  overflow-y: auto;
  box-shadow: 0 25px 60px rgba(0, 0, 0, 0.9);
  backdrop-filter: blur(20px);
}

.modal-header {
  padding: 1.5rem;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.modal-header h3 {
  margin: 0;
  color: #fff;
  font-size: 1.3rem;
}

.close-btn {
  background: transparent;
  border: none;
  color: rgba(255, 255, 255, 0.7);
  font-size: 1.8rem;
  cursor: pointer;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  transition: all 0.2s;
}

.close-btn:hover {
  background: rgba(255, 255, 255, 0.1);
  color: #fff;
}

.modal-body {
  padding: 1.5rem;
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.modal-footer {
  padding: 1.5rem;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  display: flex;
  justify-content: flex-end;
  gap: 0.8rem;
}

.modal-footer button {
  padding: 0.7rem 1.5rem;
  border-radius: 8px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  background: transparent;
  color: rgba(248, 250, 252, 0.8);
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.modal-footer .save-btn {
  background: rgba(34, 197, 94, 0.15);
  border-color: rgba(34, 197, 94, 0.4);
  color: #86efac;
}

.modal-footer .save-btn:hover {
  background: rgba(34, 197, 94, 0.25);
  border-color: rgba(34, 197, 94, 0.6);
}

.modal-footer .cancel-btn:hover {
  background: rgba(255, 255, 255, 0.1);
}

/* 选择提示 */
.selection-info {
  padding: 0.8rem 1rem;
  border-radius: 8px;
  background: rgba(34, 197, 94, 0.15);
  border: 1px solid rgba(34, 197, 94, 0.3);
}

.info-content {
  display: flex;
  align-items: center;
  gap: 0.8rem;
  justify-content: space-between;
}

.info-icon {
  color: #86efac;
  font-weight: 700;
  font-size: 1.1rem;
}

.info-text {
  color: #86efac;
  font-size: 0.9rem;
  flex: 1;
}

.info-text strong {
  color: #fff;
}

.clear-btn {
  padding: 0.3rem 0.8rem;
  border-radius: 6px;
  border: 1px solid rgba(248, 113, 113, 0.3);
  background: rgba(248, 113, 113, 0.1);
  color: #f87171;
  font-size: 0.85rem;
  cursor: pointer;
  transition: all 0.2s;
}

.clear-btn:hover {
  background: rgba(248, 113, 113, 0.2);
  border-color: rgba(248, 113, 113, 0.5);
}

@media (max-width: 768px) {
  .passengers-grid {
    grid-template-columns: 1fr;
  }

  .tabs-header {
    flex-wrap: wrap;
  }

  .tab-btn {
    flex: 1;
    min-width: 150px;
  }

  .modal-content {
    max-width: 95%;
  }
}
</style>


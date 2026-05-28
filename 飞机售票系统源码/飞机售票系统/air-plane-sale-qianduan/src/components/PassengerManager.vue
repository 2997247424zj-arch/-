<template>
  <div class="passenger-manager">
    <!-- 乘客列表 -->
    <div class="passengers-list">
      <div
        v-for="(passenger, index) in passengers"
        :key="index"
        class="passenger-item"
      >
        <div class="passenger-header">
          <span class="passenger-number">乘客 {{ index + 1 }}</span>
          <div class="passenger-actions">
            <button
              v-if="passengers.length > 1"
              class="action-btn delete-btn"
              @click="deletePassenger(index)"
              title="删除乘客"
            >
              ✕
            </button>
          </div>
        </div>

        <div class="passenger-form">
          <div class="form-row">
            <div class="form-group">
              <label>姓名 <span class="required">*</span></label>
              <input
                v-model="passenger.name"
                type="text"
                placeholder="请输入乘客姓名"
                @blur="validatePassenger(index)"
                required
              />
              <span v-if="errors[index]?.name" class="error-message">
                {{ errors[index].name }}
              </span>
            </div>
            <div class="form-group">
              <label>身份证号 <span class="required">*</span></label>
              <input
                v-model="passenger.idCard"
                type="text"
                placeholder="请输入18位身份证号"
                maxlength="18"
                @blur="validatePassenger(index)"
                required
              />
              <span v-if="errors[index]?.idCard" class="error-message">
                {{ errors[index].idCard }}
              </span>
            </div>
          </div>

          <div class="form-row">
            <div class="form-group">
              <label>舱位等级 <span class="required">*</span></label>
              <select
                v-model="passenger.seatClass"
                @change="validatePassenger(index)"
              >
                <option value="economy">经济舱</option>
                <option value="business">商务舱</option>
                <option value="first">头等舱</option>
              </select>
            </div>
            
          </div>

          <div v-if="showAdvanced" class="form-row">
            <div class="form-group">
              <label>乘客类型</label>
              <select v-model="passenger.passengerType">
                <option value="adult">成人</option>
                <option value="child">儿童</option>
                <option value="infant">婴儿</option>
              </select>
            </div>
            <div class="form-group">
              <label>联系电话</label>
              <input
                v-model="passenger.phone"
                type="tel"
                placeholder="可选"
              />
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 添加乘客按钮 -->
    <div class="add-passenger-section">
      <button
        v-if="passengers.length < maxPassengers"
        class="add-btn"
        @click="addPassenger"
      >
        <span class="add-icon">+</span>
        <span>添加乘客</span>
      </button>
      <span v-if="passengers.length >= maxPassengers" class="max-passengers-tip">
        最多可添加 {{ maxPassengers }} 位乘客
      </span>
    </div>

    <!-- 高级选项切换 -->
    <div class="advanced-toggle">
      <button
        class="toggle-btn"
        @click="showAdvanced = !showAdvanced"
      >
        <span class="toggle-icon">{{ showAdvanced ? '▼' : '▶' }}</span>
        <span>{{ showAdvanced ? '隐藏' : '显示' }}高级选项</span>
      </button>
    </div>

    <!-- 验证摘要 -->
    <div v-if="showValidationSummary" class="validation-summary">
      <div class="summary-header">
        <span class="summary-title">乘客信息验证</span>
        <span :class="['summary-status', validationStatus]">
          {{ validationStatusText }}
        </span>
      </div>
      <div v-if="validationErrors.length > 0" class="summary-errors">
        <div v-for="(error, idx) in validationErrors" :key="idx" class="error-item">
          <span class="error-icon">⚠</span>
          <span>{{ error }}</span>
        </div>
      </div>
      <div v-else class="summary-success">
        <span class="success-icon">✓</span>
        <span>所有乘客信息已验证通过</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'

interface Passenger {
  name: string
  idCard: string
  seatClass: string
  seatPreference: string
  passengerType: string
  phone: string
}

interface PassengerErrors {
  [key: number]: {
    name?: string
    idCard?: string
  }
}

const props = withDefaults(
  defineProps<{
    modelValue?: Passenger[]
    maxPassengers?: number
    showValidationSummary?: boolean
  }>(),
  {
    modelValue: () => [
      {
        name: '',
        idCard: '',
        seatClass: 'economy',
        seatPreference: 'any',
        passengerType: 'adult',
        phone: ''
      }
    ],
    maxPassengers: 9,
    showValidationSummary: true
  }
)

const emit = defineEmits<{
  'update:modelValue': [value: Passenger[]]
  'change': [value: Passenger[]]
  'validate': [isValid: boolean]
}>()

const passengers = ref<Passenger[]>(JSON.parse(JSON.stringify(props.modelValue)))
const errors = ref<PassengerErrors>({})
const showAdvanced = ref(false)

// 监听 props 变化
watch(
  () => props.modelValue,
  (newVal) => {
    passengers.value = JSON.parse(JSON.stringify(newVal))
  }
)

// 监听 passengers 变化，同步到父组件
watch(
  passengers,
  (newVal) => {
    emit('update:modelValue', newVal)
    emit('change', newVal)
    validateAll()
  },
  { deep: true }
)

// 添加乘客
const addPassenger = () => {
  if (passengers.value.length < props.maxPassengers) {
    passengers.value.push({
      name: '',
      idCard: '',
      seatClass: 'economy',
      seatPreference: 'any',
      passengerType: 'adult',
      phone: ''
    })
  }
}

// 删除乘客
const deletePassenger = (index: number) => {
  if (passengers.value.length > 1) {
    passengers.value.splice(index, 1)
    // 清除该乘客的错误信息
    if (errors.value[index]) {
      delete errors.value[index]
    }
  }
}

// 验证单个乘客
const validatePassenger = (index: number) => {
  const passenger = passengers.value[index]
  if (!errors.value[index]) {
    errors.value[index] = {}
  }

  // 清除之前的错误
  errors.value[index] = {}

  // 验证姓名
  if (!passenger.name || !passenger.name.trim()) {
    errors.value[index].name = '请输入姓名'
  } else if (passenger.name.length < 2) {
    errors.value[index].name = '姓名至少需要2个字符'
  } else if (passenger.name.length > 50) {
    errors.value[index].name = '姓名不能超过50个字符'
  }

  // 验证身份证号
  if (!passenger.idCard || !passenger.idCard.trim()) {
    errors.value[index].idCard = '请输入身份证号'
  } else if (!/^\d{17}[\dXx]$/.test(passenger.idCard.trim())) {
    errors.value[index].idCard = '身份证号格式不正确（应为18位）'
  }
}

// 验证所有乘客
const validateAll = () => {
  errors.value = {}
  passengers.value.forEach((_, index) => {
    validatePassenger(index)
  })
  emit('validate', isAllValid.value)
}

// 检查是否所有乘客都有效
const isAllValid = computed(() => {
  return passengers.value.every((passenger) => {
    return (
      passenger.name &&
      passenger.name.trim().length >= 2 &&
      passenger.idCard &&
      /^\d{17}[\dXx]$/.test(passenger.idCard.trim())
    )
  })
})

// 验证错误列表
const validationErrors = computed(() => {
  const errorList: string[] = []
  passengers.value.forEach((passenger, index) => {
    if (errors.value[index]) {
      if (errors.value[index].name) {
        errorList.push(`乘客 ${index + 1}: ${errors.value[index].name}`)
      }
      if (errors.value[index].idCard) {
        errorList.push(`乘客 ${index + 1}: ${errors.value[index].idCard}`)
      }
    }
  })
  return errorList
})

// 验证状态
const validationStatus = computed(() => {
  return isAllValid.value ? 'valid' : 'invalid'
})

const validationStatusText = computed(() => {
  return isAllValid.value ? '✓ 验证通过' : '✕ 验证未通过'
})

// 暴露方法给父组件
defineExpose({
  passengers,
  isAllValid,
  validateAll,
  addPassenger,
  deletePassenger
})
</script>

<style scoped>
.passenger-manager {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.passengers-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.passenger-item {
  padding: 1.5rem;
  border-radius: 16px;
  background: rgba(192, 200, 219, 0.6);
  border: 1px solid rgba(255, 255, 255, 0.1);
  transition: all 0.3s ease;
}

.passenger-item:hover {
  border-color: rgba(99, 102, 241, 0.3);
  background: rgba(198, 202, 213, 0.8);
}

.passenger-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
  padding-bottom: 1rem;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}

.passenger-number {
  font-weight: 600;
  color: #fff;
  font-size: 1rem;
}

.passenger-actions {
  display: flex;
  gap: 0.5rem;
}

.action-btn {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  border: 1px solid rgba(255, 255, 255, 0.15);
  background: transparent;
  color: rgba(255, 255, 255, 0.7);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
  font-size: 1.2rem;
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

.passenger-form {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.form-row {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 1rem;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.form-group label {
  font-size: 0.9rem;
  color: rgba(248, 250, 252, 0.75);
  font-weight: 500;
}

.required {
  color: #f87171;
}

.form-group input,
.form-group select {
  padding: 0.75rem 1rem;
  border-radius: 12px;
  border: 1px solid rgba(255, 255, 255, 0.15);
  background: rgba(8, 14, 35, 0.6);
  color: #fff;
  font-size: 0.95rem;
  outline: none;
  transition: all 0.3s ease;
}

.form-group input:focus,
.form-group select:focus {
  border-color: rgba(99, 102, 241, 0.5);
  background: rgba(8, 14, 35, 0.8);
  box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.1);
}

.form-group input::placeholder {
  color: rgba(248, 250, 252, 0.4);
}

.error-message {
  font-size: 0.8rem;
  color: #f87171;
  margin-top: -0.3rem;
}

.add-passenger-section {
  display: flex;
  justify-content: center;
  padding: 1rem 0;
}

.add-btn {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem 1.5rem;
  border-radius: 12px;
  border: 1px dashed rgba(99, 102, 241, 0.4);
  background: rgba(99, 102, 241, 0.08);
  color: #60a5fa;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.add-btn:hover {
  border-color: rgba(99, 102, 241, 0.6);
  background: rgba(99, 102, 241, 0.15);
  color: #a5b4fc;
}

.add-icon {
  font-size: 1.2rem;
  font-weight: 700;
}

.max-passengers-tip {
  color: rgba(248, 250, 252, 0.5);
  font-size: 0.9rem;
}

.advanced-toggle {
  display: flex;
  justify-content: center;
}

.toggle-btn {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.5rem 1rem;
  border: none;
  background: transparent;
  color: rgba(248, 250, 252, 0.7);
  font-size: 0.9rem;
  cursor: pointer;
  transition: all 0.2s;
}

.toggle-btn:hover {
  color: #fff;
}

.toggle-icon {
  display: inline-block;
  transition: transform 0.3s;
}

.validation-summary {
  padding: 1rem;
  border-radius: 12px;
  background: rgba(205, 209, 220, 0.6);
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.summary-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.8rem;
}

.summary-title {
  font-weight: 600;
  color: #fff;
  font-size: 0.95rem;
}

.summary-status {
  padding: 0.25rem 0.75rem;
  border-radius: 8px;
  font-size: 0.85rem;
  font-weight: 600;
}

.summary-status.valid {
  background: rgba(34, 197, 94, 0.2);
  color: #86efac;
  border: 1px solid rgba(34, 197, 94, 0.3);
}

.summary-status.invalid {
  background: rgba(248, 113, 113, 0.2);
  color: #fca5a5;
  border: 1px solid rgba(248, 113, 113, 0.3);
}

.summary-errors {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.error-item {
  display: flex;
  align-items: flex-start;
  gap: 0.5rem;
  font-size: 0.9rem;
  color: #fca5a5;
}

.error-icon {
  flex-shrink: 0;
  font-weight: 700;
}

.summary-success {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.9rem;
  color: #86efac;
}

.success-icon {
  font-weight: 700;
}

@media (max-width: 768px) {
  .passenger-item {
    padding: 1rem;
  }

  .form-row {
    grid-template-columns: 1fr;
  }

  .passenger-header {
    margin-bottom: 0.8rem;
    padding-bottom: 0.8rem;
  }
}
</style>


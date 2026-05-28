<template>
  <div class="glass-card search-panel">
    <div class="panel-header">
      <div>
        <p class="panel-label">智能匹配</p>
        <h2>航班搜索</h2>
      </div>
      <span class="panel-pill">实时库存</span>
    </div>

    <form @submit.prevent="handleSubmit" class="search-form">
      <div class="form-row">
        <div class="form-group">
          <label for="departure">出发城市</label>
          <input
            id="departure"
            v-model="localForm.departure"
            type="text"
            placeholder="例如：北京"
            required
          />
        </div>

        <div class="form-group">
          <label for="destination">到达城市</label>
          <input
            id="destination"
            v-model="localForm.destination"
            type="text"
            placeholder="例如：上海"
            required
          />
        </div>
      </div>

      <div class="form-row">
        <div class="form-group">
          <label for="date">出发日期</label>
          <input id="date" v-model="localForm.date" type="date" required />
        </div>
        <div class="form-group">
          <label for="passengers">乘客人数</label>
          <select id="passengers" v-model="localForm.passengers">
            <option value="1">1 人</option>
            <option value="2">2 人</option>
            <option value="3">3 人</option>
            <option value="4">4 人</option>
          </select>
        </div>
      </div>

      <button type="submit" class="search-btn" :disabled="loading">
        {{ loading ? '搜索中...' : '搜索航班' }}
      </button>
    </form>

    <div class="recommend-block">
      <div class="recommend-header">
        <div>
          <p class="panel-label">智能推荐</p>
          <h3>热门航班</h3>
        </div>
        <button class="ghost-btn" type="button">刷新</button>
      </div>
      <div class="recommend-list">
        <div
          class="recommend-card"
          v-for="flight in recommendedFlights"
          :key="flight.id"
        >
          <div>
            <p class="route">{{ flight.route }}</p>
            <p class="time">{{ flight.time }}</p>
          </div>
          <div class="price">
            <span>¥{{ flight.price }}</span>
            <small>{{ flight.seats }} 座剩余</small>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, watch } from 'vue'

export interface SearchForm {
  departure: string
  destination: string
  date: string
  passengers: string
}

export interface RecommendedFlight {
  id: string
  route: string
  time: string
  price: number
  seats: number
}

const props = defineProps<{
  searchParams: SearchForm
  loading: boolean
  recommendedFlights: RecommendedFlight[]
}>()

const emit = defineEmits<{
  (e: 'search', payload: SearchForm): void
}>()

const localForm = reactive<SearchForm>({ ...props.searchParams })

watch(
  () => props.searchParams,
  value => {
    Object.assign(localForm, value)
  },
  { deep: true }
)

const handleSubmit = () => {
  emit('search', { ...localForm })
}
</script>

<style scoped>
.glass-card {
  padding: 1.75rem;
  border-radius: 28px;
  background: rgba(2, 6, 23, 0.7);
  border: 1px solid rgba(255, 255, 255, 0.06);
  box-shadow:
    0 25px 50px rgba(2, 6, 23, 0.6),
    inset 0 1px rgba(255, 255, 255, 0.08);
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 1rem;
  margin-bottom: 1.5rem;
}

.panel-label {
  font-size: 0.85rem;
  color: rgba(248, 250, 252, 0.6);
  margin-bottom: 0.3rem;
}

.panel-header h2 {
  margin: 0;
  font-size: 1.6rem;
}

.panel-pill {
  padding: 0.4rem 0.9rem;
  border-radius: 999px;
  background: rgba(34, 197, 94, 0.12);
  color: #a7f3d0;
  font-size: 0.85rem;
  border: 1px solid rgba(167, 243, 208, 0.25);
}

.search-form {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  margin-bottom: 1.5rem;
}

.form-row {
  display: flex;
  gap: 1rem;
  flex-wrap: wrap;
}

.form-group {
  flex: 1;
  min-width: 220px;
}

.form-group label {
  display: block;
  margin-bottom: 0.4rem;
  font-size: 0.9rem;
  color: rgba(248, 250, 252, 0.75);
}

.form-group input,
.form-group select {
  width: 100%;
  border: 1px solid rgba(255, 255, 255, 0.12);
  border-radius: 16px;
  padding: 0.85rem 1rem;
  background: rgba(15, 23, 42, 0.7);
  color: #f8fafc;
  font-size: 0.95rem;
  outline: none;
}

.form-group input:focus,
.form-group select:focus {
  border-color: rgba(96, 165, 250, 0.8);
  box-shadow: 0 0 0 1px rgba(96, 165, 250, 0.5);
}

.search-btn {
  width: 100%;
  border: none;
  border-radius: 18px;
  padding: 0.95rem;
  background: linear-gradient(135deg, #87CEEB, #4AA3DF);
  color: #fff;
  font-size: 1.05rem;
  font-weight: 600;
  cursor: pointer;
  box-shadow: 0 20px 40px rgba(14, 165, 233, 0.35);
}

.search-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.recommend-block {
  border-top: 1px solid rgba(255, 255, 255, 0.05);
  padding-top: 1.2rem;
}

.recommend-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}

.recommend-header h3 {
  margin: 0.2rem 0 0;
}

.ghost-btn {
  border-radius: 999px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  background: transparent;
  color: #f8fafc;
  padding: 0.35rem 1rem;
}

.recommend-list {
  display: flex;
  flex-direction: column;
  gap: 0.8rem;
}

.recommend-card {
  border-radius: 16px;
  padding: 0.9rem 1rem;
  background: rgba(6, 12, 34, 0.75);
  border: 1px solid rgba(255, 255, 255, 0.05);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.route {
  margin: 0;
  font-weight: 600;
}

.time {
  margin: 0.2rem 0 0;
  color: rgba(248, 250, 252, 0.7);
}

.price {
  text-align: right;
}

.price span {
  font-size: 1.1rem;
  font-weight: 600;
}

.price small {
  display: block;
  color: rgba(248, 250, 252, 0.6);
}
</style>


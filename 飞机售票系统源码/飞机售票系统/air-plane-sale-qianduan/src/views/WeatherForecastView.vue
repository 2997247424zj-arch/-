<template>
  <div class="weather-forecast-view">
    <header class="hero">
      <div>
        <p class="page-label">出行信息 · 天气联动</p>
        <h1>主要航点天气速览</h1>
        <p>
          根据您的常飞航线，展示未来 3 天的气象趋势与机场运行提示，提前规划行程，避开恶劣天气造成的影响。
        </p>
      </div>
      <button class="primary-btn" @click="handleBack">返回</button>
    </header>

    <section class="city-selector">
      <h2>选择关注城市</h2>
      <div class="search-box">
        <input 
          v-model="searchCity" 
          type="text" 
          placeholder="输入城市名称，如：北京" 
          @keyup.enter="handleSearch"
          :disabled="loading"
        />
        <button class="primary-btn search-btn" @click="handleSearch" :disabled="loading">
          {{ loading ? '查询中...' : '查询' }}
        </button>
      </div>
      <div class="city-list">
        <button
          v-for="city in cityForecasts"
          :key="city.id"
          type="button"
          class="city-pill"
          :class="{ active: city.id === selectedCityId }"
          @click="selectCity(city.id)"
        >
          {{ city.city }} <span>{{ city.airport }}</span>
        </button>
      </div>
      <p v-if="errorMsg" class="error-msg">{{ errorMsg }}</p>
    </section>

    <section class="forecast-grid" v-if="activeCity">
      <article class="forecast-card">
        <header>
          <div>
            <p class="card-label">今日概况</p>
            <h3>{{ activeCity.city }} {{ activeCity.condition }}</h3>
            <p class="aqi-tag" v-if="activeCity.aqi">AQI {{ activeCity.aqi }}</p>
          </div>
          <span class="temperature">{{ activeCity.temperature }}</span>
        </header>
        <ul class="forecast-details">
          <li><strong>风向 / 风速：</strong>{{ activeCity.wind }}</li>
          <li><strong>湿度：</strong>{{ activeCity.humidity }}</li>
          <li><strong>实时天气：</strong>{{ activeCity.condition }}</li>
          <li><strong>运行提示：</strong>{{ activeCity.tips }}</li>
        </ul>
      </article>

      <article class="forecast-card">
        <header>
          <p class="card-label">未来趋势</p>
          <h3>温度与天气</h3>
        </header>
        <div class="trend-list">
          <div v-for="day in activeCity.trend" :key="day.day" class="trend-item">
            <p>{{ day.day }}</p>
            <span>{{ day.temp }}</span>
            <small>{{ day.precip }}</small>
          </div>
        </div>
      </article>

      <article class="forecast-card wide">
        <header>
          <p class="card-label">机场运行建议</p>
          <h3>航班保障提醒</h3>
        </header>
        <div class="advice-grid">
          <div v-for="guide in activeCity.guidance" :key="guide.title" class="advice-card">
            <h4>{{ guide.title }}</h4>
            <p>{{ guide.content }}</p>
          </div>
        </div>
      </article>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { weatherApi } from '../services/api'
import store from '../services/store'

const router = useRouter()

const handleBack = () => {
  // If we came from a specific page, we could use history.back(), 
  // but role-based routing is safer for this system's structure.
  const role = store.userState.role
  if (role === 'admin') {
    router.push('/dashboard')
  } else if (role === 'operator') {
    router.push('/portal/operations')
  } else {
    router.push('/portal/passengers')
  }
}

interface CityForecast {
  id: string
  city: string
  airport: string
  condition: string
  temperature: string
  wind: string
  humidity: string
  aqi?: string
  tips: string
  trend: { day: string; temp: string; precip: string }[]
  guidance: { title: string; content: string }[]
}

const cityForecasts = ref<CityForecast[]>([])
const selectedCityId = ref('')
const searchCity = ref('')
const loading = ref(false)
const errorMsg = ref('')

const activeCity = computed(() =>
  cityForecasts.value.find(city => city.id === selectedCityId.value)
)

const selectCity = (id: string) => {
  selectedCityId.value = id
}

const mapBackendDataToForecast = (data: any, cityName: string): CityForecast => {
  const realtime = data.realtime || {}
  const future = data.future || []
  
  // 映射未来几天的数据
  const trend = future.slice(0, 3).map((item: any, index: number) => {
    const days = ['今天', '明天', '后天']
    return {
      day: days[index] || item.date,
      temp: item.temperature,
      precip: item.weather // 使用天气状况代替降水概率
    }
  })

  // 模拟一些建议数据（因为API不提供）
  const guidance = [
    { title: '地面交通', content: `前往${cityName}机场的道路通畅，建议提前 2 小时抵达。` },
    { title: '行李建议', content: realtime.info && realtime.info.includes('雨') ? '今日有雨，建议为贵重行李做好防水措施。' : '天气良好，适合出行。' },
    { title: '航班运行', content: '目前空域运行正常，暂无大规模延误预警。' }
  ]

  return {
    id: cityName + Date.now(), // 唯一ID
    city: cityName,
    airport: cityName + '机场', // 简单拼接
    condition: realtime.info,
    temperature: realtime.temperature + '°C',
    wind: realtime.direct + ' ' + realtime.power,
    humidity: realtime.humidity + '%',
    aqi: realtime.aqi,
    tips: `当前AQI ${realtime.aqi || 'N/A'}，${realtime.info}。`,
    trend,
    guidance
  }
}

const handleSearch = async () => {
  if (!searchCity.value.trim()) return
  
  loading.value = true
  errorMsg.value = ''
  
  try {
    // 调用我们新增的后端API
    const result = await weatherApi.queryWeather(searchCity.value.trim())
    
    // 如果返回了结果
    if (result) {
      const forecast = mapBackendDataToForecast(result, result.city || searchCity.value)
      
      // 添加到列表（去重）
      const existingIndex = cityForecasts.value.findIndex(c => c.city === forecast.city)
      if (existingIndex > -1) {
        cityForecasts.value[existingIndex] = forecast
      } else {
        cityForecasts.value.unshift(forecast)
      }
      
      // 选中当前搜索的城市
      selectedCityId.value = forecast.id
      searchCity.value = '' // 清空搜索框
    } else {
      errorMsg.value = '未查询到该城市的天气信息'
    }
  } catch (error: any) {
    console.error('查询天气失败:', error)
    errorMsg.value = error.message || '查询服务暂时不可用'
  } finally {
    loading.value = false
  }
}

// 初始化加载一些默认城市
const initDefaults = async () => {
  if (cityForecasts.value.length === 0) {
    searchCity.value = '上海'
    await handleSearch()
    // 可以继续加载其他默认城市，如果不希望阻塞只加载一个即可
  }
}

onMounted(() => {
  initDefaults()
})
</script>

<style scoped>
.weather-forecast-view {
  padding: 32px clamp(16px, 4vw, 48px) 60px;
  background: radial-gradient(circle at 15% 20%, rgba(59, 130, 246, 0.12), transparent 45%),
    radial-gradient(circle at 80% 0%, rgba(14, 165, 233, 0.15), transparent 40%),
    #ebedf1;
  min-height: 100vh;
  color: #1e293b; /* 调整文字颜色以适应浅色背景 */
  box-sizing: border-box;
}

.hero {
  display: flex;
  justify-content: space-between;
  gap: 24px;
  margin-bottom: 32px;
  flex-wrap: wrap;
}

.page-label {
  margin: 0;
  font-size: 13px;
  letter-spacing: 0.2em;
  color: rgba(100, 116, 139, 0.8);
  text-transform: uppercase;
}

.hero h1 {
  margin: 12px 0 8px;
  font-size: clamp(26px, 4vw, 34px);
  color: #0f172a;
}
.hero p {
    color: #475569;
}

.primary-btn {
  align-self: flex-start;
  border-radius: 999px;
  padding: 12px 26px;
  background: linear-gradient(135deg, #06b6d4, #2563eb);
  border: none;
  color: #fff;
  font-weight: 600;
  cursor: pointer;
  box-shadow: 0 10px 20px rgba(6, 182, 212, 0.25);
  transition: transform 0.2s;
}
.primary-btn:active {
    transform: scale(0.98);
}
.primary-btn.search-btn {
    padding: 10px 20px;
    border-radius: 8px;
    margin-left: 12px;
}
.primary-btn:disabled {
    opacity: 0.7;
    cursor: not-allowed;
}

.city-selector {
  margin-bottom: 24px;
}

.search-box {
    display: flex;
    align-items: center;
    max-width: 400px;
    margin-bottom: 16px;
}
.search-box input {
    flex: 1;
    padding: 10px 16px;
    border-radius: 8px;
    border: 1px solid #cbd5e1;
    font-size: 16px;
    outline: none;
    transition: border-color 0.2s;
}
.search-box input:focus {
    border-color: #2563eb;
}

.city-list {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  margin-top: 12px;
}

.city-pill {
  border-radius: 999px;
  padding: 8px 16px;
  border: 1px solid #cbd5e1;
  background: #fff;
  color: #475569;
  cursor: pointer;
  transition: all 0.3s ease;
}

.city-pill span {
  margin-left: 6px;
  font-size: 12px;
  color: #94a3b8;
}

.city-pill.active {
  border-color: #0ea5e9;
  background: #e0f2fe;
  color: #0284c7;
}

.error-msg {
    color: #ef4444;
    margin-top: 8px;
    font-size: 14px;
}

.forecast-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 20px;
}

.forecast-card {
  background: rgba(255, 255, 255, 0.8);
  border-radius: 24px;
  padding: 24px;
  border: 1px solid #e2e8f0;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.05);
  backdrop-filter: blur(8px);
}

.forecast-card header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
}

.card-label {
  margin: 0;
  font-size: 13px;
  letter-spacing: 0.15em;
  text-transform: uppercase;
  color: #64748b;
}
.forecast-card h3 {
    margin: 4px 0;
    color: #0f172a;
}

.temperature {
  font-size: 32px;
  font-weight: 700;
  color: #0284c7;
}

.aqi-tag {
    display: inline-block;
    padding: 2px 8px;
    border-radius: 4px;
    background: #dcfce7;
    color: #166534;
    font-size: 12px;
    font-weight: 500;
}

.forecast-details {
  list-style: none;
  padding: 0;
  margin: 18px 0 0;
  line-height: 1.8;
  font-size: 15px;
  color: #334155;
}

.trend-list {
  display: flex;
  gap: 12px;
  margin-top: 20px;
}

.trend-item {
  flex: 1;
  background: #f1f5f9;
  border-radius: 16px;
  padding: 12px;
  text-align: center;
  border: 1px solid #e2e8f0;
}

.trend-item p {
    color: #64748b;
    margin: 0 0 4px;
    font-size: 14px;
}
.trend-item span {
  display: block;
  margin: 6px 0;
  font-size: 16px;
  font-weight: 600;
  color: #0f172a;
}
.trend-item small {
    color: #475569;
}

.advice-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 12px;
  margin-top: 18px;
}

.advice-card {
  background: #f8fafc;
  border-radius: 18px;
  padding: 16px;
  border: 1px solid #e2e8f0;
  min-height: 100px;
}
.advice-card h4 {
    margin: 0 0 8px;
    color: #334155;
    font-size: 15px;
}
.advice-card p {
    margin: 0;
    font-size: 14px;
    color: #64748b;
    line-height: 1.5;
}

.forecast-card.wide {
  grid-column: span 2;
}

@media (max-width: 900px) {
  .forecast-card.wide {
    grid-column: span 1;
  }
}
</style>


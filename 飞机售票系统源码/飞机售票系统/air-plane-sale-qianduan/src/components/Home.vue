<template>
  <div class="login-container">
    <div class="gradient-background">
      <span class="orb orb-left"></span>
      <span class="orb orb-right"></span>
      <span class="grid-overlay"></span>
    </div>

    <header class="landing-nav">
      <div class="nav-brand">
        <div class="logo-emblem">✈️</div>
        <div>
          <p class="brand-title">飞机售票系统</p>
          <p class="brand-subtitle">Air Command Center</p>
        </div>
      </div>
      <div class="nav-actions">
        <a href="#" class="nav-link" @click.prevent="showEnterpriseService">企业服务</a>
        <a href="#" class="nav-link" @click.prevent="showAPIAccess">API 接入</a>
      </div>
    </header>

    <div class="content-wrapper">
      <section class="hero-panel">
        <!-- 轮播图组件 -->
        <div class="carousel-section">
          <Carousel
            :slides="carouselSlides"
            :interval="5000"
            :autoplay="true"
          />
        </div>

        <!-- 主标题区域 -->
        <div class="hero-header">
          <div class="hero-badge">飞行即刻 · 极速出票</div>
          <h1>云端级服务，打造高端飞行体验</h1>
          <p class="hero-description">
            全球航线一键触达，智能推荐最佳行程，安全加密保障您的每一次出行。
          </p>
        </div>

        <!-- 飞机售票系统资讯区域 -->
        <div class="news-section">
          <div class="section-title-with-icon">
            <span class="section-icon">📰</span>
            <h2>系统资讯</h2>
          </div>
          <div class="news-grid">
            <div class="news-card">
              <div class="news-image" style="background: var(--gradient-primary);">
                <span class="news-icon">✈️</span>
              </div>
              <div class="news-content">
                <h3>智能选座系统上线</h3>
                <p>全新升级的选座功能，支持实时座位图查看，让您选择心仪的座位位置</p>
                <span class="news-date">2025-11-18</span>
              </div>
            </div>
            <div class="news-card">
              <div class="news-image" style="background: var(--gradient-primary-light);">
                <span class="news-icon">🎫</span>
              </div>
              <div class="news-content">
                <h3>特价机票限时抢购</h3>
                <p>每周五推出特价机票活动，覆盖全国热门航线，最低价格保证</p>
                <span class="news-date">2025-11-17</span>
              </div>
            </div>
            <div class="news-card">
              <div class="news-image" style="background: var(--gradient-success);">
                <span class="news-icon">🛡️</span>
              </div>
              <div class="news-content">
                <h3>安全保障升级</h3>
                <p>采用金融级加密技术，全方位保障您的个人信息和支付安全</p>
                <span class="news-date">2025-11-16</span>
              </div>
            </div>
            <div class="news-card">
              <div class="news-image" style="background: var(--gradient-warning);">
                <span class="news-icon">⭐</span>
              </div>
              <div class="news-content">
                <h3>会员积分系统</h3>
                <p>注册即送积分，购票累积积分，积分可兑换机票、升舱等多项权益</p>
                <span class="news-date">2025-11-15</span>
              </div>
            </div>
            <div class="news-card">
              <div class="news-image" style="background: linear-gradient(135deg, var(--color-cyan), var(--color-cyan-dark));">
                <span class="news-icon">🌐</span>
              </div>
              <div class="news-content">
                <h3>全球航线网络扩展</h3>
                <p>新增30+国际航线，覆盖亚洲、欧洲、美洲主要城市，为您提供更丰富的出行选择</p>
                <span class="news-date">2025-11-13</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 特价航班区域 -->
        <div class="flight-deals-section">
          <div class="deals-header">
            <div class="deals-title">
              <span class="deals-icon">✈️</span>
              <div>
                <p class="deals-eyebrow">热门航线限时优惠</p>
                <h2>特价活动</h2>
              </div>
            </div>
            <div class="deals-tabs">
              
            </div>
          </div>
          <div class="deal-card-group">
            <article
              v-for="deal in flightDeals"
              :key="deal.id"
              class="deal-card"
              :class="{ featured: deal.featured }"
            >
              <div class="deal-ellipse" :style="{ backgroundImage: `url(${deal.image})` }">
                <div class="deal-ellipse-shade"></div>
                <div class="deal-hover-info">
                  <p class="deal-route-text">
                    {{ deal.departureCity }} → {{ deal.arrivalCity || deal.city }}
                  </p>
                  <p class="deal-duration">
                    预计用时 {{ deal.duration }}
                  </p>
                </div>
                <div class="deal-card-content">
                  <span class="deal-chip">{{ deal.tag }}</span>
                  <p class="deal-city">{{ deal.city }}</p>
                  <p class="deal-price">
                    ¥{{ deal.price }}
                    <span>起</span>
                  </p>
                </div>
              </div>
             
            </article>
          </div>
         
        </div>

        <!-- 酒店推荐区域 -->
        <div class="hotel-section">
          <div class="section-title-with-icon">
            <span class="section-icon">🏨</span>
            <h2>酒店推荐</h2>
          </div>
          <div class="hotel-content">
            <div class="hotel-text">
              <h3>轻松入住 · 全球甄选</h3>
              <p class="hotel-desc">提供覆盖全球的酒店资源，实现您的环球之旅</p>
              <p class="hotel-desc">根据您的行程随心安排住宿，部分房型可在规定时间内免费取消</p>
              <p class="hotel-desc">优质售后与旅程顾问服务，为您的每一次停留提供贴心守护</p>
             
            </div>
            <div class="hotel-images-row" data-animate="fade-up">
              <div
                v-for="hotel in hotelImages"
                :key="hotel.id"
                class="hotel-image-card"
              >
                <div class="hotel-image" :style="{ backgroundImage: `url(${hotel.image})` }">
                  <span class="hotel-chip">{{ hotel.city }}</span>
                  <div class="hotel-image-info">
                    <p class="hotel-name">{{ hotel.name }}</p>
                    <p class="hotel-meta">{{ hotel.meta }}</p>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 服务资讯区域 -->
        <div class="services-section">
          <div class="section-title-with-icon">
            <span class="section-icon">ℹ️</span>
            <h2>服务资讯</h2>
          </div>
          <div class="services-grid-new">
            <!-- 左侧：免费酒店大卡片 -->
            <div class="service-card-large free-hotel-card">
              <div class="service-badge">免费酒店</div>
              <div class="service-image-large" style="background-image: url('/酒店.jpg'); background-size: cover; background-position: center;">
              </div>
              <div class="service-content-large">
                <p class="service-text-large">赠上海、北京、昆明、西安等地隔夜酒店</p>
                <a href="#" class="service-link-large">中转服务专区 ></a>
              </div>
            </div>

            <!-- 右侧：三个服务卡片 -->
            <div class="services-column-new">
              <!-- 空铁联运 -->
              <div class="service-card-medium">
                <div class="service-image-medium" style="background-image: url('/高铁.webp'); background-size: cover; background-position: center;">
                </div>
                <div class="service-content-medium">
                  <p class="service-text-medium">空铁联运，为出行提供更多选择</p>
                  <a href="#" class="service-link-medium">航空+铁路一站式购买 ></a>
                </div>
              </div>

              <!-- 机场巴士 -->
              <div class="service-card-medium">
                <div class="service-image-medium" style="background-image: url('/巴士.png'); background-size: cover; background-position: center;">
                </div>
                <div class="service-content-medium">
                  <p class="service-text-medium">机场巴士"空中+地面"一站式服务</p>
                  <a href="#" class="service-link-medium">安心便捷的周转换乘接驳 ></a>
                </div>
              </div>

              <!-- 机场接送 -->
              <div class="service-card-medium airport-transfer-new">
                <div class="service-badge-medium">积分抵扣</div>
                <div class="service-image-medium" style="background-image: url('/接送.webp'); background-size: cover; background-position: center;">
                </div>
                <div class="service-content-medium">
                  <p class="service-text-medium">机场接送服务尊享无忧出行</p>
                  <a href="#" class="service-link-medium">三重保障，快速预订 ></a>
                </div>
              </div>
            </div>
          </div>
        </div>

     

      
      </section>

      <section class="form-panel">
        <div class="login-card">
          <div class="logo-section">
            <div class="logo-emblem">✈️</div>
            <div>
              <h2>飞机售票系统</h2>
              <p>{{ authMode === 'login' ? '欢迎登录，开启您的飞行旅程' : '创建账户，开启飞行旅程' }}</p>
            </div>
          </div>

          <div class="mode-switch">
            <button
              type="button"
              :class="['mode-btn', { active: authMode === 'login' }]"
              @click="switchAuthMode('login')"
            >
              登录
            </button>
            <button
              type="button"
              :class="['mode-btn', { active: authMode === 'register' }]"
              @click="switchAuthMode('register')"
            >
              注册
            </button>
          </div>

          <form
            v-if="authMode === 'login'"
            @submit.prevent="handleLogin"
            class="login-form"
          >
            <div class="form-group">
              <label for="username">用户名</label>
              <div class="input-shell">
                <span class="input-icon">👤</span>
                <input
                  id="username"
                  v-model="form.username"
                  type="text"
                  placeholder="请输入用户名"
                  required
                />
              </div>
            </div>

            <div class="form-group">
              <label for="password">密码</label>
              <div class="input-shell">
                <span class="input-icon">🔒</span>
                <input
                  id="password"
                  v-model="form.password"
                  type="password"
                  placeholder="请输入密码"
                  required
                />
              </div>
            </div>

            <div class="form-group">
              <label for="role">角色</label>
              <div class="input-shell">
                <span class="input-icon">👥</span>
                <select
                  id="role"
                  v-model="form.role"
                  required
                  class="role-select"
                >
                  <option value="passenger">普通乘客</option>
                  <option value="operator">运营人员</option>
                  <option value="admin">系统管理员</option>
                </select>
              </div>
            </div>

            <div class="form-options">
              <label class="remember-me">
                <input type="checkbox" v-model="rememberMe" />
                记住我
              </label>
              <a href="#" class="forgot-password" @click.prevent="handleForgotPassword">忘记密码？</a>
            </div>

            <div v-if="errorMessage" class="error-message">
              {{ errorMessage }}
            </div>

            <button type="submit" class="login-btn" :disabled="loading">
              {{ loading ? '登录中...' : '登录' }}
            </button>
          </form>

          <form
            v-else
            class="register-form"
            @submit.prevent="handleRegister"
          >
            <div class="form-grid">
              <label>
                用户名
                <input v-model="registerForm.username" type="text" placeholder="3-12 位字符" />
                <span class="field-error" v-if="registerErrors.username">{{ registerErrors.username }}</span>
              </label>
              <label>
                身份证号
                <input v-model="registerForm.idCard" type="text" placeholder="18位身份证号" maxlength="18" />
                <span class="field-error" v-if="registerErrors.idCard">{{ registerErrors.idCard }}</span>
              </label>
            </div>
            <div class="form-grid">
              <label>
                手机号
                <input v-model="registerForm.phone" type="tel" placeholder="11 位手机号" />
                <span class="field-error" v-if="registerErrors.phone">{{ registerErrors.phone }}</span>
              </label>
              <label>
                真实姓名
                <input v-model="registerForm.realName" type="text" placeholder="请输入真实姓名" />
                <span class="field-error" v-if="registerErrors.realName">{{ registerErrors.realName }}</span>
              </label>
            </div>
            <div class="form-grid">
              <label>
                密码
                <input v-model="registerForm.password" type="password" placeholder="至少 6 位" />
                <span class="field-error" v-if="registerErrors.password">{{ registerErrors.password }}</span>
              </label>
              <label>
                确认密码
                <input v-model="registerForm.confirmPassword" type="password" placeholder="再次输入密码" />
                <span class="field-error" v-if="registerErrors.confirmPassword">{{ registerErrors.confirmPassword }}</span>
              </label>
            </div>
            <div class="form-grid">
              <label>
                角色
                <select v-model="registerForm.role" required class="role-select">
                  <option value="passenger">普通乘客</option>
                  <option value="operator">运营人员</option>
                  <option value="admin">系统管理员</option>
                </select>
                <span class="field-error" v-if="registerErrors.role">{{ registerErrors.role }}</span>
              </label>
            </div>
            <label class="terms-check">
              <input type="checkbox" v-model="agreedToTerms" />
              我已阅读并同意 <a href="#" @click.prevent="showTerms">《用户协议》</a> 与 <a href="#" @click.prevent="showPrivacy">《隐私政策》</a>
            </label>
            <span class="field-error" v-if="registerErrors.agreed">{{ registerErrors.agreed }}</span>

            <button type="submit" class="login-btn" :disabled="registerLoading">
              {{ registerLoading ? '注册中...' : '立即注册' }}
            </button>
          <div style="margin-top:8px;text-align:center;">
            <button type="button" class="link-btn" @click="router.push('/register/phone')">使用手机号或邮箱注册</button>
          </div>
          </form>

          <div class="register-section">
            <p v-if="authMode === 'login'">
              还没有账号？
              <button class="link-btn" type="button" @click="switchAuthMode('register')">立即注册</button>
            </p>
            <p v-else>
              已有账号？
              <button class="link-btn" type="button" @click="switchAuthMode('login')">立即登录</button>
            </p>
          </div>
        </div>
      </section>
    </div>

   

    <!-- 忘记密码弹窗 -->
    <ModalPrompt
      v-model="forgotPasswordDialog.visible"
      :title="forgotPasswordDialog.title"
      :message="forgotPasswordDialog.message"
      type="info"
      confirm-text="知道了"
      :adaptive-position="true"
    />

    <!-- 用户协议弹窗 -->
    <ModalPrompt
      v-model="termsDialog.visible"
      :title="termsDialog.title"
      :message="termsDialog.message"
      type="info"
      confirm-text="我已阅读"
      :adaptive-position="true"
      :top-position="true"
    />

    <!-- 隐私政策弹窗 -->
    <ModalPrompt
      v-model="privacyDialog.visible"
      :title="privacyDialog.title"
      :message="privacyDialog.message"
      type="info"
      confirm-text="我已阅读"
      :adaptive-position="true"
      :top-position="true"
    />

    <!-- 企业服务弹窗 -->
    <ModalPrompt
      v-model="enterpriseDialog.visible"
      :title="enterpriseDialog.title"
      :message="enterpriseDialog.message"
      type="info"
      confirm-text="了解更多"
      :center-offset-y="-1700"
    />

    <!-- API接入弹窗 -->
    <ModalPrompt
      v-model="apiDialog.visible"
      :title="apiDialog.title"
      :message="apiDialog.message"
      type="info"
      confirm-text="查看文档"
      :center-offset-y="-1700"
    />

    <!-- 返回顶部按钮 -->
    <button 
      :class="['back-to-top-btn', { show: showBackToTop }]"
      @click="scrollToTop"
      aria-label="返回顶部"
    >
      <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
        <path d="M12 19V5M5 12l7-7 7 7"/>
      </svg>
    </button>

    <!-- 返回底部按钮 -->
    <button 
      class="back-to-bottom-btn"
      @click="scrollToBottom"
      aria-label="返回底部"
    >
      <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
        <path d="M12 5v14M5 12l7 7 7-7"/>
      </svg>
    </button>

    <!-- 页脚 -->
    
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { authApi, passengerApi } from '../services/api'
import store from '../services/store'
import ModalPrompt from './ModalPrompt.vue'
import Carousel from './Carousel.vue'
import LandingFooter from './layout/LandingFooter.vue'
import TicketQuickEntry from './TicketQuickEntry.vue'

interface LoginForm {
  username: string
  password: string
  role: string
}

interface RegisterForm {
  username: string
  idCard: string
  phone: string
  realName: string
  password: string
  confirmPassword: string
  role: string
}

const router = useRouter()
const form = reactive<LoginForm>({
  username: '',
  password: '',
  role: 'passenger'
})

const authMode = ref<'login' | 'register'>('login')
const rememberMe = ref(false)
const loading = ref(false)
const errorMessage = ref('')
const statusDialog = reactive({
  visible: false,
  type: 'info' as 'success' | 'error' | 'info',
  title: '',
  message: '',
  top: false,
  slideFromTop: false
})
let dialogTimer: number | null = null
const registerForm = reactive<RegisterForm>({
  username: '',
  idCard: '',
  phone: '',
  realName: '',
  password: '',
  confirmPassword: '',
  role: 'passenger'
})
const registerErrors = reactive<Record<string, string>>({
  username: '',
  idCard: '',
  phone: '',
  realName: '',
  password: '',
  confirmPassword: '',
  role: '',
  agreed: ''
})
const registerLoading = ref(false)
const agreedToTerms = ref(false)

const showStatus = (
  type: 'success' | 'error' | 'info',
  title: string,
  message: string
) => {
  if (dialogTimer) {
    clearTimeout(dialogTimer)
    dialogTimer = null
  }
  statusDialog.type = type
  statusDialog.title = title
  statusDialog.message = message
  // 错误场景：从顶部下滑提示；其余维持原有模式
  statusDialog.top = type === 'error'
  statusDialog.slideFromTop = type === 'error'
  statusDialog.visible = true

  // 成功和信息提示自动关闭，错误提示需要手动关闭
  if (type === 'success' || type === 'info') {
    dialogTimer = window.setTimeout(() => {
      statusDialog.visible = false
    }, 1200)
  }
}

const handleLogin = async () => {
  if (!form.username || !form.password) {
    errorMessage.value = '请输入用户名和密码'
    return
  }
  
  loading.value = true
  errorMessage.value = ''
  
  try {
    // 第一步：调用后端登录接口（不传递role，让后端自动识别）
    const result = await authApi.login({
      username: form.username,
      password: form.password
    })
    
    console.log('登录成功:', result)
    
    // 第二步：获取后端返回的实际role
    const backendRole = result.user?.role || 'passenger'
    const userSelectedRole = form.role
    
    // 第三步：验证用户选择的role是否与后端返回的role一致
    if (backendRole !== userSelectedRole) {
      // role不匹配，显示错误提示
      errorMessage.value = `账号类型不匹配！该账号实际角色为"${getRoleDisplayName(backendRole)}"，但您选择的是"${getRoleDisplayName(userSelectedRole)}"。请重新选择正确的角色。`
      showStatus('error', '账号类型不匹配', errorMessage.value)
      loading.value = false
      return
    }
    
    // 第四步：role匹配，继续登录流程
    // 如果勾选了记住我，保存用户名到localStorage（只保存用户名，不保存密码）
    if (rememberMe.value) {
      localStorage.setItem('remembered_username', form.username)
    } else {
      localStorage.removeItem('remembered_username')
    }
    
    // 使用状态管理工具设置用户信息
    store.setUserState({
      ...result.user
    })
    
    // 根据用户角色跳转到正确的主页路径
    const roleHomeMap: Record<string, string> = {
      admin: '/dashboard',
      operator: '/portal/operations',
      passenger: '/portal/passengers'
    }
    const redirectPath = roleHomeMap[result.user?.role || 'passenger'] || '/portal/passengers'
    
    // 直接跳转，不显示弹窗，不延迟
    router.push(redirectPath)
    
  } catch (error: any) {
    console.error('登录失败:', error)
    errorMessage.value = error.message || '登录失败，请重试'
    showStatus('error', '登录失败', errorMessage.value)
  } finally {
    loading.value = false
  }
};

// 辅助函数：获取角色的显示名称
const getRoleDisplayName = (role: string): string => {
  const roleMap: Record<string, string> = {
    admin: '系统管理员',
    operator: '运营人员',
    passenger: '普通乘客'
  }
  return roleMap[role] || role
};

// 页面加载时检查是否有记住的用户名
onMounted(() => {
  // 优先检查注册邮箱，如果没有则使用记住的用户名
  const registeredEmail = localStorage.getItem('registered_email')
  if (registeredEmail) {
    form.username = registeredEmail
    // 清除注册邮箱，避免重复使用
    localStorage.removeItem('registered_email')
  } else {
    const rememberedUsername = localStorage.getItem('remembered_username')
    if (rememberedUsername) {
      form.username = rememberedUsername
      rememberMe.value = true
    }
  }
  
  // 添加滚动监听
  window.addEventListener('scroll', handleScroll, { passive: true })
  handleScroll() // 初始化检查
})

// 组件卸载时移除滚动监听
onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
})

// 新增：处理忘记密码
const forgotPasswordDialog = reactive({
  visible: false,
  title: '忘记密码',
  message: '请联系系统管理员重置密码\n\n电话：400-888-8888\n邮箱：support@airline.com\n工作时间：9:00-18:00'
})

const handleForgotPassword = () => {
  forgotPasswordDialog.visible = true
};

const validateRegisterForm = () => {
  let valid = true
  if (!registerForm.username || registerForm.username.length < 3) {
    registerErrors.username = '用户名至少 3 位'
    valid = false
  } else {
    registerErrors.username = ''
  }

  const idCardRegex = /^\d{17}[\dXx]$/
  if (!registerForm.idCard) {
    registerErrors.idCard = '请输入身份证号'
    valid = false
  } else if (!idCardRegex.test(registerForm.idCard)) {
    registerErrors.idCard = '请输入有效的18位身份证号'
    valid = false
  } else {
    registerErrors.idCard = ''
  }

  const phoneRegex = /^1[3-9]\d{9}$/
  if (!phoneRegex.test(registerForm.phone)) {
    registerErrors.phone = '请输入有效手机号'
    valid = false
  } else {
    registerErrors.phone = ''
  }

  if (!registerForm.realName) {
    registerErrors.realName = '请填写真实姓名'
    valid = false
  } else {
    registerErrors.realName = ''
  }

  if (registerForm.password.length < 6) {
    registerErrors.password = '密码至少 6 位'
    valid = false
  } else {
    registerErrors.password = ''
  }

  if (registerForm.password !== registerForm.confirmPassword) {
    registerErrors.confirmPassword = '两次输入不一致'
    valid = false
  } else {
    registerErrors.confirmPassword = ''
  }

  if (!registerForm.role) {
    registerErrors.role = '请选择角色'
    valid = false
  } else {
    registerErrors.role = ''
  }

  if (!agreedToTerms.value) {
    registerErrors.agreed = '请先同意协议'
    valid = false
  } else {
    registerErrors.agreed = ''
  }

  return valid
};

const clearRegisterForm = () => {
  registerForm.username = ''
  registerForm.idCard = ''
  registerForm.phone = ''
  registerForm.realName = ''
  registerForm.password = ''
  registerForm.confirmPassword = ''
  registerForm.role = 'passenger'
  agreedToTerms.value = false
};

const handleRegister = async () => {
  if (!validateRegisterForm()) return
  registerLoading.value = true
  try {
    await authApi.register({
      username: registerForm.username,
      idCard: registerForm.idCard,
      phone: registerForm.phone,
      realName: registerForm.realName,
      password: registerForm.password,
      role: registerForm.role
    })
    clearRegisterForm()
    showStatus('success', '注册成功', '账号创建成功，请登录')
    // 延迟切换到登录模式
    setTimeout(() => {
      authMode.value = 'login'
    }, 1000)
  } catch (error: any) {
    showStatus('error', '注册失败', error?.message || '请稍后重试')
  } finally {
    registerLoading.value = false
  }
};

const switchAuthMode = (mode: 'login' | 'register') => {
  authMode.value = mode
  if (mode === 'login') {
    errorMessage.value = ''
  }
};

const termsDialog = reactive({
  visible: false,
  title: '用户协议',
  message: `1. 用户应遵守相关法律法规
2. 不得恶意刷单或干扰系统正常运行
3. 个人信息将严格保密
4. 系统维护期间可能暂停服务
5. 禁止任何形式的资源滥用
6. 服务最终解释权归本平台所有`
})

const privacyDialog = reactive({
  visible: false,
  title: '隐私政策',
  message: `1. 收集的信息仅用于提供服务
2. 不会向第三方出售用户信息
3. 采用加密技术保护数据安全
4. 用户有权要求删除个人信息
5. 定期进行安全审计
6. 遵循数据保护相关法律法规`
})

const enterpriseDialog = reactive({
  visible: false,
  title: '企业服务',
  message: `专为企业客户提供专业服务：

• 批量预订服务
• 专属客户经理
• 定制化解决方案
• 企业账户管理
• 优先支持服务
• 月结账单服务

联系我们：
电话：400-888-9999
邮箱：enterprise@airline.com`
})

const apiDialog = reactive({
  visible: false,
  title: 'API接入',
  message: `提供完善的API服务：

• 航班查询接口
• 在线预订支持
• 实时价格获取
• 订单状态同步
• RESTful API设计
• 完整技术文档

技术支持：
邮箱：api@airline.com
文档：https://api.airline.com/docs`
})

const showTerms = () => {
  termsDialog.visible = true
}

const showPrivacy = () => {
  privacyDialog.visible = true
}

// 新增：显示企业服务信息
const showEnterpriseService = () => {
  enterpriseDialog.visible = true
}

// 新增：显示API接入信息
const showAPIAccess = () => {
  apiDialog.visible = true
}


// 页面加载时检查是否记住用户名
const loadRememberedUser = () => {
  const remembered = localStorage.getItem('remember_me')
  if (remembered === 'true') {
    rememberMe.value = true
    const rememberedUsername = localStorage.getItem('remembered_username')
    if (rememberedUsername) {
      form.username = rememberedUsername
    }
  }
};

// 调用加载记住的用户信息
loadRememberedUser()

// 当前日期
const currentDate = ref(new Date().toISOString().split('T')[0])

// 特价航班数据（使用真实图片）
const flightDeals = ref([
  {
    id: 'lz',
    city: '兰州',
    price: 249,
    tag: '上海出发',
    image: '/兰州.jpg',
    departureCity: '上海',
    arrivalCity: '兰州中川',
    duration: '约3小时20分'
  },
  {
    id: 'dl',
    city: '大连',
    price: 299,
    tag: '上海出发',
    image: '/大连.jpg',
    departureCity: '上海',
    arrivalCity: '大连周水子',
    duration: '约2小时10分'
  },
  {
    id: 'ls',
    city: '丽水',
    price: 299,
    tag: '上海出发',
    image: '/丽水.jpg',
    departureCity: '上海',
    featured: true,
    date: currentDate.value,
    arrivalCity: '丽水三蒋',
    duration: '约1小时50分'
  },
  {
    id: 'xz',
    city: '忻州',
    price: 299,
    tag: '上海出发',
    image: '/上海.png',
    departureCity: '上海',
    arrivalCity: '忻州五台山',
    duration: '约2小时40分'
  },
  {
    id: 'll',
    city: '吕梁',
    price: 299,
    tag: '上海出发',
    image: '/吕梁.jpg',
    departureCity: '上海',
    arrivalCity: '吕梁离石',
    duration: '约2小时30分'
  }
])

// 酒店图片数据
const hotelImages = ref([
  {
    id: 1,
    city: '上海',
    name: '陆家嘴云端行政套房',
    meta: '高空露台 · ¥1299 / 晚起',
    image: '/上海酒店.webp'
  },
  {
    id: 2,
    city: '北京',
    name: '国贸CBD 景观酒店',
    meta: '夜景地标 · ¥899 / 晚起',
    image: '/北京酒店.webp'
  },
  {
    id: 3,
    city: '三亚',
    name: '海岛度假海景房',
    meta: '私人沙滩 · ¥699 / 晚起',
    image: '/三亚酒店.jpg'
  },
  {
    id: 4,
    city: '墨尔本',
    name: '河畔艺术酒店',
    meta: '文化街区 · ¥1099 / 晚起',
    image: '/墨尔本.webp'
  }
])

// 返回顶部按钮显示状态
const showBackToTop = ref(false)

// 滚动监听函数
const handleScroll = () => {
  showBackToTop.value = window.scrollY > 300
  // 添加/移除show类以触发动画
  const btn = document.querySelector('.back-to-top-btn')
  if (btn) {
    if (showBackToTop.value) {
      btn.classList.add('show')
    } else {
      btn.classList.remove('show')
    }
  }
}

// 返回顶部函数
const scrollToTop = () => {
  window.scrollTo({
    top: 0,
    behavior: 'smooth'
  })
}

// 返回底部函数
const scrollToBottom = () => {
  window.scrollTo({
    top: document.documentElement.scrollHeight,
    behavior: 'smooth'
  })
}

// 轮播图数据
const carouselSlides = ref([
  {
    image: '/轮播图1.jpg',
    title: '全球航线，一键触达',
    description: '连接180+国际航司，覆盖全球主要城市，让您的出行更加便捷'
  },
  {
    image: '/轮播图2.jpg',
    title: '极速出票，智能推荐',
    description: '平均3.9秒完成出票，AI智能推荐最优方案，节省您的宝贵时间'
  },
  {
    image: '/轮播图3.webp',
    title: '安全可靠，值得信赖',
    description: '金融级加密技术，99.8%系统稳定性，全方位保障您的信息安全和出行体验'
  }
])
</script>

<style scoped>
/* 登录面板 */
.login-container {
  position: relative;
  min-height: 100vh;
  width: 100%;
  max-width: 100vw;
  /* 只使用浏览器自身的滚动条，避免页面内出现第二个滚动条 */
  overflow-x: hidden;
  overflow-y: visible;
  background: linear-gradient(135deg, #769fc4 0%, #ffffff 50%, #e0f6ff 100%);
  color: var(--color-text-primary);
  box-sizing: border-box;
  /* 滚动行为交给浏览器全局控制，避免双重滚动 */
}
/* 导航栏 */
.landing-nav {
  position: relative;
  z-index: 2;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 2rem clamp(1rem, 2vw, 2rem) 1rem;
  width: 100%;
  max-width: 100vw;
  margin: 0 auto;
  box-sizing: border-box;
  background: linear-gradient(135deg, rgba(19, 126, 168, 0.1), rgba(255, 255, 255, 0.05));
  border-bottom: 1px solid rgba(135, 206, 235, 0.2);
  backdrop-filter: blur(10px);
  transition: all var(--transition-base);
}

.nav-brand {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}
/* 品牌标题 */
.brand-title {
  font-size: 1rem;
  margin: 0;
  letter-spacing: 0.08em;
  background: var(--gradient-primary);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  font-weight: 700;
}

.brand-subtitle {
  margin: 0;
  font-size: 0.75rem;
  color: var(--color-text-secondary);
  font-weight: 500;
}

.nav-actions {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.nav-link {
  color: var(--color-text-secondary);
  text-decoration: none;
  font-size: 0.9rem;
  transition: all var(--transition-base);
  padding: 0.5rem 1rem;
  border-radius: var(--radius-lg);
}

.nav-link:hover {
  color: var(--color-primary);
  background: rgba(135, 206, 235, 0.1);
}
/* 导航栏按钮 */
.nav-cta {
  padding: 0.5rem 1.4rem;
  border-radius: 999px;
  border: 1px solid rgba(204, 209, 235, 0.35);
  text-decoration: none;
  color: #fff;
  font-weight: 600;
  transition: background 0.3s ease, transform 0.3s ease;
}

.nav-cta:hover {
  background: rgba(255, 255, 255, 0.1);
  transform: translateY(-2px);
}
/* 登录面板背景 */
.gradient-background {
  position: absolute;
  inset: 0;
  background: radial-gradient(circle at top left, rgba(91, 93, 215, 0.35), transparent 50%),
              radial-gradient(circle at top right, rgba(189, 164, 176, 0.25), transparent 45%),
              radial-gradient(circle at bottom, rgba(14, 165, 233, 0.25), transparent 55%);
  filter: blur(40px);
}
/* 圆形 */
.orb {
  position: absolute;
  width: 320px;
  height: 320px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(255, 255, 255, 0.9), rgba(255, 255, 255, 0));
  opacity: 0.1;
  animation: float 12s ease-in-out infinite;
}

.orb-left {
  top: -80px;
  left: -120px;
}

.orb-right {
  bottom: -100px;
  right: -60px;
  animation-delay: 4s;
}

.grid-overlay {
  position: absolute;
  inset: 0;
  background-image: linear-gradient(rgba(255, 255, 255, 0.05) 1px, transparent 1px),
                    linear-gradient(90deg, rgba(255, 255, 255, 0.05) 1px, transparent 1px);
  background-size: 60px 60px;
  opacity: 0.3;
}
/* 登录面板内容 */
.content-wrapper {
  position: relative;
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 2.5rem;
  padding: 4rem clamp(1rem, 2vw, 2rem) 3rem;
  z-index: 1;
  align-items: start;
  width: 1600px;
  margin: 0 auto;
  box-sizing: border-box;
}
/* 登录面板主标题区域 */
.hero-panel {
  display: flex;
  flex-direction: column;
  justify-content: flex-start;
  align-items: center;
  gap: 3rem;
  width: 100%;
  max-width: 1400px;
  margin: 0 auto;
  box-sizing: border-box;
  flex: 1;
  min-width: 0;
  margin-left: 270px;
  padding-bottom: 2rem;
}
/* 登录面板主标题区域标题 */
.hero-header {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  gap: 1rem;
  margin: 2.5rem auto 0.5rem;
  width: 100%;
  max-width: 900px;
  box-sizing: border-box;

}
/* 登录面板轮播图区域 */
.carousel-section {
  width: 100%;
  height: clamp(625px, 35vh, 420px);
  height:680px;
  margin: 0 auto 1.5rem;
  border-radius: 24px;
  overflow: hidden;
  box-shadow: 0 20px 40px rgba(205, 151, 151, 0.3);
  box-sizing: border-box;
  margin-left: -280px;

}
/* 登录面板主标题区域描述 */
.hero-description {
  color: rgba(255, 255, 255, 0.85);
  font-size: 1.1rem;
  line-height: 1.8;
  margin-bottom: 1.5rem;
  text-align: center;
  max-width: 1500px;

}

/* 特价航班区域 */
.flight-deals-section {
  margin: 2rem 0;
  padding: 2.5rem;
  width: 1500px;
  box-sizing: border-box;
  border-radius: 32px;
  border: 1px solid rgba(255, 255, 255, 0.6);
  background: linear-gradient(180deg, #f7fbff 0%, rgba(255, 255, 255, 0.95) 100%);
  box-shadow: 0 30px 60px rgba(15, 23, 42, 0.1);
}

.deals-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 1rem;
  margin-bottom: 2rem;
}

.deals-title {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.deals-icon {
  font-size: 2rem;
  color: #1E8AE6;
}

.deals-eyebrow {
  margin: 0;
  font-size: 0.85rem;
  letter-spacing: 0.2em;
  color: #94a3b8;
  text-transform: uppercase;
}

.deals-header h2 {
  margin: 0;
  font-size: 1.9rem;
  color: #0f172a;
}

.deals-tabs {
  display: flex;
  gap: 0.75rem;
}

.deal-tab {
  border: 1px solid transparent;
  border-radius: 999px;
  padding: 0.45rem 1.6rem;
  font-weight: 600;
  font-size: 0.95rem;
  cursor: pointer;
  background: transparent;
  color: #64748b;
  transition: all 0.3s ease;
}

.deal-tab.active {
  background: linear-gradient(135deg, #1E8AE6, #0A2F63);
  color: #fff;
  box-shadow: 0 14px 30px rgba(30, 138, 230, 0.35);
}

.deal-tab.ghost {
  border-color: rgba(148, 163, 184, 0.5);
}

.deal-card-group {
  display: flex;
  gap: 1.25rem;
  flex-wrap: wrap;
  justify-content: center;
  padding: 0.5rem 0 1.5rem;
}

.deal-card {
  position: relative;
  width: 240px;
  display: flex;
  justify-content: center;
  padding-bottom: 1rem;
}

.deal-card.featured {
  padding-right: 0;
}
/* 特价航班卡片 */
.deal-ellipse {
  width: 220px;
  height: 360px;
  border-radius: 140px;
  background-size: cover;
  background-position: center;
  position: relative;
  overflow: hidden;

  flex-shrink: 0;
  box-shadow:
    0 20px 45px rgba(15, 23, 42, 0.25),
    inset 0 1px rgba(255, 255, 255, 0.15);
  transition: transform 0.35s ease, box-shadow 0.35s ease;
}
/* 特价航班卡片悬浮效果 */
.deal-card:hover .deal-ellipse {
  transform: translateY(-6px);
  box-shadow:
    0 30px 60px rgba(15, 23, 42, 0.3),
    inset 0 1px rgba(255, 255, 255, 0.2);
}
/* 特价航班卡片阴影 */
.deal-ellipse-shade {
  position: absolute;
  inset: 0;
  background: linear-gradient(180deg, rgba(15, 23, 42, 0.1) 10%, rgba(15, 23, 42, 0.85) 85%);
}
/* 特价航班卡片悬浮信息 */
.deal-hover-info {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #fff;
  text-align: center;
  z-index: 3;
  padding: 1.5rem;
  background: rgba(190, 190, 227, 0.7);
  opacity: 0;
  transform: translateY(10px);
  transition: opacity 0.3s ease, transform 0.3s ease;
  pointer-events: none;
}

.deal-ellipse:hover .deal-hover-info {
  opacity: 1;
  transform: translateY(0);
}

.deal-card-content {
  position: absolute;
  bottom: 1.5rem;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.5rem;
  color: #fff;
  z-index: 2;
  width: 100%;
  padding: 0 1.5rem;
  box-sizing: border-box;
  text-align: center;
  transition: opacity 0.3s ease, transform 0.3s ease;
}

.deal-ellipse:hover .deal-card-content {
  opacity: 0;
  transform: translate(-50%, 10px);
}

.deal-route-text {
  font-size: 1.1rem;
  font-weight: 600;
  margin: 0 0 0.25rem 0;
}

.deal-duration {
  margin: 0;
  font-size: 0.95rem;
  color: rgba(255, 255, 255, 0.85);
}

.deal-chip {
  font-size: 0.85rem;
  padding: 0.25rem 0.85rem;
  border-radius: 999px;
  background: rgba(238, 239, 240, 0.45);
  border: 1px solid rgba(255, 255, 255, 0.25);
  backdrop-filter: blur(10px);
}

.deal-city {
  margin: 0;
  font-size: 1.4rem;
  font-weight: 700;
  letter-spacing: 0.05em;
}

.deal-price {
  margin: 0;
  font-size: 1.6rem;
  font-weight: 700;
  color: #3d97e1;
}

.deal-price span {
  font-size: 0.85rem;
  margin-left: 0.25rem;
  font-weight: 500;
  color: rgba(255, 255, 255, 0.8);
}

.deal-featured-panel {
  position: absolute;
  top: 50%;
  right: 0;
  transform: translate(40px, -50%);
  width: 190px;
  border-radius: 28px;
  background: #fff;
  padding: 1rem 1.25rem;
  box-shadow: 0 25px 50px rgba(15, 23, 42, 0.15);
  display: flex;
  flex-direction: column;
  gap: 0.6rem;
  color: #0f172a;
}

.deal-date {
  margin: 0;
  font-size: 0.9rem;
  color: #94a3b8;
}

.deal-route {
  display: flex;
  align-items: center;
  gap: 0.6rem;
  font-weight: 600;
}

.route-path {
  flex: 1;
  height: 42px;
  border-left: 1px dashed rgba(148, 163, 184, 0.7);
  position: relative;
}

.route-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: #1E8AE6;
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translate(-50%, 50%);
  box-shadow: 0 0 0 8px rgba(30, 138, 230, 0.15);
}
/* 更多特价按钮 */
.more-deals-btn {
  width: 220px;
  margin: 0 auto;
  display: block;
  padding: 0.85rem;
  border: none;
  border-radius: 999px;
  background: linear-gradient(135deg, #1E52E8, #35A4FF);
  color: #e3e7eb;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  box-shadow: 0 18px 32px rgba(3, 137, 227, 0.25);
  transition: all 0.3s ease;
}

.more-deals-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 25px 38px rgba(30, 138, 230, 0.3);
}

/* 酒店推荐区域 */
.hotel-section {
  margin: 2rem 0;
  padding: 2.5rem;
  background: linear-gradient(180deg, #eaeaf1 0%, #b0caed 100%);
  border-radius: 32px;
  border: 1px solid rgba(238, 231, 231, 0.952);
  width: 1500px;
  box-sizing: border-box;
  box-shadow: 0 30px 60px rgba(245, 240, 240, 0.08);
  transition: all 0.3s ease;
}
/* 酒店推荐区域悬浮效果 */
.hotel-section:hover {
  border-color: rgba(30, 138, 230, 0.25);
  box-shadow: 0 35px 65px rgba(15, 23, 42, 0.12);
}
/* 酒店推荐区域标题 */
.section-title-with-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.8rem;
  margin-bottom: 1.5rem;
}

.section-icon {
  font-size: 1.5rem;
}
/* 酒店推荐区域标题 */
.section-title-with-icon h2 {
  margin: 0;
  font-size: 1.5rem;
  color: #0f172a;
  font-weight: 600;
}
/* 酒店推荐区域内容 */
.hotel-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 2.5rem;
  margin: 0 auto;
}

.hotel-text {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  max-width: 360px;
}

.hotel-text h3 {
  margin: 0;
  font-size: 1.6rem;
  color: #ffffff;
  font-weight: 700;
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
}

.hotel-desc {
  margin: 0;
  color: #ffffff;
  font-size: 1rem;
  line-height: 1.7;
  font-weight: 500;
  text-shadow: 0 1px 4px rgba(0, 0, 0, 0.15);
  background: rgba(255, 255, 255, 0.15);
  padding: 0.75rem 1rem;
  border-radius: 12px;
  border-left: 3px solid #1E8AE6;
}
/* 更多酒店按钮 */
.more-hotels-btn {
  margin-top: 1rem;
  align-self: flex-start;
  padding: 0.85rem 2rem;
  border: none;
  border-radius: 999px;
  background: linear-gradient(135deg, #1E8AE6, #3b82f6);
  color: #fff;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 18px 30px rgba(30, 138, 230, 0.25);
}
/* 更多酒店按钮悬浮效果 */
.more-hotels-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 22px 36px rgba(30, 138, 230, 0.32);
}
/* 酒店推荐区域图片列表 */
.hotel-images-row {
  display: flex;
  gap: 1.5rem;
  width: 100%;
  justify-content: center;
  flex-wrap: nowrap;
}

.hotel-image-card {
  width: 220px;
  height: 360px;
  border-radius: 32px;
  overflow: hidden;
  position: relative;
  flex-shrink: 0;
  box-shadow: 0 25px 45px rgba(15, 23, 42, 0.15);
  transition: transform 0.35s ease, box-shadow 0.35s ease;
}

.hotel-image-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 30px 55px rgba(15, 23, 42, 0.2);
}

.hotel-image {
  position: relative;
  width: 100%;
  height: 100%;
  background-size: cover;
  background-position: center;
  transition: transform 0.5s ease;
}

.hotel-image::after {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(180deg, rgba(15, 23, 42, 0) 40%, rgba(15, 23, 42, 0.85) 100%);
}

.hotel-image-card:hover .hotel-image {
  transform: scale(1.05);
}

.hotel-chip {
  position: absolute;
  top: 1rem;
  left: 1rem;
  z-index: 2;
  padding: 0.25rem 0.85rem;
  border-radius: 999px;
  background: rgba(15, 23, 42, 0.65);
  border: 1px solid rgba(255, 255, 255, 0.3);
  color: #fff;
  font-size: 0.85rem;
}

.hotel-image-info {
  position: absolute;
  bottom: 1.25rem;
  left: 1.25rem;
  right: 1.25rem;
  z-index: 2;
  color: #fff;
}

.hotel-name {
  margin: 0;
  font-size: 1.05rem;
  font-weight: 600;
}

.hotel-meta {
  margin: 0.2rem 0 0;
  font-size: 0.9rem;
  color: rgba(255, 255, 255, 0.75);
}

/* 服务资讯区域 */
.services-section {
  margin: 2rem 0;
  padding: 2rem;
  background: rgba(239, 240, 244, 0.6);
  border-radius: 24px;
  border: 1px solid rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(20px);
  width: 1500px;  
  box-sizing: border-box;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.3);
  transition: all 0.3s ease;
}
/* 服务资讯区域悬浮效果 */
.services-section:hover {
  border-color: rgba(99, 102, 241, 0.3);
  box-shadow: 0 25px 50px rgba(99, 102, 241, 0.2);
}

.services-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1.5rem;
  max-width: 1000px;
  margin: 0 auto;
}

.services-grid-new {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 2rem;
  max-width: 1200px;
  margin: 0 auto;
  align-items: stretch;
}
/* 服务卡片 */
.service-card-large {
  position: relative;
  border-radius: 20px;
  overflow: hidden;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.15);
  transition: all 0.3s ease;
}

.service-card-large:hover {
  transform: translateY(-4px);
  box-shadow: 0 15px 35px rgba(99, 102, 241, 0.2);
}
/* 服务卡片徽章 */
.service-badge {
  position: absolute;
  top: 1rem;
  left: 1rem;
  padding: 0.4rem 0.8rem;
  background: rgba(99, 102, 241, 0.8);
  color: #fff;
  font-size: 0.85rem;
  font-weight: 600;
  border-radius: 8px;
  z-index: 2;
  backdrop-filter: blur(8px);
}
/* 服务卡片图片 */
.service-image-placeholder {
  width: 100%;
  height: 200px;
  background: linear-gradient(135deg, rgba(99, 102, 241, 0.3), rgba(139, 92, 246, 0.3));
  position: relative;
}

.service-image-placeholder.hotel-room {
  background: linear-gradient(135deg, rgba(14, 165, 233, 0.3), rgba(59, 130, 246, 0.3));
}

.service-image-placeholder.train {
  background: linear-gradient(135deg, rgba(34, 197, 94, 0.3), rgba(16, 185, 129, 0.3));
}

.service-image-placeholder.bus {
  background: linear-gradient(135deg, rgba(251, 191, 36, 0.3), rgba(245, 158, 11, 0.3));
}

.service-image-placeholder.transfer {
  background: linear-gradient(135deg, rgba(139, 92, 246, 0.3), rgba(124, 58, 237, 0.3));
}

.service-content {
  padding: 1.2rem;
}

.service-text {
  margin: 0 0 0.8rem 0;
  color: rgba(255, 255, 255, 0.9);
  font-size: 0.95rem;
  line-height: 1.5;
}

.service-link {
  color: var(--color-primary);
  text-decoration: none;
  font-size: 0.9rem;
  font-weight: 500;
  transition: all 0.3s ease;
}

.service-link:hover {
  color: var(--color-primary-light);
  text-decoration: underline;
}

.services-column {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.services-column-new {
  display: flex;
  flex-direction: column;
  gap: 1.2rem;
}

/* 大卡片样式 */
.service-image-large {
  width: 100%;
  height: 620px;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
  border-radius: 20px 20px 0 0;
}

.hotel-icon {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.service-content-large {
  padding: 1.5rem;
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.service-text-large {
  margin: 0;
  color: rgba(255, 255, 255, 0.9);
  font-size: 1.05rem;
  line-height: 1.6;
  font-weight: 500;
}

.service-link-large {
  color: var(--color-primary-light);
  text-decoration: none;
  font-size: 0.95rem;
  font-weight: 600;
  transition: all 0.3s ease;
  display: inline-flex;
  align-items: center;
  gap: 0.4rem;
  width: fit-content;
}

.service-link-large:hover {
  color: var(--color-primary);
  text-decoration: underline;
  transform: translateX(2px);
}

/* 中等卡片样式 */
.service-card-medium {
  position: relative;
  display: flex;
  flex-direction: column;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.15);
  overflow: hidden;
  transition: all 0.3s ease;
  height: 100%;
}

.service-card-medium:hover {
  transform: translateY(-4px);
  background: rgba(255, 255, 255, 0.08);
  border-color: rgba(99, 102, 241, 0.3);
  box-shadow: 0 12px 32px rgba(99, 102, 241, 0.2);
}

.service-image-medium {
  width: 100%;
  height: 140px;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
}

.service-icon {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.service-content-medium {
  padding: 1.2rem;
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.service-text-medium {
  margin: 0 0 0.8rem 0;
  color: rgba(255, 255, 255, 0.9);
  font-size: 0.95rem;
  line-height: 1.5;
  font-weight: 500;
}

.service-link-medium {
  color: var(--color-primary-light);
  text-decoration: none;
  font-size: 0.9rem;
  font-weight: 600;
  transition: all 0.3s ease;
  display: inline-flex;
  align-items: center;
  gap: 0.3rem;
  width: fit-content;
}

.service-link-medium:hover {
  color: var(--color-primary);
  text-decoration: underline;
  transform: translateX(2px);
}

.service-badge-medium {
  position: absolute;
  top: 0.8rem;
  left: 0.8rem;
  padding: 0.35rem 0.75rem;
  background: rgba(99, 102, 241, 0.8);
  color: #fff;
  font-size: 0.8rem;
  font-weight: 600;
  border-radius: 6px;
  z-index: 2;
  backdrop-filter: blur(8px);
}

.airport-transfer-new {
  position: relative;
}
/* 服务卡片小 */
.service-card-small {
  position: relative;
  display: flex;
  gap: 1rem;
  padding: 1rem;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.15);
  transition: all 0.3s ease;
}

.service-card-small:hover {
  transform: translateX(4px);
  background: rgba(255, 255, 255, 0.08);
  border-color: rgba(99, 102, 241, 0.3);
}

.service-card-small .service-image-placeholder {
  width: 120px;
  height: 100px;
  flex-shrink: 0;
  border-radius: 18px;
}
/* 服务卡片内容 */
.service-card-small .service-content {
  flex: 1;
  padding: 0;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.service-card-small .service-text {
  margin-bottom: 0.5rem;
  font-size: 0.9rem;
}

.service-card-small .service-link {
  font-size: 0.85rem;
}

.airport-transfer {
  position: relative;
}

/* 核心价值主张 */
.value-propositions {
  display: flex;
  flex-direction: column;
  gap: 1.2rem;
  margin: 1.5rem 0;
  padding: 1.5rem;
  background: rgba(15, 23, 42, 0.5);
  border-radius: 24px;
  border: 1px solid rgba(255, 255, 255, 0.12);
  backdrop-filter: blur(16px);
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.2);

}

.value-item {
  display: flex;
  align-items: flex-start;
  gap: 1.2rem;
  padding: 1rem;
  border-radius: 16px;
  transition: all 0.3s ease;
}

.value-item:hover {
  background: rgba(255, 255, 255, 0.05);
  transform: translateX(4px);
}

.value-icon {
  font-size: 2rem;
  flex-shrink: 0;
}

.value-content h3 {
  margin: 0 0 0.5rem 0;
  font-size: 1.2rem;
  color: #fff;
}

.value-content p {
  margin: 0;
  color: rgba(255, 255, 255, 0.75);
  font-size: 0.95rem;
  line-height: 1.6;
}

/* 用户利益点 */
.benefits-section {
  margin: 1.5rem 0;
}

.benefits-section h2 {
  font-size: 1.8rem;
  margin: 0 0 1.5rem 0;
  color: #fff;
  text-align: center;
}

.benefits-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(160px, 1fr));
  gap: 1.2rem;
}
/* 用户利益点卡片 */
.benefit-card {
  padding: 1.8rem;
  background: rgba(15, 23, 42, 0.6);
  border-radius: 20px;
  border: 1px solid rgba(255, 255, 255, 0.12);
  text-align: center;
  transition: all 0.3s ease;
  backdrop-filter: blur(12px);
}

.benefit-card:hover {
  transform: translateY(-6px);
  background: rgba(15, 23, 42, 0.8);
  border-color: rgba(99, 102, 241, 0.5);
  box-shadow: 0 15px 35px rgba(99, 102, 241, 0.3);
}

.benefit-icon {
  font-size: 2.5rem;
  display: block;
  margin-bottom: 0.8rem;
}

.benefit-card h4 {
  margin: 0 0 0.5rem 0;
  font-size: 1rem;
  color: #fff;
}

.benefit-card p {
  margin: 0;
  font-size: 0.85rem;
  color: rgba(255, 255, 255, 0.7);
  line-height: 1.5;
}

/* 使用引导 */
.guide-section {
  margin: 1.5rem 0;
  padding: 2rem;
  background: rgba(15, 23, 42, 0.5);
  border-radius: 24px;
  border: 1px solid rgba(255, 255, 255, 0.12);
  backdrop-filter: blur(16px);
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.2);
}

.guide-section h2 {
  font-size: 1.8rem;
  margin: 0 0 1.5rem 0;
  color: #fff;
  text-align: center;
}

.guide-steps {
  display: flex;
  flex-direction: column;
  gap: 1.2rem;
}

.guide-step {
  display: flex;
  align-items: flex-start;
  gap: 1.5rem;
}
/* 使用引导步骤编号 */
.step-number {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: linear-gradient(135deg, #1E8AE6, #0A2F63);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.5rem;
  font-weight: 700;
  flex-shrink: 0;
  box-shadow: 0 4px 15px rgba(99, 102, 241, 0.4);
}

.step-content h4 {
  margin: 0 0 0.5rem 0;
  font-size: 1.1rem;
  color: #fff;
}

.step-content p {
  margin: 0;
  color: rgba(255, 255, 255, 0.75);
  font-size: 0.95rem;
  line-height: 1.6;
}

.hero-panel h1 {
  font-size: clamp(2.2rem, 4vw, 3.4rem);
  font-weight: 700;
  line-height: 1.2;
  text-align: center;
  margin: 0 auto;
}

.hero-panel p {
  color: rgba(255, 255, 255, 0.75);
  font-size: 1.05rem;
}

.feature-ribbon {
  margin: 1.2rem 0;
  display: flex;
  flex-wrap: wrap;
  gap: 0.6rem;
}
/* 功能栏 */
.feature-ribbon span {
  padding: 0.3rem 0.9rem;
  border-radius: 999px;
  background: rgba(15, 23, 42, 0.6);
  border: 1px solid rgba(255, 255, 255, 0.08);
  font-size: 0.85rem;
  color: rgba(255, 255, 255, 0.75);
}

.hero-badge {
  align-self: center;
  padding: 0.35rem 1rem;
  border-radius: 999px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  background: rgba(129, 140, 248, 0.12);
  font-size: 0.85rem;
  letter-spacing: 0.05em;
}

.hero-stats {
  display: grid;
  grid-template-columns: repeat(3, minmax(120px, 1fr));
  gap: 1.2rem;
  margin: 1rem 0;
}

.stat-card {
  padding: 1.5rem;
  border-radius: 20px;
  border: 1px solid rgba(255, 255, 255, 0.12);
  background: rgba(15, 23, 42, 0.55);
  backdrop-filter: blur(16px);
  box-shadow: 0 20px 40px rgba(15, 23, 42, 0.4);
  transition: all 0.3s ease;
}
/* 统计数据悬浮效果 */
.stat-card:hover {
  transform: translateY(-4px);
  border-color: rgba(99, 102, 241, 0.3);
  box-shadow: 0 25px 50px rgba(99, 102, 241, 0.25);
}
/* 统计数据 */
.stat-value {
  display: block;
  font-size: 2rem;
  font-weight: 700;
  background: linear-gradient(135deg, #fff, rgba(255, 255, 255, 0.85));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin-bottom: 0.5rem;
}

.stat-label {
  color: rgba(255, 255, 255, 0.75);
  font-size: 0.95rem;
  font-weight: 500;
}

.trust-strip {
  margin-top: 1rem;
  display: flex;
  align-items: center;
  gap: 1rem;
  color: rgba(255, 255, 255, 0.6);
  font-size: 0.9rem;
}

.trust-dots {
  display: flex;
  gap: 0.5rem;
}

.trust-dots i {
  width: 36px;
  height: 12px;
  border-radius: 999px;
  background: linear-gradient(120deg, rgba(248, 113, 113, 0.6), rgba(129, 140, 248, 0.6));
  opacity: 0.5;
}
/* 登录面板 */
.form-panel {
  position: relative;
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
  position: sticky;
  top: 2rem;
  align-self: start;
  width: 100%;
  max-width: 100%;
  margin: 0 auto;
  box-sizing: border-box;
  flex-shrink: 0;
  
}

/* 登录卡片 */
.login-card {
  background: rgba(103, 159, 211, 0.95);
  padding: clamp(2rem, 5vw, 3rem);
  border-radius: 32px;
  border: 2px solid var(--color-primary);
  box-shadow:
    0 25px 60px rgba(16, 106, 141, 0.25),
    inset 0 1px rgba(122, 122, 142, 0.5);
  backdrop-filter: blur(18px);
  min-height: 420px;
  height: 680px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  transition: all var(--transition-base);
}
/* 登录卡片悬浮效果 */
.login-card:hover {
  box-shadow:
    0 35px 70px rgba(135, 206, 235, 0.35),
    inset 0 1px rgba(255, 255, 255, 0.5);
  transform: translateY(-2px);
}
/* 登录卡片logo区域 */
.logo-section {
  display: flex;
  align-items: center;
  gap: 1rem;
  margin-bottom: 2rem;
}

.logo-emblem {
  width: 56px;
  height: 56px;
  border-radius: 16px;
  background: rgba(99, 102, 241, 0.2);
  display: grid;
  place-items: center;
  font-size: 1.6rem;
  border: 1px solid rgba(255, 255, 255, 0.15);
}

.logo-section h2 {
  margin: 0;
  font-size: 1.4rem;
}

.logo-section p {
  color: rgba(255, 255, 255, 0.6);
  margin-top: 0.2rem;
}

.form-group {
  margin-bottom: 1.2rem;
}
/* 登录卡片表单组标签 */
.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  font-size: 0.95rem;
  color: var(--color-text-primary);
  font-weight: 500;
}
/* 登录卡片表单组输入框 */
.input-shell {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.8rem 1.1rem;
  border-radius: 16px;
  border: 1px solid var(--color-border-light);
  background: var(--color-bg-primary);
  box-shadow: inset 0 1px rgba(17, 163, 221, 0.1);
  transition: all var(--transition-base);
}

.input-shell:hover {
  border-color: var(--color-primary);
  box-shadow: inset 0 1px rgba(135, 206, 235, 0.2);
}

.input-shell:focus-within {
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px rgba(135, 206, 235, 0.1), inset 0 1px rgba(135, 206, 235, 0.2);
}
/* 登录卡片表单组输入框输入框 */
.input-shell input {
  width: 100%;
  background: transparent;
  border: none;
  color: var(--color-text-primary);
  font-size: 0.95rem;
  outline: none;
}

.input-shell input::placeholder {
  color: var(--color-text-tertiary);
}

.input-shell .role-select {
  width: 100%;
  background: transparent;
  border: none;
  color: var(--color-text-primary);
  font-size: 0.95rem;
  outline: none;
  cursor: pointer;
}

.input-icon {
  font-size: 1rem;
  opacity: 0.7;
}
/* 登录卡片表单组选项 */
.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
  font-size: 0.9rem;
  color: rgba(255, 255, 255, 0.7);
}

.remember-me {
  display: flex;
  gap: 0.4rem;
  align-items: center;
  cursor: pointer;
}

.forgot-password {
  color: var(--color-primary-light);
  text-decoration: none;
  font-weight: 500;
}

.forgot-password:hover {
  text-decoration: underline;
}
/* 登录卡片表单组按钮 */
.login-btn {
  width: 100%;
  padding: 1rem;
  border-radius: 16px;
  border: none;
  background: var(--gradient-primary);
  color: var(--color-text-inverse);
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  box-shadow: var(--shadow-lg);
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.login-btn:hover:not(:disabled) {
  transform: translateY(-3px);
  box-shadow: 0 30px 45px rgba(99, 102, 241, 0.45);
}

.login-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.register-section {
  margin-top: 1.5rem;
  text-align: center;
  color: var(--color-text-secondary);
  transition: all var(--transition-base);
}

.link-btn {
  border: none;
  background: none;
  color: var(--color-primary-dark);
  cursor: pointer;
  font-weight: 600;
  padding: 0;
}

.link-btn:hover {
  text-decoration: underline;
}
/* 登录卡片表单组错误消息 */
.error-message {
  color: var(--color-error);
  background: var(--color-error-light);
  border: 1px solid var(--color-error);
  border-radius: 14px;
  padding: 0.75rem 1rem;
  font-size: 0.85rem;
  margin-bottom: 1rem;
  animation: slideDown var(--transition-base);
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

.mode-switch {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 0.5rem;
  margin-bottom: 1.5rem;
  background: var(--color-bg-secondary);
  padding: 0.3rem;
  border-radius: 999px;
  border: 1px solid var(--color-border-light);
  transition: all var(--transition-base);
}

.mode-btn {
  border: none;
  border-radius: 999px;
  padding: 0.5rem 0;
  background: transparent;
  color: var(--color-text-secondary);
  font-weight: 600;
  cursor: pointer;
  transition: all var(--transition-base);
}

.mode-btn:hover {
  color: var(--color-primary);
}

.mode-btn.active {
  background: var(--gradient-primary);
  color: var(--color-text-inverse);
  box-shadow: 0 4px 12px rgba(135, 206, 235, 0.3);
}

.register-form {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 0.8rem;
}

.form-grid label {
  display: flex;
  flex-direction: column;
  gap: 0.4rem;
  font-size: 0.9rem;
  color: var(--color-text-primary);
  font-weight: 500;
}

.form-grid input {
  padding: 0.7rem 0.9rem;
  border-radius: 12px;
  border: 1px solid var(--color-border-light);
  background: var(--color-bg-primary);
  color: var(--color-text-primary);
  transition: all var(--transition-base);
}

.form-grid input:hover {
  border-color: var(--color-primary);
}

.form-grid input:focus {
  outline: none;
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px rgba(135, 206, 235, 0.1);
}

.form-grid .role-select {
  padding: 0.7rem 0.9rem;
  border-radius: 12px;
  border: 1px solid var(--color-border-light);
  background: var(--color-bg-primary);
  color: var(--color-text-primary);
  font-size: 0.9rem;
  cursor: pointer;
  transition: all var(--transition-base);
}

.form-grid .role-select:hover {
  border-color: var(--color-primary);
}

.form-grid .role-select:focus {
  outline: none;
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px rgba(135, 206, 235, 0.1);
}

.field-error {
  font-size: 0.75rem;
  color: var(--color-error);
  animation: slideDown var(--transition-fast);
}

.terms-check {
  display: flex;
  align-items: center;
  gap: 0.4rem;
  font-size: 0.85rem;
  color: var(--color-text-secondary);
  transition: all var(--transition-base);
}

.terms-check:hover {
  color: var(--color-text-primary);
}

.terms-check a {
  color: var(--color-primary-light);
  text-decoration: none;
}

.terms-check a:hover {
  text-decoration: underline;
}

@keyframes float {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-20px);
  }
}

@media (max-width: 1200px) {
  .content-wrapper {
    grid-template-columns: 1fr;
    gap: 3rem;
    width: 100%;
    max-width: 100vw;
    padding: 4rem clamp(1rem, 2vw, 2rem) 3rem;
    margin: 0 auto;
  }
  
  .form-panel {
    position: static;
  }

  .hero-panel {
    max-width: 100%;
  }

  .hotel-content {
    grid-template-columns: 1fr;
    max-width: 100%;
  }

  .services-grid {
    grid-template-columns: 1fr;
    max-width: 100%;
  }

  .news-grid {
    max-width: 100%;
  }

  .carousel-section {
    max-width: 100%;
  }

  .flight-deals-section,
  .hotel-section,
  .services-section,
  .news-section {
    max-width: 100%;
  }
}

@media (max-width: 900px) {
  .content-wrapper {
    padding: 4rem clamp(1rem, 3vw, 1.5rem) 4rem;
    width: 100%;
    max-width: 100vw;
    margin: 0 auto;
  }
  
  .landing-nav {
    padding: 1.5rem 1.5rem 0;
  }

  .landing-nav {
    flex-direction: column;
    gap: 1rem;
  }

  .hero-panel {
    max-width: none;
  }

  .carousel-section {
    height: 400px;
  }

  .benefits-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .guide-steps {
    gap: 1.2rem;
  }

  .step-number {
    width: 40px;
    height: 40px;
    font-size: 1.2rem;
  }

  .deal-card-group {
    justify-content: flex-start;
    overflow-x: auto;
    padding-bottom: 0.5rem;
    scroll-snap-type: x proximity;
  }

  .deal-card {
    width: 220px;
    padding-right: 0;
    scroll-snap-align: start;
  }

  .deal-card.featured {
    padding-right: 0;
  }

  .deal-featured-panel {
    position: static;
    transform: none;
    width: 100%;
    margin-top: 1rem;
  }

  .deal-ellipse {
    width: 200px;
    height: 320px;
  }

  .hotel-images-grid {
    grid-template-columns: repeat(auto-fit, minmax(140px, 1fr));
  }

  .hotel-image-card {
    min-height: 140px;
  }

  .service-card-small {
    flex-direction: column;
  }

  .service-card-small .service-image-placeholder {
    width: 100%;
    height: 120px;
  }
}

@media (max-width: 600px) {
  .content-wrapper {
    padding: 3.5rem clamp(0.75rem, 2vw, 1rem) 3rem;
    width: 100%;
    max-width: 100vw;
    margin: 0 auto;
  }
  
  .landing-nav {
    padding: 1rem 1rem 0;
  }
  
  .carousel-section {
    height: clamp(180px, 25vh, 200px);
  }

  .benefits-grid {
    grid-template-columns: 1fr;
  }

  .value-propositions {
    padding: 1rem;
  }

  .guide-section {
    padding: 1.5rem;
  }
}
/* 账号信息区域 */
.account-info {
  background: linear-gradient(135deg, rgba(24, 12, 163, 0.95), rgba(240, 248, 255, 0.9));
  border: 2px solid var(--color-primary);
  border-radius: 20px;
  padding: 1.5rem;
  margin: 1rem auto;
  max-width: 800px;
  backdrop-filter: blur(16px);
  box-shadow: 0 15px 35px rgba(42, 167, 216, 0.2);
  text-align: center;
  transition: all var(--transition-base);
}

.account-info:hover {
  box-shadow: 0 20px 45px rgba(135, 206, 235, 0.3);
  transform: translateY(-2px);
}

.account-info h3 {
  margin-top: 0;
  color: var(--color-primary);
  font-size: 1.2rem;
  font-weight: 700;
}

.account-info ul {
  list-style: none;
  padding: 0;
  margin: 10px 0 0;
}

.account-info li {
  padding: 8px 0;
  color: var(--color-text-primary);
  border-bottom: 1px solid rgba(135, 206, 235, 0.1);
  font-size: 0.9rem;
}

.account-info li:last-child {
  border-bottom: none;
}

.account-info strong {
  color: var(--color-primary-dark);
}

/* 系统资讯区域 */
.news-section {
  margin: 2rem 0;
  padding: 1.25rem; /* 更紧凑的内边距 */
  background: rgba(15, 23, 42, 0.6);
  border-radius: 20px;
  border: 1px solid rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(18px);
  width: 1500px; /* 与特价活动一致 */
  box-sizing: border-box;
  box-shadow: 0 16px 32px rgba(0, 0, 0, 0.25);
  transition: all 0.3s ease;
}

.news-section:hover {
  border-color: rgba(99, 102, 241, 0.3);
  box-shadow: 0 22px 44px rgba(99, 102, 241, 0.2);
}

.news-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 1rem; /* 缩小间距，更紧凑 */
  margin: 1rem auto 0;
  width: 100%;
  max-width: 1400px;
  box-sizing: border-box;
  justify-items: stretch;
}

.news-card {
  background: rgba(255, 255, 255, 0.07);
  border: 1px solid rgba(255, 255, 255, 0.18);
  border-radius: 16px; /* 更小的圆角 */
  overflow: hidden;
  transition: all 0.25s ease;
  cursor: pointer;
  width: 100%;
  display: flex;
  flex-direction: column;
  box-shadow: 0 3px 10px rgba(0, 0, 0, 0.18);
}

.news-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 14px 28px rgba(99, 102, 241, 0.35);
  border-color: rgba(99, 102, 241, 0.45);
  background: rgba(255, 255, 255, 0.1);
}

.news-image {
  width: 100%;
  height: 110px; /* 再微调更紧凑 */
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
  flex-shrink: 0;
}

.news-icon {
  font-size: 2.2rem; /* 缩小图标 */
  opacity: 0.9;
}

.news-content {
  padding: 1rem;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.news-content h3 {
  margin: 0 0 0.35rem 0;
  font-size: 1rem; /* 更小标题 */
  color: #fff;
  font-weight: 700;
  line-height: 1.25;
  min-height: auto;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.news-content p {
  margin: 0 0 0.5rem 0;
  color: rgba(255, 255, 255, 0.78);
  font-size: 0.85rem; /* 更小正文字号 */
  line-height: 1.5;
  flex: 1;
  min-height: auto;
  display: -webkit-box;
  -webkit-line-clamp: 2; /* 最多两行显示 */
  -webkit-box-orient: vertical;
  line-clamp: 2;
  overflow: hidden;
}

.news-date {
  display: inline-block;
  font-size: 0.75rem; /* 更小日期字号 */
  color: rgba(255, 255, 255, 0.6);
  padding: 0.28rem 0.55rem;
  background: rgba(15, 23, 42, 0.65);
  border-radius: 8px;
}

/* 系统资讯标题区域更紧凑 */
.news-section .section-title-with-icon {
  margin-bottom: 0.75rem;
}
.news-section .section-title-with-icon h2 {
  font-size: 1.25rem;
}
.news-section .section-icon {
  font-size: 1.2rem;
}

/* 小屏宽度自适应 */
@media (max-width: 1200px) {
  .news-section { width: 100%; }
}

@media (max-width: 1200px) {
  .news-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 1.2rem;
  }

  .services-grid-new {
    grid-template-columns: 1fr;
    gap: 1.5rem;
  }

  .services-column-new {
    flex-direction: row;
    gap: 1rem;
  }

  .service-card-medium {
    flex: 1;
  }
}

@media (max-width: 900px) {
  .carousel-section {
    height: 350px;
  }

  .login-card {
    min-height: 350px;
  }

  .news-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 1rem;
  }
}

@media (max-width: 600px) {
  .carousel-section {
    height: 280px;
  }

  .login-card {
    min-height: 280px;
  }

  .news-grid {
    grid-template-columns: 1fr;
    gap: 1rem;
  }

  .services-column-new {
    flex-direction: column;
    gap: 1rem;
  }

  .service-image-large {
    height: 180px;
  }

  .service-image-medium {
    height: 120px;
  }
}

/* 返回顶部按钮 */
.back-to-top-btn {
  position: fixed;
  bottom: 2rem;
  right: 2rem;
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background: var(--gradient-primary);
  border: 2px solid rgba(255, 255, 255, 0.2);
  color: var(--color-text-inverse);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: var(--shadow-primary);
  transition: all 0.3s ease;
  z-index: 1000;
  opacity: 0;
  transform: translateY(20px);
  pointer-events: none;
}

.back-to-top-btn.show {
  opacity: 1;
  transform: translateY(0);
  pointer-events: auto;
}

.back-to-top-btn:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 32px rgba(135, 206, 235, 0.5);
  background: linear-gradient(135deg, var(--color-primary-dark), var(--color-primary-light));
}

.back-to-top-btn:active {
  transform: translateY(-2px);
}

.back-to-top-btn svg {
  width: 24px;
  height: 24px;
  stroke-width: 2.5;
}

/* 返回底部按钮 */
.back-to-bottom-btn {
  position: fixed;
  top: 2rem;
  right: 2rem;
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background: var(--gradient-primary);
  border: 2px solid rgba(255, 255, 255, 0.2);
  color: var(--color-text-inverse);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: var(--shadow-primary);
  transition: all 0.3s ease;
  z-index: 1000;
  opacity: 1;
  transform: translateY(0);
  pointer-events: auto;
  margin-top:140px;
}

.back-to-bottom-btn:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 32px rgba(135, 206, 235, 0.5);
  background: linear-gradient(135deg, var(--color-primary-dark), var(--color-primary-light));
}

.back-to-bottom-btn:active {
  transform: translateY(-2px);
}

.back-to-bottom-btn svg {
  width: 24px;
  height: 24px;
  stroke-width: 2.5;
}

/* 移动端优化 */
@media (max-width: 768px) {
  .back-to-top-btn {
    width: 48px;
    height: 48px;
    bottom: 1.5rem;
    right: 1.5rem;
  }
  
  .back-to-top-btn svg {
    width: 20px;
    height: 20px;
  }

  .back-to-bottom-btn {
    width: 48px;
    height: 48px;
    top: 1.5rem;
    right: 1.5rem;
  }
  
  .back-to-bottom-btn svg {
    width: 20px;
    height: 20px;
  }
}

/* 优化服务卡片图片 */
.service-image-placeholder {
  position: relative;
  overflow: hidden;
}

.service-image-placeholder::before {
  content: '';
  position: absolute;
  inset: 0;
  background-size: cover;
  background-position: center;
  opacity: 0.8;
  transition: opacity 0.3s ease, transform 0.3s ease;
}

.service-card-large:hover .service-image-placeholder::before,
.service-card-small:hover .service-image-placeholder::before {
  opacity: 1;
  transform: scale(1.05);
}

.service-image-placeholder.hotel-room::before {
  background-image: url("data:image/svg+xml,%3Csvg width='400' height='300' xmlns='http://www.w3.org/2000/svg'%3E%3Cdefs%3E%3ClinearGradient id='hotel' x1='0%25' y1='0%25' x2='100%25' y2='100%25'%3E%3Cstop offset='0%25' style='stop-color:%230ea5e9;stop-opacity:1' /%3E%3Cstop offset='100%25' style='stop-color:%233b82f6;stop-opacity:1' /%3E%3C/linearGradient%3E%3C/defs%3E%3Crect width='100%25' height='100%25' fill='url(%23hotel)'/%3E%3Crect x='80' y='80' width='240' height='140' fill='rgba(255,255,255,0.25)' rx='12'/%3E%3Crect x='100' y='100' width='50' height='50' fill='rgba(255,255,255,0.35)' rx='6'/%3E%3Crect x='170' y='100' width='50' height='50' fill='rgba(255,255,255,0.35)' rx='6'/%3E%3Crect x='240' y='100' width='50' height='50' fill='rgba(255,255,255,0.35)' rx='6'/%3E%3C/svg%3E");
}

.service-image-placeholder.train::before {
  background-image: url("data:image/svg+xml,%3Csvg width='300' height='200' xmlns='http://www.w3.org/2000/svg'%3E%3Cdefs%3E%3ClinearGradient id='train' x1='0%25' y1='0%25' x2='100%25' y2='100%25'%3E%3Cstop offset='0%25' style='stop-color:%2322c55e;stop-opacity:1' /%3E%3Cstop offset='100%25' style='stop-color:%2316a34a;stop-opacity:1' /%3E%3C/linearGradient%3E%3C/defs%3E%3Crect width='100%25' height='100%25' fill='url(%23train)'/%3E%3Crect x='50' y='80' width='200' height='60' fill='rgba(255,255,255,0.3)' rx='8'/%3E%3Ccircle cx='70' cy='140' r='15' fill='rgba(255,255,255,0.4)'/%3E%3Ccircle cx='230' cy='140' r='15' fill='rgba(255,255,255,0.4)'/%3E%3C/svg%3E");
}

.service-image-placeholder.bus::before {
  background-image: url("data:image/svg+xml,%3Csvg width='300' height='200' xmlns='http://www.w3.org/2000/svg'%3E%3Cdefs%3E%3ClinearGradient id='bus' x1='0%25' y1='0%25' x2='100%25' y2='100%25'%3E%3Cstop offset='0%25' style='stop-color:%23fbbf24;stop-opacity:1' /%3E%3Cstop offset='100%25' style='stop-color:%23f59e0b;stop-opacity:1' /%3E%3C/linearGradient%3E%3C/defs%3E%3Crect width='100%25' height='100%25' fill='url(%23bus)'/%3E%3Crect x='60' y='70' width='180' height='80' fill='rgba(255,255,255,0.3)' rx='10'/%3E%3Crect x='80' y='85' width='30' height='30' fill='rgba(255,255,255,0.4)' rx='4'/%3E%3Crect x='120' y='85' width='30' height='30' fill='rgba(255,255,255,0.4)' rx='4'/%3E%3Ccircle cx='90' cy='160' r='12' fill='rgba(255,255,255,0.4)'/%3E%3Ccircle cx='210' cy='160' r='12' fill='rgba(255,255,255,0.4)'/%3E%3C/svg%3E");
}

.service-image-placeholder.transfer::before {
  background-image: url("data:image/svg+xml,%3Csvg width='300' height='200' xmlns='http://www.w3.org/2000/svg'%3E%3Cdefs%3E%3ClinearGradient id='transfer' x1='0%25' y1='0%25' x2='100%25' y2='100%25'%3E%3Cstop offset='0%25' style='stop-color:%238b5cf6;stop-opacity:1' /%3E%3Cstop offset='100%25' style='stop-color:%237c3aed;stop-opacity:1' /%3E%3C/linearGradient%3E%3C/defs%3E%3Crect width='100%25' height='100%25' fill='url(%23transfer)'/%3E%3Crect x='70' y='75' width='160' height='70' fill='rgba(255,255,255,0.3)' rx='10'/%3E%3Ccircle cx='100' cy='155' r='14' fill='rgba(255,255,255,0.4)'/%3E%3Ccircle cx='200' cy='155' r='14' fill='rgba(255,255,255,0.4)'/%3E%3Cpath d='M150 100 L150 120 M140 110 L150 120 L160 110' stroke='rgba(255,255,255,0.5)' stroke-width='3' fill='none'/%3E%3C/svg%3E");
}

</style>


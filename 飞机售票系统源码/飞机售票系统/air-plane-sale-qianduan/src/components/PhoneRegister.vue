<template>
  <div class="register-page">
    <div class="register-container">
      <div class="register-card">
        <div class="header-section">
          <h2>创建账号</h2>
          <p class="subtitle">选择注册方式，开启您的旅程</p>
        </div>

        <!-- 注册方式选择 -->
        <div class="method-selector">
          <button
            type="button"
            :class="['method-btn', { active: registerMethod === 'phone' }]"
            @click="registerMethod = 'phone'"
          >
            📱 手机号注册
          </button>
          <button
            type="button"
            :class="['method-btn', { active: registerMethod === 'email' }]"
            @click="registerMethod = 'email'"
          >
            ✉️ 邮箱注册
          </button>
        </div>

        <form @submit.prevent="handleSubmit" class="register-form">
          <!-- 手机号/邮箱输入 -->
          <div class="input-group">
            <label class="input-label has-icon">
              <span class="label-icon">{{ registerMethod === 'phone' ? '📱' : '✉️' }}</span>
              <span class="label-text">{{ registerMethod === 'phone' ? '手机号' : '邮箱地址' }}</span>
              <input
                :type="registerMethod === 'phone' ? 'tel' : 'email'"
                v-model="form.contact"
                :placeholder="registerMethod === 'phone' ? '请输入手机号' : '请输入邮箱地址'"
                required
                class="modern-input"
              />
            </label>
          </div>

          <!-- 验证码输入 -->
          <div class="input-group">
            <label class="input-label has-icon">
              <span class="label-icon">🔒</span>
              <span class="label-text">验证码</span>
              <div class="code-input-row">
                <input
                  type="text"
                  v-model="form.smsCode"
                  :placeholder="registerMethod === 'phone' ? '短信验证码' : '邮箱验证码'"
                  required
                  class="code-input modern-input"
                />
                <button
                  type="button"
                  class="send-code-btn"
                  :disabled="sending || countdown > 0"
                  @click="sendCode"
                >
                  <span v-if="countdown === 0">{{ sending ? '发送中...' : '发送验证码' }}</span>
                  <span v-else>{{ countdown }}s</span>
                </button>
              </div>
            </label>
          </div>

          <!-- 密码输入 -->
          <div class="input-group">
            <label class="input-label has-icon">
              <span class="label-icon">🔑</span>
              <span class="label-text">登录密码</span>
              <input
                type="password"
                v-model="form.password"
                placeholder="请输入登录密码"
                required
                class="modern-input"
              />
            </label>
          </div>

          <!-- 确认密码 -->
          <div class="input-group">
            <label class="input-label has-icon">
              <span class="label-icon">🔒</span>
              <span class="label-text">确认密码</span>
              <input
                type="password"
                v-model="form.confirmPassword"
                placeholder="请再次输入密码"
                required
                class="modern-input"
              />
            </label>
          </div>

          <!-- 邮箱注册额外字段 -->
          <div v-if="registerMethod === 'email'">
            <!-- 用户名输入 -->
            <div class="input-group">
              <label class="input-label has-icon">
                <span class="label-icon">👤</span>
                <span class="label-text">用户名</span>
                <input
                  type="text"
                  v-model="form.username"
                  placeholder="请输入用户名（至少3位）"
                  required
                  class="modern-input"
                />
              </label>
            </div>
          </div>

          <!-- 协议同意 -->
          <div class="terms-group">
            <label class="terms-checkbox">
              <input type="checkbox" v-model="agreed" class="checkbox-input" />
              <span class="checkmark"></span>
              <span class="terms-text">
                我已阅读并同意
                <a href="#" class="terms-link">《用户协议》</a>与
                <a href="#" class="terms-link">《隐私政策》</a>
              </span>
            </label>
          </div>

          <!-- 错误提示 -->
          <div v-if="error" class="error-message">
            <span class="error-icon">⚠️</span>
            {{ error }}
          </div>

          <!-- 提交按钮 -->
          <button type="submit" class="submit-btn" :disabled="loading || !agreed">
            <span class="btn-icon">{{ loading ? '⏳' : '🚀' }}</span>
            {{ loading ? '注册中...' : '立即注册' }}
          </button>
        </form>

        <!-- 登录链接 -->
        <div class="login-link">
          已有账号？
          <button class="login-link-text" @click="router.push('/')">立即登录</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { authApi } from '../services/api'

const router = useRouter()

// 注册方式：phone 或 email
const registerMethod = ref<'phone' | 'email'>('phone')

const form = reactive({
  contact: '', // 手机号或邮箱
  smsCode: '',
  password: '',
  confirmPassword: '',
  username: '' // 邮箱注册时需要用户名
})

const loading = ref(false)
const sending = ref(false)
const error = ref('')
const agreed = ref(false)
const countdown = ref(0)
let timer: number | null = null

const startCountdown = (sec = 60) => {
  countdown.value = sec
  timer && clearInterval(timer)
  timer = window.setInterval(() => {
    if (countdown.value > 0) {
      countdown.value -= 1
    } else {
      timer && clearInterval(timer)
      timer = null
    }
  }, 1000)
}

// 验证联系方式（手机号或邮箱）
const validateContact = () => {
  if (registerMethod.value === 'phone') {
    return /^1[3-9]\d{9}$/.test(form.contact)
  } else {
    return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.contact)
  }
}

const sendCode = async () => {
  if (!validateContact()) {
    error.value = registerMethod.value === 'phone' ? '请输入有效手机号' : '请输入有效邮箱地址'
    return
  }

  sending.value = true
  error.value = ''

  try {
    if (registerMethod.value === 'phone') {
      await authApi.sendSms(form.contact)
    } else {
      await authApi.sendEmailCode(form.contact)
    }
    startCountdown(60)
  } catch (e: any) {
    error.value = e.message || `发送${registerMethod.value === 'phone' ? '短信' : '邮件'}验证码失败`
  } finally {
    sending.value = false
  }
}

const validate = () => {
  if (!validateContact()) {
    error.value = registerMethod.value === 'phone' ? '请输入有效手机号' : '请输入有效邮箱地址'
    return false
  }

  if (!form.smsCode) {
    error.value = `请输入${registerMethod.value === 'phone' ? '短信' : '邮箱'}验证码`
    return false
  }

  if (!form.password) {
    error.value = '请输入登录密码'
    return false
  }

  if (form.password.length < 6) {
    error.value = '密码长度至少6位'
    return false
  }

  if (form.password !== form.confirmPassword) {
    error.value = '两次输入的密码不一致'
    return false
  }

  if (registerMethod.value === 'email') {
    if (!form.username) {
      error.value = '请输入用户名'
      return false
    }
    if (form.username.length < 3) {
      error.value = '用户名长度至少3位'
      return false
    }
  }

  if (!agreed.value) {
    error.value = '请同意用户协议'
    return false
  }

  return true
}

const handleSubmit = async () => {
  if (!validate()) return

  loading.value = true
  error.value = ''

  try {
    if (registerMethod.value === 'phone') {
      // 手机号注册：使用手机号作为用户名，密码为用户输入的密码
      await authApi.registerByPhone(form.contact, form.smsCode, form.password)
    } else {
      // 邮箱注册
      if (!form.username) {
        error.value = '请输入用户名'
        return
      }
      if (form.username.length < 3) {
        error.value = '用户名长度至少3位'
        return
      }
      const result = await authApi.registerByEmail({
        email: form.contact,
        emailCode: form.smsCode,
        username: form.username,
        password: form.password
      })

      // 保存注册邮箱到localStorage，用于登录时自动填充
      localStorage.setItem('registered_email', form.contact)
    }

    // 注册成功，跳转到首页
    router.push('/')
  } catch (e: any) {
    error.value = e.message || '注册失败'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.register-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 50%, #ec4899 100%);
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 2rem 1rem;
  position: relative;
  overflow: hidden;
}

.register-page::before {
  content: '';
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(circle at 30% 50%, rgba(139, 92, 246, 0.3) 0%, transparent 50%),
              radial-gradient(circle at 70% 80%, rgba(236, 72, 153, 0.3) 0%, transparent 50%);
  animation: gradientShift 15s ease infinite;
  pointer-events: none;
}

@keyframes gradientShift {
  0%, 100% { transform: translate(0, 0) rotate(0deg); }
  50% { transform: translate(5%, 5%) rotate(180deg); }
}

.register-page::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: url('data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100"><defs><pattern id="dots" width="10" height="10" patternUnits="userSpaceOnUse"><circle cx="2" cy="2" r="1" fill="%23ffffff" opacity="0.1"/></pattern></defs><rect width="100" height="100" fill="url(%23dots)"/></svg>');
  pointer-events: none;
}

.register-container {
  width: 100%;
  max-width: 700px;
  position: relative;
  z-index: 1;
}

.register-card {
  background: linear-gradient(135deg, 
    rgba(255, 255, 255, 0.95) 0%, 
    rgba(248, 250, 252, 0.95) 100%);
  backdrop-filter: blur(30px) saturate(180%);
  border-radius: 32px;
  padding: 3rem 2.75rem;
  box-shadow: 
    0 32px 80px rgba(0, 0, 0, 0.25),
    0 16px 40px rgba(0, 0, 0, 0.15),
    0 0 0 1px rgba(255, 255, 255, 0.5),
    inset 0 2px 0 rgba(255, 255, 255, 0.9);
  border: 2px solid rgba(255, 255, 255, 0.4);
  animation: slideIn 0.7s cubic-bezier(0.34, 1.56, 0.64, 1);
  position: relative;
  overflow: hidden;
}

.register-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: linear-gradient(90deg, 
    transparent 0%, 
    rgba(99, 102, 241, 0.8) 30%,
    rgba(139, 92, 246, 0.8) 50%,
    rgba(236, 72, 153, 0.8) 70%,
    transparent 100%);
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateY(40px) scale(0.95);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.header-section {
  text-align: center;
  margin-bottom: 2.5rem;
}

.header-section h2 {
  font-size: 2.25rem;
  font-weight: 800;
  color: #0f172a;
  margin: 0 0 0.75rem 0;
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 50%, #ec4899 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  letter-spacing: -0.5px;
}

.subtitle {
  color: #64748b;
  font-size: 1.05rem;
  margin: 0;
  font-weight: 500;
  letter-spacing: 0.2px;
}

/* 注册方式选择器 */
.method-selector {
  display: flex;
  gap: 0.625rem;
  margin-bottom: 2.25rem;
  background: linear-gradient(135deg, #f1f5f9 0%, #e2e8f0 100%);
  border-radius: 16px;
  padding: 0.375rem;
  box-shadow: inset 0 2px 4px rgba(0, 0, 0, 0.06);
}

.method-btn {
  flex: 1;
  padding: 0.875rem 1.25rem;
  border: none;
  border-radius: 12px;
  background: transparent;
  color: #64748b;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.35s cubic-bezier(0.4, 0, 0.2, 1);
  font-size: 0.975rem;
  position: relative;
  overflow: hidden;
}

.method-btn::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  width: 0;
  height: 0;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.3);
  transform: translate(-50%, -50%);
  transition: width 0.6s, height 0.6s;
}

.method-btn:hover::before {
  width: 300px;
  height: 300px;
}

.method-btn:hover:not(.active) {
  background: rgba(255, 255, 255, 0.9);
  color: #475569;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.method-btn.active {
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%);
  color: white;
  box-shadow: 
    0 6px 20px rgba(99, 102, 241, 0.4),
    0 0 0 1px rgba(255, 255, 255, 0.2) inset;
  transform: translateY(-2px);
}

.register-form {
  display: flex;
  flex-direction: column;
  gap: 1.75rem;
}

.input-group {
  position: relative;
}

.input-label {
  display: block;
  font-size: 0.975rem;
  color: #1e293b;
  font-weight: 600;
}

.input-label.has-icon {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 0.625rem;
  margin-bottom: 0.625rem;
}

.label-icon {
  font-size: 1.3rem;
  min-width: 1.3rem;
  filter: drop-shadow(0 1px 2px rgba(0, 0, 0, 0.1));
}

.label-text {
  flex: 0 0 auto;
  white-space: nowrap;
}

.modern-input {
  width: 100%;
  padding: 1.125rem 1.375rem;
  border: 2px solid #e2e8f0;
  border-radius: 14px;
  font-size: 1.025rem;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  background: #ffffff;
  color: #1e293b;
  font-weight: 500;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.modern-input::placeholder {
  color: #94a3b8;
  font-weight: 400;
}

.modern-input:hover {
  border-color: #cbd5e1;
}

.modern-input:focus {
  outline: none;
  border-color: #6366f1;
  box-shadow: 
    0 0 0 4px rgba(99, 102, 241, 0.12),
    0 4px 12px rgba(99, 102, 241, 0.15);
  transform: translateY(-1px);
}

.code-input-row {
  display: flex;
  gap: 0.875rem;
}

.code-input {
  flex: 1;
}

.send-code-btn {
  padding: 1.125rem 1.5rem;
  border: none;
  border-radius: 14px;
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%);
  color: white;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  min-width: 130px;
  white-space: nowrap;
  box-shadow: 
    0 4px 14px rgba(99, 102, 241, 0.35),
    0 0 0 1px rgba(255, 255, 255, 0.2) inset;
  position: relative;
  overflow: hidden;
}

.send-code-btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.3), transparent);
  transition: left 0.5s;
}

.send-code-btn:hover:not(:disabled)::before {
  left: 100%;
}

.send-code-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 
    0 8px 24px rgba(99, 102, 241, 0.45),
    0 0 0 1px rgba(255, 255, 255, 0.3) inset;
}

.send-code-btn:active:not(:disabled) {
  transform: translateY(0);
}

.send-code-btn:disabled {
  opacity: 0.65;
  cursor: not-allowed;
  transform: none;
  box-shadow: 0 2px 8px rgba(99, 102, 241, 0.2);
}

/* 协议同意 */
.terms-group {
  margin: 0.5rem 0 0.25rem;
}

.terms-checkbox {
  display: flex;
  align-items: flex-start;
  gap: 0.875rem;
  cursor: pointer;
  font-size: 0.925rem;
  line-height: 1.6;
  color: #64748b;
  font-weight: 500;
}

.checkbox-input {
  position: absolute;
  opacity: 0;
  cursor: pointer;
}

.checkmark {
  width: 22px;
  height: 22px;
  border: 2.5px solid #cbd5e1;
  border-radius: 6px;
  position: relative;
  flex-shrink: 0;
  margin-top: 2px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  background: #ffffff;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
}

.checkbox-input:checked ~ .checkmark {
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%);
  border-color: #6366f1;
  box-shadow: 
    0 4px 12px rgba(99, 102, 241, 0.3),
    0 0 0 1px rgba(255, 255, 255, 0.2) inset;
  transform: scale(1.05);
}

.checkbox-input:checked ~ .checkmark::after {
  content: '✓';
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  color: white;
  font-size: 13px;
  font-weight: bold;
}

.terms-link {
  color: #6366f1;
  text-decoration: none;
  font-weight: 600;
  transition: all 0.2s;
}

.terms-link:hover {
  color: #8b5cf6;
  text-decoration: underline;
}

/* 错误提示 */
.error-message {
  display: flex;
  align-items: center;
  gap: 0.625rem;
  padding: 0.875rem 1.125rem;
  background: linear-gradient(135deg, #fef2f2 0%, #fee2e2 100%);
  border: 2px solid #fecaca;
  border-radius: 12px;
  color: #dc2626;
  font-size: 0.925rem;
  font-weight: 600;
  animation: shake 0.5s ease-in-out;
  box-shadow: 0 4px 12px rgba(220, 38, 38, 0.15);
}

@keyframes shake {
  0%, 100% { transform: translateX(0); }
  25% { transform: translateX(-6px); }
  75% { transform: translateX(6px); }
}

.error-icon {
  font-size: 1.1rem;
  filter: drop-shadow(0 1px 2px rgba(0, 0, 0, 0.1));
}

/* 提交按钮 */
.submit-btn {
  padding: 1.25rem 2.5rem;
  border: none;
  border-radius: 14px;
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 50%, #ec4899 100%);
  background-size: 200% 100%;
  background-position: 0% 0%;
  color: white;
  font-size: 1.125rem;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.625rem;
  margin-top: 1rem;
  box-shadow: 
    0 8px 28px rgba(99, 102, 241, 0.4),
    0 0 0 1px rgba(255, 255, 255, 0.3) inset;
  position: relative;
  overflow: hidden;
  letter-spacing: 0.3px;
}

.submit-btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.25), transparent);
  transition: left 0.6s;
}

.submit-btn:hover:not(:disabled)::before {
  left: 100%;
}

.submit-btn:hover:not(:disabled) {
  transform: translateY(-3px);
  background-position: 100% 0%;
  box-shadow: 
    0 12px 40px rgba(99, 102, 241, 0.5),
    0 0 0 1px rgba(255, 255, 255, 0.4) inset;
}

.submit-btn:active:not(:disabled) {
  transform: translateY(-1px);
}

.submit-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
  transform: none;
  box-shadow: 0 4px 16px rgba(99, 102, 241, 0.25);
}

.btn-icon {
  font-size: 1.3rem;
  filter: drop-shadow(0 1px 2px rgba(0, 0, 0, 0.2));
}

/* 登录链接 */
.login-link {
  text-align: center;
  margin-top: 2.25rem;
  padding-top: 1.75rem;
  border-top: 2px solid #e2e8f0;
  color: #64748b;
  font-size: 0.975rem;
  font-weight: 500;
}

.login-link-text {
  color: #6366f1;
  background: none;
  border: none;
  text-decoration: none;
  font-weight: 600;
  margin-left: 0.375rem;
  cursor: pointer;
  transition: all 0.2s;
  font-size: 0.975rem;
  padding: 0;
}

.login-link-text:hover {
  color: #8b5cf6;
  text-decoration: underline;
}

/* 响应式设计 */
@media (max-width: 640px) {
  .register-page {
    padding: 1.5rem 1rem;
  }

  .register-card {
    padding: 2.25rem 1.75rem;
    border-radius: 28px;
  }

  .header-section {
    margin-bottom: 2rem;
  }

  .header-section h2 {
    font-size: 1.875rem;
  }

  .subtitle {
    font-size: 0.975rem;
  }

  .method-selector {
    gap: 0.5rem;
    padding: 0.3rem;
  }

  .method-btn {
    padding: 0.75rem 1rem;
    font-size: 0.925rem;
  }

  .modern-input {
    padding: 1rem 1.125rem;
    font-size: 1rem;
  }

  .code-input-row {
    flex-direction: column;
    gap: 0.75rem;
  }

  .send-code-btn {
    width: 100%;
    min-width: auto;
  }

  .submit-btn {
    padding: 1.125rem 2rem;
    font-size: 1.075rem;
  }
}

@media (max-width: 480px) {
  .register-card {
    padding: 2rem 1.5rem;
  }

  .header-section h2 {
    font-size: 1.75rem;
  }

  .register-form {
    gap: 1.5rem;
  }
}
</style>



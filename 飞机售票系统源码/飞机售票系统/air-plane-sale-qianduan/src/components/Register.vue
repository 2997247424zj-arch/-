<template>
  <div class="register-container">
    <div class="register-background">
      <div class="airplane-animation">
        <div class="airplane">✈️</div>
        <div class="cloud cloud-1">☁️</div>
        <div class="cloud cloud-2">☁️</div>
        <div class="cloud cloud-3">☁️</div>
      </div>
    </div>
    
    <div class="register-form-container">
      <div class="register-card">
        <div class="logo-section">
          <h1>✈️ 飞机售票系统</h1>
          <p>创建您的账户，开始飞行旅程</p>
        </div>
        
        <form @submit.prevent="handleRegister" class="register-form">
          <div class="form-row">
            <div class="form-group">
              <label for="username">用户名</label>
              <input
                id="username"
                v-model="form.username"
                type="text"
                placeholder="请输入用户名"
                required
                @blur="validateUsername"
              />
              <div v-if="errors.username" class="error-message">{{ errors.username }}</div>
            </div>
            
            <div class="form-group">
              <label for="idCard">身份证号</label>
              <input
                id="idCard"
                v-model="form.idCard"
                type="text"
                placeholder="请输入18位身份证号"
                maxlength="18"
                required
                @blur="validateIdCard"
              />
              <div v-if="errors.idCard" class="error-message">{{ errors.idCard }}</div>
            </div>
          </div>
          
          <div class="form-row">
            <div class="form-group">
              <label for="phone">手机号</label>
              <input
                id="phone"
                v-model="form.phone"
                type="tel"
                placeholder="请输入手机号"
                required
                @blur="validatePhone"
              />
              <div v-if="errors.phone" class="error-message">{{ errors.phone }}</div>
            </div>
            
            <div class="form-group">
              <label for="realName">真实姓名</label>
              <input
                id="realName"
                v-model="form.realName"
                type="text"
                placeholder="请输入真实姓名"
                required
              />
            </div>
          </div>
          
          <div class="form-row">
            <div class="form-group">
              <label for="password">密码</label>
              <input
                id="password"
                v-model="form.password"
                type="password"
                placeholder="请输入密码"
                required
                @blur="validatePassword"
              />
              <div v-if="errors.password" class="error-message">{{ errors.password }}</div>
            </div>
            
            <div class="form-group">
              <label for="confirmPassword">确认密码</label>
              <input
                id="confirmPassword"
                v-model="form.confirmPassword"
                type="password"
                placeholder="请再次输入密码"
                required
                @blur="validateConfirmPassword"
              />
              <div v-if="errors.confirmPassword" class="error-message">{{ errors.confirmPassword }}</div>
            </div>
          </div>
          
          <div class="form-row">
            <div class="form-group">
              <label for="role">角色</label>
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
              <div v-if="errors.role" class="error-message">{{ errors.role }}</div>
            </div>
          </div>
          
          <div class="form-group">
            <label class="checkbox-label">
              <input type="checkbox" v-model="agreedToTerms" required />
              我已阅读并同意 <a href="#" @click.prevent="showTerms">《用户协议》</a> 和 <a href="#" @click.prevent="showPrivacy">《隐私政策》</a>
            </label>
            <div v-if="errors.agreedToTerms" class="error-message">{{ errors.agreedToTerms }}</div>
          </div>
          
          <button type="submit" class="register-btn" :disabled="loading">
            {{ loading ? '注册中...' : '立即注册' }}
          </button>
        </form>
        
        <div class="login-section">
          <p>已有账号？ <router-link to="/" class="login-link" @click="handleLoginClick">立即登录</router-link></p>
        </div>
      </div>
    </div>
  </div>

  <!-- 成功提示框 -->
  <ModalPrompt
    v-model="successDialog.visible"
    :title="successDialog.title || '注册成功'"
    :message="successDialog.message || '账号创建成功'"
    type="success"
    confirm-text="好的"
    @confirm="handleSuccessConfirm"
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
</template>

<script setup lang="ts">
import { ref, reactive, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { authApi } from '../services/api'
import ModalPrompt from './ModalPrompt.vue'

interface RegisterForm {
  username: string
  idCard: string
  phone: string
  realName: string
  password: string
  confirmPassword: string
  role: string
}

interface FormErrors {
  username?: string
  idCard?: string
  phone?: string
  password?: string
  confirmPassword?: string
  role?: string
  agreedToTerms?: string
}

const router = useRouter()

const form = reactive<RegisterForm>({
  username: '',
  idCard: '',
  phone: '',
  realName: '',
  password: '',
  confirmPassword: '',
  role: 'passenger'
})

const errors = reactive<FormErrors>({})
const agreedToTerms = ref(false)
const loading = ref(false)

// 弹窗状态
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

const confirmDialog = reactive({
  visible: false,
  title: '',
  message: '',
  onConfirm: () => {}
})

const validateUsername = async () => {
  if (!form.username) {
    errors.username = '用户名不能为空'
    return false
  }
  
  if (form.username.length < 3) {
    errors.username = '用户名至少3个字符'
    return false
  }
  
  // 检查用户名是否已存在
  try {
    const exists = await authApi.checkUsername(form.username)
    if (exists) {
      errors.username = '用户名已存在'
      return false
    }
  } catch (error) {
    console.error('检查用户名失败:', error)
  }
  
  delete errors.username
  return true
}

const validateIdCard = () => {
  const idCardRegex = /^\d{17}[\dXx]$/
  
  if (!form.idCard) {
    errors.idCard = '身份证号不能为空'
    return false
  }
  
  if (!idCardRegex.test(form.idCard)) {
    errors.idCard = '请输入有效的18位身份证号'
    return false
  }
  
  delete errors.idCard
  return true
}

const validatePhone = () => {
  const phoneRegex = /^1[3-9]\d{9}$/
  
  if (!form.phone) {
    errors.phone = '手机号不能为空'
    return false
  }
  
  if (!phoneRegex.test(form.phone)) {
    errors.phone = '请输入有效的手机号'
    return false
  }
  
  delete errors.phone
  return true
}

const validatePassword = () => {
  if (!form.password) {
    errors.password = '密码不能为空'
    return false
  }
  
  if (form.password.length < 6) {
    errors.password = '密码至少6个字符'
    return false
  }
  
  delete errors.password
  return true
}

const validateConfirmPassword = () => {
  if (!form.confirmPassword) {
    errors.confirmPassword = '请确认密码'
    return false
  }
  
  if (form.password !== form.confirmPassword) {
    errors.confirmPassword = '两次输入的密码不一致'
    return false
  }
  
  delete errors.confirmPassword
  return true
}

const validateForm = async () => {
  const validations = [
    validateUsername(),
    validateIdCard(),
    validatePhone(),
    validatePassword(),
    validateConfirmPassword()
  ]
  
  if (!agreedToTerms.value) {
    errors.agreedToTerms = '请同意用户协议和隐私政策'
    return false
  }
  
  delete errors.agreedToTerms
  
  const results = await Promise.all(validations)
  return results.every(result => result)
}

// 处理成功提示确认
const handleSuccessConfirm = () => {
  successDialog.visible = false
  router.push('/')
}

const handleRegister = async () => {
  if (!(await validateForm())) {
    return
  }
  
  loading.value = true
  
  // 先清除之前的提示
  successDialog.visible = false
  errorDialog.visible = false
  
  try {
    const result = await authApi.register({
      username: form.username,
      idCard: form.idCard,
      phone: form.phone,
      realName: form.realName,
      password: form.password,
      role: form.role
    })
    
    console.log('注册成功:', result)
    
    // 设置成功提示信息
    successDialog.title = '注册成功'
    successDialog.message = `恭喜！账号 "${form.username}" 创建成功。\n\n即将跳转到登录页面，请使用您的用户名和密码登录。`
    
    // 使用nextTick确保DOM更新后再显示
    await nextTick()
    successDialog.visible = true
    
    console.log('显示成功提示框:', {
      visible: successDialog.visible,
      title: successDialog.title,
      message: successDialog.message
    })
    
    // 延迟跳转，给用户时间看到成功提示（增加到3秒）
    setTimeout(() => {
      if (successDialog.visible) {
        console.log('自动跳转到登录页')
        successDialog.visible = false
        router.push('/')
      }
    }, 3000)
  } catch (error: any) {
    console.error('注册失败:', error)
    
    // 解析错误信息，提供更友好的提示
    let errorMessage = error.message || '注册失败，请稍后重试'
    
    // 根据不同的错误类型提供更详细的提示
    if (errorMessage.includes('无法连接到服务器')) {
      errorMessage = '无法连接到服务器，请确保后端服务已启动。\n\n如果后端服务未启动，请先启动后端服务。'
    } else if (errorMessage.includes('用户名已存在')) {
      errorMessage = '注册失败：用户名已被使用。\n\n请尝试使用其他用户名。'
    } else if (errorMessage.includes('身份证号已被注册') || errorMessage.includes('身份证已被注册')) {
      errorMessage = '注册失败：该身份证号已被注册。\n\n如果您已有账号，请直接登录。如果忘记密码，请联系管理员。'
    } else if (errorMessage.includes('密码长度不能少于6位')) {
      errorMessage = '注册失败：密码长度不能少于6位。\n\n请设置一个至少6位的密码。'
    } else if (errorMessage.includes('用户名不能为空')) {
      errorMessage = '注册失败：用户名不能为空。\n\n请输入用户名。'
    } else if (errorMessage.includes('身份证号不能为空') || errorMessage.includes('身份证不能为空')) {
      errorMessage = '注册失败：身份证号不能为空。\n\n请输入有效的18位身份证号。'
    } else if (errorMessage.includes('手机号不能为空')) {
      errorMessage = '注册失败：手机号不能为空。\n\n请输入有效的手机号码。'
    } else if (errorMessage.includes('网络错误') || errorMessage.includes('请求超时')) {
      errorMessage = '网络连接异常，请检查网络设置后重试。'
    }
    
    // 显示错误提示
    errorDialog.title = '注册失败'
    errorDialog.message = errorMessage
    errorDialog.visible = true
  } finally {
    loading.value = false
  }
}

const showTerms = () => {
  infoDialog.title = '用户协议'
  infoDialog.message = `1. 用户应遵守相关法律法规
2. 不得恶意刷单或干扰系统正常运行
3. 个人信息将严格保密
4. 系统维护期间可能暂停服务
5. 禁止任何形式的资源滥用
6. 服务最终解释权归本平台所有`
  infoDialog.visible = true
}

const showPrivacy = () => {
  infoDialog.title = '隐私政策'
  infoDialog.message = `1. 收集的信息仅用于提供服务
2. 不会向第三方出售用户信息
3. 采用加密技术保护数据安全
4. 用户有权要求删除个人信息
5. 数据存储符合国家相关标准
6. 定期进行安全审计和漏洞修复`
  infoDialog.visible = true
}

// 新增：处理登录链接点击
const handleLoginClick = () => {
  confirmDialog.title = '确认返回'
  confirmDialog.message = '确定要返回登录页面吗？\n\n当前填写的信息将不会保存。'
  confirmDialog.onConfirm = () => {
    confirmDialog.visible = false
    router.push('/')
  }
  confirmDialog.visible = true
}
</script>

<style scoped>
.register-container {
  min-height: 100vh;
  display: flex;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  position: relative;
  overflow: hidden;
}

.register-background {
  flex: 1;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}

.airplane-animation {
  position: relative;
  width: 300px;
  height: 300px;
}

.airplane {
  font-size: 80px;
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  animation: fly 8s ease-in-out infinite;
}

.cloud {
  position: absolute;
  font-size: 40px;
  opacity: 0.7;
}

.cloud-1 {
  top: 20%;
  left: 10%;
  animation: float 6s ease-in-out infinite;
}

.cloud-2 {
  top: 60%;
  right: 15%;
  animation: float 8s ease-in-out infinite 2s;
}

.cloud-3 {
  bottom: 20%;
  left: 20%;
  animation: float 7s ease-in-out infinite 1s;
}

@keyframes fly {
  0%, 100% { transform: translate(-50%, -50%) translateX(0) rotate(0deg); }
  25% { transform: translate(-50%, -50%) translateX(20px) rotate(5deg); }
  50% { transform: translate(-50%, -50%) translateX(0) rotate(0deg); }
  75% { transform: translate(-50%, -50%) translateX(-20px) rotate(-5deg); }
}

@keyframes float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-20px); }
}

.register-form-container {
  width: 500px;
  background: white;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 40px;
}

.register-card {
  width: 100%;
  max-width: 420px;
}

.logo-section {
  text-align: center;
  margin-bottom: 30px;
}

.logo-section h1 {
  color: #333;
  font-size: 28px;
  margin-bottom: 8px;
  font-weight: 600;
}

.logo-section p {
  color: #666;
  font-size: 14px;
}

.register-form {
  margin-bottom: 20px;
}

.form-row {
  display: flex;
  gap: 15px;
  margin-bottom: 15px;
}

.form-group {
  flex: 1;
  margin-bottom: 0;
}

.form-group label {
  display: block;
  margin-bottom: 6px;
  color: #333;
  font-weight: 500;
  font-size: 13px;
}

.form-group input {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 13px;
  transition: border-color 0.3s;
  box-sizing: border-box;
}

.form-group input:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 2px rgba(102, 126, 234, 0.1);
}

.form-group .role-select {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 13px;
  transition: border-color 0.3s;
  box-sizing: border-box;
  background: white;
  color: #333;
  cursor: pointer;
}

.form-group .role-select:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 2px rgba(102, 126, 234, 0.1);
}

.error-message {
  color: #e74c3c;
  font-size: 12px;
  margin-top: 4px;
  min-height: 16px;
}

.checkbox-label {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: #666;
  cursor: pointer;
}

.checkbox-label a {
  color: #667eea;
  text-decoration: none;
}

.checkbox-label a:hover {
  text-decoration: underline;
}

.register-btn {
  width: 100%;
  padding: 12px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 15px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s;
  margin-top: 10px;
}

.register-btn:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.register-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.login-section {
  text-align: center;
  font-size: 13px;
  color: #666;
}

.login-link {
  color: #667eea;
  text-decoration: none;
  font-weight: 500;
}

.login-link:hover {
  text-decoration: underline;
}

@media (max-width: 768px) {
  .register-container {
    flex-direction: column;
  }
  
  .register-background {
    height: 150px;
    flex: none;
  }
  
  .register-form-container {
    width: 100%;
    padding: 30px 20px;
  }
  
  .form-row {
    flex-direction: column;
    gap: 10px;
  }
  
  .airplane-animation {
    transform: scale(0.6);
  }
}
</style>
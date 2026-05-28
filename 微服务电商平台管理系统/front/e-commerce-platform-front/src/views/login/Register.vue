<template>
  <div class="register-wrapper">
    <div class="glass-container">
      <div class="register-header">
        <h2>注册 MicroMall 账号</h2>
        <p>开启你的专属购物体验</p>
      </div>

      <el-form :model="form" :rules="rules" ref="registerFormRef" class="register-form">
        <el-form-item prop="username">
          <el-input
            v-model="form.username"
            placeholder="请输入用户名"
            size="large"
            :prefix-icon="User"
          />
        </el-form-item>
        <el-form-item prop="phone">
          <el-input
            v-model="form.phone"
            placeholder="请输入手机号"
            size="large"
            :prefix-icon="Iphone"
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            placeholder="请输入密保"
            type="password"
            show-password
            size="large"
            :prefix-icon="Lock"
          />
        </el-form-item>
        <el-form-item prop="confirmPassword">
          <el-input
            v-model="form.confirmPassword"
            placeholder="请确认密码"
            type="password"
            show-password
            size="large"
            :prefix-icon="Lock"
          />
        </el-form-item>

        <el-button
          color="#4f46e5"
          size="large"
          class="submit-btn"
          :loading="loading"
          @click="handleRegister"
        >
          注 册
        </el-button>
        <div class="login-hint">
          已有账号？ <el-link type="primary" @click="router.push('/login')">直接登录</el-link>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { User, Lock, Iphone } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { registerAPI } from '@/api/modules/user'

const router = useRouter()
const registerFormRef = ref<FormInstance>()
const loading = ref(false)

const form = reactive({
  username: '',
  phone: '',
  password: '',
  confirmPassword: '',
})

const checkConfirmPassword = (rule: any, value: string, callback: any) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== form.password) {
    callback(new Error('两次输入密码不一致!'))
  } else {
    callback()
  }
}

const rules = reactive<FormRules>({
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' },
  ],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  confirmPassword: [{ validator: checkConfirmPassword, trigger: 'blur' }],
})

const handleRegister = async () => {
  if (!registerFormRef.value) return
  await registerFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        await registerAPI({
          username: form.username,
          phone: form.phone,
          password: form.password,
        })
        ElMessage.success('注册成功，请登录')
        router.push('/login')
      } catch (err: any) {
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style scoped lang="scss">
.register-wrapper {
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #f0fdf4 0%, #dcfce7 100%);
  position: relative;
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    width: 600px;
    height: 600px;
    border-radius: 50%;
    background: linear-gradient(135deg, #4ade80 0%, #3b82f6 100%);
    opacity: 0.3;
    filter: blur(80px);
    bottom: -200px;
    right: -200px;
    animation: float 10s infinite alternate;
  }
}

@keyframes float {
  0% {
    transform: translateY(0) scale(1);
  }
  100% {
    transform: translateY(-50px) scale(1.1);
  }
}

.glass-container {
  width: 420px;
  background: rgba(255, 255, 255, 0.75);
  backdrop-filter: blur(20px);
  padding: 40px;
  border-radius: 24px;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.15);
  border: 1px solid rgba(255, 255, 255, 0.5);
  z-index: 10;
}

.register-header {
  text-align: center;
  margin-bottom: 30px;

  h2 {
    font-size: 24px;
    font-weight: 700;
    color: #0f172a;
    margin: 0;
  }

  p {
    color: #64748b;
    margin-top: 8px;
    font-size: 14px;
  }
}

.submit-btn {
  width: 100%;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 2px;
  margin-top: 10px;
  margin-bottom: 15px;
  transition: transform 0.2s;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 10px 15px -3px rgba(79, 70, 229, 0.3);
  }
}

.login-hint {
  text-align: center;
  font-size: 14px;
  color: #64748b;
}

:deep(.el-input__wrapper) {
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.9);
  box-shadow: 0 0 0 1px #cbd5e1 inset;

  &.is-focus {
    box-shadow: 0 0 0 1px #4f46e5 inset;
  }
}
</style>

<template>
  <div class="login-root">
    <!-- Left promo panel -->
    <div class="promo-panel">
      <div class="promo-content">
        <div class="promo-brand">
          <div class="brand-mark">
            <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M6 2 3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4z"/>
              <line x1="3" y1="6" x2="21" y2="6"/>
              <path d="M16 10a4 4 0 0 1-8 0"/>
            </svg>
          </div>
          <span class="brand-name">MicroMall</span>
        </div>

        <div class="promo-hero">
          <h1>品质生活，触手可及</h1>
          <p>数千款精选商品、极速履约与全程无忧售后，让你的每一次购物都成为愉悦体验。</p>
        </div>

        <ul class="promo-features">
          <li>
            <span class="feature-dot blue"></span>
            <div>
              <strong>精选品牌商品</strong>
              <p>3000+ SKU，涵盖数码、服饰、家居等热门品类</p>
            </div>
          </li>
          <li>
            <span class="feature-dot green"></span>
            <div>
              <strong>极速发货配送</strong>
              <p>下单后 24 小时内发货，大多数地区次日达</p>
            </div>
          </li>
          <li>
            <span class="feature-dot amber"></span>
            <div>
              <strong>安全购物保障</strong>
              <p>七天无理由退换、全程正品保障，放心下单</p>
            </div>
          </li>
        </ul>

        <div class="promo-bottom">
          <span>还没有账号？</span>
          <el-button text type="primary" @click="router.push('/register')">立即免费注册</el-button>
        </div>
      </div>
    </div>

    <!-- Right form panel -->
    <div class="form-panel">
      <div class="form-card glass-panel">
        <div class="form-header">
          <h2>欢迎回来</h2>
          <p>登录你的 MicroMall 账号，继续探索好物</p>
        </div>

        <el-form :model="form" :rules="rules" ref="loginFormRef" class="login-form">
          <el-form-item prop="username">
            <el-input
              v-model="form.username"
              placeholder="用户名或手机号"
              size="large"
              :prefix-icon="User"
            />
          </el-form-item>

          <el-form-item prop="password">
            <el-input
              v-model="form.password"
              placeholder="密码（至少 6 位）"
              type="password"
              show-password
              size="large"
              :prefix-icon="Lock"
              @keyup.enter="handleLogin"
            />
          </el-form-item>

          <div class="form-options">
            <el-checkbox v-model="rememberMe">记住我</el-checkbox>
            <el-link type="primary" :underline="false">忘记密码？</el-link>
          </div>

          <el-button
            type="primary"
            size="large"
            class="submit-btn"
            :loading="loading"
            @click="handleLogin"
          >
            登录账号
          </el-button>

          <div class="register-hint">
            <span>还没有账号？</span>
            <el-link type="primary" @click="router.push('/register')">立即注册</el-link>
          </div>
        </el-form>

        <div class="quick-demo">
          <p>演示账号：</p>
          <div class="demo-accounts">
            <button class="demo-btn" @click="fillDemo('admin', '123456')">管理员 admin / 123456</button>
            <button class="demo-btn" @click="fillDemo('testuser', '123456')">用户 testuser / 123456</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { User, Lock } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import { useUserStore } from '@/store/user'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const loginFormRef = ref<FormInstance>()
const loading = ref(false)
const rememberMe = ref(false)

const form = reactive({
  username: '',
  password: '',
})

const rules = reactive<FormRules>({
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
})

const fillDemo = (username: string, password: string) => {
  form.username = username
  form.password = password
}

const handleLogin = async () => {
  if (!loginFormRef.value) return
  await loginFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        await userStore.login(form)
        ElMessage.success('登录成功')

        const userRole = userStore.userInfo?.role
        if (userRole === 'admin') {
          router.push('/admin')
        } else {
          const redirect = (route.query.redirect as string) || '/'
          router.push(redirect)
        }
      } catch {
        // handled by axios interceptor
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style scoped lang="scss">
.login-root {
  min-height: 100vh;
  display: grid;
  grid-template-columns: 1.1fr 0.9fr;
}

/* ---- Promo Panel ---- */
.promo-panel {
  background: linear-gradient(160deg, #0f172a 0%, #1e3a8a 50%, #1e40af 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 60px 48px;
  position: relative;
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    width: 600px;
    height: 600px;
    border-radius: 50%;
    background: radial-gradient(circle, rgba(34,197,94,0.18), transparent 70%);
    top: -200px;
    right: -200px;
  }

  &::after {
    content: '';
    position: absolute;
    width: 400px;
    height: 400px;
    border-radius: 50%;
    background: radial-gradient(circle, rgba(59,130,246,0.16), transparent 70%);
    bottom: -100px;
    left: -100px;
  }
}

.promo-content {
  position: relative;
  z-index: 1;
  max-width: 480px;
  color: #fff;
}

.promo-brand {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-bottom: 48px;
}

.brand-mark {
  width: 50px;
  height: 50px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.16);
  backdrop-filter: blur(8px);
}

.brand-name {
  font-family: 'Rubik', sans-serif;
  font-size: 24px;
  font-weight: 800;
  color: #fff;
}

.promo-hero {
  margin-bottom: 40px;

  h1 {
    font-size: clamp(36px, 4vw, 52px);
    font-weight: 800;
    line-height: 1.08;
    color: #fff;
    margin-bottom: 18px;
  }

  p {
    font-size: 16px;
    line-height: 1.8;
    color: rgba(224, 242, 254, 0.88);
  }
}

.promo-features {
  list-style: none;
  padding: 0;
  margin: 0 0 48px;
  display: flex;
  flex-direction: column;
  gap: 22px;

  li {
    display: flex;
    align-items: flex-start;
    gap: 14px;

    strong {
      display: block;
      font-size: 16px;
      color: #fff;
      margin-bottom: 4px;
    }

    p {
      font-size: 14px;
      color: rgba(224, 242, 254, 0.78);
      line-height: 1.6;
    }
  }
}

.feature-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  flex-shrink: 0;
  margin-top: 4px;

  &.blue { background: #60a5fa; box-shadow: 0 0 12px rgba(96,165,250,0.6); }
  &.green { background: #4ade80; box-shadow: 0 0 12px rgba(74,222,128,0.6); }
  &.amber { background: #fbbf24; box-shadow: 0 0 12px rgba(251,191,36,0.6); }
}

.promo-bottom {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 15px;
  color: rgba(255,255,255,0.7);

  :deep(.el-button span) {
    color: #93c5fd;
    font-weight: 800;
    font-size: 15px;
  }
}

/* ---- Form Panel ---- */
.form-panel {
  background: linear-gradient(180deg, #f8fbff 0%, #eff6ff 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 48px 36px;
}

.form-card {
  width: min(420px, 100%);
  padding: 36px;
  border-radius: 28px;
}

.form-header {
  margin-bottom: 30px;

  h2 {
    font-size: 32px;
    margin-bottom: 10px;
  }

  p {
    font-size: 15px;
    line-height: 1.7;
  }
}

.login-form {
  :deep(.el-form-item) {
    margin-bottom: 18px;
  }

  :deep(.el-input__wrapper) {
    border-radius: 14px;
    padding: 0 16px;
    height: 52px;
    border: 1.5px solid #dbeafe;
    background: rgba(248, 251, 255, 0.9);
    box-shadow: none;
    transition: all 0.2s ease;

    &:hover {
      border-color: #93c5fd;
    }

    &.is-focus {
      border-color: #3b82f6;
      background: #fff;
      box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.15) !important;
    }
  }
}

.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 22px;
}

.submit-btn {
  width: 100%;
  height: 52px;
  border-radius: 999px;
  font-size: 17px;
  font-weight: 800;
  margin-bottom: 18px;
  letter-spacing: 0.04em;
}

.register-hint {
  text-align: center;
  font-size: 14px;
  color: #64748b;
  margin-bottom: 24px;
}

.quick-demo {
  padding-top: 18px;
  border-top: 1px solid #dbeafe;

  p {
    font-size: 12px;
    font-weight: 800;
    color: #64748b;
    margin-bottom: 10px;
  }
}

.demo-accounts {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.demo-btn {
  flex: 1;
  min-width: 0;
  border: 1px dashed #93c5fd;
  background: transparent;
  color: #3b82f6;
  border-radius: 10px;
  padding: 10px 12px;
  cursor: pointer;
  font-size: 12px;
  font-weight: 800;
  transition: all 0.2s ease;
  text-align: center;
  white-space: nowrap;

  &:hover {
    background: #eff6ff;
    border-color: #3b82f6;
  }
}

@media (max-width: 768px) {
  .login-root {
    grid-template-columns: 1fr;
  }

  .promo-panel {
    padding: 40px 28px;
    min-height: 40vh;
  }

  .promo-features {
    display: none;
  }

  .form-panel {
    padding: 32px 16px;
  }
}
</style>

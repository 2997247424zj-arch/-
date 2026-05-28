<template>
  <div class="quick-access-container">
    <div class="access-card">
      <h2>快速访问</h2>
      <p>选择你想要访问的页面：</p>

      <div class="button-grid">
        <el-button type="primary" size="large" @click="goToEasyTest">
          <el-icon><MagicStick /></el-icon>
          超级简单测试
        </el-button>

        <el-button type="primary" size="large" @click="goToHome">
          <el-icon><House /></el-icon>
          首页
        </el-button>

        <el-button type="success" size="large" @click="goToDashboard">
          <el-icon><DataAnalysis /></el-icon>
          数据看板
        </el-button>

        <el-button type="info" size="large" @click="goToProducts">
          <el-icon><Goods /></el-icon>
          商品列表
        </el-button>

        <el-button type="warning" size="large" @click="goToBackendTest">
          <el-icon><Connection /></el-icon>
          后端测试
        </el-button>

        <el-button type="danger" size="large" @click="goToLoginTest">
          <el-icon><Key /></el-icon>
          登录测试
        </el-button>

        <el-button type="primary" size="large" @click="goToSimpleLoginTest">
          <el-icon><UserFilled /></el-icon>
          简化登录测试
        </el-button>
      </div>

      <div class="login-section">
        <h3>登录状态</h3>
        <div class="status-info">
          <span>当前状态：</span>
          <el-tag :type="hasToken ? 'success' : 'danger'">
            {{ hasToken ? '已登录' : '未登录' }}
          </el-tag>
        </div>

        <div class="login-actions">
          <el-button v-if="!hasToken" type="primary" @click="setTestLogin">
            设置测试登录
          </el-button>
          <el-button v-if="hasToken" type="danger" @click="clearLogin">
            清除登录
          </el-button>
          <el-button type="default" @click="goToLogin">
            正常登录
          </el-button>
        </div>
      </div>

      <div class="mode-section">
        <h3>数据模式</h3>
        <div class="mode-info">
          <span>当前模式：</span>
          <el-tag :type="useMock ? 'warning' : 'success'">
            {{ useMock ? 'Mock 模式' : '后端模式' }}
          </el-tag>
        </div>
        <p class="mode-desc">
          {{ useMock ? '使用虚拟数据，无需启动后端服务' : '连接真实后端，需要启动后端服务' }}
        </p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { House, DataAnalysis, Goods, Connection, Key, UserFilled, MagicStick } from '@element-plus/icons-vue'
import { USE_MOCK } from '@/api/useMock'

const router = useRouter()
const hasToken = ref(false)
const useMock = ref(USE_MOCK)

const checkToken = () => {
  hasToken.value = !!localStorage.getItem('token')
}

const setTestLogin = () => {
  const testToken = 'test-token-' + Date.now()
  localStorage.setItem('token', testToken)
  hasToken.value = true
  ElMessage.success('已设置测试登录状态')
}

const clearLogin = () => {
  localStorage.removeItem('token')
  hasToken.value = false
  ElMessage.info('已清除登录状态')
}

const goToHome = () => {
  router.push('/home')
}

const goToEasyTest = () => {
  router.push('/debug/easy')
}

const goToDashboard = () => {
  router.push('/dashboard')
}

const goToProducts = () => {
  router.push('/product/list')
}

const goToBackendTest = () => {
  router.push('/debug/backend')
}

const goToLoginTest = () => {
  router.push('/debug/login')
}

const goToSimpleLoginTest = () => {
  router.push('/debug/simple-login')
}

const goToLogin = () => {
  router.push('/login')
}

onMounted(() => {
  checkToken()
})
</script>

<style scoped lang="scss">
.quick-access-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.access-card {
  background: white;
  border-radius: 16px;
  padding: 40px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
  max-width: 600px;
  width: 100%;

  h2 {
    text-align: center;
    margin-bottom: 10px;
    color: #333;
    font-size: 28px;
  }

  p {
    text-align: center;
    color: #666;
    margin-bottom: 30px;
  }
}

.button-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
  margin-bottom: 40px;

  .el-button {
    height: 60px;
    font-size: 16px;

    .el-icon {
      margin-right: 8px;
    }
  }
}

.login-section, .mode-section {
  border-top: 1px solid #eee;
  padding-top: 20px;
  margin-top: 20px;

  h3 {
    margin-bottom: 15px;
    color: #333;
    font-size: 18px;
  }
}

.status-info, .mode-info {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 15px;

  span {
    color: #666;
  }
}

.login-actions {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.mode-desc {
  color: #666;
  font-size: 14px;
  margin: 0;
}

@media (max-width: 768px) {
  .button-grid {
    grid-template-columns: 1fr;
  }

  .access-card {
    padding: 20px;
  }

  .login-actions {
    flex-direction: column;
  }
}
</style>

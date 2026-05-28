<template>
  <div class="backend-connection">
    <div class="test-container">
      <h1>🔌 前后端连接测试</h1>

      <!-- 连接状态 -->
      <div class="status-card">
        <h3>📊 当前状态</h3>
        <div class="status-grid">
          <div class="status-item">
            <span class="label">Mock 模式：</span>
            <el-tag :type="mockStatus.type">{{ mockStatus.text }}</el-tag>
          </div>
          <div class="status-item">
            <span class="label">后端地址：</span>
            <span class="value">{{ backendUrl }}</span>
          </div>
          <div class="status-item">
            <span class="label">Token 验证：</span>
            <el-tag type="info">已关闭</el-tag>
          </div>
          <div class="status-item">
            <span class="label">验证方式：</span>
            <el-tag type="success">表单验证</el-tag>
          </div>
        </div>
      </div>

      <!-- 后端健康检查 -->
      <div class="test-card">
        <h3>🏥 后端健康检查</h3>
        <el-button type="primary" @click="checkBackendHealth" :loading="healthChecking">
          检查后端服务
        </el-button>
        <div v-if="healthResult" class="result-box" :class="healthResult.type">
          <div class="result-icon">{{ healthResult.icon }}</div>
          <div class="result-text">
            <strong>{{ healthResult.title }}</strong>
            <p>{{ healthResult.message }}</p>
          </div>
        </div>
      </div>

      <!-- API 测试 -->
      <div class="test-card">
        <h3>🧪 API 接口测试</h3>

        <!-- 商品列表测试 -->
        <div class="api-test-item">
          <h4>1. 商品列表接口</h4>
          <p class="api-path">GET /api/product/list</p>
          <el-button @click="testProductList" :loading="productTesting">
            测试商品列表
          </el-button>
          <div v-if="productResult" class="result-box" :class="productResult.type">
            <strong>{{ productResult.title }}</strong>
            <p>{{ productResult.message }}</p>
            <pre v-if="productResult.data">{{ JSON.stringify(productResult.data, null, 2) }}</pre>
          </div>
        </div>

        <!-- 用户登录测试 -->
        <div class="api-test-item">
          <h4>2. 用户登录接口</h4>
          <p class="api-path">POST /api/user/login</p>
          <el-form :inline="true">
            <el-form-item label="用户名">
              <el-input v-model="loginForm.username" placeholder="zhangsan" />
            </el-form-item>
            <el-form-item label="密码">
              <el-input v-model="loginForm.password" type="password" placeholder="password123" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="testLogin" :loading="loginTesting">
                测试登录
              </el-button>
            </el-form-item>
          </el-form>
          <div v-if="loginResult" class="result-box" :class="loginResult.type">
            <strong>{{ loginResult.title }}</strong>
            <p>{{ loginResult.message }}</p>
            <pre v-if="loginResult.data">{{ JSON.stringify(loginResult.data, null, 2) }}</pre>
          </div>
        </div>

        <!-- 用户注册测试 -->
        <div class="api-test-item">
          <h4>3. 用户注册接口</h4>
          <p class="api-path">POST /api/user/register</p>
          <el-button @click="testRegister" :loading="registerTesting">
            测试注册
          </el-button>
          <div v-if="registerResult" class="result-box" :class="registerResult.type">
            <strong>{{ registerResult.title }}</strong>
            <p>{{ registerResult.message }}</p>
          </div>
        </div>
      </div>

      <!-- 测试总结 -->
      <div class="summary-card" v-if="testSummary">
        <h3>📋 测试总结</h3>
        <div class="summary-content">
          <div class="summary-item">
            <span class="label">总测试数：</span>
            <span class="value">{{ testSummary.total }}</span>
          </div>
          <div class="summary-item">
            <span class="label">成功：</span>
            <span class="value success">{{ testSummary.success }}</span>
          </div>
          <div class="summary-item">
            <span class="label">失败：</span>
            <span class="value error">{{ testSummary.failed }}</span>
          </div>
        </div>
      </div>

      <!-- 快速链接 -->
      <div class="links-card">
        <h3>🔗 快速链接</h3>
        <div class="link-buttons">
          <el-button @click="goTo('/debug/easy')">超级简单测试</el-button>
          <el-button @click="goTo('/login')">登录页面</el-button>
          <el-button @click="goTo('/home')">返回首页</el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { USE_MOCK } from '@/api/useMock'
import { getBackendUrl } from '@/config/backend'
import { loginAPI, registerAPI } from '@/api/modules/user'
import { getProductListAPI } from '@/api/modules/product'

const router = useRouter()

// 状态
const healthChecking = ref(false)
const productTesting = ref(false)
const loginTesting = ref(false)
const registerTesting = ref(false)

const healthResult = ref<any>(null)
const productResult = ref<any>(null)
const loginResult = ref<any>(null)
const registerResult = ref<any>(null)

const loginForm = ref({
  username: 'zhangsan',
  password: 'password123'
})

const backendUrl = ref(getBackendUrl())

const mockStatus = computed(() => {
  return USE_MOCK
    ? { type: 'warning', text: '启用 (虚拟数据)' }
    : { type: 'success', text: '禁用 (连接后端)' }
})

const testSummary = computed(() => {
  const results = [healthResult.value, productResult.value, loginResult.value, registerResult.value]
  const tested = results.filter(r => r !== null)
  if (tested.length === 0) return null

  return {
    total: tested.length,
    success: tested.filter(r => r.type === 'success').length,
    failed: tested.filter(r => r.type === 'error').length
  }
})

// 检查后端健康
const checkBackendHealth = async () => {
  healthChecking.value = true
  healthResult.value = null

  try {
    const response = await fetch(`${backendUrl.value}/actuator/health`, {
      method: 'GET',
      headers: { 'Content-Type': 'application/json' }
    })

    if (response.ok) {
      healthResult.value = {
        type: 'success',
        icon: '✅',
        title: '后端服务正常',
        message: '后端服务运行正常，可以进行 API 测试'
      }
    } else {
      throw new Error(`HTTP ${response.status}`)
    }
  } catch (error: any) {
    healthResult.value = {
      type: 'error',
      icon: '❌',
      title: '后端服务异常',
      message: `无法连接到后端服务: ${error.message}`
    }
  } finally {
    healthChecking.value = false
  }
}

// 测试商品列表
const testProductList = async () => {
  productTesting.value = true
  productResult.value = null

  try {
    console.log('🚀 测试商品列表 API...')
    const response = await getProductListAPI({ pageNum: 1, pageSize: 5 })
    console.log('✅ 商品列表响应:', response)

    const count = Array.isArray(response.data) ? response.data.length : 0
    productResult.value = {
      type: 'success',
      title: '✅ 商品列表接口正常',
      message: `成功获取 ${count} 个商品`,
      data: response.data
    }
  } catch (error: any) {
    console.error('❌ 商品列表测试失败:', error)
    productResult.value = {
      type: 'error',
      title: '❌ 商品列表接口失败',
      message: error.message || '请求失败'
    }
  } finally {
    productTesting.value = false
  }
}

// 测试登录
const testLogin = async () => {
  if (!loginForm.value.username || !loginForm.value.password) {
    ElMessage.error('请输入用户名和密码')
    return
  }

  loginTesting.value = true
  loginResult.value = null

  try {
    console.log('🚀 测试登录 API...', loginForm.value)
    const response = await loginAPI(loginForm.value)
    console.log('✅ 登录响应:', response)

    loginResult.value = {
      type: 'success',
      title: '✅ 登录接口正常',
      message: '登录成功',
      data: response.data
    }
  } catch (error: any) {
    console.error('❌ 登录测试失败:', error)
    loginResult.value = {
      type: 'error',
      title: '❌ 登录接口失败',
      message: error.message || '登录失败'
    }
  } finally {
    loginTesting.value = false
  }
}

// 测试注册
const testRegister = async () => {
  registerTesting.value = true
  registerResult.value = null

  try {
    const testData = {
      username: 'testuser_' + Date.now(),
      password: 'test123',
      email: 'test@example.com',
      phone: '13800138000'
    }

    console.log('🚀 测试注册 API...', testData)
    const response = await registerAPI(testData)
    console.log('✅ 注册响应:', response)

    registerResult.value = {
      type: 'success',
      title: '✅ 注册接口正常',
      message: '注册成功'
    }
  } catch (error: any) {
    console.error('❌ 注册测试失败:', error)
    registerResult.value = {
      type: 'error',
      title: '❌ 注册接口失败',
      message: error.message || '注册失败'
    }
  } finally {
    registerTesting.value = false
  }
}

const goTo = (path: string) => {
  router.push(path)
}
</script>

<style scoped lang="scss">
.backend-connection {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.test-container {
  max-width: 1000px;
  margin: 0 auto;
  background: white;
  border-radius: 16px;
  padding: 32px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);

  h1 {
    text-align: center;
    margin-bottom: 32px;
    color: #333;
  }

  h3 {
    margin: 24px 0 16px;
    color: #555;
    font-size: 18px;
    border-bottom: 2px solid #eee;
    padding-bottom: 8px;
  }

  h4 {
    margin: 16px 0 8px;
    color: #666;
    font-size: 16px;
  }
}

.status-card, .test-card, .summary-card, .links-card {
  margin-bottom: 24px;
  padding: 20px;
  background: #f9f9f9;
  border-radius: 8px;
}

.status-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;

  .status-item {
    display: flex;
    align-items: center;
    gap: 8px;

    .label {
      font-weight: 500;
      color: #666;
    }

    .value {
      font-family: monospace;
      color: #333;
    }
  }
}

.api-test-item {
  margin-bottom: 24px;
  padding: 16px;
  background: white;
  border-radius: 8px;
  border: 1px solid #e0e0e0;

  .api-path {
    font-family: monospace;
    color: #666;
    background: #f5f5f5;
    padding: 8px 12px;
    border-radius: 4px;
    margin: 8px 0;
  }
}

.result-box {
  margin-top: 16px;
  padding: 16px;
  border-radius: 8px;
  border-left: 4px solid;

  &.success {
    background: #f0f9ff;
    border-color: #10b981;
  }

  &.error {
    background: #fef2f2;
    border-color: #ef4444;
  }

  .result-icon {
    font-size: 24px;
    margin-bottom: 8px;
  }

  strong {
    display: block;
    margin-bottom: 8px;
  }

  p {
    margin: 4px 0;
    color: #666;
  }

  pre {
    margin-top: 12px;
    padding: 12px;
    background: #f5f5f5;
    border-radius: 4px;
    font-size: 12px;
    overflow-x: auto;
    max-height: 200px;
  }
}

.summary-content {
  display: flex;
  gap: 24px;

  .summary-item {
    .label {
      font-weight: 500;
      color: #666;
      margin-right: 8px;
    }

    .value {
      font-size: 20px;
      font-weight: bold;

      &.success {
        color: #10b981;
      }

      &.error {
        color: #ef4444;
      }
    }
  }
}

.link-buttons {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}
</style>

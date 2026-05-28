<template>
  <div class="login-test-container">
    <div class="test-card">
      <h2>登录测试工具</h2>

      <!-- 连接状态 -->
      <div class="status-section">
        <h3>连接状态</h3>
        <div class="status-info">
          <div class="status-item">
            <span class="label">Mock 模式：</span>
            <el-tag :type="useMock ? 'warning' : 'success'">
              {{ useMock ? '启用' : '禁用' }}
            </el-tag>
          </div>
          <div class="status-item">
            <span class="label">后端地址：</span>
            <span class="value">{{ backendUrl }}</span>
          </div>
          <div class="status-item">
            <span class="label">当前 Token：</span>
            <span class="value">{{ currentToken || '无' }}</span>
          </div>
        </div>
      </div>

      <!-- 数据库用户测试 -->
      <div class="db-users-section">
        <h3>数据库用户测试</h3>
        <p>根据截图，数据库中有以下用户，请选择一个进行测试：</p>

        <div class="user-list">
          <div
            class="user-item"
            v-for="user in dbUsers"
            :key="user.username"
            @click="selectUser(user)"
            :class="{ active: selectedUser?.username === user.username }"
          >
            <div class="user-info">
              <strong>{{ user.username }}</strong>
              <span class="phone">{{ user.phone }}</span>
              <span class="email">{{ user.email }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 登录表单 -->
      <div class="login-form-section">
        <h3>登录测试</h3>
        <el-form :model="loginForm" label-width="80px">
          <el-form-item label="用户名">
            <el-input v-model="loginForm.username" placeholder="请输入用户名" />
          </el-form-item>
          <el-form-item label="密码">
            <el-input
              v-model="loginForm.password"
              type="password"
              placeholder="请输入密码"
              show-password
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="testLogin" :loading="loginLoading">
              测试登录
            </el-button>
            <el-button @click="clearToken">清除 Token</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 测试结果 -->
      <div class="result-section" v-if="testResult">
        <h3>测试结果</h3>
        <div class="result-card" :class="testResult.success ? 'success' : 'error'">
          <div class="result-status">
            {{ testResult.success ? '✓ 登录成功' : '✗ 登录失败' }}
          </div>
          <div class="result-message">{{ testResult.message }}</div>
          <div class="result-data" v-if="testResult.data">
            <h4>响应数据：</h4>
            <pre>{{ JSON.stringify(testResult.data, null, 2) }}</pre>
          </div>
          <div class="result-error" v-if="testResult.error">
            <h4>错误信息：</h4>
            <pre>{{ testResult.error }}</pre>
          </div>
        </div>
      </div>

      <!-- API 测试 -->
      <div class="api-test-section">
        <h3>API 测试</h3>
        <div class="api-buttons">
          <el-button @click="testUserInfo" :loading="userInfoLoading">
            测试获取用户信息
          </el-button>
          <el-button @click="testProductList" :loading="productLoading">
            测试商品列表
          </el-button>
        </div>

        <div class="api-result" v-if="apiResult">
          <h4>API 测试结果：</h4>
          <div class="result-card" :class="apiResult.success ? 'success' : 'error'">
            <div class="result-status">
              {{ apiResult.success ? '✓ 成功' : '✗ 失败' }}
            </div>
            <div class="result-message">{{ apiResult.message }}</div>
            <div class="result-data" v-if="apiResult.data">
              <pre>{{ JSON.stringify(apiResult.data, null, 2) }}</pre>
            </div>
          </div>
        </div>
      </div>

      <!-- 调试信息 -->
      <div class="debug-section">
        <h3>调试信息</h3>
        <div class="debug-info">
          <div class="debug-item">
            <strong>请求基础地址：</strong> {{ request.defaults.baseURL }}
          </div>
          <div class="debug-item">
            <strong>请求超时：</strong> {{ request.defaults.timeout }}ms
          </div>
          <div class="debug-item">
            <strong>当前环境：</strong> {{ currentEnv }}
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { USE_MOCK } from '@/api/useMock'
import { getBackendUrl } from '@/config/backend'
import { loginAPI, getUserInfoAPI } from '@/api/modules/user'
import { getProductListAPI } from '@/api/modules/product'
import request from '@/api/request'

// 类型定义
interface DbUser {
  username: string
  phone: string
  email: string
  password: string
}

interface TestResult {
  success: boolean
  message: string
  data?: any
  error?: any
}

interface ApiResult {
  success: boolean
  message: string
  data?: any
}

const useMock = ref(USE_MOCK)
const backendUrl = ref(getBackendUrl())
const currentToken = ref(localStorage.getItem('token'))
const currentEnv = import.meta.env.MODE

// 根据截图中的数据库用户
const dbUsers = ref<DbUser[]>([
  {
    username: 'zhangsan',
    phone: '13800138000',
    email: 'zhangsan@qq.com',
    password: 'password123' // 假设密码
  },
  {
    username: 'lisi',
    phone: '13900139000',
    email: 'lisi@163.com',
    password: 'password123'
  },
  {
    username: 'testuser',
    phone: '',
    email: 'test@example.com',
    password: 'password123'
  }
])

const selectedUser = ref<DbUser | null>(null)
const loginForm = ref({
  username: '',
  password: ''
})

const loginLoading = ref(false)
const userInfoLoading = ref(false)
const productLoading = ref(false)
const testResult = ref<TestResult | null>(null)
const apiResult = ref<ApiResult | null>(null)

const selectUser = (user: DbUser) => {
  selectedUser.value = user
  loginForm.value.username = user.username
  loginForm.value.password = user.password
}

const testLogin = async () => {
  if (!loginForm.value.username || !loginForm.value.password) {
    ElMessage.error('请输入用户名和密码')
    return
  }

  loginLoading.value = true
  testResult.value = null

  try {
    console.log('发送登录请求:', loginForm.value)
    const res = await loginAPI(loginForm.value)
    console.log('登录响应:', res)

    testResult.value = {
      success: true,
      message: '登录成功',
      data: res.data
    }

    // 保存 token
    if (res.data) {
      let token = ''
      if (typeof res.data === 'string') {
        token = res.data
      } else if (res.data.token) {
        token = res.data.token
      }

      if (token) {
        localStorage.setItem('token', token)
        currentToken.value = token
        ElMessage.success('Token 已保存')
      }
    }
  } catch (error: any) {
    console.error('登录失败:', error)
    testResult.value = {
      success: false,
      message: '登录失败',
      error: error.message || error.toString()
    }
  } finally {
    loginLoading.value = false
  }
}

const testUserInfo = async () => {
  if (!currentToken.value) {
    ElMessage.error('请先登录获取 Token')
    return
  }

  userInfoLoading.value = true
  apiResult.value = null

  try {
    const res = await getUserInfoAPI()
    apiResult.value = {
      success: true,
      message: '获取用户信息成功',
      data: res.data
    }
  } catch (error: any) {
    apiResult.value = {
      success: false,
      message: '获取用户信息失败: ' + (error.message || error.toString())
    }
  } finally {
    userInfoLoading.value = false
  }
}

const testProductList = async () => {
  productLoading.value = true
  apiResult.value = null

  try {
    const res = await getProductListAPI({ pageNum: 1, pageSize: 5 })
    apiResult.value = {
      success: true,
      message: '获取商品列表成功',
      data: res.data
    }
  } catch (error: any) {
    apiResult.value = {
      success: false,
      message: '获取商品列表失败: ' + (error.message || error.toString())
    }
  } finally {
    productLoading.value = false
  }
}

const clearToken = () => {
  localStorage.removeItem('token')
  currentToken.value = ''
  ElMessage.info('Token 已清除')
}

onMounted(() => {
  // 默认选择第一个用户
  if (dbUsers.value.length > 0) {
    const firstUser = dbUsers.value[0]
    if (firstUser) {
      selectUser(firstUser)
    }
  }
})
</script>

<style scoped lang="scss">
.login-test-container {
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;
}

.test-card {
  background: white;
  border-radius: 8px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);

  h2 {
    margin: 0 0 24px;
    color: #333;
  }

  h3 {
    margin: 24px 0 16px;
    color: #555;
    font-size: 16px;
    border-bottom: 1px solid #eee;
    padding-bottom: 8px;
  }
}

.status-section, .db-users-section, .login-form-section,
.result-section, .api-test-section, .debug-section {
  margin-bottom: 24px;
}

.status-info {
  .status-item {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-bottom: 8px;

    .label {
      font-weight: 500;
      color: #666;
      width: 100px;
    }

    .value {
      font-family: monospace;
      color: #333;
      word-break: break-all;
    }
  }
}

.user-list {
  display: grid;
  gap: 8px;

  .user-item {
    border: 1px solid #ddd;
    border-radius: 4px;
    padding: 12px;
    cursor: pointer;
    transition: all 0.2s;

    &:hover {
      border-color: #409eff;
      background: #f0f9ff;
    }

    &.active {
      border-color: #409eff;
      background: #e1f3ff;
    }

    .user-info {
      display: flex;
      gap: 16px;
      align-items: center;

      strong {
        color: #333;
      }

      .phone, .email {
        color: #666;
        font-size: 14px;
      }
    }
  }
}

.result-card {
  border-radius: 4px;
  padding: 16px;
  margin-top: 12px;

  &.success {
    background: #f0f9ff;
    border: 1px solid #b3d8ff;
  }

  &.error {
    background: #fef0f0;
    border: 1px solid #fbc4c4;
  }

  .result-status {
    font-weight: 500;
    margin-bottom: 8px;
  }

  .result-message {
    color: #666;
    margin-bottom: 12px;
  }

  .result-data, .result-error {
    h4 {
      margin: 12px 0 8px;
      font-size: 14px;
    }

    pre {
      background: #f5f5f5;
      padding: 12px;
      border-radius: 4px;
      font-size: 12px;
      overflow-x: auto;
      margin: 0;
    }
  }
}

.api-buttons {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}

.debug-info {
  .debug-item {
    margin-bottom: 8px;
    font-size: 14px;

    strong {
      color: #333;
    }
  }
}
</style>

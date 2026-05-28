<template>
  <div class="simple-login-container">
    <div class="login-card">
      <h2>🔐 登录测试工具</h2>

      <!-- 状态信息 -->
      <div class="status-section">
        <div class="status-item">
          <span class="label">Mock 模式：</span>
          <el-tag :type="mockStatus.type">{{ mockStatus.text }}</el-tag>
        </div>
        <div class="status-item">
          <span class="label">当前 Token：</span>
          <span class="token-display">{{ tokenDisplay }}</span>
        </div>
      </div>

      <!-- 快速选择用户 -->
      <div class="user-section">
        <h3>📋 数据库用户（点击选择）</h3>
        <div class="user-buttons">
          <el-button
            v-for="user in testUsers"
            :key="user.username"
            :type="selectedUsername === user.username ? 'primary' : 'default'"
            @click="selectTestUser(user)"
          >
            {{ user.username }} ({{ user.phone }})
          </el-button>
        </div>
      </div>

      <!-- 登录表单 -->
      <div class="form-section">
        <h3>🚀 登录测试</h3>
        <el-form :model="loginData" label-width="80px">
          <el-form-item label="用户名">
            <el-input v-model="loginData.username" placeholder="请输入用户名" />
          </el-form-item>
          <el-form-item label="密码">
            <el-input
              v-model="loginData.password"
              type="password"
              placeholder="请输入密码"
              show-password
            />
          </el-form-item>
          <el-form-item>
            <el-button
              type="primary"
              @click="performLogin"
              :loading="isLoading"
              size="large"
            >
              {{ isLoading ? '登录中...' : '测试登录' }}
            </el-button>
            <el-button @click="clearAllData" size="large">
              清除数据
            </el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 测试结果 -->
      <div class="result-section" v-if="loginResult">
        <h3>📊 测试结果</h3>
        <el-alert
          :title="loginResult.title"
          :type="loginResult.type"
          :description="loginResult.message"
          show-icon
          :closable="false"
        />

        <div class="result-details" v-if="loginResult.details">
          <h4>详细信息：</h4>
          <el-input
            v-model="loginResult.details"
            type="textarea"
            :rows="6"
            readonly
          />
        </div>
      </div>

      <!-- API 测试按钮 -->
      <div class="api-section" v-if="hasToken">
        <h3>🔧 API 测试</h3>
        <div class="api-buttons">
          <el-button @click="testUserInfo" :loading="apiLoading">
            获取用户信息
          </el-button>
          <el-button @click="testProductList" :loading="apiLoading">
            获取商品列表
          </el-button>
        </div>

        <div class="api-result" v-if="apiTestResult">
          <el-alert
            :title="apiTestResult.title"
            :type="apiTestResult.type"
            :description="apiTestResult.message"
            show-icon
            :closable="false"
          />
        </div>
      </div>

      <!-- 快速链接 -->
      <div class="links-section">
        <h3>🔗 快速链接</h3>
        <div class="link-buttons">
          <el-button @click="goToPage('/debug/backend')" type="info">
            后端测试
          </el-button>
          <el-button @click="goToPage('/debug/access')" type="success">
            快速访问
          </el-button>
          <el-button @click="goToPage('/login')" type="warning">
            正常登录
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { USE_MOCK } from '@/api/useMock'
import { loginAPI, getUserInfoAPI } from '@/api/modules/user'
import { getProductListAPI } from '@/api/modules/product'

const router = useRouter()

// 响应式数据
const isLoading = ref(false)
const apiLoading = ref(false)
const selectedUsername = ref('')
const loginResult = ref<any>(null)
const apiTestResult = ref<any>(null)

// 登录表单数据
const loginData = ref({
  username: '',
  password: ''
})

// 测试用户数据
const testUsers = ref([
  { username: 'zhangsan', phone: '13800138000', password: 'password123' },
  { username: 'lisi', phone: '13900139000', password: 'password123' },
  { username: 'testuser', phone: '', password: 'password123' }
])

// 计算属性
const mockStatus = computed(() => {
  return USE_MOCK
    ? { type: 'warning', text: '启用 (虚拟数据)' }
    : { type: 'success', text: '禁用 (连接后端)' }
})

const tokenDisplay = computed(() => {
  const token = localStorage.getItem('token')
  return token ? `${token.substring(0, 20)}...` : '无'
})

const hasToken = computed(() => {
  return !!localStorage.getItem('token')
})

// 方法
const selectTestUser = (user: any) => {
  selectedUsername.value = user.username
  loginData.value.username = user.username
  loginData.value.password = user.password
  ElMessage.info(`已选择用户: ${user.username}`)
}

const performLogin = async () => {
  if (!loginData.value.username || !loginData.value.password) {
    ElMessage.error('请输入用户名和密码')
    return
  }

  isLoading.value = true
  loginResult.value = null

  try {
    console.log('🚀 发送登录请求:', loginData.value)

    const response = await loginAPI({
      username: loginData.value.username,
      password: loginData.value.password
    })

    console.log('✅ 登录响应:', response)

    // 处理登录成功
    if (response && response.data) {
      let token = ''

      if (typeof response.data === 'string') {
        token = response.data
      } else if (response.data.token) {
        token = response.data.token
      }

      if (token) {
        localStorage.setItem('token', token)
        ElMessage.success('登录成功！Token 已保存')

        loginResult.value = {
          type: 'success',
          title: '✅ 登录成功',
          message: '用户认证通过，Token 已保存到本地存储',
          details: JSON.stringify(response.data, null, 2)
        }
      } else {
        throw new Error('响应中未找到 token')
      }
    } else {
      throw new Error('登录响应数据为空')
    }

  } catch (error: any) {
    console.error('❌ 登录失败:', error)

    loginResult.value = {
      type: 'error',
      title: '❌ 登录失败',
      message: error.message || '登录过程中发生错误',
      details: `错误详情:\n${error.toString()}\n\n请检查:\n1. 用户名密码是否正确\n2. 后端服务是否启动\n3. 网络连接是否正常`
    }

    ElMessage.error('登录失败: ' + (error.message || '未知错误'))
  } finally {
    isLoading.value = false
  }
}

const testUserInfo = async () => {
  apiLoading.value = true
  apiTestResult.value = null

  try {
    const response = await getUserInfoAPI()
    apiTestResult.value = {
      type: 'success',
      title: '✅ 获取用户信息成功',
      message: `用户: ${response.data?.username || '未知'}`
    }
  } catch (error: any) {
    apiTestResult.value = {
      type: 'error',
      title: '❌ 获取用户信息失败',
      message: error.message || '请求失败'
    }
  } finally {
    apiLoading.value = false
  }
}

const testProductList = async () => {
  apiLoading.value = true
  apiTestResult.value = null

  try {
    const response = await getProductListAPI({ pageNum: 1, pageSize: 5 })
    const count = Array.isArray(response.data) ? response.data.length : 0
    apiTestResult.value = {
      type: 'success',
      title: '✅ 获取商品列表成功',
      message: `获取到 ${count} 个商品`
    }
  } catch (error: any) {
    apiTestResult.value = {
      type: 'error',
      title: '❌ 获取商品列表失败',
      message: error.message || '请求失败'
    }
  } finally {
    apiLoading.value = false
  }
}

const clearAllData = () => {
  localStorage.removeItem('token')
  loginResult.value = null
  apiTestResult.value = null
  selectedUsername.value = ''
  loginData.value = { username: '', password: '' }
  ElMessage.info('已清除所有数据')
}

const goToPage = (path: string) => {
  router.push(path)
}

// 生命周期
onMounted(() => {
  // 默认选择第一个用户
  if (testUsers.value.length > 0) {
    selectTestUser(testUsers.value[0])
  }
})
</script>

<style scoped lang="scss">
.simple-login-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.login-card {
  background: white;
  border-radius: 16px;
  padding: 32px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
  max-width: 600px;
  width: 100%;

  h2 {
    text-align: center;
    margin-bottom: 24px;
    color: #333;
    font-size: 24px;
  }

  h3 {
    margin: 24px 0 16px;
    color: #555;
    font-size: 16px;
    border-bottom: 1px solid #eee;
    padding-bottom: 8px;
  }
}

.status-section {
  margin-bottom: 24px;

  .status-item {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-bottom: 12px;

    .label {
      font-weight: 500;
      color: #666;
      width: 100px;
    }

    .token-display {
      font-family: monospace;
      font-size: 12px;
      color: #666;
      background: #f5f5f5;
      padding: 4px 8px;
      border-radius: 4px;
    }
  }
}

.user-section {
  margin-bottom: 24px;

  .user-buttons {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
  }
}

.form-section {
  margin-bottom: 24px;
}

.result-section {
  margin-bottom: 24px;

  .result-details {
    margin-top: 16px;

    h4 {
      margin-bottom: 8px;
      color: #333;
      font-size: 14px;
    }
  }
}

.api-section {
  margin-bottom: 24px;

  .api-buttons {
    display: flex;
    gap: 12px;
    margin-bottom: 16px;
  }
}

.links-section {
  .link-buttons {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
  }
}

@media (max-width: 768px) {
  .login-card {
    padding: 20px;
  }

  .user-buttons,
  .api-buttons,
  .link-buttons {
    flex-direction: column;
  }
}
</style>

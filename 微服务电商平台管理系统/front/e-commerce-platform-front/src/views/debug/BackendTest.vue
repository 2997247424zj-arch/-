<template>
  <div class="backend-test-container">
    <h2 class="page-title">后端连接测试</h2>

    <!-- 连接状态 -->
    <div class="status-card glass-panel">
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
          <span class="label">连接状态：</span>
          <el-tag :type="connectionStatus === 'connected' ? 'success' : 'danger'">
            {{ connectionStatusText }}
          </el-tag>
          <el-button size="small" @click="checkConnection" :loading="checking">
            重新检测
          </el-button>
        </div>
      </div>
    </div>

    <!-- API 测试 -->
    <div class="test-card glass-panel">
      <h3>API 接口测试</h3>
      <div class="test-grid">
        <div class="test-item" v-for="test in apiTests" :key="test.name">
          <div class="test-header">
            <span class="test-name">{{ test.name }}</span>
            <el-button
              size="small"
              type="primary"
              @click="runTest(test)"
              :loading="test.loading"
            >
              测试
            </el-button>
          </div>
          <div class="test-info">
            <div class="method-url">
              <el-tag size="small" :type="getMethodType(test.method)">{{ test.method }}</el-tag>
              <span class="url">{{ test.url }}</span>
            </div>
            <div class="test-result" v-if="test.result">
              <div class="result-status" :class="test.result.success ? 'success' : 'error'">
                {{ test.result.success ? '✓ 成功' : '✗ 失败' }}
              </div>
              <div class="result-message">{{ test.result.message }}</div>
              <div class="result-data" v-if="test.result.data">
                <pre>{{ JSON.stringify(test.result.data, null, 2) }}</pre>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 切换模式 -->
    <div class="switch-card glass-panel">
      <h3>模式切换</h3>
      <div class="switch-options">
        <el-alert
          title="提示"
          description="切换模式后需要刷新页面才能生效"
          type="info"
          :closable="false"
        />
        <div class="switch-buttons">
          <el-button
            type="primary"
            :disabled="!useMock"
            @click="switchToBackend"
          >
            切换到后端模式
          </el-button>
          <el-button
            type="warning"
            :disabled="useMock"
            @click="switchToMock"
          >
            切换到 Mock 模式
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { USE_MOCK } from '@/api/useMock'
import { getBackendUrl } from '@/config/backend'
import { getProductListAPI } from '@/api/modules/product'
import { getUserInfoAPI } from '@/api/modules/user'

const useMock = ref(USE_MOCK)
const backendUrl = ref(getBackendUrl())
const connectionStatus = ref<'connected' | 'disconnected' | 'checking'>('checking')
const checking = ref(false)

const connectionStatusText = computed(() => {
  const statusMap = {
    connected: '已连接',
    disconnected: '连接失败',
    checking: '检测中...',
  }
  return statusMap[connectionStatus.value]
})

interface ApiTest {
  name: string
  method: string
  url: string
  fn: () => Promise<any>
  loading: boolean
  result?: {
    success: boolean
    message: string
    data?: any
  }
}

const apiTests = ref<ApiTest[]>([
  {
    name: '商品列表',
    method: 'GET',
    url: '/api/product/list',
    fn: () => getProductListAPI({ pageNum: 1, pageSize: 10 }),
    loading: false,
  },
  {
    name: '用户信息',
    method: 'GET',
    url: '/api/user/info',
    fn: () => getUserInfoAPI(),
    loading: false,
  },
  {
    name: '后端健康检查',
    method: 'GET',
    url: '/actuator/health',
    fn: async () => {
      const response = await fetch('http://localhost:8080/actuator/health')
      if (!response.ok) throw new Error('Health check failed')
      return { data: await response.json() }
    },
    loading: false,
  },
])

const checkConnection = async () => {
  checking.value = true
  connectionStatus.value = 'checking'

  try {
    // 首先尝试健康检查
    const healthResponse = await fetch('http://localhost:8080/actuator/health')
    if (healthResponse.ok) {
      connectionStatus.value = 'connected'
    } else {
      throw new Error('Health check failed')
    }
  } catch (error) {
    try {
      // 如果健康检查失败，尝试调用商品列表API
      await getProductListAPI({ pageNum: 1, pageSize: 1 })
      connectionStatus.value = 'connected'
    } catch (apiError) {
      connectionStatus.value = 'disconnected'
    }
  } finally {
    checking.value = false
  }
}

const runTest = async (test: ApiTest) => {
  test.loading = true
  test.result = undefined

  try {
    const result = await test.fn()
    test.result = {
      success: true,
      message: '请求成功',
      data: result.data,
    }
  } catch (error: any) {
    test.result = {
      success: false,
      message: error.message || '请求失败',
    }
  } finally {
    test.loading = false
  }
}

const getMethodType = (method: string) => {
  const typeMap: Record<string, string> = {
    GET: 'success',
    POST: 'primary',
    PUT: 'warning',
    DELETE: 'danger',
  }
  return typeMap[method] || 'info'
}

const switchToBackend = () => {
  ElMessage.info('请手动修改 src/api/useMock.ts 中的 USE_MOCK 为 false，然后刷新页面')
}

const switchToMock = () => {
  ElMessage.info('请手动修改 src/api/useMock.ts 中的 USE_MOCK 为 true，然后刷新页面')
}

onMounted(() => {
  checkConnection()
})
</script>

<style scoped lang="scss">
.backend-test-container {
  .page-title {
    margin: 0 0 24px;
    font-size: 24px;
    font-weight: 700;
    color: #1e293b;
  }
}

.glass-panel {
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  margin-bottom: 24px;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05);

  h3 {
    margin: 0 0 20px;
    font-size: 18px;
    font-weight: 600;
    color: #1e293b;
    padding-bottom: 12px;
    border-bottom: 1px solid #f1f5f9;
  }
}

.status-info {
  .status-item {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-bottom: 12px;

    .label {
      font-weight: 500;
      color: #64748b;
      width: 80px;
    }

    .value {
      color: #334155;
      font-family: monospace;
    }
  }
}

.test-grid {
  display: grid;
  gap: 20px;

  .test-item {
    border: 1px solid #e2e8f0;
    border-radius: 8px;
    padding: 16px;

    .test-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 12px;

      .test-name {
        font-weight: 500;
        color: #334155;
      }
    }

    .test-info {
      .method-url {
        display: flex;
        align-items: center;
        gap: 8px;
        margin-bottom: 12px;

        .url {
          font-family: monospace;
          color: #64748b;
          font-size: 13px;
        }
      }

      .test-result {
        .result-status {
          font-weight: 500;
          margin-bottom: 8px;

          &.success {
            color: #10b981;
          }

          &.error {
            color: #ef4444;
          }
        }

        .result-message {
          color: #64748b;
          font-size: 14px;
          margin-bottom: 8px;
        }

        .result-data {
          background: #f8fafc;
          border-radius: 4px;
          padding: 12px;
          max-height: 200px;
          overflow-y: auto;

          pre {
            margin: 0;
            font-size: 12px;
            color: #334155;
          }
        }
      }
    }
  }
}

.switch-options {
  .switch-buttons {
    margin-top: 16px;
    display: flex;
    gap: 12px;
  }
}
</style>

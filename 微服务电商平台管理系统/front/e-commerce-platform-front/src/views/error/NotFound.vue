<template>
  <div class="not-found-container">
    <div class="content">
      <div class="error-code">404</div>
      <h1 class="title">页面未找到</h1>
      <p class="description">抱歉，您访问的页面不存在或已被移除</p>
      <div class="actions">
        <el-button type="primary" size="large" round @click="router.push('/')">
          返回首页
        </el-button>
        <el-button size="large" round @click="router.back()">
          返回上页
        </el-button>
      </div>
    </div>
    <div class="illustration">
      <el-icon :size="200" color="#e2e8f0"><Box /></el-icon>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { Box } from '@element-plus/icons-vue'

const router = useRouter()
</script>

<style scoped lang="scss">
.not-found-container {
  min-height: calc(100vh - 140px);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 80px;
  padding: 40px;

  .content {
    text-align: center;

    .error-code {
      font-size: 120px;
      font-weight: 900;
      background: linear-gradient(135deg, #4f46e5, #ec4899);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
      line-height: 1;
      margin-bottom: 16px;
    }

    .title {
      font-size: 32px;
      font-weight: 700;
      color: #1e293b;
      margin: 0 0 12px;
    }

    .description {
      font-size: 16px;
      color: #64748b;
      margin: 0 0 32px;
    }

    .actions {
      display: flex;
      gap: 16px;
      justify-content: center;
    }
  }

  .illustration {
    opacity: 0.5;
  }
}

@media (max-width: 768px) {
  .not-found-container {
    flex-direction: column;
    gap: 40px;

    .illustration {
      display: none;
    }

    .content {
      .error-code {
        font-size: 80px;
      }

      .title {
        font-size: 24px;
      }
    }
  }
}
</style>

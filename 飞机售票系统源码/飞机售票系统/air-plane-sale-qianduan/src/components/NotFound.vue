<template>
  <div class="not-found-container">
    <div class="not-found-content">
      <div class="error-code">404</div>
      <h1>页面未找到</h1>
      <p>抱歉，您访问的页面不存在或已被删除</p>
      
      <div class="error-illustration">
        <span class="illustration-icon">🔍</span>
      </div>
      
      <div class="action-buttons">
        <button class="btn-primary" @click="goHome">返回首页</button>
        <button class="btn-secondary" @click="goBack">返回上一页</button>
      </div>
      
      <div class="helpful-links">
        <p>快速导航：</p>
        <div class="links-grid">
          <router-link to="/" class="link-item">首页</router-link>
          <router-link to="/dashboard" class="link-item">管理后台</router-link>
          <router-link to="/portal/orders" class="link-item">订单管理</router-link>
          <router-link to="/portal/flights" class="link-item">航班管理</router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()

const goHome = () => {
  router.push('/')
}

const goBack = () => {
  router.back()
}
</script>

<style scoped>
.not-found-container {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #030F1F 0%, #081C35 100%);
  color: #fff;
  padding: 20px;
}

.not-found-content {
  text-align: center;
  max-width: 600px;
}

.error-code {
  font-size: 120px;
  font-weight: 900;
  background: linear-gradient(135deg, #1E8AE6, #0A2F63);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin-bottom: 20px;
  line-height: 1;
}

h1 {
  font-size: 48px;
  margin: 0 0 16px 0;
  font-weight: 700;
}

p {
  font-size: 18px;
  color: rgba(255, 255, 255, 0.7);
  margin: 0 0 40px 0;
}

.error-illustration {
  margin: 40px 0;
  font-size: 80px;
  animation: float 3s ease-in-out infinite;
}

.illustration-icon {
  display: inline-block;
}

.action-buttons {
  display: flex;
  gap: 16px;
  justify-content: center;
  margin-bottom: 40px;
  flex-wrap: wrap;
}

.btn-primary,
.btn-secondary {
  padding: 12px 32px;
  border-radius: 8px;
  border: none;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn-primary {
  background: linear-gradient(135deg, #1E8AE6, #0A2F63);
  color: #fff;
  box-shadow: 0 10px 25px rgba(99, 102, 241, 0.4);
}

.btn-primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 15px 35px rgba(99, 102, 241, 0.5);
}

.btn-secondary {
  background: rgba(255, 255, 255, 0.1);
  color: #fff;
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.btn-secondary:hover {
  background: rgba(255, 255, 255, 0.15);
  border-color: rgba(255, 255, 255, 0.3);
}

.helpful-links {
  margin-top: 40px;
  padding-top: 40px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

.helpful-links p {
  margin-bottom: 20px;
  font-size: 16px;
}

.links-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(140px, 1fr));
  gap: 12px;
}

.link-item {
  padding: 12px 16px;
  background: rgba(99, 102, 241, 0.1);
  border: 1px solid rgba(99, 102, 241, 0.3);
  border-radius: 8px;
  color: #4FB5FF;
  text-decoration: none;
  transition: all 0.3s ease;
  display: inline-block;
}

.link-item:hover {
  background: rgba(99, 102, 241, 0.2);
  border-color: rgba(99, 102, 241, 0.5);
  transform: translateY(-2px);
}

@keyframes float {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-20px);
  }
}

@media (max-width: 768px) {
  .error-code {
    font-size: 80px;
  }

  h1 {
    font-size: 32px;
  }

  p {
    font-size: 16px;
  }

  .error-illustration {
    font-size: 60px;
  }

  .action-buttons {
    flex-direction: column;
  }

  .btn-primary,
  .btn-secondary {
    width: 100%;
  }
}
</style>


<template>
  <div class="easy-test">
    <div class="test-box">
      <h1>🚀 快速测试面板</h1>

      <div class="status">
        <h3>当前状态</h3>
        <p>Mock 模式: <strong style="color: red;">已禁用</strong></p>
        <p>后端连接: <strong style="color: green;">已启用</strong></p>
        <p>路由守卫: <strong style="color: green;">已禁用</strong></p>
        <p>Token 验证: <strong style="color: orange;">已关闭</strong></p>
        <p>验证方式: <strong style="color: blue;">表单验证</strong></p>
      </div>

      <div class="actions">
        <h3>快速操作</h3>
        <button @click="autoLogin" class="btn btn-primary">
          🔐 自动登录 (Mock)
        </button>
        <button @click="setToken" class="btn btn-success">
          ✅ 设置测试 Token
        </button>
        <button @click="clearToken" class="btn btn-danger">
          ❌ 清除 Token
        </button>
      </div>

      <div class="navigation">
        <h3>页面导航</h3>
        <div class="nav-grid">
          <button @click="goTo('/debug/connection')" class="btn btn-test">🔌 后端连接测试</button>
          <button @click="goTo('/home')" class="btn">🏠 首页</button>
          <button @click="goTo('/dashboard')" class="btn">📊 数据看板</button>
          <button @click="goTo('/product/list')" class="btn">🛍️ 商品列表</button>
          <button @click="goTo('/cart')" class="btn">🛒 购物车</button>
          <button @click="goTo('/order/list')" class="btn">📦 订单列表</button>
          <button @click="goTo('/user')" class="btn">👤 个人中心</button>
          <button @click="goTo('/user/address')" class="btn">📍 地址管理</button>
          <button @click="goTo('/login')" class="btn">🔑 登录页面</button>
        </div>
      </div>

      <div class="info">
        <h3>💡 说明</h3>
        <ul>
          <li>✅ Mock 模式已禁用，连接真实后端</li>
          <li>✅ Token 验证已关闭，使用表单验证</li>
          <li>✅ 路由守卫已禁用，可以直接访问所有页面</li>
          <li>🔌 点击"后端连接测试"检查前后端对接状态</li>
          <li>✅ 所有页面都可以正常访问和测试</li>
        </ul>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { loginAPI } from '@/api/modules/user'

const router = useRouter()
const hasToken = ref(false)

const checkToken = () => {
  hasToken.value = !!localStorage.getItem('token')
}

const autoLogin = async () => {
  try {
    const res = await loginAPI({
      username: 'test',
      password: 'test'
    })

    if (res.data && res.data.token) {
      localStorage.setItem('token', res.data.token)
      hasToken.value = true
      alert('✅ 自动登录成功！Token 已设置')
    }
  } catch (error) {
    alert('❌ 登录失败: ' + error)
  }
}

const setToken = () => {
  const token = 'test-token-' + Date.now()
  localStorage.setItem('token', token)
  hasToken.value = true
  alert('✅ Token 已设置: ' + token)
}

const clearToken = () => {
  localStorage.removeItem('token')
  hasToken.value = false
  alert('✅ Token 已清除')
}

const goTo = (path: string) => {
  router.push(path)
}

onMounted(() => {
  checkToken()
})
</script>

<style scoped>
.easy-test {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.test-box {
  background: white;
  border-radius: 20px;
  padding: 40px;
  max-width: 800px;
  width: 100%;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
}

h1 {
  text-align: center;
  color: #333;
  margin-bottom: 30px;
  font-size: 32px;
}

h3 {
  color: #555;
  margin: 20px 0 15px;
  font-size: 18px;
  border-bottom: 2px solid #eee;
  padding-bottom: 10px;
}

.status p {
  margin: 10px 0;
  font-size: 16px;
  color: #666;
}

.actions {
  margin: 30px 0;
}

.btn {
  padding: 12px 24px;
  margin: 5px;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  cursor: pointer;
  transition: all 0.3s;
  font-weight: 500;
}

.btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
}

.btn-primary {
  background: #409eff;
  color: white;
}

.btn-success {
  background: #67c23a;
  color: white;
}

.btn-danger {
  background: #f56c6c;
  color: white;
}

.nav-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 10px;
  margin-top: 15px;
}

.nav-grid .btn {
  background: #f0f0f0;
  color: #333;
  width: 100%;
}

.nav-grid .btn:hover {
  background: #409eff;
  color: white;
}

.btn-test {
  background: #67c23a !important;
  color: white !important;
  font-weight: bold;
}

.btn-test:hover {
  background: #5daf34 !important;
}

.info {
  margin-top: 30px;
  background: #f9f9f9;
  padding: 20px;
  border-radius: 10px;
}

.info ul {
  margin: 10px 0;
  padding-left: 20px;
}

.info li {
  margin: 8px 0;
  color: #666;
  line-height: 1.6;
}

@media (max-width: 768px) {
  .test-box {
    padding: 20px;
  }

  h1 {
    font-size: 24px;
  }

  .nav-grid {
    grid-template-columns: 1fr;
  }
}
</style>

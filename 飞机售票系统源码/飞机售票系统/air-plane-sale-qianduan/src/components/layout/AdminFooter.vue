<template>
  <footer class="admin-footer">
    <div class="footer-content">
      <div class="footer-main">
        <div class="footer-section">
          <div class="footer-brand">
            <div class="logo-emblem">✈️</div>
            <div>
              <h3 class="footer-title">飞机售票系统</h3>
              <p class="footer-subtitle">Air Command Center</p>
            </div>
          </div>
          <p class="footer-description">专业的航空票务管理平台，为您提供便捷的机票预订服务</p>
        </div>
        <div class="footer-section">
          <h4 class="footer-heading">服务支持</h4>
          <ul class="footer-links">
            <li><a href="#" @click.prevent="goToTerms">服务条款</a></li>
       
            <li><a href="#" @click.prevent>帮助中心</a></li>
            <li>
              <a href="https://www.coze.cn/store/agent/7589114565770641442?bot_id=true" target="_blank" rel="noopener">在线客服</a>
            </li>
          </ul>
        </div>
        <div class="footer-section">
          <h4 class="footer-heading">关于我们</h4>
          <ul class="footer-links">
            <li><a href="#" @click.prevent="goToMembers">成员简介</a></li>
            <li><a href="#" @click.prevent="goToTerms">服务条款</a></li>
            <li><a href="#" @click.prevent="goToPrivacy">隐私政策</a></li>
          </ul>
        </div>
        <div class="footer-section">
          <h4 class="footer-heading">联系方式</h4>
          <div class="contact-info">
            <p class="contact-item">
              <span class="contact-icon">📞</span>
              <span>服务热线：400-888-8888</span>
            </p>
            <p class="contact-item">
              <span class="contact-icon">✉️</span>
              <span>邮箱：support@airline.com</span>
            </p>
            <p class="contact-item">
              <span class="contact-icon">🕐</span>
              <span>工作时间：9:00-18:00（周一至周五）</span>
            </p>
          </div>
        </div>
      </div>
      <div class="footer-bottom">
        <p>&copy; {{ currentYear }} 飞机售票系统. 保留所有权利.</p>
        <div class="footer-social">
          <span>关注我们：</span>
          <a href="#" class="social-link" @click.prevent="openExternal('https://weixin.qq.com')">微信</a>
          <a href="#" class="social-link" @click.prevent="openExternal('https://weibo.com')">微博</a>
          <a href="#" class="social-link" @click.prevent="openExternal('https://www.example-airline.com')">官网</a>
        </div>
      </div>
    </div>
  </footer>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const currentYear = computed(() => new Date().getFullYear())

const goToTerms = () => router.push('/about/terms')
const goToPrivacy = () => router.push('/about/privacy')
const goToMembers = () => router.push('/about/members')

const openExternal = (url: string) => {
  window.open(url, '_blank', 'noopener')
}
</script>

<style scoped>
.admin-footer {
  background: linear-gradient(180deg, var(--bg-secondary, #EFF3FF), var(--bg-primary, #F7FAFF));
  border-top: 1px solid var(--border-light, rgba(10,31,51,0.06));
  padding: 40px 20px 24px;
  margin-top: auto;
  position: relative;
  backdrop-filter: blur(6px) saturate(120%);
  z-index: 1;
  box-shadow: 0 -6px 18px rgba(16,24,40,0.03);
}

.admin-footer::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(90deg, transparent, color-mix(in srgb, var(--color-primary, #2563eb) 18%, transparent), transparent);
}

.footer-content {
  max-width: 1400px;
  margin: 0 auto;
}

.footer-main {
  display: grid;
  grid-template-columns: 2fr 1fr 1fr 1.5fr;
  gap: 32px;
  margin-bottom: 28px;
}

.footer-section {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.footer-brand {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.logo-emblem {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  background: linear-gradient(135deg, color-mix(in srgb, var(--color-primary, #2563eb) 18%, #ffffff), color-mix(in srgb, var(--color-primary, #2563eb) 6%, transparent));
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  border: 1px solid color-mix(in srgb, var(--color-primary, #2563eb) 14%, transparent);
}

.footer-title {
  margin: 0;
  font-size: 18px;
  font-weight: 700;
  color: var(--text-primary);
}

.footer-subtitle {
  margin: 4px 0 0;
  font-size: 12px;
  color: var(--text-secondary);
  font-weight: 500;
  letter-spacing: 0.04em;
}

.footer-description {
  margin: 0;
  color: var(--text-secondary);
  font-size: 14px;
  line-height: 1.6;
}

.footer-heading {
  margin: 0 0 10px 0;
  font-size: 15px;
  font-weight: 700;
  color: var(--text-primary);
}

.footer-links {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.footer-links li a {
  color: var(--text-secondary);
  text-decoration: none;
  font-size: 14px;
  transition: all 0.18s ease;
  display: inline-block;
}
.footer-links li a:hover {
  color: var(--color-primary);
  transform: translateX(6px);
}

.contact-info {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.contact-item {
  margin: 0;
  color: var(--text-secondary);
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 10px;
  line-height: 1.6;
}

.contact-icon {
  font-size: 16px;
  opacity: 0.8;
}

.footer-bottom {
  padding-top: 30px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 20px;
}

.footer-bottom p {
  margin: 0;
  color: rgba(255, 255, 255, 0.5);
  font-size: 13px;
}

.footer-social {
  display: flex;
  align-items: center;
  gap: 12px;
  color: rgba(255, 255, 255, 0.5);
  font-size: 13px;
}

.social-link {
  color: rgba(16, 13, 212, 0.6);
  text-decoration: none;
  font-size: 13px;
  padding: 4px 12px;
  border-radius: 6px;
  border: 1px solid rgba(0, 50, 249, 0.1);
  transition: all 0.3s ease;
}

.social-link:hover {
  color: #1E8AE6;
  border-color: rgba(30, 138, 230, 0.4);
  background: rgba(30, 138, 230, 0.1);
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .footer-main {
    grid-template-columns: repeat(2, 1fr);
    gap: 30px;
  }
}

@media (max-width: 768px) {
  .admin-footer {
    padding: 40px 16px 24px;
  }

  .footer-main {
    grid-template-columns: 1fr;
    gap: 30px;
  }

  .footer-bottom {
    flex-direction: column;
    text-align: center;
    gap: 16px;
  }

  .footer-social {
    justify-content: center;
  }
}
</style>


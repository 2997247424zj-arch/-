<template>
  <footer class="passenger-footer">
    <div class="footer-content">
      <div class="footer-main">
        <div class="footer-section">
          <div class="footer-brand">
            <div class="logo-emblem">✈️</div>
            <div>
              <h3 class="footer-title">{{ t('layout.brand') }}</h3>
              <p class="footer-subtitle">Air Command Center</p>
            </div>
          </div>
          <p class="footer-description">{{ t('layout.footer.description') }}</p>
        </div>
        <div class="footer-section">
          <h4 class="footer-heading">服务支持</h4>
          <ul class="footer-links">
            <li><a href="#" @click.prevent>帮助中心</a></li>
            <li><a href="#" @click.prevent>常见问题</a></li>
            <li><a href="#" @click.prevent>在线客服</a></li>
            
          </ul>
        </div>
        <div class="footer-section">
          <h4 class="footer-heading">关于我们</h4>
          <ul class="footer-links">
            <li><a href="#" @click.prevent>成员简介</a></li>
            <li><a href="#" @click.prevent>服务条款</a></li>
            <li><a href="#" @click.prevent>隐私政策</a></li>
            <li><a href="#" @click.prevent>加入我们</a></li>
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
              <span>邮箱：3440481285qq.com</span>
            </p>
            <p class="contact-item">
              <span class="contact-icon">🕐</span>
              <span>工作时间：9:00-18:00</span>
            </p>
          </div>
        </div>
      </div>
      <div class="footer-bottom">
        <p>&copy; {{ currentYear }} {{ t('layout.brand') }}. {{ t('layout.footer.copyright') }}</p>
        <div class="footer-social">
          <span>关注我们：</span>
          <a href="#" class="social-link" @click.prevent>微信</a>
          <a href="#" class="social-link" @click.prevent>微博</a>
          <a href="#" class="social-link" @click.prevent>官网</a>
        </div>
      </div>
    </div>
  </footer>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from '../../services/i18n'

const { t } = useI18n()
const currentYear = computed(() => new Date().getFullYear())
</script>

<style scoped>
.passenger-footer {
  background: linear-gradient(135deg, rgba(201, 215, 252, 0.95), rgba(129, 166, 215, 0.98));
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  padding: 60px 20px 30px;
  margin-top: auto;
  position: relative;
  backdrop-filter: blur(20px);
}

.passenger-footer::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(135, 206, 235, 0.5), transparent);
}

.footer-content {
  max-width: 1400px;
  margin: 0 auto;
}

.footer-main {
  display: grid;
  grid-template-columns: 2fr 1fr 1fr 1.5fr;
  gap: 40px;
  margin-bottom: 40px;
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
  background: linear-gradient(135deg, rgba(30, 138, 230, 0.2), rgba(59, 130, 246, 0.2));
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  border: 1px solid rgba(135, 206, 235, 0.3);
}

.footer-title {
  margin: 0;
  font-size: 20px;
  font-weight: 700;
  background: linear-gradient(135deg, #1E8AE6, #4FB5FF);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.footer-subtitle {
  margin: 4px 0 0;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.5);
  font-weight: 500;
  letter-spacing: 0.05em;
}

.footer-description {
  margin: 0;
  color: rgba(255, 255, 255, 0.6);
  font-size: 14px;
  line-height: 1.6;
}

.footer-heading {
  margin: 0 0 12px 0;
  font-size: 16px;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.9);
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
  color: rgba(255, 255, 255, 0.6);
  text-decoration: none;
  font-size: 14px;
  transition: all 0.3s ease;
  display: inline-block;
}

.footer-links li a:hover {
  color: #1E8AE6;
  transform: translateX(4px);
}

.contact-info {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.contact-item {
  margin: 0;
  color: rgba(255, 255, 255, 0.6);
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
  color: rgba(255, 255, 255, 0.6);
  text-decoration: none;
  font-size: 13px;
  padding: 4px 12px;
  border-radius: 6px;
  border: 1px solid rgba(255, 255, 255, 0.1);
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
  .passenger-footer {
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


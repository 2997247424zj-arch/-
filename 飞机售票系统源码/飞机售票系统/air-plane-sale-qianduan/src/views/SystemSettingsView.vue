<template>
  <AdminLayout>
    <!-- 面包屑导航 -->
    <div class="breadcrumb">
      <span>首页</span>
      <span class="breadcrumb-separator">/</span>
      <span>系统设置</span>
    </div>

    <div v-if="adminPrefs.showAuditTips" class="audit-banner">
      <strong>提示：</strong>根据您的偏好，我们为系统管理员展示了操作提醒与发布建议，可在下方个性化面板关闭。
    </div>

    <section class="glass-card admin-personalization" ref="adminPersonalizationSectionRef">
      <header class="section-head">
        <div>
          <p class="section-label">个性化设置</p>
          <h2>系统管理员偏好</h2>
        </div>
        <div class="panel-actions">
          <button class="ghost-btn danger" @click="resetAdminPrefs">重置个性化</button>
        </div>
      </header>
      <div class="personalization-grid">
        
        <div class="theme-column">
          <details open>
            <summary>界面主题与展示密度</summary>
            <div class="theme-settings-wrapper">
              <ThemeSettings />
            </div>
          </details>
        </div>
      </div>
    </section>

  </AdminLayout>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount } from 'vue'
const adminPersonalizationSectionRef = ref<HTMLElement | null>(null)
import AdminLayout from '../components/AdminLayout.vue'
import ThemeSettings from '../components/ThemeSettings.vue'
import { useRolePersonalization } from '../composables/useRolePersonalization'

const { preferences: adminPrefs, resetPreferences: resetAdminPrefs } = useRolePersonalization('system_settings', {
  defaultTab: 'basic',
  showAuditTips: true,
  stickyActions: true,
  remindBeforeSave: true
})

const focusAdminPersonalization = () => {
  adminPersonalizationSectionRef.value?.scrollIntoView({ behavior: 'smooth', block: 'start' })
  adminPersonalizationSectionRef.value?.classList.add('highlight-personalization')
  setTimeout(() => {
    adminPersonalizationSectionRef.value?.classList.remove('highlight-personalization')
  }, 1600)
}

const handlePersonalizationShortcut = () => {
  focusAdminPersonalization()
}

onMounted(() => {
  window.addEventListener('personalization-shortcut', handlePersonalizationShortcut)
})

onBeforeUnmount(() => {
  window.removeEventListener('personalization-shortcut', handlePersonalizationShortcut)
})
</script>

<style scoped>
/* 基础样式内联 */
.breadcrumb {
  margin-bottom: 20px;
  font-size: 14px;
  color: rgba(255, 255, 255, 0.7);
}

.breadcrumb-separator {
  margin: 0 8px;
  color: rgba(255, 255, 255, 0.4);
}

.audit-banner {
  margin-bottom: 16px;
  padding: 12px 18px;
  border-radius: 12px;
  border: 1px solid rgba(59, 130, 246, 0.3);
  background: rgba(59, 130, 246, 0.15);
  color: rgba(248, 250, 252, 0.9);
  font-size: 0.9rem;
}

.admin-personalization {
  margin-bottom: 20px;
  /* 进入时轻微上滑+渐显，突出系统管理的“控制面板”感 */
  animation: settings-fade-in 0.5s ease-out;
}

.admin-personalization.highlight-personalization {
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.4);
  transition: box-shadow 0.3s ease, transform 0.3s ease;
  transform: translateY(-2px);
}

.panel-actions {
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
}

.panel-actions .ghost-btn.danger {
  border-color: rgba(248, 113, 113, 0.4);
  color: #f87171;
}

.personalization-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 1.5rem;
}

.preferences-column {
  flex: 1 1 280px;
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.preferences-column select {
  padding: 0.65rem 0.8rem;
  border: 1px solid rgba(255, 255, 255, 0.15);
  border-radius: 10px;
  background: rgba(15, 23, 42, 0.6);
  color: #fff;
}

.toggle-list {
  display: flex;
  flex-direction: column;
  gap: 0.6rem;
}

.checkbox {
  display: flex;
  align-items: center;
  gap: 0.4rem;
  font-size: 0.9rem;
  color: rgba(248, 250, 252, 0.85);
}

.checkbox input {
  width: 16px;
  height: 16px;
  accent-color: #1E8AE6;
}

.theme-column {
  flex: 1 1 360px;
}

.theme-column details {
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 18px;
  padding: 1rem 1.2rem;
  background: rgba(1, 5, 10, 0.45);
  transition: border-color 0.3s ease, background 0.3s ease, transform 0.3s ease, box-shadow 0.3s ease;
}

.theme-column details[open] {
  box-shadow: 0 10px 28px rgba(15, 23, 42, 0.65);
  background: rgba(123, 141, 221, 0.85);
}

.theme-column summary {
  cursor: pointer;
  font-weight: 600;
  color: rgba(248, 250, 252, 0.9);
  margin-bottom: 0.8rem;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.theme-column summary::after {
  content: '⌄';
  font-size: 0.8rem;
  opacity: 0.7;
  transition: transform 0.3s ease, opacity 0.3s ease;
}

.theme-column details[open] summary::after {
  transform: rotate(180deg);
  opacity: 1;
}

.theme-settings-wrapper {
  max-height: 420px;
  overflow-y: auto;
  padding-right: 0.5rem;
}

/* 轻盈的进场动画 */
@keyframes settings-fade-in {
  from {
    opacity: 0;
    transform: translateY(12px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.btn-primary,
.btn-secondary {
  padding: 8px 16px;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s;
}

.btn-primary {
  background: linear-gradient(135deg, #1E8AE6, #0A2F63);
  color: #fff;
}

.btn-primary:hover {
  transform: translateY(-2px);
}

.btn-secondary {
  background: rgba(15, 23, 42, 0.6);
  color: rgba(255, 255, 255, 0.8);
  border: 1px solid rgba(255, 255, 255, 0.15);
}

.ghost-btn {
  padding: 0.55rem 1.2rem;
  border-radius: 999px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  background: transparent;
  color: rgba(248, 250, 252, 0.85);
  cursor: pointer;
  transition: all 0.3s;
}

.ghost-btn:hover {
  border-color: rgba(255, 255, 255, 0.4);
  color: #fff;
}

/* 设置标签页 */
.settings-tabs {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.tab-btn {
  padding: 12px 24px;
  background: transparent;
  border: none;
  border-bottom: 2px solid transparent;
  color: rgba(255, 255, 255, 0.7);
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s;
}

.tab-btn:hover {
  color: rgba(255, 255, 255, 0.9);
}

.tab-btn.active {
  color: #0A2F63;
  border-bottom-color: #0A2F63;
}

/* 设置表单 */
.settings-form {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.form-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-item label {
  font-size: 14px;
  font-weight: 500;
  color: rgba(255, 255, 255, 0.9);
}

.form-item input,
.form-item textarea {
  padding: 10px 14px;
  border: 1px solid rgba(255, 255, 255, 0.15);
  border-radius: 8px;
  background: rgba(15, 23, 42, 0.6);
  color: #fff;
  font-size: 14px;
  transition: all 0.3s;
}

.form-item input:focus,
.form-item textarea:focus {
  outline: none;
  border-color: rgba(99, 102, 241, 0.6);
  box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.1);
}

.form-item textarea {
  resize: vertical;
  font-family: inherit;
}

.input-group {
  display: flex;
  align-items: center;
  gap: 8px;
}

.input-prefix,
.input-suffix {
  color: rgba(255, 255, 255, 0.6);
  font-size: 14px;
  white-space: nowrap;
}

.input-group input {
  flex: 1;
}

/* 开关 */
.switch-group {
  display: flex;
  align-items: center;
  gap: 12px;
}

.switch {
  position: relative;
  display: inline-block;
  width: 44px;
  height: 24px;
}

.switch input {
  opacity: 0;
  width: 0;
  height: 0;
}

.slider {
  position: absolute;
  cursor: pointer;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(255, 255, 255, 0.2);
  transition: 0.3s;
  border-radius: 24px;
}

.slider:before {
  position: absolute;
  content: "";
  height: 18px;
  width: 18px;
  left: 3px;
  bottom: 3px;
  background-color: #fff;
  transition: 0.3s;
  border-radius: 50%;
}

input:checked + .slider {
  background-color: #1E8AE6;
}

input:checked + .slider:before {
  transform: translateX(20px);
}

.switch-label {
  color: rgba(255, 255, 255, 0.7);
  font-size: 13px;
}

/* 复选框组 */
.checkbox-group {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.checkbox-item {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.checkbox-item input[type="checkbox"] {
  width: 18px;
  height: 18px;
  cursor: pointer;
}

.checkbox-item span {
  color: rgba(255, 255, 255, 0.8);
  font-size: 14px;
}

/* 上传区域 */
.upload-area {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.upload-area input[type="file"] {
  padding: 8px;
}

.upload-hint {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.5);
}

/* 保存按钮 */
.settings-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

.settings-actions.sticky {
  position: sticky;
  bottom: 0;
  background: rgba(2, 6, 23, 0.9);
  padding: 20px;
  border-radius: 20px;
  box-shadow: 0 -10px 30px rgba(2, 6, 23, 0.4);
  z-index: 5;
}

@media (max-width: 768px) {
  .personalization-grid {
    flex-direction: column;
  }
}
</style>


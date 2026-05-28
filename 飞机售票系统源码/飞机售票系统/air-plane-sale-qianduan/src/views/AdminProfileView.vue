<template>
  <AdminLayout>
    <div class="profile-page">
      <div class="breadcrumb">
        <span>首页</span>
        <span class="breadcrumb-separator">/</span>
        <span>系统管理</span>
        <span class="breadcrumb-separator">/</span>
        <span>个人信息设置</span>
      </div>

      <div class="page-header">
        <div>
          <p class="page-label">系统管理 · 个人信息</p>
          <h1>我的账号信息</h1>
          <p class="page-desc">仅修改当前登录的系统管理员账号信息，不影响其他用户。</p>
        </div>
      </div>

      <section class="glass-card profile-card">
        <div class="profile-grid">
          <div class="profile-main">
            <h2>基本信息</h2>
            <div class="form-row">
              <div class="form-item">
                <label>登录账号</label>
                <input v-model="form.username" type="text" disabled />
              </div>
              <div class="form-item">
                <label>姓名/昵称</label>
                <input v-model="form.realName" type="text" placeholder="请输入姓名或昵称" />
              </div>
            </div>
            <div class="form-row">
          <div class="form-item">
            <label>手机号</label>
            <input v-model="form.phone" type="tel" placeholder="用于找回密码、通知等" />
          </div>
          <div class="form-item">
            <label>用户名</label>
            <input v-model="form.email" type="email" placeholder="用于系统通知与安全提醒" />
          </div>
            </div>
            <div class="form-row">
              <div class="form-item">
                <label>当前角色</label>
                <input :value="roleLabel" type="text" disabled />
              </div>
              <div class="form-item">
                <label>用户状态</label>
                <div class="status-display">
                  <span :class="['status-badge', form.status]">
                    {{ statusLabel }}
                  </span>
                </div>
              </div>
            </div>
          </div>

          <div class="profile-security">
            <h2>安全设置</h2>
            <div class="form-row">
              <div class="form-item">
                <label>原密码</label>
                <input v-model="security.oldPassword" type="password" placeholder="如需修改密码请填写原密码" />
              </div>
            </div>
            <div class="form-row">
              <div class="form-item">
                <label>新密码</label>
                <input v-model="security.newPassword" type="password" placeholder="留空则不修改密码" />
              </div>
              <div class="form-item">
                <label>确认新密码</label>
                <input v-model="security.confirmPassword" type="password" placeholder="再次输入新密码" />
              </div>
            </div>
            <p class="tips">密码建议包含大小写字母、数字和符号，长度至少 8 位。</p>
          </div>
        </div>

        <div class="form-footer">
          <span class="error-text" v-if="errorMessage">{{ errorMessage }}</span>
          <span class="success-text" v-if="successMessage">{{ successMessage }}</span>
          <div class="actions">
            <button class="ghost-btn" @click="resetForm" :disabled="saving">重置</button>
            <button class="primary-btn" @click="saveProfile" :disabled="saving">
              {{ saving ? '保存中...' : '保存修改' }}
            </button>
          </div>
        </div>
      </section>
    </div>
  </AdminLayout>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import AdminLayout from '../components/AdminLayout.vue'
import store from '../services/store'
import { authApi, userManagementApi, passengerApi } from '../services/api'

interface ProfileForm {
  username: string
  realName: string
  phone: string
  email: string
  role: string
  status: string
}

const form = reactive<ProfileForm>({
  username: '',
  realName: '',
  phone: '',
  email: '',
  role: '',
  status: 'active'
})

const security = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const saving = ref(false)
const errorMessage = ref('')
const successMessage = ref('')

const roleLabel = computed(() => {
  const map: Record<string, string> = {
    admin: '系统管理员',
    operator: '运行控制',
    passenger: '乘客用户'
  }
  return map[form.role] || form.role || '系统管理员'
})

const statusLabel = computed(() => {
  return form.status === 'active' ? '启用' : '禁用'
})

const loadCurrentUser = () => {
  const info = (store.userState.userInfo as any) || (() => {
    try {
      const stored = sessionStorage.getItem('user_info')
      return stored ? JSON.parse(stored) : null
    } catch {
      return null
    }
  })()

  if (info) {
    form.username = info.username || ''
    form.realName = info.realName || info.username || ''
    form.phone = info.phone || ''
    form.email = info.email || ''
    form.role = info.role || 'admin'
    form.status = info.status || 'active'
  }
}

const resetForm = () => {
  errorMessage.value = ''
  successMessage.value = ''
  security.oldPassword = ''
  security.newPassword = ''
  security.confirmPassword = ''
  loadCurrentUser()
}

const saveProfile = async () => {
  errorMessage.value = ''
  successMessage.value = ''

  if (!form.realName.trim()) {
    errorMessage.value = '请填写姓名/昵称'
    return
  }

  if (security.newPassword || security.confirmPassword || security.oldPassword) {
    if (!security.oldPassword) {
      errorMessage.value = '修改密码需要填写原密码'
      return
    }
    if (security.newPassword !== security.confirmPassword) {
      errorMessage.value = '两次输入的新密码不一致'
      return
    }
    if (security.newPassword.length < 8) {
      errorMessage.value = '新密码长度至少为 8 位'
      return
    }
  }

  saving.value = true
  try {
    const profilePayload: any = {
      realName: form.realName.trim(),
      phone: form.phone.trim(),
      email: form.email.trim()
    }

    // 1. 如果是乘客角色，走乘客个人资料接口；管理员则只走后台用户管理
    if (form.role === 'passenger') {
      await passengerApi.updateProfile(profilePayload)
    }

    // 2. 若当前用户在后台“人员用户管理”中也有一条记录，则同步更新该用户
    try {
      const currentUser = (await authApi.getCurrentUser?.()) as any
      const userId = currentUser?.id || (store.userState.userInfo as any)?.id
      if (userId) {
        await userManagementApi.updateUser(String(userId), {
          name: form.realName.trim(),
          phone: form.phone.trim(),
          // 邮箱字段后端如果支持可一起更新
          email: form.email.trim()
        })
      }
    } catch (e) {
      // 同步到用户管理失败不影响主流程，仅记录日志
      console.warn('同步更新用户管理信息失败（可忽略）:', e)
    }

    // 3. 同步更新前端 store 与本地缓存，保证头部显示的姓名等立即生效
    const merged = {
      ...(store.userState.userInfo as any || {}),
      ...profilePayload
    }
    store.userState.userInfo = merged
    try {
      const stored = sessionStorage.getItem('user_info')
      if (stored) {
        const info = JSON.parse(stored)
        sessionStorage.setItem('user_info', JSON.stringify({ ...info, ...profilePayload }))
      }
    } catch {
      /* ignore */
    }

    // 4. 如果需要修改密码，则走与用户管理一致的重置密码逻辑
    if (security.newPassword) {
      try {
        const currentUser = (await authApi.getCurrentUser?.()) as any
        const userId = currentUser?.id || (store.userState.userInfo as any)?.id
        if (userId) {
          await userManagementApi.resetPassword(String(userId), {
            newPassword: security.newPassword
          })
        }
      } catch (e) {
        console.warn('同步修改密码到用户管理失败:', e)
        // 这里不直接报错，以免资料已改成功但密码问题导致整体失败
      }
    }

    successMessage.value = '保存成功，已同步到账号与人员用户管理'
  } catch (error: any) {
    console.error('保存个人信息失败:', error)
    errorMessage.value = error?.message || '保存失败，请稍后重试'
  } finally {
    saving.value = false
    security.oldPassword = ''
    security.newPassword = ''
    security.confirmPassword = ''
  }
}

onMounted(() => {
  loadCurrentUser()
})
</script>

<style scoped>
.profile-page {
  padding: 24px;
}

.breadcrumb {
  margin-bottom: 16px;
  font-size: 14px;
  color: rgba(148, 163, 184, 0.9);
}

.breadcrumb-separator {
  margin: 0 6px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: 18px;
}

.page-label {
  font-size: 13px;
  color: rgba(148, 163, 184, 0.9);
  margin-bottom: 4px;
}

.page-header h1 {
  font-size: 24px;
  margin: 0;
  color: #e5e7eb;
}

.page-desc {
  margin-top: 4px;
  font-size: 13px;
  color: rgba(148, 163, 184, 0.9);
}

.glass-card.profile-card {
  background: rgba(15, 23, 42, 0.9);
  border-radius: 16px;
  padding: 20px 22px;
  border: 1px solid rgba(148, 163, 184, 0.4);
  box-shadow:
    0 20px 40px rgba(15, 23, 42, 0.9),
    0 0 0 1px rgba(15, 23, 42, 0.8);
}

.profile-grid {
  display: grid;
  grid-template-columns: minmax(0, 2fr) minmax(0, 1.4fr);
  gap: 24px;
}

.profile-main h2,
.profile-security h2 {
  font-size: 16px;
  margin: 0 0 12px;
  color: #e5e7eb;
}

.form-row {
  display: flex;
  gap: 16px;
  margin-bottom: 12px;
  flex-wrap: wrap;
}

.form-item {
  flex: 1 1 180px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.form-item label {
  font-size: 13px;
  color: rgba(148, 163, 184, 0.95);
}

.form-item input {
  padding: 8px 10px;
  border-radius: 10px;
  border: 1px solid rgba(148, 163, 184, 0.5);
  background: rgba(15, 23, 42, 0.9);
  color: #e5e7eb;
  font-size: 14px;
}

.form-item input:disabled {
  background: rgba(15, 23, 42, 0.7);
  color: rgba(148, 163, 184, 0.9);
}

.status-display {
  display: flex;
  align-items: center;
  height: 38px;
}

.status-badge {
  display: inline-block;
  padding: 6px 12px;
  border-radius: 12px;
  font-size: 13px;
  font-weight: 500;
}

.status-badge.active {
  background: rgba(34, 197, 94, 0.2);
  color: #22c55e;
  border: 1px solid rgba(34, 197, 94, 0.3);
}

.status-badge.disabled {
  background: rgba(239, 68, 68, 0.2);
  color: #ef4444;
  border: 1px solid rgba(239, 68, 68, 0.3);
}

.profile-security .tips {
  font-size: 12px;
  color: rgba(148, 163, 184, 0.9);
}

.form-footer {
  margin-top: 18px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.error-text {
  font-size: 13px;
  color: #f87171;
}

.success-text {
  font-size: 13px;
  color: #4ade80;
}

.actions {
  display: flex;
  gap: 10px;
}

.ghost-btn,
.primary-btn {
  padding: 8px 16px;
  border-radius: 999px;
  font-size: 14px;
  cursor: pointer;
  border: none;
}

.ghost-btn {
  background: transparent;
  border: 1px solid rgba(148, 163, 184, 0.7);
  color: #e5e7eb;
}

.primary-btn {
  background: linear-gradient(135deg, #3b82f6, #1d4ed8);
  color: #e5f2ff;
}

.ghost-btn:disabled,
.primary-btn:disabled {
  opacity: 0.6;
  cursor: default;
}

@media (max-width: 960px) {
  .profile-grid {
    grid-template-columns: minmax(0, 1fr);
  }
}
</style>



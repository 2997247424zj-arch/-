<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'

import { useAuthStore } from '@/stores/auth'
import type { RegisterRequest } from '@/types/auth'

type AuthMode = 'login' | 'register'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const mode = ref<AuthMode>('login')
const submitting = ref(false)
const errorMessage = ref('')
const showPassword = ref(false)
const showConfirmPassword = ref(false)

const form = reactive({
  username: 'demo_user',
  password: '123456',
  confirmPassword: '',
  displayName: '',
  role: 'USER' as RegisterRequest['role'],
  favoriteGenre: 'Lo-fi',
  bio: '',
})

const demoAccounts = [
  { username: 'demo_user', name: 'Night Listener', role: '普通用户', genre: 'Lo-fi' },
  { username: 'demo_creator', name: 'Aurora Lane', role: '创作者', genre: 'City Pop' },
  { username: 'demo_admin', name: 'Music Ops', role: '管理员', genre: 'Electronic' },
]

const genreOptions = ['Lo-fi', 'Indie', 'Electronic', 'City Pop']
const roleOptions: Array<{ label: string; value: RegisterRequest['role']; copy: string }> = [
  { label: '普通用户', value: 'USER', copy: '收藏歌曲、参与讨论、整理个人音乐库。' },
  { label: '创作者', value: 'CREATOR', copy: '发布作品、上传音频、管理创作内容。' },
]

const pageCopy = computed(() =>
  mode.value === 'login'
    ? {
        eyebrow: 'Account Access',
        title: '进入你的音乐空间',
        copy: '登录后继续管理收藏、播放队列、社区互动和创作者内容。',
        submit: '登录',
        pending: '登录中...',
        status: '安全登录',
      }
    : {
        eyebrow: 'Create Account',
        title: '创建新的音乐身份',
        copy: '完善角色、曲风和简介，注册后会自动进入对应工作区。',
        submit: '注册并进入',
        pending: '注册中...',
        status: '新账号配置',
      },
)

const passwordScore = computed(() => {
  const password = form.password
  let score = 0

  if (password.length >= 6) score += 1
  if (password.length >= 10) score += 1
  if (/[A-Za-z]/.test(password) && /\d/.test(password)) score += 1
  if (/[^A-Za-z0-9]/.test(password)) score += 1

  return Math.min(score, 4)
})

const passwordStrengthLabel = computed(() => {
  const labels = ['未填写', '可用', '中等', '较强', '很强']
  return labels[passwordScore.value]
})

const previewInitial = computed(() => {
  const source = form.displayName || form.username || 'M'
  return source.slice(0, 1).toUpperCase()
})

const previewName = computed(() => form.displayName || form.username || '新用户')
const previewRole = computed(() => (form.role === 'CREATOR' ? '创作者' : '普通用户'))
const redirectTarget = computed(() => safeRedirect(route.query.redirect))

function safeRedirect(value: unknown) {
  if (typeof value !== 'string') {
    return ''
  }

  return value.startsWith('/') && !value.startsWith('/login') ? value : ''
}

function switchMode(nextMode: AuthMode) {
  mode.value = nextMode
  errorMessage.value = ''
  if (nextMode === 'register' && form.username.startsWith('demo_')) {
    form.username = ''
    form.password = ''
    form.confirmPassword = ''
  }
}

function applyDemoUser(username: string) {
  mode.value = 'login'
  errorMessage.value = ''
  form.username = username
  form.password = '123456'
  form.confirmPassword = ''
}

function validateForm() {
  if (form.username.trim().length < 3) {
    errorMessage.value = '用户名至少需要 3 个字符。'
    return false
  }

  if (form.password.length < 6) {
    errorMessage.value = '密码至少需要 6 个字符。'
    return false
  }

  if (mode.value === 'register') {
    if (!form.displayName.trim()) {
      errorMessage.value = '请填写昵称。'
      return false
    }

    if (form.password !== form.confirmPassword) {
      errorMessage.value = '两次输入的密码不一致。'
      return false
    }
  }

  return true
}

async function submitAuth() {
  errorMessage.value = ''
  if (!validateForm()) {
    return
  }

  submitting.value = true

  try {
    const authResult =
      mode.value === 'login'
        ? await authStore.login({
            username: form.username,
            password: form.password,
          })
        : await authStore.register({
            username: form.username,
            password: form.password,
            displayName: form.displayName,
            role: form.role,
            favoriteGenre: form.favoriteGenre,
            bio: form.bio,
          })

    await router.push(
      redirectTarget.value ||
        (authResult.profile.role === 'CREATOR' || authResult.profile.role === 'ADMIN'
          ? '/creator'
          : '/library'),
    )
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '账号操作失败。'
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <div class="page-shell auth-stage">
    <section class="section-card auth-window motion-rise" :class="`mode-${mode}`">
      <div class="window-bar">
        <div class="window-dots" aria-hidden="true">
          <span></span>
          <span></span>
          <span></span>
        </div>
        <span class="window-title">Music Share Account</span>
        <span class="window-status">{{ pageCopy.status }}</span>
      </div>

      <div class="auth-content">
        <div class="auth-main">
          <div class="auth-heading">
            <span class="eyebrow">{{ pageCopy.eyebrow }}</span>
            <h1 class="section-title">{{ pageCopy.title }}</h1>
            <p class="section-copy">{{ pageCopy.copy }}</p>
          </div>

          <p v-if="redirectTarget" class="redirect-note">
            登录后继续访问：{{ redirectTarget }}
          </p>

          <div class="auth-tabs" role="tablist" aria-label="账号操作">
            <button
              type="button"
              class="auth-tab"
              :aria-selected="mode === 'login'"
              :class="{ active: mode === 'login' }"
              @click="switchMode('login')"
            >
              登录
            </button>
            <button
              type="button"
              class="auth-tab"
              :aria-selected="mode === 'register'"
              :class="{ active: mode === 'register' }"
              @click="switchMode('register')"
            >
              注册
            </button>
          </div>

          <form class="auth-form" @submit.prevent="submitAuth">
            <div class="field-grid">
              <label class="field">
                <span class="field-row">
                  <span>用户名</span>
                  <small>3-50 字符</small>
                </span>
                <input
                  v-model.trim="form.username"
                  type="text"
                  autocomplete="username"
                  maxlength="50"
                  required
                  placeholder="输入用户名"
                />
              </label>

              <label class="field">
                <span class="field-row">
                  <span>密码</span>
                  <small>至少 6 位</small>
                </span>
                <span class="password-field">
                  <input
                    v-model="form.password"
                    :type="showPassword ? 'text' : 'password'"
                    :autocomplete="mode === 'login' ? 'current-password' : 'new-password'"
                    maxlength="100"
                    required
                    placeholder="输入密码"
                  />
                  <button type="button" class="password-toggle" @click="showPassword = !showPassword">
                    {{ showPassword ? '隐藏' : '显示' }}
                  </button>
                </span>
              </label>
            </div>

            <div v-if="mode === 'register' && form.password" class="password-meter">
              <span>密码强度：{{ passwordStrengthLabel }}</span>
              <div class="meter-bars" aria-hidden="true">
                <span
                  v-for="level in 4"
                  :key="level"
                  :class="{ active: passwordScore >= level }"
                ></span>
              </div>
            </div>

            <Transition name="form-swap">
              <div v-if="mode === 'register'" class="register-fields">
                <label class="field">
                  <span class="field-row">
                    <span>确认密码</span>
                    <small>再次输入</small>
                  </span>
                  <span class="password-field">
                    <input
                      v-model="form.confirmPassword"
                      :type="showConfirmPassword ? 'text' : 'password'"
                      autocomplete="new-password"
                      maxlength="100"
                      required
                      placeholder="确认密码"
                    />
                    <button
                      type="button"
                      class="password-toggle"
                      @click="showConfirmPassword = !showConfirmPassword"
                    >
                      {{ showConfirmPassword ? '隐藏' : '显示' }}
                    </button>
                  </span>
                </label>

                <label class="field">
                  <span class="field-row">
                    <span>昵称</span>
                    <small>社区展示名</small>
                  </span>
                  <input
                    v-model.trim="form.displayName"
                    type="text"
                    autocomplete="name"
                    maxlength="100"
                    required
                    placeholder="例如：Night Listener"
                  />
                </label>

                <label class="field">
                  <span>偏好曲风</span>
                  <select v-model="form.favoriteGenre">
                    <option v-for="genre in genreOptions" :key="genre" :value="genre">{{ genre }}</option>
                  </select>
                </label>

                <div class="role-picker">
                  <span class="field-label">账号角色</span>
                  <button
                    v-for="option in roleOptions"
                    :key="option.value"
                    type="button"
                    class="role-option"
                    :class="{ active: form.role === option.value }"
                    @click="form.role = option.value"
                  >
                    <strong>{{ option.label }}</strong>
                    <span>{{ option.copy }}</span>
                  </button>
                </div>

                <label class="field field-wide">
                  <span class="field-row">
                    <span>个人简介</span>
                    <small>{{ form.bio.length }}/255</small>
                  </span>
                  <textarea
                    v-model.trim="form.bio"
                    rows="4"
                    maxlength="255"
                    placeholder="写一句介绍自己音乐偏好的话"
                  ></textarea>
                </label>
              </div>
            </Transition>

            <p v-if="errorMessage" class="error-text">{{ errorMessage }}</p>

            <button type="submit" class="primary-button submit-button" :disabled="submitting">
              {{ submitting ? pageCopy.pending : pageCopy.submit }}
            </button>
          </form>
        </div>

        <aside class="auth-side">
          <div class="profile-preview">
            <span class="preview-avatar">{{ previewInitial }}</span>
            <div>
              <p class="preview-label">{{ mode === 'login' ? '当前账号' : '账号预览' }}</p>
              <h2>{{ previewName }}</h2>
              <p>{{ previewRole }} · {{ form.favoriteGenre }}</p>
            </div>
          </div>

          <div class="preview-list">
            <span>播放队列会保存在本地账号状态中</span>
            <span>创作者注册后可进入创作台上传作品</span>
            <span>个人主页会展示收藏、点赞和评论记录</span>
          </div>

          <div class="demo-area">
            <div class="demo-head">
              <span class="eyebrow">Demo Accounts</span>
              <p>课堂演示和验收测试可直接使用，密码均为 123456。</p>
            </div>

            <div class="demo-list">
              <button
                v-for="account in demoAccounts"
                :key="account.username"
                type="button"
                class="demo-item"
                @click="applyDemoUser(account.username)"
              >
                <span class="account-avatar">{{ account.name.slice(0, 1) }}</span>
                <span class="account-copy">
                  <strong>{{ account.name }}</strong>
                  <span>{{ account.username }} · {{ account.role }} · {{ account.genre }}</span>
                </span>
              </button>
            </div>
          </div>
        </aside>
      </div>
    </section>
  </div>
</template>

<style scoped>
.auth-stage {
  min-height: calc(100vh - 210px);
  display: grid;
  align-items: center;
}

.auth-window {
  overflow: hidden;
  border-radius: 30px;
}

.window-bar {
  min-height: 58px;
  padding: 0 22px;
  border-bottom: 1px solid var(--color-border);
  display: grid;
  grid-template-columns: auto 1fr auto;
  gap: 16px;
  align-items: center;
  background: color-mix(in srgb, var(--color-panel) 78%, transparent);
}

.window-dots {
  display: flex;
  gap: 7px;
}

.window-dots span {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: var(--color-border-strong);
}

.window-dots span:nth-child(1) {
  background: #f47272;
}

.window-dots span:nth-child(2) {
  background: #f4b942;
}

.window-dots span:nth-child(3) {
  background: #5eead4;
}

.window-title,
.window-status {
  color: var(--color-muted);
  font-size: 0.92rem;
}

.window-title {
  font-family: var(--font-display);
  font-weight: 700;
}

.auth-content {
  display: grid;
  grid-template-columns: minmax(0, 1.05fr) minmax(360px, 0.76fr);
}

.auth-main {
  padding: clamp(24px, 4vw, 44px);
  display: grid;
  gap: 24px;
}

.auth-side {
  padding: clamp(22px, 3vw, 34px);
  border-left: 1px solid var(--color-border);
  background:
    linear-gradient(135deg, color-mix(in srgb, var(--color-primary) 10%, transparent), transparent 36%),
    color-mix(in srgb, var(--color-panel) 72%, transparent);
  display: grid;
  align-content: space-between;
  gap: 26px;
}

.auth-heading {
  display: grid;
  gap: 12px;
}

.redirect-note {
  padding: 12px 14px;
  border: 1px solid color-mix(in srgb, var(--color-primary) 28%, var(--color-border));
  border-radius: 16px;
  background: color-mix(in srgb, var(--color-primary) 9%, transparent);
  color: var(--color-heading);
  overflow-wrap: anywhere;
}

.auth-tabs {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 8px;
  padding: 6px;
  border: 1px solid var(--color-border);
  border-radius: 18px;
  background: color-mix(in srgb, var(--color-panel) 82%, transparent);
}

.auth-tab {
  min-height: 44px;
  border: 0;
  border-radius: 14px;
  background: transparent;
  color: var(--color-muted);
  cursor: pointer;
  font-weight: 700;
  transition:
    background-color 0.22s var(--motion-ease),
    color 0.22s var(--motion-ease),
    transform 0.22s var(--motion-ease);
}

.auth-tab:hover,
.auth-tab:focus-visible,
.auth-tab.active {
  background: var(--color-surface-strong);
  color: var(--color-heading);
  transform: translateY(-1px);
}

.auth-form,
.register-fields {
  display: grid;
  gap: 16px;
}

.field-grid,
.register-fields {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.register-fields {
  display: grid;
}

.field,
.role-picker {
  display: grid;
  gap: 8px;
  color: var(--color-heading);
  font-weight: 700;
}

.field-row {
  display: flex;
  justify-content: space-between;
  gap: 10px;
  align-items: center;
}

.field-row small {
  color: var(--color-muted);
  font-size: 0.78rem;
  font-weight: 500;
}

.field-label {
  color: var(--color-heading);
}

.field input,
.field select,
.field textarea {
  width: 100%;
  border: 1px solid var(--color-border);
  border-radius: 16px;
  background: color-mix(in srgb, var(--color-surface-strong) 86%, transparent);
  color: var(--color-text);
  font: inherit;
  outline: none;
  transition:
    border-color 0.2s var(--motion-ease),
    box-shadow 0.2s var(--motion-ease),
    background-color 0.2s var(--motion-ease);
}

.field input,
.field select {
  min-height: 50px;
  padding: 0 14px;
}

.field textarea {
  min-height: 112px;
  padding: 13px 14px;
  resize: vertical;
}

.field input:focus,
.field select:focus,
.field textarea:focus {
  border-color: color-mix(in srgb, var(--color-primary) 70%, var(--color-border));
  box-shadow: 0 0 0 4px color-mix(in srgb, var(--color-primary) 14%, transparent);
}

.password-field {
  position: relative;
  display: block;
}

.password-field input {
  padding-right: 68px;
}

.password-toggle {
  position: absolute;
  top: 50%;
  right: 8px;
  min-width: 52px;
  min-height: 34px;
  border: 0;
  border-radius: 12px;
  background: color-mix(in srgb, var(--color-panel) 90%, transparent);
  color: var(--color-heading);
  cursor: pointer;
  transform: translateY(-50%);
  transition:
    background-color 0.2s var(--motion-ease),
    transform 0.2s var(--motion-ease);
}

.password-toggle:hover,
.password-toggle:focus-visible {
  background: color-mix(in srgb, var(--color-primary) 16%, var(--color-panel));
  transform: translateY(-50%) scale(1.02);
}

.password-meter {
  display: flex;
  justify-content: space-between;
  gap: 14px;
  align-items: center;
  color: var(--color-muted);
  font-size: 0.9rem;
}

.meter-bars {
  width: min(220px, 46%);
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 6px;
}

.meter-bars span {
  height: 8px;
  border-radius: 999px;
  background: var(--color-border);
}

.meter-bars span.active {
  background: linear-gradient(90deg, var(--color-primary), var(--color-secondary));
}

.role-picker,
.field-wide {
  grid-column: 1 / -1;
}

.role-picker {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.role-picker .field-label {
  grid-column: 1 / -1;
}

.role-option {
  display: grid;
  gap: 6px;
  min-height: 92px;
  padding: 16px;
  border: 1px solid var(--color-border);
  border-radius: 18px;
  background: color-mix(in srgb, var(--color-panel) 86%, transparent);
  color: var(--color-muted);
  text-align: left;
  cursor: pointer;
  transition:
    border-color 0.22s var(--motion-ease),
    background-color 0.22s var(--motion-ease),
    transform 0.22s var(--motion-ease);
}

.role-option strong {
  color: var(--color-heading);
  font-family: var(--font-display);
  font-size: 1rem;
}

.role-option span {
  font-size: 0.92rem;
}

.role-option:hover,
.role-option:focus-visible,
.role-option.active {
  border-color: color-mix(in srgb, var(--color-primary) 56%, var(--color-border));
  background: color-mix(in srgb, var(--color-primary) 12%, var(--color-panel));
  transform: translateY(-1px);
}

.submit-button {
  width: 100%;
}

.submit-button:disabled {
  cursor: wait;
  opacity: 0.72;
}

.error-text {
  padding: 12px 14px;
  border: 1px solid rgba(181, 84, 57, 0.24);
  border-radius: 16px;
  background: rgba(181, 84, 57, 0.08);
  color: #b55439;
}

.profile-preview {
  display: flex;
  gap: 14px;
  align-items: center;
}

.preview-avatar,
.account-avatar {
  flex: 0 0 auto;
  border-radius: 50%;
  display: grid;
  place-items: center;
  background: linear-gradient(135deg, var(--color-primary), var(--color-secondary));
  color: #0f1124;
  font-family: var(--font-display);
  font-weight: 800;
}

.preview-avatar {
  width: 62px;
  height: 62px;
  font-size: 1.5rem;
}

.preview-label {
  color: var(--color-muted);
  font-size: 0.84rem;
  font-weight: 700;
}

.profile-preview h2 {
  color: var(--color-heading);
  font-family: var(--font-display);
  font-size: 1.35rem;
}

.profile-preview p:last-child {
  color: var(--color-muted);
}

.preview-list {
  display: grid;
  gap: 12px;
  color: var(--color-muted);
}

.preview-list span {
  padding-left: 14px;
  border-left: 3px solid color-mix(in srgb, var(--color-primary) 54%, var(--color-border));
}

.demo-area,
.demo-head {
  display: grid;
  gap: 14px;
}

.demo-head p {
  color: var(--color-muted);
}

.demo-list {
  display: grid;
  gap: 12px;
}

.demo-item {
  display: flex;
  align-items: center;
  gap: 14px;
  width: 100%;
  padding: 14px;
  border: 1px solid var(--color-border);
  border-radius: 18px;
  background: color-mix(in srgb, var(--color-panel) 86%, transparent);
  color: inherit;
  cursor: pointer;
  text-align: left;
  transition:
    border-color 0.22s var(--motion-ease),
    background-color 0.22s var(--motion-ease),
    transform 0.22s var(--motion-ease);
}

.demo-item:hover,
.demo-item:focus-visible {
  border-color: var(--color-border-strong);
  background: var(--color-surface-strong);
  transform: translateY(-1px);
}

.account-avatar {
  width: 44px;
  height: 44px;
}

.account-copy {
  display: grid;
  gap: 4px;
  min-width: 0;
}

.account-copy strong {
  color: var(--color-heading);
  font-family: var(--font-display);
}

.account-copy span {
  color: var(--color-muted);
  font-size: 0.92rem;
  overflow-wrap: anywhere;
}

.form-swap-enter-active,
.form-swap-leave-active {
  transition:
    opacity 180ms var(--motion-ease),
    transform 180ms var(--motion-ease);
}

.form-swap-enter-from,
.form-swap-leave-to {
  opacity: 0;
  transform: translate3d(0, -8px, 0);
}

@media (max-width: 1080px) {
  .auth-content {
    grid-template-columns: 1fr;
  }

  .auth-side {
    border-top: 1px solid var(--color-border);
    border-left: 0;
  }
}

@media (max-width: 760px) {
  .window-bar {
    grid-template-columns: auto 1fr;
  }

  .window-status {
    display: none;
  }

  .field-grid,
  .register-fields,
  .role-picker {
    grid-template-columns: 1fr;
  }

  .role-picker,
  .role-picker .field-label,
  .field-wide {
    grid-column: auto;
  }

  .password-meter {
    align-items: stretch;
    flex-direction: column;
  }

  .meter-bars {
    width: 100%;
  }
}

@media (max-width: 520px) {
  .auth-stage {
    min-height: auto;
  }

  .window-bar {
    padding: 0 16px;
  }

  .auth-main,
  .auth-side {
    padding: 22px;
  }
}
</style>

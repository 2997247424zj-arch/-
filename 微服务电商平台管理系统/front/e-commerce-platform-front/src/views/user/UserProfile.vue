<template>
  <div class="profile-page">
    <div class="page-header surface-card">
      <div>
        <span class="header-tag">ACCOUNT CENTER</span>
        <h1>个人资料</h1>
        <p>统一管理你的基础信息、账户安全与常用收货资料，保持购物体验始终顺畅。</p>
      </div>
      <div class="header-actions">
        <el-button round @click="router.push('/order/list')">查看订单</el-button>
        <el-button round type="primary" @click="router.push('/user/address')">管理地址</el-button>
      </div>
    </div>

    <div class="page-layout">
      <aside class="sidebar surface-card">
        <div class="user-panel">
          <div class="avatar-shell">
            <el-avatar
              :size="68"
              :src="
                userStore.userInfo?.avatar ||
                'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'
              "
            />
          </div>
          <div>
            <h3>{{ userStore.userInfo?.username || '未登录用户' }}</h3>
            <p>{{ userStore.userInfo?.email || '绑定邮箱后可接收通知' }}</p>
          </div>
        </div>

        <el-menu :default-active="activeMenu" class="user-menu" router>
          <el-menu-item index="/user">
            <el-icon><User /></el-icon>
            <span>个人信息</span>
          </el-menu-item>
          <el-menu-item index="/order/list">
            <el-icon><Tickets /></el-icon>
            <span>我的订单</span>
          </el-menu-item>
          <el-menu-item index="/user/address">
            <el-icon><Location /></el-icon>
            <span>地址管理</span>
          </el-menu-item>
        </el-menu>

        <div class="sidebar-tip glass-panel">
          <strong>安全建议</strong>
          <p>定期更新密码，并保持邮箱可用，以便快速找回账户与接收订单提醒。</p>
        </div>
      </aside>

      <main class="content-area">
        <section class="overview-grid">
          <article class="overview-card surface-card accent-blue">
            <span>账户名称</span>
            <strong>{{ userStore.userInfo?.username || 'N/A' }}</strong>
            <small>用于登录与展示的账户标识</small>
          </article>
          <article class="overview-card surface-card accent-green">
            <span>绑定邮箱</span>
            <strong>{{ userStore.userInfo?.email || '未绑定' }}</strong>
            <small>建议绑定邮箱接收订单与安全通知</small>
          </article>
          <article class="overview-card surface-card accent-amber">
            <span>手机号码</span>
            <strong>{{ userStore.userInfo?.phone || 'N/A' }}</strong>
            <small>用于收货联系与身份校验</small>
          </article>
        </section>

        <section class="profile-card surface-card">
          <div class="section-head">
            <div>
              <h2>基础资料</h2>
              <p>更新昵称、头像和邮箱信息，让账户资料始终保持最新。</p>
            </div>
          </div>

          <div class="profile-body">
            <div class="avatar-section glass-panel">
              <el-avatar
                :size="120"
                :src="
                  userStore.userInfo?.avatar ||
                  'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'
                "
              />
              <div class="avatar-copy">
                <strong>头像设置</strong>
                <p>支持 JPG、PNG 等图片格式，文件大小不超过 2MB。</p>
              </div>
              <el-upload
                :show-file-list="false"
                :before-upload="beforeAvatarUpload"
                :http-request="handleAvatarUpload"
                accept="image/*"
              >
                <el-button round>更换头像</el-button>
              </el-upload>
            </div>

            <div class="detail-section">
              <div class="field-card glass-panel">
                <div class="field-head">
                  <div>
                    <strong>用户名</strong>
                    <p>用于页面展示与账户识别</p>
                  </div>
                  <el-button
                    v-if="!editingUsername"
                    link
                    type="primary"
                    @click="startEditUsername"
                  >
                    修改
                  </el-button>
                </div>
                <div class="field-content">
                  <template v-if="editingUsername">
                    <el-input v-model="editForm.username" maxlength="20" />
                    <div class="inline-actions">
                      <el-button type="primary" @click="saveUsername">保存</el-button>
                      <el-button @click="editingUsername = false">取消</el-button>
                    </div>
                  </template>
                  <template v-else>
                    <span class="field-value">{{ userStore.userInfo?.username || 'N/A' }}</span>
                  </template>
                </div>
              </div>

              <div class="field-card glass-panel">
                <div class="field-head">
                  <div>
                    <strong>邮箱地址</strong>
                    <p>可用于接收订单通知与账号提醒</p>
                  </div>
                  <el-button v-if="!editingEmail" link type="primary" @click="startEditEmail">
                    {{ userStore.userInfo?.email ? '修改' : '绑定' }}
                  </el-button>
                </div>
                <div class="field-content">
                  <template v-if="editingEmail">
                    <el-input v-model="editForm.email" placeholder="请输入邮箱地址" />
                    <div class="inline-actions">
                      <el-button type="primary" @click="saveEmail">保存</el-button>
                      <el-button @click="editingEmail = false">取消</el-button>
                    </div>
                  </template>
                  <template v-else>
                    <span class="field-value">{{ userStore.userInfo?.email || '未绑定' }}</span>
                  </template>
                </div>
              </div>

              <div class="field-card glass-panel static-card">
                <div class="field-head">
                  <div>
                    <strong>手机号码</strong>
                    <p>默认作为收货与联系号码</p>
                  </div>
                </div>
                <div class="field-content">
                  <span class="field-value">{{ userStore.userInfo?.phone || 'N/A' }}</span>
                </div>
              </div>

              <div class="field-card glass-panel static-card">
                <div class="field-head">
                  <div>
                    <strong>注册时间</strong>
                    <p>首次创建账户的时间记录</p>
                  </div>
                </div>
                <div class="field-content">
                  <span class="field-value">{{ userStore.userInfo?.createTime || 'N/A' }}</span>
                </div>
              </div>
            </div>
          </div>
        </section>

        <section class="security-card surface-card">
          <div class="section-head">
            <div>
              <h2>账户安全</h2>
              <p>定期修改登录密码，减少账户风险并提升安全性。</p>
            </div>
          </div>

          <div class="security-layout">
            <div class="security-copy glass-panel">
              <strong>密码建议</strong>
              <ul>
                <li>密码长度不少于 6 位</li>
                <li>建议包含字母、数字与符号组合</li>
                <li>不要与其他站点使用相同密码</li>
              </ul>
            </div>

            <el-form
              ref="passwordFormRef"
              :model="passwordForm"
              :rules="passwordRules"
              label-position="top"
              class="password-form glass-panel"
            >
              <el-form-item label="当前密码" prop="oldPassword">
                <el-input
                  v-model="passwordForm.oldPassword"
                  type="password"
                  show-password
                  placeholder="请输入当前密码"
                />
              </el-form-item>
              <el-form-item label="新密码" prop="newPassword">
                <el-input
                  v-model="passwordForm.newPassword"
                  type="password"
                  show-password
                  placeholder="请输入新密码"
                />
              </el-form-item>
              <el-form-item label="确认新密码" prop="confirmPassword">
                <el-input
                  v-model="passwordForm.confirmPassword"
                  type="password"
                  show-password
                  placeholder="请再次输入新密码"
                />
              </el-form-item>
              <div class="password-actions">
                <el-button type="primary" :loading="passwordLoading" @click="changePassword">
                  保存新密码
                </el-button>
                <el-button @click="passwordFormRef?.resetFields()">重置</el-button>
              </div>
            </el-form>
          </div>
        </section>
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { User, Tickets, Location } from '@element-plus/icons-vue'
import type { FormInstance, FormRules, UploadRequestOptions } from 'element-plus'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store/user'
import { uploadAvatarAPI, updatePasswordAPI, updateUserInfoAPI } from '@/api/modules/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const activeMenu = computed(() => route.path)
const editingUsername = ref(false)
const editingEmail = ref(false)
const passwordFormRef = ref<FormInstance>()
const passwordLoading = ref(false)
const editForm = reactive({
  username: '',
  email: '',
})

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
})

const applyUserInfo = (data: any) => {
  if (data) {
    userStore.setUserInfo(data, true)
  }
}

const startEditUsername = () => {
  editForm.username = userStore.userInfo?.username || ''
  editingUsername.value = true
}

const startEditEmail = () => {
  editForm.email = userStore.userInfo?.email || ''
  editingEmail.value = true
}

const saveUsername = async () => {
  if (!editForm.username.trim()) {
    ElMessage.warning('用户名不能为空')
    return
  }

  try {
    const res: any = await updateUserInfoAPI({ username: editForm.username.trim() })
    applyUserInfo(res.data)
    ElMessage.success('用户名修改成功')
    editingUsername.value = false
  } catch {
    // Error handled by interceptor
  }
}

const saveEmail = async () => {
  if (!editForm.email.trim()) {
    ElMessage.warning('邮箱不能为空')
    return
  }

  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  if (!emailRegex.test(editForm.email)) {
    ElMessage.warning('请输入正确的邮箱格式')
    return
  }

  try {
    const res: any = await updateUserInfoAPI({ email: editForm.email.trim() })
    applyUserInfo(res.data)
    ElMessage.success('邮箱修改成功')
    editingEmail.value = false
  } catch {
    // Error handled by interceptor
  }
}

const beforeAvatarUpload = (file: File) => {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB!')
    return false
  }
  return true
}

const handleAvatarUpload = async (options: UploadRequestOptions) => {
  const formData = new FormData()
  formData.append('file', options.file)

  try {
    const res: any = await uploadAvatarAPI(formData)
    applyUserInfo(res.data)
    ElMessage.success('头像上传成功')
    options.onSuccess?.(res.data)
    return
  } catch (error) {
    options.onError?.(error as any)
    return
  }
  ElMessage.success('头像上传功能需要后端支持')
}

const validateConfirmPassword = (rule: any, value: string, callback: any) => {
  void rule
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== passwordForm.newPassword) {
    callback(new Error('两次输入密码不一致'))
  } else {
    callback()
  }
}

const passwordRules = reactive<FormRules>({
  oldPassword: [{ required: true, message: '请输入当前密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' },
  ],
  confirmPassword: [{ required: true, validator: validateConfirmPassword, trigger: 'blur' }],
})

const changePassword = async () => {
  if (!passwordFormRef.value) return

  await passwordFormRef.value.validate(async (valid) => {
    if (!valid) return

    passwordLoading.value = true
    try {
      await updatePasswordAPI({
        oldPassword: passwordForm.oldPassword,
        newPassword: passwordForm.newPassword,
      })
      ElMessage.success('密码修改成功')
      passwordFormRef.value?.resetFields()
    } catch {
      // Error handled by interceptor
    } finally {
      passwordLoading.value = false
    }
  })
}
</script>

<style scoped lang="scss">
.profile-page {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
  padding: 28px 32px;

  h1 {
    margin: 10px 0 12px;
    font-size: 32px;
    color: #0f172a;
  }

  p {
    margin: 0;
    max-width: 620px;
    color: #475569;
    line-height: 1.7;
  }
}

.header-tag {
  display: inline-flex;
  align-items: center;
  padding: 6px 12px;
  border-radius: 999px;
  background: rgba(30, 64, 175, 0.1);
  color: #1d4ed8;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.16em;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.page-layout {
  display: grid;
  grid-template-columns: 280px minmax(0, 1fr);
  gap: 24px;
}

.sidebar {
  display: flex;
  flex-direction: column;
  gap: 20px;
  padding: 22px;
  height: fit-content;
}

.user-panel {
  display: flex;
  align-items: center;
  gap: 14px;

  h3 {
    margin: 0 0 6px;
    font-size: 18px;
    color: #0f172a;
  }

  p {
    margin: 0;
    color: #64748b;
    font-size: 13px;
    line-height: 1.6;
  }
}

.avatar-shell {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 78px;
  height: 78px;
  border-radius: 24px;
  background: linear-gradient(135deg, rgba(30, 64, 175, 0.16), rgba(59, 130, 246, 0.22));
}

.user-menu {
  border-right: none;
  background: transparent;

  :deep(.el-menu-item) {
    margin-bottom: 8px;
    border-radius: 16px;
    color: #334155;
  }

  :deep(.el-menu-item.is-active) {
    background: linear-gradient(135deg, rgba(30, 64, 175, 0.12), rgba(34, 197, 94, 0.12));
    color: #1d4ed8;
  }
}

.sidebar-tip {
  padding: 18px;
  border-radius: 22px;

  strong {
    display: block;
    margin-bottom: 10px;
    color: #0f172a;
  }

  p {
    margin: 0;
    color: #64748b;
    line-height: 1.7;
    font-size: 13px;
  }
}

.content-area {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.overview-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 18px;
}

.overview-card {
  padding: 24px;

  span {
    display: block;
    margin-bottom: 14px;
    color: #64748b;
    font-size: 13px;
  }

  strong {
    display: block;
    font-size: 22px;
    color: #0f172a;
    margin-bottom: 8px;
  }

  small {
    color: #64748b;
    line-height: 1.6;
  }
}

.accent-blue {
  border: 1px solid rgba(59, 130, 246, 0.18);
}

.accent-green {
  border: 1px solid rgba(34, 197, 94, 0.2);
}

.accent-amber {
  border: 1px solid rgba(245, 158, 11, 0.22);
}

.profile-card,
.security-card {
  padding: 28px;
}

.section-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
  margin-bottom: 24px;

  h2 {
    margin: 0 0 8px;
    font-size: 24px;
    color: #0f172a;
  }

  p {
    margin: 0;
    color: #64748b;
    line-height: 1.7;
  }
}

.profile-body {
  display: grid;
  grid-template-columns: 280px minmax(0, 1fr);
  gap: 24px;
}

.avatar-section,
.field-card,
.security-copy,
.password-form {
  border-radius: 24px;
}

.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 18px;
  padding: 28px 22px;
  text-align: center;
}

.avatar-copy {
  strong {
    display: block;
    margin-bottom: 8px;
    color: #0f172a;
  }

  p {
    margin: 0;
    color: #64748b;
    line-height: 1.7;
    font-size: 14px;
  }
}

.detail-section {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 18px;
}

.field-card {
  padding: 20px;
}

.field-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 16px;

  strong {
    display: block;
    margin-bottom: 6px;
    color: #0f172a;
  }

  p {
    margin: 0;
    color: #64748b;
    font-size: 13px;
    line-height: 1.6;
  }
}

.field-content {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.field-value {
  color: #1e293b;
  font-size: 16px;
  font-weight: 600;
  word-break: break-all;
}

.inline-actions,
.password-actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.static-card {
  justify-content: space-between;
}

.security-layout {
  display: grid;
  grid-template-columns: 280px minmax(0, 1fr);
  gap: 24px;
}

.security-copy {
  padding: 22px;

  strong {
    display: block;
    margin-bottom: 14px;
    color: #0f172a;
  }

  ul {
    margin: 0;
    padding-left: 18px;
    color: #64748b;
    line-height: 1.9;
  }
}

.password-form {
  padding: 22px;
}

@media (max-width: 1200px) {
  .page-layout,
  .profile-body,
  .security-layout {
    grid-template-columns: 1fr;
  }

  .overview-grid,
  .detail-section {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    padding: 24px;

    h1 {
      font-size: 28px;
    }
  }

  .header-actions {
    width: 100%;
    flex-direction: column;
  }

  .overview-grid,
  .detail-section {
    grid-template-columns: 1fr;
  }

  .profile-card,
  .security-card,
  .sidebar {
    padding: 20px;
  }
}
</style>

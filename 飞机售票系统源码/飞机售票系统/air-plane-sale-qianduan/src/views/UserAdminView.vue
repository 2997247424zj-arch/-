<template>
  <AdminLayout>
    <!-- 面包屑导航 -->
    <div class="breadcrumb">
      <span>首页</span>
      <span class="breadcrumb-separator">/</span>
      <span>人员用户管理</span>
    </div>

    <!-- 搜索筛选区域 -->
    <div class="search-section admin-card">
      <div class="search-row">
        <div class="search-item">
          <label>姓名/用户名</label>
          <input 
            type="text" 
            v-model="searchParams.keyword" 
            placeholder="请输入姓名或用户名"
          />
        </div>
        <div class="search-item">
          <label>角色</label>
          <select v-model="searchParams.role">
            <option value="">全部角色</option>
            <option value="admin">系统管理员</option>
            <option value="operator">航空运营</option>
            <option value="passenger">普通乘客</option>
          </select>
        </div>
        <div class="search-item">
         
        
        </div>
        <div class="search-item">
          <label>状态</label>
          <select v-model="searchParams.status">
            <option value="">全部状态</option>
            <option value="active">启用</option>
            <option value="disabled">禁用</option>
          </select>
        </div>
        <div class="search-item">
          <button class="search-btn" @click="handleSearch">
            <span class="search-icon">🔍</span>
            查询
          </button>
          <button class="reset-btn" @click="handleReset">重置</button>
        </div>
      </div>
    </div>

    <!-- 操作按钮 -->
    <div class="action-buttons">
      <button class="btn-primary" @click="handleAdd">
        <span class="btn-icon">➕</span>
        新增用户
      </button>
      <button class="btn-secondary" @click="handleBatchImport">
        <span class="btn-icon">📥</span>
        批量导入
      </button>
      <button class="btn-secondary" @click="handleExport" :disabled="exportLoading">
        <span class="btn-icon">📤</span>
        {{ exportLoading ? '导出中...' : '导出报表' }}
      </button>
    </div>

    <!-- 用户列表 -->
    <div class="table-section">
      <div class="table-container admin-card">
        <table class="admin-table data-table">
          <thead>
            <tr>
              <th width="60">id</th>
              <th width="120">姓名</th>
              <th width="100">用户名</th>
              <th width="120">角色</th>
              <th width="120">手机号</th>
              <th width="150">注册日期</th>
              <th width="100">状态</th>
              <th width="200">操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(user, index) in paginatedUsers" :key="user.id">
              <td>{{ user.id }}</td>
              <td>{{ user.name }}</td>
              <td>{{ user.username }}</td>
              <td>
                <span :class="['role-badge', user.role]">
                  {{ getRoleText(user.role) }}
                </span>
              </td>

              <td>{{ user.phone }}</td>
              <td>{{ user.lastLogin }}</td>
              <td>
                <span :class="['status-badge', user.status]">
                  {{ user.status === 'active' ? '启用' : '禁用' }}
                </span>
              </td>
              <td>
                <div class="action-buttons-small">
                  <button class="action-btn permission-btn" @click="handleChangeRole(user)">
                    角色
                  </button>
                  <button class="action-btn edit-btn" @click="handleEdit(user)">
                    编辑
                  </button>
                  <button class="action-btn reset-password-btn" @click="handleResetPassword(user)">
                    重置密码
                  </button>
                  <button 
                    class="action-btn" 
                    :class="user.status === 'active' ? 'disable-btn' : 'enable-btn'"
                    @click="handleToggleStatus(user)"
                  >
                    {{ user.status === 'active' ? '禁用' : '启用' }}
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- 分页 -->
      <div class="pagination">
        <div class="pagination-info">
          共 {{ total }} 条，每页 
          <select v-model.number="pageSize" @change="handlePageSizeChange" class="page-size-select">
            <option :value="5">5</option>
            <option :value="10">10</option>
            <option :value="20">20</option>
            <option :value="50">50</option>
            <option :value="100">100</option>
          </select>
          条
        </div>
        <div class="pagination-controls">
          <button 
            class="page-btn" 
            :disabled="currentPage === 1"
            @click="goToPage(currentPage - 1)"
          >
            &lt;
          </button>
          <span class="page-number">{{ currentPage }} / {{ totalPages }}</span>
          <button 
            class="page-btn" 
            :disabled="currentPage === totalPages"
            @click="goToPage(currentPage + 1)"
          >
            &gt;
          </button>
          <div class="page-jump">
            <span>前往</span>
            <input 
              type="number" 
              v-model.number="jumpPage" 
              :min="1" 
              :max="totalPages"
              @keyup.enter="goToPage(jumpPage)"
            />
            <span>页</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 新增/编辑用户对话框 -->
    <Transition name="modal-fade">
      <div class="modal-overlay" v-if="userModal.visible" @click="closeUserModal">
        <div class="modal-content modal-large" @click.stop>
        <div class="modal-header">
          <h3>{{ userModal.mode === 'add' ? '新增用户' : '编辑用户' }}</h3>
          <button class="close-btn" @click="closeUserModal">×</button>
        </div>
        <div class="modal-body">
          <form class="user-form" @submit.prevent="handleSubmit">
            <div class="form-grid">
              <div class="form-item">
                <label>用户名 <span class="required">*</span></label>
                <input 
                  type="text" 
                  v-model="userForm.username" 
                  placeholder="请输入用户名"
                  required
                />
              </div>
              <div class="form-item">
                <label>身份证号 <span class="required">*</span></label>
                <input 
                  type="text" 
                  v-model="userForm.idCard" 
                  placeholder="请输入18位身份证号"
                  maxlength="18"
                  required
                />
              </div>
            </div>
            <div class="form-grid">
              <div class="form-item">
                <label>手机号 <span class="required">*</span></label>
                <input 
                  type="tel" 
                  v-model="userForm.phone" 
                  placeholder="请输入手机号"
                  required
                />
              </div>
              <div class="form-item">
                <label>真实姓名 <span class="required">*</span></label>
                <input 
                  type="text" 
                  v-model="userForm.name" 
                  placeholder="请输入真实姓名"
                  required
                />
              </div>
            </div>
            <div class="form-grid" v-if="userModal.mode === 'add'">
              <div class="form-item">
                <label>密码 <span class="required">*</span></label>
                <input 
                  type="password" 
                  v-model="userForm.password" 
                  placeholder="请输入密码"
                  required
                />
              </div>
              <div class="form-item">
                <label>确认密码 <span class="required">*</span></label>
                <input 
                  type="password" 
                  v-model="userForm.confirmPassword" 
                  placeholder="请再次输入密码"
                  required
                />
              </div>
            </div>
            <div class="form-grid">
              <div class="form-item">
                <label>角色 <span class="required">*</span></label>
                <select v-model="userForm.role" required>
                  <option value="">请选择角色</option>
                  <option value="admin">系统管理员</option>
                  <option value="operator">航空运营</option>
                  <option value="passenger">普通乘客</option>
                </select>
              </div>
            </div>
            <div class="form-actions">
              <button type="button" class="btn-secondary" @click="closeUserModal">取消</button>
              <button type="submit" class="btn-primary">保存</button>
            </div>
          </form>
        </div>
        </div>
      </div>
    </Transition>

    <!-- 角色管理对话框 -->
    <Transition name="modal-fade">
      <div class="modal-overlay" v-if="roleModal.visible" @click="closeRoleModal">
        <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>修改角色 - {{ roleModal.user?.name }}</h3>
          <button class="close-btn" @click="closeRoleModal">×</button>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label>当前角色</label>
            <div style="padding: 10px; background: rgba(15, 23, 42, 0.6); border-radius: 8px; margin-bottom: 15px;">
              <span :class="['role-badge', roleModal.user?.role]">
                {{ getRoleText(roleModal.user?.role || '') }}
              </span>
            </div>
          </div>
          <div class="form-group">
            <label>选择新角色 <span class="required">*</span></label>
            <div class="role-list">
              <div 
                v-for="roleOption in roleOptions" 
                :key="roleOption.value"
                class="role-item"
              >
                <label class="role-radio">
                  <input 
                    type="radio" 
                    :value="roleOption.value"
                    v-model="roleModal.selectedRole"
                    name="role"
                  />
                  <div class="role-info">
                    <span class="role-name">{{ roleOption.label }}</span>
                    <span class="role-desc">{{ roleOption.desc }}</span>
                  </div>
                </label>
              </div>
            </div>
          </div>
          <div class="form-actions">
            <button type="button" class="btn-secondary" @click="closeRoleModal">取消</button>
            <button type="button" class="btn-primary" @click="handleSaveRole">保存</button>
          </div>
        </div>
        </div>
      </div>
    </Transition>

    <!-- 密码重置对话框 -->
    <Transition name="modal-fade">
      <div class="modal-overlay" v-if="passwordResetModal.visible" @click="closePasswordResetModal">
        <div class="modal-content" @click.stop>
          <div class="modal-header">
            <h3>重置密码 - {{ passwordResetModal.user?.name }}</h3>
            <button class="close-btn" @click="closePasswordResetModal">×</button>
          </div>
          <div class="modal-body">
            <div class="form-group">
              <label>用户名</label>
              <div style="padding: 10px; background: rgba(15, 23, 42, 0.6); border-radius: 8px; margin-bottom: 15px;">
                <span style="color: rgba(255, 255, 255, 0.9);">{{ passwordResetModal.user?.username }}</span>
              </div>
            </div>
            <div class="form-group">
              <label>新密码 <span class="required">*</span></label>
              <input 
                type="password" 
                v-model="passwordResetModal.newPassword" 
                placeholder="请输入新密码（至少6个字符）"
                maxlength="50"
              />
              <div class="form-hint">密码长度至少6个字符，建议包含字母和数字</div>
            </div>
            <div class="form-group">
              <label>确认新密码 <span class="required">*</span></label>
              <input 
                type="password" 
                v-model="passwordResetModal.confirmPassword" 
                placeholder="请再次输入新密码"
                maxlength="50"
              />
            </div>
            <div class="form-actions">
              <button type="button" class="btn-secondary" @click="closePasswordResetModal">取消</button>
              <button type="button" class="btn-primary" @click="handleSavePasswordReset" :disabled="passwordResetModal.loading">
                {{ passwordResetModal.loading ? '重置中...' : '确认重置' }}
              </button>
            </div>
          </div>
        </div>
      </div>
    </Transition>

    <!-- 批量导入对话框 -->
    <Transition name="modal-fade">
      <div class="modal-overlay" v-if="batchModal.visible" @click="closeBatchModal">
        <div class="modal-content" @click.stop style="max-width:920px;">
          <div class="modal-header">
            <h3>批量导入用户</h3>
            <button class="close-btn" @click="closeBatchModal">×</button>
          </div>
          <div class="modal-body">
            <div style="margin-bottom:12px;color:var(--color-text-secondary);">
              支持 CSV 或 Excel 文件（建议按照模板填写：姓名,用户名,身份证号,手机,角色,password）。导入会创建新用户或跳过重复用户名。
            </div>
            <div style="display:flex;gap:12px;align-items:center;margin-bottom:12px;">
              <input type="file" accept=".csv,application/vnd.ms-excel,application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" @change="onBatchFileChange" />
              <button class="btn-secondary" @click="downloadImportTemplate" title="下载导入模板（CSV）">下载模板</button>
              <div style="display:flex;gap:8px;align-items:center;margin-left:auto">
                <button class="gen-btn" @click="() => generateAllRandom(false)" title="为所有空值行随机生成身份证/手机号/姓名/用户名">随机填充空值</button>
                <button class="gen-btn" @click="() => generateAllRandom(true)" title="为所有行随机生成（覆盖空值或全部）">为所有行随机生成</button>
                <button class="btn btn-primary" @click="doBatchImport" :disabled="batchModal.loading">{{ batchModal.loading ? '导入中...' : '开始导入' }}</button>
              </div>
            </div>
            <!-- 手动多条导入模板编辑 -->
            <div style="margin-top:12px;border-top:1px dashed rgba(255,255,255,0.04);padding-top:12px;">
              <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:8px;">
                <div style="font-weight:600;color:var(--color-text-primary)">手动多条导入（模板行）</div>
                <div style="display:flex;gap:8px;align-items:center">
                  <button class="btn-secondary" @click="addManualRow" title="新增一空行">新增一行</button>
                  <input type="number" v-model.number="multipleCount" min="1" style="width:72px;padding:6px;border-radius:8px;border:1px solid rgba(255,255,255,0.06);background:rgba(15,23,42,0.6);color:#fff;margin-left:6px" />
                  <button class="btn-secondary" @click="addMultipleRows" title="按数量新增多行">倍数新增</button>
        
                  <button class="btn btn-primary" @click="submitManualRows" :disabled="manualLoading">{{ manualLoading ? '导入中...' : '导入所填行' }}</button>
                </div>
              </div>

              <div style="max-height:240px;overflow:auto;border-radius:8px;background:rgba(255,255,255,0.02);padding:8px;">
                <table style="width:100%;border-collapse:collapse;">
                  <thead>
                    <tr style="text-align:left;color:var(--color-text-secondary);font-size:13px;">
                      <th style="padding:6px 8px;width:28%;">姓名</th>
                      <th style="padding:6px 8px;width:20%;">用户名</th>
                      <th style="padding:6px 8px;width:18%;">身份证号</th>
                      <th style="padding:6px 8px;width:12%;">手机号</th>
                      <th style="padding:6px 8px;width:10%;">角色</th>
                      <th style="padding:6px 8px;width:12%;">操作</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="(row, idx) in manualRows" :key="idx" style="border-top:1px solid rgba(255,255,255,0.02);">
                      <td style="padding:6px 8px;display:flex;gap:8px;align-items:center;">
                        <input v-model="row.name" placeholder="姓名" />
                        <button class="small-btn" @click="generateNameForRow(idx)" title="随机生成姓名">随机</button>
                      </td>
                      <td style="padding:6px 8px;display:flex;gap:8px;align-items:center;">
                        <input v-model="row.username" placeholder="用户名" />
                        <button class="small-btn" @click="generateUsernameForRow(idx)" title="随机生成用户名">随机</button>
                      </td>
                      <td style="padding:6px 8px;display:flex;gap:8px;align-items:center;">
                        <input v-model="row.idCard" placeholder="身份证号" />
                        <button class="small-btn" @click="generateIdForRow(idx)" title="随机生成身份证">随机</button>
                      </td>
                      <td style="padding:6px 8px;display:flex;gap:8px;align-items:center;">
                        <input v-model="row.phone" placeholder="手机号" />
                        <button class="small-btn" @click="generatePhoneForRow(idx)" title="随机生成手机号">随机</button>
                      </td>
                      <td style="padding:6px 8px;">
                        <select v-model="row.role">
                          <option value="passenger">普通乘客</option>
                          <option value="operator">航空运营</option>
                          <option value="admin">系统管理员</option>
                        </select>
                      </td>
                      <td style="padding:6px 8px;">
                        <button class="btn btn-ghost" @click="removeManualRow(idx)">删除</button>
                      </td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </div>

            <div v-if="batchModal.result" style="margin-top:12px;">
              <div style="font-weight:600;color:var(--color-text-primary);margin-bottom:8px;">导入结果</div>
              <div style="color:var(--color-text-secondary);margin-bottom:8px;">
                {{ JSON.stringify(batchModal.result.summary) }}
              </div>
              <div v-if="(batchModal.result.errors || []).length > 0" style="margin-top:8px;">
                <div style="font-weight:600;color:var(--color-text-primary);margin-bottom:6px;">失败明细</div>
                <ul style="color:var(--color-text-secondary);padding-left:16px;">
                  <li v-for="(e, i) in batchModal.result.errors" :key="i" style="margin-bottom:6px;">
                    {{ e.row ? ('第' + e.row + '行: ') : '' }}{{ e.message || e.reason || JSON.stringify(e) }}
                  </li>
                </ul>
              </div>
            </div>
          </div>
        </div>
      </div>
    </Transition>
  </AdminLayout>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import AdminLayout from '../components/AdminLayout.vue'
import { userManagementApi } from '../services/api'
import { formatDateTime } from '../utils/dateFormat'

interface User {
  id: string
  name: string
  username: string
  role: 'admin' | 'operator' | 'passenger'
  department: 'flight' | 'service' | 'tech'
  email: string
  phone: string
  lastLogin: string
  status: 'active' | 'disabled'
}


const searchParams = reactive({
  keyword: '',
  role: '',
  department: '',
  status: ''
})

const users = ref<User[]>([])

const currentPage = ref(1)
const pageSize = ref(5)
const jumpPage = ref(1)

const userModal = reactive({
  visible: false,
  mode: 'add' as 'add' | 'edit',
  user: null as User | null
})

const roleModal = reactive({
  visible: false,
  user: null as User | null,
  selectedRole: '' as string
})

const passwordResetModal = reactive({
  visible: false,
  user: null as User | null,
  newPassword: '',
  confirmPassword: '',
  loading: false
})

const userForm = reactive({
  name: '',
  username: '',
  idCard: '',
  phone: '',
  role: '',
  password: '',
  confirmPassword: ''
})

// 角色选项 - 与后端保持一致
const roleOptions = [
  { value: 'admin', label: '系统管理员', desc: '拥有系统所有权限' },
  { value: 'operator', label: '航空运营', desc: '可以管理航班和订单' },
  { value: 'passenger', label: '普通乘客', desc: '普通用户权限' }
]

const getRoleText = (role: string) => {
  const map: Record<string, string> = {
    admin: '系统管理员',
    operator: '航空运营',
    passenger: '普通乘客'
  }
  return map[role] || role
}

const getDepartmentText = (dept: string) => {
  const map: Record<string, string> = {
    flight: '航班运营',
    service: '客服中心',
    tech: '技术支持'
  }
  return map[dept] || dept
}

// 后端分页数据
const total = ref(0)
const totalPages = computed(() => Math.ceil(total.value / pageSize.value))

// 直接使用后端返回的分页数据，不再进行客户端分页
const paginatedUsers = computed(() => users.value)

const handleSearch = async () => {
  currentPage.value = 1
  jumpPage.value = 1
  await loadUsers()
}

const handlePageSizeChange = async () => {
  currentPage.value = 1
  jumpPage.value = 1
  await loadUsers()
}

const handleReset = async () => {
  searchParams.keyword = ''
  searchParams.role = ''
  searchParams.department = ''
  searchParams.status = ''
  currentPage.value = 1
  jumpPage.value = 1
  await loadUsers()
}

const handleAdd = () => {
  userModal.mode = 'add'
  userModal.user = null
  Object.assign(userForm, {
    name: '',
    username: '',
    idCard: '',
    phone: '',
    role: 'passenger',
    password: '',
    confirmPassword: ''
  })
  userModal.visible = true
}

const handleEdit = (user: User) => {
  userModal.mode = 'edit'
  userModal.user = user
  Object.assign(userForm, {
    name: user.name,
    username: user.username,
    idCard: '', // 编辑时通常不显示身份证号（安全考虑）
    phone: user.phone,
    role: user.role,
    password: '',
    confirmPassword: ''
  })
  userModal.visible = true
}

const handleSubmit = async () => {
  try {
    // 验证必填字段
    if (!userForm.username || !userForm.name || !userForm.phone || !userForm.role) {
      alert('请填写所有必填字段')
      return
    }
    
    // 新增用户时验证身份证号和密码
    if (userModal.mode === 'add') {
      if (!userForm.idCard) {
        alert('请输入身份证号')
        return
      }
      if (!userForm.password) {
        alert('请输入密码')
        return
      }
      if (userForm.password.length < 6) {
        alert('密码至少6个字符')
        return
      }
      if (userForm.password !== userForm.confirmPassword) {
        alert('两次输入的密码不一致')
        return
      }
      
      await userManagementApi.createUser({
        name: userForm.name,
        username: userForm.username,
        idCard: userForm.idCard,
        phone: userForm.phone,
        role: userForm.role,
        password: userForm.password
      })
      alert('用户创建成功')
    } else {
      // 编辑用户时，如果修改了密码，需要验证确认密码
      if (userForm.password) {
        if (userForm.password.length < 6) {
          alert('密码至少6个字符')
          return
        }
        if (userForm.password !== userForm.confirmPassword) {
          alert('两次输入的密码不一致')
          return
        }
      }
      
      await userManagementApi.updateUser(userModal.user!.id, {
        name: userForm.name,
        username: userForm.username,
        phone: userForm.phone,
        role: userForm.role,
        password: userForm.password || undefined
      })
      alert('用户更新成功')
    }
    closeUserModal()
    await loadUsers()
  } catch (error: any) {
    console.error('保存用户失败:', error)
    alert(error.message || '保存失败，请重试')
  }
}

const closeUserModal = () => {
  userModal.visible = false
  userModal.user = null
}

const handleChangeRole = (user: User) => {
  roleModal.user = user
  roleModal.selectedRole = user.role || 'passenger'
  roleModal.visible = true
}

const handleSaveRole = async () => {
  if (!roleModal.user || !roleModal.selectedRole) {
    alert('请选择角色')
    return
  }
  
  try {
    await userManagementApi.updateUser(roleModal.user.id, {
      role: roleModal.selectedRole
    })
    alert('角色更新成功')
    closeRoleModal()
    await loadUsers() // 重新加载用户列表
  } catch (error: any) {
    console.error('更新角色失败:', error)
    alert(error.message || '更新失败，请重试')
  }
}

const closeRoleModal = () => {
  roleModal.visible = false
  roleModal.user = null
  roleModal.selectedRole = ''
}

const handleToggleStatus = async (user: User) => {
  const action = user.status === 'active' ? '禁用' : '启用'
  if (confirm(`确定要${action}用户 ${user.name} 吗？`)) {
    try {
      await userManagementApi.toggleUserStatus(user.id)
      await loadUsers()
      alert(`${action}成功`)
    } catch (error: any) {
      console.error(`${action}用户失败:`, error)
      alert(error.message || `${action}失败，请重试`)
    }
  }
}

const handleBatchImport = () => {
  batchModal.visible = true
}

const handleResetPassword = (user: User) => {
  passwordResetModal.user = user
  passwordResetModal.newPassword = ''
  passwordResetModal.confirmPassword = ''
  passwordResetModal.loading = false
  passwordResetModal.visible = true
}

const closePasswordResetModal = () => {
  passwordResetModal.visible = false
  passwordResetModal.user = null
  passwordResetModal.newPassword = ''
  passwordResetModal.confirmPassword = ''
  passwordResetModal.loading = false
}

const handleSavePasswordReset = async () => {
  if (!passwordResetModal.user) {
    alert('用户信息错误')
    return
  }

  // 验证密码
  if (!passwordResetModal.newPassword) {
    alert('请输入新密码')
    return
  }

  if (passwordResetModal.newPassword.length < 6) {
    alert('密码至少需要6个字符')
    return
  }

  if (passwordResetModal.newPassword !== passwordResetModal.confirmPassword) {
    alert('两次输入的密码不一致')
    return
  }

  // 确认操作
  if (!confirm(`确定要重置用户 ${passwordResetModal.user.name} 的密码吗？`)) {
    return
  }

  passwordResetModal.loading = true

  try {
    await userManagementApi.resetPassword(passwordResetModal.user.id, {
      newPassword: passwordResetModal.newPassword
    })
    alert('密码重置成功')
    closePasswordResetModal()
  } catch (error: any) {
    console.error('重置密码失败:', error)
    alert(error.message || '重置密码失败，请重试')
  } finally {
    passwordResetModal.loading = false
  }
}

// 批量导入 modal 状态
const batchModal = reactive({
  visible: false,
  file: null as File | null,
  loading: false,
  result: null as any
})

const onBatchFileChange = (e: Event) => {
  const input = e.target as HTMLInputElement
  if (input.files && input.files.length > 0) {
    batchModal.file = input.files[0] as File
  } else {
    batchModal.file = null
  }
}

const closeBatchModal = () => {
  batchModal.visible = false
  batchModal.file = null
  batchModal.loading = false
  batchModal.result = null
}

const doBatchImport = async () => {
  if (!batchModal.file) {
    alert('请选择要导入的文件（CSV / XLSX）')
    return
  }
  batchModal.loading = true
  try {
    const res: any = await userManagementApi.batchImportUsers(batchModal.file as File)
    // try to normalize response
    const data = res?.data ?? res
    batchModal.result = {
      success: res?.success ?? true,
      summary: data?.summary ?? data?.result ?? data,
      errors: data?.errors ?? data?.failed ?? data?.failedRecords ?? []
    }
    alert('导入完成，请查看导入结果')
    // reload users list to reflect new users
    await loadUsers()
  } catch (err: any) {
    console.error('批量导入失败', err)
    alert(err?.message || '批量导入失败，请重试')
  } finally {
    batchModal.loading = false
  }

}

const exportLoading = ref(false)
const handleExport = async () => {
  try {
    exportLoading.value = true
    // Fetch all users for export (using a large page size)
    const requestParams = { 
      page: 0, 
      size: 10000,
      keyword: searchParams.keyword || undefined,
      role: searchParams.role || undefined,
      status: searchParams.status || undefined
    }
    
    const result = await userManagementApi.getUserList(requestParams)
    let exportData: any[] = []
    
    if (result && result.success && result.data) {
      const data = result.data
      let usersArray: any[] = []
      if (Array.isArray(data.users)) usersArray = data.users
      else if (Array.isArray(data.content)) usersArray = data.content
      else if (Array.isArray(data)) usersArray = data
      
      exportData = usersArray.map((u: any) => ({
        id: u.id,
        name: u.realName || u.name || '',
        username: u.username || '',
        role: getRoleText(u.role || 'passenger'),
        phone: u.phone || '',
        status: u.status === 'active' ? '启用' : '禁用',
        lastLogin: u.updatedTime ? formatDateTime(u.updatedTime) : (u.registrationTime ? formatDateTime(u.registrationTime) : '')
      }))
    }

    if (exportData.length === 0) {
      alert('没有可导出的数据')
      return
    }

    const headers = ['ID', '姓名', '用户名', '角色', '手机号', '状态', '最近更新/注册']
    const csvContent = [
      '\ufeff' + headers.join(','),
      ...exportData.map(row => [
        row.id,
        row.name,
        row.username,
        row.role,
        row.phone,
        row.status,
        row.lastLogin
      ].map(f => `"${String(f || '').replace(/"/g, '""')}"`).join(','))
    ].join('\n')

    const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' })
    const link = document.createElement('a')
    link.href = URL.createObjectURL(blob)
    link.download = `用户列表_${new Date().toLocaleDateString()}.csv`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    
  } catch (err) {
    console.error('导出失败', err)
    alert('导出报表失败')
  } finally {
    exportLoading.value = false
  }
}

const downloadImportTemplate = () => {
  const csvLines = [
    '姓名,用户名,身份证号,手机,角色,password',
    '张三,zhangsan,110101199001011234,13800138000,passenger,password123',
   
  ]
  const csvContent = '\ufeff' + csvLines.join('\n')
  const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' })
  const link = document.createElement('a')
  link.href = URL.createObjectURL(blob)
  link.download = 'user_import_template.csv'
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  URL.revokeObjectURL(link.href)
}
// 手动模板行数据与导入
const manualRows = ref<Array<any>>([
  { name: '', username: '', idCard: '', phone: '', role: 'passenger', password: '' }
])
const manualLoading = ref(false)
const multipleCount = ref(5)

const addManualRow = () => {
  manualRows.value.push({ name: '', username: '', idCard: '', phone: '', role: 'passenger', password: '' })
}
const removeManualRow = (idx: number) => {
  manualRows.value.splice(idx, 1)
}

const addMultipleRows = () => {
  const n = Math.max(1, Math.floor(Number(multipleCount.value) || 0))
  for (let i = 0; i < n; i++) {
    manualRows.value.push({ name: '', username: '', idCard: '', phone: '', role: 'passenger', password: '' })
  }
}

const submitManualRows = async () => {
  if (manualRows.value.length === 0) {
    alert('没有要导入的行')
    return
  }
  // validate rows
  const rowsToImport = manualRows.value.filter(r => (r.username && r.name))
  if (rowsToImport.length === 0) {
    alert('请至少填写用户名和姓名的行')
    return
  }
  if (!confirm(`确认导入 ${rowsToImport.length} 条用户记录吗？`)) return
  manualLoading.value = true
  try {
    const results = await Promise.allSettled(rowsToImport.map(r => {
      // ensure password (if empty, set default)
      const pwd = r.password && r.password.length >= 6 ? r.password : 'Password123'
      return userManagementApi.createUser({
        name: r.name,
        username: r.username,
        idCard: r.idCard,
        phone: r.phone,
        role: r.role,
        password: pwd
      })
    }))
    const summary = {
      total: results.length,
      succeeded: results.filter(x => x.status === 'fulfilled').length,
      failed: results.filter(x => x.status === 'rejected').length
    }
    const errors: any[] = []
    results.forEach((r, i) => {
      if (r.status === 'rejected') {
        errors.push({ row: i + 1, message: (r.reason && r.reason.message) ? r.reason.message : JSON.stringify(r.reason) })
      }
    })
    batchModal.result = { success: summary.failed === 0, summary, errors }
    alert(`导入完成：成功 ${summary.succeeded}，失败 ${summary.failed}`)
    await loadUsers()
  } catch (e) {
    console.error('手动批量导入失败', e)
    alert('手动批量导入失败')
  } finally {
    manualLoading.value = false
  }
}

// 随机生成身份证与手机号工具
const randomInt = (min:number, max:number) => Math.floor(Math.random() * (max - min + 1)) + min

const pad = (n:number, width:number) => String(n).padStart(width, '0')

const randomDateString = (startYear:number, endYear:number) => {
  const y = randomInt(startYear, endYear)
  const m = pad(randomInt(1, 12), 2)
  let dayMax = 31
  if (['04','06','09','11'].includes(m)) dayMax = 30
  if (m === '02') dayMax = (y % 4 === 0 && y % 100 !== 0) || (y % 400 === 0) ? 29 : 28
  const d = pad(randomInt(1, dayMax), 2)
  return `${y}${m}${d}`
}

const randomIdCard = () => {
  const area = '110101' // 默认北京市朝阳区
  const birth = randomDateString(1960, 2002)
  const seq = pad(randomInt(1, 999), 3)
  const check = pad(randomInt(0, 9), 1)
  return area + birth + seq + check
}

const randomPhone = () => {
  const prefixes = ['139','138','137','136','135','150','151','152','157','158','159','182','183','184','187','188','198']
  const pre = prefixes[randomInt(0, prefixes.length - 1)]
  const rest = pad(randomInt(0, 99999999), 8)
  return pre + rest
}

const generateIdForRow = (idx:number) => {
  if (!manualRows.value[idx]) return
  manualRows.value[idx].idCard = randomIdCard()
}

const generatePhoneForRow = (idx:number) => {
  if (!manualRows.value[idx]) return
  manualRows.value[idx].phone = randomPhone()
}

// name / username generation
const surnames = ['赵','钱','孙','李','周','吴','郑','王','冯','陈','褚','卫','蒋','沈','韩','杨','朱','秦','尤','许','何','吕','施','张','孔','曹','严','华']
const givenChars = ['伟','芳','娜','敏','静','丽','强','磊','洋','勇','艳','杰','娟','涛','超','明','艳','刚','平','桂','霞','浩','玲']

const randomChineseName = () => {
  const s = surnames[randomInt(0, surnames.length - 1)]
  const givenLen = Math.random() < 0.6 ? 1 : 2
  let g = ''
  for (let i = 0; i < givenLen; i++) g += givenChars[randomInt(0, givenChars.length - 1)]
  return s + g
}

const randomUsername = (base?: string) => {
  const suffix = randomInt(100, 9999)
  if (base && typeof base === 'string' && base.length > 0) {
    // try to make ascii-friendly username from base: take unicode code points
    const simple = base.split('').map(c => c.charCodeAt(0).toString(36)).join('').slice(0,8)
    return `${simple}${suffix}`
  }
  return `user${suffix}`
}

const generateNameForRow = (idx:number) => {
  if (!manualRows.value[idx]) return
  manualRows.value[idx].name = randomChineseName()
}

const generateUsernameForRow = (idx:number) => {
  if (!manualRows.value[idx]) return
  const base = manualRows.value[idx].name || ''
  manualRows.value[idx].username = randomUsername(base)
}

const generateRowRandom = (idx:number) => {
  generateIdForRow(idx)
  generatePhoneForRow(idx)
  if (!manualRows.value[idx].name) generateNameForRow(idx)
  if (!manualRows.value[idx].username) generateUsernameForRow(idx)
}

const generateAllRandom = (force = false) => {
  manualRows.value.forEach((r, i) => {
    if (force || !r.idCard) r.idCard = randomIdCard()
    if (force || !r.phone) r.phone = randomPhone()
    if (force || !r.name) r.name = randomChineseName()
    if (force || !r.username) r.username = randomUsername(r.name)
  })
}
const goToPage = async (page: number) => {
  if (page >= 1 && page <= totalPages.value) {
    currentPage.value = page
    jumpPage.value = page
    await loadUsers() // 重新加载数据
  }
}

// 加载用户列表
const loadUsers = async () => {
  try {
    console.log('开始加载用户列表，参数:', {
      page: currentPage.value - 1,
      size: pageSize.value,
      keyword: searchParams.keyword,
      role: searchParams.role,
      status: searchParams.status
    })
    
    const requestParams = { 
      page: currentPage.value - 1, 
      size: pageSize.value,
      keyword: searchParams.keyword || undefined,
      role: searchParams.role || undefined,
      status: searchParams.status || undefined
    }
    console.log('发送请求参数:', requestParams)
    const result = await userManagementApi.getUserList(requestParams)
    
    console.log('API返回结果:', result)
    console.log('API返回结果类型:', typeof result)
    console.log('API返回结果JSON:', JSON.stringify(result, null, 2))
    
    if (result && result.success && result.data) {
      const data = result.data
      console.log('解析后的数据:', data)
      console.log('数据类型:', typeof data)
      console.log('用户列表:', data.users)
      console.log('用户列表类型:', typeof data.users)
      console.log('用户列表是否为数组:', Array.isArray(data.users))
      console.log('用户列表长度:', data.users ? data.users.length : 0)
      
      // 确保 users 是数组 - 兼容不同的后端返回格式
      // Spring Data Page 默认使用 content 字段，但我们的后端返回的是 users 字段
      let usersArray: any[] = []
      if (Array.isArray(data.users)) {
        usersArray = data.users
      } else if (Array.isArray(data.content)) {
        usersArray = data.content
      } else if (Array.isArray(data)) {
        // 如果 data 本身就是数组（不应该，但兼容处理）
        usersArray = data
      }
      
      console.log('处理后的用户数组:', usersArray)
      console.log('处理后的用户数组长度:', usersArray.length)
      
      // 转换后端数据格式为前端格式
      // 后端返回的User实体包含: id, username, realName, phone, role, status, registrationTime, updatedTime
      users.value = usersArray.map((u: any) => {
        console.log('处理用户:', u)
        // 处理日期格式 - 严格使用后端数据库的时间，不进行时区转换
        let lastLogin = ''
        if (u.updatedTime) {
          lastLogin = formatDateTime(u.updatedTime)
        } else if (u.registrationTime) {
          lastLogin = formatDateTime(u.registrationTime)
        }
        
        return {
          id: u.id ? u.id.toString() : '',
          name: u.realName || u.name || '',
          username: u.username || '',
          role: u.role || 'passenger',
          department: 'flight', // 后端暂无此字段，使用默认值
          email: '', // 后端暂无此字段
          phone: u.phone || '',
          lastLogin: lastLogin,
          status: u.status || 'active'
        }
      })
      
      console.log('转换后的用户列表:', users.value)
      
      // 更新总数和当前页
      total.value = data.total || data.totalElements || 0
      currentPage.value = (data.page !== undefined ? data.page : (data.number !== undefined ? data.number : 0)) + 1 // 后端从0开始，前端从1开始
      jumpPage.value = currentPage.value
      
      console.log('更新后的总数:', total.value, '当前页:', currentPage.value)
      console.log('最终用户列表:', users.value)
      console.log('最终用户列表长度:', users.value.length)
    } else {
      console.warn('获取用户列表失败，返回结果:', result)
      console.warn('result类型:', typeof result)
      console.warn('result.success:', result?.success)
      console.warn('result.data:', result?.data)
      console.warn('result结构:', JSON.stringify(result, null, 2))
      
      // 尝试兼容不同的响应格式
      if (result && result.data && Array.isArray(result.data)) {
        // 如果data直接是数组（虽然不应该，但兼容处理）
        console.log('检测到data是数组格式，尝试转换')
        users.value = result.data.map((u: any) => ({
          id: u.id ? u.id.toString() : '',
          name: u.realName || u.name || '',
          username: u.username || '',
          role: u.role || 'passenger',
          department: 'flight',
          email: '',
          phone: u.phone || '',
          lastLogin: u.updatedTime || u.registrationTime || '',
          status: u.status || 'active'
        }))
        total.value = result.data.length
      } else {
        users.value = []
        total.value = 0
      }
    }
  } catch (error: any) {
    console.error('加载用户列表失败:', error)
    console.error('错误详情:', error.stack)
    users.value = []
    total.value = 0
    // 显示错误提示
    if (error.message && error.message.includes('无法连接到服务器')) {
      alert('无法连接到服务器，请确保后端服务已启动')
    } else {
      alert('加载用户列表失败：' + (error.message || '未知错误'))
    }
  }
}

const onAutoRefresh = async () => {
  try {
    // 如果当前页面可见且没有正在加载，则刷新
    if (document.hidden) return
    await loadUsers()
  } catch (e) {
    console.warn('auto-refresh loadUsers failed', e)
  }
}

onMounted(() => {
  loadUsers()
  window.addEventListener('auto-refresh', onAutoRefresh)
})

onUnmounted(() => {
  window.removeEventListener('auto-refresh', onAutoRefresh)
})
</script>

<style scoped>
/* 基础样式内联 */
.breadcrumb {
  margin-bottom: 20px;
  font-size: 14px;
  color: #64748b; /* Slate-500 */
}

.breadcrumb-separator {
  margin: 0 8px;
  color: #94a3b8; /* Slate-400 */
}

/* Glassmorphism Card Style (Light Theme) */
.admin-card {
  background: rgba(255, 255, 255, 0.7);
  border: 1px solid rgba(255, 255, 255, 0.8);
  border-radius: 16px;
  backdrop-filter: blur(20px);
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05), 0 2px 4px -1px rgba(0, 0, 0, 0.03);
  margin-bottom: 20px;
}

.search-section {
  padding: 24px;
}

.search-row {
  display: flex;
  align-items: flex-end;
  gap: 20px;
  flex-wrap: wrap;
}

.search-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.search-item label {
  font-size: 13px;
  font-weight: 600;
  color: #475569; /* Slate-600 */
}

.search-item input,
.search-item select {
  padding: 10px 14px;
  border: 1px solid #cbd5e1; /* Slate-300 */
  border-radius: 10px;
  font-size: 14px;
  background: rgba(255, 255, 255, 0.8);
  color: #1e293b; /* Slate-800 */
  min-width: 160px;
  transition: all 0.2s;
}

.search-item input:focus,
.search-item select:focus {
  outline: none;
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
  background: #fff;
}

.action-buttons {
  margin-bottom: 20px;
  display: flex;
  gap: 12px;
}

.btn-primary,
.btn-secondary,
.search-btn,
.reset-btn {
  padding: 10px 20px;
  border: none;
  border-radius: 10px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 600;
  transition: all 0.2s;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.btn-primary, .search-btn {
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
  color: #fff;
  box-shadow: 0 2px 4px rgba(59, 130, 246, 0.2);
}

.btn-primary:hover, .search-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.3);
}

.btn-secondary, .reset-btn {
  background: #fff;
  color: #475569;
  border: 1px solid #e2e8f0;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
}

.btn-secondary:hover, .reset-btn:hover {
  background: #f8fafc;
  color: #1e293b;
  border-color: #cbd5e1;
}

/* Table Styles */
.table-container {
  overflow-x: auto;
  border-radius: 16px;
}

.data-table {
  width: 100%;
  border-collapse: separate;
  border-spacing: 0;
}

.data-table th {
  background: rgba(241, 245, 249, 0.8); /* Slate-100 */
  padding: 16px;
  text-align: left;
  font-weight: 600;
  color: #475569;
  border-bottom: 1px solid #e2e8f0;
}

.data-table td {
  padding: 16px;
  border-bottom: 1px solid #f1f5f9;
  color: #334155;
  background: transparent;
}

.data-table tbody tr:hover td {
  background: rgba(255, 255, 255, 0.5);
}

/* Badges */
.status-badge {
  padding: 4px 10px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
}

.status-badge.active {
  background: #dcfce7;
  color: #166534;
  border: 1px solid #bbf7d0;
}

.status-badge.disabled, 
.status-badge.inactive {
  background: #fee2e2;
  color: #991b1b;
  border: 1px solid #fecaca;
}

.role-badge {
  padding: 4px 10px;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 600;
}

.role-badge.admin {
  background: #fff1f2;
  color: #be123c;
  border: 1px solid #fecdd3;
}

.role-badge.operator {
  background: #eff6ff;
  color: #1e40af;
  border: 1px solid #dbeafe;
}

.role-badge.passenger {
  background: #f8fafc;
  color: #475569;
  border: 1px solid #e2e8f0;
}

/* Action Buttons in Table */
.action-buttons-small {
  display: flex;
  gap: 8px;
}

.action-btn {
  padding: 6px 12px;
  border-radius: 8px;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  border: 1px solid transparent;
}

.permission-btn { background: #e0f2fe; color: #0369a1; }
.permission-btn:hover { background: #bae6fd; }

.edit-btn { background: #dcfce7; color: #15803d; }
.edit-btn:hover { background: #bbf7d0; }

.reset-password-btn { background: #f3e8ff; color: #7e22ce; }
.reset-password-btn:hover { background: #e9d5ff; }

.disable-btn { background: #fee2e2; color: #b91c1c; }
.disable-btn:hover { background: #fecaca; }

.enable-btn { background: #dcfce7; color: #15803d; }
.enable-btn:hover { background: #bbf7d0; }

/* Pagination */
.pagination {
  padding: 20px 24px;
  border-top: 1px solid #e2e8f0;
}

.pagination-info {
  color: #64748b;
  font-size: 14px;
}

.page-size-select {
  background: #fff;
  border: 1px solid #cbd5e1;
  color: #334155;
  padding: 4px 8px;
  border-radius: 6px;
}

.page-btn {
  background: #fff;
  border: 1px solid #e2e8f0;
  color: #64748b;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
}

.page-btn:hover:not(:disabled) {
  background: #f8fafc;
  color: #3b82f6;
  border-color: #cbd5e1;
}

.page-number {
  color: #334155;
  font-weight: 600;
}

.page-jump {
  color: #64748b;
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 8px;
  margin-left: 16px;
}

.page-jump input {
  width: 48px;
  padding: 4px 8px;
  border: 1px solid #cbd5e1;
  border-radius: 6px;
  text-align: center;
  color: #334155;
}

/* 弹窗过渡动画 */
.modal-fade-enter-active,
.modal-fade-leave-active {
  transition: opacity 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.modal-fade-enter-active .modal-content,
.modal-fade-leave-active .modal-content {
  transition: all 0.35s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.modal-fade-enter-from {
  opacity: 0;
}

.modal-fade-enter-from .modal-content {
  opacity: 0;
  transform: scale(0.85) translateY(-20px);
}

.modal-fade-leave-to {
  opacity: 0;
}

.modal-fade-leave-to .modal-content {
  opacity: 0;
  transform: scale(0.9) translateY(10px);
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(6, 11, 40, 0.75);
  backdrop-filter: blur(8px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 10000;
  padding: 20px;
  box-sizing: border-box;
  overflow-y: auto;
  overscroll-behavior: contain;
  -webkit-overflow-scrolling: touch;
}

.modal-content {
  background: linear-gradient(135deg, rgba(15, 23, 42, 0.98) 0%, rgba(5, 11, 30, 0.98) 100%);
  border-radius: 24px;
  width: 90%;
  max-width: 500px;
  max-height: 85vh;
  overflow-y: auto;
  border: 1px solid rgba(148, 163, 184, 0.15);
  box-shadow:
    0 30px 60px rgba(0, 0, 0, 0.8),
    0 0 0 1px rgba(148, 163, 184, 0.1),
    inset 0 1px 0 rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(24px);
  position: relative;
  animation: modal-pop 0.35s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.modal-content::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(90deg, 
    transparent 0%, 
    rgba(59, 130, 246, 0.3) 50%, 
    transparent 100%);
  pointer-events: none;
}

@keyframes modal-pop {
  0% {
    opacity: 0;
    transform: scale(0.85) translateY(-20px);
  }
  100% {
    opacity: 1;
    transform: scale(1) translateY(0);
  }
}

.modal-header {
  padding: 28px 28px 20px;
  border-bottom: 1px solid rgba(148, 163, 184, 0.12);
  display: flex;
  justify-content: space-between;
  align-items: center;
  position: relative;

}

.modal-header h3 {
  margin: 0;
  color: #f8fafc;
  font-size: 24px;
  font-weight: 700;
  letter-spacing: -0.02em;
  background: linear-gradient(135deg, #f8fafc 0%, #cbd5e1 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.close-btn {
  background: rgba(148, 163, 184, 0.1);
  border: 1px solid rgba(148, 163, 184, 0.2);
  font-size: 24px;
  color: #cbd5e1;
  cursor: pointer;
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 10px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  line-height: 1;
}

.close-btn:hover {
  background: rgba(239, 68, 68, 0.15);
  border-color: rgba(239, 68, 68, 0.3);
  color: #f87171;
  transform: rotate(90deg) scale(1.05);
}

.close-btn:active {
  transform: rotate(90deg) scale(0.95);
}

.modal-body {
  padding: 28px;

}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  color: rgba(255, 255, 255, 0.8);
  margin-bottom: 8px;
}

.form-group input,
.form-group select,
.form-group textarea {
  width: 100%;
  padding: 10px;
  border: 1px solid rgba(255, 255, 255, 0.15);
  border-radius: 8px;
  background: rgba(15, 23, 42, 0.6);
  color: #fff;
}

.modal-actions {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
}

.role-badge {
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}

.role-badge.admin {
  background: rgba(244, 67, 54, 0.2);
  color: #ef5350;
  border: 1px solid rgba(244, 67, 54, 0.3);
}

.role-badge.manager {
  background: rgba(255, 152, 0, 0.2);
  color: #ffb74d;
  border: 1px solid rgba(255, 152, 0, 0.3);
}

.role-badge.operator {
  background: rgba(99, 102, 241, 0.2);
  color: #0A2F63;
  border: 1px solid rgba(99, 102, 241, 0.3);
}

.action-buttons-small {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}

.permission-btn {
  background: rgba(99, 102, 241, 0.2);
  color: #0A2F63;
  border: 1px solid rgba(99, 102, 241, 0.3);
}

.permission-btn:hover {
  background: rgba(99, 102, 241, 0.3);
}

.disable-btn {
  background: rgba(244, 67, 54, 0.2);
  color: #ef5350;
  border: 1px solid rgba(244, 67, 54, 0.3);
}

.disable-btn:hover {
  background: rgba(244, 67, 54, 0.3);
}

.enable-btn {
  background: rgba(76, 175, 80, 0.2);
  color: #81c784;
  border: 1px solid rgba(76, 175, 80, 0.3);
}

.enable-btn:hover {
  background: rgba(76, 175, 80, 0.3);
}

.reset-password-btn {
  background: rgba(139, 92, 246, 0.2);
  color: #a78bfa;
  border: 1px solid rgba(139, 92, 246, 0.3);
}

.reset-password-btn:hover {
  background: rgba(139, 92, 246, 0.3);
}

.form-hint {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.5);
  margin-top: 6px;
}

.modal-large {
  max-width: 700px;
}

.user-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
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

.required {
  color: #f87171;
}

.form-item input,
.form-item select {
  padding: 10px 14px;
  border: 1px solid rgba(255, 255, 255, 0.15);
  border-radius: 8px;
  background: rgba(15, 23, 42, 0.6);
  color: #fff;
  font-size: 14px;
}

.form-item input:focus,
.form-item select:focus {
  outline: none;
  border-color: rgba(59, 130, 246, 0.6);
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.15);
  transform: translateY(-1px);
  transition: all 0.2s ease;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 10px;
}

.role-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.role-item {
  padding: 12px;
  background: rgba(15, 23, 42, 0.4);
  border-radius: 12px;
  border: 1px solid rgba(255, 255, 255, 0.1);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  cursor: pointer;
}

.role-item:hover {
  background: rgba(15, 23, 42, 0.7);
  border-color: rgba(59, 130, 246, 0.5);
  transform: translateX(4px);
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.2);
}

.role-item:has(input:checked) {
  background: rgba(59, 130, 246, 0.15);
  border-color: rgba(59, 130, 246, 0.6);
  box-shadow: 0 0 0 2px rgba(59, 130, 246, 0.3);
}

.role-radio {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  width: 100%;
}

.role-radio input[type="radio"] {
  width: 18px;
  height: 18px;
  cursor: pointer;
  accent-color: #1E8AE6;
}

.role-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
  flex: 1;
}

.role-name {
  font-weight: 500;
  color: rgba(255, 255, 255, 0.9);
  font-size: 14px;
}

.role-desc {
  color: rgba(255, 255, 255, 0.6);
  font-size: 12px;
}

.small-btn {
  padding: 6px 8px;
  font-size: 12px;
  border-radius: 6px;
  background: rgba(255,255,255,0.03);
  color: var(--color-text-primary);
  border: 1px solid rgba(255,255,255,0.04);
  cursor: pointer;
}
.small-btn:hover { filter: brightness(1.05); transform: translateY(-1px); }

.btn-secondary {
  padding: 8px 12px;
  border-radius: 8px;
  border: 1px solid rgba(255,255,255,0.08);
  background: linear-gradient(135deg, rgba(15,23,42,0.6), rgba(8,12,20,0.6));
  color: #cfe8ff;
}
.btn-secondary:hover { transform: translateY(-2px); box-shadow: 0 8px 20px rgba(14, 85, 170, 0.08); }

.gen-btn {
  padding: 8px 12px;
  border-radius: 10px;
  background: linear-gradient(90deg, rgba(59,130,246,0.95), rgba(99,102,241,0.9));
  color: #fff;
  border: none;
  box-shadow: 0 10px 24px rgba(59,130,246,0.18);
  cursor: pointer;
  transition: transform .12s ease, box-shadow .12s ease;
}
.gen-btn:hover { transform: translateY(-2px); box-shadow: 0 14px 30px rgba(59,130,246,0.22); }
</style>

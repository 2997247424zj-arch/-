<template>
  <div class="user-center-container">
    <div class="sidebar">
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
    </div>

    <div class="content-box glass-panel">
      <div class="header-action">
        <h2 class="section-title">地址管理</h2>
        <el-button type="primary" @click="openAddDialog">新增地址</el-button>
      </div>

      <el-table :data="addressList" style="width: 100%" v-loading="loading">
        <el-table-column prop="receiver" label="收货人" width="120" />
        <el-table-column prop="phone" label="手机号码" width="150" />
        <el-table-column label="地址">
          <template #default="scope">
            {{ scope.row.province }} {{ scope.row.city }} {{ scope.row.district }}
            {{ scope.row.detailAddress }}
          </template>
        </el-table-column>
        <el-table-column label="默认" width="80" align="center">
          <template #default="scope">
            <el-tag type="success" size="small" v-if="scope.row.isDefault === 1">默认</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right" align="center">
          <template #default="scope">
            <el-button link type="primary" size="small" @click="openEditDialog(scope.row)"
              >编辑</el-button
            >
            <el-button
              link
              type="warning"
              size="small"
              v-if="scope.row.isDefault !== 1"
              @click="setDefault(scope.row.id)"
              >设为默认</el-button
            >
            <el-button link type="danger" size="small" @click="handleDelete(scope.row.id)"
              >删除</el-button
            >
          </template>
        </el-table-column>
      </el-table>

      <!-- Add/Edit Address Dialog -->
      <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑地址' : '新增地址'" width="500px">
        <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
          <el-form-item label="收货人" prop="receiver">
            <el-input v-model="form.receiver" placeholder="请输入收货人姓名" />
          </el-form-item>
          <el-form-item label="手机号码" prop="phone">
            <el-input v-model="form.phone" placeholder="请输入手机号码" />
          </el-form-item>
          <el-form-item label="省市区" prop="region">
            <el-cascader
              v-model="form.region"
              :options="regionOptions"
              placeholder="请选择省市区"
              style="width: 100%"
              @change="handleRegionChange"
            />
          </el-form-item>
          <el-form-item label="详细地址" prop="detailAddress">
            <el-input
              type="textarea"
              v-model="form.detailAddress"
              placeholder="请输入详细地址"
              :rows="3"
            />
          </el-form-item>
          <el-form-item label="设为默认">
            <el-switch v-model="form.isDefault" :active-value="1" :inactive-value="0" />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit" :loading="submitLoading">确定</el-button>
        </template>
      </el-dialog>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { User, Tickets, Location } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getAddressListAPI,
  addAddressAPI,
  updateAddressAPI,
  deleteAddressAPI,
  setDefaultAddressAPI,
} from '@/api/modules/user'
import type { Address } from '@/types'

const activeMenu = '/user/address'
const addressList = ref<Address[]>([])
const loading = ref(false)

const dialogVisible = ref(false)
const submitLoading = ref(false)
const isEdit = ref(false)
const formRef = ref<FormInstance>()

const form = reactive({
  id: 0,
  receiver: '',
  phone: '',
  province: '',
  city: '',
  district: '',
  detailAddress: '',
  isDefault: 0,
  region: [] as string[],
})

// 省市区数据（简化版，实际项目应使用完整数据或API）
const regionOptions = ref([
  {
    value: '北京市',
    label: '北京市',
    children: [
      { value: '北京市', label: '北京市', children: [{ value: '东城区', label: '东城区' }] },
    ],
  },
  {
    value: '上海市',
    label: '上海市',
    children: [
      { value: '上海市', label: '上海市', children: [{ value: '黄浦区', label: '黄浦区' }] },
    ],
  },
  {
    value: '广东省',
    label: '广东省',
    children: [
      {
        value: '深圳市',
        label: '深圳市',
        children: [
          { value: '南山区', label: '南山区' },
          { value: '福田区', label: '福田区' },
          { value: '宝安区', label: '宝安区' },
        ],
      },
      {
        value: '广州市',
        label: '广州市',
        children: [
          { value: '天河区', label: '天河区' },
          { value: '越秀区', label: '越秀区' },
        ],
      },
    ],
  },
  {
    value: '浙江省',
    label: '浙江省',
    children: [
      {
        value: '杭州市',
        label: '杭州市',
        children: [{ value: '西湖区', label: '西湖区' }],
      },
    ],
  },
])

const rules = reactive<FormRules>({
  receiver: [{ required: true, message: '请输入收货人姓名', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入手机号码', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' },
  ],
  region: [{ required: true, message: '请选择省市区', trigger: 'change' }],
  detailAddress: [{ required: true, message: '请输入详细地址', trigger: 'blur' }],
})

const fetchAddresses = async () => {
  loading.value = true
  try {
    const res: any = await getAddressListAPI()
    if (res.data) {
      addressList.value = res.data
    }
  } catch (e) {
    // Error handled by interceptor
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchAddresses()
})

const resetForm = () => {
  form.id = 0
  form.receiver = ''
  form.phone = ''
  form.province = ''
  form.city = ''
  form.district = ''
  form.detailAddress = ''
  form.isDefault = 0
  form.region = []
}

const openAddDialog = () => {
  resetForm()
  isEdit.value = false
  dialogVisible.value = true
}

const openEditDialog = (row: Address) => {
  isEdit.value = true
  form.id = row.id
  form.receiver = row.receiver
  form.phone = row.phone
  form.province = row.province
  form.city = row.city
  form.district = row.district
  form.detailAddress = row.detailAddress
  form.isDefault = row.isDefault
  form.region = [row.province, row.city, row.district]
  dialogVisible.value = true
}

const handleRegionChange = (value: string[]) => {
  form.region = value || []

  if (form.region.length === 3) {
    form.province = form.region[0] || ''
    form.city = form.region[1] || ''
    form.district = form.region[2] || ''
    return
  }

  form.province = ''
  form.city = ''
  form.district = ''
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        if (isEdit.value) {
          await updateAddressAPI({
            id: form.id,
            receiver: form.receiver,
            phone: form.phone,
            province: form.province,
            city: form.city,
            district: form.district,
            detailAddress: form.detailAddress,
            isDefault: form.isDefault,
          })
          ElMessage.success('地址修改成功')
        } else {
          await addAddressAPI({
            receiver: form.receiver,
            phone: form.phone,
            province: form.province,
            city: form.city,
            district: form.district,
            detailAddress: form.detailAddress,
            isDefault: form.isDefault,
          })
          ElMessage.success('地址添加成功')
        }
        dialogVisible.value = false
        fetchAddresses()
      } catch (err) {
        // Error handled by interceptor
      } finally {
        submitLoading.value = false
      }
    }
  })
}

const setDefault = async (id: number) => {
  try {
    await setDefaultAddressAPI(id)
    ElMessage.success('已设为默认地址')
    fetchAddresses()
  } catch (err) {
    // Error handled by interceptor
  }
}

const handleDelete = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定要删除该地址吗？', '提示', { type: 'warning' })
    await deleteAddressAPI(id)
    ElMessage.success('地址已删除')
    fetchAddresses()
  } catch (err) {
    // User cancelled or API error
  }
}
</script>

<style scoped lang="scss">
.user-center-container {
  display: flex;
  gap: 24px;
}

.sidebar {
  width: 220px;
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05);
  height: fit-content;

  .user-menu {
    border-right: none;

    .el-menu-item.is-active {
      background-color: #eef2ff;
      color: #4f46e5;
      border-right: 3px solid #4f46e5;
    }
  }
}

.content-box {
  flex: 1;
  background: #fff;
  border-radius: 12px;
  padding: 32px;
  min-height: 500px;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05);
}

.header-action {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f1f5f9;

  .section-title {
    margin: 0;
    font-size: 20px;
    font-weight: 600;
    color: #1e293b;
  }
}
</style>

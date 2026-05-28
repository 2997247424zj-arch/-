<template>
  <div class="order-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>订单管理</span>
          <div class="header-actions">
            <el-select
              v-model="statusFilter"
              placeholder="订单状态"
              clearable
              style="width: 150px; margin-right: 10px"
              @change="handleSearch"
            >
              <el-option label="全部" :value="undefined" />
              <el-option label="待付款" :value="0" />
              <el-option label="已付款" :value="1" />
              <el-option label="已发货" :value="2" />
              <el-option label="已取消" :value="3" />
              <el-option label="已完成" :value="4" />
            </el-select>
            <el-input
              v-model="searchKeyword"
              placeholder="搜索订单号"
              style="width: 300px"
              clearable
              @clear="handleSearch"
            >
              <template #append>
                <el-button :icon="Search" @click="handleSearch" />
              </template>
            </el-input>
          </div>
        </div>
      </template>

      <el-table :data="orderList" style="width: 100%" v-loading="loading">
        <el-table-column prop="orderNo" label="订单号" width="180" />
        <el-table-column prop="userId" label="用户ID" width="100" />
        <el-table-column prop="totalAmount" label="订单金额" width="120">
          <template #default="{ row }">
            ¥{{ row.totalAmount }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="订单状态" width="120">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="商品信息" min-width="200">
          <template #default="{ row }">
            <div v-if="row.orderItems && row.orderItems.length > 0">
              <div
                v-for="item in row.orderItems"
                :key="item.id"
                style="margin-bottom: 5px"
              >
                {{ item.productName }} x {{ item.quantity }}
              </div>
            </div>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleViewDetail(row)">
              查看详情
            </el-button>
            <el-dropdown @command="(cmd: number) => handleStatusChange(row.orderNo, cmd)">
              <el-button type="success" size="small">
                更新状态<el-icon class="el-icon--right"><arrow-down /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item :command="1">已付款</el-dropdown-item>
                  <el-dropdown-item :command="2">已发货</el-dropdown-item>
                  <el-dropdown-item :command="3">已取消</el-dropdown-item>
                  <el-dropdown-item :command="4">已完成</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
            <el-button type="danger" size="small" @click="handleDelete(row.orderNo)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        style="margin-top: 20px; justify-content: center"
      />
    </el-card>

    <!-- 订单详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="订单详情" width="700px">
      <div v-if="currentOrder">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="订单号">
            {{ currentOrder.orderNo }}
          </el-descriptions-item>
          <el-descriptions-item label="用户ID">
            {{ currentOrder.userId }}
          </el-descriptions-item>
          <el-descriptions-item label="订单金额">
            ¥{{ currentOrder.totalAmount }}
          </el-descriptions-item>
          <el-descriptions-item label="订单状态">
            <el-tag :type="getStatusType(currentOrder.status)">
              {{ getStatusText(currentOrder.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">
            {{ currentOrder.createTime }}
          </el-descriptions-item>
          <el-descriptions-item label="更新时间">
            {{ currentOrder.updateTime }}
          </el-descriptions-item>
        </el-descriptions>

        <el-divider>商品明细</el-divider>

        <el-table :data="currentOrder.orderItems" style="width: 100%">
          <el-table-column prop="productName" label="商品名称" />
          <el-table-column prop="price" label="单价" width="120">
            <template #default="{ row }">
              ¥{{ row.price }}
            </template>
          </el-table-column>
          <el-table-column prop="quantity" label="数量" width="100" />
          <el-table-column label="小计" width="120">
            <template #default="{ row }">
              ¥{{ (row.price * row.quantity).toFixed(2) }}
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, ArrowDown } from '@element-plus/icons-vue'
import {
  getAllOrdersAPI,
  updateOrderStatusAPI,
  deleteOrderAPI,
} from '@/api/modules/admin'

const loading = ref(false)
const orderList = ref<any[]>([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const searchKeyword = ref('')
const statusFilter = ref<number | undefined>(undefined)

const detailDialogVisible = ref(false)
const currentOrder = ref<any>(null)

const getStatusText = (status: number) => {
  const statusMap: Record<number, string> = {
    0: '待付款',
    1: '已付款',
    2: '已发货',
    3: '已取消',
    4: '已完成',
  }
  return statusMap[status] || '未知'
}

const getStatusType = (status: number) => {
  const typeMap: Record<number, string> = {
    0: 'warning',
    1: 'success',
    2: 'primary',
    3: 'danger',
    4: 'info',
  }
  return typeMap[status] || ''
}

const loadOrderList = async () => {
  loading.value = true
  try {
    const res: any = await getAllOrdersAPI({
      page: currentPage.value,
      size: pageSize.value,
      status: statusFilter.value,
      keyword: searchKeyword.value || undefined,
    })

    if (res.code === 200 && res.data) {
      orderList.value = res.data.records || []
      total.value = res.data.total || 0
    }
  } catch (error) {
    console.error('加载订单列表失败:', error)
    ElMessage.error('加载订单列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  currentPage.value = 1
  loadOrderList()
}

const handleSizeChange = () => {
  loadOrderList()
}

const handleCurrentChange = () => {
  loadOrderList()
}

const handleViewDetail = (row: any) => {
  currentOrder.value = row
  detailDialogVisible.value = true
}

const handleStatusChange = async (orderNo: string, status: number) => {
  try {
    const res: any = await updateOrderStatusAPI(orderNo, status)
    if (res.code === 200) {
      ElMessage.success('订单状态更新成功')
      loadOrderList()
    } else {
      ElMessage.error(res.msg || '操作失败')
    }
  } catch (error) {
    console.error('更新订单状态失败:', error)
    ElMessage.error('操作失败')
  }
}

const handleDelete = async (orderNo: string) => {
  try {
    await ElMessageBox.confirm('确定要删除该订单吗？此操作不可恢复！', '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })

    const res: any = await deleteOrderAPI(orderNo)
    if (res.code === 200) {
      ElMessage.success('订单已删除')
      loadOrderList()
    } else {
      ElMessage.error(res.msg || '删除失败')
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('删除订单失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

onMounted(() => {
  loadOrderList()
})
</script>

<style scoped lang="scss">
.order-management {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;

  .header-actions {
    display: flex;
    align-items: center;
  }
}
</style>

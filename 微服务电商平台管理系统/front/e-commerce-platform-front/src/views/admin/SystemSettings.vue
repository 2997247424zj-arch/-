<template>
  <div class="system-settings">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>系统设置</span>
        </div>
      </template>

      <el-tabs v-model="activeTab" type="border-card">
        <el-tab-pane label="基本设置" name="basic">
          <el-form :model="basicSettings" label-width="120px">
            <el-form-item label="网站名称">
              <el-input v-model="basicSettings.siteName" placeholder="请输入网站名称" />
            </el-form-item>
            <el-form-item label="网站描述">
              <el-input
                v-model="basicSettings.siteDescription"
                type="textarea"
                :rows="3"
                placeholder="请输入网站描述"
              />
            </el-form-item>
            <el-form-item label="联系邮箱">
              <el-input v-model="basicSettings.contactEmail" placeholder="请输入联系邮箱" />
            </el-form-item>
            <el-form-item label="客服电话">
              <el-input v-model="basicSettings.servicePhone" placeholder="请输入客服电话" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="saveBasicSettings">保存设置</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="订单设置" name="order">
          <el-form :model="orderSettings" label-width="120px">
            <el-form-item label="自动取消时间">
              <el-input-number
                v-model="orderSettings.autoCancelMinutes"
                :min="1"
                :max="1440"
              />
              <span style="margin-left: 10px">分钟（未付款订单自动取消）</span>
            </el-form-item>
            <el-form-item label="自动确认收货">
              <el-input-number
                v-model="orderSettings.autoConfirmDays"
                :min="1"
                :max="30"
              />
              <span style="margin-left: 10px">天（发货后自动确认收货）</span>
            </el-form-item>
            <el-form-item label="允许退款">
              <el-switch v-model="orderSettings.allowRefund" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="saveOrderSettings">保存设置</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="支付设置" name="payment">
          <el-form :model="paymentSettings" label-width="120px">
            <el-form-item label="启用支付宝">
              <el-switch v-model="paymentSettings.enableAlipay" />
            </el-form-item>
            <el-form-item label="启用微信支付">
              <el-switch v-model="paymentSettings.enableWechat" />
            </el-form-item>
            <el-form-item label="启用货到付款">
              <el-switch v-model="paymentSettings.enableCOD" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="savePaymentSettings">保存设置</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="物流设置" name="logistics">
          <el-form :model="logisticsSettings" label-width="120px">
            <el-form-item label="默认运费">
              <el-input-number
                v-model="logisticsSettings.defaultShippingFee"
                :min="0"
                :precision="2"
              />
              <span style="margin-left: 10px">元</span>
            </el-form-item>
            <el-form-item label="包邮金额">
              <el-input-number
                v-model="logisticsSettings.freeShippingAmount"
                :min="0"
                :precision="2"
              />
              <span style="margin-left: 10px">元（订单满此金额免运费）</span>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="saveLogisticsSettings">保存设置</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="系统信息" name="info">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="系统版本">1.0.0</el-descriptions-item>
            <el-descriptions-item label="数据库版本">MySQL 8.0</el-descriptions-item>
            <el-descriptions-item label="Java版本">17</el-descriptions-item>
            <el-descriptions-item label="Spring Boot版本">3.2.0</el-descriptions-item>
            <el-descriptions-item label="运行环境">开发环境</el-descriptions-item>
            <el-descriptions-item label="启动时间">2026-04-24 13:00:00</el-descriptions-item>
          </el-descriptions>

          <el-divider />

          <el-space>
            <el-button type="primary" @click="clearCache">清除缓存</el-button>
            <el-button type="warning" @click="exportData">导出数据</el-button>
            <el-button type="danger" @click="restartSystem">重启系统</el-button>
          </el-space>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const activeTab = ref('basic')

const basicSettings = reactive({
  siteName: 'MicroMall 电商平台',
  siteDescription: '一个现代化的微服务电商平台',
  contactEmail: 'support@micromall.com',
  servicePhone: '400-123-4567',
})

const orderSettings = reactive({
  autoCancelMinutes: 30,
  autoConfirmDays: 7,
  allowRefund: true,
})

const paymentSettings = reactive({
  enableAlipay: true,
  enableWechat: true,
  enableCOD: false,
})

const logisticsSettings = reactive({
  defaultShippingFee: 10.0,
  freeShippingAmount: 99.0,
})

const saveBasicSettings = () => {
  ElMessage.success('基本设置保存成功')
}

const saveOrderSettings = () => {
  ElMessage.success('订单设置保存成功')
}

const savePaymentSettings = () => {
  ElMessage.success('支付设置保存成功')
}

const saveLogisticsSettings = () => {
  ElMessage.success('物流设置保存成功')
}

const clearCache = () => {
  ElMessageBox.confirm('确定要清除系统缓存吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(() => {
    ElMessage.success('缓存清除成功')
  })
}

const exportData = () => {
  ElMessage.info('数据导出功能开发中...')
}

const restartSystem = () => {
  ElMessageBox.confirm('确定要重启系统吗？这将中断所有用户的访问！', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'error',
  }).then(() => {
    ElMessage.warning('系统重启功能需要管理员权限')
  })
}
</script>

<style scoped lang="scss">
.system-settings {
  padding: 20px;
}

.card-header {
  font-weight: 600;
  font-size: 16px;
}

.el-divider {
  margin: 24px 0;
}
</style>

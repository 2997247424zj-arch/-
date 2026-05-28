<template>
  <AdminLayout>
    <div class="legal-page container">
      <div class="breadcrumb">
        <span>关于我们</span>
        <span class="breadcrumb-separator">/</span>
        <span>服务条款</span>
      </div>

      <div class="card legal-card">
        <h1>服务条款</h1>
        <p>欢迎使用“飞机售票系统”（以下简称“本平台”）。在使用本平台提供的服务前，请您仔细阅读并理解以下条款。使用本平台即表示您同意遵守本条款。</p>

        <h2>一、服务内容</h2>
        <p>本平台向用户提供机票预订、订单管理、改签/退票服务、行李管理、积分与优惠券、客服支持等相关服务（“服务”）。我们将根据本条款及相关法律法规提供或停止部分服务。</p>

        <h2>二、用户义务</h2>
        <p>用户应保证所提供的信息真实、完整，依法使用本平台不得进行欺诈、洗钱、扰乱系统正常运行、侵犯他人合法权益等违法行为。对因提供虚假信息或违反规则导致的损失，平台有权取消服务并保留追责权利。</p>

        <h2>三、改签与退票</h2>
        <p>改签、退票按航空公司票面规则执行。平台会在提示中列明可能的手续费、差价及退款计算方式。最终退款与手续费以航空公司/运营方结算为准。</p>

        <h2>四、责任限制</h2>
        <p>在适用法律允许的最大范围内，本平台对因网络中断、第三方服务失败或不可抗力造成的损失不承担责任。若因平台原因造成损失，我们将根据实际情况予以合理赔偿，但赔偿金额不超过用户直接支付的服务费用。</p>

        <h2>五、条款变更</h2>
        <p>本平台有权在必要时修改本条款，变更将在平台公告或站内通知告知。如您继续使用本平台服务，视为您接受经修订的条款。</p>

        <div class="actions">
          <button class="primary-btn" @click="$router.back()">返回</button>
        </div>
      </div>
    </div>
  </AdminLayout>
</template>

<script setup lang="ts">
import AdminLayout from '../components/AdminLayout.vue'
</script>

<style scoped>
.legal-page.container { padding: 24px; max-width: 1000px; margin: 0 auto; }
.legal-card { padding: 24px; border-radius: 12px; background: rgba(255,255,255,0.98); border: 1px solid rgba(0,0,0,0.06); }
.legal-card h1 { margin-top: 0; }
.legal-card h2 { margin-top: 16px; }
.legal-card p { color: #334155; line-height: 1.6; }
.actions { margin-top: 18px; display:flex; justify-content:flex-end; }
.primary-btn { padding: 8px 16px; border-radius:8px; background: linear-gradient(120deg,#3B82F6,#1E40AF); color:#fff; border:none; cursor:pointer;}

</style>



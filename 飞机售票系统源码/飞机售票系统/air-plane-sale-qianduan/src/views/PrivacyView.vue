<template>
  <AdminLayout>
    <div class="legal-page container">
      <div class="breadcrumb">
        <span>关于我们</span>
        <span class="breadcrumb-separator">/</span>
        <span>隐私政策</span>
      </div>

      <div class="card legal-card">
        <h1>隐私政策</h1>
        <p>本隐私政策说明“飞机售票系统”如何收集、使用、保护和共享您的个人数据。我们重视您的隐私并致力于采取合理的技术与管理措施保护您的信息安全。</p>

        <h2>一、我们收集的信息</h2>
        <p>为提供服务，我们可能收集您的注册信息（姓名、手机号、邮箱）、订单与行程信息、支付与票务信息以及您在使用过程中的行为数据和设备信息。</p>

        <h2>二、信息的使用目的</h2>
        <p>我们将基于合法、必要的原则，使用信息用于订单处理、行程通知、客户服务、分析与优化服务、合规审计和防欺诈。</p>

        <h2>三、信息共享与披露</h2>
        <p>除非取得您同意或法律要求，我们不会向无关第三方出售或出租个人数据。为完成服务，可能将信息与航空公司、支付机构、合作方共享。</p>

        <h2>四、数据安全与保存</h2>
        <p>我们采取加密、访问控制等措施保护数据安全，并按法规与业务需要保存数据。您可通过账户设置管理和删除部分个人信息，某些记录为合规或运营需要可能无法立即删除。</p>

        <h2>五、用户权利</h2>
        <p>您有权访问、更正、删除您的个人信息，以及撤回同意或提出投诉，请通过平台内“在线客服”或联系邮箱与我们沟通。</p>

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



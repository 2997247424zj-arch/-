<template>
  <AdminLayout>
    <div class="members-page container">
      <div class="breadcrumb">
        <span>关于我们</span>
        <span class="breadcrumb-separator">/</span>
        <span>成员简介</span>
      </div>

      <div class="card members-card">
        <div class="members-header">
          <button class="ghost-btn" @click="$router.back()">← 返回</button>
          <h1>课程设计项目成员</h1>
        </div>

        <div class="member-list">
          <div class="member-card" v-for="member in members" :key="member.id">
            <div class="avatar">{{ member.initials }}</div>
            <div class="member-info">
              <h3>{{ member.name }}</h3>
              <p class="role">{{ member.role }}</p>
              <p class="meta">学号/工号：{{ member.id }} • 邮箱：<a :href="`mailto:${member.email}`">{{ member.email }}</a></p>
              <p class="bio">{{ member.bio }}</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  </AdminLayout>
</template>

<script setup lang="ts">
import AdminLayout from '../components/AdminLayout.vue'
const members = [
  {
    id: 'ZJ-001',
    name: '周靖',
    initials: '周',
    role: '管理端产品负责人 / 数据与平台优化',
    email: 'zhoujing@example.com',
    bio: '负责管理端整体产品线与数据体系建设，主导数据统计、后台快捷操作、航班与机型管理模块的功能规划与交付；推进登录/注册流程与页面体验优化，并推动与 COZE 大模型的接入与落地，用于客服与决策支持场景。'
  },
  {
    id: 'PSX-002',
    name: '彭世鑫',
    initials: '世',
    role: '乘客端产品负责人 / 订单与出行体验',
    email: 'pengshixin@example.com',
    bio: '负责乘客端的预订与订单全流程体验，主导预订航班、订单管理、改签/退订、积分与优惠券、重点旅客预约、行李管理及打印机票等功能设计与迭代，提升下单转化率与用户留存。'
  },
  {
    id: 'PZ-003',
    name: '彭柘',
    initials: '柘',
    role: '运营端负责人 / 运营监控与航班分析',
    email: 'pengzhe@example.com',
    bio: '负责改签/退票处理、重点旅客预约与 KPI 指标监控；搭建航班网络分析与异常监控模块，支持运营调度与决策；负责热门航线排行与行李管理功能，并参与网站首页的设计优化以提升运营可视化能力。'
  }
]
</script>

<style scoped>
.members-page.container { padding: 24px; max-width: 1000px; margin: 0 auto; }
.members-card { padding: 20px; border-radius: 12px; background: rgba(255,255,255,0.98); border:1px solid rgba(0,0,0,0.06); }
.members-header { display:flex; align-items:center; gap:16px; margin-bottom:16px; }
.member-list { display:flex; flex-direction:column; gap:12px; }
.member-card { display:flex; gap:12px; align-items:flex-start; padding:12px; border-radius:8px; background:#f8fafc; border:1px solid rgba(0,0,0,0.04); }
.avatar { width:48px; height:48px; border-radius:8px; background: linear-gradient(135deg,#6366f1,#8b5cf6); color:#fff; display:flex; align-items:center; justify-content:center; font-weight:700; font-size:18px; }
.member-info h3 { margin:0; }
.role { margin:4px 0; color:#475569; font-weight:600; }
.meta { margin:4px 0; color:#64748b; font-size:13px; }
.bio { margin-top:6px; color:#334155; }
.ghost-btn { background:transparent; border:1px solid rgba(0,0,0,0.06); padding:6px 10px; border-radius:6px; cursor:pointer; }
</style>



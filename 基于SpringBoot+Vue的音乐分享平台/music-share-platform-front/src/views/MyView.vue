<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'

import { getJson } from '@/services/api'
import { useAuthStore } from '@/stores/auth'
import type { ApiResponse } from '@/types/common'
import type { UserDashboardResponse } from '@/types/user-dashboard'

const authStore = useAuthStore()
const route = useRoute()
const router = useRouter()
const dashboard = ref<UserDashboardResponse | null>(null)
const loading = ref(true)
const errorMessage = ref('')

async function loadDashboard() {
  if (!authStore.profile) {
    await router.push({ path: '/login', query: { redirect: route.fullPath } })
    return
  }

  loading.value = true
  errorMessage.value = ''

  try {
    const response = await getJson<ApiResponse<UserDashboardResponse>>(
      `/api/users/${authStore.profile.id}/dashboard`,
      authStore.token,
    )
    dashboard.value = response.data
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '个人互动数据加载失败。'
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  void loadDashboard()
})
</script>

<template>
  <div class="page-shell stack">
    <p v-if="loading" class="status-text">正在加载个人互动数据...</p>
    <p v-else-if="errorMessage" class="status-text warning">{{ errorMessage }}</p>

    <template v-else-if="dashboard">
      <section class="section-card hero-block motion-rise">
        <div>
          <span class="eyebrow">My Activity</span>
          <h1 class="section-title">{{ dashboard.displayName }} 的互动记录</h1>
          <p class="section-copy">{{ dashboard.bio }}</p>
        </div>
        <div class="tag-row">
          <span class="pill">{{ dashboard.role }}</span>
          <span class="pill">{{ dashboard.favoriteGenre }}</span>
        </div>
      </section>

      <section class="grid-three">
        <article class="section-card metric-card">
          <span>收藏总数</span>
          <strong>{{ dashboard.totalFavorites }}</strong>
        </article>
        <article class="section-card metric-card">
          <span>点赞总数</span>
          <strong>{{ dashboard.totalLikes }}</strong>
        </article>
        <article class="section-card metric-card">
          <span>评论总数</span>
          <strong>{{ dashboard.totalComments }}</strong>
        </article>
      </section>

      <section class="grid-three">
        <article class="section-card section-block">
          <span class="eyebrow">Favorites</span>
          <h2 class="section-title">最近收藏</h2>
          <div class="item-list">
            <div v-for="item in dashboard.recentFavorites" :key="item.id" class="item-card">
              <h3>{{ item.title }}</h3>
              <p>{{ item.subtitle }}</p>
              <span>{{ item.relativeTime }}</span>
            </div>
          </div>
        </article>

        <article class="section-card section-block">
          <span class="eyebrow">Likes</span>
          <h2 class="section-title">最近点赞</h2>
          <div class="item-list">
            <div v-for="item in dashboard.recentLikes" :key="item.id" class="item-card">
              <h3>{{ item.title }}</h3>
              <p>{{ item.subtitle }}</p>
              <span>{{ item.relativeTime }}</span>
            </div>
          </div>
        </article>

        <article class="section-card section-block">
          <span class="eyebrow">Comments</span>
          <h2 class="section-title">最近评论</h2>
          <div class="item-list">
            <div v-for="item in dashboard.recentComments" :key="item.id" class="item-card">
              <h3>{{ item.title }}</h3>
              <p>{{ item.subtitle }}</p>
              <span>{{ item.relativeTime }}</span>
            </div>
          </div>
        </article>
      </section>
    </template>
  </div>
</template>

<style scoped>
.hero-block,
.section-block,
.metric-card {
  padding: 26px;
}

.hero-block {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
  align-items: center;
}

.tag-row {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.metric-card {
  display: grid;
  gap: 10px;
}

.metric-card span,
.item-card p,
.item-card span,
.status-text {
  color: var(--color-muted);
}

.metric-card strong {
  color: var(--color-heading);
  font-family: var(--font-display);
  font-size: clamp(2rem, 4vw, 3rem);
}

.item-list {
  margin-top: 20px;
  display: grid;
  gap: 12px;
}

.item-card {
  padding-top: 12px;
  border-top: 1px solid rgba(19, 34, 56, 0.08);
}

.item-card:first-child {
  padding-top: 0;
  border-top: 0;
}

.item-card h3 {
  color: var(--color-heading);
  font-family: var(--font-display);
}

.item-card p {
  margin-top: 8px;
}

.item-card span {
  display: inline-block;
  margin-top: 8px;
}

.status-text.warning {
  color: #b55439;
}
</style>

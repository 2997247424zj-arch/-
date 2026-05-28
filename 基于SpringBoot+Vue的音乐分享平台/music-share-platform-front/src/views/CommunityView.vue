<script setup lang="ts">
import { onMounted, ref } from 'vue'

import { getJson } from '@/services/api'
import type { ApiResponse } from '@/types/common'
import type { CommunityResponse } from '@/types/community'

const communityData = ref<CommunityResponse | null>(null)
const loading = ref(true)
const errorMessage = ref('')

async function loadCommunitySnapshot() {
  loading.value = true
  errorMessage.value = ''

  try {
    const response = await getJson<ApiResponse<CommunityResponse>>('/api/community')
    communityData.value = response.data
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '社区数据加载失败。'
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  void loadCommunitySnapshot()
})
</script>

<template>
  <div class="page-shell stack">
    <section class="section-card hero-block motion-rise">
      <div>
        <span class="eyebrow">Community Snapshot</span>
        <h1 class="section-title">围绕歌曲、歌单和创作者内容形成可沉淀的讨论。</h1>
        <p class="section-copy">
          查看平台最新互动、评论热度和公开歌单动态，快速进入正在发生的音乐讨论。
        </p>
      </div>
    </section>

    <p v-if="loading" class="status-text">正在加载社区数据...</p>
    <p v-else-if="errorMessage" class="status-text warning">{{ errorMessage }}</p>

    <template v-else-if="communityData">
      <section class="grid-three">
        <article class="section-card metric-card">
          <span>评论总数</span>
          <strong>{{ communityData.totalComments }}</strong>
        </article>
        <article class="section-card metric-card">
          <span>公开歌单</span>
          <strong>{{ communityData.totalPlaylists }}</strong>
        </article>
        <article class="section-card metric-card">
          <span>创作者数量</span>
          <strong>{{ communityData.totalCreators }}</strong>
        </article>
      </section>

      <section class="grid-two">
        <article class="section-card section-block">
          <div class="section-head">
            <div>
              <span class="eyebrow">Latest Activities</span>
              <h2 class="section-title">社区动态</h2>
            </div>
          </div>

          <div class="activity-list">
            <div
              v-for="activity in communityData.latestActivities"
              :key="activity.id"
              class="activity-item"
            >
              <div class="activity-avatar">{{ activity.userName.slice(0, 1) }}</div>
              <div>
                <p>
                  <strong>{{ activity.userName }}</strong>
                  {{ activity.action }}
                  <strong>{{ activity.targetName }}</strong>
                </p>
                <span>{{ activity.relativeTime }}</span>
              </div>
            </div>
          </div>
        </article>

        <article class="section-card section-block">
          <div class="section-head">
            <div>
              <span class="eyebrow">Latest Comments</span>
              <h2 class="section-title">最新评论</h2>
            </div>
          </div>

          <div class="comment-list">
            <div
              v-for="comment in communityData.latestComments"
              :key="comment.id"
              class="comment-item"
            >
              <div class="comment-top">
                <div>
                  <h3>{{ comment.userName }}</h3>
                  <p>{{ comment.targetType === 'PLAYLIST' ? '歌单' : '歌曲' }} · {{ comment.targetName }}</p>
                </div>
                <span class="pill">{{ comment.likeCount }} 赞</span>
              </div>
              <p class="comment-content">{{ comment.content }}</p>
              <span class="comment-time">{{ comment.relativeTime }}</span>
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

.metric-card {
  display: grid;
  gap: 10px;
}

.metric-card span,
.activity-item span,
.comment-item p,
.comment-time,
.status-text {
  color: var(--color-muted);
}

.metric-card strong {
  color: var(--color-heading);
  font-family: var(--font-display);
  font-size: clamp(2rem, 4vw, 3rem);
}

.section-head {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
  align-items: center;
}

.activity-list,
.comment-list {
  margin-top: 20px;
  display: grid;
  gap: 14px;
}

.activity-item,
.comment-item {
  padding-top: 14px;
  border-top: 1px solid rgba(19, 34, 56, 0.08);
}

.activity-item:first-child,
.comment-item:first-child {
  padding-top: 0;
  border-top: 0;
}

.activity-item {
  display: flex;
  gap: 14px;
}

.activity-avatar {
  width: 42px;
  height: 42px;
  border-radius: 50%;
  display: grid;
  place-items: center;
  color: #fffef9;
  background: linear-gradient(135deg, #ff7a59, #3858ff);
  font-family: var(--font-display);
  font-weight: 700;
}

.activity-item p,
.comment-item h3 {
  color: var(--color-heading);
}

.comment-top {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
  align-items: center;
}

.comment-content {
  margin-top: 10px;
  color: var(--color-text);
}

.comment-time {
  display: inline-block;
  margin-top: 10px;
}

.status-text.warning {
  color: #b55439;
}
</style>

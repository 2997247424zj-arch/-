<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'

import { getJson, postJson } from '@/services/api'
import { useAuthStore } from '@/stores/auth'
import { usePlayerStore } from '@/stores/player'
import type { FavoriteResponse, LikeResponse, SongDetailResponse } from '@/types/catalog'
import type { ApiResponse } from '@/types/common'
import type { CommentSummary } from '@/types/community'
import type { PlayerTrack } from '@/types/player'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const playerStore = usePlayerStore()

const song = ref<SongDetailResponse | null>(null)
const comments = ref<CommentSummary[]>([])
const loading = ref(true)
const errorMessage = ref('')
const submittingComment = ref(false)
const actionMessage = ref('')

const commentForm = reactive({
  content: '',
})

const songId = computed(() => String(route.params.id ?? ''))

async function loadSongDetail() {
  loading.value = true
  errorMessage.value = ''

  try {
    const [songResponse, commentResponse] = await Promise.all([
      getJson<ApiResponse<SongDetailResponse>>(`/api/songs/${songId.value}`),
      getJson<ApiResponse<CommentSummary[]>>(`/api/songs/${songId.value}/comments`),
    ])
    song.value = songResponse.data
    comments.value = commentResponse.data
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '歌曲详情加载失败。'
  } finally {
    loading.value = false
  }
}

async function submitComment() {
  if (!authStore.profile) {
    await router.push({ path: '/login', query: { redirect: route.fullPath } })
    return
  }

  submittingComment.value = true
  actionMessage.value = ''

  try {
    const response = await postJson<ApiResponse<CommentSummary>>('/api/comments', {
      userId: authStore.profile.id,
      songId: songId.value,
      content: commentForm.content,
    }, authStore.token)
    comments.value = [response.data, ...comments.value]
    commentForm.content = ''
    if (song.value) {
      song.value = {
        ...song.value,
        commentCount: song.value.commentCount + 1,
      }
    }
    actionMessage.value = '评论已发布。'
  } catch (error) {
    actionMessage.value = error instanceof Error ? error.message : '评论发布失败。'
  } finally {
    submittingComment.value = false
  }
}

async function favoriteSong() {
  if (!authStore.profile) {
    await router.push({ path: '/login', query: { redirect: route.fullPath } })
    return
  }

  try {
    const response = await postJson<ApiResponse<FavoriteResponse>>('/api/favorites', {
      userId: authStore.profile.id,
      targetType: 'SONG',
      targetId: songId.value,
    }, authStore.token)
    if (song.value) {
      song.value = {
        ...song.value,
        favoriteCount: response.data.totalFavorites,
      }
    }
    actionMessage.value = '已加入收藏。'
  } catch (error) {
    actionMessage.value = error instanceof Error ? error.message : '收藏失败。'
  }
}

async function likeSong() {
  if (!authStore.profile) {
    await router.push({ path: '/login', query: { redirect: route.fullPath } })
    return
  }

  try {
    const response = await postJson<ApiResponse<LikeResponse>>('/api/likes', {
      userId: authStore.profile.id,
      targetType: 'SONG',
      targetId: songId.value,
    }, authStore.token)
    if (song.value) {
      song.value = {
        ...song.value,
        likeCount: response.data.totalLikes,
      }
    }
    actionMessage.value = '已点赞。'
  } catch (error) {
    actionMessage.value = error instanceof Error ? error.message : '点赞失败。'
  }
}

function playInFloatingPlayer() {
  if (!song.value) {
    return
  }

  const playerTrack: PlayerTrack = {
    id: song.value.id,
    title: song.value.title,
    artist: song.value.artist,
    genre: song.value.genre,
    moodTag: song.value.moodTag,
    description: song.value.description,
    streamUrl: song.value.streamUrl,
    downloadUrl: song.value.downloadUrl,
    audioSourceType: song.value.audioSourceType,
    highlightColor: song.value.highlightColor,
  }
  playerStore.setTrack(playerTrack)
  actionMessage.value = '已切换到全站播放器。'
}

onMounted(() => {
  void loadSongDetail()
})

watch(
  () => route.params.id,
  () => {
    void loadSongDetail()
  },
)
</script>

<template>
  <div class="page-shell stack">
    <p v-if="loading" class="status-text">正在加载歌曲详情...</p>
    <p v-else-if="errorMessage" class="status-text warning">{{ errorMessage }}</p>

    <template v-else-if="song">
      <section class="section-card detail-hero motion-rise" :style="{ '--song-color': song.highlightColor }">
        <div class="cover-panel">
          <div class="cover-block"></div>
        </div>

        <div class="detail-copy">
          <div class="tag-row">
            <span class="pill">{{ song.genre }}</span>
            <span class="pill">{{ song.moodTag }}</span>
            <span class="pill">{{ song.durationText }}</span>
          </div>

          <h1 class="section-title">{{ song.title }}</h1>
          <p class="artist-line">{{ song.artist }} · 上传者 {{ song.uploaderName }}</p>
          <p class="section-copy">{{ song.description }}</p>

          <div class="metric-row">
            <span>{{ song.playCount }} 播放</span>
            <span>{{ song.likeCount }} 点赞</span>
            <span>{{ song.favoriteCount }} 收藏</span>
            <span>{{ song.commentCount }} 评论</span>
          </div>

          <div class="player-panel">
            <template v-if="song.streamAvailable && song.streamUrl">
              <p class="source-text">
                {{
                  song.audioSourceType === 'REMOTE'
                    ? '当前音频来源：外部 URL 直链'
                    : '当前音频来源：本地文件托管'
                }}
              </p>
              <audio class="audio-player" :src="song.streamUrl" controls preload="none"></audio>
              <a
                v-if="song.downloadUrl"
                class="secondary-button download-link"
                :href="song.downloadUrl"
              >
                免费下载
              </a>
            </template>
            <p v-else class="status-text warning">
              当前还没有找到这首歌的本地音频文件。把对应音频放进 `media/audio` 目录后，这里就能在线播放和下载。
            </p>
          </div>

          <div class="action-row">
            <button type="button" class="secondary-button" @click="playInFloatingPlayer">全站播放</button>
            <button type="button" class="primary-button" @click="likeSong">点赞歌曲</button>
            <button type="button" class="secondary-button" @click="favoriteSong">加入收藏</button>
          </div>

          <p v-if="actionMessage" class="status-text">{{ actionMessage }}</p>
        </div>
      </section>

      <section class="grid-two">
        <article class="section-card section-block">
          <span class="eyebrow">Tags</span>
          <h2 class="section-title">歌曲标签</h2>
          <div class="tag-list">
            <span v-for="tag in song.tags" :key="tag" class="pill">{{ tag }}</span>
          </div>
        </article>

        <article class="section-card section-block">
          <span class="eyebrow">Uploader</span>
          <h2 class="section-title">创作者信息</h2>
          <p class="uploader-name">{{ song.uploaderName }}</p>
          <p class="section-copy">{{ song.uploaderBio }}</p>
        </article>
      </section>

      <section class="section-card section-block">
        <div class="comment-header">
          <div>
            <span class="eyebrow">Comments</span>
            <h2 class="section-title">评论区</h2>
          </div>
        </div>

        <form class="comment-form" @submit.prevent="submitComment">
          <textarea
            v-model="commentForm.content"
            rows="4"
            placeholder="写下你对这首歌的听感、使用场景或版本反馈"
          ></textarea>
          <button type="submit" class="primary-button" :disabled="submittingComment">
            {{ submittingComment ? '发布中...' : '发布评论' }}
          </button>
        </form>

        <div class="comment-list">
          <article v-for="comment in comments" :key="comment.id" class="comment-item">
            <div class="comment-top">
              <div>
                <h3>{{ comment.userName }}</h3>
                <p>{{ comment.relativeTime }}</p>
              </div>
              <span class="pill">{{ comment.likeCount }} 赞</span>
            </div>
            <p class="comment-content">{{ comment.content }}</p>
          </article>
        </div>
      </section>
    </template>
  </div>
</template>

<style scoped>
.detail-hero,
.section-block {
  padding: 28px;
}

.detail-hero {
  display: grid;
  grid-template-columns: 240px minmax(0, 1fr);
  gap: 24px;
}

.cover-block {
  width: 100%;
  aspect-ratio: 1;
  border-radius: 32px;
  background:
    radial-gradient(circle at 28% 24%, rgba(255, 255, 255, 0.4), transparent 22%),
    linear-gradient(135deg, color-mix(in srgb, var(--song-color) 72%, white), var(--song-color));
}

.detail-copy {
  display: grid;
  gap: 14px;
}

.tag-row,
.metric-row,
.action-row,
.tag-list {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.artist-line,
.metric-row,
.status-text {
  color: var(--color-muted);
}

.player-panel {
  display: grid;
  gap: 12px;
}

.audio-player {
  width: 100%;
}

.source-text {
  color: var(--color-muted);
  font-size: 0.92rem;
}

.download-link {
  width: fit-content;
  min-height: 42px;
}

.comment-form {
  margin-top: 20px;
  display: grid;
  gap: 12px;
}

.comment-form textarea {
  width: 100%;
  padding: 14px;
  border: 1px solid var(--color-border);
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.88);
  color: var(--color-text);
  resize: vertical;
  font: inherit;
}

.comment-list {
  margin-top: 24px;
  display: grid;
  gap: 14px;
}

.comment-item {
  padding-top: 14px;
  border-top: 1px solid rgba(19, 34, 56, 0.08);
}

.comment-item:first-child {
  padding-top: 0;
  border-top: 0;
}

.comment-top {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
  align-items: center;
}

.comment-top h3,
.uploader-name {
  color: var(--color-heading);
  font-family: var(--font-display);
}

.comment-top p {
  color: var(--color-muted);
}

.comment-content {
  margin-top: 10px;
  color: var(--color-text);
}

.status-text.warning {
  color: #b55439;
}

@media (max-width: 900px) {
  .detail-hero {
    grid-template-columns: 1fr;
  }
}
</style>

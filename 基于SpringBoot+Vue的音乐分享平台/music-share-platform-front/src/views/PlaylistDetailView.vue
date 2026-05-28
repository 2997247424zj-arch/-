<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { RouterLink, useRoute, useRouter } from 'vue-router'

import { getJson, postJson } from '@/services/api'
import { useAuthStore } from '@/stores/auth'
import { usePlayerStore } from '@/stores/player'
import type { FavoriteResponse, PlaylistDetailResponse } from '@/types/catalog'
import type { ApiResponse } from '@/types/common'
import type { CommentSummary } from '@/types/community'
import type { PlayerTrack } from '@/types/player'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const playerStore = usePlayerStore()

const playlist = ref<PlaylistDetailResponse | null>(null)
const comments = ref<CommentSummary[]>([])
const loading = ref(true)
const errorMessage = ref('')
const actionMessage = ref('')
const submittingComment = ref(false)
const playbackMessage = ref('')

const commentForm = reactive({
  content: '',
})

const playlistId = computed(() => String(route.params.id ?? ''))

function mapPlaylistTrack(song: PlaylistDetailResponse['songs'][number]): PlayerTrack {
  return {
    id: song.id,
    title: song.title,
    artist: song.artist,
    genre: song.genre,
    streamUrl: song.streamUrl,
    downloadUrl: song.downloadUrl,
    audioSourceType: song.audioSourceType,
  }
}

function isPlayableTrack(song: PlaylistDetailResponse['songs'][number]) {
  return song.streamAvailable && Boolean(song.streamUrl)
}

async function loadPlaylistDetail() {
  loading.value = true
  errorMessage.value = ''

  try {
    const [detailResponse, commentResponse] = await Promise.all([
      getJson<ApiResponse<PlaylistDetailResponse>>(`/api/playlists/${playlistId.value}`),
      getJson<ApiResponse<CommentSummary[]>>(`/api/playlists/${playlistId.value}/comments`),
    ])
    playlist.value = detailResponse.data
    comments.value = commentResponse.data
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '歌单详情加载失败。'
  } finally {
    loading.value = false
  }
}

async function favoritePlaylist() {
  if (!authStore.profile) {
    await router.push({ path: '/login', query: { redirect: route.fullPath } })
    return
  }

  try {
    const response = await postJson<ApiResponse<FavoriteResponse>>('/api/favorites', {
      userId: authStore.profile.id,
      targetType: 'PLAYLIST',
      targetId: playlistId.value,
    }, authStore.token)
    if (playlist.value) {
      playlist.value = {
        ...playlist.value,
        followers: response.data.totalFavorites,
      }
    }
    actionMessage.value = '已收藏歌单。'
  } catch (error) {
    actionMessage.value = error instanceof Error ? error.message : '收藏歌单失败。'
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
      playlistId: playlistId.value,
      content: commentForm.content,
    }, authStore.token)
    comments.value = [response.data, ...comments.value]
    commentForm.content = ''
    if (playlist.value) {
      playlist.value = {
        ...playlist.value,
        commentCount: playlist.value.commentCount + 1,
      }
    }
    actionMessage.value = '歌单评论已发布。'
  } catch (error) {
    actionMessage.value = error instanceof Error ? error.message : '评论发布失败。'
  } finally {
    submittingComment.value = false
  }
}

function playSong(song: PlaylistDetailResponse['songs'][number]) {
  if (!isPlayableTrack(song) || !playlist.value) {
    playbackMessage.value = `《${song.title}》暂时没有可用音频。`
    return
  }

  playerStore.setTrack(
    mapPlaylistTrack(song),
    true,
    playlist.value.songs.filter(isPlayableTrack).map(mapPlaylistTrack),
  )
  playbackMessage.value = `正在播放：${song.title}`
}

function playPlaylistFirstTrack() {
  if (!playlist.value) {
    return
  }

  const playableSongs = playlist.value.songs.filter(isPlayableTrack)
  const firstSong = playableSongs[0]
  if (!firstSong) {
    playbackMessage.value = '当前歌单里还没有可播放的歌曲。'
    return
  }

  playerStore.setTrack(mapPlaylistTrack(firstSong), true, playableSongs.map(mapPlaylistTrack))
  playbackMessage.value = `正在播放歌单开头曲目：${firstSong.title}`
}

function playPlaylistRandomTrack() {
  if (!playlist.value) {
    return
  }

  const playableSongs = playlist.value.songs.filter(isPlayableTrack)
  if (playableSongs.length === 0) {
    playbackMessage.value = '当前歌单里还没有可播放的歌曲。'
    return
  }

  const randomSong = playableSongs[Math.floor(Math.random() * playableSongs.length)]
  if (!randomSong) {
    return
  }

  playerStore.setTrack(mapPlaylistTrack(randomSong), true, playableSongs.map(mapPlaylistTrack))
  playbackMessage.value = `正在随机播放歌单曲目：${randomSong.title}`
}

onMounted(() => {
  void loadPlaylistDetail()
})

watch(
  () => route.params.id,
  () => {
    void loadPlaylistDetail()
  },
)
</script>

<template>
  <div class="page-shell stack">
    <p v-if="loading" class="status-text">正在加载歌单详情...</p>
    <p v-else-if="errorMessage" class="status-text warning">{{ errorMessage }}</p>

    <template v-else-if="playlist">
      <section class="section-card detail-hero motion-rise" :style="{ '--playlist-color': playlist.coverColor }">
        <div class="cover-panel">
          <div class="cover-block"></div>
        </div>

        <div class="detail-copy">
          <div class="tag-row">
            <span class="pill">{{ playlist.sceneTag }}</span>
            <span class="pill">{{ playlist.trackCount }} 首歌曲</span>
          </div>
          <h1 class="section-title">{{ playlist.title }}</h1>
          <p class="artist-line">策展人 {{ playlist.curator }}</p>
          <p class="section-copy">{{ playlist.description }}</p>

          <div class="metric-row">
            <span>{{ playlist.followers }} 收藏</span>
            <span>{{ playlist.commentCount }} 评论</span>
          </div>

          <div class="action-row">
            <button type="button" class="secondary-button" @click="playPlaylistFirstTrack">播放歌单</button>
            <button type="button" class="secondary-button" @click="playPlaylistRandomTrack">随机播放</button>
            <button type="button" class="primary-button" @click="favoritePlaylist">收藏歌单</button>
            <RouterLink class="secondary-button" :to="`/me`">查看我的互动</RouterLink>
          </div>

          <p v-if="actionMessage" class="status-text">{{ actionMessage }}</p>
        </div>
      </section>

      <section class="grid-two">
        <article class="section-card section-block">
          <span class="eyebrow">Curator</span>
          <h2 class="section-title">策展人信息</h2>
          <p class="uploader-name">{{ playlist.curator }}</p>
          <p class="section-copy">{{ playlist.curatorBio }}</p>
        </article>

        <article class="section-card section-block">
          <span class="eyebrow">Tracks</span>
          <h2 class="section-title">歌单曲目</h2>
          <p v-if="playbackMessage" class="status-text">{{ playbackMessage }}</p>
          <div class="track-list">
            <div
              v-for="song in playlist.songs"
              :key="song.id"
              class="track-item"
              :class="{ unavailable: !song.streamAvailable || !song.streamUrl }"
              role="button"
              tabindex="0"
              @click="playSong(song)"
              @keydown.enter.prevent="playSong(song)"
              @keydown.space.prevent="playSong(song)"
            >
              <div>
                <h3>{{ song.title }}</h3>
                <p>{{ song.artist }} · {{ song.genre }}</p>
              </div>
              <div class="track-actions">
                <span>{{ song.durationText }}</span>
                <button type="button" class="secondary-button mini-action" @click.stop="playSong(song)">
                  播放
                </button>
                <RouterLink class="secondary-button mini-action" :to="`/songs/${song.id}`" @click.stop>
                  详情
                </RouterLink>
              </div>
            </div>
          </div>
        </article>
      </section>

      <section class="section-card section-block">
        <div class="comment-header">
          <div>
            <span class="eyebrow">Comments</span>
            <h2 class="section-title">歌单评论</h2>
          </div>
        </div>

        <form class="comment-form" @submit.prevent="submitComment">
          <textarea
            v-model="commentForm.content"
            rows="4"
            placeholder="写下你对这个歌单的使用场景、节奏安排或补充建议"
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
    linear-gradient(135deg, color-mix(in srgb, var(--playlist-color) 72%, white), var(--playlist-color));
}

.detail-copy {
  display: grid;
  gap: 14px;
}

.tag-row,
.metric-row,
.action-row {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.artist-line,
.metric-row,
.status-text,
.track-item p,
.track-item span {
  color: var(--color-muted);
}

.track-list,
.comment-list {
  margin-top: 20px;
  display: grid;
  gap: 12px;
}

.track-item {
  padding: 14px 0;
  border-top: 1px solid rgba(19, 34, 56, 0.08);
  display: flex;
  justify-content: space-between;
  gap: 14px;
  align-items: center;
  cursor: pointer;
  outline: none;
  transition:
    border-color 0.2s var(--motion-ease),
    transform 0.2s var(--motion-ease);
}

.track-item:first-child {
  padding-top: 0;
  border-top: 0;
}

.track-item:hover,
.track-item:focus-visible {
  transform: translateX(3px);
}

.track-item.unavailable {
  cursor: not-allowed;
}

.track-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  justify-content: flex-end;
  align-items: center;
}

.mini-action {
  min-height: 36px;
  padding: 0 12px;
}

.track-item h3,
.uploader-name,
.comment-top h3 {
  color: var(--color-heading);
  font-family: var(--font-display);
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

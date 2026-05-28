<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { RouterLink } from 'vue-router'

import { getJson } from '@/services/api'
import { useAuthStore } from '@/stores/auth'
import { usePlayerStore } from '@/stores/player'
import type { PlaylistDetailResponse, PlaylistListResponse, PlaylistSummary } from '@/types/catalog'
import type { ApiResponse } from '@/types/common'
import type { PlayerTrack } from '@/types/player'

const authStore = useAuthStore()
const playerStore = usePlayerStore()
const playlists = ref<PlaylistSummary[]>([])
const loading = ref(true)
const errorMessage = ref('')
const playbackMessage = ref('')

const totalFollowers = computed(() =>
  playlists.value.reduce((total, playlist) => total + playlist.followers, 0),
)

async function loadPlaylists() {
  loading.value = true
  errorMessage.value = ''

  try {
    const response = await getJson<ApiResponse<PlaylistListResponse>>('/api/playlists')
    playlists.value = response.data.playlists
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '歌单列表加载失败。'
  } finally {
    loading.value = false
  }
}

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

async function playRandomPlaylist(playlistId: string, playlistTitle: string) {
  playbackMessage.value = `正在载入歌单《${playlistTitle}》...`

  try {
    const response = await getJson<ApiResponse<PlaylistDetailResponse>>(`/api/playlists/${playlistId}`)
    const playableSongs = response.data.songs.filter(isPlayableTrack)
    if (playableSongs.length === 0) {
      playbackMessage.value = `歌单《${playlistTitle}》里暂时没有可播放的歌曲。`
      return
    }

    const randomSong = playableSongs[Math.floor(Math.random() * playableSongs.length)]
    if (!randomSong) {
      playbackMessage.value = `歌单《${playlistTitle}》里暂时没有可播放的歌曲。`
      return
    }
    playerStore.setTrack(mapPlaylistTrack(randomSong), true, playableSongs.map(mapPlaylistTrack))
    playbackMessage.value = `正在播放歌单《${playlistTitle}》中的随机曲目：${randomSong.title}`
  } catch (error) {
    playbackMessage.value =
      error instanceof Error ? error.message : `歌单《${playlistTitle}》播放失败。`
  }
}

onMounted(() => {
  void loadPlaylists()
})
</script>

<template>
  <div class="page-shell stack">
    <section class="section-card library-hero motion-rise">
      <div>
        <span class="eyebrow">Playlist Library</span>
        <h1 class="section-title">把歌单与个人音乐库做成高频入口。</h1>
        <p class="section-copy">
          当前音乐库已接入 `/api/playlists`。现在可以直接进入歌单详情、查看曲目列表、评论和收藏。
        </p>
      </div>

      <div class="library-sidecard">
        <template v-if="authStore.profile">
          <p class="side-label">当前登录</p>
          <h2>{{ authStore.profile.displayName }}</h2>
          <p>{{ authStore.profile.bio }}</p>
          <div class="side-tags">
            <span class="pill">{{ authStore.profile.role }}</span>
            <span class="pill">{{ authStore.profile.favoriteGenre }}</span>
          </div>
          <RouterLink class="secondary-button" to="/me">查看我的互动</RouterLink>
        </template>
        <template v-else>
          <p class="side-label">建议操作</p>
          <h2>登录或注册账号</h2>
          <p>进入账号后可以查看个人互动、收藏记录，并以创作者身份发布歌曲。</p>
          <RouterLink class="primary-button" to="/login">进入账号</RouterLink>
        </template>
      </div>
    </section>

    <section class="grid-two">
      <article class="section-card metric-card">
        <span>歌单总数</span>
        <strong>{{ playlists.length }}</strong>
      </article>
      <article class="section-card metric-card">
        <span>累计收藏</span>
        <strong>{{ totalFollowers }}</strong>
      </article>
    </section>

    <section class="stack">
      <div class="section-inline">
        <div>
          <span class="eyebrow">Playlist Feed</span>
          <h2 class="section-title">精选歌单</h2>
        </div>
        <p class="section-copy">从这里进入歌单详情、评论和收藏链路。</p>
      </div>

      <p v-if="playbackMessage" class="status-text">{{ playbackMessage }}</p>
      <p v-if="loading" class="status-text">正在加载歌单列表...</p>
      <p v-else-if="errorMessage" class="status-text warning">{{ errorMessage }}</p>

      <div v-else class="grid-two">
        <article
          v-for="playlist in playlists"
          :key="playlist.id"
          class="section-card playlist-card"
          :style="{ '--playlist-color': playlist.coverColor }"
        >
          <div class="playlist-cover"></div>
          <div class="playlist-copy">
            <div class="playlist-head">
              <span class="pill">{{ playlist.sceneTag }}</span>
              <span class="pill">{{ playlist.trackCount }} 首</span>
            </div>
            <h3>{{ playlist.title }}</h3>
            <p class="playlist-curator">策展人：{{ playlist.curator }}</p>
            <p class="playlist-description">{{ playlist.description }}</p>
            <div class="playlist-footer">
              <span>{{ playlist.followers }} 收藏</span>
              <button
                type="button"
                class="secondary-button collect-button"
                @click="playRandomPlaylist(playlist.id, playlist.title)"
              >
                随机播放
              </button>
              <RouterLink class="secondary-button collect-button" :to="`/playlists/${playlist.id}`">
                查看详情
              </RouterLink>
            </div>
          </div>
        </article>
      </div>
    </section>
  </div>
</template>

<style scoped>
.library-hero,
.playlist-card,
.metric-card {
  padding: 26px;
}

.library-hero {
  display: grid;
  grid-template-columns: minmax(0, 1.15fr) minmax(280px, 0.85fr);
  gap: 20px;
}

.library-sidecard {
  padding: 20px;
  border: 1px solid var(--color-border);
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.76);
  display: grid;
  gap: 12px;
  align-content: start;
}

.library-sidecard h2,
.playlist-card h3 {
  color: var(--color-heading);
  font-family: var(--font-display);
}

.library-sidecard p,
.playlist-curator,
.playlist-description,
.playlist-footer,
.status-text {
  color: var(--color-muted);
}

.side-label {
  color: #b55439;
  font-size: 0.85rem;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.side-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.metric-card {
  display: grid;
  gap: 10px;
}

.metric-card span {
  color: var(--color-muted);
}

.metric-card strong {
  color: var(--color-heading);
  font-family: var(--font-display);
  font-size: clamp(2rem, 4vw, 3rem);
}

.section-inline {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
  align-items: center;
}

.playlist-card {
  display: grid;
  grid-template-columns: 110px minmax(0, 1fr);
  gap: 18px;
}

.playlist-cover {
  width: 110px;
  height: 110px;
  border-radius: 28px;
  background:
    radial-gradient(circle at 28% 24%, rgba(255, 255, 255, 0.44), transparent 22%),
    linear-gradient(135deg, color-mix(in srgb, var(--playlist-color) 72%, white), var(--playlist-color));
}

.playlist-copy {
  display: grid;
  gap: 12px;
}

.playlist-head,
.playlist-footer {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
  align-items: center;
}

.collect-button {
  min-height: 42px;
}

.status-text.warning {
  color: #b55439;
}

@media (max-width: 900px) {
  .library-hero {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 640px) {
  .playlist-card {
    grid-template-columns: 1fr;
  }
}
</style>

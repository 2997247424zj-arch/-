<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { RouterLink } from 'vue-router'

import { getJson } from '@/services/api'
import { usePlayerStore } from '@/stores/player'
import type { PlaylistDetailResponse, PlaylistTrackSummary, SongListResponse, SongSummary } from '@/types/catalog'
import type { ApiResponse } from '@/types/common'
import type { HomePageResponse, HomePlayableTrack, PlatformOverview, TrackSummary } from '@/types/home'
import type { PlayerTrack } from '@/types/player'

const fallbackOverview: PlatformOverview = {
  totalTracks: 15,
  totalPlaylists: 4,
  activeCreators: 3,
  dailyShares: 4,
}

const fallbackHomeData: HomePageResponse = {
  overview: fallbackOverview,
  genres: [],
  trendingTracks: [],
  recommendedSleepTracks: [],
  featuredPlaylists: [],
  latestActivities: [],
  deliveryRoadmap: [],
}

interface HomePlayerPreview {
  id: string
  title: string
  artist: string
  genre: string
  moodTag?: string
  durationText?: string
  description?: string
  highlightColor: string
  streamUrl: string | null
  downloadUrl: string | null
  streamAvailable: boolean
  audioSourceType: string
}

const homeData = ref<HomePageResponse>(fallbackHomeData)
const loading = ref(true)
const errorMessage = ref('')
const playbackMessage = ref('')
const currentTrack = ref<HomePlayerPreview | null>(null)
const playerStore = usePlayerStore()

const overviewItems = computed(() => [
  { label: '已收录歌曲', value: homeData.value.overview.totalTracks },
  { label: '精选歌单', value: homeData.value.overview.totalPlaylists },
  { label: '活跃创作者', value: homeData.value.overview.activeCreators },
  { label: '今日分享', value: homeData.value.overview.dailyShares },
])

const recommendedPlayableTracks = computed(() =>
  homeData.value.recommendedSleepTracks.filter(isPlayableTrack),
)

const trendingPlayableTracks = computed(() =>
  homeData.value.trendingTracks.filter(isPlayableTrack),
)

const queueStatus = computed(() => {
  if (playerStore.queue.length === 0) {
    return '队列为空'
  }

  const currentIndex = playerStore.currentIndex >= 0 ? playerStore.currentIndex + 1 : 1
  return `${currentIndex} / ${playerStore.queue.length}`
})

function isPlayableTrack(track: { streamAvailable: boolean; streamUrl: string | null }) {
  return track.streamAvailable && Boolean(track.streamUrl)
}

function pickRandomTrack<T>(tracks: T[]) {
  return tracks[Math.floor(Math.random() * tracks.length)] ?? null
}

function mapPlayerTrack(track: HomePlayableTrack): PlayerTrack {
  return {
    id: track.id,
    title: track.title,
    artist: track.artist,
    genre: track.genre ?? '未分类',
    moodTag: track.moodTag,
    description: track.description,
    streamUrl: track.streamUrl,
    downloadUrl: track.downloadUrl ?? null,
    audioSourceType: track.audioSourceType ?? 'REMOTE',
    highlightColor: track.highlightColor,
  }
}

function mapSongTrack(song: SongSummary): PlayerTrack {
  return {
    id: song.id,
    title: song.title,
    artist: song.artist,
    genre: song.genre,
    moodTag: song.moodTag,
    description: song.description,
    streamUrl: song.streamUrl,
    downloadUrl: song.downloadUrl,
    audioSourceType: song.audioSourceType,
    highlightColor: song.highlightColor,
  }
}

function mapTrendingTrack(track: TrackSummary): PlayerTrack {
  return {
    id: track.id,
    title: track.title,
    artist: track.artist,
    genre: track.genre,
    moodTag: track.moodTag,
    description: track.description,
    streamUrl: track.streamUrl,
    downloadUrl: track.downloadUrl,
    audioSourceType: track.audioSourceType,
    highlightColor: track.highlightColor,
  }
}

function mapPlaylistTrack(song: PlaylistTrackSummary): PlayerTrack {
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

function mapSongPreview(song: SongSummary): HomePlayerPreview {
  return {
    id: song.id,
    title: song.title,
    artist: song.artist,
    genre: song.genre,
    moodTag: song.moodTag,
    durationText: song.durationText,
    description: song.description,
    highlightColor: song.highlightColor,
    streamUrl: song.streamUrl,
    downloadUrl: song.downloadUrl,
    streamAvailable: song.streamAvailable,
    audioSourceType: song.audioSourceType,
  }
}

function mapTrendingPreview(track: TrackSummary): HomePlayerPreview {
  return {
    id: track.id,
    title: track.title,
    artist: track.artist,
    genre: track.genre,
    moodTag: track.moodTag,
    durationText: track.durationText,
    description: track.description,
    highlightColor: track.highlightColor,
    streamUrl: track.streamUrl,
    downloadUrl: track.downloadUrl,
    streamAvailable: track.streamAvailable,
    audioSourceType: track.audioSourceType,
  }
}

function mapPlaylistPreview(song: PlaylistTrackSummary, playlistTitle: string): HomePlayerPreview {
  return {
    id: song.id,
    title: song.title,
    artist: song.artist,
    genre: song.genre,
    durationText: song.durationText,
    description: `来自歌单《${playlistTitle}》`,
    highlightColor: '#5eead4',
    streamUrl: song.streamUrl,
    downloadUrl: song.downloadUrl,
    streamAvailable: song.streamAvailable,
    audioSourceType: song.audioSourceType,
  }
}

function mapPreviewTrack(track: HomePlayerPreview): PlayerTrack {
  return {
    id: track.id,
    title: track.title,
    artist: track.artist,
    genre: track.genre,
    moodTag: track.moodTag,
    description: track.description,
    streamUrl: track.streamUrl,
    downloadUrl: track.downloadUrl,
    audioSourceType: track.audioSourceType,
    highlightColor: track.highlightColor,
  }
}

function mapPlayerPreview(track: PlayerTrack): HomePlayerPreview {
  return {
    id: track.id,
    title: track.title,
    artist: track.artist,
    genre: track.genre ?? '未分类',
    moodTag: track.moodTag,
    description: track.description,
    highlightColor: track.highlightColor ?? '#5eead4',
    streamUrl: track.streamUrl,
    downloadUrl: track.downloadUrl ?? null,
    streamAvailable: Boolean(track.streamUrl),
    audioSourceType: track.audioSourceType ?? 'REMOTE',
  }
}

function syncCurrentTrackFromPlayer() {
  if (playerStore.currentTrack) {
    currentTrack.value = mapPlayerPreview(playerStore.currentTrack)
  }
}

function playTrack(track: HomePlayableTrack) {
  if (!isPlayableTrack(track)) {
    playbackMessage.value = `《${track.title}》暂时没有可用音频。`
    return
  }

  currentTrack.value = track
  playerStore.setTrack(mapPlayerTrack(track), true, recommendedPlayableTracks.value.map(mapPlayerTrack))
  playbackMessage.value = `正在播放：${track.title}`
}

function playTrendingTrack(track: TrackSummary) {
  if (!isPlayableTrack(track)) {
    playbackMessage.value = `《${track.title}》暂时没有可用音频。`
    return
  }

  currentTrack.value = mapTrendingPreview(track)
  playerStore.setTrack(mapTrendingTrack(track), true, trendingPlayableTracks.value.map(mapTrendingTrack))
  playbackMessage.value = `正在播放：${track.title}`
}

function playRecommendedQueue() {
  const firstTrack = recommendedPlayableTracks.value[0] ?? null
  if (!firstTrack) {
    playbackMessage.value = '助眠推荐里暂时没有可播放的歌曲。'
    return
  }

  currentTrack.value = firstTrack
  playerStore.setTrack(mapPlayerTrack(firstTrack), true, recommendedPlayableTracks.value.map(mapPlayerTrack))
  playbackMessage.value = `已开始播放助眠队列：${firstTrack.title}`
}

function playTrendingQueue() {
  const firstTrack = trendingPlayableTracks.value[0] ?? null
  if (!firstTrack) {
    playbackMessage.value = '热门歌曲里暂时没有可播放的歌曲。'
    return
  }

  currentTrack.value = mapTrendingPreview(firstTrack)
  playerStore.setTrack(mapTrendingTrack(firstTrack), true, trendingPlayableTracks.value.map(mapTrendingTrack))
  playbackMessage.value = `已开始播放热门队列：${firstTrack.title}`
}

function playRandomSleepTrack() {
  const randomTrack = pickRandomTrack(recommendedPlayableTracks.value)
  if (!randomTrack) {
    playbackMessage.value = '当前助眠推荐里还没有可播放的歌曲。'
    return
  }

  playTrack(randomTrack)
}

function replayCurrentTrack() {
  if (!currentTrack.value || !isPlayableTrack(currentTrack.value)) {
    playbackMessage.value = '当前曲目暂时不可播放。'
    return
  }

  playerStore.setTrack(mapPreviewTrack(currentTrack.value), true)
  playbackMessage.value = `正在播放：${currentTrack.value.title}`
}

function playPreviousFromHome() {
  playerStore.playPrevious()
  syncCurrentTrackFromPlayer()
}

function playNextFromHome() {
  playerStore.playNext()
  syncCurrentTrackFromPlayer()
}

async function playRandomGenreTrack(genreName: string) {
  playbackMessage.value = `正在为 ${genreName} 准备随机播放...`

  try {
    const response = await getJson<ApiResponse<SongListResponse>>(
      `/api/songs?genre=${encodeURIComponent(genreName)}`,
    )
    const playableSongs = response.data.songs.filter(isPlayableTrack)
    const randomSong = pickRandomTrack(playableSongs)
    if (!randomSong) {
      playbackMessage.value = `${genreName} 分类下暂时没有可播放的歌曲。`
      return
    }

    currentTrack.value = mapSongPreview(randomSong)
    playerStore.setTrack(mapSongTrack(randomSong), true, playableSongs.map(mapSongTrack))
    playbackMessage.value = `正在随机播放 ${genreName}：${randomSong.title}`
  } catch (error) {
    playbackMessage.value =
      error instanceof Error ? error.message : `${genreName} 分类随机播放失败。`
  }
}

async function playFeaturedPlaylist(playlistId: string, playlistTitle: string) {
  playbackMessage.value = `正在载入歌单《${playlistTitle}》...`

  try {
    const response = await getJson<ApiResponse<PlaylistDetailResponse>>(`/api/playlists/${playlistId}`)
    const playableSongs = response.data.songs.filter(isPlayableTrack)
    const randomSong = pickRandomTrack(playableSongs)
    if (!randomSong) {
      playbackMessage.value = `歌单《${playlistTitle}》里暂时没有可播放的歌曲。`
      return
    }

    currentTrack.value = mapPlaylistPreview(randomSong, playlistTitle)
    playerStore.setTrack(mapPlaylistTrack(randomSong), true, playableSongs.map(mapPlaylistTrack))
    playbackMessage.value = `正在播放歌单《${playlistTitle}》中的随机曲目：${randomSong.title}`
  } catch (error) {
    playbackMessage.value =
      error instanceof Error ? error.message : `歌单《${playlistTitle}》播放失败。`
  }
}

onMounted(async () => {
  try {
    const response = await getJson<ApiResponse<HomePageResponse>>('/api/home')
    homeData.value = response.data
    currentTrack.value = response.data.recommendedSleepTracks[0] ?? null
  } catch {
    errorMessage.value = '后端接口暂未连通，当前无法加载首页推荐内容。'
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div class="page-shell stack home-view">
    <section class="hero-card section-card motion-rise">
      <div class="hero-copy">
        <span class="eyebrow">Music Community Base</span>
        <h1 class="hero-title">首页即刻播放，把推荐音乐放到最前面。</h1>
        <p class="hero-description">
          首页整合播放器、助眠推荐、热门歌曲和精选歌单，用户不用进入详情页也能直接开始播放、切歌、随机和下载。
        </p>

        <div class="hero-actions">
          <button type="button" class="primary-button" @click="playRecommendedQueue">播放助眠队列</button>
          <button type="button" class="secondary-button" @click="playTrendingQueue">播放热门队列</button>
          <button type="button" class="secondary-button" @click="playRandomSleepTrack">随机播放</button>
          <RouterLink class="secondary-button" to="/discover">探索全部歌曲</RouterLink>
        </div>

        <p v-if="loading" class="hero-status">正在加载首页数据...</p>
        <p v-else-if="errorMessage" class="hero-status warning">{{ errorMessage }}</p>
        <p v-else class="hero-status">推荐内容和播放面板已就绪，可以直接播放。</p>
      </div>

      <div class="hero-panel">
        <div class="qq-panel" :style="{ '--player-accent': currentTrack?.highlightColor ?? '#5eead4' }">
          <div class="panel-disc" :class="{ active: Boolean(playerStore.currentTrack) }">
            <span class="panel-disc-core"></span>
          </div>
          <div class="panel-copy">
            <span class="eyebrow">Home Player</span>
            <h2>{{ currentTrack?.title ?? '选择一首歌开始播放' }}</h2>
            <p>
              <template v-if="currentTrack">
                {{ currentTrack.artist }} / {{ currentTrack.genre }}
                <template v-if="currentTrack.moodTag"> / {{ currentTrack.moodTag }}</template>
              </template>
              <template v-else>QQ 音乐式聚合面板，集中控制播放队列。</template>
            </p>
          </div>
          <div class="panel-controls" aria-label="首页播放控制">
            <button type="button" class="icon-button previous-icon" aria-label="上一首" @click="playPreviousFromHome"></button>
            <button type="button" class="icon-button play-icon primary-icon" aria-label="播放当前歌曲" @click="replayCurrentTrack"></button>
            <button type="button" class="icon-button next-icon" aria-label="下一首" @click="playNextFromHome"></button>
            <button
              type="button"
              class="icon-button shuffle-icon"
              :class="{ active: playerStore.shuffleEnabled }"
              aria-label="随机播放"
              @click="playerStore.toggleShuffle()"
            ></button>
          </div>
          <div class="panel-actions">
            <button type="button" class="mini-panel-button" @click="playRecommendedQueue">播放推荐</button>
            <button type="button" class="mini-panel-button" @click="playTrendingQueue">播放热门</button>
            <span class="queue-pill">{{ queueStatus }}</span>
          </div>
        </div>

        <div class="hero-stats">
          <article v-for="item in overviewItems" :key="item.label" class="stat-card">
            <span>{{ item.label }}</span>
            <strong>{{ item.value }}</strong>
          </article>
        </div>
      </div>
    </section>

    <section
      v-if="currentTrack"
      class="section-card player-card motion-rise motion-delay-1"
      :style="{ '--player-accent': currentTrack.highlightColor }"
    >
      <div class="player-copy">
        <span class="eyebrow">Now Previewing</span>
        <h2 class="section-title">{{ currentTrack.title }}</h2>
        <p class="section-copy">
          {{ currentTrack.artist }} / {{ currentTrack.genre }}
          <template v-if="currentTrack.moodTag"> / {{ currentTrack.moodTag }}</template>
          <template v-if="currentTrack.durationText"> / {{ currentTrack.durationText }}</template>
        </p>
        <p class="section-copy">{{ currentTrack.description }}</p>
        <p class="source-text">
          {{ currentTrack.audioSourceType === 'REMOTE' ? '当前来源：外部音频直链' : '当前来源：本地文件托管' }}
        </p>
      </div>

      <div class="player-controls">
        <audio
          v-if="currentTrack.streamAvailable && currentTrack.streamUrl"
          class="audio-player"
          :src="currentTrack.streamUrl"
          controls
          preload="none"
        ></audio>
        <p v-else class="hero-status warning">当前歌曲暂不可播放。</p>

        <div class="action-row">
          <button type="button" class="primary-button mini-button" @click="replayCurrentTrack">加入全站播放器</button>
          <button type="button" class="secondary-button mini-button" @click="playPreviousFromHome">上一首</button>
          <button type="button" class="secondary-button mini-button" @click="playNextFromHome">下一首</button>
          <RouterLink class="secondary-button mini-button" :to="`/songs/${currentTrack.id}`">查看详情</RouterLink>
          <a v-if="currentTrack.downloadUrl" class="secondary-button mini-button" :href="currentTrack.downloadUrl">下载</a>
        </div>
      </div>
    </section>

    <section class="stack motion-rise motion-delay-2">
      <div class="section-head">
        <div>
          <span class="eyebrow">Sleep Picks</span>
          <h2 class="section-title">助眠推荐歌曲</h2>
        </div>
        <div class="section-actions">
          <button type="button" class="secondary-button mini-button" @click="playRecommendedQueue">播放全部</button>
          <button type="button" class="secondary-button mini-button" @click="playRandomSleepTrack">随机一首</button>
        </div>
      </div>

      <p v-if="playbackMessage" class="status-text">{{ playbackMessage }}</p>
      <div class="grid-three">
        <template v-if="loading">
          <div v-for="n in 3" :key="n" class="skeleton-sleep-card"></div>
        </template>
        <template v-else>
          <article
            v-for="track in homeData.recommendedSleepTracks"
            :key="track.id"
            class="section-card sleep-card"
            :class="{ unavailable: !track.streamAvailable || !track.streamUrl }"
            :style="{ '--track-color': track.highlightColor }"
            role="button"
            tabindex="0"
            @click="playTrack(track)"
            @keydown.enter.prevent="playTrack(track)"
            @keydown.space.prevent="playTrack(track)"
          >
            <div class="sleep-cover">
              <button type="button" class="cover-play play-icon" aria-label="播放歌曲" @click.stop="playTrack(track)"></button>
              <span class="play-badge">{{ track.streamAvailable ? '可播放' : '无音频' }}</span>
            </div>
            <div class="sleep-copy">
              <div class="tag-row">
                <span class="pill">{{ track.genre }}</span>
                <span class="pill">{{ track.moodTag }}</span>
              </div>
              <h3>{{ track.title }}</h3>
              <p>{{ track.artist }} / {{ track.durationText }}</p>
              <p>{{ track.description }}</p>
              <div class="sleep-actions">
                <button type="button" class="primary-button mini-button" @click.stop="playTrack(track)">立即播放</button>
                <RouterLink class="secondary-button mini-button" :to="`/songs/${track.id}`" @click.stop>查看详情</RouterLink>
              </div>
            </div>
          </article>
        </template>
      </div>
    </section>

    <section class="stack motion-rise motion-delay-3">
      <div class="section-head">
        <div>
          <span class="eyebrow">Genres</span>
          <h2 class="section-title">本周推荐曲风</h2>
        </div>
      </div>
      <div class="grid-three">
        <article
          v-for="genre in homeData.genres"
          :key="genre.id"
          class="section-card genre-card"
          :style="{ '--accent-color': genre.accentColor }"
        >
          <div class="genre-head">
            <span class="genre-dot"></span>
            <span class="pill">{{ genre.trackCount }} 首歌曲</span>
          </div>
          <h3>{{ genre.name }}</h3>
          <p>{{ genre.description }}</p>
          <div class="genre-actions">
            <button type="button" class="secondary-button mini-button" @click="playRandomGenreTrack(genre.name)">随机播放</button>
          </div>
        </article>
      </div>
    </section>

    <section class="grid-two motion-rise motion-delay-3">
      <article class="section-card content-card">
        <div class="content-header">
          <div>
            <span class="eyebrow">Trending Tracks</span>
            <h2 class="section-title">热门歌曲</h2>
          </div>
          <div class="section-actions">
            <button type="button" class="secondary-button mini-button" @click="playTrendingQueue">播放全部</button>
            <RouterLink class="pill" to="/discover">进入发现页</RouterLink>
          </div>
        </div>

        <div class="track-list">
          <div
            v-for="track in homeData.trendingTracks"
            :key="track.id"
            class="track-item"
            :class="{ unavailable: !track.streamAvailable || !track.streamUrl }"
            role="button"
            tabindex="0"
            @click="playTrendingTrack(track)"
            @keydown.enter.prevent="playTrendingTrack(track)"
            @keydown.space.prevent="playTrendingTrack(track)"
          >
            <button type="button" class="row-play play-icon" aria-label="播放热门歌曲" @click.stop="playTrendingTrack(track)"></button>
            <div class="track-rank">{{ track.likes }}</div>
            <div class="track-copy">
              <h3>{{ track.title }}</h3>
              <p>{{ track.artist }} / {{ track.genre }} / {{ track.moodTag }}</p>
            </div>
            <RouterLink class="secondary-button mini-button" :to="`/songs/${track.id}`" @click.stop>详情</RouterLink>
          </div>
        </div>
      </article>

      <article class="section-card content-card">
        <div class="content-header">
          <div>
            <span class="eyebrow">Playlists</span>
            <h2 class="section-title">精选歌单</h2>
          </div>
          <RouterLink class="pill" to="/library">进入音乐库</RouterLink>
        </div>

        <div class="playlist-list">
          <div v-for="playlist in homeData.featuredPlaylists" :key="playlist.id" class="playlist-item">
            <button
              type="button"
              class="row-play play-icon"
              aria-label="播放歌单"
              @click="playFeaturedPlaylist(playlist.id, playlist.title)"
            ></button>
            <div class="playlist-copy">
              <h3>{{ playlist.title }}</h3>
              <p>{{ playlist.description }}</p>
            </div>
            <div class="playlist-meta">
              <span>{{ playlist.curator }}</span>
              <strong>{{ playlist.followers }} 收藏</strong>
            </div>
            <div class="playlist-actions">
              <button type="button" class="secondary-button mini-button" @click="playFeaturedPlaylist(playlist.id, playlist.title)">
                随机播放
              </button>
              <RouterLink class="secondary-button mini-button" :to="`/playlists/${playlist.id}`">查看详情</RouterLink>
            </div>
          </div>
        </div>
      </article>
    </section>
  </div>
</template>

<style scoped>
.home-view {
  gap: 22px;
}

.hero-card {
  padding: 34px;
  display: grid;
  grid-template-columns: minmax(0, 1.05fr) minmax(340px, 0.95fr);
  gap: 26px;
  overflow: hidden;
  position: relative;
}

.hero-copy,
.hero-panel {
  position: relative;
  z-index: 1;
}

.hero-title {
  margin-top: 18px;
  color: var(--color-heading);
  font-family: var(--font-display);
  font-size: clamp(2.4rem, 4.6vw, 4.5rem);
  line-height: 1;
}

.hero-description {
  max-width: 760px;
  margin-top: 18px;
  color: var(--color-muted);
  font-size: 1.05rem;
}

.hero-actions,
.action-row,
.tag-row,
.sleep-actions,
.genre-actions,
.playlist-actions,
.section-actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  align-items: center;
}

.hero-actions {
  margin-top: 28px;
}

.hero-status,
.source-text,
.status-text {
  margin-top: 18px;
  color: var(--color-muted);
  font-size: 0.95rem;
}

.hero-status.warning {
  color: #c96464;
}

.hero-panel {
  display: grid;
  gap: 18px;
}

.qq-panel {
  padding: 22px;
  border: 1px solid var(--color-border);
  border-radius: 26px;
  background:
    linear-gradient(135deg, color-mix(in srgb, var(--player-accent) 18%, transparent), transparent 44%),
    color-mix(in srgb, var(--color-panel) 92%, transparent);
  box-shadow: var(--shadow-card);
  display: grid;
  gap: 18px;
}

.panel-disc {
  width: min(54vw, 220px);
  aspect-ratio: 1;
  margin: 0 auto;
  border-radius: 50%;
  display: grid;
  place-items: center;
  background:
    radial-gradient(circle at center, var(--color-surface) 0 13%, transparent 14%),
    conic-gradient(from 120deg, #111827, var(--player-accent), #818cf8, #111827);
  box-shadow: 0 22px 44px rgba(15, 17, 36, 0.24);
}

.panel-disc.active {
  animation: disc-spin 8s linear infinite;
}

.panel-disc-core {
  width: 28%;
  aspect-ratio: 1;
  border-radius: 50%;
  background: var(--color-surface-strong);
  border: 8px solid color-mix(in srgb, var(--player-accent) 40%, white);
}

@keyframes disc-spin {
  to {
    transform: rotate(360deg);
  }
}

.panel-copy {
  text-align: center;
}

.panel-copy h2 {
  margin-top: 8px;
  color: var(--color-heading);
  font-family: var(--font-display);
  font-size: 1.45rem;
}

.panel-copy p {
  margin-top: 8px;
  color: var(--color-muted);
}

.panel-controls,
.panel-actions {
  display: flex;
  justify-content: center;
  gap: 12px;
  flex-wrap: wrap;
}

.icon-button,
.cover-play,
.row-play {
  position: relative;
  width: 48px;
  height: 48px;
  border: 1px solid var(--color-border);
  border-radius: 50%;
  background: color-mix(in srgb, var(--color-panel) 88%, transparent);
  cursor: pointer;
  transition:
    transform 0.2s var(--motion-ease),
    border-color 0.2s var(--motion-ease),
    background 0.2s var(--motion-ease);
}

.icon-button:hover,
.icon-button:focus-visible,
.cover-play:hover,
.cover-play:focus-visible,
.row-play:hover,
.row-play:focus-visible {
  transform: translateY(-1px);
  border-color: color-mix(in srgb, var(--player-accent, #5eead4) 50%, var(--color-border));
}

.primary-icon {
  width: 58px;
  height: 58px;
  background: var(--color-primary);
}

.play-icon::before {
  content: '';
  position: absolute;
  left: 50%;
  top: 50%;
  width: 0;
  height: 0;
  border-top: 9px solid transparent;
  border-bottom: 9px solid transparent;
  border-left: 14px solid var(--color-heading);
  transform: translate(-38%, -50%);
}

.primary-icon.play-icon::before {
  border-left-color: var(--color-on-primary);
}

.previous-icon::before,
.next-icon::before,
.shuffle-icon::before {
  content: '';
  position: absolute;
  inset: 15px;
  border-top: 3px solid var(--color-heading);
  border-left: 3px solid var(--color-heading);
}

.previous-icon::before {
  transform: translateX(4px) rotate(-45deg);
}

.next-icon::before {
  transform: translateX(-4px) rotate(135deg);
}

.shuffle-icon::before {
  inset: 17px 14px;
  border-left: 0;
  transform: skewX(-20deg);
}

.shuffle-icon.active,
.mini-panel-button:hover {
  background: color-mix(in srgb, var(--player-accent, #5eead4) 18%, var(--color-panel));
}

.mini-panel-button,
.queue-pill {
  min-height: 38px;
  padding: 0 14px;
  border: 1px solid var(--color-border);
  border-radius: 999px;
  background: color-mix(in srgb, var(--color-panel) 88%, transparent);
  color: var(--color-heading);
}

.mini-panel-button {
  cursor: pointer;
}

.queue-pill {
  display: inline-flex;
  align-items: center;
  color: var(--color-muted);
}

.hero-stats {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}

.stat-card,
.player-card,
.genre-card,
.content-card,
.sleep-card {
  padding: 24px;
}

.stat-card {
  border-radius: var(--radius-md);
  background: color-mix(in srgb, var(--color-panel) 90%, transparent);
}

.stat-card span {
  display: block;
  color: var(--color-muted);
  font-size: 0.92rem;
}

.stat-card strong {
  display: block;
  margin-top: 8px;
  color: var(--color-heading);
  font-size: 1.85rem;
  font-family: var(--font-display);
}

.player-card {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(320px, 1fr);
  gap: 20px;
  background:
    linear-gradient(135deg, color-mix(in srgb, var(--player-accent) 14%, transparent), transparent),
    color-mix(in srgb, var(--color-surface) 92%, transparent);
}

.audio-player {
  width: 100%;
}

.section-head,
.genre-head,
.content-header {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: center;
  flex-wrap: wrap;
}

.sleep-card {
  display: grid;
  gap: 16px;
  cursor: pointer;
  outline: none;
  transition:
    border-color 0.22s var(--motion-ease),
    box-shadow 0.22s var(--motion-ease),
    transform 0.22s var(--motion-ease);
}

.sleep-card:hover,
.sleep-card:focus-visible {
  border-color: color-mix(in srgb, var(--track-color) 38%, var(--color-border));
  box-shadow: var(--shadow-card);
  transform: translateY(-2px);
}

.sleep-card.unavailable,
.track-item.unavailable {
  cursor: not-allowed;
  opacity: 0.72;
}

.sleep-cover {
  position: relative;
  overflow: hidden;
  height: 140px;
  border-radius: 22px;
  background:
    radial-gradient(circle at 28% 24%, rgba(255, 255, 255, 0.42), transparent 22%),
    linear-gradient(135deg, color-mix(in srgb, var(--track-color) 74%, white), var(--track-color));
}

.cover-play {
  position: absolute;
  left: 14px;
  bottom: 14px;
  background: rgba(15, 17, 36, 0.72);
}

.cover-play.play-icon::before {
  border-left-color: #fff;
}

.play-badge {
  position: absolute;
  right: 12px;
  bottom: 18px;
  min-height: 34px;
  padding: 0 12px;
  border-radius: 999px;
  display: inline-flex;
  align-items: center;
  background: rgba(15, 17, 36, 0.72);
  color: #fff;
  font-size: 0.84rem;
  font-weight: 700;
}

.mini-button {
  min-height: 42px;
}

.genre-card h3,
.track-copy h3,
.playlist-item h3,
.sleep-copy h3 {
  color: var(--color-heading);
  font-family: var(--font-display);
  font-size: 1.28rem;
}

.genre-card p,
.track-copy p,
.playlist-item p,
.sleep-copy p {
  color: var(--color-muted);
}

.genre-card h3 {
  margin-top: 18px;
}

.genre-card p {
  margin-top: 10px;
}

.genre-actions {
  margin-top: 18px;
}

.genre-dot {
  width: 16px;
  height: 16px;
  border-radius: 50%;
  background: var(--accent-color);
  box-shadow: 0 0 0 8px color-mix(in srgb, var(--accent-color) 16%, white);
}

.track-list,
.playlist-list {
  margin-top: 22px;
  display: grid;
  gap: 14px;
}

.track-item,
.playlist-item {
  display: flex;
  gap: 14px;
  align-items: center;
  padding: 16px 0;
  border-top: 1px solid var(--color-border);
}

.track-item {
  cursor: pointer;
  outline: none;
  transition: transform 0.2s var(--motion-ease);
}

.track-item:hover,
.track-item:focus-visible {
  transform: translateX(3px);
}

.track-item:first-child,
.playlist-item:first-child {
  padding-top: 0;
  border-top: 0;
}

.row-play {
  flex: 0 0 44px;
  width: 44px;
  height: 44px;
}

.track-rank {
  min-width: 64px;
  padding: 10px 12px;
  border-radius: 16px;
  background: rgba(94, 234, 212, 0.14);
  color: var(--color-heading);
  text-align: center;
  font-weight: 700;
}

.track-copy,
.playlist-copy {
  min-width: 0;
  display: grid;
  gap: 6px;
  flex: 1 1 220px;
}

.playlist-item {
  justify-content: space-between;
  flex-wrap: wrap;
}

.playlist-meta {
  min-width: 118px;
  text-align: right;
}

.playlist-meta span,
.playlist-meta strong {
  display: block;
}

.playlist-meta span {
  font-size: 0.92rem;
  color: var(--color-muted);
}

.playlist-meta strong {
  margin-top: 8px;
  color: var(--color-heading);
}

.skeleton-sleep-card {
  height: 220px;
  border-radius: var(--radius-lg);
  background: linear-gradient(
    90deg,
    var(--color-border) 25%,
    color-mix(in srgb, var(--color-border) 60%, var(--color-surface)) 50%,
    var(--color-border) 75%
  );
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
}

@keyframes shimmer {
  0% {
    background-position: 200% 0;
  }
  100% {
    background-position: -200% 0;
  }
}

@media (prefers-reduced-motion: reduce) {
  .panel-disc.active,
  .skeleton-sleep-card {
    animation: none;
  }
}

@media (max-width: 960px) {
  .hero-card,
  .player-card {
    grid-template-columns: 1fr;
    padding: 26px;
  }
}

@media (max-width: 720px) {
  .hero-stats {
    grid-template-columns: 1fr;
  }

  .playlist-item {
    align-items: flex-start;
    flex-direction: column;
  }

  .playlist-actions,
  .playlist-meta {
    text-align: left;
  }

  .row-play {
    order: -1;
  }
}
</style>

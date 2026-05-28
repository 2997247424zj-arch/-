<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { RouterLink } from 'vue-router'

import { getJson } from '@/services/api'
import { usePlayerStore } from '@/stores/player'
import type { SongListResponse, SongSummary } from '@/types/catalog'
import type { ApiResponse } from '@/types/common'
import type { PlayerTrack } from '@/types/player'

const songs = ref<SongSummary[]>([])
const genres = ref<string[]>([])
const keyword = ref('')
const selectedGenre = ref('All')
const loading = ref(true)
const errorMessage = ref('')
const actionMessage = ref('')
const debounceTimer = ref<ReturnType<typeof setTimeout> | null>(null)
const playerStore = usePlayerStore()

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

function isPlayableTrack(song: SongSummary) {
  return song.streamAvailable && Boolean(song.streamUrl)
}

async function loadSongs() {
  loading.value = true
  errorMessage.value = ''

  try {
    const searchParams = new URLSearchParams()
    if (keyword.value.trim()) {
      searchParams.set('keyword', keyword.value.trim())
    }
    if (selectedGenre.value !== 'All') {
      searchParams.set('genre', selectedGenre.value)
    }

    const queryString = searchParams.toString()
    const response = await getJson<ApiResponse<SongListResponse>>(
      `/api/songs${queryString ? `?${queryString}` : ''}`,
    )
    songs.value = response.data.songs
    genres.value = response.data.availableGenres
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '歌曲列表加载失败。'
  } finally {
    loading.value = false
  }
}

function handleKeywordInput() {
  if (debounceTimer.value !== null) {
    clearTimeout(debounceTimer.value)
  }
  debounceTimer.value = setTimeout(() => {
    void loadSongs()
  }, 300)
}

function applyGenreFilter(genre: string) {
  selectedGenre.value = genre
  void loadSongs()
}

function playSong(song: SongSummary) {
  if (!isPlayableTrack(song)) {
    actionMessage.value = `《${song.title}》暂时没有可用音频。`
    return
  }

  playerStore.setTrack(
    mapSongTrack(song),
    true,
    songs.value.filter(isPlayableTrack).map(mapSongTrack),
  )
  actionMessage.value = `正在播放：${song.title}`
}

function playRandomSong() {
  const playableSongs = songs.value.filter(isPlayableTrack)
  if (playableSongs.length === 0) {
    actionMessage.value = '当前结果里还没有可播放的歌曲。'
    return
  }

  const randomSong = playableSongs[Math.floor(Math.random() * playableSongs.length)]
  if (!randomSong) {
    actionMessage.value = '当前结果里还没有可播放的歌曲。'
    return
  }
  playSong(randomSong)
}

onMounted(() => {
  void loadSongs()
})
</script>

<template>
  <div class="page-shell stack">
    <section class="section-card discover-hero motion-rise">
      <div>
        <span class="eyebrow">Song Discovery</span>
        <h1 class="section-title">从标签、曲风和情绪开始找歌。</h1>
        <p class="section-copy">
          发现页负责把搜索、筛选、详情入口和全站播放器串起来。你可以先筛歌，再把感兴趣的歌曲加入全站播放队列。
        </p>
      </div>

      <form class="search-panel" @submit.prevent="loadSongs">
        <label class="search-field">
          <span>关键词</span>
          <input
            v-model="keyword"
            type="text"
            placeholder="搜索歌曲、歌手、曲风、情绪"
            @input="handleKeywordInput"
          />
        </label>
        <button type="submit" class="primary-button">搜索歌曲</button>
      </form>
    </section>

    <section class="section-card filter-panel motion-rise motion-delay-1">
      <div class="filter-header">
        <div>
          <span class="eyebrow">Genres</span>
          <h2 class="section-title">曲风筛选</h2>
        </div>
        <p class="section-copy">筛选更轻一点，把主要注意力留给歌曲内容和播放链路。</p>
      </div>

      <div class="genre-wrap">
        <button
          v-for="genre in genres"
          :key="genre"
          type="button"
          class="genre-button"
          :class="{ active: selectedGenre === genre }"
          @click="applyGenreFilter(genre)"
        >
          {{ genre }}
        </button>
      </div>
    </section>

    <section class="stack motion-rise motion-delay-2">
      <div class="section-inline">
        <div>
          <span class="eyebrow">Results</span>
          <h2 class="section-title">歌曲列表</h2>
        </div>
        <div class="results-actions">
          <p class="section-copy">点击任意歌曲卡片即可播放，共 {{ songs.length }} 首匹配歌曲</p>
          <button type="button" class="secondary-button detail-link" @click="playRandomSong">
            随机播放当前结果
          </button>
        </div>
      </div>

      <p v-if="actionMessage" class="status-text">{{ actionMessage }}</p>

      <div v-if="loading" class="grid-two">
        <div v-for="n in 4" :key="n" class="skeleton-card"></div>
      </div>
      <p v-else-if="errorMessage" class="status-text warning">{{ errorMessage }}</p>

      <div v-else class="grid-two">
        <article
          v-for="song in songs"
          :key="song.id"
          class="section-card song-card"
          :class="{ unavailable: !song.streamAvailable || !song.streamUrl }"
          :style="{ '--song-color': song.highlightColor }"
          role="button"
          tabindex="0"
          @click="playSong(song)"
          @keydown.enter.prevent="playSong(song)"
          @keydown.space.prevent="playSong(song)"
        >
          <div class="song-cover">
            <span class="play-badge">{{ song.streamAvailable ? '播放' : '无音频' }}</span>
          </div>
          <div class="song-copy">
            <div class="song-meta-top">
              <span class="pill">{{ song.genre }}</span>
              <span class="pill">{{ song.moodTag }}</span>
            </div>
            <h3>{{ song.title }}</h3>
            <p class="song-artist">{{ song.artist }} · {{ song.durationText }}</p>
            <p class="song-description">{{ song.description }}</p>
            <div class="song-stats">
              <span>{{ song.playCount }} 播放</span>
              <span>{{ song.likeCount }} 喜欢</span>
            </div>
            <div class="song-actions">
              <button type="button" class="primary-button detail-link" @click.stop="playSong(song)">
                立即播放
              </button>
              <RouterLink class="secondary-button detail-link" :to="`/songs/${song.id}`" @click.stop>
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
.discover-hero,
.filter-panel,
.song-card {
  padding: 26px;
}

.discover-hero {
  display: grid;
  grid-template-columns: minmax(0, 1.2fr) minmax(320px, 0.8fr);
  gap: 24px;
}

.search-panel {
  display: grid;
  gap: 14px;
  align-content: start;
}

.search-field {
  display: grid;
  gap: 8px;
  color: var(--color-heading);
  font-weight: 600;
}

.search-field input {
  min-height: 50px;
  padding: 0 14px;
  border: 1px solid var(--color-border);
  border-radius: 16px;
  background: color-mix(in srgb, var(--color-panel) 90%, transparent);
  color: var(--color-text);
  transition: border-color 0.2s ease;
}

.search-field input:focus {
  outline: none;
  border-color: var(--color-primary);
}

.filter-panel,
.filter-header,
.section-inline {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: center;
  flex-wrap: wrap;
}

.results-actions {
  display: flex;
  gap: 12px;
  align-items: center;
  flex-wrap: wrap;
  justify-content: flex-end;
}

.genre-wrap {
  margin-top: 18px;
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.genre-button {
  min-height: 42px;
  padding: 0 16px;
  border: 1px solid var(--color-border);
  border-radius: 999px;
  background: color-mix(in srgb, var(--color-panel) 86%, transparent);
  color: var(--color-muted);
  cursor: pointer;
  transition:
    background-color 0.2s ease,
    color 0.2s ease,
    transform 0.2s ease;
}

.genre-button:hover,
.genre-button.active {
  background: var(--color-surface-strong);
  color: var(--color-heading);
  transform: translateY(-1px);
}

.song-card {
  display: grid;
  grid-template-columns: 110px minmax(0, 1fr);
  gap: 18px;
  cursor: pointer;
  outline: none;
  transition:
    border-color 0.22s var(--motion-ease),
    box-shadow 0.22s var(--motion-ease),
    transform 0.22s var(--motion-ease);
}

.song-card:hover,
.song-card:focus-visible {
  border-color: color-mix(in srgb, var(--song-color) 38%, var(--color-border));
  box-shadow: var(--shadow-card);
  transform: translateY(-2px);
}

.song-card.unavailable {
  cursor: not-allowed;
}

.song-cover {
  position: relative;
  overflow: hidden;
  width: 110px;
  height: 110px;
  border-radius: 26px;
  background:
    linear-gradient(135deg, color-mix(in srgb, var(--song-color) 72%, white), var(--song-color)),
    var(--song-color);
  box-shadow: inset 0 0 0 1px rgba(255, 255, 255, 0.18);
}

.play-badge {
  position: absolute;
  right: 10px;
  bottom: 10px;
  min-height: 32px;
  padding: 0 10px;
  border-radius: 999px;
  display: inline-flex;
  align-items: center;
  background: rgba(15, 17, 36, 0.72);
  color: #fff;
  font-size: 0.82rem;
  font-weight: 700;
}

.song-copy {
  display: grid;
  gap: 10px;
}

.song-meta-top,
.song-stats,
.song-actions {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.song-card h3 {
  color: var(--color-heading);
  font-family: var(--font-display);
  font-size: 1.4rem;
}

.song-artist,
.song-description,
.song-stats,
.status-text {
  color: var(--color-muted);
}

.detail-link {
  width: fit-content;
  min-height: 42px;
}

.status-text.warning {
  color: #c96464;
}

/* 骨架屏 */
.skeleton-card {
  height: 148px;
  border-radius: 20px;
  background: linear-gradient(
    90deg,
    color-mix(in srgb, var(--color-border) 60%, transparent) 25%,
    color-mix(in srgb, var(--color-border) 30%, transparent) 50%,
    color-mix(in srgb, var(--color-border) 60%, transparent) 75%
  );
  background-size: 200% 100%;
  animation: skeleton-shimmer 1.5s ease-in-out infinite;
}

@keyframes skeleton-shimmer {
  0% {
    background-position: 200% 0;
  }
  100% {
    background-position: -200% 0;
  }
}

@media (max-width: 900px) {
  .discover-hero {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 640px) {
  .song-card {
    grid-template-columns: 1fr;
  }
}
</style>

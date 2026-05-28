<script setup lang="ts">
import { computed, nextTick, ref, watch } from 'vue'
import { RouterLink, RouterView, useRoute, useRouter } from 'vue-router'

import { useAuthStore } from '@/stores/auth'
import { usePlayerStore } from '@/stores/player'
import { useThemeStore } from '@/stores/theme'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const playerStore = usePlayerStore()
const themeStore = useThemeStore()
const audioRef = ref<HTMLAudioElement | null>(null)
const routeTransitionName = ref<'route-forward' | 'route-back'>('route-forward')
const isRouteSwitching = ref(false)
const isAudioPlaying = ref(false)
const isPlayerMinimized = ref(localStorage.getItem('music-share-platform-player-ui') === 'minimized')

authStore.restoreSession()
themeStore.restoreTheme()
playerStore.restoreSession()

const navigationItems = computed(() => {
  const items = [
    { label: '首页', to: '/' },
    { label: '发现', to: '/discover' },
    { label: '音乐库', to: '/library' },
    { label: '社区', to: '/community' },
  ]

  if (authStore.profile) {
    items.push({ label: '我的', to: '/me' })
    if (authStore.profile.role === 'CREATOR' || authStore.profile.role === 'ADMIN') {
      items.push({ label: '创作台', to: '/creator' })
    }
  }

  return items
})

const profileInitial = computed(() => authStore.profile?.displayName.slice(0, 1) ?? 'G')
const themeButtonLabel = computed(() => (themeStore.mode === 'light' ? '深色' : '浅色'))
const currentTrack = computed(() => playerStore.currentTrack)
const queueStatus = computed(() => {
  if (playerStore.queue.length === 0) {
    return '队列为空'
  }

  const currentIndex = playerStore.currentIndex >= 0 ? playerStore.currentIndex + 1 : 1
  return `${currentIndex} / ${playerStore.queue.length}`
})

function getRouteDepth() {
  const depth = route.meta.depth
  return typeof depth === 'number' ? depth : route.matched.length
}

function isActiveRoute(path: string) {
  return path === '/' ? route.path === '/' : route.path.startsWith(path)
}

function closePlayer() {
  playerStore.clearTrack()
  isAudioPlaying.value = false
}

function setPlayerMinimized(value: boolean) {
  isPlayerMinimized.value = value
  localStorage.setItem('music-share-platform-player-ui', value ? 'minimized' : 'expanded')
}

async function toggleAudioPlayback() {
  if (!audioRef.value || !currentTrack.value?.streamUrl) {
    return
  }

  if (audioRef.value.paused) {
    try {
      await audioRef.value.play()
    } catch {
      isAudioPlaying.value = false
    }
  } else {
    audioRef.value.pause()
  }
}

function handleAudioEnded() {
  isAudioPlaying.value = false
  playerStore.playNext()
}

async function logout() {
  authStore.logout()
  if (route.meta.requiresAuth || route.meta.requiresCreator) {
    await router.push({
      path: '/login',
      query: { redirect: route.fullPath },
    })
  }
}

function finishRouteSwitch() {
  isRouteSwitching.value = false
}

let previousRouteDepth = getRouteDepth()

watch(
  () => route.fullPath,
  () => {
    const nextRouteDepth = getRouteDepth()
    routeTransitionName.value = nextRouteDepth < previousRouteDepth ? 'route-back' : 'route-forward'
    previousRouteDepth = nextRouteDepth
    isRouteSwitching.value = true
  },
)

watch(
  () => playerStore.autoplayToken,
  async () => {
    await nextTick()
    if (!audioRef.value || !playerStore.currentTrack?.streamUrl) {
      return
    }

    audioRef.value.load()
    try {
      await audioRef.value.play()
    } catch {
      // Browser policy may block autoplay after route changes.
    }
  },
)
</script>

<template>
  <div class="app-shell">
    <header class="topbar">
      <div class="topbar-inner">
        <RouterLink class="brand motion-rise" to="/">
          <span class="brand-mark" aria-hidden="true"></span>
          <div>
            <p class="brand-name">Music Share</p>
            <p class="brand-copy">夜间助眠、社区讨论和自由下载的一体化音乐平台</p>
          </div>
        </RouterLink>

        <nav class="topnav motion-rise motion-delay-1" aria-label="主导航">
          <RouterLink
            v-for="item in navigationItems"
            :key="item.to"
            :to="item.to"
            class="topnav-link"
            :class="{ active: isActiveRoute(item.to) }"
          >
            {{ item.label }}
          </RouterLink>
        </nav>

        <div class="toolbar motion-rise motion-delay-2">
          <button type="button" class="secondary-button theme-button" @click="themeStore.toggleTheme()">
            切换{{ themeButtonLabel }}主题
          </button>

          <div class="auth-entry">
            <template v-if="authStore.profile">
              <div class="profile-chip">
                <span class="profile-initial">{{ profileInitial }}</span>
                <div>
                  <p class="profile-name">{{ authStore.profile.displayName }}</p>
                  <p class="profile-role">{{ authStore.profile.role }} · {{ authStore.profile.favoriteGenre }}</p>
                </div>
              </div>
              <button type="button" class="secondary-button logout-button" @click="logout">
                退出
              </button>
            </template>
            <RouterLink v-else class="primary-button login-button" to="/login">登录 / 注册</RouterLink>
          </div>
        </div>
      </div>
    </header>

    <main class="content-frame" :class="{ 'is-route-switching': isRouteSwitching }">
      <RouterView v-slot="{ Component, route: viewRoute }">
        <Transition
          :name="routeTransitionName"
          mode="out-in"
          appear
          @before-enter="isRouteSwitching = true"
          @after-enter="finishRouteSwitch"
          @enter-cancelled="finishRouteSwitch"
          @before-leave="isRouteSwitching = true"
          @leave-cancelled="finishRouteSwitch"
        >
          <div :key="viewRoute.fullPath" class="route-page">
            <component :is="Component" />
          </div>
        </Transition>
      </RouterView>
    </main>

    <Transition name="player-float">
      <div v-if="currentTrack" class="floating-player" :class="{ minimized: isPlayerMinimized }">
        <div class="player-summary">
          <span class="player-disc" :class="{ playing: isAudioPlaying }" aria-hidden="true"></span>
          <div class="floating-player-copy">
            <span class="eyebrow">Now Playing</span>
            <h3>{{ currentTrack.title }}</h3>
            <p>
              {{ currentTrack.artist }}
              <template v-if="currentTrack.moodTag"> · {{ currentTrack.moodTag }}</template>
              <template v-if="currentTrack.genre"> · {{ currentTrack.genre }}</template>
            </p>
          </div>
        </div>

        <div class="floating-player-controls" :class="{ collapsed: isPlayerMinimized }">
          <audio
            v-if="currentTrack.streamUrl"
            ref="audioRef"
            class="audio-player"
        :src="currentTrack.streamUrl"
            controls
            preload="none"
            @play="isAudioPlaying = true"
            @pause="isAudioPlaying = false"
            @ended="handleAudioEnded"
          ></audio>
          <div class="queue-actions">
            <button type="button" class="secondary-button player-action" @click="playerStore.playPrevious()">
              上一首
            </button>
            <button type="button" class="secondary-button player-action" @click="toggleAudioPlayback">
              {{ isAudioPlaying ? '暂停' : '播放' }}
            </button>
            <button type="button" class="secondary-button player-action" @click="playerStore.playNext()">
              下一首
            </button>
            <button
              v-if="!isPlayerMinimized"
              type="button"
              class="secondary-button player-action"
              :class="{ active: playerStore.shuffleEnabled }"
              @click="playerStore.toggleShuffle()"
            >
              {{ playerStore.shuffleEnabled ? '随机播放中' : '随机播放' }}
            </button>
          </div>
        </div>

        <div class="floating-player-actions">
          <span class="queue-count">{{ queueStatus }}</span>
          <a
            v-if="currentTrack.downloadUrl && !isPlayerMinimized"
            class="secondary-button player-action"
            :href="currentTrack.downloadUrl"
          >
            下载
          </a>
          <button type="button" class="secondary-button player-action" @click="setPlayerMinimized(!isPlayerMinimized)">
            {{ isPlayerMinimized ? '展开' : '最小化' }}
          </button>
          <button type="button" class="secondary-button player-action" @click="closePlayer">关闭</button>
        </div>

        <div v-if="playerStore.recentTracks.length > 1 && !isPlayerMinimized" class="recent-track-strip">
          <span class="recent-label">最近播放</span>
          <button
            v-for="track in playerStore.recentTracks.slice(0, 5)"
            :key="track.id"
            type="button"
            class="recent-track"
            @click="playerStore.setTrack(track, true)"
          >
            {{ track.title }}
          </button>
        </div>
      </div>
    </Transition>
  </div>
</template>

<style scoped>
.app-shell {
  min-height: 100vh;
  padding: 10px;
}

.topbar {
  position: sticky;
  top: 14px;
  z-index: 30;
}

.topbar-inner {
  max-width: var(--content-width);
  margin: 0 auto;
  padding: 18px 24px;
  border: 1px solid var(--color-border);
  border-radius: 30px;
  background: color-mix(in srgb, var(--color-surface) 92%, transparent);
  backdrop-filter: blur(24px);
  box-shadow: var(--shadow-soft);
  display: flex;
  justify-content: space-between;
  gap: 20px;
  align-items: center;
  flex-wrap: wrap;
}

.brand {
  display: flex;
  align-items: center;
  gap: 14px;
  color: inherit;
}

.brand-mark {
  width: 48px;
  height: 48px;
  border-radius: 18px;
  background:
    radial-gradient(circle at 30% 30%, rgba(255, 255, 255, 0.86), transparent 24%),
    linear-gradient(135deg, #5eead4, #818cf8 48%, #f9a8d4 100%);
  box-shadow: 0 18px 34px rgba(94, 234, 212, 0.18);
}

.brand-name {
  font-family: var(--font-display);
  font-size: 1.05rem;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.brand-copy {
  color: var(--color-muted);
  font-size: 0.92rem;
}

.topnav {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.topnav-link {
  position: relative;
  isolation: isolate;
  overflow: hidden;
  padding: 10px 14px;
  border-radius: 999px;
  color: var(--color-muted);
  transition:
    color 0.22s var(--motion-ease),
    transform 0.22s var(--motion-ease);
}

.topnav-link::before {
  position: absolute;
  inset: 0;
  z-index: -1;
  border-radius: inherit;
  background: color-mix(in srgb, var(--color-panel) 86%, transparent);
  content: '';
  opacity: 0;
  transform: scale(0.82);
  transition:
    opacity 0.22s var(--motion-ease),
    transform 0.22s var(--motion-ease);
}

.topnav-link:hover,
.topnav-link:focus-visible,
.topnav-link.active {
  color: var(--color-heading);
  transform: translateY(-1px);
}

.topnav-link:hover::before,
.topnav-link:focus-visible::before,
.topnav-link.active::before {
  opacity: 1;
  transform: scale(1);
}

.toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-left: auto;
  flex-wrap: wrap;
}

.theme-button {
  min-height: 42px;
}

.auth-entry {
  display: flex;
  align-items: center;
  gap: 12px;
}

.profile-chip {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 12px;
  border-radius: 999px;
  border: 1px solid var(--color-border);
  background: color-mix(in srgb, var(--color-panel) 88%, transparent);
}

.profile-initial {
  width: 34px;
  height: 34px;
  border-radius: 50%;
  display: grid;
  place-items: center;
  background: linear-gradient(135deg, #5eead4, #818cf8);
  color: #0f1124;
  font-family: var(--font-display);
  font-weight: 700;
}

.profile-name {
  color: var(--color-heading);
  font-size: 0.92rem;
  font-weight: 600;
}

.profile-role {
  color: var(--color-muted);
  font-size: 0.8rem;
}

.logout-button,
.login-button {
  min-height: 42px;
}

.content-frame {
  position: relative;
  padding: 20px 0 148px;
  isolation: isolate;
  overflow-x: clip;
}

.content-frame.is-route-switching {
  cursor: progress;
}

.route-page {
  min-height: calc(100vh - 220px);
  transform-origin: 50% 0;
  backface-visibility: hidden;
  contain: layout style;
  will-change: transform, opacity;
}

.floating-player {
  position: fixed;
  left: 12px;
  right: 12px;
  bottom: 12px;
  z-index: 40;
  margin: 0 auto;
  max-width: var(--content-width);
  padding: 18px 22px;
  border: 1px solid var(--color-border);
  border-radius: 28px;
  background: color-mix(in srgb, var(--color-surface-strong) 94%, transparent);
  backdrop-filter: blur(26px);
  box-shadow: 0 28px 70px rgba(0, 0, 0, 0.22);
  display: grid;
  grid-template-columns: minmax(220px, 0.8fr) minmax(320px, 1.25fr) auto;
  gap: 18px;
  align-items: center;
  transition:
    width 220ms var(--motion-ease),
    max-width 220ms var(--motion-ease),
    padding 220ms var(--motion-ease),
    transform 220ms var(--motion-ease);
}

.floating-player.minimized {
  left: auto;
  width: min(620px, calc(100vw - 24px));
  padding: 12px 14px;
  grid-template-columns: minmax(0, 1fr) auto auto;
  gap: 12px;
}

.player-summary {
  display: flex;
  min-width: 0;
  gap: 14px;
  align-items: center;
}

.player-disc {
  width: 46px;
  height: 46px;
  flex: 0 0 auto;
  border-radius: 50%;
  background:
    radial-gradient(circle at center, var(--color-surface-strong) 0 18%, transparent 19%),
    conic-gradient(from 120deg, var(--color-primary), var(--color-secondary), var(--color-accent), var(--color-primary));
  box-shadow: 0 14px 26px rgba(15, 17, 36, 0.18);
}

.player-disc.playing {
  animation: disc-spin 4s linear infinite;
}

@keyframes disc-spin {
  to {
    transform: rotate(360deg);
  }
}

.floating-player-copy h3 {
  color: var(--color-heading);
  font-family: var(--font-display);
  font-size: 1.15rem;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.floating-player-copy p {
  margin-top: 6px;
  color: var(--color-muted);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.floating-player-controls {
  display: grid;
  gap: 12px;
}

.audio-player {
  width: 100%;
}

.floating-player.minimized .audio-player {
  display: none;
}

.queue-actions,
.floating-player-actions {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  align-items: center;
}

.player-action {
  min-height: 42px;
}

.player-action.active {
  background: rgba(94, 234, 212, 0.14);
  color: var(--color-heading);
}

.queue-count {
  min-height: 36px;
  padding: 0 12px;
  border: 1px solid var(--color-border);
  border-radius: 999px;
  display: inline-flex;
  align-items: center;
  color: var(--color-muted);
  font-size: 0.9rem;
}

.recent-track-strip {
  grid-column: 1 / -1;
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  align-items: center;
}

.recent-label {
  color: var(--color-muted);
  font-size: 0.9rem;
}

.recent-track {
  padding: 8px 12px;
  border: 1px solid var(--color-border);
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.04);
  color: var(--color-heading);
  cursor: pointer;
  transition: transform 0.2s ease;
}

.recent-track:hover {
  transform: translateY(-1px);
}

.route-forward-enter-active,
.route-forward-leave-active,
.route-back-enter-active,
.route-back-leave-active {
  transition:
    opacity var(--motion-page) var(--motion-ease),
    transform var(--motion-page) var(--motion-ease);
  will-change: transform, opacity;
}

.route-forward-enter-from,
.route-back-leave-to {
  opacity: 0;
  transform: translate3d(0, 18px, 0) scale(0.992);
}

.route-forward-leave-to,
.route-back-enter-from {
  opacity: 0;
  transform: translate3d(0, -12px, 0) scale(0.996);
}

.player-float-enter-active,
.player-float-leave-active {
  transition:
    opacity 220ms var(--motion-ease),
    transform 220ms var(--motion-ease);
}

.player-float-enter-from,
.player-float-leave-to {
  opacity: 0;
  transform: translateY(18px);
}

@media (max-width: 1080px) {
  .floating-player:not(.minimized) {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 720px) {
  .floating-player.minimized {
    left: 12px;
    grid-template-columns: 1fr;
  }

  .floating-player.minimized .queue-actions,
  .floating-player.minimized .floating-player-actions {
    justify-content: space-between;
  }

  .floating-player.minimized .player-action {
    flex: 1 1 88px;
  }
}

@media (max-width: 900px) {
  .app-shell {
    padding: 8px;
  }

  .topbar-inner {
    align-items: flex-start;
    flex-direction: column;
  }

  .topnav,
  .toolbar,
  .auth-entry {
    width: 100%;
  }

  .toolbar,
  .auth-entry {
    justify-content: space-between;
    margin-left: 0;
  }

  .topnav-link {
    flex: 1 1 120px;
    text-align: center;
  }
}
</style>

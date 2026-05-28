import { defineStore } from 'pinia'

import type { PlayerTrack } from '@/types/player'

const PLAYER_STORAGE_KEY = 'music-share-platform-player'

interface PlayerState {
  currentTrack: PlayerTrack | null
  queue: PlayerTrack[]
  recentTracks: PlayerTrack[]
  currentIndex: number
  shuffleEnabled: boolean
  autoplayToken: number
}

function loadPersistedPlayerState(): Partial<PlayerState> {
  const rawValue = localStorage.getItem(PLAYER_STORAGE_KEY)
  if (!rawValue) {
    return {}
  }

  try {
    return JSON.parse(rawValue) as Partial<PlayerState>
  } catch {
    localStorage.removeItem(PLAYER_STORAGE_KEY)
    return {}
  }
}

export const usePlayerStore = defineStore('player', {
  state: (): PlayerState => ({
    currentTrack: null,
    queue: [],
    recentTracks: [],
    currentIndex: -1,
    shuffleEnabled: false,
    autoplayToken: 0,
  }),
  actions: {
    restoreSession() {
      const persistedState = loadPersistedPlayerState()
      this.currentTrack = persistedState.currentTrack ?? null
      this.queue = persistedState.queue ?? []
      this.recentTracks = persistedState.recentTracks ?? []
      this.currentIndex = persistedState.currentIndex ?? -1
      this.shuffleEnabled = persistedState.shuffleEnabled ?? false
    },
    setTrack(track: PlayerTrack, autoplay = true, queue?: PlayerTrack[]) {
      const nextQueue = queue && queue.length > 0 ? queue : this.queue
      if (queue && queue.length > 0) {
        this.queue = queue
      } else if (this.queue.length === 0) {
        this.queue = [track]
      }

      this.currentTrack = track
      this.currentIndex = this.queue.findIndex((item) => item.id === track.id)
      if (this.currentIndex < 0) {
        this.queue = [...this.queue, track]
        this.currentIndex = this.queue.length - 1
      }

      this.recordRecentTrack(track)
      this.persist()
      if (autoplay) {
        this.autoplayToken += 1
      }
    },
    setQueue(queue: PlayerTrack[], autoplayFirst = false) {
      this.queue = queue
      const firstTrack = queue[0] ?? null
      if (queue.length === 0) {
        this.currentTrack = null
        this.currentIndex = -1
      } else if (autoplayFirst && firstTrack) {
        this.setTrack(firstTrack, true, queue)
        return
      } else if (!this.currentTrack && firstTrack) {
        this.currentTrack = firstTrack
        this.currentIndex = 0
      }
      this.persist()
    },
    toggleShuffle() {
      this.shuffleEnabled = !this.shuffleEnabled
      this.persist()
    },
    playNext() {
      const nextTrack = this.resolveNextTrack()
      if (!nextTrack) {
        return
      }
      this.setTrack(nextTrack, true)
    },
    playPrevious() {
      const previousTrack = this.resolvePreviousTrack()
      if (!previousTrack) {
        return
      }
      this.setTrack(previousTrack, true)
    },
    clearTrack() {
      this.currentTrack = null
      this.currentIndex = -1
      this.persist()
    },
    persist() {
      localStorage.setItem(
        PLAYER_STORAGE_KEY,
        JSON.stringify({
          currentTrack: this.currentTrack,
          queue: this.queue,
          recentTracks: this.recentTracks,
          currentIndex: this.currentIndex,
          shuffleEnabled: this.shuffleEnabled,
        }),
      )
    },
    recordRecentTrack(track: PlayerTrack) {
      this.recentTracks = [
        track,
        ...this.recentTracks.filter((item) => item.id !== track.id),
      ].slice(0, 8)
    },
    resolveNextTrack() {
      if (this.queue.length === 0) {
        return null
      }
      if (this.shuffleEnabled && this.queue.length > 1) {
        const candidates = this.queue.filter((item) => item.id !== this.currentTrack?.id)
        return candidates[Math.floor(Math.random() * candidates.length)] ?? null
      }
      const nextIndex = this.currentIndex >= 0 ? (this.currentIndex + 1) % this.queue.length : 0
      return this.queue[nextIndex] ?? null
    },
    resolvePreviousTrack() {
      if (this.queue.length === 0) {
        return null
      }
      const previousIndex =
        this.currentIndex > 0 ? this.currentIndex - 1 : this.queue.length - 1
      return this.queue[previousIndex] ?? null
    },
  },
})

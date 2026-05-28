import { defineStore } from 'pinia'

export type ThemeMode = 'light' | 'dark'

const STORAGE_KEY = 'music-share-platform-theme'

function applyTheme(theme: ThemeMode) {
  document.documentElement.setAttribute('data-theme', theme)
}

export const useThemeStore = defineStore('theme', {
  state: () => ({
    mode: 'light' as ThemeMode,
  }),
  actions: {
    restoreTheme() {
      const storedTheme = localStorage.getItem(STORAGE_KEY)
      this.mode = storedTheme === 'dark' ? 'dark' : 'light'
      applyTheme(this.mode)
    },
    toggleTheme() {
      this.mode = this.mode === 'light' ? 'dark' : 'light'
      localStorage.setItem(STORAGE_KEY, this.mode)
      applyTheme(this.mode)
    },
  },
})

import { defineStore } from 'pinia'

import { postJson } from '@/services/api'
import type { LoginRequest, LoginResponse, RegisterRequest, UserProfile } from '@/types/auth'
import type { ApiResponse } from '@/types/common'

const STORAGE_KEY = 'music-share-platform-auth'

interface AuthState {
  token: string
  profile: UserProfile | null
  capabilities: string[]
}

function loadPersistedState(): AuthState {
  const rawValue = localStorage.getItem(STORAGE_KEY)
  if (!rawValue) {
    return {
      token: '',
      profile: null,
      capabilities: [],
    }
  }

  try {
    return JSON.parse(rawValue) as AuthState
  } catch {
    localStorage.removeItem(STORAGE_KEY)
    return {
      token: '',
      profile: null,
      capabilities: [],
    }
  }
}

export const useAuthStore = defineStore('auth', {
  state: (): AuthState => ({
    token: '',
    profile: null,
    capabilities: [],
  }),
  getters: {
    isAuthenticated: (state) => Boolean(state.token && state.profile),
  },
  actions: {
    restoreSession() {
      const persistedState = loadPersistedState()
      this.token = persistedState.token
      this.profile = persistedState.profile
      this.capabilities = persistedState.capabilities
    },
    async login(payload: LoginRequest) {
      const response = await postJson<ApiResponse<LoginResponse>>('/api/auth/login', payload)
      this.token = response.data.accessToken
      this.profile = response.data.profile
      this.capabilities = response.data.capabilities
      this.persist()
      return response.data
    },
    async register(payload: RegisterRequest) {
      const response = await postJson<ApiResponse<LoginResponse>>('/api/auth/register', payload)
      this.token = response.data.accessToken
      this.profile = response.data.profile
      this.capabilities = response.data.capabilities
      this.persist()
      return response.data
    },
    logout() {
      this.token = ''
      this.profile = null
      this.capabilities = []
      localStorage.removeItem(STORAGE_KEY)
    },
    persist() {
      localStorage.setItem(
        STORAGE_KEY,
        JSON.stringify({
          token: this.token,
          profile: this.profile,
          capabilities: this.capabilities,
        }),
      )
    },
  },
})

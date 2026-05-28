import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getUserInfoAPI, loginAPI } from '@/api/modules/user'
import type { LoginForm, UserInfo } from '@/types'

export const useUserStore = defineStore('user', () => {
  const userInfo = ref<UserInfo | null>(null)
  const isLoggedIn = ref(false)

  const persistUserInfo = (info: UserInfo | null) => {
    if (info) {
      localStorage.setItem('userInfo', JSON.stringify(info))
    } else {
      localStorage.removeItem('userInfo')
    }
  }

  const setUserInfo = (info: UserInfo | null, loggedIn: boolean = !!info) => {
    userInfo.value = info
    isLoggedIn.value = loggedIn
    persistUserInfo(info)
  }

  const mergeUserInfo = (patch: Partial<UserInfo>) => {
    if (!userInfo.value) return
    setUserInfo({ ...userInfo.value, ...patch }, isLoggedIn.value)
  }

  const clearUserInfo = () => {
    setUserInfo(null, false)
  }

  const login = async (loginForm: LoginForm) => {
    try {
      const res: any = await loginAPI(loginForm)
      if (res.code === 200 && res.data) {
        setUserInfo(res.data, true)
        await fetchUserInfo().catch(() => undefined)
      }
      return res
    } catch (error) {
      clearUserInfo()
      throw error
    }
  }

  const fetchUserInfo = async () => {
    try {
      const res: any = await getUserInfoAPI()
      if (res.code === 200 && res.data) {
        setUserInfo(res.data, true)
        return res.data
      }

      clearUserInfo()
      throw new Error('获取用户信息失败')
    } catch (error) {
      clearUserInfo()
      throw error
    }
  }

  const logout = async () => {
    try {
      await fetch('/api/user/logout', {
        method: 'POST',
        credentials: 'include',
      })
    } finally {
      clearUserInfo()
    }
  }

  const initUserInfo = () => {
    const savedUserInfo = localStorage.getItem('userInfo')
    if (!savedUserInfo) return

    try {
      userInfo.value = JSON.parse(savedUserInfo)
    } catch {
      localStorage.removeItem('userInfo')
    }
  }

  initUserInfo()

  return {
    userInfo,
    isLoggedIn,
    login,
    fetchUserInfo,
    logout,
    setUserInfo,
    mergeUserInfo,
    clearUserInfo,
  }
})

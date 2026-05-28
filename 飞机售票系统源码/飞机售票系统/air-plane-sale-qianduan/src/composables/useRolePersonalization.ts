import { reactive, watch } from 'vue'

type PreferencesShape = Record<string, any>

interface UseRolePersonalizationReturn<T extends PreferencesShape> {
  preferences: T
  resetPreferences: () => void
}

const softClone = <T>(value: T): T => JSON.parse(JSON.stringify(value))

/**
 * 通用角色个性化偏好管理钩子
 * @param storageKey 唯一存储键，如 operations_dashboard
 * @param defaultPreferences 默认偏好
 */
export function useRolePersonalization<T extends PreferencesShape>(
  storageKey: string,
  defaultPreferences: T
): UseRolePersonalizationReturn<T> {
  const key = `personalization_${storageKey}`

  const loadInitialState = (): T => {
    try {
      const stored = localStorage.getItem(key)
      if (stored) {
        const parsed = JSON.parse(stored)
        return { ...softClone(defaultPreferences), ...parsed }
      }
    } catch (error) {
      console.warn(`加载个性化设置失败: ${key}`, error)
    }
    return softClone(defaultPreferences)
  }

  const preferences = reactive(loadInitialState()) as T

  watch(
    preferences,
    (val) => {
      localStorage.setItem(key, JSON.stringify(val))
    },
    { deep: true }
  )

  const resetPreferences = () => {
    Object.assign(preferences, softClone(defaultPreferences))
    localStorage.removeItem(key)
  }

  return {
    preferences,
    resetPreferences
  }
}


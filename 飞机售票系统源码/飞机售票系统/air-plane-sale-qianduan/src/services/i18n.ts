import { computed } from 'vue'
import store from './store'
import { messages } from '../locales/messages'
import type { Locale, LocaleMessages } from '../locales/messages'

type TranslationParams = Record<string, string | number>

const DEFAULT_LOCALE: Locale = 'zh-CN'

const getNestedValue = (source: LocaleMessages, path: string) => {
  return path.split('.').reduce<any>((acc, key) => {
    if (acc && typeof acc === 'object' && key in acc) {
      return acc[key as keyof typeof acc]
    }
    return undefined
  }, source)
}

const formatString = (template: string, params?: TranslationParams) => {
  if (!params) return template
  return template.replace(/\{(\w+)\}/g, (_, match: string) => {
    const value = params[match]
    return value !== undefined ? String(value) : ''
  })
}

export const useI18n = () => {
  const locale = computed<Locale>(() => {
    return (store.themeState.language as Locale) || DEFAULT_LOCALE
  })

  const currentMessages = computed(() => {
    return messages[locale.value] || messages[DEFAULT_LOCALE]
  })

  const t = (key: string, params?: TranslationParams) => {
    const value = getNestedValue(currentMessages.value, key)
    if (typeof value === 'string') {
      return formatString(value, params)
    }
    return key
  }

  return {
    locale,
    t,
    messages: currentMessages
  }
}

export const translate = (key: string, params?: TranslationParams) => {
  const locale = (store.themeState.language as Locale) || DEFAULT_LOCALE
  const localeMessages = messages[locale] || messages[DEFAULT_LOCALE]
  const value = getNestedValue(localeMessages, key)
  if (typeof value === 'string') {
    return formatString(value, params)
  }
  return key
}

export type { Locale }


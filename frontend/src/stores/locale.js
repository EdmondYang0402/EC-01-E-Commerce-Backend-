import { defineStore } from 'pinia'
import { messages } from '../i18n/messages'

const LOCALE_KEY = 'language'
const LEGACY_LOCALE_KEY = 'ec01.locale'
const supportedLocales = ['zh-CN', 'en-US', 'ja-JP']
const legacyLocales = { zh: 'zh-CN', en: 'en-US', ja: 'ja-JP' }

const initialLocale = () => {
  const saved = localStorage.getItem(LOCALE_KEY)
  if (supportedLocales.includes(saved)) return saved
  const legacy = legacyLocales[localStorage.getItem(LEGACY_LOCALE_KEY)]
  if (legacy) {
    localStorage.setItem(LOCALE_KEY, legacy)
    return legacy
  }
  return 'zh-CN'
}

export const useLocaleStore = defineStore('locale', {
  state: () => ({ locale: initialLocale() }),

  actions: {
    setLocale(locale) {
      if (!supportedLocales.includes(locale)) return
      this.locale = locale
      localStorage.setItem(LOCALE_KEY, locale)
      document.documentElement.lang = locale
    },

    applyLocale() {
      document.documentElement.lang = this.locale
    },

    t(key, params = {}) {
      const template = messages[this.locale]?.[key] ?? messages['en-US'][key] ?? key
      return Object.entries(params).reduce(
        (text, [name, value]) => text.replaceAll(`{${name}}`, String(value)),
        template,
      )
    },
  },
})

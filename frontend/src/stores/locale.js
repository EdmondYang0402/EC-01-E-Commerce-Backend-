import { defineStore } from 'pinia'
import { messages } from '../i18n/messages'

const LOCALE_KEY = 'ec01.locale'
const supportedLocales = ['zh', 'en', 'ja']

const initialLocale = () => {
  const saved = localStorage.getItem(LOCALE_KEY)
  if (supportedLocales.includes(saved)) return saved
  const browserLocale = navigator.language.toLowerCase()
  if (browserLocale.startsWith('zh')) return 'zh'
  if (browserLocale.startsWith('ja')) return 'ja'
  return 'en'
}

export const useLocaleStore = defineStore('locale', {
  state: () => ({ locale: initialLocale() }),

  actions: {
    setLocale(locale) {
      if (!supportedLocales.includes(locale)) return
      this.locale = locale
      localStorage.setItem(LOCALE_KEY, locale)
      document.documentElement.lang = locale === 'zh' ? 'zh-CN' : locale
    },

    applyLocale() {
      document.documentElement.lang = this.locale === 'zh' ? 'zh-CN' : this.locale
    },

    t(key, params = {}) {
      const template = messages[this.locale]?.[key] ?? messages.en[key] ?? key
      return Object.entries(params).reduce(
        (text, [name, value]) => text.replaceAll(`{${name}}`, String(value)),
        template,
      )
    },
  },
})

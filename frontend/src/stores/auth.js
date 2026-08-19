import { defineStore } from 'pinia'
import { authApi } from '../services/auth'
import { AUTH_TOKEN_KEY } from '../services/http'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem(AUTH_TOKEN_KEY),
    userId: null,
    profile: null,
    loading: false,
  }),

  getters: {
    isAuthenticated: (state) => Boolean(state.token),
    displayName: (state) => state.profile?.nickname || state.profile?.username || 'Account',
  },

  actions: {
    async register(payload) {
      await authApi.register(payload)
    },

    async login(payload) {
      this.loading = true
      try {
        const result = await authApi.login(payload)
        this.token = result.token
        this.userId = result.userId
        localStorage.setItem(AUTH_TOKEN_KEY, result.token)
        await this.fetchProfile()
        return result
      } catch (error) {
        this.clearSession()
        throw error
      } finally {
        this.loading = false
      }
    },

    async fetchProfile() {
      if (!this.token) return null
      this.profile = await authApi.getProfile()
      this.userId = this.profile?.id ?? this.userId
      return this.profile
    },

    async updateProfile(payload) {
      await authApi.updateProfile(payload)
      return this.fetchProfile()
    },

    async logout() {
      try {
        if (this.token) await authApi.logout()
      } finally {
        this.clearSession()
      }
    },

    clearSession() {
      this.token = null
      this.userId = null
      this.profile = null
      localStorage.removeItem(AUTH_TOKEN_KEY)
    },
  },
})

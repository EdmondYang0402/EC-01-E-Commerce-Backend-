import { defineStore } from 'pinia'
import { categoryApi } from '../services/categories'

export const useCategoryStore = defineStore('categories', {
  state: () => ({
    tree: [],
    loading: false,
    loaded: false,
  }),

  actions: {
    async fetchTree(force = false) {
      if (this.loaded && !force) return this.tree
      this.loading = true
      try {
        this.tree = await categoryApi.getTree() || []
        this.loaded = true
        return this.tree
      } finally {
        this.loading = false
      }
    },
  },
})

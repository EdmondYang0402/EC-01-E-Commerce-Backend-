import { defineStore } from 'pinia'
import { productApi } from '../services/products'

export const useProductStore = defineStore('products', {
  state: () => ({
    records: [],
    total: 0,
    detail: null,
    loading: false,
    detailLoading: false,
  }),

  actions: {
    async fetchPage(params = {}) {
      this.loading = true
      try {
        const page = await productApi.getPage(params)
        this.records = page?.records || []
        this.total = page?.total || 0
        return page
      } finally {
        this.loading = false
      }
    },

    async fetchDetail(productId) {
      this.detailLoading = true
      this.detail = null
      try {
        this.detail = await productApi.getDetail(productId)
        return this.detail
      } finally {
        this.detailLoading = false
      }
    },
  },
})

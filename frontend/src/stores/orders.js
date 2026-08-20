import { defineStore } from 'pinia'
import { orderApi } from '../services/orders'

export const useOrderStore = defineStore('orders', {
  state: () => ({
    records: [],
    total: 0,
    detail: null,
    listLoading: false,
    detailLoading: false,
    creating: false,
  }),

  actions: {
    async createOrder(payload) {
      this.creating = true
      try {
        return await orderApi.createOrder(payload)
      } finally {
        this.creating = false
      }
    },

    async fetchPage(params = {}) {
      this.listLoading = true
      try {
        const page = await orderApi.getMyOrders(params)
        this.records = page?.records || []
        this.total = Number(page?.total || 0)
        return page
      } finally {
        this.listLoading = false
      }
    },

    async fetchDetail(orderNo) {
      this.detailLoading = true
      this.detail = null
      try {
        this.detail = await orderApi.getOrderDetail(orderNo)
        return this.detail
      } finally {
        this.detailLoading = false
      }
    },
  },
})

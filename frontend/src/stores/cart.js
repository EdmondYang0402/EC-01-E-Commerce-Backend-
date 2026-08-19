import { defineStore } from 'pinia'
import { cartApi } from '../services/cart'

export const useCartStore = defineStore('cart', {
  state: () => ({
    items: [],
    loading: false,
  }),

  getters: {
    itemCount: (state) => state.items.reduce((count, item) => count + Number(item.quantity || 0), 0),
    selectedCount: (state) => state.items
      .filter((item) => Number(item.selected) === 1)
      .reduce((count, item) => count + Number(item.quantity || 0), 0),
    selectedTotal: (state) => state.items
      .filter((item) => Number(item.selected) === 1)
      .reduce((total, item) => total + Number(item.price || 0) * Number(item.quantity || 0), 0),
  },

  actions: {
    async fetchCart() {
      this.loading = true
      try {
        this.items = await cartApi.getCart() || []
        return this.items
      } finally {
        this.loading = false
      }
    },

    async addItem(payload) {
      await cartApi.addCart(payload)
      return this.fetchCart()
    },

    async updateItem(cartItemId, payload) {
      await cartApi.updateCartItem(cartItemId, payload)
      return this.fetchCart()
    },

    async deleteItem(cartItemId) {
      await cartApi.deleteCartItem(cartItemId)
      return this.fetchCart()
    },

    clear() {
      this.items = []
      this.loading = false
    },
  },
})

import { defineStore } from 'pinia'

export const useShopStore = defineStore('shop', {
  state: () => ({
    favoriteIds: [],
  }),
  getters: {
    isFavorite: (state) => (productId) => state.favoriteIds.includes(productId),
  },
  actions: {
    toggleFavorite(productId) {
      const index = this.favoriteIds.indexOf(productId)

      if (index === -1) {
        this.favoriteIds.push(productId)
        return
      }

      this.favoriteIds.splice(index, 1)
    },
  },
})

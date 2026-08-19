import http, { responseData } from './http'

export const cartApi = {
  async getCart() {
    return responseData(await http.get('/cart'))
  },

  async addCart(payload) {
    return responseData(await http.post('/cart', payload))
  },

  async updateCartItem(cartItemId, payload) {
    return responseData(await http.put(`/cart/${cartItemId}`, payload))
  },

  async deleteCartItem(cartItemId) {
    return responseData(await http.delete(`/cart/${cartItemId}`))
  },
}

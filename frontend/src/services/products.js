import http, { responseData } from './http'

export const productApi = {
  async getPage(params = {}) {
    return responseData(await http.get('/products', { params }))
  },

  async getDetail(productId) {
    return responseData(await http.get(`/products/${productId}`))
  },
}

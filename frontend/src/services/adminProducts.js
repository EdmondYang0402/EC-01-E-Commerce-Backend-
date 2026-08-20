import http, { responseData } from './http'

export const adminProductApi = {
  async getPage(params = {}) {
    return responseData(await http.get('/admin/products', { params }))
  },

  async getDetail(productId) {
    return responseData(await http.get(`/admin/products/${productId}`))
  },

  async create(payload) {
    return responseData(await http.post('/admin/products', payload))
  },

  async update(productId, payload) {
    return responseData(await http.put(`/admin/products/${productId}`, payload))
  },

  async updateStatus(productId, status) {
    return responseData(await http.patch(`/admin/products/${productId}/status`, { status }))
  },

  async createSku(productId, payload) {
    return responseData(await http.post(`/admin/products/${productId}/skus`, payload))
  },

  async updateSku(skuId, payload) {
    return responseData(await http.put(`/admin/skus/${skuId}`, payload))
  },

  async updateSkuStatus(skuId, status) {
    return responseData(await http.patch(`/admin/skus/${skuId}/status`, { status }))
  },
}

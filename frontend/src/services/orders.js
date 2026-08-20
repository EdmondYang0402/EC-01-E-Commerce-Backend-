import http, { responseData } from './http'

export const orderApi = {
  async createOrder(payload) {
    return responseData(await http.post('/orders', payload))
  },

  async getMyOrders(params = {}) {
    return responseData(await http.get('/orders', { params }))
  },

  async getOrderDetail(orderNo) {
    return responseData(await http.get(`/orders/${encodeURIComponent(orderNo)}`))
  },
}

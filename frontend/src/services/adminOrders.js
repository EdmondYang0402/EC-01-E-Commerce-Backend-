import http, { responseData } from './http'

export const adminOrderApi = {
  async getPage(params = {}) {
    return responseData(await http.get('/admin/orders', { params }))
  },

  async getDetail(orderNo) {
    return responseData(await http.get(`/admin/orders/${encodeURIComponent(orderNo)}`))
  },
}

import http, { responseData } from './http'

export const paymentApi = {
  async create(orderNo) {
    return responseData(await http.post(`/orders/${encodeURIComponent(orderNo)}/payment`))
  },

  async getByOrderNo(orderNo) {
    return responseData(await http.get(`/orders/${encodeURIComponent(orderNo)}/payment`))
  },
}

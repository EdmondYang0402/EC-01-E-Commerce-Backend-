import http, { responseData } from './http'

export const adminUserApi = {
  async getPage(params = {}) {
    return responseData(await http.get('/admin/users', { params }))
  },

  async updateStatus(userId, status) {
    return responseData(await http.patch(`/admin/users/${userId}/status`, { status }))
  },
}

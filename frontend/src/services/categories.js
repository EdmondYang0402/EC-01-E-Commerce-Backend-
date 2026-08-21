import http, { responseData } from './http'

export const categoryApi = {
  async getTree() {
    return responseData(await http.get('/categories'))
  },
}

export const adminCategoryApi = {
  async getAll() {
    return responseData(await http.get('/admin/categories'))
  },

  async create(payload) {
    return responseData(await http.post('/admin/categories', payload))
  },

  async update(categoryId, payload) {
    return responseData(await http.put(`/admin/categories/${categoryId}`, payload))
  },

  async updateStatus(categoryId, status) {
    return responseData(await http.patch(`/admin/categories/${categoryId}/status`, { status }))
  },
}

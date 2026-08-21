import http, { responseData } from './http'

export const authApi = {
  async register(payload) {
    return responseData(await http.post('/auth/register', payload))
  },

  async login(payload) {
    return responseData(await http.post('/auth/login', payload))
  },

  async logout() {
    return responseData(await http.post('/auth/logout'))
  },

  async getProfile() {
    return responseData(await http.get('/users/me'))
  },

  async updateProfile(payload) {
    return responseData(await http.put('/users/me', payload))
  },

  async changePassword(payload) {
    return responseData(await http.put('/users/me/password', payload))
  },
}

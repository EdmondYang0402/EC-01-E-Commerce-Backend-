import axios from 'axios'

export const AUTH_TOKEN_KEY = 'ec01.auth.token'

const http = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 10000,
})

http.interceptors.request.use((config) => {
  const token = localStorage.getItem(AUTH_TOKEN_KEY)
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

http.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem(AUTH_TOKEN_KEY)
      window.dispatchEvent(new CustomEvent('ec01:unauthorized'))
    }
    return Promise.reject(error)
  },
)

export const responseData = (response) => response.data?.data

export const errorMessage = (error, fallback = '请求失败，请稍后重试') =>
  error.response?.data?.message || error.message || fallback

export default http

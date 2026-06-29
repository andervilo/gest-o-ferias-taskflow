import axios from 'axios'
import type { ApiError } from '../types'

export const api = axios.create({
  baseURL: '/api',
  headers: { 'Content-Type': 'application/json' },
  paramsSerializer: (params) => {
    const sp = new URLSearchParams()
    for (const [key, val] of Object.entries(params)) {
      if (val === undefined || val === null) continue
      if (Array.isArray(val)) {
        val.forEach((v) => sp.append(key, String(v)))
      } else {
        sp.set(key, String(val))
      }
    }
    return sp.toString()
  },
})

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

api.interceptors.response.use(
  (res) => res,
  (err) => {
    const apiErr: ApiError | undefined = err.response?.data
    if (err.response?.status === 401) {
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      window.location.href = '/login'
    }
    return Promise.reject(apiErr ?? err)
  }
)
import { api } from './client'
import type { AuthUser } from '../types'

export interface LoginRequest {
  email: string
  password: string
}

export interface LoginResponse {
  token: string
  id: string
  name: string
  email: string
  role: string
}

export const authApi = {
  login: async (body: LoginRequest): Promise<AuthUser> => {
    const { data } = await api.post<LoginResponse>('/auth/login', body)
    return { token: data.token, id: data.id, name: data.name, email: data.email, role: data.role as AuthUser['role'] }
  },
  me: async (): Promise<AuthUser> => {
    const { data } = await api.get<LoginResponse>('/auth/me')
    const token = localStorage.getItem('token') ?? ''
    return { token, id: data.id, name: data.name, email: data.email, role: data.role as AuthUser['role'] }
  },
}
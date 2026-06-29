import { create } from 'zustand'
import type { AuthUser } from '../types'

interface AuthState {
  user: AuthUser | null
  setUser: (user: AuthUser) => void
  logout: () => void
}

const stored = localStorage.getItem('user')

export const useAuthStore = create<AuthState>((set) => ({
  user: stored ? (JSON.parse(stored) as AuthUser) : null,
  setUser: (user) => {
    localStorage.setItem('token', user.token)
    localStorage.setItem('user', JSON.stringify(user))
    set({ user })
  },
  logout: () => {
    localStorage.removeItem('token')
    localStorage.removeItem('user')
    set({ user: null })
  },
}))

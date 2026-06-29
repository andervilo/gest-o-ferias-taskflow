export type Role = 'ADMIN' | 'MANAGER' | 'COLLABORATOR'
export type VacationStatus = 'PENDING' | 'APPROVED' | 'REJECTED' | 'CANCELLED'

export interface AuthUser {
  id: string
  name: string
  email: string
  role: Role
  token: string
}

export interface Employee {
  id: string
  name: string
  email: string
  role: Role
  managerId?: string | null
  active: boolean
  createdAt: string
  updatedAt: string
}

export interface PagedResult<T> {
  content: T[]
  page: number
  size: number
  totalElements: number
  totalPages: number
}

export interface VacationRequest {
  id: string
  employeeId: string
  employeeName?: string | null
  managerName?: string | null
  startDate: string
  endDate: string
  status: VacationStatus
  reason?: string
  decidedBy?: string | null
  decidedAt?: string | null
  createdAt: string
  updatedAt: string
}

export interface CalendarEntry {
  id: string
  employeeId: string
  employeeName: string
  startDate: string
  endDate: string
  status: VacationStatus
}

export interface ApiError {
  timestamp: string
  status: number
  error: string
  code: string
  message: string
  path: string
  details: string[]
}
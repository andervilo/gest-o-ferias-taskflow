import { api } from './client'
import type { Employee, PagedResult, Role } from '../types'

export interface EmployeeFilters {
  page?: number
  size?: number
  sort?: string
  dir?: string
  q?: string
  role?: Role
  managerId?: string
}

export interface CreateEmployeeBody {
  name: string
  email: string
  password: string
  role: Role
  managerId?: string | null
}

export interface UpdateEmployeeBody {
  name: string
  email: string
  managerId?: string | null
}

export const employeesApi = {
  list: async (filters: EmployeeFilters = {}): Promise<PagedResult<Employee>> => {
    const { data } = await api.get<PagedResult<Employee>>('/employees', { params: filters })
    return data
  },
  get: async (id: string): Promise<Employee> => {
    const { data } = await api.get<Employee>(`/employees/${id}`)
    return data
  },
  create: async (body: CreateEmployeeBody): Promise<Employee> => {
    const { data } = await api.post<Employee>('/employees', body)
    return data
  },
  update: async (id: string, body: UpdateEmployeeBody): Promise<Employee> => {
    const { data } = await api.put<Employee>(`/employees/${id}`, body)
    return data
  },
  remove: async (id: string): Promise<void> => {
    await api.delete(`/employees/${id}`)
  },
}
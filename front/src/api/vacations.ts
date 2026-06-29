import { api } from './client'
import type { CalendarEntry, PagedResult, VacationRequest, VacationStatus } from '../types'

export interface VacationFilters {
  page?: number
  size?: number
  status?: VacationStatus[]
  name?: string
  managerId?: string
  from?: string
  to?: string
  employeeId?: string
}

export interface CreateVacationBody {
  employeeId?: string
  startDate: string
  endDate: string
  reason?: string
}

export interface UpdateVacationBody {
  startDate: string
  endDate: string
  reason?: string
}

export const vacationsApi = {
  list: async (filters: VacationFilters = {}): Promise<PagedResult<VacationRequest>> => {
    const { data } = await api.get<PagedResult<VacationRequest>>('/vacations', { params: filters })
    return data
  },
  get: async (id: string): Promise<VacationRequest> => {
    const { data } = await api.get<VacationRequest>(`/vacations/${id}`)
    return data
  },
  create: async (body: CreateVacationBody): Promise<VacationRequest> => {
    const { data } = await api.post<VacationRequest>('/vacations', body)
    return data
  },
  update: async (id: string, body: UpdateVacationBody): Promise<VacationRequest> => {
    const { data } = await api.put<VacationRequest>(`/vacations/${id}`, body)
    return data
  },
  approve: async (id: string): Promise<VacationRequest> => {
    const { data } = await api.post<VacationRequest>(`/vacations/${id}/approve`)
    return data
  },
  reject: async (id: string): Promise<VacationRequest> => {
    const { data } = await api.post<VacationRequest>(`/vacations/${id}/reject`)
    return data
  },
  cancel: async (id: string): Promise<VacationRequest> => {
    const { data } = await api.post<VacationRequest>(`/vacations/${id}/cancel`)
    return data
  },
  calendar: async (from: string, to: string): Promise<CalendarEntry[]> => {
    const { data } = await api.get<CalendarEntry[]>('/vacations/calendar', { params: { from, to } })
    return data
  },
}
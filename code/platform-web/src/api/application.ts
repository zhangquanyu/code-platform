import request from '@/utils/request'
import type { ApplicationVO, ApplicationSimpleVO } from '@/types'
import type { PageResult } from '@/utils/request'

export interface AppPageQuery {
  pageNum?: number
  pageSize?: number
  keyword?: string
  status?: number
}

export function pageApplications(params: AppPageQuery) {
  return request.get<any, { data: PageResult<ApplicationVO> }>('/applications', { params })
    .then(r => r.data)
}

export function listSimpleApplications() {
  return request.get<any, { data: ApplicationSimpleVO[] }>('/applications/simple')
    .then(r => r.data)
}

export function getApplication(id: number) {
  return request.get<any, { data: ApplicationVO }>(`/applications/${id}`).then(r => r.data)
}

export function createApplication(data: Partial<ApplicationVO>) {
  return request.post<any, { data: ApplicationVO }>('/applications', data).then(r => r.data)
}

export function updateApplication(id: number, data: Partial<ApplicationVO>) {
  return request.put<any, { data: ApplicationVO }>(`/applications/${id}`, data).then(r => r.data)
}

export function deleteApplication(id: number) {
  return request.delete(`/applications/${id}`)
}

export function updateApplicationStatus(id: number, status: number) {
  return request.put(`/applications/${id}/status`, { status })
}

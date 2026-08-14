import request from '@/utils/request'
import type { ServiceVO, ServiceDetailVO, ServiceSimpleVO, ServiceParamVO } from '@/types'
import type { PageResult } from '@/utils/request'

export interface SvcPageQuery {
  pageNum?: number
  pageSize?: number
  keyword?: string
  microserviceId?: string
  applicationId?: string
  category?: string
  status?: number
}

export function pageServices(params: SvcPageQuery) {
  return request.get<any, { data: PageResult<ServiceVO> }>('/services', { params }).then(r => r.data)
}

export function listServicesByMicroservice(microserviceId: string) {
  return request.get<any, { data: ServiceSimpleVO[] }>(`/services/by-microservice/${microserviceId}`)
    .then(r => r.data)
}

export function getService(id: string) {
  return request.get<any, { data: ServiceDetailVO }>(`/services/${id}`).then(r => r.data)
}

export interface ServiceSavePayload {
  microserviceId: string
  name: string
  code: string
  description?: string
  httpMethod: string
  servicePath: string
  category?: string
  status?: number
  inputs?: Partial<ServiceParamVO>[]
  outputs?: Partial<ServiceParamVO>[]
}

export function createService(data: ServiceSavePayload) {
  return request.post<any, { data: ServiceDetailVO }>('/services', data).then(r => r.data)
}

export function updateService(id: string, data: ServiceSavePayload) {
  return request.put<any, { data: ServiceDetailVO }>(`/services/${id}`, data).then(r => r.data)
}

export function deleteService(id: string) {
  return request.delete(`/services/${id}`)
}

export function updateServiceStatus(id: string, status: number) {
  return request.put(`/services/${id}/status`, { status })
}

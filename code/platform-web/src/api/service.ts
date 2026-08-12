import request from '@/utils/request'
import type { ServiceVO, ServiceDetailVO, ServiceSimpleVO, ServiceParamVO } from '@/types'
import type { PageResult } from '@/utils/request'

export interface SvcPageQuery {
  pageNum?: number
  pageSize?: number
  keyword?: string
  microserviceId?: number
  applicationId?: number
  category?: string
  status?: number
}

export function pageServices(params: SvcPageQuery) {
  return request.get<any, { data: PageResult<ServiceVO> }>('/services', { params }).then(r => r.data)
}

export function listServicesByMicroservice(microserviceId: number) {
  return request.get<any, { data: ServiceSimpleVO[] }>(`/services/by-microservice/${microserviceId}`)
    .then(r => r.data)
}

export function getService(id: number) {
  return request.get<any, { data: ServiceDetailVO }>(`/services/${id}`).then(r => r.data)
}

export interface ServiceSavePayload {
  microserviceId: number
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

export function updateService(id: number, data: ServiceSavePayload) {
  return request.put<any, { data: ServiceDetailVO }>(`/services/${id}`, data).then(r => r.data)
}

export function deleteService(id: number) {
  return request.delete(`/services/${id}`)
}

export function updateServiceStatus(id: number, status: number) {
  return request.put(`/services/${id}/status`, { status })
}

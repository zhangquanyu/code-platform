import request from '@/utils/request'
import type { MicroserviceVO, MicroserviceSimpleVO, MicroserviceSummaryVO } from '@/types'
import type { PageResult } from '@/utils/request'

export interface MsPageQuery {
  pageNum?: number
  pageSize?: number
  keyword?: string
  status?: number
  applicationId?: string
}

export function pageMicroservices(params: MsPageQuery) {
  return request.get<any, { data: PageResult<MicroserviceVO> }>('/microservices', { params })
    .then(r => r.data)
}

export function listMicroservicesByApp(applicationId: string) {
  return request.get<any, { data: MicroserviceSimpleVO[] }>(
    `/microservices/by-application/${applicationId}`
  ).then(r => r.data)
}

export function getMicroservice(id: string) {
  return request.get<any, { data: MicroserviceVO }>(`/microservices/${id}`).then(r => r.data)
}

export function getMicroserviceSummary(id: string) {
  return request.get<any, { data: MicroserviceSummaryVO }>(`/microservices/${id}/summary`)
    .then(r => r.data)
}

export function createMicroservice(data: Partial<MicroserviceVO>) {
  return request.post<any, { data: MicroserviceVO }>('/microservices', data).then(r => r.data)
}

export function updateMicroservice(id: string, data: Partial<MicroserviceVO>) {
  return request.put<any, { data: MicroserviceVO }>(`/microservices/${id}`, data).then(r => r.data)
}

export function deleteMicroservice(id: string) {
  return request.delete(`/microservices/${id}`)
}

export function updateMicroserviceStatus(id: string, status: number) {
  return request.put(`/microservices/${id}/status`, { status })
}

import request from '@/utils/request'
import type {
  OrchestrationVO, OrchestrationDetailVO, OrchHealthVO,
  OrchNodeVO, OrchEdgeVO, OrchParamVO
} from '@/types'
import type { PageResult } from '@/utils/request'

/** 编排参数保存命令 */
export interface OrchParamCmd {
  id?: string | null
  paramName: string
  dataType: string
  isRequired?: number
  paramComment?: string
  sourceNodeKey?: string | null
  sourceField?: string | null
}

export interface OrchPageQuery {
  pageNum?: number
  pageSize?: number
  keyword?: string
  microserviceId?: string
  applicationId?: string
  status?: number
}

export function pageOrchestrations(params: OrchPageQuery) {
  return request.get<any, { data: PageResult<OrchestrationVO> }>('/orchestrations', { params })
    .then(r => r.data)
}

export function getOrchestration(id: string) {
  return request.get<any, { data: OrchestrationDetailVO }>(`/orchestrations/${id}`).then(r => r.data)
}

export function getOrchestrationHealth(id: string) {
  return request.get<any, { data: OrchHealthVO }>(`/orchestrations/${id}/health`).then(r => r.data)
}

export function createOrchestration(data: {
  microserviceId: string; name: string; code: string; description?: string
}) {
  return request.post<any, { data: OrchestrationDetailVO }>('/orchestrations', data).then(r => r.data)
}

export interface OrchSavePayload {
  name: string
  description?: string
  status?: number
  txType?: string
  txTimeout?: number
  inputParams?: OrchParamCmd[]
  outputParams?: OrchParamCmd[]
  nodes: Partial<OrchNodeVO>[]
  edges: Partial<OrchEdgeVO>[]
}

export function updateOrchestration(id: string, data: OrchSavePayload) {
  return request.put<any, { data: OrchestrationDetailVO }>(`/orchestrations/${id}`, data).then(r => r.data)
}

export function validateOrchestration(id: string, data: OrchSavePayload) {
  return request.post<any, { data: string[] }>(`/orchestrations/${id}/validate`, data).then(r => r.data)
}

export function debugOrchestration(id: string, inputData: Record<string, unknown>) {
  return request.post<any, { data: unknown }>(`/orchestrations/${id}/debug`, { inputData })
    .then(r => r.data)
}

export function deleteOrchestration(id: string) {
  return request.delete(`/orchestrations/${id}`)
}

export function updateOrchestrationStatus(id: string, status: number) {
  return request.put(`/orchestrations/${id}/status`, { status })
}

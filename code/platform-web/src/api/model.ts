import request from '@/utils/request'
import type { ModelVO, ModelFieldVO, ModelDetailVO, ModelSimpleVO } from '@/types'
import type { PageResult } from '@/utils/request'

export interface ModelPageQuery {
  pageNum?: number
  pageSize?: number
  keyword?: string
  microserviceId?: number
  applicationId?: number
}

export function pageModels(params: ModelPageQuery) {
  return request.get<any, { data: PageResult<ModelVO> }>('/models', { params }).then(r => r.data)
}

export function listModelsByMicroservice(microserviceId: number) {
  return request.get<any, { data: ModelSimpleVO[] }>(`/models/by-microservice/${microserviceId}`)
    .then(r => r.data)
}

export function getModel(id: number) {
  return request.get<any, { data: ModelDetailVO }>(`/models/${id}`).then(r => r.data)
}

export function createModel(data: { microserviceId: number; name: string; code: string; description?: string }) {
  return request.post<any, { data: ModelVO }>('/models', data).then(r => r.data)
}

export function updateModel(id: number, data: { name: string; description?: string }) {
  return request.put<any, { data: ModelVO }>(`/models/${id}`, data).then(r => r.data)
}

export function deleteModel(id: number) {
  return request.delete(`/models/${id}`)
}

export function batchSaveFields(modelId: number, data: {
  fields: Partial<ModelFieldVO>[]
  deletedFieldIds?: number[]
}) {
  return request.post<any, { data: ModelFieldVO[] }>(
    `/models/${modelId}/fields/batch-save`, data
  ).then(r => r.data)
}

import request from '@/utils/request'
import type { ModelVO, ModelFieldVO, ModelIndexVO, ModelDetailVO, ModelSimpleVO } from '@/types'
import type { PageResult } from '@/utils/request'

export interface ModelPageQuery {
  pageNum?: number
  pageSize?: number
  keyword?: string
  microserviceId?: string
  applicationId?: string
}

export function pageModels(params: ModelPageQuery) {
  return request.get<any, { data: PageResult<ModelVO> }>('/models', { params }).then(r => r.data)
}

export function listModelsByMicroservice(microserviceId: string) {
  return request.get<any, { data: ModelSimpleVO[] }>(`/models/by-microservice/${microserviceId}`)
    .then(r => r.data)
}

export function getModel(id: string) {
  return request.get<any, { data: ModelDetailVO }>(`/models/${id}`).then(r => r.data)
}

export function createModel(data: { microserviceId: string; name: string; code: string; description?: string }) {
  return request.post<any, { data: ModelVO }>('/models', data).then(r => r.data)
}

export function updateModel(id: string, data: { name: string; description?: string }) {
  return request.put<any, { data: ModelVO }>(`/models/${id}`, data).then(r => r.data)
}

export function deleteModel(id: string) {
  return request.delete(`/models/${id}`)
}

export function batchSaveFields(modelId: string, data: {
  fields: Partial<ModelFieldVO>[]
  deletedFieldIds?: string[]
}) {
  return request.post<any, { data: ModelFieldVO[] }>(
    `/models/${modelId}/fields/batch-save`, data
  ).then(r => r.data)
}

export function batchSaveIndexes(modelId: string, data: {
  indexes: Partial<ModelIndexVO>[]
  deletedIndexIds?: string[]
}) {
  return request.post<any, { data: ModelIndexVO[] }>(
    `/models/${modelId}/indexes/batch-save`, data
  ).then(r => r.data)
}

import request from '@/utils/request'
import type { MetadataVO, MetadataItemVO, MetadataDetailVO, MetadataRefVO, MetadataSimpleVO } from '@/types'
import type { PageResult } from '@/utils/request'

export interface MetaPageQuery {
  pageNum?: number
  pageSize?: number
  keyword?: string
  status?: number
  applicationId?: number
}

export function pageMetadata(params: MetaPageQuery) {
  return request.get<any, { data: PageResult<MetadataVO> }>('/metadata', { params }).then(r => r.data)
}

export function listMetadataByApp(applicationId: number) {
  return request.get<any, { data: MetadataSimpleVO[] }>(`/metadata/by-application/${applicationId}`)
    .then(r => r.data)
}

export function getMetadata(id: number) {
  return request.get<any, { data: MetadataDetailVO }>(`/metadata/${id}`).then(r => r.data)
}

export function listMetadataRefs(id: number) {
  return request.get<any, { data: MetadataRefVO[] }>(`/metadata/${id}/references`).then(r => r.data)
}

export function createMetadata(data: { applicationId: number; name: string; code: string; description?: string }) {
  return request.post<any, { data: MetadataVO }>('/metadata', data).then(r => r.data)
}

export function updateMetadata(id: number, data: { name: string; description?: string }) {
  return request.put<any, { data: MetadataVO }>(`/metadata/${id}`, data).then(r => r.data)
}

export function deleteMetadata(id: number) {
  return request.delete(`/metadata/${id}`)
}

export function updateMetadataStatus(id: number, status: number) {
  return request.put(`/metadata/${id}/status`, { status })
}

export function batchSaveItems(metadataId: number, data: {
  items: Partial<MetadataItemVO>[]
  deletedItemIds?: number[]
}) {
  return request.post<any, { data: MetadataItemVO[] }>(
    `/metadata/${metadataId}/items/batch-save`, data
  ).then(r => r.data)
}

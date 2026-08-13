import type { PageResult } from '@/utils/request'

export interface ApplicationVO {
  id: number
  name: string
  code: string
  version: string
  status: 0 | 1
  description: string | null
  createTime: string
  updateTime: string
}

export interface ApplicationSimpleVO {
  id: number
  name: string
  code: string
}

export interface MicroserviceVO {
  id: number
  applicationId: number
  applicationName: string
  name: string
  code: string
  version: string
  status: 0 | 1
  description: string | null
  createTime: string
}

export interface MicroserviceSimpleVO {
  id: number
  applicationId: number
  name: string
  code: string
}

export interface MicroserviceSummaryVO {
  modelCount: number
  serviceCount: number
  orchestrationCount: number
}

export interface ModelVO {
  id: number
  microserviceId: number
  microserviceName: string
  applicationId: number
  name: string
  code: string
  description: string | null
  fieldCount: number
  createTime: string
}

export interface ModelFieldVO {
  id: number | null
  modelId: number
  name: string
  displayName: string
  fieldType: string
  length: number | null
  precision: number | null
  isRequired: 0 | 1
  isPrimary: 0 | 1
  isUnique: 0 | 1
  isIndex: 0 | 1
  defaultValue: string | null
  metadataId: number | null
  metadataName: string | null
  sortOrder: number
  fieldComment: string | null
}

export interface ModelDetailVO {
  model: ModelVO
  fields: ModelFieldVO[]
}

export interface ModelSimpleVO {
  id: number
  microserviceId: number
  name: string
  code: string
}

export interface MetadataVO {
  id: number
  applicationId: number
  applicationName: string
  name: string
  code: string
  description: string | null
  status: 0 | 1
  itemCount: number
  createTime: string
}

export interface MetadataItemVO {
  id: number | null
  metadataId: number
  itemCode: string
  itemName: string
  itemValue: string | null
  sortOrder: number
  status: 0 | 1
}

export interface MetadataDetailVO {
  metadata: MetadataVO
  items: MetadataItemVO[]
  references: MetadataRefVO[]
}

export interface MetadataRefVO {
  modelId: number
  modelName: string
  fieldId: number
  fieldName: string
  displayName: string
  microserviceName: string
}

export interface MetadataSimpleVO {
  id: number
  applicationId: number
  name: string
  code: string
}

export interface ServiceVO {
  id: number
  microserviceId: number
  microserviceName: string
  name: string
  code: string
  description: string | null
  httpMethod: string
  servicePath: string
  category: string | null
  status: 0 | 1
  inputCount: number
  outputCount: number
  createTime: string
}

export interface ServiceParamVO {
  id: number | null
  serviceId: number
  paramType: number
  paramName: string
  dataType: string
  isRequired: 0 | 1
  defaultValue: string | null
  modelFieldId: number | null
  sortOrder: number
  paramComment: string | null
}

export interface ServiceDetailVO {
  service: ServiceVO
  inputs: ServiceParamVO[]
  outputs: ServiceParamVO[]
}

export interface ServiceSimpleVO {
  id: number
  microserviceId: number
  name: string
  code: string
  httpMethod: string
  servicePath: string
}

export interface OrchestrationVO {
  id: number
  microserviceId: number
  microserviceName: string
  applicationId: number
  name: string
  code: string
  description: string | null
  status: 0 | 1
  txType: string | null
  txTimeout: number | null
  nodeCount: number
  edgeCount: number
  createTime: string
}

export interface OrchNodeVO {
  id: number
  nodeKey: string
  nodeType: string
  nodeName: string | null
  serviceId: number | null
  serviceName: string | null
  configJson: string | null
  txType: string | null
  txTimeout: number | null
  retryCount: number | null
  retryInterval: number | null
  exceptionStrategy: string | null
  loopType: string | null
  branchExpr: string | null
  xPos: number | null
  yPos: number | null
  sortOrder: number
  /** 服务节点入参定义（后端填充） */
  serviceInputs?: ServiceParamVO[]
  /** 服务节点出参定义（后端填充） */
  serviceOutputs?: ServiceParamVO[]
  // 前端辅助字段（不持久化到后端，存在 configJson 中）
  appId?: number | null
  msId?: number | null
}

export interface OrchEdgeVO {
  id: number
  edgeKey: string
  fromNodeKey: string
  toNodeKey: string
  conditionExpr: string | null
  labelText: string | null
}

export interface OrchParamVO {
  id: number | null
  paramName: string
  dataType: string
  isRequired: number
  paramComment: string | null
  sourceNodeKey: string | null
  sourceField: string | null
}

export interface OrchestrationDetailVO {
  orchestration: OrchestrationVO
  inputParams: OrchParamVO[]
  outputParams: OrchParamVO[]
  nodes: OrchNodeVO[]
  edges: OrchEdgeVO[]
}

export interface OrchHealthVO {
  healthy: boolean
  alerts: { nodeKey: string; serviceId: number; reason: string }[]
}

export type { PageResult }

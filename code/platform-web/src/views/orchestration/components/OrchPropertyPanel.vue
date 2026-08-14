<template>
  <div class="prop-panel">
    <!-- 编排属性 -->
    <template v-if="!selectedNode && !selectedEdge">
      <div class="prop-title">编排属性</div>
      <el-form label-width="80px" size="small">
        <el-form-item label="编排名称">
          <el-input v-model="orchForm.name" @change="emitOrch" />
        </el-form-item>
        <el-form-item label="编排编码">
          <el-input :value="orchForm.code" disabled />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="orchForm.status" :active-value="1" :inactive-value="0" @change="emitOrch" />
        </el-form-item>
        <el-form-item label="事务类型">
          <el-select v-model="orchForm.txType" style="width:100%" @change="emitOrch">
            <el-option label="本地事务" value="LOCAL" />
            <el-option label="分布式事务" value="DISTRIBUTED" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="orchForm.txType === 'DISTRIBUTED'" label="超时(秒)">
          <el-input-number v-model="orchForm.txTimeout" :min="1" :max="3600" style="width:100%" @change="emitOrch" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="orchForm.description" type="textarea" :rows="2" @change="emitOrch" />
        </el-form-item>
      </el-form>
    </template>

    <!-- 节点属性 -->
    <template v-if="selectedNode">
      <div class="prop-title">
        节点属性
        <el-tag size="small" :type="nodeTagType">{{ selectedNode.nodeType }}</el-tag>
      </div>
      <el-form label-width="80px" size="small">
        <el-form-item label="节点Key">
          <el-input :value="selectedNode.nodeKey" disabled />
        </el-form-item>
        <el-form-item label="节点名称">
          <el-input v-model="selectedNode.nodeName" @change="emitNode" />
        </el-form-item>

        <template v-if="['SERVICE', 'ACTION'].includes(selectedNode.nodeType)">
          <el-divider content-position="left">服务选择</el-divider>
          <el-form-item label="所属应用">
            <el-select
              v-model="nodeAppId"
              filterable clearable placeholder="选择应用"
              style="width:100%"
              @change="onAppChange"
            >
              <el-option v-for="a in applications" :key="a.id" :label="a.name" :value="a.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="微服务">
            <el-select
              v-model="nodeMsId"
              filterable clearable placeholder="请先选择应用"
              style="width:100%"
              :disabled="!nodeAppId"
              @change="onMsChange"
            >
              <el-option v-for="m in nodeMsList" :key="m.id" :label="m.name" :value="m.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="服务">
            <el-select
              v-model="selectedNode.serviceId"
              filterable clearable placeholder="请先选择微服务"
              style="width:100%"
              :disabled="!nodeMsId"
              @change="onServiceChange"
            >
              <el-option v-for="s in nodeSvcList" :key="s.id" :label="s.name" :value="s.id" />
            </el-select>
          </el-form-item>

          <!-- 服务入参展示（只读） -->
          <el-divider content-position="left">服务入参</el-divider>
          <template v-if="selectedNode.serviceInputs && selectedNode.serviceInputs.length">
            <el-table :data="selectedNode.serviceInputs" border size="small" style="width:100%; margin-bottom:8px">
              <el-table-column prop="paramName" label="参数名" min-width="100" />
              <el-table-column prop="dataType" label="类型" width="80" />
              <el-table-column label="必填" width="60">
                <template #default="{ row }">{{ row.isRequired === 1 ? '是' : '否' }}</template>
              </el-table-column>
              <el-table-column prop="paramComment" label="备注" min-width="100" show-overflow-tooltip />
            </el-table>
          </template>
          <div v-else class="hint-text">请先选择服务</div>

          <!-- 入参映射配置 -->
          <el-divider content-position="left">入参映射</el-divider>
          <div v-if="paramMappings.length">
            <div v-for="m in paramMappings" :key="m.paramName" class="mapping-row">
              <div class="mapping-name" :title="m.paramName">{{ m.paramName }}</div>
              <el-select v-model="m.sourceType" size="small" style="width:130px" @change="onParamMappingChange">
                <el-option label="开始节点入参" value="START_INPUT" />
                <el-option label="固定值" value="FIXED" />
                <el-option label="上游节点出参" value="NODE_OUTPUT" />
              </el-select>
              <!-- 开始节点入参 -->
              <el-select
                v-if="m.sourceType === 'START_INPUT'"
                v-model="m.sourceField"
                size="small"
                placeholder="选择入参"
                style="flex:1; min-width:120px"
                @change="onParamMappingChange"
              >
                <el-option v-for="p in localInputParams" :key="p.paramName" :label="p.paramName" :value="p.paramName" />
              </el-select>
              <!-- 固定值 -->
              <el-input
                v-else-if="m.sourceType === 'FIXED'"
                v-model="m.fixedValue"
                size="small"
                placeholder="输入固定值"
                style="flex:1; min-width:120px"
                @change="onParamMappingChange"
              />
              <!-- 上游节点出参 -->
              <template v-else-if="m.sourceType === 'NODE_OUTPUT'">
                <el-select
                  v-model="m.sourceNodeKey"
                  size="small"
                  placeholder="选择节点"
                  style="flex:1; min-width:120px"
                  @change="onMappingSourceNodeChange(m)"
                >
                  <el-option v-for="n in upstreamNodes" :key="n.nodeKey" :label="n.nodeName || n.nodeKey" :value="n.nodeKey" />
                </el-select>
                <el-select
                  v-model="m.sourceField"
                  size="small"
                  placeholder="选择字段"
                  style="flex:1; min-width:120px"
                  :disabled="!m.sourceNodeKey"
                  @change="onParamMappingChange"
                >
                  <el-option v-for="f in getNodeOutputs(m.sourceNodeKey || '')" :key="f.paramName" :label="f.paramName" :value="f.paramName" />
                </el-select>
              </template>
            </div>
          </div>
          <div v-else class="hint-text">暂无可映射的入参</div>

          <!-- 服务出参展示（只读） -->
          <el-divider content-position="left">服务出参</el-divider>
          <template v-if="selectedNode.serviceOutputs && selectedNode.serviceOutputs.length">
            <el-table :data="selectedNode.serviceOutputs" border size="small" style="width:100%; margin-bottom:8px">
              <el-table-column prop="paramName" label="参数名" min-width="100" />
              <el-table-column prop="dataType" label="类型" width="80" />
              <el-table-column prop="paramComment" label="备注" min-width="100" show-overflow-tooltip />
            </el-table>
          </template>
          <div v-else class="hint-text">请先选择服务</div>
        </template>

        <el-divider content-position="left">事务配置</el-divider>
        <el-form-item label="事务类型">
          <el-select v-model="selectedNode.txType" style="width:100%" @change="emitNode">
            <el-option label="本地事务" value="LOCAL" />
            <el-option label="分布式事务" value="DISTRIBUTED" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="selectedNode.txType === 'DISTRIBUTED'" label="超时(秒)">
          <el-input-number v-model="selectedNode.txTimeout" :min="1" :max="3600" style="width:100%" @change="emitNode" />
        </el-form-item>

        <el-divider content-position="left">异常处理</el-divider>
        <el-form-item label="重试次数">
          <el-input-number v-model="selectedNode.retryCount" :min="0" :max="10" style="width:100%" @change="emitNode" />
        </el-form-item>
        <el-form-item v-if="(selectedNode.retryCount ?? 0) > 0" label="重试间隔">
          <el-input-number v-model="selectedNode.retryInterval" :min="100" :max="60000" :step="500" style="width:100%" @change="emitNode" />
        </el-form-item>
        <el-form-item label="异常策略">
          <el-select v-model="selectedNode.exceptionStrategy" style="width:100%" @change="emitNode">
            <el-option label="中断" value="INTERRUPT" />
            <el-option label="跳过" value="SKIP" />
            <el-option label="重试" value="RETRY" />
          </el-select>
        </el-form-item>

        <template v-if="selectedNode.nodeType === 'LOOP'">
          <el-divider content-position="left">循环配置</el-divider>
          <el-form-item label="循环类型">
            <el-select v-model="selectedNode.loopType" style="width:100%" @change="onLoopTypeChange">
              <el-option label="串行" value="SERIAL" />
              <el-option label="并行" value="PARALLEL" />
            </el-select>
          </el-form-item>
          <el-form-item label="集合表达式">
            <el-input v-model="loopCollection" placeholder="如 ${list} 或 $.data.items" @change="onLoopConfigChange" />
          </el-form-item>
          <el-form-item label="循环变量名">
            <el-input v-model="loopVar" placeholder="如 item" @change="onLoopConfigChange" />
          </el-form-item>
          <el-form-item label="索引变量名">
            <el-input v-model="loopIndex" placeholder="如 index" @change="onLoopConfigChange" />
          </el-form-item>
        </template>

        <template v-if="selectedNode.nodeType === 'BRANCH'">
          <el-divider content-position="left">分支配置</el-divider>
          <el-form-item label="分支表达式">
            <el-input v-model="selectedNode.branchExpr" type="textarea" :rows="2" placeholder="如: result.code == 200" @change="emitNode" />
          </el-form-item>
        </template>

        <el-divider content-position="left">高级配置</el-divider>
        <el-form-item label="配置JSON">
          <el-input v-model="selectedNode.configJson" type="textarea" :rows="3" placeholder="{}" @change="emitNode" />
        </el-form-item>
      </el-form>

      <!-- 开始节点：编排入参编辑器 -->
      <template v-if="selectedNode.nodeType === 'START'">
        <div class="sub-title">编排入参</div>
        <el-table :data="localInputParams" border size="small" style="width:100%">
          <el-table-column label="参数名" min-width="120">
            <template #default="{ row }">
              <el-input v-model="row.paramName" size="small" @change="onInputParamsChange" />
            </template>
          </el-table-column>
          <el-table-column label="数据类型" min-width="120">
            <template #default="{ row }">
              <el-select v-model="row.dataType" size="small" @change="onInputParamsChange">
                <el-option v-for="t in dataTypeOptions" :key="t" :label="t" :value="t" />
              </el-select>
            </template>
          </el-table-column>
          <el-table-column label="必填" width="70">
            <template #default="{ row }">
              <el-switch v-model="row.isRequired" :active-value="1" :inactive-value="0" @change="onInputParamsChange" />
            </template>
          </el-table-column>
          <el-table-column label="备注" min-width="120">
            <template #default="{ row }">
              <el-input v-model="row.paramComment" size="small" @change="onInputParamsChange" />
            </template>
          </el-table-column>
          <el-table-column label="操作" width="60">
            <template #default="{ $index }">
              <el-button type="danger" link size="small" @click="removeInputParam($index)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <div style="margin-top:8px">
          <el-button type="primary" plain size="small" @click="addInputParam">添加入参</el-button>
        </div>
      </template>

      <!-- 结束节点：编排出参编辑器 -->
      <template v-if="selectedNode.nodeType === 'END'">
        <div class="sub-title">编排出参</div>
        <el-table :data="localOutputParams" border size="small" style="width:100%">
          <el-table-column label="参数名" min-width="100">
            <template #default="{ row }">
              <el-input v-model="row.paramName" size="small" @change="onOutputParamsChange" />
            </template>
          </el-table-column>
          <el-table-column label="数据类型" min-width="100">
            <template #default="{ row }">
              <el-select v-model="row.dataType" size="small" @change="onOutputParamsChange">
                <el-option v-for="t in dataTypeOptions" :key="t" :label="t" :value="t" />
              </el-select>
            </template>
          </el-table-column>
          <el-table-column label="必填" width="60">
            <template #default="{ row }">
              <el-switch v-model="row.isRequired" :active-value="1" :inactive-value="0" @change="onOutputParamsChange" />
            </template>
          </el-table-column>
          <el-table-column label="来源节点" min-width="120">
            <template #default="{ row }">
              <el-select v-model="row.sourceNodeKey" size="small" placeholder="选择节点" @change="onOutputSourceNodeChange(row)">
                <el-option v-for="n in sourceNodesForOutput" :key="n.nodeKey" :label="n.nodeName || n.nodeKey" :value="n.nodeKey" />
              </el-select>
            </template>
          </el-table-column>
          <el-table-column label="来源字段" min-width="100">
            <template #default="{ row }">
              <el-select v-model="row.sourceField" size="small" placeholder="选择字段" :disabled="!row.sourceNodeKey" @change="onOutputParamsChange">
                <el-option v-for="f in getNodeOutputs(row.sourceNodeKey || '')" :key="f.paramName" :label="f.paramName" :value="f.paramName" />
              </el-select>
            </template>
          </el-table-column>
          <el-table-column label="备注" min-width="100">
            <template #default="{ row }">
              <el-input v-model="row.paramComment" size="small" @change="onOutputParamsChange" />
            </template>
          </el-table-column>
          <el-table-column label="操作" width="60">
            <template #default="{ $index }">
              <el-button type="danger" link size="small" @click="removeOutputParam($index)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <div style="margin-top:8px">
          <el-button type="primary" plain size="small" @click="addOutputParam">添加出参</el-button>
        </div>
      </template>

      <!-- 循环节点：循环体配置 -->
      <template v-if="selectedNode.nodeType === 'LOOP'">
        <div class="sub-title">循环体配置</div>
        <el-table :data="loopBody" border size="small" style="width:100%">
          <el-table-column label="节点名称" min-width="120">
            <template #default="{ row }">
              <el-input v-model="row.nodeName" size="small" @change="onLoopConfigChange" />
            </template>
          </el-table-column>
          <el-table-column label="服务ID" min-width="120">
            <template #default="{ row }">
              <el-input v-model="row.serviceId" size="small" placeholder="服务ID" @change="onLoopConfigChange" />
            </template>
          </el-table-column>
          <el-table-column label="操作" width="60">
            <template #default="{ $index }">
              <el-button type="danger" link size="small" @click="removeLoopBodyItem($index)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <div style="margin-top:8px">
          <el-button type="primary" plain size="small" @click="addLoopBodyItem">添加循环体节点</el-button>
        </div>
      </template>
    </template>

    <!-- 连线属性 -->
    <template v-if="selectedEdge">
      <div class="prop-title">连线属性</div>
      <el-form label-width="80px" size="small">
        <el-form-item label="连线Key">
          <el-input :value="selectedEdge.edgeKey" disabled />
        </el-form-item>
        <el-form-item label="起点">
          <el-input :value="selectedEdge.fromNodeKey" disabled />
        </el-form-item>
        <el-form-item label="终点">
          <el-input :value="selectedEdge.toNodeKey" disabled />
        </el-form-item>
        <el-form-item label="标签">
          <el-input v-model="selectedEdge.labelText" @change="emitEdge" />
        </el-form-item>
        <el-form-item label="条件表达式">
          <el-input v-model="selectedEdge.conditionExpr" type="textarea" :rows="2" placeholder="如: result.success == true" @change="emitEdge" />
        </el-form-item>
      </el-form>
    </template>

    <div v-if="!selectedNode && !selectedEdge && !orchForm.name" class="empty-prop">
      <p>选择节点或连线查看属性</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import type {
  OrchNodeVO, OrchEdgeVO, ApplicationSimpleVO, MicroserviceSimpleVO,
  ServiceSimpleVO, ServiceParamVO, OrchParamVO
} from '@/types'
import { listMicroservicesByApp } from '@/api/microservice'
import { listServicesByMicroservice } from '@/api/service'

const props = defineProps<{
  orchName: string
  orchCode: string
  orchStatus: number
  orchTxType: string
  orchTxTimeout: number
  orchDescription: string
  selectedNode: OrchNodeVO | null
  selectedEdge: OrchEdgeVO | null
  applications: ApplicationSimpleVO[]
  // 编排级入参（开始节点配置）
  inputParams: OrchParamVO[]
  // 编排出参（结束节点配置）
  outputParams: OrchParamVO[]
  // 所有节点（用于服务节点映射选择上游节点、结束节点选择来源节点）
  allNodes: OrchNodeVO[]
}>()

const emit = defineEmits<{
  (e: 'update-orch', data: { name: string; status: number; txType: string; txTimeout: number; description: string }): void
  (e: 'update-node', node: OrchNodeVO): void
  (e: 'update-edge', edge: OrchEdgeVO): void
  (e: 'update-input-params', params: OrchParamVO[]): void
  (e: 'update-output-params', params: OrchParamVO[]): void
}>()

const orchForm = computed(() => ({
  name: props.orchName,
  code: props.orchCode,
  status: props.orchStatus,
  txType: props.orchTxType || 'LOCAL',
  txTimeout: props.orchTxTimeout || 300,
  description: props.orchDescription || ''
}))

const nodeTagType = computed(() => {
  const t = props.selectedNode?.nodeType
  if (t === 'START') return 'success'
  if (t === 'END') return 'danger'
  if (t === 'CONDITION') return 'warning'
  return 'primary'
})

// 数据类型可选项
const dataTypeOptions = ['STRING', 'INTEGER', 'DECIMAL', 'BOOLEAN', 'DATE', 'JSON']

// 入参映射结构
interface ParamMapping {
  paramName: string
  sourceType: 'START_INPUT' | 'FIXED' | 'NODE_OUTPUT'
  sourceField?: string | null
  fixedValue?: string | null
  sourceNodeKey?: string | null
}

// 循环体节点结构
interface LoopBodyItem {
  nodeKey: string
  nodeName: string
  serviceId: string | null
  paramMappings: ParamMapping[]
}

// --- 本地副本（不直接修改 props） ---
// 开始节点：编排入参编辑器
const localInputParams = ref<OrchParamVO[]>([])
// 结束节点：编排出参编辑器
const localOutputParams = ref<OrchParamVO[]>([])
// 服务节点：入参映射
const paramMappings = ref<ParamMapping[]>([])
// 循环节点：循环配置
const loopCollection = ref('')
const loopVar = ref('')
const loopIndex = ref('')
const loopBody = ref<LoopBodyItem[]>([])

// --- 节点服务选择：应用 → 微服务 → 服务 三级联动 ---
const nodeAppId = ref<string | null>(null)
const nodeMsId = ref<string | null>(null)
const nodeMsList = ref<MicroserviceSimpleVO[]>([])
const nodeSvcList = ref<ServiceSimpleVO[]>([])

// 上游节点（SERVICE/ACTION，排除当前节点），用于入参映射 NODE_OUTPUT 选择
const upstreamNodes = computed(() => {
  const curKey = props.selectedNode?.nodeKey
  return props.allNodes.filter(n =>
    ['SERVICE', 'ACTION'].includes(n.nodeType) && n.nodeKey !== curKey
  )
})

// 出参来源节点（所有 SERVICE/ACTION 节点），用于结束节点出参选择
const sourceNodesForOutput = computed(() => {
  return props.allNodes.filter(n => ['SERVICE', 'ACTION'].includes(n.nodeType))
})

// 同步 props.inputParams 到本地 ref
watch(() => props.inputParams, (val) => {
  localInputParams.value = (val || []).map(p => ({ ...p }))
}, { immediate: true, deep: true })

// 同步 props.outputParams 到本地 ref
watch(() => props.outputParams, (val) => {
  localOutputParams.value = (val || []).map(p => ({ ...p }))
}, { immediate: true, deep: true })

// 当选中的节点变化时，初始化 app/ms 状态并从 configJson 解析映射数据
watch(() => props.selectedNode, async (node) => {
  // 重置本地映射/循环状态
  paramMappings.value = []
  loopCollection.value = ''
  loopVar.value = ''
  loopIndex.value = ''
  loopBody.value = []

  if (!node) {
    nodeAppId.value = null
    nodeMsId.value = null
    nodeMsList.value = []
    nodeSvcList.value = []
    return
  }

  // 解析 configJson 一次
  let cfg: Record<string, any> = {}
  if (node.configJson) {
    try { cfg = JSON.parse(node.configJson) } catch { cfg = {} }
  }

  if (['SERVICE', 'ACTION'].includes(node.nodeType)) {
    // 从 configJson 中恢复 appId/msId
    const savedAppId = cfg._appId ?? null
    const savedMsId = cfg._msId ?? null
    nodeAppId.value = savedAppId
    nodeMsId.value = savedMsId

    if (savedAppId) {
      try {
        nodeMsList.value = await listMicroservicesByApp(savedAppId)
      } catch { nodeMsList.value = [] }
    } else {
      nodeMsList.value = []
    }

    if (savedMsId) {
      try {
        nodeSvcList.value = await listServicesByMicroservice(savedMsId)
      } catch { nodeSvcList.value = [] }
    } else {
      nodeSvcList.value = []
    }

    // 从 configJson 解析入参映射
    paramMappings.value = Array.isArray(cfg.paramMappings) ? cfg.paramMappings as ParamMapping[] : []
  } else {
    nodeAppId.value = null
    nodeMsId.value = null
    nodeMsList.value = []
    nodeSvcList.value = []
  }

  // 循环节点：从 configJson 解析循环配置
  if (node.nodeType === 'LOOP') {
    loopCollection.value = cfg.loopCollection || ''
    loopVar.value = cfg.loopVar || ''
    loopIndex.value = cfg.loopIndex || ''
    loopBody.value = Array.isArray(cfg.loopBody) ? cfg.loopBody as LoopBodyItem[] : []
  }
})

// 监听服务入参变化，保持 paramMappings 与 serviceInputs 同步
watch(() => props.selectedNode?.serviceInputs, (inputs) => {
  const node = props.selectedNode
  if (!node || !['SERVICE', 'ACTION'].includes(node.nodeType)) return
  const list = inputs || []
  const existing = new Map(paramMappings.value.map(m => [m.paramName, m]))
  paramMappings.value = list.map(inp => {
    const ex = existing.get(inp.paramName)
    if (ex) return ex
    return {
      paramName: inp.paramName,
      sourceType: 'START_INPUT',
      sourceField: null,
      fixedValue: null,
      sourceNodeKey: null
    } as ParamMapping
  })
  saveParamMappingsToConfig()
}, { deep: true })

// 根据节点Key获取该节点的服务出参列表
function getNodeOutputs(nodeKey: string): ServiceParamVO[] {
  if (!nodeKey) return []
  const n = props.allNodes.find(x => x.nodeKey === nodeKey)
  return n?.serviceOutputs || []
}

async function onAppChange(appId: string | null) {
  nodeMsId.value = null
  nodeSvcList.value = []
  if (props.selectedNode) {
    props.selectedNode.serviceId = null
    props.selectedNode.serviceName = null
  }
  if (appId) {
    try {
      nodeMsList.value = await listMicroservicesByApp(appId)
    } catch { nodeMsList.value = [] }
  } else {
    nodeMsList.value = []
  }
  saveAppMsToConfig()
  emitNode()
}

async function onMsChange(msId: string | null) {
  if (props.selectedNode) {
    props.selectedNode.serviceId = null
    props.selectedNode.serviceName = null
  }
  if (msId) {
    try {
      nodeSvcList.value = await listServicesByMicroservice(msId)
    } catch { nodeSvcList.value = [] }
  } else {
    nodeSvcList.value = []
  }
  saveAppMsToConfig()
  emitNode()
}

function onServiceChange(serviceId: string | null) {
  if (!props.selectedNode) return
  const svc = nodeSvcList.value.find(s => s.id === serviceId)
  if (svc) {
    props.selectedNode.serviceName = svc.name
  } else {
    props.selectedNode.serviceName = null
  }
  saveAppMsToConfig()
  emitNode()
}

// 将 appId/msId 保存到 configJson
function saveAppMsToConfig() {
  if (!props.selectedNode) return
  let cfg: Record<string, unknown> = {}
  if (props.selectedNode.configJson) {
    try { cfg = JSON.parse(props.selectedNode.configJson) } catch { cfg = {} }
  }
  cfg._appId = nodeAppId.value
  cfg._msId = nodeMsId.value
  props.selectedNode.configJson = JSON.stringify(cfg)
}

// 将入参映射保存到 configJson
function saveParamMappingsToConfig() {
  if (!props.selectedNode) return
  let cfg: Record<string, unknown> = {}
  if (props.selectedNode.configJson) {
    try { cfg = JSON.parse(props.selectedNode.configJson) } catch { cfg = {} }
  }
  cfg.paramMappings = paramMappings.value
  props.selectedNode.configJson = JSON.stringify(cfg)
}

// 将出参映射保存到结束节点 configJson
function saveOutputMappingsToConfig() {
  if (!props.selectedNode || props.selectedNode.nodeType !== 'END') return
  let cfg: Record<string, unknown> = {}
  if (props.selectedNode.configJson) {
    try { cfg = JSON.parse(props.selectedNode.configJson) } catch { cfg = {} }
  }
  cfg.outputMappings = localOutputParams.value
  props.selectedNode.configJson = JSON.stringify(cfg)
}

// 将循环配置保存到 configJson
function saveLoopConfigToConfig() {
  if (!props.selectedNode) return
  let cfg: Record<string, unknown> = {}
  if (props.selectedNode.configJson) {
    try { cfg = JSON.parse(props.selectedNode.configJson) } catch { cfg = {} }
  }
  if (props.selectedNode.nodeType === 'LOOP') {
    cfg.loopType = props.selectedNode.loopType
    cfg.loopCollection = loopCollection.value
    cfg.loopVar = loopVar.value
    cfg.loopIndex = loopIndex.value
    cfg.loopBody = loopBody.value
  }
  props.selectedNode.configJson = JSON.stringify(cfg)
}

// --- 开始节点：入参编辑器 ---
function addInputParam() {
  localInputParams.value.push({
    id: null,
    paramName: '',
    dataType: 'STRING',
    isRequired: 0,
    paramComment: null,
    sourceNodeKey: null,
    sourceField: null
  })
  onInputParamsChange()
}

function removeInputParam(idx: number) {
  localInputParams.value.splice(idx, 1)
  onInputParamsChange()
}

function onInputParamsChange() {
  emit('update-input-params', localInputParams.value.map(p => ({ ...p })))
}

// --- 服务节点：入参映射 ---
function onParamMappingChange() {
  saveParamMappingsToConfig()
  emitNode()
}

function onMappingSourceNodeChange(m: ParamMapping) {
  m.sourceField = null
  onParamMappingChange()
}

// --- 结束节点：出参编辑器 ---
function addOutputParam() {
  localOutputParams.value.push({
    id: null,
    paramName: '',
    dataType: 'STRING',
    isRequired: 0,
    paramComment: null,
    sourceNodeKey: null,
    sourceField: null
  })
  onOutputParamsChange()
}

function removeOutputParam(idx: number) {
  localOutputParams.value.splice(idx, 1)
  onOutputParamsChange()
}

function onOutputParamsChange() {
  emit('update-output-params', localOutputParams.value.map(p => ({ ...p })))
  saveOutputMappingsToConfig()
  if (props.selectedNode && props.selectedNode.nodeType === 'END') {
    emitNode()
  }
}

function onOutputSourceNodeChange(row: OrchParamVO) {
  row.sourceField = null
  onOutputParamsChange()
}

// --- 循环节点：循环体配置 ---
function onLoopTypeChange() {
  saveLoopConfigToConfig()
  emitNode()
}

function onLoopConfigChange() {
  saveLoopConfigToConfig()
  emitNode()
}

function addLoopBodyItem() {
  loopBody.value.push({
    nodeKey: `loop_${Date.now()}`,
    nodeName: '',
    serviceId: null,
    paramMappings: []
  })
  onLoopConfigChange()
}

function removeLoopBodyItem(idx: number) {
  loopBody.value.splice(idx, 1)
  onLoopConfigChange()
}

function emitOrch() {
  emit('update-orch', {
    name: orchForm.value.name,
    status: orchForm.value.status,
    txType: orchForm.value.txType,
    txTimeout: orchForm.value.txTimeout,
    description: orchForm.value.description
  })
}

function emitNode() {
  if (props.selectedNode) {
    props.selectedNode.appId = nodeAppId.value
    props.selectedNode.msId = nodeMsId.value
    emit('update-node', { ...props.selectedNode })
  }
}

function emitEdge() {
  if (props.selectedEdge) emit('update-edge', { ...props.selectedEdge })
}
</script>

<style scoped>
.prop-panel {
  height: 100%; overflow-y: auto;
  padding: 12px; background: #fff;
}
.prop-title {
  font-weight: 600; margin-bottom: 16px;
  padding-bottom: 8px; border-bottom: 1px solid #ebeef5;
  display: flex; align-items: center; gap: 8px;
}
.sub-title {
  font-weight: 600; font-size: 13px;
  margin: 16px 0 8px; color: #303133;
}
.empty-prop {
  color: #c0c4cc; font-size: 13px;
  text-align: center; margin-top: 40px;
}
.hint-text {
  color: #c0c4cc; font-size: 12px; padding: 8px 0;
}
.mapping-row {
  display: flex; align-items: center; gap: 8px;
  margin-bottom: 8px; flex-wrap: wrap;
}
.mapping-name {
  width: 80px; font-size: 12px; color: #606266;
  flex-shrink: 0;
  overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
}
:deep(.el-divider__text) {
  font-size: 12px; color: #909399;
}
</style>

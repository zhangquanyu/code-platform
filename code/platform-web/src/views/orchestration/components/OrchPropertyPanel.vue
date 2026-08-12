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
            <el-select v-model="selectedNode.loopType" style="width:100%" @change="emitNode">
              <el-option label="串行" value="SERIAL" />
              <el-option label="并行" value="PARALLEL" />
            </el-select>
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
import type { OrchNodeVO, OrchEdgeVO, ApplicationSimpleVO, MicroserviceSimpleVO, ServiceSimpleVO } from '@/types'
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
}>()

const emit = defineEmits<{
  (e: 'update-orch', data: { name: string; status: number; txType: string; txTimeout: number; description: string }): void
  (e: 'update-node', node: OrchNodeVO): void
  (e: 'update-edge', edge: OrchEdgeVO): void
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

// --- 节点服务选择：应用 → 微服务 → 服务 三级联动 ---
const nodeAppId = ref<number | null>(null)
const nodeMsId = ref<number | null>(null)
const nodeMsList = ref<MicroserviceSimpleVO[]>([])
const nodeSvcList = ref<ServiceSimpleVO[]>([])

// 当选中的节点变化时，初始化 app/ms 状态
watch(() => props.selectedNode, async (node) => {
  if (!node || !['SERVICE', 'ACTION'].includes(node.nodeType)) {
    nodeAppId.value = null
    nodeMsId.value = null
    nodeMsList.value = []
    nodeSvcList.value = []
    return
  }
  // 从 configJson 中恢复 appId/msId
  let savedAppId: number | null = null
  let savedMsId: number | null = null
  if (node.configJson) {
    try {
      const cfg = JSON.parse(node.configJson)
      savedAppId = cfg._appId ?? null
      savedMsId = cfg._msId ?? null
    } catch { /* ignore */ }
  }
  nodeAppId.value = savedAppId
  nodeMsId.value = savedMsId

  // 如果有保存的 appId，加载微服务列表
  if (savedAppId) {
    try {
      nodeMsList.value = await listMicroservicesByApp(savedAppId)
    } catch { nodeMsList.value = [] }
  } else {
    nodeMsList.value = []
  }

  // 如果有保存的 msId，加载服务列表
  if (savedMsId) {
    try {
      nodeSvcList.value = await listServicesByMicroservice(savedMsId)
    } catch { nodeSvcList.value = [] }
  } else {
    nodeSvcList.value = []
  }
})

async function onAppChange(appId: number | null) {
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

async function onMsChange(msId: number | null) {
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

function onServiceChange(serviceId: number | null) {
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
.empty-prop {
  color: #c0c4cc; font-size: 13px;
  text-align: center; margin-top: 40px;
}
:deep(.el-divider__text) {
  font-size: 12px; color: #909399;
}
</style>

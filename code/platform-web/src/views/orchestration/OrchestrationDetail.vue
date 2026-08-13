<template>
  <div class="orch-detail">
    <!-- 顶部操作栏 -->
    <div class="detail-header">
      <div class="header-left">
        <el-button link @click="$router.push('/orchestrations')">
          <el-icon><ArrowLeft /></el-icon> 返回列表
        </el-button>
        <span class="title">{{ orchData?.orchestration?.name || '编排详情' }}</span>
      </div>
      <div class="header-right">
        <el-button @click="onValidate" :loading="validating">校验</el-button>
        <el-button type="primary" @click="onSave" :loading="saving">保存</el-button>
      </div>
    </div>

    <!-- 三栏布局 -->
    <div class="detail-body" v-loading="loading">
      <div class="col-palette">
        <OrchNodePalette />
      </div>
      <div class="col-canvas">
        <OrchCanvas
          v-if="orchData"
          ref="canvasRef"
          :nodes="nodes"
          :edges="edges"
          @update:nodes="onUpdateNodes"
          @update:edges="onUpdateEdges"
          @select-node="onSelectNode"
          @select-edge="onSelectEdge"
        />
      </div>
      <div class="col-panel">
        <OrchPropertyPanel
          :orch-name="orchForm.name"
          :orch-code="orchForm.code"
          :orch-status="orchForm.status"
          :orch-tx-type="orchForm.txType"
          :orch-tx-timeout="orchForm.txTimeout"
          :orch-description="orchForm.description"
          :selected-node="selectedNode"
          :selected-edge="selectedEdge"
          :applications="applications"
          :input-params="inputParams"
          :output-params="outputParams"
          :all-nodes="nodes"
          @update-orch="onUpdateOrch"
          @update-node="onUpdateNode"
          @update-edge="onUpdateEdge"
          @update-input-params="(p: OrchParamVO[]) => inputParams = p"
          @update-output-params="(p: OrchParamVO[]) => outputParams = p"
        />
      </div>
    </div>

    <!-- 调试弹窗 -->
    <el-dialog v-model="debugVisible" title="调试编排" width="700px">
      <el-form label-width="80px">
        <el-form-item label="入参JSON">
          <el-input v-model="debugInput" type="textarea" :rows="6" placeholder='{"key": "value"}' />
        </el-form-item>
        <el-form-item v-if="debugResult" label="结果">
          <pre class="debug-result">{{ JSON.stringify(debugResult, null, 2) }}</pre>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="debugVisible = false">关闭</el-button>
        <el-button type="primary" @click="onDebug" :loading="debugging">执行调试</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, nextTick } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import OrchNodePalette from './components/OrchNodePalette.vue'
import OrchCanvas from './components/OrchCanvas.vue'
import OrchPropertyPanel from './components/OrchPropertyPanel.vue'
import {
  getOrchestration, updateOrchestration, validateOrchestration,
  debugOrchestration, type OrchSavePayload, type OrchParamCmd
} from '@/api/orchestration'
import { listSimpleApplications } from '@/api/application'
import type { OrchNodeVO, OrchEdgeVO, OrchestrationDetailVO, ApplicationSimpleVO, OrchParamVO } from '@/types'

const route = useRoute()
const orchId = Number(route.params.id)

const loading = ref(false)
const saving = ref(false)
const validating = ref(false)
const debugging = ref(false)
const debugVisible = ref(false)
const debugInput = ref('{}')
const debugResult = ref<unknown>(null)

const canvasRef = ref<InstanceType<typeof OrchCanvas>>()

const orchData = ref<OrchestrationDetailVO | null>(null)
const nodes = ref<OrchNodeVO[]>([])
const edges = ref<OrchEdgeVO[]>([])
const inputParams = ref<OrchParamVO[]>([])
const outputParams = ref<OrchParamVO[]>([])
const applications = ref<ApplicationSimpleVO[]>([])

const orchForm = reactive({
  name: '', code: '', status: 1 as number,
  txType: 'LOCAL', txTimeout: 300, description: ''
})

const selectedNode = ref<OrchNodeVO | null>(null)
const selectedEdge = ref<OrchEdgeVO | null>(null)

async function load() {
  loading.value = true
  try {
    const data = await getOrchestration(orchId)
    orchData.value = data
    const orch = data.orchestration
    orchForm.name = orch.name
    orchForm.code = orch.code
    orchForm.status = orch.status
    orchForm.txType = orch.txType || 'LOCAL'
    orchForm.txTimeout = orch.txTimeout || 300
    orchForm.description = orch.description || ''
    nodes.value = data.nodes || []
    edges.value = data.edges || []
    inputParams.value = data.inputParams || []
    outputParams.value = data.outputParams || []

    // 加载应用列表（供节点属性面板跨应用选择服务）
    try {
      applications.value = await listSimpleApplications()
    } catch { applications.value = [] }

    // 检查节点位置是否需要自动排版（仅当所有节点位置都为 null/undefined 时才触发）
    // 注意：不能用 !n.xPos，因为 xPos=0 时也会判定为需要排版
    const needAutoLayout = nodes.value.length > 0 && nodes.value.every(n => n.xPos == null && n.yPos == null)
    if (needAutoLayout) {
      // 等待画布初始化完成后执行自动排版
      await nextTick()
      setTimeout(() => {
        canvasRef.value?.autoLayout?.()
        // 排版后保存新位置
        setTimeout(() => {
          canvasRef.value?.forceSyncNow?.()
        }, 500)
      }, 300)
    }
  } finally { loading.value = false }
}

// --- 事件处理 ---
function onUpdateNodes(n: OrchNodeVO[]) { nodes.value = n }
function onUpdateEdges(e: OrchEdgeVO[]) { edges.value = e }
function onSelectNode(n: OrchNodeVO | null) { selectedNode.value = n }
function onSelectEdge(e: OrchEdgeVO | null) { selectedEdge.value = e }

function onUpdateOrch(data: { name: string; status: number; txType: string; txTimeout: number; description: string }) {
  orchForm.name = data.name
  orchForm.status = data.status
  orchForm.txType = data.txType
  orchForm.txTimeout = data.txTimeout
  orchForm.description = data.description
}

function onUpdateNode(node: OrchNodeVO) {
  const idx = nodes.value.findIndex(n => n.nodeKey === node.nodeKey)
  if (idx >= 0) {
    nodes.value[idx] = { ...node }
    selectedNode.value = { ...node }
  }
}

function onUpdateEdge(edge: OrchEdgeVO) {
  const idx = edges.value.findIndex(e => e.edgeKey === edge.edgeKey)
  if (idx >= 0) {
    edges.value[idx] = { ...edge }
    selectedEdge.value = { ...edge }
  }
}

function buildPayload(): OrchSavePayload {
  return {
    name: orchForm.name,
    description: orchForm.description,
    status: orchForm.status,
    txType: orchForm.txType,
    txTimeout: orchForm.txTimeout,
    inputParams: inputParams.value.map(p => ({
      id: p.id,
      paramName: p.paramName,
      dataType: p.dataType,
      isRequired: p.isRequired,
      paramComment: p.paramComment,
      sourceNodeKey: p.sourceNodeKey,
      sourceField: p.sourceField
    } as OrchParamCmd)),
    outputParams: outputParams.value.map(p => ({
      id: p.id,
      paramName: p.paramName,
      dataType: p.dataType,
      isRequired: p.isRequired,
      paramComment: p.paramComment,
      sourceNodeKey: p.sourceNodeKey,
      sourceField: p.sourceField
    } as OrchParamCmd)),
    nodes: nodes.value.map(n => ({ ...n })),
    edges: edges.value.map(e => ({ ...e }))
  }
}

async function onSave() {
  // 强制同步画布位置数据到 nodes/edges
  canvasRef.value?.forceSyncNow?.()
  await nextTick()
  saving.value = true
  try {
    await updateOrchestration(orchId, buildPayload())
    ElMessage.success('保存成功')
    selectedNode.value = null
    selectedEdge.value = null
  } catch {
    // 错误已在拦截器提示
  } finally { saving.value = false }
}

async function onValidate() {
  canvasRef.value?.forceSyncNow?.()
  await nextTick()
  validating.value = true
  try {
    const errors = await validateOrchestration(orchId, buildPayload())
    if (errors && errors.length > 0) {
      ElMessage.error(errors.join('; '))
    } else {
      ElMessage.success('校验通过')
    }
  } catch {
    // 错误已在拦截器提示
  } finally { validating.value = false }
}

async function onDebug() {
  debugging.value = true
  try {
    let inputData = {}
    try { inputData = JSON.parse(debugInput.value || '{}') }
    catch { ElMessage.error('入参JSON格式错误'); return }
    debugResult.value = await debugOrchestration(orchId, inputData)
  } catch {
    // 错误已在拦截器提示
  } finally { debugging.value = false }
}

onMounted(load)
</script>

<style scoped>
.orch-detail { display: flex; flex-direction: column; height: 100%; }
.detail-header {
  display: flex; align-items: center; justify-content: space-between;
  padding: 8px 16px; background: #fff; border-bottom: 1px solid #ebeef5;
}
.header-left { display: flex; align-items: center; gap: 12px; }
.header-left .title { font-size: 16px; font-weight: 600; }
.header-right { display: flex; gap: 8px; }

.detail-body {
  flex: 1; display: flex; overflow: hidden;
}
.col-palette {
  width: 200px; flex-shrink: 0;
  border-right: 1px solid #ebeef5;
  background: #fff; overflow-y: auto;
}
.col-canvas {
  flex: 1; overflow: hidden;
}
.col-panel {
  width: 320px; flex-shrink: 0;
  border-left: 1px solid #ebeef5;
  background: #fff; overflow-y: auto;
}
.debug-result {
  background: #f5f7fa; padding: 12px; border-radius: 4px;
  font-size: 12px; max-height: 300px; overflow: auto; margin: 0;
}
</style>

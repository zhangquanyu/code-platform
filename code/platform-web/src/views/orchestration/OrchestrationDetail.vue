<template>
  <div class="page" v-loading="loading">
    <el-page-header @back="$router.back()" :content="detail.orchestration?.name || '编排详情'">
      <template #extra>
        <el-button @click="onValidate">校验</el-button>
        <el-button type="primary" :loading="saving" @click="onSave">保存</el-button>
        <el-button type="success" @click="debugVisible = true">调试</el-button>
      </template>
    </el-page-header>

    <el-card class="info-card" v-if="detail.orchestration?.id">
      <el-form :model="form" label-width="80px" inline>
        <el-form-item label="名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="编码"><el-input v-model="form.code" disabled /></el-form-item>
        <el-form-item label="描述"><el-input v-model="form.description" style="width:300px" /></el-form-item>
        <el-form-item label="状态">
          <el-tag :type="health.healthy ? 'success' : 'danger'">
            {{ health.healthy ? '健康' : '异常' }}
          </el-tag>
        </el-form-item>
      </el-form>
    </el-card>

    <el-row :gutter="12" class="flow-area">
      <el-col :span="5">
        <el-card>
          <template #header><span>节点库</span></template>
          <div class="palette">
            <div v-for="t in nodeTypes" :key="t.type" class="palette-item" @click="addNode(t.type)">
              <el-tag :type="t.tag">{{ t.label }}</el-tag>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="13">
        <el-card>
          <template #header><span>流程画布（节点/连线列表）</span></template>
          <div class="canvas-tip">
            <el-alert type="info" :closable="false" show-icon
              title="本期为简化版画布，通过下方列表管理节点与连线。点击节点可在右侧配置属性。" />
          </div>
          <el-table :data="nodes" border size="small" style="margin-top:8px" @row-click="onSelectNode">
            <el-table-column label="节点Key" prop="nodeKey" width="130" />
            <el-table-column label="类型" width="100">
              <template #default="{ row }">
                <el-tag size="small" :type="typeTag(row.nodeType)">{{ row.nodeType }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="名称" prop="nodeName" />
            <el-table-column label="调用服务" prop="serviceName" />
            <el-table-column label="操作" width="80">
              <template #default="{ $index }">
                <el-button link type="danger" size="small" @click.stop="removeNode($index)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>

          <div class="edges-title">连线管理</div>
          <el-table :data="edges" border size="small">
            <el-table-column label="连线Key" prop="edgeKey" width="130" />
            <el-table-column label="起点" prop="fromNodeKey" width="130" />
            <el-table-column label="终点" prop="toNodeKey" width="130" />
            <el-table-column label="条件" prop="conditionExpr" />
            <el-table-column label="操作" width="80">
              <template #default="{ $index }">
                <el-button link type="danger" size="small" @click="edges.splice($index, 1)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-button size="small" type="primary" plain style="margin-top:8px" @click="addEdge">新增连线</el-button>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card>
          <template #header><span>节点属性</span></template>
          <div v-if="!selectedNode" class="empty-tip">请选择一个节点</div>
          <el-form v-else label-width="90px" size="small">
            <el-form-item label="节点Key"><el-input v-model="selectedNode.nodeKey" disabled /></el-form-item>
            <el-form-item label="节点类型">
              <el-tag :type="typeTag(selectedNode.nodeType)">{{ selectedNode.nodeType }}</el-tag>
            </el-form-item>
            <el-form-item label="节点名称"><el-input v-model="selectedNode.nodeName" /></el-form-item>
            <el-form-item v-if="selectedNode.nodeType === 'SERVICE'" label="调用服务">
              <el-select v-model="selectedNode.serviceId" placeholder="选择服务" filterable clearable style="width:100%">
                <el-option v-for="s in serviceList" :key="s.id" :label="s.name" :value="s.id" />
              </el-select>
            </el-form-item>
            <el-form-item label="X坐标"><el-input-number v-model="selectedNode.xPos" :min="0" controls-position="right" style="width:120px" /></el-form-item>
            <el-form-item label="Y坐标"><el-input-number v-model="selectedNode.yPos" :min="0" controls-position="right" style="width:120px" /></el-form-item>
            <el-form-item label="排序"><el-input-number v-model="selectedNode.sortOrder" :min="0" controls-position="right" style="width:120px" /></el-form-item>
            <el-form-item label="配置JSON">
              <el-input v-model="selectedNode.configJson" type="textarea" :rows="3" placeholder='{"mapping":{}}' />
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
    </el-row>

    <el-dialog v-model="edgeDialogVisible" title="新增连线" width="420px">
      <el-form label-width="80px">
        <el-form-item label="连线Key">
          <el-input v-model="newEdge.edgeKey" placeholder="如 e1" />
        </el-form-item>
        <el-form-item label="起点">
          <el-select v-model="newEdge.fromNodeKey" style="width:100%">
            <el-option v-for="n in nodes" :key="n.nodeKey" :label="n.nodeKey" :value="n.nodeKey" />
          </el-select>
        </el-form-item>
        <el-form-item label="终点">
          <el-select v-model="newEdge.toNodeKey" style="width:100%">
            <el-option v-for="n in nodes" :key="n.nodeKey" :label="n.nodeKey" :value="n.nodeKey" />
          </el-select>
        </el-form-item>
        <el-form-item label="条件"><el-input v-model="newEdge.conditionExpr" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="edgeDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmAddEdge">添加</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="debugVisible" title="编排调试" width="640px">
      <el-form label-width="80px">
        <el-form-item label="入参JSON">
          <el-input v-model="debugInput" type="textarea" :rows="6" placeholder='{"userId": 1001}' />
        </el-form-item>
      </el-form>
      <el-button type="primary" :loading="debugging" @click="onDebug">执行</el-button>
      <el-divider />
      <div v-if="debugResult">
        <el-tag :type="debugResult.success ? 'success' : 'danger'">
          {{ debugResult.success ? '执行成功' : '执行失败' }}
        </el-tag>
        <span style="margin-left:12px">耗时: {{ debugResult.totalDurationMs }}ms</span>
        <el-table :data="debugResult.nodeResults" border size="small" style="margin-top:8px">
          <el-table-column label="节点" prop="nodeKey" width="120" />
          <el-table-column label="名称" prop="nodeName" width="120" />
          <el-table-column label="状态" width="90">
            <template #default="{ row }">
              <el-tag size="small" :type="row.status === 'SUCCESS' ? 'success' : 'danger'">{{ row.status }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="耗时" prop="durationMs" width="80" />
          <el-table-column label="错误" prop="error" />
        </el-table>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  getOrchestration, updateOrchestration, validateOrchestration,
  debugOrchestration, getOrchestrationHealth, type OrchSavePayload
} from '@/api/orchestration'
import { listServicesByMicroservice } from '@/api/service'
import type { OrchestrationDetailVO, OrchNodeVO, OrchEdgeVO, OrchHealthVO, ServiceSimpleVO } from '@/types'

const route = useRoute()
const loading = ref(false)
const saving = ref(false)
const debugging = ref(false)
const detail = reactive<OrchestrationDetailVO>({} as OrchestrationDetailVO)
const form = reactive<any>({ name: '', code: '', description: '', status: 1 })
const nodes = ref<OrchNodeVO[]>([])
const edges = ref<OrchEdgeVO[]>([])
const selectedNode = ref<OrchNodeVO | null>(null)
const serviceList = ref<ServiceSimpleVO[]>([])
const health = reactive<OrchHealthVO>({ healthy: true, alerts: [] })

const nodeTypes = [
  { type: 'START', label: '开始节点', tag: 'success' as const },
  { type: 'SERVICE', label: '服务调用', tag: 'primary' as const },
  { type: 'CONDITION', label: '条件判断', tag: 'warning' as const },
  { type: 'LOOP', label: '循环节点', tag: 'info' as const },
  { type: 'END', label: '结束节点', tag: 'danger' as const }
]
function typeTag(t: string) {
  return ({ START: 'success', SERVICE: 'primary', CONDITION: 'warning', LOOP: 'info', END: 'danger' } as any)[t] || 'info'
}

let nodeSeq = 0
async function load() {
  loading.value = true
  const id = Number(route.params.id)
  try {
    const res = await getOrchestration(id)
    Object.assign(detail, res)
    Object.assign(form, res.orchestration)
    nodes.value = (res.nodes || []).map(n => ({ ...n }))
    edges.value = (res.edges || []).map(e => ({ ...e }))
    nodeSeq = nodes.value.length
    serviceList.value = await listServicesByMicroservice(res.orchestration.microserviceId)
    const h = await getOrchestrationHealth(id)
    Object.assign(health, h)
  } finally { loading.value = false }
}

function addNode(type: string) {
  nodeSeq++
  const suffix = Date.now().toString().slice(-4)
  const key = `${type.toLowerCase()}_${nodeSeq}_${suffix}`
  nodes.value.push({
    id: 0, nodeKey: key, nodeType: type, nodeName: type === 'START' ? '开始' : type === 'END' ? '结束' : type,
    serviceId: null, serviceName: null, configJson: null, xPos: 100 + nodeSeq * 30, yPos: 100, sortOrder: nodeSeq
  })
}
function removeNode(index: number) {
  if (selectedNode.value === nodes.value[index]) { selectedNode.value = null }
  const key = nodes.value[index].nodeKey
  nodes.value.splice(index, 1)
  edges.value = edges.value.filter(e => e.fromNodeKey !== key && e.toNodeKey !== key)
}
function onSelectNode(row: OrchNodeVO) { selectedNode.value = row }

const edgeDialogVisible = ref(false)
const newEdge = reactive<any>({ edgeKey: '', fromNodeKey: '', toNodeKey: '', conditionExpr: '' })
function addEdge() {
  Object.assign(newEdge, { edgeKey: '', fromNodeKey: '', toNodeKey: '', conditionExpr: '' })
  edgeDialogVisible.value = true
}
function confirmAddEdge() {
  if (!newEdge.edgeKey || !newEdge.fromNodeKey || !newEdge.toNodeKey) {
    ElMessage.warning('请填写连线Key、起点、终点'); return
  }
  edges.value.push({ id: 0, edgeKey: newEdge.edgeKey, fromNodeKey: newEdge.fromNodeKey, toNodeKey: newEdge.toNodeKey, conditionExpr: newEdge.conditionExpr || null, labelText: null })
  edgeDialogVisible.value = false
}

async function onSave() {
  saving.value = true
  try {
    const payload: OrchSavePayload = {
      name: form.name, description: form.description, status: form.status,
      inputParams: [], outputParams: [],
      nodes: nodes.value.map(n => ({ ...n })), edges: edges.value.map(e => ({ ...e }))
    }
    await updateOrchestration(detail.orchestration!.id, payload)
    ElMessage.success('保存成功')
    await load()
  } catch (e) {
    // 错误已在拦截器提示
  } finally { saving.value = false }
}

async function onValidate() {
  try {
    const payload: OrchSavePayload = {
      name: form.name, description: form.description, status: form.status,
      inputParams: [], outputParams: [],
      nodes: nodes.value.map(n => ({ ...n })), edges: edges.value.map(e => ({ ...e }))
    }
    await validateOrchestration(detail.orchestration!.id, payload)
    ElMessage.success('校验通过')
  } catch (e) {
    // 错误已在拦截器提示
  }
}

const debugVisible = ref(false)
const debugInput = ref('{}')
const debugResult = ref<any>(null)
async function onDebug() {
  debugging.value = true
  try {
    let inputData = {}
    try { inputData = JSON.parse(debugInput.value || '{}') } catch { ElMessage.error('入参JSON格式错误'); return }
    debugResult.value = await debugOrchestration(detail.orchestration!.id, inputData)
  } finally { debugging.value = false }
}

onMounted(load)
</script>

<style scoped>
.info-card { margin-top: 12px; }
.flow-area { margin-top: 12px; }
.palette { display: flex; flex-direction: column; gap: 8px; }
.palette-item { cursor: pointer; }
.palette-item:hover { opacity: 0.8; }
.canvas-tip { margin-bottom: 4px; }
.edges-title { margin-top: 12px; margin-bottom: 4px; font-weight: 600; }
.empty-tip { color: #909399; text-align: center; padding: 20px 0; }
</style>

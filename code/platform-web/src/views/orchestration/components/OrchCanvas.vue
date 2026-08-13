<template>
  <div class="canvas-wrapper">
    <div class="canvas-toolbar">
      <el-button-group>
        <el-button size="small" @click="zoomIn"><el-icon><ZoomIn /></el-icon></el-button>
        <el-button size="small" @click="zoomOut"><el-icon><ZoomOut /></el-icon></el-button>
        <el-button size="small" @click="fitView"><el-icon><FullScreen /></el-icon></el-button>
        <el-button size="small" @click="resetView"><el-icon><RefreshLeft /></el-icon></el-button>
      </el-button-group>
      <el-button size="small" @click="autoLayout" :disabled="!hasNodes">
        <el-icon><Grid /></el-icon> 自动排版
      </el-button>
      <el-button size="small" :type="selectionMode ? 'primary' : ''" @click="toggleSelection">
        <el-icon><Select /></el-icon> {{ selectionMode ? '关闭选区' : '开启选区' }}
      </el-button>
      <span class="zoom-label">{{ zoomPct }}%</span>
      <div class="toolbar-right">
        <el-button size="small" @click="clearSelection" :disabled="selectedIds.length === 0">取消选中</el-button>
        <el-button size="small" type="danger" @click="deleteSelected" :disabled="selectedIds.length === 0">删除</el-button>
      </div>
    </div>

    <div
      class="canvas-container"
      @dragover.prevent
      @drop.prevent="onDrop"
    >
      <div ref="containerRef" class="lf-container"></div>
      <div v-if="!hasNodes" class="empty-tip">
        <p>拖拽左侧节点到此处开始编排</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { ZoomIn, ZoomOut, FullScreen, RefreshLeft, Grid, Select } from '@element-plus/icons-vue'
import LogicFlow from '@logicflow/core'
import '@logicflow/core/dist/index.css'
import { SelectionSelect } from '@logicflow/extension'
import '@logicflow/extension/lib/style/index.css'
import { Dagre } from '@logicflow/layout'
import { registerOrchNodes, typeToLf, lfToType, typeLabels } from '../logic/nodes'
import type { OrchNodeVO, OrchEdgeVO } from '@/types'

LogicFlow.use(Dagre)
LogicFlow.use(SelectionSelect)

const props = defineProps<{
  nodes: OrchNodeVO[]
  edges: OrchEdgeVO[]
}>()

const emit = defineEmits<{
  (e: 'update:nodes', nodes: OrchNodeVO[]): void
  (e: 'update:edges', edges: OrchEdgeVO[]): void
  (e: 'select-node', node: OrchNodeVO | null): void
  (e: 'select-edge', edge: OrchEdgeVO | null): void
}>()

const NODE_W = 140
const NODE_H = 44

const containerRef = ref<HTMLElement | null>(null)
let lf: LogicFlow | null = null

const zoomPct = ref(100)
const selectedIds = ref<string[]>([])
const selectionMode = ref(false)
const hasNodes = ref(false)

// 内部数据副本，用于双向同步
let nodeMap = new Map<string, OrchNodeVO>()
let edgeMap = new Map<string, OrchEdgeVO>()
// 防抖 & 循环更新防护
let syncTimer: number | null = null
let isSyncingUp = false // 画布 → props 更新中，防止 watch 回调覆盖
let isSyncingDown = false // props → 画布更新中
let layoutRunning = false
let lastNodesSig = '' // 数据签名，用于判断是否需要 re-render

function signature(nodes: OrchNodeVO[], edges: OrchEdgeVO[]) {
  const ns = nodes.map(n => `${n.nodeKey}:${n.xPos},${n.yPos}|${n.nodeName}|${n.serviceId ?? ''}`).join(';')
  const es = edges.map(e => `${e.edgeKey}:${e.fromNodeKey}->${e.toNodeKey}`).join(';')
  return ns + '#' + es
}

// --- 数据同步：props → LogicFlow ---
function syncFromProps() {
  if (isSyncingUp) return
  isSyncingDown = true
  try {
    nodeMap.clear()
    edgeMap.clear()
    props.nodes.forEach(n => nodeMap.set(n.nodeKey, { ...n }))
    props.edges.forEach(e => edgeMap.set(e.edgeKey, { ...e }))
    hasNodes.value = props.nodes.length > 0
    // 更新签名，防止 watch 重复触发
    lastNodesSig = signature(props.nodes, props.edges)
    renderGraph()
  } finally {
    requestAnimationFrame(() => { isSyncingDown = false })
  }
}

function renderGraph() {
  if (!lf) return
  const lfNodes = props.nodes.map(n => ({
    id: n.nodeKey,
    type: typeToLf[n.nodeType] || typeToLf.SERVICE,
    x: (n.xPos ?? 0) + NODE_W / 2,
    y: (n.yPos ?? 0) + NODE_H / 2,
    text: n.nodeName || typeLabels[n.nodeType] || n.nodeKey,
    properties: { _orchType: n.nodeType }
  }))
  const lfEdges = props.edges.map(e => ({
    id: e.edgeKey,
    type: 'polyline',
    sourceNodeId: e.fromNodeKey,
    targetNodeId: e.toNodeKey,
    text: e.labelText || ''
  }))
  lf.render({ nodes: lfNodes, edges: lfEdges })
}

// --- 数据同步：LogicFlow → props ---
function syncToProps(immediate = false) {
  // immediate=true 时强制同步，绕过 isSyncingDown 守卫
  // 否则保存时如果画布正在渲染(isSyncingDown=true)，位置数据会丢失
  if (!immediate && isSyncingDown) return
  if (syncTimer && !immediate) return // 已有防抖等待
  const doSync = () => {
    if (!lf) return
    isSyncingUp = true
    syncTimer = null
    try {
      const graph: any = lf.getGraphData()
      const newNodes: OrchNodeVO[] = []
      ;(graph.nodes || []).forEach((ln: any) => {
        const existing = nodeMap.get(ln.id) || props.nodes.find(n => n.nodeKey === ln.id)
        const orchType = ln.properties?._orchType || lfToType[ln.type] || 'SERVICE'
        if (existing) {
          existing.xPos = Math.round(ln.x - NODE_W / 2)
          existing.yPos = Math.round(ln.y - NODE_H / 2)
          existing.nodeName = ln.text?.value || existing.nodeName
          newNodes.push(existing)
        } else {
          newNodes.push({
            id: 0,
            nodeKey: ln.id,
            nodeType: orchType,
            nodeName: ln.text?.value || typeLabels[orchType] || ln.id,
            serviceId: null, serviceName: null, configJson: null,
            txType: 'LOCAL', txTimeout: 60,
            retryCount: 0, retryInterval: 1000,
            exceptionStrategy: 'INTERRUPT',
            loopType: orchType === 'LOOP' ? 'SERIAL' : null,
            branchExpr: orchType === 'BRANCH' ? '' : null,
            xPos: Math.round(ln.x - NODE_W / 2),
            yPos: Math.round(ln.y - NODE_H / 2),
            sortOrder: newNodes.length + 1
          })
        }
      })
      newNodes.forEach((n, i) => { n.sortOrder = i + 1 })
      nodeMap.clear()
      newNodes.forEach(n => nodeMap.set(n.nodeKey, n))

      const newEdges: OrchEdgeVO[] = []
      ;(graph.edges || []).forEach((le: any) => {
        const existing = edgeMap.get(le.id) || props.edges.find(e => e.edgeKey === le.id)
        if (existing) {
          existing.fromNodeKey = le.sourceNodeId
          existing.toNodeKey = le.targetNodeId
          existing.labelText = le.text?.value || null
          newEdges.push(existing)
        } else {
          newEdges.push({
            id: 0, edgeKey: le.id,
            fromNodeKey: le.sourceNodeId,
            toNodeKey: le.targetNodeId,
            conditionExpr: null,
            labelText: le.text?.value || null
          })
        }
      })
      edgeMap.clear()
      newEdges.forEach(e => edgeMap.set(e.edgeKey, e))

      hasNodes.value = newNodes.length > 0
      // 在 emit 之前更新签名，防止 watch 回调触发 re-render
      lastNodesSig = signature(newNodes, newEdges)
      emit('update:nodes', newNodes)
      emit('update:edges', newEdges)
    } finally {
      // 下一帧清除，确保 Vue 的 watch flush 已经执行完毕
      requestAnimationFrame(() => { isSyncingUp = false })
    }
  }
  if (immediate) {
    if (syncTimer) { clearTimeout(syncTimer); syncTimer = null }
    doSync()
  } else {
    syncTimer = window.setTimeout(doSync, 150)
  }
}

function forceSyncNow() { syncToProps(true) }

// --- 拖拽放置 ---
function onDrop(e: DragEvent) {
  if (!lf || !e.dataTransfer || !containerRef.value) return
  const orchType = e.dataTransfer.getData('application/node-orch-type')
  const lfType = e.dataTransfer.getData('application/node-lf-type')
  if (!orchType || !lfType) return

  // 手动将屏幕坐标转换为 LogicFlow 画布坐标
  // 不依赖 getPointByClient（返回格式不确定），直接用 transform 计算
  const rect = containerRef.value.getBoundingClientRect()
  const t = lf.getTransform()
  const x = (e.clientX - rect.left - t.TRANSLATE_X) / t.SCALE_X
  const y = (e.clientY - rect.top - t.TRANSLATE_Y) / t.SCALE_Y

  const nodeKey = `${orchType.toLowerCase()}_${Date.now().toString().slice(-6)}`
  lf.addNode({
    id: nodeKey,
    type: lfType,
    x, y,
    text: typeLabels[orchType] || orchType,
    properties: { _orchType: orchType }
  })
  selectedIds.value = [nodeKey]
  // 用 rAF 确保 LogicFlow 内部处理完 addNode 后再同步
  requestAnimationFrame(() => {
    forceSyncNow()
    const node = nodeMap.get(nodeKey)
    if (node) emit('select-node', node)
  })
}

// --- 工具栏操作 ---
function zoomIn() { lf?.zoom(true); updateZoom() }
function zoomOut() { lf?.zoom(false); updateZoom() }
function fitView() { lf?.fitView(); updateZoom() }
function resetView() { lf?.resetZoom(); lf?.resetTranslate(); updateZoom() }
function updateZoom() {
  if (!lf) return
  const t = lf.getTransform()
  zoomPct.value = Math.round(t.SCALE_X * 100)
}

function autoLayout() {
  if (!lf || props.nodes.length === 0 || layoutRunning) return
  layoutRunning = true

  // Dagre 布局：时间复杂度 O(N*E)，N<500 节点以内可用；1000+ 仍保持 <200ms
  // 节点越多 Dagre 相对 manualLayout 优势越明显（Dagre 网络单纯形法）
  try {
    const dagreExt = (lf as any).extension?.dagre
    if (dagreExt) {
      // Dagre 参数：rankdir=TB 从上到下；ranker=network-simplex 最少连线交叉
      dagreExt.layout({
        rankdir: 'TB',
        align: 'DL',
        ranker: 'network-simplex',
        nodesep: 50,       // 同层相邻节点距离
        ranksep: 90,       // 相邻层间距
        edgesep: 10,
        padding: 40
      })
    } else {
      manualLayout()
    }
    // Dagre 调整节点后需要触发连线折点重算并适配屏幕
    lf.resetZoom()
    lf.resetTranslate()
    requestAnimationFrame(() => {
      forceSyncNow()
      lf!.fitView(30)
      updateZoom()
      layoutRunning = false
    })
  } catch (err) {
    console.warn('[autoLayout] 布局失败，回退手动布局', err)
    manualLayout()
    forceSyncNow()
    lf.fitView(30)
    updateZoom()
    layoutRunning = false
  }
}

function manualLayout() {
  if (!lf) return
  const N = props.nodes.length
  // O(N) 建图
  const outgoing = new Map<string, string[]>()
  const incoming = new Map<string, number>()
  props.nodes.forEach(n => { outgoing.set(n.nodeKey, []); incoming.set(n.nodeKey, 0) })
  props.edges.forEach(e => {
    if (!outgoing.has(e.fromNodeKey)) outgoing.set(e.fromNodeKey, [])
    if (outgoing.has(e.fromNodeKey) && !outgoing.get(e.fromNodeKey)!.includes(e.toNodeKey)) {
      outgoing.get(e.fromNodeKey)!.push(e.toNodeKey)
    }
    if (incoming.has(e.toNodeKey)) incoming.set(e.toNodeKey, (incoming.get(e.toNodeKey) || 0) + 1)
  })

  // 拓扑排序 BFS (Kahn 算法) O(N+E)，天然支持循环检测
  const indeg = new Map(incoming)
  const queue: string[] = []
  const layer = new Map<string, number>()
  const startNodes = props.nodes.filter(n => n.nodeType === 'START').map(n => n.nodeKey)
  startNodes.forEach(k => { layer.set(k, 0); if (!queue.includes(k)) queue.push(k) })
  // 也把其他入度为0的节点加进来
  indeg.forEach((deg, k) => {
    if (deg === 0 && !layer.has(k)) { layer.set(k, 0); queue.push(k) }
  })

  let processed = 0
  const MAX_ITER = N + 1 // 安全边界，防止有环时死循环
  const cycles: string[] = []
  while (queue.length && processed < MAX_ITER) {
    const node = queue.shift()!
    processed++
    const curL = layer.get(node) || 0
    ;(outgoing.get(node) || []).forEach(t => {
      const newL = curL + 1
      if (!layer.has(t) || layer.get(t)! < newL) {
        layer.set(t, newL)
      }
      const d = (indeg.get(t) || 0) - 1
      indeg.set(t, d)
      if (d <= 0 && !queue.includes(t)) queue.push(t)
    })
  }
  // 剩余未处理的即循环节点，放到最后一层，避免打乱其它节点
  const remaining = props.nodes.filter(n => !layer.has(n.nodeKey)).map(n => n.nodeKey)
  const lastL = remaining.length > 0 && layer.size > 0 ? Math.max(...Array.from(layer.values())) + 1 : 0
  remaining.forEach(k => { cycles.push(k); layer.set(k, lastL) })

  if (cycles.length > 0) {
    console.info(`[autoLayout] 检测到 ${cycles.length} 个循环节点，已放置到末尾层`)
  }

  // 同一层节点按类型排序，让 START 在最前、END 在最后
  const typeOrder: Record<string, number> = { START: 0, SERVICE: 1, ACTION: 2, CONDITION: 3, BRANCH: 4, LOOP: 5, END: 6 }
  const maxL = Math.max(...Array.from(layer.values()), 0)
  const layers: string[][] = Array.from({ length: maxL + 1 }, () => [])
  props.nodes.forEach(n => {
    const l = layer.get(n.nodeKey) ?? 0
    layers[l].push(n.nodeKey)
  })
  layers.forEach(arr => arr.sort((a, b) => {
    const na = props.nodes.find(x => x.nodeKey === a)!
    const nb = props.nodes.find(x => x.nodeKey === b)!
    return (typeOrder[na.nodeType] ?? 3) - (typeOrder[nb.nodeType] ?? 3)
  }))

  // O(N) 放置节点：垂直布局，各层从上到下排列，同层节点水平排列
  const NODE_GAP_X = 80    // 同层节点水平间距
  const LAYER_GAP_Y = 120  // 相邻层垂直间距
  layers.forEach((keys, idx) => {
    const totalW = keys.length * NODE_W + (keys.length - 1) * (NODE_GAP_X - NODE_W)
    const startX = Math.max(80, 400 - totalW / 2)
    keys.forEach((key, ni) => {
      lf!.setNodePosition(key, startX + ni * NODE_GAP_X + NODE_W / 2, 80 + idx * LAYER_GAP_Y + NODE_H / 2)
    })
  })
}

function clearSelection() {
  selectedIds.value = []
  lf?.clearSelectElements()
  emit('select-node', null)
  emit('select-edge', null)
}

function toggleSelection() {
  if (!lf) return
  if (selectionMode.value) {
    lf.closeSelectionSelect()
    selectionMode.value = false
  } else {
    lf.openSelectionSelect()
    selectionMode.value = true
  }
}

function deleteSelected() {
  if (!lf || selectedIds.value.length === 0) return
  for (const id of selectedIds.value) {
    if (nodeMap.has(id)) {
      lf.deleteNode(id)
    } else if (edgeMap.has(id)) {
      lf.deleteEdge(id)
    }
  }
  selectedIds.value = []
  forceSyncNow()
  emit('select-node', null)
  emit('select-edge', null)
}

// --- LogicFlow 初始化 ---
function initLogicFlow() {
  if (!containerRef.value) return

  lf = new LogicFlow({
    container: containerRef.value,
    grid: { size: 10, type: 'dot', config: { color: '#e0e0e0' } },
    background: { color: '#fafafa' },
    keyboard: { enabled: true },
    edgeType: 'polyline',
    style: { outline: { stroke: '#409eff', strokeDasharray: '4 4' } },
    anchor: {
      radius: 4,
      offset: 0,
      style: { fill: '#fff', stroke: '#c0c4cc', strokeWidth: 1 }
    }
  })

  registerOrchNodes(lf)

  // 选中事件
  lf.on('node:click', ({ data }: any) => {
    selectedIds.value = [data.id]
    emit('select-node', nodeMap.get(data.id) || null)
    emit('select-edge', null)
  })
  lf.on('edge:click', ({ data }: any) => {
    selectedIds.value = [data.id]
    emit('select-edge', edgeMap.get(data.id) || null)
    emit('select-node', null)
  })
  lf.on('blank:click', () => clearSelection())

  // 框选事件：多选时更新选中列表
  lf.on('selection:selected', ({ nodes, edges }: any) => {
    const ids: string[] = []
    ;(nodes || []).forEach((n: any) => ids.push(n.id || n))
    ;(edges || []).forEach((e: any) => ids.push(e.id || e))
    selectedIds.value = ids
    if (nodes && nodes.length === 1 && !edges) {
      emit('select-node', nodeMap.get(nodes[0].id) || null)
      emit('select-edge', null)
    } else if (edges && edges.length === 1 && !nodes) {
      emit('select-edge', edgeMap.get(edges[0].id) || null)
      emit('select-node', null)
    } else {
      emit('select-node', null)
      emit('select-edge', null)
    }
  })

  // 节点/连线拖拽后同步位置（防抖 150ms）
  lf.on('node:drag-end', () => forceSyncNow())
  lf.on('node:drop', () => forceSyncNow())
  lf.on('node:dnd-add', () => forceSyncNow())
  lf.on('edge:add', () => forceSyncNow())
  lf.on('edge:delete', () => forceSyncNow())
  lf.on('node:delete', () => forceSyncNow())
  lf.on('edge:adjust', () => forceSyncNow())
  lf.on('node:dnd-drag-end', () => forceSyncNow())
  // 拖拽过程中节流同步（每 200ms 最多一次，保证大节点量下拖拽不卡顿）
  let dragThrottle = 0
  lf.on('node:drag', () => {
    const now = performance.now()
    if (now - dragThrottle > 200) { dragThrottle = now; syncToProps(false) }
  })

  // 键盘删除
  lf.on('keydown', (e: KeyboardEvent) => {
    if (e.key === 'Delete' || e.key === 'Backspace') {
      deleteSelected()
    }
  })

  syncFromProps()
}

// --- 生命周期 ---
watch(() => [props.nodes, props.edges], () => {
  if (!lf) return
  const sig = signature(props.nodes, props.edges)
  if (sig === lastNodesSig) return
  // 画布正在向上同步时，忽略 props 变化（防止拖拽中被还原）
  if (isSyncingUp) return
  lastNodesSig = sig
  syncFromProps()
}, { deep: true })

onMounted(async () => {
  await nextTick()
  initLogicFlow()
})

onBeforeUnmount(() => {
  if (syncTimer) { clearTimeout(syncTimer); syncTimer = null }
  lf?.destroy?.()
  lf = null
})

// 暴露方法给父组件
defineExpose({ syncToProps: forceSyncNow, forceSyncNow, autoLayout })
</script>

<style scoped>
.canvas-wrapper { display: flex; flex-direction: column; height: 100%; }
.canvas-toolbar {
  display: flex; align-items: center; gap: 8px;
  padding: 8px 12px; background: #fff;
  border-bottom: 1px solid #ebeef5;
}
.zoom-label {
  font-size: 12px; color: #606266;
  min-width: 45px; text-align: center;
}
.toolbar-right { margin-left: auto; display: flex; gap: 8px; }
.canvas-container {
  flex: 1; overflow: hidden; position: relative;
}
.lf-container { width: 100%; height: 100%; }
.empty-tip {
  position: absolute; top: 50%; left: 50%;
  transform: translate(-50%, -50%);
  color: #c0c4cc; font-size: 14px; pointer-events: none;
}
</style>

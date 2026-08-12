<template>
  <div class="palette">
    <div class="palette-title">节点库</div>
    <div class="palette-list">
      <div
        v-for="item in nodeItems"
        :key="item.lfType"
        class="palette-item"
        :draggable="true"
        @dragstart="onDragStart($event, item)"
      >
        <div class="palette-icon" :style="{ background: item.color }">
          <span v-if="item.shape === 'circle'" class="icon-circle">{{ item.icon }}</span>
          <span v-else-if="item.shape === 'diamond'" class="icon-diamond">{{ item.icon }}</span>
          <span v-else class="icon-rect">{{ item.icon }}</span>
        </div>
        <span class="palette-label">{{ item.label }}</span>
      </div>
    </div>
    <div class="palette-tip">
      <p>拖拽节点到画布</p>
      <p>拖拽节点边缘连线</p>
      <p>Delete 键删除选中</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { typeToLf, typeLabels, typeColors } from '../logic/nodes'

interface DragItem {
  lfType: string
  orchType: string
  label: string
  icon: string
  shape: 'circle' | 'rect' | 'diamond'
  color: string
}

const shapeMap: Record<string, 'circle' | 'rect' | 'diamond'> = {
  START: 'circle', END: 'circle',
  CONDITION: 'diamond',
  SERVICE: 'rect', ACTION: 'rect', LOOP: 'rect', BRANCH: 'rect'
}

const iconMap: Record<string, string> = {
  START: '▶', END: '■', CONDITION: '?',
  SERVICE: '⚙', ACTION: 'A', LOOP: '↻', BRANCH: '◇'
}

const nodeItems: DragItem[] = Object.keys(typeToLf).map(orchType => ({
  lfType: typeToLf[orchType],
  orchType,
  label: typeLabels[orchType],
  icon: iconMap[orchType] || '?',
  shape: shapeMap[orchType] || 'rect',
  color: typeColors[orchType]
}))

function onDragStart(e: DragEvent, item: DragItem) {
  e.dataTransfer!.setData('application/node-orch-type', item.orchType)
  e.dataTransfer!.setData('application/node-lf-type', item.lfType)
  e.dataTransfer!.effectAllowed = 'move'
}
</script>

<style scoped>
.palette { padding: 8px; }
.palette-title {
  font-weight: 600; margin-bottom: 12px;
  padding-bottom: 8px; border-bottom: 1px solid #ebeef5;
}
.palette-list { display: flex; flex-direction: column; gap: 8px; }
.palette-item {
  display: flex; align-items: center; gap: 10px;
  padding: 8px 10px; border: 1px solid #e4e7ed;
  border-radius: 6px; cursor: grab; background: #fff;
  transition: all 0.2s; user-select: none;
}
.palette-item:hover {
  border-color: #409eff;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.15);
  transform: translateY(-1px);
}
.palette-item:active { cursor: grabbing; }
.palette-icon {
  width: 28px; height: 28px; border-radius: 6px;
  display: flex; align-items: center; justify-content: center;
  color: #fff; font-size: 14px; font-weight: 600; flex-shrink: 0;
}
.icon-circle {
  width: 22px; height: 22px; border-radius: 50%;
  background: #fff !important; color: inherit; font-size: 12px;
}
.icon-diamond {
  transform: rotate(45deg); width: 20px; height: 20px;
  background: #fff !important; color: inherit; font-size: 12px;
}
.palette-label { font-size: 13px; color: #303133; }
.palette-tip {
  margin-top: 16px; padding: 8px;
  background: #f4f4f5; border-radius: 4px;
  font-size: 11px; color: #909399; line-height: 1.6;
}
.palette-tip p { margin: 0; }
</style>

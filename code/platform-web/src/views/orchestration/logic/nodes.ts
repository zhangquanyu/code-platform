import LogicFlow, {
  RectNode, RectNodeModel,
  CircleNode, CircleNodeModel,
  DiamondNode, DiamondNodeModel,
  h
} from '@logicflow/core'

// 节点类型常量
export const NODE_TYPES = {
  START: 'orch-start',
  END: 'orch-end',
  CONDITION: 'orch-condition',
  SERVICE: 'orch-service',
  ACTION: 'orch-action',
  LOOP: 'orch-loop',
  BRANCH: 'orch-branch'
} as const

// 业务类型 → LogicFlow 类型映射
export const typeToLf: Record<string, string> = {
  START: NODE_TYPES.START,
  END: NODE_TYPES.END,
  CONDITION: NODE_TYPES.CONDITION,
  SERVICE: NODE_TYPES.SERVICE,
  ACTION: NODE_TYPES.ACTION,
  LOOP: NODE_TYPES.LOOP,
  BRANCH: NODE_TYPES.BRANCH
}

// LogicFlow 类型 → 业务类型映射
export const lfToType: Record<string, string> = Object.entries(typeToLf).reduce(
  (acc, [k, v]) => { acc[v] = k; return acc }, {} as Record<string, string>
)

// 节点显示名称
export const typeLabels: Record<string, string> = {
  START: '开始', END: '结束', CONDITION: '条件',
  SERVICE: '服务', ACTION: '动作', LOOP: '循环', BRANCH: '分支'
}

// 节点颜色
export const typeColors: Record<string, string> = {
  START: '#67C23A', END: '#F56C6C', CONDITION: '#E6A23C',
  SERVICE: '#409EFF', ACTION: '#909399', LOOP: '#67C23A', BRANCH: '#9B59B6'
}

const NODE_W = 140
const NODE_H = 44

// --- 矩形节点（SERVICE / ACTION / LOOP / BRANCH）---
function createRectNode(type: string, color: string) {
  const Model = class extends RectNodeModel {
    initNodeData(data: any) {
      super.initNodeData(data)
      this.width = NODE_W
      this.height = NODE_H
      this.radius = 6
    }
    getNodeStyle() {
      const s = super.getNodeStyle()
      s.fill = color
      s.stroke = 'none'
      return s
    }
    getTextStyle() {
      const s = super.getTextStyle()
      s.color = '#fff'
      s.fontSize = 12
      s.fontWeight = 600
      return s
    }
  }
  const View = class extends RectNode {
    getShape() {
      const { model } = this.props
      const { x, y, width, height, radius } = model
      const style = model.getNodeStyle()
      return h('rect', {
        ...style,
        x: x - width / 2, y: y - height / 2,
        width, height, rx: radius, ry: radius
      })
    }
  }
  return { type, model: Model, view: View }
}

// --- 圆形节点（START / END）---
function createCircleNode(type: string, color: string) {
  const Model = class extends CircleNodeModel {
    initNodeData(data: any) {
      super.initNodeData(data)
      this.r = NODE_H / 2
    }
    getNodeStyle() {
      const s = super.getNodeStyle()
      s.fill = color
      s.stroke = 'none'
      return s
    }
    getTextStyle() {
      const s = super.getTextStyle()
      s.color = '#fff'
      s.fontSize = 13
      s.fontWeight = 600
      return s
    }
  }
  const View = class extends CircleNode {
    getShape() {
      const { model } = this.props
      const { x, y, r } = model
      const style = model.getNodeStyle()
      return h('circle', { ...style, cx: x, cy: y, r })
    }
  }
  return { type, model: Model, view: View }
}

// --- 菱形节点（CONDITION）---
function createDiamondNode(type: string, color: string) {
  const Model = class extends DiamondNodeModel {
    initNodeData(data: any) {
      super.initNodeData(data)
      this.size = NODE_H + 10
    }
    getNodeStyle() {
      const s = super.getNodeStyle()
      s.fill = color
      s.stroke = 'none'
      return s
    }
    getTextStyle() {
      const s = super.getTextStyle()
      s.color = '#fff'
      s.fontSize = 12
      s.fontWeight = 600
      return s
    }
  }
  const View = class extends DiamondNode {
    getShape() {
      const { model } = this.props
      const { x, y, size } = model
      const style = model.getNodeStyle()
      const points = `${x},${y - size / 2} ${x + size / 2},${y} ${x},${y + size / 2} ${x - size / 2},${y}`
      return h('polygon', { ...style, points })
    }
  }
  return { type, model: Model, view: View }
}

// 注册所有自定义节点
export function registerOrchNodes(lf: LogicFlow) {
  lf.register(createCircleNode(NODE_TYPES.START, typeColors.START))
  lf.register(createCircleNode(NODE_TYPES.END, typeColors.END))
  lf.register(createDiamondNode(NODE_TYPES.CONDITION, typeColors.CONDITION))
  lf.register(createRectNode(NODE_TYPES.SERVICE, typeColors.SERVICE))
  lf.register(createRectNode(NODE_TYPES.ACTION, typeColors.ACTION))
  lf.register(createRectNode(NODE_TYPES.LOOP, typeColors.LOOP))
  lf.register(createRectNode(NODE_TYPES.BRANCH, typeColors.BRANCH))
}

// DndPanel 面板配置
export const dndPanelConfig = [
  {
    type: NODE_TYPES.START,
    label: '开始', text: '开始',
    icon: 'bpmn:startEvent',
    properties: { _orchType: 'START' }
  },
  {
    type: NODE_TYPES.SERVICE,
    label: '服务', text: '服务',
    properties: { _orchType: 'SERVICE' }
  },
  {
    type: NODE_TYPES.ACTION,
    label: '动作', text: '动作',
    properties: { _orchType: 'ACTION' }
  },
  {
    type: NODE_TYPES.CONDITION,
    label: '条件', text: '条件',
    properties: { _orchType: 'CONDITION' }
  },
  {
    type: NODE_TYPES.LOOP,
    label: '循环', text: '循环',
    properties: { _orchType: 'LOOP' }
  },
  {
    type: NODE_TYPES.BRANCH,
    label: '分支', text: '分支',
    properties: { _orchType: 'BRANCH' }
  },
  {
    type: NODE_TYPES.END,
    label: '结束', text: '结束',
    properties: { _orchType: 'END' }
  }
]

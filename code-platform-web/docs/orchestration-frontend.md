# 服务编排前端实现详细文档

## 1. 技术栈与依赖

| 类别     | 技术                   | 版本                  | 用途                   |
| ------ | -------------------- | ------------------- | -------------------- |
| 框架     | Vue 3                | 3.x                 | 前端 UI 框架             |
| 语言     | TypeScript           | 5.x                 | 类型安全                 |
| UI 组件库 | Element Plus         | 2.x                 | 表单、按钮、弹窗等            |
| 画布引擎   | LogicFlow            | @logicflow/core 2.x | 流程图绘制引擎              |
| 布局扩展   | @logicflow/layout    | 2.x                 | Dagre 自动布局算法         |
| 功能扩展   | @logicflow/extension | 2.x                 | SelectionSelect 框选插件 |
| HTTP   | Axios                | 1.x                 | 后端 API 调用            |

### 核心 npm 依赖

```
@logicflow/core    # 画布核心
@logicflow/layout  # Dagre 自动布局
@logicflow/extension # 选区等扩展
element-plus       # UI 组件
```

***

## 2. 项目结构

```
code-platform/
├── code/platform-web/                    # 前端项目根
│   ├── src/
│   │   ├── api/
│   │   │   ├── orchestration.ts         # 编排相关 API
│   │   │   ├── application.ts            # 应用管理 API
│   │   │   ├── microservice.ts           # 微服务 API
│   │   │   └── service.ts                # 服务 API
│   │   ├── router/
│   │   │   └── index.ts                 # 路由配置
│   │   ├── types/
│   │   │   └── index.ts                 # TypeScript 类型定义
│   │   ├── utils/
│   │   │   └── request.ts                # Axios 封装
│   │   └── views/
│   │       └── orchestration/
│   │           ├── OrchestrationList.vue       # 编排列表页
│   │           ├── OrchestrationDetail.vue    # 编排详情页（三栏布局）
│   │           ├── logic/
│   │           │   └── nodes.ts               # LogicFlow 自定义节点注册
│   │           └── components/
│   │               ├── OrchCanvas.vue          # 画布组件（核心）
│   │               ├── OrchNodePalette.vue     # 左侧节点库面板
│   │               └── OrchPropertyPanel.vue  # 右侧属性面板
│   └── package.json
```

***

## 3. 页面架构

### 3.1 编排列表页 (`OrchestrationList.vue`)

- **路由**: `/orchestrations`
- **功能**:
  - 按应用/微服务/关键字筛选编排
  - 分页展示编排列表（名称、编码、微服务、事务类型、节点数、状态）
  - 新建编排（弹窗表单，选择应用→微服务→填写名称/编码）
  - 删除编排
  - 点击名称跳转详情页

### 3.2 编排详情页 (`OrchestrationDetail.vue`)

- **路由**: `/orchestrations/:id`
- **三栏布局**:
  ```
  ┌─────────────────────────────────────────────────────┐
  │  顶部操作栏  [返回] [校验] [保存]                     │
  ├─────────┬───────────────────────────┬───────────────┤
  │         │                           │               │
  │ 节点库  │       编排画布             │  属性面板     │
  │  Palette│       Canvas              │  PropertyPanel│
  │  200px  │       flex:1              │  320px        │
  │         │                           │               │
  └─────────┴───────────────────────────┴───────────────┘
  ```
- **调试弹窗**: 输入 JSON 参数调用后端调试接口

***

## 4. 核心模块详解

### 4.1 自定义节点注册 (`logic/nodes.ts`)

LogicFlow 提供三种基础节点类型，通过继承 Model 和 View 实现自定义节点。

#### 4.1.1 节点类型映射

| 业务类型      | LogicFlow 类型   | 形状 | 颜色         |
| --------- | -------------- | -- | ---------- |
| START     | orch-start     | 圆形 | #67C23A（绿） |
| END       | orch-end       | 圆形 | #F56C6C（红） |
| CONDITION | orch-condition | 菱形 | #E6A23C（橙） |
| SERVICE   | orch-service   | 矩形 | #409EFF（蓝） |
| ACTION    | orch-action    | 矩形 | #909399（灰） |
| LOOP      | orch-loop      | 矩形 | #67C23A（绿） |
| BRANCH    | orch-branch    | 矩形 | #9B59B6（紫） |

```typescript
export const typeToLf: Record<string, string> = {
  START: 'orch-start',
  END: 'orch-end',
  CONDITION: 'orch-condition',
  SERVICE: 'orch-service',
  ACTION: 'orch-action',
  LOOP: 'orch-loop',
  BRANCH: 'orch-branch'
}

export const lfToType: Record<string, string> = {
  'orch-start': 'START',
  'orch-end': 'END',
  // ... 反向映射
}
```

#### 4.1.2 自定义节点实现

**矩形节点** (SERVICE/ACTION/LOOP/BRANCH):

```typescript
function createRectNode(type: string, color: string) {
  const Model = class extends RectNodeModel {
    initNodeData(data: any) {
      super.initNodeData(data)
      this.width = 140    // NODE_W
      this.height = 44    // NODE_H
      this.radius = 6     // 圆角
    }
    getNodeStyle() { /* 填充色、无边框 */ }
    getTextStyle() { /* 白色文字，12px，加粗 */ }
  }
  const View = class extends RectNode {
    getShape() { /* 绘制圆角矩形 */ }
  }
  return { type, model: Model, view: View }
}
```

**圆形节点** (START/END):

```typescript
function createCircleNode(type: string, color: string) {
  const Model = class extends CircleNodeModel {
    initNodeData(data: any) {
      super.initNodeData(data)
      this.r = 22  // NODE_H / 2
    }
  }
  const View = class extends CircleNode {
    getShape() { /* 绘制圆形 */ }
  }
}
```

**菱形节点** (CONDITION):

```typescript
function createDiamondNode(type: string, color: string) {
  const Model = class extends DiamondNodeModel {
    initNodeData(data: any) {
      super.initNodeData(data)
      this.size = 54          // NODE_H + 10
      this.rx = this.size / 2 // 27
      this.ry = this.size / 2 // 27，与 size 对齐确保锚点位置正确
    }
  }
  const View = class extends DiamondNode {
    getShape() {
      // 使用 polygon 绘制菱形
      const points = `${x},${y - size/2} ${x + size/2},${y} ${x},${y + size/2} ${x - size/2},${y}`
      return h('polygon', { ...style, points })
    }
  }
}
```

> **关键设计**: `rx`/`ry` 必须与 `size` 对齐，否则 LogicFlow 的 `getDefaultAnchor()` 计算的锚点位置会偏离菱形顶点。

#### 4.1.3 注册与插件启用

```typescript
// 在 OrchCanvas.vue 中注册
LogicFlow.use(Dagre)          // 启用 Dagre 布局
LogicFlow.use(SelectionSelect) // 启用框选

registerOrchNodes(lf)          // 注册自定义节点类型
```

***

### 4.2 画布组件 (`OrchCanvas.vue`)

#### 4.2.1 双向数据同步机制

这是画布实现的核心难点。LogicFlow 画布与 Vue props 之间需要双向同步，且必须防止循环更新。

```
┌──────────────┐   emit(update:nodes)    ┌────────────────┐
│  LogicFlow   │ ──────────────────────▶ │  Vue props     │
│  Canvas      │                         │  (nodes/edges) │
│              │ ◀────────────────────── │                │
└──────────────┘   watch(props)          └────────────────┘
                   syncFromProps()
```

**同步状态标志**:

| 变量              | 作用                          | 置位时机                 | 清除时机                       |
| --------------- | --------------------------- | -------------------- | -------------------------- |
| `isSyncingUp`   | 画布→props 同步中，阻止 watch 回调    | `syncToProps()` 开始   | `requestAnimationFrame` 回调 |
| `isSyncingDown` | props→画布 同步中，阻止 syncToProps | `syncFromProps()` 开始 | `requestAnimationFrame` 回调 |
| `lastNodesSig`  | 数据签名，判断是否需要 re-render       | 同步完成时更新              | -                          |

**数据签名算法**:

```typescript
function signature(nodes, edges) {
  const ns = nodes.map(n => `${n.nodeKey}:${n.xPos},${n.yPos}|${n.nodeName}|${n.serviceId ?? ''}`).join(';')
  const es = edges.map(e => `${e.edgeKey}:${e.fromNodeKey}->${e.toNodeKey}`).join(';')
  return ns + '#' + es
}
```

**同步流程**:

1. **props → 画布** (`syncFromProps()`):
   - 判断 `isSyncingUp`，若为 true 则跳过（画布正在向上同步）
   - 更新内部 `nodeMap`/`edgeMap`
   - 调用 `renderGraph()` 重新渲染
   - 更新签名防止 watch 重复触发
   - 下一帧清除 `isSyncingDown`
2. **画布 → props** (`syncToProps(immediate)`):
   - `immediate=false` 且 `isSyncingDown=true` 时跳过
   - `immediate=false` 且有防抖定时器时跳过
   - 从 `lf.getGraphData()` 读取节点/连线数据
   - 坐标转换: `xPos = Math.round(ln.x - NODE_W/2)`（LogicFlow 以中心点为坐标，需转为左上角）
   - 更新 `nodeMap`/`edgeMap`
   - emit `update:nodes` 和 `update:edges`
   - 防抖 150ms 或立即执行

> **关键设计**: 保存时调用 `forceSyncNow()` = `syncToProps(true)`，绕过 `isSyncingDown` 守卫，确保位置数据不丢失。

#### 4.2.2 LogicFlow 初始化配置

```typescript
lf = new LogicFlow({
  container: containerRef.value,
  grid: { size: 10, type: 'dot', config: { color: '#e0e0e0' } },  // 点状网格
  background: { color: '#fafafa' },                                  // 浅灰背景
  keyboard: { enabled: true },                                        // 键盘操作
  edgeType: 'polyline',                                               // 折线连线
  style: { outline: { stroke: '#409eff', strokeDasharray: '4 4' } }, // 选中虚线
  anchor: {                                                           // 锚点配置
    radius: 4,        // 锚点圆圈半径
    offset: 0,        // 与节点边缘的偏移（0 = 紧贴边缘）
    style: { fill: '#fff', stroke: '#c0c4cc', strokeWidth: 1 }
  }
})
```

#### 4.2.3 事件处理

| 事件                            | 处理                       |
| ----------------------------- | ------------------------ |
| `node:click`                  | 选中单节点，emit `select-node` |
| `edge:click`                  | 选中单连线，emit `select-edge` |
| `blank:click`                 | 清空选中                     |
| `selection:selected`          | 框选更新 `selectedIds` 数组    |
| `node:drag-end`               | 强制同步位置 (`forceSyncNow`)  |
| `node:drag`                   | 节流同步（200ms）              |
| `edge:add`                    | 同步数据                     |
| `node:delete` / `edge:delete` | 同步数据                     |
| `keydown` (Delete/Backspace)  | 删除选中元素                   |

#### 4.2.4 自动排版

**Dagre 布局** (主方案):

```typescript
dagreExt.layout({
  rankdir: 'TB',           // 从上到下
  align: 'DL',             // 左下对齐
  ranker: 'network-simplex', // 最少连线交叉
  nodesep: 50,             // 同层节点间距
  ranksep: 90,             // 相邻层间距
  edgesep: 10,
  padding: 40
})
```

**手动布局** (降级方案):

- 拓扑排序（BFS/Kahn 算法）计算节点层级
- 同层节点按类型排序（START → SERVICE → ACTION → CONDITION → BRANCH → LOOP → END）
- 垂直布局：各层从上到下排列，同层节点水平居中排列
- 循环检测：有环时循环节点放到末尾层

#### 4.2.5 选区功能

```typescript
function toggleSelection() {
  if (selectionMode.value) {
    lf.closeSelectionSelect()
    selectionMode.value = false
  } else {
    lf.openSelectionSelect()
    selectionMode.value = true
  }
}
```

- 开启后画布进入框选模式，拖拽绘制矩形选择多个元素
- `selectedIds` 数组存储所有选中 ID
- 支持批量删除和取消选中

#### 4.2.6 拖拽放置

```typescript
function onDrop(e: DragEvent) {
  const orchType = e.dataTransfer.getData('application/node-orch-type')
  const lfType = e.dataTransfer.getData('application/node-lf-type')
  
  // 屏幕坐标 → 画布坐标（手动转换，不依赖 getPointByClient）
  const rect = containerRef.value.getBoundingClientRect()
  const t = lf.getTransform()
  const x = (e.clientX - rect.left - t.TRANSLATE_X) / t.SCALE_X
  const y = (e.clientY - rect.top - t.TRANSLATE_Y) / t.SCALE_Y
  
  const nodeKey = `${orchType.toLowerCase()}_${Date.now().toString().slice(-6)}`
  lf.addNode({ id: nodeKey, type: lfType, x, y, ... })
}
```

***

### 4.3 节点库面板 (`OrchNodePalette.vue`)

- 展示 7 种节点类型（START、SERVICE、ACTION、CONDITION、LOOP、BRANCH、END）
- 每项为可拖拽卡片，包含图标和标签
- 拖拽时通过 `dataTransfer` 传递 `application/node-orch-type` 和 `application/node-lf-type`
- 三种图标形状：圆形（开始/结束）、菱形（条件）、矩形（其他）

***

### 4.4 属性面板 (`OrchPropertyPanel.vue`)

#### 4.4.1 三种状态切换

| 状态   | 显示内容                      |
| ---- | ------------------------- |
| 无选中  | 编排属性（名称、编码、状态、事务类型、超时、描述） |
| 选中节点 | 节点属性（根据类型动态显示）            |
| 选中连线 | 连线属性（标签、条件表达式）            |

#### 4.4.2 节点属性字段

**通用字段**（所有节点类型）:

- 节点 Key（只读）
- 节点名称（可编辑）
- 事务类型（本地/分布式）
- 超时时间（秒，分布式事务时显示）
- 重试次数（0-10）
- 重试间隔（毫秒）
- 异常策略（中断/跳过/重试）

**服务节点/动作节点额外字段**:

- 所属应用 → 微服务 → 服务（三级联动选择）
- 选择结果保存在 `configJson` 中（JSON 字符串）

**循环节点额外字段**:

- 循环类型（串行/并行）

**分支节点额外字段**:

- 分支表达式

**高级配置**:

- 配置 JSON（原始 JSON 输入框）

#### 4.4.3 三级联动服务选择

```
选择应用 → 加载微服务列表
选择微服务 → 加载服务列表
选择服务 → 保存 serviceId + serviceName
```

选择结果以 JSON 格式存入 `configJson`:

```json
{ "_appId": 1, "_msId": 2, "其他自定义配置": "..." }
```

***

### 4.5 编排保存与校验

#### 4.5.1 保存流程

```
用户点击"保存"
    │
    ▼
forceSyncNow()  ─── 强制同步画布→props（绕过 isSyncingDown 守卫）
    │
    ▼
await nextTick()  ─── 等待 Vue 响应式更新完成
    │
    ▼
buildPayload()  ─── 构建请求体
    │
    ▼
PUT /api/v1/orchestrations/{id}
    │
    ▼
后端：先 validate() → 再 update()
    │
    ▼
成功 → ElMessage.success('保存成功')
失败 → ElMessage.error()
```

#### 4.5.2 Payload 结构

```typescript
interface OrchSavePayload {
  name: string
  description?: string
  status?: number
  txType?: string
  txTimeout?: number
  inputParams?: unknown[]
  outputParams?: unknown[]
  nodes: Partial<OrchNodeVO>[]   // 包含 xPos/yPos
  edges: Partial<OrchEdgeVO>[]
}
```

#### 4.5.3 校验流程

与保存类似，调用 `POST /api/v1/orchestrations/{id}/validate`，返回错误字符串数组。

***

## 5. 后端数据契约

### 5.1 节点 DTO

```java
@Data
public class OrchestrationUpdateCmd {
    // 编排基本信息
    private String name;
    private String description;
    private Integer status;
    private String txType;
    private Integer txTimeout;
    
    // 节点列表
    private List<OrchNodeCmd> nodes;
    
    // 连线列表
    private List<OrchEdgeCmd> edges;
    
    @Data
    public static class OrchNodeCmd {
        private Long id;
        private String nodeKey;        // 前端生成，如 "service_123456"
        private String nodeType;       // START/END/CONDITION/SERVICE/ACTION/LOOP/BRANCH
        private String nodeName;
        private Long serviceId;
        private String configJson;     // JSON 字符串，存储 appId/msId 等扩展信息
        private String txType;
        private Integer txTimeout;
        private Integer retryCount;
        private Integer retryInterval;
        private String exceptionStrategy;
        private String loopType;
        private String branchExpr;
        @JsonProperty("xPos")  // 必须显式指定，避免 Jackson 命名 bug
        private Integer xPos;
        @JsonProperty("yPos")
        private Integer yPos;
        private Integer sortOrder;
    }
    
    @Data
    public static class OrchEdgeCmd {
        private Long id;
        private String edgeKey;
        private String fromNodeKey;
        private String toNodeKey;
        private String conditionExpr;
        private String labelText;
    }
}
```

### 5.2 节点实体

```java
@Entity
@Table(name = "dev_orch_node",
       uniqueConstraints = @UniqueConstraint(
           name = "uk_dev_on_orch_key",
           columnNames = {"orchestration_id", "node_key", "is_deleted"}))
public class OrchestrationNode extends BaseEntity {
    private Long id;
    private Long orchestrationId;
    private String nodeKey;
    private String nodeType;
    private String nodeName;
    private Long serviceId;
    @Lob private String configJson;
    private String txType = "LOCAL";
    private Integer txTimeout = 60;
    private Integer retryCount = 0;
    private Integer retryInterval = 1000;
    private String exceptionStrategy = "INTERRUPT";
    private String loopType = "SERIAL";
    private String branchExpr;
    private Integer xPos;       // 节点 X 坐标（画布位置）
    private Integer yPos;       // 节点 Y 坐标
    private Integer sortOrder;
}
```

### 5.3 关键设计说明

**唯一约束冲突处理**:

保存流程采用「两阶段删除」策略：

1. 先物理删除旧的软删除记录：`hardDeleteSoftDeletedByOrchestrationId(id)`
2. 再软删除当前活跃记录：`softDeleteByOrchestrationId(id)`
3. 最后保存新记录

这样避免了 `(orchestration_id, node_key, is_deleted=1)` 唯一约束冲突。

**校验前置**:

`validate()` 在 `update()` 之前调用，防止校验失败导致事务回滚，丢失位置数据。

**Jackson 字段命名**:

由于 Jackson 默认将 `getXPos()` 解析为属性名 `XPos`（首字母不小写），必须在 DTO 和 VO 上添加 `@JsonProperty("xPos")` / `@JsonProperty("yPos")` 注解。

**序列化 null 字段**:

VO 类添加 `@JsonInclude(JsonInclude.Include.ALWAYS)`，确保即使 `xPos`/`yPos` 为 null 也会被序列化返回给前端，避免前端误判需要自动排版。

***

## 6. TypeScript 类型定义

```typescript
// 编排节点 VO
interface OrchNodeVO {
  id: number
  nodeKey: string        // 前端生成的唯一标识
  nodeType: string       // START/END/CONDITION/SERVICE/ACTION/LOOP/BRANCH
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
  xPos: number | null     // X 坐标（相对于画布左上角）
  yPos: number | null     // Y 坐标
  sortOrder: number
  // 前端辅助字段（不持久化，存在 configJson 中）
  appId?: number | null
  msId?: number | null
}

// 编排连线 VO
interface OrchEdgeVO {
  id: number
  edgeKey: string
  fromNodeKey: string
  toNodeKey: string
  conditionExpr: string | null
  labelText: string | null
}

// 编排详情 VO
interface OrchestrationDetailVO {
  orchestration: OrchestrationVO
  inputParams: unknown[]
  outputParams: unknown[]
  nodes: OrchNodeVO[]
  edges: OrchEdgeVO[]
}
```

***

## 7. API 接口

| 方法     | 路径                                     | 说明             |
| ------ | -------------------------------------- | -------------- |
| GET    | `/api/v1/orchestrations`               | 分页查询编排列表       |
| GET    | `/api/v1/orchestrations/{id}`          | 获取编排详情（含节点、连线） |
| POST   | `/api/v1/orchestrations`               | 创建编排           |
| PUT    | `/api/v1/orchestrations/{id}`          | 更新编排（保存）       |
| DELETE | `/api/v1/orchestrations/{id}`          | 删除编排           |
| POST   | `/api/v1/orchestrations/{id}/validate` | 校验编排           |
| POST   | `/api/v1/orchestrations/{id}/debug`    | 调试编排           |
| PUT    | `/api/v1/orchestrations/{id}/status`   | 更新编排状态         |
| GET    | `/api/v1/orchestrations/{id}/health`   | 编排健康检查         |

***

## 8. 坐标系统说明

### 8.1 LogicFlow 坐标

- LogicFlow 以**节点中心**为坐标基准
- `getNodeData().x` / `getNodeData().y` 返回中心点坐标

### 8.2 业务存储坐标

- 以**节点左上角**为坐标基准
- 存储在 `dev_orch_node.x_pos` / `dev_orch_node.y_pos`

### 8.3 坐标转换

```typescript
// 画布 → 存储
xPos = Math.round(ln.x - NODE_W / 2)  // 中心点 → 左上角
yPos = Math.round(ln.y - NODE_H / 2)

// 存储 → 画布
x = (n.xPos ?? 0) + NODE_W / 2  // 左上角 → 中心点
y = (n.yPos ?? 0) + NODE_H / 2
```

### 8.4 拖拽坐标转换

```typescript
// 屏幕坐标 → 画布坐标
const rect = containerRef.value.getBoundingClientRect()
const t = lf.getTransform()
const x = (e.clientX - rect.left - t.TRANSLATE_X) / t.SCALE_X
const y = (e.clientY - rect.top - t.TRANSLATE_Y) / t.SCALE_Y
```

***

## 9. 已知问题与注意事项

### 9.1 位置数据丢失问题（已修复）

**根因**: `syncToProps` 的 `isSyncingDown` 守卫在画布渲染时会跳过同步，导致保存时位置数据未更新。

**修复**: `forceSyncNow()` 调用 `syncToProps(true)`，绕过守卫强制同步。

### 9.2 Jackson 字段命名问题（已修复）

**根因**: Java 字段 `xPos` 的 getter 为 `getXPos()`，Jackson 默认推断属性名为 `XPos`，与前端的 `xPos` 不匹配。

**修复**: 添加 `@JsonProperty("xPos")` / `@JsonProperty("yPos")` 注解。

### 9.3 校验回滚问题（已修复）

**根因**: `validate()` 在 `update()` 之后调用，校验失败时事务回滚导致位置数据丢失。

**修复**: 将 `validate()` 移到 `update()` 之前。

### 9.4 唯一约束冲突（已修复）

**根因**: 多次保存时旧的软删除记录累积，`(orchestration_id, node_key, is_deleted=1)` 唯一约束冲突。

**修复**: 保存前先物理删除旧的软删除记录。

### 9.5 菱形锚点偏移（已修复）

**根因**: DiamondNodeModel 默认 `rx=30, ry=50`，而自定义 View 绘制的菱形 `size=54`，锚点位置与顶点不对齐。

**修复**: 设置 `this.rx = this.ry = this.size / 2`。

### 9.6 `xPos=0` 被误判为需要排版（已修复）

**根因**: `!n.xPos` 当 `xPos=0` 时为 `true`，触发自动排版覆盖已保存位置。

**修复**: 改用 `n.xPos == null` 判断。

***

## 10. 扩展指南

### 10.1 添加新节点类型

1. 在 `nodes.ts` 的 `NODE_TYPES` 添加新类型常量
2. 在 `typeToLf` / `lfToType` 添加映射
3. 在 `typeLabels` / `typeColors` 添加显示名和颜色
4. 使用 `createRectNode` / `createCircleNode` / `createDiamondNode` 或自定义函数创建节点
5. 在 `registerOrchNodes` 注册新节点
6. 在 `dndPanelConfig` 添加面板配置
7. 在后端 `OrchestrationNode` 实体添加 `TYPE_*` 常量
8. 在后端校验逻辑中添加新类型的校验规则

### 10.2 添加新连线属性

1. 在 `OrchEdgeVO` 添加新字段
2. 在 `OrchEdgeCmd` 添加对应字段 + `@JsonProperty` 注解
3. 在 `OrchEdge` 实体添加数据库字段
4. 在 `OrchPropertyPanel.vue` 添加编辑表单项
5. 在 `OrchCanvas.vue` 的 `syncToProps` 中同步新字段

### 10.3 自定义布局算法

替换 `autoLayout()` 中的 Dagre 调用，实现自定义布局：

```typescript
function customLayout() {
  // 1. 拓扑排序计算层级
  // 2. 同层节点排序
  // 3. 计算坐标
  // 4. lf.setNodePosition() 设置位置
  // 5. forceSyncNow() 同步到 props
}
```


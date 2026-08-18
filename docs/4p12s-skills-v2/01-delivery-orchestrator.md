# 4p12s-delivery-orchestrator 技能（v2）

## 基本信息

- **技能名称**: 4p12s-delivery-orchestrator
- **技能定位**: 端到端交付系统的初始化器
- **版本**: v2.0.0
- **文件路径**: `docs/4p12s-skills-v2/01-delivery-orchestrator.md`

---

## 一、概述

本 Skill 是端到端交付系统的**初始化器**。它**只做一件事**：在指定交付目录下创建并初始化 `delivery-state.md` 状态账本。

### v2 改进点

- ✅ 新增 `max_repair_rounds` 字段，支持修复轮次弹性配置
- ✅ 新增 `reused` 状态，支持增量交付
- ✅ 新增 `flow_type` 字段，区分完整流程和轻量化流程

### 明确不执行

- ❌ 不调用任何子 Skill
- ❌ 不执行任何 Step（需求、PRD、设计、实现等）
- ❌ 不进行任何验证、修复或回退
- ✅ 只做一件事：初始化 `delivery-state.md`

---

## 二、初始化流程

当用户触发此 Skill 时：

1. 确认项目路径（`repo_root`）和业务需求名称（`delivery_name`）
2. 确认流程类型（`flow_type`）：`full`（完整流程）或 `lightweight`（轻量化流程）
3. 确认修复轮次配置（`max_repair_rounds`）：默认 `8`
4. 创建交付目录 `deliveries/<需求名>/`
5. 在该目录下创建并写入 `delivery-state.md`
6. 输出初始化结果

---

## 三、交付目录结构

```text
deliveries/<需求名>/
└── delivery-state.md
```

仅创建 `delivery-state.md` 一个文件，其它产物文件由对应的子 Skill 在后续 Step 中按需生成。

---

## 四、delivery-state.md 初始结构（v2）

```markdown
# 交付状态

- workflow: end_to_end_delivery
- delivery_name: <需求名>
- flow_type: full | lightweight
- workflow_status: pending
- current_step: requirements
- current_task:
- blocking_reason:
- next_action: 等待用户或编排器触发 4p12s-requirements Skill
- max_repair_rounds: 8
- updated_at: <ISO8601 时间戳>

## Step Checklist

| Step | Artifact | Status | Evidence |
| --- | --- | --- | --- |
| requirements | requirements-register.md | pending | |
| prd | prd.md | pending | |
| user_stories | user-stories.md | pending | |
| design | design.md | pending | |
| verification_plan | verification-plan.md | pending | |
| tasks | tasks/ | pending | |
| implementation | tasks/ | pending | |
| verification_integration | verification-result.md（集成部分） | pending | |
| verification_e2e | verification-result.md（E2E 部分） | pending | |
| git_push | deploy-log.md##Git Push | pending | |
| release_trigger | deploy-log.md##Release Trigger | pending | |

## Task Checklist

| Task ID | Task File | Status | Linked Story/Acceptance | Evidence |
| --- | --- | --- | --- | --- |
```

### v2 新增字段说明

| 字段 | 说明 | 默认值 |
|------|------|--------|
| `flow_type` | 流程类型：`full`（完整）/ `lightweight`（轻量） | `full` |
| `max_repair_rounds` | 集成测试和 E2E 测试的修复轮次上限 | `8` |

---

## 五、合法状态枚举（v2）

### Step/Task 状态

| 状态 | 含义 | 说明 |
|------|------|------|
| `pending` | 尚未开始 | 初始状态 |
| `running` | 正在执行 | 技能执行中 |
| `passed` | 已完成且验证通过 | 阶段成功完成 |
| `blocked` | 被外部因素阻断 | 等待用户输入、业务决策 |
| `failed` | 执行失败 | 业务逻辑失败或系统级错误 |
| `reused` | 复用已有产物 | **v2 新增**，未重新生成 |

### workflow_status

| 状态 | 含义 |
|------|------|
| `pending` | 尚未开始 |
| `running` | 正在执行 |
| `blocked` | 被阻断 |
| `failed` | 失败 |
| `release_triggered` | 发布已触发 |

不得使用 `todo`、`done`、`complete`、`success` 等状态别名。

---

## 六、修复轮次配置建议

| 交付类型 | 建议配置 | 说明 |
|----------|----------|------|
| 轻量变更 | `max_repair_rounds: 3` | 简单变更无需过多修复 |
| 标准交付 | `max_repair_rounds: 8` | 平衡效率和质量 |
| 复杂集成 | `max_repair_rounds: 15` | 微服务集成场景 |

---

## 七、增量交付支持

### 复用已有产物

在初始化时或执行过程中，可以将某个 Step 标记为 `reused`：

```markdown
| Step | Artifact | Status | Evidence |
| --- | --- | --- | --- |
| requirements | requirements-register.md | reused | deliveries/xxx/requirements-register.md |
| prd | prd.md | pending | |
```

**复用规则**:
- ✅ `reused` 状态表示直接引用已有产物，不重新生成
- ✅ 复用前必须确认已有产物的内容仍然有效
- ❌ 不得复用内容已过时的产物

---

## 八、禁止事项

- ❌ 调用任何子 Skill
- ❌ 执行任何 Step
- ❌ 构造路径包、注入项目上下文、扫描约定文档
- ❌ 自动推进、复核产物、修复、回退
- ❌ 修改或创建 `delivery-state.md` 之外的任何文件
- ❌ 在交付目录中预先创建 requirements-register.md、prd.md 等后续产物

---

## 九、最佳实践

### 9.1 需求命名规范

- ✅ 使用简洁明确的业务名称，如"新增用户管理功能"
- ✅ 避免使用技术术语，如"新增 UserController"
- ❌ 避免使用模糊名称，如"功能优化"

### 9.2 流程类型选择

```
是否为新功能开发？
├── 是 → flow_type: full
└── 否 → 是否涉及多模块？
    ├── 是 → flow_type: full
    └── 否 → 是否紧急修复？
        ├── 是 → 考虑使用 4p12s-lightweight-flow
        └── 否 → flow_type: full
```

### 9.3 修复轮次配置

- ✅ 根据项目复杂度配置 `max_repair_rounds`
- ✅ 微服务项目建议设置较高值（10-15）
- ✅ 单体项目建议设置中等值（5-8）

---

## 十、总结

**4p12s-delivery-orchestrator** v2 在保持 v1 单一职责设计的基础上，新增了流程类型区分、修复轮次弹性配置和增量交付支持，使技能体系更加灵活适配不同场景。

---

**文档结束**

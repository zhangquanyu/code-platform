# 4p12s-delivery-orchestrator 技能分析

## 基本信息

- **技能名称**: 4p12s-delivery-orchestrator
- **技能定位**: 端到端交付系统的初始化器
- **版本**: v1.0.3
- **文件路径**: `docs/skills/01-delivery-orchestrator.md`

---

## 原文核心内容

### 概述

本 Skill 是端到端交付系统的**初始化器**。它**只做一件事**：在指定交付目录下创建并初始化 `delivery-state.md` 状态账本。

**明确不执行：**

- 不调用任何子 Skill
- 不执行 4p12s-requirements、4p12s-prd、4p12s-user-stories、4p12s-technical-design、4p12s-verification-plan、4p12s-implementation-tasks、4p12s-implementation-execution、4p12s-integration-test、4p12s-e2e-test、4p12s-git-push、4p12s-deployment-execution 等任何 Step
- 不执行自动推进、不构造路径包、不复核产物
- 不进行任何验证、修复或回退

后续 Step 由用户或其它调度流程按需触发。

### 初始化流程

当用户触发此 Skill 时：

1. 确认项目路径（`repo_root`）和业务需求名称（`delivery_name`）
2. 创建交付目录 `deliveries/<需求名>/`（如不存在）
3. 在该目录下创建并写入 `delivery-state.md`
4. 输出初始化结果（交付目录路径 + delivery-state.md 路径）

完成后立即结束本 Skill，不进入任何后续 Step。

### 交付目录

```text
deliveries/<需求名>/
└── delivery-state.md
```

仅创建 `delivery-state.md` 一个文件，其它产物文件由对应的子 Skill 在后续 Step 中按需生成。

### delivery-state.md 初始结构

```markdown
# 交付状态

- workflow: end_to_end_delivery
- delivery_name: <需求名>
- workflow_status: pending
- current_step: requirements
- current_task:
- blocking_reason:
- next_action: 等待用户或编排器触发 4p12s-requirements Skill
- updated_at: <ISO8601 时间戳，格式示例：2024-01-15T10:30:00+08:00>

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

**注意：** Evidence 列填入产物文件路径，多文件时用英文分号 `;` 分隔。

## Task Checklist

| Task ID | Task File | Status | Linked Story/Acceptance | Evidence |
| --- | --- | --- | --- | --- |
```

**说明：** Task Checklist 由 `4p12s-implementation-tasks` Skill 在 tasks 阶段填充，初始化时为空。

### 合法状态

```text
Step/Task: pending | running | passed | blocked | failed
workflow_status: pending | running | blocked | failed | release_triggered
```

不得使用 `todo`、`done`、`complete`、`success` 等状态别名。

**状态说明：**
- `pending`：尚未开始
- `running`：正在执行
- `passed`：已完成且验证通过
- `blocked`：被外部因素阻断（如等待用户输入、业务决策）
- `failed`：执行失败（包括业务逻辑失败和系统级错误）
- `release_triggered`：发布已触发（workflow_status 专用）

### 初始化输出

初始化完成后，直接声明：本 Skill 已结束，后续 Step 需要由调用方或其它流程触发，不在本 Skill 范围内执行。

### 禁止事项

- 调用任何子 Skill
- 执行 4p12s-requirements、4p12s-prd、4p12s-user-stories、4p12s-technical-design、4p12s-verification-plan、4p12s-implementation-tasks、4p12s-implementation-execution、4p12s-integration-test、4p12s-e2e-test、4p12s-git-push、4p12s-deployment-execution 等 Step
- 构造路径包、注入项目上下文、扫描约定文档
- 自动推进、复核产物、修复、回退
- 修改或创建 `delivery-state.md` 之外的任何文件
- 在交付目录中预先创建 requirements-register.md、prd.md、design.md 等后续产物

---

## 分析说明

### 1. 设计意图

**4p12s-delivery-orchestrator** 是整个 4P12S 技能体系的**入口点**和**状态管理中心**。它的设计遵循了"单一职责原则"和"关注点分离"原则：

- **单一职责**: 只负责初始化交付状态账本，不执行任何实际交付步骤
- **关注点分离**: 将状态管理与具体执行分离，orchestrator 管理状态，子技能负责执行

这种设计的优势：
1. **状态可信**: 状态账本由独立的初始化器创建，避免执行者篡改状态
2. **可恢复性**: 中断后可以从 `current_step` 继续执行
3. **可审计性**: 所有阶段的执行结果都有据可查
4. **灵活性**: 支持自动化编排和手动执行两种模式

### 2. 核心概念解析

#### 2.1 状态账本（delivery-state.md）

状态账本是整个交付流程的"单一事实来源"，记录了：

- **工作流元信息**: workflow、delivery_name、workflow_status、current_step 等
- **Step Checklist**: 11 个阶段的完成状态和证据文件
- **Task Checklist**: 开发任务的执行状态（由 implementation-tasks 填充）

状态账本的作用：
- **进度跟踪**: 实时了解交付进度
- **阻塞管理**: 记录阻塞原因和下一步动作
- **证据管理**: 每个阶段的产物都有明确路径
- **中断恢复**: 从 `current_step` 继续执行

#### 2.2 状态枚举

状态枚举采用统一的五状态模型：

| 状态 | 含义 | 使用场景 |
|------|------|----------|
| `pending` | 尚未开始 | 初始状态或前置步骤未完成 |
| `running` | 正在执行 | 技能执行中 |
| `passed` | 已完成且验证通过 | 阶段成功完成 |
| `blocked` | 被外部因素阻断 | 等待用户输入、业务决策、外部依赖 |
| `failed` | 执行失败 | 业务逻辑失败或系统级错误 |

**注意**: 不使用 `todo`、`done`、`complete`、`success` 等别名，确保状态语义统一。

#### 2.3 交付目录结构

```
deliveries/
└── <需求名>/
    ├── delivery-state.md          # 状态账本（orchestrator 创建）
    ├── requirements-register.md   # 需求登记（requirements 创建）
    ├── prd.md                     # 产品需求文档（prd 创建）
    ├── user-stories.md            # 用户故事（user-stories 创建）
    ├── design.md                  # 技术设计（technical-design 创建）
    ├── verification-plan.md       # 验证计划（verification-plan 创建）
    ├── tasks/                     # 开发任务（implementation-tasks 创建）
    ├── verification-result.md     # 验证结果（integration-test + e2e-test 创建）
    └── deploy-log.md              # 部署日志（git-push + deployment-execution 创建）
```

### 3. 执行流程

```
用户触发
    ↓
确认项目路径 (repo_root)
    ↓
确认需求名称 (delivery_name)
    ↓
创建交付目录 deliveries/<需求名>/
    ↓
创建 delivery-state.md
    ↓
输出初始化结果
    ↓
结束（不执行任何后续 Step）
```

### 4. 使用示例

#### 4.1 独立模式调用

用户直接调用 orchestrator 初始化交付：

```
用户输入：/4p12s-delivery-orchestrator
需求名称：新增用户管理功能
```

输出：
```
交付目录：deliveries/新增用户管理功能/
状态账本：deliveries/新增用户管理功能/delivery-state.md
```

#### 4.2 编排模式调用

被其他流程调用时，接收路径包参数：

```yaml
repo_root: /path/to/project
delivery_name: 新增用户管理功能
delivery_dir: deliveries/新增用户管理功能
state_path: deliveries/新增用户管理功能/delivery-state.md
```

### 5. 与其他技能的关系

```
4p12s-delivery-orchestrator (初始化器)
         ↓
    创建 delivery-state.md
         ↓
    等待用户或编排器触发
         ↓
4p12s-requirements (需求登记)
         ↓
    更新 delivery-state.md
         ↓
    ...后续技能依次执行
```

**关键点**:
- orchestrator 只创建初始状态，不更新状态
- 独立模式下，各技能执行后自行更新 `delivery-state.md`
- 编排模式下，由 orchestrator 统一更新状态

### 6. 最佳实践

#### 6.1 需求命名规范

- ✅ 使用简洁明确的业务名称，如"新增用户管理功能"
- ✅ 避免使用技术术语，如"新增 UserController"
- ❌ 避免使用模糊名称，如"功能优化"、"系统改进"

#### 6.2 交付目录管理

- ✅ 每个需求使用独立的交付目录
- ✅ 需求名称包含中文时，注意路径编码兼容性
- ❌ 不要在交付目录中手动创建文件

#### 6.3 状态账本维护

- ✅ 定期检查 `delivery-state.md` 的状态一致性
- ✅ 在 `blocking_reason` 中详细记录阻塞原因
- ❌ 不要手动修改状态（应由技能自动更新）

### 7. 常见问题

#### Q1: 为什么 orchestrator 不执行任何 Step？

**答**: 这是为了分离"状态管理"和"具体执行"的职责。orchestrator 作为初始化器，只负责创建可信的状态账本。具体执行由子技能完成，避免执行者篡改状态。

#### Q2: 如何恢复中断的交付流程？

**答**: 查看 `delivery-state.md` 中的 `current_step` 字段，从该步骤继续执行。例如：

```yaml
current_step: implementation-tasks
workflow_status: blocked
blocking_reason: 等待用户确认任务拆分方案
```

说明在 tasks 阶段被阻塞，需要用户确认后继续。

#### Q3: 可以手动修改 delivery-state.md 吗？

**答**: 不建议。状态账本应由技能自动更新，手动修改可能导致状态不一致。如需修正，应重新执行对应的技能。

### 8. 适用场景

✅ **适合使用 orchestrator 的场景**:
- 从需求到发布的完整交付流程
- 需要状态跟踪和审计的交付
- 可能中断并需要恢复的长周期交付

❌ **不适合使用 orchestrator 的场景**:
- 简单的文档更新（无需状态跟踪）
- 紧急热修复（流程过于冗长）
- 已有成熟 CI/CD 流程且不需要变更的项目

### 9. 限制与约束

- **只做初始化**: 不执行任何实际交付步骤
- **唯一产物**: 只创建 `delivery-state.md`，不创建其他文件
- **不自动推进**: 初始化完成后立即结束，等待用户或编排器触发后续技能
- **状态枚举固定**: 不得使用 `todo`、`done` 等别名

### 10. 总结

**4p12s-delivery-orchestrator** 是 4P12S 技能体系的入口点，负责初始化交付状态账本。它遵循单一职责原则，只做初始化，不执行任何实际交付步骤。这种设计确保了状态账本的可信性和可审计性，为后续技能执行提供了可靠的状态管理基础。

**关键价值**:
- 状态可信：独立的初始化器创建状态账本
- 可恢复性：支持中断后从当前步骤继续
- 可审计性：所有阶段的执行结果都有据可查
- 灵活性：支持自动化编排和手动执行两种模式

---

**文档结束**

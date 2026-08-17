# 4p12s-user-stories 技能分析

## 基本信息

- **技能名称**: 4p12s-user-stories
- **技能定位**: 用户故事与验收场景生成
- **版本**: v1.0.3
- **文件路径**: `docs/skills/04-user-stories.md`

---

## 原文核心内容

### 概述

从 `requirements-register.md` 和 `prd.md` 生成 `user-stories.md`，将需求转换为可验收的用户故事和 Given/When/Then 场景。

### 双模运行

#### 编排模式

路径包必须包含：

```yaml
delivery_dir:
state_path:
project_entry_path:
requirements_register_path:
prd_path:
user_stories_path:      # 已存在的 user-stories.md（更新场景）
output_paths:
allowed_write_paths:    # 仅 user-stories.md
```

#### 独立模式

自动发现：
- 当前目录或指定目录下的 `requirements-register.md` 和 `prd.md`
- 项目入口文档
- 输出路径：与上游产物同目录的 `user-stories.md`

如果找不到上游产物，提示用户先运行前置 Skill。

### 输出文件

写入唯一产物：`user-stories.md`

```markdown
# <需求名> 用户故事

## 输入依据

| Artifact | Path |
| --- | --- |

## 场景总览

| 用户故事 ID | 验收场景 ID | 场景用例 ID | 来源需求 ID | PRD 验收标准 | 角色 | 目标 | 是否产品行为变更 | 最低验证层级 | 是否提交前自动化验证 | 是否阻断 Git | 说明 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |

## 用户故事

### US-001 <故事标题>

- 来源需求 ID：
- 角色：
- 目标：
- 价值：
- 范围内：
- 范围外：

#### AC-001 <验收场景标题>

- 场景用例 ID：SC-001
- 来源需求 ID：
- 来源 PRD 验收标准：
- 是否产品行为变更：是 / 否
- 最低验证层级：unit / component / service_api / service_integration / cross_boundary_e2e
- 是否允许降级验证：是 / 否
- 降级理由：
- 是否提交前自动化验证：是 / 否
- 是否阻断 Git：是 / 否
- 暂不自动化验证的原因：
- 风险判断：
- 后续处理结论：

```gherkin
Given ...
When ...
Then ...
And ...
```

## 缺口与阻断

| 项目 | 关联需求 ID | 影响 | 下一步动作 |
| --- | --- | --- | --- |
```

### 映射规则

- 范围内的每个需求 ID 必须出现在至少一个 `US-xxx` 中
- PRD 的每个验收标准必须映射到至少一个 `AC-xxx` / `SC-xxx`
- `US-xxx`、`AC-xxx`、`SC-xxx` 三类 ID 由 `user-stories.md` 拥有，是下游的验收入口
- PRD 验收标准是输入依据，下游验收场景 ID 由本文件重新分配
- 如果需要的验收场景没有对应的 PRD 验收标准，返回 `blocked` 而非凭空创建
- 验收场景以黑盒优先：先描述外部可观察行为，再描述内部实现
- 每个 `AC-xxx` 必须包含完整 Given/When/Then
- 主流程、异常流程、边界条件、权限、数据规则、集成点、状态流转和失败路径必须在相关时体现
- 范围内的前端或用户交互验收默认为 `cross_boundary_e2e`，必须标记为 Git 阻断
- 不要引入上游产物中不存在的需求
- 不要使用澄清问题 ID 作为独立源需求 ID

### 质量门禁

返回前验证：

- `user-stories.md` 已创建或更新
- 文件包含 `输入依据`、`场景总览`、`用户故事`、`缺口与阻断`
- 每个范围内需求 ID 都映射到至少一个 `US-xxx`
- PRD 的每个验收标准都映射到至少一个 `AC-xxx` / `SC-xxx`
- 每个 `US-xxx` 都包含角色、目标、价值、范围内和范围外
- 每个 `AC-xxx` 都包含完整 Given/When/Then 和 Git 阻断决策
- 没有创建脱离 PRD 验收标准的下游专属场景
- 范围内的每个前端交互都有 E2E 或跨边界自动化验证路径

### STEP_RESULT（编排模式）

```yaml
step: user_stories
status: running | passed | blocked | failed
output_files:
  - <delivery_dir>/user-stories.md
required_inputs_checked:
  - <delivery_dir>/requirements-register.md
  - <delivery_dir>/prd.md
  - <project_entry_path>
commands_run: none
gate_evidence:
blocking_reason:
next_action:
```

### 边界

- 不创建技术设计、验证计划、任务、代码、提交或部署产物
- 不决定流程终点状态

### 更新 delivery-state.md（仅独立运行模式）

> **编排模式**（被 `4p12s-delivery-orchestrator` 调用）下，`delivery-state.md` 由 orchestrator 统一处理，本 Skill **不直接更新**。

仅在**独立运行模式**（用户直接调用本 Skill）下，本 Step 完成后（或 `failed` / `blocked` 时）必须更新 `<state_path>`：

1. `## Step Checklist` 表中当前 Step 对应行的 `Status` 改为 `passed` / `failed` / `blocked`；`Evidence` 列填入产物文件路径（多文件用 `；` 分隔）
2. 顶部字段：`current_step` 设为下一 Step（`passed` 时）或保持当前 Step（`failed` / `blocked` 时）；`next_action` 设为对下一/当前 Step 的提示；`blocked` / `failed` 时填写 `blocking_reason`；`updated_at` 改为当前 ISO8601 时间戳
3. 不修改其他 Step / Task 的状态

---

## 分析说明

### 1. 设计意图

**4p12s-user-stories** 是 4P12S 技能体系的**验收场景生成器**，负责将 PRD 转换为可测试的用户故事和 Given/When/Then 验收场景。它的核心价值在于：

- **用户视角**: 从用户角度描述功能价值
- **验收场景**: 使用 Given/When/Then 格式描述可测试的验收场景
- **验证层级**: 为每个验收场景指定最低验证层级
- **Git 阻断决策**: 决定哪些验收场景必须提交前验证

这种设计的优势：
1. **可测试性**: Given/When/Then 格式可以直接转换为测试用例
2. **验证分层**: 不同场景使用不同验证层级，避免过度测试
3. **质量门禁**: Git 阻断决策确保关键验收必须提交前验证
4. **需求追溯**: 每个用户故事都可以追溯到源需求 ID

### 2. 核心概念解析

#### 2.1 ID 系统（3 种类型）

user-stories 拥有 3 种 ID 类型：

| ID 类型 | 格式 | 拥有者 | 说明 |
|--------|------|--------|------|
| 用户故事 ID | `US-xxx` | user-stories.md | 用户故事唯一标识 |
| 验收场景 ID | `AC-xxx` | user-stories.md | 验收场景唯一标识 |
| 场景用例 ID | `SC-xxx` | user-stories.md | 场景用例唯一标识 |

**关键点**:
- PRD 验收标准是输入依据，下游验收场景 ID 由本文件重新分配
- 不要使用澄清问题 ID 作为独立源需求 ID
- 已有 ID 不得重新编号

#### 2.2 验证层级（5 个层级）

验证层级从低到高分为 5 级：

| 层级 | 说明 | 适用场景 | 示例 |
|------|------|----------|------|
| `unit` | 单元测试 | 隔离被测对象，验证单一逻辑 | 验证手机号格式 |
| `component` | 组件测试 | 验证模块内部协作 | 验证 Service 层逻辑 |
| `service_api` | 服务 API 测试 | 验证 HTTP/RPC 接口 | 验证 REST API |
| `service_integration` | 服务集成测试 | 验证服务间调用 | 验证 Service 调用外部 API |
| `cross_boundary_e2e` | 跨边界 E2E | 验证完整用户流 | 验证用户注册完整流程 |

**默认规则**: 范围内的前端或用户交互验收默认为 `cross_boundary_e2e`，必须标记为 Git 阻断。

#### 2.3 Git 阻断决策

Git 阻断决策决定哪些验收场景必须提交前验证：

| 字段 | 说明 | 取值 |
|------|------|------|
| 是否提交前自动化验证 | 是否需要在提交前自动化验证 | 是/否 |
| 是否阻断 Git | 未执行是否阻断 Git 提交 | 是/否 |
| 暂不自动化验证的原因 | 如果不自动化，说明原因 | 文本 |

**默认规则**:
- ✅ 前端交互验收 → `cross_boundary_e2e` → 提交前自动化验证=是 → 阻断 Git=是
- ✅ 核心业务逻辑 → `service_api` 或 `service_integration` → 提交前自动化验证=是 → 阻断 Git=是
- ❌ 性能测试、安全测试 → 提交前自动化验证=否 → 阻断 Git=否

### 3. Given/When/Then 格式

Given/When/Then 是行为驱动开发（BDD）的标准格式：

```gherkin
Given <前置条件>
When <触发事件>
Then <预期结果>
And <额外预期>
```

**示例**:
```gherkin
Given 用户访问注册页面
When 用户输入手机号"13800138000"和验证码"123456"，点击注册按钮
Then 系统创建用户，用户状态为已激活
And 跳转到登录页
And 显示"注册成功"提示
```

**关键点**:
- Given 描述前置条件
- When 描述触发事件
- Then 描述预期结果
- And 可以追加多个预期结果

### 4. 映射规则

#### 4.1 需求 ID 映射

**规则**: 范围内的每个需求 ID 必须出现在至少一个 `US-xxx` 中。

**示例**:
```markdown
### US-001 用户注册
- 来源需求 ID：FUNC-001, RULE-001, UI-001
```

#### 4.2 PRD 验收标准映射

**规则**: PRD 的每个验收标准必须映射到至少一个 `AC-xxx` / `SC-xxx`。

**示例**:
```markdown
#### AC-001 用户注册成功
- 来源 PRD 验收标准：用户输入手机号和验证码后，点击注册按钮，系统创建用户并跳转到登录页
```

#### 4.3 禁止凭空创建

**规则**: 如果需要的验收场景没有对应的 PRD 验收标准，返回 `blocked` 而非凭空创建。

**示例**:
- ✅ PRD 验收标准："用户注册成功后跳转到登录页" → AC-001
- ❌ 凭空创建："用户注册成功后发送邮件"（PRD 中没有）→ 返回 blocked

### 5. 质量门禁

返回前必须验证的 7 项要求：

| 验证项 | 说明 | 检查方法 |
|--------|------|----------|
| 文件结构完整 | 包含 `输入依据 `、`场景总览`、` 用户故事`、` 缺口与阻断` | 检查 Markdown 结构 |
| 需求 ID 映射 | 每个范围内需求 ID 都映射到至少一个 `US-xxx` | 检查需求登记表 |
| PRD 验收标准映射 | PRD 的每个验收标准都映射到至少一个 `AC-xxx` / `SC-xxx` | 检查 PRD |
| 用户故事完整 | 每个 `US-xxx` 都包含角色、目标、价值、范围内和范围外 | 检查用户故事 |
| 验收场景完整 | 每个 `AC-xxx` 都包含完整 Given/When/Then 和 Git 阻断决策 | 检查验收场景 |
| 无下游专属场景 | 没有创建脱离 PRD 验收标准的下游专属场景 | 检查 PRD 验收标准 |
| 前端交互有 E2E | 范围内的每个前端交互都有 E2E 或跨边界自动化验证路径 | 检查验证层级 |

### 6. 执行流程

```
读取 requirements-register.md
    ↓
读取 prd.md
    ↓
提取 PRD 验收标准
    ↓
生成用户故事（US-xxx）
    ↓
生成验收场景（AC-xxx / SC-xxx）
    ↓
编写 Given/When/Then
    ↓
指定验证层级
    ↓
决策 Git 阻断
    ↓
检查质量门禁
    ↓
门禁通过？──NO──→ 修复问题
    │
    YES
    ↓
写入 user-stories.md
    ↓
返回 STEP_RESULT (passed/blocked/failed)
```

### 7. 与其他技能的关系

```
requirements-register.md + prd.md
    ↓
4p12s-user-stories
    ↓
user-stories.md (用户故事 + 验收场景)
    ↓
4p12s-technical-design
    ↓
design.md (技术设计)
```

**关键点**:
- user-stories 是 PRD 到技术设计的桥梁
- 验收场景是下游验证计划的输入依据
- Git 阻断决策影响 downstream 的验证策略

### 8. 最佳实践

#### 8.1 Given/When/Then 编写

- ✅ Given 描述前置条件
- ✅ When 描述触发事件
- ✅ Then 描述预期结果
- ✅ And 追加多个预期结果
- ❌ 不要混合 Given/When/Then（如 When 中包含 Then）

#### 8.2 验证层级指定

- ✅ 前端交互 → `cross_boundary_e2e`
- ✅ API 接口 → `service_api`
- ✅ 服务集成 → `service_integration`
- ✅ 单一逻辑 → `unit`
- ❌ 不要过度使用 `unit`（前端交互必须用 `cross_boundary_e2e`）

#### 8.3 Git 阻断决策

- ✅ 核心业务逻辑 → 阻断 Git
- ✅ 前端交互 → 阻断 Git
- ✅ 性能测试 → 不阻断 Git
- ❌ 不要将所有场景都标记为不阻断 Git

### 9. 常见问题

#### Q1: 如何判断验证层级？

**答**: 根据验证对象和边界判断：
- 单一逻辑 → `unit`
- 模块内部协作 → `component`
- HTTP/RPC 接口 → `service_api`
- 服务间调用 → `service_integration`
- 完整用户流 → `cross_boundary_e2e`

#### Q2: 什么情况标记为 Git 阻断？

**答**: 以下情况标记为 Git 阻断：
- 前端交互验收
- 核心业务逻辑
- 数据持久化
- 权限验证
- 状态流转

以下情况不标记为 Git 阻断：
- 性能测试
- 安全测试
- 兼容性测试

#### Q3: 可以创建 PRD 中没有的验收场景吗？

**答**: 不可以。如果需要的验收场景没有对应的 PRD 验收标准，返回 `blocked` 而非凭空创建。

### 10. 适用场景

✅ **适合使用 user-stories 的场景**:
- 已有 PRD 和源需求登记
- 需要可测试的验收场景
- 需要 Git 阻断决策

❌ **不适合使用 user-stories 的场景**:
- 没有 PRD 或源需求登记
- 简单的文档更新（无需验收场景）
- 紧急热修复（流程过于冗长）

### 11. 限制与约束

- **唯一产物**: 只创建 `user-stories.md`，不创建其他文件
- **不创建下游产物**: 不创建 design.md、verification-plan.md、tasks/等
- **不开始实现**: 不开始实现、测试、Git 或发布
- **不凭空创建**: 不要引入上游产物中不存在的需求

### 12. 总结

**4p12s-user-stories** 是 4P12S 技能体系的验收场景生成器，负责将 PRD 转换为可测试的用户故事和 Given/When/Then 验收场景。它通过验证层级指定和 Git 阻断决策，确保关键验收必须提交前验证。

**关键价值**:
- 用户视角：从用户角度描述功能价值
- 验收场景：使用 Given/When/Then 格式描述可测试的验收场景
- 验证分层：不同场景使用不同验证层级，避免过度测试
- Git 阻断决策：决定哪些验收场景必须提交前验证

---

**文档结束**

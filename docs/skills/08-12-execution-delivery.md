# 4P12S 技能体系 - 执行与交付阶段分析

## 概述

本文档分析 4P12S 技能体系的第 4-6 阶段技能，包括开发任务执行、验证测试和交付发布。

---

## 阶段 4: 开发实现

### 4p12s-implementation-execution（开发任务执行）

#### 核心职责

执行选定的开发 Task，按 TDD 四步骤逐项完成：写失败测试 → 红灯验证 → 实现 → 绿灯验证。

#### 核心原则：自动连续执行

**关键心智模型**:

1. **Task 不是交付点**: 每个 Task 只是中间步骤，不是独立交付物。不要在每个 Task 完成后等待用户确认。
2. **默认连续执行**: 除非遇到 `failed` / `blocked`，否则必须自动继续执行下一个 Task。
3. **总结≠停止**: 可以在每个 Task 完成后简要记录，但记录后必须立即开始下一个 Task。
4. **只在阻塞时停止**: TDD 绿灯验证真实失败、外部依赖缺失、需要用户决策。

**反模式（必须避免）**:
- ❌ 每个 Task 完成后输出详细总结并等待用户确认
- ❌ 说"TASK-XXX 已完成，接下来需要执行 TASK-YYY，请确认"
- ❌ 把编译警告、lint 提示当作 `failed` 停下来
- ❌ 因为判断某个 Task"不适合 TDD"或"太简单"而跳过它

#### 执行顺序约束（强制）

**必须严格按照 `tasks/index.md` 的 `Task 目录清单` 表格行序，从第一个未完成的 Task 开始逐个执行。**

- ✅ 禁止跳选、禁止重排
- ✅ 第一个"未完成"的 Task 就是当前必须执行的 Task
- ❌ 禁止以"该任务不适合 TDD""先做更有价值的任务"等理由跳过行序靠前的未完成 Task

**"未完成"的判定标准**:
- ✅ 已完成的定义：Task 文件中所有 Step checkbox 均为 `- [x]` **且** `## 结果记录` 中已有 `tdd_green_result` 证据
- ❌ 其余情况一律视为**未完成**

#### TDD 四步骤（强制要求）

1. **Step 1 编写失败测试**: 目标测试文件、测试意图、关键断言
2. **Step 2 运行红灯验证**: cwd、command、期望结果（失败）、证据
3. **Step 3 实现最小产品代码**: 变更文件、实现说明
4. **Step 4 运行绿灯验证**: cwd、command、期望结果（通过）、证据

**约束**:
- ❌ 不得跳过红灯步骤
- ❌ 编译、lint、类型检查不能作为红灯验证
- ✅ 每个步骤都必须有结构化证据
- ✅ 只有绿灯验证通过后才能标记 Task 为 `passed`

#### 返回状态

- `passed`: 当前 Task 所有 TDD 步骤完成且绿灯验证通过 → **继续执行下一个 Task**
- `failed`: 绿灯验证真实失败 → **停止执行**
- `blocked`: 遇到外部依赖缺失或环境问题 → **停止执行**

---

## 阶段 5: 验证测试

### 4p12s-integration-test（集成测试）

#### 核心职责

运行真实 API 和服务间集成验证，在内部完成运行、归因、修复、重跑的完整循环。

#### 执行流程（三步强规则）

**STEP-01: 服务启动**（强规则：必须执行）
- 从 `verification-plan.md` 提取所需真实服务类型、测试数据要求
- 从项目入口文档发现后端/API 启动命令、地址/端口、健康检查方式
- 实际执行启动命令，在后台启动后端服务
- 执行健康检查
- ❌ 缺少任一证据都不得返回 `passed`

**STEP-02: 测试数据准备**
- 按 `verification-plan.md` 中的测试数据要求准备初始数据
- 记录数据准备的最小执行证据

**STEP-03: 运行集成测试**
- 执行从项目入口文档发现的集成测试命令
- 逐项执行每个集成测试 TC
- 记录每个 TC 的最小执行证据

#### 修复循环

1. 对每个失败 TC，归因失败原因（产品代码、测试代码、环境、数据、配置）
2. 渐进式披露：修复代码时，只读取关联条目
3. 修复对应的产品代码或测试代码
4. 重跑失败 TC + 受影响的回归项
5. 重复直到全部通过或达到 5 轮上限
6. 超过 5 轮后返回 `blocked` 或 `failed`

#### 强规则（必须遵守）

- ✅ **没有服务启动证据不得返回 `passed`**
- ✅ **没有健康检查结果不得返回 `passed`**
- ✅ **"测试代码已创建"、"文件存在"、"CI 稍后运行"、纯编译通过不是集成测试通过证据**
- ✅ **每个集成测试 TC 必须有真实执行记录**

---

### 4p12s-e2e-test（端到端测试）

#### 核心职责

完整的 E2E 阶段：写测试代码 → 启动真实服务 → 准备数据 → 执行真实 E2E → 修复失败 → 重跑直到通过。

**关键约束**:
- ✅ 开发任务阶段**不写**E2E 测试代码
- ✅ 本阶段**必须**根据 `verification-plan.md` 中的测试用例契约补齐 E2E 测试代码

#### E2E 测试代码编写要求

- ✅ 使用 `verification-plan.md` 指定的目标测试文件
- ✅ 覆盖每个 E2E / `cross_boundary_e2e` TC
- ✅ 前端用户流测试必须通过真实前端和真实后端/API 交互
- ✅ 测试必须包含真实接口断言，不能只断言静态页面、loading、路由跳转或 mock 响应
- ❌ 禁止在 E2E 测试代码中使用 `page.route().fulfill()`、`intercept`、`mock`、`jest.fn()` 或任何请求拦截/伪造响应的手段

#### 服务启动和真实验证（强制要求）

- ✅ 前端用户流：必须启动或连接前端服务和后端/API 服务
- ✅ API E2E / 跨服务 E2E：必须启动或连接目标 API/服务和必要依赖服务
- ✅ 记录最小执行证据
- ✅ 执行每个必需服务的健康检查
- ❌ 健康检查不通过不得继续执行 E2E

#### 通过条件（全部满足才返回 `passed`）

- ✅ 每个 E2E TC 都有目标测试代码
- ✅ 当前 E2E 用例所需的所有真实服务都已启动或连接，并有健康检查证据
- ✅ 前端用户流包含真实前端和真实后端/API 证据
- ✅ 测试数据已准备或明确无需准备
- ✅ E2E 命令已真实执行
- ✅ 所有提交前必须执行的 E2E TC 都通过
- ✅ 每个 E2E TC 都有真实接口断言证据
- ❌ 只写了测试代码但没有运行 → 不得返回 `passed`
- ❌ 只运行了前端 webServer 或静态页面检查 → 不得返回 `passed`
- ❌ 没有真实服务或真实 API 证据 → 不得返回 `passed`
- ❌ 使用 mock 结果作为通过证据 → 不得返回 `passed`

---

## 阶段 6: 交付发布

### 4p12s-git-push（Git 推送）

#### 核心职责

执行交付流程的 Git 提交和推送步骤，验证通过后仅暂存交付相关文件、提交、推送、验证远端 HEAD。

#### 前置检查（强约束）

- ❌ `verification-result.md` 缺失、为空、没有 `## E2E 验证结果`、E2E 验证结论不是通过、列出了 Git 阻断项 → 返回 `blocked`
- ❌ `delivery-state.md` 中的 `verification_e2e` Step 不是 `passed`（编排模式下）→ 返回 `blocked`

#### 提交信息规范（commit-msg 格式，核心约束）

```
${commit}:${taskId}_${name}:${submitDescription}
```

> ⚠️ **关键细节**: `name` 与 `submitDescription` 之间的冒号是**中文全角冒号 `：`**，其余均为英文半角。

| 字段 | 说明 | 取值约束 |
|------|------|----------|
| `commit` | 提交类型 | `Feat`（需求）/ `Task`（任务）/ `BugFix`（缺陷） |
| `taskId` | 任务/需求 ID，不可含下划线 `_` | 如 `33413` |
| `name` | 任务/需求名称，**可含中文冒号 `：`** | 如 `新增 harness 的使用` |
| `submitDescription` | 本次提交描述，不可为空 | 如 `add harness L1 integration` |

**示例**:
```
Feat:33413_新增 harness 的使用：add harness L1 integration
Task:33414_代码审查优化：optimize review logic
BugFix:33415_修复空指针：resolve null pointer exception
```

**❌ 错误示例**（name 中被插入了全角空格）:
```
Task:33413_新增 harness 的使用：refactor wiki structure
```
原始 title 是 `新增 harness 的使用`，中间没有空格。插入的全角空格会导致 husky hook 校验失败。

#### 任务信息提取流程（提交前必须执行）

1. **搜索上下文中的任务信息**:
   - 当前会话上下文：`<work_task_context source="selected_work_task">`
   - 编排模式的 `tasks/index.md`：已通过的 Task 记录
   - `delivery-state.md` 中的任务关联字段

2. **提取字段映射**:
   - `<task_type>` → `commit`（`DEFECT`→`BugFix`；`BUSINESS_REQUIREMENT`→`Feat`；`TECHNICAL_REQUIREMENT`→`Task`）
   - `<task_id>` → `taskId`
   - `<title>`（仅去掉 `【...】` 前缀标签） → `name`
   - `git diff --stat` + 暂存文件的变更摘要 → `submitDescription`

3. **任务信息缺失时的处理**:
   - 提示用户填写任务信息
   - 用户选择"强制跳过"时使用默认值：`Task:0000_untitled：update files per git diff stat`

#### deploy-log.md##Git Push 写入规则（追加，不覆盖）

- ❌ 不存在：创建文件，写入标题和章节，再追加本次记录
- ❌ 存在但没有 `## Git Push` 章节：在文件末尾追加该章节，再追加本次记录
- ✅ 已有 `## Git Push` 章节：完整读取文件现有内容，把本次记录追加到该章节末尾
- ❌ 每次执行本 Skill 只追加一条新记录；**严禁删除、覆盖或重排历史 Git Push 记录**
- ✅ 记录编号自增：`record_no` = 现有 Git Push 记录条数 + 1，首条为 1

---

### 4p12s-deployment-execution（发布触发）

#### 核心职责

在 Git 推送成功后触发发布 API 或发布脚本，并将返回状态写入 `deploy-log.md##Release Trigger`。

**只产出固定结论**：请求已受理、请求失败、凭证缺失、入口缺失、结果无法判断。

**注意**: Pull Request / Merge Request 创建、MR ready、等待合并都不是发布触发。

#### 发布入口发现（优先级顺序）

1. 项目根目录 `scripts/deploy.sh`
2. 项目入口文档指定的发布脚本、命令、CI/CD 平台或发布说明
3. `package.json`、Makefile、CI 配置或仓库脚本中明确命名的发布命令
4. 用户在当前交付中显式指定的发布方式
5. ❌ 如果无法确定发布入口、发布环境、目标分支或凭证来源，返回 `blocked`，不得猜测

#### 凭证规则（安全约束）

- ❌ 不得在 Skill 指令、交付文档或代码中写入 CI/CD webhook URL、api-token、账号密码或长期凭证
- ✅ 发布凭证只能来自项目认可的位置（环境变量、凭证管理器、CI/CD 平台配置、项目入口文档）
- ❌ 如果发布命令需要凭证但当前环境不可用，返回 `blocked`

#### 执行流程

1. 读取 `delivery-state.md`、`deploy-log.md`、项目入口文档和发布说明
2. 从最新一条 Git Push 记录中提取提交哈希、目标分支和远端信息
3. 发现发布入口，优先使用 `scripts/deploy.sh`
4. 执行发布命令或调用发布 API
5. 记录命令、环境、提交哈希、目标分支、HTTP/进程状态和响应摘要
6. 向 `deploy-log.md##Release Trigger` 追加本次记录
7. 返回 `STEP_RESULT`

#### deploy-log.md##Release Trigger 写入规则（追加，不覆盖）

- ❌ 没有 `## Release Trigger` 章节：在文件末尾追加该章节，再追加本次记录
- ✅ 已有 `## Release Trigger` 章节：完整读取文件现有内容，把本次记录追加到该章节末尾
- ❌ 每次执行本 Skill 只追加一条新记录；**严禁删除、覆盖或重排历史 Release Trigger 记录**
- ✅ 记录编号自增：`record_no` = 现有 Release Trigger 记录条数 + 1，首条为 1

#### 结论枚举（固定值，不得发明新状态）

- `请求已受理`
- `请求失败`
- `凭证缺失`
- `入口缺失`
- `结果无法判断`

---

## 总结

### 执行阶段核心约束

| 技能 | 核心约束 |
|------|----------|
| implementation-execution | 严格按行序执行 Task；自动连续执行；TDD 四步骤必须完整 |
| integration-test | 服务启动证据必须完整；健康检查必须通过；真实执行记录 |
| e2e-test | 真实服务和真实 API 证据；禁止使用 mock；健康检查必须通过 |
| git-push | commit-msg 格式必须正确（中文全角冒号）；任务信息必须提取；deploy-log 追加不覆盖 |
| deployment-execution | 发布入口必须明确；凭证来源必须合法；结论使用固定枚举 |

### 禁止事项汇总

- ❌ implementation-execution: 跳过行序靠前的 Task；在每个 Task 完成后等待用户确认
- ❌ integration-test: 没有服务启动证据就返回 passed；使用 mock 结果
- ❌ e2e-test: 使用 mock 结果；没有真实服务证据；只写测试代码不运行
- ❌ git-push: 触发发布；创建 PR/MR；改写 deploy-log 历史记录
- ❌ deployment-execution: 决定 workflow 终点；创建 PR/MR；改写 deploy-log 历史记录

### 最佳实践

- ✅ implementation-execution: 严格按行序执行；自动连续执行；记录结构化证据
- ✅ integration-test: 优先启动服务；执行健康检查；记录最小执行证据
- ✅ e2e-test: 使用真实服务和 API；禁止 mock；准备测试数据
- ✅ git-push: 提取任务信息；组装 commit-msg；追加 deploy-log 记录
- ✅ deployment-execution: 发现发布入口；使用合法凭证；记录固定结论

---

**文档结束**

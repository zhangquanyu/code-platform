# 4p12s-implementation-tasks 技能分析

## 基本信息

- **技能名称**: 4p12s-implementation-tasks
- **技能定位**: 开发任务计划
- **版本**: v1.0.3
- **文件路径**: `docs/skills/07-implementation-tasks.md`

---

## 原文核心内容

### 概述

把已确认的产品范围和技术设计转成 `tasks/` 下的开发计划。每个 Task 都是可独立 TDD 完成的小计划，步骤使用 Markdown checkbox，执行者可以按步骤 TDD 完成。

本阶段只拆开发任务。不拆 E2E 测试代码任务，不写验证执行任务，不写发布任务。

### 双模运行

#### 编排模式

路径包必须包含：

```yaml
repo_root:
delivery_dir:
state_path:
project_entry_path:
requirements_register_path:
prd_path:
user_stories_path:
design_path:
output_paths:
allowed_write_paths:    # 仅 tasks/ 目录
```

#### 独立模式

自动发现：
- 当前目录或指定目录下的 `requirements-register.md`、`prd.md`、`user-stories.md`、`design.md`
- 项目入口文档、编码规范、测试规范
- 输出路径：与上游产物同目录的 `tasks/`

独立模式下需要聚焦读取与本次设计相关的源码和测试目录。

### 输出

```text
tasks/
├── index.md
├── TASK-001.md
└── TASK-xxx.md
```

不得写入：`delivery-state.md`、`verification-result.md`、`deploy-log.md`、产品代码、测试代码。

### 拆分原则

每个 Task 应对应一个可独立 TDD 完成的产品行为、服务能力、页面行为、接口能力或技术支撑变更。

每个 Task 必须包含：
- 清晰目标
- 具体文件清单
- 失败测试步骤
- 红灯验证命令
- 实现步骤
- 绿灯验证命令
- 结果记录区

拆分规则：
- 按用户故事、验收标准、设计模块和实际写集聚合
- 同一业务行为涉及 DTO、类型、路由、配置、页面、服务编排时，合并到直接使用它们的 Task
- 不生成独立 DTO、类型定义、路由、配置或纯文件改造 Task
- 不生成 "后续补测试" Task
- 不生成 E2E 测试代码 Task
- Task ID 就是执行顺序；后面的 Task 可以依赖前面的，前面的不得依赖后面的

### tasks/index.md 格式

（详见原文）

### TASK-xxx.md 格式

（详见原文）

### 质量门禁

返回前验证：

- `tasks/index.md` 已生成
- 每个 Task 文件已生成
- 每个 Task 在 `Task 目录清单` 中恰好出现一次
- Task 顺序和依赖方向合法
- 每个 Task 都包含失败测试、红灯验证、实现、绿灯验证四个基础步骤
- 每个 Task 都有明确文件计划
- 每个 Task 的红灯/绿灯命令来自项目规范或聚焦配置查找
- 没有 E2E 测试代码任务、集成测试执行任务、Git 任务或发布任务

### 边界

- 不实现代码
- 不执行测试
- 不做 Git 或发布

---

## 分析说明

### 1. 设计意图

**4p12s-implementation-tasks** 是 4P12S 技能体系的**开发任务拆分器**，负责将技术设计拆分为可独立 TDD 完成的开发任务。它的核心价值在于：

- **任务拆分**: 将技术设计拆分为可执行的小任务
- **TDD 计划**: 每个 Task 都包含 TDD 四步骤
- **依赖管理**: 管理 Task 之间的依赖关系
- **文件计划**: 为每个 Task 指定具体的文件清单

这种设计的优势：
1. **可执行性**: 每个 Task 都是可独立 TDD 完成的小计划
2. **TDD 强制**: 每个 Task 都包含 TDD 四步骤，确保测试覆盖
3. **依赖清晰**: Task 之间的依赖关系明确
4. **文件明确**: 每个 Task 都有具体的文件清单

### 2. 核心概念解析

#### 2.1 Task 拆分原则

**核心原则**: 每个 Task 应对应一个可独立 TDD 完成的产品行为、服务能力、页面行为、接口能力或技术支撑变更。

**拆分规则**:
- ✅ 按用户故事、验收标准、设计模块和实际写集聚合
- ✅ 同一业务行为涉及 DTO、类型、路由、配置、页面、服务编排时，合并到直接使用它们的 Task
- ❌ 不生成独立 DTO、类型定义、路由、配置或纯文件改造 Task
- ❌ 不生成 "后续补测试" Task
- ❌ 不生成 E2E 测试代码 Task
- ✅ Task ID 就是执行顺序；后面的 Task 可以依赖前面的，前面的不得依赖后面的

**示例**:
```
✅ 正确拆分:
TASK-001: 用户注册 Controller + Service + Repository
TASK-002: 手机号验证逻辑
TASK-003: 注册页面 Vue 组件

❌ 错误拆分:
TASK-001: 创建 User DTO
TASK-002: 创建 User Entity
TASK-003: 创建路由配置
```

#### 2.2 TDD 四步骤

每个 Task 必须包含 TDD 四步骤：

```markdown
## TDD 步骤

- [ ] Step 1: 编写失败测试
  - 目标测试文件：
  - 测试意图：
  - 关键断言：

- [ ] Step 2: 运行红灯验证
  - cwd:
  - command:
  - 期望结果：失败，且失败原因指向目标能力缺失
  - 证据：

- [ ] Step 3: 实现最小代码
  - 变更文件：
  - 实现说明：

- [ ] Step 4: 运行绿灯验证
  - cwd:
  - command:
  - 期望结果：通过
  - 证据：
```

**关键点**:
- ✅ Step 1 编写失败测试：定义期望行为
- ✅ Step 2 运行红灯验证：确认失败原因指向目标能力缺失
- ✅ Step 3 实现最小代码：只实现让测试通过的最小代码
- ✅ Step 4 运行绿灯验证：确认测试通过
- ❌ 不要跳过红灯步骤

### 3. Task 文件结构

TASK-xxx.md 包含以下章节：

```markdown
# TASK-001 <任务名>

## 目标
- 说明：
- 关联需求 ID：
- 关联用户故事/验收标准：
- 依赖 Task：

## 文件计划
- Create:
- Modify:
- Test:

## TDD 步骤
- [ ] Step 1: 编写失败测试
- [ ] Step 2: 运行红灯验证
- [ ] Step 3: 实现最小代码
- [ ] Step 4: 运行绿灯验证

## 结果记录
- changed_files:
- tests_added_or_changed:
- commands_run:
- tdd_red_result:
- tdd_green_result:
- requirement_ids_covered:
- user_stories_covered:
- remaining_risks:
```

### 4. 质量门禁

返回前必须验证的 8 项要求：

| 验证项 | 说明 | 检查方法 |
|--------|------|----------|
| index.md 已生成 | `tasks/index.md` 已生成 | 检查文件存在 |
| Task 文件已生成 | 每个 Task 文件已生成 | 检查文件存在 |
| Task 唯一 | 每个 Task 在 `Task 目录清单` 中恰好出现一次 | 检查 index.md |
| Task 顺序合法 | Task 顺序和依赖方向合法 | 检查依赖关系 |
| TDD 步骤完整 | 每个 Task 都包含失败测试、红灯验证、实现、绿灯验证四个基础步骤 | 检查 Task 文件 |
| 文件计划明确 | 每个 Task 都有明确文件计划 | 检查文件计划 |
| 命令来自项目规范 | 每个 Task 的红灯/绿灯命令来自项目规范或聚焦配置查找 | 检查命令来源 |
| 无 E2E 任务 | 没有 E2E 测试代码任务、集成测试执行任务、Git 任务或发布任务 | 检查 Task 列表 |

### 5. 执行流程

```
读取 requirements-register.md + prd.md + user-stories.md + design.md
    ↓
读取项目入口文档、编码规范、测试规范
    ↓
聚焦读取相关源码和测试目录
    ↓
按用户故事、验收标准、设计模块拆分 Task
    ↓
生成 tasks/index.md
    ↓
生成 TASK-001.md, TASK-002.md, ...
    ↓
检查质量门禁
    ↓
门禁通过？──NO──→ 修复问题
    │
    YES
    ↓
写入 tasks/
    ↓
返回 STEP_RESULT (passed/blocked/failed)
```

### 6. 与其他技能的关系

```
requirements-register.md + prd.md + user-stories.md + design.md
    ↓
4p12s-implementation-tasks
    ↓
tasks/ (开发任务计划)
    ↓
4p12s-implementation-execution
    ↓
执行开发任务
```

**关键点**:
- implementation-tasks 是设计到执行的桥梁
- Task 计划是下游执行阶段的输入依据
- TDD 步骤影响执行阶段的 TDD 流程

### 7. 最佳实践

#### 7.1 Task 拆分

- ✅ 按用户故事、验收标准、设计模块拆分
- ✅ 同一业务行为合并到一个 Task
- ✅ Task 大小适中（可在 1-2 小时内完成）
- ❌ 不要拆分为独立的 DTO、类型定义、路由配置 Task

#### 7.2 TDD 步骤

- ✅ Step 1 编写失败测试
- ✅ Step 2 运行红灯验证
- ✅ Step 3 实现最小代码
- ✅ Step 4 运行绿灯验证
- ❌ 不要跳过红灯步骤

#### 7.3 文件计划

- ✅ Create: 列出需要创建的文件
- ✅ Modify: 列出需要修改的文件
- ✅ Test: 列出需要编写或修改的测试文件
- ❌ 不要遗漏文件

### 8. 常见问题

#### Q1: 如何确定 Task 大小？

**答**: Task 大小应适中，可在 1-2 小时内完成。过大则拆分，过小则合并。

#### Q2: 如何处理依赖 Task？

**答**: 在"依赖 Task"字段中列出前置 Task ID。例如：
```markdown
- 依赖 Task：TASK-001, TASK-002
```

#### Q3: 什么是红灯验证？

**答**: 红灯验证是运行失败测试，确认测试失败且失败原因指向目标能力缺失。例如：
- ✅ 红灯：测试失败，提示"方法未实现"
- ❌ 红灯：测试失败，提示"配置错误"（不是目标能力缺失）

### 9. 适用场景

✅ **适合使用 implementation-tasks 的场景**:
- 已有 PRD、用户故事和技术设计
- 需要开发任务计划
- 需要 TDD 流程

❌ **不适合使用 implementation-tasks 的场景**:
- 没有上游产物
- 简单的文档更新（无需开发任务）
- 紧急热修复（流程过于冗长）

### 10. 限制与约束

- **唯一产物**: 只创建 `tasks/` 目录，不创建其他文件
- **不实现代码**: 不实现产品代码
- **不执行测试**: 不执行测试
- **不做 Git 或发布**: 不做 Git 或发布相关操作
- **不拆 E2E 任务**: 不拆分 E2E 测试代码任务

### 11. 总结

**4p12s-implementation-tasks** 是 4P12S 技能体系的开发任务拆分器，负责将技术设计拆分为可独立 TDD 完成的开发任务。它通过 TDD 四步骤、依赖管理和文件计划，确保开发任务可执行、可测试、可追溯。

**关键价值**:
- 任务拆分：将技术设计拆分为可执行的小任务
- TDD 计划：每个 Task 都包含 TDD 四步骤
- 依赖管理：管理 Task 之间的依赖关系
- 文件计划：为每个 Task 指定具体的文件清单

---

**文档结束**

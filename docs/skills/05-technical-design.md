# 4p12s-technical-design 技能分析

## 基本信息

- **技能名称**: 4p12s-technical-design
- **技能定位**: 技术设计方案
- **版本**: v1.0.3
- **文件路径**: `docs/skills/05-technical-design.md`

---

## 原文核心内容

### 概述

为已接受的业务范围生成 `design.md`。连接需求和验收场景到当前项目的具体技术实现。

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
output_paths:
allowed_write_paths:    # 仅 design.md
```

#### 独立模式

自动发现：
- 当前目录或指定目录下的 `requirements-register.md`、`prd.md`、`user-stories.md`
- 项目入口文档和项目结构
- 输出路径：与上游产物同目录的 `design.md`

独立模式下需要主动扫描项目结构以理解现有架构。

### 上下文发现

使用渐进式披露：

1. 首先读取项目入口文档：`.gientech/AGENTS.md`、`AGENTS.md` 或 README
2. 仅跟进本次功能所需的文档
3. 当文档无法识别文件、接口、模型、服务时，使用聚焦的符号或文本搜索
4. 仅读取与已接受范围直接相关的文件
5. 不要扫描整个仓库

### 输出文件

写入唯一产物：`design.md`

```markdown
# <需求名> 技术设计

## 输入依据

| Artifact | Path |
| --- | --- |

## 项目上下文

| 项目项 | 结论 | 来源 |
| --- | --- | --- |

## 实现范围

### 范围内
### 范围外

## 方案设计

### 模块与职责
### 接口、数据流或事件流
### 状态流与边界情况
### 权限、审计、安全或合规
### 配置、数据迁移或兼容性
### 错误处理与恢复

## 设计元素清单

| 设计元素 ID | 类型 | 名称 | 所属模块 | 来源需求 ID | 关联用户故事/验收场景 | 是否需要实现 | 是否需要验证 | 说明 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |

## 需求到设计映射

| 需求 ID | 用户故事/验收场景 | 设计承接 | 影响文件或模块 |
| --- | --- | --- | --- |

## 测试与验证设计

| 用户故事/验收场景 | 验证方式 | 提交前是否必须验证 | 说明 |
| --- | --- | --- | --- |

## 风险与开放问题

| 项目 | 关联需求 ID | 影响 | 下一步动作 | 是否阻断开发计划 |
| --- | --- | --- | --- | --- |
```

### 设计规则

- 范围内的每个需求 ID 必须有设计承接行或已记录的原因
- 每个 Git 阻断验收项必须有设计级验证策略
- 设计引用的 `US-xxx`、`AC-xxx`、`SC-xxx` 必须存在于上游 `user-stories.md` 中
- 不要创建新的用户故事、验收场景或场景用例 ID
- 不要添加上游产物中不存在的需求
- 不要编造文件路径或 API
- 优先使用现有架构和项目规范
- 设计引入的每个具体实现元素必须列在 `设计元素清单` 中
- 每个设计元素必须映射到上游需求 ID 和 `US-xxx` / `AC-xxx` / `SC-xxx`
- 设计必须内部一致
- 如果上游包含前端/页面/表单/交互需求，设计必须包含前端实现范围和 E2E 验证策略
- 不要使用澄清问题 ID 作为独立源需求

### 质量门禁

返回前验证：

- 每个范围内的需求 ID 都有设计承接或已记录的原因
- 每个 Git 阻断验收项都有验证策略
- 每个具体设计元素在 `设计元素清单` 中恰好出现一次
- 每个需要实现或验证的设计元素都映射到至少一个上游需求 ID 和 `US-xxx` / `AC-xxx` / `SC-xxx`
- 每个范围内的前端交互都有前端设计范围和 E2E 验证策略
- 前端范围没有在上游无明确排除时移到 `范围外`
- 没有引入新的下游专属验收场景或需求

### STEP_RESULT（编排模式）

```yaml
step: design
status: running | passed | blocked | failed
output_files:
  - <delivery_dir>/design.md
required_inputs_checked:
  - <delivery_dir>/requirements-register.md
  - <delivery_dir>/prd.md
  - <delivery_dir>/user-stories.md
  - <project_entry_path>
commands_run: none
gate_evidence:
blocking_reason:
next_action:
```

### 边界

- 不创建 `verification-plan.md`、`tasks/`、代码、提交或部署产物
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

**4p12s-technical-design** 是 4P12S 技能体系的**技术设计器**，负责将业务需求和技术设计连接到具体实现。它的核心价值在于：

- **技术翻译**: 将业务需求翻译为技术方案
- **架构一致性**: 优先使用现有架构和项目规范
- **设计追溯**: 每个设计元素都追溯到上游需求
- **验证策略**: 为每个 Git 阻断验收项设计验证策略

这种设计的优势：
1. **业务到技术**: 将抽象的业务需求转换为具体的技术实现
2. **架构一致**: 遵循现有架构，避免重复造轮子
3. **可追溯**: 设计元素可以追溯到源需求和用户故事
4. **验证完整**: 每个 Git 阻断项都有验证策略

### 2. 核心概念解析

#### 2.1 渐进式披露（上下文发现策略）

渐进式披露是一种聚焦的上下文发现策略：

```
1. 读取项目入口文档 (.gientech/AGENTS.md / AGENTS.md / README)
   ↓
2. 仅跟进本次功能所需的文档
   ↓
3. 文档无法识别时，使用聚焦的符号或文本搜索
   ↓
4. 仅读取与已接受范围直接相关的文件
   ↓
5. 禁止扫描整个仓库
```

**关键点**:
- ✅ 优先读取项目入口文档
- ✅ 仅读取与本次功能相关的文件
- ❌ 不要扫描整个仓库

#### 2.2 设计元素清单

设计元素清单是技术设计的核心产出：

| 字段 | 说明 | 示例 |
|------|------|------|
| 设计元素 ID | `DE-xxx` 格式 | DE-001 |
| 类型 | Controller/Service/Repository/Entity/DTO/等 | Controller |
| 名称 | 具体名称 | UserController |
| 所属模块 | 模块路径 | cn.zhangquanyu.application.controller |
| 来源需求 ID | 源需求 ID | FUNC-001 |
| 关联用户故事/验收场景 | US-xxx / AC-xxx | US-001, AC-001 |
| 是否需要实现 | 是/否 | 是 |
| 是否需要验证 | 是/否 | 是 |
| 说明 | 补充说明 | 新增注册接口 |

**示例**:
```markdown
| DE-001 | Controller | UserController | application.controller | FUNC-001 | US-001 | 是 | 是 | 新增注册接口 |
| DE-002 | Service | UserService | application.service | FUNC-001 | US-001 | 是 | 是 | 处理注册逻辑 |
| DE-003 | Entity | User | domain.entity | FUNC-001 | US-001 | 是 | 否 | 用户实体 |
```

#### 2.3 设计承接规则

**规则**: 范围内的每个需求 ID 必须有设计承接行或已记录的原因。

**示例**:
```markdown
## 需求到设计映射

| 需求 ID | 用户故事/验收场景 | 设计承接 | 影响文件或模块 |
|---------|-------------------|----------|----------------|
| FUNC-001 | US-001, AC-001 | DE-001, DE-002, DE-003 | UserController, UserService, User |
| RULE-001 | US-001, AC-002 | DE-002, DE-004 | UserService, PhoneValidator |
| UI-001 | US-001, AC-003 | DE-005 | RegisterView.vue |
```

**禁止事项**:
- ❌ 需求 ID 没有设计承接且没有记录原因
- ✅ 需求 ID 在范围外，原因："业务方明确暂不做"

### 3. 方案设计章节

方案设计包含 6 个子章节：

#### 3.1 模块与职责

描述涉及的模块及其职责：

```markdown
### 模块与职责

- **Controller 层**: 接口适配，DTO 入参校验，VO 统一包装
- **Application 层**: 用例编排，事务边界，调用领域层与基础设施层
- **Domain 层**: 实体 Entity、值对象 VO、聚合根、领域服务、仓储接口
- **Infrastructure 层**: JPA 仓储实现、外部服务调用、配置
```

#### 3.2 接口、数据流或事件流

描述接口定义、数据流或事件流：

```markdown
### 接口、数据流或事件流

用户注册数据流：
1. 用户 → RegisterView.vue → 提交注册表单
2. RegisterView.vue → UserController → POST /api/v1/users/register
3. UserController → UserService → register()
4. UserService → PhoneValidator → validate()
5. UserService → UserRepository → save()
6. UserRepository → MySQL → INSERT INTO dev_user
```

#### 3.3 状态流与边界情况

描述状态流转和边界情况：

```markdown
### 状态流与边界情况

用户状态流转：
未激活 (0) → 验证通过 → 已激活 (1)
已激活 (1) → 管理员禁用 → 已禁用 (2)

边界情况：
- 验证码超时：5 分钟后自动失效
- 验证码重试：最多重试 3 次，超过后锁定
```

#### 3.4 权限、审计、安全或合规

描述权限、审计、安全或合规设计：

```markdown
### 权限、审计、安全或合规

- 权限：管理员可以查看所有用户，普通用户只能查看自己
- 审计：用户创建时间、更新时间通过 JPA Auditing 自动填充
- 安全：密码使用 BCrypt 加密存储
- 合规：手机号脱敏显示
```

#### 3.5 配置、数据迁移或兼容性

描述配置、数据迁移或兼容性设计：

```markdown
### 配置、数据迁移或兼容性

- 配置：新增短信服务配置（sms.api.url, sms.api.key）
- 数据迁移：无需数据迁移
- 兼容性：向后兼容，不影响现有用户
```

#### 3.6 错误处理与恢复

描述错误处理与恢复设计：

```markdown
### 错误处理与恢复

- 验证码错误：返回错误码 1001，提示"验证码错误"
- 手机号已注册：返回错误码 1002，提示"手机号已注册"
- 网络异常：返回错误码 5000，提示"网络异常，请稍后重试"
```

### 4. 质量门禁

返回前必须验证的 7 项要求：

| 验证项 | 说明 | 检查方法 |
|--------|------|----------|
| 需求 ID 有设计承接 | 每个范围内的需求 ID 都有设计承接或已记录的原因 | 检查需求到设计映射表 |
| Git 阻断项有验证策略 | 每个 Git 阻断验收项都有设计级验证策略 | 检查测试与验证设计表 |
| 设计元素唯一 | 每个具体设计元素在 `设计元素清单` 中恰好出现一次 | 检查设计元素清单 |
| 设计元素有映射 | 每个需要实现或验证的设计元素都映射到至少一个上游需求 ID 和 US/AC/SC | 检查设计元素清单 |
| 前端交互有设计 | 每个范围内的前端交互都有前端设计范围和 E2E 验证策略 | 检查方案设计 |
| 前端范围未静默移除 | 前端范围没有在上游无明确排除时移到 `范围外` | 检查实现范围 |
| 无新需求 | 没有引入新的下游专属验收场景或需求 | 检查设计元素 |

### 5. 执行流程

```
读取 requirements-register.md + prd.md + user-stories.md
    ↓
读取项目入口文档
    ↓
渐进式披露上下文（仅读取相关文档）
    ↓
生成项目上下文表
    ↓
生成实现范围（范围内/范围外）
    ↓
生成方案设计（6 个子章节）
    ↓
生成设计元素清单
    ↓
生成需求到设计映射表
    ↓
生成测试与验证设计表
    ↓
生成风险与开放问题表
    ↓
检查质量门禁
    ↓
门禁通过？──NO──→ 修复问题
    │
    YES
    ↓
写入 design.md
    ↓
返回 STEP_RESULT (passed/blocked/failed)
```

### 6. 与其他技能的关系

```
requirements-register.md + prd.md + user-stories.md
    ↓
4p12s-technical-design
    ↓
design.md (技术设计)
    ↓
4p12s-verification-plan
    ↓
verification-plan.md (验证计划)
```

**关键点**:
- design 是用户故事到验证计划的桥梁
- 设计元素清单是下游验证计划的输入依据
- 测试与验证设计表影响验证计划的验证策略

### 7. 最佳实践

#### 7.1 渐进式披露

- ✅ 优先读取项目入口文档
- ✅ 仅读取与本次功能相关的文件
- ❌ 不要扫描整个仓库

#### 7.2 设计元素清单

- ✅ 每个设计元素都有稳定 ID（DE-xxx）
- ✅ 每个设计元素都映射到上游需求
- ❌ 不要编造文件路径或 API

#### 7.3 设计承接

- ✅ 每个需求 ID 都有设计承接或原因
- ✅ 每个 Git 阻断项都有验证策略
- ❌ 不要引入上游不存在的需求

### 8. 常见问题

#### Q1: 如何确定设计元素的类型？

**答**: 根据 DDD 分层架构确定：
- Controller 层：Controller
- Application 层：AppService, DTO, VO
- Domain 层：Entity, ValueObject, DomainService, Repository 接口
- Infrastructure 层：Repository 实现，外部服务调用

#### Q2: 如何处理前端设计？

**答**: 如果上游包含前端需求，设计必须包含：
- 前端组件设计（如 RegisterView.vue）
- 前端状态管理（如 Vuex store）
- 前端 E2E 验证策略

#### Q3: 如何设计验证策略？

**答**: 根据 user-stories 中的验证层级设计：
- unit → 单元测试策略
- service_api → API 测试策略
- cross_boundary_e2e → E2E 测试策略

### 9. 适用场景

✅ **适合使用 technical-design 的场景**:
- 已有 PRD 和用户故事
- 需要技术设计方案
- 需要设计追溯

❌ **不适合使用 technical-design 的场景**:
- 没有 PRD 或用户故事
- 简单的文档更新（无需技术设计）
- 紧急热修复（流程过于冗长）

### 10. 限制与约束

- **唯一产物**: 只创建 `design.md`，不创建其他文件
- **不创建下游产物**: 不创建 verification-plan.md、tasks/等
- **不开始实现**: 不开始实现、测试、Git 或发布
- **不编造**: 不要编造文件路径或 API

### 11. 总结

**4p12s-technical-design** 是 4P12S 技能体系的技术设计器，负责将业务需求翻译为技术方案。它通过渐进式披露、设计元素清单和需求到设计映射，确保设计完整、可追溯、与现有架构一致。

**关键价值**:
- 技术翻译：将业务需求翻译为技术方案
- 架构一致性：优先使用现有架构和项目规范
- 设计追溯：每个设计元素都追溯到上游需求
- 验证策略：为每个 Git 阻断验收项设计验证策略

---

**文档结束**

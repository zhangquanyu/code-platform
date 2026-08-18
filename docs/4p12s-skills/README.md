# 4P12S 端到端交付技能体系

## 文档信息

- **技能版本**: v2.0.0
- **技术栈**: Java 17 + Spring Boot + Maven + Vue3 + DDD
- **包名规范**: `cn.zhangquanyu.<模块名>`
- **文档路径**: `docs/4p12s-skills/`

---

## 一、技能清单

### 完整流程（12 个技能）

| 序号 | 技能名称 | 文件 | 职责 |
|------|----------|------|------|
| 1 | 4p12s-delivery-orchestrator | [01-delivery-orchestrator.md](./01-delivery-orchestrator.md) | 初始化交付状态账本 |
| 2 | 4p12s-requirements | [02-requirements.md](./02-requirements.md) | 需求访谈与登记 |
| 3 | 4p12s-prd | [03-prd.md](./03-prd.md) | 生成产品需求文档 |
| 4 | 4p12s-user-stories | [04-user-stories.md](./04-user-stories.md) | 用户故事与验收场景 |
| 5 | 4p12s-technical-design | [05-technical-design.md](./05-technical-design.md) | 技术设计方案（DDD） |
| 6 | 4p12s-verification-plan | [06-verification-plan.md](./06-verification-plan.md) | 验证计划 |
| 7 | 4p12s-implementation-tasks | [07-implementation-tasks.md](./07-implementation-tasks.md) | 开发任务计划 |
| 8 | 4p12s-implementation-execution | [08-implementation-execution.md](./08-implementation-execution.md) | 开发任务执行（TDD） |
| 9 | 4p12s-integration-test | [09-integration-test.md](./09-integration-test.md) | 集成测试 |
| 10 | 4p12s-e2e-test | [10-e2e-test.md](./10-e2e-test.md) | E2E 测试 |
| 11 | 4p12s-git-push | [11-git-push.md](./11-git-push.md) | Git 推送 |
| 12 | 4p12s-deployment-execution | [12-deployment-execution.md](./12-deployment-execution.md) | 发布触发 |

### 轻量化流程

| 序号 | 技能名称 | 文件 | 职责 |
|------|----------|------|------|
| 0 | 4p12s-lightweight-flow | [00-lightweight-flow.md](./00-lightweight-flow.md) | 简单变更精简流程 |

---

## 二、技能依赖关系图

### 完整流程

```
4p12s-delivery-orchestrator (初始化器)
         ↓
4p12s-requirements (需求登记)
         ↓
4p12s-prd (产品需求文档)
         ↓
4p12s-user-stories (用户故事)
         ↓
4p12s-technical-design (技术设计 - DDD)
         ↓
4p12s-verification-plan (验证计划)
         ↓
4p12s-implementation-tasks (任务计划)
         ↓
4p12s-implementation-execution (任务执行 - TDD)
         ↓
4p12s-integration-test (集成测试)
         ↓
4p12s-e2e-test (E2E 测试)
         ↓
4p12s-git-push (Git 推送)
         ↓
4p12s-deployment-execution (发布触发)
```

### 轻量化流程

```
4p12s-lightweight-flow (精简流程)
    ├── 需求确认（简版）
    ├── 实现（TDD 可选）
    ├── 基础验证（单元测试）
    └── Git 推送
```

---

## 三、技术栈对应关系

### 3.1 后端

| 维度 | 工具 |
|------|------|
| 构建工具 | Maven |
| 测试框架 | JUnit 5 + Mockito |
| 集成测试 | Spring Boot Test + Testcontainers |
| 覆盖率工具 | JaCoCo |
| lint 工具 | checkstyle/spotbugs |
| 启动命令 | `mvn spring-boot:run` |
| 测试命令 | `mvn test` |
| 打包命令 | `mvn clean package` |

### 3.2 前端

| 维度 | 工具 |
|------|------|
| 框架 | Vue3 + Vite |
| E2E 框架 | Cypress / Playwright |
| 测试框架 | Vitest |
| 启动命令 | `npm run dev` |
| 构建命令 | `npm run build` |

### 3.3 DDD 分层架构

```
cn.zhangquanyu.<模块名>
├── interfaces/        # 接口层（Controller、DTO）
├── application/       # 应用层（AppService、Cmd、VO）
├── domain/            # 领域层（Entity、Repository、DomainService）
└── infrastructure/    # 基础设施层（RepositoryImpl、Config）
```

---

## 四、核心设计模式

1. **状态账本模式**: 通过 `delivery-state.md` 统一跟踪交付进度
2. **双模运行模式**: 支持编排模式和独立模式
3. **稳定 ID 追溯模式**: 从需求到代码的完整追溯链
4. **TDD 自动化模式**: 红灯 → 实现 → 绿灯
5. **真实验证模式**: 禁止 mock 作为通过证据
6. **最小执行证据模式**: 结构化证据记录

---

## 五、合法状态枚举

### Step/Task 状态

| 状态 | 含义 |
|------|------|
| `pending` | 尚未开始 |
| `running` | 正在执行 |
| `passed` | 已完成且验证通过 |
| `blocked` | 被外部因素阻断 |
| `failed` | 执行失败 |
| `reused` | 复用已有产物 |

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

## 六、commit-msg 规范

### 格式

```
${commit}:${taskId}_${name}:${submitDescription}
```

所有冒号统一为**英文半角冒号 `:`**。

| 字段 | 说明 | 取值约束 |
|------|------|----------|
| `commit` | 提交类型 | `Feat` / `Task` / `BugFix` |
| `taskId` | 任务/需求 ID，不可含下划线 | 如 `33413` |
| `name` | 任务/需求名称，可含中文 | 如 `新增用户管理功能` |
| `submitDescription` | 本次提交描述，不可为空 | 如 `add user management module` |

### 示例

```
Feat:33413_新增用户管理功能:add user management module
Task:33414_代码审查优化:optimize review logic
BugFix:33415_修复空指针:resolve null pointer exception
```

---

## 七、修复轮次配置

在 `delivery-state.md` 中通过 `max_repair_rounds` 字段配置：

| 交付类型 | 建议配置 | 说明 |
|----------|----------|------|
| 轻量变更 | `3` | 简单变更 |
| 标准交付 | `8` | 平衡效率和质量 |
| 复杂集成 | `15` | 微服务集成场景 |

未配置时默认值为 `8`。

---

## 八、通用禁止事项

- ❌ 不得在文档、代码或指令中写入敏感凭证（api-token、webhook URL、账号密码）
- ❌ 不得使用占位符路径作为目标测试文件
- ❌ 不得将编译警告、lint 提示当作 TDD 红灯验证
- ❌ 不得跳过行序靠前的未完成 Task 去执行后面的 Task
- ❌ 不得在每个 Task 完成后等待用户确认（除非遇到 failed/blocked）
- ❌ 不得将 MR/PR 创建当作发布触发
- ❌ 不得覆盖、删除或改写 deploy-log.md 中的历史记录
- ❌ 不得使用 `todo`、`done`、`complete`、`success` 等状态别名
- ❌ 不得使用 `mvn compile` 作为 TDD 红灯验证
- ❌ 不得在集成测试中使用 `@MockBean` 替代真实数据库连接（除非明确声明为降级验证）

---

## 九、使用指南

### 完整流程

```
/4p12s-delivery-orchestrator
/4p12s-requirements
/4p12s-prd
/4p12s-user-stories
/4p12s-technical-design
/4p12s-verification-plan
/4p12s-implementation-tasks
/4p12s-implementation-execution
/4p12s-integration-test
/4p12s-e2e-test
/4p12s-git-push
/4p12s-deployment-execution
```

### 轻量化流程

适用于：紧急修复、配置变更、简单 UI 调整、文档更新。

```
/4p12s-lightweight-flow
```

### 增量交付

在 `delivery-state.md` 中标记需要复用的阶段为 `reused`，然后从需要执行的阶段开始。

---

**文档结束**

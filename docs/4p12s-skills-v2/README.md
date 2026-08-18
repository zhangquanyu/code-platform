# 4P12S 技能体系 v2（Java/Maven/Vue3 适配版）

## 文档信息

- **版本**: v2.0.0
- **基于版本**: v1.0.3
- **适配技术栈**: Java 17 + Spring Boot + Maven + Vue3 + DDD
- **生成时间**: 2026-08-18
- **文档路径**: `docs/4p12s-skills-v2/`

---

## 一、v2 版本改进总览

基于 [4P12S 技能体系分析结论](../4P12S%20技能体系分析结论.md) 中的改进建议，v2 版本做了以下核心改进：

| 改进项 | v1 状态 | v2 改进 | 对应结论文档章节 |
|--------|---------|---------|------------------|
| 文档结构一致性 | 08-12 合并为一个文件 | 拆分为 5 个独立文件 | 3.1 文档结构不一致 |
| 技术栈适配 | 偏向 Node.js 生态 | 全面适配 Java/Maven/Vue3 | 3.2 技术栈倾向性 |
| 轻量化流程 | 无 | 新增 00-lightweight-flow | 6.2 中期改进 |
| commit-msg 规范 | 中文全角冒号，易出错 | 统一英文冒号，降低混淆 | 3.5 commit-msg 脆弱性 |
| 修复轮次弹性 | 固定 5 轮 | 可配置，默认 8 轮 | 3.4 修复轮次限制 |
| 增量交付支持 | 无 | 支持复用已有产物 | 6.2 中期改进 |
| DDD 架构支持 | 无 | 设计阶段支持 DDD 分层 | 3.2 技术栈倾向性 |
| 测试框架适配 | Node.js 测试 | JUnit 5 + Testcontainers + Cypress | 3.2 技术栈倾向性 |

---

## 二、技能清单

### 完整流程（12 个技能）

| 序号 | 技能名称 | 文件 | 职责 |
|------|----------|------|------|
| 1 | 4p12s-delivery-orchestrator | [01-delivery-orchestrator.md](./01-delivery-orchestrator.md) | 初始化交付状态账本 |
| 2 | 4p12s-requirements | [02-requirements.md](./02-requirements.md) | 需求访谈与登记 |
| 3 | 4p12s-prd | [03-prd.md](./03-prd.md) | 生成产品需求文档 |
| 4 | 4p12s-user-stories | [04-user-stories.md](./04-user-stories.md) | 用户故事与验收场景 |
| 5 | 4p12s-technical-design | [05-technical-design.md](./05-technical-design.md) | 技术设计方案（DDD 支持） |
| 6 | 4p12s-verification-plan | [06-verification-plan.md](./06-verification-plan.md) | 验证计划 |
| 7 | 4p12s-implementation-tasks | [07-implementation-tasks.md](./07-implementation-tasks.md) | 开发任务计划 |
| 8 | 4p12s-implementation-execution | [08-implementation-execution.md](./08-implementation-execution.md) | 开发任务执行（TDD） |
| 9 | 4p12s-integration-test | [09-integration-test.md](./09-integration-test.md) | 集成测试 |
| 10 | 4p12s-e2e-test | [10-e2e-test.md](./10-e2e-test.md) | E2E 测试 |
| 11 | 4p12s-git-push | [11-git-push.md](./11-git-push.md) | Git 推送 |
| 12 | 4p12s-deployment-execution | [12-deployment-execution.md](./12-deployment-execution.md) | 发布触发 |

### 轻量化流程（新增）

| 序号 | 技能名称 | 文件 | 职责 |
|------|----------|------|------|
| 0 | 4p12s-lightweight-flow | [00-lightweight-flow.md](./00-lightweight-flow.md) | 简单变更精简流程 |

---

## 三、技术栈适配说明

### 3.1 后端技术栈

| 维度 | v1（Node.js） | v2（Java） |
|------|---------------|------------|
| 构建工具 | npm/yarn | Maven |
| 测试框架 | jest/mocha | JUnit 5 + Mockito |
| 集成测试 | supertest | Spring Boot Test + Testcontainers |
| 覆盖率工具 | nyc/c8 | JaCoCo |
| lint 工具 | eslint | checkstyle/spotbugs |
| 启动命令 | npm start | `mvn spring-boot:run` |
| 测试命令 | npm test | `mvn test` |
| 打包命令 | npm run build | `mvn clean package` |

### 3.2 前端技术栈

| 维度 | v1 | v2 |
|------|----|----|
| 框架 | React/Next.js | Vue3 + Vite |
| E2E 框架 | Playwright | Cypress（推荐）/ Playwright |
| 测试框架 | jest | Vitest |
| 启动命令 | npm run dev | `npm run dev` |
| 构建命令 | npm run build | `npm run build` |

### 3.3 DDD 架构适配

设计阶段支持 DDD 分层架构：

```
cn.zhangquanyu.<模块名>
├── interfaces/        # 接口层（Controller、API）
├── application/       # 应用层（AppService、Cmd、VO）
├── domain/            # 领域层（Entity、Repository、DomainService）
└── infrastructure/    # 基础设施层（RepositoryImpl、Config）
```

---

## 四、技能依赖关系图

### 4.1 完整流程

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

### 4.2 轻量化流程

```
4p12s-lightweight-flow (精简流程)
    ├── 需求确认（简版）
    ├── 实现（TDD 可选）
    ├── 基础验证（单元测试）
    └── Git 推送
```

---

## 五、核心设计模式（保持不变）

1. **状态账本模式**: 通过 `delivery-state.md` 统一跟踪交付进度
2. **双模运行模式**: 支持编排模式和独立模式
3. **稳定 ID 追溯模式**: 从需求到代码的完整追溯链
4. **TDD 自动化模式**: 红灯 → 实现 → 绿灯
5. **真实验证模式**: 禁止 mock 作为通过证据
6. **最小执行证据模式**: 结构化证据记录

---

## 六、commit-msg 规范改进

### 6.1 v1 规范（已弃用）

```
${commit}:${taskId}_${name}：${submitDescription}
```

**问题**: 中文全角冒号 `：` 与英文半角 `:` 混用，全角空格难以发现。

### 6.2 v2 规范（统一英文冒号）

```
${commit}:${taskId}_${name}:${submitDescription}
```

| 字段 | 说明 | 取值约束 |
|------|------|----------|
| `commit` | 提交类型 | `Feat` / `Task` / `BugFix` |
| `taskId` | 任务/需求 ID，不可含下划线 | 如 `33413` |
| `name` | 任务/需求名称，可含中文 | 如 `新增用户管理功能` |
| `submitDescription` | 本次提交描述，不可为空 | 如 `add user management module` |

**示例**:

```
Feat:33413_新增用户管理功能:add user management module
Task:33414_代码审查优化:optimize review logic
BugFix:33415_修复空指针:resolve null pointer exception
```

**改进点**: 所有冒号统一为英文半角 `:`，消除全角/半角混淆风险。

---

## 七、修复轮次弹性配置

### 7.1 v1 固定 5 轮

集成测试和 E2E 测试修复循环固定为 5 轮，复杂场景可能不足。

### 7.2 v2 可配置

| 交付类型 | 默认修复轮次 | 说明 |
|----------|--------------|------|
| 轻量变更 | 3 轮 | 简单变更无需过多修复 |
| 标准交付 | 8 轮 | 平衡效率和质量 |
| 复杂集成 | 15 轮 | 微服务集成场景 |

**配置方式**: 在 `delivery-state.md` 中添加 `max_repair_rounds` 字段：

```yaml
max_repair_rounds: 8
```

---

## 八、增量交付支持

v2 支持复用已有产物，避免重复生成：

### 8.1 复用判定规则

| 产物 | 复用条件 | 复用方式 |
|------|----------|----------|
| `requirements-register.md` | 需求未变更 | 直接引用，跳过 requirements 技能 |
| `prd.md` | PRD 未变更 | 直接引用，跳过 prd 技能 |
| `user-stories.md` | 用户故事未变更 | 直接引用，跳过 user-stories 技能 |
| `design.md` | 设计未变更 | 直接引用，跳过 technical-design 技能 |
| `tasks/` | 任务计划未变更 | 直接引用，跳过 implementation-tasks 技能 |

### 8.2 复用标记

在 `delivery-state.md` 的 Step Checklist 中使用 `reused` 状态：

```markdown
| Step | Artifact | Status | Evidence |
| --- | --- | --- | --- |
| requirements | requirements-register.md | reused | deliveries/xxx/requirements-register.md |
```

**合法状态新增**: `reused`（复用已有产物，未重新生成）

---

## 九、质量门禁体系

每个技能都有严格的质量门禁，详见各技能文档的"质量门禁"章节。

### 跨阶段一致性检查

- ✅ 需求 ID 覆盖：范围内的每个需求 ID 在所有下游产物中都有对应行
- ✅ 前端交互覆盖：每个 `UI-*` 需求在 PRD、US、design、verification-plan、E2E 测试中都有对应内容
- ✅ Git 阻断项追踪：user-stories 中标记为 Git 阻断的验收项，在 verification-plan 中必须有验证路径
- ✅ 产物文件路径一致性：verification-plan 中指定的目标测试文件必须与项目运行器配置一致

---

## 十、禁止事项汇总

### 通用禁止事项

- ❌ 不得在文档、代码或指令中写入敏感凭证（api-token、webhook URL、账号密码）
- ❌ 不得使用占位符路径作为目标测试文件
- ❌ 不得将编译警告、lint 提示当作 TDD 红灯验证
- ❌ 不得跳过行序靠前的未完成 Task 去执行后面的 Task
- ❌ 不得在每个 Task 完成后等待用户确认（除非遇到 failed/blocked）
- ❌ 不得将 MR/PR 创建当作发布触发
- ❌ 不得覆盖、删除或改写 deploy-log.md 中的历史记录
- ❌ 不得使用 `todo`、`done`、`complete`、`success` 等状态别名

### Java/Maven 特定禁止事项

- ❌ 不得使用 `mvn compile` 作为 TDD 红灯验证
- ❌ 不得跳过 JaCoCo 覆盖率检查
- ❌ 不得在集成测试中使用 `@MockBean` 替代真实数据库连接（除非明确声明为降级验证）

---

## 十一、使用指南

### 11.1 完整流程使用

```
/4p12s-delivery-orchestrator  # 初始化
/4p12s-requirements           # 需求登记
/4p12s-prd                    # 生成 PRD
/4p12s-user-stories           # 用户故事
/4p12s-technical-design       # 技术设计
/4p12s-verification-plan      # 验证计划
/4p12s-implementation-tasks   # 任务计划
/4p12s-implementation-execution # 任务执行
/4p12s-integration-test       # 集成测试
/4p12s-e2e-test               # E2E 测试
/4p12s-git-push               # Git 推送
/4p12s-deployment-execution   # 发布触发
```

### 11.2 轻量化流程使用

适用于：紧急修复、配置变更、简单 UI 调整、文档更新。

```
/4p12s-lightweight-flow       # 精简流程
```

### 11.3 增量交付使用

在 `delivery-state.md` 中标记需要复用的阶段为 `reused`，然后从需要执行的阶段开始。

---

## 十二、合法状态枚举

### 12.1 Step/Task 状态

| 状态 | 含义 | 说明 |
|------|------|------|
| `pending` | 尚未开始 | 初始状态 |
| `running` | 正在执行 | 技能执行中 |
| `passed` | 已完成且验证通过 | 阶段成功完成 |
| `blocked` | 被外部因素阻断 | 等待用户输入、业务决策 |
| `failed` | 执行失败 | 业务逻辑失败或系统级错误 |
| `reused` | 复用已有产物 | **v2 新增**，未重新生成 |

### 12.2 workflow_status

| 状态 | 含义 |
|------|------|
| `pending` | 尚未开始 |
| `running` | 正在执行 |
| `blocked` | 被阻断 |
| `failed` | 失败 |
| `release_triggered` | 发布已触发 |

---

## 版本信息

- **技能版本**: v2.0.0
- **基于版本**: v1.0.3
- **生成时间**: 2026-08-18
- **文档路径**: `docs/4p12s-skills-v2/`

---

**文档结束**

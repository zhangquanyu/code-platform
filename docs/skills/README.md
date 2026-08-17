# 4P12S 技能体系文档索引

## 文档说明

本目录包含 4P12S 端到端交付技能体系的 12 个技能的详细分析文档。

## 技能列表

### 阶段 1: 初始化

| 序号 | 技能名称 | 文件 | 职责 |
|------|----------|------|------|
| 1 | 4p12s-delivery-orchestrator | [01-delivery-orchestrator.md](./01-delivery-orchestrator.md) | 初始化交付状态账本 |

### 阶段 2: 需求分析

| 序号 | 技能名称 | 文件 | 职责 |
|------|----------|------|------|
| 2 | 4p12s-requirements | [02-requirements.md](./02-requirements.md) | 需求访谈与登记 |
| 3 | 4p12s-prd | [03-prd.md](./03-prd.md) | 生成产品需求文档 |
| 4 | 4p12s-user-stories | [04-user-stories.md](./04-user-stories.md) | 用户故事与验收场景 |

### 阶段 3: 设计规划

| 序号 | 技能名称 | 文件 | 职责 |
|------|----------|------|------|
| 5 | 4p12s-technical-design | [05-technical-design.md](./05-technical-design.md) | 技术设计方案 |
| 6 | 4p12s-verification-plan | [06-verification-plan.md](./06-verification-plan.md) | 验证计划 |

### 阶段 4: 开发实现

| 序号 | 技能名称 | 文件 | 职责 |
|------|----------|------|------|
| 7 | 4p12s-implementation-tasks | [07-implementation-tasks.md](./07-implementation-tasks.md) | 开发任务计划 |
| 8 | 4p12s-implementation-execution | [08-implementation-execution.md](./08-implementation-execution.md) | 开发任务执行 |

### 阶段 5: 验证测试

| 序号 | 技能名称 | 文件 | 职责 |
|------|----------|------|------|
| 9 | 4p12s-integration-test | [09-integration-test.md](./09-integration-test.md) | 集成测试 |
| 10 | 4p12s-e2e-test | [10-e2e-test.md](./10-e2e-test.md) | E2E 测试 |

### 阶段 6: 交付发布

| 序号 | 技能名称 | 文件 | 职责 |
|------|----------|------|------|
| 11 | 4p12s-git-push | [11-git-push.md](./11-git-push.md) | Git 推送 |
| 12 | 4p12s-deployment-execution | [12-deployment-execution.md](./12-deployment-execution.md) | 发布触发 |

## 技能依赖关系图

```
4p12s-delivery-orchestrator (初始化器)
         ↓
    创建 delivery-state.md
         ↓
4p12s-requirements (需求登记)
         ↓
    requirements-register.md
         ↓
4p12s-prd (产品需求文档)
         ↓
    prd.md
         ↓
4p12s-user-stories (用户故事)
         ↓
    user-stories.md
         ↓
4p12s-technical-design (技术设计)
         ↓
    design.md
         ↓
4p12s-verification-plan (验证计划)
         ↓
    verification-plan.md
         ↓
4p12s-implementation-tasks (任务计划)
         ↓
    tasks/
         ↓
4p12s-implementation-execution (任务执行)
         ↓
    产品代码 + 测试代码
         ↓
4p12s-integration-test (集成测试)
         ↓
    verification-result.md (集成部分)
         ↓
4p12s-e2e-test (E2E 测试)
         ↓
    verification-result.md (E2E 部分)
         ↓
4p12s-git-push (Git 推送)
         ↓
    deploy-log.md (Git Push)
         ↓
4p12s-deployment-execution (发布触发)
         ↓
    deploy-log.md (Release Trigger)
```

## 核心设计模式

### 1. 状态账本模式
通过 `delivery-state.md` 统一跟踪交付进度、阻塞原因和证据文件。

### 2. 双模运行模式
每个技能支持编排模式（被 orchestrator 调用）和独立模式（用户直接调用）。

### 3. 稳定 ID 追溯模式
从需求到代码的每个工件都有稳定的 ID，形成完整的追溯链。

### 4. TDD 自动化模式
在 implementation-execution 阶段强制执行"红灯→实现→绿灯"的测试驱动开发流程。

### 5. 真实验证模式
在 integration-test 和 e2e-test 阶段必须使用真实服务和真实接口断言。

### 6. 最小执行证据模式
每个步骤的执行结果必须记录结构化证据，而非简单的"通过/失败"。

## 质量门禁体系

每个技能都有严格的质量门禁，确保：
- 需求 ID 覆盖
- 验证完整
- 设计一致
- 证据充分

详见各技能文档的"质量门禁"章节。

## 禁止事项汇总

每个技能都有明确的禁止事项，详见各技能文档的"边界"和"禁止事项"章节。

通用禁止事项：
- ❌ 不得在文档、代码或指令中写入敏感凭证
- ❌ 不得使用占位符路径作为目标测试文件
- ❌ 不得将编译警告、lint 提示当作 TDD 红灯验证
- ❌ 不得跳过行序靠前的未完成 Task 去执行后面的 Task
- ❌ 不得在每个 Task 完成后等待用户确认（除非遇到 failed/blocked）
- ❌ 不得将 MR/PR 创建当作发布触发
- ❌ 不得覆盖、删除或改写 deploy-log.md 中的历史记录

## 使用指南

### 独立模式使用

用户可以直接调用单个技能：

```
/4p12s-delivery-orchestrator  # 初始化
/4p12s-requirements           # 需求登记
/4p12s-prd                    # 生成 PRD
...
```

### 编排模式使用

被 orchestrator 调用时，按顺序自动执行所有技能。

## 文档结构说明

每个技能分析文档包含以下章节：

1. 基本信息
2. 原文核心内容
3. 分析说明
   - 设计意图
   - 核心概念解析
   - 执行流程
   - 与其他技能的关系
   - 最佳实践
   - 常见问题
   - 适用场景
   - 限制与约束
   - 总结

## 版本信息

- 技能版本：v1.0.3
- 分析时间：2026-08-17
- 文档路径：`docs/skills/`

---

**文档结束**

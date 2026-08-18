# 4p12s-lightweight-flow 技能

## 基本信息

- **技能名称**: 4p12s-lightweight-flow
- **技能定位**: 简单变更的精简交付流程
- **版本**: 1.0.0
- **文件路径**: `docs/skills/00-lightweight-flow.md`

---

## 一、概述

本技能是轻量化交付流程，适用于不需要完整 12 步流程的简单变更场景。它将完整流程精简为 **4 个步骤**，在保证基本质量的前提下大幅降低流程成本。

### 适用场景

**适合使用轻量化流程的场景**:
- 紧急热修复（BugFix）
- 简单配置变更
- 文案或 UI 微调
- 小范围重构（不改变外部行为）
- 依赖版本升级
- 已有功能的参数调整

**不适合使用轻量化流程的场景**:
- 新功能开发（应使用完整流程）
- 涉及多个模块的变更
- 需要完整追溯链的合规项目
- 涉及数据库 schema 变更
- 涉及外部 API 契约变更

---

## 二、流程设计

### 2.1 流程对比

| 完整流程（12 步） | 轻量化流程（4 步） |
|-------------------|-------------------|
| 1. delivery-orchestrator | - |
| 2. requirements | 1. 需求确认（简版） |
| 3. prd | - |
| 4. user-stories | - |
| 5. technical-design | - |
| 6. verification-plan | - |
| 7. implementation-tasks | - |
| 8. implementation-execution | 2. 实现（TDD 可选） |
| 9. integration-test | - |
| 10. e2e-test | 3. 基础验证（单元测试） |
| 11. git-push | 4. Git 推送 |
| 12. deployment-execution | - |

### 2.2 流程步骤

```
用户触发 /4p12s-lightweight-flow
         ↓
Step 1: 需求确认（简版）
    ├── 确认变更内容
    ├── 确认影响范围
    └── 记录到 lightweight-change.md
         ↓
Step 2: 实现（TDD 可选）
    ├── 简单变更：直接修改代码
    ├── 复杂变更：执行 TDD 四步骤
    └── 记录变更文件
         ↓
Step 3: 基础验证
    ├── 后端：mvn test（相关模块）
    ├── 前端：npm run test（相关组件）
    └── 记录验证证据
         ↓
Step 4: Git 推送
    ├── 暂存变更文件
    ├── 组装 commit-msg
    ├── 提交并推送
    └── 验证远端 HEAD
```

---

## 三、详细执行流程

### Step 1: 需求确认（简版）

**目标**: 快速确认变更内容和影响范围，不生成完整的 PRD 和用户故事。

**执行动作**:

1. 向用户确认变更内容：
   - 变更类型（BugFix / 配置变更 / UI 调整 / 重构 / 其他）
   - 变更描述（一句话描述）
   - 影响范围（涉及的模块/文件）

2. 创建轻量化交付目录：
   ```
   deliveries/<变更名>/
   └── lightweight-change.md
   ```

3. 写入 `lightweight-change.md`：
   ```markdown
   # 轻量化变更记录

   - change_type: BugFix | ConfigChange | UITweak | Refactor | Other
   - change_description: <一句话描述>
   - impact_scope: <影响的模块/文件>
   - need_tdd: true | false
   - created_at: <ISO8601 时间戳>
   ```

**质量门禁**:
- 变更类型明确
- 影响范围已确认
- 是否需要 TDD 已决策

### Step 2: 实现（TDD 可选）

**目标**: 完成代码变更，根据复杂度决定是否执行 TDD。

#### 2.1 简单变更（无需 TDD）

适用于：配置变更、文案修改、简单参数调整。

**执行动作**:
1. 直接修改相关文件
2. 记录变更文件列表
3. 进入 Step 3

#### 2.2 复杂变更（需要 TDD）

适用于：Bug 修复涉及业务逻辑、重构涉及行为变更。

**执行 TDD 四步骤**:

1. **编写失败测试**
   - 后端：在 `src/test/java/` 下创建或修改测试类
   - 前端：在 `__tests__/` 下创建或修改测试文件
   - 测试命令：`mvn test -Dtest=<TestClass>` 或 `npm run test -- <testFile>`

2. **运行红灯验证**
   - 命令：`mvn test -Dtest=<TestClass>` 或 `npm run test -- <testFile>`
   - 期望结果：测试失败
   - 记录证据

3. **实现最小代码**
   - 修改产品代码
   - 只实现让测试通过的最小代码

4. **运行绿灯验证**
   - 命令：`mvn test -Dtest=<TestClass>` 或 `npm run test -- <testFile>`
   - 期望结果：测试通过
   - 记录证据

**TDD 结果记录**:
```yaml
changed_files: [...]
tests_added_or_changed: [...]
commands_run:
  - cwd: /path/to/module
    command: mvn test -Dtest=<TestClass>
    exit_code: 0
    result: passed
    output_summary: 5 tests passed
tdd_red_result: 测试失败，原因：NullPointerException at UserService.createUser
tdd_green_result: 所有测试通过
```

### Step 3: 基础验证

**目标**: 确保变更不破坏现有功能。

**执行动作**:

1. **后端验证**（如涉及后端变更）:
   ```bash
   # 运行相关模块的单元测试
   mvn test -pl <module-name>

   # 运行 checkstyle（如配置）
   mvn checkstyle:check
   ```

2. **前端验证**（如涉及前端变更）:
   ```bash
   # 运行前端单元测试
   npm run test

   # 运行 lint
   npm run lint

   # 构建检查
   npm run build
   ```

3. **编译验证**:
   ```bash
   # 后端编译
   mvn compile

   # 前端构建
   npm run build
   ```

**验证证据记录**:
```yaml
verification_commands:
  - cwd: /path/to/backend
    command: mvn test -pl user-service
    exit_code: 0
    result: passed
    output_summary: 12 tests passed
  - cwd: /path/to/frontend
    command: npm run build
    exit_code: 0
    result: passed
    output_summary: build successful
```

**质量门禁**:
- 相关单元测试全部通过
- 编译/构建成功
- 无新增 lint 错误
- 如果测试失败，返回 `failed`，不继续 Git 推送

### Step 4: Git 推送

**目标**: 提交并推送变更。

**执行动作**:

1. 确定变更文件列表：
   ```bash
   git status
   git diff --stat
   ```

2. 暂存变更文件（仅暂存本次变更相关文件）：
   ```bash
   git add <file1> <file2> ...
   ```

3. 组装 commit-msg：
   ```
   ${commit}:${taskId}_${name}:${submitDescription}
   ```

   **字段映射**:
   | 提取字段 | 映射到 commit-msg |
   |----------|-------------------|
   | 变更类型 | `commit`（BugFix / Task / Feat） |
   | 任务 ID | `taskId`（如无则为 `0000`） |
   | 变更描述 | `name` |
   | git diff 摘要 | `submitDescription` |

   **示例**:
   ```
   BugFix:33415_修复用户登录空指针:resolve null pointer in login service
   Task:0000_配置参数调整:update timeout config
   ```

4. 提交并推送：
   ```bash
   git commit -m "<commit-msg>"
   git push
   ```

5. 验证远端 HEAD：
   ```bash
   git log --oneline -1
   git ls-remote origin <branch>
   ```

6. 向 `lightweight-change.md` 追加完成记录：
   ```markdown
   ## 完成记录

   - completed_at: <ISO8601 时间戳>
   - commit_hash: <commit-hash>
   - branch: <branch-name>
   - changed_files:
     - <file1>
     - <file2>
   - verification_result: passed
   ```

---

## 四、返回状态

| 状态 | 含义 | 后续动作 |
|------|------|----------|
| `passed` | 所有步骤完成，验证通过 | 交付完成 |
| `failed` | 验证失败或代码错误 | 停止，修复后重新执行 |
| `blocked` | 外部依赖缺失或需要用户决策 | 停止，等待用户介入 |

---

## 五、与完整流程的关系

### 5.1 升级路径

如果在执行过程中发现变更比预期复杂，可以升级到完整流程：

1. 将 `lightweight-change.md` 中的内容作为 `requirements-register.md` 的输入
2. 触发 `4p12s-delivery-orchestrator` 初始化完整流程
3. 从 `4p12s-prd` 开始执行完整流程

### 5.2 升级条件

如果发现以下情况，应建议用户升级到完整流程：

- 变更涉及多个模块的协作
- 变更涉及数据库 schema 修改
- 变更涉及外部 API 契约
- 变更涉及权限或安全相关逻辑
- 单元测试发现需要集成测试才能验证的问题

---

## 六、禁止事项

- 不得用轻量化流程处理新功能开发
- 不得跳过基础验证直接推送
- 不得在执行过程中执行 E2E 测试（应升级到完整流程）
- 不得在执行过程中触发发布（只到 Git 推送为止）
- 不得使用 `mvn compile` 替代 `mvn test` 作为验证

---

## 七、最佳实践

### 7.1 判断是否使用轻量化流程

```
变更是否涉及新功能？
├── 是 → 使用完整流程
└── 否 → 变更是否涉及多模块协作？
    ├── 是 → 使用完整流程
    └── 否 → 变更是否涉及数据库 schema？
        ├── 是 → 使用完整流程
        └── 否 → 使用轻量化流程
```

### 7.2 TDD 决策

```
变更是否涉及业务逻辑？
├── 是 → 执行 TDD 四步骤
└── 否 → 变更是否涉及行为变更？
    ├── 是 → 执行 TDD 四步骤
    └── 否 → 直接修改代码，跳过 TDD
```

### 7.3 commit-msg 组装

- 从 `git diff --stat` 总结 submitDescription
- 使用英文冒号 `:` 分隔字段
- name 字段使用简洁的中文描述
- 不要照搬任务描述作为 submitDescription
- 不要使用中文全角冒号 `：`

---

## 八、总结

**4p12s-lightweight-flow** 是轻量化交付流程，适用于简单变更场景。它将 12 步流程精简为 4 步，在保证基本质量的前提下大幅降低流程成本。

**关键价值**:
- **快速交付**: 4 步完成简单变更
- **质量保证**: 基础验证确保不破坏现有功能
- **灵活升级**: 发现复杂度超预期时可升级到完整流程
- **成本可控**: 文档负担最小化

---

**文档结束**

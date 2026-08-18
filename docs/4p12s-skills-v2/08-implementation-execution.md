# 4p12s-implementation-execution 技能（v2）

## 基本信息

- **技能名称**: 4p12s-implementation-execution
- **技能定位**: 开发任务执行（TDD）
- **版本**: v2.0.0
- **文件路径**: `docs/4p12s-skills-v2/08-implementation-execution.md`

---

## 一、概述

执行选定的开发 Task，按 TDD 四步骤逐项完成。

### v2 改进点

- ✅ 红灯/绿灯验证命令使用 `mvn test`
- ✅ 结果记录适配 Java 项目结构
- ✅ 非 TDD 形态任务（DDL、配置）的验证命令明确化

---

## 二、核心原则：自动连续执行

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

---

## 三、执行顺序约束（强制）

**必须严格按照 `tasks/index.md` 的 `Task 目录清单` 表格行序，从第一个未完成的 Task 开始逐个执行。**

- ✅ 禁止跳选、禁止重排
- ❌ 禁止以"该任务不适合 TDD""先做更有价值的任务"等理由跳过行序靠前的未完成 Task

**"未完成"的判定标准**:
- ✅ 已完成的定义：Task 文件中所有 Step checkbox 均为 `- [x]` **且** `## 结果记录` 中已有 `tdd_green_result` 证据
- ❌ 其余情况一律视为**未完成**

---

## 四、TDD 四步骤（强制要求）

### Step 1 编写失败测试

- 目标测试文件路径：`src/test/java/cn/zhangquanyu/...`
- 测试意图
- 关键断言

### Step 2 运行红灯验证

```bash
# 后端单元测试
mvn test -Dtest=<TestClass>#<TestMethod>

# 前端单元测试
npm run test -- <testFile>
```

- cwd
- command
- 期望结果（失败）
- 证据

**约束**:
- ❌ `mvn compile` 不能作为红灯验证
- ❌ `mvn checkstyle:check` 不能作为红灯验证
- ✅ 必须是测试执行且测试失败

### Step 3 实现最小产品代码

- 变更文件路径：`src/main/java/cn/zhangquanyu/...`
- 实现说明

### Step 4 运行绿灯验证

```bash
# 后端单元测试
mvn test -Dtest=<TestClass>#<TestMethod>

# 前端单元测试
npm run test -- <testFile>
```

- cwd
- command
- 期望结果（通过）
- 证据

---

## 五、非 TDD 形态任务的处理

### DDL 变更任务

- ✅ 同样必须按行序执行，不得跳过
- ✅ 红灯步骤：查询表不存在作为红灯证据
  ```bash
  mysql -e "SHOW TABLES LIKE '<table_name>'" <database>
  ```
- ✅ 绿灯步骤：执行 SQL 后验证表存在
  ```bash
  mysql < src/main/resources/db/init.sql
  mysql -e "SHOW TABLES LIKE '<table_name>'" <database>
  ```

### 配置变更任务

- ✅ 红灯步骤：检查配置不存在
  ```bash
  grep "<config_key>" src/main/resources/application.yml || echo "NOT FOUND"
  ```
- ✅ 绿灯步骤：编译验证
  ```bash
  mvn compile
  ```

### 依赖变更任务

- ✅ 红灯步骤：检查依赖不存在
  ```bash
  grep "<artifactId>" pom.xml || echo "NOT FOUND"
  ```
- ✅ 绿灯步骤：依赖下载并编译
  ```bash
  mvn compile
  ```

---

## 六、结果记录（结构化证据）

```yaml
changed_files:
  - src/main/java/cn/zhangquanyu/user/application/service/UserAppService.java
  - src/main/java/cn/zhangquanyu/user/application/cmd/CreateUserCmd.java
tests_added_or_changed:
  - src/test/java/cn/zhangquanyu/user/application/service/UserAppServiceTest.java
commands_run:
  - cwd: /path/to/project
    command: mvn test -Dtest=UserAppServiceTest#createUser_shouldReturnUserId
    exit_code: 1
    result: failed
    output_summary: Compilation failure: cannot find symbol class UserAppService
  - cwd: /path/to/project
    command: mvn test -Dtest=UserAppServiceTest#createUser_shouldReturnUserId
    exit_code: 0
    result: passed
    output_summary: Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
tdd_red_result: 编译失败，UserAppService 类不存在
tdd_green_result: 测试通过，createUser 方法返回正确的用户 ID
requirement_ids_covered:
  - FUNC-001
  - RULE-001
user_stories_covered:
  - US-001
```

---

## 七、返回状态

| 状态 | 含义 | 后续动作 |
|------|------|----------|
| `passed` | 当前 Task 所有 TDD 步骤完成且绿灯验证通过 | **继续执行下一个 Task** |
| `failed` | 绿灯验证真实失败 | **停止执行** |
| `blocked` | 遇到外部依赖缺失或环境问题 | **停止执行** |

---

## 八、禁止事项

- ❌ 改写 tasks/index.md
- ❌ 写 E2E 测试代码
- ❌ 运行最终集成测试和 E2E 测试
- ❌ 提交、推送、发布
- ❌ 跳过行序靠前的未完成 Task
- ❌ 在每个 Task 完成后等待用户确认
- ❌ 使用 `mvn compile` 作为 TDD 红灯验证
- ❌ 跳过红灯步骤

---

**文档结束**

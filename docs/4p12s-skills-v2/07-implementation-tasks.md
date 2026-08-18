# 4p12s-implementation-tasks 技能（v2）

## 基本信息

- **技能名称**: 4p12s-implementation-tasks
- **技能定位**: 开发任务计划
- **版本**: v2.0.0
- **文件路径**: `docs/4p12s-skills-v2/07-implementation-tasks.md`

---

## 一、概述

把已确认的产品范围和技术设计转成 `tasks/` 下的开发计划，每个 Task 都是可独立 TDD 完成的小计划。

### v2 改进点

- ✅ TDD 步骤增加 Maven 命令示例
- ✅ 文件计划适配 Java 目录结构
- ✅ 红灯/绿灯命令使用 `mvn test`
- ✅ 支持非 TDD 形态任务（DDL、配置）的验证命令

---

## 二、前置依赖

`requirements-register.md`、`prd.md`、`user-stories.md`、`design.md`

---

## 三、输出产物

```
tasks/
├── index.md
├── TASK-001.md
└── TASK-xxx.md
```

---

## 四、拆分原则

- ✅ 每个 Task 应对应一个可独立 TDD 完成的产品行为、服务能力、页面行为、接口能力或技术支撑变更
- ✅ 同一业务行为涉及 DTO、类型、路由、配置、页面、服务编排时，合并到直接使用它们的 Task
- ❌ 不生成独立 DTO、类型定义、路由、配置或纯文件改造 Task
- ❌ 不生成"后续补测试"Task
- ❌ 不生成 E2E 测试代码 Task
- ❌ Task ID 就是执行顺序，后面的 Task 可以依赖前面的，前面的不得依赖后面的

---

## 五、TASK-xxx.md 结构（TDD 四步骤）

```markdown
## 目标
## 文件计划（Create / Modify / Test）
## TDD 步骤
- [ ] Step 1: 编写失败测试
- [ ] Step 2: 运行红灯验证
- [ ] Step 3: 实现最小代码
- [ ] Step 4: 运行绿灯验证
## 结果记录
```

---

## 六、TDD 步骤详细模板（v2 Java 适配）

### Step 1: 编写失败测试

```markdown
- [ ] Step 1: 编写失败测试
  - 目标测试文件: `src/test/java/cn/zhangquanyu/user/application/service/UserAppServiceTest.java`
  - 测试意图: 验证创建用户时返回正确的用户 ID
  - 关键断言: `assertThat(result.getUserId()).isNotNull()`
```

### Step 2: 运行红灯验证

```markdown
- [ ] Step 2: 运行红灯验证
  - cwd: /path/to/project
  - command: `mvn test -Dtest=UserAppServiceTest`
  - 期望结果: 测试失败（编译错误或断言失败）
  - 证据: 记录失败原因
```

### Step 3: 实现最小代码

```markdown
- [ ] Step 3: 实现最小代码
  - 变更文件:
    - `src/main/java/cn/zhangquanyu/user/application/service/UserAppService.java` (Create)
    - `src/main/java/cn/zhangquanyu/user/application/cmd/CreateUserCmd.java` (Create)
  - 实现说明: 实现 createUser 方法，调用 Repository 保存用户
```

### Step 4: 运行绿灯验证

```markdown
- [ ] Step 4: 运行绿灯验证
  - cwd: /path/to/project
  - command: `mvn test -Dtest=UserAppServiceTest`
  - 期望结果: 测试通过
  - 证据: 记录通过信息
```

---

## 七、非 TDD 形态任务模板（v2 新增）

### DDL 变更任务

```markdown
## 目标
创建 dev_user 表

## 文件计划
- Modify: `src/main/resources/db/init.sql` (DDL)

## TDD 步骤
- [x] Step 1: 编写失败测试（N/A - DDL 变更）
  - 红灯证据: dev_user 表不存在
- [ ] Step 2: 运行红灯验证
  - cwd: /path/to/project
  - command: `mysql -e "SHOW TABLES LIKE 'dev_user'"` 或查询 information_schema
  - 期望结果: 表不存在
- [ ] Step 3: 实现最小代码
  - 变更文件: `src/main/resources/db/init.sql`
  - 实现说明: 添加 CREATE TABLE dev_user 语句
- [ ] Step 4: 运行绿灯验证
  - cwd: /path/to/project
  - command: `mysql < src/main/resources/db/init.sql && mysql -e "SHOW TABLES LIKE 'dev_user'"`
  - 期望结果: 表存在
```

### 配置变更任务

```markdown
## 目标
添加 Redis 缓存配置

## 文件计划
- Modify: `src/main/resources/application.yml` (Config)
- Modify: `pom.xml` (Dependency)

## TDD 步骤
- [x] Step 1: 编写失败测试（N/A - 配置变更）
  - 红灯证据: application.yml 中缺少 redis 配置
- [ ] Step 2: 运行红灯验证
  - cwd: /path/to/project
  - command: `grep "redis" src/main/resources/application.yml || echo "NOT FOUND"`
  - 期望结果: NOT FOUND
- [ ] Step 3: 实现最小代码
  - 变更文件: application.yml, pom.xml
  - 实现说明: 添加 spring.redis.* 配置和 spring-boot-starter-data-redis 依赖
- [ ] Step 4: 运行绿灯验证
  - cwd: /path/to/project
  - command: `mvn compile`
  - 期望结果: BUILD SUCCESS
```

---

## 八、质量门禁

- ✅ 每个 Task 都包含失败测试、红灯验证、实现、绿灯验证四个基础步骤
- ✅ 每个 Task 都有明确文件计划
- ✅ 每个 Task 的红灯/绿灯命令来自项目规范或聚焦配置查找
- ❌ 没有 E2E 测试代码任务、集成测试执行任务、Git 任务或发布任务

---

## 九、禁止事项

- ❌ 实现代码
- ❌ 执行测试
- ❌ 做 Git 或发布
- ❌ 生成 E2E 测试代码任务

---

**文档结束**

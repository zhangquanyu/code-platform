# 4p12s-integration-test 技能（v2）

## 基本信息

- **技能名称**: 4p12s-integration-test
- **技能定位**: 集成测试
- **版本**: v2.0.0
- **文件路径**: `docs/4p12s-skills-v2/09-integration-test.md`

---

## 一、概述

运行真实 API 和服务间集成验证，在内部完成运行、归因、修复、重跑的完整循环。

### v2 改进点

- ✅ 使用 Spring Boot Test + Testcontainers 进行真实集成测试
- ✅ 修复轮次可配置（默认 8 轮，从 `delivery-state.md` 读取 `max_repair_rounds`）
- ✅ 服务启动命令适配 Maven 生态
- ✅ 健康检查适配 Spring Boot Actuator

---

## 二、前置依赖

`verification-plan.md`、`verification-result.md`

---

## 三、输出产物

追加写入 `verification-result.md` 的集成部分

---

## 四、执行流程（三步强规则）

### STEP-01: 服务启动（强规则：必须执行）

1. 从 `verification-plan.md` 提取所需真实服务类型、测试数据要求、禁用 Mock 规则
2. 从项目入口文档发现后端/API 启动命令、地址/端口、健康检查方式
3. 实际执行启动命令，在后台启动后端服务

**v2 启动命令示例**:

```bash
# 后端服务启动
mvn spring-boot:run -Dspring-boot.run.profiles=test

# 或打包后启动
mvn clean package -DskipTests
java -jar target/<app-name>.jar --spring.profiles.active=test
```

4. 执行健康检查

**v2 健康检查示例**:

```bash
# Spring Boot Actuator 健康检查
curl -f http://localhost:8080/actuator/health

# 期望响应
# {"status":"UP"}
```

5. ❌ 缺少任一证据都不得返回 `passed`

### STEP-02: 测试数据准备

1. 按 `verification-plan.md` 中的测试数据要求准备初始数据
2. 记录数据准备的最小执行证据

**v2 数据准备示例**:

```bash
# 执行 SQL 脚本初始化测试数据
mysql -u<user> -p<password> <database> < src/test/resources/data/init_test_data.sql

# 或使用 Flyway/Liquibase
mvn flyway:migrate -Dflyway.url=<url>
```

### STEP-03: 运行集成测试

1. 执行从项目入口文档发现的集成测试命令

**v2 集成测试命令示例**:

```bash
# 运行所有集成测试（按命名约定）
mvn test -Dtest="*IntegrationTest"

# 运行特定测试类
mvn test -Dtest=UserIntegrationTest

# 运行特定测试方法
mvn test -Dtest=UserIntegrationTest#createUser_shouldPersistToDatabase
```

2. 逐项执行每个集成测试 TC
3. 记录每个 TC 的最小执行证据

---

## 五、修复循环（v2 弹性轮次）

1. 对每个失败 TC，归因失败原因（产品代码、测试代码、环境、数据、配置）
2. 渐进式披露：修复代码时，按失败 TC → `user-stories.md` → `prd.md` 的路径，只读取关联条目
3. 修复对应的产品代码或测试代码
4. 重跑失败 TC + 受影响的回归项
5. 重复直到全部通过或达到修复轮次上限

**修复轮次配置**:

| 来源 | 配置值 | 说明 |
|------|--------|------|
| `delivery-state.md` 的 `max_repair_rounds` | 用户配置 | 优先使用 |
| 默认值 | `8` | 未配置时使用 |

6. 超过修复轮次后返回 `blocked` 或 `failed`

---

## 六、强规则（必须遵守）

- ✅ **没有服务启动证据不得返回 `passed`**
- ✅ **没有健康检查结果不得返回 `passed`**
- ✅ **"测试代码已创建"、"文件存在"、"CI 稍后运行"、纯编译通过不是集成测试通过证据**
- ✅ **每个集成测试 TC 必须有真实执行记录**
- ✅ **只有 STEP-01 到 STEP-03 都满足完成条件并有证据，才允许返回 `passed`**

---

## 七、最小执行证据规则

- ✅ 只记录发现来源、执行动作、命令摘要、结果、关键输出摘要
- ✅ 命令摘要格式：`<cwd> <command>`
- ❌ 不记录完整 stdout/stderr
- ❌ 不能只写"完成""已执行""通过"

**证据格式示例**:

```yaml
integration_test_evidence:
  service_startup:
    command: mvn spring-boot:run -Dspring-boot.run.profiles=test
    cwd: /path/to/project
    started_at: 2026-08-18T10:00:00+08:00
    health_check:
      command: curl -f http://localhost:8080/actuator/health
      response: '{"status":"UP"}'
      result: passed
  test_data_preparation:
    command: mysql -uroot -p test_db < src/test/resources/data/init_test_data.sql
    result: passed
  test_execution:
    - tc_id: TC-001
      command: mvn test -Dtest=UserIntegrationTest#createUser_shouldPersistToDatabase
      exit_code: 0
      result: passed
      output_summary: Tests run: 1, Failures: 0
    - tc_id: TC-002
      command: mvn test -Dtest=UserIntegrationTest#createUser_shouldValidateUniqueEmail
      exit_code: 1
      result: failed
      output_summary: Expected exception not thrown
      repair_round: 1
```

---

## 八、禁用 Mock 规则（v2 Java 适配）

- ❌ 禁止使用 `@MockBean` 替代真实数据库连接
- ❌ 禁止使用 `@MockBean` 替代真实外部服务调用
- ✅ 必须使用 Testcontainers 启动真实数据库（MySQL 8.0）
- ✅ 必须使用真实 HTTP 调用验证外部 API
- ✅ 降级验证必须在 `verification-plan.md` 中明确声明并附带理由

---

## 九、禁止事项

- ❌ 运行 E2E 测试
- ❌ 提交、推送、发布
- ❌ 没有服务启动证据就返回 `passed`
- ❌ 使用 `@MockBean` 替代真实数据库连接（除非明确声明为降级验证）

---

**文档结束**

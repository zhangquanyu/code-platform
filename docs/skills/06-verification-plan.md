# 4p12s-verification-plan 技能

## 基本信息

- **技能名称**: 4p12s-verification-plan
- **技能定位**: 验证计划
- **版本**: 1.0.0
- **文件路径**: `docs/skills/06-verification-plan.md`

---

## 一、概述

从上游交付产物生成验证计划，定义"要验证什么"，不回答"命令如何运行"。

---

## 二、前置依赖

`requirements-register.md`、`prd.md`、`user-stories.md`、`design.md`

---

## 三、输出产物

`verification-plan.md`

---

## 四、验证契约边界（强约束）

- 必须生成：覆盖关系、验证层级、Git 阻断决策、目标测试文件、真实接口断言规则、禁用 Mock 规则
- 不写：启动或连接命令、端口和地址、健康检查命令、执行命令

---

## 五、验证覆盖表（核心资产）

```markdown
| 来源需求 ID | 用户故事 ID | 验收场景 ID | 场景用例 ID | PRD 验收标准 | Given/When/Then 摘要 | 验证类型 | 验证对象层级 | 最低验证层级 | 是否允许降级验证 | 降级理由 | 是否产品行为变更 | 是否提交前自动化验证 | 是否阻断 Git | 是否自动化 | 目标测试文件/目录 | 所需真实服务类型 | 前端是否适用 | 测试数据要求 | 真实接口断言 | 禁用 Mock 规则 |
```

---

## 六、覆盖规则（严格约束）

- 范围内的每个需求 ID 必须映射到至少一个测试用例或验证项
- PRD 的每个验收标准必须映射到至少一个测试用例
- `user-stories.md` 的每个 `AC-xxx` / `SC-xxx` 必须映射到至少一个测试用例
- `design.md` 的每个设计元素必须映射到至少一个测试用例或非代码处理决策
- 范围内的每个前端交互必须映射到至少一个前端 E2E 或 `cross_boundary_e2e` 验证用例
- 目标测试文件/目录不得使用占位符路径

---

## 七、验证类型与 Java 生态对应

| 验证类型 | 说明 | Java 生态对应 | 目标测试文件路径 |
|----------|------|---------------|------------------|
| 单元测试 | 隔离被测对象 | JUnit 5 + Mockito | `src/test/java/cn/zhangquanyu/...` |
| 组件测试 | 模块内部协作 | Spring Boot Test（@WebMvcTest） | `src/test/java/.../interfaces/` |
| 接口测试 | HTTP/RPC 接口 | MockMvc / WebTestClient | `src/test/java/.../interfaces/` |
| 集成测试 | 服务间调用 | Testcontainers + @SpringBootTest | `src/test/java/.../integration/` |
| E2E 测试 | 完整用户流 | Cypress / Playwright | `frontend/cypress/e2e/` |

---

## 八、测试用例结构

```markdown
### TC-001 <用例标题>
- 关联需求 ID / 用户故事 ID / 验收场景 ID / 场景用例 ID
- 优先级：P0 / P1 / P2
- 类型：单元 / 接口 / 集成 / E2E 候选 / 人工验收 / 非代码验证 / 安全 / 性能
- 最低验证层级
- 是否提交前必须执行
- 未执行是否阻断 Git
- 目标测试文件/目录
- 所需真实服务类型
- 真实接口断言
- 禁用 Mock 规则
- 测试数据要求
- 步骤与预期结果
```

---

## 九、禁用 Mock 规则（Java 适配）

### 集成测试禁用 Mock 规则

- 禁止使用 `@MockBean` 替代真实数据库连接（除非明确声明为降级验证）
- 禁止使用 `@MockBean` 替代真实外部服务调用
- 必须使用 Testcontainers 启动真实数据库（MySQL 8.0）
- 必须使用真实 HTTP 调用验证外部 API

### E2E 测试禁用 Mock 规则

- 禁止使用 `cy.intercept()` 拦截请求
- 禁止使用 `page.route().fulfill()` 伪造响应
- 禁止使用 `jest.fn()` 或任何 mock 手段
- 前端用户流必须通过真实前端和真实后端 API 交互

---

## 十、真实服务类型定义

| 服务类型 | 说明 | 启动方式 |
|----------|------|----------|
| `mysql` | MySQL 8.0 数据库 | Testcontainers 或本地 MySQL |
| `redis` | Redis 缓存 | Testcontainers 或本地 Redis |
| `backend` | Spring Boot 后端服务 | `mvn spring-boot:run` |
| `frontend` | Vue3 前端服务 | `npm run dev` |
| `external_api` | 外部 API 服务 | 真实服务或测试环境 |

---

## 十一、禁止事项

- 编写自动化测试代码
- 运行测试
- 编辑产品代码
- 创建 tasks/

---

**文档结束**

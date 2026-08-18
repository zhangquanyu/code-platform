# 4p12s-e2e-test 技能

## 基本信息

- **技能名称**: 4p12s-e2e-test
- **技能定位**: E2E 测试
- **版本**: 1.0.0
- **文件路径**: `docs/skills/10-e2e-test.md`

---

## 一、概述

完整的 E2E 阶段：写测试代码 → 启动真实服务 → 准备数据 → 执行真实 E2E → 修复失败 → 重跑直到通过。

---

## 二、关键约束

- 开发任务阶段**不写**E2E 测试代码
- 本阶段**必须**根据 `verification-plan.md` 中的测试用例契约补齐 E2E 测试代码

---

## 三、前置依赖

`verification-plan.md`、`verification-result.md`

---

## 四、输出产物

追加写入 `verification-result.md` 的 E2E 部分

---

## 五、E2E 测试代码编写要求

### 5.1 目标测试文件路径

```
frontend/cypress/e2e/<模块名>.cy.js     # Cypress
frontend/tests/e2e/<模块名>.spec.ts      # Playwright
```

### 5.2 编写要求

- 使用 `verification-plan.md` 指定的目标测试文件
- 覆盖每个 E2E / `cross_boundary_e2e` TC
- 前端用户流测试必须通过真实前端和真实后端/API 交互
- 测试必须包含真实接口断言，不能只断言静态页面、loading、路由跳转或 mock 响应

### 5.3 禁用 Mock 规则（Cypress/Playwright 适配）

**Cypress 禁用**:

- 禁止使用 `cy.intercept()` 拦截请求
- 禁止使用 `cy.route()` 伪造响应
- 禁止使用 `cy.stub()` 替代真实 API 调用

**Playwright 禁用**:

- 禁止使用 `page.route().fulfill()` 伪造响应
- 禁止使用 `page.route().abort()` 阻断请求

**通用禁用**:

- 禁止使用 `jest.fn()` 或任何 mock 手段
- 不得凭空发明新的业务范围

---

## 六、服务启动和真实验证（强制要求）

### 6.1 前端服务启动

**Vue3 + Vite 启动命令**:

```bash
# 开发模式
npm run dev

# 或预览构建产物
npm run build && npm run preview
```

### 6.2 后端服务启动

```bash
# Spring Boot 启动
mvn spring-boot:run

# 或打包后启动
java -jar target/<app-name>.jar
```

### 6.3 健康检查

```bash
# 前端健康检查
curl -f http://localhost:5173

# 后端健康检查
curl -f http://localhost:8080/actuator/health
```

### 6.4 强制要求

- 前端用户流：必须启动或连接前端服务和后端/API 服务
- API E2E / 跨服务 E2E：必须启动或连接目标 API/服务和必要依赖服务
- 记录最小执行证据
- 执行每个必需服务的健康检查
- 健康检查不通过不得继续执行 E2E

---

## 七、E2E 测试执行

### 7.1 Cypress 执行命令

```bash
# 运行所有 E2E 测试（无头模式）
npx cypress run

# 运行指定文件
npx cypress run --spec cypress/e2e/user.cy.js

# 有头模式（调试用）
npx cypress open
```

### 7.2 Playwright 执行命令

```bash
# 运行所有 E2E 测试
npx playwright test

# 运行指定文件
npx playwright test tests/e2e/user.spec.ts

# 有头模式（调试用）
npx playwright test --headed
```

---

## 八、修复循环（弹性轮次）

1. 对失败 TC 归因：产品代码、E2E 测试代码、环境、测试数据、配置
2. 按失败 TC 关联的 US/AC/SC 和 PRD 读取必要上下文
3. 修复前端代码、后端代码、配置、测试数据或 E2E 测试代码
4. 重跑失败 TC 和受影响回归项
5. 最多达到修复轮次上限（从 `delivery-state.md` 读取 `max_repair_rounds`，默认 8 轮）
6. 超过后返回 `blocked` 或 `failed`

---

## 九、通过条件（全部满足才返回 `passed`）

- 每个 E2E TC 都有目标测试代码
- 当前 E2E 用例所需的所有真实服务都已启动或连接，并有健康检查证据
- 前端用户流包含真实前端和真实后端/API 证据
- 测试数据已准备或明确无需准备
- E2E 命令已真实执行
- 所有提交前必须执行的 E2E TC 都通过
- 每个 E2E TC 都有真实接口断言证据
- 只写了测试代码但没有运行 → 不得返回 `passed`
- 只运行了前端 webServer 或静态页面检查 → 不得返回 `passed`
- 没有真实服务或真实 API 证据 → 不得返回 `passed`
- 使用 mock 结果作为通过证据 → 不得返回 `passed`

---

## 十、真实接口断言示例

### Cypress 真实接口断言

```javascript
describe('用户管理 E2E', () => {
  it('创建用户后应能在列表中看到', () => {
    // 访问用户创建页面
    cy.visit('/users/create')

    // 填写表单
    cy.get('[data-cy=username]').type('testuser')
    cy.get('[data-cy=email]').type('test@example.com')
    cy.get('[data-cy=submit]').click()

    // 真实接口断言：验证 API 返回
    cy.request('GET', '/api/users').then((response) => {
      expect(response.status).to.eq(200)
      expect(response.body).to.have.property('data')
      expect(response.body.data).to.have.length.greaterThan(0)
      expect(response.body.data[0]).to.include({ username: 'testuser' })
    })

    // 验证页面显示
    cy.visit('/users')
    cy.get('[data-cy=user-list]').should('contain', 'testuser')
  })
})
```

---

## 十一、禁止事项

- 使用 `cy.intercept()`、`page.route().fulfill()` 等 mock 手段
- 只写测试代码不运行
- 只运行前端 webServer 或静态页面检查
- 没有真实服务或真实 API 证据
- 使用 mock 结果作为通过证据
- 重复运行集成测试（除非 E2E 修复影响了集成边界）
- 提交、推送、发布

---

**文档结束**

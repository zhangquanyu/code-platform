# 4p12s-user-stories 技能（v2）

## 基本信息

- **技能名称**: 4p12s-user-stories
- **技能定位**: 用户故事与验收场景
- **版本**: v2.0.0
- **文件路径**: `docs/4p12s-skills-v2/04-user-stories.md`

---

## 一、概述

从 `requirements-register.md` 和 `prd.md` 生成用户故事和 Given/When/Then 验收场景。

### v2 改进点

- ✅ 验证层级增加 Java 生态对应说明
- ✅ 支持增量交付（复用已有 user-stories.md）

---

## 二、前置依赖

`requirements-register.md`、`prd.md`

---

## 三、输出产物

`user-stories.md`

---

## 四、核心结构

```markdown
## 场景总览（表格）
## 用户故事
### US-001 <故事标题>
#### AC-001 <验收场景标题>
- 场景用例 ID: SC-001
- 是否产品行为变更：是/否
- 最低验证层级：unit / component / service_api / service_integration / cross_boundary_e2e
- 是否提交前自动化验证：是/否
- 是否阻断 Git：是/否
```gherkin
Given ...
When ...
Then ...
```
```

---

## 五、映射规则（严格约束）

- ✅ 范围内的每个需求 ID 必须出现在至少一个 `US-xxx` 中
- ✅ PRD 的每个验收标准必须映射到至少一个 `AC-xxx` / `SC-xxx`
- ✅ 范围内的前端或用户交互验收默认为 `cross_boundary_e2e`，必须标记为 Git 阻断
- ❌ 如果需要的验收场景没有对应的 PRD 验收标准，返回 `blocked` 而非凭空创建
- ❌ 不得引入上游产物中不存在的需求

---

## 六、验证层级定义（v2 Java 适配）

| 层级 | 说明 | Java 生态对应 |
|------|------|---------------|
| `unit` | 单元测试，隔离被测对象 | JUnit 5 + Mockito |
| `component` | 组件测试，验证模块内部协作 | Spring Boot Test（切片测试） |
| `service_api` | 服务 API 测试，验证 HTTP/RPC 接口 | MockMvc / WebTestClient |
| `service_integration` | 服务集成测试，验证服务间调用 | Testcontainers + RestTemplate |
| `cross_boundary_e2e` | 跨边界 E2E，验证完整用户流 | Cypress / Playwright |

---

## 七、ID 所有权

- `US-xxx`、`AC-xxx`、`SC-xxx` 由 `user-stories.md` 拥有
- PRD 验收标准是输入依据，下游验收场景 ID 由本文件重新分配

---

## 八、增量交付支持

如果已有 `user-stories.md` 且用户故事未变更：

1. 读取已有文件
2. 确认内容仍然有效
3. 在 `delivery-state.md` 中标记为 `reused`
4. 跳过本技能，直接进入 `4p12s-technical-design`

---

## 九、禁止事项

- ❌ 创建技术设计、验证计划、任务、代码、提交或部署产物
- ❌ 引入上游不存在的需求

---

**文档结束**

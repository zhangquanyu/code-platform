# 4p12s-technical-design 技能（v2）

## 基本信息

- **技能名称**: 4p12s-technical-design
- **技能定位**: 技术设计方案（DDD 支持）
- **版本**: v2.0.0
- **文件路径**: `docs/4p12s-skills-v2/05-technical-design.md`

---

## 一、概述

为已接受的业务范围生成技术设计文档，连接需求和验收场景到具体技术实现。

### v2 改进点

- ✅ **新增 DDD 分层架构设计指导**（interfaces/application/domain/infrastructure）
- ✅ 增加 Java/Spring Boot/Maven 项目的设计模板
- ✅ 支持 MapStruct、Lombok 等常用库的设计约束
- ✅ 支持增量交付（复用已有 design.md）

---

## 二、前置依赖

`requirements-register.md`、`prd.md`、`user-stories.md`

---

## 三、输出产物

`design.md`

---

## 四、上下文发现策略（渐进式披露）

1. 首先读取项目入口文档（`.gientech/AGENTS.md`、`AGENTS.md` 或 README）
2. 仅跟进本次功能所需的文档
3. 当文档无法识别时，使用聚焦的符号或文本搜索
4. ❌ 禁止扫描整个仓库

---

## 五、核心章节

1. 项目上下文
2. 实现范围（范围内/范围外）
3. 方案设计（模块与职责、接口/数据流/事件流、状态流、权限、配置、错误处理）
4. 设计元素清单
5. 需求到设计映射表
6. 测试与验证设计
7. 风险与开放问题

---

## 六、DDD 分层架构设计（v2 新增）

### 6.1 包结构模板

```
cn.zhangquanyu.<模块名>
├── interfaces/           # 接口层
│   ├── rest/             # REST Controller
│   │   └── XxxController.java
│   └── dto/              # 数据传输对象
│       └── XxxDTO.java
├── application/          # 应用层
│   ├── service/          # 应用服务
│   │   └── XxxAppService.java
│   ├── cmd/              # 命令对象
│   │   └── XxxCmd.java
│   └── vo/               # 值对象
│       └── XxxVO.java
├── domain/               # 领域层
│   ├── model/            # 领域实体
│   │   └── Xxx.java
│   ├── repository/       # 仓储接口
│   │   └── XxxRepository.java
│   └── service/          # 领域服务
│       └── XxxDomainService.java
└── infrastructure/       # 基础设施层
    ├── persistence/      # 仓储实现
    │   ├── XxxPO.java    # 持久化对象
    │   ├── XxxRepositoryImpl.java
    │   └── XxxJpaRepository.java
    └── config/           # 配置
        └── XxxConfig.java
```

### 6.2 DDD 设计约束

- ✅ 领域层不依赖基础设施层
- ✅ Repository 接口定义在领域层，实现在基础设施层
- ✅ AppService 负责事务边界和编排，不包含业务规则
- ✅ 领域实体包含业务规则和不变式
- ✅ 使用 MapStruct 进行对象转换（DTO ↔ Cmd ↔ Entity ↔ PO）
- ✅ 使用 Lombok 简化样板代码

### 6.3 技术栈约束

| 层次 | 技术选型 | 说明 |
|------|----------|------|
| 接口层 | Spring MVC | RESTful API |
| 应用层 | Spring Service | 事务管理、编排 |
| 领域层 | POJO | 无框架依赖 |
| 基础设施层 | Spring Data JPA | 数据持久化 |
| 对象映射 | MapStruct | 编译时生成 |
| 样板代码 | Lombok | @Data、@Builder 等 |
| ID 生成 | Snowflake | 雪花算法 |

---

## 七、设计元素清单（强制要求）

```markdown
| 设计元素 ID | 类型 | 名称 | 所属模块 | 来源需求 ID | 关联用户故事/验收场景 | 是否需要实现 | 是否需要验证 | 说明 |
```

---

## 八、映射规则

- ✅ 范围内的每个需求 ID 必须有设计承接行或已记录的原因
- ✅ 每个 Git 阻断验收项必须有设计级验证策略
- ✅ 每个设计元素必须映射到上游需求 ID 和 `US-xxx` / `AC-xxx` / `SC-xxx`
- ✅ 每个范围内的前端交互都有前端设计范围和 E2E 验证策略
- ❌ 不得创建新的用户故事、验收场景或场景用例 ID
- ❌ 不得编造文件路径或 API

---

## 九、增量交付支持

如果已有 `design.md` 且设计未变更：

1. 读取已有文件
2. 确认内容仍然有效
3. 在 `delivery-state.md` 中标记为 `reused`
4. 跳过本技能，直接进入 `4p12s-verification-plan`

---

## 十、禁止事项

- ❌ 创建 verification-plan.md、tasks/、代码、提交或部署产物
- ❌ 编造文件路径或 API
- ❌ 在领域层引入框架依赖

---

**文档结束**

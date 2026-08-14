# 开发平台（code-platform）

基于元数据驱动的低代码开发平台，包含应用、微服务、模型、元数据、服务、服务编排六大模块的完整研发资产管理能力。

## 目录结构

```
code/
├── platform-server/     # 后端服务（Spring Boot 3 + JPA + MySQL）
│   ├── pom.xml
│   ├── src/main/java/cn/zhangquanyu/
│   │   ├── CodePlatformApplication.java   # 启动类
│   │   ├── shared/                        # 公共层（Result/PageQuery/BaseEntity/异常）
│   │   ├── application/                   # 应用+微服务模块
│   │   ├── model/                         # 模型模块
│   │   ├── metadata/                      # 元数据模块
│   │   ├── service/                       # 服务+编排模块
│   │   └── infrastructure/                # 基础设施（配置/审计/雪花ID）
│   └── src/main/resources/
│       ├── application.yml
│       └── db/init.sql                    # 数据库初始化脚本
│
└── platform-web/        # 前端（Vue3 + Vite + Element Plus）
    ├── package.json
    ├── vite.config.ts
    └── src/
        ├── api/          # 6 个模块的接口封装
        ├── types/        # TS 类型定义
        ├── utils/        # axios 封装
        ├── layouts/      # 主布局（侧边栏导航）
        ├── router/       # Vue Router 路由
        └── views/        # 各模块页面（列表+详情）
```

## 技术栈

- **后端**：Java 17、Spring Boot 3.2、Spring Data JPA、MySQL 8.0、Lombok、MapStruct
- **前端**：Vue 3（Composition API）、Vite、Element Plus、Pinia、Vue Router、Axios

## 快速启动

### 1. 初始化数据库

```bash
mysql -uroot -p < code/platform-server/src/main/resources/db/init.sql
```

> 默认数据库名 `code_platform`，可在 `application.yml` 中修改连接信息。

### 2. 启动后端

```bash
cd code/platform-server
mvn spring-boot:run
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

后端启动后监听 `http://localhost:8080`，所有接口前缀 `/api/v1`。

### 3. 启动前端

```bash
cd code/platform-web
npm install
npm run dev
```

前端启动后访问 `http://localhost:5173`，开发环境已配置代理将 `/api` 转发到后端。

## 核心模块与接口

| 模块 | 后端 Controller | 前端路由 |
| --- | --- | --- |
| 应用管理 | `/api/v1/applications` | `/applications` |
| 微服务管理 | `/api/v1/microservices` | `/microservices` |
| 模型管理 | `/api/v1/models` | `/models` |
| 元数据管理 | `/api/v1/metadata` | `/metadata` |
| 服务管理 | `/api/v1/services` | `/services` |
| 服务编排 | `/api/v1/orchestrations` | `/orchestrations` |

## 关键说明

1. **主键策略**：使用简化版雪花算法（`SnowflakeConfig`），保证单机唯一；生产环境建议替换为完整雪花或分布式 ID 服务。
2. **软删除**：所有核心业务表使用 `is_deleted` 字段，删除操作为软删除。
3. **审计字段**：通过 Spring Data JPA Auditing 自动填充 `create_time/update_time/create_by/update_by`，当前用户 ID 为 Mock（`AuditorAwareImpl` 返回 1）。
4. **编排调试**：当前为 Mock 执行模式，按节点顺序遍历并对 SERVICE 节点生成 mock 出参；后续可接入真实 HTTP 转发。
5. **CORS**：开发期后端已开放 `/api/**` 跨域，前端 Vite 也配置了代理，二者择一即可。
6. **鉴权**：本期未接入鉴权，接口可直接访问；`AuditorAwareImpl` 预留了用户上下文接入点。

## 实体关联关系

```
应用 1:N 微服务 1:N (模型 1:N 字段→可关联元数据 / 服务 / 编排→可跨微服务调用服务)
应用 1:N 元数据 1:N 元数据项
```

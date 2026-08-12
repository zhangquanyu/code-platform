# 开发平台运维手册

> 本手册面向开发、测试与运维人员，涵盖环境准备、构建部署、配置管理、监控运维、故障排查等全流程操作。

---

## 一、项目概览

### 1.1 系统简介

开发平台（code-platform）是基于元数据驱动的低代码开发平台，包含六大核心模块：

| 模块 | 说明 |
|------|------|
| 应用管理 | 管理业务应用的全生命周期 |
| 微服务管理 | 管理应用下的微服务 |
| 模型管理 | 定义数据模型及其字段 |
| 元数据管理 | 管理枚举类元数据 |
| 服务管理 | 定义微服务对外提供的 API |
| 服务编排 | 可视化编排服务调用流程 |

### 1.2 技术栈

| 层级 | 技术 | 版本 |
|------|------|------|
| 后端框架 | Spring Boot | 3.2.5 |
| 持久层 | Spring Data JPA + Hibernate | 6.4.4 |
| 数据库 | MySQL | 8.0 |
| ORM 工具 | MapStruct | 1.5.5 |
| 代码简化 | Lombok | 1.18.30 |
| 监控 | Spring Boot Actuator | 3.2.5 |
| 前端框架 | Vue 3 (Composition API) | 3.4.31 |
| 前端构建 | Vite | 5.3.3 |
| UI 组件 | Element Plus | 2.7.6 |
| 状态管理 | Pinia | 2.1.7 |
| JDK | OpenJDK | 17 |

### 1.3 项目结构

```
code-platform/
├── code/
│   ├── platform-server/                    # 后端服务
│   │   ├── pom.xml
│   │   ├── src/main/java/cn/zhangquanyu/
│   │   │   ├── CodePlatformApplication.java          # 启动类
│   │   │   ├── shared/                               # 公共层
│   │   │   │   ├── api/                              # Result/PageQuery/PageResult
│   │   │   │   ├── domain/                           # BaseEntity/StatusEnum
│   │   │   │   ├── exception/                        # 全局异常处理
│   │   │   │   └── util/                             # SpecUtil 工具类
│   │   │   ├── application/                          # 应用+微服务模块
│   │   │   ├── model/                                # 模型模块
│   │   │   ├── metadata/                             # 元数据模块
│   │   │   ├── service/                              # 服务+编排模块
│   │   │   └── infrastructure/                       # 基础设施
│   │   │       ├── audit/                            # 审计填充
│   │   │       ├── config/                           # 雪花ID/CORS配置
│   │   │       └── MockDataInitializer.java          # Mock 数据初始化
│   │   └── src/main/resources/
│   │       ├── application.yml                       # 默认配置（MySQL）
│   │       ├── application-dev.yml                   # 开发环境
│   │       ├── application-prod.yml                  # 生产环境
│   │       ├── application-h2.yml                    # H2 内存数据库
│   │       └── db/init.sql                           # 数据库初始化脚本
│   │
│   └── platform-web/                                 # 前端
│       ├── package.json
│       ├── vite.config.ts
│       └── src/
│           ├── api/                                  # 接口封装
│           ├── types/                                # TS 类型
│           ├── utils/                                # axios 封装
│           ├── layouts/                              # 布局
│           ├── router/                               # 路由
│           └── views/                                # 页面
│
└── docs/
    ├── logging-guide.md                              # 日志配置与排查指南
    └── devops-manual.md                              # 本手册
```

---

## 二、环境准备

### 2.1 开发环境要求

| 软件 | 最低版本 | 推荐版本 | 说明 |
|------|----------|----------|------|
| JDK | 17 | 17 | 必须，项目基于 Java 17 |
| Maven | 3.8 | 3.9+ | 后端构建工具 |
| Node.js | 18 | 20 LTS | 前端构建工具 |
| npm | 9 | 10+ | 前端包管理 |
| MySQL | 8.0 | 8.0 | 主数据库 |
| Git | 2.30 | 最新 | 版本控制 |

### 2.2 JDK 安装与验证

```bash
# 安装 JDK 17（macOS）
brew install openjdk@17

# 配置环境变量（~/.zshrc）
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
export PATH=$JAVA_HOME/bin:$PATH

# 验证
java -version
# 输出: openjdk version "17.x.x"
```

### 2.3 Maven 安装与验证

```bash
# 安装 Maven（macOS）
brew install maven

# 验证
mvn -version
# 输出: Apache Maven 3.9.x
```

### 2.4 Node.js 安装与验证

```bash
# 安装 Node.js（macOS）
brew install node@20

# 验证
node -v
# 输出: v20.x.x

npm -v
# 输出: 10.x.x
```

### 2.5 MySQL 安装与配置

```bash
# 安装 MySQL 8.0（macOS）
brew install mysql@8.0

# 启动 MySQL 服务
brew services start mysql@8.0

# 初始化 root 密码
mysql_secure_installation

# 创建项目数据库用户
mysql -uroot -p
```

```sql
-- 创建数据库
CREATE DATABASE IF NOT EXISTS code_platform
  DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 创建用户并授权
CREATE USER 'quanyu'@'localhost' IDENTIFIED BY '123456';
GRANT ALL PRIVILEGES ON code_platform.* TO 'quanyu'@'localhost';
FLUSH PRIVILEGES;
```

---

## 三、构建与部署

### 3.1 数据库初始化

```bash
# 使用项目配置的用户执行初始化脚本
mysql -h localhost -P 3306 -u quanyu -p123456 < code/platform-server/src/main/resources/db/init.sql
```

**验证表创建**：
```bash
mysql -h localhost -P 3306 -u quanyu -p123456 code_platform -e "SHOW TABLES;"
```

**预期结果（11 张表）**：
```
dev_application
dev_metadata
dev_metadata_item
dev_microservice
dev_model
dev_model_field
dev_orch_edge
dev_orch_node
dev_orchestration
dev_service
dev_service_param
```

### 3.2 后端构建

#### 3.2.1 开发环境运行

```bash
cd code/platform-server

# 方式一：直接运行（使用默认 MySQL 配置）
mvn spring-boot:run

# 方式二：使用开发 profile（DEBUG 日志 + 全部 Actuator 端点）
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# 方式三：使用 H2 内存数据库（无需 MySQL）
mvn spring-boot:run -Dspring-boot.run.profiles=h2
```

#### 3.2.2 编译打包

```bash
cd code/platform-server

# 清理并打包（跳过测试）
mvn clean package -DskipTests

# 产物位置
ls target/platform-server-1.0.0.jar
```

#### 3.2.3 生产环境部署

```bash
# 启动 JAR 包（生产 profile）
java -jar target/platform-server-1.0.0.jar --spring.profiles.active=prod

# 后台运行
nohup java -jar platform-server-1.0.0.jar --spring.profiles.active=prod > /dev/null 2>&1 &

# 指定端口
java -jar platform-server-1.0.0.jar --spring.profiles.active=prod --server.port=9090
```

### 3.3 前端构建

#### 3.3.1 开发环境运行

```bash
cd code/platform-web

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

前端开发服务器启动后访问 `http://localhost:5173`，Vite 已配置代理将 `/api` 请求转发到 `http://localhost:8080`。

#### 3.3.2 生产环境构建

```bash
cd code/platform-web

# 安装依赖
npm install

# 构建生产包
npm run build

# 产物位置
ls dist/
```

#### 3.3.3 前端部署

将 `dist/` 目录部署到 Nginx：

```nginx
server {
    listen 80;
    server_name your-domain.com;

    # 前端静态资源
    location / {
        root /usr/share/nginx/html/platform-web;
        try_files $uri $uri/ /index.html;
    }

    # API 反向代理
    location /api/ {
        proxy_pass http://backend-server:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

### 3.4 完整启动流程

```bash
# 1. 启动 MySQL（如未设置开机启动）
brew services start mysql@8.0

# 2. 初始化数据库（首次部署）
mysql -h localhost -P 3306 -u quanyu -p123456 < code/platform-server/src/main/resources/db/init.sql

# 3. 启动后端（终端 1）
cd code/platform-server
mvn spring-boot:run

# 4. 启动前端（终端 2）
cd code/platform-web
npm run dev

# 5. 访问应用
# 前端: http://localhost:5173
# 后端: http://localhost:8080
# Actuator: http://localhost:8080/actuator/loggers
```

---

## 四、配置管理

### 4.1 配置文件体系

项目采用多 Profile 配置体系：

| 配置文件 | Profile | 用途 | 日志级别 |
|----------|---------|------|----------|
| `application.yml` | default | 默认配置（MySQL） | INFO |
| `application-dev.yml` | dev | 开发环境 | DEBUG |
| `application-prod.yml` | prod | 生产环境 | INFO |
| `application-h2.yml` | h2 | H2 内存数据库（本地测试） | DEBUG |

### 4.2 核心配置说明

#### 4.2.1 数据源配置

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/code_platform?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
    username: quanyu
    password: 123456
    driver-class-name: com.mysql.cj.jdbc.Driver
```

**参数说明**：
- `useUnicode=true&characterEncoding=utf8`：使用 UTF-8 编码
- `useSSL=false`：关闭 SSL（开发环境）
- `serverTimezone=Asia/Shanghai`：时区设置
- `allowPublicKeyRetrieval=true`：允许公钥检索（MySQL 8.0）

#### 4.2.2 JPA 配置

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: none          # 不自动建表，使用 init.sql
    show-sql: false            # 不在控制台打印 SQL
    properties:
      hibernate:
        format_sql: true       # 格式化 SQL（日志中）
        dialect: org.hibernate.dialect.MySQLDialect
    open-in-view: false        # 关闭 OSIV（生产推荐）
```

**`ddl-auto` 选项说明**：
| 值 | 行为 | 使用场景 |
|----|------|----------|
| `none` | 不做任何操作 | 生产环境（默认） |
| `validate` | 只验证不修改 | 测试环境 |
| `update` | 自动更新表结构 | 开发环境（不推荐） |
| `create` | 每次启动重建表 | H2 测试环境 |
| `create-drop` | 启动建表，停止删表 | H2 内存数据库 |

#### 4.2.3 日志配置

```yaml
logging:
  level:
    cn.zhangquanyu: INFO                              # 业务模块
    org.hibernate.SQL: WARN                           # SQL 语句
    org.hibernate.type.descriptor.sql.BasicBinder: WARN # SQL 参数
    org.springframework: WARN                          # Spring 框架
    org.springframework.web: INFO                     # Web 请求
  file:
    name: logs/code-platform.log                      # 日志文件
  logback:
    rollingpolicy:
      max-file-size: 100MB                            # 单文件最大
      max-history: 30                                 # 保留天数
      total-size-cap: 5GB                             # 总容量上限
```

#### 4.2.4 Actuator 配置

```yaml
management:
  endpoints:
    web:
      exposure:
        include: loggers                              # 暴露端点
  endpoint:
    loggers:
      enabled: true                                   # 启用日志动态调整
```

### 4.3 环境变量覆盖

所有配置项均可通过环境变量覆盖，格式为 `SPRING_` 前缀 + 下划线分隔：

```bash
# 覆盖数据库连接
export SPRING_DATASOURCE_URL="jdbc:mysql://prod-db:3306/code_platform"
export SPRING_DATASOURCE_USERNAME="prod_user"
export SPRING_DATASOURCE_PASSWORD="prod_password"

# 覆盖端口
export SERVER_PORT="9090"

# 指定 Profile
export SPRING_PROFILES_ACTIVE="prod"

# 启动
java -jar platform-server-1.0.0.jar
```

### 4.4 Profile 切换

```bash
# 方式一：命令行参数
java -jar app.jar --spring.profiles.active=prod

# 方式二：环境变量
export SPRING_PROFILES_ACTIVE=prod
java -jar app.jar

# 方式三：Maven 启动
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### 4.5 各 Profile 配置对比

| 配置项 | default | dev | prod | h2 |
|--------|---------|-----|------|-----|
| 数据库 | MySQL | MySQL | MySQL | H2 内存 |
| ddl-auto | none | none | none | create-drop |
| 业务日志 | INFO | DEBUG | INFO | DEBUG |
| SQL 日志 | WARN | DEBUG | WARN | DEBUG |
| Actuator | loggers | 全部 | loggers | - |
| 日志文件 | logs/ | 控制台 | /var/log/ | 控制台 |
| Mock 数据 | 不初始化 | 不初始化 | 不初始化 | 自动初始化 |

---

## 五、API 接口说明

### 5.1 接口规范

- **基础路径**：`/api/v1`
- **请求格式**：`application/json`
- **响应格式**：统一 `Result<T>` 结构
- **时间格式**：`yyyy-MM-dd HH:mm:ss`
- **时区**：`Asia/Shanghai`

### 5.2 统一响应结构

```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

### 5.3 接口列表

#### 5.3.1 应用管理 `/api/v1/applications`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/` | 分页查询应用 |
| GET | `/simple` | 简易列表（下拉选择用） |
| GET | `/{id}` | 查询应用详情 |
| GET | `/{id}/microservices` | 查询应用下的微服务 |
| POST | `/` | 创建应用 |
| PUT | `/{id}` | 更新应用 |
| DELETE | `/{id}` | 删除应用（软删除） |
| PUT | `/{id}/status` | 更新应用状态 |

#### 5.3.2 微服务管理 `/api/v1/microservices`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/` | 分页查询微服务 |
| GET | `/by-application/{applicationId}` | 按应用查询微服务 |
| GET | `/{id}` | 查询微服务详情 |
| GET | `/{id}/summary` | 查询微服务资源汇总 |
| POST | `/` | 创建微服务 |
| PUT | `/{id}` | 更新微服务 |
| DELETE | `/{id}` | 删除微服务（软删除） |
| PUT | `/{id}/status` | 更新微服务状态 |

#### 5.3.3 模型管理 `/api/v1/models`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/` | 分页查询模型 |
| GET | `/by-microservice/{microserviceId}` | 按微服务查询模型 |
| GET | `/{id}` | 查询模型详情（含字段） |
| POST | `/` | 创建模型 |
| PUT | `/{id}` | 更新模型 |
| DELETE | `/{id}` | 删除模型（软删除） |
| POST | `/{id}/fields/batch-save` | 批量保存模型字段 |

#### 5.3.4 元数据管理 `/api/v1/metadata`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/` | 分页查询元数据 |
| GET | `/by-application/{applicationId}` | 按应用查询元数据 |
| GET | `/{id}` | 查询元数据详情（含枚举项） |
| GET | `/{id}/references` | 查询元数据引用关系 |
| POST | `/` | 创建元数据 |
| PUT | `/{id}` | 更新元数据 |
| DELETE | `/{id}` | 删除元数据（软删除） |
| PUT | `/{id}/status` | 更新元数据状态 |
| POST | `/{id}/items/batch-save` | 批量保存元数据项 |

#### 5.3.5 服务管理 `/api/v1/services`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/` | 分页查询服务 |
| GET | `/by-microservice/{microserviceId}` | 按微服务查询服务 |
| GET | `/{id}` | 查询服务详情（含参数） |
| POST | `/` | 创建服务 |
| PUT | `/{id}` | 更新服务 |
| DELETE | `/{id}` | 删除服务（软删除） |
| PUT | `/{id}/status` | 更新服务状态 |

#### 5.3.6 服务编排 `/api/v1/orchestrations`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/` | 分页查询编排 |
| GET | `/{id}` | 查询编排详情（含节点/连线） |
| GET | `/{id}/health` | 健康检查 |
| POST | `/` | 创建编排 |
| PUT | `/{id}` | 更新编排（含节点/连线整体保存） |
| POST | `/{id}/validate` | 流程校验 |
| POST | `/{id}/debug` | 调试执行 |
| DELETE | `/{id}` | 删除编排（软删除） |
| PUT | `/{id}/status` | 更新编排状态 |

---

## 六、监控与运维

### 6.1 健康检查

```bash
# 检查应用是否启动
curl http://localhost:8080/actuator/health

# 响应示例
{
  "status": "UP"
}
```

### 6.2 日志动态调整

无需重启应用，通过 Actuator 在线调整日志级别：

```bash
# 查看当前日志级别
curl http://localhost:8080/actuator/loggers/cn.zhangquanyu

# 设置为 DEBUG
curl -X POST http://localhost:8080/actuator/loggers/cn.zhangquanyu \
  -H "Content-Type: application/json" \
  -d '{"configuredLevel": "DEBUG"}'

# 设置特定模块
curl -X POST http://localhost:8080/actuator/loggers/cn.zhangquanyu.service.application \
  -H "Content-Type: application/json" \
  -d '{"configuredLevel": "DEBUG"}'

# 恢复 INFO
curl -X POST http://localhost:8080/actuator/loggers/cn.zhangquanyu \
  -H "Content-Type: application/json" \
  -d '{"configuredLevel": "INFO"}'
```

### 6.3 日志文件管理

#### 6.3.1 日志文件位置

| 环境 | 路径 |
|------|------|
| 开发（default/dev） | `logs/code-platform.log` |
| 生产（prod） | `/var/log/code-platform/app.log` |

#### 6.3.2 日志滚动策略

- **单文件最大**：100MB
- **保留天数**：30 天
- **总容量上限**：5GB（开发）/ 10GB（生产）

#### 6.3.3 常用日志查看命令

```bash
# 实时查看日志
tail -f logs/code-platform.log

# 查看错误日志
grep "ERROR" logs/code-platform.log | tail -20

# 按模块过滤
grep "\[应用\]" logs/code-platform.log
grep "\[微服务\]" logs/code-platform.log
grep "\[模型\]" logs/code-platform.log
grep "\[元数据\]" logs/code-platform.log
grep "\[服务\]" logs/code-platform.log
grep "\[编排\]" logs/code-platform.log

# 按操作类型过滤
grep "创建开始" logs/code-platform.log
grep "删除开始" logs/code-platform.log

# 追踪特定 ID
grep "id=500" logs/code-platform.log
```

### 6.4 数据库监控

```bash
# 连接数查看
mysql -h localhost -P 3306 -u quanyu -p123456 -e "
SHOW STATUS LIKE 'Threads_connected';
SHOW STATUS LIKE 'Max_used_connections';
SHOW VARIABLES LIKE 'max_connections';
"

# 慢查询（需开启慢查询日志）
mysql -h localhost -P 3306 -u quanyu -p123456 -e "
SHOW VARIABLES LIKE 'slow_query_log%';
SHOW VARIABLES LIKE 'long_query_time';
"

# 表数据量统计
mysql -h localhost -P 3306 -u quanyu -p123456 code_platform -e "
SELECT table_name, table_rows, data_length/1024/1024 AS data_mb
FROM information_schema.tables
WHERE table_schema = 'code_platform';
"
```

### 6.5 JVM 监控

```bash
# 查看进程
jps -l

# 查看内存使用
jstat -gcutil <pid> 1000 10

# 生成堆dump
jmap -dump:format=b,file=heap.hprof <pid>

# 查看线程
jstack <pid> > thread-dump.txt
```

---

## 七、测试

### 7.1 单元测试

项目包含 6 个 Service 层单元测试，共 86 个测试用例。

```bash
cd code/platform-server

# 运行所有测试
mvn test

# 运行特定模块测试
mvn test -Dtest="ApplicationAppServiceTest"
mvn test -Dtest="MicroserviceAppServiceTest"
mvn test -Dtest="ModelAppServiceTest"
mvn test -Dtest="MetadataAppServiceTest"
mvn test -Dtest="ServiceAppServiceTest"
mvn test -Dtest="OrchestrationAppServiceTest"

# 运行并生成报告
mvn test surefire-report:report
# 报告位置: target/site/surefire-report.html
```

### 7.2 测试覆盖模块

| 测试类 | 覆盖内容 |
|--------|----------|
| ApplicationAppServiceTest | 分页、详情、创建、更新、删除、状态更新 |
| MicroserviceAppServiceTest | 分页、详情、创建、更新、删除、资源汇总 |
| ModelAppServiceTest | 分页、详情、创建、更新、删除、批量保存字段 |
| MetadataAppServiceTest | 分页、详情、创建、更新、删除、批量保存枚举项 |
| ServiceAppServiceTest | 分页、详情、创建、更新、删除、参数 CRUD |
| OrchestrationAppServiceTest | 分页、详情、创建、更新、删除、健康检查、调试、流程校验 |

---

## 八、故障排查

### 8.1 常见问题

#### 问题 1：应用启动失败 - 端口被占用

**现象**：
```
Web server failed to start. Port 8080 was already in use.
```

**解决**：
```bash
# 查找占用进程
lsof -i:8080

# 终止进程
kill -9 <pid>

# 或更换端口
mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=9090"
```

#### 问题 2：数据库连接失败

**现象**：
```
Failed to obtain JDBC Connection
Communications link failure
```

**排查步骤**：
1. 确认 MySQL 服务已启动：`brew services list | grep mysql`
2. 确认连接信息正确：检查 `application.yml` 中的 `url`/`username`/`password`
3. 确认用户权限：`mysql -u quanyu -p123456 -e "SHOW GRANTS;"`
4. 确认防火墙未拦截 3306 端口

#### 问题 3：表不存在

**现象**：
```
Table 'code_platform.xxx' doesn't exist
```

**解决**：
```bash
# 执行初始化脚本
mysql -h localhost -P 3306 -u quanyu -p123456 < code/platform-server/src/main/resources/db/init.sql

# 验证表
mysql -h localhost -P 3306 -u quanyu -p123456 code_platform -e "SHOW TABLES;"
```

#### 问题 4：雪花 ID 生成失败

**现象**：
```
Table 'code_platform.snowflake' doesn't exist
```

**原因**：实体类未正确配置 `@GenericGenerator` 注解。

**解决**：确认实体类 `@Id` 注解上方包含：
```java
@GenericGenerator(name = "snowflake", strategy = "cn.zhangquanyu.infrastructure.config.SnowflakeConfig")
@GeneratedValue(generator = "snowflake")
```

#### 问题 5：前端无法访问 API

**现象**：前端页面显示但数据加载失败，控制台报 CORS 错误。

**排查步骤**：
1. 确认后端已启动：`curl http://localhost:8080/actuator/health`
2. 确认 Vite 代理配置（`vite.config.ts` 中 `proxy` 指向 `8080`）
3. 确认后端 CORS 配置（`WebCorsConfig.java` 允许 `/api/**`）

#### 问题 6：H2 Profile 启动后无数据

**现象**：使用 `h2` profile 启动，页面无数据。

**原因**：`MockDataInitializer` 仅在 `h2` profile 下执行。

**解决**：
```bash
# 使用 h2 profile 启动会自动初始化 Mock 数据
mvn spring-boot:run -Dspring-boot.run.profiles=h2
```

### 8.2 故障排查流程

```
问题发生
  │
  ├─ 应用无法启动？
  │   ├─ 端口冲突？ → lsof -i:8080
  │   ├─ 数据库连不上？ → 检查 MySQL 服务和配置
  │   └─ 编译错误？ → mvn clean compile
  │
  ├─ 接口返回错误？
  │   ├─ 400 → 检查请求参数格式
  │   ├─ 404 → 检查请求路径
  │   ├─ 500 → 查看后端日志 logs/code-platform.log
  │   └─ 业务异常 → 查看日志中的 WARN 信息
  │
  ├─ 数据不正确？
  │   ├─ 查看业务日志（按模块前缀过滤）
  │   ├─ 开启 DEBUG 级别查看详细数据
  │   └─ 直接查询数据库确认
  │
  └─ 性能问题？
      ├─ 检查 JVM 内存：jstat -gcutil <pid>
      ├─ 检查数据库慢查询
      └─ 检查日志文件大小
```

---

## 九、数据备份与恢复

### 9.1 数据库备份

```bash
# 全量备份
mysqldump -h localhost -P 3306 -u quanyu -p123456 code_platform > backup_$(date +%Y%m%d).sql

# 压缩备份
mysqldump -h localhost -P 3306 -u quanyu -p123456 code_platform | gzip > backup_$(date +%Y%m%d).sql.gz

# 仅备份数据（不包含建表语句）
mysqldump -h localhost -P 3306 -u quanyu -p123456 --no-create-info code_platform > data_backup.sql
```

### 9.2 数据库恢复

```bash
# 恢复全量备份
mysql -h localhost -P 3306 -u quanyu -p123456 code_platform < backup_20260812.sql

# 恢复压缩备份
gunzip < backup_20260812.sql.gz | mysql -h localhost -P 3306 -u quanyu -p123456 code_platform
```

### 9.3 定时备份（crontab）

```bash
# 编辑定时任务
crontab -e

# 每天凌晨 2 点备份
0 2 * * * mysqldump -h localhost -u quanyu -p123456 code_platform > /backup/code_platform_$(date +\%Y\%m\%d).sql

# 保留最近 30 天的备份
0 3 * * * find /backup -name "code_platform_*.sql" -mtime +30 -delete
```

---

## 十、安全注意事项

### 10.1 生产环境安全清单

- [ ] 修改默认数据库密码（`123456` → 强密码）
- [ ] 创建专用数据库用户（避免使用 root）
- [ ] 开启 MySQL SSL 连接（`useSSL=true`）
- [ ] 配置防火墙（仅允许应用服务器访问 3306 端口）
- [ ] 启用 HTTPS（配置 SSL 证书）
- [ ] 限制 Actuator 端点暴露（仅 `loggers` 和 `health`）
- [ ] 配置日志文件权限（`chmod 640 logs/code-platform.log`）
- [ ] 关闭 H2 Console（生产环境不使用 h2 profile）
- [ ] 接入鉴权机制（当前版本未接入）

### 10.2 敏感配置管理

生产环境建议使用环境变量管理敏感信息：

```bash
# /etc/profile.d/code-platform.sh
export SPRING_DATASOURCE_URL="jdbc:mysql://prod-db:3306/code_platform"
export SPRING_DATASOURCE_USERNAME="prod_user"
export SPRING_DATASOURCE_PASSWORD="强密码"
export SPRING_PROFILES_ACTIVE="prod"
```

```bash
# 启动时加载
source /etc/profile.d/code-platform.sh
java -jar platform-server-1.0.0.jar
```

---

## 十一、附录

### 11.1 端口清单

| 服务 | 端口 | 说明 |
|------|------|------|
| 后端应用 | 8080 | Spring Boot 服务 |
| 前端开发 | 5173 | Vite 开发服务器 |
| MySQL | 3306 | 数据库 |
| H2 Console | 8080 | H2 控制台（h2 profile，路径 `/h2-console`） |

### 11.2 关键文件路径

| 文件 | 路径 |
|------|------|
| 数据库脚本 | `code/platform-server/src/main/resources/db/init.sql` |
| 主配置 | `code/platform-server/src/main/resources/application.yml` |
| 开发配置 | `code/platform-server/src/main/resources/application-dev.yml` |
| 生产配置 | `code/platform-server/src/main/resources/application-prod.yml` |
| H2 配置 | `code/platform-server/src/main/resources/application-h2.yml` |
| 日志文件 | `logs/code-platform.log`（开发）/ `/var/log/code-platform/app.log`（生产） |
| 日志指南 | `docs/logging-guide.md` |

### 11.3 常用命令速查

```bash
# ===== 后端 =====
mvn clean compile                          # 编译
mvn clean package -DskipTests              # 打包（跳过测试）
mvn spring-boot:run                        # 启动（默认）
mvn spring-boot:run -Dspring-boot.run.profiles=dev   # 启动（开发）
mvn spring-boot:run -Dspring-boot.run.profiles=h2    # 启动（H2）
mvn test                                   # 运行测试
mvn test -Dtest="XxxTest"                  # 运行指定测试

# ===== 前端 =====
cd code/platform-web
npm install                                # 安装依赖
npm run dev                                # 启动开发服务器
npm run build                              # 构建生产包

# ===== 数据库 =====
mysql -h localhost -P 3306 -u quanyu -p123456 code_platform   # 连接数据库
mysql -u quanyu -p123456 < init.sql                          # 执行脚本
mysqldump -u quanyu -p123456 code_platform > backup.sql      # 备份

# ===== 运维 =====
lsof -i:8080                               # 查看端口占用
curl http://localhost:8080/actuator/health # 健康检查
tail -f logs/code-platform.log             # 实时日志
jps -l                                     # 查看 Java 进程
```

### 11.4 联系方式

| 角色 | 职责 |
|------|------|
| 开发负责人 | 代码维护、功能开发 |
| 运维负责人 | 部署、监控、故障处理 |
| DBA | 数据库维护、备份 |

---

**文档版本**：v1.0  
**最后更新**：2026-08-12  
**维护团队**：开发平台团队
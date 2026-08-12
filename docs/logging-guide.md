# 代码平台日志配置与排查指南

## 一、日志架构说明

### 1.1 日志分级策略

| 级别 | 使用场景 | 输出内容示例 |
|------|----------|-------------|
| `ERROR` | 系统异常、数据库连接失败、未捕获异常 | 堆栈信息、错误码、上下文数据 |
| `WARN` | 业务校验失败、数据未找到、关联资源阻塞 | 编码已存在、实体未找到、关联资源统计 |
| `INFO` | 方法入口/出口、关键业务节点 | 创建开始、保存成功、删除完成 |
| `DEBUG` | 中间数据明细、校验过程、数据填充 | 实体字段详情、校验通过、软删除明细 |

### 1.2 日志格式规范

```
[时间戳] [级别] [Logger名] [模块前缀] 业务描述: 参数1={}, 参数2={}
```

**示例**：
```
11:10:09.114 [main] INFO  c.z.a.application.ApplicationAppService -- [应用] 创建开始: name=订单中心, code=ORDER_CENTER, version=1.0.0
11:10:09.115 [main] DEBUG c.z.a.application.ApplicationAppService -- [应用] 准备保存实体: name=订单中心, code=ORDER_CENTER, version=1.0.0, status=1
11:10:09.116 [main] INFO  c.z.a.application.ApplicationAppService -- [应用] 创建成功: id=1, name=订单中心, code=ORDER_CENTER
11:10:09.117 [main] WARN  c.z.a.application.ApplicationAppService -- [应用] 实体未找到或已删除: id=999, isDeleted=0
```

### 1.3 模块前缀对照表

| 前缀 | 模块 | 对应 Service 类 |
|------|------|-----------------|
| `[应用]` | 应用管理 | `ApplicationAppService` |
| `[微服务]` | 微服务管理 | `MicroserviceAppService` |
| `[模型]` | 模型管理 | `ModelAppService` |
| `[元数据]` | 元数据管理 | `MetadataAppService` |
| `[服务]` | 服务管理 | `ServiceAppService` |
| `[编排]` | 服务编排 | `OrchestrationAppService` |

---

## 二、日志配置说明

### 2.1 当前配置

```yaml
# application.yml
logging:
  level:
    cn.zhangquanyu: INFO          # 业务模块默认 INFO
    org.hibernate.SQL: WARN        # JPA SQL 默认 WARN
```

### 2.2 日志级别调整指南

#### 场景一：日常开发与测试

```yaml
logging:
  level:
    cn.zhangquanyu: INFO
    org.hibernate.SQL: DEBUG
```

**说明**：
- 业务模块保持 INFO，查看关键业务流程
- Hibernate SQL 开 DEBUG，查看所有 SQL 语句和参数

#### 场景二：排查业务逻辑问题

```yaml
logging:
  level:
    cn.zhangquanyu: DEBUG
    org.hibernate.SQL: WARN
```

**说明**：
- 业务模块开 DEBUG，查看完整数据链路
- Hibernate SQL 保持 WARN，避免日志过多

#### 场景三：排查特定模块问题

```yaml
logging:
  level:
    cn.zhangquanyu: INFO                    # 其他模块保持 INFO
    cn.zhangquanyu.service.application: DEBUG  # 仅服务管理模块开 DEBUG
```

**说明**：
- 只针对问题模块开 DEBUG，避免日志过多

#### 场景四：生产环境

```yaml
logging:
  level:
    cn.zhangquanyu: INFO
    org.hibernate.SQL: WARN
```

**说明**：
- 生产环境严格控制日志级别
- 只输出关键业务节点和警告

### 2.3 动态调整日志级别（不重启）

使用 Spring Boot Actuator 在线调整：

```bash
# 查看当前日志级别
curl http://localhost:8080/actuator/loggers/cn.zhangquanyu

# 动态设置 DEBUG
curl -X POST http://localhost:8080/actuator/loggers/cn.zhangquanyu \
  -H "Content-Type: application/json" \
  -d '{"configuredLevel": "DEBUG"}'

# 恢复 INFO
curl -X POST http://localhost:8080/actuator/loggers/cn.zhangquanyu \
  -H "Content-Type: application/json" \
  -d '{"configuredLevel": "INFO"}'
```

---

## 三、常见问题排查场景

### 3.1 场景一：创建失败 - 编码已存在

**现象**：前端提示"编码已存在"

**排查步骤**：
```bash
# 过滤相关日志
grep "\[应用\]" app.log | grep "创建失败"
grep "\[微服务\]" app.log | grep "创建失败"
grep "\[模型\]" app.log | grep "创建失败"
grep "\[元数据\]" app.log | grep "创建失败"
grep "\[服务\]" app.log | grep "创建失败"
grep "\[编排\]" app.log | grep "创建失败"
```

**日志特征**：
```
WARN [应用] 创建失败-编码已存在: code=ORDER_CENTER
WARN [微服务] 创建失败-编码已存在: applicationId=1, code=ORDER_SERVICE
```

**排查方向**：
1. 确认编码是否真的重复（软删除的数据也需检查）
2. 确认同一父级下编码是否唯一

### 3.2 场景二：查询详情 - 数据不存在

**现象**：前端提示"数据不存在"

**排查步骤**：
```bash
# 查找实体未找到日志
grep "实体未找到或已删除" app.log
```

**日志特征**：
```
WARN [应用] 实体未找到或已删除: id=999, isDeleted=0
WARN [服务] 实体未找到或已删除: id=300, isDeleted=0
```

**排查方向**：
1. 确认 ID 是否正确
2. 确认数据是否已被软删除（is_deleted=1）
3. 确认数据是否属于当前用户/租户

### 3.3 场景三：删除失败 - 存在关联资源

**现象**：前端提示"存在关联资源，无法删除"

**排查步骤**：
```bash
# 查看关联资源统计
grep "关联资源统计" app.log
```

**日志特征**（需 DEBUG 级别）：
```
DEBUG [应用] 关联资源统计: id=1, microserviceCount=3, metadataCount=1
DEBUG [微服务] 关联资源统计: id=10, modelCount=2, serviceCount=5, orchestrationCount=1
DEBUG [模型] 删除校验: id=100, 被服务参数引用次数=3
```

**排查方向**：
1. 根据统计数字，定位具体关联的资源
2. 确认是否可以级联删除或手动清理
3. 检查删除顺序（先删子资源，再删父资源）

### 3.4 场景四：服务编排 - 流程校验失败

**现象**：编排保存时提示校验错误

**排查步骤**：
```bash
# 查看节点统计日志
grep "节点统计" app.log
```

**日志特征**（需 DEBUG 级别）：
```
DEBUG [编排] 节点统计: id=500, startCount=0, endCount=2, totalNodes=5
```

**常见问题**：
| 错误信息 | 原因 | 解决方案 |
|----------|------|----------|
| 开始节点数量必须为1 | 未设置 START 节点或多个 START | 确保有且仅有一个 `NODE_TYPE=START` 的节点 |
| 结束节点数量至少为1 | 未设置 END 节点 | 添加 `NODE_TYPE=END` 的节点 |
| 连线起点/终点节点不存在 | 连线引用了不存在的节点 | 检查连线的 `FROM_NODE_KEY` 和 `TO_NODE_KEY` |
| 服务调用节点未选择服务 | SERVICE 节点未关联服务 | 设置 SERVICE 节点的 `SERVICE_ID` |
| 引用的服务已停用 | 被引用的服务 status=0 | 启用服务或更换引用 |
| 条件判断节点出边不足 | CONDITION 节点少于2条出边 | 添加至少2条出边 |

### 3.5 场景五：批量保存字段/枚举项异常

**现象**：批量保存时提示校验错误

**排查步骤**：
```bash
# 查看校验日志
grep "校验通过" app.log
grep "校验失败" app.log
```

**日志特征**（需 DEBUG 级别）：
```
DEBUG [模型] 主键校验通过: modelId=100
DEBUG [模型] 字段名唯一性校验通过: modelId=100, fieldCount=5
WARN [模型] 批量保存失败-缺少主键字段: modelId=100
WARN [模型] 批量保存失败-字段名重复: modelId=100, fieldName=name
```

**常见问题**：
| 错误信息 | 原因 | 解决方案 |
|----------|------|----------|
| 模型必须包含至少一个主键字段 | 所有字段的 `isPrimary` 都为 0 | 设置至少一个字段的 `isPrimary=1` |
| 字段名重复 | 同一模型下有重复字段名 | 修改重复字段的 name |
| 枚举字段必须关联元数据 | ENUM 类型字段未设置 metadataId | 设置有效的元数据 ID |
| 关联的元数据不属于当前应用 | 元数据跨应用引用 | 使用当前应用下的元数据 |
| 元数据项编码重复 | 同一元数据下有重复 itemCode | 修改重复项的 itemCode |

### 3.6 场景六：服务参数保存异常

**现象**：服务创建/更新时参数保存失败

**排查步骤**：
```bash
# 查看参数保存日志
grep "开始保存参数" app.log
grep "名重复" app.log
```

**日志特征**：
```
DEBUG [服务] 开始保存参数: serviceId=300, inputCount=2, outputCount=1
WARN [服务] 创建失败-编码已存在: msId=10, code=CREATE_ORDER
WARN [服务] 创建失败-路径已存在: msId=10, path=/api/order/create
```

**排查方向**：
1. 检查输入/输出参数名是否重复
2. 检查服务编码在微服务下是否唯一
3. 检查服务路径在微服务下是否唯一

---

## 四、数据链路追踪

### 4.1 创建流程日志链路

```
[模块] 创建开始: name=xxx, code=xxx
  → [DEBUG] 校验通过: xxxId=xxx
  → [DEBUG] 准备保存实体: name=xxx, code=xxx, status=1
  → [INFO] xxx主体创建成功: id=xxx
  → [DEBUG] 开始保存参数/字段: xxxId=xxx, count=N
  → [INFO] 创建完成(含子资源): id=xxx
```

### 4.2 更新流程日志链路

```
[模块] 更新开始: id=xxx, name=xxx
  → [DEBUG] 更新前数据: id=xxx, name=xxx, description=xxx
  → [DEBUG] 软删除子资源: xxxId=xxx
  → [DEBUG] 新增/更新子资源: count=N
  → [INFO] 更新完成(含子资源重写): id=xxx
```

### 4.3 删除流程日志链路

```
[模块] 删除开始: id=xxx
  → [DEBUG] 关联资源统计: id=xxx, count=N
  → [DEBUG] 子资源软删除完成: xxxId=xxx
  → [INFO] 删除成功(软删除): id=xxx
```

### 4.4 查询流程日志链路

```
[模块] 查询详情开始: id=xxx
  → [DEBUG] 实体查询成功: id=xxx, name=xxx, code=xxx
  → [DEBUG] 填充关联数据: xxxId=xxx
  → [INFO] 查询详情成功: id=xxx, name=xxx, code=xxx, fieldCount=N
```

---

## 五、常用排查命令速查

### 5.1 按模块过滤日志

```bash
# 应用模块
grep "\[应用\]" app.log

# 微服务模块
grep "\[微服务\]" app.log

# 模型模块
grep "\[模型\]" app.log

# 元数据模块
grep "\[元数据\]" app.log

# 服务模块
grep "\[服务\]" app.log

# 编排模块
grep "\[编排\]" app.log
```

### 5.2 按操作类型过滤

```bash
# 查看所有创建操作
grep "创建开始" app.log

# 查看所有删除操作
grep "删除开始" app.log

# 查看所有更新操作
grep "更新开始" app.log

# 查看所有警告信息
grep "WARN" app.log

# 查看实体未找到
grep "实体未找到" app.log
```

### 5.3 按 ID 过滤

```bash
# 追踪特定 ID 的完整日志链路
grep "id=500" app.log
```

### 5.4 实时查看日志

```bash
# 实时查看并过滤编排模块
tail -f app.log | grep "\[编排\]"

# 实时查看错误日志
tail -f app.log | grep -E "ERROR|WARN"
```

---

## 六、日志配置最佳实践

### 6.1 生产环境配置

```yaml
# 推荐生产环境配置
logging:
  level:
    cn.zhangquanyu: INFO
    org.hibernate.SQL: WARN
    org.springframework: WARN
```

**说明**：
- 业务模块 INFO：只记录关键业务节点
- Hibernate SQL WARN：不输出 SQL，避免日志膨胀
- Spring WARN：不输出框架层面的 INFO 日志

### 6.2 开发环境配置

```yaml
# 推荐开发环境配置
logging:
  level:
    cn.zhangquanyu: DEBUG
    org.hibernate.SQL: DEBUG
    org.springframework: INFO
```

**说明**：
- 业务模块 DEBUG：查看完整数据链路
- Hibernate SQL DEBUG：查看 SQL 语句和参数
- Spring INFO：查看框架启动信息

### 6.3 临时排查配置

```yaml
# 临时排查特定模块
logging:
  level:
    cn.zhangquanyu: INFO
    cn.zhangquanyu.service.application: DEBUG
    org.hibernate.SQL: DEBUG
```

**说明**：
- 其他模块保持 INFO
- 仅问题模块开 DEBUG
- 同时开 SQL 日志，方便排查数据问题

### 6.4 日志文件滚动配置

```yaml
# 建议添加日志文件配置
logging:
  file:
    name: logs/code-platform.log
  logback:
    rollingpolicy:
      max-file-size: 100MB
      max-history: 30
      total-size-cap: 5GB
```

---

## 七、问题排查 Checklist

当遇到数据问题时，按以下顺序排查：

- [ ] **确认问题模块**：使用模块前缀 `[应用]`、`[服务]` 等过滤日志
- [ ] **确认操作类型**：搜索 `创建开始`、`更新开始`、`删除开始`、`查询详情`
- [ ] **检查 WARN 日志**：搜索 `WARN`，查看是否有业务校验失败
- [ ] **检查实体是否存在**：搜索 `实体未找到或已删除`
- [ ] **检查关联资源**：开启 DEBUG 后查看 `关联资源统计` 日志
- [ ] **检查校验过程**：开启 DEBUG 后查看 `校验通过`、`节点统计` 日志
- [ ] **检查 SQL 执行**：开启 `org.hibernate.SQL: DEBUG` 查看 SQL 语句
- [ ] **检查事务回滚**：搜索 `rollback` 或 `TransactionSystemException`

---

## 附录：日志配置速查表

| 场景 | 业务模块 | Hibernate SQL | 备注 |
|------|----------|---------------|------|
| 日常开发 | INFO | DEBUG | 查看 SQL |
| 功能测试 | INFO | WARN | 聚焦业务 |
| 排查问题 | DEBUG | DEBUG | 完整链路 |
| 生产环境 | INFO | WARN | 控制日志量 |
| 临时排查 | DEBUG(模块) | DEBUG | 定点分析 |

**文档维护者**：技术平台团队  
**最后更新**：2026-08-12
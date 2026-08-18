# 4p12s-deployment-execution 技能

## 基本信息

- **技能名称**: 4p12s-deployment-execution
- **技能定位**: 发布触发
- **版本**: 1.0.0
- **文件路径**: `docs/skills/12-deployment-execution.md`

---

## 一、概述

在 Git 推送成功后触发发布 API 或发布脚本，并将返回状态写入 `deploy-log.md##Release Trigger`。

---

## 二、前置依赖

`deploy-log.md` 中的 `## Git Push` 章节（存在多条记录时以最新一条为准）

---

## 三、输出产物

追加写入 `deploy-log.md` 的 Release Trigger 部分

---

## 四、发布入口发现（优先级顺序）

1. 项目根目录 `scripts/deploy.sh`
2. 项目入口文档指定的发布脚本、命令、CI/CD 平台或发布说明
3. `Makefile`、CI 配置或仓库脚本中明确命名的发布命令
4. 用户在当前交付中显式指定的发布方式
5. 如果无法确定发布入口、发布环境、目标分支或凭证来源，返回 `blocked`，不得猜测

---

## 五、Java 项目部署脚本示例

### 5.1 scripts/deploy.sh 示例

```bash
#!/bin/bash

# Java/Maven 项目部署脚本示例

# 环境变量检查
if [ -z "$DEPLOY_ENV" ]; then
  echo "DEPLOY_ENV not set"
  exit 1
fi

if [ -z "$DEPLOY_TARGET" ]; then
  echo "DEPLOY_TARGET not set"
  exit 1
fi

# Maven 打包
echo "Building with Maven..."
mvn clean package -DskipTests -Pprod

# 部署 JAR
echo "Deploying to $DEPLOY_TARGET..."
JAR_FILE=$(ls target/*.jar | head -1)
scp $JAR_FILE $DEPLOY_TARGET:/app/

# 重启服务
ssh $DEPLOY_TARGET "systemctl restart myapp"

echo "Deployment completed."
```

### 5.2 Maven 部署命令示例

```bash
# 使用 Maven Deploy 插件
mvn deploy -DskipTests -Prelease

# 或使用 Docker 部署
mvn clean package -DskipTests
docker build -t myapp:latest .
docker push myregistry/myapp:latest
```

---

## 六、凭证规则（安全约束）

- 不得在 Skill 指令、交付文档或代码中写入 CI/CD webhook URL、api-token、账号密码或长期凭证
- 发布凭证只能来自项目认可的位置：
  - 环境变量（`DEPLOY_TOKEN`、`CI_API_TOKEN`）
  - 凭证管理器（Vault、AWS Secrets Manager）
  - CI/CD 平台配置（GitLab CI Variables、GitHub Secrets）
  - 项目入口文档指定的凭证位置
- 如果发布命令需要凭证但当前环境不可用，返回 `blocked`

---

## 七、执行流程

1. 读取 `delivery-state.md`、`deploy-log.md`、项目入口文档和发布说明
2. 从最新一条 Git Push 记录中提取提交哈希、目标分支和远端信息
3. 发现发布入口，优先使用 `scripts/deploy.sh`
4. 执行发布命令或调用发布 API
5. 记录命令、环境、提交哈希、目标分支、HTTP/进程状态和响应摘要
6. 向 `deploy-log.md##Release Trigger` 追加本次记录
7. 返回 `STEP_RESULT`

---

## 八、deploy-log.md##Release Trigger 写入规则

### 写入规则（追加，不覆盖）

- 没有 `## Release Trigger` 章节：在文件末尾追加该章节，再追加本次记录
- 已有 `## Release Trigger` 章节：完整读取文件现有内容，把本次记录追加到该章节末尾
- 每次执行本 Skill 只追加一条新记录；**严禁删除、覆盖或重排历史 Release Trigger 记录**
- 记录编号自增：`record_no` = 现有 Release Trigger 记录条数 + 1，首条为 1

### 记录模板

```markdown
### Release Trigger #<record_no>

- record_no:
- trigger_time:
- commit_hash:
- target_branch:
- environment:
- deploy_entry:
- deploy_command:
- exit_code:
- credential_source:
- response_summary:
- result:
```

---

## 九、结论枚举（固定值，不得发明新状态）

| 结论 | 含义 |
|------|------|
| `请求已受理` | 发布 API 返回成功，发布流程已触发 |
| `请求失败` | 发布 API 返回错误或部署脚本执行失败 |
| `凭证缺失` | 发布所需凭证不可用 |
| `入口缺失` | 无法确定发布入口 |
| `结果无法判断` | 发布命令执行但结果不明确 |

---

## 十、边界（禁止事项）

- 不决定 workflow terminal state
- 不改写 `deploy-log.md##Git Push`
- 不覆盖、删除或改写 `deploy-log.md` 中的历史记录，仅追加本次记录
- 不创建、打开、准备或等待 Pull Request / Merge Request
- Pull Request / Merge Request 创建、MR ready、等待合并都不是发布触发
- 不得在文档或代码中写入敏感凭证

---

## 十一、最佳实践

### 11.1 发布入口选择

```
项目根目录是否有 scripts/deploy.sh？
├── 是 → 使用 scripts/deploy.sh
└── 否 → 项目入口文档是否指定发布方式？
    ├── 是 → 使用指定方式
    └── 否 → Makefile 或 CI 配置是否有发布命令？
        ├── 是 → 使用该命令
        └── 否 → 提示用户指定发布方式，否则返回 blocked
```

### 11.2 凭证管理

- 使用环境变量传递凭证
- 在 CI/CD 平台配置 Secrets
- 使用 Vault 或 AWS Secrets Manager 管理长期凭证
- 不要在代码或文档中硬编码凭证
- 不要在日志中输出凭证值

### 11.3 Maven 项目部署建议

- 使用 `mvn clean package -DskipTests` 打包
- 使用 Spring Boot 的 profile 区分环境（dev/test/prod）
- 使用 Docker 容器化部署
- 记录部署版本号和提交哈希的对应关系

---

**文档结束**

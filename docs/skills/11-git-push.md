# 4p12s-git-push 技能

## 基本信息

- **技能名称**: 4p12s-git-push
- **技能定位**: Git 推送
- **版本**: 1.0.0
- **文件路径**: `docs/skills/11-git-push.md`

---

## 一、概述

执行交付流程的 Git 提交和推送步骤，验证通过后仅暂存交付相关文件、提交、推送、验证远端 HEAD。

---

## 二、前置依赖

`verification-result.md`（必须包含 `## E2E 验证结果` 且结论为通过）

---

## 三、前置检查（强约束）

- `verification-result.md` 缺失、为空、没有 `## E2E 验证结果`、E2E 验证结论不是通过、列出了 Git 阻断项 → 返回 `blocked`
- `delivery-state.md` 中的 `verification_e2e` Step 不是 `passed`（编排模式下）→ 返回 `blocked`

---

## 四、提交信息规范

### 4.1 commit-msg 格式

```
${commit}:${taskId}_${name}:${submitDescription}
```

> 所有冒号统一为**英文半角冒号 `:`**，消除中文全角冒号 `：` 导致的混淆和 husky hook 校验失败问题。

### 4.2 字段说明

| 字段 | 说明 | 取值约束 |
|------|------|----------|
| `commit` | 提交类型 | `Feat`（需求）/ `Task`（任务）/ `BugFix`（缺陷） |
| `taskId` | 任务/需求 ID，不可含下划线 `_` | 如 `33413` |
| `name` | 任务/需求名称，可含中文 | 如 `新增用户管理功能` |
| `submitDescription` | 本次提交描述，不可为空，从 `git diff --stat` 总结 | 如 `add user management module` |

### 4.3 示例

```
Feat:33413_新增用户管理功能:add user management module
Task:33414_代码审查优化:optimize review logic
BugFix:33415_修复空指针:resolve null pointer exception
```

---

## 五、任务信息提取流程

### 5.1 搜索上下文中的任务信息

1. 当前会话上下文（系统提示词注入的 XML）：`<work_task_context source="selected_work_task">`
2. 编排模式的 `tasks/index.md`：已通过的 Task 记录
3. `delivery-state.md` 中的任务关联字段

### 5.2 提取字段映射

| 提取字段 | 映射到 commit-msg |
|----------|-------------------|
| `<task_type>` | `commit`（`DEFECT`→`BugFix`；`BUSINESS_REQUIREMENT`→`Feat`；`TECHNICAL_REQUIREMENT`→`Task`） |
| `<task_id>` | `taskId` |
| `<title>`（仅去掉 `【...】` 前缀标签） | `name` |
| `git diff --stat` + 暂存文件的变更摘要 | `submitDescription` |

### 5.3 任务信息缺失时的处理

- 提示用户填写任务信息
- 用户选择"强制跳过"时使用默认值：`Task:0000_untitled:update files per git diff stat`

---

## 六、范围选择（文件暂存规则）

- 从交付产物、当前交付目录、已通过 Task 记录、`verification-result.md`、`git status` 确定本次交付的文件集
- 仅暂存属于本次交付的明确路径，保留无关变更
- 如果交付文件集无法与无关变更分离，返回 `blocked`

---

## 七、执行流程

1. 运行 `git status` 和聚焦的 `git diff`
2. 确定本次交付的明确文件列表
3. 仅暂存这些明确路径
4. **必须**提取任务信息
5. 按 commit-msg 格式模板直接填充组装
6. 执行 `git commit -m "<组装好的 commit-msg>"`
7. 推送当前分支
8. 验证远端分支 HEAD 等于本地提交哈希
9. 向 `deploy-log.md` 追加一条新的 Git Push 记录

---

## 八、deploy-log.md##Git Push 写入规则

### 写入规则（追加，不覆盖）

- 不存在：创建文件，写入标题和章节，再追加本次记录
- 存在但没有 `## Git Push` 章节：在文件末尾追加该章节，再追加本次记录
- 已有 `## Git Push` 章节：完整读取文件现有内容，把本次记录追加到该章节末尾
- 每次执行本 Skill 只追加一条新记录；**严禁删除、覆盖或重排历史 Git Push 记录**
- 记录编号自增：`record_no` = 现有 Git Push 记录条数 + 1，首条为 1

### 记录模板

```markdown
### Git Push #<record_no>

- record_no:
- push_time:              # ISO8601 时间戳
- commit_type:            # Feat | Task | BugFix
- task_id:
- task_name:
- commit_hash:
- branch:
- remote:
- staged_files:
- commit_message:         # 完整 commit-msg
- commit_command:
- commit_exit_code:
- push_command:
- push_exit_code:
- remote_head_check_command:
- remote_head:
- result:
```

---

## 九、边界（禁止事项）

- 不触发发布或部署 API
- 不写入 `deploy-log.md##Release Trigger`
- 不覆盖、删除或改写 `deploy-log.md` 中的历史记录与其他章节，仅追加本次记录
- 不创建或切换分支
- 不创建、打开、准备或等待 Pull Request / Merge Request
- 不使用中文全角冒号 `：`

---

## 十、最佳实践

### 10.1 commit-msg 组装检查清单

- 所有冒号均为英文半角 `:`
- taskId 不含下划线 `_`
- name 字段无全角空格或零宽字符
- submitDescription 从 `git diff --stat` 总结，不照搬任务描述
- submitDescription 不为空

### 10.2 暂存文件最佳实践

- 使用 `git add <具体文件路径>` 而非 `git add .`
- 暂存前确认文件列表与交付范围一致
- 保留无关变更（如本地配置文件修改）

---

**文档结束**

-- 编排表增加事务配置
ALTER TABLE dev_orchestration
  ADD COLUMN tx_type VARCHAR(20) DEFAULT 'LOCAL' COMMENT '事务类型: LOCAL-本地事务, DISTRIBUTED-分布式事务' AFTER status,
  ADD COLUMN tx_timeout INT DEFAULT 300 COMMENT '事务超时(秒)' AFTER tx_type;

-- 编排节点表增加事务与异常配置
ALTER TABLE dev_orch_node
  ADD COLUMN tx_type VARCHAR(20) DEFAULT 'LOCAL' COMMENT '节点事务类型: LOCAL/DISTRIBUTED/NONE' AFTER config_json,
  ADD COLUMN tx_timeout INT DEFAULT 60 COMMENT '节点事务超时(秒)' AFTER tx_type,
  ADD COLUMN retry_count INT DEFAULT 0 COMMENT '重试次数' AFTER tx_timeout,
  ADD COLUMN retry_interval INT DEFAULT 1000 COMMENT '重试间隔(ms)' AFTER retry_count,
  ADD COLUMN exception_strategy VARCHAR(20) DEFAULT 'INTERRUPT' COMMENT '异常策略: INTERRUPT-中断, CONTINUE-继续, IGNORE-忽略' AFTER retry_interval,
  ADD COLUMN loop_type VARCHAR(20) DEFAULT 'SERIAL' COMMENT '循环类型: SERIAL-串行, PARALLEL-并行 (LOOP节点)' AFTER exception_strategy,
  ADD COLUMN branch_expr VARCHAR(1024) DEFAULT NULL COMMENT '分支表达式 (BRANCH节点)' AFTER loop_type;
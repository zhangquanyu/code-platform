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

-- 12. 编排参数表（编排级入参/出参定义）
CREATE TABLE IF NOT EXISTS `dev_orch_param` (
  `id` BIGINT NOT NULL,
  `orchestration_id` BIGINT NOT NULL COMMENT '所属编排',
  `param_scope` VARCHAR(16) NOT NULL COMMENT 'INPUT-编排入参, OUTPUT-编排出参',
  `param_name` VARCHAR(64) NOT NULL,
  `data_type` VARCHAR(32) NOT NULL,
  `is_required` TINYINT NOT NULL DEFAULT 1,
  `param_comment` VARCHAR(512) DEFAULT NULL,
  `source_node_key` VARCHAR(64) DEFAULT NULL COMMENT '出参来源节点Key(仅OUTPUT)',
  `source_field` VARCHAR(128) DEFAULT NULL COMMENT '出参来源字段(仅OUTPUT)',
  `sort_order` INT NOT NULL DEFAULT 0,
  `is_deleted` TINYINT NOT NULL DEFAULT 0,
  `create_by` BIGINT DEFAULT NULL,
  `update_by` BIGINT DEFAULT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_dev_op_orch_scope_name` (`orchestration_id`, `param_scope`, `param_name`, `is_deleted`),
  KEY `idx_dev_op_orch_id` (`orchestration_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='编排参数表';

-- 13. 补充 dev_orch_param 表缺失的审计列（如果表已存在但缺少 create_by/update_by）
-- MySQL 8.0 不支持 ADD COLUMN IF NOT EXISTS，如果列已存在会报错，忽略即可
ALTER TABLE `dev_orch_param` ADD COLUMN `create_by` BIGINT DEFAULT NULL AFTER `is_deleted`;
ALTER TABLE `dev_orch_param` ADD COLUMN `update_by` BIGINT DEFAULT NULL AFTER `create_by`;
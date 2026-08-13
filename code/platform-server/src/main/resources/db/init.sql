-- 开发平台数据库初始化脚本
-- 数据库：code_platform  字符集：utf8mb4

CREATE DATABASE IF NOT EXISTS `code_platform`
  DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `code_platform`;

-- 1. 应用表
CREATE TABLE `dev_application` (
  `id` BIGINT NOT NULL COMMENT '主键',
  `name` VARCHAR(128) NOT NULL COMMENT '应用名称',
  `code` VARCHAR(64) NOT NULL COMMENT '应用编码',
  `description` VARCHAR(512) DEFAULT NULL COMMENT '描述',
  `version` VARCHAR(32) DEFAULT '1.0.0' COMMENT '版本',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态 1启用 0停用',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '软删除',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `create_by` BIGINT DEFAULT NULL,
  `update_by` BIGINT DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_dev_application_code` (`code`, `is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='应用表';

-- 2. 微服务表
CREATE TABLE `dev_microservice` (
  `id` BIGINT NOT NULL,
  `application_id` BIGINT NOT NULL COMMENT '所属应用',
  `name` VARCHAR(128) NOT NULL,
  `code` VARCHAR(64) NOT NULL,
  `description` VARCHAR(512) DEFAULT NULL,
  `version` VARCHAR(32) DEFAULT '1.0.0',
  `status` TINYINT NOT NULL DEFAULT 1,
  `is_deleted` TINYINT NOT NULL DEFAULT 0,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `create_by` BIGINT DEFAULT NULL,
  `update_by` BIGINT DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_dev_ms_app_code` (`application_id`, `code`, `is_deleted`),
  KEY `idx_dev_ms_application_id` (`application_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='微服务表';

-- 3. 模型表
CREATE TABLE `dev_model` (
  `id` BIGINT NOT NULL,
  `microservice_id` BIGINT NOT NULL COMMENT '所属微服务',
  `name` VARCHAR(128) NOT NULL,
  `code` VARCHAR(64) NOT NULL,
  `description` VARCHAR(512) DEFAULT NULL,
  `is_deleted` TINYINT NOT NULL DEFAULT 0,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `create_by` BIGINT DEFAULT NULL,
  `update_by` BIGINT DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_dev_model_ms_code` (`microservice_id`, `code`, `is_deleted`),
  KEY `idx_dev_model_microservice_id` (`microservice_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='模型表';

-- 4. 模型字段表
CREATE TABLE `dev_model_field` (
  `id` BIGINT NOT NULL,
  `model_id` BIGINT NOT NULL COMMENT '所属模型',
  `name` VARCHAR(64) NOT NULL COMMENT '字段名',
  `display_name` VARCHAR(128) NOT NULL COMMENT '显示名',
  `field_type` VARCHAR(32) NOT NULL COMMENT '数据类型',
  `length` INT DEFAULT NULL,
  `precision` INT DEFAULT NULL,
  `is_required` TINYINT NOT NULL DEFAULT 0,
  `is_primary` TINYINT NOT NULL DEFAULT 0,
  `is_unique` TINYINT NOT NULL DEFAULT 0,
  `is_index` TINYINT NOT NULL DEFAULT 0,
  `default_value` VARCHAR(256) DEFAULT NULL,
  `metadata_id` BIGINT DEFAULT NULL COMMENT '关联元数据(枚举类型)',
  `sort_order` INT NOT NULL DEFAULT 0,
  `field_comment` VARCHAR(512) DEFAULT NULL,
  `is_deleted` TINYINT NOT NULL DEFAULT 0,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `create_by` BIGINT DEFAULT NULL,
  `update_by` BIGINT DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_dev_mf_model_name` (`model_id`, `name`, `is_deleted`),
  KEY `idx_dev_mf_model_id` (`model_id`),
  KEY `idx_dev_mf_metadata_id` (`metadata_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='模型字段表';

-- 5. 元数据表
CREATE TABLE `dev_metadata` (
  `id` BIGINT NOT NULL,
  `application_id` BIGINT NOT NULL COMMENT '所属应用',
  `name` VARCHAR(128) NOT NULL,
  `code` VARCHAR(64) NOT NULL,
  `description` VARCHAR(512) DEFAULT NULL,
  `status` TINYINT NOT NULL DEFAULT 1,
  `is_deleted` TINYINT NOT NULL DEFAULT 0,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `create_by` BIGINT DEFAULT NULL,
  `update_by` BIGINT DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_dev_meta_app_code` (`application_id`, `code`, `is_deleted`),
  KEY `idx_dev_meta_application_id` (`application_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='元数据表';

-- 6. 元数据项表
CREATE TABLE `dev_metadata_item` (
  `id` BIGINT NOT NULL,
  `metadata_id` BIGINT NOT NULL COMMENT '所属元数据',
  `item_code` VARCHAR(64) NOT NULL,
  `item_name` VARCHAR(128) NOT NULL,
  `item_value` VARCHAR(256) DEFAULT NULL,
  `sort_order` INT NOT NULL DEFAULT 0,
  `status` TINYINT NOT NULL DEFAULT 1,
  `is_deleted` TINYINT NOT NULL DEFAULT 0,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `create_by` BIGINT DEFAULT NULL,
  `update_by` BIGINT DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_dev_mi_meta_code` (`metadata_id`, `item_code`, `is_deleted`),
  KEY `idx_dev_mi_metadata_id` (`metadata_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='元数据项表';

-- 7. 服务定义表
CREATE TABLE `dev_service` (
  `id` BIGINT NOT NULL,
  `microservice_id` BIGINT NOT NULL COMMENT '所属微服务',
  `name` VARCHAR(128) NOT NULL,
  `code` VARCHAR(64) NOT NULL,
  `description` VARCHAR(1024) DEFAULT NULL,
  `http_method` VARCHAR(16) NOT NULL DEFAULT 'POST',
  `service_path` VARCHAR(256) NOT NULL,
  `category` VARCHAR(64) DEFAULT NULL,
  `status` TINYINT NOT NULL DEFAULT 1,
  `is_deleted` TINYINT NOT NULL DEFAULT 0,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `create_by` BIGINT DEFAULT NULL,
  `update_by` BIGINT DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_dev_svc_ms_code` (`microservice_id`, `code`, `is_deleted`),
  UNIQUE KEY `uk_dev_svc_ms_path` (`microservice_id`, `service_path`, `is_deleted`),
  KEY `idx_dev_svc_microservice_id` (`microservice_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='服务定义表';

-- 8. 服务参数表
CREATE TABLE `dev_service_param` (
  `id` BIGINT NOT NULL,
  `service_id` BIGINT NOT NULL COMMENT '所属服务',
  `param_type` TINYINT NOT NULL COMMENT '1=入参 2=出参',
  `param_name` VARCHAR(64) NOT NULL,
  `data_type` VARCHAR(32) NOT NULL,
  `is_required` TINYINT NOT NULL DEFAULT 1,
  `default_value` VARCHAR(256) DEFAULT NULL,
  `model_field_id` BIGINT DEFAULT NULL COMMENT '关联模型字段',
  `sort_order` INT NOT NULL DEFAULT 0,
  `param_comment` VARCHAR(512) DEFAULT NULL,
  `is_deleted` TINYINT NOT NULL DEFAULT 0,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `create_by` BIGINT DEFAULT NULL,
  `update_by` BIGINT DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_dev_sp_svc_type_name` (`service_id`, `param_type`, `param_name`, `is_deleted`),
  KEY `idx_dev_sp_service_id` (`service_id`),
  KEY `idx_dev_sp_field_id` (`model_field_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='服务参数表';

-- 9. 服务编排表
CREATE TABLE `dev_orchestration` (
  `id` BIGINT NOT NULL,
  `microservice_id` BIGINT NOT NULL COMMENT '所属微服务',
  `name` VARCHAR(128) NOT NULL,
  `code` VARCHAR(64) NOT NULL,
  `description` VARCHAR(1024) DEFAULT NULL,
  `status` TINYINT NOT NULL DEFAULT 1,
  `is_deleted` TINYINT NOT NULL DEFAULT 0,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `create_by` BIGINT DEFAULT NULL,
  `update_by` BIGINT DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_dev_orch_ms_code` (`microservice_id`, `code`, `is_deleted`),
  KEY `idx_dev_orch_microservice_id` (`microservice_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='服务编排表';

-- 10. 编排节点表
CREATE TABLE `dev_orch_node` (
  `id` BIGINT NOT NULL,
  `orchestration_id` BIGINT NOT NULL COMMENT '所属编排',
  `node_key` VARCHAR(64) NOT NULL,
  `node_type` VARCHAR(32) NOT NULL COMMENT 'START/SERVICE/CONDITION/LOOP/END',
  `node_name` VARCHAR(128) DEFAULT NULL,
  `service_id` BIGINT DEFAULT NULL COMMENT '被调用服务(SERVICE节点)',
  `config_json` LONGTEXT,
  `x_pos` INT DEFAULT NULL,
  `y_pos` INT DEFAULT NULL,
  `sort_order` INT NOT NULL DEFAULT 0,
  `is_deleted` TINYINT NOT NULL DEFAULT 0,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_dev_on_orch_key` (`orchestration_id`, `node_key`, `is_deleted`),
  KEY `idx_dev_on_orch_id` (`orchestration_id`),
  KEY `idx_dev_on_service_id` (`service_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='编排节点表';

-- 11. 编排连线表
CREATE TABLE `dev_orch_edge` (
  `id` BIGINT NOT NULL,
  `orchestration_id` BIGINT NOT NULL COMMENT '所属编排',
  `edge_key` VARCHAR(64) NOT NULL,
  `from_node_key` VARCHAR(64) NOT NULL,
  `to_node_key` VARCHAR(64) NOT NULL,
  `condition_expr` VARCHAR(1024) DEFAULT NULL,
  `label_text` VARCHAR(256) DEFAULT NULL,
  `is_deleted` TINYINT NOT NULL DEFAULT 0,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_dev_oe_orch_edgekey` (`orchestration_id`, `edge_key`, `is_deleted`),
  KEY `idx_dev_oe_orch_id` (`orchestration_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='编排连线表';

-- 12. 编排参数表（编排级入参/出参定义）
CREATE TABLE `dev_orch_param` (
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

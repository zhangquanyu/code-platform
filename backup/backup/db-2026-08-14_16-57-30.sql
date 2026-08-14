-- 数据库导出脚本
-- 数据库：code_platform
-- 导出时间：2026-08-14 16:57:30
-- 导出工具：Python + PyMySQL

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

USE `code_platform`;

-- ----------------------------
-- 表结构：dev_application
-- ----------------------------
DROP TABLE IF EXISTS `dev_application`;
CREATE TABLE `dev_application` (
  `id` bigint NOT NULL COMMENT '主键',
  `name` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '应用名称',
  `code` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '应用编码',
  `description` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '描述',
  `version` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT '1.0.0' COMMENT '版本',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态 1启用 0停用',
  `is_deleted` tinyint NOT NULL DEFAULT '0' COMMENT '软删除',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `create_by` bigint DEFAULT NULL,
  `update_by` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_dev_application_code` (`code`,`is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='应用表';

-- ----------------------------
-- 表数据：dev_application (共 1 条)
-- ----------------------------
INSERT INTO `dev_application` (`id,`name,`code,`description,`version,`status,`is_deleted,`create_time,`update_time,`create_by,`update_by`) VALUES (8661365572000009, '积分账务', 'PA', '影响积分变动的事件、积分账户、积分批次的管理', '1.0.0', 1, 0, '2026-08-13 17:34:16', '2026-08-13 17:34:16', 1, 1);

-- ----------------------------
-- 表结构：dev_metadata
-- ----------------------------
DROP TABLE IF EXISTS `dev_metadata`;
CREATE TABLE `dev_metadata` (
  `id` bigint NOT NULL,
  `application_id` bigint NOT NULL COMMENT '所属应用',
  `name` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL,
  `code` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `status` tinyint NOT NULL DEFAULT '1',
  `is_deleted` tinyint NOT NULL DEFAULT '0',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `create_by` bigint DEFAULT NULL,
  `update_by` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_dev_meta_app_code` (`application_id`,`code`,`is_deleted`),
  KEY `idx_dev_meta_application_id` (`application_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='元数据表';

-- ----------------------------
-- 表数据：dev_metadata (共 1 条)
-- ----------------------------
INSERT INTO `dev_metadata` (`id,`application_id,`name,`code,`description,`status,`is_deleted,`create_time,`update_time,`create_by,`update_by`) VALUES (8669691024800001, 8661365572000009, '事件状态', 'event_status', '', 1, 0, '2026-08-14 16:41:50', '2026-08-14 16:41:50', 1, 1);

-- ----------------------------
-- 表结构：dev_metadata_item
-- ----------------------------
DROP TABLE IF EXISTS `dev_metadata_item`;
CREATE TABLE `dev_metadata_item` (
  `id` bigint NOT NULL,
  `metadata_id` bigint NOT NULL COMMENT '所属元数据',
  `item_code` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `item_name` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL,
  `item_value` varchar(256) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `sort_order` int NOT NULL DEFAULT '0',
  `status` tinyint NOT NULL DEFAULT '1',
  `is_deleted` tinyint NOT NULL DEFAULT '0',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `create_by` bigint DEFAULT NULL,
  `update_by` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_dev_mi_meta_code` (`metadata_id`,`item_code`,`is_deleted`),
  KEY `idx_dev_mi_metadata_id` (`metadata_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='元数据项表';

-- ----------------------------
-- 表结构：dev_microservice
-- ----------------------------
DROP TABLE IF EXISTS `dev_microservice`;
CREATE TABLE `dev_microservice` (
  `id` bigint NOT NULL,
  `application_id` bigint NOT NULL COMMENT '所属应用',
  `name` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL,
  `code` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `version` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT '1.0.0',
  `status` tinyint NOT NULL DEFAULT '1',
  `is_deleted` tinyint NOT NULL DEFAULT '0',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `create_by` bigint DEFAULT NULL,
  `update_by` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_dev_ms_app_code` (`application_id`,`code`,`is_deleted`),
  KEY `idx_dev_ms_application_id` (`application_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='微服务表';

-- ----------------------------
-- 表数据：dev_microservice (共 5 条)
-- ----------------------------
INSERT INTO `dev_microservice` (`id,`application_id,`name,`code,`description,`version,`status,`is_deleted,`create_time,`update_time,`create_by,`update_by`) VALUES (8661390544500010, 8661365572000009, '事件管理', 'PA_EVENT', '事件、产品、交易的管理', '1.0.0', 1, 0, '2026-08-13 17:38:25', '2026-08-13 17:43:58', 1, 1);
INSERT INTO `dev_microservice` (`id,`application_id,`name,`code,`description,`version,`status,`is_deleted,`create_time,`update_time,`create_by,`update_by`) VALUES (8661403944300011, 8661365572000009, '账户管理', 'PA_ACCOUNT', '账户的创建、更新、查询等管理功能', '1.0.0', 1, 0, '2026-08-13 17:40:39', '2026-08-13 17:48:46', 1, 1);
INSERT INTO `dev_microservice` (`id,`application_id,`name,`code,`description,`version,`status,`is_deleted,`create_time,`update_time,`create_by,`update_by`) VALUES (8661420445700012, 8661365572000009, '账户余额', 'PA_BALANCE', '账户的余额管理，冻结、解冻、变动流水、统计额更新等', '1.0.0', 1, 0, '2026-08-13 17:43:24', '2026-08-13 17:43:24', 1, 1);
INSERT INTO `dev_microservice` (`id,`application_id,`name,`code,`description,`version,`status,`is_deleted,`create_time,`update_time,`create_by,`update_by`) VALUES (8661435276200013, 8661365572000009, '批次管理', 'PA_BATCH', '批次的创建、余额更新、余额冻结、余额解冻、变动流水等', '1.0.0', 1, 0, '2026-08-13 17:45:53', '2026-08-13 17:48:31', 1, 1);
INSERT INTO `dev_microservice` (`id,`application_id,`name,`code,`description,`version,`status,`is_deleted,`create_time,`update_time,`create_by,`update_by`) VALUES (8661474827700014, 8661365572000009, '系统管理', 'PA_SYS', '品牌、积分、码表、字典、定时任务等功能的管理', '1.0.0', 1, 0, '2026-08-13 17:52:28', '2026-08-13 17:52:28', 1, 1);

-- ----------------------------
-- 表结构：dev_model
-- ----------------------------
DROP TABLE IF EXISTS `dev_model`;
CREATE TABLE `dev_model` (
  `id` bigint NOT NULL,
  `microservice_id` bigint NOT NULL COMMENT '所属微服务',
  `name` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL,
  `code` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `is_deleted` tinyint NOT NULL DEFAULT '0',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `create_by` bigint DEFAULT NULL,
  `update_by` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_dev_model_ms_code` (`microservice_id`,`code`,`is_deleted`),
  KEY `idx_dev_model_microservice_id` (`microservice_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='模型表';

-- ----------------------------
-- 表数据：dev_model (共 5 条)
-- ----------------------------
INSERT INTO `dev_model` (`id,`microservice_id,`name,`code,`description,`is_deleted,`create_time,`update_time,`create_by,`update_by`) VALUES (8667221518200015, 8661390544500010, '事件表', 'pa_event', '事件表', 0, '2026-08-14 09:50:15', '2026-08-14 09:50:15', 1, 1);
INSERT INTO `dev_model` (`id,`microservice_id,`name,`code,`description,`is_deleted,`create_time,`update_time,`create_by,`update_by`) VALUES (8667485435400001, 8661390544500010, '航空产品表', 'pa_event_air_product', '事件的航空产品', 0, '2026-08-14 10:34:14', '2026-08-14 10:36:43', 1, 1);
INSERT INTO `dev_model` (`id,`microservice_id,`name,`code,`description,`is_deleted,`create_time,`update_time,`create_by,`update_by`) VALUES (8667490905300002, 8661390544500010, '非航空产品表', 'pa_event_non_air_product', '事件的非航空产品', 0, '2026-08-14 10:35:09', '2026-08-14 10:36:37', 1, 1);
INSERT INTO `dev_model` (`id,`microservice_id,`name,`code,`description,`is_deleted,`create_time,`update_time,`create_by,`update_by`) VALUES (8667498634400003, 8661390544500010, '奖励明细', 'pa_event_award', '事件奖励明细', 0, '2026-08-14 10:36:26', '2026-08-14 10:36:26', 1, 1);
INSERT INTO `dev_model` (`id,`microservice_id,`name,`code,`description,`is_deleted,`create_time,`update_time,`create_by,`update_by`) VALUES (8667524845700004, 8661390544500010, '交易明细', 'pa_event_transaction', '事件交易信息', 0, '2026-08-14 10:40:48', '2026-08-14 10:40:48', 1, 1);

-- ----------------------------
-- 表结构：dev_model_field
-- ----------------------------
DROP TABLE IF EXISTS `dev_model_field`;
CREATE TABLE `dev_model_field` (
  `id` bigint NOT NULL,
  `model_id` bigint NOT NULL COMMENT '所属模型',
  `name` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '字段名',
  `display_name` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '显示名',
  `field_type` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '数据类型',
  `length` int DEFAULT NULL,
  `precision` int DEFAULT NULL,
  `is_required` tinyint NOT NULL DEFAULT '0',
  `is_primary` tinyint NOT NULL DEFAULT '0',
  `is_index` tinyint NOT NULL DEFAULT '0',
  `default_value` varchar(256) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `metadata_id` bigint DEFAULT NULL COMMENT '关联元数据(枚举类型)',
  `sort_order` int NOT NULL DEFAULT '0',
  `field_comment` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `is_deleted` tinyint NOT NULL DEFAULT '0',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `create_by` bigint DEFAULT NULL,
  `update_by` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_dev_mf_model_name` (`model_id`,`name`,`is_deleted`),
  KEY `idx_dev_mf_model_id` (`model_id`),
  KEY `idx_dev_mf_metadata_id` (`metadata_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='模型字段表';

-- ----------------------------
-- 表数据：dev_model_field (共 18 条)
-- ----------------------------
INSERT INTO `dev_model_field` (`id,`model_id,`name,`display_name,`field_type,`length,`precision,`is_required,`is_primary,`is_index,`default_value,`metadata_id,`sort_order,`field_comment,`is_deleted,`create_time,`update_time,`create_by,`update_by`) VALUES (8667391913100016, 8667221518200015, 'brand_code', '品牌', 'TEXT', 32, NULL, 1, 0, 0, NULL, NULL, 0, '品牌', 0, '2026-08-14 10:18:39', '2026-08-14 10:18:39', 1, 1);
INSERT INTO `dev_model_field` (`id,`model_id,`name,`display_name,`field_type,`length,`precision,`is_required,`is_primary,`is_index,`default_value,`metadata_id,`sort_order,`field_comment,`is_deleted,`create_time,`update_time,`create_by,`update_by`) VALUES (8667391913600017, 8667221518200015, 'chnl', '渠道', 'TEXT', 32, NULL, 0, 0, 0, NULL, NULL, 1, '渠道', 0, '2026-08-14 10:18:39', '2026-08-14 10:18:39', 1, 1);
INSERT INTO `dev_model_field` (`id,`model_id,`name,`display_name,`field_type,`length,`precision,`is_required,`is_primary,`is_index,`default_value,`metadata_id,`sort_order,`field_comment,`is_deleted,`create_time,`update_time,`create_by,`update_by`) VALUES (8667391913600018, 8667221518200015, 'external_flow1', '外部一级流水', 'TEXT', 32, NULL, 0, 0, 0, NULL, NULL, 2, '外部一级流水', 0, '2026-08-14 10:18:39', '2026-08-14 10:18:39', 1, 1);
INSERT INTO `dev_model_field` (`id,`model_id,`name,`display_name,`field_type,`length,`precision,`is_required,`is_primary,`is_index,`default_value,`metadata_id,`sort_order,`field_comment,`is_deleted,`create_time,`update_time,`create_by,`update_by`) VALUES (8667391913600019, 8667221518200015, 'external_flow2', '外部二级流水', 'TEXT', 32, NULL, 0, 0, 0, NULL, NULL, 3, '外部二级流水', 0, '2026-08-14 10:18:39', '2026-08-14 10:18:39', 1, 1);
INSERT INTO `dev_model_field` (`id,`model_id,`name,`display_name,`field_type,`length,`precision,`is_required,`is_primary,`is_index,`default_value,`metadata_id,`sort_order,`field_comment,`is_deleted,`create_time,`update_time,`create_by,`update_by`) VALUES (8667391913600020, 8667221518200015, 'external_flow3', '外部三级流水', 'TEXT', 32, NULL, 0, 0, 0, NULL, NULL, 4, '外部三级流水', 0, '2026-08-14 10:18:39', '2026-08-14 10:18:39', 1, 1);
INSERT INTO `dev_model_field` (`id,`model_id,`name,`display_name,`field_type,`length,`precision,`is_required,`is_primary,`is_index,`default_value,`metadata_id,`sort_order,`field_comment,`is_deleted,`create_time,`update_time,`create_by,`update_by`) VALUES (8667391913600021, 8667221518200015, 'flow_main_no', '主事件流水号', 'TEXT', 32, NULL, 1, 0, 0, NULL, NULL, 5, '主事件流水号', 0, '2026-08-14 10:18:39', '2026-08-14 10:18:39', 1, 1);
INSERT INTO `dev_model_field` (`id,`model_id,`name,`display_name,`field_type,`length,`precision,`is_required,`is_primary,`is_index,`default_value,`metadata_id,`sort_order,`field_comment,`is_deleted,`create_time,`update_time,`create_by,`update_by`) VALUES (8667391913600022, 8667221518200015, 'flow_no', '事件流水号', 'TEXT', 32, NULL, 1, 1, 0, NULL, NULL, 6, '事件流水号', 0, '2026-08-14 10:18:39', '2026-08-14 10:18:39', 1, 1);
INSERT INTO `dev_model_field` (`id,`model_id,`name,`display_name,`field_type,`length,`precision,`is_required,`is_primary,`is_index,`default_value,`metadata_id,`sort_order,`field_comment,`is_deleted,`create_time,`update_time,`create_by,`update_by`) VALUES (8667391913600023, 8667221518200015, 'event_type', '事件类型', 'TEXT', 10, NULL, 1, 0, 0, NULL, NULL, 7, '事件类型', 0, '2026-08-14 10:18:39', '2026-08-14 10:18:39', 1, 1);
INSERT INTO `dev_model_field` (`id,`model_id,`name,`display_name,`field_type,`length,`precision,`is_required,`is_primary,`is_index,`default_value,`metadata_id,`sort_order,`field_comment,`is_deleted,`create_time,`update_time,`create_by,`update_by`) VALUES (8667391913700024, 8667221518200015, 'event_time', '事件时间', 'DATETIME', NULL, NULL, 1, 0, 0, NULL, NULL, 8, '事件时间', 0, '2026-08-14 10:18:39', '2026-08-14 10:18:39', 1, 1);
INSERT INTO `dev_model_field` (`id,`model_id,`name,`display_name,`field_type,`length,`precision,`is_required,`is_primary,`is_index,`default_value,`metadata_id,`sort_order,`field_comment,`is_deleted,`create_time,`update_time,`create_by,`update_by`) VALUES (8667391913700025, 8667221518200015, 'acct_time', '账务时间', 'DATETIME', NULL, NULL, 1, 0, 0, NULL, NULL, 9, '账务时间', 0, '2026-08-14 10:18:39', '2026-08-14 10:18:39', 1, 1);
INSERT INTO `dev_model_field` (`id,`model_id,`name,`display_name,`field_type,`length,`precision,`is_required,`is_primary,`is_index,`default_value,`metadata_id,`sort_order,`field_comment,`is_deleted,`create_time,`update_time,`create_by,`update_by`) VALUES (8667391913700026, 8667221518200015, 'business_tags', '业务标签', 'TEXT', 32, NULL, 0, 0, 0, NULL, NULL, 10, '业务标签', 0, '2026-08-14 10:18:39', '2026-08-14 10:18:39', 1, 1);
INSERT INTO `dev_model_field` (`id,`model_id,`name,`display_name,`field_type,`length,`precision,`is_required,`is_primary,`is_index,`default_value,`metadata_id,`sort_order,`field_comment,`is_deleted,`create_time,`update_time,`create_by,`update_by`) VALUES (8667391913700027, 8667221518200015, 'business_status', '业务状态', 'TEXT', 10, NULL, 0, 0, 0, NULL, NULL, 11, '业务状态', 0, '2026-08-14 10:18:39', '2026-08-14 10:18:39', 1, 1);
INSERT INTO `dev_model_field` (`id,`model_id,`name,`display_name,`field_type,`length,`precision,`is_required,`is_primary,`is_index,`default_value,`metadata_id,`sort_order,`field_comment,`is_deleted,`create_time,`update_time,`create_by,`update_by`) VALUES (8667391913700028, 8667221518200015, 'scene_code', '场景码', 'TEXT', 10, NULL, 1, 0, 0, NULL, NULL, 12, '场景码', 0, '2026-08-14 10:18:39', '2026-08-14 10:18:39', 1, 1);
INSERT INTO `dev_model_field` (`id,`model_id,`name,`display_name,`field_type,`length,`precision,`is_required,`is_primary,`is_index,`default_value,`metadata_id,`sort_order,`field_comment,`is_deleted,`create_time,`update_time,`create_by,`update_by`) VALUES (8667391913700029, 8667221518200015, 'partner_code', '合作方', 'TEXT', 10, NULL, 0, 0, 0, NULL, NULL, 13, '合作方', 0, '2026-08-14 10:18:39', '2026-08-14 10:18:39', 1, 1);
INSERT INTO `dev_model_field` (`id,`model_id,`name,`display_name,`field_type,`length,`precision,`is_required,`is_primary,`is_index,`default_value,`metadata_id,`sort_order,`field_comment,`is_deleted,`create_time,`update_time,`create_by,`update_by`) VALUES (8667391913700030, 8667221518200015, 'member_code', '会员卡号', 'TEXT', 32, NULL, 0, 0, 0, NULL, NULL, 14, '会员卡号', 0, '2026-08-14 10:18:39', '2026-08-14 10:18:39', 1, 1);
INSERT INTO `dev_model_field` (`id,`model_id,`name,`display_name,`field_type,`length,`precision,`is_required,`is_primary,`is_index,`default_value,`metadata_id,`sort_order,`field_comment,`is_deleted,`create_time,`update_time,`create_by,`update_by`) VALUES (8667391913700031, 8667221518200015, 'acct_no', '会员账号', 'TEXT', 32, NULL, 1, 0, 0, NULL, NULL, 15, '会员账号', 0, '2026-08-14 10:18:39', '2026-08-14 10:18:39', 1, 1);
INSERT INTO `dev_model_field` (`id,`model_id,`name,`display_name,`field_type,`length,`precision,`is_required,`is_primary,`is_index,`default_value,`metadata_id,`sort_order,`field_comment,`is_deleted,`create_time,`update_time,`create_by,`update_by`) VALUES (8667391913700032, 8667221518200015, 'amt', '事件额度', 'DECIMAL', 18, 2, 0, 0, 0, NULL, NULL, 16, '事件额度', 0, '2026-08-14 10:18:39', '2026-08-14 15:15:28', 1, 1);
INSERT INTO `dev_model_field` (`id,`model_id,`name,`display_name,`field_type,`length,`precision,`is_required,`is_primary,`is_index,`default_value,`metadata_id,`sort_order,`field_comment,`is_deleted,`create_time,`update_time,`create_by,`update_by`) VALUES (8669433820900001, 8667221518200015, 'event_status', '事件状态', 'TEXT', 32, NULL, 0, 0, 0, NULL, NULL, 17, '事件状态', 0, '2026-08-14 15:58:58', '2026-08-14 15:58:58', 1, 1);

-- ----------------------------
-- 表结构：dev_model_index
-- ----------------------------
DROP TABLE IF EXISTS `dev_model_index`;
CREATE TABLE `dev_model_index` (
  `id` bigint NOT NULL,
  `model_id` bigint NOT NULL COMMENT '所属模型',
  `index_name` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '索引名称',
  `index_type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'NORMAL' COMMENT '索引类型: NORMAL/UNIQUE/FULLTEXT',
  `field_ids` varchar(1024) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '字段ID列表(JSON数组)',
  `is_deleted` tinyint NOT NULL DEFAULT '0',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `create_by` bigint DEFAULT NULL,
  `update_by` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_dev_mi_model_name` (`model_id`,`index_name`,`is_deleted`),
  KEY `idx_dev_mi_model_id` (`model_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='模型索引表';

-- ----------------------------
-- 表数据：dev_model_index (共 1 条)
-- ----------------------------
INSERT INTO `dev_model_index` (`id,`model_id,`index_name,`index_type,`field_ids,`is_deleted,`create_time,`update_time,`create_by,`update_by`) VALUES (8669438956100002, 8667221518200015, 'idx_event_acct_no', 'NORMAL', '["8667391913700031"]', 0, '2026-08-14 15:59:50', '2026-08-14 16:22:21', 1, 1);

-- ----------------------------
-- 表结构：dev_orch_edge
-- ----------------------------
DROP TABLE IF EXISTS `dev_orch_edge`;
CREATE TABLE `dev_orch_edge` (
  `id` bigint NOT NULL,
  `orchestration_id` bigint NOT NULL COMMENT '所属编排',
  `edge_key` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `from_node_key` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `to_node_key` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `condition_expr` varchar(1024) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `label_text` varchar(256) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `is_deleted` tinyint NOT NULL DEFAULT '0',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `create_by` bigint DEFAULT NULL,
  `update_by` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_dev_oe_orch_edgekey` (`orchestration_id`,`edge_key`,`is_deleted`),
  KEY `idx_dev_oe_orch_id` (`orchestration_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='编排连线表';

-- ----------------------------
-- 表结构：dev_orch_node
-- ----------------------------
DROP TABLE IF EXISTS `dev_orch_node`;
CREATE TABLE `dev_orch_node` (
  `id` bigint NOT NULL,
  `orchestration_id` bigint NOT NULL COMMENT '所属编排',
  `node_key` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `node_type` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'START/SERVICE/CONDITION/LOOP/END',
  `node_name` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `service_id` bigint DEFAULT NULL COMMENT '被调用服务(SERVICE节点)',
  `config_json` longtext COLLATE utf8mb4_unicode_ci,
  `tx_type` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT 'LOCAL' COMMENT '节点事务类型: LOCAL/DISTRIBUTED/NONE',
  `tx_timeout` int DEFAULT '60' COMMENT '节点事务超时(秒)',
  `retry_count` int DEFAULT '0' COMMENT '重试次数',
  `retry_interval` int DEFAULT '1000' COMMENT '重试间隔(ms)',
  `exception_strategy` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT 'INTERRUPT' COMMENT '异常策略: INTERRUPT-中断, CONTINUE-继续, IGNORE-忽略',
  `loop_type` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT 'SERIAL' COMMENT '循环类型: SERIAL-串行, PARALLEL-并行 (LOOP节点)',
  `branch_expr` varchar(1024) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '分支表达式 (BRANCH节点)',
  `x_pos` int DEFAULT NULL,
  `y_pos` int DEFAULT NULL,
  `sort_order` int NOT NULL DEFAULT '0',
  `is_deleted` tinyint NOT NULL DEFAULT '0',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `create_by` bigint DEFAULT NULL,
  `update_by` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_dev_on_orch_key` (`orchestration_id`,`node_key`,`is_deleted`),
  KEY `idx_dev_on_orch_id` (`orchestration_id`),
  KEY `idx_dev_on_service_id` (`service_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='编排节点表';

-- ----------------------------
-- 表结构：dev_orch_param
-- ----------------------------
DROP TABLE IF EXISTS `dev_orch_param`;
CREATE TABLE `dev_orch_param` (
  `id` bigint NOT NULL,
  `orchestration_id` bigint NOT NULL COMMENT '所属编排',
  `param_scope` varchar(16) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'INPUT-编排入参, OUTPUT-编排出参',
  `param_name` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `data_type` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL,
  `is_required` tinyint NOT NULL DEFAULT '1',
  `param_comment` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `source_node_key` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '出参来源节点Key(仅OUTPUT)',
  `source_field` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '出参来源字段(仅OUTPUT)',
  `sort_order` int NOT NULL DEFAULT '0',
  `is_deleted` tinyint NOT NULL DEFAULT '0',
  `create_by` bigint DEFAULT NULL,
  `update_by` bigint DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_dev_op_orch_scope_name` (`orchestration_id`,`param_scope`,`param_name`,`is_deleted`),
  KEY `idx_dev_op_orch_id` (`orchestration_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='编排参数表';

-- ----------------------------
-- 表结构：dev_orchestration
-- ----------------------------
DROP TABLE IF EXISTS `dev_orchestration`;
CREATE TABLE `dev_orchestration` (
  `id` bigint NOT NULL,
  `microservice_id` bigint NOT NULL COMMENT '所属微服务',
  `name` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL,
  `code` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` varchar(1024) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `status` tinyint NOT NULL DEFAULT '1',
  `tx_type` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT 'LOCAL' COMMENT '事务类型: LOCAL-本地事务, DISTRIBUTED-分布式事务',
  `tx_timeout` int DEFAULT '300' COMMENT '事务超时(秒)',
  `is_deleted` tinyint NOT NULL DEFAULT '0',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `create_by` bigint DEFAULT NULL,
  `update_by` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_dev_orch_ms_code` (`microservice_id`,`code`,`is_deleted`),
  KEY `idx_dev_orch_microservice_id` (`microservice_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='服务编排表';

-- ----------------------------
-- 表结构：dev_service
-- ----------------------------
DROP TABLE IF EXISTS `dev_service`;
CREATE TABLE `dev_service` (
  `id` bigint NOT NULL,
  `microservice_id` bigint NOT NULL COMMENT '所属微服务',
  `name` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL,
  `code` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` varchar(1024) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `http_method` varchar(16) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'POST',
  `service_path` varchar(256) COLLATE utf8mb4_unicode_ci NOT NULL,
  `category` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `status` tinyint NOT NULL DEFAULT '1',
  `is_deleted` tinyint NOT NULL DEFAULT '0',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `create_by` bigint DEFAULT NULL,
  `update_by` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_dev_svc_ms_code` (`microservice_id`,`code`,`is_deleted`),
  UNIQUE KEY `uk_dev_svc_ms_path` (`microservice_id`,`service_path`,`is_deleted`),
  KEY `idx_dev_svc_microservice_id` (`microservice_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='服务定义表';

-- ----------------------------
-- 表结构：dev_service_param
-- ----------------------------
DROP TABLE IF EXISTS `dev_service_param`;
CREATE TABLE `dev_service_param` (
  `id` bigint NOT NULL,
  `service_id` bigint NOT NULL COMMENT '所属服务',
  `param_type` tinyint NOT NULL COMMENT '1=入参 2=出参',
  `param_name` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `data_type` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL,
  `is_required` tinyint NOT NULL DEFAULT '1',
  `default_value` varchar(256) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `model_field_id` bigint DEFAULT NULL COMMENT '关联模型字段',
  `sort_order` int NOT NULL DEFAULT '0',
  `param_comment` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `is_deleted` tinyint NOT NULL DEFAULT '0',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `create_by` bigint DEFAULT NULL,
  `update_by` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_dev_sp_svc_type_name` (`service_id`,`param_type`,`param_name`,`is_deleted`),
  KEY `idx_dev_sp_service_id` (`service_id`),
  KEY `idx_dev_sp_field_id` (`model_field_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='服务参数表';

SET FOREIGN_KEY_CHECKS = 1;

-- 导出完成
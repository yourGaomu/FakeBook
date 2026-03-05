-- =========================================================================================
-- MySQL 建表语句 (仅供参考)
-- 注意：本项目推荐使用 MongoDB，如果必须使用 MySQL，请使用以下 SQL
-- =========================================================================================

CREATE DATABASE IF NOT EXISTS `share_things_chat` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `share_things_chat`;

-- ----------------------------
-- 1. 聊天消息表 (chat_message)
-- ----------------------------
CREATE TABLE IF NOT EXISTS `chat_message` (
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `conversation_id` varchar(64) NOT NULL COMMENT '会话ID (单聊:userId1_userId2, 群聊:groupId)',
  `from_user_id` varchar(64) NOT NULL COMMENT '发送者ID',
  `to_user_id` varchar(64) NOT NULL COMMENT '接收者ID (单聊为对方ID，群聊为群ID)',
  `msg_type` tinyint(4) NOT NULL DEFAULT 1 COMMENT '消息类型 1:文本 2:图片 3:语音 4:视频',
  `content` text COMMENT '消息内容 (文本内容 或 媒体URL)',
  `extra` json DEFAULT NULL COMMENT '扩展信息 (JSON结构)',
  `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '消息状态 0:发送中 1:已发送 2:已读 3:已撤回',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_conversation_time` (`conversation_id`,`create_time`) USING BTREE COMMENT '会话历史记录查询索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='聊天消息表';

-- ----------------------------
-- 2. 会话列表表 (chat_conversation)
-- ----------------------------
CREATE TABLE IF NOT EXISTS `chat_conversation` (
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `conversation_id` varchar(64) NOT NULL COMMENT '会话ID (唯一标识)',
  `type` tinyint(4) NOT NULL DEFAULT 1 COMMENT '会话类型 1:单聊 2:群聊',
  `last_message_id` varchar(64) DEFAULT NULL COMMENT '最后一条消息ID',
  `last_message_content` varchar(500) DEFAULT NULL COMMENT '最后一条消息内容预览',
  `last_message_time` datetime DEFAULT NULL COMMENT '最后一条消息时间',
  `unread_counts` json DEFAULT NULL COMMENT '各成员未读数 (JSON: {"userA": 0, "userB": 1})',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_conversation_id` (`conversation_id`) USING BTREE COMMENT '会话ID唯一索引',
  KEY `idx_last_message_time` (`last_message_time`) USING BTREE COMMENT '排序索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会话列表表';

-- ----------------------------
-- 注意：MySQL 版本的缺陷
-- ----------------------------
-- 1. chat_conversation 表难以高效查询“我的会话列表”。
--    因为 MySQL 不支持像 MongoDB 那样高效地对 `members` 数组建索引。
--    如果用 MySQL，通常需要引入第三张表 `chat_conversation_member` 来存储 会话与人的多对多关系：
-- 
--    CREATE TABLE `chat_conversation_member` (
--      `conversation_id` varchar(64) NOT NULL,
--      `user_id` varchar(64) NOT NULL,
--      `display_name` varchar(64) DEFAULT NULL,
--      KEY `idx_user_conv` (`user_id`, `conversation_id`)
--    );

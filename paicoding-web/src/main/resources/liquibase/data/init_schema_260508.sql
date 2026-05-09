SET NAMES utf8;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
--  Table structure for `conversation`
-- ----------------------------
DROP TABLE IF EXISTS `conversation`;
CREATE TABLE `conversation` (
  `id`                BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
  `conversation_type` VARCHAR(20)     NOT NULL DEFAULT 'PRIVATE' COMMENT '会话类型: PRIVATE-一对一, GROUP-群聊(预留)',
  `initiator_id`      BIGINT          NOT NULL DEFAULT 0       COMMENT '会话发起者用户ID',
  `last_message_id`   BIGINT          NOT NULL DEFAULT 0       COMMENT '最后一条消息ID(冗余,加速列表查询)',
  `last_message_time` DATETIME        NULL                     COMMENT '最后一条消息时间(冗余)',
  `deleted`           TINYINT         NOT NULL DEFAULT 0       COMMENT '逻辑删除标记: 0-未删除, 1-已删除',
  `create_time`       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  INDEX `idx_conversation_last_msg_time` (`last_message_time` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会话表';

-- ----------------------------
--  Table structure for `conversation_member`
-- ----------------------------
DROP TABLE IF EXISTS `conversation_member`;
CREATE TABLE `conversation_member` (
  `id`                    BIGINT      NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
  `conversation_id`       BIGINT      NOT NULL DEFAULT 0       COMMENT '会话ID',
  `user_id`               BIGINT      NOT NULL DEFAULT 0       COMMENT '用户ID',
  `unread_count`          INT         NOT NULL DEFAULT 0       COMMENT '该用户在此会话中的未读消息数',
  `last_read_message_id`  BIGINT      NOT NULL DEFAULT 0       COMMENT '该用户最后已读的消息ID',
  `is_deleted`            TINYINT     NOT NULL DEFAULT 0       COMMENT '软删除标记: 0-未删除, 1-已删除',
  `is_top`                TINYINT     NOT NULL DEFAULT 0       COMMENT '置顶标记: 0-未置顶, 1-已置顶(预留, S2)',
  `create_time`           DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
  `update_time`           DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE INDEX `idx_cm_conv_user` (`conversation_id`, `user_id`) COMMENT '一个用户在一个会话中只有一条成员记录',
  INDEX `idx_cm_user_deleted_time` (`user_id`, `is_deleted`, `update_time` DESC) COMMENT '加速查询用户的会话列表'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会话成员表';

-- ----------------------------
--  Table structure for `message`
-- ----------------------------
DROP TABLE IF EXISTS `message`;
CREATE TABLE `message` (
  `id`                BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
  `conversation_id`   BIGINT          NOT NULL DEFAULT 0       COMMENT '所属会话ID',
  `sender_id`         BIGINT          NOT NULL DEFAULT 0       COMMENT '发送者用户ID',
  `message_type`      VARCHAR(20)     NOT NULL DEFAULT 'TEXT'  COMMENT '消息类型: TEXT-文本, IMAGE/FILE/VIDEO(预留)',
  `content`           TEXT            NOT NULL                 COMMENT '消息正文',
  `referenced_msg_id` BIGINT          NULL                     COMMENT '引用的消息ID(预留,可为null)',
  `attachment`        JSON            NULL                     COMMENT '附件信息(预留,可为null)',
  `status`            VARCHAR(20)     NOT NULL DEFAULT 'SENT'  COMMENT '消息状态: SENDING-发送中, SENT-已发送, DELIVERED-已送达, READ-已读',
  `create_time`       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
  `update_time`       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  INDEX `idx_msg_conversation_time` (`conversation_id`, `create_time` DESC) COMMENT '加速查询某个会话的消息列表',
  INDEX `idx_msg_sender_time` (`sender_id`, `create_time` DESC) COMMENT '加速查询某个用户发送的消息',
  INDEX `idx_msg_status` (`status`) COMMENT '加速按状态查询'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息表';

SET FOREIGN_KEY_CHECKS = 1;

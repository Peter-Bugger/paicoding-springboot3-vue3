package com.github.paicoding.forum.service.msg.repository.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.github.paicoding.forum.api.model.entity.BaseDO;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 消息实体
 *
 * @author Mr.Bu
 * @date 2026-05-08
 */
@Data
@Accessors(chain = true)
@TableName("message")
public class MessageDO extends BaseDO {
    private static final long serialVersionUID = -1328444059235815135L;

    /**
     * 所属会话ID
     */
    private Long conversationId;

    /**
     * 发送者用户ID
     */
    private Long senderId;

    /**
     * 消息类型: TEXT-文本, IMAGE/FILE/VIDEO(预留)
     */
    private String messageType;

    /**
     * 消息正文
     */
    private String content;

    /**
     * 引用的消息ID(预留,可为null)
     */
    private Long referencedMsgId;

    /**
     * 附件信息(预留,可为null)
     */
    private String attachment;

    /**
     * 消息状态: SENDING-发送中, SENT-已发送, DELIVERED-已送达, READ-已读
     */
    private String status;
}

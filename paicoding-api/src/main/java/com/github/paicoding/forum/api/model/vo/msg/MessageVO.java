package com.github.paicoding.forum.api.model.vo.msg;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * 消息视图对象
 *
 * @author Mr.Bu
 * @date 2026-05-08
 */
@Data
@Accessors(chain = true)
public class MessageVO {

    /**
     * 消息ID
     */
    private Long messageId;

    /**
     * 所属会话ID
     */
    private Long conversationId;

    /**
     * 发送者用户ID
     */
    private Long fromUserId;

    /**
     * 发送者用户名
     */
    private String fromUserName;

    /**
     * 发送者头像
     */
    private String fromUserPhoto;

    /**
     * 消息类型: TEXT/IMAGE/FILE/VIDEO
     */
    private String messageType;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 引用的消息ID
     */
    private Long referencedMsgId;

    /**
     * 附件信息(JSON)
     */
    private String attachment;

    /**
     * 消息状态: SENDING/SENT/DELIVERED/READ
     */
    private String status;

    /**
     * 发送时间
     */
    private Date createTime;
}

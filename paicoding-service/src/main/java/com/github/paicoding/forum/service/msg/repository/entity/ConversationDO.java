package com.github.paicoding.forum.service.msg.repository.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.github.paicoding.forum.api.model.entity.BaseDO;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * 会话实体
 *
 * @author Mr.Bu
 * @date 2026-05-08
 */
@Data
@Accessors(chain = true)
@TableName("conversation")
public class ConversationDO extends BaseDO {
    private static final long serialVersionUID = -3677931104491222688L;

    /**
     * 会话类型: PRIVATE-一对一, GROUP-群聊(预留)
     */
    private String conversationType;

    /**
     * 会话发起者用户ID
     */
    private Long initiatorId;

    /**
     * 最后一条消息ID(冗余,加速列表查询)
     */
    private Long lastMessageId;

    /**
     * 最后一条消息时间(冗余)
     */
    private Date lastMessageTime;

    /**
     * 逻辑删除标记: 0-未删除, 1-已删除
     */
    private Integer deleted;
}

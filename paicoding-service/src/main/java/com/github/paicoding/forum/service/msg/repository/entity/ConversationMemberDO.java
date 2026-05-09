package com.github.paicoding.forum.service.msg.repository.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.github.paicoding.forum.api.model.entity.BaseDO;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * 会话成员实体
 *
 * @author Mr.Bu
 * @date 2026-05-08
 */
@Data
@Accessors(chain = true)
@TableName("conversation_member")
public class ConversationMemberDO extends BaseDO {
    private static final long serialVersionUID = 2137553574197683422L;

    /**
     * 会话ID
     */
    private Long conversationId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 该用户在此会话中的未读消息数
     */
    private Integer unreadCount;

    /**
     * 该用户最后已读的消息ID
     */
    private Long lastReadMessageId;

    /**
     * 软删除标记: 0-未删除, 1-已删除
     */
    private Integer isDeleted;

    /**
     * 置顶标记: 0-未置顶, 1-已置顶(预留, S2)
     */
    private Integer isTop;

    /**
     * 清除时间(用户清除会话时记录)
     */
    private Date clearedAt;
}

package com.github.paicoding.forum.api.model.vo.msg;

import com.github.paicoding.forum.api.model.vo.user.dto.BaseUserInfoDTO;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * 会话视图对象
 *
 * @author Mr.Bu
 * @date 2026-05-08
 */
@Data
@Accessors(chain = true)
public class ConversationVO {

    /**
     * 会话ID
     */
    private Long conversationId;

    /**
     * 会话类型: PRIVATE/GROUP
     */
    private String conversationType;

    /**
     * 目标用户信息（私信对方）
     */
    private BaseUserInfoDTO targetUser;

    /**
     * 最后一条消息摘要
     */
    private LastMessageVO lastMessage;

    /**
     * 未读消息数
     */
    private Integer unreadCount;

    /**
     * 置顶标记
     */
    private Integer isTop;

    /**
     * 会话创建时间
     */
    private Date createTime;

    @Data
    @Accessors(chain = true)
    public static class LastMessageVO {
        /**
         * 消息内容
         */
        private String content;

        /**
         * 发送时间
         */
        private Date createTime;

        /**
         * 消息状态
         */
        private String status;

        /**
         * 发送者用户ID
         */
        private Long fromUserId;
    }
}

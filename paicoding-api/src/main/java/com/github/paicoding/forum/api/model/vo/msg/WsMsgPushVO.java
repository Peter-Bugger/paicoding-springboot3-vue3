package com.github.paicoding.forum.api.model.vo.msg;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * WebSocket 消息推送体
 *
 * @author Mr.Bu
 * @date 2026-05-08
 */
@Data
@Accessors(chain = true)
public class WsMsgPushVO {

    /**
     * 消息类型: NEW_MESSAGE / READ_STATUS
     */
    private String type;

    /**
     * 消息载荷
     */
    private Object payload;

    /**
     * 新消息推送载荷
     */
    @Data
    @Accessors(chain = true)
    public static class NewMessagePayload {
        private Long conversationId;
        private Long messageId;
        private Long fromUserId;
        private String fromUserName;
        private String fromUserPhoto;
        private String content;
        private String messageType;
        private Date createTime;
    }

    /**
     * 已读状态推送载荷
     */
    @Data
    @Accessors(chain = true)
    public static class ReadStatusPayload {
        private Long conversationId;
        private Long messageId;
        private Long readByUserId;
        private Date readTime;
    }

    public static final String TYPE_NEW_MESSAGE = "NEW_MESSAGE";
    public static final String TYPE_READ_STATUS = "READ_STATUS";

    public static WsMsgPushVO newMessage(Object payload) {
        WsMsgPushVO vo = new WsMsgPushVO();
        vo.setType(TYPE_NEW_MESSAGE);
        vo.setPayload(payload);
        return vo;
    }

    public static WsMsgPushVO readStatus(Object payload) {
        WsMsgPushVO vo = new WsMsgPushVO();
        vo.setType(TYPE_READ_STATUS);
        vo.setPayload(payload);
        return vo;
    }
}

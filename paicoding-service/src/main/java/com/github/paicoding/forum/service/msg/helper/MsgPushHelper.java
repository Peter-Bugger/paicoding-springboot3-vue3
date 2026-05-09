package com.github.paicoding.forum.service.msg.helper;

import com.github.paicoding.forum.api.model.vo.msg.WsMsgPushVO;
import com.github.paicoding.forum.service.msg.repository.entity.MessageDO;
import com.github.paicoding.forum.service.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * 私信 WebSocket 推送辅助
 *
 * @author Mr.Bu
 * @date 2026-05-08
 */
@Slf4j
@Component
public class MsgPushHelper {

    /** userId → sessionId 映射，由 MsgHandshakeInterceptor 在握手时填充，用于 WebSocket 向指定用户推送消息 */
    public static final java.util.concurrent.ConcurrentHashMap<Long, String> USER_SESSION_MAP = new java.util.concurrent.ConcurrentHashMap<>();

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    @Autowired
    private UserService userService;

    /**
     * 推送新消息给目标用户
     *
     * @param targetUserId 目标用户ID
     * @param msg          消息实体
     */
    public void pushNewMessage(Long targetUserId, MessageDO msg) {
        try {
            var user = userService.queryBasicUserInfo(msg.getSenderId());
            WsMsgPushVO.NewMessagePayload payload = new WsMsgPushVO.NewMessagePayload();
            payload.setConversationId(msg.getConversationId());
            payload.setMessageId(msg.getId());
            payload.setFromUserId(msg.getSenderId());
            payload.setFromUserName(user != null ? user.getUserName() : "");
            payload.setFromUserPhoto(user != null ? user.getPhoto() : "");
            payload.setContent(msg.getContent());
            payload.setMessageType(msg.getMessageType());
            payload.setCreateTime(msg.getCreateTime() != null ? msg.getCreateTime() : new Date());

            WsMsgPushVO pushVO = WsMsgPushVO.newMessage(payload);
            // 通过 userId→session 映射发送到用户私有通道
            String userSession = USER_SESSION_MAP.get(targetUserId);
            if (userSession != null) {
                simpMessagingTemplate.convertAndSendToUser(userSession, "/msg/new", pushVO);
                log.info("WS推送新消息: userId={}, session={}, conversationId={}", targetUserId, userSession, msg.getConversationId());
            } else {
                log.info("WS推送跳过: userId={} 未连接，conversationId={}", targetUserId, msg.getConversationId());
            }
        } catch (Exception e) {
            log.error("WS推送新消息失败: userId={}, msgId={}", targetUserId, msg.getId(), e);
        }
    }

    /**
     * 推送已读状态给消息发送者
     *
     * @param senderUserId   原消息发送者ID
     * @param conversationId 会话ID
     * @param messageId      最后已读的消息ID
     * @param readByUserId   已读操作的用户ID
     */
    public void pushReadStatus(Long senderUserId, Long conversationId, Long messageId, Long readByUserId) {
        try {
            WsMsgPushVO.ReadStatusPayload payload = new WsMsgPushVO.ReadStatusPayload();
            payload.setConversationId(conversationId);
            payload.setMessageId(messageId);
            payload.setReadByUserId(readByUserId);
            payload.setReadTime(new Date());

            WsMsgPushVO pushVO = WsMsgPushVO.readStatus(payload);
            String userSession = USER_SESSION_MAP.get(senderUserId);
            if (userSession != null) {
                simpMessagingTemplate.convertAndSendToUser(userSession, "/msg/read", pushVO);
                log.info("WS推送已读状态: senderId={}, session={}, conversationId={}", senderUserId, userSession, conversationId);
            } else {
                log.info("WS推送已读跳过: senderId={} 未连接，conversationId={}", senderUserId, conversationId);
            }
        } catch (Exception e) {
            log.error("WS推送已读状态失败: senderId={}, conversationId={}", senderUserId, conversationId, e);
        }
    }
}

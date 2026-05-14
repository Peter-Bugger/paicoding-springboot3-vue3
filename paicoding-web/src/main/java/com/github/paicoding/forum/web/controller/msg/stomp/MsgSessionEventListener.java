package com.github.paicoding.forum.web.controller.msg.stomp;

import com.github.paicoding.forum.service.msg.helper.MsgPushHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.Set;

/**
 * 私信 WS 会话事件监听（清理 userId↔session 映射）
 *
 * @author Mr.Bu
 * @date 2026-05-09
 */
@Slf4j
@Component
public class MsgSessionEventListener {

    @EventListener
    public void onDisconnect(SessionDisconnectEvent event) {
        if (event == null || event.getUser() == null) {
            return;
        }

        String session = event.getUser().getName();
        if (session == null || session.isBlank()) {
            return;
        }

        Long userId = MsgPushHelper.SESSION_USER_MAP.remove(session);
        if (userId == null) {
            return;
        }

        Set<String> sessions = MsgPushHelper.USER_SESSION_MAP.get(userId);
        if (sessions != null) {
            sessions.remove(session);
            if (sessions.isEmpty()) {
                MsgPushHelper.USER_SESSION_MAP.remove(userId);
            }
        }

        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        log.info("Msg WS disconnect: userId={}, session={}, simpSessionId={}", userId, session, accessor.getSessionId());
    }
}

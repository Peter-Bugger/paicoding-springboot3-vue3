package com.github.paicoding.forum.web.controller.msg.stomp;

import com.github.paicoding.forum.api.model.context.ReqInfoContext;
import com.github.paicoding.forum.service.user.service.LoginService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;

/**
 * 私信 WebSocket 握手处理器
 *
 * @author Mr.Bu
 * @date 2026-05-08
 */
@Slf4j
public class MsgHandshakeHandler extends DefaultHandshakeHandler {

    @Override
    protected Principal determineUser(ServerHttpRequest request, WebSocketHandler wsHandler, Map<String, Object> attributes) {
        // 优先使用拦截器设置的用户信息
        ReqInfoContext.ReqInfo reqInfo = (ReqInfoContext.ReqInfo) attributes.get(LoginService.SESSION_KEY);
        if (reqInfo != null) {
            return reqInfo;
        }

        // 从路径中提取 session 作为兜底标识
        String uri = request.getURI().toString();
        String session = uri.substring(uri.lastIndexOf("/") + 1);
        log.info("Msg WS handshake, session={}", session);
        return () -> session;
    }
}

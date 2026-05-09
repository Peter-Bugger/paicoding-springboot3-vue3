package com.github.paicoding.forum.web.controller.msg.stomp;

import com.github.paicoding.forum.api.model.context.ReqInfoContext;
import com.github.paicoding.forum.core.mdc.MdcUtil;
import com.github.paicoding.forum.core.mdc.SelfTraceIdGenerator;
import com.github.paicoding.forum.core.util.SessionUtil;
import com.github.paicoding.forum.core.util.SpringUtil;
import com.github.paicoding.forum.service.user.service.LoginService;
import com.github.paicoding.forum.web.global.GlobalInitService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 私信 WebSocket 握手拦截器，用于身份验证
 *
 * @author Mr.Bu
 * @date 2026-05-08
 */
@Slf4j
public class MsgHandshakeInterceptor extends HttpSessionHandshakeInterceptor {

    /** userId → sessionId 集合映射，委托给 MsgPushHelper.USER_SESSION_MAP */
    private static final ConcurrentHashMap<Long, java.util.Set<String>> USER_SESSION_MAP =
            com.github.paicoding.forum.service.msg.helper.MsgPushHelper.USER_SESSION_MAP;

    /** sessionId → userId 反向索引，委托给 MsgPushHelper.SESSION_USER_MAP */
    private static final ConcurrentHashMap<String, Long> SESSION_USER_MAP =
            com.github.paicoding.forum.service.msg.helper.MsgPushHelper.SESSION_USER_MAP;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        log.info("Msg WS 开始握手!");
        String session = SessionUtil.findCookieByName(request, LoginService.SESSION_KEY);
        ReqInfoContext.ReqInfo reqInfo = new ReqInfoContext.ReqInfo();
        SpringUtil.getBean(GlobalInitService.class).initLoginUser(session, reqInfo);

        if (reqInfo.getUser() == null) {
            log.warn("Msg WS 握手失败，未登录");
            return false;
        }

        attributes.put(MdcUtil.TRACE_ID_KEY, SelfTraceIdGenerator.generate());
        attributes.put(LoginService.SESSION_KEY, reqInfo);

        // 建立 userId → sessionId 映射，供 MsgPushHelper 向指定用户推送消息（同账号多端在线时需要保留多个 session）
        if (reqInfo.getUserId() != null) {
            USER_SESSION_MAP.computeIfAbsent(reqInfo.getUserId(), k -> java.util.concurrent.ConcurrentHashMap.newKeySet())
                    .add(session);
            SESSION_USER_MAP.put(session, reqInfo.getUserId());
            log.info("Msg WS 映射 userId={} → session={} (totalSessions={})", reqInfo.getUserId(), session,
                    USER_SESSION_MAP.get(reqInfo.getUserId()).size());
        }
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception ex) {
        log.info("Msg WS 握手成功");
        super.afterHandshake(request, response, wsHandler, ex);
    }
}

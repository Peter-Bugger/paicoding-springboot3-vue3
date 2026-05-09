package com.github.paicoding.forum.web.controller.msg.stomp;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * 私信功能 WebSocket STOMP 配置
 * 独立于 AI 聊天配置，使用单独的 broker 端点和前缀
 *
 * @author Mr.Bu
 * @date 2026-05-08
 */
@Slf4j
@Configuration
public class WsMsgConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // 应用目的地前缀（消息代理由 WsChatConfig 统一配置，包含 /chat 和 /msg 前缀）
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 注册 /msg/{session} 端点
        // {session} 为用户会话标识，用于 Principal 识别
        registry.addEndpoint("/msg/{session}")
                .setHandshakeHandler(new MsgHandshakeHandler())
                .addInterceptors(new MsgHandshakeInterceptor())
                .setAllowedOriginPatterns("*");
    }
}

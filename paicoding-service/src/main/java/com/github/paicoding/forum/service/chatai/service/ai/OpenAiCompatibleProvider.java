package com.github.paicoding.forum.service.chatai.service.ai;

import com.github.paicoding.forum.service.chatai.service.ai.AiModelConfig.ModelConf;
import lombok.extern.slf4j.Slf4j;

/**
 * OpenAI 兼容协议模型提供者实现
 * <p>
 * 通用实现，支持任何 OpenAI 兼容的 API 提供商。
 * 通过 AiModelConfig.ModelConf 配置实例化，新增模型无需编写 Java 代码。
 *
 * @author XuYifei
 * @date 2024-07-12
 */
// @PRD: US4 — AC4.4 — 新增配置，自动加入可用池
@Slf4j
public class OpenAiCompatibleProvider implements AiModelProvider {

    private final String providerName;
    private final String displayName;
    private final int priority;
    private volatile boolean available = true;

    private final OpenAiChatClient chatClient;

    public OpenAiCompatibleProvider(ModelConf config) {
        this.providerName = config.getName();
        this.displayName = config.getDisplayName();
        this.priority = config.getPriority();
        this.available = config.isEnabled();
        this.chatClient = new OpenAiChatClient(
                config.getApiHost(),
                config.getApiKey(),
                config.getModelName(),
                config.getMaxTokens(),
                config.getTimeoutSeconds()
        );
        log.info("初始化 AI 模型提供商: {} ({})", providerName, displayName);
    }

    @Override
    public String getProviderName() {
        return providerName;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public int getPriority() {
        return priority;
    }

    @Override
    public boolean isAvailable() {
        return available;
    }

    @Override
    public void markUnavailable() {
        log.warn("标记模型 {} 为不可用", providerName);
        this.available = false;
    }

    @Override
    public void markAvailable() {
        log.info("标记模型 {} 为可用", providerName);
        this.available = true;
    }

    @Override
    public String chat(String systemPrompt, String userMessage) {
        return chatClient.chat(systemPrompt, userMessage);
    }

    @Override
    public void chatStream(String systemPrompt, String userMessage, StreamCallback callback) {
        chatClient.chatStream(systemPrompt, userMessage, callback);
    }

    /**
     * 健康检查
     *
     * @return true 表示 API 正常响应
     */
    public boolean healthCheck() {
        return chatClient.healthCheck();
    }
}

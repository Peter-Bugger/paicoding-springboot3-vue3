package com.github.paicoding.forum.service.chatai.service.ai;

/**
 * AI 模型提供者接口
 *
 * 所有 AI 模型提供商（DeepSeek, Kimi, 通义千问等）均实现此接口，
 * 通过 AiModelRouter 统一路由和降级。
 *
 * @author XuYifei
 * @date 2024-07-12
 */
// @PRD: US4 — AC4.1 AC4.2 AC4.3 — 多模型配置，按优先级选择，失败切换
public interface AiModelProvider {

    /**
     * @return 提供商唯一标识，如 "deepseek", "kimi", "qwen"
     */
    String getProviderName();

    /**
     * @return 提供商显示名称，如 "DeepSeek V3", "月之暗面 Kimi"
     */
    String getDisplayName();

    /**
     * @return 优先级（数值越小优先级越高）
     */
    int getPriority();

    /**
     * @return 当前是否可用
     */
    boolean isAvailable();

    /**
     * 标记为不可用（触发健康检查后重新标记）
     */
    void markUnavailable();

    /**
     * 标记为可用
     */
    void markAvailable();

    /**
     * 同步调用，直接返回完整回答
     *
     * @param systemPrompt 系统提示词
     * @param userMessage  用户消息
     * @return AI 回答内容
     */
    String chat(String systemPrompt, String userMessage);

    /**
     * 流式调用，通过回调逐块返回
     *
     * @param systemPrompt 系统提示词
     * @param userMessage  用户消息
     * @param callback     流式回调
     */
    void chatStream(String systemPrompt, String userMessage, StreamCallback callback);
}

package com.github.paicoding.forum.service.chatai.service.ai;

/**
 * AI 流式调用的回调接口
 * <p>
 * 接口定义与 XunFeiIntegration.StreamCallback 不同：
 * XunFeiIntegration 的 onComplete() 无参数；onError(Throwable, String) 含额外 response 参数。
 * 本接口简化设计，在 onComplete 中返回完整结果，在 onError 中仅返回异常。
 *
 * @author XuYifei
 * @date 2024-07-12
 */
// @PRD: US3 — AC3.1 — 流式返回，逐块回调
public interface StreamCallback {

    /**
     * 接收到一个文本片段时回调
     *
     * @param content 本次返回的文本内容
     */
    void onMessage(String content);

    /**
     * 所有流式数据接收完毕时回调
     *
     * @param fullContent 完整的回答内容
     */
    void onComplete(String fullContent);

    /**
     * 发生错误时回调
     *
     * @param error 异常信息
     */
    void onError(Throwable error);
}

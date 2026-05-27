package com.github.paicoding.forum.service.chatai.service.ai;

import com.github.paicoding.forum.core.util.JsonUtil;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 通用 OpenAI 兼容 API 客户端
 * <p>
 * 支持同步调用（stream=false）和流式调用（stream=true）。
 * 流式响应采用 SSE 解析，复用 XunFeiIntegration 中已有的解析模式：
 * 按行 split，识别 "data:" 前缀，检测 "[DONE]" 终止符。
 *
 * @author XuYifei
 * @date 2024-07-12
 */
// @PRD: US4 — AC4.4 — 新增配置，自动加入可用池（通用客户端支持任何 OpenAI 兼容 API）
@Slf4j
public class OpenAiChatClient {

    private final OkHttpClient okHttpClient;
    private final String apiHost;
    private final String apiKey;
    private final String modelName;
    private final int maxTokens;
    private final int timeoutSeconds;

    public OpenAiChatClient(String apiHost, String apiKey, String modelName,
                            int maxTokens, int timeoutSeconds) {
        this.apiHost = apiHost;
        this.apiKey = apiKey;
        this.modelName = modelName;
        this.maxTokens = maxTokens;
        this.timeoutSeconds = timeoutSeconds;
        this.okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(timeoutSeconds, TimeUnit.SECONDS)
                .readTimeout(timeoutSeconds, TimeUnit.SECONDS)
                .writeTimeout(timeoutSeconds, TimeUnit.SECONDS)
                .build();
    }

    /**
     * 同步调用，直接返回完整回答
     *
     * @param systemPrompt 系统提示词
     * @param userMessage  用户消息
     * @return AI 回答内容，失败返回 null
     */
    public String chat(String systemPrompt, String userMessage) {
        try {
            OpenAiCompletionRequest request = buildRequest(systemPrompt, userMessage, false);
            Request httpRequest = buildHttpRequest(request);

            try (Response response = okHttpClient.newCall(httpRequest).execute()) {
                if (!response.isSuccessful()) {
                    String errorBody = response.body() != null ? response.body().string() : "";
                    log.warn("OpenAI 兼容 API 返回错误, HTTP code={}, body={}", response.code(), errorBody);
                    return null;
                }
                String bodyStr = response.body().string();
                OpenAiCompletionResponse result = JsonUtil.toObj(bodyStr, OpenAiCompletionResponse.class);
                if (result != null && result.getChoices() != null && !result.getChoices().isEmpty()
                        && result.getChoices().get(0).getMessage() != null) {
                    return result.getChoices().get(0).getMessage().getContent();
                }
                log.warn("OpenAI 兼容 API 返回为空");
                return null;
            }
        } catch (Exception e) {
            log.warn("OpenAI 兼容 API 同步调用失败: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 流式调用，通过 SSE 逐块返回 AI 回答
     *
     * @param systemPrompt 系统提示词
     * @param userMessage  用户消息
     * @param callback     流式回调
     */
    public void chatStream(String systemPrompt, String userMessage, StreamCallback callback) {
        try {
            OpenAiCompletionRequest request = buildRequest(systemPrompt, userMessage, true);
            Request httpRequest = buildHttpRequest(request);

            okHttpClient.newCall(httpRequest).enqueue(new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    log.warn("OpenAI 兼容 API 流式调用连接失败: {}", e.getMessage(), e);
                    callback.onError(e);
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    ResponseBody responseBody = response.body();
                    if (!response.isSuccessful() || responseBody == null) {
                        String errorMsg = "HTTP " + response.code();
                        if (responseBody != null) {
                            errorMsg += " " + responseBody.string();
                        }
                        callback.onError(new IOException(errorMsg));
                        return;
                    }

                    StringBuilder fullContent = new StringBuilder();
                    try (BufferedReader reader = new BufferedReader(
                            new InputStreamReader(responseBody.byteStream(), StandardCharsets.UTF_8))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            if (line.startsWith("data: ")) {
                                String data = line.substring(6).trim();
                                // @PRD: US3 — AC3.1 — SSE 流式解析，检测 [DONE] 终止符
                                if ("[DONE]".equals(data)) {
                                    callback.onComplete(fullContent.toString());
                                    return;
                                }
                                try {
                                    OpenAiCompletionResponse chunk =
                                            JsonUtil.toObj(data, OpenAiCompletionResponse.class);
                                    if (chunk == null) {
                                        continue;
                                    }
                                    if (chunk.hasError()) {
                                        String errorInfo = chunk.getErrorMessage();
                                        log.warn("OpenAI 兼容 API SSE 返回错误: {}", errorInfo);
                                        callback.onError(new IOException(errorInfo));
                                        return;
                                    }
                                    if (chunk.getChoices() != null && !chunk.getChoices().isEmpty()
                                            && chunk.getChoices().get(0).getDelta() != null) {
                                        String content = chunk.getChoices().get(0).getDelta().getContent();
                                        if (StringUtils.isNotBlank(content)) {
                                            fullContent.append(content);
                                            callback.onMessage(content);
                                        }
                                    }
                                } catch (Exception e) {
                                    log.warn("解析 SSE 数据块失败: {}", data, e);
                                }
                            }
                        }
                        // 正常读完但没有收到 [DONE]
                        callback.onComplete(fullContent.toString());
                    } catch (Exception e) {
                        log.warn("读取 SSE 流失败: {}", e.getMessage(), e);
                        callback.onError(e);
                    }
                }
            });
        } catch (Exception e) {
            log.warn("OpenAI 兼容 API 流式调用失败: {}", e.getMessage(), e);
            callback.onError(e);
        }
    }

    /**
     * 发送一个最小健康探测请求（同步，极简消息）
     *
     * @return true 表示 API 响应正常
     */
    public boolean healthCheck() {
        try {
            // 构建极简请求：一条短消息，非流式
            OpenAiCompletionRequest request = new OpenAiCompletionRequest();
            request.setModel(modelName);
            request.setMessages(List.of(
                    new OpenAiCompletionRequest.OpenAiMessage("user", "hi")
            ));
            request.setStream(false);
            request.setMax_tokens(10);

            Request httpRequest = buildHttpRequest(request);
            try (Response response = okHttpClient.newCall(httpRequest).execute()) {
                return response.isSuccessful();
            }
        } catch (Exception e) {
            log.warn("健康探测请求失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 构建 OpenAI 兼容的请求体
     */
    private OpenAiCompletionRequest buildRequest(String systemPrompt, String userMessage, boolean stream) {
        OpenAiCompletionRequest request = new OpenAiCompletionRequest();
        request.setModel(modelName);
        request.setStream(stream);
        request.setMax_tokens(maxTokens);
        if (StringUtils.isNotBlank(systemPrompt)) {
            request.setMessages(List.of(
                    new OpenAiCompletionRequest.OpenAiMessage("system", systemPrompt),
                    new OpenAiCompletionRequest.OpenAiMessage("user", userMessage)
            ));
        } else {
            request.setMessages(List.of(
                    new OpenAiCompletionRequest.OpenAiMessage("user", userMessage)
            ));
        }
        return request;
    }

    /**
     * 构建 HTTP 请求
     */
    private Request buildHttpRequest(OpenAiCompletionRequest body) {
        RequestBody requestBody = RequestBody.create(
                JsonUtil.toStr(body),
                MediaType.parse("application/json; charset=utf-8")
        );
        return new Request.Builder()
                .url(apiHost)
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .post(requestBody)
                .build();
    }

    // ==================== OpenAI 兼容 DTO ====================

    @Data
    public static class OpenAiCompletionRequest {
        private String model;
        private List<OpenAiMessage> messages;
        private boolean stream;
        private Integer max_tokens;

        @Data
        @AllArgsConstructor
        public static class OpenAiMessage {
            private String role;
            private String content;
        }
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class OpenAiCompletionResponse {
        private List<OpenAiChoice> choices;
        private Integer code;
        private String message;
        private String sid;

        public boolean hasError() {
            return code != null && code != 0;
        }

        public String getErrorMessage() {
            return "code=" + code + ", message=" + message + ", sid=" + sid;
        }

        @Data
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class OpenAiChoice {
            private OpenAiResponseMessage message;
            private OpenAiResponseDelta delta;
        }

        @Data
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class OpenAiResponseMessage {
            private String role;
            private String content;
        }

        @Data
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class OpenAiResponseDelta {
            private String content;
        }
    }
}

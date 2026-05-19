package com.github.paicoding.forum.service.chatai.service.impl.xunfei;

import com.github.paicoding.forum.api.model.enums.ChatAnswerTypeEnum;
import com.github.paicoding.forum.api.model.vo.chat.ChatItemVo;
import com.github.paicoding.forum.core.util.JsonUtil;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 讯飞星火大模型 OpenAI 兼容接口集成
 * <p>
 * 使用 OpenAI 兼容的 HTTP REST API 替代原有的 WebSocket 私有协议，
 * 便于后续接入更多 OpenAI 兼容的 AI 提供商。
 *
 * @author XuYifei
 * @date 2024-07-12
 */
@Slf4j
@Component
public class XunFeiIntegration {

    @Autowired
    private XunFeiConfig xunFeiConfig;

    private OkHttpClient okHttpClient;

    private OkHttpClient getOkHttpClient() {
        if (okHttpClient == null) {
            synchronized (this) {
                if (okHttpClient == null) {
                    okHttpClient = new OkHttpClient.Builder()
                            .connectTimeout(xunFeiConfig.getTimeOut(), TimeUnit.SECONDS)
                            .readTimeout(xunFeiConfig.getTimeOut(), TimeUnit.SECONDS)
                            .writeTimeout(xunFeiConfig.getTimeOut(), TimeUnit.SECONDS)
                            .build();
                }
            }
        }
        return okHttpClient;
    }

    /**
     * 同步提问，直接返回 AI 回答
     *
     * @param userId 用户ID
     * @param chat   聊天项
     * @return true 表示成功，false 表示失败
     */
    public boolean directReturn(Long userId, ChatItemVo chat) {
        try {
            OpenAiCompletionRequest request = buildRequest(chat.getQuestion(), false);
            Request httpRequest = buildHttpRequest(request);

            try (Response response = getOkHttpClient().newCall(httpRequest).execute()) {
                if (!response.isSuccessful()) {
                    String errorBody = response.body() != null ? response.body().string() : "";
                    log.warn("讯飞API返回错误, HTTP code={}, body={}", response.code(), errorBody);
                    chat.initAnswer("讯飞AI返回错误: HTTP " + response.code());
                    return false;
                }
                String bodyStr = response.body().string();
                OpenAiCompletionResponse result = JsonUtil.toObj(bodyStr, OpenAiCompletionResponse.class);
                if (result.getChoices() != null && !result.getChoices().isEmpty()
                        && result.getChoices().get(0).getMessage() != null) {
                    String content = result.getChoices().get(0).getMessage().getContent();
                    chat.initAnswer(content, ChatAnswerTypeEnum.TEXT);
                    return true;
                }
                chat.initAnswer("讯飞AI返回为空");
                return false;
            }
        } catch (Exception e) {
            log.warn("讯飞同步调用失败: {}", e.getMessage(), e);
            chat.initAnswer("讯飞AI连接失败: " + e.getMessage());
            return false;
        }
    }

    /**
     * 流式提问，通过 SSE 逐块返回 AI 回答
     *
     * @param userId   用户ID
     * @param chat     聊天项
     * @param callback 流式回调接口
     */
    public void streamReturn(Long userId, ChatItemVo chat, StreamCallback callback) {
        try {
            OpenAiCompletionRequest request = buildRequest(chat.getQuestion(), true);
            Request httpRequest = buildHttpRequest(request);

            getOkHttpClient().newCall(httpRequest).enqueue(new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    log.warn("讯飞流式调用连接失败: {}", e.getMessage(), e);
                    callback.onError(e, null);
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    ResponseBody responseBody = response.body();
                    if (!response.isSuccessful() || responseBody == null) {
                        String errorMsg = "HTTP " + response.code();
                        if (responseBody != null) {
                            errorMsg += " " + responseBody.string();
                        }
                        callback.onError(new IOException(errorMsg), errorMsg);
                        return;
                    }
                    try (BufferedReader reader = new BufferedReader(
                            new InputStreamReader(responseBody.byteStream(), StandardCharsets.UTF_8))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            if (line.startsWith("data: ")) {
                                String data = line.substring(6);
                                if ("[DONE]".equals(data.trim())) {
                                    callback.onComplete();
                                    return;
                                }
                                try {
                                    OpenAiCompletionResponse chunk =
                                            JsonUtil.toObj(data, OpenAiCompletionResponse.class);
                                    if (chunk.hasError()) {
                                        // 检测到 API 返回的错误响应
                                        String errorInfo = chunk.getErrorMessage();
                                        log.warn("讯飞SSE返回错误: {}", errorInfo);
                                        callback.onError(new IOException(errorInfo), errorInfo);
                                        return;
                                    }
                                    if (chunk.getChoices() != null && !chunk.getChoices().isEmpty()
                                            && chunk.getChoices().get(0).getDelta() != null) {
                                        String content = chunk.getChoices().get(0).getDelta().getContent();
                                        if (StringUtils.isNotBlank(content)) {
                                            callback.onMessage(content);
                                        }
                                    }
                                } catch (Exception e) {
                                    log.warn("解析SSE数据块失败: {}", data, e);
                                }
                            }
                        }
                        // 正常读完但没有收到 [DONE]
                        callback.onComplete();
                    } catch (Exception e) {
                        callback.onError(e, null);
                    }
                }
            });
        } catch (Exception e) {
            log.warn("讯飞流式调用失败: {}", e.getMessage(), e);
            callback.onError(e, null);
        }
    }

    /**
     * 构建 OpenAI 兼容的请求体
     */
    private OpenAiCompletionRequest buildRequest(String question, boolean stream) {
        OpenAiCompletionRequest request = new OpenAiCompletionRequest();
        request.setModel(xunFeiConfig.getModel());
        request.setMessages(List.of(
                new OpenAiCompletionRequest.OpenAiMessage("user", question)
        ));
        request.setStream(stream);
        request.setMax_tokens(xunFeiConfig.getMaxToken());
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
                .url(xunFeiConfig.getApiHost())
                .header("Authorization", "Bearer " + xunFeiConfig.getApiKey())
                .header("Content-Type", "application/json")
                .post(requestBody)
                .build();
    }

    // ==================== 配置类 ====================

    @Component
    @ConfigurationProperties(prefix = "xunfei")
    @Data
    public static class XunFeiConfig {
        /**
         * OpenAI 兼容的 API 地址
         */
        private String apiHost = "https://spark-api-open.xf-yun.com/v1/chat/completions";
        /**
         * API Key，用作 Bearer Token
         */
        private String apiKey = "";
        /**
         * 模型名称
         */
        private String model = "general";
        /**
         * 最大返回 token 数
         */
        private int maxToken = 4096;
        /**
         * 超时时间（秒）
         */
        private int timeOut = 300;
        /**
         * 是否使用代理
         */
        private boolean proxy = false;
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
        /**
         * 错误码，0 表示正常，非 0 表示出错
         */
        private Integer code;
        /**
         * 错误信息
         */
        private String message;
        /**
         * 会话 ID
         */
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

    // ==================== 流式回调接口 ====================

    public interface StreamCallback {
        void onMessage(String message);
        void onComplete();
        void onError(Throwable throwable, String response);
    }
}

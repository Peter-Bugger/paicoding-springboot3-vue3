package com.github.paicoding.forum.service.chatai.service.ai;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * AI 解读功能配置类
 * <p>
 * 绑定 YAML 配置前缀 "ai-interpret"，支持多模型配置。
 * 新增模型仅需在 YAML 中追加一条 ModelConf 配置，无需修改 Java 代码。
 *
 * @author XuYifei
 * @date 2024-07-12
 */
// @PRD: US4 — AC4.4 — 新增配置，自动加入可用池
@Component
@ConfigurationProperties(prefix = "ai-interpret")
@Data
public class AiModelConfig {

    /**
     * 功能总开关
     */
    private boolean enabled = true;

    /**
     * 每个用户每日最大解读请求次数
     */
    private int maxDailyRequests = 50;

    /**
     * 模型配置列表
     */
    private List<ModelConf> models = new ArrayList<>();

    @Data
    public static class ModelConf {
        /**
         * 模型唯一标识，如 "deepseek"
         */
        private String name;

        /**
         * 模型显示名称，如 "DeepSeek V3"
         */
        private String displayName;

        /**
         * 优先级：数值越小优先级越高，0 为最高
         */
        private int priority;

        /**
         * OpenAI 兼容 API 地址
         */
        private String apiHost;

        /**
         * API Key
         */
        private String apiKey;

        /**
         * 模型名称，如 "deepseek-chat"
         */
        private String modelName;

        /**
         * 最大返回 token 数
         */
        private int maxTokens = 4096;

        /**
         * 超时时间（秒）
         */
        private int timeoutSeconds = 30;

        /**
         * 是否启用
         */
        private boolean enabled = true;
    }
}

package com.github.paicoding.forum.service.chatai.service;

import com.github.paicoding.forum.api.model.vo.article.dto.ArticleDTO;
import com.github.paicoding.forum.service.article.service.ArticleReadService;
import com.github.paicoding.forum.service.chatai.service.ai.AiModelConfig;
import com.github.paicoding.forum.service.chatai.service.ai.StreamCallback;
import com.github.paicoding.forum.service.chatai.service.impl.xunfei.XunFeiIntegration;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;

/**
 * 文章 AI 解读服务
 * <p>
 * 核心流程：
 * 1. 日限额检查（Redis 计数器）
 * 2. 获取文章内容（使用 queryDetailArticleInfo 避免阅读计数+1副作用）
 * 3. 提取选中文本前后各500字符的上下文
 * 4. Token 估算与截断（优先保留选中文本）
 * 5. 构建提示词并通过讯飞 AI 流式解读
 *
 * @author XuYifei
 * @date 2024-07-12
 */
// @PRD: US2 — AC2.1 — 后端构建请求，使用硬编码提示词
// @PRD: US2 — AC2.2 — 构建请求，选中文本+前后500字+标题
// @PRD: US2 — AC2.3 — 固定提示词硬编码
// @PRD: US2 — AC2.4 — 超 token 限制，优先保留选中文本截断
@Slf4j
@Service
public class ArticleInterpretService {

    /**
     * 固定系统提示词 — 硬编码，不可配置
     */
    static final String SYSTEM_PROMPT =
            "你是文章专属解读助手，只能基于提供的原文解释选中片段，不准脱离原文，不准编造内容，语言通俗易懂，条理清晰。";

    /**
     * 用户消息模板
     */
    static final String USER_PROMPT_TEMPLATE =
            "文章标题: %s\n\n上下文（选中片段前后各500字符）：\n%s\n\n请解读以下原文片段：\n%s";

    /**
     * 上下文窗口大小（单侧字符数）
     */
    static final int CONTEXT_WINDOW_SIZE = 500;

    /**
     * 默认模型 max_tokens（兜底值）
     */
    static final int DEFAULT_MAX_TOKENS = 4096;

    @Autowired
    private XunFeiIntegration xunFeiIntegration;

    @Autowired
    private ArticleReadService articleReadService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private AiModelConfig aiModelConfig;

    /**
     * 解读指定文章中的选中文本
     *
     * @param articleId    文章 ID
     * @param userId       用户 ID
     * @param selectedText 用户选中的文本
     * @param startPos     选中文本在文章中的起始字符偏移
     * @param endPos       选中文本在文章中的结束字符偏移
     * @param callback     流式回调
     */
    // @PRD: US2 — AC2.2 — 提取上下文，startPos/endPos精确定位（修复 indexOf 重复文本问题）
    public void interpret(Long articleId, Long userId, String selectedText,
                          Integer startPos, Integer endPos, StreamCallback callback) {
        // 1. 日限额检查
        // @PRD: AC1.6, US1 — 未登录 → 弹出登录提示（通过 userId == null 由上游处理）
        if (userId == null) {
            callback.onError(new RuntimeException("请先登录"));
            return;
        }
        if (!checkDailyLimit(userId)) {
            callback.onError(new RuntimeException("今日解读次数已用完"));
            return;
        }

        // 2. 获取文章内容
        ArticleDTO article = articleReadService.queryDetailArticleInfo(articleId);
        // @PRD: US1 — AC1.3 — 文章内容为空
        if (article == null || StringUtils.isBlank(article.getContent())) {
            callback.onError(new RuntimeException("文章内容为空"));
            return;
        }

        // 3. 提取上下文（前后各500字符），使用 startPos 精确定位而非 indexOf
        // @PRD: US2 — AC2.4 — 上下文超长截断
        String context = extractContext(article.getContent(), selectedText, startPos);

        // 4. 构建用户消息
        String userMessage = buildUserMessage(context, selectedText, article.getTitle());

        // 5. Token 估算与截断（优先保留选中文本）
        // @PRD: US2 — AC2.4 — 上下文超长截断
        int totalTokens = estimateTokens(SYSTEM_PROMPT) + estimateTokens(userMessage);
        int modelMaxTokens = Math.max(DEFAULT_MAX_TOKENS, xunFeiIntegration.getMaxToken());
        if (totalTokens > modelMaxTokens) {
            log.warn("Prompt token {} 超过模型限制 {}, 截断上下文", totalTokens, modelMaxTokens);
            int excessTokens = totalTokens - modelMaxTokens;
            int charsToRemove = Math.min(context.length() - selectedText.length() - 2,
                    excessTokens * 2);
            int halfRemove = Math.max(0, charsToRemove / 2);
            int prefixLen = CONTEXT_WINDOW_SIZE - halfRemove;
            if (startPos != null) {
                int ctxStart = Math.max(0, Math.max(0, startPos) - Math.max(CONTEXT_WINDOW_SIZE, prefixLen));
                int ctxEnd = Math.min(article.getContent().length(),
                        Math.max(0, startPos) + selectedText.length() + Math.max(CONTEXT_WINDOW_SIZE, prefixLen));
                context = article.getContent().substring(ctxStart, ctxEnd);
            } else {
                int idx = article.getContent().indexOf(selectedText);
                if (idx >= 0) {
                    int newWindow = Math.max(50, prefixLen);
                    int ctxStart = Math.max(0, idx - newWindow);
                    int ctxEnd = Math.min(article.getContent().length(), idx + selectedText.length() + newWindow);
                    context = article.getContent().substring(ctxStart, ctxEnd);
                }
            }
            userMessage = buildUserMessage(context, selectedText, article.getTitle());
        }

        // 6. 通过讯飞 AI 进行流式解读
        final String finalUserMessage = userMessage;
        StringBuilder fullContent = new StringBuilder();
        xunFeiIntegration.streamReturn(SYSTEM_PROMPT, finalUserMessage,
                new XunFeiIntegration.StreamCallback() {
                    @Override
                    public void onMessage(String message) {
                        fullContent.append(message);
                        callback.onMessage(message);
                    }

                    @Override
                    public void onComplete() {
                        callback.onComplete(fullContent.toString());
                    }

                    @Override
                    public void onError(Throwable throwable, String response) {
                        log.warn("讯飞文章解读失败: {}", throwable.getMessage(), throwable);
                        callback.onError(throwable);
                    }
                });
    }

    /**
     * 从文章内容中提取选中文本前后各 CONTEXT_WINDOW_SIZE 字符的上下文
     * <p>
     * 使用 startPos 精确定位选中文本，避免 indexOf 因重复文本定位到第一个匹配。
     *
     * @param fullContent   文章完整内容
     * @param selectedText  用户选中的文本
     * @param startPos      选中文本起始字符偏移（可为 null，回退到 indexOf）
     * @return 截取的上下文
     */
    // @PRD: US2 — AC2.2 — 上下文提取，使用 startPos 精确定位（修复 indexOf 重复文本问题）
    String extractContext(String fullContent, String selectedText, Integer startPos) {
        if (StringUtils.isBlank(fullContent) || StringUtils.isBlank(selectedText)) {
            return "";
        }
        int idx;
        if (startPos != null && startPos >= 0 && startPos < fullContent.length()) {
            // 使用 startPos 精确定位
            String contentFromPos = fullContent.substring(startPos);
            int relIdx = contentFromPos.indexOf(selectedText);
            if (relIdx < 0) {
                // 选中文本不在 startPos 处，回退到全文 indexOf
                idx = fullContent.indexOf(selectedText);
            } else {
                idx = startPos + relIdx;
            }
        } else {
            idx = fullContent.indexOf(selectedText);
        }
        if (idx < 0) {
            // 选中文本不在内容中，可能被截断或格式不一致
            return "";
        }
        int start = Math.max(0, idx - CONTEXT_WINDOW_SIZE);
        int end = Math.min(fullContent.length(), idx + selectedText.length() + CONTEXT_WINDOW_SIZE);
        return fullContent.substring(start, end);
    }

    /**
     * 构建用户消息
     *
     * @param context      上下文
     * @param selectedText 选中的文本
     * @param title        文章标题
     * @return 格式化后的用户消息
     */
    private String buildUserMessage(String context, String selectedText, String title) {
        return String.format(USER_PROMPT_TEMPLATE,
                title != null ? title : "",
                context != null ? context : "",
                selectedText != null ? selectedText : "");
    }

    /**
     * Token 估算
     * 公式：1 中文字符 = 1.5 token，1 英文字母 = 0.25 token，1 英文单词 = 0.75 token
     *
     * @param text 输入文本
     * @return 估算的 token 数
     */
    int estimateTokens(String text) {
        if (text == null) {
            return 0;
        }
        int chineseChars = 0;
        int englishLetters = 0;
        boolean inWord = false;
        int englishWords = 0;

        for (char c : text.toCharArray()) {
            if (Character.UnicodeScript.of(c) == Character.UnicodeScript.HAN) {
                chineseChars++;
                if (inWord) {
                    inWord = false;
                }
            } else if (Character.isLetter(c)) {
                englishLetters++;
                if (!inWord) {
                    inWord = true;
                    englishWords++;
                }
            } else {
                if (inWord) {
                    inWord = false;
                }
            }
        }

        return (int) Math.ceil(chineseChars * 1.5 + englishLetters * 0.25 + englishWords * 0.75);
    }

    /**
     * Redis 日限额检查
     * Key 格式: interpret:limit:{userId}:{yyyy-MM-dd}
     * 限额值由 ai-interpret.max-daily-requests 配置，可运维调整。
     *
     * @param userId 用户 ID
     * @return true 表示未超限；false 表示今日次数已用完
     */
    // @PRD: US1 — AC1.6 — 日限额检查，使用配置值而非硬编码
    private boolean checkDailyLimit(Long userId) {
        String key = "interpret:limit:" + userId + ":" + LocalDate.now();
        Long cnt = redisTemplate.opsForValue().increment(key);
        if (cnt != null && cnt == 1) {
            redisTemplate.expire(key, Duration.ofDays(1));
        }
        return cnt != null && cnt <= aiModelConfig.getMaxDailyRequests();
    }
}

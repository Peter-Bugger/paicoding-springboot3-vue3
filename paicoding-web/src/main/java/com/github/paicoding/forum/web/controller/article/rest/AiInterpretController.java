package com.github.paicoding.forum.web.controller.article.rest;

import com.github.paicoding.forum.api.model.context.ReqInfoContext;
import com.github.paicoding.forum.api.model.vo.ResVo;
import com.github.paicoding.forum.api.model.vo.ai.InterpretReq;
import com.github.paicoding.forum.api.model.vo.ai.ModelInfoVO;
import com.github.paicoding.forum.service.chatai.service.ArticleInterpretService;
import com.github.paicoding.forum.service.chatai.service.ai.AiModelRouter;
import com.github.paicoding.forum.service.chatai.service.ai.StreamCallback;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;

/**
 * 文章 AI 解读控制器
 * <p>
 * 提供 SSE 流式解读接口和模型列表查询接口。
 *
 * @author XuYifei
 * @date 2024-07-12
 */
// @PRD: US1 — AC1.1 — 选中文本发送请求
// @PRD: US1 — AC1.6 — 未登录弹出登录提示
// @PRD: US3 — AC3.1 — SSE 流式返回
// @PRD: US3 — AC3.2 — 解读完成
// @PRD: US3 — AC3.3 — 超时处理
@Slf4j
@RestController
@RequestMapping(path = "article/api/ai")
public class AiInterpretController {

    @Autowired
    private ArticleInterpretService articleInterpretService;

    @Autowired
    private AiModelRouter aiModelRouter;

    /**
     * 文章 AI 解读（流式 SSE 返回）
     *
     * @param req 解读请求
     * @return SSE 事件流
     */
    @PostMapping(path = "interpret", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter interpret(@RequestBody InterpretReq req) {
        // 从请求上下文获取当前用户 ID
        Long userId = ReqInfoContext.getReqInfo() != null
                ? ReqInfoContext.getReqInfo().getUserId()
                : null;

        // 未登录时直接返回错误
        if (userId == null) {
            SseEmitter emitter = new SseEmitter(0L);
            try {
                emitter.send(SseEmitter.event().data(
                        "{\"type\":\"ERROR\",\"message\":\"请先登录\"}"));
            } catch (IOException e) {
                // ignore
            }
            emitter.completeWithError(new RuntimeException("请先登录"));
            return emitter;
        }

        // 超时 30 秒
        SseEmitter emitter = new SseEmitter(600_000L);

        // 异步执行解读（传入 startPos/endPos 精确定位选中文本）
        // @PRD: US2 — AC2.2 — 传入 startPos/endPos 避免重复文本定位错误
        articleInterpretService.interpret(req.getArticleId(), userId, req.getSelectedText(),
                req.getStartPos(), req.getEndPos(), new StreamCallback() {
                    @Override
                    public void onMessage(String content) {
                        try {
                            emitter.send(SseEmitter.event().data(
                                    "{\"type\":\"STREAM\",\"content\":\"" + escapeJson(content) + "\"}"));
                        } catch (IOException e) {
                            log.warn("SSE 发送消息失败: {}", e.getMessage());
                            emitter.completeWithError(e);
                        }
                    }

                    @Override
                    public void onComplete(String fullContent) {
                        try {
                            emitter.send(SseEmitter.event().data(
                                    "{\"type\":\"STREAM_END\",\"content\":\"" + escapeJson(fullContent) + "\"}"));
                        } catch (IOException e) {
                            log.warn("SSE 发送完成事件失败: {}", e.getMessage());
                        }
                        emitter.complete();
                    }

                    @Override
                    public void onError(Throwable error) {
                        try {
                            emitter.send(SseEmitter.event().data(
                                    "{\"type\":\"ERROR\",\"message\":\"" + escapeJson(error.getMessage()) + "\"}"));
                        } catch (IOException e) {
                            // ignore
                        }
                        emitter.completeWithError(error);
                    }

                    private String escapeJson(String s) {
                        if (s == null) {
                            return "";
                        }
                        return s.replace("\\", "\\\\")
                                .replace("\"", "\\\"")
                                .replace("\n", "\\n")
                                .replace("\r", "\\r")
                                .replace("\t", "\\t");
                    }
                });

        // 超时回调
        emitter.onTimeout(() -> {
            log.warn("AI 解读 SSE 超时, articleId={}", req.getArticleId());
            try {
                emitter.send(SseEmitter.event().data(
                        "{\"type\":\"ERROR\",\"message\":\"AI响应超时，请稍后重试\"}"));
            } catch (IOException e) {
                // ignore
            }
            emitter.complete();
        });

        // 异常回调
        emitter.onError(throwable -> {
            log.warn("AI 解读 SSE 异常: {}", throwable.getMessage());
        });

        return emitter;
    }

    /**
     * 获取可用模型列表
     *
     * @return 模型信息列表
     */
    @GetMapping(path = "models")
    public ResVo<List<ModelInfoVO>> listModels() {
        return ResVo.ok(aiModelRouter.listAvailableModels());
    }
}

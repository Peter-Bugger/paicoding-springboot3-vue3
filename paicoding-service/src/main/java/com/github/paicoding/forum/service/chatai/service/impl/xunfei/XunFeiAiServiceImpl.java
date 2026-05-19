package com.github.paicoding.forum.service.chatai.service.impl.xunfei;

import com.github.paicoding.forum.api.model.enums.ChatAnswerTypeEnum;
import com.github.paicoding.forum.api.model.enums.ai.AISourceEnum;
import com.github.paicoding.forum.api.model.enums.ai.AiChatStatEnum;
import com.github.paicoding.forum.api.model.vo.chat.ChatItemVo;
import com.github.paicoding.forum.api.model.vo.chat.ChatRecordsVo;
import com.github.paicoding.forum.service.chatai.service.AbsChatService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.function.BiConsumer;

/**
 * 讯飞星火大模型 - OpenAI 兼容接口实现
 * <p>
 * 使用 OpenAI 兼容的 HTTP API（同步 + SSE 流式）替代原有的 WebSocket 私有协议。
 *
 * @author XuYifei
 * @date 2024-07-12
 */
@Slf4j
@Service
public class XunFeiAiServiceImpl extends AbsChatService {

    @Autowired
    private XunFeiIntegration xunFeiIntegration;

    @Override
    public AISourceEnum source() {
        return AISourceEnum.XUN_FEI_AI;
    }

    @Override
    public boolean asyncFirst() {
        return true;
    }

    /**
     * 同步提问
     */
    @Override
    public AiChatStatEnum doAnswer(Long user, ChatItemVo chat) {
        if (xunFeiIntegration.directReturn(user, chat)) {
            return AiChatStatEnum.END;
        }
        return AiChatStatEnum.ERROR;
    }

    /**
     * 异步流式提问
     */
    @Override
    public AiChatStatEnum doAsyncAnswer(Long user, ChatRecordsVo chatRes,
            BiConsumer<AiChatStatEnum, ChatRecordsVo> consumer) {
        ChatItemVo item = chatRes.getRecords().get(0);

        xunFeiIntegration.streamReturn(user, item, new XunFeiIntegration.StreamCallback() {
            @Override
            public void onMessage(String message) {
                item.appendAnswer(message);
                consumer.accept(AiChatStatEnum.MID, chatRes);
            }

            @Override
            public void onComplete() {
                item.appendAnswer("\n")
                        .setAnswerType(ChatAnswerTypeEnum.STREAM_END);
                consumer.accept(AiChatStatEnum.END, chatRes);
            }

            @Override
            public void onError(Throwable throwable, String response) {
                String errorMsg = "Error:" + (StringUtils.isBlank(response)
                        ? throwable.getMessage() : response);
                item.appendAnswer(errorMsg)
                        .setAnswerType(ChatAnswerTypeEnum.STREAM_END);
                consumer.accept(AiChatStatEnum.ERROR, chatRes);
            }
        });
        return AiChatStatEnum.IGNORE;
    }
}

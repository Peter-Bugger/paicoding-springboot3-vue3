package com.github.paicoding.forum.api.model.vo.msg;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * 发送消息响应
 *
 * @author Mr.Bu
 * @date 2026-05-08
 */
@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class SendMsgRes {

    /**
     * 会话ID
     */
    private Long conversationId;

    /**
     * 消息ID
     */
    private Long messageId;
}

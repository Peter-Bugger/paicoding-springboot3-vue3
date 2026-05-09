package com.github.paicoding.forum.api.model.vo.msg;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 发送消息请求
 *
 * @author Mr.Bu
 * @date 2026-05-08
 */
@Data
@Accessors(chain = true)
public class SendMsgReq {

    /**
     * 目标用户ID
     */
    private Long toUserId;

    /**
     * 消息内容
     */
    private String content;
}

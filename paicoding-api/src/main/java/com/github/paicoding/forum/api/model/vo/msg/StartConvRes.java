package com.github.paicoding.forum.api.model.vo.msg;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * 创建或获取会话响应
 *
 * @author Mr.Bu
 * @date 2026-05-08
 */
@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class StartConvRes {

    /**
     * 会话ID
     */
    private Long conversationId;
}

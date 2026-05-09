package com.github.paicoding.forum.api.model.vo.msg;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 创建或获取会话请求
 *
 * @author Mr.Bu
 * @date 2026-05-08
 */
@Data
@Accessors(chain = true)
public class StartConvReq {

    /**
     * 目标用户ID
     */
    private Long toUserId;
}

package com.github.paicoding.forum.api.model.vo.msg;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * 未读消息总数响应
 *
 * @author Mr.Bu
 * @date 2026-05-08
 */
@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class UnreadCountRes {

    /**
     * 未读消息总数
     */
    private Integer totalUnread;
}

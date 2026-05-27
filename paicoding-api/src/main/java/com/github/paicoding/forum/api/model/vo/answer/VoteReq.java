package com.github.paicoding.forum.api.model.vo.answer;

import lombok.Data;

/**
 * @author Mr.Bu
 * @date 2026-05-27
 */
@Data
public class VoteReq {
    /**
     * 回答ID
     */
    private Long answerId;

    /**
     * 投票类型: 1-赞同, 2-反对
     */
    private Integer voteType;
}

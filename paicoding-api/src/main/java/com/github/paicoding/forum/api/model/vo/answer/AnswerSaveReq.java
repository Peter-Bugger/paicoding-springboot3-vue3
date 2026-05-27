package com.github.paicoding.forum.api.model.vo.answer;

import lombok.Data;

/**
 * @author Mr.Bu
 * @date 2026-05-27
 */
@Data
public class AnswerSaveReq {
    /**
     * 问题ID
     */
    private Long articleId;

    /**
     * 回答内容
     */
    private String content;

    /**
     * 回答ID（更新时传入）
     */
    private Long answerId;
}

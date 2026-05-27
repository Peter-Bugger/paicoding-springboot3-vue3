package com.github.paicoding.forum.service.answer.repository.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.github.paicoding.forum.api.model.entity.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 回答投票记录表
 *
 * @author Mr.Bu
 * @date 2026-05-27
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("answer_vote")
public class AnswerVoteDO extends BaseDO {
    private static final long serialVersionUID = 1L;

    /**
     * 回答ID
     */
    private Long answerId;

    /**
     * 投票用户ID
     */
    private Long userId;

    /**
     * 投票类型 1-赞同 2-反对
     */
    private Integer voteType;
}

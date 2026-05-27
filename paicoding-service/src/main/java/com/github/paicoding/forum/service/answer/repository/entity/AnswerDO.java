package com.github.paicoding.forum.service.answer.repository.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.github.paicoding.forum.api.model.entity.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 问答回答表
 *
 * @author Mr.Bu
 * @date 2026-05-27
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("answer")
public class AnswerDO extends BaseDO {
    private static final long serialVersionUID = 1L;

    /**
     * 问题ID
     */
    private Long articleId;

    /**
     * 回答用户ID
     */
    private Long userId;

    /**
     * 回答内容（Markdown）
     */
    private String content;

    /**
     * 赞同数
     */
    private Integer voteUpCount;

    /**
     * 反对数
     */
    private Integer voteDownCount;

    /**
     * 是否被采纳 0-未采纳 1-已采纳
     */
    private Integer accepted;

    /**
     * 是否删除 0-未删除 1-已删除
     */
    private Integer deleted;
}

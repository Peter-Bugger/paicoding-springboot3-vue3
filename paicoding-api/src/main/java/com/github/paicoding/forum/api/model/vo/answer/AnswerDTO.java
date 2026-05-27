package com.github.paicoding.forum.api.model.vo.answer;

import com.github.paicoding.forum.api.model.vo.user.dto.BaseUserInfoDTO;
import lombok.Data;

import java.util.Date;

/**
 * @author Mr.Bu
 * @date 2026-05-27
 */
@Data
public class AnswerDTO {
    /**
     * 回答ID
     */
    private Long answerId;

    /**
     * 问题ID
     */
    private Long articleId;

    /**
     * 回答内容
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
     * 是否被采纳
     */
    private Boolean accepted;

    /**
     * 当前用户是否已赞同
     */
    private Boolean votedUp;

    /**
     * 当前用户是否已反对
     */
    private Boolean votedDown;

    /**
     * 回答用户信息
     */
    private BaseUserInfoDTO user;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;
}

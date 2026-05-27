package com.github.paicoding.forum.service.answer.service;

import com.github.paicoding.forum.api.model.vo.PageParam;
import com.github.paicoding.forum.api.model.vo.PageVo;
import com.github.paicoding.forum.api.model.vo.answer.AnswerDTO;
import com.github.paicoding.forum.api.model.vo.answer.AnswerSaveReq;
import com.github.paicoding.forum.api.model.vo.answer.VoteReq;

/**
 * @author Mr.Bu
 * @date 2026-05-27
 */
public interface AnswerService {

    /**
     * 发布/更新回答
     */
    Long saveAnswer(AnswerSaveReq req, Long userId);

    /**
     * 删除回答
     */
    void deleteAnswer(Long answerId, Long userId);

    /**
     * 获取问题的回答列表
     */
    PageVo<AnswerDTO> listAnswers(Long articleId, PageParam pageParam, Long currentUserId);

    /**
     * 采纳回答
     */
    void acceptAnswer(Long answerId, Long userId);

    /**
     * 投票（赞同/反对）
     */
    void vote(VoteReq req, Long userId);

    /**
     * 获取单个回答详情
     */
    AnswerDTO getAnswer(Long answerId, Long currentUserId);
}

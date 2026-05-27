package com.github.paicoding.forum.service.answer.repository.dao;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.paicoding.forum.service.answer.repository.entity.AnswerVoteDO;
import com.github.paicoding.forum.service.answer.repository.mapper.AnswerVoteMapper;
import org.springframework.stereotype.Repository;

/**
 * @author Mr.Bu
 * @date 2026-05-27
 */
@Repository
public class AnswerVoteDao extends ServiceImpl<AnswerVoteMapper, AnswerVoteDO> {

    /**
     * 查询用户对某个回答的投票记录
     */
    public AnswerVoteDO getVoteRecord(Long answerId, Long userId) {
        return lambdaQuery()
                .eq(AnswerVoteDO::getAnswerId, answerId)
                .eq(AnswerVoteDO::getUserId, userId)
                .one();
    }
}

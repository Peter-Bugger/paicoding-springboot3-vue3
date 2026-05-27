package com.github.paicoding.forum.service.answer.repository.dao;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.paicoding.forum.api.model.vo.PageParam;
import com.github.paicoding.forum.service.answer.repository.entity.AnswerDO;
import com.github.paicoding.forum.service.answer.repository.mapper.AnswerMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author Mr.Bu
 * @date 2026-05-27
 */
@Repository
public class AnswerDao extends ServiceImpl<AnswerMapper, AnswerDO> {

    /**
     * 获取问题的回答列表，按采纳+投票排序
     */
    public List<AnswerDO> listAnswersByArticleId(Long articleId, PageParam pageParam) {
        return lambdaQuery()
                .eq(AnswerDO::getArticleId, articleId)
                .eq(AnswerDO::getDeleted, 0)
                .orderByDesc(AnswerDO::getAccepted)
                .orderByDesc(AnswerDO::getVoteUpCount)
                .orderByAsc(AnswerDO::getCreateTime)
                .last(PageParam.getLimitSql(pageParam))
                .list();
    }

    /**
     * 获取问题的回答总数
     */
    public long countAnswersByArticleId(Long articleId) {
        return lambdaQuery()
                .eq(AnswerDO::getArticleId, articleId)
                .eq(AnswerDO::getDeleted, 0)
                .count();
    }

    /**
     * 获取用户的回答列表
     */
    public List<AnswerDO> listAnswersByUserId(Long userId, PageParam pageParam) {
        return lambdaQuery()
                .eq(AnswerDO::getUserId, userId)
                .eq(AnswerDO::getDeleted, 0)
                .orderByDesc(AnswerDO::getId)
                .last(PageParam.getLimitSql(pageParam))
                .list();
    }

    /**
     * 获取问题的已采纳回答
     */
    public AnswerDO getAcceptedAnswer(Long articleId) {
        return lambdaQuery()
                .eq(AnswerDO::getArticleId, articleId)
                .eq(AnswerDO::getAccepted, 1)
                .eq(AnswerDO::getDeleted, 0)
                .one();
    }

    /**
     * 取消该问题下所有已采纳的回答
     */
    public void cancelAcceptedAnswers(Long articleId) {
        List<AnswerDO> answers = lambdaQuery()
                .eq(AnswerDO::getArticleId, articleId)
                .eq(AnswerDO::getAccepted, 1)
                .list();
        for (AnswerDO answer : answers) {
            answer.setAccepted(0);
            updateById(answer);
        }
    }
}

package com.github.paicoding.forum.service.answer.service.impl;

import com.github.paicoding.forum.api.model.context.ReqInfoContext;
import com.github.paicoding.forum.api.model.enums.NotifyStatEnum;
import com.github.paicoding.forum.api.model.enums.NotifyTypeEnum;
import com.github.paicoding.forum.api.model.exception.ExceptionUtil;
import com.github.paicoding.forum.api.model.vo.PageParam;
import com.github.paicoding.forum.api.model.vo.PageVo;
import com.github.paicoding.forum.api.model.vo.answer.AnswerDTO;
import com.github.paicoding.forum.api.model.vo.answer.AnswerSaveReq;
import com.github.paicoding.forum.api.model.vo.answer.VoteReq;
import com.github.paicoding.forum.api.model.vo.constants.StatusEnum;
import com.github.paicoding.forum.api.model.vo.notify.NotifyMsgEvent;
import com.github.paicoding.forum.api.model.vo.user.dto.BaseUserInfoDTO;
import com.github.paicoding.forum.core.util.SpringUtil;
import com.github.paicoding.forum.service.answer.repository.dao.AnswerDao;
import com.github.paicoding.forum.service.answer.repository.dao.AnswerVoteDao;
import com.github.paicoding.forum.service.answer.repository.entity.AnswerDO;
import com.github.paicoding.forum.service.answer.repository.entity.AnswerVoteDO;
import com.github.paicoding.forum.service.answer.service.AnswerService;
import com.github.paicoding.forum.service.article.repository.entity.ArticleDO;
import com.github.paicoding.forum.service.article.service.ArticleReadService;
import com.github.paicoding.forum.service.notify.repository.dao.NotifyMsgDao;
import com.github.paicoding.forum.service.notify.repository.entity.NotifyMsgDO;
import com.github.paicoding.forum.service.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Mr.Bu
 * @date 2026-05-27
 */
@Slf4j
@Service
public class AnswerServiceImpl implements AnswerService {

    @Autowired
    private AnswerDao answerDao;

    @Autowired
    private AnswerVoteDao answerVoteDao;

    @Autowired
    private ArticleReadService articleReadService;

    @Autowired
    private UserService userService;

    @Autowired
    private NotifyMsgDao notifyMsgDao;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long saveAnswer(AnswerSaveReq req, Long userId) {
        ArticleDO article = articleReadService.queryBasicArticle(req.getArticleId());
        if (article == null) {
            throw ExceptionUtil.of(StatusEnum.ARTICLE_NOT_EXISTS, req.getArticleId());
        }

        AnswerDO answer;
        if (req.getAnswerId() != null && req.getAnswerId() > 0) {
            answer = answerDao.getById(req.getAnswerId());
            if (answer == null || !Objects.equals(answer.getUserId(), userId)) {
                throw ExceptionUtil.of(StatusEnum.FORBID_ERROR_MIXED, "无权编辑该回答");
            }
            answer.setContent(req.getContent());
            answer.setUpdateTime(new Date());
            answerDao.updateById(answer);
        } else {
            answer = new AnswerDO();
            answer.setArticleId(req.getArticleId());
            answer.setUserId(userId);
            answer.setContent(req.getContent());
            answer.setVoteUpCount(0);
            answer.setVoteDownCount(0);
            answer.setAccepted(0);
            answer.setDeleted(0);
            answer.setCreateTime(new Date());
            answer.setUpdateTime(new Date());
            answerDao.save(answer);

            // 通知提问者有人回答了问题
            SpringUtil.publishEvent(new NotifyMsgEvent<>(this, NotifyTypeEnum.ANSWER, answer));
        }
        return answer.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteAnswer(Long answerId, Long userId) {
        AnswerDO answer = answerDao.getById(answerId);
        if (answer == null) {
            throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "回答不存在");
        }
        if (!Objects.equals(answer.getUserId(), userId)) {
            throw ExceptionUtil.of(StatusEnum.FORBID_ERROR_MIXED, "无权删除该回答");
        }
        answer.setDeleted(1);
        answer.setUpdateTime(new Date());
        answerDao.updateById(answer);
    }

    @Override
    public PageVo<AnswerDTO> listAnswers(Long articleId, PageParam pageParam, Long currentUserId) {
        List<AnswerDO> answerList = answerDao.listAnswersByArticleId(articleId, pageParam);
        long totalCount = answerDao.countAnswersByArticleId(articleId);

        List<AnswerDTO> result = answerList.stream()
                .map(answer -> buildAnswerDTO(answer, currentUserId))
                .collect(Collectors.toList());

        return PageVo.build(result, pageParam.getPageSize(), pageParam.getPageNum(), totalCount);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void acceptAnswer(Long answerId, Long userId) {
        AnswerDO answer = answerDao.getById(answerId);
        if (answer == null) {
            throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "回答不存在");
        }
        if (answer.getDeleted() == 1) {
            throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "回答已被删除");
        }

        // 验证当前用户是否是提问者
        ArticleDO article = articleReadService.queryBasicArticle(answer.getArticleId());
        if (article == null || !Objects.equals(article.getUserId(), userId)) {
            throw ExceptionUtil.of(StatusEnum.FORBID_ERROR_MIXED, "只有提问者才能采纳回答");
        }

        // 如果已经采纳，取消采纳
        if (answer.getAccepted() == 1) {
            answer.setAccepted(0);
            answer.setUpdateTime(new Date());
            answerDao.updateById(answer);
            return;
        }

        // 取消该问题下其他已采纳的回答
        answerDao.cancelAcceptedAnswers(answer.getArticleId());

        // 采纳当前回答
        answer.setAccepted(1);
        answer.setUpdateTime(new Date());
        answerDao.updateById(answer);

        // 通知回答者被采纳
        SpringUtil.publishEvent(new NotifyMsgEvent<>(this, NotifyTypeEnum.ACCEPT, answer));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void vote(VoteReq req, Long userId) {
        long answerId = req.getAnswerId();
        int voteType = req.getVoteType();

        AnswerDO answer = answerDao.getById(answerId);
        if (answer == null || answer.getDeleted() == 1) {
            throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "回答不存在");
        }
        if (Objects.equals(answer.getUserId(), userId)) {
            throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "不能给自己的回答投票");
        }

        AnswerVoteDO existingVote = answerVoteDao.getVoteRecord(answerId, userId);

        if (existingVote != null) {
            int oldVoteType = existingVote.getVoteType();
            if (oldVoteType == voteType) {
                // 重复点击同一投票类型 = 取消投票
                answerVoteDao.removeById(existingVote.getId());
                if (voteType == 1) {
                    answer.setVoteUpCount(Math.max(0, answer.getVoteUpCount() - 1));
                } else {
                    answer.setVoteDownCount(Math.max(0, answer.getVoteDownCount() - 1));
                }
            } else {
                // 更换投票方向
                existingVote.setVoteType(voteType);
                existingVote.setUpdateTime(new Date());
                answerVoteDao.updateById(existingVote);
                if (voteType == 1) {
                    answer.setVoteUpCount(answer.getVoteUpCount() + 1);
                    answer.setVoteDownCount(Math.max(0, answer.getVoteDownCount() - 1));
                } else {
                    answer.setVoteDownCount(answer.getVoteDownCount() + 1);
                    answer.setVoteUpCount(Math.max(0, answer.getVoteUpCount() - 1));
                }
            }
        } else {
            // 新投票
            AnswerVoteDO vote = new AnswerVoteDO();
            vote.setAnswerId(answerId);
            vote.setUserId(userId);
            vote.setVoteType(voteType);
            vote.setCreateTime(new Date());
            vote.setUpdateTime(new Date());
            answerVoteDao.save(vote);

            if (voteType == 1) {
                answer.setVoteUpCount(answer.getVoteUpCount() + 1);
            } else {
                answer.setVoteDownCount(answer.getVoteDownCount() + 1);
            }

            // 通知回答者获得赞同
            if (voteType == 1) {
                saveVoteUpNotify(answer, userId);
            }
        }

        answer.setUpdateTime(new Date());
        answerDao.updateById(answer);
    }

    @Override
    public AnswerDTO getAnswer(Long answerId, Long currentUserId) {
        AnswerDO answer = answerDao.getById(answerId);
        if (answer == null) {
            return null;
        }
        return buildAnswerDTO(answer, currentUserId);
    }

    private AnswerDTO buildAnswerDTO(AnswerDO answer, Long currentUserId) {
        AnswerDTO dto = new AnswerDTO();
        dto.setAnswerId(answer.getId());
        dto.setArticleId(answer.getArticleId());
        dto.setContent(answer.getContent());
        dto.setVoteUpCount(answer.getVoteUpCount());
        dto.setVoteDownCount(answer.getVoteDownCount());
        dto.setAccepted(answer.getAccepted() == 1);
        dto.setCreateTime(answer.getCreateTime());
        dto.setUpdateTime(answer.getUpdateTime());

        // 查询回答者信息
        BaseUserInfoDTO userInfo = userService.queryBasicUserInfo(answer.getUserId());
        dto.setUser(userInfo);

        // 查询当前用户的投票状态
        if (currentUserId != null && currentUserId > 0) {
            AnswerVoteDO vote = answerVoteDao.getVoteRecord(answer.getId(), currentUserId);
            if (vote != null) {
                dto.setVotedUp(vote.getVoteType() == 1);
                dto.setVotedDown(vote.getVoteType() == 2);
            }
        }

        return dto;
    }

    private void saveVoteUpNotify(AnswerDO answer, Long voterUserId) {
        NotifyMsgDO msg = new NotifyMsgDO()
                .setRelatedId(answer.getId())
                .setNotifyUserId(answer.getUserId())
                .setOperateUserId(voterUserId)
                .setType(NotifyTypeEnum.VOTE_UP.getType())
                .setState(NotifyStatEnum.UNREAD.getStat())
                .setMsg("赞同了你的回答");
        notifyMsgDao.save(msg);
    }
}

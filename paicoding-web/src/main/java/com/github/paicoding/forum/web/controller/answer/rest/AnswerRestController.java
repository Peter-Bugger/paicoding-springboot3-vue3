package com.github.paicoding.forum.web.controller.answer.rest;

import com.github.paicoding.forum.api.model.context.ReqInfoContext;
import com.github.paicoding.forum.api.model.vo.PageParam;
import com.github.paicoding.forum.api.model.vo.PageVo;
import com.github.paicoding.forum.api.model.vo.ResVo;
import com.github.paicoding.forum.api.model.vo.answer.AnswerDTO;
import com.github.paicoding.forum.api.model.vo.answer.AnswerSaveReq;
import com.github.paicoding.forum.api.model.vo.answer.VoteReq;
import com.github.paicoding.forum.api.model.vo.constants.StatusEnum;
import com.github.paicoding.forum.core.permission.Permission;
import com.github.paicoding.forum.core.permission.UserRole;
import com.github.paicoding.forum.core.util.NumUtil;
import com.github.paicoding.forum.service.answer.service.AnswerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * Q&A 问答系统控制器
 *
 * @author Mr.Bu
 * @date 2026-05-27
 */
@RestController
@RequestMapping(path = "qa/api")
public class AnswerRestController {

    @Autowired
    private AnswerService answerService;

    /**
     * 发布/更新回答
     */
    @Permission(role = UserRole.LOGIN)
    @PostMapping(path = "/answers")
    public ResVo<Long> saveAnswer(@RequestBody AnswerSaveReq req) {
        if (NumUtil.nullOrZero(req.getArticleId())) {
            return ResVo.fail(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "问题ID不能为空");
        }
        if (req.getContent() == null || req.getContent().trim().isEmpty()) {
            return ResVo.fail(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "回答内容不能为空");
        }
        Long answerId = answerService.saveAnswer(req, ReqInfoContext.getReqInfo().getUserId());
        return ResVo.ok(answerId);
    }

    /**
     * 获取问题的回答列表
     */
    @GetMapping(path = "/answers/{articleId}")
    public ResVo<PageVo<AnswerDTO>> listAnswers(@PathVariable Long articleId,
                                                 @RequestParam(required = false) Long pageNum,
                                                 @RequestParam(required = false) Long pageSize) {
        if (NumUtil.nullOrZero(articleId)) {
            return ResVo.fail(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "问题ID不能为空");
        }
        pageNum = Optional.ofNullable(pageNum).orElse(PageParam.DEFAULT_PAGE_NUM);
        pageSize = Optional.ofNullable(pageSize).orElse(PageParam.DEFAULT_PAGE_SIZE);
        Long currentUserId = ReqInfoContext.getReqInfo() != null ? ReqInfoContext.getReqInfo().getUserId() : null;
        PageVo<AnswerDTO> result = answerService.listAnswers(articleId, PageParam.newPageInstance(pageNum, pageSize), currentUserId);
        return ResVo.ok(result);
    }

    /**
     * 删除回答
     */
    @Permission(role = UserRole.LOGIN)
    @DeleteMapping(path = "/answers/{answerId}")
    public ResVo<Boolean> deleteAnswer(@PathVariable Long answerId) {
        answerService.deleteAnswer(answerId, ReqInfoContext.getReqInfo().getUserId());
        return ResVo.ok(true);
    }

    /**
     * 采纳回答
     */
    @Permission(role = UserRole.LOGIN)
    @PutMapping(path = "/answers/{answerId}/accept")
    public ResVo<Boolean> acceptAnswer(@PathVariable Long answerId) {
        answerService.acceptAnswer(answerId, ReqInfoContext.getReqInfo().getUserId());
        return ResVo.ok(true);
    }

    /**
     * 投票（赞同/反对）
     */
    @Permission(role = UserRole.LOGIN)
    @PostMapping(path = "/answers/vote")
    public ResVo<Boolean> vote(@RequestBody VoteReq req) {
        if (NumUtil.nullOrZero(req.getAnswerId())) {
            return ResVo.fail(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "回答ID不能为空");
        }
        if (req.getVoteType() == null || (req.getVoteType() != 1 && req.getVoteType() != 2)) {
            return ResVo.fail(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "投票类型非法");
        }
        answerService.vote(req, ReqInfoContext.getReqInfo().getUserId());
        return ResVo.ok(true);
    }
}

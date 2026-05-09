package com.github.paicoding.forum.web.controller.msg.rest;

import com.github.paicoding.forum.api.model.context.ReqInfoContext;
import com.github.paicoding.forum.api.model.exception.ExceptionUtil;
import com.github.paicoding.forum.api.model.vo.PageListVo;
import com.github.paicoding.forum.api.model.vo.PageParam;
import com.github.paicoding.forum.api.model.vo.ResVo;
import com.github.paicoding.forum.api.model.vo.constants.StatusEnum;
import com.github.paicoding.forum.api.model.vo.msg.*;
import com.github.paicoding.forum.core.permission.Permission;
import com.github.paicoding.forum.core.permission.UserRole;
import com.github.paicoding.forum.service.msg.service.MsgService;
import com.github.paicoding.forum.service.user.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 私信 REST API
 *
 * @author Mr.Bu
 * @date 2026-05-08
 */
@Permission(role = UserRole.LOGIN)
@RestController
@RequestMapping(path = "/msg/api")
public class MsgController {

    @Resource
    private MsgService msgService;

    @Resource
    private UserService userService;

    /**
     * 发送消息
     * POST /api/msg/send
     */
    @PostMapping(path = "send")
    public ResVo<SendMsgRes> send(@RequestBody SendMsgReq req) {
        Long currentUserId = ReqInfoContext.getReqInfo().getUserId();

        // 校验目标用户是否存在
        if (req.getToUserId() == null || userService.queryBasicUserInfo(req.getToUserId()) == null) {
            throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "目标用户不存在");
        }

        // 校验不能给自己发消息
        if (currentUserId.equals(req.getToUserId())) {
            throw ExceptionUtil.of(StatusEnum.FORBID_ERROR_MIXED, "不能给自己发消息");
        }

        // 校验消息内容
        if (req.getContent() == null || req.getContent().trim().isEmpty()) {
            throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "消息内容不能为空");
        }
        if (req.getContent().length() > 5000) {
            throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "消息内容不能超过5000字符");
        }

        SendMsgRes res = msgService.send(currentUserId, req);
        return ResVo.ok(res);
    }

    /**
     * 创建或获取一对一会话
     * POST /api/msg/start
     */
    @PostMapping(path = "start")
    public ResVo<StartConvRes> startConversation(@RequestBody StartConvReq req) {
        Long currentUserId = ReqInfoContext.getReqInfo().getUserId();

        if (req.getToUserId() == null) {
            throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "目标用户不能为空");
        }
        if (currentUserId.equals(req.getToUserId())) {
            throw ExceptionUtil.of(StatusEnum.FORBID_ERROR_MIXED, "不能和自己发起会话");
        }
        if (userService.queryBasicUserInfo(req.getToUserId()) == null) {
            throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "目标用户不存在");
        }

        Long conversationId = msgService.getOrCreatePrivateConversation(currentUserId, req.getToUserId());
        return ResVo.ok(new StartConvRes(conversationId));
    }

    /**
     * 获取会话列表
     * GET /api/msg/conversations?page=1&pageSize=20
     */
    @GetMapping(path = "conversations")
    public ResVo<PageListVo<ConversationVO>> listConversations(
            @RequestParam(name = "page", defaultValue = "1") Long page,
            @RequestParam(name = "pageSize", defaultValue = "20") Long pageSize) {
        Long userId = ReqInfoContext.getReqInfo().getUserId();
        PageParam pageParam = PageParam.newPageInstance(page, pageSize);
        PageListVo<ConversationVO> result = msgService.listConversations(userId, pageParam);
        return ResVo.ok(result);
    }

    /**
     * 获取会话历史消息（含对方用户信息，解决前端 targetUser 无法确定导致发送失败的问题）
     * GET /api/msg/messages/{conversationId}?page=1&pageSize=20
     */
    @GetMapping(path = "messages/{conversationId}")
    public ResVo<MessageListVO> listMessages(
            @PathVariable("conversationId") Long conversationId,
            @RequestParam(name = "page", defaultValue = "1") Long page,
            @RequestParam(name = "pageSize", defaultValue = "20") Long pageSize) {
        Long userId = ReqInfoContext.getReqInfo().getUserId();
        PageParam pageParam = PageParam.newPageInstance(page, pageSize);

        try {
            MessageListVO result = msgService.listMessages(userId, conversationId, pageParam);
            return ResVo.ok(result);
        } catch (IllegalArgumentException e) {
            if ("非会话成员，无权查看".equals(e.getMessage())) {
                throw ExceptionUtil.of(StatusEnum.FORBID_ERROR_MIXED, "非会话成员");
            }
            if ("会话已被清除".equals(e.getMessage())) {
                throw ExceptionUtil.of(StatusEnum.FORBID_ERROR_MIXED, "会话已被清除");
            }
            if ("会话不存在".equals(e.getMessage())) {
                throw ExceptionUtil.of(StatusEnum.RECORDS_NOT_EXISTS, "会话不存在");
            }
            throw e;
        }
    }

    /**
     * 标记会话为已读
     * PUT /api/msg/read/{conversationId}
     */
    @PutMapping(path = "read/{conversationId}")
    public ResVo<Boolean> markAsRead(@PathVariable("conversationId") Long conversationId) {
        Long userId = ReqInfoContext.getReqInfo().getUserId();

        try {
            msgService.markAsRead(userId, conversationId);
            return ResVo.ok(true);
        } catch (IllegalArgumentException e) {
            if ("非会话成员".equals(e.getMessage())) {
                throw ExceptionUtil.of(StatusEnum.FORBID_ERROR_MIXED, "非会话成员");
            }
            throw e;
        }
    }

    /**
     * 清除会话(当前用户侧)
     * DELETE /api/msg/conversations/{conversationId}
     */
    @DeleteMapping(path = "conversations/{conversationId}")
    public ResVo<Boolean> deleteConversation(@PathVariable("conversationId") Long conversationId) {
        Long userId = ReqInfoContext.getReqInfo().getUserId();

        try {
            msgService.clearConversation(userId, conversationId);
            return ResVo.ok(true);
        } catch (IllegalArgumentException e) {
            if ("非会话成员，无权清除".equals(e.getMessage())) {
                throw ExceptionUtil.of(StatusEnum.FORBID_ERROR_MIXED, "非会话成员");
            }
            if ("会话已被清除".equals(e.getMessage())) {
                throw ExceptionUtil.of(StatusEnum.FORBID_ERROR_MIXED, "会话已被清除");
            }
            throw e;
        }
    }

    /**
     * 获取未读消息总数
     * GET /api/msg/unread-count
     */
    @GetMapping(path = "unread-count")
    public ResVo<UnreadCountRes> unreadCount() {
        Long userId = ReqInfoContext.getReqInfo().getUserId();
        int count = msgService.queryUnreadTotal(userId);
        return ResVo.ok(new UnreadCountRes(count));
    }
}

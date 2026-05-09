package com.github.paicoding.forum.service.msg.service;

import com.github.paicoding.forum.api.model.vo.PageListVo;
import com.github.paicoding.forum.api.model.vo.PageParam;
import com.github.paicoding.forum.api.model.vo.msg.ConversationVO;
import com.github.paicoding.forum.api.model.vo.msg.MessageListVO;
import com.github.paicoding.forum.api.model.vo.msg.MessageVO;
import com.github.paicoding.forum.api.model.vo.msg.SendMsgReq;
import com.github.paicoding.forum.api.model.vo.msg.SendMsgRes;

/**
 * 私信服务接口
 *
 * @author Mr.Bu
 * @date 2026-05-08
 */
public interface MsgService {

    /**
     * 发送消息
     *
     * @param currentUserId 当前登录用户ID
     * @param req           发送消息请求
     * @return 发送结果
     */
    SendMsgRes send(Long currentUserId, SendMsgReq req);

    /**
     * 获取会话列表
     *
     * @param userId 用户ID
     * @param page   分页参数
     * @return 会话列表
     */
    PageListVo<ConversationVO> listConversations(Long userId, PageParam page);

    /**
     * 获取会话历史消息
     *
     * @param userId         用户ID（用于权限校验）
     * @param conversationId 会话ID
     * @param page           分页参数
     * @return 消息列表
     */
    MessageListVO listMessages(Long userId, Long conversationId, PageParam page);

    /**
     * 标记会话为已读
     *
     * @param userId         用户ID
     * @param conversationId 会话ID
     */
    void markAsRead(Long userId, Long conversationId);

    /**
     * 查询用户未读消息总数
     *
     * @param userId 用户ID
     * @return 未读消息总数
     */
    int queryUnreadTotal(Long userId);

    /**
     * 清除会话(当前用户侧)
     *
     * @param userId         当前登录用户ID
     * @param conversationId 会话ID
     */
    void clearConversation(Long userId, Long conversationId);

    /**
     * 创建或获取已有一对一会话
     *
     * @param currentUserId 当前用户ID
     * @param targetUserId  目标用户ID
     * @return 会话ID
     */
    Long getOrCreatePrivateConversation(Long currentUserId, Long targetUserId);
}

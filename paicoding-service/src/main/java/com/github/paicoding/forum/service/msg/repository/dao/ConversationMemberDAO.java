package com.github.paicoding.forum.service.msg.repository.dao;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.paicoding.forum.service.msg.repository.entity.ConversationMemberDO;
import com.github.paicoding.forum.service.msg.repository.mapper.ConversationMemberMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 会话成员 DAO
 *
 * @author Mr.Bu
 * @date 2026-05-08
 */
@Repository
public class ConversationMemberDAO extends ServiceImpl<ConversationMemberMapper, ConversationMemberDO> {

    /**
     * 查询用户的所有未删除会话成员记录
     *
     * @param userId 用户ID
     * @return 会话成员列表
     */
    public List<ConversationMemberDO> listByUserId(Long userId) {
        return lambdaQuery()
                .eq(ConversationMemberDO::getUserId, userId)
                .eq(ConversationMemberDO::getIsDeleted, 0)
                .orderByDesc(ConversationMemberDO::getUpdateTime)
                .list();
    }

    /**
     * 查询用户在指定会话中的成员记录
     *
     * @param conversationId 会话ID
     * @param userId         用户ID
     * @return 会话成员记录
     */
    public ConversationMemberDO getByConversationAndUser(Long conversationId, Long userId) {
        return lambdaQuery()
                .eq(ConversationMemberDO::getConversationId, conversationId)
                .eq(ConversationMemberDO::getUserId, userId)
                .one();
    }

    /**
     * 统计用户所有会话的总未读数
     *
     * @param userId 用户ID
     * @return 未读消息总数
     */
    public Integer countUnreadByUser(Long userId) {
        LambdaQueryWrapper<ConversationMemberDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.select(ConversationMemberDO::getUnreadCount)
                .eq(ConversationMemberDO::getUserId, userId)
                .eq(ConversationMemberDO::getIsDeleted, 0);
        List<ConversationMemberDO> list = list(wrapper);
        return list.stream()
                .mapToInt(m -> m.getUnreadCount() != null ? m.getUnreadCount() : 0)
                .sum();
    }

    /**
     * 查询用户的所有已删除(已清除)会话成员记录
     *
     * @param userId 用户ID
     * @return 会话成员列表
     */
    public List<ConversationMemberDO> listDeletedByUserId(Long userId) {
        return lambdaQuery()
                .eq(ConversationMemberDO::getUserId, userId)
                .eq(ConversationMemberDO::getIsDeleted, 1)
                .orderByDesc(ConversationMemberDO::getUpdateTime)
                .list();
    }

    /**
     * 查询会话的所有成员
     *
     * @param conversationId 会话ID
     * @return 成员列表
     */
    public List<ConversationMemberDO> listByConversation(Long conversationId) {
        return lambdaQuery()
                .eq(ConversationMemberDO::getConversationId, conversationId)
                .eq(ConversationMemberDO::getIsDeleted, 0)
                .list();
    }
}

package com.github.paicoding.forum.service.msg.repository.dao;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.paicoding.forum.service.msg.repository.entity.MessageDO;
import com.github.paicoding.forum.service.msg.repository.mapper.MessageMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 消息 DAO
 *
 * @author Mr.Bu
 * @date 2026-05-08
 */
@Repository
public class MessageDAO extends ServiceImpl<MessageMapper, MessageDO> {

    /**
     * 按会话ID分页查询消息（按创建时间倒序）
     *
     * @param conversationId 会话ID
     * @param offset         偏移量
     * @param limit          每页大小
     * @return 消息列表
     */
    public List<MessageDO> listByConversation(Long conversationId, long offset, long limit) {
        return lambdaQuery()
                .eq(MessageDO::getConversationId, conversationId)
                .orderByDesc(MessageDO::getCreateTime)
                .last("limit " + offset + "," + limit)
                .list();
    }

    /**
     * 查询会话中未读的消息（按ID升序）
     *
     * @param conversationId 会话ID
     * @param excludeSenderId  排除发送者（即只查接收方未读的消息）
     * @return 未读消息列表
     */
    public List<MessageDO> listUnreadByConversation(Long conversationId, Long excludeSenderId) {
        return lambdaQuery()
                .eq(MessageDO::getConversationId, conversationId)
                .ne(MessageDO::getSenderId, excludeSenderId)
                .ne(MessageDO::getStatus, "READ")
                .list();
    }
}

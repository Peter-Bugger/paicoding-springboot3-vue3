package com.github.paicoding.forum.service.msg.repository.dao;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.paicoding.forum.service.msg.repository.entity.ConversationDO;
import com.github.paicoding.forum.service.msg.repository.mapper.ConversationMapper;
import org.springframework.stereotype.Repository;

/**
 * 会话 DAO
 *
 * @author Mr.Bu
 * @date 2026-05-08
 */
@Repository
public class ConversationDAO extends ServiceImpl<ConversationMapper, ConversationDO> {
}

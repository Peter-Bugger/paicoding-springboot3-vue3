package com.github.paicoding.forum.service.msg.service.impl;

import com.github.paicoding.forum.api.model.vo.PageListVo;
import com.github.paicoding.forum.api.model.vo.PageParam;
import com.github.paicoding.forum.api.model.vo.msg.ConversationVO;
import com.github.paicoding.forum.api.model.vo.msg.MessageListVO;
import com.github.paicoding.forum.api.model.vo.msg.MessageVO;
import com.github.paicoding.forum.api.model.vo.msg.SendMsgReq;
import com.github.paicoding.forum.api.model.vo.msg.SendMsgRes;
import com.github.paicoding.forum.api.model.vo.msg.StartConvReq;
import com.github.paicoding.forum.api.model.vo.msg.StartConvRes;
import com.github.paicoding.forum.api.model.vo.user.dto.BaseUserInfoDTO;
import com.github.paicoding.forum.core.permission.Permission;
import com.github.paicoding.forum.core.permission.UserRole;
import com.github.paicoding.forum.core.util.TransactionUtil;
import com.github.paicoding.forum.service.msg.helper.MsgPushHelper;
import com.github.paicoding.forum.service.msg.repository.dao.ConversationDAO;
import com.github.paicoding.forum.service.msg.repository.dao.ConversationMemberDAO;
import com.github.paicoding.forum.service.msg.repository.dao.MessageDAO;
import com.github.paicoding.forum.service.msg.repository.entity.ConversationDO;
import com.github.paicoding.forum.service.msg.repository.entity.ConversationMemberDO;
import com.github.paicoding.forum.service.msg.repository.entity.MessageDO;
import com.github.paicoding.forum.service.msg.service.MsgService;
import com.github.paicoding.forum.service.user.service.UserService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.util.HtmlUtils;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 私信服务实现
 *
 * @author Mr.Bu
 * @date 2026-05-08
 */
@Slf4j
@Service
public class MsgServiceImpl implements MsgService {

    @Resource
    private ConversationDAO conversationDAO;

    @Resource
    private ConversationMemberDAO conversationMemberDAO;

    @Resource
    private MessageDAO messageDAO;

    @Resource
    private MsgPushHelper msgPushHelper;

    @Resource
    private UserService userService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SendMsgRes send(Long currentUserId, SendMsgReq req) {
        // 1. 业务校验（参数格式校验已在 Controller 层完成，此处为防御性二次校验）
        Assert.isTrue(!Objects.equals(currentUserId, req.getToUserId()), "不能给自己发消息");
        Assert.isTrue(StringUtils.hasText(req.getContent()), "消息内容不能为空");

        String safeContent = HtmlUtils.htmlEscape(req.getContent().trim());
        if (safeContent.length() > 5000) {
            throw new IllegalArgumentException("消息内容不能超过5000字符");
        }

        // 2. 查找或创建会话
        Long conversationId = getOrCreatePrivateConversation(currentUserId, req.getToUserId());

        // 3. 保存消息
        MessageDO msg = new MessageDO()
                .setConversationId(conversationId)
                .setSenderId(currentUserId)
                .setMessageType("TEXT")
                .setContent(safeContent)
                .setStatus("SENT");
        messageDAO.save(msg);

        // 4. 更新会话冗余字段
        conversationDAO.lambdaUpdate()
                .eq(ConversationDO::getId, conversationId)
                .set(ConversationDO::getLastMessageId, msg.getId())
                .set(ConversationDO::getLastMessageTime, msg.getCreateTime())
                .update();

        // 5. 递增接收方未读数
        conversationMemberDAO.lambdaUpdate()
                .eq(ConversationMemberDO::getConversationId, conversationId)
                .eq(ConversationMemberDO::getUserId, req.getToUserId())
                .setSql("unread_count = unread_count + 1")
                .update();

        // 6 & 7 在事务提交后执行（避免前端收到推送时读到旧数据）
        final Long targetUserId = req.getToUserId();
        final Long msgId = msg.getId();
        TransactionUtil.registryAfterCommitOrImmediatelyRun(() -> {
            // 6. WebSocket 推送
            msgPushHelper.pushNewMessage(targetUserId, msg);

            // 7. 推送成功后标记消息为 DELIVERED
            messageDAO.lambdaUpdate()
                    .eq(MessageDO::getId, msgId)
                    .set(MessageDO::getStatus, "DELIVERED")
                    .update();
        });

        return new SendMsgRes(conversationId, msg.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void clearConversation(Long userId, Long conversationId) {
        // 1. 校验成员关系
        ConversationMemberDO member = conversationMemberDAO.getByConversationAndUser(conversationId, userId);
        Assert.notNull(member, "非会话成员，无权清除");

        // 2. 校验 is_deleted=0 (防止重复清除)
        if (member.getIsDeleted() != null && member.getIsDeleted() == 1) {
            throw new IllegalArgumentException("会话已被清除");
        }

        // 3. 更新: is_deleted=1, cleared_at=NOW(), unread_count=0
        conversationMemberDAO.lambdaUpdate()
                .eq(ConversationMemberDO::getId, member.getId())
                .set(ConversationMemberDO::getIsDeleted, 1)
                .set(ConversationMemberDO::getClearedAt, new Date())
                .set(ConversationMemberDO::getUnreadCount, 0)
                .update();
    }

    @Override
    public PageListVo<ConversationVO> listConversations(Long userId, PageParam page) {
        // 1. 查询该用户的所有会话成员记录
        List<ConversationMemberDO> members = conversationMemberDAO.listByUserId(userId);
        if (members.isEmpty()) {
            return PageListVo.emptyVo();
        }

        // 2. 构建会话ID集合，按 updateTime 分页
        List<Long> convIds = members.stream()
                .map(ConversationMemberDO::getConversationId)
                .collect(Collectors.toList());

        // 3. 查询会话信息（DB 层分页）[已修复]
        List<ConversationDO> pageConvs = conversationDAO.lambdaQuery()
                .in(ConversationDO::getId, convIds)
                .eq(ConversationDO::getDeleted, 0)
                .orderByDesc(ConversationDO::getLastMessageTime)
                .last("LIMIT " + page.getOffset() + ", " + page.getPageSize())
                .list();

        // 4. 构建会话成员 Map
        Map<Long, ConversationMemberDO> memberMap = members.stream()
                .collect(Collectors.toMap(ConversationMemberDO::getConversationId, m -> m, (a, b) -> a));

        // 5. 批量获取所有相关会话的成员（一次查询，避免 N+1）
        List<ConversationMemberDO> allMembers = conversationMemberDAO.lambdaQuery()
                .in(ConversationMemberDO::getConversationId, convIds)
                .eq(ConversationMemberDO::getIsDeleted, 0)
                .list();

        // 构建 conversationId → otherUserId 映射
        Map<Long, Long> convOtherUserMap = allMembers.stream()
                .filter(m -> !m.getUserId().equals(userId))
                .collect(Collectors.toMap(
                        ConversationMemberDO::getConversationId,
                        ConversationMemberDO::getUserId,
                        (a, b) -> a));

        // 6. 批量查询目标用户信息
        Set<Long> targetUserIds = new java.util.HashSet<>(convOtherUserMap.values());
        Map<Long, BaseUserInfoDTO> userInfoMap;
        if (!targetUserIds.isEmpty()) {
            List<BaseUserInfoDTO> userInfos = userService.batchQueryBasicUserInfo(targetUserIds);
            userInfoMap = userInfos.stream()
                    .collect(Collectors.toMap(BaseUserInfoDTO::getUserId, u -> u, (a, b) -> a));
        } else {
            userInfoMap = Collections.emptyMap();
        }

        // 7. 批量查询最后一条消息（一次查询，避免 N+1）
        Set<Long> lastMsgIds = pageConvs.stream()
                .map(ConversationDO::getLastMessageId)
                .filter(id -> id != null && id > 0)
                .collect(Collectors.toSet());
        Map<Long, MessageDO> msgMap = lastMsgIds.isEmpty() ? Collections.emptyMap() :
                messageDAO.lambdaQuery().in(MessageDO::getId, lastMsgIds).list()
                        .stream().collect(Collectors.toMap(MessageDO::getId, m -> m));

        // 8. 构建 VO 列表
        List<ConversationVO> voList = pageConvs.stream().map(conv -> {
            ConversationVO vo = new ConversationVO();
            vo.setConversationId(conv.getId());
            vo.setConversationType(conv.getConversationType());
            vo.setCreateTime(conv.getCreateTime());

            ConversationMemberDO member = memberMap.get(conv.getId());
            if (member != null) {
                vo.setUnreadCount(member.getUnreadCount() != null ? member.getUnreadCount() : 0);
                vo.setIsTop(member.getIsTop() != null ? member.getIsTop() : 0);
            }

            // 目标用户信息（从批量查询的映射中获取）
            Long otherUserId = convOtherUserMap.get(conv.getId());
            if (otherUserId != null) {
                BaseUserInfoDTO userInfo = userInfoMap.get(otherUserId);
                if (userInfo != null) {
                    vo.setTargetUser(userInfo);
                }
            }

            // 最后一条消息（从批量查询的映射中获取）
            if (conv.getLastMessageId() != null && conv.getLastMessageId() > 0) {
                MessageDO lastMsg = msgMap.get(conv.getLastMessageId());
                if (lastMsg != null) {
                    ConversationVO.LastMessageVO lastMsgVo = new ConversationVO.LastMessageVO();
                    lastMsgVo.setContent(lastMsg.getContent());
                    lastMsgVo.setCreateTime(lastMsg.getCreateTime());
                    lastMsgVo.setStatus(lastMsg.getStatus());
                    lastMsgVo.setFromUserId(lastMsg.getSenderId());
                    vo.setLastMessage(lastMsgVo);
                }
            }

            return vo;
        }).collect(Collectors.toList());

        return PageListVo.newVo(voList, page.getPageSize());
    }

    @Override
    public MessageListVO listMessages(Long userId, Long conversationId, PageParam page) {
        // 1. 校验会话存在
        ConversationDO conv = conversationDAO.getById(conversationId);
        Assert.notNull(conv, "会话不存在");

        // 2. 校验当前用户是否为会话成员
        ConversationMemberDO member = conversationMemberDAO.getByConversationAndUser(conversationId, userId);
        Assert.notNull(member, "非会话成员，无权查看");

        // 2.1 校验会话是否已被清除
        if (member.getIsDeleted() != null && member.getIsDeleted() == 1) {
            throw new IllegalArgumentException("会话已被清除");
        }

        // 3. 查询消息（按时间倒序，取指定偏移）
        //    如果有 cleared_at，只查询清除时间之后的消息
        var query = messageDAO.lambdaQuery()
                .eq(MessageDO::getConversationId, conversationId)
                .orderByDesc(MessageDO::getCreateTime);

        if (member.getClearedAt() != null) {
            query.gt(MessageDO::getCreateTime, member.getClearedAt());
        }

        List<MessageDO> messages = query
                .last("limit " + page.getOffset() + "," + page.getPageSize())
                .list();

        // 4. 反转为正序展示（最旧在前，最新在后）
        Collections.reverse(messages);

        // 5. 批量查询发送者用户信息
        Set<Long> senderIds = messages.stream()
                .map(MessageDO::getSenderId)
                .collect(Collectors.toSet());
        Map<Long, BaseUserInfoDTO> userInfoMap;
        if (!senderIds.isEmpty()) {
            List<BaseUserInfoDTO> userInfos = userService.batchQueryBasicUserInfo(senderIds);
            userInfoMap = userInfos.stream()
                    .collect(Collectors.toMap(BaseUserInfoDTO::getUserId, u -> u, (a, b) -> a));
        } else {
            userInfoMap = Collections.emptyMap();
        }

        // 6. 转换为 VO
        List<MessageVO> voList = messages.stream().map(msg -> {
            MessageVO vo = new MessageVO();
            vo.setMessageId(msg.getId());
            vo.setConversationId(msg.getConversationId());
            vo.setFromUserId(msg.getSenderId());

            BaseUserInfoDTO userInfo = userInfoMap.get(msg.getSenderId());
            if (userInfo != null) {
                vo.setFromUserName(userInfo.getUserName());
                vo.setFromUserPhoto(userInfo.getPhoto());
            }

            vo.setMessageType(msg.getMessageType());
            vo.setContent(msg.getContent());
            vo.setReferencedMsgId(msg.getReferencedMsgId());
            vo.setAttachment(msg.getAttachment());
            vo.setStatus(msg.getStatus());
            vo.setCreateTime(msg.getCreateTime());
            return vo;
        }).collect(Collectors.toList());

        // 7. 查询对方用户信息（解决前端 targetUser 兜底解析失败的问题）
        BaseUserInfoDTO targetUser = resolveTargetUser(userId, conversationId);

        MessageListVO result = new MessageListVO();
        result.setList(voList);
        result.setHasMore(voList.size() == page.getPageSize());
        result.setTargetUser(targetUser);
        return result;
    }

    /**
     * 解析会话的对方用户信息
     */
    private BaseUserInfoDTO resolveTargetUser(Long userId, Long conversationId) {
        // 查询会话所有成员（含已删除），确保任意一方删除后仍能解析对方用户信息
        List<ConversationMemberDO> allMembers = conversationMemberDAO.lambdaQuery()
                .eq(ConversationMemberDO::getConversationId, conversationId)
                .list();

        Long otherUserId = allMembers.stream()
                .filter(m -> !m.getUserId().equals(userId))
                .map(ConversationMemberDO::getUserId)
                .findFirst()
                .orElse(null);

        if (otherUserId != null) {
            return userService.queryBasicUserInfo(otherUserId);
        }
        return null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAsRead(Long userId, Long conversationId) {
        // 1. 校验成员关系
        ConversationMemberDO member = conversationMemberDAO.getByConversationAndUser(conversationId, userId);
        Assert.notNull(member, "非会话成员");

        // 2. 批量更新消息状态为已读
        messageDAO.lambdaUpdate()
                .eq(MessageDO::getConversationId, conversationId)
                .ne(MessageDO::getSenderId, userId)
                .ne(MessageDO::getStatus, "READ")
                .set(MessageDO::getStatus, "READ")
                .update();

        // 3. 查询最后一条消息（用于更新 last_read_message_id）
        MessageDO lastMsg = messageDAO.lambdaQuery()
                .eq(MessageDO::getConversationId, conversationId)
                .orderByDesc(MessageDO::getId)
                .last("limit 1")
                .one();

        // 4. 重置未读数并更新最后已读消息ID
        conversationMemberDAO.lambdaUpdate()
                .eq(ConversationMemberDO::getId, member.getId())
                .set(ConversationMemberDO::getUnreadCount, 0)
                .set(lastMsg != null, ConversationMemberDO::getLastReadMessageId,
                        lastMsg != null ? lastMsg.getId() : null)
                .update();

        // 5. 如果最后一条消息不是当前用户发的，推送已读状态给发送者
        if (lastMsg != null && !lastMsg.getSenderId().equals(userId)) {
            msgPushHelper.pushReadStatus(lastMsg.getSenderId(), conversationId,
                    lastMsg.getId(), userId);
        }
    }

    @Override
    public int queryUnreadTotal(Long userId) {
        Integer total = conversationMemberDAO.countUnreadByUser(userId);
        return total != null ? total : 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public synchronized Long getOrCreatePrivateConversation(Long currentUserId, Long targetUserId) {
        // 1. 查询 currentUserId 的所有未删除会话
        List<ConversationMemberDO> membersOfA = conversationMemberDAO.lambdaQuery()
                .eq(ConversationMemberDO::getUserId, currentUserId)
                .eq(ConversationMemberDO::getIsDeleted, 0)
                .list();

        Set<Long> convIdsOfA = membersOfA.stream()
                .map(ConversationMemberDO::getConversationId)
                .collect(Collectors.toSet());

        // 2. 查找共同 PRIVATE 会话（含目标用户已删除的成员记录，需复活）
        if (!convIdsOfA.isEmpty()) {
            List<ConversationMemberDO> membersOfB = conversationMemberDAO.lambdaQuery()
                    .eq(ConversationMemberDO::getUserId, targetUserId)
                    .in(ConversationMemberDO::getConversationId, convIdsOfA)
                    .list();

            for (ConversationMemberDO mb : membersOfB) {
                ConversationDO conv = conversationDAO.getById(mb.getConversationId());
                if (conv != null && "PRIVATE".equals(conv.getConversationType())
                        && conv.getDeleted() == 0) {
                    // 如果目标用户的成员记录已删除（当前用户未删除但对方已删除），复活目标用户
                    if (mb.getIsDeleted() != null && mb.getIsDeleted() == 1) {
                        conversationMemberDAO.lambdaUpdate()
                                .eq(ConversationMemberDO::getId, mb.getId())
                                .set(ConversationMemberDO::getIsDeleted, 0)
                                .set(ConversationMemberDO::getUnreadCount, 0)
                                .update();
                    }
                    return conv.getId();
                }
            }
        }

        // 3. 未找到活跃会话，查找已清除(is_deleted=1)的会话并复活
        List<ConversationMemberDO> deletedMembersOfA = conversationMemberDAO.lambdaQuery()
                .eq(ConversationMemberDO::getUserId, currentUserId)
                .eq(ConversationMemberDO::getIsDeleted, 1)
                .list();

        if (!deletedMembersOfA.isEmpty()) {
            Set<Long> deletedConvIdsOfA = deletedMembersOfA.stream()
                    .map(ConversationMemberDO::getConversationId)
                    .collect(Collectors.toSet());

            List<ConversationMemberDO> membersOfB = conversationMemberDAO.lambdaQuery()
                    .eq(ConversationMemberDO::getUserId, targetUserId)
                    .eq(ConversationMemberDO::getIsDeleted, 0)
                    .in(ConversationMemberDO::getConversationId, deletedConvIdsOfA)
                    .list();

            for (ConversationMemberDO mb : membersOfB) {
                ConversationDO conv = conversationDAO.getById(mb.getConversationId());
                if (conv != null && "PRIVATE".equals(conv.getConversationType())
                        && conv.getDeleted() == 0) {
                    // 复活 currentUserId 的成员记录
                    ConversationMemberDO deletedMember = deletedMembersOfA.stream()
                            .filter(m -> m.getConversationId().equals(conv.getId()))
                            .findFirst().orElse(null);
                    if (deletedMember != null) {
                        conversationMemberDAO.lambdaUpdate()
                                .eq(ConversationMemberDO::getId, deletedMember.getId())
                                .set(ConversationMemberDO::getIsDeleted, 0)
                                .set(ConversationMemberDO::getUnreadCount, 0)
                                // cleared_at 保持不变: 清除前的消息不重新出现
                                .update();
                    }
                    return conv.getId();
                }
            }
        }

        // 4. 未找到任何已有关联，创建新会话
        ConversationDO conv = new ConversationDO()
                .setConversationType("PRIVATE")
                .setInitiatorId(currentUserId);
        conversationDAO.save(conv);

        // 4. 添加双方成员
        conversationMemberDAO.save(new ConversationMemberDO()
                .setConversationId(conv.getId())
                .setUserId(currentUserId)
                .setUnreadCount(0)
                .setIsDeleted(0));
        conversationMemberDAO.save(new ConversationMemberDO()
                .setConversationId(conv.getId())
                .setUserId(targetUserId)
                .setUnreadCount(0)
                .setIsDeleted(0));

        return conv.getId();
    }
}

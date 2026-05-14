package com.github.paicoding.forum.api.model.vo.msg;

import com.github.paicoding.forum.api.model.vo.PageListVo;
import com.github.paicoding.forum.api.model.vo.user.dto.BaseUserInfoDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 消息列表 VO（含目标用户信息，用于前端 targetUser 解析）
 *
 * @author Mr.Bu
 * @date 2026-05-09
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class MessageListVO extends PageListVo<MessageVO> {

    /**
     * 会话对方用户信息（解决前端 targetUser 兜底解析失败的问题）
     */
    private BaseUserInfoDTO targetUser;
}

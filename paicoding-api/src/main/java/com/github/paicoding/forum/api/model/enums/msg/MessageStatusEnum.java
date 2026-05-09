package com.github.paicoding.forum.api.model.enums.msg;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * 消息状态枚举
 *
 * @author Mr.Bu
 * @date 2026-05-08
 */
@Getter
public enum MessageStatusEnum {

    SENDING("SENDING", "发送中"),
    SENT("SENT", "已发送"),
    DELIVERED("DELIVERED", "已送达"),
    READ("READ", "已读"),
    ;

    private String status;
    private String desc;

    private static Map<String, MessageStatusEnum> mapper;

    static {
        mapper = new HashMap<>();
        for (MessageStatusEnum status : values()) {
            mapper.put(status.status, status);
        }
    }

    MessageStatusEnum(String status, String desc) {
        this.status = status;
        this.desc = desc;
    }

    public static MessageStatusEnum typeOf(String status) {
        return mapper.get(status);
    }
}

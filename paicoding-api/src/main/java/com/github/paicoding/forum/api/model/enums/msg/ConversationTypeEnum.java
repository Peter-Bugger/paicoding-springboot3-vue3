package com.github.paicoding.forum.api.model.enums.msg;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * 会话类型枚举
 *
 * @author Mr.Bu
 * @date 2026-05-08
 */
@Getter
public enum ConversationTypeEnum {

    PRIVATE("PRIVATE", "一对一私信"),
    GROUP("GROUP", "群聊(预留)"),
    ;

    private String type;
    private String desc;

    private static Map<String, ConversationTypeEnum> mapper;

    static {
        mapper = new HashMap<>();
        for (ConversationTypeEnum type : values()) {
            mapper.put(type.type, type);
        }
    }

    ConversationTypeEnum(String type, String desc) {
        this.type = type;
        this.desc = desc;
    }

    public static ConversationTypeEnum typeOf(String type) {
        return mapper.get(type);
    }
}

package com.github.paicoding.forum.api.model.enums.msg;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * 消息类型枚举
 *
 * @author Mr.Bu
 * @date 2026-05-08
 */
@Getter
public enum MessageTypeEnum {

    TEXT("TEXT", "文本消息"),
    IMAGE("IMAGE", "图片消息(预留)"),
    FILE("FILE", "文件消息(预留)"),
    VIDEO("VIDEO", "视频消息(预留)"),
    ;

    private String type;
    private String desc;

    private static Map<String, MessageTypeEnum> mapper;

    static {
        mapper = new HashMap<>();
        for (MessageTypeEnum type : values()) {
            mapper.put(type.type, type);
        }
    }

    MessageTypeEnum(String type, String desc) {
        this.type = type;
        this.desc = desc;
    }

    public static MessageTypeEnum typeOf(String type) {
        return mapper.get(type);
    }
}

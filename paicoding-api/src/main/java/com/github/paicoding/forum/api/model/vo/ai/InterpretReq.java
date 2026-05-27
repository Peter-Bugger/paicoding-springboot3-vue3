package com.github.paicoding.forum.api.model.vo.ai;

import lombok.Data;

/**
 * 文章 AI 解读请求 DTO
 *
 * @author XuYifei
 * @date 2024-07-12
 */
@Data
public class InterpretReq {
    /**
     * 文章 ID
     */
    private Long articleId;

    /**
     * 用户选中的文本，1~2000 字符
     */
    private String selectedText;

    /**
     * 选中文本起始字符偏移
     */
    private Integer startPos;

    /**
     * 选中文本结束字符偏移
     */
    private Integer endPos;
}

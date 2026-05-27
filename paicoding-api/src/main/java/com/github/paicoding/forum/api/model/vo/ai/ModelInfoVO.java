package com.github.paicoding.forum.api.model.vo.ai;

import lombok.Data;

/**
 * 模型信息 VO
 *
 * @author XuYifei
 * @date 2024-07-12
 */
@Data
public class ModelInfoVO {
    /**
     * 模型唯一标识，如 "deepseek"
     */
    private String name;

    /**
     * 模型显示名称，如 "DeepSeek V3"
     */
    private String displayName;

    /**
     * 当前是否可用
     */
    private boolean available;

    /**
     * 优先级（数值越小优先级越高）
     */
    private int priority;
}

package xyz.qy.implatform.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 投诉对象类型枚举
 */
@Getter
@AllArgsConstructor
public enum ComplaintTargetTypeEnum {
    /**
     * 用户
     */
    USER("user", "用户"),

    /**
     * 群聊
     */
    GROUP("group", "群聊"),

    /**
     * 地区群聊
     */
    REGION_GROUP("regionGroup", "地区群聊");

    /**
     * 类型编码
     */
    private final String code;

    /**
     * 类型描述
     */
    private final String description;
}
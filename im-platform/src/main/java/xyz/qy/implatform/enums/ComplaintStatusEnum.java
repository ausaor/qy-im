package xyz.qy.implatform.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 投诉状态枚举
 */
@Getter
@AllArgsConstructor
public enum ComplaintStatusEnum {
    /**
     * 已投诉
     */
    SUBMITTED(0, "已投诉"),

    /**
     * 处理中
     */
    PROCESSING(1, "处理中"),

    /**
     * 已处理
     */
    PROCESSED(2, "已处理"),

    /**
     * 无效投诉
     */
    INVALID(3, "无效投诉");

    /**
     * 编码
     */
    private final Integer code;

    /**
     * 状态描述
     */
    private final String description;
}
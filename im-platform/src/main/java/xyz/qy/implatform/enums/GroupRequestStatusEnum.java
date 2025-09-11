package xyz.qy.implatform.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 进群请求状态枚举
 * 状态（0：撤回；1：申请；2：同意；3：拒绝）
 */
@Getter
@AllArgsConstructor
public enum GroupRequestStatusEnum {
    /**
     * 撤回
     */
    RECALL(0, "撤回"),

    /**
     * 申请
     */
    APPLYING(1, "申请"),

    /**
     * 同意
     */
    AGREED(2, "同意"),

    /**
     * 拒绝
     */
    REFUSED(3, "拒绝");

    /**
     * 编码
     */
    private final Integer code;

    /**
     * 状态描述
     */
    private final String description;
}

package xyz.qy.implatform.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum GroupRequestTypeEnum {
    SELF_JOIN(1, "主动加入"),

    INVITE_JOIN(2, "邀请加入");

    /**
     * 编码
     */
    private final Integer code;

    /**
     * 状态描述
     */
    private final String desc;
}

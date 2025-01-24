package xyz.qy.implatform.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum GroupTypeEnum {
    /**
     * 普通群聊
     */
    COMMON(0),

    /**
     * 模板群聊
     */
    TEMPLATE(1),

    /**
     * 多元角色
     */
    MULT_CHARTER(2),

    /**
     * 角色群聊
     */
    CHARTERS(3),

    /**
     * 模板角色群聊
     */
    TEMPLATE_MULT_CHARTER(4),

    ;
    private final Integer code;
}

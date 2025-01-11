package xyz.qy.implatform.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 群聊信息变动枚举
 *
 * @author Polaris
 * @since 2024-10-20
 */
@Getter
@AllArgsConstructor
public enum GroupChangeTypeEnum {
    NEW_USER_JOIN(1, "新用户加入"),

    GROUP_TYPE_CHANGE(2, "群聊类型改变"),

    GROUP_MEMBER_CHANGE(3, "群聊成员信息改变"),

    DELETE_GROUP(4, "群聊解散"),

    TEMPLATE_CHARACTER_CHANGE(5, "模板角色改变"),
    ;

    private final Integer code;

    private final String name;
}

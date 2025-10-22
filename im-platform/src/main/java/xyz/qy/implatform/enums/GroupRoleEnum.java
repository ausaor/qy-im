package xyz.qy.implatform.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 群角色枚举
 *
 * @author Polaris
 */

@Getter
@AllArgsConstructor
public enum GroupRoleEnum {
    OWNER(1, "群主"),

    ADMIN(2, "管理员"),

    MEMBER(3, "群成员");

    private final int code;

    private final String description;
}

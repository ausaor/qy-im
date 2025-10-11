package xyz.qy.implatform.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 角色枚举
 *
 * @author Polaris
 */

@Getter
@AllArgsConstructor
public enum RoleEnum {
    // 超级管理员
    SUPER_ADMIN("SUPER_ADMIN", "超级管理员"),

    // 系统管理员
    ADMIN("ADMIN", "系统管理员"),

    // 普通用户
    USER("USER", "普通用户"),
    ;

    private final String code;

    private final String name;
}

package xyz.qy.implatform.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @description: 审核结果枚举
 * @author: Polaris
 * @create: 2023-08-05 20:21
 **/
@Getter
@AllArgsConstructor
public enum AuditResultEnum {
    PASS("pass", "通过"),

    NO_PASS("noPass", "不通过");

    private final String code;

    private final String name;
}

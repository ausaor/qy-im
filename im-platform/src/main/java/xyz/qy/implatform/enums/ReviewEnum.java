package xyz.qy.implatform.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @description: 审核状态枚举
 * @author: Polaris
 * @create: 2023-07-30 16:03
 **/
@Getter
@AllArgsConstructor
public enum ReviewEnum {
    TO_BE_REVIEW("0", "待审批"),

    REVIEWING("1", "审核中"),

    REVIEWED("2", "已发布"),

    NO_PASS("3", "未通过");

    private final String code;

    private final String name;
}

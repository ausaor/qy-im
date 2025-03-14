package xyz.qy.implatform.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum BanTypeEnum {
    /**
     * 系统禁止
     */
    SYS( "sys"),

    /**
     * 群主禁止
     */
    MASTER( "master");

    private final String code;
}

package xyz.qy.implatform.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ViewScopeEnum {
    PUBLIC(9, "公开"),

    FRIENDS(2, "好友可见"),

    PRIVATE(1, "私密");

    private Integer code;

    private String desc;

    public static boolean contains(Integer code) {
        for (ViewScopeEnum value : ViewScopeEnum.values()) {
            if (value.code.equals(code)) {
                return true;
            }
        }
        return false;
    }
}

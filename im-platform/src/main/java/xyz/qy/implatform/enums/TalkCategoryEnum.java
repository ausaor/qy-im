package xyz.qy.implatform.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TalkCategoryEnum {
    PRIVATE("private"),

    REGION("region"),

    GROUP("group");

    private final String code;

    public static boolean contains(String code) {
        for (TalkCategoryEnum value : values()) {
            if (value.code.equals(code)) {
                return true;
            }
        }
        return false;
    }
}

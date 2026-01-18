package xyz.qy.implatform.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SectionEnum {
    MY_FRIENDS("my-friends"),

    MY("my"),

    FRIENDS("friends"),

    FRIEND("friend"),

    GROUP("group"),

    MY_REGION("my-region"),

    REGION("region"),

    CHARACTER("character"),

    GROUP_TEMPLATE("groupTemplate"),

    GROUP_TEMPLATE_CHARACTERS("groupTemplate-characters"),

    CHARACTERS("characters"),

    ALL_CHARACTERS("allCharacters"),

    MY_CHARACTERS("myCharacters");

    private final String code;

    public static boolean contains(String code) {
        for (SectionEnum value : values()) {
            if (value.code.equals(code)) {
                return true;
            }
        }
        return false;
    }
}
package xyz.qy.implatform.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TargetTypeEnum {
    TALK("talk"),

    SHORT_VIDEO("shortVideo");

    private final String code;
}

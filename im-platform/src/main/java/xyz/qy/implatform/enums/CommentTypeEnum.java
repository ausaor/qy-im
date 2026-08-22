package xyz.qy.implatform.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CommentTypeEnum {
    TEXT(0, "文字"),
    IMAGE(1, "图片"),
    WORD_VOICE(5, "台词语音"),
    ;
    private final Integer code;

    private final String desc;
}

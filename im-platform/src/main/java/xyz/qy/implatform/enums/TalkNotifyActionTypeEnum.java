package xyz.qy.implatform.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TalkNotifyActionTypeEnum {
    COMMENT(1, "评论"),

    LIKE(2, "点赞"),

    AT(3, "@"),

    REPLY(4, "回复");

    private final Integer code;
    private final String desc;
}

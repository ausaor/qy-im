package xyz.qy.implatform.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum NotifyActionTypeEnum {
    COMMENT(1, "评论"),

    LIKE(2, "点赞"),

    AT(3, "@"),

    REPLY(4, "回复"),

    COLLECT(5, "收藏"),

    ;

    private final Integer code;
    private final String desc;
}

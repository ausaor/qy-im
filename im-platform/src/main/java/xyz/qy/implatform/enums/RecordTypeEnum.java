package xyz.qy.implatform.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RecordTypeEnum {
    COMMENT(1, "评论"),

    LIKE(2, "点赞"),

    COLLECT(3, "收藏"),

    ;

    private final Integer code;
    private final String desc;
}

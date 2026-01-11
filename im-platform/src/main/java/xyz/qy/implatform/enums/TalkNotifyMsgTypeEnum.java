package xyz.qy.implatform.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 动态通知消息类型
 */
@Getter
@AllArgsConstructor
public enum TalkNotifyMsgTypeEnum {
    TALK(1, "动态"),

    COMMENT(2, "评论"),

    STAR(3, "点赞")
    ;


    private final Integer code;
    private final String desc;
}

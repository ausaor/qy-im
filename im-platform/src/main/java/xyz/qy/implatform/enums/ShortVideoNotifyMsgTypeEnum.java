package xyz.qy.implatform.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 短视频通知消息类型
 */
@Getter
@AllArgsConstructor
public enum ShortVideoNotifyMsgTypeEnum {
    SHORT_VIDEO(1, "短视频"),

    COMMENT(2, "评论"),

    LIKE(3, "点赞"),

    COLLECT(4, "收藏"),

    COMMENT_LIKE(5, "评论点赞"),
    ;


    private final Integer code;

    private final String desc;
}

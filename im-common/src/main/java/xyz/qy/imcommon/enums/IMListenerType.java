package xyz.qy.imcommon.enums;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum IMListenerType {
    ALL(0, "全部消息"),
    PRIVATE_MESSAGE(1, "私聊消息"),
    GROUP_MESSAGE(2, "群聊消息"),

    REGION_GROUP_MESSAGE(3, "地区群聊消息"),

    SYSTEM_MESSAGE(4, "系统消息"),
    ;

    private final Integer code;

    private final String desc;

    public Integer code() {
        return this.code;
    }
}

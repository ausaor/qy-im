package xyz.qy.imcommon.enums;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum IMCmdType {
    LOGIN(0, "登陆"),
    HEART_BEAT(1, "心跳"),
    FORCE_LOGOUT(2, "强制下线"),
    PRIVATE_MESSAGE(3, "私聊消息"),
    GROUP_MESSAGE(4, "群发消息"),
    SYSTEM_MESSAGE(5,"系统消息"),
    TALK_MESSAGE(6,"动态消息"),
    REGION_GROUP_MESSAGE(9,"地区群聊消息");

    private final Integer code;

    private final String desc;

    public static IMCmdType fromCode(Integer code) {
        for (IMCmdType typeEnum : values()) {
            if (typeEnum.code.equals(code)) {
                return typeEnum;
            }
        }
        return null;
    }

    public Integer code() {
        return this.code;
    }
}


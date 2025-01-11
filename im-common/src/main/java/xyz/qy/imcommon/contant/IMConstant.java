package xyz.qy.imcommon.contant;

public final class IMConstant {
    private IMConstant() {
    }
    // 在线状态过期时间 600s
    public static final long ONLINE_TIMEOUT_SECOND = 600;
    // 消息允许撤回时间 300s
    public static final long ALLOW_RECALL_SECOND = 300;
    // 私聊消息redis主题
    public static final String PRIVATE_MSG_TOPIC = "private-message-topic";
    // 群聊消息redis主题
    public static final String GROUP_MSG_TOPIC = "group-message-topic";

    // 地区群聊消息redis主题
    public static final String REGION_GROUP_MSG_TOPIC = "region-group-message-topic";

    // 系统消息redis主题
    public static final String SYSTEM_MSG_TOPIC = "system-message-topic";

    // 私聊消息发送结果redis主题
    public static final String PRIVATE_MSG_SEND_RESULT_TOPIC = "private-msg-send-result-topic";
    // 群聊消息发送结果redis主题
    public static final String GROUP_MSG_SEND_RESULT_TOPIC = "group-msg-send-result-topic";

    // 地区群聊消息发送结果redis主题
    public static final String REGION_GROUP_MSG_SEND_RESULT_TOPIC = "region-group-msg-send-result-topic";

    // 系统消息发送结果redis主题
    public static final String SYSTEM_MSG_SEND_RESULT_TOPIC = "system-msg-send-result-topic";
}

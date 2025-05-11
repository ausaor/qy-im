package xyz.qy.imcommon.contant;

public final class IMRedisKey {
    public IMRedisKey() {
    }

    // im-server最大id,从0开始递增
    public final static String  IM_MAX_SERVER_ID = "im:max_server_id";
    // 用户ID所连接的IM-server的ID
    public final static String  IM_USER_SERVER_ID = "im:user:server_id";
    // 未读私聊消息队列
    public final static String IM_MESSAGE_PRIVATE_QUEUE = "im:message:private";
    // 未读群聊消息队列
    public final static String IM_MESSAGE_GROUP_QUEUE = "im:message:group";

    // 未读地区群聊消息队列
    public final static String IM_MESSAGE_REGION_GROUP_QUEUE = "im:message:region:group";

    /**
     * 系统消息队列
     */
    public static final String IM_MESSAGE_SYSTEM_QUEUE = "im:message:system";

    /**
     * 动态消息队列
     */
    public static final String IM_MESSAGE_TALK_QUEUE = "im:message:talk";

    // 私聊消息发送结果队列
    public final static String IM_RESULT_PRIVATE_QUEUE = "im:result:private";
    // 群聊消息发送结果队列
    public final static String IM_RESULT_GROUP_QUEUE = "im:result:group";

    // 地区群聊消息发送结果队列
    public final static String IM_RESULT_REGION_GROUP_QUEUE = "im:result:region:group";

    /**
     * 系统消息发送结果队列
     */
    public static final String IM_RESULT_SYSTEM_QUEUE = "im:result:system";

    // 用户加入的临时地区群聊key
    public static final String IM_USER_TEMP_REGION_GROUP = "im:cache:region-group:user-temp-region-groups:";

    // 地区群聊编号临时用户key
    public static final String IM_REGION_GROUP_NUM_TEMP_USER = "im:cache:region-group:region-group-temp-users:";

    // 用户6小时内临时加入的地区群聊数量统计key
    public static final String IM_REGION_GROUP_USER_TEMP_JOIN = "im:cache:region-group:user-temp-join-num:";

    // 地区群聊群主投票记录key
    public static final String IM_REGION_GROUP_LEADER_VOTE = "im:cache:region-group:leader-vote:";

    // 地区群聊群主投票解除记录key
    public static final String IM_REGION_GROUP_LEADER_VOTE_REMOVE = "im:cache:region-group:leader-vote-remove:";

    // 用户地区群聊投票记录key
    public static final String IM_REGION_GROUP_LEADER_VOTE_RECORD = "im:cache:region-group:leader-vote-record:";

    // 用户地区群聊投票解除群主身份记录key
    public static final String IM_REGION_GROUP_LEADER_VOTE_REMOVE_RECORD = "im:cache:region-group:leader-vote-remove-record:";
}

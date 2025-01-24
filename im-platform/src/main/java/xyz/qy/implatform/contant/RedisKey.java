package xyz.qy.implatform.contant;

public final class RedisKey {
    /**
     *  用户状态 无值:空闲  1:正在忙
     */
    public static final String IM_USER_STATE = "im:user:state";

    // 已读群聊消息位置(已读最大id)
    public final static String IM_GROUP_READED_POSITION = "im:readed:group:position";

    // 已读系统消息位置(已读最大id)
    public final static String IM_SYSTEM_READED_POSITION = "im:readed:system:position";

    // 已读地区群聊消息位置(已读最大id)
    public final static String IM_REGION_GROUP_READED_POSITION = "im:readed:region:group:position";

    /**
     * 私聊离线通知
     */
    public static final String IM_OFFLINE_NOTIFY_PRIVATE = "im:notify:private";

    // 缓存前缀
    public final static String  IM_CACHE = "im:cache:";
    // 缓存是否好友：bool
    public final static String  IM_CACHE_FRIEND = IM_CACHE+"friend";
    // 缓存群聊信息
    public final static String  IM_CACHE_GROUP = IM_CACHE+"group";
    // 缓存群聊成员id
    public final static String IM_CACHE_GROUP_MEMBER_ID = IM_CACHE+"group_member_ids";
    // 验证码 redis key
    public static final String CAPTCHA_CODE_KEY = IM_CACHE + "captcha_codes:";
    // 验证码有效期（分钟）
    public static final Integer CAPTCHA_EXPIRATION = 5;
    // 访客
    public static final String UNIQUE_VISITOR = IM_CACHE + "im_unique_visitor";
    // 登录页媒体信息缓存key
    public static final String LOGIN_MEDIA = IM_CACHE + "media";
    // 登录页是否开启媒体播放
    public static final String ON_OFF_DISPLAY_MEDIA = IM_CACHE + "display_media";
    // 首页是否开启媒体播放
    public static final String ON_OFF_HOME_DISPLAY_MEDIA = "home_display_media";
    /**
     * webrtc 单人通话
     */
    public static final String IM_WEBRTC_PRIVATE_SESSION = "im:webrtc:private:session";
    /**
     * webrtc 群通话
     */
    public static final String IM_WEBRTC_GROUP_SESSION = "im:webrtc:group:session";
    // 绑定邮箱验证码缓存
    public final static String  IM_CACHE_MAIL_BIND = IM_CACHE + "mail:bind:";

    // 百度图片检测accessToken
    public final static String IM_BAIDU_TOKEN = IM_CACHE + "token:baidu";

    // 用户文件上传次数key
    public final static String IM_USER_UPLOAD_FILE = IM_CACHE + "file:upload:userid:";

    // 系统聊天功能开关key
    public final static String IM_SYSTEM_MSG_SWITCH = IM_CACHE + "ban:msg:system:switch:";

    // 用户聊天功能开关key
    public final static String IM_USER_MSG_SWITCH = IM_CACHE + "ban:msg:user:switch:";

    // 群聊聊天功能开关key
    public final static String IM_GROUP_MSG_SWITCH = IM_CACHE + "ban:msg:group:switch:";

    // 群聊成员聊天功能开关key
    public final static String IM_GROUP_MEMBER_MSG_SWITCH = IM_CACHE + "ban:msg:group_member:switch:";

    // 地区群聊聊天功能开关key
    public final static String IM_REGION_GROUP_MSG_SWITCH = IM_CACHE + "ban:msg:region-group:switch:";

    // 地区群聊成员聊天功能开关key
    public final static String IM_REGION_GROUP_MEMBER_MSG_SWITCH = IM_CACHE + "ban:msg:region_group_member:switch:";

    /**
     * 分布式锁前缀
     */
    public static final String IM_LOCK = "im:lock:";

    /**
     * 分布式锁前缀
     */
    public static final String IM_LOCK_RTC_GROUP = IM_LOCK + "rtc:group";

    /**
     * 地区群聊用户发言消息数量限制
     */
    public static final String IM_REGION_GROUP_MSG_LIMIT = IM_CACHE + "region-group:msg-limit:";

    public static final String IM_REGION_GROUP_LEADER_TRANSFER = IM_CACHE + "region-group:leader-transfer:";

    public static final String IM_REGION_GROUP_LEADER_LOCK = IM_LOCK + "region-group:leader-user-lock:";

    public static final String REGION_ACTIVITY_RANGE = IM_CACHE + "region:activity-range";
}

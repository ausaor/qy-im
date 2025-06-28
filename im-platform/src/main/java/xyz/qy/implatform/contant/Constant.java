package xyz.qy.implatform.contant;

public final class Constant {
    /**
     * 系统用户id
     */
    public static final Long SYS_USER_ID = 0L;

    // 管理员用户id
    public static final long ADMIN_USER_ID = 1L;

    // 最大图片上传大小
    public static final long MAX_IMAGE_SIZE = 4 * 1024 * 1024;
    // 最大上传文件大小
    public static final long MAX_FILE_SIZE = 10 * 1024 * 1024;
    // 群聊最大人数
    public static final long MAX_GROUP_MEMBER = 200;

    // 群聊角色最大数量
    public static final int MAX_CHARACTER_NUM = 10;

    // 常量1：是
    public static final int YES = 1;
    // 常量0：否
    public static final int NO = 0;
    // 常量1：是
    public static final String YES_STR = "1";
    // 常量0：否
    public static final String NO_STR = "0";

    // 模板群聊切换时间间隔30MIN
    public static final int SWITCH_INTERVAL = 30;
    // 媒体播放字典key
    public static final String MEDIA_SWITCH = "sys_media_switch";
    // 首页媒体播放字典key
    public static final String HOME_MEDIA_SWITCH = "home_media_switch";

    // 管理员欢迎语
    public static final String ADMIN_WELCOME_MSG = "欢迎您的到来#玫瑰;";
    // 用户可以创建的最多模板群聊数量
    public static final int USER_MAX_TEMPLATE_GROUP_NUM = 10;
    // 用户创建的模板群聊的最多模板人物数量
    public static final int USER_MAX_TEMPLATE_CHARACTER_NUM = 100;
    // 每位模板人物最多配置的人物头像数量
    public static final int MAX_CHARACTER_AVATAR_NUM = 20;
    // 当前页
    public static final String PAGE_NO = "pageNo";
    // 分页大小
    public static final String PAGE_SIZE = "pageSize";
    // 默认分页大小
    public static final String DEFAULT_SIZE = "10";
    // 默认第一页
    public static final String DEFAULT_PAGE_NO = "1";
    // 英文逗号（,）
    public static final String COMMA = ",";

    // 罗马数字1-10
    public static final String[] ROME_NUM = new String[]{"I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X"};

    /**
     * 自动识别json对象白名单配置（仅允许解析的包名，范围越小越安全）
     */
    public static final String[] JSON_WHITELIST_STR = { "org.springframework", "xyz.qy" };

    /**
     * 限流 redis key
     */
    public static final String RATE_LIMIT_KEY = "rate_limit:";
}

package xyz.qy.implatform.enums;

/**
 * 功能开关枚举
 */
public enum FeatureEnum {
    /**
     * 图片上传
     */
    FEATURE_IMAGE_UPLOAD("FEATURE_IMAGE_UPLOAD", "图片上传"),
    
    /**
     * 视频上传
     */
    FEATURE_VIDEO_UPLOAD("FEATURE_VIDEO_UPLOAD", "视频上传"),
    
    /**
     * 音频上传
     */
    FEATURE_AUDIO_UPLOAD("FEATURE_AUDIO_UPLOAD", "音频上传"),
    
    /**
     * 文件上传
     */
    FEATURE_FILE_UPLOAD("FEATURE_FILE_UPLOAD", "文件上传"),
    
    /**
     * 发送私聊消息
     */
    FEATURE_PRIVATE_MESSAGE_SEND("FEATURE_PRIVATE_MESSAGE_SEND", "发送私聊消息"),
    
    /**
     * 发送群聊消息
     */
    FEATURE_GROUP_MESSAGE_SEND("FEATURE_GROUP_MESSAGE_SEND", "发送群聊消息"),
    
    /**
     * 发送地区群聊消息
     */
    FEATURE_REGIONAL_GROUP_MESSAGE_SEND("FEATURE_REGIONAL_GROUP_MESSAGE_SEND", "发送地区群聊消息"),
    
    /**
     * 用户注册
     */
    FEATURE_REGISTER("FEATURE_REGISTER", "用户注册"),
    
    /**
     * 新增音乐
     */
    FEATURE_MUSIC_PUBLISH("FEATURE_MUSIC_PUBLISH", "新增音乐"),
    
    /**
     * 新增动态
     */
    FEATURE_DYNAMIC_PUBLISH("FEATURE_DYNAMIC_PUBLISH", "新增动态"),
    
    /**
     * 动态点赞
     */
    FEATURE_DYNAMIC_LIKE("FEATURE_DYNAMIC_LIKE", "动态点赞"),
    
    /**
     * 动态评论
     */
    FEATURE_DYNAMIC_COMMENT("FEATURE_DYNAMIC_COMMENT", "动态评论");
    
    private final String code;
    private final String name;
    
    FeatureEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getName() {
        return name;
    }
    
    /**
     * 根据code获取name
     *
     * @param code 功能代码
     * @return 功能名称，未找到返回null
     */
    public static String getNameByCode(String code) {
        if (code == null) {
            return null;
        }
        for (FeatureEnum featureEnum : values()) {
            if (featureEnum.getCode().equals(code)) {
                return featureEnum.getName();
            }
        }
        return null;
    }
}

package xyz.qy.implatform.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 功能项编码枚举类，定义系统中所有可控制的功能项
 * @author Polaris
 */
@Getter
@AllArgsConstructor
public enum FeatureCodeEnum {
    LOGIN("FEATURE_LOGIN", "登录"),

    REGISTER("FEATURE_REGISTER", "注册"),

    PRIVATE_MESSAGE_SEND("FEATURE_PRIVATE_MESSAGE_SEND", "私聊消息发送"),

    GROUP_MESSAGE_SEND("FEATURE_GROUP_MESSAGE_SEND", "群聊消息发送"),

    REGIONAL_GROUP_MESSAGE_SEND("FEATURE_REGIONAL_GROUP_MESSAGE_SEND", "地区群聊消息发送"),

    DYNAMIC_PUBLISH("FEATURE_DYNAMIC_PUBLISH", "动态发布"),

    GROUP_DYNAMIC_PUBLISH("FEATURE_GROUP_DYNAMIC_PUBLISH", "群聊动态发布"),

    REGION_DYNAMIC_PUBLISH("FEATURE_REGION_DYNAMIC_PUBLISH", "地区动态发布"),

    DYNAMIC_LIKE("FEATURE_DYNAMIC_LIKE", "动态点赞"),

    DYNAMIC_COMMENT("FEATURE_DYNAMIC_COMMENT", "动态评论"),

    MUSIC_PUBLISH("FEATURE_MUSIC_PUBLISH", "音乐发布"),

    IMAGE_UPLOAD("FEATURE_IMAGE_UPLOAD", "图片上传"),

    VIDEO_UPLOAD("FEATURE_VIDEO_UPLOAD", "视频上传"),

    FILE_UPLOAD("FEATURE_FILE_UPLOAD", "文件上传"),

    AUDIO_UPLOAD("FEATURE_AUDIO_UPLOAD", "音频上传"),
    ;

    // 功能项编码值
    private final String code;

    // 功能项描述
    private final String desc;


    /**
     * 根据编码字符串获取对应的描述
     *
     * @param code 功能项编码字符串
     * @return 对应的枚举描述
     */
    public static String getDescByCode(String code) {
        for (FeatureCodeEnum featureCode : FeatureCodeEnum.values()) {
            if (featureCode.code.equals(code)) {
                return featureCode.desc;
            }
        }
        return null;
    }
}


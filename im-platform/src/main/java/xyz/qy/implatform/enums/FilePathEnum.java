package xyz.qy.implatform.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 文件路径枚举
 *
 * @author yezhiqiu
 * @since 2021/08/04
 */
@Getter
@AllArgsConstructor
public enum FilePathEnum {
    /**
     * 头像路径
     */
    AVATAR("avatar/", "头像路径"),

    /**
     * 图片路径
     */
    IMAGE("image/","图片路径"),

    /**
     * 视频路径
     */
    VIDEO("video/","视频路径"),

    /**
     * 配置图片路径
     */
    CONFIG("config/","配置图片路径"),

    /**
     * 动态图片路径
     */
    TALK("talks/","配置图片路径"),

    /**
     * 语音路径
     */
    VOICE("voice/", "语音路径"),

    /**
     * 音频路径
     */
    AUDIO("audio/", "音频路径"),

    /**
     * 音频路径
     */
    FILE("file/", "文件路径");

    /**
     * 路径
     */
    private final String path;

    /**
     * 描述
     */
    private final String desc;
}

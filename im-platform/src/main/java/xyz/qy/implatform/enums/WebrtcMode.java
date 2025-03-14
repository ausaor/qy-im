package xyz.qy.implatform.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum WebrtcMode {

    /**
     * 视频通话
     */
    VIDEO( "video"),

    /**
     * 语音通话
     */
    VOICE( "voice");

    private final String value;
}


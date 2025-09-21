package xyz.qy.implatform.enums;

public enum SysMsgTypeEnum {
    TEXT(0, "文字"),
    IMAGE(1, "图片"),
    FILE(2, "文件"),
    AUDIO(3, "音频"),
    VIDEO(4, "视频"),
    EXTERNAL_LINK(5, "外部链接"),
    RICH_TEXT(9, "富文本");

    private final int value;
    private final String label;

    SysMsgTypeEnum(int value, String label) {
        this.value = value;
        this.label = label;
    }

    public int getValue() {
        return value;
    }

    public String getLabel() {
        return label;
    }

    /**
     * 根据value获取枚举实例
     */
    public static SysMsgTypeEnum fromValue(int value) {
        for (SysMsgTypeEnum type : SysMsgTypeEnum.values()) {
            if (type.getValue() == value) {
                return type;
            }
        }
        throw new IllegalArgumentException("无效的value: " + value);
    }

    /**
     * 根据label获取枚举实例（如果需要）
     */
    public static SysMsgTypeEnum fromLabel(String label) {
        for (SysMsgTypeEnum type : SysMsgTypeEnum.values()) {
            if (type.getLabel().equals(label)) {
                return type;
            }
        }
        throw new IllegalArgumentException("无效的label: " + label);
    }
}

package xyz.qy.implatform.enums;

/**
 * 关注类别枚举
 */
public enum FollowEnum {
    USER("user", "用户"),
    GROUP("group", "群组"),
    CHARACTER("character", "角色"),
    TEMPLATE("template", "群聊模板");

    private final String code;
    private final String name;

    FollowEnum(String code, String name) {
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
     * 根据code获取枚举
     *
     * @param code 编码
     * @return 枚举对象
     */
    public static String getByCode(String code) {
        for (FollowEnum followEnum : values()) {
            if (followEnum.getCode().equals(code)) {
                return followEnum.getName();
            }
        }
        return "";
    }
}

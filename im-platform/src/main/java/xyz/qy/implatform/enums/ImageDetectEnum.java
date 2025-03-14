package xyz.qy.implatform.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 图片检测枚举
 *
 * @author Polairs
 * @since 2024-10-16
 */
@Getter
@AllArgsConstructor
public enum ImageDetectEnum {
    BAIDU("baidu", "baiduImageDetectStrategyImpl"),

    TENSOR_FLOW("tensorFlow", "tensorFlowImageDetectStrategyImpl");

    /**
     * 模式
     */
    private final String mode;

    /**
     * 策略
     */
    private final String strategy;

    /**
     * 获取策略
     *
     * @param mode 模式
     * @return 搜索策略
     */
    public static String getStrategy(String mode) {
        for (ImageDetectEnum value : ImageDetectEnum.values()) {
            if (value.getMode().equals(mode)) {
                return value.getStrategy();
            }
        }
        return null;
    }
}

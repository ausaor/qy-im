package xyz.qy.implatform.strategy;

/**
 * 图片检测
 *
 * @author Polaris
 * @since 2024-10-13
 */
public interface ImageDetectStrategy {
    /**
     * 图片检测
     *
     * @param filePath 文件路径
     * @return 是否违禁
     */
    boolean handleImageDetect(String filePath);

    /**
     * 图片检测
     *
     * @param imgUrl 图片链接
     * @return 是否违禁
     */
    boolean handleImageDetectByUrl(String imgUrl);
}

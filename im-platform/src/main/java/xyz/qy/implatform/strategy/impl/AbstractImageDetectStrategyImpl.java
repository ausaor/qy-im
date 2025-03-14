package xyz.qy.implatform.strategy.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import xyz.qy.implatform.strategy.ImageDetectStrategy;

/**
 * 图片检测抽象类
 *
 * @author Polaris
 * @since 2024-10-13
 */
@Slf4j
@Service
public abstract class AbstractImageDetectStrategyImpl implements ImageDetectStrategy {
    @Override
    public boolean handleImageDetect(String filePath) {
        return doImageDetect(filePath);
    }

    @Override
    public boolean handleImageDetectByUrl(String imgUrl) {
        return doImageDetectByUrl(imgUrl);
    }

    /**
     * 图片检测抽象方法
     *
     * @param filePath 文件路径
     * @return 是否违禁
     */
    public abstract boolean doImageDetect(String filePath);

    /**
     * 图片检测抽象方法
     *
     * @param imgUrl 图片链接
     * @return 是否违禁
     */
    public abstract boolean doImageDetectByUrl(String imgUrl);
}

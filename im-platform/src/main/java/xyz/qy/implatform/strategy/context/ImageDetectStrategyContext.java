package xyz.qy.implatform.strategy.context;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import xyz.qy.implatform.enums.ImageDetectEnum;
import xyz.qy.implatform.strategy.ImageDetectStrategy;

import java.util.Map;

/**
 * 图片检测上下文
 *
 * @author Polaris
 * @since 2024-10-13
 */
@Service
public class ImageDetectStrategyContext {
    /**
     * 图片检测模式
     */
    @Value("${image.detect.mode}")
    private String detectMode;

    @Autowired
    private Map<String, ImageDetectStrategy> imageDetectStrategyMap;

    public boolean executeImageDetectStrategy(String filePath) {
        return imageDetectStrategyMap.get(ImageDetectEnum.getStrategy(detectMode)).handleImageDetect(filePath);
    }

    public boolean executeImageDetectByUrlStrategy(String imgUrl) {
        return imageDetectStrategyMap.get(ImageDetectEnum.getStrategy(detectMode)).handleImageDetectByUrl(imgUrl);
    }
}

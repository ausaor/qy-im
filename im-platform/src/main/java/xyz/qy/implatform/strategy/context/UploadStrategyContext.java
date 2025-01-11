package xyz.qy.implatform.strategy.context;

import com.alibaba.fastjson.JSONObject;
import xyz.qy.implatform.strategy.UploadStrategy;
import xyz.qy.implatform.vo.UploadImageVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import xyz.qy.implatform.enums.UploadImageModeEnum;
import xyz.qy.implatform.vo.UploadVideoVO;

import java.util.Map;

/**
 * 上传策略上下文
 */
@Service
public class UploadStrategyContext {
    /**
     * 上传模式
     */
    @Value("${upload.mode}")
    private String uploadMode;

    @Autowired
    private Map<String, UploadStrategy> uploadStrategyMap;

    /**
     * 上传图片
     *
     * @param file 文件
     * @param path 路径
     * @return UploadImageVO 图片路径信息
     */
    public UploadImageVO executeUploadImageStrategy(MultipartFile file, String path) {
        return uploadStrategyMap.get(UploadImageModeEnum.getStrategy(uploadMode)).uploadImageCommon(file, path);
    }

    /**
     * 上传视频
     *
     * @param file 文件
     * @param path 路径
     * @return UploadImageVO 图片路径信息
     */
    public UploadVideoVO executeUploadVideoStrategy(MultipartFile file, String path) {
        return uploadStrategyMap.get(UploadImageModeEnum.getStrategy(uploadMode)).uploadVideoCommon(file, path);
    }

    /**
     * 上传音频
     *
     * @param file 文件
     * @param path 路径
     * @return UploadImageVO 图片路径信息
     */
    public JSONObject executeUploadAudioStrategy(MultipartFile file, String path) {
        return uploadStrategyMap.get(UploadImageModeEnum.getStrategy(uploadMode)).uploadAudioCommon(file, path);
    }

    /**
     * 上传文件
     *
     * @param file 文件
     * @param path 路径
     * @return 文件链接
     */
    public String executeUploadFileStrategy(MultipartFile file, String path) {
        return uploadStrategyMap.get(UploadImageModeEnum.getStrategy(uploadMode)).uploadFileCommon(file, path);
    }
}

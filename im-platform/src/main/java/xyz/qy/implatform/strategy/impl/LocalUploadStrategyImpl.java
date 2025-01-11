package xyz.qy.implatform.strategy.impl;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import xyz.qy.implatform.entity.DictData;
import xyz.qy.implatform.enums.FilePathEnum;
import xyz.qy.implatform.exception.GlobalException;
import xyz.qy.implatform.mapper.DictDataMapper;
import xyz.qy.implatform.strategy.context.ImageDetectStrategyContext;
import xyz.qy.implatform.vo.UploadImageVO;
import xyz.qy.implatform.vo.UploadVideoVO;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Random;

/**
 * 本地上传策略
 *
 * @author Polaris
 * @since 2023-08-19
 */
@Slf4j
@Service("localUploadStrategyImpl")
public class LocalUploadStrategyImpl extends AbstractUploadStrategyImpl {
    /**
     * 本地路径
     */
    @Value("${upload.local.path}")
    private String localPath;

    /**
     * 访问url
     */
    @Value("${upload.local.url}")
    private String localUrl;

    @Resource
    private ImageDetectStrategyContext imageDetectStrategyContext;

    @Resource
    private DictDataMapper dictDataMapper;

    @Override
    public Boolean exists(String filePath) {
        return new File(localPath + filePath).exists();
    }

    @Override
    public UploadImageVO uploadImage(String path, String fileName, MultipartFile file) throws IOException {
        // 判断目录是否存在
        createDirectory(localPath + path);
        UploadImageVO uploadImageVO = new UploadImageVO();
        File targetFile = new File(localPath + path, fileName);
        if (targetFile.createNewFile()) {
            FileUtils.writeByteArrayToFile(targetFile, file.getBytes());
            if (isOpenImageDetect() && !imageDetectStrategyContext.executeImageDetectStrategy(localPath + path + fileName)) {
                targetFile.delete();
                throw new GlobalException("图片存在违禁内容，禁止上传");
            }
            String url = localUrl + path + fileName;
            uploadImageVO.setOriginUrl(url);
            uploadImageVO.setThumbUrl(url);
        }
        return uploadImageVO;
    }

    @Override
    public UploadVideoVO uploadVideo(String path, String fileName, MultipartFile file) throws IOException {
        // 判断目录是否存在
        createDirectory(localPath + path);
        UploadVideoVO uploadVideoVO = new UploadVideoVO();
        File targetFile = new File(localPath + path, fileName);
        if (targetFile.createNewFile()) {
            FileUtils.writeByteArrayToFile(targetFile, file.getBytes());
            String url = localUrl + path + fileName;
            // 获取封面图片
            String coverUrl = randomGrabberFFmpegImage(targetFile, fileName);
            uploadVideoVO.setVideoUrl(url);
            uploadVideoVO.setCoverUrl(coverUrl);
        }
        return uploadVideoVO;
    }

    @Override
    public JSONObject uploadAudio(String path, String fileName, MultipartFile file) throws IOException {
        // 判断目录是否存在
        createDirectory(localPath + path);
        JSONObject jsonObject = new JSONObject();
        File targetFile = new File(localPath + path, fileName);
        if (targetFile.createNewFile()) {
            FileUtils.writeByteArrayToFile(targetFile, file.getBytes());
            String url = localUrl + path + fileName;
            jsonObject.put("url", url);
        }
        return jsonObject;
    }

    @Override
    public String uploadFile(String path, String fileName, MultipartFile file) throws IOException {
        // 判断目录是否存在
        String url = null;
        createDirectory(localPath + path);
        File targetFile = new File(localPath + path, fileName);
        if (targetFile.createNewFile()) {
            FileUtils.writeByteArrayToFile(targetFile, file.getBytes());
            url = localUrl + path + fileName;
        }
        return url;
    }

    @Override
    public UploadImageVO getImageInfo(String path, String fileName) {
        UploadImageVO uploadImageVO = new UploadImageVO();
        String url = localUrl + path + fileName;
        uploadImageVO.setOriginUrl(url);
        uploadImageVO.setThumbUrl(url);
        return uploadImageVO;
    }

    @Override
    public UploadVideoVO getVideoInfo(String path, String fileName) {
        UploadVideoVO uploadVideoVO = new UploadVideoVO();
        String url = localUrl + path + fileName;
        String coverUrl = localUrl + FilePathEnum.IMAGE.getPath() + fileName + ".jpg";
        uploadVideoVO.setVideoUrl(url);
        uploadVideoVO.setCoverUrl(coverUrl);
        return uploadVideoVO;
    }

    @Override
    public JSONObject getAudioInfo(String path, String fileName) {
        JSONObject jsonObject = new JSONObject();
        String url = localUrl + path + fileName;
        jsonObject.put("url", url);
        return jsonObject;
    }

    @Override
    public String getFileUrl(String path, String fileName) {
        return localUrl + path + fileName;
    }

    private void createDirectory(String dirPath) {
        File directory = new File(dirPath);
        if (!directory.exists()) {
            if (!directory.mkdirs()) {
                throw new GlobalException("创建目录失败");
            }
        }
    }

    private boolean isOpenImageDetect() {
        LambdaQueryWrapper<DictData> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(DictData::getDictType, "open_image_detect");
        DictData dictData1 = dictDataMapper.selectOne(lambdaQueryWrapper);
        if (ObjectUtil.isNotNull(dictData1) && "YES".equals(dictData1.getDictValue())) {
            return true;
        }
        return false;
    }

    /**
     * 随机获取视频截图
     *
     * @param videFile 视频文件
     * @return 截图列表
     * */
    public String randomGrabberFFmpegImage(File videFile, String fileName) {
        FFmpegFrameGrabber grabber = null;
        try (ByteArrayOutputStream outStream =new ByteArrayOutputStream();) {
            grabber = new FFmpegFrameGrabber(videFile);
            grabber.start();
            // 获取视频总帧数
            // int lengthInVideoFrames = grabber.getLengthInVideoFrames();
            // 获取视频时长， / 1000000 将单位转换为秒
            long delayedTime = grabber.getLengthInTime() / 1000000;

            Random random = new Random();
            // 跳转到响应时间
            grabber.setTimestamp((random.nextInt((int)delayedTime - 1) + 1) * 1000000L);
            Frame f = grabber.grabImage();
            Java2DFrameConverter converter = new Java2DFrameConverter();
            BufferedImage bi = converter.getBufferedImage(f);

            ImageIO.write(bi, "jpg", outStream);

            File targetFile = new File(localPath + FilePathEnum.IMAGE.getPath(), fileName + ".jpg");
            if (targetFile.createNewFile()) {
                FileUtils.writeByteArrayToFile(targetFile, outStream.toByteArray());
                return localUrl + FilePathEnum.IMAGE.getPath() + fileName + ".jpg";
            }
        } catch (Exception e) {
            log.error("getVideoInfo grabber.release failed 获取文件信息失败：{}", e.getMessage());
        } finally {
            try {
                if (grabber != null) {
                    grabber.stop();
                    grabber.release();
                }
            } catch (FFmpegFrameGrabber.Exception e) {
                log.error("getVideoInfo grabber.release failed 获取文件信息失败：{}", e.getMessage());
            }
        }
        return "";
    }
}

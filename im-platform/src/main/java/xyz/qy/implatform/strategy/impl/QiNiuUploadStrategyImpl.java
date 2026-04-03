package xyz.qy.implatform.strategy.impl;

import cn.hutool.core.lang.UUID;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.io.FileUtils;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import xyz.qy.implatform.config.QiNiuConfigProperties;
import xyz.qy.implatform.exception.GlobalException;
import xyz.qy.implatform.session.SessionContext;
import xyz.qy.implatform.strategy.context.ImageDetectStrategyContext;
import xyz.qy.implatform.vo.UploadImageVO;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.Region;
import com.qiniu.storage.UploadManager;
import com.qiniu.storage.model.DefaultPutRet;
import com.qiniu.util.Auth;
import com.qiniu.util.IOUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import xyz.qy.implatform.vo.UploadVideoVO;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Random;

@Slf4j
@Service("qiNiuUploadStrategyImpl")
public class QiNiuUploadStrategyImpl extends AbstractUploadStrategyImpl {

    @Autowired
    private QiNiuConfigProperties qiNiuConfigProperties;

    @Resource
    private ImageDetectStrategyContext imageDetectStrategyContext;

    @Override
    public Boolean exists(String filePath) {
        return false;
    }

    @Override
    protected String getStorageType() {
        return "qiniu";
    }

    @Override
    public UploadImageVO uploadImage(String path, String fileName, MultipartFile file) throws IOException {
        UploadImageVO uploadImageVO = new UploadImageVO();
        Configuration cfg = new Configuration(Region.huanan());
        UploadManager uploadManager = new UploadManager(cfg);
        byte[] bytes = IOUtils.toByteArray(file.getInputStream());
        Auth auth = Auth.create(qiNiuConfigProperties.getAccessKey(), qiNiuConfigProperties.getSecretKey());
        String token = auth.uploadToken(qiNiuConfigProperties.getBucket());
        Response response = uploadManager.put(bytes, path + fileName, token);
        DefaultPutRet putRet = JSON.parseObject(response.bodyString(), DefaultPutRet.class);
        log.info("putRet{}", putRet);
        String url = qiNiuConfigProperties.getDomain() + path + fileName;
        if (!imageDetectStrategyContext.executeImageDetectByUrlStrategy(url)) {
            throw new GlobalException("图片存在违禁内容，禁止上传");
        }
        uploadImageVO.setOriginUrl(url);
        uploadImageVO.setThumbUrl(url);
        return uploadImageVO;
    }

    @Override
    public UploadVideoVO uploadVideo(String path, String fileName, MultipartFile file) throws IOException {
        Long userId = SessionContext.getSession().getUserId();
        UploadVideoVO uploadVideoVO = new UploadVideoVO();
        
        // 上传视频到七牛云
        Configuration cfg = new Configuration(Region.huanan());
        UploadManager uploadManager = new UploadManager(cfg);
        byte[] videoBytes = IOUtils.toByteArray(file.getInputStream());
        Auth auth = Auth.create(qiNiuConfigProperties.getAccessKey(), qiNiuConfigProperties.getSecretKey());
        String token = auth.uploadToken(qiNiuConfigProperties.getBucket());
        Response response = uploadManager.put(videoBytes, path + fileName, token);
        DefaultPutRet putRet = JSON.parseObject(response.bodyString(), DefaultPutRet.class);
        log.info("video putRet{}", putRet);
        String videoUrl = qiNiuConfigProperties.getDomain() + path + fileName;
        uploadVideoVO.setVideoUrl(videoUrl);
        
        // 生成封面图片
        String coverUrl = randomGrabberFFmpegImage(file, path);
        uploadVideoVO.setCoverUrl(coverUrl);
        
        log.info("七牛云 - 上传视频成功，用户 id:{},videoUrl:{}", userId, videoUrl);
        return uploadVideoVO;
    }

    @Override
    public JSONObject uploadAudio(String path, String fileName, MultipartFile file) throws IOException {
        Long userId = SessionContext.getSession().getUserId();
        
        // 上传音频到七牛云
        Configuration cfg = new Configuration(Region.huanan());
        UploadManager uploadManager = new UploadManager(cfg);
        byte[] audioBytes = IOUtils.toByteArray(file.getInputStream());
        Auth auth = Auth.create(qiNiuConfigProperties.getAccessKey(), qiNiuConfigProperties.getSecretKey());
        String token = auth.uploadToken(qiNiuConfigProperties.getBucket());
        Response response = uploadManager.put(audioBytes, path + fileName, token);
        DefaultPutRet putRet = JSON.parseObject(response.bodyString(), DefaultPutRet.class);
        log.info("audio putRet{}", putRet);
        String audioUrl = qiNiuConfigProperties.getDomain() + path + fileName;
        
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("url", audioUrl);
        
        log.info("七牛云 - 上传音频成功，用户 id:{},url:{}", userId, audioUrl);
        return jsonObject;
    }

    @Override
    public String uploadFile(String path, String fileName, MultipartFile file) throws IOException {
        Configuration cfg = new Configuration(Region.huanan());
        UploadManager uploadManager = new UploadManager(cfg);
        byte[] bytes = IOUtils.toByteArray(file.getInputStream());
        Auth auth = Auth.create(qiNiuConfigProperties.getAccessKey(), qiNiuConfigProperties.getSecretKey());
        String token = auth.uploadToken(qiNiuConfigProperties.getBucket());
        Response response = uploadManager.put(bytes, path + fileName, token);
        DefaultPutRet putRet = JSON.parseObject(response.bodyString(), DefaultPutRet.class);
        log.info("putRet{}", putRet);
        return qiNiuConfigProperties.getDomain() + path + fileName;
    }

    @Override
    public UploadImageVO getImageInfo(String path, String fileName) {
        UploadImageVO uploadImageVO = new UploadImageVO();
        String url = qiNiuConfigProperties.getDomain() + path + fileName;
        uploadImageVO.setOriginUrl(url);
        uploadImageVO.setThumbUrl(url);
        return uploadImageVO;
    }

    @Override
    public UploadVideoVO getVideoInfo(String path, String fileName) {
        return null;
    }

    @Override
    public JSONObject getAudioInfo(String path, String fileName) {
        return null;
    }

    @Override
    public String getFileUrl(String path, String fileName) {
        return qiNiuConfigProperties.getDomain() + path + fileName;
    }

    /**
     * 随机获取视频截图
     *
     * @param file 视频文件
     * @param path 存储路径
     * @return 截图 URL
     */
    public String randomGrabberFFmpegImage(MultipartFile file, String path) {
        FFmpegFrameGrabber grabber = null;
        try (ByteArrayOutputStream outStream = new ByteArrayOutputStream()) {
            // 保存临时文件
            String tempDir = System.getProperty("java.io.tmpdir");
            File targetFile = new File(tempDir, UUID.randomUUID().toString(false) + ".tmp");
            FileUtils.writeByteArrayToFile(targetFile, file.getBytes());
            
            grabber = new FFmpegFrameGrabber(targetFile);
            grabber.start();
            // 获取视频时长，/ 1000000 将单位转换为秒
            long delayedTime = grabber.getLengthInTime() / 1000000;

            Random random = new Random();
            // 跳转到响应时间
            if (delayedTime > 1) {
                grabber.setTimestamp((random.nextInt((int) delayedTime - 1) + 1) * 1000000L);
            } else {
                grabber.setTimestamp(0L);
            }
            Frame f = grabber.grabImage();
            Java2DFrameConverter converter = new Java2DFrameConverter();
            BufferedImage bi = converter.getBufferedImage(f);

            ImageIO.write(bi, "jpg", outStream);

            MultipartFile multipartFile = convertOutputStreamToMultipartFile(outStream, UUID.randomUUID().toString(false) + ".jpg");
            
            // 上传封面图片到七牛云
            Configuration cfg = new Configuration(Region.huanan());
            UploadManager uploadManager = new UploadManager(cfg);
            byte[] imageBytes = IOUtils.toByteArray(multipartFile.getInputStream());
            Auth auth = Auth.create(qiNiuConfigProperties.getAccessKey(), qiNiuConfigProperties.getSecretKey());
            String token = auth.uploadToken(qiNiuConfigProperties.getBucket());
            String coverFileName = UUID.randomUUID().toString(false) + ".jpg";
            Response response = uploadManager.put(imageBytes, path + coverFileName, token);
            DefaultPutRet putRet = JSON.parseObject(response.bodyString(), DefaultPutRet.class);
            log.info("cover putRet{}", putRet);
            
            targetFile.delete();
            return qiNiuConfigProperties.getDomain() + path + coverFileName;
        } catch (Exception e) {
            log.error("randomGrabberFFmpegImage error", e);
        } finally {
            try {
                if (grabber != null) {
                    grabber.stop();
                    grabber.release();
                }
            } catch (FFmpegFrameGrabber.Exception e) {
                log.error("randomGrabberFFmpegImage grabber.release failed", e);
            }
        }
        return "";
    }

    public MultipartFile convertOutputStreamToMultipartFile(OutputStream outputStream, String fileName) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = null;
        try {
            // 使用 ByteArrayOutputStream
            byteArrayOutputStream = (ByteArrayOutputStream) outputStream;

            // 获取输出流中的字节数组
            byte[] byteArray = byteArrayOutputStream.toByteArray();

            // 创建 FileItem
            FileItem fileItem = new DiskFileItem("file", "application/octet-stream", false, fileName, byteArray.length, null);
            fileItem.getOutputStream().write(byteArray);

            // 创建 MultipartFile
            return new CommonsMultipartFile(fileItem);
        } catch (Exception e) {
            log.error("convertOutputStreamToMultipartFile error", e);
        } finally {
            if (byteArrayOutputStream != null) {
                byteArrayOutputStream.close();
            }
        }

        return null;
    }
}

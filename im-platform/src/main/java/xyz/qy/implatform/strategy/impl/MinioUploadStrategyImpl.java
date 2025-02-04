package xyz.qy.implatform.strategy.impl;

import cn.hutool.core.lang.UUID;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.io.FileUtils;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import xyz.qy.implatform.config.props.MinioProperties;
import xyz.qy.implatform.enums.FileType;
import xyz.qy.implatform.enums.ResultCode;
import xyz.qy.implatform.exception.GlobalException;
import xyz.qy.implatform.session.SessionContext;
import xyz.qy.implatform.util.ImageUtil;
import xyz.qy.implatform.util.MinioUtil;
import xyz.qy.implatform.vo.UploadImageVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import xyz.qy.implatform.vo.UploadVideoVO;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;
import java.util.Random;

@Slf4j
@Service("minioUploadStrategyImpl")
public class MinioUploadStrategyImpl extends AbstractUploadStrategyImpl {
    @Resource
    private MinioUtil minioUtil;

    @Resource
    private MinioProperties minioProps;

    @PostConstruct
    public void init() {
        if (!minioUtil.bucketExists(minioProps.getBucketName())) {
            // 创建bucket
            minioUtil.makeBucket(minioProps.getBucketName());
            // 公开bucket
            minioUtil.setBucketPublic(minioProps.getBucketName());
        }
    }

    @Override
    public Boolean exists(String filePath) {
        return false;
    }

    @Override
    public UploadImageVO uploadImage(String path, String fileName, MultipartFile file) throws IOException {
        Long userId = SessionContext.getSession().getUserId();
        // 上传原图
        UploadImageVO vo = new UploadImageVO();
        String miniofileName = minioUtil.upload(minioProps.getBucketName(), minioProps.getImagePath(), file);
        if (StringUtils.isEmpty(miniofileName)) {
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "图片上传失败");
        }
        vo.setOriginUrl(generUrl(FileType.IMAGE, miniofileName));

        // 大于30K的文件需上传缩略图
        if (file.getSize() > 30 * 1024) {
            byte[] imageByte = ImageUtil.compressForScale(file.getBytes(), 30);
            miniofileName = minioUtil.upload(minioProps.getBucketName(), minioProps.getImagePath(), Objects.requireNonNull(file.getOriginalFilename()), imageByte, file.getContentType());
            if (StringUtils.isEmpty(miniofileName)) {
                throw new GlobalException(ResultCode.PROGRAM_ERROR, "图片上传失败");
            }
        }
        vo.setThumbUrl(generUrl(FileType.IMAGE, miniofileName));

        log.info("minio-上传图片成功，用户id:{},url:{}", userId, vo.getOriginUrl());
        return vo;
    }

    public String generUrl(FileType fileTypeEnum, String fileName) {
        String url = minioProps.getDomain() + "/" + minioProps.getBucketName();
        switch (fileTypeEnum) {
            case FILE:
                url += "/" + minioProps.getFilePath() + "/";
                break;
            case IMAGE:
                url += "/" + minioProps.getImagePath() + "/";
                break;
            case VIDEO:
                url += "/" + minioProps.getVideoPath() + "/";
                break;
            case AUDIO:
                url += "/" + minioProps.getAudioPath() + "/";
                break;
            default:
                break;
        }
        url += fileName;
        return url;
    }

    @Override
    public String uploadFile(String path, String fileName, MultipartFile file) {
        Long userId = SessionContext.getSession().getUserId();
        // 上传
        String minioFileName = minioUtil.upload(minioProps.getBucketName(), minioProps.getFilePath(), file);
        if (StringUtils.isEmpty(minioFileName)) {
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "文件上传失败");
        }
        String url = generUrl(FileType.FILE, minioFileName);
        log.info("minio-上传文件成功，用户id:{},url:{}", userId, url);
        return url;
    }

    @Override
    public UploadVideoVO uploadVideo(String path, String fileName, MultipartFile file) throws IOException {
        Long userId = SessionContext.getSession().getUserId();
        // 上传
        String minioFileName = minioUtil.upload(minioProps.getBucketName(), minioProps.getVideoPath(), file);
        if (StringUtils.isEmpty(minioFileName)) {
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "文件上传失败");
        }
        String url = generUrl(FileType.VIDEO, minioFileName);
        log.info("minio-上传视频成功，用户id:{},url:{}", userId, url);
        UploadVideoVO uploadVideoVO = new UploadVideoVO();
        uploadVideoVO.setVideoUrl(url);

        // 获取临时目录路径
        String tempDir = System.getProperty("java.io.tmpdir");
        File targetFile = new File(tempDir, fileName);
        if (targetFile.createNewFile()) {
            FileUtils.writeByteArrayToFile(targetFile, file.getBytes());
            // 获取封面图片
            String coverUrl = randomGrabberFFmpegImage(targetFile);
            uploadVideoVO.setCoverUrl(coverUrl);
            targetFile.delete();
        }

        return uploadVideoVO;
    }

    @Override
    public JSONObject uploadAudio(String path, String fileName, MultipartFile file) {
        Long userId = SessionContext.getSession().getUserId();
        // 上传
        String minioFileName = minioUtil.upload(minioProps.getBucketName(), minioProps.getAudioPath(), file);
        if (StringUtils.isEmpty(minioFileName)) {
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "文件上传失败");
        }
        String url = generUrl(FileType.AUDIO, minioFileName);
        log.info("minio-上传音频成功，用户id:{},url:{}", userId, url);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("url", url);
        return jsonObject;
    }

    @Override
    public UploadImageVO getImageInfo(String path, String fileName) {
        return null;
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
        return null;
    }

    /**
     * 随机获取视频截图
     *
     * @param videFile 视频文件
     * @return 截图列表
     * */
    public String randomGrabberFFmpegImage(File videFile) {
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

            MultipartFile multipartFile = convertOutputStreamToMultipartFile(outStream, UUID.randomUUID().toString(false) + ".jpg");
            String miniofileName = minioUtil.upload(minioProps.getBucketName(), minioProps.getImagePath(), multipartFile);
            if (StringUtils.isNotBlank(miniofileName)) {
                return generUrl(FileType.IMAGE, miniofileName);
            }
        } catch (Exception e) {
            log.error("getVideoInfo grabber.release failed 获取文件信息失败。", e);
        } finally {
            try {
                if (grabber != null) {
                    grabber.stop();
                    grabber.release();
                }
            } catch (FFmpegFrameGrabber.Exception e) {
                log.error("getVideoInfo grabber.release failed 获取文件信息失败。", e);
            }
        }
        return "";
    }

    public MultipartFile convertOutputStreamToMultipartFile(OutputStream outputStream, String fileName) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = null;
        try {
            // 使用ByteArrayOutputStream
            byteArrayOutputStream = (ByteArrayOutputStream) outputStream;

            // 获取输出流中的字节数组
            byte[] byteArray = byteArrayOutputStream.toByteArray();

            // 将字节数组转为ByteArrayInputStream
            //ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArray);

            // 创建FileItem
            FileItem fileItem = new DiskFileItem("file", "application/octet-stream", false, fileName, byteArray.length, null);
            fileItem.getOutputStream().write(byteArray);

            // 创建MultipartFile
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

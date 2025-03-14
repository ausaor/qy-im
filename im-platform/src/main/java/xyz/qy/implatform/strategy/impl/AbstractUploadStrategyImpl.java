package xyz.qy.implatform.strategy.impl;

import com.alibaba.fastjson.JSONObject;
import xyz.qy.implatform.contant.Constant;
import xyz.qy.implatform.enums.ResultCode;
import xyz.qy.implatform.exception.GlobalException;
import xyz.qy.implatform.strategy.UploadStrategy;
import xyz.qy.implatform.util.FileUtil;
import xyz.qy.implatform.util.FileUtils;
import xyz.qy.implatform.vo.UploadImageVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import xyz.qy.implatform.vo.UploadVideoVO;

import java.io.IOException;

/**
 * 图片检测模板
 *
 * @author Polaris
 * @since 2024-10-13
 */
@Slf4j
@Service
public abstract class AbstractUploadStrategyImpl implements UploadStrategy {

    @Override
    public UploadImageVO uploadImageCommon(MultipartFile file, String path) {
        UploadImageVO uploadImageVO = null;
        try {
            // 大小校验
            if (file.getSize() > Constant.MAX_IMAGE_SIZE) {
                throw new GlobalException(ResultCode.PROGRAM_ERROR, "图片大小不能超过4M");
            }
            // 图片格式校验
            if (!FileUtil.isImage(file.getOriginalFilename())) {
                throw new GlobalException(ResultCode.PROGRAM_ERROR, "图片格式不合法");
            }

            // 获取文件md5值
            String md5 = FileUtils.getMd5(file.getInputStream());
            // 获取文件扩展名
            String extName = FileUtils.getExtName(file.getOriginalFilename());
            // 获取文件名称
            String originalName = FileUtils.getFileName(file.getOriginalFilename());
            // 重新生成文件名
            String fileName = md5 + extName;
            // 判断文件是否已存在
            if (!exists(path + fileName)) {
                // 不存在则继续上传
                uploadImageVO = uploadImage(path, fileName, file);
            } else {
                uploadImageVO = getImageInfo(path, fileName);
            }
            uploadImageVO.setName(originalName);
            uploadImageVO.setOriginName(originalName + extName);
            // 返回文件访问路径
            return uploadImageVO;
        } catch (Exception e) {
            log.error("上传图片异常:{}", e.getMessage());
            throw new GlobalException(e.getMessage());
        }
    }

    @Override
    public UploadVideoVO uploadVideoCommon(MultipartFile file, String path) {
        UploadVideoVO uploadVideoVO = null;
        try {
            // 大小校验
            if (file.getSize() > Constant.MAX_FILE_SIZE) {
                throw new GlobalException(ResultCode.PROGRAM_ERROR, "视频大小不能超过10M");
            }
            // 图片格式校验
            if (!FileUtil.isVideo(file.getOriginalFilename())) {
                throw new GlobalException(ResultCode.PROGRAM_ERROR, "视频格式不合法");
            }

            // 获取文件md5值
            String md5 = FileUtils.getMd5(file.getInputStream());
            // 获取文件扩展名
            String extName = FileUtils.getExtName(file.getOriginalFilename());
            // 获取文件名称
            String originalName = FileUtils.getFileName(file.getOriginalFilename());
            // 重新生成文件名
            String fileName = md5 + extName;
            // 判断文件是否已存在
            if (!exists(path + fileName)) {
                // 不存在则继续上传
                uploadVideoVO = uploadVideo(path, fileName, file);
            } else {
                uploadVideoVO = getVideoInfo(path, fileName);
            }
            // 返回文件访问路径
            return uploadVideoVO;
        } catch (Exception e) {
            log.error("上传视频异常:{}", e.getMessage());
            throw new GlobalException(e.getMessage());
        }
    }

    @Override
    public JSONObject uploadAudioCommon(MultipartFile file, String path) {
        JSONObject jsonObject = null;
        try {
            // 大小校验
            if (file.getSize() > Constant.MAX_FILE_SIZE) {
                throw new GlobalException(ResultCode.PROGRAM_ERROR, "音频大小不能超过10M");
            }
            // 音频格式校验
            if (!FileUtil.isAudio(file.getOriginalFilename())) {
                throw new GlobalException(ResultCode.PROGRAM_ERROR, "音频格式不合法");
            }

            // 获取文件md5值
            String md5 = FileUtils.getMd5(file.getInputStream());
            // 获取文件扩展名
            String extName = FileUtils.getExtName(file.getOriginalFilename());
            // 获取文件名称
            String originalName = FileUtils.getFileName(file.getOriginalFilename());
            // 重新生成文件名
            String fileName = md5 + extName;
            // 判断文件是否已存在
            if (!exists(path + fileName)) {
                // 不存在则继续上传
                jsonObject = uploadAudio(path, fileName, file);
            } else {
                jsonObject = getAudioInfo(path, fileName);
            }
            jsonObject.put("originalName", originalName);
            return jsonObject;
        } catch (Exception e) {
            log.error("上传音频异常:{}", e.getMessage());
            throw new GlobalException(e.getMessage());
        }
    }

    @Override
    public String uploadFileCommon(MultipartFile file, String path) {
        String url = null;
        try {
            // 大小校验
            if(file.getSize() > Constant.MAX_FILE_SIZE){
                throw new GlobalException(ResultCode.PROGRAM_ERROR,"文件大小不能超过10M");
            }
            // 获取文件md5值
            String md5 = FileUtils.getMd5(file.getInputStream());
            // 获取文件扩展名
            String extName = FileUtils.getExtName(file.getOriginalFilename());
            // 重新生成文件名
            String fileName = md5 + extName;
            // 判断文件是否已存在
            if (!exists(path + fileName)) {
                // 不存在则继续上传
                url = uploadFile(path, fileName, file);
            } else {
                url = getFileUrl(path, fileName);
            }
        } catch (Exception e) {
            log.error("上传文件异常:{}", e.getMessage());
            throw new GlobalException("文件上传失败");
        }
        return url;
    }

    /**
     * 判断文件是否存在
     *
     * @param filePath 文件路径
     * @return {@link Boolean}
     */
    public abstract Boolean exists(String filePath);

    /**
     * 上传图片
     *
     * @param path     路径
     * @param fileName 文件名
     * @param file     文件对象
     * @throws IOException io异常
     */
    public abstract UploadImageVO uploadImage(String path, String fileName, MultipartFile file) throws IOException;

    /**
     * 上传视频
     *
     * @param path     路径
     * @param fileName 文件名
     * @param file     文件对象
     * @throws IOException io异常
     */
    public abstract UploadVideoVO uploadVideo(String path, String fileName, MultipartFile file) throws IOException;

    /**
     * 上传音频
     *
     * @param path     路径
     * @param fileName 文件名
     * @param file     文件对象
     * @throws IOException io异常
     */
    public abstract JSONObject uploadAudio(String path, String fileName, MultipartFile file) throws IOException;

    /**
     * 上传文件
     *
     * @param path     路径
     * @param fileName 文件名
     * @param file     文件对象
     * @throws IOException io异常
     */
    public abstract String uploadFile(String path, String fileName, MultipartFile file) throws IOException;

    /**
     * 获取文件信息
     *
     * @param path 路径
     * @param fileName 文件名
     * @return 图片名称
     */
    public abstract UploadImageVO getImageInfo(String path, String fileName);

    /**
     * 获取视频信息
     *
     * @param path 路径
     * @param fileName 文件名
     * @return 视频信息
     */
    public abstract UploadVideoVO getVideoInfo(String path, String fileName);

    /**
     * 获取音频信息
     *
     * @param path 路径
     * @param fileName 文件名
     * @return 视频信息
     */
    public abstract JSONObject getAudioInfo(String path, String fileName);

    /**
     * 获取文件路径
     *
     * @param path 路径
     * @param fileName 文件名
     * @return 文件路径
     */
    public abstract String getFileUrl(String path, String fileName);
}

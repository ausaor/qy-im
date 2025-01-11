package xyz.qy.implatform.strategy.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import xyz.qy.implatform.config.QiNiuConfigProperties;
import xyz.qy.implatform.exception.GlobalException;
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
import java.io.IOException;

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
        return null;
    }

    @Override
    public JSONObject uploadAudio(String path, String fileName, MultipartFile file) throws IOException {
        return null;
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
}

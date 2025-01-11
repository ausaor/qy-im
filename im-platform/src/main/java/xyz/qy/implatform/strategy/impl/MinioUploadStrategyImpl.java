//package xyz.qy.implatform.strategy.impl;
//
//import xyz.qy.implatform.enums.FileType;
//import xyz.qy.implatform.enums.ResultCode;
//import xyz.qy.implatform.exception.GlobalException;
//import xyz.qy.implatform.session.SessionContext;
//import xyz.qy.implatform.util.ImageUtil;
//import xyz.qy.implatform.util.MinioUtil;
//import xyz.qy.implatform.vo.UploadImageVO;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.commons.lang3.StringUtils;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.io.IOException;
//
//@Slf4j
//@Service("minioUploadStrategyImpl")
//public class MinioUploadStrategyImpl extends AbstractUploadStrategyImpl {
//    @Autowired
//    private MinioUtil minioUtil;
//
//    @Value("${minio.public}")
//    private String minIOServer;
//    @Value("${minio.bucketName}")
//    private String bucketName;
//
//    @Value("${minio.imagePath}")
//    private String imagePath;
//
//    @Value("${minio.filePath}")
//    private String filePath;
//
//    @Override
//    public Boolean exists(String filePath) {
//        return false;
//    }
//
//    @Override
//    public UploadImageVO uploadImage(String path, String fileName, MultipartFile file) throws IOException {
//        Long userId = SessionContext.getSession().getUserId();
//        // 上传原图
//        UploadImageVO vo = new UploadImageVO();
//        String miniofileName = minioUtil.upload(bucketName, imagePath, file);
//        if (StringUtils.isEmpty(fileName)) {
//            throw new GlobalException(ResultCode.PROGRAM_ERROR, "图片上传失败");
//        }
//        vo.setOriginUrl(generUrl(FileType.IMAGE, fileName));
//        // 上传缩略图
//        byte[] imageByte = ImageUtil.compressForScale(file.getBytes(), 100);
//        fileName = minioUtil.upload(bucketName, imagePath, file.getOriginalFilename(), imageByte, file.getContentType());
//        if (StringUtils.isEmpty(fileName)) {
//            throw new GlobalException(ResultCode.PROGRAM_ERROR, "图片上传失败");
//        }
//        vo.setThumbUrl(generUrl(FileType.IMAGE, miniofileName));
//        log.info("文件图片成功，用户id:{},url:{}", userId, vo.getOriginUrl());
//        return vo;
//    }
//
//    public String generUrl(FileType fileTypeEnum, String fileName) {
//        String url = minIOServer + "/" + bucketName;
//        switch (fileTypeEnum) {
//            case FILE:
//                url += "/file/";
//                break;
//            case IMAGE:
//                url += "/image/";
//                break;
//            case VIDEO:
//                url += "/video/";
//                break;
//        }
//        url += fileName;
//        return url;
//    }
//
//    @Override
//    public String uploadFile(String path, String fileName, MultipartFile file) throws IOException {
//        Long userId = SessionContext.getSession().getUserId();
//        // 上传
//        String minioFileName = minioUtil.upload(bucketName, filePath, file);
//        if (StringUtils.isEmpty(fileName)) {
//            throw new GlobalException(ResultCode.PROGRAM_ERROR, "文件上传失败");
//        }
//        String url = generUrl(FileType.FILE, minioFileName);
//        log.info("文件文件成功，用户id:{},url:{}", userId, url);
//        return url;
//    }
//}

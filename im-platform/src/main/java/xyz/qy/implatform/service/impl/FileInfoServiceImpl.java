package xyz.qy.implatform.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import xyz.qy.implatform.entity.FileInfo;
import xyz.qy.implatform.mapper.FileInfoMapper;
import xyz.qy.implatform.service.IFileInfoService;

/**
 * 上传文件信息表 Service 实现类
 *
 * @author Polaris
 * @since 2026-04-02
 */
@Slf4j
@Service
public class FileInfoServiceImpl extends ServiceImpl<FileInfoMapper, FileInfo> implements IFileInfoService {
    
    /**
     * 保存文件信息到数据库
     *
     * @param fileName 文件名称
     * @param fileType 文件类型
     * @param fileSize 文件大小
     * @param url 文件链接
     * @param path 文件位置
     * @param storageType 文件存储类型
     * @param createBy 创建者
     */
    public void saveFileInfo(String fileName, String fileType, String extension, Long fileSize,
                                 String url, String path, String storageType, Long createBy) {
        try {
            FileInfo fileInfo = new FileInfo();
            fileInfo.setFileName(fileName);
            fileInfo.setFileType(fileType);
            fileInfo.setFileSize(fileSize / 1024.0); // 转换为 KB
            fileInfo.setExtension(extension);
            fileInfo.setUrl(url);
            fileInfo.setPath(path);
            fileInfo.setStorageType(storageType);
            fileInfo.setCreateBy(createBy);
            fileInfo.setDeleted(false);

            this.save(fileInfo);
        } catch (Exception e) {
            log.error("保存文件信息失败", e);
        }
    }
}

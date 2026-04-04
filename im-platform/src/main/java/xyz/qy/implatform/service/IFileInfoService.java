package xyz.qy.implatform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import xyz.qy.implatform.entity.FileInfo;

/**
 * 上传文件信息表 Service 接口
 *
 * @author Polaris
 * @since 2026-04-02
 */
public interface IFileInfoService extends IService<FileInfo> {
    
    /**
     * 保存文件信息到数据库
     *
     * @param fileName 文件名称
     * @param fileType 文件类型
     * @param extension 文件后缀
     * @param fileSize 文件大小
     * @param url 文件链接
     * @param path 文件位置
     * @param storageType 文件存储类型
     * @param createBy 创建者
     * @return 文件信息
     */
    void saveFileInfo(String fileName, String fileType, String extension, Long fileSize,
                          String url, String path, String storageType, Long createBy);
}

package xyz.qy.implatform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import xyz.qy.implatform.dto.FileInfoDTO;
import xyz.qy.implatform.entity.FileInfo;
import xyz.qy.implatform.entity.User;
import xyz.qy.implatform.mapper.FileInfoMapper;
import xyz.qy.implatform.mapper.UserMapper;
import xyz.qy.implatform.service.IFileInfoService;
import xyz.qy.implatform.util.BeanUtils;
import xyz.qy.implatform.util.PageUtils;
import xyz.qy.implatform.vo.FileInfoVo;
import xyz.qy.implatform.vo.PageResultVO;

import javax.annotation.Resource;
import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

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
     * 本地路径
     */
    @Value("${upload.local.path}")
    private String localPath;

    @Resource
    private UserMapper userMapper;
    
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

    @Override
    public PageResultVO pageFileInfo(FileInfoDTO fileInfoDTO) {
        LambdaQueryWrapper<FileInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StringUtils.isNotBlank(fileInfoDTO.getFileName()), FileInfo::getFileName, fileInfoDTO.getFileName());
        queryWrapper.eq(StringUtils.isNotBlank(fileInfoDTO.getFileType()), FileInfo::getFileType, fileInfoDTO.getFileType());
        queryWrapper.eq(StringUtils.isNotBlank(fileInfoDTO.getStorageType()), FileInfo::getStorageType, fileInfoDTO.getStorageType());
        queryWrapper.eq(FileInfo::getDeleted, false);
        queryWrapper.orderByDesc(FileInfo::getCreateTime);

        Page<FileInfo> page = this.page(new Page<>(PageUtils.getPageNo(), PageUtils.getPageSize()), queryWrapper);
        if (CollectionUtils.isEmpty(page.getRecords())) {
            return PageResultVO.builder().total(0L).data(Collections.emptyList()).build();
        }

        List<FileInfo> records = page.getRecords();
        List<Long> userIds = records.stream().map(FileInfo::getCreateBy).distinct().collect(Collectors.toList());
        LambdaQueryWrapper<User> userWrapper = new LambdaQueryWrapper<>();
        userWrapper.select(User::getId, User::getUserName);
        userWrapper.in(User::getId, userIds);
        List<User> users = userMapper.selectList(userWrapper);
        Map<Long, String> userMap = users.stream().collect(Collectors.toMap(User::getId, User::getUserName));

        List<FileInfoVo> fileInfoVos = BeanUtils.copyProperties(records, FileInfoVo.class);
        fileInfoVos.forEach(fileInfoVo -> fileInfoVo.setCreateByName(userMap.get(fileInfoVo.getCreateBy())));

        return PageResultVO.builder().total(page.getTotal()).data(fileInfoVos).build();
    }

    @Override
    public void deleteFileInfo(FileInfoDTO fileInfoDTO) {
        if (Objects.isNull(fileInfoDTO.getId())) {
            throw new RuntimeException("文件 ID 不能为空");
        }
        FileInfo fileInfo = this.getById(fileInfoDTO.getId());
        if (Objects.isNull(fileInfo)) {
            throw new RuntimeException("文件不存在");
        }
            
        // 判断文件存储介质是否为磁盘
        if ("disk".equals(fileInfo.getStorageType())) {
            // 从磁盘删除文件
            File file = new File(localPath + fileInfo.getPath());
            if (file.exists()) {
                if (!file.delete()) {
                    log.error("删除文件失败：{}", fileInfo.getPath());
                } else {
                    log.info("成功删除文件：{}", fileInfo.getPath());
                }
            }
        }
            
        // 修改文件 deleted 字段为 true
        fileInfo.setDeleted(true);
        this.updateById(fileInfo);
    }
}

package xyz.qy.implatform.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.util.Date;

@Data
@ApiModel("上传文件信息")
public class FileInfoVo {
    /**
     * 主键
     */
    private Long id;

    /**
     * 文件名称
     */
    private String fileName;

    /**
     * 文件后缀
     */
    private String extension;

    /**
     * 文件类型
     */
    private String fileType;

    /**
     * 文件大小
     */
    private Double fileSize;

    /**
     * 文件链接
     */
    private String url;

    /**
     * 文件位置
     */
    private String path;

    /**
     * 文件存储类型：disk-本地磁盘，minio-MinIO，qiniu-七牛云
     */
    private String storageType;

    /**
     * 上传时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    /**
     * 上传人id
     */
    private Long createBy;

    /**
     * 上传人
     */
    private String createByName;
}

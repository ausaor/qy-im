package xyz.qy.implatform.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * 上传文件信息表
 *
 * @author Polaris
 * @since 2026-04-02
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("im_file_info")
public class FileInfo extends Model<FileInfo> {
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 文件名称
     */
    @TableField("file_name")
    private String fileName;

    /**
     * 文件类型
     */
    @TableField("file_type")
    private String fileType;

    /**
     * 文件大小
     */
    @TableField("file_size")
    private Double fileSize;

    /**
     * 文件链接
     */
    @TableField("url")
    private String url;

    /**
     * 文件位置
     */
    @TableField("path")
    private String path;

    /**
     * 文件存储类型：disk-本地磁盘，minio-MinIO，qiniu-七牛云
     */
    @TableField("storage_type")
    private String storageType;

    /**
     * 创建者
     */
    @TableField("create_by")
    private Long createBy;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone="GMT+8")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private Date createTime;

    /**
     * 是否删除
     */
    @TableField("deleted")
    private Boolean deleted;

    @Override
    protected Serializable pkVal() {
        return this.id;
    }
}

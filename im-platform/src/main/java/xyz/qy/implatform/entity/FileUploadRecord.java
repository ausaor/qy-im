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
 * 文件上传记录
 *
 * @author Polaris
 * @since 2024-10-14
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("im_file_upload_record")
public class FileUploadRecord extends Model<FileUploadRecord> {
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户id
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 文件名称
     */
    @TableField("file_name")
    private String fileName;

    /**
     * 文件原名称
     */
    @TableField("original_name")
    private String originalName;

    /**
     * 存储位置（磁盘-disk，七牛云-qiniuyun）
     */
    @TableField("location")
    private String location;

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

    @Override
    protected Serializable pkVal() {
        return this.id;
    }
}

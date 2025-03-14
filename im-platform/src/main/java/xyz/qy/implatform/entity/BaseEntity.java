package xyz.qy.implatform.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 实体基类
 *
 * @author Polaris
 * @since 2023-03-12 16:25
 **/
@Data
public class BaseEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 是否删除：0-否；1-是 */
    @TableField("deleted")
    private Boolean deleted;

    /** 创建者 */
    @TableField("create_by")
    private Long createBy;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone="GMT+8")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private Date createTime;

    /** 更新者 */
    @TableField("update_by")
    private Long updateBy;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone="GMT+8")
    @TableField(value = "update_time", fill = FieldFill.UPDATE)
    private Date updateTime;
}

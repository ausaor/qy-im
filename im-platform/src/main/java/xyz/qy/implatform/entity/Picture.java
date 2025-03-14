package xyz.qy.implatform.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * @description: 图片
 * @author: Polaris
 * @create: 2023-07-08 10:18
 **/
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("im_picture")
public class Picture extends Model<Picture> {
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 图片名称
     */
    @TableField("picture_name")
    private String pictureName;

    /**
     * 图片分类
     */
    @TableField("category")
    private String category;

    /**
     * 图片链接
     */
    @TableField("url")
    private String url;

    /**
     * 图片来源
     */
    @TableField("origin")
    private String origin;

    /**
     * 图片来源id
     */
    @TableField("origin_id")
    private String originId;

    /**
     * 格式
     */
    @TableField("format")
    private String format;

    /**
     * 状态
     */
    @TableField("status")
    private Integer status;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private Date createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    @Override
    protected Serializable pkVal() {
        return this.id;
    }
}

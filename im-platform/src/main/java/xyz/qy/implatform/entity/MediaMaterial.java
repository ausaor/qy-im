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
 * @description: 媒体素材
 * @author: Polaris
 * @create: 2023-05-02 09:05
 **/
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("im_media_material")
public class MediaMaterial extends Model<MediaMaterial> {
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 素材名称
     */
    @TableField("title")
    private String title;

    /**
     * 素材简介
     */
    @TableField("description")
    private String description;

    /**
     * 素材链接
     */
    @TableField("url")
    private String url;

    /**
     * 封面图片
     */
    @TableField("cover_image")
    private String coverImage;

    /**
     * 素材类型
     */
    @TableField("type")
    private String type;

    /**
     * 格式
     */
    @TableField("format")
    private String format;


    /**
     * 素材展示时长（单位秒）
     */
    @TableField("display_duration")
    private Integer displayDuration;

    /**
     * 排序号
     */
    @TableField("sort")
    private Integer sort;

    /**
     * 状态
     */
    @TableField("status")
    private Integer status;

    /**
     * 素材展示开始时间
     */
    @TableField("start_time")
    private Date startTime;

    /**
     * 素材展示结束时间
     */
    @TableField("end_time")
    private Date endTime;

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

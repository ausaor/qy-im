package xyz.qy.implatform.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * 短视频
 *
 * @author Polaris
 * @since 2026-06-17
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("im_short_video")
public class ShortVideo extends Model<ShortVideo> {
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 作者用户id
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 对象id
     */
    @TableField("object_id")
    private Long objectId;

    /**
     * 分类
     */
    @TableField("category")
    private String category;

    /**
     * 公布范围
     */
    @TableField("scope")
    private Integer scope;

    /**
     * 标题
     */
    @TableField("title")
    private String title;

    /**
     * 描述
     */
    @TableField("description")
    private String description;

    /**
     * 视频地址
     */
    @TableField("video_url")
    private String videoUrl;

    /**
     * 封面图地址
     */
    @TableField("cover_url")
    private String coverUrl;

    /**
     * 视频时长（秒）
     */
    @TableField("duration")
    private Integer duration;

    /**
     * 视频宽度
     */
    @TableField("width")
    private Integer width;

    /**
     * 视频高度
     */
    @TableField("height")
    private Integer height;

    /**
     * 文件大小（字节）
     */
    @TableField("size")
    private Long size;

    /**
     * 播放数
     */
    @TableField("play_count")
    private Integer playCount;

    /**
     * 点赞数
     */
    @TableField("like_count")
    private Integer likeCount;

    /**
     * 收藏数
     */
    @TableField("favorite_count")
    private Integer favoriteCount;

    /**
     * 评论数
     */
    @TableField("comment_count")
    private Integer commentCount;

    /**
     * 状态
     */
    @TableField("status")
    private String status;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private Date createTime;

    /**
     * 更新时间
     */
    @TableField("update_time")
    private Date updateTime;

    /**
     * 是否已删除：0-否；1-是
     */
    @TableField("deleted")
    private Boolean deleted;

    @Override
    protected Serializable pkVal() {
        return this.id;
    }
}

package xyz.qy.implatform.entity;

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
 * 短视频评论
 *
 * @author Polaris
 * @since 2026-06-17
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("im_short_video_comment")
public class ShortVideoComment extends Model<ShortVideoComment> {
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 短视频id
     */
    @TableField("video_id")
    private Long videoId;

    /**
     * 评论用户id
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 父评论id，0表示顶级评论
     */
    @TableField("parent_id")
    private Long parentId;

    /**
     * 被回复的用户id
     */
    @TableField("reply_to_user_id")
    private Long replyToUserId;

    /**
     * 评论内容
     */
    @TableField("content")
    private String content;

    /**
     * 点赞数
     */
    @TableField("like_count")
    private Integer likeCount;

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

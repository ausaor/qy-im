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
     * 用户昵称
     */
    @TableField("user_nickname")
    private String userNickname;

    /**
     * 用户头像
     */
    @TableField("user_avatar")
    private String userAvatar;

    /**
     * 角色id
     */
    @TableField("character_id")
    private Long characterId;

    /**
     * 头像id
     */
    @TableField("avatar_id")
    private Long avatarId;

    /**
     * 父评论id
     */
    @TableField("reply_comment_id")
    private Long replyCommentId;

    /**
     * 顶级回复的评论id,0表示顶级评论
     */
    @TableField("top_reply_comment_id")
    private Long topReplyCommentId;

    /**
     * 被回复的用户id
     */
    @TableField("reply_to_user_id")
    private Long replyToUserId;

    /**
     * 被回复用户头像
     */
    @TableField("reply_to_user_avatar")
    private String replyToUserAvatar;

    /**
     * 被回复用户角色id
     */
    @TableField("reply_to_user_character_id")
    private Long replyToUserCharacterId;

    /**
     * 被回复用户昵称
     */
    @TableField("reply_to_user_nickname")
    private String replyToUserNickname;

    /**
     * 消息类型 0:文字 1:图片 5:语音台词
     */
    @TableField("type")
    private Integer type;

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
     * 数字IP地址
     */
    @TableField("ip")
    private String ip;

    /**
     * IP来源
     */
    @TableField("ip_address")
    private String ipAddress;

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

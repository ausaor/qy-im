package xyz.qy.implatform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @description: 动态评论表
 * @author: Polaris
 * @create: 2023-12-24 15:15
 **/
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("im_talk_comment")
public class TalkComment extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 动态id
     */
    @TableField(value = "talk_id")
    private Long talkId;

    /**
     * 用户id
     */
    @TableField(value = "user_id")
    private Long userId;

    /**
     * 用户昵称
     */
    @TableField(value = "user_nickname")
    private String userNickname;

    /**
     * 用户头像
     */
    @TableField(value = "user_avatar")
    private String userAvatar;

    /**
     * 评论内容
     */
    @TableField(value = "content")
    private String content;

    /**
     * 角色id
     */
    @TableField(value = "character_id")
    private Long characterId;

    /**
     * 头像id
     */
    @TableField(value = "avatar_id")
    private Long avatarId;

    /**
     * 回复的评论id
     */
    @TableField(value = "reply_comment_id")
    private Long replyCommentId;

    /**
     * 顶级回复的评论id
     */
    @TableField(value = "top_reply_comment_id")
    private Long topReplyCommentId;

    /**
     * 回复用户id
     */
    @TableField(value = "reply_user_id")
    private Long replyUserId;

    /**
     * 回复用户头像
     */
    @TableField(value = "reply_user_avatar")
    private String replyUserAvatar;

    /**
     * 回复用户角色id
     */
    @TableField(value = "reply_user_character_id")
    private Long replyUserCharacterId;

    /**
     * 回复用户昵称
     */
    @TableField(value = "reply_user_nickname")
    private String replyUserNickname;

    /**
     * 评论类型 0:文字 1:图片 5:语音台词
     */
    @TableField(value = "type")
    private Integer type;

    /**
     * IP
     */
    @TableField(value = "ip")
    private String ip;

    /**
     * IP来源
     */
    @TableField(value = "ip_address")
    private String ipAddress;
}

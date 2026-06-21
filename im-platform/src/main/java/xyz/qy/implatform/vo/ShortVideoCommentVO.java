package xyz.qy.implatform.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.util.Date;

@Data
public class ShortVideoCommentVO {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 短视频id
     */
    private Long videoId;

    /**
     * 评论用户id
     */
    private Long userId;

    /**
     * 用户昵称
     */
    private String userNickname;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 角色id
     */
    private Long characterId;

    /**
     * 头像id
     */
    private Long avatarId;

    /**
     * 父评论id，0表示顶级评论
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long replyCommentId;

    /**
     * 顶级回复的评论id
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long topReplyCommentId;

    /**
     * 被回复的用户id
     */
    private Long replyToUserId;

    /**
     * 被回复用户头像
     */
    private String replyToUserAvatar;

    /**
     * 被回复用户角色id
     */
    private Long replyToUserCharacterId;

    /**
     * 被回复用户昵称
     */
    private String replyToUserNickname;

    /**
     * 消息类型 0:文字 1:图片 5:语音台词
     */
    private Integer type;

    /**
     * 评论内容
     */
    private String content;

    /**
     * 点赞数
     */
    private Integer likeCount;

    /**
     * 数字IP地址
     */
    private String ip;

    /**
     * IP来源
     */
    private String ipAddress;

    private Boolean deleted;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;

    /**
     * 子评论数
     */
    private Long childCommentCount;

    private Boolean isOwner = Boolean.FALSE;
}

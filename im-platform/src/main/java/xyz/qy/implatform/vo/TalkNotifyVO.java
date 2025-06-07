package xyz.qy.implatform.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class TalkNotifyVO {
    private Long id;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 动态id
     */
    private Long talkId;

    /**
     * 评论id
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long commentId;

    /**
     * 点赞id
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long starId;

    /**
     * 分类（private：个人，group：群聊，region：地区）
     */
    private String category;

    /**
     * 群聊id
     */
    private Long groupId;

    /**
     * 地区编码
     */
    private String regionCode;

    /**
     * 提醒类型（1点赞 2评论 3@提及）
     */
    private Integer actionType;

    /**
     * 是否已读（0未读 1已读）
     */
    private Boolean isRead;

    /**
     * 是否删除（0未删除 1已删除）
     */
    private Boolean deleted;

    /**
     * 提醒生成的时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    /**
     * 评论用户头像
     */
    private String avatar;

    /**
     * 评论用户昵称
     */
    private String nickname;

    /**
     * 评论用户id
     */
    private Long commentUserId;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;

    private TalkVO talk;

    private TalkCommentVO talkComment;

    private List<TalkCommentVO> replyTalkComment;

    private TalkStarVO talkStar;
}

package xyz.qy.implatform.vo;

import lombok.Data;

import java.time.LocalDateTime;

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
    private Long commentId;

    /**
     * 点赞id
     */
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
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    private TalkVO talk;

    private TalkCommentVO talkComment;

    private TalkCommentVO replyTalkComment;

    private TalkStarVO talkStar;
}

package xyz.qy.implatform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("im_talk_notify")
public class TalkNotify {
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户id
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 动态id
     */
    @TableField("talk_id")
    private Long talkId;

    /**
     * 评论id
     */
    @TableField("comment_id")
    private Long commentId;

    /**
     * 点赞id
     */
    @TableField("star_id")
    private Long starId;

    /**
     * 分类（private：个人，group：群聊，region：地区）
     */
    @TableField("category")
    private String category;

    /**
     * 提醒类型（1点赞 2评论 3@提及）
     */
    @TableField("action_type")
    private Integer actionType;

    /**
     * 是否已读（0未读 1已读）
     */
    @TableField("is_read")
    private Boolean isRead;

    /**
     * 是否删除（0未删除 1已删除）
     */
    @TableField("deleted")
    private Boolean deleted;

    /**
     * 提醒生成的时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField("update_time")
    private LocalDateTime updateTime;
}

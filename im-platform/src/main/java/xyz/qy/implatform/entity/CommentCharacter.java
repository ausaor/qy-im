package xyz.qy.implatform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * @description: 评论角色关联表
 * @author: Polaris
 * @create: 2026-06-21 15:15
 **/
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("im_comment_character")
public class CommentCharacter extends Model<CommentCharacter> {
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 用户id
     */
    @TableField(value = "user_id")
    private Long userId;

    /**
     * 目标id
     */
    @TableField(value = "target_id")
    private Long targetId;

    /**
     * 目标类型：talk（动态），shortVideo（短视频）
     */
    @TableField(value = "target_type")
    private String targetType;

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
     * 创建时间
     */
    @TableField(value = "create_time")
    private LocalDateTime createTime;
}

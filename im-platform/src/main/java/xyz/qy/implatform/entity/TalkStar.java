package xyz.qy.implatform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @description: 动态赞星表
 * @author: Polaris
 * @create: 2023-12-24 15:04
 **/
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("im_talk_star")
public class TalkStar extends BaseEntity{
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
    @TableField(value = "nickname")
    private String nickname;

    /**
     * 用户头像
     */
    @TableField(value = "avatar")
    private String avatar;

    /**
     * 角色id
     */
    @TableField(value = "character_id")
    private Long characterId;
}

package xyz.qy.implatform.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
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
 * 群成员
 *
 * @author Polaris
 * @since 2022-10-31
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("im_group_member")
public class GroupMember extends Model<GroupMember> {

    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 群id
     */
    @TableField("group_id")
    private Long groupId;

    /**
     * 用户id
     */
    @TableField("user_id")
    private Long userId;

    /**
     *  群内显示名称
     */
    @TableField("alias_name")
    private String aliasName;

    /**
     * 昵称前缀
     */
    @TableField("alias_name_prefix")
    private String aliasNamePrefix;


    /**
     * 昵称后缀
     */
    @TableField("alias_name_suffix")
    private String aliasNameSuffix;

    /**
     * 用户昵称（用于非普通群聊时备注）
     */
    @TableField("nickname")
    private String nickname;

    /**
     *  头像
     */
    @TableField(value = "head_image", fill = FieldFill.UPDATE)
    private String headImage;

    /**
     *  自定义头像
     */
    @TableField("head_image_def")
    private String headImageDef;

    /**
     * 是否模板人物
     */
    @TableField("is_template")
    private Boolean isTemplate;

    /**
     * 模板人物id
     */
    @TableField("template_character_id")
    private Long templateCharacterId;

    /**
     * 模板人物头像id
     */
    @TableField(value = "character_avatar_id", fill = FieldFill.INSERT_UPDATE)
    private Long characterAvatarId;

    /**
     * 头像别名
     */
    @TableField(value = "avatar_alias", fill = FieldFill.INSERT_UPDATE)
    private String avatarAlias;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;

    /**
     * 是否已离开群聊
     */
    @TableField("quit")
    private Boolean quit;

    /**
     * 退群时间
     */
    @TableField("quit_time")
    private Date quitTime;

    /**
     * 是否展示昵称
     */
    @TableField("show_nickname")
    private Boolean showNickName;

    /**
     * 是否被禁止发言
     */
    @TableField("is_banned")
    private Boolean isBanned;

    /**
     * 被禁止发言类型（sys：系统禁止；master：群主禁止）
     */
    @TableField("ban_type")
    private String banType;

    /**
     * 禁止发言失效时间
     */
    @TableField("ban_expire_time")
    private Date banExpireTime;

    /**
     * 创建时间
     */
    @TableField("created_time")
    private Date createdTime;

    @TableField("switch_time")
    private Date switchTime;


    @Override
    protected Serializable pkVal() {
        return this.id;
    }

}

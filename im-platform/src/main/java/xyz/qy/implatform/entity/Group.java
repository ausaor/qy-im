package xyz.qy.implatform.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
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
 * 群
 *
 * @author Polaris
 * @since 2022-10-31
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("im_group")
public class Group extends Model<Group> {
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 群名字
     */
    @TableField("name")
    private String name;

    /**
     * 群主id
     */
    @TableField("owner_id")
    private Long ownerId;

    /**
     * 头像
     */
    @TableField("head_image")
    private String headImage;

    /**
     * 头像缩略图
     */
    @TableField("head_image_thumb")
    private String headImageThumb;

    /**
     * 是否模板群聊
     */
    @TableField("is_template")
    private Boolean isTemplate;

    /**
     * 群聊类型
     */
    @TableField("group_type")
    private Integer groupType;

    /**
     * 模板群聊id
     */
    @TableField(value = "template_group_id", updateStrategy = FieldStrategy.IGNORED)
    private Long templateGroupId;

    /**
     * 群公告
     */
    @TableField("notice")
    private String notice;

    /**
     * 群备注
     */
    @TableField("remark")
    private String remark;

    /**
     * 是否已删除
     */
    @TableField("deleted")
    private Boolean deleted;

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

    /**
     * 切换模板群聊时间
     */
    @TableField("switch_time")
    private Date switchTime;

    /**
     * 入群审核
     */
    @TableField("enter_review")
    private Boolean enterReview;

    /**
     * 群版本
     */
    @TableField("version")
    private Long version;


    @Override
    protected Serializable pkVal() {
        return this.id;
    }

}

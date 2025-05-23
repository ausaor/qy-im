package xyz.qy.implatform.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * 地区群聊常驻用户表
 *
 * @author Polaris
 * @since 2024-10-26
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("im_region_group_member")
public class RegionGroupMember extends Model<RegionGroupMember> {
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 地区群聊id
     */
    @TableField("region_group_id")
    private Long regionGroupId;

    /**
     * 地区编码
     */
    @TableField("code")
    private String code;

    /**
     * 用户id
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 聊天名称
     */
    @TableField("alias_name")
    private String aliasName;

    /**
     * 用户头像
     */
    @TableField("head_image")
    private String headImage;

    /**
     * 是否退出
     */
    @TableField("quit")
    private Boolean quit;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;

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

    /** 创建者 */
    @TableField("create_by")
    private Long createBy;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone="GMT+8")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private Date createTime;

    /** 更新者 */
    @TableField("update_by")
    private Long updateBy;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone="GMT+8")
    @TableField(value = "update_time", fill = FieldFill.UPDATE)
    private Date updateTime;

    @Override
    protected Serializable pkVal() {
        return this.id;
    }
}

package xyz.qy.implatform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 地区群聊表
 *
 * @author Polaris
 * @since 2024-10-26
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("im_region_group")
public class RegionGroup extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 地区编码
     */
    @TableField("code")
    private String code;

    /**
     * 地区群聊名称
     */
    @TableField("region_group_name")
    private String regionGroupName;

    /**
     * 地区群聊编号
     */
    @TableField("num")
    private String num;

    /**
     * 地区群聊群主id
     */
    @TableField("owner_id")
    private Long ownerId;

    /**
     * 群主生效时间
     */
    @TableField("effective_time")
    private Date effectiveTime;

    /**
     * 群主失效时间
     */
    @TableField("expiration_time")
    private Date expirationTime;

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
}

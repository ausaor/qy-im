package xyz.qy.implatform.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 地区表
 *
 * @author Polaris
 * @since 2024-10-26
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("im_region")
public class Region extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "code")
    private String code;

    /**
     * 上级地区编码
     */
    @TableField("parent_code")
    private String parentCode;

    /**
     * 地区级别
     */
    @TableField("level")
    private Integer level;

    /**
     * 地区名称
     */
    @TableField("name")
    private String name;

    /**
     * 地区领导称呼
     */
    @TableField("leader_call_name")
    private String leaderCallName;

    /**
     * 纬度
     */
    @TableField("latitude")
    private String latitude;

    /**
     * 经度
     */
    @TableField("longitude")
    private String longitude;

    /**
     * 是否被禁止发言
     */
    @TableField("is_banned")
    private Boolean isBanned;

    /**
     * 被禁止发言类型（sys：管理员禁止；master：群主禁止）
     */
    @TableField("ban_type")
    private String banType;

    /**
     * 禁止发言失效时间
     */
    @TableField("ban_expire_time")
    private Date banExpireTime;
}

package xyz.qy.implatform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;

import java.io.Serializable;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 群组请求表
 *
 * @author Polaris
 * @since 2025-09-11
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("im_group_request")
public class GroupRequest extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 群id
     */
    @TableField("group_id")
    private Long groupId;

    /**
     * 类型（1：用户主动申请加入；2：用户被邀请加入）
     */
    @TableField("type")
    private Integer type;

    /**
     * 用户id
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 用户昵称
     */
    @TableField("user_nickname")
    private String userNickname;

    /**
     * 用户头像
     */
    @TableField("user_head_image")
    private String userHeadImage;

    /**
     * 状态（0：撤回；1：审核中；2：同意；3：拒绝）
     */
    @TableField("status")
    private Integer status;

    /**
     * 发起人id
     */
    @TableField("launch_user_id")
    private Long launchUserId;

    /**
     * 模板角色id
     */
    @TableField("template_character_id")
    private Long templateCharacterId;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;
}

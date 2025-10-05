package xyz.qy.implatform.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * 地区群聊成员信息VO
 *
 * @author Polaris
 * @since 2024-10-26
 */
@Data
public class RegionGroupMemberVO {
    private Long id;

    /**
     * 地区群聊id
     */
    private Long regionGroupId;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 用户名称
     */
    private String userName;

    /**
     * 聊天名称
     */
    private String aliasName;

    /**
     * 用户头像
     */
    private String headImage;

    /**
     * 是否退出
     */
    private Boolean quit;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 备注
     */
    private String remark;

    @ApiModelProperty(value = "是否在线")
    private Boolean online;

    private Integer joinType;

    private Boolean isBanned = false;

    /**
     * 是否通知群成员
     */
    private Boolean announce = false;

    /**
     * 是否群主
     */
    private Boolean isLeader = false;
}

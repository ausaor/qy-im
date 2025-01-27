package xyz.qy.implatform.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

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
     * 备注
     */
    private String remark;

    @ApiModelProperty(value = "是否在线")
    private Boolean online;

    private Integer joinType;

    /**
     * 是否通知群成员
     */
    private Boolean announce = false;

    /**
     * 是否群主
     */
    private Boolean isLeader = false;
}

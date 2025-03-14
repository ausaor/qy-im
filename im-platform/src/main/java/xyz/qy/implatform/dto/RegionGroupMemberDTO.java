package xyz.qy.implatform.dto;

import io.swagger.annotations.ApiModel;
import lombok.Data;

/**
 * 地区群聊成员
 *
 * @author Polaris
 * @since 2024-11-30
 */
@Data
@ApiModel("地区群聊成员DTO")
public class RegionGroupMemberDTO {
    /**
     * 地区群聊id
     */
    private Long regionGroupId;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 加入方式
     */
    private Integer joinType;

    /**
     * 是否通知群成员
     */
    private Boolean announce = false;

    /**
     * 群成员昵称
     */
    private String aliasName;

    /**
     * 群成员头像
     */
    private String headImage;
}

package xyz.qy.implatform.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @description: 动态查询 DTO
 * @author: Polaris
 * @create: 2023-11-20 20:09
 **/
@Data
public class TalkQueryDTO {
    @ApiModelProperty(value = "可见范围")
    private Integer scope;

    @ApiModelProperty(value = "当前用户id")
    private Long ownerId;

    @ApiModelProperty(value = "好友id")
    private List<Long> friendIds;

    @ApiModelProperty(value = "群友id")
    private List<Long> groupMemberUserIds;

    @ApiModelProperty(value = "分类")
    private String category;

    @ApiModelProperty("查询范围：my-自己的，friends-朋友的，group-群聊的，region-地区的；")
    private String section;

    @ApiModelProperty("群id")
    private Long groupId;

    @ApiModelProperty("地区群聊id")
    private Long regionGroupId;

    @ApiModelProperty("地区编码")
    private String regionCode;
}

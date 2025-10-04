package xyz.qy.implatform.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@ApiModel("群聊查询参数")
@Data
public class GroupQueryDTO {
    @ApiModelProperty(value = "群聊名称")
    private String name;

    @ApiModelProperty(value = "群主id")
    private Long ownerId;

    @ApiModelProperty(value = "群聊类型")
    private Integer groupType;
}

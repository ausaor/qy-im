package xyz.qy.implatform.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@ApiModel("好友添加")
public class FriendDTO {
    @ApiModelProperty("好友id")
    @NotNull(message = "好友id不能为空")
    private Long friendId;

    @ApiModelProperty("备注")
    private String remark;

    @ApiModelProperty("是否免打扰")
    private Boolean isDnd = false;
}

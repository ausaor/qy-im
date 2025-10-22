package xyz.qy.implatform.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@ApiModel("群成员DTO")
public class GroupMemberDTO {
    @ApiModelProperty("群聊id")
    @NotNull(message = "群聊id不能为空")
    private Long groupId;

    @ApiModelProperty("用户id")
    @NotNull(message = "用户id不能为空")
    private Long userId;
}

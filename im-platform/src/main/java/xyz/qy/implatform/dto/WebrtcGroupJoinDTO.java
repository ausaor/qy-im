package xyz.qy.implatform.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@ApiModel("进入群视频通话DTO")
public class WebrtcGroupJoinDTO {

    @NotNull(message = "群聊id不可为空")
    @ApiModelProperty(value = "群聊id")
    private Long groupId;
}

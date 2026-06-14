package xyz.qy.implatform.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@ApiModel("关注操作")
public class FollowDTO {

    @ApiModelProperty("目标id")
    @NotNull(message = "目标id不能为空")
    private Long targetId;

    @ApiModelProperty("类别")
    @NotBlank(message = "类别不能为空")
    private String type;
}

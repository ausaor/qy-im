package xyz.qy.implatform.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import xyz.qy.implatform.enums.FollowEnum;
import xyz.qy.implatform.enums.ValidEnum;

import javax.validation.constraints.NotBlank;

@Data
@ApiModel(description = "目标查询DTO")
public class TargetQueryDTO {

    @NotBlank
    @ApiModelProperty(value = "目标id")
    private Long targetId;

    @NotBlank
    @ValidEnum(enumClass = FollowEnum.class, property = "code", message = "无效的类别")
    @ApiModelProperty(value = "目标类型")
    private String type;
}

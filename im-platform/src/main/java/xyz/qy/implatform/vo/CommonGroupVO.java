package xyz.qy.implatform.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class CommonGroupVO {
    @NotNull(message = "群id不可为空")
    @ApiModelProperty(value = "群id")
    private Long groupId;

    @NotNull(message = "群名称不可为空")
    @ApiModelProperty(value = "群名称")
    private String name;

    @ApiModelProperty(value = "群头像")
    private String avatar;
}

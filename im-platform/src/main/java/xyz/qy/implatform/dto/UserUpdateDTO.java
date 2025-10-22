package xyz.qy.implatform.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@ApiModel("用户更新DTO")
public class UserUpdateDTO {
    @ApiModelProperty("用户id")
    @NotNull
    private Long userId;

    @ApiModelProperty("用户角色")
    private String role;
}

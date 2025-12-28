package xyz.qy.implatform.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@ApiModel(value = "CharacterUserBindDTO", description = "角色用户绑定DTO")
public class CharacterUserBindDTO {
    @NotNull(message = "角色id不能为空")
    @ApiModelProperty("角色id")
    private Long characterId;

    @NotEmpty(message = "用户id不能为空")
    @ApiModelProperty("用户id")
    private List<Long> userId;
}

package xyz.qy.implatform.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@ApiModel("角色表情DTO")
public class CharacterEmoDTO {
    @NotNull(message="群聊模板id不可为空")
    @ApiModelProperty(value = "群聊模板id")
    private Long templateGroupId;

    @ApiModelProperty(value = "角色id")
    private Long characterId;

    @ApiModelProperty(value = "主键")
    private Long id;

    @ApiModelProperty(value = "角色名称")
    private String characterName;

    @ApiModelProperty(value = "表情名称")
    private String name;

    @ApiModelProperty(value = "状态")
    private String status;

    @ApiModelProperty(value = "链接")
    private String url;
}

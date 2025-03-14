package xyz.qy.implatform.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@ApiModel("角色语音台词DTO")
public class CharacterWordDTO {
    @NotNull(message="群聊模板id不可为空")
    @ApiModelProperty(value = "群聊模板id")
    private Long templateGroupId;

    @ApiModelProperty(value = "角色id")
    private Long characterId;

    @ApiModelProperty(value = "台词")
    private String word;

    @ApiModelProperty(value = "语音")
    private String voice;

    @ApiModelProperty(value = "主键")
    private Long id;

    @ApiModelProperty(value = "角色名称")
    private String characterName;

    @ApiModelProperty(value = "状态")
    private String status;
}

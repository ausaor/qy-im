package xyz.qy.implatform.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class SwitchCharacterAvatarVO {
    @NotNull(message="群聊id不可为空")
    @ApiModelProperty(value = "群id")
    private Long groupId;

    @ApiModelProperty("群聊类型")
    private Integer groupType;

    @ApiModelProperty(value = "模板群聊id")
    private Long templateGroupId;

    @NotNull(message = "模板人物id不可为空")
    @ApiModelProperty(value = "模板人物id")
    private Long templateCharacterId;

    @NotNull(message = "模板人物头像id不能为空")
    @ApiModelProperty(value = "模板人物头像id")
    private Long characterAvatarId;
}

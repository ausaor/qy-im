package xyz.qy.implatform.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @description: 主动请求加入群聊VO
 * @author: Polaris
 * @create: 2023-11-12 09:14
 **/
@Data
@ApiModel("主动请求加入群聊VO")
public class GroupJoinVO {
    @NotNull(message = "群id不可为空")
    @ApiModelProperty(value = "群id")
    private Long groupId;

    @ApiModelProperty(value = "用户id")
    private Long userId;

    @ApiModelProperty(value = "是否模板群聊")
    private Boolean isTemplate;

    @ApiModelProperty(value = "群聊类型")
    private Integer groupType;

    @ApiModelProperty(value = "模板群聊id")
    private Long templateGroupId;

    @ApiModelProperty(value = "模板人物id")
    public Long templateCharacterId;

    @ApiModelProperty(value = "模板人物头像")
    public String templateCharacterAvatar;

    @ApiModelProperty(value = "模板人物名称")
    public String templateCharacterName;

    @ApiModelProperty(value = "备注")
    private String remark;
}

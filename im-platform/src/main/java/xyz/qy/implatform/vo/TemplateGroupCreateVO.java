package xyz.qy.implatform.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @description:
 * @author: Polaris
 * @create: 2023-03-18 17:19
 **/
@Data
public class TemplateGroupCreateVO {
    /**
     * 群聊类型
     */
    @NotNull(message = "群聊类型不能为空")
    private Integer groupType;

    /**
     * 模板群聊id
     */
    private Long templateGroupId;

    /**
     * 模板群聊头像
     */
    private String templateGroupAvatar;

    /**
     * 模板群聊名称
     */
    private String templateGroupName;

    /**
     * 模板人物id
     */
    @NotNull(message = "模板角色为空")
    private Long templateCharacterId;

    /**
     * 模板人物头像
     */
    private String templateCharacterAvatar;

    /**
     * 模板人物名称
     */
    private String templateCharacterName;

    @ApiModelProperty(value = "昵称前缀")
    private String aliasNamePrefix;

    @ApiModelProperty(value = "昵称后缀")
    private String aliasNameSuffix;

    /**
     * 群聊名称
     */
    @NotBlank(message = "群聊名称不能为空")
    private String name;

    /**
     * 群聊头像
     */
    private String headImage;
}

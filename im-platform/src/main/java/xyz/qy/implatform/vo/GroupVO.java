package xyz.qy.implatform.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;

@Data
@ApiModel("群信息VO")
public class GroupVO {
    @ApiModelProperty(value = "群id")
    private Long id;

    @Length(max = 20, message = "群名称长度不能大于20")
    @NotEmpty(message = "群名称不可为空")
    @ApiModelProperty(value = "群名称")
    private String name;

    @ApiModelProperty(value = "群主id")
    private Long ownerId;

    @ApiModelProperty(value = "头像")
    private String headImage;

    @ApiModelProperty(value = "头像缩略图")
    private String headImageThumb;

    @Length(max = 1024, message = "群聊显示长度不能大于1024")
    @ApiModelProperty(value = "群公告")
    private String notice;

    @Length(max = 20, message = "群聊显示长度不能大于20")
    @ApiModelProperty(value = "用户在群显示昵称")
    private String aliasName;

    @Length(max = 20, message = "群聊显示长度不能大于20")
    @ApiModelProperty(value = "群聊显示备注")
    private String remark;

    @ApiModelProperty(value = "是否模板群聊：0 否；1 是")
    private Boolean isTemplate;

    @ApiModelProperty(value = "群聊类型")
    private Integer groupType;

    @ApiModelProperty(value = "模板群聊id")
    private Long templateGroupId;

    @ApiModelProperty(value = "是否模板人物：0 否；1 是")
    private Boolean isTemplateCharacter;

    @ApiModelProperty(value = "模板人物Id")
    private Long templateCharacterId;

    @ApiModelProperty(value = "昵称前缀")
    private String aliasNamePrefix;

    @ApiModelProperty(value = "昵称后缀")
    private String aliasNameSuffix;

    @ApiModelProperty(value = "用户昵称，用于非普通群聊时展示")
    private String nickName;

    @ApiModelProperty(value = "是否展示昵称")
    private Boolean showNickName;

    @ApiModelProperty(value = "是否已删除")
    private Boolean deleted;

    @ApiModelProperty(value = "是否已退出")
    private Boolean quit;

    @ApiModelProperty(value = "群成员头像")
    private String memberHeadImage;
}

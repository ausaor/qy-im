package xyz.qy.implatform.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
@ApiModel("群成员信息VO")
public class GroupMemberVO {
    @ApiModelProperty("用户id")
    private Long userId;

    @ApiModelProperty("用户名称")
    private String userName;

    @ApiModelProperty("角色")
    private String role;

    @ApiModelProperty("群内显示名称")
    private String aliasName;

    @ApiModelProperty(value = "昵称前缀")
    private String aliasNamePrefix;

    @ApiModelProperty(value = "昵称后缀")
    private String aliasNameSuffix;

    @ApiModelProperty("群角色")
    private Integer groupRole;

    @ApiModelProperty("头像")
    private String headImage;

    @ApiModelProperty("是否已退出")
    private Boolean quit;

    @ApiModelProperty("备注")
    private String remark;

    @ApiModelProperty("群聊id")
    private String groupId;

    @ApiModelProperty("模板群聊id")
    private Long templateGroupId;

    @ApiModelProperty("是否模板人物")
    private Boolean isTemplate;

    @ApiModelProperty("群聊类型")
    private Integer groupType;

    @ApiModelProperty("模板人物id")
    private Long templateCharacterId;

    @ApiModelProperty("模板人物名称")
    private String templateCharacterName;

    @ApiModelProperty("模板人物头像")
    private String templateCharacterAvatar;

    @ApiModelProperty("用户昵称")
    private String nickName;

    @ApiModelProperty("模板人物头像id")
    private Long characterAvatarId;

    @ApiModelProperty("头像别名")
    private String avatarAlias;

    @ApiModelProperty("用户头像")
    private String userAvatar;

    @ApiModelProperty("是否展示群成员昵称")
    private Boolean showNickName = false;

    @ApiModelProperty("是否群管理员")
    private Boolean isAdmin = false;

    @ApiModelProperty(value = "是否在线")
    private Boolean online;

    @ApiModelProperty(value = "角色序号")
    private Integer characterNum;

    @ApiModelProperty(value = "是否禁言")
    private Boolean isBanned;

    @ApiModelProperty(value = "创建时间")
    private Date createdTime;
}

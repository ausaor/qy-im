package xyz.qy.implatform.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
@ApiModel("关注信息VO")
public class FollowVO {

    @ApiModelProperty(value = "主键id")
    private Long id;

    @ApiModelProperty(value = "目标id")
    private Long targetId;

    @ApiModelProperty(value = "目标名称")
    private String targetName;

    @ApiModelProperty(value = "目标头像")
    private String targetAvatar;

    @ApiModelProperty(value = "类别")
    private String type;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty("群聊模板信息")
    private TemplateGroupVO templateGroup;

    @ApiModelProperty("角色信息")
    private TemplateCharacterVO character;

    @ApiModelProperty("角色头像信息")
    private List<CharacterAvatarVO> characterAvatars;

    private Boolean followed = true;
}

package xyz.qy.implatform.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel("角色用户关系")
public class CharacterUserVO {
    @ApiModelProperty("角色用户关系id")
    private Long id;

    @ApiModelProperty("角色id")
    private Long characterId;

    @ApiModelProperty("角色名称")
    private String characterName;

    @ApiModelProperty("角色头像")
    private String characterAvatar;

    @ApiModelProperty("用户id")
    private Long userId;

    @ApiModelProperty("用户昵称")
    private String nickName;

    @ApiModelProperty("用户头像")
    private String headImage;

    @ApiModelProperty("用户名称")
    private String userName;

    @ApiModelProperty("角色信息")
    private TemplateCharacterVO character;

    @ApiModelProperty("角色头像信息")
    private List<CharacterAvatarVO> characterAvatars;
}

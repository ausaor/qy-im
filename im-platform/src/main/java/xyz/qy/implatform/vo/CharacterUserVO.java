package xyz.qy.implatform.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("角色用户关系")
public class CharacterUserVO {
    @ApiModelProperty("角色用户关系id")
    private Long id;

    @ApiModelProperty("角色id")
    private Long characterId;

    @ApiModelProperty("用户id")
    private Long userId;

    @ApiModelProperty("用户昵称")
    private String nickName;

    @ApiModelProperty("用户头像")
    private String headImage;

    @ApiModelProperty("用户名称")
    private String userName;
}

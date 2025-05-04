package xyz.qy.implatform.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @description: 动态赞星
 * @author: Polaris
 * @create: 2023-12-30 14:22
 **/
@Data
public class TalkStarDTO {
    @NotNull(message = "参数异常")
    @ApiModelProperty(value = "动态id")
    private Long talkId;

    @ApiModelProperty(value = "用户昵称")
    private String nickname;

    @ApiModelProperty(value = "角色id")
    private Long characterId;

    @ApiModelProperty(value = "角色头像id")
    private Long avatarId;

    @ApiModelProperty(value = "头像")
    private String avatar;
}

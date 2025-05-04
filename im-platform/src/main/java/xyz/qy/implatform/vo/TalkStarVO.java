package xyz.qy.implatform.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @description: 动态赞星
 * @author: Polaris
 * @create: 2023-12-25 20:19
 **/
@Data
public class TalkStarVO {
    @ApiModelProperty(value = "主键")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @ApiModelProperty(value = "动态id")
    private Long talkId;

    @ApiModelProperty(value = "用户id")
    private Long userId;

    @ApiModelProperty(value = "用户昵称")
    private String nickname;

    @ApiModelProperty(value = "用户头像")
    private String avatar;

    @ApiModelProperty(value = "角色id")
    private Long characterId;

    @ApiModelProperty(value = "角色头像id")
    private Long avatarId;

    @ApiModelProperty(value = "是否自己的")
    private Boolean isOwner = Boolean.FALSE;
}

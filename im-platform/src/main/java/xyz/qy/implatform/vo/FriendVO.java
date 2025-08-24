package xyz.qy.implatform.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@ApiModel("好友信息VO")
public class FriendVO {
    @NotNull(message = "好友id不可为空")
    @ApiModelProperty(value = "好友id")
    private Long id;

    @NotNull(message = "好友昵称不可为空")
    @ApiModelProperty(value = "好友昵称")
    private String nickName;

    @ApiModelProperty(value = "好友头像")
    private String headImage;

    @ApiModelProperty(value = "好友备注")
    private String friendRemark;

    @ApiModelProperty(value = "与好友聊天头像")
    private String myHeadImageToFriend;

    @ApiModelProperty(value = "是否删除")
    private Boolean deleted;
}

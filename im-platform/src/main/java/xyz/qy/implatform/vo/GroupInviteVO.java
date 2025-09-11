package xyz.qy.implatform.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@ApiModel("邀请好友进群请求VO")
public class GroupInviteVO {
    @NotNull(message = "群id不可为空")
    @ApiModelProperty(value = "群id")
    private Long groupId;

    @ApiModelProperty(value = "是否模板群聊")
    private Boolean isTemplate;

    @ApiModelProperty(value = "群聊类型")
    private Integer groupType;

    @ApiModelProperty(value = "好友模板人物信息")
    private List<InviteFriendVO> inviteFriends;
}

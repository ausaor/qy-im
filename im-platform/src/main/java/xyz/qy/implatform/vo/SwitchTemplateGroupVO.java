package xyz.qy.implatform.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class SwitchTemplateGroupVO {
    @NotNull(message="群聊id不可为空")
    @ApiModelProperty(value = "群id")
    private Long groupId;

    @NotNull(message="模板群聊id不可为空")
    @ApiModelProperty(value = "模板群聊id")
    private Long templateGroupId;

    @NotEmpty(message="群成员不可为空")
    @ApiModelProperty(value = "群成员")
    private List<GroupMemberVO> groupMembers;
}

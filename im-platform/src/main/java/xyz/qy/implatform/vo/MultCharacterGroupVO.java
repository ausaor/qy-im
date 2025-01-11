package xyz.qy.implatform.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class MultCharacterGroupVO {
    @NotNull(message="群聊id不可为空")
    @ApiModelProperty(value = "群id")
    private Long groupId;

    @ApiModelProperty(value = "群聊名称")
    @NotBlank(message = "群聊名称为空")
    private String name;

    @ApiModelProperty(value = "群头像")
    private String avatar;

    @NotEmpty(message="群成员不可为空")
    @ApiModelProperty(value = "群成员")
    private List<GroupMemberVO> groupMembers;
}

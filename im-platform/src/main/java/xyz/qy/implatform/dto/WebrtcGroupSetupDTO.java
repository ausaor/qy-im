package xyz.qy.implatform.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import xyz.qy.implatform.session.WebrtcUserInfo;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@ApiModel("发起群视频通话DTO")
public class WebrtcGroupSetupDTO {

    @NotNull(message = "群聊id不可为空")
    @ApiModelProperty(value = "群聊id")
    private Long groupId;

    @NotEmpty(message = "参与用户信息不可为空")
    @ApiModelProperty(value = "参与用户信息")
    private List<WebrtcUserInfo> userInfos;

}


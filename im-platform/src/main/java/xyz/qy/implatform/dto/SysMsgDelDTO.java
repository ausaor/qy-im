package xyz.qy.implatform.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.List;

@Data
@ApiModel("系统消息删除DTO")
public class SysMsgDelDTO {
    @ApiModelProperty(value = "系统消息id")
    @NotEmpty(message = "系统消息id不可为空")
    private List<Long> ids;
}

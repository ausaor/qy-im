package xyz.qy.implatform.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("系统消息查询DTO")
public class SysMsgQueryDTO {
    @ApiModelProperty(value = "标题")
    private String title;

    @ApiModelProperty(value = "消息类型")
    private Integer type;
}

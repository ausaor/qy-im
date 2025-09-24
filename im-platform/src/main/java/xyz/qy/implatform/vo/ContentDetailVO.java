package xyz.qy.implatform.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@ApiModel("内容详情VO")
@Data
public class ContentDetailVO {
    @ApiModelProperty(value = "内容id")
    private Long id;

    @ApiModelProperty(value = "内容")
    private String richText;
}

package xyz.qy.implatform.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import xyz.qy.implatform.enums.ReviewEnum;
import xyz.qy.implatform.enums.ValidEnum;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@ApiModel(description = "动态审核DTO")
public class TalkReviewDTO {
    @NotNull(message = "动态id不能为空")
    @ApiModelProperty(value = "动态id")
    private Long id;

    @ApiModelProperty(value = "审核不通过原因")
    private String reason;

    @NotBlank(message = "审核状态不能为空")
    @ValidEnum(enumClass = ReviewEnum.class, property = "code", message = "审核状态参数错误")
    @ApiModelProperty(value = "审核状态")
    private String status;
}

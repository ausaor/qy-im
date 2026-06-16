package xyz.qy.implatform.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@ApiModel(description = "功能控制DTO")
public class FeatureControlDTO {

    @NotBlank
    @ApiModelProperty(value = "功能码", required = true)
    private String featureCode;

    @ApiModelProperty(value = "时长")
    private Integer duration;

    @NotNull
    @ApiModelProperty(value = "是否开启")
    private Boolean isOpen;
}

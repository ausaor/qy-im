package xyz.qy.implatform.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@ApiModel("地区查询参数")
public class RegionQueryDTO {
    @ApiModelProperty("地区编码")
    private String code;

    @ApiModelProperty("地区名称")
    private String name;

    @ApiModelProperty("上级地区编码")
    private String parentCode;

    @ApiModelProperty("地区层级")
    @NotNull(message = "地区层级不能为空")
    private Integer level;
}

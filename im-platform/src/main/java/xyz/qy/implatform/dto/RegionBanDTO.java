package xyz.qy.implatform.dto;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@ApiModel("地区群禁言入参")
@Data
public class RegionBanDTO {
    @NotEmpty(message = "地区编码不能为空")
    private List<String> codes;

    /**
     * 禁言类型
     */
    @NotBlank(message = "禁言类型为空")
    private String banType;

    /**
     * 禁言时长
     */
    @Min(value = 1, message = "禁言时长最短1分钟")
    @NotNull(message = "禁言时长不能为空")
    private Integer banDuration;

    /**
     * 是否全局禁言
     */
    private Boolean allBanned = false;
}

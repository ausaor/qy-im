package xyz.qy.implatform.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 功能开关状态VO
 *
 * @author QY
 */
@Data
@ApiModel("功能开关状态VO")
public class FeatureStateVO {

    @ApiModelProperty(value = "功能代码")
    private String featureCode;

    @ApiModelProperty(value = "功能名称")
    private String featureName;

    @ApiModelProperty(value = "是否开启：true=开启，false=关闭")
    private Boolean isOpen;

    @ApiModelProperty(value = "剩余自动恢复秒数（仅关闭且设置时长时有值）")
    private Long remainingSeconds;
}

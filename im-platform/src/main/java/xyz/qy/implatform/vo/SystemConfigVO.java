package xyz.qy.implatform.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import xyz.qy.implatform.config.WebrtcConfig;

@Data
@ApiModel("系统配置VO")
@AllArgsConstructor
public class SystemConfigVO {

    @ApiModelProperty(value = "webrtc配置")
    private WebrtcConfig webrtc;
}

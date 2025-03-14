package xyz.qy.implatform.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import xyz.qy.implatform.config.WebrtcConfig;

@Data
@ApiModel("系统配置VO")
public class SystemConfigVO {

    @ApiModelProperty(value = "webrtc配置")
    private WebrtcConfig webrtc;

    @ApiModelProperty(value = "高德地图key")
    private String gaoDeMapKey;
}


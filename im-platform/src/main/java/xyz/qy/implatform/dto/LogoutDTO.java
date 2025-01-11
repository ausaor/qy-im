package xyz.qy.implatform.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("用户登录DTO")
public class LogoutDTO {

    @ApiModelProperty(value = "用户客户端id")
    private String cid;

}

package xyz.qy.implatform.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import xyz.qy.implatform.enums.LoginTypeEnum;
import xyz.qy.implatform.enums.ValidEnum;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Data
@ApiModel("用户登录VO")
public class LoginDTO {
    @Max(value = 2, message = "登录终端类型取值范围:0,2")
    @Min(value = 0, message = "登录终端类型取值范围:0,2")
    @NotNull(message = "登录终端类型不可为空")
    @ApiModelProperty(value = "登录终端 0:web 1:app 2:pc")
    private Integer terminal;

    @ValidEnum(enumClass = LoginTypeEnum.class, property = "type", message = "无效的登录方式")
    @NotNull(message = "登录方式不可为空")
    private Integer loginType;

    @ApiModelProperty(value = "用户名")
    private String userName;

    @ApiModelProperty(value = "用户密码")
    private String password;

    @ApiModelProperty(value = "验证码")
    private String code;

    @ApiModelProperty(value = "验证码缓存记录字符串")
    private String uuid;

    @ApiModelProperty(value = "用户客户端id")
    private String cid;

    @ApiModelProperty(value = "用户邮箱")
    private String email;

    @ApiModelProperty(value = "邮箱验证码")
    private String emailCode;
}

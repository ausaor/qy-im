package xyz.qy.implatform.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;

@Data
@ApiModel("用户注册VO")
public class RegisterDTO {
    @Length(max = 64,message = "用户名不能大于64字符")
    @NotEmpty(message="用户名不可为空")
    @ApiModelProperty(value = "用户名")
    private String userName;

    @Length(min=5,max = 20,message = "密码长度必须在5-20个字符之间")
    @NotEmpty(message="用户密码不可为空")
    @ApiModelProperty(value = "用户密码")
    private String password;

    @Length(max = 64,message = "昵称不能大于64字符")
    @NotEmpty(message="用户昵称不可为空")
    @ApiModelProperty(value = "用户昵称")
    private String nickName;

    @Length(max = 4,message = "验证码大于4字符")
    @NotEmpty(message="验证码不可为空")
    @ApiModelProperty(value = "验证码")
    private String code;

    @NotEmpty(message="参数异常")
    @ApiModelProperty(value = "验证码缓存记录字符串")
    private String uuid;
}

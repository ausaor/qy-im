package xyz.qy.implatform.dto;

import lombok.Data;
import org.hibernate.validator.constraints.Length;
import xyz.qy.implatform.annotation.ValidEmailSuffix;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Data
public class ResetPwdDTO {
    @NotBlank(message = "邮箱验证码不可为空")
    private String emailCode;

    @Length(min=5,max = 20,message = "密码长度必须在5-20个字符之间")
    @NotBlank(message = "新密码不可为空")
    private String newPassword;

    @Email
    @NotBlank(message = "邮箱不可为空")
    @ValidEmailSuffix
    private String email;
}

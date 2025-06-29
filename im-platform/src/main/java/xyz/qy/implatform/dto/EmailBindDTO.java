package xyz.qy.implatform.dto;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Data
public class EmailBindDTO {
    @Email
    @NotBlank(message = "邮箱不可为空")
    private String email;

    @NotBlank(message = "邮箱验证码不可为空")
    private String emailCode;
}

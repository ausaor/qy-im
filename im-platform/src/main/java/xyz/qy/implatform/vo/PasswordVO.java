package xyz.qy.implatform.vo;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @description: 重置密码
 * @author: Polaris
 * @create: 2023-08-20 10:27
 **/
@Data
public class PasswordVO {
    /**
     * 旧密码
     */
    @NotBlank(message = "旧密码不能为空")
    private String oldPassword;

    /**
     * 新密码
     */
    @NotBlank(message = "新密码不能为空")
    private String newPassWord;
}

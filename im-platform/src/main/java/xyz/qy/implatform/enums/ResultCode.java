package xyz.qy.implatform.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 响应码枚举
 *
 * @author Polaris
 * @since 2020/10/19
 */
@Getter
@AllArgsConstructor
public enum ResultCode {
    SUCCESS(200,"成功"),
    LOGIN_ERROR(400,"登录异常"),
    NO_LOGIN(400,"未登录"),
    INVALID_TOKEN(401,"token无效或已过期"),
    FORBIDDEN(403,"禁止访问"),
    NOT_FIND(404,"无法找到文件"),
    PROGRAM_ERROR(500,"系统繁忙，请稍后再试"),
    PASSWORD_ERROR(10001,"密码不正确"),
    VERITY_CODE_NOT_EXIST(10002,"验证码不存在"),
    USERNAME_ALREADY_REGISTER(10003,"该用户名已注册"),
    VERITY_CODE_ERROR(10004,"验证码不正确"),
    MOBILE_ALREADY_REGISTER(10005,"该手机号码已注册"),
    XSS_PARAM_ERROR(10006,"请不要输入非法内容"),
    EMAIL_ALREADY_REGISTER(10007,"邮箱已被注册"),
    ;

    private final int code;
    private final String msg;
}


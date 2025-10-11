package xyz.qy.implatform.annotation;

import xyz.qy.implatform.validator.EmailSuffixValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

/**
 * 邮箱后缀校验注解
 *
 * @author Polaris
 */
@Documented
@Constraint(validatedBy = {EmailSuffixValidator.class})
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidEmailSuffix {
    // 错误提示信息
    String message() default "不支持的邮箱后缀";

    // 允许的邮箱后缀
    String[] allowedSuffixes() default {"@126.com", "@163.com", "@qq.com", "@yeah.net", "@foxmail.com", "@sina.com", "@aliyun.com"};

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}

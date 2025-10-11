package xyz.qy.implatform.validator;

import xyz.qy.implatform.annotation.ValidEmailSuffix;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * 邮箱后缀校验器
 *
 * @author Polaris
 */
public class EmailSuffixValidator implements ConstraintValidator<ValidEmailSuffix, String> {
    private String[] allowedSuffixes;

    @Override
    public void initialize(ValidEmailSuffix constraintAnnotation) {
        this.allowedSuffixes = constraintAnnotation.allowedSuffixes();
    }

    @Override
    public boolean isValid(String email, ConstraintValidatorContext context) {
        if (email == null || email.isEmpty()) {
            // 为空的情况可以由@NotBlank等注解处理
            return true;
        }

        // 先进行基本的邮箱格式校验
        String regex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        if (!email.matches(regex)) {
            return false;
        }

        // 检查是否包含允许的后缀
        for (String suffix : allowedSuffixes) {
            if (email.endsWith(suffix)) {
                return true;
            }
        }
        return false;
    }
}

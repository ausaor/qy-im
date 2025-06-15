package xyz.qy.implatform.enums;

import xyz.qy.implatform.validator.EnumValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

/**
 * @author aurora
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Constraint(validatedBy = EnumValidator.class)
public @interface ValidEnum {
    String message() default "无效的枚举值";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    // 枚举类
    Class<? extends Enum<?>> enumClass();

    // 要匹配的枚举属性名（默认为 "name"）
    String property() default "name";

    // 是否忽略大小写（默认为 false）
    boolean ignoreCase() default false;
}

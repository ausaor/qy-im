package xyz.qy.imclient.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 调用次数限制注解
 *
 * @author Polaris
 * @since 2024-10-13
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CountLimit {
    String limitType() default "";
    int count() default 150;
    int time() default 24;
    String description() default "";
}

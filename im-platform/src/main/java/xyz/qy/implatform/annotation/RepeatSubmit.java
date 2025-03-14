package xyz.qy.implatform.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 *  防止表单重复提交注解
 *
 * @author Polaris
 * @since 2025-02-16
 */
@Inherited
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RepeatSubmit {


    /**
     * 间隔时间，小于此时间视为重复提交
     */
    int interval() default 5000;

    /**
     * 间隔时间单位
     */
    TimeUnit timeUnit() default TimeUnit.MILLISECONDS;

    /**
     * 提示消息
     */
    String message() default "请勿提交重复请求";

}

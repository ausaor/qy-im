package xyz.qy.implatform.annotation;

import xyz.qy.implatform.contant.Constant;
import xyz.qy.implatform.enums.LimitType;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 限流注解
 *
 * @author ruoyi
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimiter {
    /**
     * 限流key
     */
    public String key() default Constant.RATE_LIMIT_KEY;

    /**
     * 限流时间,单位秒
     */
    public int time() default 10;

    /**
     * 限流次数
     */
    public int count() default 60;

    /**
     * 限流类型
     */
    public LimitType limitType() default LimitType.DEFAULT;
}

package xyz.qy.implatform.annotation;


import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 功能控制注解，用于标记接口对应的功能项
 * @author Polaris
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface FeatureControl {
    /**
     * 功能项编码，对应FeatureCodeConstants中的常量
     */
    String value();
}


package xyz.qy.implatform.annotation;


import xyz.qy.implatform.enums.RoleEnum;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自定义权限注解，用于标记接口所需的访问角色
 *
 * @author Polaris
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequireRoles {
    /**
     * 允许访问的角色列表
     */
    RoleEnum[] value();
}


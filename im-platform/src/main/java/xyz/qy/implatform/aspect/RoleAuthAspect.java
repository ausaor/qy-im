package xyz.qy.implatform.aspect;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import xyz.qy.implatform.annotation.RequireRoles;
import xyz.qy.implatform.enums.ResultCode;
import xyz.qy.implatform.enums.RoleEnum;
import xyz.qy.implatform.exception.GlobalException;
import xyz.qy.implatform.session.SessionContext;
import xyz.qy.implatform.session.UserSession;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 角色权限验证切面
 *
 * @author Polaris
 */
@Aspect
@Component
public class RoleAuthAspect {

    /**
     * 定义切点，匹配所有标注了RequireRoles注解的方法
     */
    @Pointcut("@annotation(requireRoles)")
    public void rolePointcut(RequireRoles requireRoles) {}

    /**
     * 前置通知，在方法执行前进行权限验证
     */
    @Before("rolePointcut(requireRoles)")
    public void checkRole(RequireRoles requireRoles) {
        // 获取当前登录用户的认证信息
        UserSession session = SessionContext.getSession();
        if (session == null) {
            throw new GlobalException(ResultCode.NO_LOGIN, "用户未登录");
        }

        // 获取用户角色
        String userRole = getUserRole(session);

        if (userRole == null) {
            throw new GlobalException(ResultCode.FORBIDDEN, "用户角色未定义");
        }

        // 检查用户角色是否有权限访问
        checkPermission(requireRoles, userRole);
    }

    /**
     * 从认证信息中获取用户角色
     */
    private String getUserRole(UserSession session) {
        return session.getRole();
    }

    /**
     * 检查用户是否有权限访问
     */
    private void checkPermission(RequireRoles requireRoles, String userRole) {
        // 获取接口要求的角色
        RoleEnum[] requiredRoles = requireRoles.value();
        List<String> requiredRoleCodes = Arrays.stream(requiredRoles)
                .map(RoleEnum::getCode)
                .collect(Collectors.toList());

        // 检查用户角色是否在允许的角色列表中
        if (!requiredRoleCodes.contains(userRole)) {
            throw new GlobalException(ResultCode.FORBIDDEN,
                    String.format("没有访问权限，需要以下角色之一: %s", requiredRoleCodes));
        }
    }
}


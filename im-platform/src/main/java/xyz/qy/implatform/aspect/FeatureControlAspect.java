package xyz.qy.implatform.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import xyz.qy.implatform.annotation.FeatureControl;
import xyz.qy.implatform.contant.Constant;
import xyz.qy.implatform.contant.RedisKey;
import xyz.qy.implatform.enums.FeatureCodeEnum;
import xyz.qy.implatform.enums.RoleEnum;
import xyz.qy.implatform.exception.GlobalException;
import xyz.qy.implatform.session.SessionContext;
import xyz.qy.implatform.session.UserSession;
import xyz.qy.implatform.util.DateTimeUtils;
import xyz.qy.implatform.util.RedisCache;

import javax.annotation.Resource;
import java.lang.reflect.Method;

/**
 * 功能项控制切面，用于判断功能是否被禁用
 * @author Polaris
 */
@Slf4j
@Aspect
@Component
@Order(9)
public class FeatureControlAspect {
    @Resource
    private RedisCache redisCache;

    /**
     * 环绕通知，用于拦截带有@FeatureControl注解的方法
     */
    @Around("@annotation(xyz.qy.implatform.annotation.FeatureControl)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            UserSession session = SessionContext.getSession();
            if (session != null && session.getRole().equals(RoleEnum.SUPER_ADMIN.getCode())) {
                return joinPoint.proceed();
            }
        } catch (Exception e) {
            log.error("用户未登录");
        }

        // 获取方法签名
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        // 获取注解及功能编码
        FeatureControl featureControl = method.getAnnotation(FeatureControl.class);
        String featureCode = featureControl.value();

        // 判断Redis中是否存在该功能项的禁用标记
        Boolean isDisabled = redisCache.hasKey(RedisKey.IM_FEATURE_BAN + featureCode);

        if (Boolean.TRUE.equals(isDisabled)) {
            long expire = redisCache.getExpire(RedisKey.IM_FEATURE_BAN + featureCode);
            if (expire == -1) {
                // 功能被禁用，抛出异常或返回特定结果
                throw new GlobalException("【" + FeatureCodeEnum.getDescByCode(featureCode) + "】功能已被禁用");
            } else {
                throw new GlobalException("【" + FeatureCodeEnum.getDescByCode(featureCode) + "】功能已被禁用，请在" + DateTimeUtils.getTimeValueDesc(expire) + "后再尝试");
            }
        }

        // 功能未被禁用，正常执行方法
        return joinPoint.proceed();
    }
}


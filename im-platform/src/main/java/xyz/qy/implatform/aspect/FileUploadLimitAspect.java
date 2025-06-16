package xyz.qy.implatform.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import xyz.qy.imclient.annotation.CountLimit;
import xyz.qy.implatform.contant.RedisKey;
import xyz.qy.implatform.exception.GlobalException;
import xyz.qy.implatform.session.SessionContext;
import xyz.qy.implatform.session.UserSession;
import xyz.qy.implatform.util.RedisCache;

import javax.annotation.Resource;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

/**
 * 执行次数限制切面
 *
 * @author Polaris
 * @since 2024-10-14
 */
@Slf4j
@Aspect
@Order(99)
@Component
public class FileUploadLimitAspect {
    @Resource
    private RedisCache redisCache;

    @Pointcut("@annotation(xyz.qy.imclient.annotation.CountLimit)")
    public void freqLimitAspect() {
    }

    @Around("freqLimitAspect()")
    public Object aspectBefore(ProceedingJoinPoint point) throws Throwable {
        UserSession session = SessionContext.getSession();
        Long userId = session.getUserId();

        Class<?> clazz = point.getTarget().getClass();
        MethodSignature methodSignature = (MethodSignature) point.getSignature();
        Method method = clazz.getDeclaredMethod(methodSignature.getName(), methodSignature.getParameterTypes());
        CountLimit countLimitAnnotation = method.getAnnotation(CountLimit.class);

        Boolean exists = redisCache.hasKey(RedisKey.IM_USER_OPERATION + countLimitAnnotation.limitType() + userId);
        int redisCount = redisCache.incrementInt(RedisKey.IM_USER_OPERATION + countLimitAnnotation.limitType() + userId);
        if (!exists) {
            redisCache.expire(RedisKey.IM_USER_OPERATION + countLimitAnnotation.limitType() + userId,
                    countLimitAnnotation.time(), TimeUnit.HOURS);
        }

        int count = countLimitAnnotation.count();

        if (redisCount > count) {
            throw new GlobalException("您在" + countLimitAnnotation.time() +"小时内" + countLimitAnnotation.description() + "数量已达到上限");
        } else {
            return point.proceed();
        }
    }
}

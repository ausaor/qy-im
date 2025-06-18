package xyz.qy.implatform.aspect;

import cn.hutool.core.util.ObjectUtil;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;
import xyz.qy.implatform.annotation.RateLimiter;
import xyz.qy.implatform.contant.Constant;
import xyz.qy.implatform.enums.LimitType;
import xyz.qy.implatform.exception.GlobalException;
import xyz.qy.implatform.util.IpUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

/**
 * 限流处理
 *
 * @author ruoyi
 */
@Aspect
@Component
public class RateLimiterAspect {
    private static final Logger log = LoggerFactory.getLogger(RateLimiterAspect.class);

    private static final int COUNT = 10;

    private static final int TIME = 5;

    private RedisTemplate<Object, Object> redisTemplate;

    private RedisScript<Long> limitScript;

    @Resource
    private HttpServletRequest request;

    @Qualifier("objRedisTemplate")
    @Autowired
    public void setRedisTemplate1(RedisTemplate<Object, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Autowired
    public void setLimitScript(RedisScript<Long> limitScript) {
        this.limitScript = limitScript;
    }

    //@Before("@annotation(rateLimiter)")
    @Before("execution(* xyz.qy.implatform.controller..*(..))")
    public void doBefore(JoinPoint point) throws Throwable {

        String combineKey = getCombineKey(point);
        List<Object> keys = Collections.singletonList(combineKey);
        try {
            Long number = redisTemplate.execute(limitScript, keys, COUNT, TIME);
            if (ObjectUtil.isNull(number) || number.intValue() > COUNT) {
                throw new GlobalException("访问过于频繁，请稍候再试");
            }
            log.info("限制请求'{}',当前请求'{}',缓存key'{}'", COUNT, number.intValue(), combineKey);
        } catch (GlobalException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("服务器限流异常，请稍候再试");
        }
    }

    public String getCombineKey(JoinPoint point) {
        //StringBuffer stringBuffer = new StringBuffer(rateLimiter.key());
        StringBuffer stringBuffer = new StringBuffer(Constant.RATE_LIMIT_KEY);
        //if (rateLimiter.limitType() == LimitType.IP) {
        stringBuffer.append(IpUtils.getIp(request)).append("-");
        //}
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        Class<?> targetClass = method.getDeclaringClass();
        stringBuffer.append(targetClass.getName()).append("-").append(method.getName());
        return stringBuffer.toString();
    }
}

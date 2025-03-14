package xyz.qy.implatform.aspect;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import xyz.qy.imclient.IMClient;
import xyz.qy.implatform.exception.GlobalException;
import xyz.qy.implatform.session.SessionContext;
import xyz.qy.implatform.session.UserSession;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class OnlineCheckAspect {

    private final IMClient imClient;

    @Around("@annotation(xyz.qy.implatform.annotation.OnlineCheck)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        UserSession session = SessionContext.getSession();
        if(!imClient.isOnline(session.getUserId())){
            throw new GlobalException("您当前的网络连接已断开,请稍后重试");
        }
        return joinPoint.proceed();
    }

}

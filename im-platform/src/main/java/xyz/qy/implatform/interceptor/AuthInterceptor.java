package xyz.qy.implatform.interceptor;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import xyz.qy.imcommon.util.JwtUtil;
import xyz.qy.implatform.config.JwtProperties;
import xyz.qy.implatform.contant.RedisKey;
import xyz.qy.implatform.enums.ResultCode;
import xyz.qy.implatform.exception.GlobalException;
import xyz.qy.implatform.session.UserSession;
import xyz.qy.implatform.util.RedisCache;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
@Component
@AllArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    private final JwtProperties jwtProperties;

    private final RedisCache redisCache;

    @Override
    public boolean preHandle(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull Object handler) throws Exception {
        //如果不是映射到方法直接通过
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        //从 http 请求头中取出 token
        String token = request.getHeader("accessToken");
        if (StrUtil.isEmpty(token)) {
            log.error("未登陆，url:{}", request.getRequestURI());
            throw new GlobalException(ResultCode.NO_LOGIN);
        }
        String strJson = JwtUtil.getInfo(token);
        UserSession userSession = JSON.parseObject(strJson, UserSession.class);
        if (redisCache.hasKey(RedisKey.IM_USER_BAN_ACCOUNT + userSession.getUserId())) {
            throw new GlobalException("账号已被封禁");
        }

        //验证 token
        if (!JwtUtil.checkSign(token, jwtProperties.getAccessTokenSecret())) {
            log.error("token已失效，用户:{}", userSession.getUserName());
            log.error("token:{}", token);
            throw new GlobalException(ResultCode.INVALID_TOKEN);
        }
        // 存放session
        request.setAttribute("session", userSession);
        return true;
    }
}

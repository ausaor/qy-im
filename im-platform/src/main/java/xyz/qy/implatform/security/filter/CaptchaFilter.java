//package xyz.qy.implatform.security.filter;
//
//import xyz.qy.implatform.contant.RedisKey;
//import xyz.qy.implatform.exception.CaptchaException;
//import xyz.qy.implatform.security.LoginFailureHandler;
//import xyz.qy.implatform.util.RedisCache;
//import org.apache.commons.lang3.StringUtils;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//import org.springframework.web.filter.OncePerRequestFilter;
//
//import javax.servlet.FilterChain;
//import javax.servlet.ServletException;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import java.io.IOException;
//
///**
// * @description: 验证码过滤器
// * @author: Polaris
// * @create: 2023-04-23 20:43
// **/
//@Component
//public class CaptchaFilter extends OncePerRequestFilter {
//    @Autowired
//    LoginFailureHandler loginFailureHandler;
//
//    @Autowired
//    RedisCache redisCache;
//
//    @Override
//    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
//        String url = request.getRequestURI();
//        if ("/login".equals(url) && request.getMethod().equals("POST")) {
//            // 校验验证码
//            try {
//                validate(request);
//            } catch (CaptchaException e) {
//                // 交给认证失败处理器
//                loginFailureHandler.onAuthenticationFailure(request, response, e);
//            }
//        }
//        filterChain.doFilter(request, response);
//    }
//
//    // 校验验证码逻辑
//    private void validate(HttpServletRequest httpServletRequest) {
//        String code = httpServletRequest.getParameter("code");
//        String uuid = httpServletRequest.getParameter("uuid");
//
//        if (StringUtils.isBlank(code) || StringUtils.isBlank(uuid)) {
//            throw new CaptchaException("验证码错误");
//        }
//        String verifyKey = RedisKey.CAPTCHA_CODE_KEY + uuid;
//        String captcha = redisCache.getCacheObject(verifyKey);
//        if (StringUtils.isBlank(captcha)) {
//            throw new CaptchaException("验证码已过期");
//        }
//
//        if (!code.equals(captcha)) {
//            throw new CaptchaException("验证码错误");
//        }
//        redisCache.deleteObject(verifyKey);
//    }
//}

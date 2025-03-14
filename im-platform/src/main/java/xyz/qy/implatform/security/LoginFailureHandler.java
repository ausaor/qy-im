//package xyz.qy.implatform.security;
//
//import xyz.qy.implatform.enums.ResultCode;
//import xyz.qy.implatform.exception.CaptchaException;
//import xyz.qy.implatform.result.Result;
//import xyz.qy.implatform.result.ResultUtils;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.security.authentication.AccountExpiredException;
//import org.springframework.security.authentication.BadCredentialsException;
//import org.springframework.security.authentication.CredentialsExpiredException;
//import org.springframework.security.authentication.DisabledException;
//import org.springframework.security.authentication.LockedException;
//import org.springframework.security.core.AuthenticationException;
//import org.springframework.security.web.authentication.AuthenticationFailureHandler;
//import org.springframework.stereotype.Component;
//
//import javax.servlet.ServletException;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import java.io.IOException;
//import java.io.PrintWriter;
//
///**
// * @description: 登录失败处理类
// * @author: Polaris
// * @create: 2023-04-23 20:34
// **/
//@Slf4j
//@Component
//public class LoginFailureHandler implements AuthenticationFailureHandler {
//    @Override
//    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
//        response.setContentType("application/json;charset=utf-8");
//        PrintWriter out = response.getWriter();
//        Result result = ResultUtils.error(ResultCode.LOGIN_ERROR,exception.getMessage());
//        if (exception instanceof LockedException) {
//            result =ResultUtils.error(ResultCode.LOGIN_ERROR,"账户被锁定，请联系管理员!");
//        } else if (exception instanceof CredentialsExpiredException) {
//            result = ResultUtils.error(ResultCode.LOGIN_ERROR,"密码过期，请联系管理员!");
//        } else if (exception instanceof AccountExpiredException) {
//            result =ResultUtils.error(ResultCode.LOGIN_ERROR,"账户过期，请联系管理员!");
//        } else if (exception instanceof DisabledException) {
//            result = ResultUtils.error(ResultCode.LOGIN_ERROR,"账户被禁用，请联系管理员!");
//        } else if (exception instanceof BadCredentialsException) {
//            result =ResultUtils.error(ResultCode.LOGIN_ERROR,"用户名或者密码输入错误，请重新输入!");
//        } else if (exception instanceof CaptchaException) {
//            result =ResultUtils.error(ResultCode.LOGIN_ERROR, exception.getMessage());
//        }
//        out.write(new ObjectMapper().writeValueAsString(result));
//        out.flush();
//        out.close();
//    }
//}

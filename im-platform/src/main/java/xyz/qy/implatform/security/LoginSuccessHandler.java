//package xyz.qy.implatform.security;
//
//import xyz.qy.implatform.result.Result;
//import xyz.qy.implatform.result.ResultUtils;
//import xyz.qy.implatform.session.UserSession;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
//import org.springframework.stereotype.Component;
//
//import javax.servlet.ServletException;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import java.io.IOException;
//import java.io.PrintWriter;
//
///**
// * @description: 登陆成功处理类
// * @author: Polaris
// * @create: 2023-04-23 20:30
// **/
//@Slf4j
//@Component
//public class LoginSuccessHandler implements AuthenticationSuccessHandler {
//
//    @Override
//    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
//        UserSession userSession = (UserSession)authentication.getPrincipal();
//        log.info("用户 '{}' 登录,id:{},昵称:{}",userSession.getUserName(),userSession.getId(),userSession.getNickName());
//        // 响应
//        response.setContentType("application/json;charset=utf-8");
//        PrintWriter out = response.getWriter();
//        Result result = ResultUtils.success();
//        out.write(new ObjectMapper().writeValueAsString(result));
//        out.flush();
//        out.close();
//    }
//}

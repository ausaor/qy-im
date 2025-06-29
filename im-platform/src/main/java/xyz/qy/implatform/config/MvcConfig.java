package xyz.qy.implatform.config;

import lombok.AllArgsConstructor;
import xyz.qy.implatform.config.intercept.PageableInterceptor;
import xyz.qy.implatform.interceptor.AuthInterceptor;
import xyz.qy.implatform.interceptor.XssInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@AllArgsConstructor
public class MvcConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;
    private final XssInterceptor xssInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(xssInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/error");
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/website/**", "/home/**", "/captchaImage", "/social/login/*" ,
                        "/login","/logout","/register","/refreshToken", "/openApi/**", "/email/getCode",
                        "/swagger-resources/**", "/webjars/**", "/v2/**", "/swagger-ui.html/**");
        //分页拦截器
        registry.addInterceptor(new PageableInterceptor());
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        // 使用BCrypt加密密码
        return new BCryptPasswordEncoder();
    }
}

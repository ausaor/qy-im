package xyz.qy.implatform.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class GlobalCorsConfig {
    @Bean
    public FilterRegistrationBean corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        // 设置访问源地址
        // config.addAllowedOriginPattern(CorsConfiguration.ALL);
        //config.setAllowedOriginPatterns(Collections.singletonList(CorsConfiguration.ALL));
        // 设置允许的域名访问
        config.addAllowedOriginPattern("http://localhost:*");
        config.addAllowedOriginPattern("https://localhost:*");
        config.addAllowedOriginPattern("https://im.timemory.xyz");

        // 设置访问源请求头
        config.addAllowedHeader(CorsConfiguration.ALL);
        // 设置访问源请求方法
        config.addAllowedMethod(CorsConfiguration.ALL);
        // 对接口配置跨域设置
        source.registerCorsConfiguration("/**", config);
        FilterRegistrationBean bean = new FilterRegistrationBean(new CorsFilter(source));
        bean.setOrder(0);
        return bean;
        //return new CorsFilter(source);
    }

}

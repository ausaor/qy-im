package xyz.qy.implatform.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

    @Autowired
    private DeepSeekConfig deepSeekConfig;

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder){
        return builder.build();
    }

    @Bean(name = "deepSeekRestTemplate")
    public RestTemplate deepSeekRestTemplate() {
        RestTemplate restTemplate = new RestTemplate();

        // 添加请求拦截器，自动添加Authorization头
        restTemplate.getInterceptors().add((request, body, execution) -> {
            request.getHeaders().add("Authorization", "Bearer " + deepSeekConfig.getKey());
            return execution.execute(request, body);
        });

        return restTemplate;
    }
}


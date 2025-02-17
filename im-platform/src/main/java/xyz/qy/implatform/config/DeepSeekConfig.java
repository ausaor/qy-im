package xyz.qy.implatform.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "deepseek.api")
@Data
public class DeepSeekConfig {
    private String key;
    private String url;
}
package xyz.qy.implatform.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @description: 七牛云配置
 * @author: Polaris
 * @create: 2022-08-07 09:48
 **/
@Data
@Configuration
@ConfigurationProperties(prefix = "upload.qiniu")
public class QiNiuConfigProperties {
    /**
     * 域名
     */
    private String domain;

    /**
     * 公钥
     */
    private String accessKey;

    /**
     * 私钥
     */
    private String secretKey;

    /**
     * 存储空间名
     */
    private String bucket;
}

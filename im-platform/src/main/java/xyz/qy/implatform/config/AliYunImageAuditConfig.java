package xyz.qy.implatform.config;

import com.aliyun.imageaudit20191230.Client;
import com.aliyun.teaopenapi.models.Config;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 阿里云内容检测配置类
 * 配置阿里云图片检测 SDK 客户端
 *
 * @author Polaris
 * @since 2026-03-16
 */
@Slf4j
@Configuration
public class AliYunImageAuditConfig {

    @Value("${image.detect.aliyun.ak}")
    private String accessKeyId;

    @Value("${image.detect.aliyun.sk}")
    private String accessKeySecret;

    /**
     * 创建阿里云图片检测客户端
     *
     * @return 图片检测客户端实例
     * @throws Exception 创建失败时抛出异常
     */
    @Bean
    public Client aliYunImageAuditClient() throws Exception {
        log.info("初始化阿里云图片检测客户端...");
        
        // 初始化配置对象，存放 AccessKeyId、AccessKeySecret、endpoint 等配置
        Config config = new Config()
                .setAccessKeyId(accessKeyId)
                .setAccessKeySecret(accessKeySecret);
        
        // 访问的域名
        config.endpoint = "imageaudit.cn-shanghai.aliyuncs.com";
        
        Client client = new Client(config);
        log.info("阿里云图片检测客户端初始化完成");
        return client;
    }
}

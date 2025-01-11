package xyz.qy.implatform;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@Slf4j
@EnableAsync
@EnableScheduling
@EnableAspectJAutoProxy(exposeProxy = true)
@ComponentScan(basePackages = {"xyz.qy"})
@MapperScan(basePackages = {"xyz.qy.implatform.mapper"})
@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
public class ImplatformApp {
    public static void main(String[] args) {
        ConfigurableApplicationContext application = SpringApplication.run(ImplatformApp.class, args);

        Environment env = application.getEnvironment();
        String port = env.getProperty("server.port");
        String uploadMode = env.getProperty("upload.mode");
        String imageDetectMode = env.getProperty("image.detect.mode");

        System.out.println("\n----------------------------------------------------------\n\t" +
                "qy-im is running! \n\t" +
                "server.port:" + port + "\n\t" +
                "upload.mode:" + uploadMode + "\n\t" +
                "image.detect.mode:" + imageDetectMode + "\n" +
                "----------------------------------------------------------");
    }
}

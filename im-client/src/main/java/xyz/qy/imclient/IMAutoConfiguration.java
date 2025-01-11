package xyz.qy.imclient;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;


@Slf4j
@Configuration
@ComponentScan(basePackages = {"xyz.qy.imclient", "xyz.qy.imcommon"})
public class IMAutoConfiguration {
}

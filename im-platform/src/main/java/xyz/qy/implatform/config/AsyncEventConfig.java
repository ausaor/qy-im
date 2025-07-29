//package xyz.qy.implatform.config;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.context.event.ApplicationEventMulticaster;
//import org.springframework.context.event.SimpleApplicationEventMulticaster;
//import org.springframework.core.task.SimpleAsyncTaskExecutor;
//
//@Configuration
//public class AsyncEventConfig {
//    @Bean
//    public ApplicationEventMulticaster eventMulticaster() {
//        SimpleApplicationEventMulticaster multicaster = new SimpleApplicationEventMulticaster();
//        multicaster.setTaskExecutor(new SimpleAsyncTaskExecutor());
//        return multicaster;
//    }
//}
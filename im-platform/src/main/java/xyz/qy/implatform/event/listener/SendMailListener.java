package xyz.qy.implatform.event.listener;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import xyz.qy.implatform.event.EmailEvent;

import javax.annotation.Resource;

/**
 * 发送邮件
 *
 * @author Polaris
 * @since 202409-08
 */
@Slf4j
@Component
public class SendMailListener implements ApplicationListener<EmailEvent> {
    /**
     * 邮箱号
     */
    @Value("${spring.mail.username}")
    private String email;

    @Resource
    private JavaMailSender javaMailSender;

    //@EventListener(EmailEvent.class)
    @Async("threadPoolTaskExecutor")
    public void onApplicationEvent(@NotNull EmailEvent event) {
        log.info("SendMailListener event:{}", event);
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(email);
        message.setTo(event.getReceiver());
        message.setSubject(event.getSubject());
        message.setText(event.getContent());
        javaMailSender.send(message);
        log.info("绑定邮箱验证码发送成功:{}", event.getReceiver());
    }
}

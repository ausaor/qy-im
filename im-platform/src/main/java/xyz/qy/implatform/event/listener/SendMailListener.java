package xyz.qy.implatform.event.listener;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import xyz.qy.implatform.event.EmailEvent;

import javax.annotation.Resource;
import javax.mail.internet.MimeMessage;

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

    @Resource
    private TemplateEngine templateEngine;

    //@EventListener(EmailEvent.class)
    @Async("threadPoolTaskExecutor")
    public void onApplicationEvent(@NotNull EmailEvent event) {
        try {
            log.info("SendMailListener event:{}", event);
            
            // 创建 MIME 消息
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            // 设置发件人
            helper.setFrom(email);
            // 设置收件人
            helper.setTo(event.getReceiver());
            // 设置主题
            helper.setSubject(event.getSubject());
            
            // 使用 Thymeleaf 模板引擎处理 HTML 模板
            Context context = new Context();
            context.setVariable("code", event.getCode());
            context.setVariable("senderName", "聼夏");
            context.setVariable("senderEmail", "qingyu017@qq.com");
            String htmlContent = templateEngine.process("emailCaptcha", context);
            
            // 设置 HTML 内容
            helper.setText(htmlContent, true);
            
            // 发送邮件
            javaMailSender.send(message);
            log.info("邮箱验证码发送成功:{}", event.getReceiver());
        } catch (Exception e) {
            log.error("发送邮件失败:{}", event.getReceiver(), e);
        }
    }
}

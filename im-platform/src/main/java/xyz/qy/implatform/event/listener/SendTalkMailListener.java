package xyz.qy.implatform.event.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import xyz.qy.implatform.event.TalkEvent;
import xyz.qy.implatform.service.IUserService;

import javax.annotation.Resource;
import javax.mail.internet.MimeMessage;

@Slf4j
@Component
public class SendTalkMailListener implements ApplicationListener<TalkEvent> {
    /**
     * 发件邮箱
     */
    @Value("${spring.mail.username}")
    private String email;

    @Resource
    private JavaMailSender javaMailSender;

    @Resource
    private TemplateEngine templateEngine;

    @Resource
    private IUserService userService;

    @Override
    @Async("threadPoolTaskExecutor")
    public void onApplicationEvent(TalkEvent event) {
        try {
            String[] adminEmails = userService.getAdminEmail();
            if (adminEmails == null || adminEmails.length == 0) {
                log.info("当前没有管理员邮箱，不发送邮件");
                return;
            }

            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(email);
            helper.setTo(adminEmails);
            helper.setSubject(event.getSubject());

            Context context = new Context();
            context.setVariable("subject", event.getSubject());
            context.setVariable("nickName", event.getNickName());
            context.setVariable("userName", event.getUserName());
            String htmlContent = templateEngine.process("talkReview", context);
            helper.setText(htmlContent, true);

            javaMailSender.send(message);
            log.info("动态审核邮件发送成功: userName={}, receiver={}", event.getUserName(), email);
        } catch (Exception e) {
            log.error("动态审核邮件发送失败: userName={}, receiver={}", event.getUserName(), email, e);
        }
    }
}

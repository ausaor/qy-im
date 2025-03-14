package xyz.qy.implatform.event.publisher;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import xyz.qy.implatform.event.EmailEvent;

import javax.annotation.Resource;

/**
 * 发送邮件推送
 *
 * @author Polaris
 * @since 2024-09-08
 */
@Component
public class SendMailEventPublisher {
    @Resource
    private ApplicationEventPublisher publisher;

    public void sendMailAsync(String subject, String content, String receiver) {
        publisher.publishEvent(new EmailEvent(this, subject, content, receiver));
    }
}

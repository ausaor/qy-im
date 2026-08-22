package xyz.qy.implatform.event.publisher;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import xyz.qy.implatform.event.ShortVideoEvent;

import javax.annotation.Resource;

/**
 * 短视频审核邮件推送
 *
 * @author Polaris
 * @since 2024-09-08
 */
@Component
public class SendShortVideoMailEventPublisher {
    @Resource
    private ApplicationEventPublisher publisher;

    public void sendMailAsync(String subject, String nickName, String userName) {
        publisher.publishEvent(new ShortVideoEvent(this, subject, nickName, userName));
    }
}

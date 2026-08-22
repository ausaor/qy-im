package xyz.qy.implatform.event.publisher;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import xyz.qy.implatform.event.TalkEvent;

import javax.annotation.Resource;

/**
 * 动态审核邮件推送
 *
 * @author Polaris
 * @since 2024-09-08
 */
@Component
public class SendTalkMailEventPublisher {
    @Resource
    private ApplicationEventPublisher publisher;

    public void sendMailAsync(String subject, String nickName, String userName) {
        publisher.publishEvent(new TalkEvent(this, subject, nickName, userName));
    }
}

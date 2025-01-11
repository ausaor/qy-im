package xyz.qy.implatform.event;

import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

/**
 * 邮件发送事件
 *
 * @author Polaris
 * @since 2024-09-08
 */
@Getter
@Setter
public class EmailEvent extends ApplicationEvent {
    // 主题
    private String subject;

    // 内容
    private String content;

    // 收件人
    private String receiver;

    public EmailEvent(Object source) {
        super(source);
    }

    public EmailEvent(Object source, String subject, String content, String receiver) {
        super(source);
        this.subject = subject;
        this.content = content;
        this.receiver = receiver;
    }
}

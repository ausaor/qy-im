package xyz.qy.implatform.event;

import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

/**
 * 动态审核事件
 *
 * @author Polaris
 * @since 2026-08-21
 */
@Getter
@Setter
public class TalkEvent extends ApplicationEvent {
    // 主题
    private String subject;

    private String nickName;

    private String userName;

    public TalkEvent(Object source) {
        super(source);
    }

    public TalkEvent(Object source, String subject, String nickName, String userName) {
        super(source);
        this.subject = subject;
        this.nickName = nickName;
        this.userName = userName;
    }
}

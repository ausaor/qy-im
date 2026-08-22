package xyz.qy.implatform.event;

import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

/**
 * 短视频审核事件
 *
 * @author Polaris
 * @since 2026-08-21
 */
@Getter
@Setter
public class ShortVideoEvent extends ApplicationEvent {
    // 主题
    private String subject;

    private String nickName;

    private String userName;

    public ShortVideoEvent(Object source) {
        super(source);
    }

    public ShortVideoEvent(Object source, String subject, String nickName, String userName) {
        super(source);
        this.subject = subject;
        this.nickName = nickName;
        this.userName = userName;
    }
}

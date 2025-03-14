package xyz.qy.implatform.session;

import lombok.Data;
import lombok.EqualsAndHashCode;
import xyz.qy.imcommon.model.IMSessionInfo;

@EqualsAndHashCode(callSuper = true)
@Data
public class UserSession extends IMSessionInfo {
    /**
     * 用户名称
     */
    private String userName;

    /**
     * 用户昵称
     */
    private String nickName;
}

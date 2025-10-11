package xyz.qy.implatform.session;

import lombok.Data;
import lombok.EqualsAndHashCode;
import xyz.qy.imcommon.model.IMSessionInfo;

/**
 * 用户会话
 *
 * @author Polaris
 */
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

    /**
     * 角色
     */
    private String role;
}

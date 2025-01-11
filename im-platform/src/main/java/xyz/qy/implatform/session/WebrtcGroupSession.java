package xyz.qy.implatform.session;

import lombok.Data;
import xyz.qy.imcommon.model.IMUserInfo;

import java.util.LinkedList;
import java.util.List;

@Data
public class WebrtcGroupSession {

    /**
     *  通话发起者
     */
    private IMUserInfo host;

    /**
     * 所有被邀请的用户列表
     */
    private List<WebrtcUserInfo> userInfos;

    /**
     * 已经进入通话的用户列表
     */
    private List<IMUserInfo> inChatUsers = new LinkedList<>();


}


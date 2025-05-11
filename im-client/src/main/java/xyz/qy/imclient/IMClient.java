package xyz.qy.imclient;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import xyz.qy.imclient.sender.IMSender;
import xyz.qy.imcommon.enums.IMTerminalType;
import xyz.qy.imcommon.model.IMGroupMessage;
import xyz.qy.imcommon.model.IMPrivateMessage;
import xyz.qy.imcommon.model.IMRegionGroupMessage;
import xyz.qy.imcommon.model.IMSystemMessage;
import xyz.qy.imcommon.model.IMTalkMessage;

import java.util.List;
import java.util.Map;

@Configuration
public class IMClient {

    @Autowired
    private IMSender imSender;

    /**
     * 判断用户是否在线
     *
     * @param userId 用户id
     */
    public Boolean isOnline(Long userId) {
        return imSender.isOnline(userId);
    }

    /**
     * 判断多个用户是否在线
     *
     * @param userIds 用户id列表
     * @return 在线的用户列表
     */
    public List<Long> getOnlineUser(List<Long> userIds){
        return imSender.getOnlineUser(userIds);
    }


    /**
     * 判断多个用户是否在线
     *
     * @param userIds 用户id列表
     * @return 在线的用户终端
     */
    public Map<Long,List<IMTerminalType>> getOnlineTerminal(List<Long> userIds){
        return imSender.getOnlineTerminal(userIds);
    }

    /**
     * 发送私聊消息（发送结果通过MessageListener接收）
     *
     * @param message 私有消息
     */
    public<T> void sendPrivateMessage(IMPrivateMessage<T> message) {
        imSender.sendPrivateMessage(message);
    }

    /**
     * 发送群聊消息（发送结果通过MessageListener接收）
     *
     * @param message 群聊消息
     */
    public<T> void sendGroupMessage(IMGroupMessage<T> message) {
        imSender.sendGroupMessage(message);
    }

    /**
     * 发送地区群聊消息（发送结果通过MessageListener接收）
     *
     * @param message 群聊消息
     */
    public<T> void sendRegionGroupMessage(IMRegionGroupMessage<T> message) {
        imSender.sendRegionGroupMessage(message);
    }

    /**
     * 发送系统消息（发送结果通过MessageListener接收）
     *
     * @param message 私有消息
     */
    public<T> void sendSystemMessage(IMSystemMessage<T> message){
        imSender.sendSystemMessage(message);
    }

    /**
     * 发送动态消息（发送结果通过MessageListener接收）
     *
     * @param message 动态消息
     */
    public<T> void sendTalkMessage(IMTalkMessage<T> message){
        imSender.sendTalkMessage(message);
    }
}

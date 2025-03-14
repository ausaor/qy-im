package xyz.qy.imclient.listener;

import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import xyz.qy.imclient.task.RegionGroupMessageResultTask;

import javax.annotation.Resource;

/**
 * 地区群聊消息发送结果redis通道监听
 *
 * @author Polaris
 * @since  2024-11-03
 **/
public class RegionGroupMsgSendResultChannelListener implements MessageListener {
    @Resource
    private RegionGroupMessageResultTask regionGroupMessageResultTask;

    @Override
    public void onMessage(Message message, byte[] bytes) {
        regionGroupMessageResultTask.pullMessage();
    }
}

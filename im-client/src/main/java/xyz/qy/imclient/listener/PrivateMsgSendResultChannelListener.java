package xyz.qy.imclient.listener;

import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import xyz.qy.imclient.task.PrivateMessageResultTask;

import javax.annotation.Resource;

/**
 * @description: 私聊消息发送结果redis通道监听
 * @author: Polaris
 * @create: 2023-09-09 09:50
 **/
public class PrivateMsgSendResultChannelListener implements MessageListener {
    @Resource
    private PrivateMessageResultTask privateMessageResultTask;

    @Override
    public void onMessage(Message message, byte[] bytes) {
        privateMessageResultTask.pullMessage();
    }
}

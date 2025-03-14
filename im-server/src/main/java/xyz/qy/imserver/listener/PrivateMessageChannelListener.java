package xyz.qy.imserver.listener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import xyz.qy.imserver.task.PullPrivateMessageTask;

/**
 * @description: 私聊消息redis通道监听
 * @author: Polaris
 * @create: 2022-12-11 10:19
 **/
public class PrivateMessageChannelListener implements MessageListener {
    @Autowired
    private PullPrivateMessageTask pullPrivateMessageTask;

    @Override
    public void onMessage(Message message, byte[] bytes) {
        pullPrivateMessageTask.pullMessage();
    }
}

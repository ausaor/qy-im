package xyz.qy.imserver.listener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import xyz.qy.imserver.task.PullGroupMessageTask;

/**
 * @description: 群聊消息redis通道监听
 * @author: Polaris
 * @create: 2023-09-09
 **/
public class GroupMessageChannelListener implements MessageListener {
    @Autowired
    private PullGroupMessageTask pullGroupMessageTask;

    @Override
    public void onMessage(Message message, byte[] bytes) {
        pullGroupMessageTask.pullMessage();
    }
}

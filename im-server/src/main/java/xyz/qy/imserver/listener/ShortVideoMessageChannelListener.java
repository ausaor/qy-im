package xyz.qy.imserver.listener;

import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import xyz.qy.imserver.task.PullShortVideoMessageTask;

import javax.annotation.Resource;

public class ShortVideoMessageChannelListener implements MessageListener {
    @Resource
    private PullShortVideoMessageTask pullShortVideoMessageTask;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        pullShortVideoMessageTask.pullMessage();
    }
}

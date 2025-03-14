package xyz.qy.imserver.listener;

import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import xyz.qy.imserver.task.PullSystemMessageTask;

import javax.annotation.Resource;

public class SystemMessageChannelListener implements MessageListener {
    @Resource
    private PullSystemMessageTask pullSystemMessageTask;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        pullSystemMessageTask.pullMessage();
    }
}

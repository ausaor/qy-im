package xyz.qy.imserver.listener;

import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import xyz.qy.imserver.task.PullTalkMessageTask;

import javax.annotation.Resource;

public class TalkMessageChannelListener implements MessageListener {
    @Resource
    private PullTalkMessageTask pullTalkMessageTask;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        pullTalkMessageTask.pullMessage();
    }
}

package xyz.qy.imserver.listener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import xyz.qy.imserver.task.PullRegionGroupMessageTask;

/**
 * 地区群聊消息redis通道监听
 *
 * @author Polaris
 * @since 2024-11-03
 **/
public class RegionGroupMessageChannelListener implements MessageListener {
    @Autowired
    private PullRegionGroupMessageTask pullRegionGroupMessageTask;

    @Override
    public void onMessage(Message message, byte[] bytes) {
        pullRegionGroupMessageTask.pullMessage();
    }
}

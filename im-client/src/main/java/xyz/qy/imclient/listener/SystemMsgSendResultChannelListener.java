package xyz.qy.imclient.listener;

import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import xyz.qy.imclient.task.SystemMessageResultTask;

import javax.annotation.Resource;

/**
 * 系统消息发送结果redis通道监听
 * @author Polaris
 * @since  2024-12-29
 **/
public class SystemMsgSendResultChannelListener implements MessageListener {
    @Resource
    private SystemMessageResultTask systemMessageResultTask;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        systemMessageResultTask.pullMessage();
    }
}

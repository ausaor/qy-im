package xyz.qy.imclient.listener;

import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import xyz.qy.imclient.task.GroupMessageResultTask;

import javax.annotation.Resource;

/**
 * @description: 群聊消息发送结果redis通道监听
 * @author: Polaris
 * @create: 2023-09-09 09:45
 **/
public class GroupMsgSendResultChannelListener implements MessageListener {
    @Resource
    private GroupMessageResultTask groupMessageResultTask;

    @Override
    public void onMessage(Message message, byte[] bytes) {
        groupMessageResultTask.pullMessage();
    }
}
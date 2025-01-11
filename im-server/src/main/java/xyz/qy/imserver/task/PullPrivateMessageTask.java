package xyz.qy.imserver.task;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import xyz.qy.imcommon.contant.IMRedisKey;
import xyz.qy.imcommon.enums.IMCmdType;
import xyz.qy.imcommon.model.IMRecvInfo;
import xyz.qy.imserver.netty.IMServerGroup;
import xyz.qy.imserver.netty.processor.AbstractMessageProcessor;
import xyz.qy.imserver.netty.processor.ProcessorFactory;
import xyz.qy.imserver.netty.ws.WebSocketServer;

import javax.annotation.Resource;
import java.util.Objects;

@Slf4j
@Component
public class PullPrivateMessageTask extends AbstractPullMessageTask {
    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public void pullMessage() {
        // 从redis拉取未读消息
        String key = String.join(":", IMRedisKey.IM_MESSAGE_PRIVATE_QUEUE, IMServerGroup.serverId + "");
        JSONObject jsonObject = (JSONObject) redisTemplate.opsForList().leftPop(key);
        while (!Objects.isNull(jsonObject)) {
            IMRecvInfo recvInfo = jsonObject.toJavaObject(IMRecvInfo.class);
            AbstractMessageProcessor processor = ProcessorFactory.createProcessor(IMCmdType.PRIVATE_MESSAGE);
            processor.process(recvInfo);
            // 下一条消息
            jsonObject = (JSONObject) redisTemplate.opsForList().leftPop(key);
        }
    }
}

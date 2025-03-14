package xyz.qy.imclient.task;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import xyz.qy.imclient.listener.MessageListenerMulticaster;
import xyz.qy.imcommon.contant.IMRedisKey;
import xyz.qy.imcommon.enums.IMListenerType;
import xyz.qy.imcommon.model.IMSendResult;

import javax.annotation.Resource;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * 系统消息结果处理任务
 *
 * @author Polaris
 * @since 2024-12-29
 */
@Component
@RequiredArgsConstructor
public class SystemMessageResultTask extends AbstractMessageResultTask {
    @Value("${im.result.batch:100}")
    private int batchSize;

    @Value("${spring.application.name}")
    private String appName;

    private final MessageListenerMulticaster listenerMulticaster;

    @Resource(name = "IMRedisTemplate")
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public void pullMessage() {
        List<IMSendResult> results;
        do {
            results = loadBatch();
            if (!results.isEmpty()) {
                listenerMulticaster.multicast(IMListenerType.SYSTEM_MESSAGE, results);
            }
        } while (results.size() >= batchSize);
    }

    private List<IMSendResult> loadBatch() {
        String key = StrUtil.join(":", IMRedisKey.IM_RESULT_SYSTEM_QUEUE, appName);
        //这个接口redis6.2以上才支持
        //List<Object> list = redisTemplate.opsForList().leftPop(key, batchSize);
        List<IMSendResult> results = new LinkedList<>();
        JSONObject jsonObject = (JSONObject) redisTemplate.opsForList().leftPop(key);
        while (!Objects.isNull(jsonObject) && results.size() < batchSize) {
            results.add(jsonObject.toJavaObject(IMSendResult.class));
            jsonObject = (JSONObject) redisTemplate.opsForList().leftPop(key);
        }
        return results;
    }
}

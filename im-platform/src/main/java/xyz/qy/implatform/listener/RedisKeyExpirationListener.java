package xyz.qy.implatform.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.listener.KeyExpirationEventMessageListener;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;

/**
 * Redis key 过期监听器
 *
 * @author Polaris
 */
@Component
public class RedisKeyExpirationListener extends KeyExpirationEventMessageListener {

    private static final Logger logger = LoggerFactory.getLogger(RedisKeyExpirationListener.class);

    public RedisKeyExpirationListener(@Qualifier("redisMessageListenerContainer")RedisMessageListenerContainer listenerContainer) {
        super(listenerContainer);
    }

    /**
     * 处理Redis key过期事件
     */
    @Override
    public void onMessage(Message message, byte[] pattern) {
        String expiredKey = message.toString();
        //logger.info("Redis key expired: {}", expiredKey);

        // 根据key的前缀或格式进行不同的业务处理
        handleExpiredKey(expiredKey);
    }

    private void handleExpiredKey(String key) {
        try {
            // 示例：处理验证码过期
            if (key.startsWith("captcha:")) {
                handleCaptchaExpired(key);
            }
        } catch (Exception e) {
            logger.error("处理Redis key过期事件失败, key: {}", key, e);
        }
    }

    private void handleCaptchaExpired(String captchaKey) {
        logger.info("验证码过期: {}", captchaKey);

        // 验证码过期处理逻辑
    }
}

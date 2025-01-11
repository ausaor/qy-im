package xyz.qy.imserver.config;

import com.alibaba.fastjson.support.spring.FastJsonRedisSerializer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import xyz.qy.imcommon.contant.IMConstant;
import xyz.qy.imserver.listener.GroupMessageChannelListener;
import xyz.qy.imserver.listener.PrivateMessageChannelListener;
import xyz.qy.imserver.listener.RegionGroupMessageChannelListener;
import xyz.qy.imserver.listener.SystemMessageChannelListener;

import javax.annotation.Resource;

@EnableCaching
@Configuration
public class RedisConfig {
    @Resource
    private RedisConnectionFactory factory;

    @Primary
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);

        // 设置值（value）的序列化采用FastJsonRedisSerializer
        redisTemplate.setValueSerializer(fastJsonRedisSerializer());
        redisTemplate.setHashValueSerializer(fastJsonRedisSerializer());

        // 设置键（key）的序列化采用StringRedisSerializer。
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setDefaultSerializer(new StringRedisSerializer());
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }

    public FastJsonRedisSerializer fastJsonRedisSerializer() {
        return new FastJsonRedisSerializer<>(Object.class);
    }

    /**
     * 监听器配置
     */
    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer() {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(factory);
        container.addMessageListener(privateMessageListenerAdapter(), privateMessageChannelTopic());
        container.addMessageListener(groupMessageListenerAdapter(), groupMessageChannelTopic());
        container.addMessageListener(regionGroupMessageListenerAdapter(), regionGroupMessageChannelTopic());
        container.addMessageListener(systemMessageListenerAdapter(), systemMessageChannelTopic());
        return container;
    }

    @Bean
    public MessageListenerAdapter privateMessageListenerAdapter() {
        return new MessageListenerAdapter(privateMessageChannelListener());
    }

    @Bean
    public MessageListenerAdapter groupMessageListenerAdapter() {
        return new MessageListenerAdapter(groupMessageChannelListener());
    }

    @Bean
    public MessageListenerAdapter regionGroupMessageListenerAdapter() {
        return new MessageListenerAdapter(regionGroupMessageChannelListener());
    }

    @Bean
    public MessageListenerAdapter systemMessageListenerAdapter() {
        return new MessageListenerAdapter(systemMessageChannelListener());
    }

    @Bean
    public PrivateMessageChannelListener privateMessageChannelListener() {
        return new PrivateMessageChannelListener();
    }

    @Bean
    public GroupMessageChannelListener groupMessageChannelListener() {
        return new GroupMessageChannelListener();
    }

    @Bean
    public RegionGroupMessageChannelListener regionGroupMessageChannelListener() {
        return new RegionGroupMessageChannelListener();
    }

    @Bean
    public SystemMessageChannelListener systemMessageChannelListener() {
        return new SystemMessageChannelListener();
    }

    /**
     * 私聊消息redis主题
     */
    @Bean
    ChannelTopic privateMessageChannelTopic() {
        return new ChannelTopic(IMConstant.PRIVATE_MSG_TOPIC);
    }

    /**
     * 群聊消息redis主题
     */
    @Bean
    ChannelTopic groupMessageChannelTopic() {
        return new ChannelTopic(IMConstant.GROUP_MSG_TOPIC);
    }

    @Bean
    ChannelTopic regionGroupMessageChannelTopic() {
        return new ChannelTopic(IMConstant.REGION_GROUP_MSG_TOPIC);
    }

    /**
     * 系统消息redis主题
     */
    @Bean
    ChannelTopic systemMessageChannelTopic() {
        return new ChannelTopic(IMConstant.SYSTEM_MSG_TOPIC);
    }
}

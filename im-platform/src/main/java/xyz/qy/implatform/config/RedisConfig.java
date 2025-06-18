package xyz.qy.implatform.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.cache.interceptor.CacheResolver;
import org.springframework.cache.interceptor.SimpleCacheErrorHandler;
import org.springframework.cache.interceptor.SimpleCacheResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import xyz.qy.imclient.listener.GroupMsgSendResultChannelListener;
import xyz.qy.imclient.listener.PrivateMsgSendResultChannelListener;
import xyz.qy.imclient.listener.RegionGroupMsgSendResultChannelListener;
import xyz.qy.imclient.listener.SystemMsgSendResultChannelListener;
import xyz.qy.imcommon.contant.IMConstant;

import javax.annotation.Resource;
import java.time.Duration;
import java.util.Objects;

@EnableCaching
@Configuration
public class RedisConfig extends CachingConfigurerSupport {

    @Resource
    private RedisConnectionFactory factory;

    @Primary
    @Bean(name = "redisTemplate")
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);

        // 设置值（value）的序列化采用jackson2JsonRedisSerializer
        redisTemplate.setValueSerializer(jackson2JsonRedisSerializer());
        redisTemplate.setHashValueSerializer(jackson2JsonRedisSerializer());

        // 设置键（key）的序列化采用StringRedisSerializer。
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setDefaultSerializer(new StringRedisSerializer());
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }

    @Bean
    public Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer() {
        Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<>(Object.class);
        ObjectMapper om = new ObjectMapper();
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        // 解决jackson2无法反序列化LocalDateTime的问题
        om.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        om.registerModule(new JavaTimeModule());
        om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);
        jackson2JsonRedisSerializer.setObjectMapper(om);
        return jackson2JsonRedisSerializer;
    }

    @Bean
    @Override
    public CacheResolver cacheResolver() {
        return new SimpleCacheResolver(Objects.requireNonNull(cacheManager()));
    }

    @Bean
    @Override
    public CacheErrorHandler errorHandler() {
        // 用于捕获从Cache中进行CRUD时的异常的回调处理器。
        return new SimpleCacheErrorHandler();
    }

    @Bean
    @Override
    public CacheManager cacheManager() {
        // 设置redis缓存管理器
        RedisCacheConfiguration cacheConfiguration =
                RedisCacheConfiguration.defaultCacheConfig()
                        .disableCachingNullValues()
                        .entryTtl(Duration.ofMinutes(10))
                        .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(jackson2JsonRedisSerializer()));
        return RedisCacheManager.builder(factory).cacheDefaults(cacheConfiguration).build();
    }

    /**
     * 监听器配置
     */
    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer() {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(factory);
        container.addMessageListener(privateMsgSendResultListenerAdapter(), privateMsgSendResultChannelTopic());
        container.addMessageListener(groupMsgSendResultListenerAdapter(), groupMsgSendResultChannelTopic());
        container.addMessageListener(regionGroupMsgSendResultListenerAdapter(), regionGroupMsgSendResultChannelTopic());
        container.addMessageListener(systemMsgSendResultListenerAdapter(), systemMsgSendResultChannelTopic());
        return container;
    }

    @Bean
    public MessageListenerAdapter privateMsgSendResultListenerAdapter() {
        return new MessageListenerAdapter(privateMsgSendResultChannelListener());
    }

    @Bean
    public MessageListenerAdapter groupMsgSendResultListenerAdapter() {
        return new MessageListenerAdapter(groupMsgSendResultChannelListener());
    }

    @Bean
    public MessageListenerAdapter regionGroupMsgSendResultListenerAdapter() {
        return new MessageListenerAdapter(regionGroupMsgSendResultChannelListener());
    }

    @Bean
    public MessageListenerAdapter systemMsgSendResultListenerAdapter() {
        return new MessageListenerAdapter(systemMsgSendResultChannelListener());
    }

    @Bean
    public PrivateMsgSendResultChannelListener privateMsgSendResultChannelListener() {
        return new PrivateMsgSendResultChannelListener();
    }

    @Bean
    public GroupMsgSendResultChannelListener groupMsgSendResultChannelListener() {
        return new GroupMsgSendResultChannelListener();
    }

    @Bean
    public RegionGroupMsgSendResultChannelListener regionGroupMsgSendResultChannelListener() {
        return new RegionGroupMsgSendResultChannelListener();
    }

    @Bean
    public SystemMsgSendResultChannelListener systemMsgSendResultChannelListener() {
        return new SystemMsgSendResultChannelListener();
    }

    /**
     * 私聊消息发送结果redis主题
     */
    @Bean
    ChannelTopic privateMsgSendResultChannelTopic() {
        return new ChannelTopic(IMConstant.PRIVATE_MSG_SEND_RESULT_TOPIC);
    }

    /**
     * 群聊消息发送结果redis主题
     */
    @Bean
    ChannelTopic groupMsgSendResultChannelTopic() {
        return new ChannelTopic(IMConstant.GROUP_MSG_SEND_RESULT_TOPIC);
    }

    /**
     * 群聊消息发送结果redis主题
     */
    @Bean
    ChannelTopic regionGroupMsgSendResultChannelTopic() {
        return new ChannelTopic(IMConstant.REGION_GROUP_MSG_SEND_RESULT_TOPIC);
    }

    /**
     * 系统消息发送结果redis主题
     */
    @Bean
    ChannelTopic systemMsgSendResultChannelTopic() {
        return new ChannelTopic(IMConstant.SYSTEM_MSG_SEND_RESULT_TOPIC);
    }

    @Bean(name = "objRedisTemplate")
    @SuppressWarnings(value = {"unchecked", "rawtypes"})
    public RedisTemplate<Object, Object> objRedisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<Object, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        FastJson2JsonRedisSerializer serializer = new FastJson2JsonRedisSerializer(Object.class);

        // 使用StringRedisSerializer来序列化和反序列化redis的key值
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(serializer);

        // Hash的key也采用StringRedisSerializer的序列化方式
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(serializer);

        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public DefaultRedisScript<Long> limitScript() {
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptText(limitScriptText());
        redisScript.setResultType(Long.class);
        return redisScript;
    }

    /**
     * 限流脚本
     */
    private String limitScriptText() {
        return "local key = KEYS[1]\n" +
                "local count = tonumber(ARGV[1])\n" +
                "local time = tonumber(ARGV[2])\n" +
                "local current = redis.call('get', key);\n" +
                "if current and tonumber(current) > count then\n" +
                "    return tonumber(current);\n" +
                "end\n" +
                "current = redis.call('incr', key)\n" +
                "if tonumber(current) == 1 then\n" +
                "    redis.call('expire', key, time)\n" +
                "end\n" +
                "return tonumber(current);";
    }
}

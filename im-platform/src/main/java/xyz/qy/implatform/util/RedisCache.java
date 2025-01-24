package xyz.qy.implatform.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * spring redis 工具类
 *
 * @author ruoyi
 **/
@SuppressWarnings(value = { "unchecked", "rawtypes" })
@Component
public class RedisCache
{
    @Autowired
    public RedisTemplate redisTemplate;

    /**
     * 缓存基本的对象，Integer、String、实体类等
     *
     * @param key 缓存的键值
     * @param value 缓存的值
     */
    public <T> void setCacheObject(final String key, final T value)
    {
        redisTemplate.opsForValue().set(key, value);
    }

    /**
     * 缓存基本的对象，Integer、String、实体类等
     *
     * @param key 缓存的键值
     * @param value 缓存的值
     * @param timeout 时间
     * @param timeUnit 时间颗粒度
     */
    public <T> void setCacheObject(final String key, final T value, final Integer timeout, final TimeUnit timeUnit)
    {
        redisTemplate.opsForValue().set(key, value, timeout, timeUnit);
    }

    /**
     * 设置有效时间
     *
     * @param key Redis键
     * @param timeout 超时时间
     * @return true=设置成功；false=设置失败
     */
    public boolean expire(final String key, final long timeout)
    {
        return expire(key, timeout, TimeUnit.SECONDS);
    }

    /**
     * 设置有效时间
     *
     * @param key Redis键
     * @param timeout 超时时间
     * @param unit 时间单位
     * @return true=设置成功；false=设置失败
     */
    public boolean expire(final String key, final long timeout, final TimeUnit unit)
    {
        return redisTemplate.expire(key, timeout, unit);
    }

    /**
     * 获取有效时间
     *
     * @param key Redis键
     * @return 有效时间
     */
    public long getExpire(final String key)
    {
        return redisTemplate.getExpire(key);
    }

    /**
     * 判断 key是否存在
     *
     * @param key 键
     * @return true 存在 false不存在
     */
    public Boolean hasKey(String key)
    {
        return redisTemplate.hasKey(key);
    }

    /**
     * 获得缓存的基本对象。
     *
     * @param key 缓存键值
     * @return 缓存键值对应的数据
     */
    public <T> T getCacheObject(final String key)
    {
        ValueOperations<String, T> operation = redisTemplate.opsForValue();
        return operation.get(key);
    }

    /**
     * 删除单个对象
     *
     * @param key
     */
    public boolean deleteObject(final String key)
    {
        return redisTemplate.delete(key);
    }

    /**
     * 删除集合对象
     *
     * @param collection 多个对象
     * @return
     */
    public boolean deleteObject(final Collection collection)
    {
        return redisTemplate.delete(collection) > 0;
    }

    /**
     * 缓存List数据
     *
     * @param key 缓存的键值
     * @param dataList 待缓存的List数据
     * @return 缓存的对象
     */
    public <T> long setCacheList(final String key, final List<T> dataList)
    {
        Long count = redisTemplate.opsForList().rightPushAll(key, dataList);
        return count == null ? 0 : count;
    }

    /**
     * 获得缓存的list对象
     *
     * @param key 缓存的键值
     * @return 缓存键值对应的数据
     */
    public <T> List<T> getCacheList(final String key)
    {
        return redisTemplate.opsForList().range(key, 0, -1);
    }

    /**
     * 缓存Set
     *
     * @param key 缓存键值
     * @param dataSet 缓存的数据
     * @return 缓存数据的对象
     */
    public <T> BoundSetOperations<String, T> setCacheSet(final String key, final Set<T> dataSet)
    {
        BoundSetOperations<String, T> setOperation = redisTemplate.boundSetOps(key);
        Iterator<T> it = dataSet.iterator();
        while (it.hasNext())
        {
            setOperation.add(it.next());
        }
        return setOperation;
    }

    /**
     * 获得缓存的set
     *
     * @param key
     * @return
     */
    public <T> Set<T> getCacheSet(final String key)
    {
        return redisTemplate.opsForSet().members(key);
    }

    /**
     * 缓存Map
     *
     * @param key
     * @param dataMap
     */
    public <T> void setCacheMap(final String key, final Map<String, T> dataMap)
    {
        if (dataMap != null) {
            redisTemplate.opsForHash().putAll(key, dataMap);
        }
    }

    /**
     * 获得缓存的Map
     *
     * @param key
     * @return
     */
    public <T> Map<String, T> getCacheMap(final String key)
    {
        return redisTemplate.opsForHash().entries(key);
    }

    /**
     * 往Hash中存入数据
     *
     * @param key Redis键
     * @param hKey Hash键
     * @param value 值
     */
    public <T> void setCacheMapValue(final String key, final String hKey, final T value)
    {
        redisTemplate.opsForHash().put(key, hKey, value);
    }

    /**
     * 获取Hash中的数据
     *
     * @param key Redis键
     * @param hKey Hash键
     * @return Hash中的对象
     */
    public <T> T getCacheMapValue(final String key, final String hKey)
    {
        HashOperations<String, String, T> opsForHash = redisTemplate.opsForHash();
        return opsForHash.get(key, hKey);
    }

    /**
     * 获取多个Hash中的数据
     *
     * @param key Redis键
     * @param hKeys Hash键集合
     * @return Hash对象集合
     */
    public <T> List<T> getMultiCacheMapValue(final String key, final Collection<Object> hKeys)
    {
        return redisTemplate.opsForHash().multiGet(key, hKeys);
    }

    /**
     * 删除Hash中的某条数据
     *
     * @param key Redis键
     * @param hKey Hash键
     * @return 是否成功
     */
    public boolean deleteCacheMapValue(final String key, final String hKey)
    {
        return redisTemplate.opsForHash().delete(key, hKey) > 0;
    }

    /**
     * 获得缓存的基本对象列表
     *
     * @param pattern 字符串前缀
     * @return 对象列表
     */
    public Collection<String> keys(final String pattern)
    {
        return redisTemplate.keys(pattern);
    }

    public Boolean sIsMember(String key, Object value) {
        return redisTemplate.opsForSet().isMember(key, value);
    }

    public Long sAdd(String key, Object... values) {
        return redisTemplate.opsForSet().add(key, values);
    }

    public int incrementInt(final String key) {
        return incrementInt(key, 1);
    }

    public int incrementInt(final String key, final int delta) {
        Long increment = redisTemplate.opsForValue().increment(key, delta);
        return increment.intValue();
    }

    /**
     * 添加一个元素, zset与set最大的区别就是每个元素都有一个score，因此有个排序的辅助功能;  zadd
     *
     * @param key
     * @param value
     * @param score
     */
    public void add(String key, String value, double score) {
        redisTemplate.opsForZSet().add(key, value, score);
    }

    /**
     * 尝试将元素添加到有序集合中，但仅当该元素不存在时才添加
     *
     * @param key
     * @param value
     * @param score
     * @return
     */
    public boolean addIfAbsentToZSet(String key, String value, double score) {
        return redisTemplate.opsForZSet().addIfAbsent(key, value, score);
    }

    public Double recordKeyValueScore(String key, String value, double score) {
        // 尝试将元素添加到有序集合中，但仅当该元素不存在时才添加
        redisTemplate.opsForZSet().addIfAbsent(key, value, 0D);
        return incrScore(key, value, score);
    }

    /**
     * 删除元素 zrem
     *
     * @param key
     * @param value
     */
    public void remove(String key, String value) {
        redisTemplate.opsForZSet().remove(key, value);
    }

    /**
     * score的增加or减少
     *
     * @param key
     * @param value
     * @param score
     */
    public Double incrScore(String key, String value, double score) {
        return redisTemplate.opsForZSet().incrementScore(key, value, score);
    }

    /**
     * 查询value对应的score   zscore
     *
     * @param key
     * @param value
     * @return
     */
    public Double score(String key, String value) {
        return redisTemplate.opsForZSet().score(key, value);
    }

    /**
     * 判断value在zset中的排名  zrank
     *
     * @param key
     * @param value
     * @return
     */
    public Long rank(String key, String value) {
        return redisTemplate.opsForZSet().rank(key, value);
    }

    /**
     * 返回集合的长度
     *
     * @param key
     * @return
     */
    public Long size(String key) {
        return redisTemplate.opsForZSet().zCard(key);
    }

    /**
     * 查询集合中指定顺序的值， 0 -1 表示获取全部的集合内容  zrange
     *
     * 返回有序的集合，score小的在前面
     *
     * @param key
     * @param start
     * @param end
     * @return
     */
    public Set<String> range(String key, int start, int end) {
        return redisTemplate.opsForZSet().range(key, start, end);
    }

    /**
     * 查询集合中指定顺序的值和score，0, -1 表示获取全部的集合内容
     *
     * @param key
     * @param start
     * @param end
     * @return
     */
    public Set<ZSetOperations.TypedTuple<String>> rangeWithScore(String key, int start, int end) {
        return redisTemplate.opsForZSet().rangeWithScores(key, start, end);
    }

    /**
     * 查询集合中指定顺序的值  zrevrange
     *
     * 返回有序的集合中，score大的在前面
     *
     * @param key
     * @param start
     * @param end
     * @return
     */
    public Set<String> revRange(String key, int start, int end) {
        return redisTemplate.opsForZSet().reverseRange(key, start, end);
    }

    /**
     * 根据score的值，来获取满足条件的集合  zrangebyscore
     *
     * @param key
     * @param min
     * @param max
     * @return
     */
    public Set<String> sortRange(String key, int min, int max) {
        return redisTemplate.opsForZSet().rangeByScore(key, min, max);
    }
}

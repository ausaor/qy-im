package xyz.qy.implatform.util;

import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import xyz.qy.implatform.contant.RedisKey;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserStateUtils {

    private final RedisTemplate<String, Object> redisTemplate;

    public void setBusy(Long userId){
        String key = StrUtil.join(":", RedisKey.IM_USER_STATE,userId);
        redisTemplate.opsForValue().set(key,1,30, TimeUnit.SECONDS);
    }

    public void expire(Long userId){
        String key = StrUtil.join(":", RedisKey.IM_USER_STATE,userId);
        redisTemplate.expire(key,30, TimeUnit.SECONDS);
    }

    public void setFree(Long userId){
        String key = StrUtil.join(":", RedisKey.IM_USER_STATE,userId);
        redisTemplate.delete(key);
    }

    public Boolean isBusy(Long userId){
        String key = StrUtil.join(":", RedisKey.IM_USER_STATE,userId);
        return  redisTemplate.hasKey(key);
    }
}


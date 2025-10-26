package xyz.qy.implatform.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import xyz.qy.implatform.config.WebrtcConfig;
import xyz.qy.implatform.contant.RedisKey;
import xyz.qy.implatform.util.RSAUtil;
import xyz.qy.implatform.util.RedisCache;
import xyz.qy.implatform.vo.SystemConfigVO;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class SystemService {

    private final WebrtcConfig webrtcConfig;

    private final RedisCache redisCache;

    @Value("${gaode.map.web-key}")
    private String gaoDeMapKey;

    public SystemConfigVO getSystemConfig() {
        SystemConfigVO systemConfigVO = new SystemConfigVO();

        systemConfigVO.setWebrtc(webrtcConfig);
        systemConfigVO.setGaoDeMapKey(RSAUtil.encrypt(gaoDeMapKey));
        return systemConfigVO;
    }

    public void allBanned(Integer time) {
        if (time != null) {
            redisCache.setCacheObject(RedisKey.IM_SYSTEM_MSG_SWITCH, 1, time, TimeUnit.MINUTES);
        } else {
            redisCache.setCacheObject(RedisKey.IM_SYSTEM_MSG_SWITCH, 1);
        }
    }

    public void unAllBanned() {
        redisCache.deleteObject(RedisKey.IM_SYSTEM_MSG_SWITCH);
    }
}

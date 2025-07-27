package xyz.qy.implatform.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import xyz.qy.implatform.config.WebrtcConfig;
import xyz.qy.implatform.contant.Constant;
import xyz.qy.implatform.contant.RedisKey;
import xyz.qy.implatform.exception.GlobalException;
import xyz.qy.implatform.session.SessionContext;
import xyz.qy.implatform.session.UserSession;
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
        systemConfigVO.setGaoDeMapKey(gaoDeMapKey);
        return systemConfigVO;
    }

    public void allBanned(Integer time) {
        UserSession session = SessionContext.getSession();
        Long userId = session.getUserId();
        if (!userId.equals(Constant.ADMIN_USER_ID)) {
            throw new GlobalException("不是系统管理员，无法操作");
        }
        if (time != null) {
            redisCache.setCacheObject(RedisKey.IM_SYSTEM_MSG_SWITCH, 1, time, TimeUnit.MINUTES);
        } else {
            redisCache.setCacheObject(RedisKey.IM_SYSTEM_MSG_SWITCH, 1);
        }
    }

    public void unAllBanned() {
        UserSession session = SessionContext.getSession();
        Long userId = session.getUserId();
        if (!userId.equals(Constant.ADMIN_USER_ID)) {
            throw new GlobalException("不是系统管理员，无法操作");
        }
        redisCache.deleteObject(RedisKey.IM_SYSTEM_MSG_SWITCH);
    }
}

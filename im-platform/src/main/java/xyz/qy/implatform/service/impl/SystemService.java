package xyz.qy.implatform.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import xyz.qy.implatform.config.WebrtcConfig;
import xyz.qy.implatform.contant.RedisKey;
import xyz.qy.implatform.dto.FeatureControlDTO;
import xyz.qy.implatform.enums.FeatureEnum;
import xyz.qy.implatform.util.RedisCache;
import xyz.qy.implatform.vo.FeatureStateVO;
import xyz.qy.implatform.vo.SystemConfigVO;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class SystemService {

    private final WebrtcConfig webrtcConfig;

    private final RedisCache redisCache;

    public SystemConfigVO getSystemConfig() {
        SystemConfigVO systemConfigVO = new SystemConfigVO();

        systemConfigVO.setWebrtc(webrtcConfig);
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

    public void switchFeature(FeatureControlDTO dto) {
        log.info("switch feature: {}, isOpen: {}, duration: {}", dto.getFeatureCode(), dto.getIsOpen(), dto.getDuration());

        String key = RedisKey.IM_FEATURE_BAN + dto.getFeatureCode();

        if (Boolean.TRUE.equals(dto.getIsOpen())) {
            // 开启功能：删除禁用标记
            redisCache.deleteObject(key);
            log.info("Feature [{}] has been enabled", dto.getFeatureCode());
        } else {
            // 关闭功能：设置禁用标记
            if (dto.getDuration() != null && dto.getDuration() > 0) {
                redisCache.setCacheObject(key, "1", dto.getDuration(), TimeUnit.SECONDS);
                log.info("Feature [{}] has been disabled for {} seconds", dto.getFeatureCode(), dto.getDuration());
            } else {
                redisCache.setCacheObject(key, "1");
                log.info("Feature [{}] has been permanently disabled", dto.getFeatureCode());
            }
        }
    }

    /**
     * 获取所有功能开关状态
     *
     * @return 功能开关状态列表
     */
    public List<FeatureStateVO> getFeatureStates() {
        List<FeatureStateVO> result = new ArrayList<>();
        for (FeatureEnum featureEnum : FeatureEnum.values()) {
            String key = RedisKey.IM_FEATURE_BAN + featureEnum.getCode();
            FeatureStateVO vo = new FeatureStateVO();
            vo.setFeatureCode(featureEnum.getCode());
            vo.setFeatureName(featureEnum.getName());

            if (Boolean.TRUE.equals(redisCache.hasKey(key))) {
                // 功能已关闭
                vo.setIsOpen(false);
                long expire = redisCache.getExpire(key);
                if (expire > 0) {
                    vo.setRemainingSeconds(expire);
                }
            } else {
                // 功能开启中
                vo.setIsOpen(true);
            }
            result.add(vo);
        }
        return result;
    }
}

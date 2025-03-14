package xyz.qy.implatform.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import xyz.qy.implatform.config.WebrtcConfig;
import xyz.qy.implatform.vo.SystemConfigVO;

@Service
@RequiredArgsConstructor
public class SystemService {

    private final WebrtcConfig webrtcConfig;

    @Value("${gaode.map.web-key}")
    private String gaoDeMapKey;

    public SystemConfigVO getSystemConfig() {
        SystemConfigVO systemConfigVO = new SystemConfigVO();

        systemConfigVO.setWebrtc(webrtcConfig);
        systemConfigVO.setGaoDeMapKey(gaoDeMapKey);
        return systemConfigVO;
    }
}

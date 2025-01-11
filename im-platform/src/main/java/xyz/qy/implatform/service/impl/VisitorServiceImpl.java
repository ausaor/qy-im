package xyz.qy.implatform.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import eu.bitwalker.useragentutils.Browser;
import eu.bitwalker.useragentutils.OperatingSystem;
import eu.bitwalker.useragentutils.UserAgent;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import xyz.qy.implatform.entity.Visitor;
import xyz.qy.implatform.mapper.VisitorMapper;
import xyz.qy.implatform.service.IVisitorService;
import xyz.qy.implatform.util.IpUtils;
import xyz.qy.implatform.util.LocationServicesUtil;
import xyz.qy.implatform.util.RedisCache;
import xyz.qy.implatform.util.SpringContextUtil;
import xyz.qy.implatform.vo.IpGeoInfoVO;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Objects;

import static xyz.qy.implatform.contant.RedisKey.UNIQUE_VISITOR;

/**
 * @description: 访客信息
 * @author: Polaris
 * @create: 2023-04-29 09:40
 **/
@Slf4j
@Service
public class VisitorServiceImpl extends ServiceImpl<VisitorMapper, Visitor> implements IVisitorService {
    private static final String DEV_PROFILE = "dev";

    @Resource
    private HttpServletRequest request;

    @Autowired
    private RedisCache redisCache;

    @Resource
    private LocationServicesUtil locationServicesUtil;

    @Override
    public void report() {
        // 获取ip
        String ipAddress = IpUtils.getIpAddress(request);
        if (StringUtils.isBlank(ipAddress)) {
            log.info("获取到空IP {}", LocalDateTime.now());
            return;
        }
        // 获取访问设备
        UserAgent userAgent = IpUtils.getUserAgent(request);
        Browser browser = userAgent.getBrowser();
        OperatingSystem operatingSystem = userAgent.getOperatingSystem();
        String serverName = request.getServerName();
        // 生成唯一用户标识
        String uuid = ipAddress + browser.getName() + operatingSystem.getName() + serverName;
        String md5 = DigestUtils.md5DigestAsHex(uuid.getBytes());
        // 域名

        // 判断是否访问
        if (!redisCache.sIsMember(UNIQUE_VISITOR, md5)) {
            Visitor visitor = null;
            String activeProfile = SpringContextUtil.getActiveProfile();
            if (!DEV_PROFILE.equals(activeProfile)) {
                visitor = getVisitorInfoByIp(ipAddress);
            }
            if (Objects.isNull(visitor)) {
                visitor = new Visitor();
            }
            visitor.setIp(ipAddress);
            visitor.setAddr(IpUtils.getIpSource(ipAddress));
            visitor.setBrowser(browser.getName());
            visitor.setDomain(serverName);
            visitor.setOperatingSystem(operatingSystem.getName());
            baseMapper.insert(visitor);
            // 保存唯一标识
            redisCache.sAdd(UNIQUE_VISITOR, md5);
        }
    }

    private Visitor getVisitorInfoByIp(String ip) {
        IpGeoInfoVO ipGeoInfoVO = locationServicesUtil.getIpGeoInfoByTencentApi(ip);
        Visitor visitor = null;
        try {
            if (ipGeoInfoVO != null) {
                visitor = new Visitor();
                visitor.setIp(ip);
                visitor.setNation(ipGeoInfoVO.getNation());
                visitor.setPro(ipGeoInfoVO.getPro());
                visitor.setCity(ipGeoInfoVO.getCity());
                visitor.setLocationInfo(ipGeoInfoVO.getLocationInfo());
            }
        } catch (Exception e) {
            log.error("getVisitorInfo error:{}", e.getMessage());
        }
        return visitor;
    }
}

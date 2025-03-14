package xyz.qy.implatform.strategy.impl;

import com.alibaba.fastjson.JSON;
import xyz.qy.implatform.config.QQConfigProperties;
import xyz.qy.implatform.dto.QQTokenDTO;
import xyz.qy.implatform.dto.QQUserInfoDTO;
import xyz.qy.implatform.dto.SocialTokenDTO;
import xyz.qy.implatform.dto.SocialUserInfoDTO;
import xyz.qy.implatform.enums.LoginTypeEnum;
import xyz.qy.implatform.exception.GlobalException;
import xyz.qy.implatform.util.CommonUtils;
import xyz.qy.implatform.vo.QQLoginVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import xyz.qy.implatform.contant.SocialLoginConst;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @description:
 * @author: Polaris
 * @create: 2023-04-02 15:09
 **/
@Slf4j
@Service("qqLoginStrategyImpl")
public class QQLoginStrategyImpl extends AbstractSocialLoginStrategyImpl{

    @Autowired
    private QQConfigProperties qqConfigProperties;

    @Autowired
    private RestTemplate restTemplate;

    @Override
    public SocialTokenDTO getSocialToken(String data) {
        QQLoginVO qqLoginVO = JSON.parseObject(data, QQLoginVO.class);
        // 校验QQ token信息
        checkQQToken(qqLoginVO);
        // 返回token信息
        return SocialTokenDTO.builder()
                .openId(qqLoginVO.getOpenId())
                .accessToken(qqLoginVO.getAccessToken())
                .loginType(LoginTypeEnum.QQ.getType())
                .build();
    }

    @Override
    public SocialUserInfoDTO getSocialUserInfo(SocialTokenDTO socialTokenDTO) {
        // 定义请求参数
        Map<String, String> formData = new HashMap<>(3);
        formData.put(SocialLoginConst.QQ_OPEN_ID, socialTokenDTO.getOpenId());
        formData.put(SocialLoginConst.ACCESS_TOKEN, socialTokenDTO.getAccessToken());
        formData.put(SocialLoginConst.OAUTH_CONSUMER_KEY, qqConfigProperties.getAppId());
        // 获取QQ返回的用户信息
        QQUserInfoDTO qqUserInfoDTO = JSON.parseObject(restTemplate.getForObject(qqConfigProperties.getUserInfoUrl(), String.class, formData), QQUserInfoDTO.class);
        // 返回用户信息
        return SocialUserInfoDTO.builder()
                .nickname(Objects.requireNonNull(qqUserInfoDTO).getNickname())
                .avatar(qqUserInfoDTO.getFigureurl_qq_1())
                .build();
    }

    /**
     * 校验qq token信息
     *
     * @param qqLoginVO qq登录信息
     */
    private void checkQQToken(QQLoginVO qqLoginVO) {
        // 根据token获取qq openId信息
        Map<String, String> qqData = new HashMap<>(1);
        qqData.put(SocialLoginConst.ACCESS_TOKEN, qqLoginVO.getAccessToken());
        try {
            String result = restTemplate.getForObject(qqConfigProperties.getCheckTokenUrl(), String.class, qqData);
            QQTokenDTO qqTokenDTO = JSON.parseObject(CommonUtils.getBracketsContent(Objects.requireNonNull(result)), QQTokenDTO.class);
            // 判断openId是否一致
            if (!qqLoginVO.getOpenId().equals(qqTokenDTO.getOpenid())) {
                throw new GlobalException("qq登录错误");
            }
        } catch (Exception e) {
            log.error("qq login error:{}", e.getMessage());
            throw new GlobalException("qq登录错误");
        }
    }
}

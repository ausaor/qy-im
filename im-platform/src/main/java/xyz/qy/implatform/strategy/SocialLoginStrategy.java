package xyz.qy.implatform.strategy;

import xyz.qy.implatform.vo.LoginVO;

/**
 * 第三方登录策略
 */
public interface SocialLoginStrategy {
    /**
     * 登录
     *
     * @param data 数据
     * @return 用户信息
     */
    LoginVO login(String data);
}

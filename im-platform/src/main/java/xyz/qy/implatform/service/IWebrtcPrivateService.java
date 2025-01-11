package xyz.qy.implatform.service;

import xyz.qy.implatform.config.ICEServer;

import java.util.List;

/**
 * @description: 通信服务
 * @author: Polaris
 * @create: 2024-01-13 15:33
 **/
public interface IWebrtcPrivateService {
    void call(Long uid, String mode,String offer);

    void accept(Long uid,  String answer);

    void reject( Long uid);

    void cancel( Long uid);

    void failed( Long uid, String reason);

    void handup( Long uid) ;

    void candidate( Long uid, String candidate);

    void heartbeat(Long uid);
}

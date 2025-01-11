package xyz.qy.implatform.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import xyz.qy.imclient.IMClient;
import xyz.qy.imcommon.model.IMPrivateMessage;
import xyz.qy.imcommon.model.IMUserInfo;
import xyz.qy.implatform.annotation.OnlineCheck;
import xyz.qy.implatform.contant.RedisKey;
import xyz.qy.implatform.entity.PrivateMessage;
import xyz.qy.implatform.enums.MessageStatus;
import xyz.qy.implatform.enums.MessageType;
import xyz.qy.implatform.enums.WebrtcMode;
import xyz.qy.implatform.exception.GlobalException;
import xyz.qy.implatform.service.IPrivateMessageService;
import xyz.qy.implatform.service.IWebrtcPrivateService;
import xyz.qy.implatform.session.SessionContext;
import xyz.qy.implatform.session.UserSession;
import xyz.qy.implatform.session.WebrtcPrivateSession;
import xyz.qy.implatform.util.BeanUtils;
import xyz.qy.implatform.util.UserStateUtils;
import xyz.qy.implatform.vo.PrivateMessageVO;

import java.util.Collections;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * @description:
 * @author: Polaris
 * @create: 2024-01-13 15:34
 **/
@Slf4j
@Service
@RequiredArgsConstructor
public class WebrtcPrivateServiceImpl implements IWebrtcPrivateService {

    private final IMClient imClient;
    private final RedisTemplate<String, Object> redisTemplate;
    private final IPrivateMessageService privateMessageService;
    private final UserStateUtils userStateUtils;

    @OnlineCheck
    @Override
    public void call(Long uid, String mode, String offer) {
        UserSession session = SessionContext.getSession();
        log.info("发起呼叫,sid:{},uid:{}", session.getUserId(), uid);
        // 创建webrtc会话
        WebrtcPrivateSession webrtcSession = new WebrtcPrivateSession();
        webrtcSession.setCallerId(session.getUserId());
        webrtcSession.setCallerTerminal(session.getTerminal());
        webrtcSession.setAcceptorId(uid);
        webrtcSession.setMode(mode);
        // 校验
        if (!imClient.isOnline(uid)) {
            this.sendActMessage(webrtcSession, MessageStatus.UNSEND, "未接通");
            log.info("对方不在线,uid:{}", uid);
            throw new GlobalException("对方目前不在线");
        }
        if (userStateUtils.isBusy(uid)) {
            this.sendActMessage(webrtcSession, MessageStatus.UNSEND, "未接通");
            log.info("对方正忙,uid:{}", uid);
            throw new GlobalException("对方正忙");
        }
        // 保存rtc session
        String key = getWebRtcSessionKey(session.getUserId(), uid);
        redisTemplate.opsForValue().set(key, webrtcSession, 60, TimeUnit.SECONDS);
        // 设置用户忙线状态
        userStateUtils.setBusy(uid);
        userStateUtils.setBusy(session.getUserId());
        // 向对方所有终端发起呼叫
        PrivateMessageVO messageInfo = new PrivateMessageVO();
        MessageType messageType =
                mode.equals(WebrtcMode.VIDEO.getValue()) ? MessageType.RTC_CALL_VIDEO : MessageType.RTC_CALL_VOICE;
        messageInfo.setType(messageType.code());
        messageInfo.setRecvId(uid);
        messageInfo.setSendId(session.getUserId());
        messageInfo.setContent(offer);

        IMPrivateMessage<PrivateMessageVO> sendMessage = new IMPrivateMessage<>();
        sendMessage.setSender(new IMUserInfo(session.getUserId(), session.getTerminal()));
        sendMessage.setRecvId(uid);
        sendMessage.setSendToSelf(false);
        sendMessage.setSendResult(false);
        sendMessage.setData(messageInfo);
        imClient.sendPrivateMessage(sendMessage);

    }

    @Override
    public void accept(Long uid, @RequestBody String answer) {
        UserSession session = SessionContext.getSession();
        log.info("接受通话,sid:{},uid:{}", session.getUserId(), uid);
        // 查询webrtc会话
        WebrtcPrivateSession webrtcSession = getWebrtcSession(session.getUserId(), uid);
        // 更新接受者信息
        webrtcSession.setAcceptorId(session.getUserId());
        webrtcSession.setAcceptorTerminal(session.getTerminal());
        webrtcSession.setChatTimeStamp(System.currentTimeMillis());
        String key = getWebRtcSessionKey(session.getUserId(), uid);
        redisTemplate.opsForValue().set(key, webrtcSession, 60, TimeUnit.SECONDS);
        // 向发起人推送接受通话信令
        PrivateMessageVO messageInfo = new PrivateMessageVO();
        messageInfo.setType(MessageType.RTC_ACCEPT.code());
        messageInfo.setRecvId(uid);
        messageInfo.setSendId(session.getUserId());
        messageInfo.setContent(answer);

        IMPrivateMessage<PrivateMessageVO> sendMessage = new IMPrivateMessage<>();
        sendMessage.setSender(new IMUserInfo(session.getUserId(), session.getTerminal()));
        sendMessage.setRecvId(uid);
        // 告知其他终端已经接受会话,中止呼叫
        sendMessage.setSendToSelf(true);
        sendMessage.setSendResult(false);
        sendMessage.setRecvTerminals((Collections.singletonList(webrtcSession.getCallerTerminal())));
        sendMessage.setData(messageInfo);
        imClient.sendPrivateMessage(sendMessage);
    }

    @Override
    public void reject(Long uid) {
        UserSession session = SessionContext.getSession();
        log.info("拒绝通话,sid:{},uid:{}", session.getUserId(), uid);
        // 查询webrtc会话
        WebrtcPrivateSession webrtcSession = getWebrtcSession(session.getUserId(), uid);
        // 删除会话信息
        removeWebrtcSession(uid, session.getUserId());
        // 设置用户空闲状态
        userStateUtils.setFree(uid);
        userStateUtils.setFree(session.getUserId());
        // 向发起人推送拒绝通话信令
        PrivateMessageVO messageInfo = new PrivateMessageVO();
        messageInfo.setType(MessageType.RTC_REJECT.code());
        messageInfo.setRecvId(uid);
        messageInfo.setSendId(session.getUserId());

        IMPrivateMessage<PrivateMessageVO> sendMessage = new IMPrivateMessage<>();
        sendMessage.setSender(new IMUserInfo(session.getUserId(), session.getTerminal()));
        sendMessage.setRecvId(uid);
        // 告知其他终端已经拒绝会话,中止呼叫
        sendMessage.setSendToSelf(true);
        sendMessage.setSendResult(false);
        sendMessage.setRecvTerminals(Collections.singletonList(webrtcSession.getCallerTerminal()));
        sendMessage.setData(messageInfo);
        imClient.sendPrivateMessage(sendMessage);
        // 生成通话消息
        sendActMessage(webrtcSession, MessageStatus.READED,"已拒绝");
    }

    @Override
    public void cancel(Long uid) {
        UserSession session = SessionContext.getSession();
        log.info("取消通话,sid:{},uid:{}", session.getUserId(), uid);
        // 查询webrtc会话
        WebrtcPrivateSession webrtcSession = getWebrtcSession(session.getUserId(), uid);
        // 删除会话信息
        removeWebrtcSession(session.getUserId(), uid);
        // 设置用户空闲状态
        userStateUtils.setFree(uid);
        userStateUtils.setFree(session.getUserId());
        // 向对方所有终端推送取消通话信令
        PrivateMessageVO messageInfo = new PrivateMessageVO();
        messageInfo.setType(MessageType.RTC_CANCEL.code());
        messageInfo.setRecvId(uid);
        messageInfo.setSendId(session.getUserId());

        IMPrivateMessage<PrivateMessageVO> sendMessage = new IMPrivateMessage<>();
        sendMessage.setSender(new IMUserInfo(session.getUserId(), session.getTerminal()));
        sendMessage.setRecvId(uid);
        sendMessage.setSendToSelf(false);
        sendMessage.setSendResult(false);
        sendMessage.setData(messageInfo);
        // 通知对方取消会话
        imClient.sendPrivateMessage(sendMessage);
        // 生成通话消息
        sendActMessage(webrtcSession, MessageStatus.UNSEND,"已取消");
    }

    @Override
    public void failed(Long uid, String reason) {
        UserSession session = SessionContext.getSession();
        log.info("通话失败,sid:{},uid:{},reason:{}", session.getUserId(), uid, reason);
        // 查询webrtc会话
        WebrtcPrivateSession webrtcSession = getWebrtcSession(session.getUserId(), uid);
        // 删除会话信息
        removeWebrtcSession(uid, session.getUserId());
        // 设置用户空闲状态
        userStateUtils.setFree(uid);
        userStateUtils.setFree(session.getUserId());
        // 向发起方推送通话失败信令
        PrivateMessageVO messageInfo = new PrivateMessageVO();
        messageInfo.setType(MessageType.RTC_FAILED.code());
        messageInfo.setRecvId(uid);
        messageInfo.setSendId(session.getUserId());
        messageInfo.setContent(reason);

        IMPrivateMessage<PrivateMessageVO> sendMessage = new IMPrivateMessage<>();
        sendMessage.setSender(new IMUserInfo(session.getUserId(), session.getTerminal()));
        sendMessage.setRecvId(uid);
        sendMessage.setSendToSelf(false);
        sendMessage.setSendResult(false);
        sendMessage.setRecvTerminals(Collections.singletonList(webrtcSession.getCallerTerminal()));
        sendMessage.setData(messageInfo);
        // 通知对方取消会话
        imClient.sendPrivateMessage(sendMessage);
        // 生成消息
        sendActMessage(webrtcSession, MessageStatus.READED,"未接通");
    }

    @Override
    public void handup(Long uid) {
        UserSession session = SessionContext.getSession();
        log.info("挂断通话,sid:{},uid:{}", session.getUserId(), uid);
        // 查询webrtc会话
        WebrtcPrivateSession webrtcSession = getWebrtcSession(session.getUserId(), uid);
        // 删除会话信息
        removeWebrtcSession(uid, session.getUserId());
        // 设置用户空闲状态
        userStateUtils.setFree(uid);
        userStateUtils.setFree(session.getUserId());
        // 向对方推送挂断通话信令
        PrivateMessageVO messageInfo = new PrivateMessageVO();
        messageInfo.setType(MessageType.RTC_HANDUP.code());
        messageInfo.setRecvId(uid);
        messageInfo.setSendId(session.getUserId());

        IMPrivateMessage<PrivateMessageVO> sendMessage = new IMPrivateMessage<>();
        sendMessage.setSender(new IMUserInfo(session.getUserId(), session.getTerminal()));
        sendMessage.setRecvId(uid);
        sendMessage.setSendToSelf(false);
        sendMessage.setSendResult(false);
        Integer terminal = getTerminalType(uid, webrtcSession);
        sendMessage.setRecvTerminals(Collections.singletonList(terminal));
        sendMessage.setData(messageInfo);
        // 通知对方取消会话
        imClient.sendPrivateMessage(sendMessage);
        // 生成通话消息
        sendActMessage(webrtcSession, MessageStatus.READED,"通话时长 " + chatTimeText(webrtcSession));
    }

    @Override
    public void candidate(Long uid, String candidate) {
        UserSession session = SessionContext.getSession();
        // 查询webrtc会话
        WebrtcPrivateSession webrtcSession = getWebrtcSession(session.getUserId(), uid);
        // 向发起方推送同步candidate信令
        PrivateMessageVO messageInfo = new PrivateMessageVO();
        messageInfo.setType(MessageType.RTC_CANDIDATE.code());
        messageInfo.setRecvId(uid);
        messageInfo.setSendId(session.getUserId());
        messageInfo.setContent(candidate);

        IMPrivateMessage<PrivateMessageVO> sendMessage = new IMPrivateMessage<>();
        sendMessage.setSender(new IMUserInfo(session.getUserId(), session.getTerminal()));
        sendMessage.setRecvId(uid);
        sendMessage.setSendToSelf(false);
        sendMessage.setSendResult(false);
        Integer terminal = getTerminalType(uid, webrtcSession);
        sendMessage.setRecvTerminals(Collections.singletonList(terminal));
        sendMessage.setData(messageInfo);
        imClient.sendPrivateMessage(sendMessage);
    }

    @Override
    public void heartbeat(Long uid) {
        UserSession session = SessionContext.getSession();
        // 会话续命
        String key = getWebRtcSessionKey(session.getUserId(), uid);
        redisTemplate.expire(key, 60, TimeUnit.SECONDS);
        // 用户状态续命
        userStateUtils.expire(session.getUserId());
    }

    private WebrtcPrivateSession getWebrtcSession(Long userId, Long uid) {
        String key = getWebRtcSessionKey(userId, uid);
        WebrtcPrivateSession webrtcSession = (WebrtcPrivateSession)redisTemplate.opsForValue().get(key);
        if (webrtcSession == null) {
            throw new GlobalException("通话已结束");
        }
        return webrtcSession;
    }

    private void removeWebrtcSession(Long userId, Long uid) {
        String key = getWebRtcSessionKey(userId, uid);
        redisTemplate.delete(key);
    }

    private String getWebRtcSessionKey(Long id1, Long id2) {
        Long minId = id1 > id2 ? id2 : id1;
        Long maxId = id1 > id2 ? id1 : id2;
        return String.join(":", RedisKey.IM_WEBRTC_PRIVATE_SESSION, minId.toString(), maxId.toString());
    }

    private Integer getTerminalType(Long uid, WebrtcPrivateSession webrtcSession) {
        if (uid.equals(webrtcSession.getCallerId())) {
            return webrtcSession.getCallerTerminal();
        }
        return webrtcSession.getAcceptorTerminal();
    }

    private void sendActMessage(WebrtcPrivateSession rtcSession, MessageStatus status, String content) {
        // 保存消息
        PrivateMessage msg = new PrivateMessage();
        msg.setSendId(rtcSession.getCallerId());
        msg.setRecvId(rtcSession.getAcceptorId());
        msg.setContent(content);
        msg.setSendTime(new Date());
        msg.setStatus(status.code());
        MessageType type = rtcSession.getMode().equals(WebrtcMode.VIDEO.getValue()) ? MessageType.ACT_RT_VIDEO
                : MessageType.ACT_RT_VOICE;
        msg.setType(type.code());
        privateMessageService.save(msg);
        // 推给发起人
        PrivateMessageVO messageInfo = BeanUtils.copyProperties(msg, PrivateMessageVO.class);
        IMPrivateMessage<PrivateMessageVO> sendMessage = new IMPrivateMessage<>();
        sendMessage.setSender(new IMUserInfo(rtcSession.getCallerId(), rtcSession.getCallerTerminal()));
        sendMessage.setRecvId(rtcSession.getCallerId());
        sendMessage.setSendToSelf(false);
        sendMessage.setSendResult(false);
        sendMessage.setData(messageInfo);
        imClient.sendPrivateMessage(sendMessage);
        // 推给接听方
        sendMessage.setRecvId(rtcSession.getAcceptorId());
        imClient.sendPrivateMessage(sendMessage);
    }

    private String chatTimeText(WebrtcPrivateSession rtcSession) {
        long chatTime = (System.currentTimeMillis() - rtcSession.getChatTimeStamp()) / 1000;
        int min = Math.abs((int)chatTime / 60);
        int sec = Math.abs((int)chatTime % 60);
        String strTime = min < 10 ? "0" : "";
        strTime += min;
        strTime += ":";
        strTime += sec < 10 ? "0" : "";
        strTime += sec;
        return strTime;
    }

}

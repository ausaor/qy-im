package xyz.qy.imserver.netty.processor;

import cn.hutool.core.util.StrUtil;
import io.netty.channel.ChannelHandlerContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import xyz.qy.imcommon.contant.IMConstant;
import xyz.qy.imcommon.contant.IMRedisKey;
import xyz.qy.imcommon.enums.IMCmdType;
import xyz.qy.imcommon.enums.IMSendCode;
import xyz.qy.imcommon.model.IMRecvInfo;
import xyz.qy.imcommon.model.IMSendInfo;
import xyz.qy.imcommon.model.IMSendResult;
import xyz.qy.imcommon.model.IMUserInfo;
import xyz.qy.imserver.netty.UserChannelCtxMap;

import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class SystemMessageProcessor extends AbstractMessageProcessor<IMRecvInfo> {

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public void process(IMRecvInfo recvInfo) {
        log.info("接收到系统消息,接收用户数量:{}，内容:{}",  recvInfo.getReceivers().size(), recvInfo.getData());
        for (IMUserInfo receiver : recvInfo.getReceivers()) {
            try {
                ChannelHandlerContext channelCtx =
                        UserChannelCtxMap.getChannelCtx(receiver.getId(), receiver.getTerminal());
                if (!Objects.isNull(channelCtx)) {
                    // 推送消息到用户
                    IMSendInfo<Object> sendInfo = new IMSendInfo<>();
                    sendInfo.setCmd(IMCmdType.SYSTEM_MESSAGE.code());
                    sendInfo.setData(recvInfo.getData());
                    channelCtx.channel().writeAndFlush(sendInfo);
                    // 消息发送成功确认
                    sendResult(recvInfo, IMSendCode.SUCCESS);
                } else {
                    // 消息推送失败确认
                    sendResult(recvInfo, IMSendCode.NOT_FIND_CHANNEL);
                    log.error("未找到channel，接收者:{}，内容:{}", receiver.getId(), recvInfo.getData());
                }
            } catch (Exception e) {
                // 消息推送失败确认
                sendResult(recvInfo, IMSendCode.UNKONW_ERROR);
                log.error("发送异常，,接收者:{}，内容:{}", receiver.getId(), recvInfo.getData(), e);
            }
        }
    }

    private void sendResult(IMRecvInfo recvInfo, IMSendCode sendCode) {
        if (recvInfo.getSendResult()) {
            IMSendResult<Object> result = new IMSendResult<>();
            result.setReceiver(recvInfo.getReceivers().get(0));
            result.setCode(sendCode.code());
            result.setData(recvInfo.getData());
            // 推送到结果队列
            String key = StrUtil.join(":", IMRedisKey.IM_RESULT_SYSTEM_QUEUE,recvInfo.getServiceName());
            redisTemplate.opsForList().rightPush(key, result);
            redisTemplate.convertAndSend(IMConstant.SYSTEM_MSG_SEND_RESULT_TOPIC, key);
        }
    }
}

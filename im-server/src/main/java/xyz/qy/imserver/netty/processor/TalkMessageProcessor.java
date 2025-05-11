package xyz.qy.imserver.netty.processor;

import io.netty.channel.ChannelHandlerContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import xyz.qy.imcommon.enums.IMCmdType;
import xyz.qy.imcommon.model.IMRecvInfo;
import xyz.qy.imcommon.model.IMSendInfo;
import xyz.qy.imcommon.model.IMUserInfo;
import xyz.qy.imserver.netty.UserChannelCtxMap;

import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class TalkMessageProcessor extends AbstractMessageProcessor<IMRecvInfo> {

    @Override
    public void process(IMRecvInfo recvInfo) {
        log.info("接收到动态消息,接收用户数量:{}，内容:{}",  recvInfo.getReceivers().size(), recvInfo.getData());
        for (IMUserInfo receiver : recvInfo.getReceivers()) {
            try {
                ChannelHandlerContext channelCtx =
                        UserChannelCtxMap.getChannelCtx(receiver.getId(), receiver.getTerminal());
                if (!Objects.isNull(channelCtx)) {
                    // 推送消息到用户
                    IMSendInfo<Object> sendInfo = new IMSendInfo<>();
                    sendInfo.setCmd(IMCmdType.TALK_MESSAGE.code());
                    sendInfo.setData(recvInfo.getData());
                    channelCtx.channel().writeAndFlush(sendInfo);
                } else {
                    // 消息推送失败确认
                    log.error("未找到channel，接收者:{}，内容:{}", receiver.getId(), recvInfo.getData());
                }
            } catch (Exception e) {
                // 消息推送失败确认
                log.error("发送异常，,接收者:{}，内容:{}", receiver.getId(), recvInfo.getData(), e);
            }
        }
    }
}

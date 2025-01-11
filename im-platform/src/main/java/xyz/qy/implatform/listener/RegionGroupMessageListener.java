package xyz.qy.implatform.listener;

import lombok.extern.slf4j.Slf4j;
import xyz.qy.imclient.annotation.IMListener;
import xyz.qy.imclient.listener.MessageListener;
import xyz.qy.imcommon.enums.IMListenerType;
import xyz.qy.imcommon.enums.IMSendCode;
import xyz.qy.imcommon.model.IMSendResult;
import xyz.qy.implatform.vo.RegionGroupMessageVO;

import java.util.List;

@Slf4j
@IMListener(type = IMListenerType.REGION_GROUP_MESSAGE)
public class RegionGroupMessageListener implements MessageListener<RegionGroupMessageVO> {

    @Override
    public void process(List<IMSendResult<RegionGroupMessageVO>> results) {
        for (IMSendResult<RegionGroupMessageVO> result : results) {
            RegionGroupMessageVO messageInfo = result.getData();
            if (result.getCode().equals(IMSendCode.SUCCESS.code())) {
                log.info("地区群聊消息送达，消息id:{}，发送者:{},接收者:{},终端:{}", messageInfo.getId(), result.getSender().getId(), result.getReceiver().getId(), result.getReceiver().getTerminal());
            }
        }
    }
}

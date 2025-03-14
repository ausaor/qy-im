package xyz.qy.implatform.listener;

import lombok.extern.slf4j.Slf4j;
import xyz.qy.imclient.annotation.IMListener;
import xyz.qy.imclient.listener.MessageListener;
import xyz.qy.imcommon.enums.IMListenerType;
import xyz.qy.imcommon.enums.IMSendCode;
import xyz.qy.imcommon.model.IMSendResult;
import xyz.qy.implatform.vo.SystemMessageVO;

import java.util.List;

@Slf4j
@IMListener(type = IMListenerType.SYSTEM_MESSAGE)
public class SystemMessageListener implements MessageListener<SystemMessageVO> {

    @Override
    public void process(List<IMSendResult<SystemMessageVO>> results) {
        for(IMSendResult<SystemMessageVO> result : results){
            SystemMessageVO messageInfo = result.getData();
            if (result.getCode().equals(IMSendCode.SUCCESS.code())) {
                log.info("消息送达，消息id:{},接收者:{},终端:{}", messageInfo.getId(), result.getReceiver().getId(), result.getReceiver().getTerminal());
            }
        }
    }
}

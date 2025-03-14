package xyz.qy.implatform.listener;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import xyz.qy.imclient.annotation.IMListener;
import xyz.qy.imclient.listener.MessageListener;
import xyz.qy.imcommon.enums.IMListenerType;
import xyz.qy.imcommon.enums.IMSendCode;
import xyz.qy.imcommon.enums.IMTerminalType;
import xyz.qy.imcommon.model.IMSendResult;
import xyz.qy.implatform.entity.PrivateMessage;
import xyz.qy.implatform.enums.MessageStatus;
import xyz.qy.implatform.service.IPrivateMessageService;
import xyz.qy.implatform.vo.PrivateMessageVO;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@IMListener(type = IMListenerType.PRIVATE_MESSAGE)
public class PrivateMessageListener implements MessageListener<PrivateMessageVO> {
    @Lazy
    @Autowired
    private IPrivateMessageService privateMessageService;

    @Override
    public void process(List<IMSendResult<PrivateMessageVO>> results) {
        Set<Long> messageIds = new HashSet<>();
        for (IMSendResult<PrivateMessageVO> result : results) {
            PrivateMessageVO messageInfo = result.getData();
            // 更新消息状态,这里只处理成功消息，失败的消息继续保持未读状态
            if (result.getCode().equals(IMSendCode.SUCCESS.code())) {
                messageIds.add(messageInfo.getId());
                log.info("消息送达，消息id:{}，发送者:{},接收者:{},终端:{}", messageInfo.getId(), result.getSender().getId(), result.getReceiver().getId(), result.getReceiver().getTerminal());
            }
        }
        // 批量修改状态
        if (CollUtil.isNotEmpty(messageIds)) {
            UpdateWrapper<PrivateMessage> updateWrapper = new UpdateWrapper<>();
            updateWrapper.lambda().in(PrivateMessage::getId, messageIds)
                    .eq(PrivateMessage::getStatus, MessageStatus.UNSEND.code())
                    .set(PrivateMessage::getStatus, MessageStatus.SENDED.code());
            privateMessageService.update(updateWrapper);
        }
    }
}

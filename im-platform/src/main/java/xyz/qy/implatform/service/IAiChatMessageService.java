package xyz.qy.implatform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import xyz.qy.implatform.entity.AiChatMessage;
import xyz.qy.implatform.vo.AiChatMessageVO;

import java.util.List;

public interface IAiChatMessageService extends IService<AiChatMessage> {
    List<AiChatMessageVO> listMessages(Long sessionId);
}

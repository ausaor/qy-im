package xyz.qy.implatform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import xyz.qy.implatform.entity.AiChatSession;
import xyz.qy.implatform.vo.AiChatSessionVO;

import java.util.List;

public interface IAiChatSessionService extends IService<AiChatSession> {
    List<AiChatSessionVO> listSessions();
}

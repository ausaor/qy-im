package xyz.qy.implatform.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import xyz.qy.implatform.entity.AiChatMessage;
import xyz.qy.implatform.entity.AiChatSession;
import xyz.qy.implatform.mapper.AiChatSessionMapper;
import xyz.qy.implatform.service.IAiChatMessageService;
import xyz.qy.implatform.service.IAiChatSessionService;
import xyz.qy.implatform.session.SessionContext;
import xyz.qy.implatform.session.UserSession;
import xyz.qy.implatform.util.BeanUtils;
import xyz.qy.implatform.vo.AiChatMessageVO;
import xyz.qy.implatform.vo.AiChatSessionVO;

import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
@Service
public class AiChatSessionServiceImpl extends ServiceImpl<AiChatSessionMapper, AiChatSession> implements IAiChatSessionService {

    private final IAiChatMessageService aiChatMessageService;

    @Override
    public List<AiChatSessionVO> listSessions() {
        UserSession session = SessionContext.getSession();
        LambdaQueryWrapper<AiChatSession> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiChatSession::getUserId, session.getUserId());
        wrapper.eq(AiChatSession::getDeleted, false);
        wrapper.orderByDesc(AiChatSession::getCreateTime);

        List<AiChatSession> list = list(wrapper);
        if (CollectionUtil.isEmpty(list)) {
            return Collections.emptyList();
        }
        List<AiChatSessionVO> sessionVos = BeanUtils.copyPropertiesList(list, AiChatSessionVO.class);
        Long sessionId = sessionVos.get(0).getId();

        List<AiChatMessage> chatMessages = aiChatMessageService.lambdaQuery()
                .eq(AiChatMessage::getSessionId, sessionId)
                .eq(AiChatMessage::getUserId, session.getUserId())
                .list();
        if (CollectionUtil.isNotEmpty(chatMessages)) {
            sessionVos.get(0).setMessages(BeanUtils.copyPropertiesList(chatMessages, AiChatMessageVO.class));
        }

        return sessionVos;
    }
}

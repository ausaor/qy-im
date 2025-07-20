package xyz.qy.implatform.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import xyz.qy.implatform.entity.AiChatMessage;
import xyz.qy.implatform.mapper.AiChatMessageMapper;
import xyz.qy.implatform.service.IAiChatMessageService;
import xyz.qy.implatform.session.SessionContext;
import xyz.qy.implatform.session.UserSession;
import xyz.qy.implatform.util.BeanUtils;
import xyz.qy.implatform.vo.AiChatMessageVO;

import java.util.Collections;
import java.util.List;

@Service
public class AiChatMessageServiceImpl extends ServiceImpl<AiChatMessageMapper, AiChatMessage> implements IAiChatMessageService {
    @Override
    public List<AiChatMessageVO> listMessages(Long sessionId) {
        UserSession session = SessionContext.getSession();
        LambdaQueryWrapper<AiChatMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiChatMessage::getSessionId, sessionId);
        wrapper.eq(AiChatMessage::getUserId, session.getUserId());
        wrapper.orderByAsc(AiChatMessage::getCreateTime);

        List<AiChatMessage> messages = this.list(wrapper);
        if (CollectionUtil.isEmpty(messages)) {
            return Collections.emptyList();
        }

        return BeanUtils.copyPropertiesList(messages, AiChatMessageVO.class);
    }
}

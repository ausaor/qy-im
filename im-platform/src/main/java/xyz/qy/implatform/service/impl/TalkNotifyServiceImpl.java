package xyz.qy.implatform.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import xyz.qy.implatform.entity.TalkNotify;
import xyz.qy.implatform.mapper.TalkNotifyMapper;
import xyz.qy.implatform.service.ITalkNotifyService;
import xyz.qy.implatform.session.SessionContext;
import xyz.qy.implatform.session.UserSession;

import java.time.LocalDateTime;

@Slf4j
@Service
public class TalkNotifyServiceImpl extends ServiceImpl<TalkNotifyMapper, TalkNotify> implements ITalkNotifyService {
    @Override
    public void readedTalkNotify(String category) {
        UserSession session = SessionContext.getSession();
        Long userId = session.getUserId();

        LambdaUpdateWrapper<TalkNotify> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(TalkNotify::getUserId, userId);
        updateWrapper.eq(TalkNotify::getCategory, category);
        updateWrapper.set(TalkNotify::getIsRead, true);
        updateWrapper.set(TalkNotify::getUpdateTime, LocalDateTime.now());

        this.update(updateWrapper);
    }
}

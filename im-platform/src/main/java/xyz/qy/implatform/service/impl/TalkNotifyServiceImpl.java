package xyz.qy.implatform.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import xyz.qy.implatform.entity.TalkNotify;
import xyz.qy.implatform.mapper.TalkNotifyMapper;
import xyz.qy.implatform.service.ITalkNotifyService;

@Service
public class TalkNotifyServiceImpl extends ServiceImpl<TalkNotifyMapper, TalkNotify> implements ITalkNotifyService {
}

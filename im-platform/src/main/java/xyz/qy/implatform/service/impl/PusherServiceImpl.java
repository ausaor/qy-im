package xyz.qy.implatform.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import xyz.qy.implatform.entity.Pusher;
import xyz.qy.implatform.mapper.PusherMapper;
import xyz.qy.implatform.service.IPusherService;

@Service
public class PusherServiceImpl extends ServiceImpl<PusherMapper, Pusher> implements IPusherService {
}

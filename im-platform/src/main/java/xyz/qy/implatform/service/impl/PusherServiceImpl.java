package xyz.qy.implatform.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import xyz.qy.implatform.contant.Constant;
import xyz.qy.implatform.entity.Pusher;
import xyz.qy.implatform.exception.GlobalException;
import xyz.qy.implatform.mapper.PusherMapper;
import xyz.qy.implatform.service.IPusherService;
import xyz.qy.implatform.session.SessionContext;

import java.util.List;

@Service
public class PusherServiceImpl extends ServiceImpl<PusherMapper, Pusher> implements IPusherService {
    @Override
    public List<Pusher> listPusher() {
        Long userId = SessionContext.getSession().getUserId();
        if (!userId.equals(Constant.ADMIN_USER_ID)) {
            throw new GlobalException("不是系统管理员，无权限");
        }
        return this.lambdaQuery().eq(Pusher::getDeleted, false).list();
    }
}

package xyz.qy.implatform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import xyz.qy.implatform.entity.Pusher;

import java.util.List;

public interface IPusherService extends IService<Pusher> {
    /**
     * 查询推送主体列表
     *
     * @return 推送主体列表
     */
    List<Pusher> listPusher();
}

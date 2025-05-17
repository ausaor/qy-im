package xyz.qy.implatform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import xyz.qy.implatform.entity.TalkNotify;

public interface ITalkNotifyService extends IService<TalkNotify> {

    void readedTalkNotify(String category);
}

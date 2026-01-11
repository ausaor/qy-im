package xyz.qy.implatform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import xyz.qy.implatform.dto.TalkNotifyQueryDTO;
import xyz.qy.implatform.dto.TalkNotifyUpdateDTO;
import xyz.qy.implatform.entity.TalkNotify;
import xyz.qy.implatform.vo.PageResultVO;

public interface ITalkNotifyService extends IService<TalkNotify> {

    void readedTalkNotify(TalkNotifyUpdateDTO dto);

    void readedAllTalkNotify(TalkNotifyUpdateDTO dto);

    PageResultVO pageQueryTalkNotify(TalkNotifyQueryDTO dto);

    void pullOfflineNotify();
}

package xyz.qy.implatform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import xyz.qy.implatform.dto.ShortVideoNotifyQueryDTO;
import xyz.qy.implatform.entity.ShortVideoNotify;
import xyz.qy.implatform.vo.PageResultVO;
import xyz.qy.implatform.vo.ShortVideoNotifyVO;

import java.util.List;

public interface IShortVideoNotifyService extends IService<ShortVideoNotify> {
    void readedShortVideoNotify(Long targetId, String targetType);

    void readedAllShortVideoNotify();

    PageResultVO<List<ShortVideoNotifyVO>> pageShortVideoNotify(ShortVideoNotifyQueryDTO dto);
}

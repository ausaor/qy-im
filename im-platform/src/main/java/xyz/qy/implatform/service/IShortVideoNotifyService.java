package xyz.qy.implatform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import xyz.qy.implatform.dto.ShortVideoNotifyAddDTO;
import xyz.qy.implatform.dto.ShortVideoNotifyQueryDTO;
import xyz.qy.implatform.dto.ShortVideoNotifyUpdateDTO;
import xyz.qy.implatform.entity.ShortVideoNotify;
import xyz.qy.implatform.vo.PageResultVO;
import xyz.qy.implatform.vo.ShortVideoNotifyVO;

import java.util.List;

public interface IShortVideoNotifyService extends IService<ShortVideoNotify> {
    ShortVideoNotifyVO getShortVideoNotify(Long id);

    List<ShortVideoNotifyVO> listShortVideoNotify(ShortVideoNotifyQueryDTO dto);

    PageResultVO<List<ShortVideoNotifyVO>> pageShortVideoNotify(ShortVideoNotifyQueryDTO dto);

    ShortVideoNotifyVO addShortVideoNotify(ShortVideoNotifyAddDTO dto);

    ShortVideoNotifyVO updateShortVideoNotify(ShortVideoNotifyUpdateDTO dto);

    void deleteShortVideoNotify(Long id);

    void batchDeleteShortVideoNotify(List<Long> ids);
}

package xyz.qy.implatform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import xyz.qy.implatform.dto.ShortVideoLikeAddDTO;
import xyz.qy.implatform.dto.ShortVideoLikeDelDTO;
import xyz.qy.implatform.dto.ShortVideoLikeQueryDTO;
import xyz.qy.implatform.entity.ShortVideoLike;
import xyz.qy.implatform.vo.PageResultVO;
import xyz.qy.implatform.vo.ShortVideoLikeVO;

import java.util.List;

public interface IShortVideoLikeService extends IService<ShortVideoLike> {
    List<ShortVideoLikeVO> listShortVideoLikes(ShortVideoLikeQueryDTO dto);

    PageResultVO<List<ShortVideoLikeVO>> pageShortVideoLikes(ShortVideoLikeQueryDTO dto);

    ShortVideoLikeVO addShortVideoLike(ShortVideoLikeAddDTO dto);

    void deleteShortVideoLike(ShortVideoLikeDelDTO dto);

    void deleteShortVideoLikes(List<Long> videoIds);

    PageResultVO<List<ShortVideoLikeVO>> pageShortVideoLikeUser(ShortVideoLikeQueryDTO dto);
}

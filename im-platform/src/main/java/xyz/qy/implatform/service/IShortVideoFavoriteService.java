package xyz.qy.implatform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import xyz.qy.implatform.dto.ShortVideoFavoriteAddDTO;
import xyz.qy.implatform.dto.ShortVideoFavoriteDelDTO;
import xyz.qy.implatform.dto.ShortVideoFavoriteQueryDTO;
import xyz.qy.implatform.dto.ShortVideoFavoriteUpdateDTO;
import xyz.qy.implatform.entity.ShortVideoFavorite;
import xyz.qy.implatform.vo.PageResultVO;
import xyz.qy.implatform.vo.ShortVideoFavoriteVO;

import java.util.List;

public interface IShortVideoFavoriteService extends IService<ShortVideoFavorite> {
    List<ShortVideoFavoriteVO> listShortVideoFavorites(ShortVideoFavoriteQueryDTO dto);

    PageResultVO<List<ShortVideoFavoriteVO>> pageShortVideoFavorites(ShortVideoFavoriteQueryDTO dto);

    ShortVideoFavoriteVO getShortVideoFavoriteById(Long id);

    ShortVideoFavoriteVO addShortVideoFavorite(ShortVideoFavoriteAddDTO dto);

    void deleteShortVideoFavorite(ShortVideoFavoriteDelDTO dto);

    void deleteShortVideoFavorites(List<Long> videoIds);

    PageResultVO<List<ShortVideoFavoriteVO>> pageShortVideoFavoritesUser(ShortVideoFavoriteQueryDTO dto);
}

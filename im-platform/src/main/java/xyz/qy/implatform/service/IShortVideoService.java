package xyz.qy.implatform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import xyz.qy.implatform.dto.ShortVideoAddDTO;
import xyz.qy.implatform.dto.ShortVideoBatchDelDTO;
import xyz.qy.implatform.dto.ShortVideoBatchScopeDTO;
import xyz.qy.implatform.dto.ShortVideoDelDTO;
import xyz.qy.implatform.dto.ShortVideoQueryDTO;
import xyz.qy.implatform.dto.ShortVideoReviewDTO;
import xyz.qy.implatform.dto.ShortVideoUpdateDTO;
import xyz.qy.implatform.entity.ShortVideo;
import xyz.qy.implatform.vo.PageResultVO;
import xyz.qy.implatform.vo.ShortVideoVO;

import java.util.List;

public interface IShortVideoService extends IService<ShortVideo> {
    List<ShortVideoVO> listShortVideos(ShortVideoQueryDTO dto);

    List<ShortVideoVO> myShortVideos(ShortVideoQueryDTO dto);

    List<ShortVideoVO> myLikedShortVideos();

    List<ShortVideoVO> myFavoriteShortVideos();

    PageResultVO<List<ShortVideoVO>> pageShortVideos(ShortVideoQueryDTO dto);

    PageResultVO<List<ShortVideoVO>> getRecommendShortVideos(ShortVideoQueryDTO dto);

    ShortVideoVO getShortVideoById(Long id);

    ShortVideoVO addShortVideo(ShortVideoAddDTO dto);

    ShortVideoVO updateShortVideo(ShortVideoUpdateDTO dto);

    void deleteShortVideo(ShortVideoDelDTO dto);

    void batchDeleteShortVideo(ShortVideoBatchDelDTO dto);

    void batchUpdateScope(ShortVideoBatchScopeDTO dto);

    void addPlayCount(Long videoId);

    void verifyUserDataAuth(ShortVideo shortVideo);

    void review(ShortVideoReviewDTO dto);
}

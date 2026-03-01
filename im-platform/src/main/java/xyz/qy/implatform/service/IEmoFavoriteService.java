package xyz.qy.implatform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import xyz.qy.implatform.dto.EmoFavoriteDTO;
import xyz.qy.implatform.entity.EmoFavorite;
import xyz.qy.implatform.vo.EmoFavoriteVO;

import java.util.List;

public interface IEmoFavoriteService extends IService<EmoFavorite> {
    EmoFavoriteVO addEmoFavorite(EmoFavoriteDTO dto);

    void deleteEmoFavorite(String id);

    List<EmoFavoriteVO> listEmoFavorite();
}
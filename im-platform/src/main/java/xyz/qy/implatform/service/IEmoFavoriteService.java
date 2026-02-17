package xyz.qy.implatform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import xyz.qy.implatform.dto.EmoFavoriteDTO;
import xyz.qy.implatform.entity.EmoFavorite;

public interface IEmoFavoriteService extends IService<EmoFavorite> {
    void addEmoFavorite(EmoFavoriteDTO dto);

    void deleteEmoFavorite(Long id);
}
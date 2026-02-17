package xyz.qy.implatform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import xyz.qy.implatform.dto.EmoAlbumDTO;
import xyz.qy.implatform.entity.EmoAlbum;

public interface IEmoAlbumService extends IService<EmoAlbum> {
    void addEmoAlbum(EmoAlbumDTO dto);

    void updateEmoAlbum(EmoAlbumDTO dto);

    void deleteEmoAlbum(Long id);
}

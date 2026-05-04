package xyz.qy.implatform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import xyz.qy.implatform.dto.EmoAlbumDTO;
import xyz.qy.implatform.entity.EmoAlbum;
import xyz.qy.implatform.vo.EmoAlbumVO;

import java.util.List;

public interface IEmoAlbumService extends IService<EmoAlbum> {
    void addEmoAlbum(EmoAlbumDTO dto);

    void updateEmoAlbum(EmoAlbumDTO dto);

    void deleteEmoAlbum(Long id);

    List<EmoAlbumVO> getEmoAlbumList();
}

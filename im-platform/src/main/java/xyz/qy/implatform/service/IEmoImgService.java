package xyz.qy.implatform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import xyz.qy.implatform.entity.EmoImg;
import xyz.qy.implatform.vo.EmoAlbumVO;
import xyz.qy.implatform.vo.EmoImgVO;

import java.util.List;

public interface IEmoImgService extends IService<EmoImg> {
    List<EmoAlbumVO> getEmoAlbumImgList();

    List<EmoImgVO> search(String name);
}
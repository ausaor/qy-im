package xyz.qy.implatform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import xyz.qy.implatform.dto.MusicStarDTO;
import xyz.qy.implatform.entity.MusicStar;

public interface IMusicStarService extends IService<MusicStar> {
    void like(MusicStarDTO dto);

    void cancelLike(MusicStarDTO dto);
}

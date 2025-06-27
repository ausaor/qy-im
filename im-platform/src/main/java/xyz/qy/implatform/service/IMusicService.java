package xyz.qy.implatform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import xyz.qy.implatform.dto.MusicAddDTO;
import xyz.qy.implatform.dto.MusicQueryDTO;
import xyz.qy.implatform.entity.Music;
import xyz.qy.implatform.vo.MusicVO;

import java.util.List;

public interface IMusicService extends IService<Music> {
    List<MusicVO> listMusic(MusicQueryDTO dto);

    MusicVO addMusic(MusicAddDTO dto);
}

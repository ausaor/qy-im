package xyz.qy.implatform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import xyz.qy.implatform.dto.MusicAddDTO;
import xyz.qy.implatform.dto.MusicDelDTO;
import xyz.qy.implatform.dto.MusicQueryDTO;
import xyz.qy.implatform.dto.MusicUpdateDTO;
import xyz.qy.implatform.entity.Music;
import xyz.qy.implatform.vo.MusicVO;
import xyz.qy.implatform.vo.PageResultVO;

import java.util.List;

public interface IMusicService extends IService<Music> {
    List<MusicVO> listMusic(MusicQueryDTO dto);

    MusicVO addMusic(MusicAddDTO dto);

    MusicVO updateMusic(MusicUpdateDTO dto);

    void deleteMusic(MusicDelDTO dto);

    void increasePlayCount(Long id);

    PageResultVO listMusicPage(MusicQueryDTO dto);

    MusicVO getMusicById(Long id);
}

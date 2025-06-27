package xyz.qy.implatform.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import xyz.qy.implatform.entity.MusicStar;
import xyz.qy.implatform.mapper.MusicStarMapper;
import xyz.qy.implatform.service.IMusicStarService;

@Slf4j
@Service
public class MusicStarServiceImpl extends ServiceImpl<MusicStarMapper, MusicStar> implements IMusicStarService {
    @Override
    public void likeMusic(Long musicId) {

    }
}

package xyz.qy.implatform.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xyz.qy.implatform.dto.MusicStarDTO;
import xyz.qy.implatform.dto.UserDataAuthDTO;
import xyz.qy.implatform.entity.Music;
import xyz.qy.implatform.entity.MusicStar;
import xyz.qy.implatform.entity.TalkStar;
import xyz.qy.implatform.exception.GlobalException;
import xyz.qy.implatform.mapper.MusicStarMapper;
import xyz.qy.implatform.service.IMusicService;
import xyz.qy.implatform.service.IMusicStarService;
import xyz.qy.implatform.service.ITalkService;
import xyz.qy.implatform.session.SessionContext;
import xyz.qy.implatform.session.UserSession;

import javax.annotation.Resource;
import java.time.LocalDateTime;

@Slf4j
@Service
public class MusicStarServiceImpl extends ServiceImpl<MusicStarMapper, MusicStar> implements IMusicStarService {
    @Resource
    private IMusicService musicService;

    @Resource
    private ITalkService talkService;

    @Transactional
    @Override
    public void like(MusicStarDTO dto) {
        UserSession session = SessionContext.getSession();
        Long userId = session.getUserId();
        Music music = musicService.getById(dto.getMusicId());
        if (ObjectUtil.isNull(music) || music.getDeleted()) {
            throw new GlobalException("点赞歌曲不存在");
        }

        UserDataAuthDTO userDataAuthDTO = UserDataAuthDTO.builder()
                .category(music.getCategory())
                .userId(userId)
                .friendId(music.getUserId())
                .groupId(music.getGroupId())
                .regionCode(music.getRegionCode())
                .build();
        if (!talkService.verifyUserDataAuth(userDataAuthDTO)) {
            throw new GlobalException("您无权限操作");
        }

        LambdaQueryWrapper<MusicStar> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(MusicStar::getMusicId, music.getId())
                .eq(MusicStar::getUserId, userId)
                .eq(MusicStar::getDeleted, false);
        MusicStar one = this.getOne(wrapper);
        if (ObjectUtil.isNotNull(one)) {
            throw new GlobalException("您已点赞过此歌曲");
        }

        MusicStar musicStar = new MusicStar();
        musicStar.setUserId(userId);
        musicStar.setMusicId(dto.getMusicId());
        musicStar.setCreateTime(LocalDateTime.now());

        this.save(musicStar);
    }

    @Override
    public void cancelLike(MusicStarDTO dto) {
        UserSession session = SessionContext.getSession();
        Long userId = session.getUserId();

        LambdaQueryWrapper<MusicStar> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MusicStar::getUserId, userId);
        wrapper.eq(MusicStar::getMusicId, dto.getMusicId());
        wrapper.eq(MusicStar::getDeleted, false);

        MusicStar musicStar = this.getOne(wrapper);
        if (ObjectUtil.isNull(musicStar)) {
            throw new GlobalException("点赞记录不存在");
        }

        this.remove(wrapper);
    }
}

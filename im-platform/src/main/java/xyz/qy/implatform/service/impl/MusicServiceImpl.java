package xyz.qy.implatform.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import xyz.qy.implatform.dto.MusicAddDTO;
import xyz.qy.implatform.dto.MusicDelDTO;
import xyz.qy.implatform.dto.MusicQueryDTO;
import xyz.qy.implatform.entity.Group;
import xyz.qy.implatform.entity.Music;
import xyz.qy.implatform.entity.MusicStar;
import xyz.qy.implatform.enums.TalkCategoryEnum;
import xyz.qy.implatform.exception.GlobalException;
import xyz.qy.implatform.mapper.MusicMapper;
import xyz.qy.implatform.service.IFriendService;
import xyz.qy.implatform.service.IGroupMemberService;
import xyz.qy.implatform.service.IGroupService;
import xyz.qy.implatform.service.IMusicService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import xyz.qy.implatform.service.IMusicStarService;
import xyz.qy.implatform.service.IRegionGroupMemberService;
import xyz.qy.implatform.session.SessionContext;
import xyz.qy.implatform.util.BeanUtils;
import xyz.qy.implatform.vo.FriendVO;
import xyz.qy.implatform.vo.MusicVO;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class MusicServiceImpl extends ServiceImpl<MusicMapper, Music> implements IMusicService {
    @Resource
    private IGroupMemberService groupMemberService;

    @Resource
    private IGroupService groupService;

    @Resource
    private IRegionGroupMemberService regionGroupMemberService;

    @Resource
    private IMusicStarService musicStarService;

    @Resource
    private IFriendService friendService;

    @Override
    public List<MusicVO> listMusic(MusicQueryDTO dto) {
        long userId = SessionContext.getSession().getUserId();

        List<Music> musics = null;
        if (StringUtils.equalsAny(dto.getCategory(), TalkCategoryEnum.GROUP.getCode(), TalkCategoryEnum.REGION.getCode())) {
            LambdaQueryWrapper<Music> wrapper = Wrappers.lambdaQuery();
            wrapper.eq(Music::getDeleted, false)
                    .eq(Music::getCategory, dto.getCategory())
                    .eq(StringUtils.isNotBlank(dto.getRegionCode()), Music::getRegionCode, dto.getRegionCode())
                    .eq(ObjectUtil.isNotNull(dto.getGroupId()), Music::getGroupId, dto.getGroupId())
                    .orderByDesc(Music::getCreateTime)
                    .last("limit 100");
            musics = this.list(wrapper);
        } else if (StringUtils.equals(dto.getCategory(), TalkCategoryEnum.PRIVATE.getCode())) {
            Long queryUserId = null;
            if ("friend".equals(dto.getSection())) {
                if (ObjectUtil.isNull(dto.getFriendId())) {
                    throw new GlobalException("好友Id不能为空");
                }
                // 判断是否好友
                FriendVO friendVO = friendService.findFriend(dto.getFriendId());
                queryUserId = friendVO.getId();
            } else if ("my".equals(dto.getSection())) {
                queryUserId = userId;
            }

            LambdaQueryWrapper<Music> wrapper = Wrappers.lambdaQuery();
            wrapper.eq(Music::getDeleted, false)
                    .eq(Music::getCategory, dto.getCategory())
                    .eq(Music::getUserId, queryUserId)
                    .orderByDesc(Music::getCreateTime)
                    .last("limit 100");
            musics = this.list(wrapper);
        }
        
        if (CollectionUtils.isEmpty(musics)) {
            return Collections.emptyList();
        }

        List<MusicVO> musicVos = BeanUtils.copyPropertiesList(musics, MusicVO.class);

        List<Long> musicIds = musics.stream().map(Music::getId).collect(Collectors.toList());
        List<MusicStar> musicStarList = musicStarService.lambdaQuery()
                .in(MusicStar::getMusicId, musicIds)
                .eq(MusicStar::getDeleted, false).list();

        // musicStarList根据musicId进行分组
        Map<Long, List<MusicStar>> musicStarMap = musicStarList.stream().collect(Collectors.groupingBy(MusicStar::getMusicId));
        musicVos.forEach(musicVO -> {
            List<MusicStar> musicStars = musicStarMap.getOrDefault(musicVO.getId(), new ArrayList<>());
            musicVO.setLikeCount(musicStars.size());
            musicVO.setLiked(musicStars.stream().anyMatch(musicStar -> musicStar.getUserId().equals(userId)));
            musicVO.setIsOwner(musicVO.getUserId().equals(userId));
        });

        return musicVos;
    }

    @Override
    public MusicVO addMusic(MusicAddDTO dto) {
        long userId = SessionContext.getSession().getUserId();

        // 校验数据
        checkMusicData(dto);

        Music music = BeanUtils.copyProperties(dto, Music.class);
        music.setUserId(userId);
        music.setCreateTime(LocalDateTime.now());
        this.save(music);
        MusicVO musicVO = BeanUtils.copyProperties(music, MusicVO.class);
        musicVO.setIsOwner(Boolean.TRUE);
        return musicVO;
    }

    @Override
    public void deleteMusic(MusicDelDTO dto) {
        Long userId = SessionContext.getSession().getUserId();
        Music music = this.getById(dto.getId());
        if (ObjectUtil.isNull(music)) {
            throw new GlobalException("歌曲不存在");
        }

        if (!music.getUserId().equals(userId)) {
            throw new GlobalException("无权限删除");
        }
        music.setDeleted(true);
        music.setUpdateTime(LocalDateTime.now());
        boolean update = this.updateById(music);
        if (update) {
            log.info("成功删除音乐,musicId={}：", music.getId());
        } else {
            log.info("删除音乐失败,musicId={}：", music.getId());
        }
    }

    private void checkMusicData(MusicAddDTO dto) {
        long userId = SessionContext.getSession().getUserId();

        if (StringUtils.equalsAny(dto.getCategory(), TalkCategoryEnum.GROUP.getCode(), TalkCategoryEnum.REGION.getCode())) {
            // 查询是否在一个月内上传数量超过100
            if (StringUtils.equals(dto.getCategory(), TalkCategoryEnum.GROUP.getCode()) && ObjectUtil.isNull(dto.getGroupId())) {
                throw new GlobalException("群聊id为空");
            }
            if (StringUtils.equals(dto.getCategory(), TalkCategoryEnum.REGION.getCode()) && StringUtils.isBlank(dto.getRegionCode())) {
                throw new GlobalException("区域编码为空");
            }

            // 判断用户是否群成员
            if (TalkCategoryEnum.GROUP.getCode().equals(dto.getCategory())) {
                Group group = groupService.getAndCheckById(dto.getGroupId());
                if (ObjectUtil.isNull(group)) {
                    throw new GlobalException("群不存在");
                }

                if (!groupMemberService.isInGroup(dto.getGroupId(), Collections.singletonList(userId))) {
                    throw new GlobalException("您不是群聊成员");
                }
            } else if (TalkCategoryEnum.REGION.getCode().equals(dto.getCategory())) {
                if (!regionGroupMemberService.isInRegionGroup(dto.getRegionCode(), Collections.singletonList(userId))) {
                    throw new GlobalException("您不是地区群聊常驻成员");
                }
            }

            LambdaQueryWrapper<Music> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Music::getCategory, dto.getCategory())
                    .eq(ObjectUtil.isNotNull(dto.getGroupId()), Music::getGroupId, dto.getGroupId())
                    .eq(StringUtils.isNotBlank(dto.getRegionCode()), Music::getRegionCode, dto.getRegionCode())
                    .ge(Music::getCreateTime, LocalDateTime.now().minusDays(30));
            List<Music> musics = this.list(wrapper);
            if (musics.size() >= 100) {
                throw new GlobalException("一个月内整体上传数量超过100，无法继续上传");
            }

            //判断用户上传是否超过20
            long count = musics.stream().filter(m -> m.getUserId().equals(userId)).count();
            if (count >= 10) {
                throw new GlobalException("您在一个月内上传数量超过20，无法继续上传");
            }
        } else if (StringUtils.equals(dto.getCategory(), TalkCategoryEnum.PRIVATE.getCode())) {
            // 判断用户一个月内上传数量是否超过100
            LambdaQueryWrapper<Music> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Music::getCategory, dto.getCategory())
                    .eq(Music::getUserId, userId)
                    .ge(Music::getCreateTime, LocalDateTime.now().minusDays(30));
            List<Music> musics = this.list(wrapper);
            if (musics.size() >= 100) {
                throw new GlobalException("您在一个月内上传数量超过100，无法继续上传");
            }
        }
    }
}

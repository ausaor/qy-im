package xyz.qy.implatform.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xyz.qy.implatform.dto.FollowDTO;
import xyz.qy.implatform.dto.ShortVideoAddDTO;
import xyz.qy.implatform.dto.ShortVideoBatchDelDTO;
import xyz.qy.implatform.dto.ShortVideoBatchScopeDTO;
import xyz.qy.implatform.dto.ShortVideoDelDTO;
import xyz.qy.implatform.dto.ShortVideoQueryDTO;
import xyz.qy.implatform.dto.ShortVideoUpdateDTO;
import xyz.qy.implatform.entity.Group;
import xyz.qy.implatform.entity.ShortVideo;
import xyz.qy.implatform.entity.ShortVideoFavorite;
import xyz.qy.implatform.entity.ShortVideoLike;
import xyz.qy.implatform.entity.TemplateCharacter;
import xyz.qy.implatform.entity.TemplateGroup;
import xyz.qy.implatform.entity.User;
import xyz.qy.implatform.enums.FollowEnum;
import xyz.qy.implatform.enums.ReviewEnum;
import xyz.qy.implatform.enums.SectionEnum;
import xyz.qy.implatform.enums.ViewScopeEnum;
import xyz.qy.implatform.exception.GlobalException;
import xyz.qy.implatform.mapper.ShortVideoFavoriteMapper;
import xyz.qy.implatform.mapper.ShortVideoLikeMapper;
import xyz.qy.implatform.mapper.ShortVideoMapper;
import xyz.qy.implatform.service.IFollowService;
import xyz.qy.implatform.service.IFriendService;
import xyz.qy.implatform.service.IGroupService;
import xyz.qy.implatform.service.IShortVideoService;
import xyz.qy.implatform.service.ITemplateCharacterService;
import xyz.qy.implatform.service.ITemplateGroupService;
import xyz.qy.implatform.service.IUserService;
import xyz.qy.implatform.session.SessionContext;
import xyz.qy.implatform.session.UserSession;
import xyz.qy.implatform.util.BeanUtils;
import xyz.qy.implatform.util.PageUtils;
import xyz.qy.implatform.contant.RedisKey;
import xyz.qy.implatform.util.RedisCache;
import xyz.qy.implatform.vo.FollowVO;
import xyz.qy.implatform.vo.PageResultVO;
import xyz.qy.implatform.vo.ShortVideoVO;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class ShortVideoServiceImpl extends ServiceImpl<ShortVideoMapper, ShortVideo> implements IShortVideoService {
    @Resource
    private ShortVideoLikeMapper shortVideoLikeMapper;

    @Resource
    private ShortVideoFavoriteMapper shortVideoFavoriteMapper;

    @Resource
    private IUserService userService;

    @Resource
    private RedisCache redisCache;

    @Resource
    private IFriendService friendService;

    @Resource
    private IFollowService followService;

    @Resource
    private ITemplateGroupService templateGroupService;

    @Resource
    private ITemplateCharacterService templateCharacterService;

    @Resource
    private IGroupService  groupService;

    @Override
    public List<ShortVideoVO> myShortVideos(ShortVideoQueryDTO dto) {
        UserSession session = SessionContext.getSession();
        dto.setUserId(session.getUserId());
        List<ShortVideo> shortVideos = this.list(buildQueryWrapper(dto));
        List<ShortVideoVO> shortVideoVOS = BeanUtils.copyPropertiesList(shortVideos, ShortVideoVO.class);
        fillOwnerFlag(shortVideoVOS);
        return shortVideoVOS;
    }

    @Override
    public List<ShortVideoVO> myLikedShortVideos() {
        UserSession session = SessionContext.getSession();
        LambdaQueryWrapper<ShortVideoLike> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShortVideoLike::getUserId, session.getUserId());
        List<ShortVideoLike> shortVideoLikes = shortVideoLikeMapper.selectList(wrapper);

        if (CollectionUtils.isEmpty(shortVideoLikes)) {
            return Collections.emptyList();
        }
        List<Long> videoIds = shortVideoLikes.stream().map(ShortVideoLike::getVideoId).collect(Collectors.toList());
        List<ShortVideo> shortVideos = this.lambdaQuery()
                .in(ShortVideo::getId, videoIds)
                .eq(ShortVideo::getDeleted, false)
                .eq(ShortVideo::getStatus, ReviewEnum.REVIEWED.getCode())
                .list();

        List<ShortVideoVO> shortVideoVOS = BeanUtils.copyPropertiesList(shortVideos, ShortVideoVO.class);
        fillOwnerFlag(shortVideoVOS);
        return shortVideoVOS;
    }

    @Override
    public List<ShortVideoVO> myFavoriteShortVideos() {
        UserSession session = SessionContext.getSession();
        LambdaQueryWrapper<ShortVideoFavorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShortVideoFavorite::getUserId, session.getUserId());
        List<ShortVideoFavorite> shortVideoFavorites = shortVideoFavoriteMapper.selectList(wrapper);
        if (CollectionUtils.isEmpty(shortVideoFavorites)) {
            return Collections.emptyList();
        }
        List<Long> videoIds = shortVideoFavorites.stream().map(ShortVideoFavorite::getVideoId).collect(Collectors.toList());
        List<ShortVideo> shortVideos = this.lambdaQuery()
                .in(ShortVideo::getId, videoIds)
                .eq(ShortVideo::getDeleted, false)
                .eq(ShortVideo::getStatus, ReviewEnum.REVIEWED.getCode())
                .list();
        List<ShortVideoVO> shortVideoVOS = BeanUtils.copyPropertiesList(shortVideos, ShortVideoVO.class);
        fillOwnerFlag(shortVideoVOS);
        return shortVideoVOS;
    }

    @Override
    public List<ShortVideoVO> listShortVideos(ShortVideoQueryDTO dto) {
        LambdaQueryWrapper<ShortVideo> wrapper = buildQueryWrapper(dto);
        wrapper.orderByDesc(ShortVideo::getCreateTime);
        List<ShortVideo> shortVideos = this.list(wrapper);
        List<ShortVideoVO> vos = BeanUtils.copyPropertiesList(shortVideos, ShortVideoVO.class);
        fillOwnerFlag(vos);
        return vos;
    }

    @Override
    public PageResultVO<List<ShortVideoVO>> pageShortVideos(ShortVideoQueryDTO dto) {
        LambdaQueryWrapper<ShortVideo> wrapper = buildQueryWrapper(dto);
        wrapper.orderByDesc(ShortVideo::getCreateTime);
        Page<ShortVideo> page = this.page(new Page<>(PageUtils.getPageNo(), PageUtils.getPageSize()), wrapper);
        List<ShortVideoVO> vos = BeanUtils.copyPropertiesList(page.getRecords(), ShortVideoVO.class);
        fillOwnerFlag(vos);
        return PageResultVO.<List<ShortVideoVO>>builder().data(vos).total(page.getTotal()).build();
    }

    @Override
    public PageResultVO<List<ShortVideoVO>> getRecommendShortVideos(ShortVideoQueryDTO dto) {
        UserSession session = SessionContext.getSession();
        Long userId = session.getUserId();
        
        if (SectionEnum.FOLLOWS.getCode().equals(dto.getSection())) {
            List<FollowVO> myFollows = followService.findMyFollows();
            if (CollectionUtils.isEmpty(myFollows)) {
                return PageResultVO.<List<ShortVideoVO>>builder().data(Collections.emptyList()).total(0).build();
            }
            dto.setFollows(BeanUtils.copyProperties(myFollows, FollowDTO.class));
        } else if (SectionEnum.FRIENDS.getCode().equals(dto.getSection())) {
            List<Long> friendIds = friendService.getFriendIdsByUserId(userId);
            if (CollectionUtils.isEmpty(friendIds)) {
                return PageResultVO.<List<ShortVideoVO>>builder().data(Collections.emptyList()).total(0).build();
            }
            dto.setFriendIds(friendIds);
        }

        Page<ShortVideo> page = this.baseMapper.getRecommendShortVideos(new Page<>(PageUtils.getPageNo(), PageUtils.getPageSize()), dto);

        if (CollectionUtils.isEmpty(page.getRecords())) {
            return PageResultVO.<List<ShortVideoVO>>builder().data(Collections.emptyList()).total(0).build();
        }


        List<ShortVideo> shortVideos = page.getRecords();
        List<ShortVideoVO> vos = BeanUtils.copyPropertiesList(shortVideos, ShortVideoVO.class);
        fillOwnerFlag(vos);

        List<Long> userIds = shortVideos.stream().map(ShortVideo::getUserId).distinct().collect(Collectors.toList());
        List<Long> videoIds = shortVideos.stream().map(ShortVideo::getId).collect(Collectors.toList());
        // shortVideos根据type 进行分组, 获取objectId，得到Map<String, List<Long>>
        Map<String, List<Long>> objectIdMap = shortVideos.stream().collect(Collectors.groupingBy(ShortVideo::getType, Collectors.mapping(ShortVideo::getObjectId, Collectors.toList())));

        Map<Long, Group> groupMap = new HashMap<>();
        if (objectIdMap.containsKey(FollowEnum.GROUP.getCode())) {
            List<Long> groupIds = objectIdMap.get(FollowEnum.GROUP.getCode());
            List<Group> groups = groupService.listByIds(groupIds);
            groupMap = groups.stream().collect(Collectors.toMap(Group::getId, group -> group));
        }

        Map<Long, TemplateGroup> templateGroupMap = new HashMap<>();
        if (objectIdMap.containsKey(FollowEnum.TEMPLATE.getCode())) {
            List<Long> templateIds = objectIdMap.get(FollowEnum.TEMPLATE.getCode());
            List<TemplateGroup> templateGroups = templateGroupService.listByIds(templateIds);
            templateGroupMap = templateGroups.stream().collect(Collectors.toMap(TemplateGroup::getId, templateGroup -> templateGroup));
        }

        Map<Long, TemplateCharacter> characterMap = new HashMap<>();
        if (objectIdMap.containsKey(FollowEnum.CHARACTER.getCode())) {
            List<Long> characterIds = objectIdMap.get(FollowEnum.CHARACTER.getCode());
            List<TemplateCharacter> templateCharacters = templateCharacterService.listByIds(characterIds);
            characterMap = templateCharacters.stream().collect(Collectors.toMap(TemplateCharacter::getId, templateCharacter -> templateCharacter));
        }
        
        // 批量查询当前用户是否点赞
        List<ShortVideoLike> likes = shortVideoLikeMapper.selectList(
                new LambdaQueryWrapper<ShortVideoLike>()
                        .eq(ShortVideoLike::getUserId, userId)
                        .in(ShortVideoLike::getVideoId, videoIds));
        Set<Long> likedVideoIds = likes.stream().map(ShortVideoLike::getVideoId).collect(Collectors.toSet());

        // 批量查询当前用户是否收藏
        List<ShortVideoFavorite> favorites = shortVideoFavoriteMapper.selectList(
                new LambdaQueryWrapper<ShortVideoFavorite>()
                        .eq(ShortVideoFavorite::getUserId, userId)
                        .in(ShortVideoFavorite::getVideoId, videoIds));
        Set<Long> favoritedVideoIds = favorites.stream().map(ShortVideoFavorite::getVideoId).collect(Collectors.toSet());

        Set<String> allFollows = followService.findAllFollows();

        List<User> userList = userService.findUserByIds(userIds);
        // userList根据id分组得到Map<Long, User>
        Map<Long, User> userMap = userList.stream().collect(Collectors.toMap(User::getId, user -> user));
        for (ShortVideoVO vo : vos) {
            User user = userMap.get(vo.getUserId());
            if (FollowEnum.USER.getCode().equals(vo.getType()) && ObjectUtil.isNotNull(user)) {
                vo.setNickName(user.getNickName());
                vo.setHeadImage(user.getHeadImage());
                vo.setAuthorName(user.getNickName());
            } else if (FollowEnum.GROUP.getCode().equals(vo.getType()) && ObjectUtil.isNotNull(groupMap.get(vo.getObjectId()))) {
                Group group = groupMap.get(vo.getObjectId());
                vo.setNickName(group.getName());
                vo.setHeadImage(group.getHeadImage());
                vo.setAuthorName(group.getName());
            } else if (FollowEnum.TEMPLATE.getCode().equals(vo.getType()) && ObjectUtil.isNotNull(templateGroupMap.get(vo.getObjectId()))) {
                TemplateGroup templateGroup = templateGroupMap.get(vo.getObjectId());
                vo.setNickName(templateGroup.getGroupName());
                vo.setHeadImage(templateGroup.getAvatar());
                vo.setAuthorName(templateGroup.getGroupName());
            } else if (FollowEnum.CHARACTER.getCode().equals(vo.getType()) && ObjectUtil.isNotNull(characterMap.get(vo.getObjectId()))) {
                TemplateCharacter templateCharacter = characterMap.get(vo.getObjectId());
                vo.setNickName(templateCharacter.getName());
                vo.setHeadImage(templateCharacter.getAvatar());
                vo.setAuthorName(templateCharacter.getName());
            }
            vo.setLiked(likedVideoIds.contains(vo.getId()));
            vo.setFavorited(favoritedVideoIds.contains(vo.getId()));
            if (allFollows.contains(vo.getObjectId() + ":" + vo.getType())) {
                vo.setFollowed(true);
            }
        }

        return PageResultVO.<List<ShortVideoVO>>builder().data(vos).total(page.getTotal()).build();
    }

    @Override
    public ShortVideoVO getShortVideoById(Long id) {
        ShortVideo shortVideo = this.getById(id);
        if (ObjectUtil.isNull(shortVideo) || Boolean.TRUE.equals(shortVideo.getDeleted())) {
            return null;
        }
        ShortVideoVO vo = BeanUtils.copyProperties(shortVideo, ShortVideoVO.class);
        vo.setIsOwner(isOwner(shortVideo.getUserId()));
        return vo;
    }

    @Transactional
    @Override
    public ShortVideoVO addShortVideo(ShortVideoAddDTO dto) {
        UserSession session = SessionContext.getSession();
        ShortVideo shortVideo = BeanUtils.copyProperties(dto, ShortVideo.class);
        shortVideo.setUserId(session.getUserId());
        shortVideo.setCreateTime(new Date());
        shortVideo.setUpdateTime(new Date());
        shortVideo.setDeleted(false);
        shortVideo.setStatus(ReviewEnum.REVIEWING.getCode());
        setObjectId(shortVideo);
        this.save(shortVideo);
        ShortVideoVO vo = BeanUtils.copyProperties(shortVideo, ShortVideoVO.class);
        vo.setIsOwner(Boolean.TRUE);
        return vo;
    }

    @Transactional
    @Override
    public ShortVideoVO updateShortVideo(ShortVideoUpdateDTO dto) {
        ShortVideo shortVideo = this.getById(dto.getId());
        checkExists(shortVideo);
        checkOwner(shortVideo);
        if (Objects.nonNull(dto.getObjectId())) {
            shortVideo.setObjectId(dto.getObjectId());
        }
        if (StringUtils.isNotBlank(dto.getType())) {
            shortVideo.setType(dto.getType());
        }
        if (Objects.nonNull(dto.getScope())) {
            shortVideo.setScope(dto.getScope());
        }
        if (StringUtils.isNotBlank(dto.getTitle())) {
            shortVideo.setTitle(dto.getTitle());
        }
        if (dto.getDescription() != null) {
            shortVideo.setDescription(dto.getDescription());
        }
        if (dto.getVideoUrl() != null) {
            shortVideo.setVideoUrl(dto.getVideoUrl());
        }
        if (dto.getCoverUrl() != null) {
            shortVideo.setCoverUrl(dto.getCoverUrl());
        }
        if (Objects.nonNull(dto.getDuration())) {
            shortVideo.setDuration(dto.getDuration());
        }
        if (Objects.nonNull(dto.getWidth())) {
            shortVideo.setWidth(dto.getWidth());
        }
        if (Objects.nonNull(dto.getHeight())) {
            shortVideo.setHeight(dto.getHeight());
        }
        if (Objects.nonNull(dto.getSize())) {
            shortVideo.setSize(dto.getSize());
        }
        if (Objects.nonNull(dto.getStatus())) {
            shortVideo.setStatus(dto.getStatus());
        }
        shortVideo.setUpdateTime(new Date());
        this.updateById(shortVideo);
        ShortVideoVO vo = BeanUtils.copyProperties(shortVideo, ShortVideoVO.class);
        vo.setIsOwner(Boolean.TRUE);
        return vo;
    }

    private void setObjectId(ShortVideo shortVideo) {
        if (FollowEnum.USER.getCode().equals(shortVideo.getType())) {
            shortVideo.setObjectId(shortVideo.getUserId());
        }
    }

    @Transactional
    @Override
    public void deleteShortVideo(ShortVideoDelDTO dto) {
        ShortVideo shortVideo = this.getById(dto.getId());
        checkExists(shortVideo);
        checkOwner(shortVideo);
        shortVideo.setDeleted(true);
        shortVideo.setUpdateTime(new Date());
        this.updateById(shortVideo);
    }

    @Transactional
    @Override
    public void batchDeleteShortVideo(ShortVideoBatchDelDTO dto) {
        Long userId = SessionContext.getSession().getUserId();
        List<ShortVideo> shortVideos = this.lambdaQuery()
                .in(ShortVideo::getId, dto.getIds())
                .eq(ShortVideo::getDeleted, false)
                .list();
        for (ShortVideo shortVideo : shortVideos) {
            if (!userId.equals(shortVideo.getUserId())) {
                throw new GlobalException("无权操作");
            }
            shortVideo.setDeleted(true);
            shortVideo.setUpdateTime(new Date());
        }
        if (CollectionUtils.isNotEmpty(shortVideos)) {
            this.updateBatchById(shortVideos);
        }
    }

    @Transactional
    @Override
    public void batchUpdateScope(ShortVideoBatchScopeDTO dto) {
        Long userId = SessionContext.getSession().getUserId();
        List<ShortVideo> shortVideos = this.lambdaQuery()
                .in(ShortVideo::getId, dto.getIds())
                .eq(ShortVideo::getDeleted, false)
                .list();
        for (ShortVideo shortVideo : shortVideos) {
            if (!userId.equals(shortVideo.getUserId())) {
                throw new GlobalException("无权操作");
            }
            shortVideo.setScope(dto.getScope());
            shortVideo.setUpdateTime(new Date());
        }
        if (CollectionUtils.isNotEmpty(shortVideos)) {
            this.updateBatchById(shortVideos);
        }
    }

    @Override
    public void addPlayCount(Long videoId) {
        UserSession session = SessionContext.getSession();
        Long userId = session.getUserId();
        String key = RedisKey.IM_SHORT_VIDEO_PLAY + videoId + ":" + userId;
        // 24h内已播放则不再计数
        if (redisCache.hasKey(key)) {
            return;
        }
        // 播放次数+1
        this.lambdaUpdate()
                .setSql("play_count = play_count + 1")
                .eq(ShortVideo::getId, videoId)
                .update();
        // 标记已播放，24h过期
        redisCache.setCacheObject(key, "1", 24, TimeUnit.HOURS);
    }

    @Override
    public void verifyUserDataAuth(ShortVideo shortVideo) {
        Long userId = SessionContext.getSession().getUserId();
        if (FollowEnum.USER.getCode().equals(shortVideo.getType())) {
            if (!userId.equals(shortVideo.getUserId()) && ViewScopeEnum.PRIVATE.getCode().equals(shortVideo.getScope())) {
                throw new GlobalException("私密作品无法评论或点赞");
            }

            // 好友可见判断是否是好友
            if (ViewScopeEnum.FRIENDS.getCode().equals(shortVideo.getScope())) {
                if (!userId.equals(shortVideo.getUserId())) {
                    Boolean isFriend = friendService.isFriend(shortVideo.getUserId(), userId);
                    if (!isFriend) {
                        throw new GlobalException("无权操作");
                    }
                }
            }
            // 关注可见判断是否是关注者
            else if (ViewScopeEnum.FOLLOW.getCode().equals(shortVideo.getScope())) {
                if (!userId.equals(shortVideo.getUserId())) {
                    Boolean isFollow = followService.isFollow(userId, shortVideo.getUserId(), FollowEnum.USER.getCode());
                    if (!isFollow) {
                        throw new GlobalException("无权操作");
                    }
                }
            }
        }
    }

    private LambdaQueryWrapper<ShortVideo> buildQueryWrapper(ShortVideoQueryDTO dto) {
        LambdaQueryWrapper<ShortVideo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShortVideo::getDeleted, false);
        wrapper.eq(ObjectUtil.isNotNull(dto.getId()), ShortVideo::getId, dto.getId());
        wrapper.eq(ObjectUtil.isNotNull(dto.getUserId()), ShortVideo::getUserId, dto.getUserId());
        wrapper.eq(ObjectUtil.isNotNull(dto.getObjectId()), ShortVideo::getObjectId, dto.getObjectId());
        wrapper.eq(StringUtils.isNotBlank(dto.getType()), ShortVideo::getType, dto.getType());
        wrapper.eq(ObjectUtil.isNotNull(dto.getScope()), ShortVideo::getScope, dto.getScope());
        wrapper.eq(ObjectUtil.isNotNull(dto.getStatus()), ShortVideo::getStatus, dto.getStatus());
        wrapper.like(StringUtils.isNotBlank(dto.getTitle()), ShortVideo::getTitle, dto.getTitle());
        return wrapper;
    }

    private void fillOwnerFlag(List<ShortVideoVO> vos) {
        Long userId = SessionContext.getSession().getUserId();
        if (vos == null) {
            return;
        }
        vos.forEach(item -> item.setIsOwner(userId.equals(item.getUserId())));
    }

    private Boolean isOwner(Long userId) {
        return SessionContext.getSession().getUserId().equals(userId);
    }

    private void checkExists(ShortVideo shortVideo) {
        if (ObjectUtil.isNull(shortVideo) || Boolean.TRUE.equals(shortVideo.getDeleted())) {
            throw new GlobalException("短视频不存在");
        }
    }

    private void checkOwner(ShortVideo shortVideo) {
        if (!SessionContext.getSession().getUserId().equals(shortVideo.getUserId())) {
            throw new GlobalException("无权操作");
        }
    }
}

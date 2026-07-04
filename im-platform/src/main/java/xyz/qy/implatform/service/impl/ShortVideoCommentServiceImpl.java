package xyz.qy.implatform.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xyz.qy.implatform.contant.RedisKey;
import xyz.qy.implatform.dto.ShortVideoCommentAddDTO;
import xyz.qy.implatform.dto.ShortVideoCommentDelDTO;
import xyz.qy.implatform.dto.ShortVideoCommentQueryDTO;
import xyz.qy.implatform.entity.CharacterAvatar;
import xyz.qy.implatform.entity.ShortVideo;
import xyz.qy.implatform.entity.ShortVideoComment;
import xyz.qy.implatform.entity.TemplateCharacter;
import xyz.qy.implatform.entity.User;
import xyz.qy.implatform.enums.ReviewEnum;
import xyz.qy.implatform.enums.TargetTypeEnum;
import xyz.qy.implatform.exception.GlobalException;
import xyz.qy.implatform.mapper.ShortVideoCommentMapper;
import xyz.qy.implatform.mapper.ShortVideoMapper;
import xyz.qy.implatform.service.ICharacterAvatarService;
import xyz.qy.implatform.service.ICommentCharacterService;
import xyz.qy.implatform.service.IShortVideoCommentService;
import xyz.qy.implatform.service.IShortVideoService;
import xyz.qy.implatform.service.ITemplateCharacterService;
import xyz.qy.implatform.service.IUserService;
import xyz.qy.implatform.session.SessionContext;
import xyz.qy.implatform.session.UserSession;
import xyz.qy.implatform.util.BeanUtils;
import xyz.qy.implatform.util.PageUtils;
import xyz.qy.implatform.util.RedisCache;
import xyz.qy.implatform.util.SensitiveUtil;
import xyz.qy.implatform.vo.PageResultVO;
import xyz.qy.implatform.vo.ShortVideoCommentVO;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class ShortVideoCommentServiceImpl extends ServiceImpl<ShortVideoCommentMapper, ShortVideoComment> implements IShortVideoCommentService {
    @Resource
    private ShortVideoMapper shortVideoMapper;

    @Resource
    private ShortVideoCommentMapper shortVideoCommentMapper;

    @Resource
    private ICommentCharacterService commentCharacterService;

    @Resource
    private IShortVideoService shortVideoService;

    @Resource
    private IUserService userService;

    @Resource
    private RedisCache redisCache;

    @Resource
    private ITemplateCharacterService characterService;

    @Resource
    private ICharacterAvatarService characterAvatarService;

    @Override
    public List<ShortVideoCommentVO> listShortVideoComments(ShortVideoCommentQueryDTO dto) {
        LambdaQueryWrapper<ShortVideoComment> wrapper = buildQueryWrapper(dto);
        wrapper.orderByAsc(ShortVideoComment::getCreateTime);
        List<ShortVideoCommentVO> vos = BeanUtils.copyPropertiesList(this.list(wrapper), ShortVideoCommentVO.class);
        fillOwnerFlag(vos);
        return vos;
    }

    @Override
    public PageResultVO<List<ShortVideoCommentVO>> pageShortVideoComments(ShortVideoCommentQueryDTO dto) {
        LambdaQueryWrapper<ShortVideoComment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShortVideoComment::getDeleted, false);
        wrapper.eq(ShortVideoComment::getVideoId, dto.getVideoId());

        wrapper.eq(ObjectUtil.isNotNull(dto.getUserId()), ShortVideoComment::getUserId, dto.getUserId());
        if (Objects.isNull(dto.getTopReplyCommentId())) {
            wrapper.eq(ShortVideoComment::getTopReplyCommentId, 0);
            wrapper.orderByDesc(ShortVideoComment::getCreateTime);
        } else {
            wrapper.eq(ShortVideoComment::getTopReplyCommentId, dto.getTopReplyCommentId());
            wrapper.orderByAsc(ShortVideoComment::getCreateTime);
        }


        Page<ShortVideoComment> page = this.page(new Page<>(PageUtils.getPageNo(), PageUtils.getPageSize()), wrapper);
        List<ShortVideoComment> videoComments = page.getRecords();
        if (CollectionUtils.isEmpty(videoComments)) {
            return PageResultVO.<List<ShortVideoCommentVO>>builder().data(Collections.emptyList()).total(0).build();
        }

        List<ShortVideoCommentVO> videoCommentVOS = BeanUtils.copyProperties(videoComments, ShortVideoCommentVO.class);
        List<Long> parentIds = videoCommentVOS.stream().map(ShortVideoCommentVO::getId).collect(Collectors.toList());
        if (Objects.isNull(dto.getTopReplyCommentId()) || dto.getTopReplyCommentId() == 0L) {
            // 统计每个顶级评论的子评论数
            List<Map<String, Object>> childCountMaps = shortVideoCommentMapper.countChildByTopCommentIds(parentIds);
            Map<Long, Long> childCountMap = childCountMaps.stream()
                    .collect(Collectors.toMap(
                            m -> Long.valueOf(m.get("topReplyCommentId").toString()),
                            m -> Long.valueOf(m.get("childCount").toString())
                    ));
            videoCommentVOS.forEach(vo -> {
                Long count = childCountMap.get(vo.getId());
                vo.setChildCommentCount(ObjectUtil.isNotNull(count) ? count : 0L);
            });
        }

        fillOwnerFlag(videoCommentVOS);
        return PageResultVO.<List<ShortVideoCommentVO>>builder().data(videoCommentVOS).total(page.getTotal()).build();
    }

    @Transactional
    @Override
    public ShortVideoCommentVO addShortVideoComment(ShortVideoCommentAddDTO dto) {
        ShortVideo shortVideo = checkVideo(dto.getVideoId());
        if (!shortVideo.getStatus().equals(ReviewEnum.REVIEWED.getCode())) {
            throw new GlobalException("视频未审核通过，请稍后再试");
        }
        ShortVideoComment parentComment = null;
        if (ObjectUtil.isNotNull(dto.getReplyCommentId())) {
            parentComment = checkParent(dto.getVideoId(), dto.getReplyCommentId());
        }
        UserSession session = SessionContext.getSession();
        Long userId = session.getUserId();

        // 验证用户数据权限
        shortVideoService.verifyUserDataAuth(shortVideo);

        if (ObjectUtil.isNotNull(dto.getCharacterId())) {
            // 校验角色权限
            commentCharacterService.verifyCommentCharacter(userId, dto.getVideoId(), TargetTypeEnum.SHORT_VIDEO.getCode(), dto.getCharacterId(), dto.getAvatarId());
        }

        User user = userService.getById(userId);

        ShortVideoComment comment = BeanUtils.copyProperties(dto, ShortVideoComment.class);

        if (ObjectUtil.isNotNull(dto.getCharacterId())) {
            TemplateCharacter templateCharacter = characterService.getById(dto.getCharacterId());
            if (Objects.isNull(templateCharacter)) {
                throw new GlobalException("当前角色不存在");
            }
            comment.setCharacterId(templateCharacter.getId());
            comment.setUserNickname(templateCharacter.getName());
            comment.setUserAvatar(templateCharacter.getAvatar());

            if (ObjectUtil.isNotNull(dto.getAvatarId())) {
                CharacterAvatar characterAvatar = characterAvatarService.getById(dto.getAvatarId());
                if (ObjectUtil.isNotNull(characterAvatar)) {
                    if (!characterAvatar.getTemplateCharacterId().equals(templateCharacter.getId())) {
                        throw new GlobalException("所选角色头像不属于当前角色");
                    }

                    comment.setAvatarId(characterAvatar.getId());
                    comment.setUserAvatar(characterAvatar.getAvatar());
                    if (!characterAvatar.getLevel().equals(0)) {
                        comment.setUserNickname(characterAvatar.getName());
                    }
                }
            }
        } else {
            comment.setUserNickname(user.getNickName());
            comment.setUserAvatar(user.getHeadImage());
        }

        if (parentComment != null) {
            comment.setReplyCommentId(parentComment.getId());
            if (parentComment.getTopReplyCommentId() == 0L) {
                comment.setTopReplyCommentId(parentComment.getId());
            } else {
                comment.setTopReplyCommentId(parentComment.getTopReplyCommentId());
            }
            comment.setReplyToUserId(parentComment.getUserId());
            comment.setReplyToUserNickname(parentComment.getUserNickname());
            comment.setReplyToUserCharacterId(parentComment.getCharacterId());
            comment.setReplyToUserAvatar(parentComment.getUserAvatar());
        }
        comment.setContent(SensitiveUtil.filter(comment.getContent()));
        comment.setUserId(userId);
        comment.setLikeCount(0);
        comment.setIp(user.getIpAddress());
        comment.setIpAddress(user.getProvince());
        comment.setCreateTime(new Date());
        comment.setUpdateTime(new Date());
        comment.setDeleted(false);
        this.save(comment);
        ShortVideoCommentVO vo = BeanUtils.copyProperties(comment, ShortVideoCommentVO.class);
        vo.setIsOwner(Boolean.TRUE);

        Integer commentCount = this.getCommentCountByVideoId(shortVideo.getId());
        shortVideo.setCommentCount(commentCount);
        shortVideoMapper.updateById(shortVideo);

        return vo;
    }

    @Transactional
    @Override
    public void deleteShortVideoComment(ShortVideoCommentDelDTO dto) {
        ShortVideoComment comment = this.getById(dto.getId());
        checkExists(comment);
        checkOwner(comment);
        comment.setDeleted(true);
        comment.setUpdateTime(new Date());
        this.updateById(comment);

        ShortVideo shortVideo = shortVideoMapper.selectById(comment.getVideoId());
        shortVideo.setCommentCount(this.getCommentCountByVideoId(shortVideo.getId()));
        shortVideoMapper.updateById(shortVideo);
    }

    @Override
    public Integer getCommentCountByVideoId(Long videoId) {
        LambdaQueryWrapper<ShortVideoComment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShortVideoComment::getVideoId, videoId);
        wrapper.eq(ShortVideoComment::getDeleted, false);
        return this.count(wrapper);
    }

    @Override
    public void addCommentLike(Long commentId) {
        UserSession session = SessionContext.getSession();
        Long userId = session.getUserId();
        if (redisCache.hasKey(RedisKey.IM_SHORT_COMMENT_LIKE + userId + ":" + commentId)) {
            throw new GlobalException("您已点赞过该评论");
        }
        ShortVideoComment comment = this.getById(commentId);
        redisCache.setCacheObject(RedisKey.IM_SHORT_COMMENT_LIKE + userId + ":" + commentId, "1",  24, TimeUnit.HOURS);
        comment.setLikeCount(comment.getLikeCount() + 1);
        this.updateById(comment);
    }

    private LambdaQueryWrapper<ShortVideoComment> buildQueryWrapper(ShortVideoCommentQueryDTO dto) {
        LambdaQueryWrapper<ShortVideoComment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShortVideoComment::getDeleted, false);
        wrapper.eq(ObjectUtil.isNotNull(dto.getVideoId()), ShortVideoComment::getVideoId, dto.getVideoId());
        wrapper.eq(ObjectUtil.isNotNull(dto.getUserId()), ShortVideoComment::getUserId, dto.getUserId());
        return wrapper;
    }

    private void fillOwnerFlag(List<ShortVideoCommentVO> vos) {
        Long userId = SessionContext.getSession().getUserId();
        if (vos == null) {
            return;
        }
        vos.forEach(item -> item.setIsOwner(userId.equals(item.getUserId())));
    }

    private ShortVideo checkVideo(Long videoId) {
        ShortVideo shortVideo = shortVideoMapper.selectById(videoId);
        if (ObjectUtil.isNull(shortVideo) || Boolean.TRUE.equals(shortVideo.getDeleted())) {
            throw new GlobalException("短视频不存在");
        }
        return shortVideo;
    }

    private ShortVideoComment checkParent(Long videoId, Long parentId) {
        ShortVideoComment parent = this.getById(parentId);
        if (ObjectUtil.isNull(parent) || Boolean.TRUE.equals(parent.getDeleted())) {
            throw new GlobalException("父评论不存在");
        }
        if (!videoId.equals(parent.getVideoId())) {
            throw new GlobalException("父评论不属于当前短视频");
        }
        return parent;
    }

    private void checkExists(ShortVideoComment comment) {
        if (ObjectUtil.isNull(comment) || Boolean.TRUE.equals(comment.getDeleted())) {
            throw new GlobalException("评论不存在");
        }
    }

    private void checkOwner(ShortVideoComment comment) {
        if (!SessionContext.getSession().getUserId().equals(comment.getUserId())) {
            throw new GlobalException("无权操作");
        }
    }
}

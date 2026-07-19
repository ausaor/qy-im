package xyz.qy.implatform.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xyz.qy.imclient.IMClient;
import xyz.qy.imcommon.model.IMShortVideoMessage;
import xyz.qy.imcommon.model.IMUserInfo;
import xyz.qy.implatform.dto.ShortVideoLikeAddDTO;
import xyz.qy.implatform.dto.ShortVideoLikeDelDTO;
import xyz.qy.implatform.dto.ShortVideoLikeQueryDTO;
import xyz.qy.implatform.entity.ShortVideo;
import xyz.qy.implatform.entity.ShortVideoLike;
import xyz.qy.implatform.entity.ShortVideoNotify;
import xyz.qy.implatform.entity.User;
import xyz.qy.implatform.enums.NotifyActionTypeEnum;
import xyz.qy.implatform.enums.RecordTypeEnum;
import xyz.qy.implatform.enums.ReviewEnum;
import xyz.qy.implatform.enums.ShortVideoNotifyMsgTypeEnum;
import xyz.qy.implatform.exception.GlobalException;
import xyz.qy.implatform.mapper.ShortVideoLikeMapper;
import xyz.qy.implatform.service.IShortVideoLikeService;
import xyz.qy.implatform.service.IShortVideoNotifyService;
import xyz.qy.implatform.service.IShortVideoService;
import xyz.qy.implatform.service.IUserService;
import xyz.qy.implatform.session.SessionContext;
import xyz.qy.implatform.session.UserSession;
import xyz.qy.implatform.util.BeanUtils;
import xyz.qy.implatform.util.PageUtils;
import xyz.qy.implatform.vo.PageResultVO;
import xyz.qy.implatform.vo.ShortVideoLikeVO;
import xyz.qy.implatform.vo.ShortVideoMessageVO;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ShortVideoLikeServiceImpl extends ServiceImpl<ShortVideoLikeMapper, ShortVideoLike> implements IShortVideoLikeService {

    @Resource
    private IShortVideoService shortVideoService;

    @Resource
    private IUserService userService;

    @Resource
    private IShortVideoNotifyService shortVideoNotifyService;

    @Resource
    private IMClient imClient;

    @Override
    public List<ShortVideoLikeVO> listShortVideoLikes(ShortVideoLikeQueryDTO dto) {
        LambdaQueryWrapper<ShortVideoLike> wrapper = buildQueryWrapper(dto);
        wrapper.orderByDesc(ShortVideoLike::getCreateTime);
        List<ShortVideoLikeVO> vos = BeanUtils.copyPropertiesList(this.list(wrapper), ShortVideoLikeVO.class);
        fillOwnerFlag(vos);
        return vos;
    }

    @Override
    public PageResultVO<List<ShortVideoLikeVO>> pageShortVideoLikes(ShortVideoLikeQueryDTO dto) {
        LambdaQueryWrapper<ShortVideoLike> wrapper = buildQueryWrapper(dto);
        wrapper.orderByDesc(ShortVideoLike::getCreateTime);
        Page<ShortVideoLike> page = this.page(new Page<>(PageUtils.getPageNo(), PageUtils.getPageSize()), wrapper);
        List<ShortVideoLikeVO> vos = BeanUtils.copyPropertiesList(page.getRecords(), ShortVideoLikeVO.class);
        fillOwnerFlag(vos);
        return PageResultVO.<List<ShortVideoLikeVO>>builder().data(vos).total(page.getTotal()).build();
    }

    @Transactional
    @Override
    public ShortVideoLikeVO addShortVideoLike(ShortVideoLikeAddDTO dto) {
        UserSession session = SessionContext.getSession();
        Long userId = session.getUserId();
        ShortVideo shortVideo = checkVideo(dto.getVideoId());
        if (!shortVideo.getStatus().equals(ReviewEnum.REVIEWED.getCode())) {
            throw new GlobalException("视频未审核通过，请稍后再试");
        }

        // 验证用户数据权限
        shortVideoService.verifyUserDataAuth(shortVideo);

        checkDuplicate(userId, dto.getVideoId(), null);
        ShortVideoLike like = new ShortVideoLike();
        like.setUserId(userId);
        like.setVideoId(dto.getVideoId());
        like.setTargetId(shortVideo.getObjectId());
        like.setType(shortVideo.getType());
        like.setCreateTime(new Date());
        this.save(like);
        updateVideoLikeCount(dto.getVideoId());
        ShortVideoLikeVO vo = BeanUtils.copyProperties(like, ShortVideoLikeVO.class);
        vo.setIsOwner(Boolean.TRUE);

        if (!userId.equals(shortVideo.getUserId())) {
            ShortVideoNotify shortVideoNotify = new ShortVideoNotify();
            shortVideoNotify.setUserId(shortVideo.getUserId());
            shortVideoNotify.setVideoId(shortVideo.getId());
            shortVideoNotify.setTargetId(shortVideo.getObjectId());
            shortVideoNotify.setTargetType(shortVideo.getType());
            shortVideoNotify.setActionType(NotifyActionTypeEnum.LIKE.getCode());
            shortVideoNotify.setOperateUserId(userId);
            shortVideoNotify.setRecordId(like.getId());
            shortVideoNotify.setRecordType(RecordTypeEnum.LIKE.getCode());
            shortVideoNotifyService.save(shortVideoNotify);

            ShortVideoMessageVO msgInfo = new ShortVideoMessageVO();
            msgInfo.setType(ShortVideoNotifyMsgTypeEnum.LIKE.getCode());
            ShortVideoLikeVO shortVideoLikeVO = BeanUtils.copyProperties(like, ShortVideoLikeVO.class);
            msgInfo.setShortVideoLike(shortVideoLikeVO);
            IMShortVideoMessage<ShortVideoMessageVO> sendMessage = new IMShortVideoMessage<>();
            sendMessage.setData(msgInfo);
            sendMessage.setRecvIds(List.of(shortVideo.getUserId()));
            sendMessage.setSendResult(false);
            sendMessage.setSender(new IMUserInfo(userId, session.getTerminal()));
            imClient.sendShortVideoMessage(sendMessage);
        }
        return vo;
    }

    @Transactional
    @Override
    public void deleteShortVideoLike(ShortVideoLikeDelDTO dto) {
        Long userId = SessionContext.getSession().getUserId();
        Long videoId = dto.getVideoId();
        LambdaQueryWrapper<ShortVideoLike> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShortVideoLike::getUserId, userId)
                .eq(ShortVideoLike::getVideoId, videoId);
        ShortVideoLike like = this.getOne(wrapper);
        checkExists(like);
        this.removeById(like.getId());
        updateVideoLikeCount(videoId);
    }

    @Transactional
    @Override
    public void deleteShortVideoLikes(List<Long> videoIds) {
        Long userId = SessionContext.getSession().getUserId();
        Set<Long> toDeleteVideoIds = new HashSet<>();
        List<Long> likeIds = new ArrayList<>();
        for (Long videoId : videoIds) {
            LambdaQueryWrapper<ShortVideoLike> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ShortVideoLike::getUserId, userId)
                    .eq(ShortVideoLike::getVideoId, videoId);
            ShortVideoLike like = this.getOne(wrapper);
            if (ObjectUtil.isNull(like)) {
                continue;
            }
            if (!userId.equals(like.getUserId())) {
                continue;
            }
            likeIds.add(like.getId());
            toDeleteVideoIds.add(videoId);
        }
        this.removeByIds(likeIds);
        for (Long videoId : toDeleteVideoIds) {
            updateVideoLikeCount(videoId);
        }
    }

    @Override
    public PageResultVO<List<ShortVideoLikeVO>> pageShortVideoLikeUser(ShortVideoLikeQueryDTO dto) {
        if (ObjectUtil.isNull(dto.getVideoId())) {
            throw new GlobalException("请选择视频");
        }
        Long userId = SessionContext.getSession().getUserId();

        ShortVideo shortVideo = shortVideoService.getById(dto.getVideoId());
        if (!userId.equals(shortVideo.getUserId())) {
            throw new GlobalException("无权操作");
        }

        LambdaQueryWrapper<ShortVideoLike> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShortVideoLike::getVideoId, dto.getVideoId());
        wrapper.orderByDesc(ShortVideoLike::getCreateTime);
        Page<ShortVideoLike> page = this.page(new Page<>(PageUtils.getPageNo(), PageUtils.getPageSize()), wrapper);
        if (CollectionUtils.isEmpty(page.getRecords())) {
            return PageResultVO.<List<ShortVideoLikeVO>>builder().data(new ArrayList<>()).total(0).build();
        }
        List<ShortVideoLike> videoLikes = page.getRecords();
        List<Long> userIds = videoLikes.stream().map(ShortVideoLike::getUserId).collect(Collectors.toList());
        List<User> userList = userService.lambdaQuery()
                .select(User::getId, User::getNickName, User::getHeadImage)
                .in(User::getId, userIds)
                .list();
        Map<Long, User> userMap = userList.stream().collect(Collectors.toMap(User::getId, userItem -> userItem));

        List<ShortVideoLikeVO> shortVideoLikeVOS = BeanUtils.copyProperties(videoLikes, ShortVideoLikeVO.class);
        shortVideoLikeVOS.forEach(item -> {
            User user = userMap.get(item.getUserId());
            if (user != null) {
                item.setNickName(user.getNickName());
                item.setHeadImage(user.getHeadImage());
            }
        });

        return PageResultVO.<List<ShortVideoLikeVO>>builder().data(shortVideoLikeVOS).total(page.getTotal()).build();
    }

    private LambdaQueryWrapper<ShortVideoLike> buildQueryWrapper(ShortVideoLikeQueryDTO dto) {
        Long queryUserId = ObjectUtil.isNotNull(dto.getUserId()) ? dto.getUserId() : SessionContext.getSession().getUserId();
        LambdaQueryWrapper<ShortVideoLike> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ObjectUtil.isNotNull(dto.getId()), ShortVideoLike::getId, dto.getId());
        wrapper.eq(ShortVideoLike::getUserId, queryUserId);
        wrapper.eq(ObjectUtil.isNotNull(dto.getVideoId()), ShortVideoLike::getVideoId, dto.getVideoId());
        return wrapper;
    }

    private void fillOwnerFlag(List<ShortVideoLikeVO> vos) {
        Long userId = SessionContext.getSession().getUserId();
        if (vos == null) {
            return;
        }
        vos.forEach(item -> item.setIsOwner(userId.equals(item.getUserId())));
    }

    private ShortVideo checkVideo(Long videoId) {
        ShortVideo shortVideo = shortVideoService.getById(videoId);
        if (ObjectUtil.isNull(shortVideo) || Boolean.TRUE.equals(shortVideo.getDeleted())) {
            throw new GlobalException("短视频不存在");
        }
        return shortVideo;
    }

    private void checkDuplicate(Long userId, Long videoId, Long currentId) {
        LambdaQueryWrapper<ShortVideoLike> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShortVideoLike::getUserId, userId)
                .eq(ShortVideoLike::getVideoId, videoId);
        if (ObjectUtil.isNotNull(currentId)) {
            wrapper.ne(ShortVideoLike::getId, currentId);
        }
        if (this.count(wrapper) > 0) {
            throw new GlobalException("当前短视频已点赞");
        }
    }

    private void checkExists(ShortVideoLike like) {
        if (ObjectUtil.isNull(like)) {
            throw new GlobalException("点赞记录不存在");
        }
    }

    private void checkOwner(ShortVideoLike like) {
        if (!SessionContext.getSession().getUserId().equals(like.getUserId())) {
            throw new GlobalException("无权操作");
        }
    }

    /**
     * 更新短视频点赞数（重新查询）
     */
    private void updateVideoLikeCount(Long videoId) {
        LambdaQueryWrapper<ShortVideoLike> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShortVideoLike::getVideoId, videoId);
        long count = this.count(wrapper);
        ShortVideo video = new ShortVideo();
        video.setId(videoId);
        video.setLikeCount((int) count);
        shortVideoService.updateById(video);
    }
}

package xyz.qy.implatform.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xyz.qy.implatform.dto.ShortVideoNotifyAddDTO;
import xyz.qy.implatform.dto.ShortVideoNotifyQueryDTO;
import xyz.qy.implatform.dto.ShortVideoNotifyUpdateDTO;
import xyz.qy.implatform.entity.ShortVideo;
import xyz.qy.implatform.entity.ShortVideoComment;
import xyz.qy.implatform.entity.ShortVideoNotify;
import xyz.qy.implatform.entity.User;
import xyz.qy.implatform.enums.RecordTypeEnum;
import xyz.qy.implatform.exception.GlobalException;
import xyz.qy.implatform.mapper.ShortVideoCommentMapper;
import xyz.qy.implatform.mapper.ShortVideoMapper;
import xyz.qy.implatform.mapper.ShortVideoNotifyMapper;
import xyz.qy.implatform.mapper.UserMapper;
import xyz.qy.implatform.service.IShortVideoNotifyService;
import xyz.qy.implatform.session.SessionContext;
import xyz.qy.implatform.session.UserSession;
import xyz.qy.implatform.util.BeanUtils;
import xyz.qy.implatform.util.PageUtils;
import xyz.qy.implatform.vo.PageResultVO;
import xyz.qy.implatform.vo.ShortVideoCommentVO;
import xyz.qy.implatform.vo.ShortVideoNotifyVO;
import xyz.qy.implatform.vo.ShortVideoVO;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ShortVideoNotifyServiceImpl extends ServiceImpl<ShortVideoNotifyMapper, ShortVideoNotify> implements IShortVideoNotifyService {
    @Resource
    private ShortVideoMapper shortVideoMapper;

    @Resource
    private ShortVideoCommentMapper shortVideoCommentMapper;

    @Resource
    private UserMapper userMapper;

    @Override
    public ShortVideoNotifyVO getShortVideoNotify(Long id) {
        ShortVideoNotify notify = this.getById(id);
        if (ObjectUtil.isNull(notify)) {
            throw new GlobalException("短视频通知不存在");
        }
        return BeanUtils.copyProperties(notify, ShortVideoNotifyVO.class);
    }

    @Override
    public List<ShortVideoNotifyVO> listShortVideoNotify(ShortVideoNotifyQueryDTO dto) {
        LambdaQueryWrapper<ShortVideoNotify> wrapper = buildQueryWrapper(dto);
        wrapper.orderByDesc(ShortVideoNotify::getCreateTime);
        return BeanUtils.copyPropertiesList(this.list(wrapper), ShortVideoNotifyVO.class);
    }

    @Override
    public PageResultVO<List<ShortVideoNotifyVO>> pageShortVideoNotify(ShortVideoNotifyQueryDTO dto) {
        UserSession session = SessionContext.getSession();
        Long userId = session.getUserId();

        LambdaQueryWrapper<ShortVideoNotify> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShortVideoNotify::getUserId, userId);
        wrapper.eq(ObjectUtil.isNotNull(dto.getRecordType()), ShortVideoNotify::getRecordType, dto.getRecordType());
        wrapper.eq(ShortVideoNotify::getDeleted, false);
        wrapper.orderByDesc(ShortVideoNotify::getCreateTime);
        Page<ShortVideoNotify> page = this.page(new Page<>(PageUtils.getPageNo(), PageUtils.getPageSize()), wrapper);
        if (CollectionUtils.isEmpty(page.getRecords())) {
            return PageResultVO.<List<ShortVideoNotifyVO>>builder().data(Collections.emptyList()).total(0L).build();
        }

        List<ShortVideoNotifyVO> vos = BeanUtils.copyPropertiesList(page.getRecords(), ShortVideoNotifyVO.class);
        List<Long> videoIds = vos.stream().map(ShortVideoNotifyVO::getVideoId).distinct().collect(Collectors.toList());
        List<Long> commentIds = vos.stream().filter(item -> RecordTypeEnum.COMMENT.getCode().equals(item.getRecordType()))
                .map(ShortVideoNotifyVO::getRecordId)
                .collect(Collectors.toList());
        List<Long> userIds = vos.stream().map(ShortVideoNotifyVO::getOperateUserId).distinct().collect(Collectors.toList());

        List<ShortVideo> shortVideos = shortVideoMapper.selectBatchIds(videoIds);
        // shortVideo根据id转成Map<Long, ShortVideo>
        Map<Long, ShortVideo> shortVideoMap = shortVideos.stream().collect(
                Collectors.toMap(ShortVideo::getId, Function.identity(), (key1, key2) -> key2));

        Map<Long, ShortVideoComment> videoCommentMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(commentIds)) {
            List<ShortVideoComment> shortVideoComments = shortVideoCommentMapper.selectBatchIds(commentIds);
            videoCommentMap = shortVideoComments.stream().collect(
                    Collectors.toMap(ShortVideoComment::getId, Function.identity(), (key1, key2) -> key2)
            );

            List<Long> replyCommentIds = shortVideoComments.stream().filter(item -> ObjectUtil.isNotNull(item.getReplyCommentId()))
                    .map(ShortVideoComment::getReplyCommentId)
                    .collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(replyCommentIds)) {
                List<ShortVideoComment> replyComments = shortVideoCommentMapper.selectBatchIds(replyCommentIds);
                videoCommentMap.putAll(replyComments.stream().collect(
                        Collectors.toMap(ShortVideoComment::getId, Function.identity(), (key1, key2) -> key2)
                ));
            }
        }

        List<User> users = userMapper.selectBatchIds(userIds);
        Map<Long, User> userMap = users.stream().collect(Collectors.toMap(User::getId, Function.identity(), (key1, key2) -> key2));

        for (ShortVideoNotifyVO item : vos) {
            item.setOperateUserNickname(userMap.get(item.getOperateUserId()).getNickName());
            item.setOperateUserHeadImage(userMap.get(item.getOperateUserId()).getHeadImage());

            ShortVideo shortVideo = shortVideoMap.get(item.getVideoId());
            item.setShortVideo(BeanUtils.copyProperties(shortVideo, ShortVideoVO.class));
            if (RecordTypeEnum.COMMENT.getCode().equals(item.getRecordType())) {
                ShortVideoComment comment = videoCommentMap.get(item.getRecordId());
                item.setShortVideoComment(BeanUtils.copyProperties(comment, ShortVideoCommentVO.class));
                item.setOperateUserNickname(comment.getUserNickname());
                item.setOperateUserHeadImage(comment.getUserAvatar());
                if (ObjectUtil.isNotNull(comment.getReplyCommentId())) {
                    ShortVideoComment replyComment = videoCommentMap.get(comment.getReplyCommentId());
                    item.getShortVideoComment().setReplyToCommentContent(replyComment.getContent());
                    item.getShortVideoComment().setReplyToCommentType(replyComment.getType());
                }
            }
        }

        return PageResultVO.<List<ShortVideoNotifyVO>>builder().data(vos).total(page.getTotal()).build();
    }

    @Transactional
    @Override
    public ShortVideoNotifyVO addShortVideoNotify(ShortVideoNotifyAddDTO dto) {
        ShortVideoNotify notify = BeanUtils.copyProperties(dto, ShortVideoNotify.class);
        if (ObjectUtil.isNull(notify.getIsRead())) {
            notify.setIsRead(Boolean.FALSE);
        }
        notify.setDeleted(Boolean.FALSE);
        this.save(notify);
        return BeanUtils.copyProperties(notify, ShortVideoNotifyVO.class);
    }

    @Transactional
    @Override
    public ShortVideoNotifyVO updateShortVideoNotify(ShortVideoNotifyUpdateDTO dto) {
        ShortVideoNotify existed = this.getById(dto.getId());
        if (ObjectUtil.isNull(existed)) {
            throw new GlobalException("短视频通知不存在");
        }
        ShortVideoNotify notify = BeanUtils.copyProperties(dto, ShortVideoNotify.class);
        this.updateById(notify);
        return getShortVideoNotify(dto.getId());
    }

    @Transactional
    @Override
    public void deleteShortVideoNotify(Long id) {
        ShortVideoNotify notify = this.getById(id);
        if (ObjectUtil.isNull(notify)) {
            throw new GlobalException("短视频通知不存在");
        }
        this.removeById(id);
    }

    @Transactional
    @Override
    public void batchDeleteShortVideoNotify(List<Long> ids) {
        this.removeByIds(ids);
    }

    private LambdaQueryWrapper<ShortVideoNotify> buildQueryWrapper(ShortVideoNotifyQueryDTO dto) {
        LambdaQueryWrapper<ShortVideoNotify> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ObjectUtil.isNotNull(dto.getRecordType()), ShortVideoNotify::getRecordType, dto.getRecordType());
        return wrapper;
    }
}

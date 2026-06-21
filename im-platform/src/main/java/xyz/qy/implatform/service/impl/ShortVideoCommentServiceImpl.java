package xyz.qy.implatform.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xyz.qy.implatform.dto.ShortVideoCommentAddDTO;
import xyz.qy.implatform.dto.ShortVideoCommentDelDTO;
import xyz.qy.implatform.dto.ShortVideoCommentQueryDTO;
import xyz.qy.implatform.dto.ShortVideoCommentUpdateDTO;
import xyz.qy.implatform.entity.ShortVideo;
import xyz.qy.implatform.entity.ShortVideoComment;
import xyz.qy.implatform.exception.GlobalException;
import xyz.qy.implatform.mapper.ShortVideoCommentMapper;
import xyz.qy.implatform.mapper.ShortVideoMapper;
import xyz.qy.implatform.service.IShortVideoCommentService;
import xyz.qy.implatform.session.SessionContext;
import xyz.qy.implatform.util.BeanUtils;
import xyz.qy.implatform.util.PageUtils;
import xyz.qy.implatform.vo.PageResultVO;
import xyz.qy.implatform.vo.ShortVideoCommentVO;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@Service
public class ShortVideoCommentServiceImpl extends ServiceImpl<ShortVideoCommentMapper, ShortVideoComment> implements IShortVideoCommentService {
    @Resource
    private ShortVideoMapper shortVideoMapper;

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
        LambdaQueryWrapper<ShortVideoComment> wrapper = buildQueryWrapper(dto);
        wrapper.orderByAsc(ShortVideoComment::getCreateTime);
        Page<ShortVideoComment> page = this.page(new Page<>(PageUtils.getPageNo(), PageUtils.getPageSize()), wrapper);
        List<ShortVideoCommentVO> vos = BeanUtils.copyPropertiesList(page.getRecords(), ShortVideoCommentVO.class);
        fillOwnerFlag(vos);
        return PageResultVO.<List<ShortVideoCommentVO>>builder().data(vos).total(page.getTotal()).build();
    }

    @Override
    public ShortVideoCommentVO getShortVideoCommentById(Long id) {
        ShortVideoComment comment = this.getById(id);
        if (ObjectUtil.isNull(comment) || Boolean.TRUE.equals(comment.getDeleted())) {
            return null;
        }
        ShortVideoCommentVO vo = BeanUtils.copyProperties(comment, ShortVideoCommentVO.class);
        vo.setIsOwner(SessionContext.getSession().getUserId().equals(comment.getUserId()));
        return vo;
    }

    @Transactional
    @Override
    public ShortVideoCommentVO addShortVideoComment(ShortVideoCommentAddDTO dto) {
        checkVideo(dto.getVideoId());
        if (ObjectUtil.isNotNull(dto.getParentId())) {
            checkParent(dto.getVideoId(), dto.getParentId());
        }
        ShortVideoComment comment = BeanUtils.copyProperties(dto, ShortVideoComment.class);
        comment.setUserId(SessionContext.getSession().getUserId());
        comment.setLikeCount(0);
        comment.setCreateTime(new Date());
        comment.setUpdateTime(new Date());
        comment.setDeleted(false);
        this.save(comment);
        ShortVideoCommentVO vo = BeanUtils.copyProperties(comment, ShortVideoCommentVO.class);
        vo.setIsOwner(Boolean.TRUE);
        return vo;
    }

    @Transactional
    @Override
    public ShortVideoCommentVO updateShortVideoComment(ShortVideoCommentUpdateDTO dto) {
        ShortVideoComment comment = this.getById(dto.getId());
        checkExists(comment);
        checkOwner(comment);
        if (ObjectUtil.isNotNull(dto.getVideoId())) {
            checkVideo(dto.getVideoId());
            comment.setVideoId(dto.getVideoId());
        }
        if (ObjectUtil.isNotNull(dto.getReplyCommentId())) {
            checkParent(ObjectUtil.isNotNull(dto.getVideoId()) ? dto.getVideoId() : comment.getVideoId(), dto.getReplyCommentId());
            comment.setReplyCommentId(dto.getReplyCommentId());
        }
        if (dto.getReplyToUserId() != null) {
            comment.setReplyToUserId(dto.getReplyToUserId());
        }
        comment.setContent(dto.getContent());
        comment.setUpdateTime(new Date());
        this.updateById(comment);
        ShortVideoCommentVO vo = BeanUtils.copyProperties(comment, ShortVideoCommentVO.class);
        vo.setIsOwner(Boolean.TRUE);
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
    }

    private LambdaQueryWrapper<ShortVideoComment> buildQueryWrapper(ShortVideoCommentQueryDTO dto) {
        LambdaQueryWrapper<ShortVideoComment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShortVideoComment::getDeleted, false);
        wrapper.eq(ObjectUtil.isNotNull(dto.getId()), ShortVideoComment::getId, dto.getId());
        wrapper.eq(ObjectUtil.isNotNull(dto.getVideoId()), ShortVideoComment::getVideoId, dto.getVideoId());
        wrapper.eq(ObjectUtil.isNotNull(dto.getUserId()), ShortVideoComment::getUserId, dto.getUserId());
        wrapper.eq(ObjectUtil.isNotNull(dto.getReplyCommentId()), ShortVideoComment::getReplyCommentId, dto.getReplyCommentId());
        wrapper.eq(ObjectUtil.isNotNull(dto.getReplyToUserId()), ShortVideoComment::getReplyToUserId, dto.getReplyToUserId());
        wrapper.like(StringUtils.isNotBlank(dto.getContent()), ShortVideoComment::getContent, dto.getContent());
        return wrapper;
    }

    private void fillOwnerFlag(List<ShortVideoCommentVO> vos) {
        Long userId = SessionContext.getSession().getUserId();
        if (vos == null) {
            return;
        }
        vos.forEach(item -> item.setIsOwner(userId.equals(item.getUserId())));
    }

    private void checkVideo(Long videoId) {
        ShortVideo shortVideo = shortVideoMapper.selectById(videoId);
        if (ObjectUtil.isNull(shortVideo) || Boolean.TRUE.equals(shortVideo.getDeleted())) {
            throw new GlobalException("短视频不存在");
        }
    }

    private void checkParent(Long videoId, Long parentId) {
        ShortVideoComment parent = this.getById(parentId);
        if (ObjectUtil.isNull(parent) || Boolean.TRUE.equals(parent.getDeleted())) {
            throw new GlobalException("父评论不存在");
        }
        if (!videoId.equals(parent.getVideoId())) {
            throw new GlobalException("父评论不属于当前短视频");
        }
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

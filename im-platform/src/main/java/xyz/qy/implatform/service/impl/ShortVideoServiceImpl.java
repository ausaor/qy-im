package xyz.qy.implatform.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xyz.qy.implatform.dto.ShortVideoAddDTO;
import xyz.qy.implatform.dto.ShortVideoDelDTO;
import xyz.qy.implatform.dto.ShortVideoQueryDTO;
import xyz.qy.implatform.dto.ShortVideoUpdateDTO;
import xyz.qy.implatform.entity.ShortVideo;
import xyz.qy.implatform.enums.ReviewEnum;
import xyz.qy.implatform.exception.GlobalException;
import xyz.qy.implatform.mapper.ShortVideoMapper;
import xyz.qy.implatform.service.IShortVideoService;
import xyz.qy.implatform.session.SessionContext;
import xyz.qy.implatform.session.UserSession;
import xyz.qy.implatform.util.BeanUtils;
import xyz.qy.implatform.util.PageUtils;
import xyz.qy.implatform.vo.PageResultVO;
import xyz.qy.implatform.vo.ShortVideoVO;

import java.util.Date;
import java.util.List;
import java.util.Objects;

@Service
public class ShortVideoServiceImpl extends ServiceImpl<ShortVideoMapper, ShortVideo> implements IShortVideoService {
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
        if (StringUtils.isNotBlank(dto.getCategory())) {
            shortVideo.setCategory(dto.getCategory());
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

    private LambdaQueryWrapper<ShortVideo> buildQueryWrapper(ShortVideoQueryDTO dto) {
        LambdaQueryWrapper<ShortVideo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShortVideo::getDeleted, false);
        wrapper.eq(ObjectUtil.isNotNull(dto.getId()), ShortVideo::getId, dto.getId());
        wrapper.eq(ObjectUtil.isNotNull(dto.getUserId()), ShortVideo::getUserId, dto.getUserId());
        wrapper.eq(ObjectUtil.isNotNull(dto.getObjectId()), ShortVideo::getObjectId, dto.getObjectId());
        wrapper.eq(StringUtils.isNotBlank(dto.getCategory()), ShortVideo::getCategory, dto.getCategory());
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

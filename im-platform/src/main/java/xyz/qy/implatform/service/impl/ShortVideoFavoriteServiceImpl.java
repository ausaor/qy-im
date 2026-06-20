package xyz.qy.implatform.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xyz.qy.implatform.dto.ShortVideoFavoriteAddDTO;
import xyz.qy.implatform.dto.ShortVideoFavoriteDelDTO;
import xyz.qy.implatform.dto.ShortVideoFavoriteQueryDTO;
import xyz.qy.implatform.dto.ShortVideoFavoriteUpdateDTO;
import xyz.qy.implatform.entity.ShortVideo;
import xyz.qy.implatform.entity.ShortVideoFavorite;
import xyz.qy.implatform.exception.GlobalException;
import xyz.qy.implatform.mapper.ShortVideoFavoriteMapper;
import xyz.qy.implatform.mapper.ShortVideoMapper;
import xyz.qy.implatform.service.IShortVideoFavoriteService;
import xyz.qy.implatform.session.SessionContext;
import xyz.qy.implatform.util.BeanUtils;
import xyz.qy.implatform.util.PageUtils;
import xyz.qy.implatform.vo.PageResultVO;
import xyz.qy.implatform.vo.ShortVideoFavoriteVO;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class ShortVideoFavoriteServiceImpl extends ServiceImpl<ShortVideoFavoriteMapper, ShortVideoFavorite> implements IShortVideoFavoriteService {
    @Resource
    private ShortVideoMapper shortVideoMapper;

    @Override
    public List<ShortVideoFavoriteVO> listShortVideoFavorites(ShortVideoFavoriteQueryDTO dto) {
        LambdaQueryWrapper<ShortVideoFavorite> wrapper = buildQueryWrapper(dto);
        wrapper.orderByDesc(ShortVideoFavorite::getCreateTime);
        List<ShortVideoFavoriteVO> vos = BeanUtils.copyPropertiesList(this.list(wrapper), ShortVideoFavoriteVO.class);
        fillOwnerFlag(vos);
        return vos;
    }

    @Override
    public PageResultVO<List<ShortVideoFavoriteVO>> pageShortVideoFavorites(ShortVideoFavoriteQueryDTO dto) {
        LambdaQueryWrapper<ShortVideoFavorite> wrapper = buildQueryWrapper(dto);
        wrapper.orderByDesc(ShortVideoFavorite::getCreateTime);
        Page<ShortVideoFavorite> page = this.page(new Page<>(PageUtils.getPageNo(), PageUtils.getPageSize()), wrapper);
        List<ShortVideoFavoriteVO> vos = BeanUtils.copyPropertiesList(page.getRecords(), ShortVideoFavoriteVO.class);
        fillOwnerFlag(vos);
        return PageResultVO.<List<ShortVideoFavoriteVO>>builder().data(vos).total(page.getTotal()).build();
    }

    @Override
    public ShortVideoFavoriteVO getShortVideoFavoriteById(Long id) {
        ShortVideoFavorite favorite = this.getById(id);
        if (ObjectUtil.isNull(favorite)) {
            return null;
        }
        ShortVideoFavoriteVO vo = BeanUtils.copyProperties(favorite, ShortVideoFavoriteVO.class);
        vo.setIsOwner(SessionContext.getSession().getUserId().equals(favorite.getUserId()));
        return vo;
    }

    @Transactional
    @Override
    public ShortVideoFavoriteVO addShortVideoFavorite(ShortVideoFavoriteAddDTO dto) {
        Long userId = SessionContext.getSession().getUserId();
        checkVideo(dto.getVideoId());
        checkDuplicate(userId, dto.getVideoId(), null);
        ShortVideoFavorite favorite = new ShortVideoFavorite();
        favorite.setUserId(userId);
        favorite.setVideoId(dto.getVideoId());
        favorite.setCreateTime(new Date());
        this.save(favorite);
        updateVideoFavoriteCount(dto.getVideoId());
        ShortVideoFavoriteVO vo = BeanUtils.copyProperties(favorite, ShortVideoFavoriteVO.class);
        vo.setIsOwner(Boolean.TRUE);
        return vo;
    }

    @Transactional
    @Override
    public void deleteShortVideoFavorite(ShortVideoFavoriteDelDTO dto) {
        Long userId = SessionContext.getSession().getUserId();
        Long videoId = dto.getVideoId();
        LambdaQueryWrapper<ShortVideoFavorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShortVideoFavorite::getUserId, userId)
                .eq(ShortVideoFavorite::getVideoId, videoId);
        ShortVideoFavorite favorite = this.getOne(wrapper);
        checkExists(favorite);
        this.removeById(favorite.getId());
        updateVideoFavoriteCount(videoId);
    }

    @Transactional
    @Override
    public void deleteShortVideoFavorites(List<Long> videoIds) {
        Long userId = SessionContext.getSession().getUserId();
        Set<Long> toDeleteVideoIds = new HashSet<>();
        List<Long> favoriteIds = new ArrayList<>();
        for (Long videoId : videoIds) {
            LambdaQueryWrapper<ShortVideoFavorite> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ShortVideoFavorite::getUserId, userId)
                    .eq(ShortVideoFavorite::getVideoId, videoId);
            ShortVideoFavorite favorite = this.getOne(wrapper);
            if (ObjectUtil.isNull(favorite)) {
                continue;
            }
            if (!userId.equals(favorite.getUserId())) {
                continue;
            }
            favoriteIds.add(favorite.getId());
            toDeleteVideoIds.add(videoId);
        }
        this.removeByIds(favoriteIds);
        for (Long videoId : toDeleteVideoIds) {
            updateVideoFavoriteCount(videoId);
        }
    }

    private LambdaQueryWrapper<ShortVideoFavorite> buildQueryWrapper(ShortVideoFavoriteQueryDTO dto) {
        Long queryUserId = ObjectUtil.isNotNull(dto.getUserId()) ? dto.getUserId() : SessionContext.getSession().getUserId();
        LambdaQueryWrapper<ShortVideoFavorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ObjectUtil.isNotNull(dto.getId()), ShortVideoFavorite::getId, dto.getId());
        wrapper.eq(ShortVideoFavorite::getUserId, queryUserId);
        wrapper.eq(ObjectUtil.isNotNull(dto.getVideoId()), ShortVideoFavorite::getVideoId, dto.getVideoId());
        return wrapper;
    }

    private void fillOwnerFlag(List<ShortVideoFavoriteVO> vos) {
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

    private void checkDuplicate(Long userId, Long videoId, Long currentId) {
        LambdaQueryWrapper<ShortVideoFavorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShortVideoFavorite::getUserId, userId)
                .eq(ShortVideoFavorite::getVideoId, videoId);
        if (ObjectUtil.isNotNull(currentId)) {
            wrapper.ne(ShortVideoFavorite::getId, currentId);
        }
        if (this.count(wrapper) > 0) {
            throw new GlobalException("当前短视频已收藏");
        }
    }

    private void checkExists(ShortVideoFavorite favorite) {
        if (ObjectUtil.isNull(favorite)) {
            throw new GlobalException("收藏记录不存在");
        }
    }

    private void checkOwner(ShortVideoFavorite favorite) {
        if (!SessionContext.getSession().getUserId().equals(favorite.getUserId())) {
            throw new GlobalException("无权操作");
        }
    }

    /**
     * 更新短视频收藏数（重新查询）
     */
    private void updateVideoFavoriteCount(Long videoId) {
        LambdaQueryWrapper<ShortVideoFavorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShortVideoFavorite::getVideoId, videoId);
        long count = this.count(wrapper);
        ShortVideo video = new ShortVideo();
        video.setId(videoId);
        video.setFavoriteCount((int) count);
        shortVideoMapper.updateById(video);
    }
}

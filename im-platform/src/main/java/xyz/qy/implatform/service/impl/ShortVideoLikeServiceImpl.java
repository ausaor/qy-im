package xyz.qy.implatform.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xyz.qy.implatform.dto.ShortVideoLikeAddDTO;
import xyz.qy.implatform.dto.ShortVideoLikeDelDTO;
import xyz.qy.implatform.dto.ShortVideoLikeQueryDTO;
import xyz.qy.implatform.dto.ShortVideoLikeUpdateDTO;
import xyz.qy.implatform.entity.ShortVideo;
import xyz.qy.implatform.entity.ShortVideoLike;
import xyz.qy.implatform.exception.GlobalException;
import xyz.qy.implatform.mapper.ShortVideoLikeMapper;
import xyz.qy.implatform.mapper.ShortVideoMapper;
import xyz.qy.implatform.service.IShortVideoLikeService;
import xyz.qy.implatform.session.SessionContext;
import xyz.qy.implatform.util.BeanUtils;
import xyz.qy.implatform.util.PageUtils;
import xyz.qy.implatform.vo.PageResultVO;
import xyz.qy.implatform.vo.ShortVideoLikeVO;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@Service
public class ShortVideoLikeServiceImpl extends ServiceImpl<ShortVideoLikeMapper, ShortVideoLike> implements IShortVideoLikeService {
    @Resource
    private ShortVideoMapper shortVideoMapper;

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

    @Override
    public ShortVideoLikeVO getShortVideoLikeById(Long id) {
        ShortVideoLike like = this.getById(id);
        if (ObjectUtil.isNull(like)) {
            return null;
        }
        ShortVideoLikeVO vo = BeanUtils.copyProperties(like, ShortVideoLikeVO.class);
        vo.setIsOwner(SessionContext.getSession().getUserId().equals(like.getUserId()));
        return vo;
    }

    @Transactional
    @Override
    public ShortVideoLikeVO addShortVideoLike(ShortVideoLikeAddDTO dto) {
        Long userId = SessionContext.getSession().getUserId();
        ShortVideo shortVideo = checkVideo(dto.getVideoId());
        checkDuplicate(userId, dto.getVideoId(), null);
        ShortVideoLike like = new ShortVideoLike();
        like.setUserId(userId);
        like.setVideoId(dto.getVideoId());
        like.setTargetUserId(shortVideo.getUserId());
        like.setCreateTime(new Date());
        this.save(like);
        ShortVideoLikeVO vo = BeanUtils.copyProperties(like, ShortVideoLikeVO.class);
        vo.setIsOwner(Boolean.TRUE);
        return vo;
    }

    @Transactional
    @Override
    public void deleteShortVideoLike(ShortVideoLikeDelDTO dto) {
        ShortVideoLike like = this.getById(dto.getId());
        checkExists(like);
        checkOwner(like);
        this.removeById(dto.getId());
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
        ShortVideo shortVideo = shortVideoMapper.selectById(videoId);
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
}

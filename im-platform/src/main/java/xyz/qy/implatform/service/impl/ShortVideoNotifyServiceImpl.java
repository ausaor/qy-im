package xyz.qy.implatform.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xyz.qy.implatform.dto.ShortVideoNotifyAddDTO;
import xyz.qy.implatform.dto.ShortVideoNotifyQueryDTO;
import xyz.qy.implatform.dto.ShortVideoNotifyUpdateDTO;
import xyz.qy.implatform.entity.ShortVideoNotify;
import xyz.qy.implatform.exception.GlobalException;
import xyz.qy.implatform.mapper.ShortVideoNotifyMapper;
import xyz.qy.implatform.service.IShortVideoNotifyService;
import xyz.qy.implatform.util.BeanUtils;
import xyz.qy.implatform.util.PageUtils;
import xyz.qy.implatform.vo.PageResultVO;
import xyz.qy.implatform.vo.ShortVideoNotifyVO;

import java.util.List;

@Service
public class ShortVideoNotifyServiceImpl extends ServiceImpl<ShortVideoNotifyMapper, ShortVideoNotify> implements IShortVideoNotifyService {

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
        LambdaQueryWrapper<ShortVideoNotify> wrapper = buildQueryWrapper(dto);
        wrapper.orderByDesc(ShortVideoNotify::getCreateTime);
        Page<ShortVideoNotify> page = this.page(new Page<>(PageUtils.getPageNo(), PageUtils.getPageSize()), wrapper);
        List<ShortVideoNotifyVO> vos = BeanUtils.copyPropertiesList(page.getRecords(), ShortVideoNotifyVO.class);
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
        wrapper.eq(ObjectUtil.isNotNull(dto.getId()), ShortVideoNotify::getId, dto.getId());
        wrapper.eq(ObjectUtil.isNotNull(dto.getUserId()), ShortVideoNotify::getUserId, dto.getUserId());
        wrapper.eq(ObjectUtil.isNotNull(dto.getVideoId()), ShortVideoNotify::getVideoId, dto.getVideoId());
        wrapper.eq(ObjectUtil.isNotNull(dto.getTargetId()), ShortVideoNotify::getTargetId, dto.getTargetId());
        wrapper.eq(ObjectUtil.isNotNull(dto.getTargetType()), ShortVideoNotify::getTargetType, dto.getTargetType());
        wrapper.eq(ObjectUtil.isNotNull(dto.getActionType()), ShortVideoNotify::getActionType, dto.getActionType());
        wrapper.eq(ObjectUtil.isNotNull(dto.getIsRead()), ShortVideoNotify::getIsRead, dto.getIsRead());
        wrapper.eq(ObjectUtil.isNotNull(dto.getDeleted()), ShortVideoNotify::getDeleted, dto.getDeleted());
        return wrapper;
    }
}

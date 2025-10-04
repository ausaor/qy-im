package xyz.qy.implatform.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import xyz.qy.implatform.contant.RedisKey;
import xyz.qy.implatform.dto.RegionQueryDTO;
import xyz.qy.implatform.entity.Region;
import xyz.qy.implatform.exception.GlobalException;
import xyz.qy.implatform.mapper.RegionMapper;
import xyz.qy.implatform.service.IRegionService;
import xyz.qy.implatform.util.BeanUtils;
import xyz.qy.implatform.util.LocationServicesUtil;
import xyz.qy.implatform.util.PageUtils;
import xyz.qy.implatform.util.RedisCache;
import xyz.qy.implatform.vo.PageResultVO;
import xyz.qy.implatform.vo.RegionVO;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 地区serviceImpl
 *
 * @author Polaris
 * @since 2024-10-26
 */
@Slf4j
@Service
public class RegionServiceImpl extends ServiceImpl<RegionMapper, Region> implements IRegionService {
    @Resource
    private LocationServicesUtil locationServicesUtil;

    @Resource
    private RedisCache redisCache;

    @Override
    public Region findRegionByCode(String code) {
        return this.getById(code);
    }

    @Override
    public List<RegionVO> findRegionByParentCode(String parentCode) {
        if (StringUtils.isBlank(parentCode)) {
            parentCode = "100000";
        }

        LambdaQueryWrapper<Region> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Region::getParentCode, parentCode)
                .eq(Region::getDeleted, false);
        List<Region> regionList = this.list(queryWrapper);
        if (CollectionUtils.isEmpty(regionList)) {
            return Collections.emptyList();
        }

        return BeanUtils.copyPropertiesList(regionList, RegionVO.class);
    }

    @Override
    public PageResultVO pageRegions(RegionQueryDTO dto) {
        LambdaQueryWrapper<Region> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Region::getDeleted, false);
        wrapper.like(StringUtils.isNotBlank(dto.getName()), Region::getName, dto.getName());
        wrapper.eq(StringUtils.isNotBlank(dto.getCode()), Region::getCode, dto.getCode());
        wrapper.eq(StringUtils.isNotBlank(dto.getParentCode()), Region::getParentCode, dto.getParentCode());
        wrapper.eq(ObjectUtil.isNotNull(dto.getLevel()), Region::getLevel, dto.getLevel());
        wrapper.orderByAsc(Region::getCode);

        Page<Region> page = this.page(new Page<>(PageUtils.getPageNo(), PageUtils.getPageSize()), wrapper);
        if (CollUtil.isEmpty(page.getRecords())) {
            return PageResultVO.builder().data(Collections.emptyList()).build();
        }
        List<Region> regionList = page.getRecords();
        List<RegionVO> regionVOS = BeanUtils.copyPropertiesList(regionList, RegionVO.class);

        return PageResultVO.builder().data(regionVOS).total(page.getTotal()).build();
    }

    @Override
    public List<Region> findAllParentRegionByParentCode(String parentCode) {
        List<Region> parentRegionList = new ArrayList<>();
        findParentRegion(parentRegionList, parentCode);
        return parentRegionList;
    }

    private String getRegionFullNameByCode(String code) {
        Region region = this.getById(code);
        List<Region> parentRegionList = this.findAllParentRegionByParentCode(region.getParentCode());
        parentRegionList.sort((Comparator.comparingInt(Region::getLevel)));
        if (CollectionUtils.isEmpty(parentRegionList)) {
            return region.getName();
        }
        parentRegionList.add(region);
        return parentRegionList.stream().map(Region::getName).collect(Collectors.joining("->"));
    }

    private void findParentRegion(List<Region> parentRegionList, String parentCode) {
        Region region = this.findRegionByCode(parentCode);
        if (ObjectUtil.isNull(region) || region.getLevel().equals(0)) {
            return;
        }
        parentRegionList.add(region);
        findParentRegion(parentRegionList, region.getParentCode());
    }

    @Override
    public RegionVO getRegionLonAndLat(String code) {
        Region region = this.getById(code);
        if (ObjectUtil.isNull(region)) {
            throw new GlobalException("当前地区不存在");
        }
        if (StringUtils.isNoneBlank(region.getLongitude(), region.getLatitude())) {
            return BeanUtils.copyProperties(region, RegionVO.class);
        }

        // 获取上级列表
        List<Region> parentRegionList = this.findAllParentRegionByParentCode(region.getParentCode());
        parentRegionList.sort((Comparator.comparingInt(Region::getLevel)));
        parentRegionList.add(region);
        String address = parentRegionList.stream().map(Region::getName).collect(Collectors.joining());
        Map<String, String> map =locationServicesUtil.getLngAndLatByAddr(address);
        if (StringUtils.isNoneBlank(map.get("lng"), map.get("lat"))) {
            region.setLongitude(map.get("lng"));
            region.setLatitude(map.get("lat"));
            this.updateById(region);
        }
        return BeanUtils.copyProperties(region, RegionVO.class);
    }

    @Override
    public List<RegionVO> findActivityRegions() {
        Set<String> regionCodes = redisCache.revRange(RedisKey.REGION_ACTIVITY_RANGE, 0, 99);
        if (CollectionUtils.isEmpty(regionCodes)) {
            return Collections.emptyList();
        }
        List<Region> regionList = this.listByIds(regionCodes);
        if (CollectionUtils.isNotEmpty(regionList)) {
            List<RegionVO> regionVOS = BeanUtils.copyPropertiesList(regionList, RegionVO.class);
            regionVOS.forEach(regionVO -> {
                regionVO.setFullName(getRegionFullNameByCode(regionVO.getCode()));
            });
            return regionVOS;
        }
        return Collections.emptyList();
    }
}

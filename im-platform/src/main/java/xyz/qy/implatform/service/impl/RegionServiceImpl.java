package xyz.qy.implatform.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import xyz.qy.implatform.entity.Region;
import xyz.qy.implatform.exception.GlobalException;
import xyz.qy.implatform.mapper.RegionMapper;
import xyz.qy.implatform.service.IRegionService;
import xyz.qy.implatform.util.BeanUtils;
import xyz.qy.implatform.util.LocationServicesUtil;
import xyz.qy.implatform.vo.RegionVO;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
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
    public List<Region> findAllParentRegionByParentCode(String parentCode) {
        List<Region> parentRegionList = new ArrayList<>();
        findParentRegion(parentRegionList, parentCode);
        return parentRegionList;
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
}

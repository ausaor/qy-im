package xyz.qy.implatform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import xyz.qy.implatform.entity.Region;
import xyz.qy.implatform.vo.RegionVO;

import java.util.List;

/**
 * 地区service
 *
 * @author Polaris
 * @since 2024-10-26
 */
public interface IRegionService extends IService<Region> {
    /**
     * 根据地区编码地区查询地区
     *
     * @param code 地区编码
     * @return 地区
     */
    Region findRegionByCode(String code);

    /**
     * 根据上级地区编码查找地区
     *
     * @param parentCode 上级地区编码
     * @return 地区列表
     */
    List<RegionVO> findRegionByParentCode(String parentCode);

    /**
     * 根据上级编码递归查询所有上级地区
     *
     * @param parentCode 上级地区编码
     * @return 所有上级地区编码
     */
    List<Region> findAllParentRegionByParentCode(String parentCode);

    /**
     * 获取地区中心经纬度
     *
     * @param code 地区编码
     * @return 地区信息VO
     */
    RegionVO getRegionLonAndLat(String code);
}

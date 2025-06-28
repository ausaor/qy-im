package xyz.qy.implatform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import xyz.qy.implatform.entity.RegionGroupMember;

import java.util.List;

/**
 * 地区群聊常驻用户service
 *
 * @author Polaris
 * @since 2024-10-26
 */
public interface IRegionGroupMemberService extends IService<RegionGroupMember> {
    /**
     * 查询用户常驻地区群聊（包含退出数据）
     *
     * @param userId 用户id
     * @return 地区群聊成员列表
     */
    List<RegionGroupMember> findByUserId(Long userId);

    /**
     * 查询用户常驻地区群聊（未退出）
     *
     * @param userId 用户id
     * @return 地区群聊成员列表
     */
    List<RegionGroupMember> findNoQuitByUserId(Long userId);

    /**
     * 查询地区群聊常驻成员
     *
     * @param regionGroupId 地区群聊id
     * @return 地区群聊常驻成员
     */
    List<RegionGroupMember> findByRegionGroupId(Long regionGroupId);

    /**
     * 根据地区群聊id和用户id查询成员
     *
     * @param regionGroupId 地区群聊id
     * @param userId 用户id
     * @return 常驻用户
     */
    RegionGroupMember findByRegionGroupIdAndUserId(Long regionGroupId, Long userId);

    /**
     * 根据地区群聊id查询常驻用户id列表
     *
     * @param regionGroupId 地区群聊id
     * @return 用户id列表
     */
    List<Long> findUserIdsByRegionGroupId(Long regionGroupId);

    /**
     * 根据地区编码查询地区群聊成员id列表
     *
     * @param regionCode 地区编码
     * @return 地区群聊成员id列表
     */
    List<Long> findUserIdsByCode(String regionCode);

    Boolean isInRegionGroup(String regionCode, List<Long> userIds);

    Boolean isTempMember(String regionCode, Long userId);
}

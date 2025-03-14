package xyz.qy.implatform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import xyz.qy.implatform.entity.RegionGroupMember;
import xyz.qy.implatform.mapper.RegionGroupMemberMapper;
import xyz.qy.implatform.service.IRegionGroupMemberService;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 地区群聊常驻用户serviceImpl
 *
 * @author Polaris
 * @since 2024-10-26
 */
@Slf4j
@Service
public class RegionGroupMemberServiceImpl extends ServiceImpl<RegionGroupMemberMapper, RegionGroupMember> implements IRegionGroupMemberService {
    @Override
    public List<RegionGroupMember> findByUserId(Long userId) {
        LambdaQueryWrapper<RegionGroupMember> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(RegionGroupMember::getUserId, userId);
        return this.list(queryWrapper);
    }

    @Override
    public List<RegionGroupMember> findNoQuitByUserId(Long userId) {
        List<RegionGroupMember> regionGroupMembers = this.findByUserId(userId);
        return regionGroupMembers.stream().filter(item -> !item.getQuit()).collect(Collectors.toList());
    }

    @Override
    public List<RegionGroupMember> findByRegionGroupId(Long regionGroupId) {
        LambdaQueryWrapper<RegionGroupMember> memberWrapper = Wrappers.lambdaQuery();
        memberWrapper.eq(RegionGroupMember::getRegionGroupId, regionGroupId);
        return this.list(memberWrapper);
    }

    @Override
    public RegionGroupMember findByRegionGroupIdAndUserId(Long regionGroupId, Long userId) {
        LambdaQueryWrapper<RegionGroupMember> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(RegionGroupMember::getRegionGroupId, regionGroupId);
        queryWrapper.eq(RegionGroupMember::getUserId, userId);
        return this.getOne(queryWrapper);
    }

    @Override
    public List<Long> findUserIdsByRegionGroupId(Long regionGroupId) {
        LambdaQueryWrapper<RegionGroupMember> memberWrapper = Wrappers.lambdaQuery();
        memberWrapper.eq(RegionGroupMember::getRegionGroupId, regionGroupId)
                .eq(RegionGroupMember::getQuit, false);
        List<RegionGroupMember> members = this.list(memberWrapper);
        return members.stream().map(RegionGroupMember::getUserId).collect(Collectors.toList());
    }
}

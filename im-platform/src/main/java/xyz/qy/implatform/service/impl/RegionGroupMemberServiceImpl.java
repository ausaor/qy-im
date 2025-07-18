package xyz.qy.implatform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import xyz.qy.imcommon.contant.IMRedisKey;
import xyz.qy.implatform.entity.RegionGroupMember;
import xyz.qy.implatform.mapper.RegionGroupMemberMapper;
import xyz.qy.implatform.service.IRegionGroupMemberService;
import xyz.qy.implatform.util.RedisCache;

import javax.annotation.Resource;
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
    @Resource
    private RedisCache redisCache;

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

    @Override
    public List<Long> findUserIdsByCode(String regionCode) {
        return this.lambdaQuery().eq(RegionGroupMember::getCode, regionCode)
                .eq(RegionGroupMember::getQuit, false)
                .select(RegionGroupMember::getUserId)
                .list().stream().map(RegionGroupMember::getUserId).collect(Collectors.toList());
    }

    @Override
    public Boolean isInRegionGroup(String regionCode, List<Long> userId) {
        LambdaQueryWrapper<RegionGroupMember> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(RegionGroupMember::getCode, regionCode)
                .eq(RegionGroupMember::getQuit, false)
                .in(RegionGroupMember::getUserId, userId);
        return this.count(wrapper) == userId.size();
    }

    @Override
    public Boolean isTempMember(String regionCode, Long userId) {
        return redisCache.hasKey(IMRedisKey.IM_USER_TEMP_REGION_GROUP + userId + ":" + regionCode);
    }

    @Override
    public Boolean existsInSameGroup(Long userId1, Long userId2) {
        return baseMapper.existsInSameGroup(userId1, userId2);
    }
}

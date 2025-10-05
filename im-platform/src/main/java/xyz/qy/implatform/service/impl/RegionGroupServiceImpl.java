package xyz.qy.implatform.service.impl;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xyz.qy.imclient.IMClient;
import xyz.qy.imclient.annotation.Lock;
import xyz.qy.imcommon.contant.IMRedisKey;
import xyz.qy.implatform.contant.Constant;
import xyz.qy.implatform.contant.DateFormat;
import xyz.qy.implatform.contant.RedisKey;
import xyz.qy.implatform.contant.RegionGroupConst;
import xyz.qy.implatform.dto.RegionGroupBanDTO;
import xyz.qy.implatform.dto.RegionGroupDTO;
import xyz.qy.implatform.dto.RegionGroupMemberDTO;
import xyz.qy.implatform.entity.Region;
import xyz.qy.implatform.entity.RegionGroup;
import xyz.qy.implatform.entity.RegionGroupMember;
import xyz.qy.implatform.entity.User;
import xyz.qy.implatform.enums.BanTypeEnum;
import xyz.qy.implatform.enums.GroupChangeTypeEnum;
import xyz.qy.implatform.enums.LeaderVoteNumEnum;
import xyz.qy.implatform.enums.ResultCode;
import xyz.qy.implatform.exception.GlobalException;
import xyz.qy.implatform.mapper.RegionGroupMapper;
import xyz.qy.implatform.service.IRegionGroupMemberService;
import xyz.qy.implatform.service.IRegionGroupService;
import xyz.qy.implatform.service.IRegionService;
import xyz.qy.implatform.service.IUserService;
import xyz.qy.implatform.session.SessionContext;
import xyz.qy.implatform.session.UserSession;
import xyz.qy.implatform.util.BeanUtils;
import xyz.qy.implatform.util.DateTimeUtils;
import xyz.qy.implatform.util.MessageSendUtil;
import xyz.qy.implatform.util.RedisCache;
import xyz.qy.implatform.vo.RegionGroupMemberVO;
import xyz.qy.implatform.vo.RegionGroupVO;
import xyz.qy.implatform.vo.UserVO;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 地区群聊serviceImpl
 *
 * @author Polaris
 * @since 2024-10-26
 */
@Slf4j
@Service
public class RegionGroupServiceImpl extends ServiceImpl<RegionGroupMapper, RegionGroup> implements IRegionGroupService {
    @Resource
    private IRegionService regionService;

    @Resource
    private IRegionGroupMemberService regionGroupMemberService;

    @Resource
    private IUserService userService;

    @Resource
    private IMClient imClient;

    @Resource
    private MessageSendUtil messageSendUtil;

    @Resource
    private RedisCache redisCache;

    @Resource
    private RedissonClient redissonClient;

    @Override
    public List<RegionGroupVO> findRegionGroups() {
        UserSession session = SessionContext.getSession();
        Long userId = session.getUserId();

        // 1、查询用户常驻地区群聊
        List<RegionGroupMember> regionGroupMembers = regionGroupMemberService.findNoQuitByUserId(session.getUserId());

        List<RegionGroupVO> regionGroupVOS = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(regionGroupMembers)) {
            List<Long> regionGroupIds = regionGroupMembers.stream()
                    .map(RegionGroupMember::getRegionGroupId).collect(Collectors.toList());

            LambdaQueryWrapper<RegionGroup> groupWrapper = Wrappers.lambdaQuery();
            groupWrapper.in(RegionGroup::getId, regionGroupIds);
            // 查询用户地区群聊信息
            List<RegionGroup> regionGroupList = this.list(groupWrapper);
            if (CollectionUtils.isNotEmpty(regionGroupList)) {
                regionGroupList.forEach(item -> {
                    RegionGroupVO regionGroupVO = BeanUtils.copyProperties(item, RegionGroupVO.class);
                    regionGroupVO.setJoinType(1);
                    regionGroupVOS.add(regionGroupVO);
                });
            }
        }


        //2、查询用户临时加入的地区群聊
        Collection<String> keys = redisCache.keys(IMRedisKey.IM_USER_TEMP_REGION_GROUP + userId + ":*");
        if (CollectionUtils.isNotEmpty(keys)) {
            for (String key : keys) {
                Object object = redisCache.getCacheObject(key);
                RegionGroup regionGroup = Convert.convert(RegionGroup.class, object);
                RegionGroup group = this.getById(regionGroup.getId());
                RegionGroupVO regionGroupVO = BeanUtils.copyProperties(group, RegionGroupVO.class);
                regionGroupVO.setJoinType(0);
                regionGroupVOS.add(regionGroupVO);
            }
        }

        return regionGroupVOS;
    }

    @Override
    public RegionGroupVO findById(Long regionGroupId) {
        UserSession session = SessionContext.getSession();
        RegionGroup regionGroup = super.getById(regionGroupId);
        if (Objects.isNull(regionGroup)) {
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "群组不存在");
        }

        RegionGroupVO vo = BeanUtils.copyProperties(regionGroup, RegionGroupVO.class);
        assert vo != null;
        RegionGroupMember member = regionGroupMemberService.findByRegionGroupIdAndUserId(regionGroupId, session.getUserId());

        if (ObjectUtil.isNotNull(regionGroup.getLeaderId()) && ObjectUtil.isNotNull(regionGroup.getExpirationTime())) {
            if (new Date().after(regionGroup.getExpirationTime())) {
                vo.setLeaderId(null);
            }
        }

        // 判断当前用户是否临时加入
        Object object = redisCache.getCacheObject(IMRedisKey.IM_REGION_GROUP_NUM_TEMP_USER
                + regionGroup.getCode() + ":" + regionGroup.getNum() + ":" + session.getUserId());

        if (ObjectUtil.isNotNull(member) && !member.getQuit()) {
            vo.setJoinType(1);
            vo.setQuit(false);
        } else if (ObjectUtil.isNotNull(object)){
            vo.setJoinType(0);
            vo.setQuit(false);
        } else {
            throw new GlobalException("您未加入当前地区群聊");
        }
        return vo;
    }

    @Override
    public List<RegionGroupVO> findRegionGroupsByCode(String code) {
        LambdaQueryWrapper<RegionGroup> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RegionGroup::getCode, code);
        wrapper.eq(RegionGroup::getDeleted, false);
        List<RegionGroup> regionGroups = this.list(wrapper);
        if (CollectionUtils.isEmpty(regionGroups)) {
            return Collections.emptyList();
        }

        List<RegionGroupVO> regionGroupVOS = BeanUtils.copyProperties(regionGroups, RegionGroupVO.class);

        // 获取群主信息
        List<Long> userIds = regionGroups.stream().map(RegionGroup::getLeaderId)
                .filter(Objects::nonNull).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(userIds)) {
            List<User> userList = userService.findUserByIds(userIds);
            // userList根据id分组获取Map
            Map<Long, User> userMap = userList.stream().collect(Collectors.toMap(User::getId, Function.identity()));
            regionGroupVOS.forEach(item -> {
               if (ObjectUtil.isNotNull(item.getLeaderId())) {
                   User user = userMap.get(item.getLeaderId());
                   if (ObjectUtil.isNotNull(user)) {
                       item.setGroupAdmin(user.getUserName());
                   }
               }
            });
        }

        return regionGroupVOS;
    }

    @Override
    public RegionGroupMemberVO modifyRegionGroupMember(RegionGroupMemberDTO dto) {
        // 只有常驻成员可以修改群昵称和头像
        UserSession session = SessionContext.getSession();

        if (ObjectUtil.isNull(dto.getRegionGroupId())) {
            throw new GlobalException("参数异常");
        }
        if (StringUtils.isBlank(dto.getAliasName())) {
            throw new GlobalException("群昵称不能为空");
        }
        
        // 查询常驻成员信息
        RegionGroupMember regionGroupMember = regionGroupMemberService.findByRegionGroupIdAndUserId(dto.getRegionGroupId(), session.getUserId());
        if (ObjectUtil.isNull(regionGroupMember) || regionGroupMember.getQuit()) {
            throw new GlobalException("您不是当前地区群聊常驻成员");
        }
        regionGroupMember.setAliasName(dto.getAliasName());
        regionGroupMember.setHeadImage(dto.getHeadImage());
        regionGroupMemberService.updateById(regionGroupMember);

        messageSendUtil.sendRegionGroupTipMsg(regionGroupMember.getRegionGroupId(), session.getUserId(), session.getNickName(), null,
                "地区群聊数据有更新", GroupChangeTypeEnum.GROUP_MEMBER_CHANGE.getCode());
        return BeanUtils.copyProperties(regionGroupMember, RegionGroupMemberVO.class);
    }

    @Override
    public List<RegionGroupMemberVO> findRegionGroupMembers(Long regionGroupId) {
        RegionGroup regionGroup = this.getById(regionGroupId);
        if (ObjectUtil.isNull(regionGroup) || regionGroup.getDeleted()) {
            throw new GlobalException("当前地区群聊不存在");
        }

        // 查询所有常驻用户（包含已退出）
        List<RegionGroupMember> regionGroupMembers = regionGroupMemberService.findByRegionGroupId(regionGroupId);

        // 查询当前地区群聊临时加入用户
        Collection<String> keys = redisCache.keys(IMRedisKey.IM_REGION_GROUP_NUM_TEMP_USER + regionGroup.getCode() + ":" + regionGroup.getNum() + ":*");
        List<RegionGroupMemberVO> tempMemberVos = new ArrayList<>();
        for (String key : keys) {
            Object object = redisCache.getCacheObject(key);
            if (ObjectUtil.isNull(object)) {
                continue;
            }
            UserSession userSession = Convert.convert(UserSession.class, object);
            UserVO userVO = userService.findUserById(userSession.getUserId());
            RegionGroupMemberVO memberVO = new RegionGroupMemberVO();
            memberVO.setRegionGroupId(regionGroup.getId());
            memberVO.setQuit(false);
            memberVO.setHeadImage(userVO.getHeadImage());
            memberVO.setAliasName(userVO.getNickName());
            memberVO.setRemark(userVO.getNickName());
            memberVO.setUserId(userVO.getId());
            memberVO.setJoinType(0);
            memberVO.setOnline(imClient.isOnline(userVO.getId()));
            tempMemberVos.add(memberVO);
        }

        if (CollectionUtils.isEmpty(regionGroupMembers) && CollectionUtils.isEmpty(tempMemberVos)) {
            return Collections.emptyList();
        }
        Date date = new Date();
        if (CollectionUtils.isNotEmpty(regionGroupMembers)) {
            List<Long> userIds = regionGroupMembers.stream().map(RegionGroupMember::getUserId).collect(Collectors.toList());
            List<User> userList = userService.listByIds(userIds);
            List<Long> onlineUserIds = imClient.getOnlineUser(userIds);
            Map<Long, User> userMap = userList.stream().collect(Collectors.toMap(User::getId, Function.identity()));
            List<RegionGroupMemberVO> groupMemberVOS = regionGroupMembers.stream().map(m -> {
                RegionGroupMemberVO vo = BeanUtils.copyProperties(m, RegionGroupMemberVO.class);
                if (StringUtils.isBlank(vo.getHeadImage())) {
                    User user = userMap.get(vo.getUserId());
                    vo.setHeadImage(user.getHeadImage());
                }
                if (vo.getUserId().equals(regionGroup.getLeaderId()) && date.before(regionGroup.getExpirationTime())) {
                    vo.setIsLeader(true);
                }
                vo.setJoinType(1);
                vo.setOnline(onlineUserIds.contains(m.getUserId()));
                return vo;
            }).collect(Collectors.toList());
            groupMemberVOS.addAll(tempMemberVos);
            return groupMemberVOS.stream().sorted((m1,m2)-> m2.getOnline().compareTo(m1.getOnline()))
                    .collect(Collectors.toList());
        }

        return tempMemberVos.stream().sorted((m1,m2)-> m2.getOnline().compareTo(m1.getOnline()))
                .collect(Collectors.toList());
    }

    @Lock(prefix = "im:region:group:member:modify", key = "#regionGroupDTO.getCode()")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public RegionGroupVO joinRegionGroup(RegionGroupDTO regionGroupDTO) {
        UserSession session = SessionContext.getSession();
        Region region = regionService.findRegionByCode(regionGroupDTO.getCode());
        if (ObjectUtil.isNull(region) || region.getDeleted()) {
            throw new GlobalException("所选地区不存在");
        }

        // 判断用户加入类型
        Integer joinType = regionGroupDTO.getJoinType();

        // 临时加入
        if (joinType == 0) {
            RegionGroup regionGroup = tempJoinRegionGroupHandle(regionGroupDTO, region, session);
            RegionGroupVO regionGroupVO = BeanUtils.copyProperties(regionGroup, RegionGroupVO.class);
            regionGroupVO.setJoinType(0);
            return regionGroupVO;
        } else {
            RegionGroup regionGroup = permanentJoinRegionGroupHandle(regionGroupDTO, region, session);
            RegionGroupVO regionGroupVO = BeanUtils.copyProperties(regionGroup, RegionGroupVO.class);
            regionGroupVO.setJoinType(1);
            return regionGroupVO;
        }
    }

    private RegionGroup permanentJoinRegionGroupHandle(RegionGroupDTO regionGroupDTO, Region region, UserSession session) {
        Collection<String> keys = redisCache.keys(IMRedisKey.IM_USER_TEMP_REGION_GROUP + session.getUserId() + ":*");

        // 判断选择的地区群聊是否已加入
        for (String key : keys) {
            if (key.equals(IMRedisKey.IM_USER_TEMP_REGION_GROUP + session.getUserId() + ":" + regionGroupDTO.getCode())) {
                throw new GlobalException("您已临时加入当前地区群聊，请先退出");
            }
        }

        // 查询用户所有常驻群聊
        List<RegionGroupMember> regionGroupMembers = regionGroupMemberService.findByUserId(session.getUserId());

        // 计算未退出数量
        long count = regionGroupMembers.stream().filter(item -> !item.getQuit()).count();
        if (count >= RegionGroupConst.MAX_REGION_GROUP_NUM) {
            throw new GlobalException("每位用户最多只能常驻3个地区群聊");
        }

        // 若用户历史加入的地区群聊超过3个，判断距离最近一次加入的时间是否超过30分钟
        if (regionGroupMembers.size() >= RegionGroupConst.MAX_REGION_GROUP_NUM) {
            checkLastPermanentJoinInterval(regionGroupMembers);
        }
        return getPermanentJoinRegionGroupNum(regionGroupDTO, region, session);
    }

    private void checkLastPermanentJoinInterval(List<RegionGroupMember> regionGroupMembers) {
        Collections.sort(regionGroupMembers, Comparator.comparing(RegionGroupMember::getCreateTime, Comparator.reverseOrder()));

        Date createTime = regionGroupMembers.get(0).getCreateTime();

        Date now = new Date();
        long between = DateUtil.between(createTime, now, DateUnit.MINUTE);
        if (between < RegionGroupConst.REGION_GROUP_JOIN_GAP) {
            throw new GlobalException("距离您上次加入地区群聊未超过" + RegionGroupConst.REGION_GROUP_JOIN_GAP + "分钟，请稍后再尝试");
        }
    }

    private RegionGroup tempJoinRegionGroupHandle(RegionGroupDTO regionGroupDTO, Region region, UserSession session) {
        Long userId = session.getUserId();
        // 判断当前用户加入的临时地区群聊redis数量
        Collection<String> keys = redisCache.keys(IMRedisKey.IM_USER_TEMP_REGION_GROUP + userId + ":*");
        if (keys.size() >= RegionGroupConst.MAX_REGION_GROUP_NUM) {
            throw new GlobalException("每位用户最多只能临时加入" + RegionGroupConst.MAX_REGION_GROUP_NUM + "个地区群聊");
        }

        // 判断选择的地区群聊是否已加入
        for (String key : keys) {
            if (key.equals(IMRedisKey.IM_USER_TEMP_REGION_GROUP + userId + ":" + regionGroupDTO.getCode())) {
                throw new GlobalException("您已加入当前地区群聊");
            }
        }

        Boolean exists = redisCache.hasKey(IMRedisKey.IM_REGION_GROUP_USER_TEMP_JOIN + userId);

        // 判断用户6小时内临时加入的地区群聊是否达到上限
        int count = redisCache.incrementInt(IMRedisKey.IM_REGION_GROUP_USER_TEMP_JOIN + userId);
        if (!exists) {
            redisCache.expire(IMRedisKey.IM_REGION_GROUP_USER_TEMP_JOIN + userId, RegionGroupConst.MAX_REGION_GROUP_NUM * 2, TimeUnit.HOURS);
        }
        if (count > RegionGroupConst.MAX_REGION_GROUP_NUM * 2) {
            long expire = redisCache.getExpire(IMRedisKey.IM_REGION_GROUP_USER_TEMP_JOIN + userId);
            throw new GlobalException("您在"+ (RegionGroupConst.MAX_REGION_GROUP_NUM * 2) +"小时内临时加入的地区群聊数量已达上限，请在" + DateTimeUtils.getTimeValueDesc(expire) + "后再尝试");
        }

        // 判断要加入的地区群聊编号
        RegionGroup regionGroup = getTempJoinRegionGroupNum(region, userId);

        this.saveOrUpdate(regionGroup);

        // 保存用户临时加入的地区群聊到redis
        redisCache.setCacheObject(IMRedisKey.IM_USER_TEMP_REGION_GROUP + userId + ":" + regionGroupDTO.getCode(),
                regionGroup, RegionGroupConst.TEMP_MEMBER_DURATION, TimeUnit.HOURS);

        // 保存地区群聊对应编号的临时用户数据到redis
        redisCache.setCacheObject(IMRedisKey.IM_REGION_GROUP_NUM_TEMP_USER
                + regionGroup.getCode() + ":" + regionGroup.getNum() + ":" + userId, session, RegionGroupConst.TEMP_MEMBER_DURATION, TimeUnit.HOURS);

        String content = "用户【" + session.getNickName() + "】加入了群聊";
        messageSendUtil.sendRegionGroupTipMsg(regionGroup.getId(), userId, session.getNickName(), null, content, GroupChangeTypeEnum.NEW_USER_JOIN.getCode());
        return regionGroup;
    }

    private RegionGroup getPermanentJoinRegionGroupNum(RegionGroupDTO regionGroupDTO, Region region, UserSession session) {
        LambdaQueryWrapper<RegionGroup> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(RegionGroup::getCode, region.getCode());
        queryWrapper.eq(RegionGroup::getDeleted, false);

        List<RegionGroup> regionGroupList = this.list(queryWrapper);

        // 为空代表当前地区没有创建群聊
        if (CollectionUtils.isEmpty(regionGroupList)) {
            RegionGroup regionGroup = new RegionGroup();
            regionGroup.setCode(region.getCode());
            regionGroup.setNum(Constant.ROME_NUM[0]);
            regionGroup.setRegionGroupName(generateRegionGroupName(region) + "-" + regionGroup.getNum());
            regionGroup.setRemark(region.getName() + "-" + regionGroup.getNum());
            regionGroup.setCreateBy(session.getUserId());
            this.saveOrUpdate(regionGroup);

            saveRegionGroupMember(new RegionGroupMember(), regionGroupDTO, regionGroup, session);
            return regionGroup;
        }

        // 已存在的地区群聊编号
        List<String> existsNum = new ArrayList<>();

        // 不为空判断要加入的地区群聊常驻用户不超过100的群聊，获取第一个
        for (RegionGroup regionGroup : regionGroupList) {
            existsNum.add(regionGroup.getNum());
            // 查询当前区域下所有群聊常驻人员
            List<RegionGroupMember> regionGroupMembers = regionGroupMemberService
                    .findByRegionGroupId(regionGroup.getId());

            // 计算所有未退出常驻人员数量
            long count = regionGroupMembers.stream().filter(item -> !item.getQuit()).count();
            if (count >= RegionGroupConst.MAX_REGION_GROUP_MEMBER_COUNT) {
                continue;
            }
            Optional<RegionGroupMember> optional = regionGroupMembers.stream()
                    .filter(item -> session.getUserId().equals(item.getUserId())).findFirst();
            RegionGroupMember regionGroupMember = optional.orElseGet(RegionGroupMember::new);
            this.saveOrUpdate(regionGroup);
            saveRegionGroupMember(regionGroupMember, regionGroupDTO, regionGroup, session);
            return regionGroup;
        }

        // 执行到此处说明所有已存在的地区群聊编号常驻用户已满，判断当前地区是否还可以创建地区群聊
        for (String num : Constant.ROME_NUM) {
            if (existsNum.contains(num)) {
                continue;
            }
            RegionGroup regionGroup = new RegionGroup();
            regionGroup.setNum(num);
            regionGroup.setCode(region.getCode());
            regionGroup.setRegionGroupName(generateRegionGroupName(region) + "-" + regionGroup.getNum());
            regionGroup.setRemark(region.getName() + "-" + regionGroup.getNum());
            regionGroup.setCreateBy(session.getUserId());
            this.saveOrUpdate(regionGroup);

            saveRegionGroupMember(new RegionGroupMember(), regionGroupDTO, regionGroup, session);
            return regionGroup;
        }

        throw new GlobalException("当前地区常驻用户已满，不能再加入");
    }

    private void saveRegionGroupMember(RegionGroupMember regionGroupMember, RegionGroupDTO regionGroupDTO,
                                       RegionGroup regionGroup, UserSession session) {
        regionGroupMember.setQuit(false);
        regionGroupMember.setCode(regionGroup.getCode());
        regionGroupMember.setUserId(session.getUserId());
        regionGroupMember.setRegionGroupId(regionGroup.getId());
        regionGroupMember.setAliasName(StringUtils.isNotBlank(regionGroupDTO.getNickname()) ?
                regionGroupDTO.getNickname() : session.getNickName());
        regionGroupMember.setCreateBy(session.getUserId());

        List<RegionGroupMember> list = regionGroupMemberService.lambdaQuery()
                .eq(RegionGroupMember::getRegionGroupId, regionGroup.getId())
                .eq(RegionGroupMember::getUserId, session.getUserId())
                .eq(RegionGroupMember::getQuit, false).list();
        if (CollectionUtils.isNotEmpty(list)) {
            throw new GlobalException("您已加入当前地区群聊");
        }

        regionGroupMemberService.saveOrUpdate(regionGroupMember);

        // 删除redis保存的用户临时加入的地区群聊到
        redisCache.deleteObject(IMRedisKey.IM_USER_TEMP_REGION_GROUP + session.getUserId() + ":" + regionGroupDTO.getCode());

        // 删除redis保存的地区群聊对应编号的临时用户数据
        redisCache.deleteObject(IMRedisKey.IM_REGION_GROUP_NUM_TEMP_USER
                + regionGroup.getCode() + ":" + regionGroup.getNum() + ":" + session.getUserId());

        String content = "用户【" + session.getNickName() + "】加入了群聊";
        messageSendUtil.sendRegionGroupTipMsg(regionGroup.getId(), session.getUserId(), session.getNickName(), null, content, GroupChangeTypeEnum.NEW_USER_JOIN.getCode());
    }

    private RegionGroup getTempJoinRegionGroupNum(Region region, Long userId) {
        LambdaQueryWrapper<RegionGroup> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(RegionGroup::getCode, region.getCode());
        queryWrapper.eq(RegionGroup::getDeleted, false);

        List<RegionGroup> regionGroupList = this.list(queryWrapper);

        // 为空代表当前地区没有创建群聊
        if (CollectionUtils.isEmpty(regionGroupList)) {
            RegionGroup regionGroup = new RegionGroup();
            regionGroup.setCode(region.getCode());
            regionGroup.setNum(Constant.ROME_NUM[0]);
            regionGroup.setRegionGroupName(generateRegionGroupName(region) + "-" + regionGroup.getNum());
            regionGroup.setRemark(region.getName() + "-" + regionGroup.getNum());
            regionGroup.setCreateBy(userId);
            return regionGroup;
        }

        // 已存在的地区群聊编号
        List<String> existsNum = new ArrayList<>();

        // 不为空判断要加入的地区群聊临时用户不超过20的群聊，获取第一个
        for (RegionGroup regionGroup : regionGroupList) {
            // 判断用户是否在常驻名单里
            RegionGroupMember regionGroupMember = regionGroupMemberService
                    .findByRegionGroupIdAndUserId(regionGroup.getId(), userId);
            if (ObjectUtil.isNotNull(regionGroupMember) && !regionGroupMember.getQuit()) {
                throw new GlobalException("您已存在当前地区的常驻用户中");
            }

            existsNum.add(regionGroup.getNum());
            Collection<String> keys = redisCache.keys(IMRedisKey.IM_REGION_GROUP_NUM_TEMP_USER
                    + regionGroup.getCode() + ":" + regionGroup.getNum() + ":*");
            if (keys.size() >= RegionGroupConst.MAX_REGION_GROUP_TEMP_MEMBER_COUNT) {
                continue;
            }
            return regionGroup;
        }

        // 执行到此处说明所有已存在的地区群聊编号临时用户已满，判断当前地区是否还可以创建地区群聊
        for (String num : Constant.ROME_NUM) {
            if (existsNum.contains(num)) {
                continue;
            }
            RegionGroup regionGroup = new RegionGroup();
            regionGroup.setNum(num);
            regionGroup.setCode(region.getCode());
            regionGroup.setRegionGroupName(generateRegionGroupName(region) + "-" + regionGroup.getNum());
            regionGroup.setRemark(region.getName() + "-" + regionGroup.getNum());
            regionGroup.setCreateBy(userId);
            return regionGroup;
        }

        throw new GlobalException("当前地区临时用户已满，不能再加入");
    }

    private String generateRegionGroupName(Region region) {
        List<Region> parentRegions = regionService.findAllParentRegionByParentCode(region.getParentCode());
        if (CollectionUtils.isEmpty(parentRegions)) {
            return region.getName();
        }
        parentRegions.sort((Comparator.comparingInt(Region::getLevel)));
        parentRegions.add(region);

        return parentRegions.stream().map(Region::getName).collect(Collectors.joining("->"));
    }

    @Lock(prefix = "im:region:group:modify", key = "#regionGroupDTO.getId()")
    @Override
    public void quitRegionGroup(RegionGroupDTO regionGroupDTO) {
        if (ObjectUtil.isNull(regionGroupDTO.getId())
            || !Arrays.asList(Constant.ROME_NUM).contains(regionGroupDTO.getNum())) {
            throw new GlobalException("参数异常");
        }
        UserSession session = SessionContext.getSession();
        if (regionGroupDTO.getJoinType() == 0) {
            quitTempRegionGroup(regionGroupDTO, session.getUserId());
        } else if (regionGroupDTO.getJoinType() == 1) {
            quitPermanentRegionGroup(regionGroupDTO, session.getUserId());
        }
    }

    @Lock(prefix = "im:region:group:modify", key = "#dto.getRegionGroupId()")
    @Override
    public void voteRegionGroupLeader(RegionGroupMemberDTO dto) {
        if (ObjectUtil.isAllEmpty(dto.getRegionGroupId(), dto.getUserId())) {
            throw new GlobalException("操作异常");
        }
        UserSession session = SessionContext.getSession();

        // 查询当前地区群聊
        RegionGroup regionGroup = this.getById(dto.getRegionGroupId());

        // 判断当前群聊是否可投票选择群主
        if (regionGroup == null || regionGroup.getDeleted()) {
            throw new GlobalException("当前地区群聊不存在");
        }

        Region region = regionService.getById(regionGroup.getCode());
        if (ObjectUtil.isNull(region) || region.getDeleted()) {
            throw new GlobalException("地区不存在");
        }

        // 判断当前用户是否在24小时内在当前地区群聊投票过
        Object object = redisCache.getCacheObject(IMRedisKey.IM_REGION_GROUP_LEADER_VOTE_RECORD + regionGroup.getId() + ":" + session.getUserId());
        if (ObjectUtil.isNotNull(object)) {
            throw new GlobalException("您在" + RegionGroupConst.VOTE_EXPIRE_TIME + "小时内已有过投票");
        }

        Date now = new Date();
        // 当前群聊已有群主并且还未过失效时间
        if (regionGroup.getLeaderId() != null && now.before(regionGroup.getExpirationTime())) {
            throw new GlobalException("当前地区群聊已有群主，可投票解除群主或等待群主有效期结束");
        }

        // 判断用户是否常驻成员
        RegionGroupMember myMemerInfo = regionGroupMemberService.findByRegionGroupIdAndUserId(dto.getRegionGroupId(), session.getUserId());
        if (ObjectUtil.isNull(myMemerInfo) || myMemerInfo.getQuit()) {
            throw new GlobalException("您不是当前地区群聊常驻用户");
        }
        // 判断当前用户加入当前地区群聊日期距离当前时间是否超过指定时间
        long between2 = DateUtil.between(myMemerInfo.getCreateTime(), now, DateUnit.DAY);
        if (between2 < RegionGroupConst.ENABLE_LEADER_TIME) {
            throw new GlobalException("您加入群聊时间未超过【" + RegionGroupConst.ENABLE_LEADER_TIME + "】天，不能参与投票");
        }

        RegionGroupMember regionGroupMember = regionGroupMemberService.findByRegionGroupIdAndUserId(dto.getRegionGroupId(), dto.getUserId());
        if (ObjectUtil.isNull(regionGroupMember) || regionGroupMember.getQuit()) {
            throw new GlobalException("您所投票的用户不在当前地区群聊里");
        }
        // 判断投票的用户加入当前地区群聊日期距离当前时间是否超过指定时间
        long between = DateUtil.between(regionGroupMember.getCreateTime(), now, DateUnit.DAY);
        if (between < RegionGroupConst.ENABLE_LEADER_TIME) {
            throw new GlobalException("您所投票的用户加入群聊时间未超过【" + RegionGroupConst.ENABLE_LEADER_TIME + "】天");
        }

        // 判断投票用户数据是否存在
        Boolean exists = redisCache.hasKey(IMRedisKey.IM_REGION_GROUP_LEADER_VOTE + regionGroup.getId() + ":" + dto.getUserId());

        // 记录所投票的用户的票数，24小时有效
        int count = redisCache.incrementInt(IMRedisKey.IM_REGION_GROUP_LEADER_VOTE + regionGroup.getId() + ":" + dto.getUserId());
        if (!exists) {
            redisCache.expire(IMRedisKey.IM_REGION_GROUP_LEADER_VOTE + regionGroup.getId() + ":" + dto.getUserId(), RegionGroupConst.VOTE_EXPIRE_TIME, TimeUnit.HOURS);
        }

        // 记录当前用户投票记录（24小时内同一个地区群聊不可以多次投票）
        redisCache.setCacheObject(IMRedisKey.IM_REGION_GROUP_LEADER_VOTE_RECORD + regionGroup.getId() + ":" + session.getUserId(),
                dto.getUserId(), RegionGroupConst.VOTE_EXPIRE_TIME, TimeUnit.HOURS);

        Integer voteEffectNum = LeaderVoteNumEnum.getCountByLevel(region.getLevel());

        // 投票操作是否通知群成员
        if (dto.getAnnounce()) {
            // 判断投票给自己还是其他成员
            String content = null;
            if (session.getUserId().equals(dto.getUserId())) {
                content = "【群主投票】用户【" + myMemerInfo.getAliasName() + "】自荐申请群主身份。总计获得票数：" + count + "。距生效票数还差" + (voteEffectNum - count) + "票";
            } else {
                content = "【群主投票】用户【" + myMemerInfo.getAliasName() + "】将票投给了【" + regionGroupMember.getAliasName() + "】。总计获得票数：" + count + "。距生效票数还差" + (voteEffectNum - count) + "票";
            }
            // 通知群成员
            messageSendUtil.sendRegionGroupTipMsg(dto.getRegionGroupId(), dto.getUserId(),
                    regionGroupMember.getAliasName(), Collections.emptyList(), content, null);
        } else {
            String content = "【群主投票】用户【" + regionGroupMember.getAliasName() + "】当前总计获得票数：" + count+ "。距生效票数还差" + (voteEffectNum - count) + "票";
            // 通知群成员
            messageSendUtil.sendRegionGroupTipMsg(dto.getRegionGroupId(), dto.getUserId(),
                    regionGroupMember.getAliasName(), Collections.emptyList(), content, null);
        }

        // 判断票数是否超过所需票数
        if (count >= voteEffectNum) {
            final RLock lock = redissonClient.getLock(RedisKey.IM_REGION_GROUP_LEADER_LOCK + dto.getUserId());
            try {
                lock.lock(10L, TimeUnit.SECONDS);
                // 判断投票的用户是否在其他地区群聊成为群主
                if (this.hasBecomeRegionGroupLeader(dto.getUserId())) {
                    throw new GlobalException("您所投票的用户已是其他地区群聊群主");
                }
                // 将所投票用户选为群主
                regionGroup.setLeaderId(dto.getUserId());
                regionGroup.setEffectiveTime(now);
                // 有效期30天
                DateTime expirationTime = DateUtil.offsetDay(now, RegionGroupConst.REGION_LEADER_PERIOD);
                regionGroup.setExpirationTime(expirationTime);

                // 更新群信息
                this.updateById(regionGroup);

                String content = "用户【" + regionGroupMember.getAliasName() + "】已被选为群主，有效期至"+ DateUtil.format(regionGroup.getExpirationTime(), DateFormat.DATE_FORMAT_01) + "，将开启群管理功能";

                // 通知群成员
                messageSendUtil.sendRegionGroupTipMsg(dto.getRegionGroupId(), dto.getUserId(),
                        regionGroupMember.getAliasName(), Collections.emptyList(), content, GroupChangeTypeEnum.GROUP_MEMBER_CHANGE.getCode());
            } finally {
                lock.unlock();
            }
        }
    }

    @Lock(prefix = "im:region:group:modify", key = "#dto.getRegionGroupId()")
    @Override
    public void voteRemoveRegionGroupLeader(RegionGroupMemberDTO dto) {
        if (ObjectUtil.isAllEmpty(dto.getRegionGroupId(), dto.getUserId())) {
            throw new GlobalException("操作异常");
        }
        UserSession session = SessionContext.getSession();

        // 查询当前地区群聊
        RegionGroup regionGroup = this.getById(dto.getRegionGroupId());

        // 判断当前群聊是否可投票选择群主
        if (regionGroup == null || regionGroup.getDeleted()) {
            throw new GlobalException("当前地区群聊不存在");
        }

        Region region = regionService.getById(regionGroup.getCode());
        if (ObjectUtil.isNull(region) || region.getDeleted()) {
            throw new GlobalException("当前地区不存在");
        }

        // 判断当前用户是否在24小时内在当前地区群聊投票过
        Object object = redisCache.getCacheObject(IMRedisKey.IM_REGION_GROUP_LEADER_VOTE_REMOVE_RECORD + regionGroup.getId() + ":" + session.getUserId());
        if (ObjectUtil.isNotNull(object)) {
            throw new GlobalException("您在" + RegionGroupConst.VOTE_EXPIRE_TIME + "小时内已有过投票");
        }

        Date now = new Date();
        // 当前群聊没有群主或者群主身份已过失效时间
        if (regionGroup.getLeaderId() == null || now.after(regionGroup.getExpirationTime())) {
            throw new GlobalException("当前地区群聊没有群主或群主身份已过期");
        }
        
        // 判断当前时间距离群主生效时间是否超过指定时间
        long between = DateUtil.between(regionGroup.getEffectiveTime(), now, DateUnit.DAY);
        if (between < RegionGroupConst.MIN_LEADER_DURATION) {
            throw new GlobalException("距离当前群主身份生效时长未超过" + RegionGroupConst.MIN_LEADER_DURATION + "天，不能投票解除");
        }

        // 判断用户是否常驻成员
        RegionGroupMember myMemerInfo = regionGroupMemberService.findByRegionGroupIdAndUserId(dto.getRegionGroupId(), session.getUserId());
        if (ObjectUtil.isNull(myMemerInfo) || myMemerInfo.getQuit()) {
            throw new GlobalException("您不在当前地区群聊里");
        }

        // 判断当前用户加入当前地区群聊日期距离当前时间是否超过指定时间
        long between2 = DateUtil.between(myMemerInfo.getCreateTime(), now, DateUnit.DAY);
        if (between2 < RegionGroupConst.ENABLE_LEADER_TIME) {
            throw new GlobalException("您加入群聊时间未超过【" + RegionGroupConst.ENABLE_LEADER_TIME + "】天，不能参与投票");
        }

        RegionGroupMember regionGroupMember = regionGroupMemberService.findByRegionGroupIdAndUserId(dto.getRegionGroupId(), dto.getUserId());
        if (ObjectUtil.isNull(regionGroupMember) || regionGroupMember.getQuit()) {
            throw new GlobalException("您所投票的用户不在当前地区群聊里");
        }
        // 判断投票解除的用户是否群主
        if (!dto.getUserId().equals(regionGroup.getLeaderId())) {
            throw new GlobalException("您所选择的用户不是群主");
        }

        // 判断投票用户数据是否存在
        Boolean exists = redisCache.hasKey(IMRedisKey.IM_REGION_GROUP_LEADER_VOTE_REMOVE + regionGroup.getId() + ":" + dto.getUserId());

        // 记录所投票的用户的票数，24小时有效
        int count = redisCache.incrementInt(IMRedisKey.IM_REGION_GROUP_LEADER_VOTE_REMOVE + regionGroup.getId() + ":" + dto.getUserId());
        if (!exists) {
            redisCache.expire(IMRedisKey.IM_REGION_GROUP_LEADER_VOTE_REMOVE + regionGroup.getId() + ":" + dto.getUserId(), RegionGroupConst.VOTE_EXPIRE_TIME, TimeUnit.HOURS);
        }

        // 记录当前用户投票记录（24小时内同一个地区群聊不可以多次投票）
        redisCache.setCacheObject(IMRedisKey.IM_REGION_GROUP_LEADER_VOTE_REMOVE_RECORD + regionGroup.getId() + ":" + session.getUserId(),
                dto.getUserId(), RegionGroupConst.VOTE_EXPIRE_TIME, TimeUnit.HOURS);

        Integer voteEffectNum = LeaderVoteNumEnum.getCountByLevel(region.getLevel());

        // 投票操作是否通知群成员
        if (dto.getAnnounce()) {
            String content = "【群主解除投票】用户【" + myMemerInfo.getAliasName() + "】投出了群主解除票。总计票数：" + count + "。距生效票数还差" + (voteEffectNum - count) + "票";
            // 通知群成员
            messageSendUtil.sendRegionGroupTipMsg(dto.getRegionGroupId(), dto.getUserId(),
                    regionGroupMember.getAliasName(), Collections.emptyList(), content, null);
        } else {
            String content = "【群主解除投票】总计票数：" + count + "。距生效票数还差" + (voteEffectNum - count) + "票";
            // 通知群成员
            messageSendUtil.sendRegionGroupTipMsg(dto.getRegionGroupId(), dto.getUserId(),
                    regionGroupMember.getAliasName(), Collections.emptyList(), content, null);
        }

        // 判断票数是否超过所需票数
        if (count >= voteEffectNum) {
            // 将群主信息置空
            LambdaUpdateWrapper<RegionGroup> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.set(RegionGroup::getLeaderId, null);
            updateWrapper.set(RegionGroup::getEffectiveTime, null);
            updateWrapper.set(RegionGroup::getExpirationTime, null);
            updateWrapper.eq(RegionGroup::getId, regionGroup.getId());
            if (regionGroup.getIsBanned() && BanTypeEnum.MASTER.getCode().equals(regionGroup.getBanType())) {
                updateWrapper.set(RegionGroup::getIsBanned, false);
                updateWrapper.set(RegionGroup::getBanType, null);
                updateWrapper.set(RegionGroup::getBanExpireTime, null);
            }

            // 更新群信息
            this.update(updateWrapper);

            // 更新常驻成员被群主禁言的
            LambdaUpdateWrapper<RegionGroupMember> wrapper = new LambdaUpdateWrapper<>();
            wrapper.set(RegionGroupMember::getIsBanned, false);
            wrapper.set(RegionGroupMember::getBanType, null);
            wrapper.set(RegionGroupMember::getBanExpireTime, null);

            wrapper.eq(RegionGroupMember::getRegionGroupId, regionGroup.getId());
            wrapper.eq(RegionGroupMember::getBanType, BanTypeEnum.MASTER.getCode());
            regionGroupMemberService.update(wrapper);

            Collection<String> keys = redisCache.keys(RedisKey.IM_REGION_GROUP_MEMBER_MSG_SWITCH + regionGroup.getId() + ":*");
            if (CollectionUtils.isNotEmpty(keys)) {
                keys.forEach(key -> {
                    Object cacheObject = redisCache.getCacheObject(key);
                    if (ObjectUtil.isNotNull(cacheObject)) {
                        if (BanTypeEnum.MASTER.getCode().equals(cacheObject.toString())){
                            redisCache.deleteObject(key);
                        }
                    }
                });
            }

            String content = "用户【" + regionGroupMember.getAliasName() + "】已被解除群主身份。因无群主管理，每位群成员"+ RegionGroupConst.MSG_LIMIT_TIME +"小时内发言数量限制" + RegionGroupConst.MSG_LIMIT_COUNT + "条。";

            // 通知群成员
            messageSendUtil.sendRegionGroupTipMsg(dto.getRegionGroupId(), dto.getUserId(),
                    regionGroupMember.getAliasName(), Collections.emptyList(), content, GroupChangeTypeEnum.GROUP_MEMBER_CHANGE.getCode());
        }
    }

    @Lock(prefix = "im:region:group:modify", key = "#dto.getId()")
    @Override
    public void banMsg(RegionGroupBanDTO dto) {
        UserSession session = SessionContext.getSession();
        RegionGroup regionGroup = this.getById(dto.getId());

        Date now = new Date();
        // 判断用户是否群主或系统管理员
        if (!session.getUserId().equals(Constant.ADMIN_USER_ID) && !isRegionGroupLeader(session.getUserId(), regionGroup)) {
            throw new GlobalException("您不是群主或系统管理员，无权限操作");
        }
        if (!StringUtils.equalsAny(dto.getBanType(), BanTypeEnum.SYS.getCode(), BanTypeEnum.MASTER.getCode())) {
            throw new GlobalException("禁言类型错误");
        }

        // 全局禁言
        if (dto.getAllBanned()) {
            regionGroup.setIsBanned(true);
            regionGroup.setBanType(dto.getBanType());
            if (ObjectUtil.isNotNull(dto.getBanDuration()) && dto.getBanDuration() > 0) {
                DateTime dateTime = DateUtil.offsetHour(now, dto.getBanDuration());
                regionGroup.setBanExpireTime(dateTime);
            }
            this.updateById(regionGroup);
            String content = null;
            if (session.getUserId().equals(Constant.ADMIN_USER_ID)) {
                content = "当前群聊已被系统禁言，禁言时长" + dto.getBanDuration() + "小时";
            } else {
                content = "群主已开启全员禁言，禁言时长" + dto.getBanDuration() + "小时";
            }

            messageSendUtil.sendRegionGroupTipMsg(regionGroup.getId(), session.getUserId(), session.getNickName(), null, content, null);
        } else {
            if (ObjectUtil.isNull(dto.getUserId())) {
                throw new GlobalException("未选择要禁言的用户");
            }
            // 常驻成员禁言
            if (dto.getJoinType() == 1) {
                RegionGroupMember regionGroupMember = regionGroupMemberService.findByRegionGroupIdAndUserId(regionGroup.getId(), dto.getUserId());
                if (ObjectUtil.isNull(regionGroupMember)) {
                    throw new GlobalException("被禁言用户不在群聊中");
                }

                regionGroupMember.setIsBanned(true);
                regionGroupMember.setBanType(dto.getBanType());
                if (ObjectUtil.isNotNull(dto.getBanDuration()) && dto.getBanDuration() > 0) {
                    DateTime dateTime = DateUtil.offsetHour(now, dto.getBanDuration());
                    regionGroupMember.setBanExpireTime(dateTime);
                }
                regionGroupMemberService.updateById(regionGroupMember);
            } else if (dto.getJoinType() == 0) {
                if (ObjectUtil.isNotNull(dto.getBanDuration()) && dto.getBanDuration() > 0) {
                    redisCache.setCacheObject(RedisKey.IM_REGION_GROUP_MEMBER_MSG_SWITCH + regionGroup.getId() + ":" + dto.getUserId(),
                            dto.getBanType(), dto.getBanDuration(), TimeUnit.HOURS);
                }
            }

            String content = null;
            if (session.getUserId().equals(Constant.ADMIN_USER_ID)) {
                content = "群成员【"+ dto.getAliasName() + "】已被系统禁言，禁言时长" + dto.getBanDuration() + "小时";
            } else {
                content = "群成员【"+ dto.getAliasName() + "】已被群主禁言，禁言时长" + dto.getBanDuration() + "小时";
            }

            messageSendUtil.sendRegionGroupTipMsg(regionGroup.getId(), session.getUserId(), session.getNickName(), null, content, null);
        }
    }

    @Lock(prefix = "im:region:group:modify", key = "#dto.getId()")
    @Override
    public void unBanMsg(RegionGroupBanDTO dto) {
        UserSession session = SessionContext.getSession();
        RegionGroup regionGroup = this.getById(dto.getId());

        // 判断用户是否群主或系统管理员
        if (!session.getUserId().equals(Constant.ADMIN_USER_ID) && !isRegionGroupLeader(session.getUserId(), regionGroup)) {
            throw new GlobalException("您不是群主或系统管理员，无权限操作");
        }

        // 判断是解禁全局禁言还是解禁用户
        if (ObjectUtil.isNull(dto.getUserId())) {
            if (!regionGroup.getIsBanned()) {
                throw new GlobalException("当前地区群聊未开启全员禁言");
            }
            // 全局禁言解除，判断被禁言类型
            if (BanTypeEnum.SYS.getCode().equals(regionGroup.getBanType())
                    && !session.getUserId().equals(Constant.ADMIN_USER_ID)) {
                throw new GlobalException("当前地区群聊被系统禁言，您无权限解除！");
            }

            LambdaUpdateWrapper<RegionGroup> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.set(RegionGroup::getIsBanned, false);
            updateWrapper.set(RegionGroup::getBanType, null);
            updateWrapper.set(RegionGroup::getBanExpireTime, null);
            updateWrapper.eq(RegionGroup::getId, regionGroup.getId());

            this.update(updateWrapper);

            String content = "当前地区群聊已解除全员禁言";
            messageSendUtil.sendRegionGroupTipMsg(regionGroup.getId(), session.getUserId(), session.getNickName(), null, content, null);
        } else {
            // 判断是解禁临时还是常驻用户
            if (dto.getJoinType() == 1) {
                RegionGroupMember regionGroupMember = regionGroupMemberService.findByRegionGroupIdAndUserId(regionGroup.getId(), dto.getUserId());
                if (ObjectUtil.isNull(regionGroupMember)) {
                    throw new GlobalException("用户不在当前地区群聊中");
                }
                if (!regionGroupMember.getIsBanned()) {
                    throw new GlobalException("用户未被禁言");
                }

                if (BanTypeEnum.SYS.getCode().equals(regionGroupMember.getBanType())
                        && !session.getUserId().equals(Constant.ADMIN_USER_ID)) {
                    throw new GlobalException("用户被系统禁言，您无权限解除！");
                }
                LambdaUpdateWrapper<RegionGroupMember> updateWrapper = new LambdaUpdateWrapper<>();
                updateWrapper.set(RegionGroupMember::getIsBanned, false);
                updateWrapper.set(RegionGroupMember::getBanType, null);
                updateWrapper.set(RegionGroupMember::getBanExpireTime, null);
                updateWrapper.eq(RegionGroupMember::getId, regionGroupMember.getId());

                regionGroupMemberService.update(updateWrapper);
            } else if (dto.getJoinType() == 0) {
                Object object = redisCache.getCacheObject(RedisKey.IM_REGION_GROUP_MEMBER_MSG_SWITCH + regionGroup.getId() + ":" + dto.getUserId());

                if (ObjectUtil.isNull(object)) {
                    throw new GlobalException("用户未被禁言");
                }

                if (BanTypeEnum.SYS.getCode().equals(object.toString())
                        && !session.getUserId().equals(Constant.ADMIN_USER_ID)) {
                    throw new GlobalException("用户被系统禁言，您无权限解除！");
                }

                redisCache.deleteObject(RedisKey.IM_REGION_GROUP_MEMBER_MSG_SWITCH + regionGroup.getId() + ":" + dto.getUserId());
            }

            String content = "群成员【" + dto.getAliasName() + "】已解除禁言";
            messageSendUtil.sendRegionGroupTipMsg(regionGroup.getId(), session.getUserId(), session.getNickName(), null, content, null);
        }
    }

    @Lock(prefix = "im:region:group:modify", key = "#dto.getRegionGroupId()")
    @Override
    public void leaderTransfer(RegionGroupMemberDTO dto) {
        UserSession session = SessionContext.getSession();
        if (ObjectUtil.isNull(dto.getRegionGroupId()) || ObjectUtil.isNull(dto.getUserId())) {
            throw new GlobalException("参数异常");
        }

        // 判断当前用户是否群主
        RegionGroup regionGroup = this.getById(dto.getRegionGroupId());
        if (!isRegionGroupLeader(session.getUserId(), regionGroup)) {
            throw new GlobalException("您不是当前地区群聊群主");
        }

        Boolean exists = redisCache.hasKey(RedisKey.IM_REGION_GROUP_LEADER_TRANSFER + regionGroup.getCode() + "-" + regionGroup.getNum());
        if (exists) {
            throw new GlobalException("距离上次群主转移时间未超过" + RegionGroupConst.LEADER_TRANSFER_TIME + "天。");
        }

        // 判断所选用户是否常驻成员，并且加入的时间是否超过指定天数
        RegionGroupMember regionGroupMember = regionGroupMemberService.findByRegionGroupIdAndUserId(regionGroup.getId(), dto.getUserId());
        if (ObjectUtil.isNull(regionGroupMember) || regionGroupMember.getQuit()) {
            throw new GlobalException("所选用户不是当前地区群聊常驻成员");
        }
        long between = DateUtil.between(regionGroupMember.getCreateTime(), new Date(), DateUnit.DAY);
        if (between < RegionGroupConst.ENABLE_LEADER_TIME) {
            throw new GlobalException("您所选择的用户加入群聊时间未超过【" + RegionGroupConst.ENABLE_LEADER_TIME + "】天");
        }

        final RLock lock = redissonClient.getLock(RedisKey.IM_REGION_GROUP_LEADER_LOCK + dto.getUserId());
        try {
            lock.lock(10L, TimeUnit.SECONDS);
            // 判断所选用户是否在其他地区群聊成为群主
            if (this.hasBecomeRegionGroupLeader(dto.getUserId())) {
                throw new GlobalException("您所选择的用户已是其他地区群聊群主");
            }
            regionGroup.setLeaderId(dto.getUserId());
            this.updateById(regionGroup);

            // 记录群主转移操作
            redisCache.setCacheObject(RedisKey.IM_REGION_GROUP_LEADER_TRANSFER + regionGroup.getCode() + "-" + regionGroup.getNum(),
                    session.getUserId(), RegionGroupConst.LEADER_TRANSFER_TIME, TimeUnit.DAYS);

            String content = "群主已转移到群成员【" + regionGroupMember.getAliasName() + "】，有效期至：" + DateUtil.format(regionGroup.getExpirationTime(), DateFormat.DATE_FORMAT_01);
            messageSendUtil.sendRegionGroupTipMsg(regionGroup.getId(), session.getUserId(), session.getNickName(),
                    null, content, GroupChangeTypeEnum.GROUP_MEMBER_CHANGE.getCode());
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean hasBecomeRegionGroupLeader(Long userId) {
        List<RegionGroup> regionGroupList = this.lambdaQuery().eq(RegionGroup::getLeaderId, userId)
                .gt(RegionGroup::getExpirationTime, new Date())
                .list();
        if (CollectionUtils.isNotEmpty(regionGroupList)) {
            return true;
        }

        return false;
    }

    private boolean isRegionGroupLeader(Long userId, RegionGroup regionGroup) {
        return (userId.equals(regionGroup.getLeaderId()) && new Date().before(regionGroup.getExpirationTime()));
    }

    private void quitTempRegionGroup(RegionGroupDTO regionGroupDTO, Long userId) {
        // 直接根据key删除redis数据

        // 删除redis保存的用户临时加入的地区群聊到
        redisCache.deleteObject(IMRedisKey.IM_USER_TEMP_REGION_GROUP + userId + ":" + regionGroupDTO.getCode());

        // 删除redis保存的地区群聊对应编号的临时用户数据
        redisCache.deleteObject(IMRedisKey.IM_REGION_GROUP_NUM_TEMP_USER
                + regionGroupDTO.getCode() + ":" + regionGroupDTO.getNum() + ":" + userId);
    }

    private void quitPermanentRegionGroup(RegionGroupDTO regionGroupDTO, Long userId) {
        // 查询用户常驻地区群聊信息
        RegionGroupMember regionGroupMember = regionGroupMemberService.findByRegionGroupIdAndUserId(regionGroupDTO.getId(), userId);
        if (ObjectUtil.isNull(regionGroupMember)) {
            return;
        }
        RegionGroup regionGroup = this.getById(regionGroupMember.getRegionGroupId());

        regionGroupMember.setQuit(true);
        regionGroupMemberService.updateById(regionGroupMember);


        if (isRegionGroupLeader(regionGroupMember.getUserId(), regionGroup)) {
            // 将群主信息置空
            LambdaUpdateWrapper<RegionGroup> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.set(RegionGroup::getLeaderId, null);
            updateWrapper.set(RegionGroup::getEffectiveTime, null);
            updateWrapper.set(RegionGroup::getExpirationTime, null);
            if (regionGroup.getIsBanned() && BanTypeEnum.MASTER.getCode().equals(regionGroup.getBanType())) {
                updateWrapper.set(RegionGroup::getIsBanned, false);
                updateWrapper.set(RegionGroup::getBanType, null);
                updateWrapper.set(RegionGroup::getBanExpireTime, null);
            }
            updateWrapper.eq(RegionGroup::getId, regionGroup.getId());

            // 更新群信息
            this.update(updateWrapper);

            // 更新常驻成员被群主禁言的
            LambdaUpdateWrapper<RegionGroupMember> wrapper = new LambdaUpdateWrapper<>();
            wrapper.set(RegionGroupMember::getIsBanned, false);
            wrapper.set(RegionGroupMember::getBanType, null);
            wrapper.set(RegionGroupMember::getBanExpireTime, null);

            wrapper.eq(RegionGroupMember::getRegionGroupId, regionGroup.getId());
            wrapper.eq(RegionGroupMember::getBanType, BanTypeEnum.MASTER.getCode());
            regionGroupMemberService.update(wrapper);

            Collection<String> keys = redisCache.keys(RedisKey.IM_REGION_GROUP_MEMBER_MSG_SWITCH + regionGroup.getId() + ":*");
            if (CollectionUtils.isNotEmpty(keys)) {
                keys.forEach(key -> {
                    Object cacheObject = redisCache.getCacheObject(key);
                    if (ObjectUtil.isNotNull(cacheObject)) {
                        if (BanTypeEnum.MASTER.getCode().equals(cacheObject.toString())){
                            redisCache.deleteObject(key);
                        }
                    }
                });
            }

            String content = "群主【" + regionGroupMember.getAliasName() + "】已退出群聊，因无群主管理，每位群成员"+ RegionGroupConst.MSG_LIMIT_TIME +"小时内发言数量限制" + RegionGroupConst.MSG_LIMIT_COUNT + "条。";
            messageSendUtil.sendRegionGroupTipMsg(regionGroup.getId(), userId, regionGroupMember.getAliasName(), null, content, GroupChangeTypeEnum.GROUP_MEMBER_CHANGE.getCode());
        }
    }
}

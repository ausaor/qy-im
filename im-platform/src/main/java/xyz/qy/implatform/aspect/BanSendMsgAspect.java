package xyz.qy.implatform.aspect;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import xyz.qy.imclient.annotation.BanSendMsg;
import xyz.qy.imcommon.contant.IMRedisKey;
import xyz.qy.implatform.contant.Constant;
import xyz.qy.implatform.contant.RedisKey;
import xyz.qy.implatform.dto.GroupMessageDTO;
import xyz.qy.implatform.dto.RegionGroupMessageDTO;
import xyz.qy.implatform.entity.Group;
import xyz.qy.implatform.entity.GroupMember;
import xyz.qy.implatform.entity.Region;
import xyz.qy.implatform.entity.RegionGroup;
import xyz.qy.implatform.entity.RegionGroupMember;
import xyz.qy.implatform.enums.BanTypeEnum;
import xyz.qy.implatform.exception.GlobalException;
import xyz.qy.implatform.service.IGroupMemberService;
import xyz.qy.implatform.service.IGroupService;
import xyz.qy.implatform.service.IRegionGroupMemberService;
import xyz.qy.implatform.service.IRegionGroupService;
import xyz.qy.implatform.service.IRegionService;
import xyz.qy.implatform.session.SessionContext;
import xyz.qy.implatform.session.UserSession;
import xyz.qy.implatform.util.DateTimeUtils;
import xyz.qy.implatform.util.RedisCache;

import javax.annotation.Resource;
import java.lang.reflect.Method;
import java.util.Date;

/**
 * 判断是否禁言切面
 *
 * @author Polaris
 * @since 2024-10-21
 */
@Slf4j
@Aspect
@Order(80)
@Component
public class BanSendMsgAspect {

    @Resource
    private RedisCache redisCache;

    @Resource
    private IGroupService groupService;

    @Resource
    IGroupMemberService groupMemberService;

    @Resource
    private IRegionGroupService regionGroupService;

    @Resource
    private IRegionGroupMemberService regionGroupMemberService;

    @Resource
    private IRegionService regionService;

    @Pointcut("@annotation(xyz.qy.imclient.annotation.BanSendMsg)")
    public void banSendMagAspect() {
    }

    @Around("banSendMagAspect()")
    public Object aspectBefore(ProceedingJoinPoint point) throws Throwable {
        UserSession session = SessionContext.getSession();
        Long userId = session.getUserId();

        // 网站管理员无需校验
        if (userId.equals(Constant.ADMIN_USER_ID)) {
            return point.proceed();
        }

        // 1、判断管理员是否禁止网站聊天功能
        systemMsgCheck();

        // 2、校验用户是否被禁止聊天功能
        userMsgCheck(userId);

        Class<?> clazz = point.getTarget().getClass();
        MethodSignature methodSignature = (MethodSignature) point.getSignature();
        Method method = clazz.getDeclaredMethod(methodSignature.getName(), methodSignature.getParameterTypes());
        BanSendMsg banSendMsg = method.getAnnotation(BanSendMsg.class);
        String msgType = banSendMsg.msgType();
        if (StringUtils.isBlank(msgType)) {
            return point.proceed();
        }

        Object[] args = point.getArgs();
        if (args == null || args.length == 0) {
            throw new GlobalException("参数异常");
        }
        Object arg = args[0];
        if (arg instanceof GroupMessageDTO) {
            log.info("群聊消息校验");
            GroupMessageDTO groupMessageDTO = Convert.convert(GroupMessageDTO.class, arg);
            groupMsgCheck(groupMessageDTO.getGroupId(), userId);
        } else if (arg instanceof RegionGroupMessageDTO) {
            log.info("地区群聊消息校验");
            RegionGroupMessageDTO groupMessageDTO = Convert.convert(RegionGroupMessageDTO.class, arg);
            regionGroupMsgCheck(groupMessageDTO.getRegionGroupId(), userId);
        }

        return point.proceed();
    }

    private void systemMsgCheck() {
        Boolean exists = redisCache.hasKey(RedisKey.IM_SYSTEM_MSG_SWITCH);
        if (exists) {
            long expire = redisCache.getExpire(RedisKey.IM_SYSTEM_MSG_SWITCH);
            String msg = null;
            if (expire == -1) {
                msg = "网站聊天功能目前无法使用";
            } else {
                msg = "网站聊天功能目前无法使用，请在" + DateTimeUtils.getTimeValueDesc(expire) + "后再尝试";
            }
            throw new GlobalException(msg);
        }
    }

    private void userMsgCheck(Long userId) {
        Boolean exists = redisCache.hasKey(RedisKey.IM_USER_MSG_SWITCH + userId);
        if (exists) {
            long expire = redisCache.getExpire(RedisKey.IM_USER_MSG_SWITCH + userId);
            String msg = null;
            if (expire == -1) {
                msg = "您已被禁止发送聊天信息";
            } else {
                msg = "您已被禁止发送聊天信息，请在" + DateTimeUtils.getTimeValueDesc(expire) + "后再尝试";
            }
            throw new GlobalException(msg);
        }
    }

    private void groupMsgCheck(Long groupId, Long userId) {
        Group group = groupService.getById(groupId);
        if (group == null) {
            throw new GlobalException("当前群聊不存在");
        }
        if (group.getIsBanned()) {
            Date now = new Date();
            // 被系统禁止
            if (BanTypeEnum.SYS.getCode().equals(group.getBanType())) {
                String msg = null;
                if (group.getBanExpireTime() == null) {
                    msg = "当前群聊已被系统禁止发送消息";
                } else if (now.before(group.getBanExpireTime())) {
                    long between = DateUtil.between(now, group.getBanExpireTime(), DateUnit.SECOND);
                    msg = "当前群聊已被系统禁止发送消息，请在" + DateTimeUtils.getTimeValueDesc(between) + "后再尝试";
                } else {
                    // 已过系统禁止时间
                    group.setIsBanned(false);
                    group.setBanType("");
                    groupService.updateById(group);

                    // 群主不用做校验
                    if (userId.equals(group.getOwnerId())) {
                        return;
                    }
                    groupMemberMsgCheck(group, userId);
                }
                if (StringUtils.isNotBlank(msg)) {
                    throw new GlobalException(msg);
                }
            } else if (BanTypeEnum.MASTER.getCode().equals(group.getBanType())) {
                // 群主不用做校验
                if (userId.equals(group.getOwnerId())) {
                    return;
                }

                // 没有设置失效时间，则被永久禁止
                Date expireTime = group.getBanExpireTime();
                if (expireTime == null) {
                    throw new GlobalException("当前群聊已禁止发送消息");
                }

                // 当前时间已过失效时间
                if (now.after(expireTime)) {
                    group.setIsBanned(false);
                    group.setBanType("");
                    groupService.updateById(group);

                    // 判断群聊成员是否被禁止发言
                    groupMemberMsgCheck(group, userId);
                } else {
                    // 计算两个时间之间的差值
                    int between = (int)DateUtil.between(now, expireTime, DateUnit.SECOND);
                    throw new GlobalException("当前群聊已禁止发送消息，请在" + DateTimeUtils.getTimeValueDesc(between) + "后再尝试");
                }
            }
        } else {
            groupMemberMsgCheck(group, userId);
        }
    }

    private void groupMemberMsgCheck(Group group, Long userId) {
        GroupMember groupMember = groupMemberService.findByGroupAndUserId(group.getId(), userId);
        if (groupMember == null) {
            throw new GlobalException("您不是当前群聊成员");
        }

        if (!groupMember.getIsBanned()) {
            return;
        }

        // 没有设置失效时间，则被永久禁止
        Date expireTime = groupMember.getBanExpireTime();
        if (expireTime == null) {
            throw new GlobalException("您在当前群聊已被禁止发送消息");
        }
        Date now = new Date();

        // 当前时间已过失效时间
        if (now.after(expireTime)) {
            groupMember.setIsBanned(false);
            groupMember.setBanType("");
            groupMemberService.updateById(groupMember);
        } else {
            // 计算两个时间之间的差值
            int between = (int)DateUtil.between(now, expireTime, DateUnit.SECOND);
            throw new GlobalException("您在当前群聊已被禁止发送消息，请在" + DateTimeUtils.getTimeValueDesc(between) + "后再尝试");
        }
    }

    private void regionGroupMsgCheck(Long regionGroupId, Long userId) {
        RegionGroup group = regionGroupService.getById(regionGroupId);
        if (group == null) {
            throw new GlobalException("当前群聊不存在");
        }

        Region region = regionService.getById(group.getCode());
        if (ObjectUtil.isNull(region) || region.getDeleted()) {
            throw new GlobalException("当前地区不存在");
        }
        if (region.getIsBanned()) {
            Date now = new Date();
            if (region.getBanExpireTime() == null) {
                throw new GlobalException("当前地区已被系统禁止发送消息");
            } else if (now.before(region.getBanExpireTime())) {
                long between = DateUtil.between(now, region.getBanExpireTime(), DateUnit.SECOND);
                throw new GlobalException("当前地区已被系统禁止发送消息，请在" + DateTimeUtils.getTimeValueDesc(between) + "后再尝试");
            } else {
                // 已过系统禁止时间
                region.setIsBanned(false);
                region.setBanType("");
                regionService.updateById(region);
            }
        }

        if (group.getIsBanned()) {
            Date now = new Date();
            // 被系统禁止
            if (BanTypeEnum.SYS.getCode().equals(group.getBanType())) {
                String msg = null;
                if (group.getBanExpireTime() == null) {
                    msg = "当前群聊已被系统禁止发送消息";
                } else if (now.before(group.getBanExpireTime())) {
                    long between = DateUtil.between(now, group.getBanExpireTime(), DateUnit.SECOND);
                    msg = "当前群聊已被系统禁止发送消息，请在" + DateTimeUtils.getTimeValueDesc(between) + "后再尝试";
                } else {
                    // 已过系统禁止时间
                    group.setIsBanned(false);
                    group.setBanType("");
                    regionGroupService.updateById(group);

                    regionGroupMemberMsgCheck(group, userId);
                }
                if (StringUtils.isNotBlank(msg)) {
                    throw new GlobalException(msg);
                }
            } else if (BanTypeEnum.MASTER.getCode().equals(group.getBanType())) {
                // 群主不禁言
                if (userId.equals(group.getLeaderId())) {
                    return;
                }

                // 没有设置失效时间，则被永久禁止
                Date expireTime = group.getBanExpireTime();
                if (expireTime == null) {
                    throw new GlobalException("当前群聊已禁止发送消息");
                }

                // 当前时间已过失效时间
                if (now.after(expireTime)) {
                    group.setIsBanned(false);
                    group.setBanType("");
                    regionGroupService.updateById(group);

                    // 判断群聊成员是否被禁止发言
                    regionGroupMemberMsgCheck(group, userId);
                } else {
                    // 没有群主
                    if (ObjectUtil.isNull(group.getLeaderId()) || now.after(group.getExpirationTime())) {
                        // 判断群聊成员是否被禁止发言
                        regionGroupMemberMsgCheck(group, userId);
                    } else {
                        // 计算两个时间之间的差值
                        int between = (int)DateUtil.between(now, expireTime, DateUnit.SECOND);
                        throw new GlobalException("当前群聊已禁止发送消息，请在" + DateTimeUtils.getTimeValueDesc(between) + "后再尝试");
                    }
                }
            }
        } else {
            regionGroupMemberMsgCheck(group, userId);
        }
    }

    private void regionGroupMemberMsgCheck(RegionGroup regionGroup, Long userId) {
        RegionGroupMember groupMember = regionGroupMemberService.findByRegionGroupIdAndUserId(regionGroup.getId(), userId);

        if (ObjectUtil.isNotNull(groupMember) && groupMember.getQuit()) {
            groupMember = null;
        }

        // 判断当前用户是否临时加入
        Object object = redisCache.getCacheObject(IMRedisKey.IM_REGION_GROUP_NUM_TEMP_USER
                + regionGroup.getCode() + ":" + regionGroup.getNum() + ":" + userId);
        if (ObjectUtil.isAllEmpty(groupMember, object)) {
            throw new GlobalException("您不是当前群聊成员");
        }

        if (ObjectUtil.isNotNull(groupMember)) {
            if (!groupMember.getIsBanned()) {
                return;
            }
            Date now = new Date();
            // 被系统禁言
            if (BanTypeEnum.SYS.getCode().equals(groupMember.getBanType()) && now.before(groupMember.getBanExpireTime())) {
                // 计算两个时间之间的差值
                int between = (int)DateUtil.between(now, groupMember.getBanExpireTime(), DateUnit.SECOND);
                throw new GlobalException("您在当前群聊已被系统禁言，请在" + DateTimeUtils.getTimeValueDesc(between) + "后再尝试");
            }

            // 群主身份已失效
            if (BanTypeEnum.MASTER.getCode().equals(groupMember.getBanType()) &&
                    (ObjectUtil.isNull(regionGroup.getLeaderId()) || now.after(regionGroup.getExpirationTime()))) {
                return;
            }

            // 没有设置失效时间，则被永久禁止
            Date expireTime = groupMember.getBanExpireTime();
            if (expireTime == null) {
                throw new GlobalException("您在当前群聊已被禁止发送消息");
            }

            // 当前时间已过失效时间
            if (now.after(expireTime)) {
                groupMember.setIsBanned(false);
                groupMember.setBanType("");
                regionGroupMemberService.updateById(groupMember);
            } else {
                // 计算两个时间之间的差值
                int between = (int)DateUtil.between(now, expireTime, DateUnit.SECOND);
                throw new GlobalException("您在当前群聊已被禁止发送消息，请在" + DateTimeUtils.getTimeValueDesc(between) + "后再尝试");
            }
        } else if (ObjectUtil.isNotNull(object)) {
            Boolean exists = redisCache.hasKey(RedisKey.IM_REGION_GROUP_MEMBER_MSG_SWITCH + regionGroup.getId() + ":" + userId);
            if (exists) {
                Object banTypeObj = redisCache.getCacheObject(RedisKey.IM_REGION_GROUP_MEMBER_MSG_SWITCH + regionGroup.getId() + ":" + userId);
                // 被群主禁言，但是群主身份已失效
                if (BanTypeEnum.MASTER.getCode().equals(banTypeObj.toString()) &&
                        (ObjectUtil.isNull(regionGroup.getLeaderId()) || new Date().after(regionGroup.getExpirationTime()))) {
                    return;
                }

                long expire = redisCache.getExpire(RedisKey.IM_REGION_GROUP_MEMBER_MSG_SWITCH + regionGroup.getId() + ":" + userId);
                String msg = null;
                if (expire == -1) {
                    msg = "您已被禁止发送聊天信息";
                } else {
                    msg = "您已被禁止发送聊天信息，请在" + DateTimeUtils.getTimeValueDesc(expire) + "后再尝试";
                }
                throw new GlobalException(msg);
            }
        }
    }
}

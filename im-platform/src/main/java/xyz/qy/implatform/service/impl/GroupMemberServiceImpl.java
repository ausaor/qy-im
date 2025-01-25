package xyz.qy.implatform.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import xyz.qy.imclient.annotation.Lock;
import xyz.qy.implatform.contant.Constant;
import xyz.qy.implatform.contant.RedisKey;
import xyz.qy.implatform.dto.GroupMessageDTO;
import xyz.qy.implatform.dto.PrivateMessageDTO;
import xyz.qy.implatform.entity.CharacterAvatar;
import xyz.qy.implatform.entity.DictData;
import xyz.qy.implatform.entity.Group;
import xyz.qy.implatform.entity.GroupMember;
import xyz.qy.implatform.entity.TemplateCharacter;
import xyz.qy.implatform.entity.User;
import xyz.qy.implatform.enums.GroupChangeTypeEnum;
import xyz.qy.implatform.enums.GroupTypeEnum;
import xyz.qy.implatform.enums.MessageType;
import xyz.qy.implatform.enums.ResultCode;
import xyz.qy.implatform.exception.GlobalException;
import xyz.qy.implatform.mapper.DictDataMapper;
import xyz.qy.implatform.mapper.GroupMemberMapper;
import xyz.qy.implatform.service.ICharacterAvatarService;
import xyz.qy.implatform.service.IFriendService;
import xyz.qy.implatform.service.IGroupMemberService;
import xyz.qy.implatform.service.IGroupMessageService;
import xyz.qy.implatform.service.IGroupService;
import xyz.qy.implatform.service.IPrivateMessageService;
import xyz.qy.implatform.service.ITemplateCharacterService;
import xyz.qy.implatform.session.SessionContext;
import xyz.qy.implatform.session.UserSession;
import xyz.qy.implatform.util.CommonUtils;
import xyz.qy.implatform.util.DateTimeUtils;
import xyz.qy.implatform.util.MessageSendUtil;
import xyz.qy.implatform.vo.GroupMemberVO;
import xyz.qy.implatform.vo.SwitchCharacterAvatarVO;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@CacheConfig(cacheNames = RedisKey.IM_CACHE_GROUP_MEMBER_ID)
public class GroupMemberServiceImpl extends ServiceImpl<GroupMemberMapper, GroupMember> implements IGroupMemberService {
    @Resource
    private IGroupService groupService;

    @Resource
    private ITemplateCharacterService templateCharacterService;

    @Resource
    private ICharacterAvatarService characterAvatarService;

    @Resource
    private IGroupMessageService groupMessageService;

    @Resource
    private IPrivateMessageService privateMessageService;

    @Resource
    private IFriendService friendService;

    @Resource
    private DictDataMapper dictDataMapper;

    @Resource
    private MessageSendUtil messageSendUtil;

    /**
     * 添加群聊成员
     *
     * @param member 成员
     * @return 成功或失败
     */
    @CacheEvict(key = "#member.getGroupId()")
    @Override
    public boolean save(GroupMember member) {
        return super.save(member);
    }

    /**
     * 批量添加成员
     *
     * @param groupId 群聊id
     * @param members 成员列表
     * @return 成功或失败
     */
    @CacheEvict(key = "#groupId")
    @Override
    public boolean saveOrUpdateBatch(Long groupId, List<GroupMember> members) {
        return super.saveOrUpdateBatch(members);
    }

    /**
     * 根据群聊id和用户id查询群聊成员
     *
     * @param groupId 群聊id
     * @param userId  用户id
     * @return 群聊成员信息
     */
    @Override
    public GroupMember findByGroupAndUserId(Long groupId, Long userId) {
        QueryWrapper<GroupMember> wrapper = new QueryWrapper<>();
        wrapper.lambda().eq(GroupMember::getGroupId, groupId)
                .eq(GroupMember::getUserId, userId);
        return this.getOne(wrapper);
    }

    /**
     * 根据用户id查询所有未退出群聊
     *
     * @param userId 用户id
     * @return 成员列表
     */
    @Override
    public List<GroupMember> findByUserId(Long userId) {
        LambdaQueryWrapper<GroupMember> memberWrapper = Wrappers.lambdaQuery();
        memberWrapper.eq(GroupMember::getUserId, userId)
                .eq(GroupMember::getQuit, false);
        return this.list(memberWrapper);
    }

    @Override
    public List<GroupMember> findQuitInMonth(Long userId) {
        Date monthTime = DateTimeUtils.addMonths(new Date(),-1);
        LambdaQueryWrapper<GroupMember> memberWrapper = Wrappers.lambdaQuery();
        memberWrapper.eq(GroupMember::getUserId, userId)
                .eq(GroupMember::getQuit, true)
                .ge(GroupMember::getQuitTime,monthTime);
        return this.list(memberWrapper);
    }

    /**
     * 根据群聊id查询群聊成员（包括已退出）
     *
     * @param groupId 群聊id
     * @return 群聊成员列表
     */
    @Override
    public List<GroupMember> findByGroupId(Long groupId) {
        LambdaQueryWrapper<GroupMember> memberWrapper = Wrappers.lambdaQuery();
        memberWrapper.eq(GroupMember::getGroupId, groupId);
        return this.list(memberWrapper);
    }

    /**
     * 根据群聊id查询未推出群聊成员
     *
     * @param groupId 群聊id
     * @return 未退出群聊成员
     */
    @Override
    public List<GroupMember> findNoQuitGroupMembers(Long groupId) {
        return this.findByGroupId(groupId).stream()
                .filter(item -> !item.getQuit()).collect(Collectors.toList());
    }

    /**
     * 根据群聊id查询没有退出的群聊成员id
     *
     * @param groupId 群聊id
     * @return 用户id
     */
    @Cacheable(key = "#groupId")
    @Override
    public List<Long> findUserIdsByGroupId(Long groupId) {
        LambdaQueryWrapper<GroupMember> memberWrapper = Wrappers.lambdaQuery();
        memberWrapper.eq(GroupMember::getGroupId, groupId)
                .eq(GroupMember::getQuit, false)
                .select(GroupMember::getUserId);
        List<GroupMember> members = this.list(memberWrapper);
        return members.stream().map(GroupMember::getUserId).collect(Collectors.toList());
    }

    /**
     * 根据用户id获取用户所在所有群id
     *
     * @param userId 用户id
     * @return 用户id
     */
    @Override
    public List<Long> getAllGroupIdsByUserId(Long userId) {
        // 查询用户所有群聊
        List<GroupMember> groupMembers = findByUserId(userId);

        if (CollectionUtils.isEmpty(groupMembers)) {
            return Collections.emptyList();
        }
        // 群聊id
        return groupMembers.stream().map(GroupMember::getGroupId).collect(Collectors.toList());
    }

    /**
     * 获取用户所有群用户id
     *
     * @param userId 用户id
     * @return 用户id
     */
    @Override
    public List<Long> getAllGroupMemberIdsByUserId(Long userId) {
        List<Long> groupIds = this.getAllGroupIdsByUserId(userId);

        if (CollectionUtils.isEmpty(groupIds)) {
            return Collections.emptyList();
        }

        // 获取群聊所有用户id
        LambdaQueryWrapper<GroupMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(GroupMember::getGroupId, groupIds);
        wrapper.ne(GroupMember::getUserId, userId);
        wrapper.eq(GroupMember::getQuit, false);
        return this.list(wrapper).stream().map(GroupMember::getUserId).distinct().collect(Collectors.toList());
    }

    /**
     * 根据群聊id删除移除成员
     *
     * @param groupId 群聊id
     */
    @CacheEvict(key = "#groupId")
    @Override
    public void removeByGroupId(Long groupId) {
        LambdaUpdateWrapper<GroupMember> wrapper = Wrappers.lambdaUpdate();
        wrapper.eq(GroupMember::getGroupId, groupId)
                .set(GroupMember::getQuit, true)
                .set(GroupMember::getQuitTime,new Date());
        this.update(wrapper);
    }

    /**
     * 根据群聊id和用户id移除成员
     *
     * @param groupId 群聊id
     * @param userId  用户id
     */
    @CacheEvict(key = "#groupId")
    @Override
    public void removeByGroupAndUserId(Long groupId, Long userId) {
        LambdaUpdateWrapper<GroupMember> wrapper = Wrappers.lambdaUpdate();
        wrapper.eq(GroupMember::getGroupId, groupId)
                .eq(GroupMember::getUserId, userId)
                .set(GroupMember::getQuit, true)
                .set(GroupMember::getQuitTime,new Date());
        this.update(wrapper);
    }

    @CacheEvict(key = "#groupMemberVO.getGroupId()")
    @Override
    @Lock(prefix = "im:group:member:modify", key = "#groupMemberVO.getGroupId()")
    public void switchTemplateCharacter(GroupMemberVO groupMemberVO) {
        if (Arrays.stream(GroupTypeEnum.values()).noneMatch(item -> item.getCode().equals(groupMemberVO.getGroupType()))
                || GroupTypeEnum.COMMON.getCode().equals(groupMemberVO.getGroupType())) {
            throw new GlobalException("群聊类型错误");
        }
        UserSession session = SessionContext.getSession();
        // 查询当前用户的群用户信息
        QueryWrapper<GroupMember> wrapper = new QueryWrapper();
        wrapper.lambda().eq(GroupMember::getGroupId, groupMemberVO.getGroupId())
                .eq(GroupMember::getUserId, session.getUserId());
        GroupMember groupMember = this.getOne(wrapper);
        if (ObjectUtil.isNull(groupMember) || groupMember.getQuit()) {
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "您已不在群聊里面，无法切换模板角色");
        }

        // 查询群信息
        Group group = groupService.getById(groupMemberVO.getGroupId());
        if (group == null || group.getDeleted()) {
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "群聊不存在");
        }
        // 不能是普通群聊
        if (GroupTypeEnum.COMMON.getCode().equals(group.getGroupType())) {
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "当前群聊是普通群聊，不能切换模板角色");
        }
        if (!group.getGroupType().equals(groupMemberVO.getGroupType())) {
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "群聊类型已切换");
        }

        // 判断上次切换时间与当前时间间隔
        Date switchTime = groupMember.getSwitchTime();
        if (switchTime != null) {
            long interval = (new Date().getTime() - switchTime.getTime()) / 1000;
            if (interval < Constant.SWITCH_INTERVAL) {
                throw new GlobalException("距离上次切换模板人物未超过30分钟");
            }
        }

        TemplateCharacter templateCharacter = null;
        // 当前群聊是模板群聊
        if (GroupTypeEnum.TEMPLATE.getCode().equals(group.getGroupType())
            || GroupTypeEnum.TEMPLATE_MULT_CHARTER.getCode().equals(group.getGroupType())) {
            if (!group.getTemplateGroupId().equals(groupMemberVO.getTemplateGroupId())) {
                throw new GlobalException("群聊模板已切换，请重新选择");
            }
            // 判断所选模板人物是否属于当前模板群聊
            List<TemplateCharacter> templateCharacters = templateCharacterService.findPublishedByTemplateGroupIdAndCharacterIds(group.getTemplateGroupId(),
                    Collections.singletonList(groupMemberVO.getTemplateCharacterId()));
            if (CollectionUtils.isEmpty(templateCharacters)) {
                throw new GlobalException("群聊模板不存在所选模板角色");
            }
            templateCharacter = templateCharacters.get(0);
        } else {
            // 判断模板人物是否存在
            List<TemplateCharacter> templateCharacters = templateCharacterService.findPublishedByTemplateGroupIdAndCharacterIds(groupMemberVO.getTemplateGroupId(),
                    Collections.singletonList(groupMemberVO.getTemplateCharacterId()));
            if (CollectionUtils.isEmpty(templateCharacters)) {
                throw new GlobalException("群聊模板不存在所选模板角色");
            }
            templateCharacter = templateCharacters.get(0);
        }
        // 查询新的模板人物是否已存在群聊里
        LambdaQueryWrapper<GroupMember> memberWrapper = new LambdaQueryWrapper<>();
        memberWrapper.eq(GroupMember::getGroupId, group.getId())
                .eq(GroupMember::getTemplateCharacterId, groupMemberVO.getTemplateCharacterId())
                .eq(GroupMember::getQuit, false);
        List<GroupMember> groupMemberList = this.list(memberWrapper);
        if (GroupTypeEnum.TEMPLATE.getCode().equals(group.getGroupType())
                || GroupTypeEnum.MULT_CHARTER.getCode().equals(group.getGroupType())) {
            if (CollectionUtils.isNotEmpty(groupMemberList)) {
                throw new GlobalException(ResultCode.PROGRAM_ERROR, "当前模板角色已有用户选择");
            }
        } else if (GroupTypeEnum.CHARTERS.getCode().equals(group.getGroupType())
            || GroupTypeEnum.TEMPLATE_MULT_CHARTER.getCode().equals(group.getGroupType())) {
            // 判断新的模板角色在群聊中的用户是否超过10
            if (groupMemberList.size() >= Constant.MAX_CHARACTER_NUM) {
                throw new GlobalException("所选模板角色在当前群聊已被" + Constant.MAX_CHARACTER_NUM + "位用户选择，请重新选择其他角色");
            }
        }

        groupMember.setTemplateCharacterId(groupMemberVO.getTemplateCharacterId());
        groupMember.setAliasName(templateCharacter.getName());
        groupMember.setHeadImage(templateCharacter.getAvatar());
        groupMember.setSwitchTime(new Date());

        this.saveOrUpdateBatch(group.getId(), Collections.singletonList(groupMember));
        String content = "用户" + session.getNickName() + "将模板角色切换成【" + groupMember.getAliasName() + "】";
        messageSendUtil.sendTipMessage(group.getId(), session.getUserId(), session.getNickName(),
                Collections.emptyList(), content, GroupChangeTypeEnum.TEMPLATE_CHARACTER_CHANGE.getCode());
    }

    @CacheEvict(key = "#avatarVO.getGroupId()")
    @Override
    @Lock(prefix = "im:group:member:modify", key = "#avatarVO.getGroupId()")
    public void switchCharacterAvatar(SwitchCharacterAvatarVO avatarVO) {
        if (Arrays.stream(GroupTypeEnum.values()).noneMatch(item -> item.getCode().equals(avatarVO.getGroupType()))
                || GroupTypeEnum.COMMON.getCode().equals(avatarVO.getGroupType())) {
            throw new GlobalException("群聊类型错误");
        }
        UserSession session = SessionContext.getSession();
        // 查询当前用户的群用户信息
        QueryWrapper<GroupMember> wrapper = new QueryWrapper();
        wrapper.lambda().eq(GroupMember::getGroupId, avatarVO.getGroupId())
                .eq(GroupMember::getUserId, session.getUserId());
        GroupMember groupMember = this.getOne(wrapper);
        if (ObjectUtil.isNull(groupMember) || groupMember.getQuit()) {
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "您已不在群聊里面，无法切换模板角色头像");
        }

        // 查询群信息
        Group group = groupService.getById(avatarVO.getGroupId());
        if (group == null || group.getDeleted()) {
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "群聊不存在");
        }
        // 不能是普通群聊
        if (GroupTypeEnum.COMMON.getCode().equals(group.getGroupType())) {
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "当前群聊是普通群聊，不能切换模板角色头像");
        }
        if (!group.getGroupType().equals(avatarVO.getGroupType())) {
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "群聊类型已切换");
        }
        // 判断模板角色是否属于当前模板群聊
        if (GroupTypeEnum.TEMPLATE.getCode().equals(group.getGroupType())) {
            if (ObjectUtil.isNull(avatarVO.getTemplateGroupId())) {
                throw new GlobalException("参数异常");
            }
            if (!group.getTemplateGroupId().equals(avatarVO.getTemplateGroupId())) {
                throw new GlobalException("群聊模板已切换");
            }
            List<TemplateCharacter> templateCharacters = templateCharacterService.findPublishedByTemplateGroupIdAndCharacterIds(group.getTemplateGroupId(),
                    Collections.singletonList(avatarVO.getTemplateCharacterId()));
            if (CollectionUtils.isEmpty(templateCharacters)) {
                throw new GlobalException(ResultCode.PROGRAM_ERROR, "当前群聊模板不存在该模板角色");
            }
        } else {
            // 判断模板角色是否存在
            TemplateCharacter templateCharacter = templateCharacterService.findPublishedById(avatarVO.getTemplateCharacterId());
            if (ObjectUtil.isNull(templateCharacter)) {
                throw new GlobalException("模板角色不存在");
            }
        }

        // 判断模板人物头像是否属于模板人物
        CharacterAvatar characterAvatar = characterAvatarService.findPublishedCharacterAvatarById(avatarVO.getCharacterAvatarId());
        if (ObjectUtil.isNull(characterAvatar) || !avatarVO.getTemplateCharacterId().equals(characterAvatar.getTemplateCharacterId())) {
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "所选模板角色头像不属于当前模板角色");
        }

        groupMember.setCharacterAvatarId(characterAvatar.getId());
        groupMember.setHeadImage(characterAvatar.getAvatar());
        if (characterAvatar.getLevel() != 0) {
            groupMember.setAliasName(characterAvatar.getName());
        } else {
            groupMember.setAliasName(characterAvatar.getTemplateCharacterName());
        }
        groupMember.setAvatarAlias(characterAvatar.getName());

        this.saveOrUpdateBatch(group.getId(), Collections.singletonList(groupMember));

        String content = "用户【" + session.getNickName() + "】切换了模板角色头像";
        messageSendUtil.sendTipMessage(group.getId(), session.getUserId(), session.getNickName(),
                Collections.emptyList(), content, GroupChangeTypeEnum.TEMPLATE_CHARACTER_CHANGE.getCode());
    }

    @Override
    public void newRegisteredUserHandle(User user) {
        try {
            LambdaQueryWrapper<DictData> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(DictData::getDictType, "new_register_user");
            DictData dictData1 = dictDataMapper.selectOne(lambdaQueryWrapper);
            if (ObjectUtil.isNull(dictData1) || "NO".equals(dictData1.getDictValue())) {
                return;
            }

            LambdaQueryWrapper<DictData> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(DictData::getDictType, "common_group");
            DictData dictData = dictDataMapper.selectOne(queryWrapper);

            GroupMember groupMember = groupService.addToCommonGroup(user, Long.parseLong(dictData.getDictValue()));
            if (ObjectUtil.isNotNull(groupMember)) {
                GroupMessageDTO groupMessageVO = CommonUtils.buildGroupMessageVO(groupMember.getGroupId(), CommonUtils.buildWelcomeMessage(user, groupMember), MessageType.TEXT.code());
                groupMessageVO.setAtUserIds(Collections.singletonList(user.getId()));

                String content = null;
                if (groupMember.getIsTemplate()) {
                    content = "用户" + user.getNickName() + "【" + groupMember.getAliasName() + "】" + "加入了群聊";
                } else {
                    content = "用户" + user.getNickName() + "加入了群聊";
                }
                messageSendUtil.sendTipMessage(groupMember.getGroupId(),
                        Constant.ADMIN_USER_ID, "管理员", Collections.emptyList(),
                        content, GroupChangeTypeEnum.NEW_USER_JOIN.getCode());
                groupMessageService.sendGroupMessage(groupMessageVO, Constant.ADMIN_USER_ID);
            }
            if (!user.getId().equals(Constant.ADMIN_USER_ID)) {
                friendService.addFriend(user.getId(), Constant.ADMIN_USER_ID);
                PrivateMessageDTO privateMessageVO = CommonUtils.buildPrivateMessageVO(user.getId(), Constant.ADMIN_WELCOME_MSG, MessageType.TEXT.code());
                privateMessageService.sendPrivateMessage(privateMessageVO, Constant.ADMIN_USER_ID);
            }
        } catch (Exception e) {
            log.error("error:{}", e.getMessage());
        }
    }

    @Override
    public Boolean isInGroup(Long groupId, List<Long> userIds) {
        if (CollectionUtils.isEmpty(userIds)) {
            return true;
        }
        LambdaQueryWrapper<GroupMember> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(GroupMember::getGroupId, groupId).eq(GroupMember::getQuit, false)
                .in(GroupMember::getUserId, userIds);
        return userIds.size() == this.count(wrapper);
    }
}

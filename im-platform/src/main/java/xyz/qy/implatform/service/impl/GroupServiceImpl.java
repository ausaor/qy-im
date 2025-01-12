package xyz.qy.implatform.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xyz.qy.imclient.IMClient;
import xyz.qy.imclient.annotation.Lock;
import xyz.qy.imcommon.model.IMGroupMessage;
import xyz.qy.imcommon.model.IMUserInfo;
import xyz.qy.imcommon.util.CommaTextUtils;
import xyz.qy.implatform.contant.Constant;
import xyz.qy.implatform.contant.RedisKey;
import xyz.qy.implatform.entity.Friend;
import xyz.qy.implatform.entity.Group;
import xyz.qy.implatform.entity.GroupMember;
import xyz.qy.implatform.entity.GroupMessage;
import xyz.qy.implatform.entity.TemplateCharacter;
import xyz.qy.implatform.entity.TemplateGroup;
import xyz.qy.implatform.entity.User;
import xyz.qy.implatform.enums.GroupChangeTypeEnum;
import xyz.qy.implatform.enums.GroupTypeEnum;
import xyz.qy.implatform.enums.MessageStatus;
import xyz.qy.implatform.enums.MessageType;
import xyz.qy.implatform.enums.ResultCode;
import xyz.qy.implatform.enums.ReviewEnum;
import xyz.qy.implatform.exception.GlobalException;
import xyz.qy.implatform.mapper.GroupMapper;
import xyz.qy.implatform.mapper.GroupMessageMapper;
import xyz.qy.implatform.service.IFriendService;
import xyz.qy.implatform.service.IGroupMemberService;
import xyz.qy.implatform.service.IGroupService;
import xyz.qy.implatform.service.ITemplateCharacterService;
import xyz.qy.implatform.service.ITemplateGroupService;
import xyz.qy.implatform.service.IUserService;
import xyz.qy.implatform.session.SessionContext;
import xyz.qy.implatform.session.UserSession;
import xyz.qy.implatform.util.BeanUtils;
import xyz.qy.implatform.util.MessageSendUtil;
import xyz.qy.implatform.util.PageUtils;
import xyz.qy.implatform.vo.CommonGroupVO;
import xyz.qy.implatform.vo.GroupInviteVO;
import xyz.qy.implatform.vo.GroupJoinVO;
import xyz.qy.implatform.vo.GroupMemberVO;
import xyz.qy.implatform.vo.GroupMessageVO;
import xyz.qy.implatform.vo.GroupVO;
import xyz.qy.implatform.vo.MultCharacterGroupVO;
import xyz.qy.implatform.vo.PageResultVO;
import xyz.qy.implatform.vo.SwitchTemplateGroupVO;
import xyz.qy.implatform.vo.TemplateCharacterInviteVO;
import xyz.qy.implatform.vo.TemplateGroupCreateVO;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@CacheConfig(cacheNames = RedisKey.IM_CACHE_GROUP)
public class GroupServiceImpl extends ServiceImpl<GroupMapper, Group> implements IGroupService {
    @Resource
    private IUserService userService;

    @Resource
    private IGroupMemberService groupMemberService;

    @Resource
    private IFriendService friendsService;

    @Resource
    private ITemplateGroupService templateGroupService;

    @Resource
    private ITemplateCharacterService templateCharacterService;

    @Resource
    private IMClient imClient;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private MessageSendUtil messageSendUtil;

    @Resource
    private GroupMessageMapper groupMessageMapper;

    /**
     * 创建普通群聊
     *
     * @Param vo 群聊信息
     * @return 群聊信息
     **/
    @Transactional(rollbackFor = Exception.class)
    @Override
    public GroupVO createGroup(GroupVO vo) {
        UserSession session = SessionContext.getSession();
        if (Arrays.stream(GroupTypeEnum.values()).noneMatch(item -> item.getCode().equals(vo.getGroupType()))) {
            throw new GlobalException("群聊类型错误");
        }
        User user = userService.getById(session.getUserId());
        // 保存群组数据
        Group group = BeanUtils.copyProperties(vo, Group.class);
        assert group != null;
        group.setOwnerId(user.getId());
        group.setGroupType(GroupTypeEnum.COMMON.getCode());
        group.setIsTemplate(false);
        this.save(group);
        // 把群主加入群
        GroupMember groupMember = new GroupMember();
        groupMember.setGroupId(group.getId());
        groupMember.setUserId(user.getId());
        groupMember.setHeadImage(user.getHeadImage());
        groupMember.setAliasName(StringUtils.isEmpty(vo.getAliasName()) ? session.getNickName() : vo.getAliasName());
        groupMember.setRemark(StringUtils.isEmpty(vo.getRemark()) ? group.getName() : vo.getRemark());
        groupMemberService.save(groupMember);

        vo.setId(group.getId());
        vo.setAliasName(groupMember.getAliasName());
        vo.setRemark(groupMember.getRemark());
        log.info("创建群聊，群聊id:{},群聊名称:{}", group.getId(), group.getName());
        return vo;
    }

    /**
     * 修改群聊信息
     *
     * @return
     * @Param GroupVO 群聊信息
     **/
    @CacheEvict(key = "#vo.getId()")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public GroupVO modifyGroup(GroupVO vo) {
        UserSession session = SessionContext.getSession();
        // 校验是不是群主，只有群主能改信息
        Group group = this.getById(vo.getId());
        // 群主有权修改群基本信息
        if (group.getOwnerId().equals(session.getUserId())) {
            group = BeanUtils.copyProperties(vo, Group.class);
            assert group != null;
            group.setName(group.getName());
            group.setRemark(group.getRemark());
            group.setNotice(group.getNotice());
            this.updateById(group);
        }
        // 更新成员信息
        GroupMember member = groupMemberService.findByGroupAndUserId(vo.getId(), session.getUserId());
        if (member == null || member.getQuit()) {
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "您不是群聊的成员");
        }
        member.setRemark(StringUtils.isEmpty(vo.getRemark()) ? Objects.requireNonNull(group).getName() : vo.getRemark());
        member.setShowNickName(vo.getShowNickName());
        member.setAliasNamePrefix(vo.getAliasNamePrefix());
        member.setAliasNameSuffix(vo.getAliasNameSuffix());
        member.setNickname(vo.getNickName());
        if (GroupTypeEnum.COMMON.getCode().equals(group.getGroupType())) {
            member.setAliasName(StringUtils.isEmpty(vo.getAliasName()) ? session.getNickName() : vo.getAliasName());
            member.setHeadImage(vo.getMemberHeadImage());
        }
        groupMemberService.updateById(member);
        log.info("修改群聊，群聊id:{},群聊名称:{}", group.getId(), group.getName());
        return vo;
    }

    /**
     * 删除群聊
     *
     * @Param groupId 群聊id
     **/
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(key = "#groupId")
    @Override
    public void deleteGroup(Long groupId) {
        UserSession session = SessionContext.getSession();
        Group group = this.getById(groupId);
        if (!group.getOwnerId().equals(session.getUserId())) {
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "只有群主才有权限解除群聊");
        }
        // 逻辑删除群数据
        group.setDeleted(true);
        this.updateById(group);
        List<Long> userIds = groupMemberService.findUserIdsByGroupId(groupId);
        // 删除成员数据
        groupMemberService.removeByGroupId(groupId);
        // 清理已读缓存
        String key = StrUtil.join(":", RedisKey.IM_GROUP_READED_POSITION, groupId);
        redisTemplate.delete(key);
        // 推送解散群聊提示
        this.sendTipMessage(groupId,userIds,String.format("'%s'解散了群聊",session.getNickName()));
//        messageSendUtil.sendTipMessage(group.getId(),
//                session.getUserId(), session.getNickName(), userIds,
//                "群主已将当前群聊解散", GroupChangeTypeEnum.DELETE_GROUP.getCode());
        log.info("删除群聊，群聊id:{},群聊名称:{}", group.getId(), group.getName());
    }

    /**
     * 退出群聊
     *
     * @param groupId 群聊id
     */
    @Override
    public void quitGroup(Long groupId) {
        Long userId = SessionContext.getSession().getUserId();
        Group group = this.getById(groupId);
        if (group.getOwnerId().equals(userId)) {
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "您是群主，不可退出群聊");
        }
        // 删除群聊成员
        groupMemberService.removeByGroupAndUserId(groupId, userId);
        // 清理已读缓存
        String key = StrUtil.join(":", RedisKey.IM_GROUP_READED_POSITION, groupId);
        redisTemplate.opsForHash().delete(key,userId.toString());
        // 推送退出群聊提示
        this.sendTipMessage(groupId, Arrays.asList(userId),"您已退出群聊");
        log.info("退出群聊，群聊id:{},群聊名称:{},用户id:{}", group.getId(), group.getName(), userId);
    }

    /**
     * 将用户踢出群聊
     *
     * @param groupId 群聊id
     * @param userId  用户id
     */
    @Override
    public void kickGroup(Long groupId, Long userId) {
        UserSession session = SessionContext.getSession();
        Group group = this.getById(groupId);
        if (!group.getOwnerId().equals(session.getUserId())) {
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "您不是群主，没有权限踢人");
        }
        if (userId.equals(session.getUserId())) {
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "亲，不能自己踢自己哟");
        }
        // 删除群聊成员
        groupMemberService.removeByGroupAndUserId(groupId, userId);
        // 清理已读缓存
        String key = StrUtil.join(":", RedisKey.IM_GROUP_READED_POSITION, groupId);
        redisTemplate.opsForHash().delete(key,userId.toString());
        // 推送踢出群聊提示
        this.sendTipMessage(groupId,Arrays.asList(userId),"您已被移出群聊");
        log.info("踢出群聊，群聊id:{},群聊名称:{},用户id:{}", group.getId(), group.getName(), userId);
    }

    @Override
    public GroupVO findById(Long groupId) {
        UserSession session = SessionContext.getSession();
        Group group = super.getById(groupId);
        if (Objects.isNull(group)) {
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "群组不存在");
        }
        GroupMember member = groupMemberService.findByGroupAndUserId(groupId, session.getUserId());
        if (Objects.isNull(member)) {
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "您未加入群聊");
        }
        GroupVO vo = BeanUtils.copyProperties(group, GroupVO.class);
        assert vo != null;
        vo.setAliasName(member.getAliasName());
        vo.setRemark(member.getRemark());
        vo.setIsTemplateCharacter(member.getIsTemplate());
        vo.setTemplateCharacterId(member.getTemplateCharacterId());
        vo.setQuit(member.getQuit());
        vo.setAliasNamePrefix(member.getAliasNamePrefix());
        vo.setAliasNameSuffix(member.getAliasNameSuffix());
        vo.setNickName(member.getNickname());
        return vo;
    }

    /**
     * 根据id查找群聊，并进行缓存
     *
     * @param groupId 群聊id
     */
    @Cacheable(key = "#groupId")
    @Override
    public Group GetById(Long groupId) {
        Group group = super.getById(groupId);
        if (Objects.isNull(group)) {
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "群组不存在");
        }
        if (group.getDeleted()) {
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "群组" + group.getName() + "已解散");
        }
        return group;
    }

    @Cacheable(key = "#groupId")
    @Override
    public Group getAndCheckById(Long groupId) {
        Group group = super.getById(groupId);
        if (Objects.isNull(group)) {
            throw new GlobalException("群组不存在");
        }
        if (group.getDeleted()) {
            throw new GlobalException("群组'" + group.getName() + "'已解散");
        }
        if (group.getIsBanned()) {
            throw new GlobalException("群组'" + group.getName() + "'已被封禁");
        }
        return group;
    }

    /**
     * 查询当前用户的所有群聊
     *
     * @return 群聊列表
     **/
    @Override
    public List<GroupVO> findGroups() {
        UserSession session = SessionContext.getSession();
        // 查询当前用户的群id列表
        List<GroupMember> groupMembers = groupMemberService.findByUserId(session.getUserId());
        // 一个月内退的群可能存在退群前的离线消息,一并返回作为前端缓存
        groupMembers.addAll(groupMemberService.findQuitInMonth(session.getUserId()));
        if (groupMembers.isEmpty()) {
            return Collections.emptyList();
        }
        // 群id
        List<Long> ids = groupMembers.stream().map((GroupMember::getGroupId)).collect(Collectors.toList());
        LambdaQueryWrapper<Group> groupWrapper = Wrappers.lambdaQuery();
        groupWrapper.in(Group::getId, ids);
        // 查询用户群聊信息
        List<Group> groups = this.list(groupWrapper);
        // 转vo
        return groups.stream().map(g -> {
            GroupVO vo = BeanUtils.copyProperties(g, GroupVO.class);
            GroupMember member = groupMembers.stream().filter(m -> g.getId().equals(m.getGroupId())).findFirst().get();
            assert vo != null;
            vo.setAliasName(member.getAliasName());
            vo.setRemark(member.getRemark());
            vo.setIsTemplateCharacter(member.getIsTemplate());
            vo.setTemplateCharacterId(member.getTemplateCharacterId());
            vo.setQuit(member.getQuit());
            vo.setNickName(member.getNickname());
            return vo;
        }).collect(Collectors.toList());
    }

    /**
     * 邀请好友进群
     *
     * @Param GroupInviteVO  群id、好友id列表
     **/
    @Override
    @Lock(prefix = "im:group:member:modify", key = "#vo.getGroupId()")
    public void invite(GroupInviteVO vo) {
        UserSession session = SessionContext.getSession();
        Group group = this.getById(vo.getGroupId());
        if (Objects.isNull(group) || group.getDeleted()) {
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "群聊不存在");
        }
        if (!group.getGroupType().equals(vo.getGroupType())) {
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "群聊类型错误");
        }
        GroupMember member = groupMemberService.findByGroupAndUserId(vo.getGroupId(), session.getUserId());
        if (Objects.isNull(member) || member.getQuit()) {
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "您不在群聊中,邀请失败");
        }
        // 群聊人数校验
        List<GroupMember> members = groupMemberService.findByGroupId(vo.getGroupId());
        long size = members.stream().filter(m -> !m.getQuit()).count();
        if (vo.getFriendIds().size() + size > Constant.MAX_GROUP_MEMBER) {
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "群聊人数不能大于" + Constant.MAX_GROUP_MEMBER + "人");
        }

        List<TemplateCharacter> templateCharacterList = null;
        // 模板群聊人数不能超过模板群所有角色人物数量
        if (GroupTypeEnum.TEMPLATE.getCode().equals(vo.getGroupType())) {
            // 查询模板群所有角色人物数量
            int characterCount = templateCharacterService.count(new LambdaQueryWrapper<TemplateCharacter>()
                    .eq(TemplateCharacter::getTemplateGroupId, group.getTemplateGroupId())
                    .eq(TemplateCharacter::getStatus, ReviewEnum.REVIEWED.getCode())
                    .eq(TemplateCharacter::getDeleted, false));
            if (vo.getFriendIds().size() + size > characterCount) {
                throw new GlobalException(ResultCode.PROGRAM_ERROR, "当前模板群聊人数不能大于" + characterCount + "人");
            }
            List<Long> templateCharacterIds = vo.getCharacterInviteVOList().stream()
                    .map(TemplateCharacterInviteVO::getTemplateCharacterId)
                    .collect(Collectors.toList());
            
            // 判断所选模板角色是否重复
            List<Long> distinctIds = templateCharacterIds.stream().distinct().collect(Collectors.toList());
            if (distinctIds.size() != templateCharacterIds.size()) {
                throw new GlobalException("所选模板角色存在重复");
            }

            // 判断当前模板群聊是否包含所选模板人物
            templateCharacterList = templateCharacterService.findPublishedByTemplateGroupIdAndCharacterIds(group.getTemplateGroupId(), templateCharacterIds);
            if (templateCharacterList.size() != templateCharacterIds.size()) {
                throw new GlobalException("部分模板角色不存在当前群聊模板");
            }

            // 判断新的群用户的模板角色人物是否已存在
            List<GroupMember> groupMemberList = groupMemberService.lambdaQuery()
                    .eq(GroupMember::getGroupId, group.getId())
                    .eq(GroupMember::getQuit, false)
                    .in(GroupMember::getTemplateCharacterId, templateCharacterIds).list();
            if (CollectionUtils.isNotEmpty(groupMemberList)) {
                List<TemplateCharacter> templateCharacters = templateCharacterService.listByIds(groupMemberList.stream()
                        .map(GroupMember::getTemplateCharacterId)
                        .collect(Collectors.toList()));
                throw new GlobalException(ResultCode.PROGRAM_ERROR, "当前群聊已存在角色人物是" + templateCharacters.stream()
                        .map(TemplateCharacter::getName).collect(Collectors.joining(",")) + "的用户");
            }
        } else if (GroupTypeEnum.MULT_CHARTER.getCode().equals(vo.getGroupType())) {
            List<Long> templateCharacterIds = vo.getCharacterInviteVOList().stream()
                    .map(TemplateCharacterInviteVO::getTemplateCharacterId)
                    .collect(Collectors.toList());

            // 判断是否有重复的角色
            List<Long> distinctList = vo.getCharacterInviteVOList().stream().map(TemplateCharacterInviteVO::getTemplateCharacterId).distinct().collect(Collectors.toList());
            if (distinctList.size() != templateCharacterIds.size()) {
                throw new GlobalException("模板角色有重复");
            }

            // 判断模板人物是否存在
            templateCharacterList = templateCharacterService.findPublishedByCharacterIds(templateCharacterIds);
            if (templateCharacterList.size() != templateCharacterIds.size()) {
                throw new GlobalException("部分模板角色不存在");
            }

            // 判断已存在成员角色是否包含所选模板角色
            List<GroupMember> groupMemberList = groupMemberService.lambdaQuery()
                    .eq(GroupMember::getGroupId, group.getId())
                    .eq(GroupMember::getQuit, false)
                    .in(GroupMember::getTemplateCharacterId, templateCharacterIds).list();
            if (CollectionUtils.isNotEmpty(groupMemberList)) {
                List<TemplateCharacter> templateCharacters = templateCharacterService.listByIds(groupMemberList.stream()
                        .map(GroupMember::getTemplateCharacterId)
                        .collect(Collectors.toList()));
                throw new GlobalException(ResultCode.PROGRAM_ERROR, "当前群聊已存在角色人物是" + templateCharacters.stream()
                        .map(TemplateCharacter::getName).collect(Collectors.joining(",")) + "的用户");
            }
        } else if (GroupTypeEnum.CHARTERS.getCode().equals(vo.getGroupType())
            || GroupTypeEnum.TEMPLATE_MULT_CHARTER.getCode().equals(vo.getGroupType())) {
            // 判断参数中每个角色的数量加已存在成员的对应角色数量是否超过10
            List<TemplateCharacterInviteVO> characterInviteVOList = vo.getCharacterInviteVOList();
            Map<Long, List<TemplateCharacterInviteVO>> characterUserMap = characterInviteVOList.stream().collect(Collectors.groupingBy(TemplateCharacterInviteVO::getTemplateCharacterId));
            Set<Long> characterIds = characterUserMap.keySet();

            // 判断角色是否存在
            templateCharacterList = templateCharacterService.findPublishedByCharacterIds(new ArrayList<>(characterIds));
            if (templateCharacterList.size() != characterIds.size()) {
                throw new GlobalException("部分模板角色不存在");
            }

            // 若是模板多元角色群聊，判断当前群聊模板是否包含所选模板人物
            if (GroupTypeEnum.TEMPLATE_MULT_CHARTER.getCode().equals(vo.getGroupType())) {
                templateCharacterList = templateCharacterService.findPublishedByTemplateGroupIdAndCharacterIds(group.getTemplateGroupId(), new ArrayList<>(characterIds));
                if (templateCharacterList.size() != characterIds.size()) {
                    throw new GlobalException("部分模板角色不存在当前群聊模板");
                }
            }


            // 校验所选角色用户数量是否超过10个
            characterUserMap.forEach((key, value) ->{
                if (value.size() > Constant.MAX_CHARACTER_NUM) {
                    throw new GlobalException("角色【"+ value.get(0).getTemplateCharacterName() +"】的用户数量不能超过" + Constant.MAX_CHARACTER_NUM + "个");
                }
                long count = members.stream().filter(item -> item.getTemplateCharacterId().equals(key) && !item.getQuit()).count();
                if ((value.size() + count) > Constant.MAX_CHARACTER_NUM) {
                    throw new GlobalException("角色【"+value.get(0).templateCharacterName+"】在当前群聊的已有用户数量加所选用户数量已超过" + Constant.MAX_CHARACTER_NUM + "个");
                }
            });
        }

        // 找出好友信息
        List<Friend> friends = friendsService.findFriendByUserId(session.getUserId());
        List<Friend> friendsList = vo.getFriendIds().stream().map(id ->
                friends.stream().filter(f -> f.getFriendId().equals(id)).findFirst().get()).collect(Collectors.toList());
        if (friendsList.size() != vo.getFriendIds().size()) {
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "部分用户不是您的好友，邀请失败");
        }
        Map<Long, Friend> friendMap = friendsList.stream().collect(Collectors.toMap(Friend::getFriendId, Function.identity(), (key1, key2) -> key2));

        List<GroupMember> groupMembers = null;
        // 不是模板群聊
        if (GroupTypeEnum.COMMON.getCode().equals(vo.getGroupType())) {
            // 批量保存成员数据
            groupMembers = friendsList.stream()
                    .map(f -> {
                        Optional<GroupMember> optional = members.stream().filter(m -> m.getUserId().equals(f.getFriendId())).findFirst();
                        GroupMember groupMember = optional.orElseGet(GroupMember::new);
                        groupMember.setGroupId(vo.getGroupId());
                        groupMember.setUserId(f.getFriendId());
                        groupMember.setAliasName(f.getFriendNickName());
                        groupMember.setRemark(group.getName());
                        groupMember.setHeadImage(f.getFriendHeadImage());
                        groupMember.setCreatedTime(new Date());
                        groupMember.setQuit(false);
                        groupMember.setCharacterAvatarId(null);
                        groupMember.setAvatarAlias(null);
                        return groupMember;
                    }).collect(Collectors.toList());
        } else {
            assert templateCharacterList != null;
            Map<Long, TemplateCharacter> templateCharacterMap = templateCharacterList.stream().collect(Collectors.toMap(TemplateCharacter::getId, Function.identity(), (key1, key2) -> key2));

            groupMembers = vo.getCharacterInviteVOList().stream().map(f -> {
                Optional<GroupMember> optional = members.stream().filter(m -> m.getUserId().equals(f.getFriendId())).findFirst();
                GroupMember groupMember = optional.orElseGet(GroupMember::new);
                groupMember.setGroupId(vo.getGroupId());
                groupMember.setUserId(f.getFriendId());
                groupMember.setRemark(group.getName());
                if (!templateCharacterMap.containsKey(f.getTemplateCharacterId())) {
                    throw new GlobalException("数据异常");
                }
                groupMember.setAliasName(templateCharacterMap.get(f.getTemplateCharacterId()).getName());
                groupMember.setHeadImage(templateCharacterMap.get(f.getTemplateCharacterId()).getAvatar());
                groupMember.setTemplateCharacterId(f.getTemplateCharacterId());
                groupMember.setIsTemplate(true);
                groupMember.setCreatedTime(new Date());
                groupMember.setQuit(false);
                groupMember.setCharacterAvatarId(null);
                groupMember.setAvatarAlias(null);
                return groupMember;
            }).collect(Collectors.toList());
        }

        if (CollectionUtils.isNotEmpty(groupMembers)) {
            groupMemberService.saveOrUpdateBatch(group.getId(), groupMembers);

            String membersInfo = groupMembers.stream().map(item -> {
                if (GroupTypeEnum.COMMON.getCode().equals(vo.getGroupType())) {
                    return item.getAliasName();
                } else {
                    return friendMap.getOrDefault(item.getUserId(), new Friend()).getFriendNickName() + "【" + item.getAliasName() + "】";
                }
            }).collect(Collectors.joining("，"));
            StringBuilder builder = new StringBuilder();
            String content = builder.append("用户")
                    .append(session.getUserName()).append("【").append(session.getNickName()).append("】")
                    .append("邀请")
                    .append(membersInfo)
                    .append("加入了群聊").toString();
            messageSendUtil.sendTipMessage(group.getId(),
                    session.getUserId(), session.getNickName(), Collections.emptyList(),
                    content, GroupChangeTypeEnum.NEW_USER_JOIN.getCode());
        }
        log.info("邀请进入群聊，群聊id:{},群聊名称:{},被邀请用户id:{}", group.getId(), group.getName(), vo.getFriendIds());
    }

    /**
     * 查询群成员
     *
     * @Param groupId 群聊id
     * @return List<GroupMemberVO>
     */
    @Override
    public List<GroupMemberVO> findGroupMembers(Long groupId) {
        Group group = this.GetById(groupId);
        List<GroupMember> members = groupMemberService.findByGroupId(groupId);
        List<Long> userIds = members.stream().map(GroupMember::getUserId).collect(Collectors.toList());
        List<User> userList = userService.listByIds(userIds);
        List<Long> onlineUserIds = imClient.getOnlineUser(userIds);
        Map<Long, User> userMap = userList.stream().collect(Collectors.toMap(User::getId, Function.identity()));

        Map<Long, List<Long>> characterUserMap = new HashMap<>();
        return members.stream().map(m -> {
            GroupMemberVO vo = BeanUtils.copyProperties(m, GroupMemberVO.class);
            User user = userMap.get(vo.getUserId());
            if (user.getId().equals(group.getOwnerId())) {
                vo.setIsAdmin(true);
            }

            // 是角色群聊计算用户角色序号
            if (GroupTypeEnum.CHARTERS.getCode().equals(group.getGroupType()) && !vo.getQuit()) {
                List<Long> userIdList = characterUserMap.getOrDefault(vo.getTemplateCharacterId(), new ArrayList<>());
                userIdList.add(vo.getUserId());
                vo.setCharacterNum(userIdList.size());
                characterUserMap.put(vo.getTemplateCharacterId(), userIdList);
            }
            if (!GroupTypeEnum.COMMON.getCode().equals(group.getGroupType())) {
                if (StringUtils.isNotBlank(vo.getAliasNamePrefix())) {
                    vo.setAliasName(vo.getAliasNamePrefix() + vo.getAliasName());
                }
                if (StringUtils.isNotBlank(vo.getAliasNameSuffix())) {
                    vo.setAliasName(vo.getAliasName() + vo.getAliasNameSuffix());
                }
            }
            vo.setGroupId(String.valueOf(group.getId()));
            if (StringUtils.isNotBlank(m.getNickname())) {
                vo.setNickName(m.getNickname());
            } else {
                vo.setNickName(user.getNickName());
            }
            vo.setUserAvatar(user.getHeadImage());
            vo.setOnline(onlineUserIds.contains(m.getUserId()));
            return vo;
        }).sorted((m1,m2)-> m2.getOnline().compareTo(m1.getOnline())).collect(Collectors.toList());
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public GroupVO createTemplateGroup(TemplateGroupCreateVO templateGroupCreateVO) {
        if (Arrays.stream(GroupTypeEnum.values()).noneMatch(item -> item.getCode().equals(templateGroupCreateVO.getGroupType()))
        || GroupTypeEnum.COMMON.getCode().equals(templateGroupCreateVO.getGroupType())) {
            throw new GlobalException("群聊类型错误");
        }
        UserSession session = SessionContext.getSession();
        User user = userService.getById(session.getUserId());

        Group group = new Group();
        GroupMember groupMember = new GroupMember();
        TemplateCharacter templateCharacter = null;
        // 判断是否模板群聊
        if (GroupTypeEnum.TEMPLATE.getCode().equals(templateGroupCreateVO.getGroupType())
                || GroupTypeEnum.TEMPLATE_MULT_CHARTER.getCode().equals(templateGroupCreateVO.getGroupType())) {
            if (ObjectUtil.isNull(templateGroupCreateVO.getTemplateGroupId())) {
                throw new GlobalException("群聊模板id为空");
            }
            // 查询模板群聊
            TemplateGroup templateGroup = templateGroupService.findPublishedById(templateGroupCreateVO.getTemplateGroupId());
            if (ObjectUtil.isNull(templateGroup)) {
                throw new GlobalException("所选群聊模板异常");
            }
            group.setName(templateGroup.getGroupName());
            group.setIsTemplate(true);
            group.setHeadImage(templateGroup.getAvatar());
            group.setHeadImageThumb(templateGroup.getAvatar());
            group.setTemplateGroupId(templateGroup.getId());

            if (ObjectUtil.isNull(templateGroupCreateVO.getTemplateCharacterId())) {
                throw new GlobalException("模板角色数据异常");
            }
            // 判断角色是否存在当前群聊模板
            templateCharacter = templateCharacterService.findPublishedById(templateGroupCreateVO.getTemplateCharacterId());
            if (ObjectUtil.isNull(templateCharacter) || !templateCharacter.getTemplateGroupId().equals(templateGroup.getId())) {
                throw new GlobalException("所选模板角色异常");
            }
        } else {
            group.setName(templateGroupCreateVO.getName());
            group.setHeadImage(templateGroupCreateVO.getHeadImage());
            group.setHeadImageThumb(templateGroupCreateVO.getHeadImage());

            templateCharacter = templateCharacterService.findPublishedById(templateGroupCreateVO.getTemplateCharacterId());
            if (ObjectUtil.isNull(templateCharacter)) {
                throw new GlobalException("所选模板角色异常");
            }
        }
        group.setGroupType(templateGroupCreateVO.getGroupType());
        group.setOwnerId(user.getId());
        group.setRemark(group.getName());

        groupMember.setUserId(user.getId());
        groupMember.setRemark(group.getName());
        groupMember.setAliasName(templateCharacter.getName());
        groupMember.setHeadImage(templateCharacter.getAvatar());
        groupMember.setTemplateCharacterId(templateCharacter.getId());
        groupMember.setIsTemplate(true);

        // 保存群组数据
        this.save(group);

        groupMember.setGroupId(group.getId());
        // 把群主加入群
        groupMemberService.save(groupMember);

        GroupVO vo = BeanUtils.copyProperties(group, GroupVO.class);
        assert vo != null;
        vo.setAliasName(groupMember.getAliasName());
        vo.setRemark(group.getName());
        log.info("创建群聊，群聊id:{},群聊名称:{}", group.getId(), group.getName());
        return vo;
    }

    @CacheEvict(key = "#vo.getGroupId()")
    @Lock(prefix = "im:group:member:modify", key = "#vo.getGroupId()")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public GroupVO switchTemplateGroup(SwitchTemplateGroupVO vo) {
        // 判断是否群主
        UserSession session = SessionContext.getSession();
        User user = userService.getById(session.getUserId());
        Group group = baseMapper.selectById(vo.getGroupId());
        if (!user.getId().equals(group.getOwnerId())) {
            throw new GlobalException("您不是群主");
        }
        // 判断上次切换时间与当前时间间隔
        Date switchTime = group.getSwitchTime();
        if (switchTime != null) {
            long interval = (new Date().getTime() - switchTime.getTime()) / 1000;
            if (interval < Constant.SWITCH_INTERVAL) {
                throw new GlobalException("距离上次切换群聊类型未超过30分钟");
            }
        }

        // 判断当前群聊里的模板人物是否重复
        List<GroupMemberVO> groupMembers = vo.getGroupMembers().stream()
                .filter(item -> !item.getQuit()).collect(Collectors.toList());
        List<Long> characterIds = groupMembers.stream()
                .map(GroupMemberVO::getTemplateCharacterId)
                .distinct()
                .collect(Collectors.toList());
        if (characterIds.size() != groupMembers.size()) {
            throw new GlobalException("当前模板群聊存在模板人物重复，请重新选择");
        }

        TemplateGroup templateGroup = templateGroupService.findPublishedById(vo.getTemplateGroupId());
        if (ObjectUtil.isNull(templateGroup) || templateGroup.getDeleted()) {
            throw new GlobalException("当前群聊模板已被删除");
        }
        // 判断模板角色是否存在当前模板群聊
        List<TemplateCharacter> templateCharacters = templateCharacterService.findPublishedByTemplateGroupIdAndCharacterIds(vo.getTemplateGroupId(), characterIds);
        if (templateCharacters.size() != characterIds.size()) {
            throw new GlobalException("部分模板角色不存在当前群聊模板");
        }
        Map<Long, TemplateCharacter> templateCharacterMap = templateCharacters.stream().collect(Collectors.toMap(TemplateCharacter::getId, Function.identity()));

        group.setGroupType(GroupTypeEnum.TEMPLATE.getCode());
        group.setTemplateGroupId(templateGroup.getId());
        group.setName(templateGroup.getGroupName());
        group.setHeadImage(templateGroup.getAvatar());
        group.setHeadImageThumb(templateGroup.getAvatar());
        group.setIsTemplate(true);
        group.setSwitchTime(new Date());
        group.setRemark(templateGroup.getGroupName());

        Map<Long, GroupMemberVO> groupMemberMap = groupMembers.stream().collect(Collectors.toMap(GroupMemberVO::getUserId, Function.identity()));

        List<GroupMember> noQuitGroupMembers = groupMemberService.findNoQuitGroupMembers(vo.getGroupId());
        for (GroupMember groupMember : noQuitGroupMembers) {
            if (!groupMemberMap.containsKey(groupMember.getUserId())) {
                throw new GlobalException("有群聊成员未配置模板人物");
            }
            GroupMemberVO groupMemberVO = groupMemberMap.get(groupMember.getUserId());
            groupMember.setTemplateCharacterId(groupMemberVO.getTemplateCharacterId());
            if (!templateCharacterMap.containsKey(groupMemberVO.getTemplateCharacterId())) {
                throw new GlobalException("数据异常");
            }
            groupMember.setAliasName(templateCharacterMap.get(groupMemberVO.getTemplateCharacterId()).getName());
            groupMember.setHeadImage(templateCharacterMap.get(groupMemberVO.getTemplateCharacterId()).getAvatar());
            groupMember.setRemark(group.getName());
            groupMember.setIsTemplate(true);
            groupMember.setCharacterAvatarId(null);
            groupMember.setAvatarAlias(null);
        }
        baseMapper.updateById(group);
        groupMemberService.saveOrUpdateBatch(group.getId(), noQuitGroupMembers);
        GroupVO groupVO = BeanUtils.copyProperties(group, GroupVO.class);
        groupVO.setAliasName(groupMemberMap.get(user.getId()).getTemplateCharacterName());

        messageSendUtil.sendTipMessage(group.getId(),
                session.getUserId(), session.getNickName(), Collections.emptyList(),
                "群主将群聊类型切换到模板群聊【" + group.getName() + "】", GroupChangeTypeEnum.GROUP_TYPE_CHANGE.getCode());
        return groupVO;
    }

    @CacheEvict(key = "#vo.getGroupId()")
    @Lock(prefix = "im:group:member:modify", key = "#vo.getGroupId()")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public GroupVO switchCommonGroup(CommonGroupVO vo) {
        // 判断是否群主
        UserSession session = SessionContext.getSession();
        User user = userService.getById(session.getUserId());
        Group group = baseMapper.selectById(vo.getGroupId());
        if (GroupTypeEnum.COMMON.getCode().equals(group.getGroupType())) {
            throw new GlobalException("当前群聊已是普通群聊");
        }
        if (!user.getId().equals(group.getOwnerId())) {
            throw new GlobalException("您不是群主");
        }
        // 判断上次切换时间与当前时间间隔
        Date switchTime = group.getSwitchTime();
        if (switchTime != null) {
            long interval = (new Date().getTime() - switchTime.getTime()) / 1000;
            if (interval < Constant.SWITCH_INTERVAL) {
                throw new GlobalException("距离上次切换群聊类型未超过30分钟");
            }
        }
        // 查询群聊所有用户
        List<GroupMember> groupMemberList = groupMemberService.findByGroupId(vo.getGroupId());
        List<Long> userIds = groupMemberList.stream().map(GroupMember::getUserId).collect(Collectors.toList());

        // 查询所有用户信息
        List<User> userList = userService.listByIds(userIds);
        // 根据用户id分组
        Map<Long, User> userMap = userList.stream().collect(Collectors.toMap(User::getId, Function.identity()));

        // 设置群用户信息
        for (GroupMember groupMember : groupMemberList) {
            groupMember.setIsTemplate(false);
            groupMember.setTemplateCharacterId(0L);
            groupMember.setRemark(vo.getName());
            groupMember.setCharacterAvatarId(null);
            groupMember.setAvatarAlias(null);
            groupMember.setShowNickName(false);
            if (userMap.containsKey(groupMember.getUserId())) {
                User userInfo = userMap.get(groupMember.getUserId());
                groupMember.setHeadImage(userInfo.getHeadImage());
                groupMember.setAliasName(userInfo.getNickName());
            }
        }

        Map<Long, GroupMember> groupMemberMap = groupMemberList.stream().collect(Collectors.toMap(GroupMember::getUserId, Function.identity()));

        // 设置群聊信息
        group.setGroupType(GroupTypeEnum.COMMON.getCode());
        group.setIsTemplate(false);
        group.setTemplateGroupId(null);
        group.setName(vo.getName());
        group.setRemark(vo.getName());
        if (StringUtils.isNotBlank(vo.getAvatar())) {
            group.setHeadImage(vo.getAvatar());
            group.setHeadImageThumb(vo.getAvatar());
        } else {
            group.setHeadImage("");
            group.setHeadImageThumb("");
        }
        group.setSwitchTime(new Date());
        // 更新群信息
        baseMapper.updateById(group);
        // 更新群用户信息
        groupMemberService.saveOrUpdateBatch(vo.getGroupId(), groupMemberList);
        GroupVO groupVO = BeanUtils.copyProperties(group, GroupVO.class);
        groupVO.setAliasName(groupMemberMap.get(user.getId()).getAliasName());
        messageSendUtil.sendTipMessage(group.getId(),
                session.getUserId(), session.getNickName(), Collections.emptyList(),
                "群主将群聊类型切换到普通群聊【" + group.getName() + "】", GroupChangeTypeEnum.GROUP_TYPE_CHANGE.getCode());
        return groupVO;
    }

    @CacheEvict(key = "#vo.getGroupId()")
    @Lock(prefix = "im:group:member:modify", key = "#vo.getGroupId()")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public GroupVO switchMultCharacterGroup(MultCharacterGroupVO vo) {
        // 判断是否群主
        UserSession session = SessionContext.getSession();
        User user = userService.getById(session.getUserId());
        Group group = baseMapper.selectById(vo.getGroupId());
        if (GroupTypeEnum.MULT_CHARTER.getCode().equals(group.getGroupType())) {
            throw new GlobalException("当前群聊已是多元角色群聊");
        }
        if (!user.getId().equals(group.getOwnerId())) {
            throw new GlobalException("您不是群主");
        }
        // 判断上次切换时间与当前时间间隔
        Date switchTime = group.getSwitchTime();
        if (switchTime != null) {
            long interval = (new Date().getTime() - switchTime.getTime()) / 1000;
            if (interval < Constant.SWITCH_INTERVAL) {
                throw new GlobalException("距离上次切换群聊类型未超过30分钟");
            }
        }

        // 判断当前群聊里的模板人物是否重复
        List<GroupMemberVO> groupMembers = vo.getGroupMembers().stream()
                .filter(item -> !item.getQuit()).collect(Collectors.toList());
        List<Long> characterIds = groupMembers.stream()
                .map(GroupMemberVO::getTemplateCharacterId)
                .distinct()
                .collect(Collectors.toList());
        if (characterIds.size() != groupMembers.size()) {
            throw new GlobalException("存在模板角色重复，请重新选择");
        }
        // 判断模板角色是否存在
        List<TemplateCharacter> characterList = templateCharacterService.findPublishedByCharacterIds(characterIds);
        if (characterList.size() != characterIds.size()) {
            throw new GlobalException("部分模板角色不存在");
        }

        Map<Long, TemplateCharacter> templateCharacterMap = characterList.stream().collect(Collectors.toMap(TemplateCharacter::getId, Function.identity()));

        if (StringUtils.isNotBlank(vo.getName())) {
            group.setName(vo.getName());
        }
        group.setGroupType(GroupTypeEnum.MULT_CHARTER.getCode());
        group.setTemplateGroupId(null);
        group.setIsTemplate(false);
        group.setSwitchTime(new Date());
        group.setRemark(vo.getName());
        if (StringUtils.isNotBlank(vo.getAvatar())) {
            group.setHeadImage(vo.getAvatar());
            group.setHeadImageThumb(vo.getAvatar());
        } else {
            group.setHeadImage("");
            group.setHeadImageThumb("");
        }

        Map<Long, GroupMemberVO> groupMemberMap = groupMembers.stream().collect(Collectors.toMap(GroupMemberVO::getUserId, Function.identity()));

        List<GroupMember> noQuitGroupMembers = groupMemberService.findNoQuitGroupMembers(vo.getGroupId());
        for (GroupMember groupMember : noQuitGroupMembers) {
            if (!groupMemberMap.containsKey(groupMember.getUserId())) {
                throw new GlobalException("有群聊成员未配置模板人物");
            }
            GroupMemberVO groupMemberVO = groupMemberMap.get(groupMember.getUserId());
            groupMember.setTemplateCharacterId(groupMemberVO.getTemplateCharacterId());
            if (!templateCharacterMap.containsKey(groupMemberVO.getTemplateCharacterId())) {
                throw new GlobalException("数据异常");
            }
            groupMember.setAliasName(templateCharacterMap.get(groupMemberVO.getTemplateCharacterId()).getName());
            groupMember.setHeadImage(templateCharacterMap.get(groupMemberVO.getTemplateCharacterId()).getAvatar());
            groupMember.setRemark(group.getName());
            groupMember.setIsTemplate(true);
            groupMember.setCharacterAvatarId(null);
            groupMember.setAvatarAlias(null);
        }
        baseMapper.updateById(group);
        groupMemberService.saveOrUpdateBatch(group.getId(), noQuitGroupMembers);

        GroupVO groupVO = BeanUtils.copyProperties(group, GroupVO.class);
        assert groupVO != null;
        groupVO.setAliasName(groupMemberMap.get(user.getId()).getTemplateCharacterName());

        messageSendUtil.sendTipMessage(group.getId(),
                session.getUserId(), session.getNickName(), Collections.emptyList(),
                "群主将群聊类型切换到多元角色群聊【" + group.getName() + "】", GroupChangeTypeEnum.GROUP_TYPE_CHANGE.getCode());
        return groupVO;
    }

    @CacheEvict(key = "#vo.getGroupId()")
    @Lock(prefix = "im:group:member:modify", key = "#vo.getGroupId()")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public GroupVO switchMoreCharactersGroup(MultCharacterGroupVO vo) {
        // 判断是否群主
        UserSession session = SessionContext.getSession();
        User user = userService.getById(session.getUserId());
        Group group = baseMapper.selectById(vo.getGroupId());
        if (GroupTypeEnum.CHARTERS.getCode().equals(group.getGroupType())) {
            throw new GlobalException("当前群聊已是角色群聊");
        }
        if (!user.getId().equals(group.getOwnerId())) {
            throw new GlobalException("您不是群主");
        }
        // 判断上次切换时间与当前时间间隔
        Date switchTime = group.getSwitchTime();
        if (switchTime != null) {
            long interval = (new Date().getTime() - switchTime.getTime()) / 1000;
            if (interval < Constant.SWITCH_INTERVAL) {
                throw new GlobalException("距离上次切换群聊类型未超过30分钟");
            }
        }

        Map<Long, List<GroupMemberVO>> characterMap = vo.getGroupMembers().stream().collect(Collectors.groupingBy(GroupMemberVO::getTemplateCharacterId));
        if (MapUtils.isEmpty(characterMap)) {
            throw new GlobalException("用户未分配角色");
        }
        // 判断每种角色人数是否超过10
        characterMap.forEach((key, value) -> {
            if (value.size() > Constant.MAX_CHARACTER_NUM) {
                throw new GlobalException("每个角色的人数不能超过" + Constant.MAX_CHARACTER_NUM);
            }
        });

        // 判断角色是否存在
        List<TemplateCharacter> characters = templateCharacterService.findPublishedByCharacterIds(new ArrayList<>(characterMap.keySet()));
        if (characters.size() != characterMap.keySet().size()) {
            throw new GlobalException("部分角色不存在");
        }
        Map<Long, TemplateCharacter> templateCharacterMap = characters.stream().collect(Collectors.toMap(TemplateCharacter::getId, Function.identity()));

        Map<Long, GroupMemberVO> groupMemberMap = vo.getGroupMembers().stream().collect(Collectors.toMap(GroupMemberVO::getUserId, Function.identity()));

        List<GroupMember> noQuitGroupMembers = groupMemberService.findNoQuitGroupMembers(vo.getGroupId());
        for (GroupMember groupMember : noQuitGroupMembers) {
            if (!groupMemberMap.containsKey(groupMember.getUserId())) {
                throw new GlobalException("有群聊成员未配置模板人物");
            }
            GroupMemberVO groupMemberVO = groupMemberMap.get(groupMember.getUserId());
            groupMember.setTemplateCharacterId(groupMemberVO.getTemplateCharacterId());
            if (!templateCharacterMap.containsKey(groupMemberVO.getTemplateCharacterId())) {
                throw new GlobalException("数据异常");
            }
            groupMember.setAliasName(templateCharacterMap.get(groupMemberVO.getTemplateCharacterId()).getName());
            groupMember.setHeadImage(templateCharacterMap.get(groupMemberVO.getTemplateCharacterId()).getAvatar());
            groupMember.setRemark(group.getName());
            groupMember.setIsTemplate(true);
            groupMember.setCharacterAvatarId(null);
            groupMember.setAvatarAlias(null);
        }
        if (StringUtils.isNotBlank(vo.getName())) {
            group.setName(vo.getName());
        }
        group.setGroupType(GroupTypeEnum.CHARTERS.getCode());
        group.setTemplateGroupId(null);
        group.setIsTemplate(false);
        group.setSwitchTime(new Date());
        group.setRemark(vo.getName());
        if (StringUtils.isNotBlank(vo.getAvatar())) {
            group.setHeadImage(vo.getAvatar());
            group.setHeadImageThumb(vo.getAvatar());
        } else {
            group.setHeadImage("");
            group.setHeadImageThumb("");
        }
        baseMapper.updateById(group);
        groupMemberService.saveOrUpdateBatch(group.getId(), noQuitGroupMembers);

        GroupVO groupVO = BeanUtils.copyProperties(group, GroupVO.class);
        assert groupVO != null;
        groupVO.setAliasName(groupMemberMap.get(user.getId()).getTemplateCharacterName());

        messageSendUtil.sendTipMessage(group.getId(),
                session.getUserId(), session.getNickName(), Collections.emptyList(),
                "群主将群聊类型切换到角色群聊【" + group.getName() + "】", GroupChangeTypeEnum.GROUP_TYPE_CHANGE.getCode());
        return groupVO;
    }

    @CacheEvict(key = "#vo.getGroupId()")
    @Lock(prefix = "im:group:member:modify", key = "#vo.getGroupId()")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public GroupVO switchTemplateMultCharactersGroup(SwitchTemplateGroupVO vo) {
        // 判断是否群主
        UserSession session = SessionContext.getSession();
        User user = userService.getById(session.getUserId());
        Group group = baseMapper.selectById(vo.getGroupId());
        if (GroupTypeEnum.TEMPLATE_MULT_CHARTER.getCode().equals(group.getGroupType())) {
            throw new GlobalException("当前群聊已是模板多元角色群聊");
        }
        if (!user.getId().equals(group.getOwnerId())) {
            throw new GlobalException("您不是群主");
        }
        // 判断上次切换时间与当前时间间隔
        Date switchTime = group.getSwitchTime();
        if (switchTime != null) {
            long interval = (new Date().getTime() - switchTime.getTime()) / 1000;
            if (interval < Constant.SWITCH_INTERVAL) {
                throw new GlobalException("距离上次切换群聊类型未超过30分钟");
            }
        }

        Map<Long, List<GroupMemberVO>> characterMap = vo.getGroupMembers().stream().collect(Collectors.groupingBy(GroupMemberVO::getTemplateCharacterId));
        if (MapUtils.isEmpty(characterMap)) {
            throw new GlobalException("用户未分配角色");
        }
        // 判断每种角色人数是否超过10
        characterMap.forEach((key, value) -> {
            if (value.size() > Constant.MAX_CHARACTER_NUM) {
                throw new GlobalException("每个角色的人数不能超过" + Constant.MAX_CHARACTER_NUM);
            }
        });

        List<GroupMemberVO> groupMembers = vo.getGroupMembers().stream()
                .filter(item -> !item.getQuit()).collect(Collectors.toList());

        Set<Long> characterIds = characterMap.keySet();

        TemplateGroup templateGroup = templateGroupService.findPublishedById(vo.getTemplateGroupId());
        if (ObjectUtil.isNull(templateGroup) || templateGroup.getDeleted()) {
            throw new GlobalException("当前群聊模板已被删除");
        }
        // 判断模板角色是否存在当前模板群聊
        List<TemplateCharacter> templateCharacters = templateCharacterService.findPublishedByTemplateGroupIdAndCharacterIds(vo.getTemplateGroupId(), new ArrayList<>(characterIds));
        if (templateCharacters.size() != characterIds.size()) {
            throw new GlobalException("部分模板角色不存在当前群聊模板");
        }
        Map<Long, TemplateCharacter> templateCharacterMap = templateCharacters.stream().collect(Collectors.toMap(TemplateCharacter::getId, Function.identity()));
        group.setGroupType(GroupTypeEnum.TEMPLATE_MULT_CHARTER.getCode());
        group.setTemplateGroupId(templateGroup.getId());
        group.setName(templateGroup.getGroupName());
        group.setHeadImage(templateGroup.getAvatar());
        group.setHeadImageThumb(templateGroup.getAvatar());
        group.setIsTemplate(true);
        group.setSwitchTime(new Date());
        group.setRemark(templateGroup.getGroupName());

        Map<Long, GroupMemberVO> groupMemberMap = groupMembers.stream().collect(Collectors.toMap(GroupMemberVO::getUserId, Function.identity()));

        List<GroupMember> noQuitGroupMembers = groupMemberService.findNoQuitGroupMembers(vo.getGroupId());
        for (GroupMember groupMember : noQuitGroupMembers) {
            if (!groupMemberMap.containsKey(groupMember.getUserId())) {
                throw new GlobalException("有群聊成员未配置模板人物");
            }
            GroupMemberVO groupMemberVO = groupMemberMap.get(groupMember.getUserId());
            groupMember.setTemplateCharacterId(groupMemberVO.getTemplateCharacterId());
            if (!templateCharacterMap.containsKey(groupMemberVO.getTemplateCharacterId())) {
                throw new GlobalException("数据异常");
            }
            groupMember.setAliasName(templateCharacterMap.get(groupMemberVO.getTemplateCharacterId()).getName());
            groupMember.setHeadImage(templateCharacterMap.get(groupMemberVO.getTemplateCharacterId()).getAvatar());
            groupMember.setRemark(group.getName());
            groupMember.setIsTemplate(true);
            groupMember.setCharacterAvatarId(null);
            groupMember.setAvatarAlias(null);
        }
        baseMapper.updateById(group);
        groupMemberService.saveOrUpdateBatch(group.getId(), noQuitGroupMembers);
        GroupVO groupVO = BeanUtils.copyProperties(group, GroupVO.class);
        groupVO.setAliasName(groupMemberMap.get(user.getId()).getTemplateCharacterName());

        messageSendUtil.sendTipMessage(group.getId(),
                session.getUserId(), session.getNickName(), Collections.emptyList(),
                "群主将群聊类型切换到模板多元角色群聊【" + group.getName() + "】", GroupChangeTypeEnum.GROUP_TYPE_CHANGE.getCode());
        return groupVO;
    }

    @CacheEvict(key = "#groupId")
    @Lock(prefix = "im:group:member:modify", key = "#groupId")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public GroupMember addToCommonGroup(User user, Long groupId) {
        Group group = baseMapper.selectById(groupId);
        if (ObjectUtil.isNull(group)) {
            return null;

        }
        GroupMember groupMember = new GroupMember();
        // 是模板群聊
        if (group.getIsTemplate()) {
            // 查询存在当前模板群聊的模板角色id
            List<GroupMember> groupMembers = groupMemberService.findNoQuitGroupMembers(group.getId());
            // 过滤得到所有已存在的模板角色id
            List<Long> templateCharacterIds = groupMembers.stream()
                    .filter(item -> item.getIsTemplate())
                    .map(GroupMember::getTemplateCharacterId)
                    .collect(Collectors.toList());
            // 查询当前模板群聊还未分配的模板角色
            List<TemplateCharacter> templateCharacters = templateCharacterService.lambdaQuery()
                    .eq(TemplateCharacter::getTemplateGroupId, group.getTemplateGroupId())
                    .eq(TemplateCharacter::getDeleted, false)
                    .notIn(CollectionUtils.isNotEmpty(templateCharacterIds), TemplateCharacter::getId, templateCharacterIds)
                    .list();
            if (CollectionUtils.isEmpty(templateCharacters)) {
                return null;
            }
            // 不为空取第一个模板角色分配当前注册用户
            TemplateCharacter templateCharacter = templateCharacters.get(0);

            groupMember.setGroupId(group.getId());
            groupMember.setUserId(user.getId());
            groupMember.setAliasName(templateCharacter.getName());
            groupMember.setHeadImage(templateCharacter.getAvatar());
            groupMember.setIsTemplate(true);
            groupMember.setTemplateCharacterId(templateCharacter.getId());
            groupMember.setRemark(group.getName());
        } else {
            groupMember.setGroupId(group.getId());
            groupMember.setUserId(user.getId());
            groupMember.setAliasName(user.getNickName());
            groupMember.setHeadImage(user.getHeadImage());
            groupMember.setIsTemplate(false);
            groupMember.setRemark(group.getName());
        }
        groupMemberService.saveOrUpdateBatch(group.getId(), Collections.singletonList(groupMember));
        return groupMember;
    }

    @Override
    public PageResultVO queryNotJoinGroups(String keyWord) {
        UserSession session = SessionContext.getSession();
        Long userId = session.getUserId();

        // 查询用户未加入的群聊
        // 先查询当前用户的群id列表
        List<GroupMember> groupMembers = groupMemberService.findByUserId(userId);
        // 已经加入的群id
        List<Long> hasJoinGroupIds = groupMembers.stream().map((GroupMember::getGroupId)).collect(Collectors.toList());

        // 查询未加入的群聊
        LambdaQueryWrapper<Group> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Group::getDeleted, false);
        if (CollectionUtils.isNotEmpty(hasJoinGroupIds)) {
            queryWrapper.notIn(Group::getId, hasJoinGroupIds);
        }
        queryWrapper.like(StringUtils.isNotBlank(keyWord), Group::getName, keyWord);
        queryWrapper.orderByAsc(Group::getCreatedTime);
        Page<Group> page = this.page(new Page<>(PageUtils.getPageNo(), PageUtils.getPageSize()), queryWrapper);
        if (CollectionUtils.isEmpty(page.getRecords())) {
            return PageResultVO.builder().data(Collections.emptyList()).build();
        }
        List<GroupVO> groupVOList = BeanUtils.copyPropertiesList(page.getRecords(), GroupVO.class);
        return PageResultVO.builder().data(groupVOList).total(page.getTotal()).build();
    }

    @Lock(prefix = "im:group:member:modify", key = "#vo.getGroupId()")
    @Override
    public GroupVO joinGroup(GroupJoinVO vo) {
        UserSession session = SessionContext.getSession();
        Long userId = session.getUserId();

        User user = userService.getById(userId);
        // 判断群聊是否存在
        Group group = this.getById(vo.getGroupId());
        if (group == null || group.getDeleted()) {
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "群聊不存在");
        }
        if (!group.getGroupType().equals(vo.getGroupType())) {
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "群聊类型有误");
        }
        // 群聊人数校验
        List<GroupMember> members = groupMemberService.findByGroupId(vo.getGroupId());
        long size = members.stream().filter(m -> !m.getQuit()).count();
        if (size >= Constant.MAX_GROUP_MEMBER) {
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "当前群聊人数已达上限");
        }
        Optional<GroupMember> optional = members.stream().filter(m -> m.getUserId().equals(userId)).findFirst();
        if (optional.isPresent() && !optional.get().getQuit()) {
            throw new GlobalException("您已加入当前群聊");
        }
        GroupMember member = optional.orElseGet(GroupMember::new);
        // 不是模板群聊
        if (GroupTypeEnum.COMMON.getCode().equals(group.getGroupType())) {
            member.setGroupId(vo.getGroupId());
            member.setUserId(userId);
            member.setAliasName(user.getNickName());
            member.setRemark(group.getName());
            member.setHeadImage(user.getHeadImage());
            member.setIsTemplate(false);
            member.setCreatedTime(new Date());
            member.setQuit(false);
            member.setCharacterAvatarId(null);
            member.setAvatarAlias(null);
            groupMemberService.saveOrUpdateBatch(group.getId(), Collections.singletonList(member));
        } else if (GroupTypeEnum.TEMPLATE.getCode().equals(group.getGroupType())) {
            if (ObjectUtil.isNull(vo.getTemplateGroupId()) || ObjectUtil.isNull(vo.getTemplateCharacterId())) {
                throw new GlobalException("参数异常");
            }
            Long templateGroupId = vo.getTemplateGroupId();
            if (!group.getTemplateGroupId().equals(templateGroupId)) {
                throw new GlobalException("群聊类型已改变，请重新操作");
            }
            Long templateCharacterId = vo.getTemplateCharacterId();
            // 判断用户选择的模板人物是否已存在
            GroupMember groupMember = members.stream().filter(m -> Objects.equals(m.getTemplateCharacterId(), templateCharacterId) && !m.getQuit()).findFirst().orElse(null);
            if (ObjectUtil.isNotNull(groupMember)) {
                throw new GlobalException("当前模板人物已有用户选择，请重新选择");
            }
            // 判断当前模板人物是否存在模板群聊中
            List<TemplateCharacter> templateCharacter = templateCharacterService
                    .findPublishedByTemplateGroupIdAndCharacterIds(group.getTemplateGroupId(), Collections.singletonList(vo.getTemplateCharacterId()));
            if (CollectionUtils.isEmpty(templateCharacter)) {
                throw new GlobalException("所选模板人物不存在于当前模板群聊");
            }
            member = optional.orElseGet(GroupMember::new);
            member.setGroupId(vo.getGroupId());
            member.setUserId(userId);
            member.setRemark(group.getName());
            member.setAliasName(templateCharacter.get(0).getName());
            member.setHeadImage(templateCharacter.get(0).getAvatar());
            member.setTemplateCharacterId(templateCharacterId);
            member.setIsTemplate(true);
            member.setCreatedTime(new Date());
            member.setQuit(false);
            member.setCharacterAvatarId(null);
            member.setAvatarAlias(null);
            groupMemberService.saveOrUpdateBatch(group.getId(), Collections.singletonList(member));
        } else if (GroupTypeEnum.MULT_CHARTER.getCode().equals(vo.getGroupType())) {
            if (ObjectUtil.isNull(vo.getTemplateCharacterId())) {
                throw new GlobalException("参数异常");
            }

            Long templateCharacterId = vo.getTemplateCharacterId();
            // 判断用户选择的模板人物是否已存在
            GroupMember groupMember = members.stream().filter(m -> Objects.equals(m.getTemplateCharacterId(), templateCharacterId) && !m.getQuit()).findFirst().orElse(null);
            if (ObjectUtil.isNotNull(groupMember)) {
                throw new GlobalException("当前模板人物已有用户选择，请重新选择");
            }
            // 判断当前模板人物是否存在
            TemplateCharacter templateCharacter = templateCharacterService.findPublishedById(vo.getTemplateCharacterId());
            if (ObjectUtil.isNull(templateCharacter)) {
                throw new GlobalException("所选模板角色不存在");
            }
            member = optional.orElseGet(GroupMember::new);
            member.setGroupId(vo.getGroupId());
            member.setUserId(userId);
            member.setAliasName(templateCharacter.getName());
            member.setHeadImage(templateCharacter.getAvatar());
            member.setTemplateCharacterId(templateCharacterId);
            member.setIsTemplate(true);
            member.setRemark(group.getName());
            member.setCreatedTime(new Date());
            member.setQuit(false);
            member.setCharacterAvatarId(null);
            member.setAvatarAlias(null);
            groupMemberService.saveOrUpdateBatch(group.getId(), Collections.singletonList(member));
        } else if (GroupTypeEnum.CHARTERS.getCode().equals(vo.getGroupType())
            || GroupTypeEnum.TEMPLATE_MULT_CHARTER.getCode().equals(vo.getGroupType())) {
            if (ObjectUtil.isNull(vo.getTemplateCharacterId())) {
                throw new GlobalException("参数异常");
            }
            Long templateCharacterId = vo.getTemplateCharacterId();
            // 判断当前模板角色是否存在
            TemplateCharacter templateCharacter = null;

            if (GroupTypeEnum.TEMPLATE_MULT_CHARTER.getCode().equals(vo.getGroupType())) {
                if (ObjectUtil.isNull(vo.getTemplateGroupId())) {
                    throw new GlobalException("参数异常");
                }
                Long templateGroupId = vo.getTemplateGroupId();
                if (!group.getTemplateGroupId().equals(templateGroupId)) {
                    throw new GlobalException("群聊类型已改变，请重新操作");
                }
                // 判断群聊模板的是否存在所选模板角色
                List<TemplateCharacter> templateCharacters = templateCharacterService.findPublishedByTemplateGroupIdAndCharacterIds(templateGroupId, Collections.singletonList(templateCharacterId));
                if (CollectionUtils.isEmpty(templateCharacters)) {
                    throw new GlobalException("所选模板角色不存在");
                }
                templateCharacter = templateCharacters.get(0);
            } else {
                templateCharacter = templateCharacterService.findPublishedById(templateCharacterId);
                if (ObjectUtil.isNull(templateCharacter)) {
                    throw new GlobalException("所选模板角色不存在");
                }
            }


            // 判断用户选择的模板人物在当前群聊用户数量是否超过10个
            long count = members.stream().filter(m -> Objects.equals(m.getTemplateCharacterId(), templateCharacterId) && !m.getQuit()).count();
            if (count >= Constant.MAX_CHARACTER_NUM) {
                throw new GlobalException("所选角色在当前群聊的用户数量已超过" + Constant.MAX_CHARACTER_NUM + "个");
            }

            member = optional.orElseGet(GroupMember::new);
            member.setGroupId(vo.getGroupId());
            member.setUserId(userId);
            member.setAliasName(templateCharacter.getName());
            member.setHeadImage(templateCharacter.getAvatar());
            member.setTemplateCharacterId(templateCharacterId);
            member.setIsTemplate(true);
            member.setRemark(group.getName());
            member.setCreatedTime(new Date());
            member.setQuit(false);
            member.setCharacterAvatarId(null);
            member.setAvatarAlias(null);
            groupMemberService.saveOrUpdateBatch(group.getId(), Collections.singletonList(member));
        } else {
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "参数异常");
        }
        log.info("用户{}进入群聊，群聊id:{},群聊名称:{},用户id:{}", user.getUserName(), group.getId(), group.getName(), userId);

        String content = null;
        if (GroupTypeEnum.COMMON.getCode().equals(group.getGroupType())) {
            content = "用户" + session.getNickName() + "加入了群聊";
        } else {
            content = "用户" + session.getNickName() + "【" + member.getAliasName() + "】" + "加入了群聊";
        }
        messageSendUtil.sendTipMessage(group.getId(),
                session.getUserId(), session.getNickName(), Collections.emptyList(),
                content, GroupChangeTypeEnum.NEW_USER_JOIN.getCode());

        GroupVO groupVO = BeanUtils.copyProperties(group, GroupVO.class);
        assert groupVO != null;
        groupVO.setAliasName(member.getAliasName());
        groupVO.setRemark(groupVO.getName());
        return groupVO;
    }

    private void sendTipMessage(Long groupId,List<Long> recvIds,String content){
        UserSession session = SessionContext.getSession();
        // 消息入库
        GroupMessage message = new GroupMessage();
        message.setContent(content);
        message.setType(MessageType.TIP_TEXT.code());
        message.setStatus(MessageStatus.UNSEND.code());
        message.setSendTime(new Date());
        message.setSendNickName(session.getNickName());
        message.setGroupId(groupId);
        message.setSendId(session.getUserId());
        message.setRecvIds(CommaTextUtils.asText(recvIds));
        groupMessageMapper.insert(message);
        // 推送
        GroupMessageVO msgInfo = BeanUtils.copyProperties(message,GroupMessageVO.class);
        IMGroupMessage<GroupMessageVO> sendMessage = new IMGroupMessage<>();
        sendMessage.setSender(new IMUserInfo(session.getUserId(), session.getTerminal()));
        if(CollUtil.isEmpty(recvIds)){
            // 为空表示向全体发送
            List<Long> userIds = groupMemberService.findUserIdsByGroupId(groupId);
            sendMessage.setRecvIds(userIds);
        }else{
            sendMessage.setRecvIds(recvIds);
        }
        sendMessage.setData(msgInfo);
        sendMessage.setSendResult(false);
        sendMessage.setSendToSelf(false);
        imClient.sendGroupMessage(sendMessage);
    }
}

package xyz.qy.implatform.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xyz.qy.imclient.IMClient;
import xyz.qy.imclient.annotation.Lock;
import xyz.qy.imcommon.enums.IMTerminalType;
import xyz.qy.imcommon.model.IMGroupMessage;
import xyz.qy.imcommon.model.IMUserInfo;
import xyz.qy.implatform.contant.Constant;
import xyz.qy.implatform.dto.EnterGroupUsersDTO;
import xyz.qy.implatform.entity.Group;
import xyz.qy.implatform.entity.GroupMember;
import xyz.qy.implatform.entity.GroupMessage;
import xyz.qy.implatform.entity.GroupRequest;
import xyz.qy.implatform.entity.TemplateCharacter;
import xyz.qy.implatform.entity.User;
import xyz.qy.implatform.enums.GroupChangeTypeEnum;
import xyz.qy.implatform.enums.GroupRequestStatusEnum;
import xyz.qy.implatform.enums.GroupRequestTypeEnum;
import xyz.qy.implatform.enums.GroupTypeEnum;
import xyz.qy.implatform.enums.MessageStatus;
import xyz.qy.implatform.enums.MessageType;
import xyz.qy.implatform.enums.ResultCode;
import xyz.qy.implatform.exception.GlobalException;
import xyz.qy.implatform.mapper.GroupRequestMapper;
import xyz.qy.implatform.service.IGroupMemberService;
import xyz.qy.implatform.service.IGroupRequestService;
import xyz.qy.implatform.service.IGroupService;
import xyz.qy.implatform.service.ITemplateCharacterService;
import xyz.qy.implatform.service.IUserService;
import xyz.qy.implatform.session.SessionContext;
import xyz.qy.implatform.session.UserSession;
import xyz.qy.implatform.util.BeanUtils;
import xyz.qy.implatform.util.MessageSendUtil;
import xyz.qy.implatform.vo.GroupJoinVO;
import xyz.qy.implatform.vo.GroupMessageVO;
import xyz.qy.implatform.vo.InviteFriendVO;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 群组请求表 服务实现类
 *
 * @author Polaris
 * @since 2025-09-11
 */
@Slf4j
@Service
public class GroupRequestServiceImpl extends ServiceImpl<GroupRequestMapper, GroupRequest> implements IGroupRequestService {
    @Resource
    private IUserService userService;

    @Resource
    private IGroupService groupService;

    @Resource
    private IGroupMemberService groupMemberService;

    @Resource
    private ITemplateCharacterService templateCharacterService;

    @Resource
    private IMClient imClient;

    @Resource
    private MessageSendUtil messageSendUtil;

    @Resource
    private RedissonClient redissonClient;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveEnterGroupRequestInfo(EnterGroupUsersDTO enterGroupUsersDTO) {
        if (CollectionUtils.isEmpty(enterGroupUsersDTO.getReviewUserList())) {
            return;
        }
        List<InviteFriendVO> reviewUserList = enterGroupUsersDTO.getReviewUserList();
        List<Long> userIds = reviewUserList.stream().map(InviteFriendVO::getFriendId).collect(Collectors.toList());
        List<User> userList = userService.findUserByIds(userIds);
        // userList根据id为key转换成map
        Map<Long, User> userMap = userList.stream().collect(Collectors.toMap(User::getId, Function.identity(), (key1, key2) -> key2));

        List<GroupRequest> groupRequestList = reviewUserList.stream().map(item -> {
            GroupRequest groupRequest = new GroupRequest();
            groupRequest.setLaunchUserId(enterGroupUsersDTO.getLaunchUserId());
            groupRequest.setUserId(item.getFriendId());
            groupRequest.setUserNickname(userMap.get(item.getFriendId()).getUserName());
            groupRequest.setUserHeadImage(userMap.get(item.getFriendId()).getHeadImage());
            groupRequest.setTemplateCharacterId(item.getTemplateCharacterId());
            groupRequest.setGroupId(enterGroupUsersDTO.getGroupId());
            groupRequest.setType(GroupRequestTypeEnum.INVITE_JOIN.getCode());
            groupRequest.setStatus(GroupRequestStatusEnum.APPLYING.getCode());
            groupRequest.setCreateTime(LocalDateTime.now());
            return groupRequest;
        }).collect(Collectors.toList());

        this.saveBatch(groupRequestList);
        log.info("邀请进入群组待用户同意信息,groupId:{}, userIds:{}", enterGroupUsersDTO.getGroupId(), userIds);

        Group group = groupService.getById(enterGroupUsersDTO.getGroupId());
        groupRequestList.forEach(item -> {
            List<Long> recvIds = List.of(item.getUserId(), group.getOwnerId());
            this.sendGroupRequestMessage(item, recvIds, MessageType.GROUP_JOIN_REQUEST.code());
        });
    }

    @Override
    public void saveUserJoinGroupRequestInfo(GroupJoinVO groupJoinVO) {
        // 判断是否已存在加群申请
        LambdaQueryWrapper<GroupRequest> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GroupRequest::getGroupId, groupJoinVO.getGroupId())
                .eq(GroupRequest::getUserId, groupJoinVO.getUserId())
                .eq(GroupRequest::getType, GroupRequestTypeEnum.SELF_JOIN.getCode())
                .eq(GroupRequest::getStatus, GroupRequestStatusEnum.APPLYING.getCode());
        if (this.count(wrapper) > 0) {
            throw new GlobalException("已存在申请记录");
        }

        User user = userService.getById(groupJoinVO.getUserId());

        GroupRequest groupRequest = new GroupRequest();
        groupRequest.setGroupId(groupJoinVO.getGroupId());
        groupRequest.setLaunchUserId(groupJoinVO.getUserId());
        groupRequest.setUserId(groupJoinVO.getUserId());
        groupRequest.setUserNickname(user.getNickName());
        groupRequest.setUserHeadImage(user.getHeadImage());
        groupRequest.setTemplateCharacterId(groupJoinVO.getTemplateCharacterId());
        groupRequest.setType(GroupRequestTypeEnum.SELF_JOIN.getCode());
        groupRequest.setStatus(GroupRequestStatusEnum.APPLYING.getCode());
        groupRequest.setCreateTime(LocalDateTime.now());
        this.save(groupRequest);

        Group group = groupService.getById(groupRequest.getGroupId());
        List<Long> recvIds = List.of(group.getOwnerId(), groupRequest.getUserId());
        this.sendGroupRequestMessage(groupRequest, recvIds, MessageType.GROUP_JOIN_REQUEST.code());
    }

    @Lock(prefix = "im:group:request", key = "#id")
    @Override
    public void recall(Long id) {
        UserSession session = SessionContext.getSession();
        Long userId = session.getUserId();

        GroupRequest groupRequest = this.getById(id);
        if (!groupRequest.getLaunchUserId().equals(userId)) {
            throw new GlobalException("不是您的数据,无权限操作");
        }
        if (!GroupRequestStatusEnum.APPLYING.getCode().equals(groupRequest.getStatus())) {
            throw new GlobalException("当前数据状态不能操作");
        }
        groupRequest.setStatus(GroupRequestStatusEnum.RECALL.getCode());
        this.updateById(groupRequest);

        Group group = groupService.getById(groupRequest.getGroupId());
        List<Long> recvIds = List.of(group.getOwnerId(), groupRequest.getUserId());
        this.sendGroupRequestMessage(groupRequest, recvIds, MessageType.GROUP_JOIN_REQUEST_MODIFY.code());
    }

    @Lock(prefix = "im:group:request", key = "#id")
    @Override
    public void reject(Long id) {
        UserSession session = SessionContext.getSession();
        Long userId = session.getUserId();

        GroupRequest groupRequest = this.getById(id);
        if (!GroupRequestStatusEnum.APPLYING.getCode().equals(groupRequest.getStatus())) {
            throw new GlobalException("当前数据状态不能操作");
        }

        Group group = groupService.getById(groupRequest.getGroupId());
        // 用户主动申请加入，群主拒绝
        if (GroupRequestTypeEnum.SELF_JOIN.getCode().equals(groupRequest.getType())
                && !userId.equals(group.getOwnerId())) {
            throw new GlobalException("您不是群主,无权限操作");

        } else if (GroupRequestTypeEnum.INVITE_JOIN.getCode().equals(groupRequest.getType())
                && !userId.equals(groupRequest.getUserId())) {
            // 群主邀请加入，用户拒绝
            throw new GlobalException("不是您的数据,无权限操作");
        }
        groupRequest.setStatus(GroupRequestStatusEnum.REFUSED.getCode());
        this.updateById(groupRequest);
        List<Long> recvIds = List.of(group.getOwnerId(), groupRequest.getUserId());
        this.sendGroupRequestMessage(groupRequest, recvIds, MessageType.GROUP_JOIN_REQUEST_MODIFY.code());
    }

    @Transactional(rollbackFor = Exception.class)
    @Lock(prefix = "im:group:request", key = "#id")
    @Override
    public void approve(Long id) {
        UserSession session = SessionContext.getSession();
        Long userId = session.getUserId();
        GroupRequest groupRequest = this.getById(id);
        if (!GroupRequestStatusEnum.APPLYING.getCode().equals(groupRequest.getStatus())) {
            throw new GlobalException("当前数据状态不能操作");
        }

        Group group = groupService.getById(groupRequest.getGroupId());
        // 用户主动申请加入，群主同意
        if (GroupRequestTypeEnum.SELF_JOIN.getCode().equals(groupRequest.getType())
                && !userId.equals(group.getOwnerId())) {
            throw new GlobalException("您不是群主,无权限操作");
        } else if (GroupRequestTypeEnum.INVITE_JOIN.getCode().equals(groupRequest.getType())
                && !userId.equals(groupRequest.getUserId())) {
            // 群主邀请加入，用户同意
            throw new GlobalException("不是您的数据,无权限操作");
        }

        // 手动加分布式锁，避免多个线程对群的修改冲突
        RLock lock = redissonClient.getLock("im:group:member:modify:" + group.getId());
        lock.lock();

        try {
            // 群聊人数校验
            List<GroupMember> members = groupMemberService.findByGroupId(group.getId());
            long size = members.stream().filter(m -> !m.getQuit()).count();
            if (size >= Constant.MAX_GROUP_MEMBER) {
                throw new GlobalException(ResultCode.PROGRAM_ERROR, "当前群聊人数已达上限");
            }
            Optional<GroupMember> optional = members.stream().filter(m -> m.getUserId().equals(userId)).findFirst();
            if (optional.isPresent() && !optional.get().getQuit()) {
                throw new GlobalException("您已加入当前群聊");
            }

            GroupMember member = optional.orElseGet(GroupMember::new);
            User user = userService.getById(userId);

            // 普通群聊
            if (GroupTypeEnum.COMMON.getCode().equals(group.getGroupType())) {
                member.setGroupId(group.getId());
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
                // 模板群聊
                if (ObjectUtil.isNull(groupRequest.getTemplateCharacterId())) {
                    throw new GlobalException("未选择模板角色");
                }
                // 判断用户选择的模板人物是否已存在
                GroupMember groupMember = members.stream().filter(m -> Objects.equals(m.getTemplateCharacterId(), groupRequest.getTemplateCharacterId()) && !m.getQuit()).findFirst().orElse(null);
                if (ObjectUtil.isNotNull(groupMember)) {
                    throw new GlobalException("当前模板角色已有用户选择，请重新选择");
                }
                // 判断所选角色是否群聊模板的角色
                List<TemplateCharacter> templateCharacter = templateCharacterService
                        .findPublishedByTemplateGroupIdAndCharacterIds(group.getTemplateGroupId(), Collections.singletonList(groupRequest.getTemplateCharacterId()));
                if (CollectionUtils.isEmpty(templateCharacter)) {
                    throw new GlobalException("所选模板人物不存在于当前模板群聊");
                }
                member = optional.orElseGet(GroupMember::new);
                member.setGroupId(group.getId());
                member.setUserId(userId);
                member.setRemark(group.getName());
                member.setAliasName(templateCharacter.get(0).getName());
                member.setHeadImage(templateCharacter.get(0).getAvatar());
                member.setTemplateCharacterId(groupRequest.getTemplateCharacterId());
                member.setIsTemplate(true);
                member.setCreatedTime(new Date());
                member.setQuit(false);
                member.setCharacterAvatarId(null);
                member.setAvatarAlias(null);
                groupMemberService.saveOrUpdateBatch(group.getId(), Collections.singletonList(member));
            } else if (GroupTypeEnum.MULT_CHARTER.getCode().equals(group.getGroupType())) {
                // 多元角色群聊
                if (ObjectUtil.isNull(groupRequest.getTemplateCharacterId())) {
                    throw new GlobalException("未选择模板角色");
                }

                Long templateCharacterId = groupRequest.getTemplateCharacterId();
                // 判断用户选择的模板人物是否已存在
                GroupMember groupMember = members.stream().filter(m -> Objects.equals(m.getTemplateCharacterId(), templateCharacterId) && !m.getQuit()).findFirst().orElse(null);
                if (ObjectUtil.isNotNull(groupMember)) {
                    throw new GlobalException("当前模板角色已有用户选择，请重新选择");
                }
                // 判断当前模板人物是否存在
                TemplateCharacter templateCharacter = templateCharacterService.findPublishedById(groupRequest.getTemplateCharacterId());
                if (ObjectUtil.isNull(templateCharacter)) {
                    throw new GlobalException("所选模板角色不存在");
                }
                member = optional.orElseGet(GroupMember::new);
                member.setGroupId(group.getId());
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
            } else if (GroupTypeEnum.CHARTERS.getCode().equals(group.getGroupType())
                    || GroupTypeEnum.TEMPLATE_MULT_CHARTER.getCode().equals(group.getGroupType())) {
                if (ObjectUtil.isNull(groupRequest.getTemplateCharacterId())) {
                    throw new GlobalException("未选择模板角色");
                }
                Long templateCharacterId = groupRequest.getTemplateCharacterId();
                // 判断当前模板角色是否存在
                TemplateCharacter templateCharacter = null;

                if (GroupTypeEnum.TEMPLATE_MULT_CHARTER.getCode().equals(group.getGroupType())) {
                    // 判断群聊模板的是否存在所选模板角色
                    List<TemplateCharacter> templateCharacters = templateCharacterService.findPublishedByTemplateGroupIdAndCharacterIds(group.getTemplateGroupId(), Collections.singletonList(templateCharacterId));
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
                member.setGroupId(group.getId());
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
            }

            groupRequest.setStatus(GroupRequestStatusEnum.AGREED.getCode());
            this.updateById(groupRequest);
            List<Long> recvIds = List.of(group.getOwnerId(), groupRequest.getUserId());
            this.sendGroupRequestMessage(groupRequest, recvIds, MessageType.GROUP_JOIN_REQUEST_MODIFY.code());

            String content = null;
            if (GroupTypeEnum.COMMON.getCode().equals(group.getGroupType())) {
                content = "用户" + user.getNickName() + "加入了群聊";
            } else {
                content = "用户" + user.getNickName() + "【" + member.getAliasName() + "】" + "加入了群聊";
            }
            messageSendUtil.sendTipMessage(group.getId(),
                    session.getUserId(), session.getNickName(), Collections.emptyList(),
                    content, GroupChangeTypeEnum.NEW_USER_JOIN.getCode());
        } catch (Exception e) {
            throw new GlobalException("系统异常");
        } finally {
            lock.unlock();
        }
    }

    private void sendGroupRequestMessage(GroupRequest groupRequest, List<Long> recvIds, Integer type) {
        GroupMessage groupMessage = new GroupMessage();
        groupMessage.setGroupId(groupRequest.getGroupId());
        groupMessage.setSendId(groupRequest.getLaunchUserId());
        groupMessage.setContent(JSON.toJSONString(groupRequest));
        groupMessage.setRecvIds(recvIds.stream().map(String::valueOf).collect(Collectors.joining(",")));
        groupMessage.setType(type);
        groupMessage.setSendTime(new Date());
        groupMessage.setStatus(MessageStatus.UNSEND.code());
        GroupMessageVO groupMessageVO = BeanUtils.copyProperties(groupMessage, GroupMessageVO.class);

        IMGroupMessage<GroupMessageVO> sendMessage = new IMGroupMessage<>();
        sendMessage.setSender(new IMUserInfo(groupRequest.getLaunchUserId(), IMTerminalType.WEB.code()));
        sendMessage.setRecvIds(recvIds);
        sendMessage.setData(groupMessageVO);
        sendMessage.setSendResult(false);
        sendMessage.setSendToSelf(false);
        imClient.sendGroupMessage(sendMessage);
    }
}

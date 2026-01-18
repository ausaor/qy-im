package xyz.qy.implatform.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xyz.qy.imclient.IMClient;
import xyz.qy.imclient.annotation.Lock;
import xyz.qy.imcommon.contant.IMRedisKey;
import xyz.qy.imcommon.model.IMTalkMessage;
import xyz.qy.imcommon.model.IMUserInfo;
import xyz.qy.implatform.contant.RedisKey;
import xyz.qy.implatform.dto.TalkAddDTO;
import xyz.qy.implatform.dto.TalkDelDTO;
import xyz.qy.implatform.dto.TalkQueryDTO;
import xyz.qy.implatform.dto.TalkUpdateDTO;
import xyz.qy.implatform.dto.UserDataAuthDTO;
import xyz.qy.implatform.entity.CharacterAvatar;
import xyz.qy.implatform.entity.CharacterUser;
import xyz.qy.implatform.entity.Friend;
import xyz.qy.implatform.entity.GroupMember;
import xyz.qy.implatform.entity.Region;
import xyz.qy.implatform.entity.RegionGroup;
import xyz.qy.implatform.entity.RegionGroupMember;
import xyz.qy.implatform.entity.Talk;
import xyz.qy.implatform.entity.TalkComment;
import xyz.qy.implatform.entity.TalkNotify;
import xyz.qy.implatform.entity.TalkStar;
import xyz.qy.implatform.entity.TemplateCharacter;
import xyz.qy.implatform.entity.TemplateGroup;
import xyz.qy.implatform.entity.User;
import xyz.qy.implatform.enums.ResultCode;
import xyz.qy.implatform.enums.SectionEnum;
import xyz.qy.implatform.enums.TalkCategoryEnum;
import xyz.qy.implatform.enums.TalkNotifyActionTypeEnum;
import xyz.qy.implatform.enums.TalkNotifyMsgTypeEnum;
import xyz.qy.implatform.enums.ViewScopeEnum;
import xyz.qy.implatform.exception.GlobalException;
import xyz.qy.implatform.mapper.TalkMapper;
import xyz.qy.implatform.service.ICharacterAvatarService;
import xyz.qy.implatform.service.ICharacterUserService;
import xyz.qy.implatform.service.IFriendService;
import xyz.qy.implatform.service.IGroupMemberService;
import xyz.qy.implatform.service.IRegionGroupMemberService;
import xyz.qy.implatform.service.IRegionGroupService;
import xyz.qy.implatform.service.IRegionService;
import xyz.qy.implatform.service.ITalkCommentService;
import xyz.qy.implatform.service.ITalkNotifyService;
import xyz.qy.implatform.service.ITalkService;
import xyz.qy.implatform.service.ITalkStarService;
import xyz.qy.implatform.service.ITemplateCharacterService;
import xyz.qy.implatform.service.ITemplateGroupService;
import xyz.qy.implatform.service.IUserService;
import xyz.qy.implatform.session.SessionContext;
import xyz.qy.implatform.session.UserSession;
import xyz.qy.implatform.util.BeanUtils;
import xyz.qy.implatform.util.PageUtils;
import xyz.qy.implatform.util.RedisCache;
import xyz.qy.implatform.util.SensitiveUtil;
import xyz.qy.implatform.vo.PageResultVO;
import xyz.qy.implatform.vo.TalkCommentVO;
import xyz.qy.implatform.vo.TalkMessageVO;
import xyz.qy.implatform.vo.TalkStarVO;
import xyz.qy.implatform.vo.TalkVO;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @description: 动态
 * @author: Polaris
 * @create: 2023-11-19 21:39
 **/
@Slf4j
@Service
public class TalkServiceImpl extends ServiceImpl<TalkMapper, Talk> implements ITalkService {
    @Resource
    private IUserService userService;

    @Resource
    private IFriendService friendService;

    @Resource
    private IGroupMemberService groupMemberService;

    @Resource
    private ITemplateGroupService templateGroupService;

    @Resource
    private ITemplateCharacterService characterService;

    @Resource
    private ICharacterAvatarService characterAvatarService;

    @Resource
    private ITalkStarService talkStarService;

    @Resource
    private ITalkCommentService talkCommentService;

    @Resource
    private IRegionGroupMemberService regionGroupMemberService;

    @Resource
    private IRegionGroupService regionGroupService;

    @Resource
    private IRegionService regionService;

    @Resource
    private RedisCache redisCache;

    @Resource
    private ITalkNotifyService talkNotifyService;

    @Resource
    private ICharacterUserService characterUserService;

    @Resource
    private IMClient imClient;

    @Transactional
    @Override
    public void addTalk(TalkAddDTO talkAddDTO) {
        checkTalkFiles(talkAddDTO.getFiles());
        checkViewScope(talkAddDTO.getScope(), talkAddDTO.getGroupVisible(), talkAddDTO.getRegionVisible(), talkAddDTO.getCharacterVisible());
        checkCategory(talkAddDTO.getCategory(), talkAddDTO.getGroupId(), talkAddDTO.getRegionCode(), talkAddDTO.getCharacterId(), talkAddDTO.getGroupTemplateId());
        UserSession session = SessionContext.getSession();
        User user = userService.getById(session.getUserId());
        Talk talk = BeanUtils.copyProperties(talkAddDTO, Talk.class);
        assert talk != null;

        talk.setUserId(session.getUserId());
        talk.setCreateBy(session.getUserId());
        talk.setAddress(user.getProvince());
        if (ObjectUtil.isNotNull(talkAddDTO.getCharacterId()) || ObjectUtil.isNotNull(talkAddDTO.getGroupTemplateId())) {
            checkCharacterInfo(talkAddDTO.getCharacterId(), talkAddDTO.getAvatarId(), talkAddDTO.getGroupTemplateId(), talk);
        }
        if (TalkCategoryEnum.CHARACTER.getCode().equals(talkAddDTO.getCategory())
            || (talkAddDTO.getCharacterVisible() && TalkCategoryEnum.PRIVATE.getCode().equals(talkAddDTO.getCategory()))) {
            checkCharacterUser(talkAddDTO.getCharacterId(), talkAddDTO.getGroupTemplateId(), session.getUserId(), talkAddDTO.getCategory());
        }

        if (StringUtils.isBlank(talk.getNickName()) && ObjectUtil.isNull(talkAddDTO.getCharacterId()) && ObjectUtil.isNull(talkAddDTO.getGroupTemplateId())) {
            talk.setNickName(user.getNickName());
        }
        if (StringUtils.isBlank(talk.getAvatar()) && ObjectUtil.isNull(talkAddDTO.getCharacterId()) && ObjectUtil.isNull(talkAddDTO.getGroupTemplateId())) {
            talk.setAvatar(user.getHeadImage());
        }
        talk.setContent(SensitiveUtil.filter(talk.getContent()));
        if (CollectionUtils.isNotEmpty(talkAddDTO.getFiles())) {
            talk.setFiles(talkAddDTO.getFiles().toJSONString());
        }
        this.baseMapper.insert(talk);

        // 自己可见的不通知
        if (!talk.getScope().equals(ViewScopeEnum.PRIVATE.getCode())) {
            List<Long> userIds = new ArrayList<>();
            // 个人动态通知好友列表
            if (TalkCategoryEnum.PRIVATE.getCode().equals(talk.getCategory())) {
                // 查询用户好友
                userIds = friendService.findFriendByUserId(session.getUserId()).stream()
                        .map(Friend::getFriendId)
                        .collect(Collectors.toList());
            } else if (TalkCategoryEnum.GROUP.getCode().equals(talk.getCategory())) {
                userIds = groupMemberService.findUserIdsByGroupId(talk.getGroupId());
                if (!userIds.contains(session.getUserId())) {
                    throw new GlobalException("您不是当前群聊用户");
                }
                // 排除自己的userId
                userIds.remove(session.getUserId());
            } else if (TalkCategoryEnum.REGION.getCode().equals(talk.getCategory())) {
                // 查询地区群聊的常驻用户
                userIds = regionGroupMemberService.findUserIdsByCode(talk.getRegionCode());
                if (!userIds.contains(session.getUserId())) {
                    throw new GlobalException("您不是当前地区群聊常驻用户");
                }
                // 排除自己的userId
                userIds.remove(session.getUserId());
            }

            if (CollectionUtils.isNotEmpty(userIds)) {
                TalkMessageVO msgInfo = new TalkMessageVO();
                msgInfo.setType(TalkNotifyMsgTypeEnum.TALK.getCode());
                msgInfo.setTalk(talk);

                IMTalkMessage<TalkMessageVO> sendMessage = new IMTalkMessage<>();
                sendMessage.setSender(new IMUserInfo(session.getUserId(), session.getTerminal()));
                sendMessage.setRecvIds(userIds);
                sendMessage.setSendResult(false);
                sendMessage.setData(msgInfo);

                imClient.sendTalkMessage(sendMessage);
            }
        }
    }

    @Transactional
    @Lock(prefix = "im:talk:comment", key = "#talkUpdateDTO.getId()")
    @Override
    public void updateTalk(TalkUpdateDTO talkUpdateDTO) {
        checkTalkFiles(talkUpdateDTO.getFiles());
        checkViewScope(talkUpdateDTO.getScope(), talkUpdateDTO.getGroupVisible(), talkUpdateDTO.getRegionVisible(), talkUpdateDTO.getCharacterVisible());
        checkCategory(talkUpdateDTO.getCategory(), talkUpdateDTO.getGroupId(), talkUpdateDTO.getRegionCode(), talkUpdateDTO.getCharacterId(), talkUpdateDTO.getGroupTemplateId());
        UserSession session = SessionContext.getSession();
        Long userId = session.getUserId();
        User user = userService.getById(userId);

        Talk talk = this.baseMapper.selectById(talkUpdateDTO.getId());
        if (!userId.equals(talk.getUserId())) {
            throw new GlobalException("您没有权限修改该动态");
        }
        if (ObjectUtil.isNotNull(talkUpdateDTO.getCharacterId())) {
            boolean verified = verifyTalkCommentCharacter(talk.getId(), talkUpdateDTO.getCharacterId(), talkUpdateDTO.getAvatarId());
            if (verified) {
                throw new GlobalException("所选角色不允许");
            }
        }

        if (!talk.getCategory().equals(talkUpdateDTO.getCategory())) {
            throw new GlobalException("数据异常");
        }

        if (TalkCategoryEnum.CHARACTER.getCode().equals(talkUpdateDTO.getCategory())
                || (talkUpdateDTO.getCharacterVisible() && TalkCategoryEnum.PRIVATE.getCode().equals(talkUpdateDTO.getCategory()))) {
            checkCharacterUser(talkUpdateDTO.getCharacterId(), talkUpdateDTO.getGroupTemplateId(), session.getUserId(), talk.getCategory());
        }

        BeanUtils.copyProperties(talkUpdateDTO, talk);

        if (CollectionUtils.isNotEmpty(talkUpdateDTO.getFiles())) {
            talk.setFiles(talkUpdateDTO.getFiles().toJSONString());
        }
        if (ObjectUtil.isNotNull(talkUpdateDTO.getCharacterId()) || ObjectUtil.isNotNull(talkUpdateDTO.getGroupTemplateId())) {
            checkCharacterInfo(talkUpdateDTO.getCharacterId(), talkUpdateDTO.getAvatarId(), talkUpdateDTO.getGroupTemplateId(), talk);
        }
        talk.setAddress(user.getProvince());
        talk.setUpdateBy(userId);
        if (StringUtils.isBlank(talk.getNickName()) && ObjectUtil.isNull(talkUpdateDTO.getCharacterId()) && ObjectUtil.isNull(talkUpdateDTO.getGroupTemplateId())) {
            talk.setNickName(user.getNickName());
        }
        if (StringUtils.isBlank(talk.getAvatar()) && ObjectUtil.isNull(talkUpdateDTO.getCharacterId()) && ObjectUtil.isNull(talkUpdateDTO.getGroupTemplateId())) {
            talk.setAvatar(user.getHeadImage());
        }
        talk.setContent(SensitiveUtil.filter(talk.getContent()));
        this.baseMapper.updateById(talk);
    }

    private void checkCategory(String category, Long groupId, String regionCode, Long characterId, Long groupTemplateId) {
        if (TalkCategoryEnum.GROUP.getCode().equals(category) && ObjectUtil.isNull(groupId)) {
            throw new GlobalException("群聊id为空");
        }
        if (TalkCategoryEnum.REGION.getCode().equals(category) && StringUtils.isBlank(regionCode)) {
            throw new GlobalException("地区编码为空");
        }
        if (TalkCategoryEnum.CHARACTER.getCode().equals(category) && (ObjectUtil.isNull(characterId) && ObjectUtil.isNull(groupTemplateId))) {
            throw new GlobalException("角色id为空");
        }
    }

    private void checkViewScope(Integer scope, Boolean groupVisible, Boolean regionVisible, Boolean characterVisible) {
        if ((groupVisible || regionVisible || characterVisible) && !ViewScopeEnum.PUBLIC.getCode().equals(scope)) {
            throw new GlobalException("可见范围请选择公开");
        }
    }

    private void checkCharacterUser(Long characterId, Long groupTemplateId, Long userId, String category) {
        if (ObjectUtil.isNull(characterId) && ObjectUtil.isNull(groupTemplateId)) {
            throw new GlobalException("参数异常");
        }
        if (ObjectUtil.isNotNull(characterId) && ObjectUtil.isNotNull(groupTemplateId)) {
            throw new GlobalException("参数异常");
        }
        if (ObjectUtil.isNotNull(characterId)) {
            TemplateCharacter character = characterService.getById(characterId);
            if (Objects.isNull(character)) {
                throw new GlobalException("角色不存在");
            }

            // 角色的创建者可以发布角色空间动态
            if (character.getCreateBy().equals(String.valueOf(userId)) && TalkCategoryEnum.CHARACTER.getCode().equals(category)) {
                return;
            }

            CharacterUser characterUser = characterUserService.lambdaQuery()
                    .eq(CharacterUser::getCharacterId, characterId)
                    .eq(CharacterUser::getUserId, userId)
                    .eq(CharacterUser::getDeleted, false).one();
            if (Objects.isNull(characterUser)) {
                throw new GlobalException("您没有权限发布该角色空间动态");
            }
        } else if (ObjectUtil.isNotNull(groupTemplateId)) {
            TemplateGroup templateGroup = templateGroupService.getById(groupTemplateId);
            if (Objects.isNull(templateGroup)) {
                throw new GlobalException("群聊模板不存在");
            }

            // 群聊模板的创建者可以发布群聊模板动态
            if (!templateGroup.getCreateBy().equals(String.valueOf(userId)) || !TalkCategoryEnum.CHARACTER.getCode().equals(category)) {
                throw new GlobalException("您没有权限发布该群聊模板动态");
            }
        }

    }

    private void checkCharacterInfo(Long characterId, Long avatarId, Long groupTemplateId, Talk talk) {
        if (avatarId != null) {
            CharacterAvatar characterAvatar = characterAvatarService.getById(avatarId);
            if (ObjectUtil.isNull(characterAvatar)) {
                throw new GlobalException("角色头像不存在");
            }
            TemplateCharacter character = characterService.getById(characterId);
            if (Objects.isNull(character)) {
                throw new GlobalException("角色不存在");
            }
            if (!characterId.equals(characterAvatar.getTemplateCharacterId())) {
                throw new GlobalException("所选角色头像不属于当前角色");
            }
            if (characterAvatar.getLevel().equals(0)) {
                talk.setNickName(character.getName());
            } else {
                talk.setNickName(characterAvatar.getName());
            }
            talk.setAvatar(characterAvatar.getAvatar());
        } else if (ObjectUtil.isNotNull(characterId)) {
            TemplateCharacter character = characterService.getById(characterId);
            if (Objects.isNull(character)) {
                throw new GlobalException("角色不存在");
            }
            talk.setNickName(character.getName());
            talk.setAvatar(character.getAvatar());
        } else if (ObjectUtil.isNotNull(groupTemplateId)) {
            TemplateGroup templateGroup = templateGroupService.getById(groupTemplateId);
            if (Objects.isNull(templateGroup)) {
                throw new GlobalException("群聊模板不存在");
            }
            talk.setNickName(templateGroup.getGroupName());
            talk.setAvatar(templateGroup.getAvatar());
        }
    }

    private void checkTalkFiles(JSONArray files) {
        if (CollectionUtils.isEmpty(files)) {
            return;
        }
        if (files.size() > 9) {
            throw new GlobalException("最多只能上传9个文件");
        }

        // 判断files中fileType等于2的数量是否大于1
        long count = files.stream()
                .filter(file -> file instanceof Map)
                .map(file -> (Map<?, ?>) file)
                .filter(map -> "2".equals(String.valueOf(map.get("fileType"))))
                .count();
        if (count > 1) {
            throw new GlobalException("只能上传一个视频文件");
        }

        // 判断files中fileType等于2的数量是否大于1
        long count2 = files.stream()
                .filter(file -> file instanceof Map)
                .map(file -> (Map<?, ?>) file)
                .filter(map -> "3".equals(String.valueOf(map.get("fileType"))))
                .count();
        if (count2 > 1) {
            throw new GlobalException("只能上传一个音频文件");
        }

        // 获取files中fileType属性的所有值
        Set<String> fileTypeValues = files.stream()
                .filter(file -> file instanceof Map)
                .map(file -> (Map<?, ?>) file)
                .map(map -> String.valueOf(map.get("fileType")))
                .collect(Collectors.toSet());
       if (fileTypeValues.size() > 1) {
           throw new GlobalException("不能同时上传多种类型文件");
       }
    }

    @Override
    public PageResultVO pageQueryTalkList(TalkQueryDTO queryDTO) {
        TalkQueryDTO dto = new TalkQueryDTO();
        UserSession session = SessionContext.getSession();
        Long myUserId = session.getUserId();

        List<Talk> records = null;
        Page<Talk> talkPage = null;

        // 查询自己和所有好友的
        if (SectionEnum.MY_FRIENDS.getCode().equals(queryDTO.getSection())) {
            dto.setOwnerId(myUserId);
            // 查询获取好友用户id
            List<Long> friendIds = friendService.getFriendIdsByUserId(myUserId);
            dto.setFriendIds(friendIds);
            talkPage = this.baseMapper.pageQueryMyAndFriendsTalkList(new Page<>(PageUtils.getPageNo(), PageUtils.getPageSize()), dto);
            if (ObjectUtil.isNotNull(talkPage)) {
                records = talkPage.getRecords();
            }
        } else if (SectionEnum.MY.getCode().equals(queryDTO.getSection())) {// 查询自己的
            dto.setOwnerId(myUserId);
            talkPage = this.baseMapper.pageQueryMyTalkList(new Page<>(PageUtils.getPageNo(), PageUtils.getPageSize()), dto);
            if (ObjectUtil.isNotNull(talkPage)) {
                records = talkPage.getRecords();
            }
        } else if (SectionEnum.FRIENDS.getCode().equals(queryDTO.getSection())) {// 查询所有好友的
            // 查询获取好友用户id
            List<Long> friendIds = friendService.getFriendIdsByUserId(myUserId);
            if (CollectionUtils.isEmpty(friendIds)) {
                return PageResultVO.builder().data(Collections.emptyList()).total(0).build();
            }
            dto.setFriendIds(friendIds);
            talkPage = this.baseMapper.pageQueryFriendsTalkList(new Page<>(PageUtils.getPageNo(), PageUtils.getPageSize()), dto);
            if (ObjectUtil.isNotNull(talkPage)) {
                records = talkPage.getRecords();
            }
        } else if (SectionEnum.FRIEND.getCode().equals(queryDTO.getSection())) {// 查询单个好友的
            if (CollectionUtils.isEmpty(queryDTO.getFriendIds())) {
                throw new GlobalException("好友参数异常");
            }

            // 判断是否用户的好友
            Boolean isFriends = friendService.isFriend(session.getUserId(), queryDTO.getFriendIds().get(0));
            if (!isFriends) {
                throw new GlobalException(ResultCode.PROGRAM_ERROR, "您已不是对方好友，无法查看对方空间");
            }

            talkPage = this.baseMapper.pageQueryFriendsTalkList(new Page<>(PageUtils.getPageNo(), PageUtils.getPageSize()), queryDTO);
            if (ObjectUtil.isNotNull(talkPage)) {
                records = talkPage.getRecords();
            }
        } else if (SectionEnum.GROUP.getCode().equals(queryDTO.getSection())) {
            if (ObjectUtil.isNull(queryDTO.getGroupId())) {
                throw new GlobalException("群参数异常");
            }
            // 判断用户是否还在群聊中
            GroupMember groupMember = groupMemberService.findByGroupAndUserId(queryDTO.getGroupId(), myUserId);
            if (ObjectUtil.isNull(groupMember) || groupMember.getQuit()) {
                throw new GlobalException("您不在当前群聊中");
            }

            // 查询群聊成员信息
            List<GroupMember> groupMembers = groupMemberService.findNoQuitGroupMembers(queryDTO.getGroupId());
            List<Long> userIds = groupMembers.stream().map(GroupMember::getUserId).collect(Collectors.toList());

            dto.setGroupId(queryDTO.getGroupId());
            dto.setGroupMemberUserIds(userIds);
            talkPage = this.baseMapper.pageQueryGroupTalkList(new Page<>(PageUtils.getPageNo(), PageUtils.getPageSize()), dto);
            if (ObjectUtil.isNotNull(talkPage)) {
                records = talkPage.getRecords();
            }
        } else if (SectionEnum.MY_REGION.getCode().equals(queryDTO.getSection())) {
            if (ObjectUtil.isNull(queryDTO.getRegionGroupId())) {
                throw new GlobalException("地区参数异常");
            }
            RegionGroup regionGroup = regionGroupService.getById(queryDTO.getRegionGroupId());

            Object object = redisCache.getCacheObject(IMRedisKey.IM_REGION_GROUP_NUM_TEMP_USER + regionGroup.getCode() + ":" + regionGroup.getNum() + ":" + myUserId);

            // 判断用户是否地区群聊成员
            RegionGroupMember regionGroupMember = regionGroupMemberService.findByRegionGroupIdAndUserId(queryDTO.getRegionGroupId(), myUserId);
            if ((ObjectUtil.isNull(regionGroupMember) || regionGroupMember.getQuit()) && ObjectUtil.isNull(object)) {
                throw new GlobalException("您不是当前地区群聊成员");
            }
            // 查询所有常驻成员
            List<Long> userIds = regionGroupMemberService.findUserIdsByRegionGroupId(regionGroup.getId());
            dto.setGroupMemberUserIds(userIds);
            dto.setRegionCode(regionGroup.getCode());

            talkPage = this.baseMapper.pageQueryRegionTalkList(new Page<>(PageUtils.getPageNo(), PageUtils.getPageSize()), dto);
            if (ObjectUtil.isNotNull(talkPage)) {
                records = talkPage.getRecords();
            }
        } else if (SectionEnum.REGION.getCode().equals(queryDTO.getSection())) {
            if (StringUtils.isBlank(queryDTO.getRegionCode())) {
                throw new GlobalException("地区参数异常");
            }
            Region region = regionService.findRegionByCode(queryDTO.getRegionCode());
            if (ObjectUtil.isNull(region) || region.getDeleted()) {
                throw new GlobalException("地区不存在");
            }
            dto.setRegionCode(region.getCode());
            talkPage = this.baseMapper.pageQueryRegionTalkList(new Page<>(PageUtils.getPageNo(), PageUtils.getPageSize()), dto);
            if (ObjectUtil.isNotNull(talkPage)) {
                records = talkPage.getRecords();
            }
        } else if (SectionEnum.CHARACTER.getCode().equals(queryDTO.getSection())) {
            if (ObjectUtil.isNull(queryDTO.getCharacterId())) {
                throw new GlobalException("角色参数异常");
            }
            List<Long> userIdList = characterUserService.getUserIdListByCharacterIds(List.of(queryDTO.getCharacterId()));
            if (CollectionUtils.isNotEmpty(userIdList)) {
                queryDTO.setCharacterUserIds(userIdList);
            }

            talkPage = this.baseMapper.pageQueryCharacterTalkList(new Page<>(PageUtils.getPageNo(), PageUtils.getPageSize()), queryDTO);
            if (ObjectUtil.isNotNull(talkPage)) {
                records = talkPage.getRecords();
            }
        } else if (SectionEnum.GROUP_TEMPLATE.getCode().equals(queryDTO.getSection())) {
            if (ObjectUtil.isNull(queryDTO.getGroupTemplateId())) {
                throw new GlobalException("参数异常");
            }
            talkPage = this.baseMapper.pageQueryGroupTemplateTalkList(new Page<>(PageUtils.getPageNo(), PageUtils.getPageSize()), queryDTO);
            if (ObjectUtil.isNotNull(talkPage)) {
                records = talkPage.getRecords();
            }
        } else if (SectionEnum.GROUP_TEMPLATE_CHARACTERS.getCode().equals(queryDTO.getSection())) {
            if (ObjectUtil.isNull(queryDTO.getGroupTemplateId())) {
                throw new GlobalException("参数异常");
            }
            List<Long> characterIds = characterService.findPublishedCharacterIdsByGroupId(queryDTO.getGroupTemplateId());
            if (CollectionUtils.isNotEmpty(characterIds)) {
                queryDTO.setCharacterIds(characterIds);

                List<Long> userIdList = characterUserService.getUserIdListByCharacterIds(characterIds);
                if (CollectionUtils.isNotEmpty(userIdList)) {
                    queryDTO.setCharacterUserIds(userIdList);
                }
            }
            talkPage = this.baseMapper.pageQueryGroupTemplateCharactersTalkList(new Page<>(PageUtils.getPageNo(), PageUtils.getPageSize()), queryDTO);
            if (ObjectUtil.isNotNull(talkPage)) {
                records = talkPage.getRecords();
            }
        } else if (SectionEnum.CHARACTERS.getCode().equals(queryDTO.getSection())) {
            if (ObjectUtil.isNotNull(queryDTO.getGroupId())) {
                queryDTO.setCharacterIds(groupMemberService.getGroupCharacterIds(queryDTO.getGroupId()));
            }

            if (CollectionUtils.isEmpty(queryDTO.getCharacterIds())) {
                throw new GlobalException("参数异常");
            }
            queryDTO.setCharacterIds(queryDTO.getCharacterIds().stream().distinct().collect(Collectors.toList()));
            List<Long> userIdList = characterUserService.getUserIdListByCharacterIds(queryDTO.getCharacterIds());
            if (CollectionUtils.isNotEmpty(userIdList)) {
                queryDTO.setCharacterUserIds(userIdList);
            }
            talkPage = this.baseMapper.pageQueryCharactersTalkList(new Page<>(PageUtils.getPageNo(), PageUtils.getPageSize()), queryDTO);
            if (ObjectUtil.isNotNull(talkPage)) {
                records = talkPage.getRecords();
            }
        } else if (SectionEnum.ALL_CHARACTERS.getCode().equals(queryDTO.getSection())) {
            talkPage = this.baseMapper.pageQueryAllCharactersTalkList(new Page<>(PageUtils.getPageNo(), PageUtils.getPageSize()));
            if (ObjectUtil.isNotNull(talkPage)) {
                records = talkPage.getRecords();
            }
        } else if (SectionEnum.MY_CHARACTERS.getCode().equals(queryDTO.getSection())) {
            List<Long> myCharacterIds = characterUserService.getMyCharacterIds();
            if (CollectionUtils.isNotEmpty(myCharacterIds)) {
                queryDTO.setCharacterIds(myCharacterIds);
                queryDTO.setOwnerId(myUserId);
                talkPage = this.baseMapper.pageQueryMyCharactersTalkList(new Page<>(PageUtils.getPageNo(), PageUtils.getPageSize()), queryDTO);
                if (ObjectUtil.isNotNull(talkPage)) {
                    records = talkPage.getRecords();
                }
            }
        }

        if (CollectionUtils.isEmpty(records)) {
            return PageResultVO.builder().data(Collections.emptyList()).build();
        }

        Set<Long> userIds = records.stream().map(Talk::getUserId).collect(Collectors.toSet());

        // 查询当前用户数据
        User user = userService.getById(myUserId);

        // 动态id
        List<Long> talkIds = records.stream().map(Talk::getId).collect(Collectors.toList());

        // 动态赞星数据
        List<TalkStar> talkStarList = talkStarService.lambdaQuery().in(TalkStar::getTalkId, talkIds)
                .eq(TalkStar::getDeleted, false)
                .orderByAsc(TalkStar::getCreateTime)
                .list();
        userIds.addAll(talkStarList.stream().map(TalkStar::getUserId).collect(Collectors.toList()));

        // 动态评论数据(包含删除的)
        List<TalkComment> allTalkCommentList = talkCommentService.lambdaQuery().in(TalkComment::getTalkId, talkIds)
                .orderByAsc(TalkComment::getCreateTime).list();

        userIds.addAll(allTalkCommentList.stream().map(TalkComment::getUserId).collect(Collectors.toList()));
        userIds.addAll(allTalkCommentList.stream().map(TalkComment::getReplyUserId)
                .filter(ObjectUtil::isNotNull).collect(Collectors.toList()));

        List<User> userList = userService.listByIds(userIds);
        Map<Long, User> userMap = userList.stream().collect(Collectors.toMap(User::getId, userItem -> userItem));

        // 根据动态id分组
        Map<Long, List<TalkComment>> allTalkCommentGroupMap = allTalkCommentList.stream().collect(Collectors.groupingBy(TalkComment::getTalkId));

        // 动态评论数据-未删除
        List<TalkComment> talkCommentList = allTalkCommentList.stream().filter(item -> !item.getDeleted()).collect(Collectors.toList());

        List<TalkStarVO> talkStarVOS = BeanUtils.copyProperties(talkStarList, TalkStarVO.class);

        List<TalkCommentVO> talkCommentVOS = BeanUtils.copyProperties(talkCommentList, TalkCommentVO.class);

        talkCommentVOS.forEach(item -> {
            if (ObjectUtil.isNull(item.getCharacterId())) {
                item.setUserAvatar(userMap.get(item.getUserId()).getHeadImage());
                item.setUserNickname(userMap.get(item.getUserId()).getNickName());
            }
            if (ObjectUtil.isNull(item.getReplyUserCharacterId()) && ObjectUtil.isNotNull(item.getReplyUserId())) {
                item.setReplyUserAvatar(userMap.get(item.getReplyUserId()).getHeadImage());
                item.setReplyUserNickname(userMap.get(item.getReplyUserId()).getNickName());
            }
        });
        talkStarVOS.forEach(item -> {
            if (ObjectUtil.isNull(item.getCharacterId())) {
                item.setAvatar(userMap.get(item.getUserId()).getHeadImage());
                item.setNickname(userMap.get(item.getUserId()).getNickName());
            }
        });

        Map<Long, List<TalkStarVO>> talkStarMap = talkStarVOS.stream().collect(Collectors.groupingBy(TalkStarVO::getTalkId));
        Map<Long, List<TalkCommentVO>> talkCommentMap = talkCommentVOS.stream().collect(Collectors.groupingBy(TalkCommentVO::getTalkId));

        List<TalkVO> talkVOS = records.parallelStream().map(obj -> {
            TalkVO talkVO = BeanUtils.copyProperties(obj, TalkVO.class);
            assert talkVO != null;
            if (StringUtils.isNotBlank(obj.getFiles())) {
                talkVO.setFileList(JSONArray.parseArray(obj.getFiles()));
            }
            // 当前用户是否发布作者
            if (myUserId.equals(talkVO.getUserId())) {
                talkVO.setIsOwner(Boolean.TRUE);
            }
            talkVO.setCommentUserAvatar(user.getHeadImage());
            talkVO.setCommentUserNickname(user.getNickName());
            Set<Long> characterIds = new HashSet<>();
            if (ObjectUtil.isNotNull(talkVO.getCharacterId())) {
                characterIds.add(talkVO.getCharacterId());
                if (obj.getUserId().equals(myUserId)) {
                    talkVO.setCommentCharacterAvatarId(talkVO.getAvatarId());
                    talkVO.setCommentCharacterId(talkVO.getCharacterId());
                    talkVO.setCommentCharacterName(talkVO.getNickName());
                    talkVO.setCommentCharacterAvatar(talkVO.getAvatar());
                }
            } else if (ObjectUtil.isNotNull(talkVO.getGroupTemplateId())) {
                log.info("群聊模板id：{}", talkVO.getGroupTemplateId());
            } else {
                talkVO.setAvatar(userMap.get(obj.getUserId()).getHeadImage());
                talkVO.setNickName(userMap.get(obj.getUserId()).getNickName());
            }
            if (talkStarMap.containsKey(talkVO.getId())) {
                talkVO.setTalkStarVOS(talkStarMap.get(talkVO.getId()));
                // 找到当前用户点赞，并且角色id不为空的数据
                Optional<TalkStarVO> talkStarVOOptional = talkVO.getTalkStarVOS().stream().filter(item -> item.getUserId().equals(myUserId)
                        && ObjectUtil.isNotNull(item.getCharacterId())).findFirst();
                talkStarVOOptional.ifPresent(talkStarVO -> {
                    talkVO.setCommentCharacterAvatarId(talkStarVO.getAvatarId());
                    talkVO.setCommentCharacterId(talkStarVO.getCharacterId());
                    talkVO.setCommentCharacterName(talkStarVO.getNickname());
                    talkVO.setCommentCharacterAvatar(talkStarVO.getAvatar());
                });
                characterIds.addAll(talkVO.getTalkStarVOS().stream().map(TalkStarVO::getCharacterId).collect(Collectors.toSet()));

                // 判断当前用户是否点赞此条动态
                talkVO.setIsLike(talkVO.getTalkStarVOS().stream()
                        .anyMatch(item -> item.getUserId().equals(myUserId)));

                talkVO.getTalkStarVOS().forEach(item -> {
                    if (myUserId.equals(item.getUserId())) {
                        item.setIsOwner(Boolean.TRUE);
                    }
                });
            } else {
                talkVO.setTalkStarVOS(Collections.emptyList());
            }
            if (allTalkCommentGroupMap.containsKey(talkVO.getId())) {
                // 找到当前用户评论，并且角色id不为空的数据
                Optional<TalkComment> talkCommentOptional = allTalkCommentGroupMap.get(talkVO.getId()).stream().filter(item -> item.getUserId().equals(myUserId)
                        && !Objects.isNull(item.getCharacterId())).findFirst();
                talkCommentOptional.ifPresent(talkComment -> {
                    talkVO.setCommentCharacterAvatarId(talkComment.getAvatarId());
                    talkVO.setCommentCharacterId(talkComment.getCharacterId());
                    talkVO.setCommentCharacterName(talkComment.getUserNickname());
                    talkVO.setCommentCharacterAvatar(talkComment.getUserAvatar());
                });
            }
            if (talkCommentMap.containsKey(talkVO.getId())) {
                talkVO.setTalkCommentVOS(talkCommentMap.get(talkVO.getId()));
                talkVO.getTalkCommentVOS().forEach(item -> {
                    if (myUserId.equals(item.getUserId())) {
                        item.setIsOwner(Boolean.TRUE);
                    }
                });
                characterIds.addAll(talkVO.getTalkCommentVOS().stream().map(TalkCommentVO::getCharacterId).collect(Collectors.toSet()));
            } else {
                talkVO.setTalkCommentVOS(Collections.emptyList());
            }
            talkVO.setSelectedCharacterIds(characterIds);
            return talkVO;
        }).collect(Collectors.toList());

        return PageResultVO.builder().data(talkVOS).total(talkPage.getTotal()).build();
    }

    @Override
    public void delTalk(TalkDelDTO talkDelDTO) {
        UserSession session = SessionContext.getSession();
        Long userId = session.getUserId();

        Talk talk = this.baseMapper.selectById(talkDelDTO.getId());
        if (Objects.isNull(talk)) {
            throw new GlobalException("当前动态不存在");
        }
        if (!userId.equals(talk.getUserId())) {
            throw new GlobalException("您不是当前动态作者");
        }
        talk.setDeleted(Boolean.TRUE);
        boolean update = this.updateById(talk);
        if (update) {
            log.info("成功删除动态,talkId={}：", talk.getId());
        } else {
            log.info("删除动态失败,talkId={}：", talk.getId());
        }
    }

    @Override
    public TalkVO getTalkDetail(Long talkId) {
        UserSession session = SessionContext.getSession();
        Long userId = session.getUserId();

        Talk talk = this.baseMapper.selectById(talkId);
        if (Objects.isNull(talk) || talk.getDeleted()) {
            throw new GlobalException("当前动态已被删除");
        }
        if (!userId.equals(talk.getCreateBy())) {
            throw new GlobalException("您不是当前动态作者");
        }
        TalkVO talkVO = BeanUtils.copyProperties(talk, TalkVO.class);
        assert talkVO != null;
        if (StringUtils.isNotBlank(talk.getFiles())) {
            talkVO.setFileList(JSONArray.parseArray(talk.getFiles()));
        }

        // 是否有使用过角色用于 编辑、评论、点赞 动态
        boolean hasCharacter = false;

        // 是否有使用过角色头像用于 编辑、评论、点赞 动态
        boolean hasCharacterAvatar = false;

        if (ObjectUtil.isNotNull(talk.getCharacterId())) {
            TemplateCharacter character = characterService.getById(talk.getCharacterId());
            if (ObjectUtil.isNotNull(character)) {
                talkVO.setCommentCharacterId(character.getId());
                talkVO.setCommentCharacterName(character.getName());
                talkVO.setCommentCharacterAvatar(character.getAvatar());
                hasCharacter = true;
            }
        }

        if (ObjectUtil.isNotNull(talk.getAvatarId())) {
            CharacterAvatar characterAvatar = characterAvatarService.getById(talk.getAvatarId());
            if (ObjectUtil.isNotNull(characterAvatar)) {
                talkVO.setCommentCharacterAvatarId(characterAvatar.getId());
                talkVO.setCommentCharacterAvatar(characterAvatar.getAvatar());
                if (!characterAvatar.getLevel().equals(0)) {
                    talkVO.setCommentCharacterName(characterAvatar.getName());
                }
                hasCharacterAvatar = true;
            }
        }

        if (!hasCharacter) {
            // 查询当前用户是否在点赞数据使用模板角色
            List<TalkStar> talkStars = talkStarService.lambdaQuery()
                    .eq(TalkStar::getTalkId, talkId)
                    .eq(TalkStar::getUserId, userId)
                    .eq(TalkStar::getDeleted, Boolean.FALSE)
                    .isNotNull(TalkStar::getCharacterId)
                    .last("limit 1")
                    .list();
            if (CollectionUtils.isNotEmpty(talkStars)) {
                TemplateCharacter character = characterService.getById(talkStars.get(0).getCharacterId());
                if (ObjectUtil.isNotNull(character)) {
                    talkVO.setCommentCharacterId(character.getId());
                    talkVO.setCommentCharacterName(character.getName());
                    talkVO.setCommentCharacterAvatar(character.getAvatar());
                    hasCharacter = true;
                }
            }
        }

        if (!hasCharacterAvatar) {
            // 查询当前用户是否在点赞数据使用模板角色头像
            List<TalkStar> talkStars = talkStarService.lambdaQuery()
                    .eq(TalkStar::getTalkId, talkId)
                    .eq(TalkStar::getUserId, userId)
                    .eq(TalkStar::getDeleted, Boolean.FALSE)
                    .isNotNull(TalkStar::getAvatarId)
                    .last("limit 1")
                    .list();
            if (CollectionUtils.isNotEmpty(talkStars)) {
                CharacterAvatar characterAvatar = characterAvatarService.getById(talkStars.get(0).getAvatarId());
                if (ObjectUtil.isNotNull(characterAvatar)) {
                    talkVO.setCommentCharacterAvatarId(characterAvatar.getId());
                    talkVO.setCommentCharacterAvatar(characterAvatar.getAvatar());
                    if (!characterAvatar.getLevel().equals(0)) {
                        talkVO.setCommentCharacterName(characterAvatar.getName());
                    }
                    hasCharacterAvatar = true;
                }
            }
        }

        if (!hasCharacter) {
            // 查询当前用户是否在评论数据使用模板角色
            List<TalkComment> talkComments = talkCommentService.lambdaQuery()
                    .eq(TalkComment::getTalkId, talkId)
                    .eq(TalkComment::getUserId, userId)
                    .eq(TalkComment::getDeleted, Boolean.FALSE)
                    .isNotNull(TalkComment::getCharacterId)
                    .last("limit 1")
                    .list();
            if (CollectionUtils.isNotEmpty(talkComments)) {
                TemplateCharacter character = characterService.getById(talkComments.get(0).getCharacterId());
                if (ObjectUtil.isNotNull(character)) {
                    talkVO.setCommentCharacterId(character.getId());
                    talkVO.setCommentCharacterName(character.getName());
                    talkVO.setCommentCharacterAvatar(character.getAvatar());
                    hasCharacter = true;
                }
            }
        }

        if (!hasCharacterAvatar) {
            // 查询当前用户是否在评论数据使用模板角色头像
            List<TalkComment> talkComments = talkCommentService.lambdaQuery()
                    .eq(TalkComment::getTalkId, talkId)
                    .eq(TalkComment::getUserId, userId)
                    .eq(TalkComment::getDeleted, Boolean.FALSE)
                    .isNotNull(TalkComment::getAvatarId)
                    .last("limit 1")
                    .list();
            if (CollectionUtils.isNotEmpty(talkComments)) {
                CharacterAvatar characterAvatar = characterAvatarService.getById(talkComments.get(0).getAvatarId());
                if (ObjectUtil.isNotNull(characterAvatar)) {
                    talkVO.setCommentCharacterAvatarId(characterAvatar.getId());
                    talkVO.setCommentCharacterAvatar(characterAvatar.getAvatar());
                    if (!characterAvatar.getLevel().equals(0)) {
                        talkVO.setCommentCharacterName(characterAvatar.getName());
                    }
                    hasCharacterAvatar = true;
                }
            }
        }

        if (!hasCharacter) {
            talkVO.setEnableCharacterChoose(Boolean.TRUE);
        }

        return talkVO;
    }

    @Override
    public boolean verifyTalkCommentCharacter(Long talkId, Long characterId, Long avatarId) {
        if (Objects.isNull(talkId) || Objects.isNull(characterId)) {
            throw new GlobalException("参数异常");
        }
        UserSession session = SessionContext.getSession();
        Long userId = session.getUserId();
        Talk talk = baseMapper.selectById(talkId);
        if (Objects.isNull(talk) || talk.getDeleted()) {
            throw new GlobalException("当前动态已被删除");
        }
        // 自己选择过的角色与入参的角色不一致
        if (userId.equals(talk.getUserId())
                && !Objects.isNull(talk.getCharacterId())) {
            if (!talk.getCharacterId().equals(characterId)) {
                return true;
            }
        }

        // 自己选择过的角色头像与入参的角色头像不一致
        if (userId.equals(talk.getUserId())
                && !Objects.isNull(talk.getAvatarId())) {
            if (!talk.getAvatarId().equals(avatarId)) {
                return true;
            }
        }

        // 自己选择的角色与其他人选择的角色一样
        if (!userId.equals(talk.getUserId())
                && !Objects.isNull(talk.getCharacterId())) {
            if (talk.getCharacterId().equals(characterId)) {
                return true;
            }
        }

        // 自己选择的角色头像与其他人选择的角色头像一样
        if (!userId.equals(talk.getUserId())
                && !Objects.isNull(talk.getAvatarId())) {
            if (talk.getAvatarId().equals(avatarId)) {
                return true;
            }
        }

        List<TalkStar> talkStarList = talkStarService.lambdaQuery()
                .eq(TalkStar::getTalkId, talkId)
                .eq(TalkStar::getDeleted, false).list();
        if (CollectionUtils.isNotEmpty(talkStarList)) {
            // 自己选择的角色与其他人选择的角色一样
            Optional<TalkStar> optional1 = talkStarList.stream().filter(item -> !Objects.isNull(item.getCharacterId())
                    && characterId.equals(item.getCharacterId())
                    && !item.getUserId().equals(userId)).findFirst();
            if (optional1.isPresent()) {
                return true;
            }

            // 自己选择的角色头像与其他人选择的角色头像一样
            Optional<TalkStar> optional3 = talkStarList.stream().filter(item -> !Objects.isNull(item.getAvatarId())
                    && item.getAvatarId().equals(avatarId)
                    && !item.getUserId().equals(userId)).findFirst();
            if (optional3.isPresent()) {
                return true;
            }

            // 自己选择过的角色与入参的角色不一致
            Optional<TalkStar> optional2 = talkStarList.stream().filter(item -> !Objects.isNull(item.getCharacterId())
                    && !characterId.equals(item.getCharacterId())
                    && item.getUserId().equals(userId)).findFirst();
            if (optional2.isPresent()) {
                return true;
            }

            // 自己选择过的角色头像与入参的角色头像不一致
            Optional<TalkStar> optional4 = talkStarList.stream().filter(item -> !Objects.isNull(item.getAvatarId())
                    && !item.getAvatarId().equals(avatarId)
                    && item.getUserId().equals(userId)).findFirst();
            if (optional4.isPresent()) {
                return true;
            }
        }

        List<TalkComment> talkCommentList = talkCommentService.lambdaQuery()
                .eq(TalkComment::getTalkId, talkId).list();
        if (CollectionUtils.isNotEmpty(talkCommentList)) {
            // 自己选择的角色与其他人选择的角色一样
            Optional<TalkComment> optional1 = talkCommentList.stream().filter(item -> !Objects.isNull(item.getCharacterId())
                    && characterId.equals(item.getCharacterId())
                    && !item.getUserId().equals(userId)).findFirst();
            if (optional1.isPresent()) {
                return true;
            }

            // 自己选择的角色与其他人选择的角色头像一样
            Optional<TalkComment> optional3 = talkCommentList.stream().filter(item -> !Objects.isNull(item.getAvatarId())
                    && item.getAvatarId().equals(avatarId)
                    && !item.getUserId().equals(userId)).findFirst();
            if (optional3.isPresent()) {
                return true;
            }

            // 自己选择过的角色与入参的角色不一致
            Optional<TalkComment> optional2 = talkCommentList.stream().filter(item -> !Objects.isNull(item.getCharacterId())
                    && !characterId.equals(item.getCharacterId()) &&
                    item.getUserId().equals(userId)).findFirst();
            if (optional2.isPresent()) {
                return true;
            }

            // 自己选择过的角色头像与入参的角色头像不一致
            Optional<TalkComment> optional4 = talkCommentList.stream().filter(item -> !Objects.isNull(item.getAvatarId())
                    && !item.getAvatarId().equals(avatarId) &&
                    item.getUserId().equals(userId)).findFirst();
            if (optional4.isPresent()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public JSONObject pullOfflineTalks(Long minId) {
        minId = minId == null ? 0L : minId;
        JSONObject jsonObject = new JSONObject();
        UserSession session = SessionContext.getSession();
        Long userId = session.getUserId();

        // 获取未读点赞和评论数据数量
        LambdaQueryWrapper<TalkNotify> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TalkNotify::getUserId, userId);
        wrapper.eq(TalkNotify::getIsRead, false);
        wrapper.eq(TalkNotify::getDeleted, false);
        wrapper.eq(TalkNotify::getCategory, TalkCategoryEnum.PRIVATE.getCode());
        wrapper.in(TalkNotify::getActionType, Arrays.asList(TalkNotifyActionTypeEnum.COMMENT.getCode(),  TalkNotifyActionTypeEnum.LIKE.getCode()));

        int notifyCount = talkNotifyService.count(wrapper);

        jsonObject.put("notifyCount", notifyCount);

        // 查询好友列表
        List<Long> friendIds = friendService.getFriendIdsByUserId(userId);

        if (CollectionUtils.isEmpty(friendIds)) {
            return jsonObject;
        }
        Date minDate = DateUtils.addMonths(new Date(), -1);

        String key = StrUtil.join(":", RedisKey.IM_TALK_READED_POSITION, userId);
        Object object = redisCache.getCacheObject(key);
        minId = object == null ? minId : Long.parseLong(object.toString());

        // 查询未读的好友一个月内发布的动态数量
        List<Talk> talkList = this.lambdaQuery()
                .in(Talk::getUserId, friendIds)
                .in(Talk::getScope, Arrays.asList(ViewScopeEnum.FRIENDS.getCode(), ViewScopeEnum.PUBLIC.getCode()))
                .eq(Talk::getCategory, TalkCategoryEnum.PRIVATE.getCode())
                .eq(Talk::getDeleted, false)
                .ge(Talk::getCreateTime, minDate)
                .gt(Talk::getId, minId)
                .orderByDesc(Talk::getId)
                .last("limit 100")
                .list();
        if (CollectionUtils.isEmpty(talkList)) {
            return jsonObject;
        }

        // 获取最大id
        Long maxId = talkList.get(0).getId();

        redisCache.setCacheObject(key, maxId);

        // 统计用户数量
        Set<Long> userIds = talkList.stream().map(Talk::getUserId).collect(Collectors.toSet());

        // 将talkList根据用户id去重，并保留id最大的一条
        talkList = talkList.stream().collect(Collectors.groupingBy(Talk::getUserId))
                .entrySet().stream().map(item -> {
                    List<Talk> list = item.getValue();
                    return list.get(0);
                }).collect(Collectors.toList());

        // 获取talkList前两条数据
        List<Talk> lastTwoTalkList = talkList.stream().sorted(Comparator.comparing(Talk::getId).reversed()).limit(2).collect(Collectors.toList());


        jsonObject.put("maxId", maxId);
        jsonObject.put("userList", userIds);
        jsonObject.put("talkList", lastTwoTalkList);
        return jsonObject;
    }

    @Override
    public boolean verifyUserDataAuth(UserDataAuthDTO dto) {
        if (TalkCategoryEnum.PRIVATE.getCode().equals(dto.getCategory())) {
            if (dto.getUserId().equals(dto.getFriendId())) {
                return true;
            }
            // 判断用户是否好友
            Boolean friend = friendService.isFriend(dto.getUserId(), dto.getFriendId());
            if (friend) {
                return true;
            }

            if (dto.getGroupVisible() && ViewScopeEnum.PUBLIC.getCode().equals(dto.getScope())) {
                // 判断是否群友
                Boolean inGroup = groupMemberService.existsInSameGroup(dto.getUserId(), dto.getFriendId());
                if (inGroup) {
                    return true;
                }
            }

            if (dto.getRegionVisible() && ViewScopeEnum.PUBLIC.getCode().equals(dto.getScope())) {
                // 判断用户是否地区群聊常驻成员
                Boolean inRegionGroup = regionGroupMemberService.existsInSameGroup(dto.getUserId(), dto.getFriendId());
                if (inRegionGroup) {
                    return true;
                }
            }
        } else if (TalkCategoryEnum.GROUP.getCode().equals(dto.getCategory())) {
            // 判断当前用户是否群成员
            return groupMemberService.isInGroup(dto.getGroupId(), Collections.singletonList(dto.getUserId()));
        } else if (TalkCategoryEnum.REGION.getCode().equals(dto.getCategory())) {
            // 判断当前用户是否地区群聊常驻成员
            Boolean inRegionGroup = regionGroupMemberService.isInRegionGroup(dto.getRegionCode(), Collections.singletonList(dto.getUserId()));

            // 判断当前用户是否临时成员
            Boolean tempMember = regionGroupMemberService.isTempMember(dto.getRegionCode(), dto.getUserId());
            if (inRegionGroup || tempMember) {
                return true;
            }
        } else if (TalkCategoryEnum.PUBLIC.getCode().equals(dto.getCategory())) {
            return true;
        } else if (TalkCategoryEnum.CHARACTER.getCode().equals(dto.getCategory())) {
            return true;
        }
        return false;
    }
}

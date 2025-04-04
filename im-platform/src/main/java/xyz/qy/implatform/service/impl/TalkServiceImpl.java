package xyz.qy.implatform.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import xyz.qy.imcommon.contant.IMRedisKey;
import xyz.qy.implatform.contant.Constant;
import xyz.qy.implatform.dto.TalkAddDTO;
import xyz.qy.implatform.dto.TalkDelDTO;
import xyz.qy.implatform.dto.TalkQueryDTO;
import xyz.qy.implatform.dto.TalkUpdateDTO;
import xyz.qy.implatform.entity.GroupMember;
import xyz.qy.implatform.entity.Region;
import xyz.qy.implatform.entity.RegionGroup;
import xyz.qy.implatform.entity.RegionGroupMember;
import xyz.qy.implatform.entity.Talk;
import xyz.qy.implatform.entity.TalkComment;
import xyz.qy.implatform.entity.TalkStar;
import xyz.qy.implatform.entity.TemplateCharacter;
import xyz.qy.implatform.entity.User;
import xyz.qy.implatform.enums.ResultCode;
import xyz.qy.implatform.exception.GlobalException;
import xyz.qy.implatform.mapper.TalkMapper;
import xyz.qy.implatform.service.IFriendService;
import xyz.qy.implatform.service.IGroupMemberService;
import xyz.qy.implatform.service.IRegionGroupMemberService;
import xyz.qy.implatform.service.IRegionGroupService;
import xyz.qy.implatform.service.IRegionService;
import xyz.qy.implatform.service.ITalkCommentService;
import xyz.qy.implatform.service.ITalkService;
import xyz.qy.implatform.service.ITalkStarService;
import xyz.qy.implatform.service.ITemplateCharacterService;
import xyz.qy.implatform.service.IUserService;
import xyz.qy.implatform.session.SessionContext;
import xyz.qy.implatform.session.UserSession;
import xyz.qy.implatform.util.BeanUtils;
import xyz.qy.implatform.util.PageUtils;
import xyz.qy.implatform.util.RedisCache;
import xyz.qy.implatform.util.SensitiveUtil;
import xyz.qy.implatform.vo.PageResultVO;
import xyz.qy.implatform.vo.TalkCommentVO;
import xyz.qy.implatform.vo.TalkStarVO;
import xyz.qy.implatform.vo.TalkVO;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
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
    private ITemplateCharacterService characterService;

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

    @Override
    public void addTalk(TalkAddDTO talkAddDTO) {
        UserSession session = SessionContext.getSession();
        User user = userService.getById(session.getUserId());
        Talk talk = BeanUtils.copyProperties(talkAddDTO, Talk.class);
        assert talk != null;
        talk.setUserId(session.getUserId());
        talk.setCreateBy(session.getUserId());
        talk.setAddress(user.getProvince());
        if (!Objects.isNull(talkAddDTO.getCharacterId())) {
            TemplateCharacter character = characterService.getById(talkAddDTO.getCharacterId());
            if (Objects.isNull(character)) {
                throw new GlobalException("角色不存在");
            }
        }

        talk.setNickName(StringUtils.isNotBlank(talkAddDTO.getNickName()) ? talkAddDTO.getNickName() : user.getNickName());
        if (StringUtils.isBlank(talk.getAvatar())) {
            talk.setAvatar(user.getHeadImage());
        }
        talk.setContent(SensitiveUtil.filter(talk.getContent()));
        if (CollectionUtils.isNotEmpty(talkAddDTO.getFiles())) {
            talk.setFiles(talkAddDTO.getFiles().toJSONString());
        }
        this.baseMapper.insert(talk);
    }

    @Override
    public void updateTalk(TalkUpdateDTO talkUpdateDTO) {
        UserSession session = SessionContext.getSession();
        Long userId = session.getUserId();
        User user = userService.getById(userId);

        Talk talk = this.baseMapper.selectById(talkUpdateDTO.getId());
        if (ObjectUtil.isNotNull(talkUpdateDTO.getCharacterId())) {
            boolean verified = verifyTalkCommentCharacter(talk.getId(), talkUpdateDTO.getCharacterId());
            if (verified) {
                throw new GlobalException("所选角色不允许");
            }
        }
        if (!Objects.isNull(talkUpdateDTO.getCharacterId())) {
            TemplateCharacter character = characterService.getById(talkUpdateDTO.getCharacterId());
            if (Objects.isNull(character)) {
                throw new GlobalException("角色不存在");
            }
        }
        if (!talk.getCategory().equals(talkUpdateDTO.getCategory())) {
            throw new GlobalException("数据异常");
        }
        BeanUtils.copyProperties(talkUpdateDTO, talk);
        talk.setAddress(user.getProvince());
        talk.setUpdateBy(userId);
        talk.setNickName(StringUtils.isNotBlank(talkUpdateDTO.getNickName()) ? talkUpdateDTO.getNickName() : user.getNickName());
        if (StringUtils.isBlank(talk.getAvatar())) {
            talk.setAvatar(user.getHeadImage());
        }
        talk.setContent(SensitiveUtil.filter(talk.getContent()));
        if (CollectionUtils.isNotEmpty(talkUpdateDTO.getFiles())) {
            talk.setFiles(talkUpdateDTO.getFiles().toJSONString());
        }
        this.baseMapper.updateById(talk);
    }

    @Override
    public PageResultVO pageQueryTalkList(TalkQueryDTO queryDTO) {
        TalkQueryDTO dto = new TalkQueryDTO();
        UserSession session = SessionContext.getSession();
        Long myUserId = session.getUserId();

        List<Talk> records = null;
        Page<Talk> talkPage = null;

        // 查询自己和所有好友的
        if ("my-friends".equals(queryDTO.getSection())) {
            dto.setOwnerId(myUserId);
            // 查询获取好友用户id
            List<Long> friendIds = friendService.getFriendIdsByUserId(myUserId);
            dto.setFriendIds(friendIds);
            talkPage = this.baseMapper.pageQueryMyAndFriendsTalkList(new Page<>(PageUtils.getPageNo(), PageUtils.getPageSize()), dto);
            if (ObjectUtil.isNotNull(talkPage)) {
                records = talkPage.getRecords();
            }
        } else if ("my".equals(queryDTO.getSection())) {// 查询自己的
            dto.setOwnerId(myUserId);
            talkPage = this.baseMapper.pageQueryMyTalkList(new Page<>(PageUtils.getPageNo(), PageUtils.getPageSize()), dto);
            if (ObjectUtil.isNotNull(talkPage)) {
                records = talkPage.getRecords();
            }
        } else if ("friends".equals(queryDTO.getSection())) {// 查询所有好友的
            // 查询获取好友用户id
            List<Long> friendIds = friendService.getFriendIdsByUserId(myUserId);
            dto.setFriendIds(friendIds);
            talkPage = this.baseMapper.pageQueryFriendsTalkList(new Page<>(PageUtils.getPageNo(), PageUtils.getPageSize()), dto);
            if (ObjectUtil.isNotNull(talkPage)) {
                records = talkPage.getRecords();
            }
        } else if ("friend".equals(queryDTO.getSection())) {// 查询单个好友的
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
        } else if ("group".equals(queryDTO.getSection())) {
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
        } else if ("my-region".equals(queryDTO.getSection())) {
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
        } else if ("region".equals(queryDTO.getSection())) {
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
        }
        if (CollectionUtils.isEmpty(records)) {
            return PageResultVO.builder().data(Collections.emptyList()).build();
        }
        // 查询当前用户数据
        User user = userService.getById(myUserId);

        // 动态id
        List<Long> talkIds = records.stream().map(Talk::getId).collect(Collectors.toList());

        // 动态赞星数据
        List<TalkStar> talkStarList = talkStarService.lambdaQuery().in(TalkStar::getTalkId, talkIds)
                .eq(TalkStar::getDeleted, false)
                .orderByAsc(TalkStar::getCreateTime)
                .list();

        // 动态评论数据(包含删除的)
        List<TalkComment> allTalkCommentList = talkCommentService.lambdaQuery().in(TalkComment::getTalkId, talkIds)
                .orderByAsc(TalkComment::getCreateTime).list();
        // 根据动态id分组
        Map<Long, List<TalkComment>> allTalkCommentGroupMap = allTalkCommentList.stream().collect(Collectors.groupingBy(TalkComment::getTalkId));

        // 动态评论数据-未删除
        List<TalkComment> talkCommentList = allTalkCommentList.stream().filter(item -> !item.getDeleted()).collect(Collectors.toList());

        List<TalkStarVO> talkStarVOS = BeanUtils.copyProperties(talkStarList, TalkStarVO.class);

        List<TalkCommentVO> talkCommentVOS = BeanUtils.copyProperties(talkCommentList, TalkCommentVO.class);

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
                    talkVO.setCommentCharacterId(talkVO.getCharacterId());
                    talkVO.setCommentCharacterName(talkVO.getNickName());
                    talkVO.setCommentCharacterAvatar(talkVO.getAvatar());
                }
            }
            if (talkStarMap.containsKey(talkVO.getId())) {
                talkVO.setTalkStarVOS(talkStarMap.get(talkVO.getId()));
                // 找到当前用户点赞，并且角色id不为空的数据
                Optional<TalkStarVO> talkStarVOOptional = talkVO.getTalkStarVOS().stream().filter(item -> item.getUserId().equals(myUserId)
                        && ObjectUtil.isNotNull(item.getCharacterId())).findFirst();
                talkStarVOOptional.ifPresent(talkStarVO -> {
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

        if (ObjectUtil.isNotNull(talk.getCharacterId())) {
            TemplateCharacter character = characterService.getById(talk.getCharacterId());
            if (ObjectUtil.isNotNull(character)) {
                talkVO.setCommentCharacterId(character.getId());
                talkVO.setCommentCharacterName(character.getName());
                talkVO.setCommentCharacterAvatar(character.getAvatar());
                hasCharacter = true;
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

        if (!hasCharacter) {
            talkVO.setEnableCharacterChoose(Boolean.TRUE);
        }

        return talkVO;
    }

    @Override
    public boolean verifyTalkCommentCharacter(Long talkId, Long characterId) {
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

        // 自己选择的角色与其他人选择的角色一样
        if (!userId.equals(talk.getUserId())
                && !Objects.isNull(talk.getCharacterId())) {
            if (talk.getCharacterId().equals(characterId)) {
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

            // 自己选择过的角色与入参的角色不一致
            Optional<TalkStar> optional2 = talkStarList.stream().filter(item -> !Objects.isNull(item.getCharacterId())
                    && !characterId.equals(item.getCharacterId())
                    && item.getUserId().equals(userId)).findFirst();
            if (optional2.isPresent()) {
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

            // 自己选择过的角色与入参的角色不一致
            Optional<TalkComment> optional2 = talkCommentList.stream().filter(item -> !Objects.isNull(item.getCharacterId())
                    && !characterId.equals(item.getCharacterId()) &&
                    item.getUserId().equals(userId)).findFirst();
            if (optional2.isPresent()) {
                return true;
            }
        }
        return false;
    }
}

package xyz.qy.implatform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import xyz.qy.implatform.dto.FollowDTO;
import xyz.qy.implatform.entity.Follow;
import xyz.qy.implatform.entity.Group;
import xyz.qy.implatform.entity.TemplateCharacter;
import xyz.qy.implatform.entity.TemplateGroup;
import xyz.qy.implatform.entity.User;
import xyz.qy.implatform.enums.FollowEnum;
import xyz.qy.implatform.exception.GlobalException;
import xyz.qy.implatform.mapper.FollowMapper;
import xyz.qy.implatform.mapper.GroupMapper;
import xyz.qy.implatform.mapper.TemplateCharacterMapper;
import xyz.qy.implatform.mapper.TemplateGroupMapper;
import xyz.qy.implatform.mapper.UserMapper;
import xyz.qy.implatform.service.ICharacterAvatarService;
import xyz.qy.implatform.service.IFollowService;
import xyz.qy.implatform.session.SessionContext;
import xyz.qy.implatform.session.UserSession;
import xyz.qy.implatform.util.BeanUtils;
import xyz.qy.implatform.vo.CharacterAvatarVO;
import xyz.qy.implatform.vo.FollowVO;
import xyz.qy.implatform.vo.TemplateCharacterVO;
import xyz.qy.implatform.vo.TemplateGroupVO;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FollowServiceImpl extends ServiceImpl<FollowMapper, Follow> implements IFollowService {
    @Resource
    private UserMapper userMapper;

    @Resource
    private GroupMapper groupMapper;

    @Resource
    private TemplateCharacterMapper characterMapper;

    @Resource
    private TemplateGroupMapper templateGroupMapper;

    @Resource
    private ICharacterAvatarService characterAvatarService;

    @Override
    public void addFollow(FollowDTO dto) {
        UserSession session = SessionContext.getSession();
        Long userId = session.getUserId();

        // 校验是否已关注
        LambdaQueryWrapper<Follow> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Follow::getUserId, userId)
                .eq(Follow::getTargetId, dto.getTargetId())
                .eq(Follow::getType, dto.getType());
        if (this.count(queryWrapper) > 0) {
            throw new GlobalException("已关注该目标!");
        }

        Follow follow = new Follow();
        follow.setTargetId(dto.getTargetId());
        follow.setType(dto.getType());
        follow.setUserId(userId);
        follow.setCreateTime(new Date());
        this.save(follow);
    }

    @Override
    public void cancelFollow(Long targetId, String type) {
        UserSession session = SessionContext.getSession();
        Long userId = session.getUserId();

        LambdaQueryWrapper<Follow> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Follow::getUserId, userId)
                .eq(Follow::getTargetId, targetId)
                .eq(Follow::getType, type);
        this.remove(queryWrapper);
    }

    @Override
    public List<FollowVO> findFollows(String type) {
        UserSession session = SessionContext.getSession();
        Long userId = session.getUserId();

        LambdaQueryWrapper<Follow> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Follow::getUserId, userId)
                .eq(StringUtils.isNotBlank(type), Follow::getType, type)
                .orderByDesc(Follow::getCreateTime);
        List<Follow> list = this.list(queryWrapper);
        List<FollowVO> followVOS = BeanUtils.copyProperties(list, FollowVO.class);

        // 按关注类型分组收集目标ID，批量查询
        Map<String, List<Long>> typeTargetIdsMap = followVOS.stream()
                .collect(Collectors.groupingBy(FollowVO::getType,
                        Collectors.mapping(FollowVO::getTargetId, Collectors.toList())));

        // 批量查询用户
        List<Long> userIds = typeTargetIdsMap.get(FollowEnum.USER.getCode());
        if (CollectionUtils.isNotEmpty(userIds)) {
            Map<Long, User> userMap = userMapper.selectBatchIds(userIds).stream()
                    .collect(Collectors.toMap(User::getId, u -> u));
            followVOS.stream()
                    .filter(vo -> FollowEnum.USER.getCode().equals(vo.getType()))
                    .forEach(vo -> {
                        User user = userMap.get(vo.getTargetId());
                        if (user != null) {
                            vo.setTargetName(user.getNickName());
                            vo.setTargetAvatar(user.getHeadImage());
                        }
                    });
        }

        // 批量查询群组
        List<Long> groupIds = typeTargetIdsMap.get(FollowEnum.GROUP.getCode());
        if (CollectionUtils.isNotEmpty(groupIds)) {
            Map<Long, Group> groupMap = groupMapper.selectBatchIds(groupIds).stream()
                    .collect(Collectors.toMap(Group::getId, g -> g));
            followVOS.stream()
                    .filter(vo -> FollowEnum.GROUP.getCode().equals(vo.getType()))
                    .forEach(vo -> {
                        Group group = groupMap.get(vo.getTargetId());
                        if (group != null) {
                            vo.setTargetName(group.getName());
                            vo.setTargetAvatar(group.getHeadImage());
                        }
                    });
        }


        // 批量查询角色
        List<Long> characterIds = typeTargetIdsMap.get(FollowEnum.CHARACTER.getCode());
        if (CollectionUtils.isNotEmpty(characterIds)) {
            List<CharacterAvatarVO> characterAvatars = characterAvatarService.queryPublishCharacterAvatarByCharacterIds(characterIds);
            // characterAvatars根据characterId分组得到Map<Long, List<CharacterAvatarVO>>
            Map<Long, List<CharacterAvatarVO>> characterAvatarMap = characterAvatars.stream().collect(Collectors.groupingBy(CharacterAvatarVO::getTemplateCharacterId));

            Map<Long, TemplateCharacter> characterMap = characterMapper.selectBatchIds(characterIds).stream()
                    .collect(Collectors.toMap(TemplateCharacter::getId, c -> c));
            followVOS.stream()
                    .filter(vo -> FollowEnum.CHARACTER.getCode().equals(vo.getType()))
                    .forEach(vo -> {
                        TemplateCharacter character = characterMap.get(vo.getTargetId());
                        if (character != null) {
                            vo.setTargetName(character.getName());
                            vo.setTargetAvatar(character.getAvatar());
                            vo.setCharacter(BeanUtils.copyProperties(character, TemplateCharacterVO.class));
                        }
                        List<CharacterAvatarVO> characterAvatarVOList = characterAvatarMap.get(vo.getTargetId());
                        vo.setCharacterAvatars(characterAvatarVOList);
                    });
        }

        // 批量查询群聊模板
        List<Long> templateIds = typeTargetIdsMap.get(FollowEnum.TEMPLATE.getCode());
        if (CollectionUtils.isNotEmpty(templateIds)) {
            Map<Long, TemplateGroup> templateGroupMap = templateGroupMapper.selectBatchIds(templateIds).stream()
                    .collect(Collectors.toMap(TemplateGroup::getId, t -> t));
            followVOS.stream()
                    .filter(vo -> FollowEnum.TEMPLATE.getCode().equals(vo.getType()))
                    .forEach(vo -> {
                        TemplateGroup templateGroup = templateGroupMap.get(vo.getTargetId());
                        if (templateGroup != null) {
                            vo.setTargetName(templateGroup.getGroupName());
                            vo.setTargetAvatar(templateGroup.getAvatar());
                            vo.setTemplateGroup(BeanUtils.copyProperties(templateGroup, TemplateGroupVO.class));
                        }
                    });
        }

        return followVOS;
    }

    @Override
    public List<FollowVO> findMyFans() {
        UserSession session = SessionContext.getSession();
        Long userId = session.getUserId();

        LambdaQueryWrapper<Follow> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Follow::getTargetId, userId)
                .eq(Follow::getType, FollowEnum.USER.getCode())
                .orderByDesc(Follow::getCreateTime);
        List<Follow> list = this.list(queryWrapper);
        List<FollowVO> followVOS = BeanUtils.copyProperties(list, FollowVO.class);

        LambdaQueryWrapper<Follow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Follow::getUserId, userId)
                .eq(Follow::getType, FollowEnum.USER.getCode());
        List<Follow> myFollows = this.list(wrapper);
        Set<Long> myFollowIds = myFollows.stream().map(Follow::getTargetId).collect(Collectors.toSet());


        List<Long> userIds = followVOS.stream().map(FollowVO::getUserId).collect(Collectors.toList());
        Map<Long, User> userMap = userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));
        followVOS.forEach(vo -> {
            User user = userMap.get(vo.getUserId());
            if (user != null) {
                vo.setNickName(user.getNickName());
                vo.setHeadImage(user.getHeadImage());
            }

            // 判断是否已关注粉丝
            vo.setFollowed(myFollowIds.contains(vo.getUserId()));
        });
        return followVOS;
    }

    @Override
    public Set<String> findAllFollows() {
        UserSession session = SessionContext.getSession();
        Long userId = session.getUserId();

        LambdaQueryWrapper<Follow> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Follow::getUserId, userId);
        queryWrapper.select(Follow::getTargetId, Follow::getType);
        List<Follow> list = this.list(queryWrapper);
        return list.stream().map(follow -> follow.getTargetId() + ":" + follow.getType()).collect(Collectors.toSet());
    }

    @Override
    public Set<Long> findFollowsTargetIds(String type) {
        if (StringUtils.isBlank(type)) {
            return Set.of();
        }
        List<FollowVO> follows = this.findFollows(type);
        return follows.stream()
                .map(FollowVO::getTargetId)
                .collect(Collectors.toSet());
    }

    @Override
    public Boolean isFollow(Long userId, Long targetId, String type) {
        return this.lambdaQuery()
                .eq(Follow::getUserId, userId)
                .eq(Follow::getTargetId, targetId)
                .eq(Follow::getType, type)
                .count() > 0;
    }
}

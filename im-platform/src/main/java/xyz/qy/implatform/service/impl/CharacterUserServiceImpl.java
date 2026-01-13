package xyz.qy.implatform.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xyz.qy.implatform.dto.CharacterUserBindDTO;
import xyz.qy.implatform.entity.CharacterUser;
import xyz.qy.implatform.entity.TemplateCharacter;
import xyz.qy.implatform.entity.User;
import xyz.qy.implatform.mapper.CharacterUserMapper;
import xyz.qy.implatform.service.ICharacterAvatarService;
import xyz.qy.implatform.service.ICharacterUserService;
import xyz.qy.implatform.service.ITemplateCharacterService;
import xyz.qy.implatform.service.IUserService;
import xyz.qy.implatform.session.SessionContext;
import xyz.qy.implatform.session.UserSession;
import xyz.qy.implatform.util.BeanUtils;
import xyz.qy.implatform.vo.CharacterAvatarVO;
import xyz.qy.implatform.vo.CharacterUserVO;
import xyz.qy.implatform.vo.TemplateCharacterVO;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CharacterUserServiceImpl extends ServiceImpl<CharacterUserMapper, CharacterUser> implements ICharacterUserService {
    @Resource
    private ITemplateCharacterService templateCharacterService;

    @Resource
    private ICharacterAvatarService characterAvatarService;

    @Resource
    private IUserService userService;

    @Override
    public List<Long> getUserIdListByCharacterIds(List<Long> characterIds) {
        List<CharacterUser> characterUsers = this.lambdaQuery()
                .in(CharacterUser::getCharacterId, characterIds)
                .eq(CharacterUser::getDeleted, false)
                .select(CharacterUser::getUserId)
                .list();
        return characterUsers.stream().map(CharacterUser::getUserId).collect(Collectors.toList());
    }

    @Override
    public List<CharacterUserVO> getCharacterUsersByCharacterId(Long characterId) {
        List<CharacterUser> characterUsers = this.lambdaQuery()
                .eq(CharacterUser::getCharacterId, characterId)
                .eq(CharacterUser::getDeleted, false)
                .list();
        if (CollUtil.isEmpty(characterUsers)) {
            return Collections.emptyList();
        }

        List<Long> userIds = characterUsers.stream().map(CharacterUser::getUserId).collect(Collectors.toList());
        List<User> userList = userService.findUserByIds(userIds);
        // userList根据id分组得到Map<Long, User>
        Map<Long, User> userMap = userList.stream().collect(Collectors.toMap(User::getId, user -> user));

        return characterUsers.stream().map(characterUser -> {
            CharacterUserVO vo = new CharacterUserVO();
            vo.setId(characterUser.getId());
            vo.setCharacterId(characterUser.getCharacterId());
            vo.setUserId(characterUser.getUserId());
            
            User user = userMap.get(characterUser.getUserId());
            if (user != null) {
                vo.setNickName(user.getNickName());
                vo.setHeadImage(user.getHeadImage());
                vo.setUserName(user.getUserName());
            }
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public List<CharacterUserVO> getMyCharacters() {
        UserSession session = SessionContext.getSession();
        Long userId = session.getUserId();
        List<CharacterUser> characterUsers = this.lambdaQuery()
                .eq(CharacterUser::getUserId, userId)
                .eq(CharacterUser::getDeleted, false)
                .list();
        if (CollUtil.isEmpty(characterUsers)) {
            return Collections.emptyList();
        }
        List<Long> characterIds = characterUsers.stream().map(CharacterUser::getCharacterId).collect(Collectors.toList());
        List<TemplateCharacter> templateCharacterList = templateCharacterService.findPublishedByCharacterIds(characterIds);

        List<CharacterAvatarVO> characterAvatars = characterAvatarService.queryPublishCharacterAvatarByCharacterIds(characterIds);
        // characterAvatars根据characterId分组得到Map<Long, List<CharacterAvatarVO>>
        Map<Long, List<CharacterAvatarVO>> characterAvatarMap = characterAvatars.stream().collect(Collectors.groupingBy(CharacterAvatarVO::getTemplateCharacterId));

        // templateCharacterList根据id分组得到Map<Long, TemplateCharacter>
        Map<Long, TemplateCharacter> characterMap = templateCharacterList.stream().collect(Collectors.toMap(TemplateCharacter::getId, character -> character));
        return characterUsers.stream().map(characterUser -> {
            CharacterUserVO vo = new CharacterUserVO();
            vo.setId(characterUser.getId());
            vo.setCharacterId(characterUser.getCharacterId());
            vo.setUserId(characterUser.getUserId());
            TemplateCharacter character = characterMap.get(characterUser.getCharacterId());
            if (character != null) {
                vo.setCharacterName(character.getName());
                vo.setCharacterAvatar(character.getAvatar());
                TemplateCharacterVO templateCharacterVO = BeanUtils.copyProperties(character, TemplateCharacterVO.class);
                assert templateCharacterVO != null;
                templateCharacterVO.setIsOwner(true);
                vo.setCharacter(templateCharacterVO);
            }
            vo.setCharacterAvatars(characterAvatarMap.getOrDefault(characterUser.getCharacterId(), Collections.emptyList()));
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public List<Long> getMyCharacterIds() {
        UserSession session = SessionContext.getSession();
        Long userId = session.getUserId();
        List<CharacterUser> characterUsers = this.lambdaQuery()
                .eq(CharacterUser::getUserId, userId)
                .eq(CharacterUser::getDeleted, false)
                .list();
        if (CollUtil.isEmpty(characterUsers)) {
            return Collections.emptyList();
        }
        return characterUsers.stream().map(CharacterUser::getCharacterId).collect(Collectors.toList());
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void bindCharacterUser(CharacterUserBindDTO dto) {
        UserSession session = SessionContext.getSession();

        TemplateCharacter character = templateCharacterService.getById(dto.getCharacterId());
        if (ObjectUtil.isNull(character)) {
            throw new RuntimeException("角色不存在");
        }
        if (!character.getCreateBy().equals(String.valueOf(session.getUserId()))) {
            throw new RuntimeException("无权限操作");
        }

        dto.setUserIds(dto.getUserIds().stream().distinct().collect(Collectors.toList()));
        
        // 判断用户是否存在
        List<User> userList = userService.findUserByIds(dto.getUserIds());
        Map<Long, User> userMap = userList.stream().collect(Collectors.toMap(User::getId, user -> user));


        LambdaQueryWrapper<CharacterUser> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(CharacterUser::getCharacterId, dto.getCharacterId());
        wrapper.in(CharacterUser::getUserId, dto.getUserIds());

        List<CharacterUser> characterUsers = this.list(wrapper);
        List<CharacterUser> characterUserList = dto.getUserIds().stream().map(userId -> {
            User user = userMap.get(userId);
            if (ObjectUtil.isNull(user)) {
                log.info("用户不存在：{}", userId);
                throw new RuntimeException("用户不存在:" + userId);
            }
            Optional<CharacterUser> optional = characterUsers.stream().filter(characterUser -> characterUser.getUserId().equals(userId)).findFirst();
            CharacterUser item = optional.orElseGet(CharacterUser::new);
            item.setCharacterId(dto.getCharacterId());
            item.setUserId(userId);
            item.setCreateBy(session.getUserId());
            item.setDeleted(false);
            return item;
        }).collect(Collectors.toList());

        this.saveOrUpdateBatch(characterUserList);
    }

    @Override
    public void unbindCharacterUser(CharacterUserBindDTO dto) {
        UserSession session = SessionContext.getSession();

        TemplateCharacter character = templateCharacterService.getById(dto.getCharacterId());
        if (ObjectUtil.isNull(character)) {
            throw new RuntimeException("角色不存在");
        }
        if (!character.getCreateBy().equals(String.valueOf(session.getUserId()))) {
            throw new RuntimeException("无权限操作");
        }

        LambdaUpdateWrapper<CharacterUser> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(CharacterUser::getCharacterId, dto.getCharacterId());
        wrapper.in(CharacterUser::getUserId, dto.getUserIds());
        wrapper.set(CharacterUser::getUpdateBy, session.getUserId());
        wrapper.set(CharacterUser::getUpdateTime, DateUtil.date());
        wrapper.set(CharacterUser::getDeleted, true);
        this.update(wrapper);
    }
}

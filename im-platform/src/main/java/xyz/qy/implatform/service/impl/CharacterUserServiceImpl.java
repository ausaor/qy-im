package xyz.qy.implatform.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xyz.qy.implatform.dto.CharacterUserBindDTO;
import xyz.qy.implatform.entity.CharacterUser;
import xyz.qy.implatform.entity.TemplateCharacter;
import xyz.qy.implatform.mapper.CharacterUserMapper;
import xyz.qy.implatform.service.ICharacterUserService;
import xyz.qy.implatform.service.ITemplateCharacterService;
import xyz.qy.implatform.session.SessionContext;
import xyz.qy.implatform.session.UserSession;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CharacterUserServiceImpl extends ServiceImpl<CharacterUserMapper, CharacterUser> implements ICharacterUserService {
    @Resource
    private ITemplateCharacterService templateCharacterService;

    @Override
    public List<Long> getUserIdListByCharacterId(List<Long> characterIds) {
        List<CharacterUser> characterUsers = this.lambdaQuery()
                .in(CharacterUser::getCharacterId, characterIds)
                .eq(CharacterUser::getDeleted, false)
                .select(CharacterUser::getUserId)
                .list();
        return characterUsers.stream().map(CharacterUser::getUserId).collect(Collectors.toList());
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

        LambdaQueryWrapper<CharacterUser> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(CharacterUser::getCharacterId, dto.getCharacterId());
        wrapper.in(CharacterUser::getUserId, dto.getUserIds());

        List<CharacterUser> characterUsers = this.list(wrapper);
        List<CharacterUser> characterUserList = dto.getUserIds().stream().map(userId -> {
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
        wrapper.set(CharacterUser::getDeleted, true);
        this.update(wrapper);
    }
}

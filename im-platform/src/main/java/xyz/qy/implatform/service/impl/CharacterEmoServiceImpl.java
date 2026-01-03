package xyz.qy.implatform.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xyz.qy.implatform.contant.Constant;
import xyz.qy.implatform.dto.CharacterEmoDTO;
import xyz.qy.implatform.dto.CharacterEmoSaveDTO;
import xyz.qy.implatform.entity.CharacterEmo;
import xyz.qy.implatform.entity.TemplateCharacter;
import xyz.qy.implatform.entity.TemplateGroup;
import xyz.qy.implatform.enums.ReviewEnum;
import xyz.qy.implatform.exception.GlobalException;
import xyz.qy.implatform.mapper.CharacterEmoMapper;
import xyz.qy.implatform.service.ICharacterEmoService;
import xyz.qy.implatform.service.ICharacterUserService;
import xyz.qy.implatform.service.ITemplateCharacterService;
import xyz.qy.implatform.service.ITemplateGroupService;
import xyz.qy.implatform.session.SessionContext;
import xyz.qy.implatform.session.UserSession;
import xyz.qy.implatform.util.BeanUtils;
import xyz.qy.implatform.vo.CharacterEmoVO;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CharacterEmoServiceImpl extends ServiceImpl<CharacterEmoMapper, CharacterEmo> implements ICharacterEmoService {
    @Resource
    private ITemplateCharacterService characterService;

    @Resource
    private ITemplateGroupService templateGroupService;

    @Resource
    private ITemplateCharacterService templateCharacterService;

    @Resource
    private ICharacterUserService characterUserService;

    @Override
    public JSONObject findPublishedEmoByCharacterId(Long characterId) {
        JSONObject jsonObject = new JSONObject();

        TemplateCharacter character = characterService.getById(characterId);

        List<CharacterEmo> groupEmos = this.lambdaQuery()
                .eq(CharacterEmo::getCharacterId, -1)
                .eq(CharacterEmo::getTemplateGroupId, character.getTemplateGroupId())
                .eq(CharacterEmo::getStatus, ReviewEnum.REVIEWED.getCode())
                .list();
        List<CharacterEmoVO> groupEmoVOS = BeanUtils.copyProperties(groupEmos, CharacterEmoVO.class);

        List<CharacterEmo> characterEmos = this.lambdaQuery()
                .eq(CharacterEmo::getCharacterId, characterId)
                .eq(CharacterEmo::getTemplateGroupId, character.getTemplateGroupId())
                .eq(CharacterEmo::getStatus, ReviewEnum.REVIEWED.getCode())
                .list();
        List<CharacterEmoVO> characterEmoVOS = BeanUtils.copyProperties(characterEmos, CharacterEmoVO.class);
        jsonObject.put("group", groupEmoVOS);
        jsonObject.put("character", characterEmoVOS);

        return jsonObject;
    }

    @Override
    public List<CharacterEmoVO> findCharacterEmos(CharacterEmoDTO dto) {
        UserSession session = SessionContext.getSession();
        Long userId = session.getUserId();
        LambdaQueryWrapper<CharacterEmo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CharacterEmo::getTemplateGroupId, dto.getTemplateGroupId());
        if (ObjectUtil.isNull(dto.getCharacterId())) {
            queryWrapper.eq(CharacterEmo::getCharacterId, -1);
        } else {
            queryWrapper.eq(CharacterEmo::getCharacterId, dto.getCharacterId());
        }
        queryWrapper.eq(CharacterEmo::getDeleted, Constant.NO);

        List<CharacterEmo> characterEmos = this.list(queryWrapper);
        List<CharacterEmoVO> characterEmoVOS = BeanUtils.copyPropertiesList(characterEmos, CharacterEmoVO.class);
        characterEmoVOS.forEach(item -> {
            if (userId.equals(item.getCreateBy())) {
                item.setIsOwner(true);
            }
        });

        return characterEmoVOS;
    }

    @Override
    public void delete(Long id) {
        UserSession session = SessionContext.getSession();
        Long userId = session.getUserId();

        CharacterEmo characterEmo = this.getById(id);
        if (!userId.equals(characterEmo.getCreateBy())) {
            throw new GlobalException("您不是创建人");
        }
        characterEmo.setDeleted(true);
        this.updateById(characterEmo);
    }

    @Transactional
    @Override
    public void save(CharacterEmoSaveDTO dto) {
        UserSession session = SessionContext.getSession();
        Long userId = session.getUserId();

        if (CollectionUtils.isEmpty(dto.getEmos())) {
            throw new GlobalException("台词不能为空");
        }
        if (dto.getEmos().size() > 50) {
            throw new GlobalException("单个角色表情数量不能超过50");
        }

        // 判断群聊模板是否存在
        TemplateGroup templateGroup = templateGroupService.getById(dto.getTemplateGroupId());
        if (ObjectUtil.isNull(templateGroup) || templateGroup.getDeleted()) {
            throw new GlobalException("群聊模板不存在");
        }

        boolean hasAuth = false;
        if (userId.toString().equals(templateGroup.getCreateBy())) {
            hasAuth = true;
        }

        String characterName;
        // 判断角色是否存在
        if (ObjectUtil.isNotNull(dto.getCharacterId())) {
            TemplateCharacter templateCharacter = templateCharacterService.lambdaQuery()
                    .eq(TemplateCharacter::getTemplateGroupId, dto.getTemplateGroupId())
                    .eq(TemplateCharacter::getId, dto.getCharacterId())
                    .eq(TemplateCharacter::getDeleted, false)
                    .one();
            if (ObjectUtil.isNull(templateCharacter)) {
                throw new GlobalException("角色不存在");
            }

            List<Long> myCharacterIds = characterUserService.getMyCharacterIds();

            if (userId.toString().equals(templateCharacter.getCreateBy()) || myCharacterIds.contains(dto.getCharacterId())) {
                hasAuth = true;
            }
            characterName = templateCharacter.getName();
        } else {
            characterName = templateGroup.getGroupName();
        }
        if (!hasAuth) {
            throw new GlobalException("您没有权限");
        }


        Long characterId = ObjectUtil.isNotNull(dto.getCharacterId()) ? dto.getCharacterId() : -1;

        List<CharacterEmoDTO> emoDTOList = dto.getEmos().stream().filter(item -> StringUtils.isNotBlank(item.getUrl())).collect(Collectors.toList());

        List<CharacterEmo> characterEmos = BeanUtils.copyPropertiesList(emoDTOList, CharacterEmo.class);

        characterEmos.forEach(item -> {
            item.setCharacterName(characterName);
            item.setCharacterId(characterId);
            item.setTemplateGroupId(dto.getTemplateGroupId());
            item.setStatus(ReviewEnum.REVIEWED.getCode());
            item.setCreateBy(userId);
            item.setUpdateBy(userId);
        });

        this.saveOrUpdateBatch(characterEmos);
    }
}
